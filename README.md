# BooksOnline

BooksOnline is a production-style REST backend for a small webshop built with Java 21 and Spring Boot 3.x.

## Tech stack

- Java 21
- Spring Boot 3.x
- Spring Web
- Spring Data JPA
- PostgreSQL
- MapStruct
- Lombok
- Jakarta Validation
- Spring Boot Actuator
- OpenAPI / Swagger
- Testcontainers
- Maven
- Docker / Docker Compose

## Features

- Browse the three assignment products:
  - Book: `E-Commerce done right`
  - Digital software license: `E-Commerce simulator`
  - Voucher
- Check product availability via product stock and availability flag
- Purchase products and decrease stock atomically
- Track orders by id
- Retrieve all orders for admin use

## Project structure

Main code is organized under:

- `config`
- `controller`
- `dto`
- `entity`
- `exception`
- `mapper`
- `repository`
- `service`
- `service/impl`

## API endpoints

- `GET /api/products`
- `GET /api/products/{id}`
- `POST /api/orders`
- `GET /api/orders/{id}`
- `GET /api/admin/orders`

## Swagger / OpenAPI

After starting the application, open:

- Minimal UI: `http://localhost:8080/`
- Swagger UI: `http://localhost:8080/swagger-ui.html`
- OpenAPI JSON: `http://localhost:8080/api-docs`
- Actuator health: `http://localhost:8080/actuator/health`

The minimal UI is intended only for a short demo of the backend scenarios: browse products, create an order, look up an order by id, and load all orders for the shop-owner view.

## Assignment documentation

- Technical decisions: `docs/technical-decisions.md`
- Architecture: `docs/architecture.md`
- Seasonal x10 scaling approach: `docs/seasonal-scaling.md`
- CI/CD, fault injection, and recovery: `docs/cicd-fault-recovery.md`
- PlantUML diagrams: `docs/diagrams/`

The repository also includes a GitHub Actions example at `.github/workflows/ci.yml`.

## Running locally

### Prerequisites

- Java 21
- Maven 3.9+
- PostgreSQL running locally on `localhost:5432`

### Build and test

```bash
mvn test
```

### Run the application

```bash
mvn spring-boot:run
```

The application uses these default datasource settings in `application.yml`:

- database: `books_online`
- username: `books_online`
- password: `books_online`

Override them with environment variables if needed:

- `SPRING_DATASOURCE_URL`
- `SPRING_DATASOURCE_USERNAME`
- `SPRING_DATASOURCE_PASSWORD`

## Running with Docker

Build and start the app and PostgreSQL:

```bash
docker compose up --build
```

## Example requests

### List products

```bash
curl http://localhost:8080/api/products
```

### Get a product

```bash
curl http://localhost:8080/api/products/1
```

### Create an order

```bash
curl -X POST http://localhost:8080/api/orders ^
  -H "Content-Type: application/json" ^
  -d "{\"items\":[{\"productId\":1,\"quantity\":1},{\"productId\":2,\"quantity\":2}]}"
```

### Get an order

```bash
curl http://localhost:8080/api/orders/1
```

### Get all orders for admin

```bash
curl http://localhost:8080/api/admin/orders
```

## Testing

- Unit tests: `src/test/java/com/daniil/booksonline/service/impl`
- Integration tests: `src/test/java/com/daniil/booksonline/BooksOnlineApiIntegrationTest.java`

Integration tests use Testcontainers with PostgreSQL automatically.
