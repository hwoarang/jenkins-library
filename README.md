Kubic Jenkins Pipeline Library
==============================

This is a Jenkins pipeline library of components needed to perform Kubic/CaaSP
CI.

**This code is under heavy development**

Each component is designed to be reusable within the context of the different
kinds of CI which Kubic/CaaSP may require, and are all written in Groovy, the
language used to define Jenkins Pipelines.

Each component is described below, including it's purpose, expected inputs and
outputs, and ideas for improvement.

Sample Jenkinsfiles
-------------------

### Example Jenkinsfile for a core Kubic project

    def targetBranch = env.getEnvironment().get('CHANGE_TARGET', env.BRANCH_NAME)
    library "kubic-jenkins-library@${targetBranch}"
    coreKubicProjectCi()

### Example Jenkinsfile for a Periodic Kubic build

	library "kubic-jenkins-library@${env.BRANCH_NAME}"
	coreKubicProjectPeriodic(minionCount: 50)

### Example Jenkinsfile for a Periodic Kubic build with additional stages

	library "kubic-jenkins-library@${env.BRANCH_NAME}"
	coreKubicProjectPeriodic(minionCount: 50) {
		stage('Perform Some Additional Tasks') {
			// Add any extra step here
		}
	}


Pipeline Methods
----------------

### coreKubicProjectCi

For any of the core kubic projects, this will be only method called. This allows us to
keep each projects Jenkinsfile slim, and reuse code.

**Example usage:**

    coreKubicProjectCi()

**Inputs:**

| Name | Description |
|:-----|:------------|
| None | N/A         |

**Context varables:**

| Name | Description |
|:-----|:------------|
| None | N/A         |

**Outputs:**

| Name | Description |
|:-----|:------------|
| None | N/A         |

