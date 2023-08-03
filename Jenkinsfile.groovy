pipeline {
    agent any

    stages {
        stage('Checkout SCM') {
            steps {
                checkout([$class: 'GitSCM', branches: [[name: '*/master']], userRemoteConfigs: [[url: 'https://github.com/deepucsk847/retisio-test-ssh.git']]])
            }
        }

        stage('Read Jenkinsfile.env') {
            environment {
                ENV_FILE = 'Jenkinsfile.env'
            }
            steps {
                withCredentials([file(credentialsId: 'jenkinsfile-env-credentials', variable: 'ENV_FILE')]) {
                    sh "cp $ENV_FILE ."
                }
            }
        }

        stage('Build Docker Image') {
            steps {
                // Your build Docker image steps here
                // For example:
                sh "docker build -t $DOCKER_REGISTRY/$DOCKER_IMAGE_NAME:$DOCKER_IMAGE_TAG ."
            }
        }

        stage('Deploy to Kubernetes') {
            steps {
                // Your deploy to Kubernetes steps here
                // For example:
                sh "kubectl apply -f nginx-deployment.yaml -n $K8S_NAMESPACE"
                sh "kubectl apply -f nginx-service.yaml -n $K8S_NAMESPACE"
            }
        }
    }
}
