pipeline {
    agent any

    environment {
        DOCKER_REGISTRY = "localhost:5000"
        DOCKER_IMAGE_NAME = "nginx-kubernetes"
        DOCKER_IMAGE_TAG = "latest"
        K8S_NAMESPACE = "nginx-namespace"
        K8S_DEPLOYMENT_NAME = "nginx-deployment"
        JENKINSFILE_ENV_PATH = "Jenkinsfile.env" // Replace with the correct path
    }

    stages {
        stage('Checkout SCM') {
            steps {
                checkout scm
            }
        }

        stage('Read Jenkinsfile.env') {
            steps {
                script {
                    def jenkinsfileEnv = readFile(env.JENKINSFILE_ENV_PATH).trim()
                    def envMap = new groovy.json.JsonSlurper().parseText(jenkinsfileEnv)
                    DOCKER_REGISTRY = envMap.DOCKER_REGISTRY ?: DOCKER_REGISTRY
                    DOCKER_IMAGE_NAME = envMap.DOCKER_IMAGE_NAME ?: DOCKER_IMAGE_NAME
                    DOCKER_IMAGE_TAG = envMap.DOCKER_IMAGE_TAG ?: DOCKER_IMAGE_TAG
                    K8S_NAMESPACE = envMap.K8S_NAMESPACE ?: K8S_NAMESPACE
                    K8S_DEPLOYMENT_NAME = envMap.K8S_DEPLOYMENT_NAME ?: K8S_DEPLOYMENT_NAME
                }
            }
        }

        stage('Build Docker Image') {
            when {
                expression { return env.BRANCH_NAME == 'master' }
            }
            steps {
                script {
                    docker.build("${DOCKER_REGISTRY}/${DOCKER_IMAGE_NAME}:${DOCKER_IMAGE_TAG}")
                    docker.withRegistry("${DOCKER_REGISTRY}", 'docker-hub-credentials') {
                        dockerImage.push("${DOCKER_IMAGE_TAG}")
                    }
                }
            }
        }

        stage('Deploy to Kubernetes') {
            when {
                expression { return env.BRANCH_NAME == 'master' }
            }
            steps {
                // Your Kubernetes deployment steps here
                // For example, using kubectl to apply manifests, etc.
                sh "kubectl apply -n ${K8S_NAMESPACE} -f your-kubernetes-manifest.yaml"
            }
        }
    }

    post {
        success {
            echo 'Deployment successful!'
        }
        failure {
            echo 'Deployment failed!'
        }
    }
}
