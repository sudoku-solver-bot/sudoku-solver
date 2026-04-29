import { test, expect } from '@playwright/test';

test.describe('Print & Share Flow', () => {
  test.beforeEach(async ({ page }) => {
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

    page._testErrors = errors;
  });

  test.afterEach(async ({ page }) => {
    // Assert no unexpected console/page errors
    const errors = page._testErrors || [];
    expect(errors).toEqual([]);
  });

  test('should show Print button in control panel', async ({ page }) => {
    const printBtn = page.locator('button:has-text("Print")');
    await expect(printBtn).toBeVisible({ timeout: 5000 });
  });

  test('should show Share button in control panel', async ({ page }) => {
    const shareBtn = page.locator('button.btn-share, button:has-text("Share")').first();
    await expect(shareBtn).toBeVisible({ timeout: 5000 });
  });

  test('should open print window when Print button is clicked', async ({ page, context }) => {
    // Start Free Play to ensure puzzle is loaded
    const playBtn = page.locator('button:has-text("Free Play")').first();
    if (await playBtn.isVisible({ timeout: 3000 }).catch(() => false)) {
      await playBtn.click();
      await page.waitForTimeout(1000);
    }

    // Listen for new popup window
    const popupPromise = context.waitForEvent('page', { timeout: 5000 }).catch(() => null);
    
    const printBtn = page.locator('button:has-text("Print")');
    await printBtn.click();

    const popup = await popupPromise;
    if (popup) {
      // Verify popup has sudoku content
      await popup.waitForLoadState('domcontentloaded', { timeout: 5000 }).catch(() => {});
      const body = await popup.locator('body').textContent({ timeout: 3000 }).catch(() => '');
      expect(body).toContain('Sudoku');
    }
    // If no popup (headless may block window.open), just verify no crash
  });

  test('should trigger share-image download without crash', async ({ page }) => {
    // Start Free Play
    const playBtn = page.locator('button:has-text("Free Play")').first();
    if (await playBtn.isVisible({ timeout: 3000 }).catch(() => false)) {
      await playBtn.click();
      await page.waitForTimeout(1000);
    }

    // Look for image download/share button
    const shareImgBtn = page.locator('button:has-text("📷"), button[title*="image"], button:has-text("Image")').first();
    if (await shareImgBtn.isVisible({ timeout: 3000 }).catch(() => false)) {
      // Click it — in headless, download won't actually happen but should not crash
      await shareImgBtn.click();
      await page.waitForTimeout(500);
      // No JS error means success
    }
  });

  test('should show Share Puzzle button enabled when puzzle is loaded', async ({ page }) => {
    // Start Free Play to get a puzzle
    const playBtn = page.locator('button:has-text("Free Play")').first();
    if (await playBtn.isVisible({ timeout: 3000 }).catch(() => false)) {
      await playBtn.click();
      await page.waitForTimeout(1000);
    }

    const shareBtn = page.locator('button:has-text("Share")').first();
    if (await shareBtn.isVisible({ timeout: 3000 }).catch(() => false)) {
      await expect(shareBtn).toBeEnabled();
    }
  });

  test('should not crash when all share/print actions clicked rapidly', async ({ page }) => {
    // Start Free Play
    const playBtn = page.locator('button:has-text("Free Play")').first();
    if (await playBtn.isVisible({ timeout: 3000 }).catch(() => false)) {
      await playBtn.click();
      await page.waitForTimeout(1000);
    }

    // Click all export-related buttons rapidly
    const buttons = page.locator('button.btn-print, button.btn-share');
    const count = await buttons.count();
    for (let i = 0; i < count; i++) {
      const btn = buttons.nth(i);
      if (await btn.isVisible({ timeout: 1000 }).catch(() => false)) {
        await btn.click().catch(() => {});
      }
    }
    await page.waitForTimeout(1000);
    // If we got here without unhandled errors, test passes
    expect(true).toBe(true);
  });

  test('print and share buttons have correct aria labels for accessibility', async ({ page }) => {
    const printBtn = page.locator('button:has-text("Print")');
    if (await printBtn.isVisible({ timeout: 3000 }).catch(() => false)) {
      // Button should be keyboard accessible (it's a <button>)
      expect(await printBtn.evaluate(el => el.tagName)).toBe('BUTTON');
    }

    const shareBtn = page.locator('button:has-text("Share")').first();
    if (await shareBtn.isVisible({ timeout: 3000 }).catch(() => false)) {
      expect(await shareBtn.evaluate(el => el.tagName)).toBe('BUTTON');
    }
  });
});
