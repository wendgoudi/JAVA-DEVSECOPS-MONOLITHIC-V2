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
            jacoco execPattern: 'target/jacoco.exec', skipCopy: true
        }
      }
    }
  }
}
