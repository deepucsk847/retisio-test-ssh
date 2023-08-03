node {
    stage('Declarative: Checkout SCM') {
        checkout scm
    }

    stage('Read Jenkinsfile.env') {
        def jenkinsfileEnvPath = "Jenkinsfile.env" // Replace with the correct path
        def jenkinsfileEnvContent = readFile(jenkinsfileEnvPath).trim()

        echo "Jenkinsfile.env contents:"
        echo jenkinsfileEnvContent

        try {
            def envMap = new groovy.json.JsonSlurper().parseText(jenkinsfileEnvContent)
            env.DOCKER_REGISTRY = envMap.DOCKER_REGISTRY ?: 'localhost:5000'
            env.DOCKER_IMAGE_NAME = envMap.DOCKER_IMAGE_NAME ?: 'nginx-kubernetes'
            env.DOCKER_IMAGE_TAG = envMap.DOCKER_IMAGE_TAG ?: 'latest'
            env.K8S_NAMESPACE = envMap.K8S_NAMESPACE ?: 'nginx-namespace'
            env.K8S_DEPLOYMENT_NAME = envMap.K8S_DEPLOYMENT_NAME ?: 'nginx-deployment'
        } catch (Exception e) {
            echo "Error parsing Jenkinsfile.env as JSON: ${e.message}"
        }
    }

    stage('Authenticate with Kubernetes') {
        withCredentials([file(credentialsId: 'jenkins-kubeconfig', variable: 'KUBECONFIG')]) {
            sh 'kubectl version' // Verify that kubectl is using the correct config
        }
    }

    stage('Build Docker Image') {
        // Your build steps here
        echo "Building Docker Image..."
        // For example:
        sh 'docker build -t ${env.DOCKER_REGISTRY}/${env.DOCKER_IMAGE_NAME}:${env.DOCKER_IMAGE_TAG} .'
    }

    stage('Deploy to Kubernetes') {
        // Your deployment steps here
        echo "Deploying to Kubernetes..."
        // For example:
        sh "kubectl apply -f nginx-deployment.yaml --namespace=${env.K8S_NAMESPACE} --record"
    }

    stage('Declarative: Post Actions') {
        echo 'Deployment failed!'
    }
}
