
pipeline {
    agent any
    parameters {
        choice(name: 'BASIC_AUTH', choices: ['true', 'false'], description: 'Enable basic authentication?')
    }
    environment {
        AUTH_URL = "http://basic-auth-plugin:8080/validate"
        COMPOSE_FILE = "/var/jenkins_home/faas/faas/docker-compose.yml"
    }
    stages {
        stage('Setup Secrets') {
            steps {
                script {
                    // Генерация случайного пароля
                    def secret = sh(script: "head -c 16 /dev/urandom | shasum -a 256 | cut -d ' ' -f1", returnStdout: true).trim()
                    // Создание Docker Secrets
                    sh '''
                       echo "admin" | docker secret create basic-auth-user - || echo "Secret basic-auth-user already exists"
                       echo "${secret}" | docker secret create basic-auth-password - || echo "Secret basic-auth-password already exists"
                   '''
                    sh "curl -sSLf https://cli.openfaas.com | sh"
                    // sh "echo -n ${secret} | faas-cli login --username=admin --password-stdin"
                    // Выводим учетные данные
                    echo "[Credentials] username: admin"
                    echo "[Credentials] password: ${secret}"
                }
            }
        }

        stage('Deploy OpenFaaS') {
            steps {
                script {
                    // Деплой OpenFaaS через Docker Swarm
                    sh "docker stack deploy func --compose-file ${COMPOSE_FILE}"
                }
            }
        }
        stage('Verify Deployment') {
            steps {
                script {
                    // Проверяем, что OpenFaaS запущен
                    sh "docker service ls | grep 'func_gateway'"
                }
            }
        }
    }
    post {
        success {
            echo "OpenFaaS успешно развернут"
        }
        failure {
            echo "Ошибка при развертывании OpenFaaS"
        }
    }
}