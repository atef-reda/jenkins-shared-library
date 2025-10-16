def call(Map config = [:]) {
    pipeline {
        agent any

        stages {
            stage('Prepare Variables') {
                steps {
                    script {
                        IMAGE_NAME = config.imageName ?: 'my-image:latest'
                        DOCKER_PATH = config.dockerfilePath ?: '.'
                        GIT_URL = config.gitUrl ?: ''
                        GIT_BRANCH = config.gitBranch ?: 'main'
                    }
                }
            }

            stage('Checkout Repo') {
                steps {
                    git url: GIT_URL, branch: GIT_BRANCH
                }
            }

            stage('Build Docker Image') {
                steps {
                    script {
                        docker.build("${IMAGE_NAME}", "${DOCKER_PATH}")
                    }
                }
            }

            stage('Push Docker Image') {
                steps {
                    withCredentials([usernamePassword(
                        credentialsId: config.credentialsId,
                        usernameVariable: 'DOCKER_USERNAME',
                        passwordVariable: 'DOCKER_PASSWORD'
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
