### Hexlet tests and linter status:
[![Actions Status](https://github.com/alexeymelekhov-dev/spring-boot-project-99/actions/workflows/hexlet-check.yml/badge.svg)](https://github.com/alexeymelekhov-dev/spring-boot-project-99/actions)

![CI](https://github.com/alexeymelekhov-dev/spring-boot-project-99/actions/workflows/ci.yml/badge.svg)
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=alexeymelekhov-dev_spring-boot-project-99&metric=alert_status)](https://sonarcloud.io/summary/new_code?id=alexeymelekhov-dev_spring-boot-project-99)

# 🚀 Spring Boot Task App

A backend application for task management, deployed using Docker with PostgreSQL and Nginx reverse proxy.

---

## 🌍 Live Demo

🚀 Application is deployed and available at:

http://185.247.185.121/

---

## 🔐 Demo Credentials

For testing purposes, you can use:

- Email: `hexlet@example.com`
- Password: `qwerty`

> ⚠️ This is a demo account used for testing the application.

---

## 📚 API Documentation

Swagger UI is available for interactive API testing:

🔗 **Swagger UI:** http://185.247.185.121/swagger-ui/index.html

Features:
- Explore all REST endpoints
- Execute requests directly from browser
- Inspect request/response schemas
- Useful for testing and debugging

---

## 🧱 Tech Stack

- Java 21
- Spring Boot
- Spring Data JPA
- PostgreSQL 16
- Docker & Docker Compose
- Nginx (reverse proxy)

---

## 🧱 Architecture Diagram

```mermaid id="d2"
flowchart LR
    A[Client Browser] --> B[Nginx Reverse Proxy]
    B --> C[Spring Boot REST API]
    C --> D[(PostgreSQL Database)]

    subgraph Docker Environment
        B
        C
        D
    end
```

---

## 🔁 API Flow Diagram

### 1. Login and Task Creation Flow

```mermaid id="f2"
sequenceDiagram
    participant U as User
    participant N as Nginx
    participant A as Spring Boot API
    participant D as PostgreSQL

    U->>N: POST /login (email + password)
    N->>A: Forward request
    A->>D: Verify user credentials
    D-->>A: User data / auth result
    A-->>U: JWT / session response

    U->>N: POST /api/tasks (JWT + task data)
    N->>A: Forward request
    A->>D: Save task to database
    D-->>A: Task saved
    A-->>U: Task created response
```

---

## 🚀 How to Run Locally

### Clone repository

```bash
git clone https://github.com/your-repo/spring-boot-project-99.git
cd spring-boot-project-99
```

### Start project

```bash
docker compose up --build -d
```

---

## 📌 Features

- REST API for task management
- CRUD operations
- PostgreSQL database integration
- Swagger documentation
- Dockerized deployment
- Nginx reverse proxy
- CI/CD with GitHub Actions
- Code quality checks (SonarCloud)
