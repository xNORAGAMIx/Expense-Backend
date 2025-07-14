# 💰 Udhaari - Expense Backend

This is the backend service for [Udhaari](https://udhaari.vercel.app/), a modern expense-sharing web app designed for groups. Built using **Spring Boot**, this backend leverages **Kafka** for event-driven architecture, **Redis** for caching, and **Brevo (SendinBlue)** for transactional emails.

## 🔧 Tech Stack

- **Java 17**
- **Spring Boot**
- **Spring Data JPA**
- **MySQL** (via RDS or local)
- **Kafka** (Dockerized for messaging)
- **Redis** (Dockerized for caching)
- **Brevo API** (Email service)
- **Lombok**, **MapStruct** for cleaner code
- **JWT-based Auth**

---


---

## 🚀 Features

- ✅ User and group management
- ✅ Expense creation and splitting
- ✅ Email notifications via **Brevo**
- ✅ Kafka-based messaging between services
- ✅ Redis caching for performance
- ✅ Environment-specific configs (Dev & Prod)
- ✅ Secure authentication with **JWT**

---

## 🐳 Running Locally with Docker

Make sure you have `Docker` and `Docker Compose` installed.

```bash
# Start Kafka and Redis containers
docker-compose up -d

# Start the Spring Boot app (Dev profile)
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev

spring.datasource.url=jdbc:mysql://localhost:3306/udhaari
spring.datasource.username=root
spring.datasource.password=your_password

spring.kafka.bootstrap-servers=localhost:9092
spring.redis.host=localhost
spring.redis.port=6379

brevo.api.key=your_brevo_api_key
jwt.secret=your_jwt_secret


