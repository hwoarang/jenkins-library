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
def call() {
    echo "Starting Kubic core project CI"

    // TODO: Don't hardcode salt repo name, find the right place
    // to lookup this information dynamically.
    githubCollaboratorCheck(
        org: 'kubic-project',
        repo: 'salt',
        user: env.CHANGE_AUTHOR,
        credentialsId: 'github-token')

    // Configure the job properties
    properties([
        buildDiscarder(logRotator(numToKeepStr: '15')),
        disableConcurrentBuilds(),
        parameters([
            booleanParam(name: 'ENVIRONMENT_DESTROY', defaultValue: true, description: 'Destroy env once done?')
        ]),
    ])

    withKubicEnvironment(
            nodeLabel: 'leap15.0&&caasp-pr-worker',
            environmentType: 'caasp-kvm',
            environmentDestroy: env.getEnvironment().get('ENVIRONMENT_DESTROY', 'true').toBoolean(),
            gitBase: 'https://github.com/kubic-project',
            gitBranch: env.getEnvironment().get('CHANGE_TARGET', env.BRANCH_NAME),
            gitCredentialsId: 'github-token',
            masterCount: 3,
            workerCount: 2) {

        // Run the Core Project Tests
        coreKubicProjectTests(
          environment: environment,
          podName: 'default'
        )

        // Run through the upgrade orchestration
        upgradeEnvironmentStage1(
            environment: environment,
            fakeUpdatesAvailable: true
        )

        upgradeEnvironmentStage2(
            environment: environment
        )

        // Run the Core Project Tests again
        coreKubicProjectTests(
          environment: environment,
          podName: 'default'
        )
    }
}
