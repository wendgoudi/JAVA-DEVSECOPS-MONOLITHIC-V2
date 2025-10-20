pipeline {
  agent any

  stages {
/*
    stage('git version') {
        steps {
            sh "git version"
        }
    }

    stage('maven version') {
        steps {
            sh "mvn -v"
        }
    }
  */
    stage('docker version') {
        steps {
            sh "docker -v"
        }
    }

    stage('kubernetes version') {
        steps {
            sh " kubectl version --client"
        }
    }

    stage('Build Artifact') {
      steps {
        sh "mvn clean package -DskipTests=true"
        archive 'target/*.jar' //Pour qu'on puisse télécharger ultérieurement
      }
    }
/*   
    stage('repository recovery') {
        steps {
            git branch: 'main', url: 'https://github.com/wendgoudi/JAVA-DEVSECOPS-MONOLITHIC-V2.git'
        }
    } 
  
    stage('trufflehog scan') {
      steps {
          script {
              sh '''
                docker run --rm -v $WORKSPACE:/src trufflesecurity/trufflehog:latest \
                filesystem /src --json > trufflehog-report.json || true
              '''
          }
      }
      post {
          always {
              // Archive le rapport dans Jenkins
              archiveArtifacts artifacts: 'trufflehog-report.json', fingerprint: true
          }
          success {
              echo "Scan terminé. Rapport archivé dans Jenkins."
          }
          failure {
              echo "Des secrets ont peut-être été détectés."
          }
      }
    }
*/
    stage('sast-sonarqube-analysis') {
        steps {
            script {
                def mvn = tool 'Default Maven'
                withSonarQubeEnv('SonarQube') {
                    sh "${mvn}/bin/mvn clean verify sonar:sonar -Dsonar.projectKey=gestion-des-personnes -Dsonar.projectName='gestion-des-personnes'"
                }
            }        
        }
    }

    stage('sonarque quality gate') {
        steps {
            timeout(time: 5, unit: 'MINUTES') {
                waitForQualityGate abortPipeline: true
            }
        }
    }

    stage('Dependency Check') {
        environment {
            NVD_API_KEY = credentials('NVD_API_KEY')
        }
        steps {
            sh '''
            echo "Running OWASP Dependency Check..."
            
            # Créer un répertoire persistant pour la base
            mkdir -p /var/jenkins_home/dependency-check-data

            # Mettre à jour la base NVD
            mvn org.owasp:dependency-check-maven:12.1.0:update \
                -DnvdApiKey=$NVD_API_KEY \
                -DdataDirectory=/var/jenkins_home/dependency-check-data

            # Lancer l’analyse
            mvn org.owasp:dependency-check-maven:12.1.0:check \
                -DnvdApiKey=$NVD_API_KEY \
                -Dformat=ALL \
                -DfailBuildOnCVSS=8 \
                -DdataDirectory=/var/jenkins_home/dependency-check-data \
                -DoutputDirectory=target/dependency-check-report
            '''
        }
        post {
            always {
                dependencyCheckPublisher pattern: 'target/dependency-check-report/dependency-check-report.xml'
            }
        }
    }


 /* 
    stage('docker build and push') {
      steps {
        withDockerRegistry([credentialsId: "docker-hub", url: ""]) {
          sh 'printenv'
          sh 'docker build -t wendgoudi/gestion-personnes:latest .'
          sh 'docker push wendgoudi/gestion-personnes:latest'
        }
      }
    }

    stage('Kubernetes Deployment') {
        environment {
            // Pointe vers le kubeconfig de Jenkins
            KUBECONFIG = '/var/lib/jenkins/.kube/config'
        }
        steps {
            script {
                // Mise à jour de l'image dans le manifest
                sh """
                    sed -i 's#image: gestion-personnes:1.0#image: wendgoudi/gestion-personnes:latest#g' k8s_deployment_service.yaml
                    kubectl apply -f k8s_deployment_service.yaml
                    kubectl rollout status deployment/gestion-personnes-deployment
                """
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
*/
  }
}
