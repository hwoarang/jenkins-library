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


Environment call(Map parameters = [:]) {
    Environment environment = parameters.get('environment')
    boolean chooseCrio = parameters.get('chooseCrio', false)

    // TODO: This and bootstrapEnvironment share 90% of the same code

    timeout(125) {
        dir('automation/misc-tools') {
            sh(script: "python3 ./wait-for-velum https://\$(jq '.minions[0].addresses.publicIpv4' -r ${WORKSPACE}/environment.json) --timeout 2")
        }
    }

    timeout(90) {
        dir('automation/velum-bootstrap') {
            sh(script: './velum-interactions --setup')
        }
    }

    timeout(90) {
        try {
            dir('automation/velum-bootstrap') {
                if (chooseCrio) {
                    echo "Choosing cri-o"
                    sh(script: "./velum-interactions --configure --enable-tiller --environment ${WORKSPACE}/environment.json --choose-crio")
                } else {
                    echo "Choosing Docker"
                    sh(script: "./velum-interactions --configure --enable-tiller --environment ${WORKSPACE}/environment.json")
                }
            }
        } finally {
            dir('automation/velum-bootstrap') {
                junit "velum-bootstrap.xml"
                try {
                    archiveArtifacts(artifacts: "screenshots/**")
                } catch (Exception exc) {
                    echo "Failed to Archive Artifacts"
                }
            }
        }
    }
}
