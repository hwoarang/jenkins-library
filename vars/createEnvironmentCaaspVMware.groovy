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
//import com.suse.kubic.CaaspKvmTypeOptions


Environment call(Map parameters = [:]) {
    int masterCount = parameters.get('masterCount')
    int workerCount = parameters.get('workerCount')
    Environment environment

    timeout(3600) {
        withCredentials([
          usernamePassword(credentialsId: 'github-token-caaspjenkins', passwordVariable: 'GH-CAASPJENKINS-PASS', usernameVariable: 'GH-CAASPJENKINS-USER'),
          usernamePassword(credentialsId: 'jazz.qa.prv.suse.net', passwordVariable: 'VC_PASSWORD', usernameVariable: 'VC_USERNAME'),
          usernamePassword(credentialsId: 'github-token', passwordVariable: 'GITHUB-TOKEN', usernameVariable: 'GITHUB-USER')
        ]) {
            // no collab check: do not use for PRs
            sh "automation/misc-tools/setup-python PTable"
            sh "cat automation/vmware_img_name"

            dir("automation/caasp-vmware") {
                echo "Destroy old VMs and templates if any"
                sh(script: './caasp-vmware  --vc-host jazz.qa.prv.suse.net --media-dir caasp-team --stack-name jenkins-ci- destroy')

                echo "Show existing VMs and templates"
                sh "./caasp-vmware --vc-host jazz.qa.prv.suse.net --stack-name jenkins-ci- status --show-regex jenkins-ci"

                // TODO add  --master-count ${masterCount} --worker-count ${workerCount}    and so on e.g.
                // --image ${options.image}  --admin-ram ${options.adminRam} --admin-cpu ${options.adminCpu} --master-ram ${options.masterRam} --master-cpu ${options.masterCpu} --worker-ram ${options.workerRam} --worker-cpu ${options.workerCpu}
                sh(script: 'set -o pipefail; ./caasp-vmware --vc-host jazz.qa.prv.suse.net --media-dir caasp-team --stack-name jenkins-ci- --media $(cat ../vmware_img_name)  deploy | tee ${WORKSPACE}/logs/caasp-vmware-build.log')

                sh(script: './caasp-vmware --vc-host jazz.qa.prv.suse.net  --stack-name jenkins-ci- status --show-regex jenkins-ci')
            }
            // generate automation/caasp-vmware/environment.json
            sh "./automation/caasp-vmware/tools/generate-environment"
            // update automation/caasp-vmware/environment.json
            sh(script: "cd automation/caasp-vmware && ${WORKSPACE}/py3venv/bin/python3 fix-env-fqdns.py")
        }

        // Read the generated environment file
        sh(script: "cp automation/caasp-vmware/environment.json environment.json")

        echo "Generating automation/misc-tools/environment.ssh_config"
        sh(script: '${WORKSPACE}/automation/misc-tools/generate-ssh-config ${WORKSPACE}/environment.json')

        environment = new Environment(readJSON(file: 'environment.json'))
        sh(script: "cat ${WORKSPACE}/environment.json")
        archiveArtifacts(artifacts: 'environment.json', fingerprint: true)
    }

    return environment
}
