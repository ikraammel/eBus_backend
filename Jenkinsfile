pipeline {
    agent any

    stages {

        stage('Check Env') {
            steps {
              bat '''
                  echo DB_URL=%DB_URL%
                  echo DB_USERNAME=%DB_USERNAME%
                  echo DB_PASSWORD=%DB_PASSWORD%
                  '''
            }
        }

        stage('Build') {
            steps {
                bat 'mvn clean package'
            }
        }

        stage('Test') {
            steps {
                bat 'mvn test'
            }
        }

        stage('Run App') {
            steps {
                bat 'java -jar target/*.jar'
            }
        }
    }
}