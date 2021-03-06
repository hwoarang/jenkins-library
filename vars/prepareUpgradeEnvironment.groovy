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
    Map<String,String> extraRepos = parameters.get('extraRepos', new HashMap<String,String>())

    stage('Prepare Upgrade Environment') {
        // Inject the extra repos
        extraRepos.each { extraRepoAlias, extraRepoUrl ->
            sh(script: "set -o pipefail; ./automation/misc-tools/inject_repo.sh $extraRepoUrl $extraRepoAlias 2>&1 | tee ${WORKSPACE}/logs/inject-repos-${extraRepoAlias}.log")
        }
    }
}
