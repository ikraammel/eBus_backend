pipeline {
    agent any

    stages {
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