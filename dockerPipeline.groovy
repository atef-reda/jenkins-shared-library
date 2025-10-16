def call(Map config = [:]) {
    pipeline {
        agent any

        environment {
            IMAGE_NAME = config.imageName ?: 'my-image:latest'
            DOCKERFILE_PATH = config.dockerfilePath ?: '.'
            GIT_URL = config.gitUrl ?: ''
            GIT_BRANCH = config.gitBranch ?: 'main'
        }

        stages {
            stage('Checkout Repo') {
                steps {
                    git branch: "${GIT_BRANCH}", url: "${GIT_URL}"
                }
            }

            stage('Build Docker Image') {
                steps {
                    script {
                        docker.build("${IMAGE_NAME}", "${DOCKERFILE_PATH}")
                    }
                }
            }

            stage('Push Docker Image') {
                steps {
                    withCredentials([usernamePassword(
                        credentialsId: config.credentialsId,
                        passwordVariable: 'DOCKER_PASSWORD',
                        usernameVariable: 'DOCKER_USERNAME'
                    )]) {
                        script {
                            docker.withRegistry('https://index.docker.io/v1/', config.credentialsId) {
                                docker.image("${IMAGE_NAME}").push()
                            }
                        }
                    }
                }
            }
        }
    }
}
