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

The repository currently has no committed frontend package-lock.json, so use npm install; npm ci is not yet reproducible from a checked-in lockfile. Vitest excludes frontend/e2e so Playwright specs are not loaded by the unit-test runner. Playwright uses its own config and Chromium browser.

## Backend commands

Run from the backend directory:

```sh
mvn --batch-mode verify
```

The Maven verify lifecycle runs unit/context and integration tests. CI provisions MySQL 8.4, generates ephemeral test-only JWT/admin credentials, runs Maven verification, and builds the backend Docker image.

## CI verification

The ShopIt CI workflow runs frontend dependency installation, ESLint, npm audit, Vitest, production build, Chromium installation and Playwright smoke tests. Its backend job runs Maven verification against MySQL 8.4 and builds the backend container. Dependency review and CodeQL execute as separate pull-request checks.

Always inspect the workflow runs attached to the latest PR head SHA. A successful backend run on one SHA does not validate frontend changes on another SHA, and a previous successful workflow run does not validate the current PR head.

## Automated coverage

Frontend Vitest includes route guard, navigation, shared UI component and formatting/API-client regressions. Playwright currently checks that a customer cannot enter nested admin routes and that an administrator can navigate the orders, products, categories and customer workspaces. It does not yet automate full storefront checkout, payment or account lifecycle journeys.

Backend tests cover:
- pricing boundaries, tax/shipping/discount calculation and rounding;
- order status transitions, paid-order restrictions and unpaid-order expiration;
- authentication, reserved administrator identity and customer ownership;
- stock disclosure, stock updates and inactive-category visibility;
- coupon validation/redemption and checkout totals;
- dashboard reporting, customer order history, admin pagination, search and detail endpoints;
- CSV output escaping/formula neutralization and export authorization;
- CSRF request boundaries, request logging, rate limiting and API surface configuration.

## Manual isolated-staging smoke test

Use disposable staging identities and test data only. Do not use production accounts, live customer data or real payment credentials.

1. Verify health/readiness and confirm the API starts with valid staging-only environment settings.
2. Register a customer, sign in, then verify customer requests to /api/admin/** are rejected.
3. Sign in as the configured administrator and verify the dashboard analytics and each owner workspace load.
4. Create a disposable category/product/coupon; verify create, edit, status, filtering and pagination behavior.
5. Verify product exact stock and purchase cost are visible only to administrators.
6. Verify customer detail and paginated order history show only the selected customer's records.
7. Create an order with a request ID; verify the server calculates all totals and updates stock/cart state.
8. Exercise simulated payment only when explicitly enabled in isolated staging; verify simulated results are not counted as verified sales.
9. Verify unpaid cancellation/expiry restores stock and paid-order cancellation is rejected.
10. Verify logout clears browser session state, while documenting that copied JWTs are not revoked before expiry.
11. Verify API docs are disabled unless explicitly enabled for trusted local/test use.
12. Verify mobile layout, keyboard navigation, focus visibility and responsive admin navigation.

## Evidence boundaries

MySQL 8.4 CI does not establish TiDB compatibility. Follow docs/TIDB_VERIFICATION.md for the owner-operated isolated TiDB matrix. CI does not measure 512 MB target-instance memory behavior. No Render deployment or production database operation is part of these tests.
