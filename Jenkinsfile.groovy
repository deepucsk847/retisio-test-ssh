pipeline {
    agent any

    environment {
        IMAGE_NAME = "my-app"
        REGISTRY_URL = "your.registry.url"
        KUBE_NAMESPACE = "your-namespace"
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
                    env.envVars = readJSON text: jenkinsfileEnv
                }
            }
        }

        stage('Build Docker Image') {
            when {
                branch 'master'
            }
            steps {
                script {
                    docker.build("${REGISTRY_URL}/${IMAGE_NAME}:${env.envVars.IMAGE_TAG}")
                    docker.withRegistry("${REGISTRY_URL}", 'docker-hub-credentials') {
                        dockerImage.push("${env.envVars.IMAGE_TAG}")
                    }
                }
            }
        }

        stage('Deploy to Kubernetes') {
            when {
                branch 'master'
            }
            steps {
                script {
                    // Your Kubernetes deployment steps here
                    // For example, using kubectl to apply manifests, etc.
                }
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
