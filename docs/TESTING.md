# Testing

## Required local commands

Install frontend dependencies once from `frontend` with `npm install` (there is currently no committed package-lock file). Then run:

```sh
cd frontend && npm test -- --run && npm run build
```

Run backend verification, including unit, context and integration tests:

```sh
cd backend && mvn -B verify
```

GitHub Actions also builds the backend Docker image. Verify the workflow run attached to the exact PR head commit before reporting success; a workflow file or an older commit's successful run is not evidence for the current head.

## Automated coverage

- Frontend Vitest: INR/date formatting, GET-only transient retry policy, unauthenticated admin-route redirect, and sign-in-to-registration navigation.
- Backend unit tests: pricing boundaries/rounding, order-state transitions, payment simulation switch, rate-limit buckets, unpaid-order expiration and application context.
- MySQL 8.4 integration tests in CI: authentication/admin reservation, customer ownership boundaries, stock disclosure, coupons/redemption, checkout totals, cancellation/status and admin pagination.
- CI generates ephemeral JWT/admin test values per run. Backend integration tests read the admin identity from environment variables; never embed credentials in source or log them.

## Manual staging smoke test

Use isolated staging data and test identities; never use production customer records, production orders or real payment credentials.

1. Start the API; verify health/readiness.
2. Register a customer and sign in; confirm the configured administrator email cannot be claimed through customer registration.
3. As admin, create/edit/deactivate products and categories; verify private purchase cost and exact stock are admin-only.
4. Verify admin product pagination, search/active filters, Page metadata, page navigation and maximum size 100.
5. Verify admin order pagination, status filter and invalid page/size handling.
6. Confirm dashboard valuation includes inactive stock and is independent of the current page.
7. With two customers, verify cart, wishlist, profile, addresses, payments and orders cannot be accessed across accounts.
8. Place an order with an idempotency request ID; verify server-calculated subtotal, discount, shipping, tax, total, stock decrement and cart clearing.
9. Simulate payment only in the explicitly enabled staging environment; confirm it is labelled simulated and excluded from verified-sales totals.
10. Cancel an unpaid order and verify stock restoration/timeline; confirm paid-order cancellation is rejected.
11. Verify logout clears browser session state but does not revoke a copied token before expiry; password change also does not revoke existing tokens.
12. Verify Swagger/OpenAPI routes are 404 by default and enabled only when explicitly turned on in trusted local/test use.

## Evidence boundaries

- CI evidence applies only to the exact commit SHA attached to the workflow run.
- Passing MySQL 8.4 CI does not prove TiDB compatibility; see `docs/TIDB_VERIFICATION.md` for the isolated staging owner matrix.
- No Render, TiDB or production database action is part of this guide.
