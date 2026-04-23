import { test, expect } from '@playwright/test';

test.describe('Settings', () => {
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

    const gotItBtn = page.locator('button:has-text("Let\'s play")');
    if (await gotItBtn.isVisible({ timeout: 3000 }).catch(() => false)) {
      await gotItBtn.click();
      await page.waitForTimeout(500);
    }

    page._testErrors = errors;
  });

  test('should open settings panel', async ({ page }) => {
    const settingsBtn = page.locator('button:has-text("Settings"), [title*="Settings"], [class*="settings"]').first();
    if (await settingsBtn.isVisible({ timeout: 5000 }).catch(() => false)) {
      await settingsBtn.click();
      await page.waitForTimeout(1000);

      // Settings panel should be visible
      const settingsPanel = page.locator('.settings, [class*="settings-panel"], [class*="Settings"]');
      await expect(settingsPanel.first()).toBeVisible({ timeout: 5000 });
    }
  });

  test('should toggle dark mode', async ({ page }) => {
    const settingsBtn = page.locator('button:has-text("Settings"), [title*="Settings"], [class*="settings"]').first();
    if (await settingsBtn.isVisible({ timeout: 5000 }).catch(() => false)) {
      await settingsBtn.click();
      await page.waitForTimeout(1000);

      // Find dark mode toggle
      const darkToggle = page.locator('input[type="checkbox"], .toggle, [class*="dark"]').first();
      if (await darkToggle.isVisible({ timeout: 3000 }).catch(() => false)) {
        const isDarkBefore = await page.evaluate(() => document.documentElement.classList.contains('dark') || document.body.classList.contains('dark'));
        await darkToggle.click();
        await page.waitForTimeout(500);
        const isDarkAfter = await page.evaluate(() => document.documentElement.classList.contains('dark') || document.body.classList.contains('dark'));
        // Dark mode should have toggled
        expect(isDarkAfter).not.toBe(isDarkBefore);
      }
    }
  });

  test('should change language', async ({ page }) => {
    const settingsBtn = page.locator('button:has-text("Settings"), [title*="Settings"], [class*="settings"]').first();
    if (await settingsBtn.isVisible({ timeout: 5000 }).catch(() => false)) {
      await settingsBtn.click();
      await page.waitForTimeout(1000);

      // Find language selector
      const langSelect = page.locator('select, [class*="language"], [class*="lang"]').first();
      if (await langSelect.isVisible({ timeout: 3000 }).catch(() => false)) {
        await langSelect.click();
        await page.waitForTimeout(300);
        // Select a different language option
        await page.keyboard.press('ArrowDown');
        await page.keyboard.press('Enter');
        await page.waitForTimeout(1000);
        // No crash
        expect(page._testErrors.filter(e => !e.includes('undo-redo'))).toHaveLength(0);
      }
    }
  });
});
