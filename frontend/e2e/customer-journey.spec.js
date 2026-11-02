import { expect, test } from '@playwright/test';

test('customer can register, add a product, place an order and review order history', async ({ page }) => {
  const customer = {
    id: 41,
    name: 'Journey Customer',
    email: 'journey@example.test',
    phone: '9999999999',
    role: 'CUSTOMER',
  };
  const product = {
    id: 73,
    name: 'Everyday Ceramic Cup',
    description: 'A simple ceramic cup.',
    categoryName: 'Home',
    price: 1000,
    stock: 5,
    availableStock: 5,
    inStock: true,
    lowStock: false,
    imageUrl: 'https://example.test/cup.png',
  };
  const cart = {
    items: [{
      id: 17,
      productId: product.id,
      name: product.name,
      imageUrl: product.imageUrl,
      unitPrice: 1000,
      quantity: 1,
      lineTotal: 1000,
      available: true,
      availableStock: 5,
    }],
    subtotal: 1000,
    shipping: 49,
    tax: 180,
    total: 1229,
    freeShippingRemaining: 999,
  };
  const order = {
    id: 501,
    status: 'PENDING_PAYMENT',
    createdAt: '2026-11-02T10:00:00Z',
    shippingAddress: '12 Example Street, Hyderabad, Telangana, 500001, India',
    items: [{
      productId: product.id,
      productName: product.name,
      quantity: 1,
      unitPrice: 1000,
      lineTotal: 1000,
    }],
    subtotal: 1000,
    discountAmount: 0,
    shipping: 49,
    tax: 180,
    total: 1229,
    couponCode: null,
    timeline: [],
    simulatedPaymentEnabled: false,
  };
  let paymentSimulationRequests = 0;
  let registered = false;

  await page.route('**/api/**', async (route) => {
    const request = route.request();
    const method = request.method();
    const path = new URL(request.url()).pathname.replace(/^\/api/, '');

    if (method === 'POST' && path === '/auth/register') {
      registered = true;
      return route.fulfill({ json: { token: 'playwright-customer-token', user: customer } });
    }
    if (method === 'GET' && path === '/categories') {
      return route.fulfill({ json: [] });
    }
    if (method === 'GET' && path === '/products') {
      return route.fulfill({
        json: { content: [product], number: 0, size: 24, totalElements: 1, totalPages: 1 },
      });
    }
    if (method === 'POST' && path === '/cart/items') {
      return route.fulfill({ status: 201, json: cart });
    }
    if (method === 'GET' && path === '/cart') {
      return route.fulfill({ json: cart });
    }
    if (method === 'GET' && path === '/addresses') {
      return route.fulfill({ json: [] });
    }
    if (method === 'POST' && path === '/orders') {
      return route.fulfill({ status: 201, json: order });
    }
    if (method === 'GET' && path === '/orders/501') {
      return route.fulfill({ json: order });
    }
    if (method === 'GET' && path === '/orders/my') {
      return route.fulfill({ json: [order] });
    }
    if (path === '/payments/simulate/501') {
      paymentSimulationRequests += 1;
      return route.fulfill({ status: 409, json: { message: 'Simulation is disabled.' } });
    }

    return route.fulfill({
      status: 404,
      json: { message: 'Unmocked customer journey request: ' + method + ' ' + path },
    });
  });

  await page.goto('/register');
  await page.getByLabel('Your name').fill(customer.name);
  await page.getByLabel('Email address').fill(customer.email);
  await page.getByLabel('Phone (optional)').fill(customer.phone);
  await page.getByLabel('Password').fill('JourneyPassword!2026');
  await page.getByRole('button', { name: 'Create account' }).click();

  await expect(page.getByRole('heading', { name: 'The collection' })).toBeVisible();
  expect(registered).toBe(true);
  await page.getByRole('button', { name: 'Add' }).click();
  await expect(page.getByRole('status')).toContainText('Added to your bag');

  await page.getByRole('link', { name: 'Shopping bag' }).click();
  await expect(page.getByRole('heading', { name: 'Your bag' })).toBeVisible();
  await expect(page.getByText(product.name)).toBeVisible();
  await page.getByRole('button', { name: /Continue to checkout/ }).click();

  await expect(page.getByRole('heading', { name: 'Checkout' })).toBeVisible();
  await page.getByLabel('Full delivery address').fill(order.shippingAddress);
  await page.getByRole('button', { name: 'Place order' }).click();

  await expect(page.getByRole('heading', { name: 'Order details' })).toBeVisible();
  await expect(page.getByText(/Simulated payment is disabled in this environment/)).toBeVisible();
  await expect(page.getByText(order.shippingAddress)).toBeVisible();
  expect(paymentSimulationRequests).toBe(0);

  await page.getByRole('link', { name: 'Your orders' }).click();
  await expect(page.getByRole('heading', { name: 'Your orders' })).toBeVisible();
  await expect(page.getByText('ORDER #501')).toBeVisible();
  expect(paymentSimulationRequests).toBe(0);
});
