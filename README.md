# StockInfo – Stock Market Information & Portfolio Management System

BCA Final Year Major Project

## Repository Structure

```
stockinfo/
├── stockinfo-backend/     # Spring Boot 3 REST API (Java 21)
├── stockinfo-frontend/    # HTML/CSS/JS + Bootstrap 5 + Chart.js
├── database/              # schema.sql - run this first in MySQL
└── docs/                  # ER diagram, architecture diagram, API docs
```

## Getting Started

### 1. Database
```
mysql -u root -p < database/schema.sql
```

### 2. Backend
```
cd stockinfo-backend
# update src/main/resources/application.properties with your MySQL password
mvn spring-boot:run
```
API runs at: `http://localhost:8080`
Swagger UI: `http://localhost:8080/swagger-ui.html`

### 3. Frontend
Open `stockinfo-frontend/index.html` with a live server
(e.g. VS Code "Live Server" extension) at `http://127.0.0.1:5500`.

## Tech Stack
Java 21 · Spring Boot 3 · Spring Security (JWT) · Spring Data JPA · Hibernate ·
MySQL · Bootstrap 5 · Chart.js · Maven · Swagger

## Project Status
- [x] Phase 1: Architecture & Database Design
- [x] Phase 2: Project Setup & Configuration Skeleton
- [ ] Phase 3: Entities, Repositories & Authentication
- [ ] Phase 4: Core Services & REST APIs
- [ ] Phase 5: Frontend Integration
- [ ] Phase 6: Admin Module
- [ ] Phase 7: Testing & Documentation
