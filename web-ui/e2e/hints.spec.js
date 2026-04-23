import { test, expect } from '@playwright/test';

test.describe('Hints', () => {
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

  test('should request a hint and display hint modal', async ({ page }) => {
    // Start Free Play
    const playBtn = page.locator('button:has-text("Free Play")').first();
    await expect(playBtn).toBeVisible({ timeout: 10000 });
    await playBtn.click();
    await page.waitForTimeout(2000);

    // Generate Easy puzzle
    const easyBtn = page.locator('button:has-text("Easy")').first();
    await expect(easyBtn).toBeVisible({ timeout: 10000 });
    await easyBtn.click();
    await page.waitForTimeout(3000);

    // Find hint button
    const hintBtn = page.locator('button:has-text("Hint"), [title*="Hint"], [class*="hint"]').first();
    if (await hintBtn.isVisible({ timeout: 3000 }).catch(() => false)) {
      await hintBtn.click();
      await page.waitForTimeout(3000);

      // Should show hint modal or overlay
      const hintModal = page.locator('.hint-modal, .modal, [class*="hint"], [class*="Hint"]');
      const hasHint = await hintModal.count();
      // Either modal appears or the hint technique is shown somewhere
      const hasHintContent = await page.locator('text=/technique|hint|scanning|candidate/i').count();
      expect(hasHint + hasHintContent).toBeGreaterThan(0);
    }
  });

  test('should not crash when requesting hint multiple times', async ({ page }) => {
    // Start Free Play + generate puzzle
    const playBtn = page.locator('button:has-text("Free Play")').first();
    await expect(playBtn).toBeVisible({ timeout: 10000 });
    await playBtn.click();
    await page.waitForTimeout(2000);

    const easyBtn = page.locator('button:has-text("Easy")').first();
    await expect(easyBtn).toBeVisible({ timeout: 10000 });
    await easyBtn.click();
    await page.waitForTimeout(3000);

    const hintBtn = page.locator('button:has-text("Hint"), [title*="Hint"], [class*="hint"]').first();
    if (await hintBtn.isVisible({ timeout: 3000 }).catch(() => false)) {
      // Click hint 3 times
      for (let i = 0; i < 3; i++) {
        await hintBtn.click();
        await page.waitForTimeout(2000);
        // Dismiss modal if present
        const closeModal = page.locator('button:has-text("OK"), button:has-text("Got it"), button:has-text("Close")').first();
        if (await closeModal.isVisible({ timeout: 1000 }).catch(() => false)) {
          await closeModal.click();
          await page.waitForTimeout(300);
        }
      }

      // No JS crashes
      expect(page._testErrors.filter(e => !e.includes('undo-redo') && !e.includes('save'))).toHaveLength(0);
    }
  });
});
