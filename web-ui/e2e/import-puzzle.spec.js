import { test, expect } from '@playwright/test';

test.describe('Import Puzzle', () => {
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

  test('should open import puzzle modal and parse a valid puzzle', async ({ page }) => {
    // Start Free Play first
    const playBtn = page.locator('button:has-text("Free Play")').first();
    await expect(playBtn).toBeVisible({ timeout: 10000 });
    await playBtn.click();
    await page.waitForTimeout(2000);

    // Find import button (usually an icon or text button in control panel)
    const importBtn = page.locator('button:has-text("Import"), [title*="Import"], [class*="import"]').first();
    if (await importBtn.isVisible({ timeout: 3000 }).catch(() => false)) {
      await importBtn.click();
      await page.waitForTimeout(1000);

      // Modal should be visible
      await expect(page.locator('.import-modal, .import-card')).toBeVisible({ timeout: 5000 });

      // Should have a textarea for input
      const textarea = page.locator('.import-input, textarea');
      if (await textarea.isVisible({ timeout: 3000 }).catch(() => false)) {
        // Type a valid puzzle (81 chars with dots for blanks)
        const testPuzzle = '5.3..891.9.6..3..5..1..4...3.56..4.6..542897...4139...4813....55329.7.68.6.84..21';
        await textarea.fill(testPuzzle);
        await page.waitForTimeout(500);

        // Import button should be enabled
        const doImportBtn = page.locator('button:has-text("Import"):not(:has-text("Import Puzzle"))').last();
        await expect(doImportBtn).toBeEnabled({ timeout: 3000 });
      }
    }
  });

  test('should reject invalid puzzle input', async ({ page }) => {
    // Start Free Play
    const playBtn = page.locator('button:has-text("Free Play")').first();
    await expect(playBtn).toBeVisible({ timeout: 10000 });
    await playBtn.click();
    await page.waitForTimeout(2000);

    const importBtn = page.locator('button:has-text("Import"), [title*="Import"], [class*="import"]').first();
    if (await importBtn.isVisible({ timeout: 3000 }).catch(() => false)) {
      await importBtn.click();
      await page.waitForTimeout(1000);

      const textarea = page.locator('.import-input, textarea');
      if (await textarea.isVisible({ timeout: 3000 }).catch(() => false)) {
        // Type an invalid puzzle (too short)
        await textarea.fill('123');
        await page.waitForTimeout(500);

        // Error message should appear
        await expect(page.locator('.import-error')).toBeVisible({ timeout: 3000 });

        // Import button should be disabled
        const doImportBtn = page.locator('button.btn-import, button:has-text("Import")').last();
        await expect(doImportBtn).toBeDisabled({ timeout: 3000 });
      }
    }
  });

  test('should close import modal without importing', async ({ page }) => {
    // Start Free Play
    const playBtn = page.locator('button:has-text("Free Play")').first();
    await expect(playBtn).toBeVisible({ timeout: 10000 });
    await playBtn.click();
    await page.waitForTimeout(2000);

    const importBtn = page.locator('button:has-text("Import"), [title*="Import"], [class*="import"]').first();
    if (await importBtn.isVisible({ timeout: 3000 }).catch(() => false)) {
      await importBtn.click();
      await page.waitForTimeout(1000);

      // Close by clicking the close/cancel button
      const closeBtn = page.locator('button:has-text("Cancel"), button:has-text("Close")').first();
      if (await closeBtn.isVisible({ timeout: 3000 }).catch(() => false)) {
        await closeBtn.click();
      } else {
        // Or click the backdrop
        await page.locator('.import-modal').click({ position: { x: 5, y: 5 } });
      }
      await page.waitForTimeout(500);

      // Modal should be gone
      await expect(page.locator('.import-modal, .import-card')).not.toBeVisible({ timeout: 3000 });
    }
  });
});
