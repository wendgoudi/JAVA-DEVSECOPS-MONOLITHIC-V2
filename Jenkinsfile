pipeline {
  agent any

  stages {
    stage('Build Artifact') {
      steps {
        sh "mvn clean package -DskipTests=true"
        archive 'target/*.jar' //Pour qu'on puisse télécharger ultérieurement
      }
    }

    stage('Unit Tests') {
      steps {
        sh "mvn test"
      }
      post {
        always {
            junit allowEmptyResults: true, testResults: 'target/surefire-reports/*.xml'
            jacoco execPattern: 'target/jacoco.exec'
        }
      }
    }
  //gestion-personnes:1.0 .
    stage('Docker Build and Push') {
      steps {
        withDockerRegistry([credentialsId: "docker-hub", url: ""]) {
          sh 'printenv'
          sh 'docker build -t wendgoudi/gestion-personnes:latest .'
          sh 'docker push wendgoudi/gestion-personnes:latest'
        }
      }
    }

  stage('Kubernetes Deployment') {
      steps {
          withKubeConfig([credentialsId: 'kubeconfig']) {
              // Mise à jour dynamique de l'image dans le manifest
              sh """
                sudo sed -i 's#/home/wendgoudi/.minikube#/var/lib/jenkins/.minikube#g' /var/lib/jenkins/.kube/config
                sed -i 's#image: gestion-personnes:1.0#image: wendgoudi/gestion-personnes:latest#g' k8s_deployment_service.yaml
                kubectl apply -f k8s_deployment_service.yaml
                kubectl rollout status deployment/gestion-personnes-deployment
              """
          }
      }
    }

  }
}
