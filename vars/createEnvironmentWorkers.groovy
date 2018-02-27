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

// Deploy worker nodes where needed

import com.suse.kubic.Environment

Environment call(Map parameters = [:]) {
    Environment environment = parameters.get('environment')
    String type = parameters.get('type', 'caasp-kvm')
    def typeOptions = parameters.get('typeOptions')
    int workerCount = parameters.get('workerCount')

    switch (type) {
        case 'caasp-kvm':
            echo "Secondary worker creation step unnecessary"
            return environment
        case 'openstack':
            echo "Secondary worker creation step unnecessary"
            return environment
        case 'hyperv':
            echo "Secondary worker creation step unnecessary"
            return environment
        case 'bare-metal':
            // return a new, up-to-date environment object
            return createEnvironmentWorkersCaaspBareMetal()
        default:
            error("Unknown environment type: ${type}")
    }
}
