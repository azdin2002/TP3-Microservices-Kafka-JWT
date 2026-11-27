# Mini-Projet – Microservices Architecture with Spring Boot, Kafka & OAuth2 / JWT

A distributed, event-driven, and secured microservices application for university management (**Étudiants & Filières**) built with **Spring Boot**, **Spring Security (OAuth2 Resource Server & JWT RS256)**, **Apache Kafka (KRaft)**, and **MySQL**.

## Architecture

The application consists of three independent Spring Boot microservices following the **Database-per-Service** pattern, with asynchronous event synchronization via Apache Kafka and stateless JWT authentication.

| Service | Port | Responsibility | Storage |
|---|---:|---|---|
| **Auth Service** | `9100` | Authentication (`/login`), token refresh (`/refresh`), RS256 JWT signing, JWKS | In-memory BCrypt users + RSA 2048-bit key pair |
| **Filière Service** | `8083` | CRUD REST API for Filières, Kafka event producer (`AFTER_COMMIT`) | MySQL (`filiere_db`) |
| **Étudiant Service** | `8082` | CRUD REST API for Étudiants, pagination, filtering by filière, Kafka consumer & local projection | MySQL (`etudiant_db`) |
| **Apache Kafka** | `9092` | Event broker in KRaft mode (no ZooKeeper) | Topic `faculte.filieres.v1` |

### Architecture Overview

```text
                       Client (Postman / Swagger)
                                  |
                 1. Login / Refresh (RS256 JWT)
                                  v
                       +---------------------+
                       |    Auth Service     |
                       |       :9100         |
                       | /.well-known/jwks   |
                       +----------+----------+
                                  |
                      2. Public Key (JWKS)
                   +--------------+--------------+
                   |                             |
                   v                             v
        +---------------------+       +---------------------+
        |   Filière Service   |       |  Étudiant Service   |
        |       :8083         |       |       :8082         |
        +----+-----------+----+       +----+-----------+----+
             |           |                 ^           |
             |           | 3. Publish      | 4. Consume|
             |           v                 |           |
             |     +-----------------------------+     |
             |     |     Apache Kafka :9092      |     |
             |     |  Topic: faculte.filieres.v1 |     |
             |     +-----------------------------+     |
             v                                         v
     +---------------+                         +---------------+
     |  MySQL :3306  |                         |  MySQL :3306  |
     |  filiere_db   |                         |  etudiant_db  |
     |  (filieres)   |                         |  (etudiants + |
     +---------------+                         |   snapshot)   |
                                               +---------------+
```

## Technologies

- **Language:** Java 21 (LTS)
- **Framework:** Spring Boot 3.5.16, Spring Web, Spring Data JPA, Spring Validation
- **Security:** Spring Security, OAuth2 Resource Server, JWT (RS256), JWKS, BCrypt
- **Messaging:** Apache Kafka 3.9.1 (KRaft mode)
- **Database:** MySQL 8.x
- **API Documentation:** Springdoc OpenAPI 2.8.17 (Swagger UI)
- **Build & Testing:** Maven Wrapper, Postman, Git

## Microservices

### 1. Auth Service (`:9100`)
- Authenticates users with BCrypt-hashed credentials.
- Issues short-lived **Access Tokens** (`15 min`, `type=access`) and long-lived **Refresh Tokens** (`7 days`, `type=refresh`) signed with **RS256**.
- Exposes the RSA public key via `GET /.well-known/jwks.json`.

### 2. Filière Service (`:8083`)
- Full CRUD REST API (`/api/filieres`) backed by `filiere_db`.
- Enforces unique `code` validation (`409 Conflict` on duplicates).
- Publishes `FiliereEvent` messages (`CREATED`, `UPDATED`, `DELETED`) to Kafka using `@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)` with the filière `id` as the Kafka message key to preserve ordering.

### 3. Étudiant Service (`:8082`)
- Full CRUD REST API (`/api/etudiants`) backed by `etudiant_db`.
- Consumes `faculte.filieres.v1` (`group-id: etudiant-filiere-projection`) to maintain a local `filieres_snapshot` table (no cross-database `@ManyToOne` or synchronous REST calls).
- Handles `DELETED` events via soft delete (`active = false`) to preserve historical student records while blocking new enrollments (`409 Conflict`).
- Enriches student responses with the nested `filiere` object (`id`, `code`, `libelle`, `active`).
- **Mini-Projet Extensions:**
  - `GET /api/etudiants/page?page=0&size=5` — Paginated and alphabetically sorted student list.
  - `GET /api/etudiants/filiere/{filiereId}?page=0&size=5` — Paginated filtering of students by filière.

