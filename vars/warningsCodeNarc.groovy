// Copyright 2018 SUSE LINUX GmbH, Nuernberg, Germany.
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
def call(Map parameters = [:]) {
    // Given the WarningsPublisher code is pretty ugly, extract this
    // into a function so we don't spread the ugly around too much!
    def filename = parameters.get('filename')

    echo "Skipping parsing of CodeNarc XML report due to JENKINS-45930"
    // step([
    //     $class: 'WarningsPublisher',
    //     parserConfigurations: [[
    //         parserName: 'CodeNarc',
    //         pattern: filename,
    //     ]],
    //     unstableTotalAll: '0',
    //     usePreviousBuildAsReference: true
    // ])
}
