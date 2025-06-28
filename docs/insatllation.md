# Инструкция по развертыванию OpenFaaS без Shipyard

Этот проект автоматизирует развертывание платформы OpenFaaS в Docker Swarm с поддержкой мультиарендности через Traefik.
Изначально проект разрабатывался для интеграции с облачной платформой Shipyard, но ввиду её текущей стадии разработки (
июнь 2025) предлагается инструкция для самостоятельного развертывания без Shipyard.

## Предварительные требования

Linux или macOS.  
Docker и Docker Swarm.  
Jenkins.

Репозиторий: Склонируйте проект:

```shell
git clone https://github.com/Vanmors/openfaas-auto-deploy.git
cd openfaas-auto-deploy
```

## Установка зависимостей

Установите Docker:

```shell
sudo apt-get update
sudo apt-get install -y docker-ce docker-ce-cli containerd.io
```

Инициализируйте Docker Swarm:

```shell
docker swarm init
```

Установите Jenkins:

Следуйте официальной инструкции или используйте Docker:

```shell
docker run -p 8080:8080 -v jenkins_home:/var/jenkins_home jenkins/jenkins:lts
```

Настройте Jenkins через веб-интерфейс (http://localhost:8080), создайте пользователя и получите API-токен.

## Настройка Jenkins

1. Создайте задачу Jenkins:

В веб-интерфейсе Jenkins создайте новую задачу типа «Pipeline» с именем, например, openfaas-deploy.

Укажите репозиторий: https://github.com/Vanmors/openfaas-auto-deploy.

Укажите путь к Jenkinsfile (если он есть) или используйте скрипты напрямую.

2. Скопируйте скрипты:

Поместите файлы из репозитория:

[jobs/create_openfaas_swarm.groovy](../jobs/create_openfaas_swarm.groovy) — для развертывания OpenFaaS.

[jobs/destroy_openfaas_swarm.groovy](../jobs/destroy_openfaas_swarm.groovy) — для удаления экземпляра.

Скопируйте их в директорию Jenkins, доступную для выполнения (обычно /var/jenkins_home/jobs/ в контейнере Jenkins):

```shell
cp jobs/*.groovy /var/jenkins_home/jobs/
```

Скопируйте [sources/docker-compose.yml](../sources/docker-compose.yml) в директорию, доступную для Jenkins (например,
/var/jenkins_home/workspace/):

```shell
cp sources/docker-compose.yml /var/jenkins_home/workspace/
```

3. Настройте Pipeline:

В задаче Jenkins выберите «Pipeline script from SCM» (если используете Jenkinsfile) или вставьте содержимое
create_openfaas_swarm.groovy в поле «Pipeline script».

## Запуск скрипта

Через веб-интерфейс Jenkins

1. Перейдите в задачу openfaas-deploy в Jenkins.

2. Нажмите «Build Now» для запуска развертывания.

3. Для удаления экземпляра OpenFaaS создайте отдельную задачу с destroy_openfaas_swarm.groovy и запустите её.

Через консоль (Jenkins CLI)

1. Скачайте jenkins-cli.jar:

```shell
wget http://localhost:8080/jnlpJars/jenkins-cli.jar
```

2. Запустите задачу:

```shell
java -jar jenkins-cli.jar -s http://localhost:8080 -auth <USERNAME>:<API_TOKEN> build openfaas-deploy -v -w
```

Замените <USERNAME> и <API_TOKEN> на ваши учётные данные.

Через HTTP API

```shell
curl -X POST http://localhost:8080/job/openfaas-deploy/build --user <USERNAME>:<API_TOKEN>
```

## Проверка результата

1. Проверьте, что OpenFaaS развернулся:
```shell
curl http://localhost:8080
```

Ожидаемый результат: доступ к веб-интерфейсу OpenFaaS.

2. Проверьте мультиарендность:
```shell
curl -u admin:password http://localhost/function/node
```


Ожидаемый результат: ответ от функции для арендатора node.

3. Если возникли ошибки, проверьте логи Jenkins:
```shell
java -jar jenkins-cli.jar -s http://localhost:8080 -auth <USERNAME>:<API_TOKEN> console openfaas-deploy
```


Или логи Docker:

docker service ls
docker service logs openfaas_gateway

## Устранение неполадок

1. Jenkins не запускает задачу: Проверьте правильность пути к docker-compose.yml и наличие прав доступа.

2. OpenFaaS недоступен: Убедитесь, что Docker Swarm инициализирован и сервисы запущены (docker service ls).

3. Ошибка Traefik: Проверьте конфигурацию (users_admin.htpasswd) и логи:
```shell
docker logs traefik
```



