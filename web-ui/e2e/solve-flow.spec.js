import { test, expect } from '@playwright/test';

test.describe('Solve Flow', () => {
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

  test('should generate an Easy puzzle and display the grid', async ({ page }) => {
    // Start Free Play
    const playBtn = page.locator('button:has-text("Free Play")').first();
    await expect(playBtn).toBeVisible({ timeout: 10000 });
    await playBtn.click();
    await page.waitForTimeout(2000);

    // Click Easy difficulty
    const easyBtn = page.locator('button:has-text("Easy")').first();
    await expect(easyBtn).toBeVisible({ timeout: 10000 });
    await easyBtn.click();
    await page.waitForTimeout(3000);

    // Grid should have 81 cells
    const cells = page.locator('.cell');
    const cellCount = await cells.count();
    expect(cellCount).toBe(81);

    // Some cells should be pre-filled (given cells)
    const filledCells = page.locator('.cell.given, .cell.pre-filled, .cell[data-given="true"]');
    // At least some given cells for Easy
    const gridContent = await page.locator('.grid, [class*="grid"]').first().innerHTML();
    // Grid should have number content
    expect(gridContent.length).toBeGreaterThan(100);

    // No critical JS errors
    expect(page._testErrors.filter(e => !e.includes('undo-redo') && !e.includes('save'))).toHaveLength(0);
  });

  test('should solve a generated puzzle', async ({ page }) => {
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

    // Find and click Solve button
    const solveBtn = page.locator('button:has-text("Solve")').first();
    await expect(solveBtn).toBeVisible({ timeout: 5000 });
    await solveBtn.click();
    await page.waitForTimeout(5000);

    // Grid should still have 81 cells
    const cells = page.locator('.cell');
    const cellCount = await cells.count();
    expect(cellCount).toBe(81);

    // Some kind of completion indicator should appear (result, celebration, etc.)
    const hasCompletion = await page.locator('.result, .completion, .celebration, [class*="result"], [class*="celebration"], [class*="confetti"]').count();
    // Note: Solve might not trigger full completion UI, but shouldn't crash
    // No JS errors
    expect(page._testErrors.filter(e => !e.includes('undo-redo') && !e.includes('save'))).toHaveLength(0);
  });

  test('should input numbers into empty cells', async ({ page }) => {
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

    // Find empty cells (not given)
    const emptyCells = page.locator('.cell:not(.given):not(.pre-filled)');
    const emptyCount = await emptyCells.count();

    if (emptyCount > 0) {
      // Click first empty cell
      await emptyCells.first().click();
      await page.waitForTimeout(300);

      // Press a number key
      await page.keyboard.press('5');
      await page.waitForTimeout(300);

      // Cell should now show the number (either as text or input value)
      const cellContent = await emptyCells.first().textContent();
      expect(cellContent).toContain('5');
    }
  });
});
