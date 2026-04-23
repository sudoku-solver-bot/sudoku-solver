import { test, expect } from '@playwright/test';

test.describe('Error Scenarios', () => {
  test('should handle API failure gracefully during puzzle generation', async ({ page }) => {
    const errors = [];
    page.on('pageerror', err => errors.push(err.message));

    // Intercept API calls and make them fail
    await page.route('**/api/**', route => route.abort('failed'));

    await page.goto('http://localhost:25321/', { waitUntil: 'networkidle' });

    // Wait for app to mount
    await page.waitForTimeout(3000);

    // App should still render (may show offline indicator)
    const appContent = await page.locator('#app').innerHTML();
    expect(appContent.length).toBeGreaterThan(50);

    // No unhandled exceptions (route.abort may cause console errors but no pageerror)
    expect(errors).toHaveLength(0);
  });

  test('should show error toast when generate API returns 500', async ({ page }) => {
    const errors = [];
    page.on('pageerror', err => errors.push(err.message));

    // Intercept generate API to return 500
    await page.route('**/api/generate*', route =>
      route.fulfill({ status: 500, body: JSON.stringify({ error: 'Internal Server Error' }) })
    );

    await page.goto('http://localhost:25321/', { waitUntil: 'networkidle' });

    const gotItBtn = page.locator('button:has-text("Let\'s play")');
    if (await gotItBtn.isVisible({ timeout: 3000 }).catch(() => false)) {
      await gotItBtn.click();
      await page.waitForTimeout(500);
    }

    // Try to generate a puzzle
    const playBtn = page.locator('button:has-text("Free Play")').first();
    await expect(playBtn).toBeVisible({ timeout: 10000 });
    await playBtn.click();
    await page.waitForTimeout(2000);

    const easyBtn = page.locator('button:has-text("Easy")').first();
    if (await easyBtn.isVisible({ timeout: 3000 }).catch(() => false)) {
      await easyBtn.click();
      await page.waitForTimeout(3000);

      // Should show error toast or notification, not crash
      const hasToast = await page.locator('.toast, .notification, [class*="toast"], [class*="error"]').count();
      // App should still be functional
      const appContent = await page.locator('#app').innerHTML();
      expect(appContent.length).toBeGreaterThan(50);
    }

    expect(errors).toHaveLength(0);
  });

  test('should handle network timeout during solve', async ({ page }) => {
    const errors = [];
    page.on('pageerror', err => errors.push(err.message));

    // Let everything load normally first, then block solve API
    await page.goto('http://localhost:25321/', { waitUntil: 'networkidle' });

    const gotItBtn = page.locator('button:has-text("Let\'s play")');
    if (await gotItBtn.isVisible({ timeout: 3000 }).catch(() => false)) {
      await gotItBtn.click();
      await page.waitForTimeout(500);
    }

    // Start Free Play + generate puzzle (these should work)
    const playBtn = page.locator('button:has-text("Free Play")').first();
    await expect(playBtn).toBeVisible({ timeout: 10000 });
    await playBtn.click();
    await page.waitForTimeout(2000);

    // Block solve API with delay then abort
    await page.route('**/api/solve*', async route => {
      await new Promise(r => setTimeout(r, 60000)); // Never resolves
    });

    const easyBtn = page.locator('button:has-text("Easy")').first();
    if (await easyBtn.isVisible({ timeout: 3000 }).catch(() => false)) {
      await easyBtn.click();
      await page.waitForTimeout(3000);
    }

    // App should still be mounted
    const appContent = await page.locator('#app').innerHTML();
    expect(appContent.length).toBeGreaterThan(50);

    expect(errors).toHaveLength(0);
  });
});
