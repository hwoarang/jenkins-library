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
import com.suse.kubic.Minion

// Upgrade Stage 2 - Performs remaining upgrade steps using the "new"
// version of the automation tools.

Environment call(Map parameters = [:]) {
    Environment environment = parameters.get('environment')

    stage('Upgrade Environment 2') {
        // Perform the upgrade
        timeout(185) {
            try {
                dir('automation/velum-bootstrap') {
                    parallel 'monitor-logs-update-minions': {
                        sh(script: "${WORKSPACE}/automation/misc-tools/parallel-ssh -e ${WORKSPACE}/environment.json -i ${WORKSPACE}/automation/misc-files/id_shared all -- journalctl -f")
                    },
                    'update-minions': {
                        try {
                            sh(script: "./velum-interactions --update-minions --environment ${WORKSPACE}/environment.json")
                        } finally {
                            sh(script: "${WORKSPACE}/automation/misc-tools/parallel-ssh --stop -e ${WORKSPACE}/environment.json -i ${WORKSPACE}/automation/misc-files/id_shared all -- journalctl -f")
                        }
                    }
                    sh(script: "./velum-interactions --download-kubeconfig --environment ${WORKSPACE}/environment.json")
                    sh(script: "mv ${WORKSPACE}/kubeconfig ${WORKSPACE}/kubeconfig.old")
                    sh(script: "cp kubeconfig ${WORKSPACE}/kubeconfig")
                    sh(script: "diff -u ${WORKSPACE}/kubeconfig.old ${WORKSPACE}/kubeconfig || :")
                }
            } finally {
                dir('automation/velum-bootstrap') {
                    junit "velum-bootstrap.xml"
                    try {
                        archiveArtifacts(artifacts: "screenshots/**")
                        archiveArtifacts(artifacts: "kubeconfig")
                    } catch (Exception exc) {
                        echo "Failed to Archive Artifacts"
                    }
                }
            }
        }
    }
}
