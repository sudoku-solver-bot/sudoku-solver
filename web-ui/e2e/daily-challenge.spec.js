import { test, expect } from '@playwright/test';

test.describe('Daily Challenge', () => {
  test.beforeEach(async ({ page }) => {
    // Collect JS errors
    const errors = [];
    page.on('pageerror', err => errors.push(err.message));
    page.on('console', msg => {
      if (msg.type() === 'error') {
        const text = msg.text();
        if (!text.includes('favicon') && !text.includes('404') && !text.includes('Failed to load history state')) {
          errors.push(text);
        }
      }
    });

    await page.goto('http://localhost:25321/', { waitUntil: 'networkidle' });

    // Dismiss "What's New" modal if present
    const gotItBtn = page.locator('button:has-text("Let\'s play")');
    if (await gotItBtn.isVisible({ timeout: 3000 }).catch(() => false)) {
      await gotItBtn.click();
      await page.waitForTimeout(500);
    }

    // Store errors on page for assertions
    page._testErrors = errors;
  });

  test('should navigate to Daily Challenge from dashboard', async ({ page }) => {
    // Find and click the Daily Challenge button on dashboard
    const dailyBtn = page.locator('button.daily-btn, button:has-text("Daily")').first();
    await expect(dailyBtn).toBeVisible({ timeout: 10000 });
    await dailyBtn.click();
    await page.waitForTimeout(2000);

    // Should see Daily Challenge header
    await expect(page.locator('h2:has-text("Daily Challenge")')).toBeVisible({ timeout: 10000 });

    // Should see the calendar icon
    await expect(page.locator('.calendar-icon')).toBeVisible();

    // No JS errors
    expect(page._testErrors.filter(e => !e.includes('undo-redo'))).toHaveLength(0);
  });

  test('should load and display the daily puzzle grid', async ({ page }) => {
    // Navigate to Daily Challenge
    const dailyBtn = page.locator('button.daily-btn, button:has-text("Daily")').first();
    await expect(dailyBtn).toBeVisible({ timeout: 10000 });
    await dailyBtn.click();
    await page.waitForTimeout(3000);

    // Grid should be visible with 81 cells
    const cells = page.locator('.cell');
    const cellCount = await cells.count();
    expect(cellCount).toBe(81);

    // Timer should be running
    await expect(page.locator('.timer')).toBeVisible();
    await expect(page.locator('.timer-value')).toBeVisible();

    // Number pad should be present (1-9 + erase)
    const numBtns = page.locator('.num-btn');
    const btnCount = await numBtns.count();
    expect(btnCount).toBe(10); // 1-9 + backspace

    // Belt/badge info should show
    await expect(page.locator('.puzzle-info')).toBeVisible();
  });

  test('should exit Daily Challenge and return to dashboard', async ({ page }) => {
    // Navigate to Daily Challenge
    const dailyBtn = page.locator('button.daily-btn, button:has-text("Daily")').first();
    await expect(dailyBtn).toBeVisible({ timeout: 10000 });
    await dailyBtn.click();
    await page.waitForTimeout(2000);

    // Click back button
    const backBtn = page.locator('button.back-btn, button:has-text("Back")').first();
    await expect(backBtn).toBeVisible({ timeout: 5000 });
    await backBtn.click();
    await page.waitForTimeout(1000);

    // Should be back on dashboard (no Daily Challenge header)
    await expect(page.locator('h2:has-text("Daily Challenge")')).not.toBeVisible({ timeout: 5000 });

    // Dashboard elements should be visible again
    const dailyBtnAgain = page.locator('button.daily-btn, button:has-text("Daily")').first();
    await expect(dailyBtnAgain).toBeVisible({ timeout: 5000 });
  });
});
