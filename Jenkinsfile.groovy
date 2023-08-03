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
                withCredentials([file(credentialsId: 'jenkinsfile-env-credentials', variable: 'JENKINSFILE_ENV_PATH')]) {
                    def jenkinsfileEnv = readFile(JENKINSFILE_ENV_PATH).trim()
                    envVars = readJSON text: jenkinsfileEnv
                }
            }
        }

        stage('Build Docker Image') {
            when {
                branch 'master' // Only build the Docker image for the master branch
            }
            steps {
                script {
                    docker.build("${REGISTRY_URL}/${IMAGE_NAME}:${envVars.IMAGE_TAG}")
                    docker.withRegistry("${REGISTRY_URL}", 'docker-hub-credentials') {
                        dockerImage.push("${envVars.IMAGE_TAG}")
                    }
                }
            }
        }

        stage('Deploy to Kubernetes') {
            when {
                branch 'master' // Only deploy to Kubernetes for the master branch
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
