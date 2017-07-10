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
import com.suse.kubic.Minion

def call(Map parameters = [:]) {
    Minion minion = parameters.get('minion')
    String script = parameters.get('script')
    boolean returnStatus = parameters.get('returnStatus', false)
    boolean returnStdout = parameters.get('returnStdout', false)

    // TODO: Probably shouldn't hardcode the key here
    // TODO: returnStatus won't work right now as SSH considers it's job done
    // sucessfully when the command is ran remotely, even if it fails.
    sh(script: "ssh -o StrictHostKeyChecking=no -i ${WORKSPACE}/terraform/ssh/id_docker root@${minion.ipv4} -- ${script}", returnStatus: returnStatus, returnStdout: returnStdout)
}