// Copyright 2017 SUSE LINUX GmbH, Nuernberg, Germany.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
import com.suse.kubic.Environment

def call(Map parameters = [:], Closure preBootstrapBody = null, Closure body) {
    def nodeLabel = parameters.get('nodeLabel', 'leap15.0&&caasp-pr-worker')
    def environmentType = parameters.get('environmentType', 'caasp-kvm')
    def environmentTypeOptions = parameters.get('environmentTypeOptions', null)
    boolean environmentDestroy = parameters.get('environmentDestroy', true)
    boolean retrieveSupportconfigOnlyOnFailure = parameters.get('retrieveSupportconfigOnlyOnFailure', false)
    def gitBase = parameters.get('gitBase', 'https://github.com/kubic-project')
    def gitBranch = parameters.get('gitBranch', env.getEnvironment().get('CHANGE_TARGET', env.BRANCH_NAME))
    def gitCredentialsId = parameters.get('gitCredentialsId', 'github-token')
    boolean gitIgnorePullRequest = parameters.get('gitIgnorePullRequest', false)
    int masterCount = parameters.get('masterCount', 3)
    int workerCount = parameters.get('workerCount', 2)
    boolean chooseCrio = parameters.get('chooseCrio', false)
    boolean writeLogsToDb = parameters.get('writeLogsToDb', true)

    echo "Creating Kubic Environment"

    // Allocate a node
    node (nodeLabel) {
        // Show some info about the node were running on
        stage('Node Info') {
            echo "Node: ${env.NODE_NAME}"
            echo "Workspace: ${env.WORKSPACE}"
            sh(script: 'env | sort')
            sh(script: 'ip a')
            sh(script: 'ip r')
            sh(script: 'cat /etc/resolv.conf')
            def response = httpRequest(url: 'http://169.254.169.254/latest/meta-data/public-ipv4')
            echo "Public IPv4: ${response.content}"
        }

        // Cleanup host before run
        stage('Cleanup') {
            // Delete any leftover workspace and create a new one
            sh(script: 'rm -rf ${WORKSPACE}; mkdir -p ${WORKSPACE}/logs')
            sh(script: 'chmod a+x ${WORKSPACE} || : ')
            sh(script: 'virsh net-undefine caasp-dev-net || : ')
            sh(script: 'virsh net-destroy caasp-dev-net || : ')
            sh(script: 'virsh net-undefine net || : ')
            sh(script: 'virsh net-destroy net || : ')
            sh(script: 'for i in $(virsh list --all --name);do echo $i;virsh destroy $i || : ;done')
            sh(script: 'for i in $(virsh list --all --name);do echo $i;virsh undefine $i;done')
            sh(script: 'for fn in $(virsh vol-list default|awk \'/var/ {print $2}\'); do echo $fn; virsh vol-delete $fn ; done')
            sh(script: 'virsh list --all ; virsh net-list --all ; virsh pool-list --all; virsh vol-list default')
            sh(script: 'docker rm -f $(docker ps -a -q) || :')
            sh(script: 'docker system prune --all --force --volumes || :')
        }

        // Fetch the necessary code
        stage('Retrieve Code') {
            cloneAllKubicRepos(gitBase: gitBase, branch: gitBranch, credentialsId: gitCredentialsId, ignorePullRequest: gitIgnorePullRequest)
        }

        // Fetch the necessary images
        stage('Retrieve Image') {
            environmentTypeOptions = prepareImage(
                type: environmentType,
                typeOptions: environmentTypeOptions
            )
        }

        Environment environment;

        boolean buildFailure = false
        try {
            // Create the Kubic environment
            stage('Create Environment') {
                environment = createEnvironment(
                    type: environmentType,
                    typeOptions: environmentTypeOptions,
                    masterCount: masterCount,
                    workerCount: workerCount
                )
            }

            stage('Deploy CI Tools') {
                // Install Netdata on admin host
                sh(script: "set -o pipefail; ${WORKSPACE}/automation/misc-tools/netdata/install admin | tee ${WORKSPACE}/logs/netdata-install-admin.log")
            }

            if (preBootstrapBody != null) {
                // Prepare the body closure delegate
                def delegate = [:]
                // Set some context variables available inside the preBootstrapBody() method
                delegate['environment'] = environment
                delegate['environmentTypeOptions'] = environmentTypeOptions
                preBootstrapBody.delegate = delegate

                // Execute the preBootstrapBody of the test
                def preBootstrapBodyResult = preBootstrapBody()
                if (preBootstrapBodyResult instanceof Environment) {
                    // TODO: Update closures to always return the environment, to
                    // handle cases where the closure modify the environment.
                    environment = preBootstrapBodyResult
                }
            }

            // Configure the Kubic environment
            stage('Configure Environment') {
                configureEnvironment(environment: environment, chooseCrio: chooseCrio)
            }

            // Create Workers
            stage('Create Environment Workers') {
                environment = createEnvironmentWorkers(
                    environment: environment,
                    type: environmentType,
                    typeOptions: environmentTypeOptions,
                    masterCount: masterCount,
                    workerCount: workerCount
                )
            }

            // Bootstrap the Kubic environment
            // and fetch ${WORKSPACE}/kubeconfig
            stage('Bootstrap Environment') {
                environment = bootstrapEnvironment(environment: environment)
            }

            // Prepare the body closure delegate
            def delegate = [:]
            // Set some context variables available inside the body() method
            delegate['environment'] = environment
            delegate['environmentTypeOptions'] = environmentTypeOptions
            body.delegate = delegate

            // Execute the body of the test
            def bodyResult = body()
            if (bodyResult instanceof Environment) {
                // TODO: Update closures to always return the environment, to
                // handle cases where the closure modify the environment.
                environment = bodyResult
            }
        } catch (Exception exc) {
            buildFailure = true
            throw exc
        } finally {
            if (buildFailure || !retrieveSupportconfigOnlyOnFailure) {
                // Gather Netdata metrics and generate charts
                stage('Gather Netdata metrics') {
                  netdataCaptureCharts()
                }

                // Gather logs from the environment
                stage('Gather Logs') {
                    try {
                        gatherKubicLogs(environment: environment)
                    } catch (Exception exc) {
                        // TODO: Figure out if we can mark this stage as failed, while allowing the remaining stages to proceed.
                        echo "Failed to Gather Logs"
                    }
                }
            }

            // Destroy the Kubic Environment
            stage('Destroy Environment') {
                if (environmentDestroy) {
                    try {
                        cleanupEnvironment(
                            type: environmentType,
                            typeOptions: environmentTypeOptions,
                            masterCount: masterCount,
                            workerCount: workerCount
                        )
                    } catch (Exception exc) {
                        echo "Failed to Destroy Environment."
                    }
                } else {
                    echo "Skipping Destroy Environment as requested"
                    offlineJenkinsSlave(message: "Marked offline by ${env.BUILD_URL} due to user request")
                }
            }

            if (buildFailure || !retrieveSupportconfigOnlyOnFailure) {
                // Archive the logs
                stage('Archive Logs') {
                    try {
                        archiveArtifacts(artifacts: 'logs/**', fingerprint: true)
                        archiveArtifacts(artifacts: 'netdata/**', fingerprint: true)
                    } catch (Exception exc) {
                        // TODO: Figure out if we can mark this stage as failed, while allowing the remaining stages to proceed.
                        echo "Failed to Archive Logs"
                    }

                    if (writeLogsToDb) {
                        echo "Writing logs to database"
                        try {
                            withCredentials([string(credentialsId: 'database-host', variable: 'DBHOST')]) {
                                withCredentials([string(credentialsId: 'database-password', variable: 'DBPASS')]) {
                                    String status = currentBuild.currentResult
                                    def starttime = new Date(currentBuild.startTimeInMillis).format("yyyy-MM-dd HH:mm")
                                    sh(script: "/usr/bin/mysql -h ${DBHOST} -u jenkins -p${DBPASS} testplan -e \"INSERT INTO test_outcome (build_num, build_url, branch, status, pipeline, start_time) VALUES (\'$BUILD_NUMBER\', \'$BUILD_URL\', \'$BRANCH_NAME\', \'${status}\', \'$JOB_NAME\', \'${starttime}\') \" ")
                                }
                            }
                        } catch (Exception exc) {
                            echo "Failed to write to database"
                        }
                    }
                }
            }

            // Cleanup the node
            stage('Cleanup') {
                if (environmentDestroy) {
                    try {
                        cleanWs()
                    } catch (Exception exc) {
                        // TODO: Figure out if we can mark this stage as failed, while allowing the remaining stages to proceed.
                        echo "Failed to clean workspace"
                    }
                } else {
                    echo "Skipping Cleanup as request was made to NOT destroy the environment"
                }
            }
        }
    }
}
