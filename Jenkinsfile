pipeline {
  agent any

  stages {

    stage('Check Talisman') {
        steps {
            sh 'talisman --version || echo "Talisman non trouvé"'
        }
    }
    
    stage('Repository recovery') {
        steps {
            git branch: 'main', url: 'https://github.com/wendgoudi/JAVA-DEVSECOPS-MONOLITHIC-V2.git'
        }
    } 
  
    stage('Talisman Security Scan') {
      steps {
          script {
              sh '''
                echo "Lancement du scan Talisman..."
                # Génère un rapport JSON lisible par l'outil HTML
                talisman --scan > talisman-report.json || true
                echo "Scan terminé. Rapport généré : talisman-report.json"
              '''
          }
      }
    }
  
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

  post {
    always {
        //Archive le rapport Talisman dans Jenkins
        archiveArtifacts artifacts: 'talisman-report.json', fingerprint: true
    }
  }
}
