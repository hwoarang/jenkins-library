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
import com.suse.kubic.CaaspVMwareTypeOptions


CaaspVMwareTypeOptions call(Map parameters = [:]) {
    CaaspVMwareTypeOptions options = parameters.get('typeOptions', null)

    if (options == null) {
        options = new CaaspVMwareTypeOptions()
    }

    if (options.image != null && options.image != '') {
        return options
    }

    def proxyFlag = ""
    if (env.hasProperty("http_proxy")) {
        proxyFlag = "-P ${env.http_proxy}"
    }

    timeout(30) {
        withCredentials([
            usernamePassword(credentialsId: 'jazz.qa.prv.suse.net', passwordVariable: 'VC_PASSWORD', usernameVariable: 'VC_USERNAME')
        ]) {
            // warm up virtualenv and show status
            sh "./automation/misc-tools/setup-python lxml"
            echo "Show existing VMs and templates"
            sh "cd automation/caasp-vmware && ./caasp-vmware --vc-host jazz.qa.prv.suse.net --stack-name jenkins-ci- status --show-regex jenkins-ci"
        }
    }

    timeout(240) {
        withCredentials([
            usernamePassword(credentialsId: 'jazz.qa.prv.suse.net', passwordVariable: 'VC_PASSWORD', usernameVariable: 'VC_USERNAME'),
            string(credentialsId: 'caasp-proxy-host', variable: 'proxy'),
            string(credentialsId: 'caasp-location', variable: 'location')
        ]) {
            dir('automation') {
                // creates ${WORKSPACE}/automation/vmware_img_name
                sh(script: "set -o pipefail; ${WORKSPACE}/py3venv/bin/python3 misc-tools/vmware-image-handler.py 2>&1 | tee ${WORKSPACE}/logs/caasp-kvm-prepare-image-caasp.log")
            }
        }
    }

    return options
}
