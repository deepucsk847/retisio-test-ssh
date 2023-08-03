pipeline {
    agent any

    environment {
        DOCKER_REGISTRY = 'localhost:5000'
        DOCKER_IMAGE_NAME = 'nginx-kubernetes'
        DOCKER_IMAGE_TAG = 'latest'
        K8S_NAMESPACE = 'nginx-namespace'
        K8S_DEPLOYMENT_NAME = 'nginx-deployment'
    }

    stages {
        stage('Declarative: Checkout SCM') {
            steps {
                checkout scm
            }
        }

        stage('Read Jenkinsfile.env') {
            steps {
                script {
                    def jenkinsfileEnvPath = "Jenkinsfile.env" // Replace with the correct path
                    def jenkinsfileEnvContent = readFile(jenkinsfileEnvPath).trim()

                    echo "Jenkinsfile.env contents:"
                    echo jenkinsfileEnvContent

                    try {
                        def envMap = new groovy.json.JsonSlurper().parseText(jenkinsfileEnvContent)
                        DOCKER_REGISTRY = envMap.DOCKER_REGISTRY ?: DOCKER_REGISTRY
                        DOCKER_IMAGE_NAME = envMap.DOCKER_IMAGE_NAME ?: DOCKER_IMAGE_NAME
                        DOCKER_IMAGE_TAG = envMap.DOCKER_IMAGE_TAG ?: DOCKER_IMAGE_TAG
                        K8S_NAMESPACE = envMap.K8S_NAMESPACE ?: K8S_NAMESPACE
                        K8S_DEPLOYMENT_NAME = envMap.K8S_DEPLOYMENT_NAME ?: K8S_DEPLOYMENT_NAME
                    } catch (Exception e) {
                        echo "Error parsing Jenkinsfile.env as JSON: ${e.message}"
                    }
                }
            }
        }

        stage('Build Docker Image') {
            // Your build steps here
        }

        stage('Deploy to Kubernetes') {
            // Your deployment steps here
        }

        stage('Declarative: Post Actions') {
            steps {
                echo 'Deployment failed!'
            }
        }
    }
}
