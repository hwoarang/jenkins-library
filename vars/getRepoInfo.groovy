// Copyright 2019 SUSE LINUX GmbH, Nuernberg, Germany.
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

def call(String repository, String branch) {
    // Definitive list of repositories
    def Repositories = [
            "automation": [
                "master": [
                    "host": "github.com",
                    "org": "kubic-project",
                    "repo": "automation",
                    "token": "github-token"
                ],
                "release-3.0": [
                    "host": "github.com",
                    "org": "kubic-project",
                    "repo": "automation",
                    "token": "github-token",
                ]
            ],

            "jenkins-library": [
                "master": [
                    "host": "github.com",
                    "org": "kubic-project",
                    "repo": "jenkins-library",
                    "token": "github-token",
                ],
                "release-3.0": [
                    "host": "github.com",
                    "org": "kubic-project",
                    "repo": "jenkins-library",
                    "token": "github-token",
                ]
            ],

            "salt": [
                "master": [
                    "host": "github.com",
                    "org": "kubic-project",
                    "repo": "salt",
                    "token": "github-token",
                ],
                "release-3.0": [
                    "host": "github.com",
                    "org": "kubic-project",
                    "repo": "salt",
                    "token": "github-token",
                ]
            ],

           "velum": [
                "master": [
                    "host": "github.com",
                    "org": "kubic-project",
                    "repo": "velum",
                    "token": "github-token",
                ],
                "release-3.0": [
                    "host": "github.com",
                    "org": "kubic-project",
                    "repo": "velum",
                    "token": "github-token",
                ]
            ],
            "caasp-container-manifests": [
                "master": [
                    "host": "github.com",
                    "org": "kubic-project",
                    "repo": "caasp-container-manifests",
                    "token": "github-token",
                ],
                "release-3.0": [
                    "host": "github.com",
                    "org": "kubic-project",
                    "repo": "caasp-container-manifests",
                    "token": "github-token",
                ]
            ]
        ]
    return [
        "hosting": Repositories[repository][branch]["host"],
        "organization": Repositories[repository][branch]["org"],
        "repository": Repositories[repository][branch]["repo"],
        "token": Repositories[repository][branch]["token"]
    ]
}
