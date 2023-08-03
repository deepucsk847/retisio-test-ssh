pipeline {
    agent any

    environment {
        DOCKER_IMAGE = 'my-docker-image'
        K8S_NAMESPACE = 'my-kubernetes-namespace'
        K8S_DEPLOYMENT_NAME = 'my-kubernetes-deployment'
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
                    withCredentials([file(credentialsId: 'jenkinsfile-env-credentials', variable: 'JENKINSFILE_ENV_PATH')]) {
                        ENV_FILE = readFile(env.JENKINSFILE_ENV_PATH).trim()
                    }
                }
            }
        }

        stage('Build Docker Image') {
            when {
                expression { env.BRANCH_NAME == 'master' }
            }
            steps {
                script {
                    def dockerImage = docker.build("${DOCKER_IMAGE}:${env.BUILD_NUMBER}")
                    dockerImage.push()
                    dockerImage.push("latest")
                }
            }
        }

        stage('Deploy to Kubernetes') {
            when {
                expression { env.BRANCH_NAME == 'master' }
            }
            steps {
                script {
                    def kubeconfig = readYaml file: 'kubeconfig.yaml'
                    kubeconfig.currentContext = kubeconfig.clusters[0].name
                    writeFile file: 'kubeconfig-updated.yaml', text: toYaml(kubeconfig)

                    sh """
                        export KUBECONFIG=kubeconfig-updated.yaml
                        kubectl config view
                        kubectl config use-context \${K8S_CLUSTER_NAME}
                        kubectl config view
                        kubectl set image deployment/\${K8S_DEPLOYMENT_NAME} \${K8S_CONTAINER_NAME}=\${DOCKER_IMAGE}:\${env.BUILD_NUMBER} -n \${K8S_NAMESPACE}
                    """
                }
            }
        }
    }
}
