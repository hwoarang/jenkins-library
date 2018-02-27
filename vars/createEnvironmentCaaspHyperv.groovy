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
import com.suse.kubic.CaaspHypervTypeOptions

Environment call(Map parameters = [:]) {
    int masterCount = parameters.get('masterCount')
    int workerCount = parameters.get('workerCount')

    CaaspHypervTypeOptions options = parameters.get('typeOptions', null)

    if (options == null) {
        options = new CaaspHypervTypeOptions()
    }

    Environment environment

    timeout(120) {
        String stackName = "${JOB_NAME}-${BUILD_NUMBER}".replace("/", "-")

	    // https://github.com/PowerShell/Win32-OpenSSH/issues/1049 -> Use SSH password
       	withCredentials([usernamePassword(credentialsId: 'hvcore-ssh', usernameVariable: 'SSHUSER', passwordVariable: 'SSHPASS')]) {
            sh(script: "set -o pipefail; sshpass -e ssh -o StrictHostKeyChecking=no -o UserKnownHostsFile=/dev/null \"${SSHUSER}\"@${options.hvJumpHost} 'Get-ChildItem Env:; git checkout ${env.BRANCH_NAME}; git pull; caasp-hyperv.ps1 deploy -caaspImage ${options.image} -stackName ${stackName} -adminRam ${options.adminRam} -adminCpu ${options.adminCpu} -masters ${masterCount} -masterRam ${options.masterRam} -masterCpu ${options.masterCpu} -workers ${workerCount} -workerRam ${options.workerRam} -workerCpu ${options.workerCpu} -Force' 2>&1 | tee ${WORKSPACE}/logs/caasp-hyperv.log")
        }
        // Extract state from log file and generate environment.json
        dir("automation/caasp-hyperv") {
            sh(script: "sed '/^===/,/^===/!d ; /^===.*/d' ${WORKSPACE}/logs/caasp-hyperv.log > ./caasp-hyperv.hvstate ; jq '.' ./caasp-hyperv.hvstate > /dev/null 2>&1")
            sh(script: "cp ./caasp-hyperv.hvstate ../../logs")
            sh(script: "./tools/generate-environment")
            sh(script: "../misc-tools/generate-ssh-config ./environment.json")
            // Read the generated environment file
            environment = new Environment(readJSON(file: 'environment.json'))
            sh(script: "cp environment.json ${WORKSPACE}/environment.json")
            sh(script: "cat ${WORKSPACE}/environment.json")
        }

        archiveArtifacts(artifacts: 'environment.json', fingerprint: true)
    }

    return environment
}
