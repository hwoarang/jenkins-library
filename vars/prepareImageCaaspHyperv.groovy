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
import com.suse.kubic.CaaspHypervTypeOptions


CaaspHypervTypeOptions call(Map parameters = [:]) {
    CaaspHypervTypeOptions options = parameters.get('typeOptions', null)

    if (options == null) {
        options = new CaaspHypervTypeOptions()
    }

    timeout(120) {
        withCredentials([usernamePassword(credentialsId: 'hvcore-ssh', usernameVariable: 'SSHUSER', passwordVariable: 'SSHPASS')]) {
            sh(script: "set -o pipefail; sshpass -e ssh -o StrictHostKeyChecking=no -o UserKnownHostsFile=/dev/null \"${SSHUSER}\"@${options.hvJumpHost} 'Get-ChildItem Env:;git checkout ${env.BRANCH_NAME}; git pull; caasp-hyperv.ps1 fetchimage -caaspImageSourceUrl ${options.imageSourceUrl} -nochecksum' 2>&1 | tee ${WORKSPACE}/logs/caasp-hyperv.log")
        }
    }

    return options
}
