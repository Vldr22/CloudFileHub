![Java](https://img.shields.io/badge/Java-21-orange)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.3.4-brightgreen)
![Maven](https://img.shields.io/badge/Maven-4.0.0-blue)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-003153)
![Redis](https://img.shields.io/badge/Redis-7-7B001C)
![Kafka](https://img.shields.io/badge/Kafka-3.7-9400D3)
![Docker](https://img.shields.io/badge/Docker-Compose-0092CC)
![AWS S3](https://img.shields.io/badge/AWS%20S3-Yandex-orange)
![Nginx](https://img.shields.io/badge/Nginx-alpine-green)
![Swagger](https://img.shields.io/badge/Swagger-OpenAPI%203.0-85EA2D)
![ClamAV](https://img.shields.io/badge/ClamAV-antivirus-EFEF34)

# CloudFileHub
Файловый хостинг на S3 с JWT-аутентификацией, ролевой моделью и асинхронным антивирусным сканированием.

## Архитектура и технологии
```
┌─────────────┐     ┌──────────────────┐     ┌──────────────────┐
│   Nginx     │────▶│  s3-file-service │────▶│  Yandex Object   │
│   :8000     │     │  Swagger/OpenAPI │     │  Storage (S3)    │
│  rate limit │     │  PostgreSQL 16   │     └──────────────────┘
│    gzip     │     │  Redis 7 (JWT)   │
└─────────────┘     └────────┬─────────┘
                       Kafka │
                    ┌────────▼─────────┐     ┌──────────────────┐
                    │ antivirus-service│────▶│   ClamAV :3310   │  
                    │ retry + DLT      │     │   (files scan)   │
                    └──────────────────┘     └──────────────────┘
```
## Ключевые особенности

- **Публичный доступ** — просмотр и скачивание файлов без регистрации
- **JWT + Redis** — HttpOnly cookies с whitelist токенов
- **Двухуровневая проверка** — валидация типа/размера (до 30MB) + антивирус через Kafka
- **Ролевая модель:**
    - USER — загрузка 1 файла, удаление только своих файлов
    - ADMIN — загрузка до 5 файлов, удаление любых файлов, управление пользователями, просмотр аудит-логов и статистики
- **Rate limiting** — Nginx (auth: 5r/min, upload: 2r/s, api: 10r/s)

**Доступ:**
- API: http://localhost:8000/home
- Swagger UI: http://localhost:8000/swagger-ui/index.html
- Kafka UI: http://localhost:8000/kafka-ui/

## Быстрый старт
```bash
git clone https://github.com/Vldr22/CloudFileHub.git
cd CloudFileHub
cp .env.example .env  # заполнить переменные окружения
./scripts/docker-build-and-logs.sh  # 1) Собрать, 2) Поднять
```

## Структура проекта
```
CloudFileHub/
├── s3-file-service/      # REST API, авторизация, бизнес-логика
├── antivirus-service/    # Антивирусное сканирование, Kafka retry
├── common-kafka/         # Общие модели событий
├── docker-compose.yml
├── nginx.conf
└── scripts/
```
## Roadmap

- [ ] Unit и integration тесты
- [ ] Email-уведомления о результатах сканирования
- [ ] Деплой на VPS
- [ ] Полнотекстовый поиск (Elasticsearch)
