# Testing

Local checks:
- Frontend: `cd frontend && npm install && npm test -- --run && npm run build`
- Backend: `cd backend && mvn verify`

The frontend unit test covers INR currency formatting. Backend currently has an application-context smoke test using H2. GitHub Actions runs frontend tests/build and Maven verification on feature-branch pushes and pull requests.

Manual integration:
1. Start MySQL and configure DB/JWT environment variables. Generate the JWT key with `openssl rand -base64 32`.
2. Start the backend; confirm `/actuator/health` reports UP.
3. Register a customer and sign in.
4. Provision the first admin using APP_ADMIN_EMAIL and APP_ADMIN_PASSWORD.
5. As an admin, add/edit/deactivate products and categories.
6. As an admin, verify customer list and enable/disable operations.
7. As a customer, update profile details, change password, and create a saved delivery address.
8. Add products to cart and wishlist; verify another account cannot access these records.
9. Place an order using a saved address; verify server-calculated totals, price snapshots, stock decrement and cart clearing.
10. Simulate payment; verify the response is labelled SIMULATED, the order status changes, and admin simulated value does not appear in real verified sales.
11. Verify a customer cannot retrieve another customer's order, payment, profile, address, cart, or wishlist.
12. Verify customer tokens receive 403 from /api/admin and disabled customer tokens cannot authenticate.
13. Verify unpaid customer/admin cancellation restores stock; paid-order cancellation is blocked without refund handling.

A committed test or CI workflow is not evidence that the relevant test passed. Check the workflow run associated with the current commit before reporting success.
