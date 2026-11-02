# Testing

## Frontend commands

Run from the frontend directory:

```sh
npm install
npm run lint
npm audit --audit-level=high
npm test -- --run
npm run build
npx playwright install chromium
npm run test:e2e
```

The repository currently has no committed `frontend/package-lock.json`, so CI uses `npm install`; reproducible `npm ci` is not available until a reviewed lockfile is committed. Vitest excludes `frontend/e2e/**` so Playwright specs are not loaded by the unit-test runner. Playwright uses its own config and Chromium browser.

## Backend commands

Run from the backend directory:

```sh
mvn --batch-mode verify
```

The Maven verify lifecycle runs unit, context and integration tests. CI provisions MySQL 8.4, generates ephemeral test-only JWT/admin credentials, runs Maven verification, and builds the backend Docker image.

## Verified GitHub Actions run

Verified PR head: `32c4b233902028634d923107acffa8b4646cb4a1`.

| Workflow | Result |
|---|---|
| ShopIt CI `41483344271` | Passed |
| CodeQL `41483344120` | Java/Kotlin and JavaScript/TypeScript passed |
| Dependency review `41483344287` | Passed |

ShopIt CI results:
- Frontend ESLint passed.
- `npm audit --audit-level=high` passed.
- Vitest: 39 tests passed across 11 files.
- Vite production build passed.
- Playwright Chromium: 4 browser tests passed.
- Backend Maven/H2/MySQL 8.4 verification: 73 tests passed, 0 failures/errors.
- Backend container build passed.

Always inspect workflow runs attached to the latest PR head SHA. A successful workflow run on an older SHA does not validate newer code.

## Automated coverage

Frontend Vitest includes route guard, sign-in navigation, shared UI component, formatter and API-client regressions. Playwright covers non-admin access blocking, administrator navigation, and a mocked customer journey through registration, product add, cart, checkout, order details and order history. The customer flow asserts that disabled simulated payments do not trigger the payment endpoint. These Playwright tests stub API responses; backend API behavior is verified separately by Maven integration tests against MySQL 8.4. A full browser-to-live-backend staging journey is still an owner-operated check.

Backend tests cover:
- pricing boundaries, tax/shipping/discount calculation and rounding;
- order transitions, paid-order restrictions and unpaid-order expiration;
- authentication, administrator identity reservation and customer ownership;
- stock disclosure/updates and inactive-category visibility;
- coupon validation/redemption and checkout totals;
- dashboard reporting, customer order history, admin pagination, search and detail endpoints;
- CSV output escaping/formula neutralization and export authorization;
- CSRF request boundaries, metadata-only request logging, rate limiting and API surface configuration;
- demo-profile database-host allowlisting and runtime configuration logging.

## Manual isolated-staging smoke test

Use disposable staging identities and test data only. Do not use production accounts, live customer data or real payment credentials.

1. Verify health/readiness and confirm the API starts with valid staging-only environment settings.
2. Register a customer, sign in, then verify customer requests to `/api/admin/**` are rejected.
3. Sign in as the configured administrator and verify dashboard analytics and owner workspaces load.
4. Create a disposable category/product/coupon; verify create, edit, status, filtering and pagination.
5. Verify exact stock and purchase cost are visible only to administrators.
6. Verify customer detail and paginated order history show only the selected customer's records.
7. Create an order with a request ID; verify server-calculated totals, stock update and cart behavior.
8. Exercise simulated payment only when explicitly enabled in isolated staging; verify simulated results are excluded from verified-sales reporting.
9. Verify unpaid cancellation/expiry restores stock and paid-order cancellation is rejected.
10. Verify logout clears browser session state, while documenting that copied JWTs are not revoked before expiry.
11. Verify API docs remain disabled unless explicitly enabled for trusted local/test use.
12. Verify mobile layout, keyboard navigation, focus visibility and responsive admin navigation.
13. In a local demo profile, verify a loopback MySQL URL is accepted and a remote datasource URL is rejected before datasource/Flyway initialization.

## Evidence boundaries

MySQL 8.4 CI does not establish TiDB compatibility. Follow `docs/TIDB_VERIFICATION.md` for the owner-operated isolated TiDB matrix. CI does not measure 512 MB target-instance memory behavior. No Render deployment or production database operation is part of these tests.
