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
                    "repo": "github.com",
                    "org": "kubic-project",
                    "token": "github-token"
                ],
                "release-3.0": [
                    "repo": "github.com",
                    "org": "kubic-project",
                    "token": "github-token",
                ]
            ],

            "jenkins-library": [
                "master": [
                    "repo": "github.com",
                    "org": "kubic-project",
                    "token": "github-token",
                ],
                "release-3.0": [
                    "repo": "github.com",
                    "org": "kubic-project",
                    "token": "github-token",
                ]
            ],

            "salt": [
                "master": [
                    "repo": "github.com",
                    "org": "kubic-project",
                    "token": "github-token",
                ],
                "release-3.0": [
                    "repo": "github.com",
                    "org": "kubic-project",
                    "token": "github-token",
                ]
            ],

           "velum": [
                "master": [
                    "repo": "github.com",
                    "org": "kubic-project",
                    "token": "github-token",
                ],
                "release-3.0": [
                    "repo": "github.com",
                    "org": "kubic-project",
                    "token": "github-token",
                ]
            ],
            "caasp-container-manifests": [
                "master": [
                    "repo": "github.com",
                    "org": "kubic-project",
                    "token": "github-token",
                ],
                "release-3.0": [
                    "repo": "github.com",
                    "org": "kubic-project",
                    "token": "github-token",
                ]
            ]
        ]
    return [
        "hosting": Repositories[repository][branch]["repo"],
        "organization": Repositories[repository][branch]["org"],
        "token": Repositories[repository][branch]["token"]
    ]
}
