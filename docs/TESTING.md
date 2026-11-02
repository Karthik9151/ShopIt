# Testing

Local checks:
- Frontend: cd frontend && npm install && npm test -- --run && npm run build
- Backend: cd backend && mvn verify

Frontend unit test covers currency formatting. Backend has an application context smoke test using H2 and the test profile. GitHub Actions runs frontend tests/build and backend verification on pushes and pull requests.

Manual integration:
1. Start MySQL and configure environment variables.
2. Start backend; confirm /actuator/health returns UP.
3. Register a customer and sign in.
4. Provision the first admin using APP_ADMIN_EMAIL and APP_ADMIN_PASSWORD.
5. Use admin APIs/UI to add catalogue records.
6. As a customer, add products to cart and wishlist.
7. Checkout; verify stock decrement and cart clearing.
8. Simulate payment; verify the payment succeeds and order status changes.
9. Verify a customer cannot retrieve another customer's order, cart, wishlist or payment.
10. Verify customer tokens receive 403 from /api/admin.

A committed test or CI workflow is not evidence that a test passed. Check the workflow run for the current commit before reporting success.
