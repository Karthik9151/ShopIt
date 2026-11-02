# ShopIt

Full-stack e-commerce application using React/Vite, Java 21, Spring Boot 3.5, Spring Security/JWT, Spring Data JPA, Flyway, and MySQL 8.

## Local setup
Requirements: JDK 21, Maven 3.9+, Node 22.12+, npm, and MySQL 8.

1. Create a MySQL database named shopit and a user with privileges on it.
2. Copy .env.example to .env and export the values in your shell. Do not commit .env.
3. Start the API: cd backend && mvn spring-boot:run
4. Start the UI in another terminal: cd frontend && npm install && npm run dev
5. Open http://localhost:5173. API: http://localhost:8080. OpenAPI UI: /swagger-ui/index.html.

Flyway creates the schema. Set APP_ADMIN_EMAIL and APP_ADMIN_PASSWORD (minimum 12 characters) before first startup to provision an administrator. Existing account passwords are never reset by bootstrap.

## Payments
Only an explicitly simulated payment flow is included. It does not charge money or collect card details. Configure and verify a real provider server-side before enabling production payments.

## Checks
Frontend: npm test -- --run && npm run build
Backend: mvn verify

See docs for architecture, requirements, database design, endpoints, and the manual integration checklist.
