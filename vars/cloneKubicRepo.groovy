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
// Clones a single Kubic Repo.
def call(Map parameters = [:]) {
    def branch = parameters.get('branch')
    boolean ignorePullRequest = parameters.get('ignorePullRequest', false)
    // The directory in which we will checkout the source code
    def repo = parameters.get('repo')
    // The name of the repository as it's hosted on Git.
    def project_repo = getRepoInfo(repo, branch)["repository"]
    def gitBase = "https://" + getRepoInfo(repo, branch)["hosting"] + "/" + getRepoInfo(repo, branch)["organization"]

    echo "Cloning Kubic Repo: ${project_repo}"

    timeout(5) {
        dir(repo) {
            if (!ignorePullRequest && env.JOB_NAME.contains(project_repo)) {
                if (env.CHANGE_ID) {
                    echo 'Attempting rebase...'
                    checkout([
                        $class: 'GitSCM',
                        branches:  [[name: "*/${env.CHANGE_TARGET}"]],
                        extensions: [
                            [$class: 'LocalBranch'],
                            [$class: 'CleanCheckout']
                        ],
                        userRemoteConfigs: [
                            [url:"${gitBase}/${project_repo}.git", credentialsId: getRepoInfo(repo, branch)["token"]]
                        ]
                    ])

                    def gitVars = checkout scm
                    def rebaseScript = "git -c 'user.name=${gitVars.GIT_COMMITTER_NAME}' -c 'user.email=${gitVars.GIT_COMMITTER_EMAIL}' rebase ${env.CHANGE_TARGET}"
                    def rebaseCode = sh(script: rebaseScript, returnStatus: true)

                    if (rebaseCode) {
                        sh('git rebase --abort')
                        error("Rebase failed with code: '${rebaseCode}'. Manual rebase required.")
                    } else {
                        echo 'Rebase successful!'
                    }
                } else {
                    checkout scm
                }

            } else {
                checkout([
                    $class: 'GitSCM',
                    branches: [[name: "*/${branch}"]],
                    userRemoteConfigs: [
                        [url: "${gitBase}/${project_repo}.git", credentialsId: getRepoInfo(repo, branch)["token"]]
                    ],
                    extensions: [
                        [$class: 'CleanCheckout']
                    ],
                ])
            }
        }
    }
}
