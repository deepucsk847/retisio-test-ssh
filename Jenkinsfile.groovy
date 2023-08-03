def props

pipeline {
    agent any
    stages {
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
        stage('Checkout') {
            steps {
                checkout([$class: 'GitSCM',
                    branches: [[name: '*/master']],
                    doGenerateSubmoduleConfigurations: false,
                    extensions: [[$class: 'LocalBranch', localBranch: '**']],
                    submoduleCfg: [],
                    userRemoteConfigs: [[
                        credentialsId: 'github-ssh-keycheck',
                        url: 'git@github.com:deepucsk847/retisio-test-ssh.git' // Updated GitHub repository URL
                    ]]
                ])
            }
        }
        stage('Build Docker Image') {
            steps {
                script {
                    def image = docker.build("${props.DOCKER_REGISTRY}/${props.DOCKER_IMAGE_NAME}:${props.DOCKER_IMAGE_TAG}", "-f Dockerfile .")
                    docker.withRegistry("${props.DOCKER_REGISTRY}", "jenkinsfile-env-credentials") {
                        image.push()
                    }
                }
            }
        }
        stage('Deploy to Kubernetes') {
            steps {
                script {
                    sh "kubectl config use-context minikube" // Set Minikube as the current context
                    sh "kubectl create namespace ${props.K8S_NAMESPACE}" // Create namespace if it doesn't exist
                    sh "kubectl apply -f nginx-deployment.yaml -n ${props.K8S_NAMESPACE}" // Deploy the Kubernetes deployment
                }
            }
        }
    }
}
