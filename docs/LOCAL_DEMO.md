# Local-only demo profile

This profile is intended for a developer's local workstation with a disposable MySQL database. It is not a hosted demo profile and must not be activated in Render, production, or a shared environment.

## What the profile changes

- Uses a local database named `shopit_demo` by default; `DB_URL`, `DB_USERNAME`, and `DB_PASSWORD` can be supplied through the local process environment.
- Restricts the default CORS origin to `http://localhost:5173`.
- Enables Swagger/OpenAPI for local API exploration.
- Keeps simulated payments disabled by default. The application default is also disabled. The Render staging-only payment switch remains owner-managed and is not set by this profile.
- Does not seed demo accounts with known passwords and does not disable the API rate limiter.

## Start the local demo

1. Start a local MySQL 8 instance and create a disposable database named `shopit_demo`.
2. Supply a local database URL/user/password through your workstation's secret manager or process environment. Do not commit a local `.env` file.
3. Generate a fresh JWT signing key for this process and supply owner-chosen admin values in the local environment. Keep the password hidden while entering it; do not paste it into source files or terminal screenshots.
4. From the backend directory, activate the `demo` profile and start Spring Boot:

   ```sh
   export SPRING_PROFILES_ACTIVE=demo
   export JWT_SECRET_BASE64="$(openssl rand -base64 32 | tr -d '\n')"
   export APP_PAYMENTS_SIMULATED_ENABLED=false
   cd backend && mvn spring-boot:run
   ```

   Supply `APP_ADMIN_EMAIL`, `APP_ADMIN_PASSWORD`, `DB_URL`, `DB_USERNAME` and `DB_PASSWORD` through the local environment before launch. Admin passwords must meet the application bootstrap policy; use a unique value and never reuse a production password.

5. In another terminal, run the frontend with the local API base URL and open `http://localhost:5173`.
6. Use only disposable sample data. Remove the local demo database when the exercise is complete.

## Safety rules

- Do not set `SPRING_PROFILES_ACTIVE=demo` in Render or a production environment.
- Do not copy production database URLs, admin identities, JWT secrets or real customer data into this profile.
- Do not enable simulated payment in a local demo. Decision D8 reserves the simulated-payment environment switch for owner-managed Render staging and keeps it disabled by default.
- The profile does not prove the TiDB staging database has been verified. See `docs/TIDB_VERIFICATION.md` for the separate owner-run compatibility procedure.
- This profile does not make a production deployment, create a real payment provider, or change repository/hosting settings.
