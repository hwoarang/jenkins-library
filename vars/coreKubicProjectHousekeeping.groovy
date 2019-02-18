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
    boolean enableBugzillaReferenceCheck = parameters.get('enableBugzillaReferenceCheck', true)

    echo "Starting Kubic core project housekeeping"

    // TODO: Don't hardcode salt repo name, find the right place
    // to lookup this information dynamically.
    githubCollaboratorCheck(
        org: 'kubic-project',
        repo: 'salt',
        user: env.CHANGE_AUTHOR,
        credentialsId: getRepoInfo('salt', env.getEnvironment().get('CHANGE_TARGET',env.BRANCH_NAME))["token"])

    def label = "housekeeping-${UUID.randomUUID().toString()}"

    String changeTarget = env.getEnvironment().get('CHANGE_TARGET', env.BRANCH_NAME)
    boolean isBackport = changeTarget.matches(/release-\d\.\d/)

    stage('GitHub Labels') {
        // If this is a Pull Request build...
        if (env.CHANGE_ID) {
            echo "Add a backport label if needed"
            boolean hasBackportLabel = pullRequest.labels.contains("${changeTarget}-backport")
            if (isBackport && !hasBackportLabel) {
                echo "Adding backport label: ${changeTarget}-backport"
                pullRequest.addLabels(["${changeTarget}-backport".toString()])
            }

            // Remove any invalid backport labels
            // TODO: Disabled due to plugin issue re parsing GitHub JSON - add later once fixed.
            // echo "Remove any invalid backport labels"
            // def prLabels = pullRequest.labels
            // prLabels.each { prLabel ->
            //     echo "Checking label: ${prLabel}"
            //     if (prLabel.matches(/release-\d\.\d-backport/) && prLabel != changeTarget + '-backport') {
            //         echo "Removing label: ${prLabel}"
            //         pullRequest.removeLabel(prLabel.toString())
            //         echo "Removed label: ${prLabel}"
            //     }
            // }
        } else {
            echo "Not a PR, no PR labels required"
        }
    }

    if (enableBugzillaReferenceCheck) {
        stage('Bugzilla Refs') {
            // Future TODO: Extract bugzilla IDs, post a link to this PR as
            // a comment on the bug.

            // If this is a Pull Request build...
            if (env.CHANGE_ID) {
                // TODO: I don't really like how this works.. If you forget the label as you
                // create the PR, the job will run + fail, you then add the label, and have to
                // manually re-trigger the job.
                boolean skipBugzillaRefs = pullRequest.labels.contains("no-bugzilla-ref")

                if (!skipBugzillaRefs && isBackport) {
                    echo "Checking for bug references"

                    boolean result = true;

                    pullRequest.commits.each { commit ->
                        echo("Checking commit ${commit.sha} for a bug reference")

                        boolean hasBugReference = commit.message.matches(/(?s).*bsc#\d+.*/)

                        if (!hasBugReference) {
                            echo("Please amend commit ${commit.sha} to include a bug reference")
                            result = false
                        }
                    }

                    if (!result) {
                        error("All backport commits require a bug reference. If this PR does not require bug references, please add the no-bugzilla-ref label.")
                    }
                } else {
                    // This is probably not the right thing to do...
                    echo "Not a backport, or explicit skip requested, no bug references required"
                }
            } else {
                echo "Not a PR, no bug references required"
            }
        }
    }
}
