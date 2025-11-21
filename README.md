## Инструкция по запуску:

### 🔧 Требования

Перед запуском убедитесь, что установлены:

- **Docker**
- **Docker Compose

Java и Maven НЕ требуются — всё работает в контейнере.

---

## ▶️ Запуск проекта

### 1️⃣ Создать `.env`

Создайте файл `.env` в корне проекта:

```
POSTGRES_DB=cardsdb  
POSTGRES_USER=user  
POSTGRES_PASSWORD=pass  
POSTGRES_URL=jdbc:postgresql://localhost:5432/cardsdb  
JWT_SECRET=rOmVtoPS7FaHfgf8vhswtsqbiLtWT5QUJOv9OqwTex87hJ7FEVBGNAfgSWj0Lzi2
```

### 2️⃣ Сборка и запуск

В корневой директории:

```bash
docker compose up -d --build
```
---
## 🧩 Проверить, что сервис работает

После запуска:
### 🔥 Swagger UI:

```
http://localhost:8080/swagger-ui/index.html
```

### 🔥 OpenAPI YAML:

```
http://localhost:8080/v3/api-docs.yaml
```

---

## 🛡 Аутентификация

Используется схема:

- `POST /auth/login` → выдаёт `access_token + refresh_token`
- `POST /auth/refresh` → обновляет токен
- Доступ к пользовательским и админским эндпоинтам защищён `JwtFilter`

Пример заголовка:

```
Authorization: Bearer <token>
```

---
## 🛑 Остановка контейнеров

```bash
docker compose down
```

С удалением volume:

```bash
docker compose down -v
```
---
## Контакты:

Telegram: https://t.me/s3mmm_7

Почта: semyondiulin@yandex.ru