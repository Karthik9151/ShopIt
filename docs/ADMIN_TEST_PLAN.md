# Admin console test plan

## Preconditions

Use an isolated local database (preferred) or isolated staging service with disposable customer/product/order data. Never run this plan against production customer data. Configure a dedicated test administrator and a separate CUSTOMER account. Simulated payments must be explicitly enabled only in the isolated test profile.

## Access boundaries

1. Call `GET /api/admin/dashboard` with no token: expect 401.
2. Call the same endpoint with a CUSTOMER token: expect 403.
3. Call it with an ADMIN token: expect 200.
4. Confirm customer search never returns ADMIN accounts and customer detail for a non-customer ID returns 404.
5. Confirm public product APIs never return exact stock or costPrice.

## Products and inventory

1. Create a disposable product using the admin product endpoint.
2. Change stock with `PATCH /api/admin/products/{id}/stock`; verify 0 and 1,000,000 are accepted and -1 or 1,000,001 are rejected.
3. Verify public product views use `inStock` and `lowStock` only.
4. Deactivate the product and confirm it disappears from public search/detail.
5. Verify list pagination defaults to 20 and rejects sizes above 100.

## Categories

1. Create a category and assign a product to it.
2. Read `GET /api/admin/categories`; verify inactive categories are present and `productCount` matches.
3. Deactivate the category. Confirm it disappears from public categories and assigned products disappear from public product list/detail.
4. Reactivate via `PATCH /api/admin/categories/{id}/status`; verify product/category visibility is restored.

## Customers

1. Register a disposable customer.
2. Search via `GET /api/admin/customers?q=...` using name, email, and phone fragments.
3. Open `GET /api/admin/customers/{id}`; verify only the explicit customer DTO is returned.
4. Enable/disable the customer. Confirm a disabled customer cannot authenticate.
5. Confirm customer search never returns admin bootstrap users.

## Orders, coupons and payment

The current admin API supports order paging/status filtering and permitted status transitions. The complete requested date/order-id/email filters, CSV export, and grouped dashboard analytics are not implemented in this branch; do not mark them passed.

1. Use an isolated customer cart, create a coupon, and preview then check out with it.
2. Verify the backend calculates subtotal, discount, shipping, tax and total.
3. Confirm an unpaid order cannot be moved to CONFIRMED without successful payment.
4. Simulate payment only when the test profile enables the simulation switch; verify every UI/API description labels it as simulated.
5. Move the paid order only through CONFIRMED → PROCESSING → SHIPPED → DELIVERED.
6. Confirm an illegal transition and paid-order cancellation both return 409.
7. Confirm status history records each transition and the customer can see the timeline.
8. For an unpaid order older than 30 minutes, verify automatic cancellation restores stock and coupon usage exactly once.

## Frontend review still required

Verify every route on 360 px and desktop widths, keyboard-only navigation, destructive-action confirmation, visible loading/empty/error states, AA color contrast, and page titles. The full AdminLayout/shared component split and all Playwright scenarios are not yet complete in this branch; manual inspection cannot be substituted for the missing e2e suite.
