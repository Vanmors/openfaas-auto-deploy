pipeline {
    agent any

    stages {
        stage('Destroy OpenFaaS') {
            steps {
                script {
                    // remove services
                    sh "docker stack rm func"
                    // remove secrets
                    sh "docker secret rm basic-auth-user basic-auth-password "
                }
            }
        }
    }

    post {
        success {
            echo "OpenFaaS успешно удалён"
        }
        failure {
            echo "Ошибка при удалении OpenFaaS, проверьте, что все функции удалены"
        }
    }
}