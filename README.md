# ShopIt

A full-stack e-commerce application built with React/Vite, Java 21, Spring Boot 3.5, Spring Security/JWT, Spring Data JPA, Flyway, and MySQL 8.

## Local setup

Requirements: JDK 21, Maven 3.9+, Node.js 22.12+, npm, and MySQL 8.

1. Create a MySQL database named `shopit` and a database user with privileges on it.
2. Copy `.env.example` to `.env`. Generate a JWT key and fill in local values:
   ```bash
   openssl rand -base64 32
   ```
   Set the output as `JWT_SECRET_BASE64`. The backend intentionally refuses to start with a missing/weak JWT key.
3. Export the environment values in your shell (or use your IDE's environment configuration).
4. Start the API: `cd backend && mvn spring-boot:run`.
5. Start the UI in another terminal: `cd frontend && npm install && npm run dev`.
6. Open `http://localhost:5173`. API: `http://localhost:8080`. OpenAPI UI: `http://localhost:8080/swagger-ui/index.html`.

Flyway creates the schema and seeds a small sample catalogue. Set `APP_ADMIN_EMAIL` and `APP_ADMIN_PASSWORD` (minimum 12 characters) before first startup to provision an administrator. Bootstrap does not overwrite existing accounts.

## Payments

Only an explicitly simulated payment workflow is included. It does not charge money or collect card details. The admin dashboard separates simulated payment value from real verified sales. Integrate and verify a real provider server-side before enabling live payments.

## Checks

Frontend: `cd frontend && npm install && npm test -- --run && npm run build`

Backend: `cd backend && mvn verify`

See `docs/` for architecture, requirements, database design, endpoints, security notes, and manual integration checks.
