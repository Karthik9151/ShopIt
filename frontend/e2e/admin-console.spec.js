import { expect, test } from '@playwright/test';

const adminUser = {
  id: 1,
  name: 'Test Owner',
  email: 'owner@example.test',
  role: 'ADMIN',
};

const customerUser = {
  id: 2,
  name: 'Test Customer',
  email: 'customer@example.test',
  role: 'CUSTOMER',
};

async function establishSession(page, user) {
  await page.addInitScript(
    ({ currentUser }) => {
      sessionStorage.setItem('shopit_access_token', 'playwright-only-token');
      sessionStorage.setItem('shopit_user', JSON.stringify(currentUser));
    },
    { currentUser: user },
  );
}

async function mockAdminApi(page) {
  await page.route('**/api/admin/**', async (route) => {
    const url = new URL(route.request().url());
    const path = url.pathname.replace('/api', '');

    if (path === '/admin/dashboard') {
      return route.fulfill({
        json: {
          customers: 1,
          products: 0,
          orders: 0,
          paidPayments: 0,
          verifiedSales: 0,
          simulatedPaymentValue: 0,
          pendingOrders: 0,
          lowStockProducts: 0,
          inventoryCostBudget: 0,
          potentialRetailValue: 0,
          estimatedGrossMargin: 0,
          salesByDay: [],
          statusBreakdown: [],
          topProducts: [],
          lowStockItems: [],
          recentOrders: [],
        },
      });
    }

    if (path === '/admin/orders') {
      return route.fulfill({ json: { content: [], number: 0, size: 20, totalElements: 0, totalPages: 0 } });
    }

    if (path === '/admin/products') {
      return route.fulfill({ json: { content: [], number: 0, size: 20, totalElements: 0, totalPages: 0 } });
    }

    if (path === '/admin/customers') {
      return route.fulfill({ json: { content: [], number: 0, size: 20, totalElements: 0, totalPages: 0 } });
    }

    if (path === '/admin/categories/paged') {
      return route.fulfill({ json: { content: [], number: 0, size: 20, totalElements: 0, totalPages: 0 } });
    }

    if (path === '/admin/inventory') {
      return route.fulfill({ json: { content: [], number: 0, size: 20, totalElements: 0, totalPages: 0 } });
    }

    if (path === '/admin/coupons/paged') {
      return route.fulfill({ json: { content: [], number: 0, size: 20, totalElements: 0, totalPages: 0 } });
    }

    return route.fulfill({ status: 404, json: { message: 'Unmocked admin route: ' + path } });
  });
}

test('customer accounts cannot enter nested administrator pages', async ({ page }) => {
  await establishSession(page, customerUser);
  await page.goto('/admin/customers/42');

  await expect(page.getByRole('heading', { name: 'Admin access required' })).toBeVisible();
  await expect(page.getByRole('link', { name: 'Back to shop' })).toHaveAttribute('href', '/products');
});

test('administrator can navigate to paged order and product workspaces', async ({ page }) => {
  await establishSession(page, adminUser);
  await mockAdminApi(page);

  await page.goto('/admin');
  await expect(page.getByRole('heading', { name: 'Store dashboard' })).toBeVisible();

  const navigation = page.getByRole('navigation', { name: 'Administrator navigation' });
  await navigation.getByRole('link', { name: 'Orders' }).click();
  await expect(page.getByRole('heading', { name: 'Orders', exact: true })).toBeVisible();
  await expect(page.getByText('No orders match these filters.')).toBeVisible();

  await navigation.getByRole('link', { name: 'Products' }).click();
  await expect(page.getByRole('heading', { name: 'Products', exact: true })).toBeVisible();
  await expect(page.getByText('No products found.')).toBeVisible();
});

test('administrator can open category and customer registers', async ({ page }) => {
  await establishSession(page, adminUser);
  await mockAdminApi(page);
  await page.goto('/admin/categories');

  await expect(page.getByRole('heading', { name: 'Categories', exact: true })).toBeVisible();
  await expect(page.getByText('No categories have been created.')).toBeVisible();

  await page.getByRole('navigation', { name: 'Administrator navigation' }).getByRole('link', { name: 'Customers' }).click();
  await expect(page.getByRole('heading', { name: 'Customers', exact: true })).toBeVisible();
  await expect(page.getByText('No customers found.')).toBeVisible();
});
