# Technical Decisions

BooksOnline is implemented with Java 21 and Spring Boot because the assignment asks for a backend-focused webshop that should be easy to run, test, and defend. Spring Web provides a straightforward REST API model, Spring Data JPA keeps persistence code small, and PostgreSQL is used as the relational database because orders, products, and order items have clear transactional relationships.

DTOs are used at the API boundary so persistence entities do not become the public contract. MapStruct removes repetitive mapping code while keeping mappings explicit and compile-time checked. Lombok is used only for simple boilerplate such as getters, setters, constructors, and builders.

Purchasing is handled in one transactional service method. Products are loaded with a pessimistic write lock before stock is reduced, which keeps the implementation simple and avoids overselling in concurrent purchase requests. This is appropriate for the assignment scale; at much higher throughput, stock reservation or event-driven order processing would be considered.

Testcontainers is used for integration tests so the persistence behavior is verified against PostgreSQL instead of an in-memory substitute. Docker Compose provides the simplest local execution path for reviewers. OpenAPI/Swagger is included to make the API easy to inspect during the interview.

The frontend is intentionally minimal and static. The main deliverable remains the backend API, tests, Docker setup, and operational documentation.
