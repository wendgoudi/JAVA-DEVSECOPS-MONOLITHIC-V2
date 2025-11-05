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

    stage('build artifact') {
      steps {
        sh "mvn clean package -DskipTests=true"
        archive 'target/*.jar' //Pour qu'on puisse télécharger ultérieurement
      }
    }

    stage('repository recovery') {
        steps {
            git branch: 'main', url: 'https://github.com/wendgoudi/JAVA-DEVSECOPS-MONOLITHIC-V2.git'
        }
    } 
  
    stage('SECRETS - trufflehog scan') {
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

    stage('SAST - sonarqube analysis & quality gate') {
        steps {
            script {
                // Exécution de l’analyse SonarQube
                def mvn = tool 'Default Maven'
                withSonarQubeEnv('SonarQube') {
                    sh "${mvn}/bin/mvn clean verify sonar:sonar -Dsonar.projectKey=gestion-des-personnes -Dsonar.projectName='gestion-des-personnes'"
                }

                // Attente du Quality Gate (5 minutes max)
                timeout(time: 5, unit: 'MINUTES') {
                    waitForQualityGate abortPipeline: true
                }
            }
        }
    }

    stage('SCA - snyk dependency scan') {
        environment {
            SNYK_TOKEN = credentials('SNYK_TOKEN')
        }
        steps {
            script {
                echo "Authentification à Snyk..."
                sh 'snyk auth $SNYK_TOKEN'

                echo "Analyse de sécurité avec Snyk (ne bloque pas le build)..."
                // On capture le code de retour, sans faire échouer le build
                def statusCode = sh(returnStatus: true, script: '''
                    snyk test --severity-threshold=high --json-file-output=snyk-report.json || true
                ''')

                echo "Snyk scan terminé avec le code: ${statusCode}"

                echo "Génération du rapport HTML..."
                sh 'snyk-to-html -i snyk-report.json -o snyk-report.html'

                // Déplacer le rapport HTML dans le dossier target/site pour cohérence
                sh '''
                    mkdir -p target/site
                    mv snyk-report.html target/site/index.html
                '''
            }
        }
        post {
            always {
                publishHTML([
                    allowMissing: true,
                    alwaysLinkToLastBuild: true,
                    keepAll: true,
                    reportDir: 'target/site',
                    reportFiles: 'index.html',
                    reportName: 'Snyk Security Report',
                    reportTitles: 'Snyk Scan Results'
                ])
            }
            success {
                echo 'Analyse Snyk terminée. Aucune vulnérabilité critique détectée.'
            }
            unstable {
                echo 'Des vulnérabilités ont été détectées, mais le build n’est pas bloqué.'
            }
        }
    }

    stage('BUILD - docker image') {
    steps {
        timeout(time: 10, unit: 'MINUTES') {
        sh '''
            export DOCKER_BUILDKIT=0
            docker build --no-cache -t wendgoudi/gestion-personnes:latest .
        '''
        }
      }
    }

    stage('CONTAINER SCAN - trivy vulnerability search') {
        steps {
            script {
                echo "Scanning Docker image with Trivy..."

                // Scan image mais ne pas échouer en cas de vulnérabilités
                sh '''
                    trivy image --severity HIGH,CRITICAL --ignore-unfixed \
                    --format json -o trivy-report.json wendgoudi/gestion-personnes:latest || true
                '''

                // Convert JSON -> HTML
                sh '''
                    trivy convert report trivy-report.json --format template \
                    --template "@contrib/html.tpl" \
                    --output trivy-report.html || true
                '''

                // Zip du rapport si besoin
                sh '''
                    zip -r trivy-report.zip trivy-report.json trivy-report.html || true
                '''
            }
        }
        post {
            always {
                echo "Archiving Trivy reports..."
                archiveArtifacts artifacts: 'trivy-report.*', allowEmptyArchive: true

                publishHTML([
                    allowMissing: true,
                    alwaysLinkToLastBuild: true,
                    keepAll: true,
                    reportDir: '.',
                    reportFiles: 'trivy-report.html',
                    reportName: 'Trivy Container Security Report'
                ])
            }
            failure {
                echo "Trivy detected vulnerabilities, but pipeline continues."
            }
        }
    }

    stage('DAST - owasp zap scan') {
        steps {
            script {
                sh """
                echo "Creating report directory..."
                mkdir -p zap-report
                chmod 777 zap-report 

                echo "Running OWASP ZAP baseline scan..."

                docker run --network=host \
                    -v \$(pwd)/zap-report:/zap/wrk/reports \
                    ghcr.io/zaproxy/zaproxy:stable zap-baseline.py \
                    -t http://192.168.220.1:9090 \
                    -r reports/zap-report.html || true
                """
            }
        }
        post {
            always {
                publishHTML([
                    allowMissing: true,
                    alwaysLinkToLastBuild: true,
                    keepAll: true,
                    reportDir: 'zap-report',
                    reportFiles: 'zap-report.html',
                    reportName: 'OWASP ZAP DAST Report'
                ])
            }
        }
    }

    stage('PUSH - push code to docker hub') {
      steps {
        withDockerRegistry([credentialsId: "docker-hub", url: ""]) {
          sh 'printenv'
          sh 'docker push wendgoudi/gestion-personnes:latest'
        }
      }
    }

 /*
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