## Security & Authorization (RBAC)

Each business microservice independently validates JWTs (`iss`, `aud=faculte-api`, `exp`, `type=access`, and RS256 signature via JWKS). Refresh tokens are rejected (`401 Unauthorized`) on business APIs.

| Endpoint | Anonymous | `ROLE_USER` | `ROLE_ADMIN` |
|---|---|---|---|
| `POST /api/auth/login`, `/api/auth/refresh` | `200` | `200` | `200` |
| `GET /.well-known/jwks.json` | `200` | `200` | `200` |
| `GET /api/filieres/{id}`, `GET /api/etudiants/{id}` | `401` | `200` | `200` |
| `GET /api/etudiants/filiere/{id}`, `/api/etudiants/page` | `401` | `200` | `200` |
| `GET /api/filieres`, `GET /api/etudiants` | `401` | `403` | `200` |
| `POST` / `PUT` / `DELETE` (Filières & Étudiants) | `401` | `403` | `201` / `200` / `204` |

**Demo Accounts (in-memory):**
- **Admin:** `admin` / `admin123` (`ROLE_ADMIN`, `ROLE_USER`)
- **User:** `user` / `user123` (`ROLE_USER`)

> **Security Note:** Private RSA keys (`keys/private.pem`) and `.env` files containing database credentials are excluded via `.gitignore` and must never be committed.

## Project Structure

```text
TP3-Microservices-Kafka-JWT/
├── auth-service/
│   ├── src/
│   ├── keys/                  # Generated locally (private.pem is gitignored)
│   ├── pom.xml
│   └── mvnw
├── filiere-service/
│   ├── src/
│   ├── pom.xml
│   └── mvnw
├── etudiant-service/
│   ├── src/
│   ├── pom.xml
│   └── mvnw
├── .env.example
├── .gitignore
└── README.md
```

## Prerequisites

- Java 21 (JDK)
- MySQL 8.x (running on port `3306`)
- Apache Kafka 3.9.1 (KRaft mode on port `9092`)
- Git & Maven (or the included Maven Wrapper)

## Configuration & Setup

1. **Clone the repository:**
   ```bash
   git clone https://github.com/azdin2002/TP3-Microservices-Kafka-JWT.git
   cd TP3-Microservices-Kafka-JWT
   ```

2. **Create the MySQL databases:**
   ```sql
   CREATE DATABASE IF NOT EXISTS filiere_db;
   CREATE DATABASE IF NOT EXISTS etudiant_db;
   ```

3. **Configure environment variables:**
   Copy `.env.example` to `.env` and fill in your local MySQL credentials:
   ```bash
   cp .env.example .env
   ```
   ```env
   DB_USERNAME=your_mysql_username
   DB_PASSWORD=your_mysql_password
   ```

4. **Generate the RSA key pair (first run only):**
   Run `RsaKeyGenerator` inside `auth-service` (with working directory set to `auth-service/`) to generate `keys/private.pem` and `keys/public.pem`.

5. **Start Kafka (KRaft) and create the topic:**
   ```bash
   kafka-topics.bat --bootstrap-server localhost:9092 --create --if-not-exists --topic faculte.filieres.v1 --partitions 1 --replication-factor 1
   ```

## Running the Application

Start MySQL and Kafka first, then launch each microservice from its own directory (use `mvnw.cmd` on Windows):

```bash
# Terminal 1 – Auth Service (:9100)
cd auth-service
./mvnw spring-boot:run

# Terminal 2 – Filière Service (:8083)
cd filiere-service
./mvnw spring-boot:run

# Terminal 3 – Étudiant Service (:8082)
cd etudiant-service
./mvnw spring-boot:run
```

## Endpoints & API Documentation

| Service | URL |
|---|---|
| **Auth Service (Login)** | `POST http://localhost:9100/api/auth/login` |
| **Auth Service (JWKS)** | `GET http://localhost:9100/.well-known/jwks.json` |
| **Filière Service (Swagger UI)** | `http://localhost:8083/swagger-ui/index.html` |
| **Étudiant Service (Swagger UI)** | `http://localhost:8082/swagger-ui/index.html` |

## Author

**Azzeddine Salmoun**  
Master DevOps & Cloud Computing – Semestre 3  
Faculté Polydisciplinaire de Larache – Université Abdelmalek Essaâdi  
Module : Architecture Orientée Services (AOS) et APIs — Encadré par **Pr. Mohammed ACHKARI BEGDOURI**