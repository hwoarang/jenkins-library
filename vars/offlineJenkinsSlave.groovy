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
import jenkins.model.Jenkins
import hudson.slaves.OfflineCause
import jenkins.plugins.openstack.compute.JCloudsSlave

def call(Map parameters = [:]) {
    String name = parameters.get('name', env.NODE_NAME)
    boolean offline = parameters.get('offline', true)
    String message = parameters.get('message', 'Marked offline by pipeline')

    Computer computer = Jenkins.instance.getNode(name).toComputer()

    if (computer == null) {
        echo "Computer is null?"
    } else {
        if (offline) {
            echo "Marking build slave as offline"
        } else {
            echo "Marking build slave as online"
        }

        computer.setTemporarilyOffline(offline, new OfflineCause.ByCLI(message))
    }
}
