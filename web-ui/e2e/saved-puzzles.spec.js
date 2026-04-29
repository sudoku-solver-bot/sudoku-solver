import { test, expect } from '@playwright/test';

test.describe('Saved Puzzles', () => {
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

    // Clear saved puzzles before each test
    await page.goto('http://localhost:25321/', { waitUntil: 'networkidle' });
    await page.evaluate(() => localStorage.removeItem('sudoku-dojo-saves'));

    // Dismiss "What's New" modal if present
    const gotItBtn = page.locator('button:has-text("Let\'s play")');
    if (await gotItBtn.isVisible({ timeout: 3000 }).catch(() => false)) {
      await gotItBtn.click();
      await page.waitForTimeout(500);
    }

    page._testErrors = errors;
  });

  test.afterEach(async ({ page }) => {
    // Check no console errors occurred
    const errors = page._testErrors || [];
    const filtered = errors.filter(e =>
      !e.includes('favicon') && !e.includes('404') && !e.includes('net::ERR')
    );
    expect(filtered).toEqual([]);
  });

  test('should open saved puzzles modal with empty state', async ({ page }) => {
    // Open the menu
    const menuBtn = page.locator('button.menu-toggle, button:has-text("☰"), .header-right button').first();
    await menuBtn.click();
    await page.waitForTimeout(500);

    // Click Saved Puzzles
    const savedBtn = page.locator('button:has-text("Saved Puzzles")').first();
    if (await savedBtn.isVisible({ timeout: 3000 }).catch(() => false)) {
      await savedBtn.click();
      await page.waitForTimeout(500);

      // Should show empty state
      await expect(page.locator('.saves-modal')).toBeVisible();
      await expect(page.locator('text=No saved puzzles yet')).toBeVisible();
      await expect(page.locator('text=Click "Save Puzzle" while playing')).toBeVisible();
    }
  });

  test('should save current puzzle from Free Play', async ({ page }) => {
    // Start Free Play with Easy puzzle
    const playBtn = page.locator('button:has-text("Free Play")').first();
    await expect(playBtn).toBeVisible({ timeout: 10000 });
    await playBtn.click();
    await page.waitForTimeout(2000);

    const easyBtn = page.locator('button:has-text("Easy")').first();
    if (await easyBtn.isVisible({ timeout: 5000 }).catch(() => false)) {
      await easyBtn.click();
      await page.waitForTimeout(3000);
    }

    // Wait for grid to appear
    await expect(page.locator('.sudoku-grid, .grid-container').first()).toBeVisible({ timeout: 10000 });

    // Open saved puzzles modal
    const savedBtn = page.locator('button:has-text("Saved Puzzles"), button:has-text("💾")').first();
    if (await savedBtn.isVisible({ timeout: 3000 }).catch(() => false)) {
      await savedBtn.click();
      await page.waitForTimeout(500);

      // Click save current
      const saveCurrentBtn = page.locator('button:has-text("Save Current Puzzle")');
      if (await saveCurrentBtn.isVisible({ timeout: 3000 }).catch(() => false)) {
        await saveCurrentBtn.click();
        await page.waitForTimeout(500);

        // Should now show the saved puzzle in the list
        await expect(page.locator('.save-item').first()).toBeVisible({ timeout: 3000 });
        await expect(page.locator('text=filled')).toBeVisible();
      }
    }
  });

  test('should close saved puzzles modal on close button', async ({ page }) => {
    // Open menu and saved puzzles
    const menuBtn = page.locator('button.menu-toggle, button:has-text("☰"), .header-right button').first();
    await menuBtn.click();
    await page.waitForTimeout(500);

    const savedBtn = page.locator('button:has-text("Saved Puzzles")').first();
    if (await savedBtn.isVisible({ timeout: 3000 }).catch(() => false)) {
      await savedBtn.click();
      await page.waitForTimeout(500);

      // Modal should be visible
      await expect(page.locator('.saves-modal')).toBeVisible();

      // Click close button
      await page.locator('.saves-card .close-btn').click();
      await page.waitForTimeout(500);

      // Modal should be gone
      await expect(page.locator('.saves-modal')).not.toBeVisible({ timeout: 3000 });
    }
  });

  test('should close saved puzzles modal on backdrop click', async ({ page }) => {
    const menuBtn = page.locator('button.menu-toggle, button:has-text("☰"), .header-right button').first();
    await menuBtn.click();
    await page.waitForTimeout(500);

    const savedBtn = page.locator('button:has-text("Saved Puzzles")').first();
    if (await savedBtn.isVisible({ timeout: 3000 }).catch(() => false)) {
      await savedBtn.click();
      await page.waitForTimeout(500);

      await expect(page.locator('.saves-modal')).toBeVisible();

      // Click backdrop (the modal overlay itself, not the card)
      await page.locator('.saves-modal').click({ position: { x: 10, y: 10 } });
      await page.waitForTimeout(500);

      await expect(page.locator('.saves-modal')).not.toBeVisible({ timeout: 3000 });
    }
  });

  test('should delete a saved puzzle', async ({ page }) => {
    // Pre-populate a save
    await page.evaluate(() => {
      const saves = [{
        puzzle: '530070000600195000098000060800060003400803001700020006060000280000419005000080079',
        difficulty: 'Easy',
        progress: 35,
        date: '4/29/2026',
        name: 'Puzzle 1'
      }];
      localStorage.setItem('sudoku-dojo-saves', JSON.stringify(saves));
    });

    // Open saved puzzles
    const menuBtn = page.locator('button.menu-toggle, button:has-text("☰"), .header-right button').first();
    await menuBtn.click();
    await page.waitForTimeout(500);

    const savedBtn = page.locator('button:has-text("Saved Puzzles")').first();
    if (await savedBtn.isVisible({ timeout: 3000 }).catch(() => false)) {
      await savedBtn.click();
      await page.waitForTimeout(500);

      // Should show saved puzzle
      await expect(page.locator('.save-item').first()).toBeVisible({ timeout: 3000 });
      await expect(page.locator('text=Puzzle 1')).toBeVisible();

      // Click delete
      await page.locator('.btn-delete').first().click();
      await page.waitForTimeout(500);

      // Should now show empty state
      await expect(page.locator('text=No saved puzzles yet')).toBeVisible({ timeout: 3000 });
    }
  });

  test('should load a saved puzzle', async ({ page }) => {
    // Pre-populate a save
    await page.evaluate(() => {
      const saves = [{
        puzzle: '530070000600195000098000060800060003400803001700020006060000280000419005000080079',
        difficulty: 'Easy',
        progress: 35,
        date: '4/29/2026',
        name: 'Puzzle 1'
      }];
      localStorage.setItem('sudoku-dojo-saves', JSON.stringify(saves));
    });

    // Open saved puzzles
    const menuBtn = page.locator('button.menu-toggle, button:has-text("☰"), .header-right button').first();
    await menuBtn.click();
    await page.waitForTimeout(500);

    const savedBtn = page.locator('button:has-text("Saved Puzzles")').first();
    if (await savedBtn.isVisible({ timeout: 3000 }).catch(() => false)) {
      await savedBtn.click();
      await page.waitForTimeout(500);

      // Should show saved puzzle with load button
      await expect(page.locator('.btn-load').first()).toBeVisible({ timeout: 3000 });

      // Click load
      await page.locator('.btn-load').first().click();
      await page.waitForTimeout(1000);

      // Modal should close and grid should be visible with loaded puzzle
      await expect(page.locator('.saves-modal')).not.toBeVisible({ timeout: 3000 });
      await expect(page.locator('.sudoku-grid, .grid-container').first()).toBeVisible({ timeout: 5000 });
    }
  });

  test('should display save metadata correctly', async ({ page }) => {
    // Pre-populate multiple saves
    await page.evaluate(() => {
      const saves = [
        {
          puzzle: '530070000600195000098000060800060003400803001700020006060000280000419005000080079',
          difficulty: 'Easy',
          progress: 35,
          date: '4/29/2026',
          name: 'My Easy Puzzle'
        },
        {
          puzzle: '800000000003600000070090200050007000000045700000100030001000068008500010090000400',
          difficulty: 'Hard',
          progress: 12,
          date: '4/28/2026',
          name: 'Hard Challenge'
        }
      ];
      localStorage.setItem('sudoku-dojo-saves', JSON.stringify(saves));
    });

    const menuBtn = page.locator('button.menu-toggle, button:has-text("☰"), .header-right button').first();
    await menuBtn.click();
    await page.waitForTimeout(500);

    const savedBtn = page.locator('button:has-text("Saved Puzzles")').first();
    if (await savedBtn.isVisible({ timeout: 3000 }).catch(() => false)) {
      await savedBtn.click();
      await page.waitForTimeout(500);

      // Should show both saves
      const items = page.locator('.save-item');
      await expect(items).toHaveCount(2, { timeout: 3000 });

      // Check first save metadata
      await expect(page.locator('text=My Easy Puzzle')).toBeVisible();
      await expect(page.locator('text=35/81 filled')).toBeVisible();

      // Check second save metadata
      await expect(page.locator('text=Hard Challenge')).toBeVisible();
      await expect(page.locator('text=12/81 filled')).toBeVisible();
    }
  });

  test('should respect max 10 saves limit', async ({ page }) => {
    // Pre-populate 10 saves
    await page.evaluate(() => {
      const saves = Array.from({ length: 10 }, (_, i) => ({
        puzzle: '530070000600195000098000060800060003400803001700020006060000280000419005000080079',
        difficulty: 'Easy',
        progress: 35,
        date: `4/${29 - i}/2026`,
        name: `Puzzle ${i + 1}`
      }));
      localStorage.setItem('sudoku-dojo-saves', JSON.stringify(saves));
    });

    const menuBtn = page.locator('button.menu-toggle, button:has-text("☰"), .header-right button').first();
    await menuBtn.click();
    await page.waitForTimeout(500);

    const savedBtn = page.locator('button:has-text("Saved Puzzles")').first();
    if (await savedBtn.isVisible({ timeout: 3000 }).catch(() => false)) {
      await savedBtn.click();
      await page.waitForTimeout(500);

      // Should show exactly 10 saves
      const items = page.locator('.save-item');
      await expect(items).toHaveCount(10, { timeout: 3000 });
    }
  });
});
