# Architecture

BooksOnline follows a conventional layered Spring Boot architecture.

## Layers

- `controller`: REST endpoints and HTTP concerns.
- `service`: business use cases, validation beyond DTO constraints, transaction boundaries, logging, and orchestration.
- `repository`: Spring Data JPA database access.
- `entity`: persistence model for products, orders, and order items.
- `dto`: request and response objects exposed by the API.
- `mapper`: MapStruct conversions between entities and DTOs.
- `exception`: centralized exception-to-HTTP response mapping.
- `config`: OpenAPI, shared beans, and demo data seeding.

## Main Flow

Customers browse products through `GET /api/products` and inspect availability through the returned `stock` and `available` fields. Purchases are submitted to `POST /api/orders`. The order service aggregates duplicate product lines, locks the requested products, verifies stock, decrements stock, creates order items, and persists the order in one transaction.

Customers track orders with `GET /api/orders/{id}`. The shop owner can review all orders through `GET /api/admin/orders`.

## Persistence

The model is intentionally small:

- `Product`: catalog item with name, description, price, stock, and type.
- `Order`: purchase record with creation time, status, total price, and line items.
- `OrderItem`: snapshot of purchased product, quantity, and unit price.

Fetch-join repository queries are used for order reads so DTO mapping does not trigger avoidable lazy-loading issues.

## Operational Shape

The application runs as a single stateless Spring Boot container backed by PostgreSQL. Horizontal scaling is possible because request state is not stored in memory. Database locking protects stock updates when multiple application instances process purchases concurrently.
