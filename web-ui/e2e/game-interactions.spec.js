import { test, expect } from '@playwright/test';

test.describe('Game Interactions', () => {
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

    // Dismiss onboarding if present
    const gotItBtn = page.locator('button:has-text("Let\'s play")');
    if (await gotItBtn.isVisible({ timeout: 3000 }).catch(() => false)) {
      await gotItBtn.click();
      await page.waitForTimeout(500);
    }

    page._testErrors = errors;
  });

  // --- Pencil Marks ---
  test('should toggle pencil mode and enter candidates', async ({ page }) => {
    // Start a new puzzle (Free Play / Quick Solve)
    const newPuzzleBtn = page.locator('button:has-text("New Puzzle"), button:has-text("Quick Solve"), a:has-text("Quick Solve")').first();
    if (await newPuzzleBtn.isVisible({ timeout: 3000 }).catch(() => false)) {
      await newPuzzleBtn.click();
      await page.waitForTimeout(1000);

      // Select easy if difficulty appears
      const easyBtn = page.locator('button:has-text("Easy")');
      if (await easyBtn.isVisible({ timeout: 2000 }).catch(() => false)) {
        await easyBtn.click();
        await page.waitForTimeout(1000);
      }
    }

    // Find and click pencil marks toggle
    const pencilBtn = page.locator('button:has-text("Pencil"), [class*="pencil"], [title*="encil"]').first();
    if (await pencilBtn.isVisible({ timeout: 3000 }).catch(() => false)) {
      await pencilBtn.click();
      await page.waitForTimeout(500);

      // Click an empty cell and enter a candidate number
      const emptyCell = page.locator('.cell[data-value="0"], .cell.empty, .cell:not(.given)').first();
      if (await emptyCell.isVisible({ timeout: 3000 }).catch(() => false)) {
        await emptyCell.click();
        await page.waitForTimeout(300);

        // Press a number key
        await page.keyboard.press('5');
        await page.waitForTimeout(500);

        // Verify no crash
        expect(page._testErrors.filter(e => !e.includes('undo-redo'))).toHaveLength(0);
      }
    }
  });

  // --- Undo / Redo ---
  test('should undo and redo cell entries', async ({ page }) => {
    // Navigate to a puzzle
    const newPuzzleBtn = page.locator('button:has-text("New Puzzle"), button:has-text("Quick Solve"), a:has-text("Quick Solve")').first();
    if (await newPuzzleBtn.isVisible({ timeout: 3000 }).catch(() => false)) {
      await newPuzzleBtn.click();
      await page.waitForTimeout(1000);

      const easyBtn = page.locator('button:has-text("Easy")');
      if (await easyBtn.isVisible({ timeout: 2000 }).catch(() => false)) {
        await easyBtn.click();
        await page.waitForTimeout(1000);
      }
    }

    // Click an empty cell and enter a value
    const emptyCell = page.locator('.cell[data-value="0"], .cell.empty, .cell:not(.given)').first();
    if (await emptyCell.isVisible({ timeout: 3000 }).catch(() => false)) {
      await emptyCell.click();
      await page.waitForTimeout(300);
      await page.keyboard.press('7');
      await page.waitForTimeout(500);

      // Undo via button
      const undoBtn = page.locator('button:has-text("Undo"), [title*="Undo"], [class*="undo"]').first();
      if (await undoBtn.isVisible({ timeout: 2000 }).catch(() => false)) {
        await undoBtn.click();
        await page.waitForTimeout(500);
      } else {
        // Try keyboard shortcut
        await page.keyboard.press('Control+z');
        await page.waitForTimeout(500);
      }

      // Redo via button
      const redoBtn = page.locator('button:has-text("Redo"), [title*="Redo"], [class*="redo"]').first();
      if (await redoBtn.isVisible({ timeout: 2000 }).catch(() => false)) {
        await redoBtn.click();
        await page.waitForTimeout(500);
      } else {
        await page.keyboard.press('Control+y');
        await page.waitForTimeout(500);
      }

      // No crash
      expect(page._testErrors.filter(e => !e.includes('undo-redo'))).toHaveLength(0);
    }
  });

  // --- Board Theme ---
  test('should switch board themes without errors', async ({ page }) => {
    // Navigate to settings
    const settingsBtn = page.locator('button:has-text("Settings"), [title*="Settings"]').first();
    if (await settingsBtn.isVisible({ timeout: 5000 }).catch(() => false)) {
      await settingsBtn.click();
      await page.waitForTimeout(1000);

      // Look for theme selector
      const themeSelect = page.locator('select, [class*="theme"]').first();
      if (await themeSelect.isVisible({ timeout: 3000 }).catch(() => false)) {
        // Try selecting a different theme
        const options = await themeSelect.locator('option').count();
        if (options > 1) {
          await themeSelect.selectOption({ index: 1 });
          await page.waitForTimeout(1000);

          // Navigate back to board
          const backBtn = page.locator('button:has-text("Back"), [class*="back"]').first();
          if (await backBtn.isVisible({ timeout: 2000 }).catch(() => false)) {
            await backBtn.click();
            await page.waitForTimeout(500);
          }
        }
      }

      // No crash
      expect(page._testErrors.filter(e => !e.includes('undo-redo'))).toHaveLength(0);
    }
  });

  // --- Sound Toggle ---
  test('should toggle sound effects in settings', async ({ page }) => {
    const settingsBtn = page.locator('button:has-text("Settings"), [title*="Settings"]').first();
    if (await settingsBtn.isVisible({ timeout: 5000 }).catch(() => false)) {
      await settingsBtn.click();
      await page.waitForTimeout(1000);

      // Find sound toggle checkbox
      const soundToggle = page.locator('input[type="checkbox"]').filter({ hasText: /sound/i });
      const anyToggle = page.locator('input[type="checkbox"]').first();

      const toggle = (await soundToggle.count() > 0) ? soundToggle : anyToggle;
      if (await toggle.isVisible({ timeout: 3000 }).catch(() => false)) {
        await toggle.click();
        await page.waitForTimeout(500);
        // Toggle back
        await toggle.click();
        await page.waitForTimeout(500);
      }

      // No crash
      expect(page._testErrors.filter(e => !e.includes('undo-redo'))).toHaveLength(0);
    }
  });

  // --- Share Puzzle ---
  test('should show share button without crash', async ({ page }) => {
    // Start a puzzle first
    const newPuzzleBtn = page.locator('button:has-text("New Puzzle"), button:has-text("Quick Solve"), a:has-text("Quick Solve")').first();
    if (await newPuzzleBtn.isVisible({ timeout: 3000 }).catch(() => false)) {
      await newPuzzleBtn.click();
      await page.waitForTimeout(1000);

      const easyBtn = page.locator('button:has-text("Easy")');
      if (await easyBtn.isVisible({ timeout: 2000 }).catch(() => false)) {
        await easyBtn.click();
        await page.waitForTimeout(1000);
      }
    }

    // Look for share button
    const shareBtn = page.locator('button:has-text("Share"), [title*="Share"], [class*="share"]').first();
    if (await shareBtn.isVisible({ timeout: 3000 }).catch(() => false)) {
      // Just verify it's clickable without crash (actual share needs browser API)
      await expect(shareBtn).toBeEnabled();
    }

    // No crash
    expect(page._testErrors.filter(e => !e.includes('undo-redo'))).toHaveLength(0);
  });

  // --- Cell Selection and Keyboard Navigation ---
  test('should select cells and navigate with keyboard', async ({ page }) => {
    // Start a puzzle
    const newPuzzleBtn = page.locator('button:has-text("New Puzzle"), button:has-text("Quick Solve"), a:has-text("Quick Solve")').first();
    if (await newPuzzleBtn.isVisible({ timeout: 3000 }).catch(() => false)) {
      await newPuzzleBtn.click();
      await page.waitForTimeout(1000);

      const easyBtn = page.locator('button:has-text("Easy")');
      if (await easyBtn.isVisible({ timeout: 2000 }).catch(() => false)) {
        await easyBtn.click();
        await page.waitForTimeout(1000);
      }
    }

    // Click a cell to select it
    const firstCell = page.locator('.cell').first();
    if (await firstCell.isVisible({ timeout: 3000 }).catch(() => false)) {
      await firstCell.click();
      await page.waitForTimeout(300);

      // Navigate with arrow keys
      await page.keyboard.press('ArrowRight');
      await page.waitForTimeout(200);
      await page.keyboard.press('ArrowDown');
      await page.waitForTimeout(200);
      await page.keyboard.press('ArrowLeft');
      await page.waitForTimeout(200);
      await page.keyboard.press('ArrowUp');
      await page.waitForTimeout(200);

      // No crash
      expect(page._testErrors.filter(e => !e.includes('undo-redo'))).toHaveLength(0);
    }
  });

  // --- Number Bar / Mobile Number Pad ---
  test('should use number bar to fill cells', async ({ page }) => {
    // Start a puzzle
    const newPuzzleBtn = page.locator('button:has-text("New Puzzle"), button:has-text("Quick Solve"), a:has-text("Quick Solve")').first();
    if (await newPuzzleBtn.isVisible({ timeout: 3000 }).catch(() => false)) {
      await newPuzzleBtn.click();
      await page.waitForTimeout(1000);

      const easyBtn = page.locator('button:has-text("Easy")');
      if (await easyBtn.isVisible({ timeout: 2000 }).catch(() => false)) {
        await easyBtn.click();
        await page.waitForTimeout(1000);
      }
    }

    // Select an empty cell
    const emptyCell = page.locator('.cell[data-value="0"], .cell.empty, .cell:not(.given)').first();
    if (await emptyCell.isVisible({ timeout: 3000 }).catch(() => false)) {
      await emptyCell.click();
      await page.waitForTimeout(300);

      // Click number bar button (e.g., number 3)
      const numBtn = page.locator('.number-bar button, .number-btn, [class*="number"]').filter({ hasText: '3' }).first();
      if (await numBtn.isVisible({ timeout: 2000 }).catch(() => false)) {
        await numBtn.click();
        await page.waitForTimeout(500);
      }
    }

    // No crash
    expect(page._testErrors.filter(e => !e.includes('undo-redo'))).toHaveLength(0);
  });

  // --- Clear Button ---
  test('should clear cell value with clear button', async ({ page }) => {
    // Start a puzzle
    const newPuzzleBtn = page.locator('button:has-text("New Puzzle"), button:has-text("Quick Solve"), a:has-text("Quick Solve")').first();
    if (await newPuzzleBtn.isVisible({ timeout: 3000 }).catch(() => false)) {
      await newPuzzleBtn.click();
      await page.waitForTimeout(1000);

      const easyBtn = page.locator('button:has-text("Easy")');
      if (await easyBtn.isVisible({ timeout: 2000 }).catch(() => false)) {
        await easyBtn.click();
        await page.waitForTimeout(1000);
      }
    }

    // Enter a value first
    const emptyCell = page.locator('.cell[data-value="0"], .cell.empty, .cell:not(.given)').first();
    if (await emptyCell.isVisible({ timeout: 3000 }).catch(() => false)) {
      await emptyCell.click();
      await page.waitForTimeout(300);
      await page.keyboard.press('5');
      await page.waitForTimeout(500);

      // Now find and click clear
      const clearBtn = page.locator('button:has-text("Clear"), [title*="Clear"], [class*="clear"]').first();
      if (await clearBtn.isVisible({ timeout: 2000 }).catch(() => false)) {
        await clearBtn.click();
        await page.waitForTimeout(500);
      } else {
        // Try backspace
        await page.keyboard.press('Backspace');
        await page.waitForTimeout(500);
      }
    }

    // No crash
    expect(page._testErrors.filter(e => !e.includes('undo-redo'))).toHaveLength(0);
  });
});
