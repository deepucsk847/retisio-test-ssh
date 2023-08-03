def props

stage('Read Jenkinsfile.env') {
    steps {
        script {
            withCredentials([file(credentialsId: 'jenkinsfile-env-credentials', variable: 'ENV_FILE')]) {
                sh "cp $ENV_FILE Jenkinsfile.env"
                props = readProperties file: "Jenkinsfile.env"
            }
        }
    }
}

pipeline {
    agent any
    environment {
        DOCKER_REGISTRY = props.DOCKER_REGISTRY
        DOCKER_IMAGE_NAME = props.DOCKER_IMAGE_NAME
        DOCKER_IMAGE_TAG = props.DOCKER_IMAGE_TAG
        K8S_NAMESPACE = props.K8S_NAMESPACE
        K8S_DEPLOYMENT_NAME = props.K8S_DEPLOYMENT_NAME
    }
    stages {
        stage('Checkout') {
            steps {
                checkout([$class: 'GitSCM',
                    branches: [[name: '*/master']],
                    doGenerateSubmoduleConfigurations: false,
                    extensions: [[$class: 'LocalBranch', localBranch: '**']],
                    submoduleCfg: [],
                    userRemoteConfigs: [[
                        credentialsId: 'github-ssh-keycheck',
                        url: 'https://github.com/deepucsk847/retisio-test-ssh.git'
                    ]]
                ])
            }
        }
        stage('Build Docker Image') {
            steps {
                script {
                    def image = docker.build("${DOCKER_REGISTRY}/${DOCKER_IMAGE_NAME}:${DOCKER_IMAGE_TAG}", "-f Dockerfile .")
                    docker.withRegistry("${DOCKER_REGISTRY}", "jenkinsfile-env-credentials") {
                        image.push()
                    }
                }
            }
        }
        stage('Deploy to Kubernetes') {
            steps {
                script {
                    sh "kubectl config use-context minikube" // Set Minikube as the current context
                    sh "kubectl create namespace ${K8S_NAMESPACE}" // Create namespace if it doesn't exist
                    sh "kubectl apply -f nginx-deployment.yaml -n ${K8S_NAMESPACE}" // Deploy the Kubernetes deployment
                }
            }
        }
    }
}
