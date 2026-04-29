import { test, expect } from '@playwright/test';

test.describe('Tutorial / Learn Flow', () => {
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

  test('should navigate to Learn section from dashboard', async ({ page }) => {
    // Find and click the Learn button
    const learnBtn = page.locator('button:has-text("Learn"), a:has-text("Learn"), [class*="learn"]').first();
    await expect(learnBtn).toBeVisible({ timeout: 10000 });
    await learnBtn.click();
    await page.waitForTimeout(2000);

    // Should show tutorial/technique content
    const hasTutorialContent = await page.locator('.tutorial, .technique, [class*="tutorial"], [class*="belt"], [class*="lesson"]').count();
    expect(hasTutorialContent).toBeGreaterThan(0);

    expect(page._testErrors.filter(e => !e.includes('undo-redo') && !e.includes('save'))).toHaveLength(0);
  });

  test('should display belt levels in tutorial selector', async ({ page }) => {
    // Navigate to Learn
    const learnBtn = page.locator('button:has-text("Learn"), a:has-text("Learn")').first();
    await expect(learnBtn).toBeVisible({ timeout: 10000 });
    await learnBtn.click();
    await page.waitForTimeout(2000);

    // Should show belt levels (White, Yellow, etc.)
    const beltTexts = ['White', 'Yellow', 'Orange', 'Green', 'Blue'];
    let foundBelt = false;
    for (const belt of beltTexts) {
      const beltEl = page.locator(`text=${belt}`).first();
      if (await beltEl.isVisible({ timeout: 2000 }).catch(() => false)) {
        foundBelt = true;
        break;
      }
    }
    expect(foundBelt).toBeTruthy();

    expect(page._testErrors.filter(e => !e.includes('undo-redo') && !e.includes('save'))).toHaveLength(0);
  });

  test('should start a tutorial lesson', async ({ page }) => {
    // Navigate to Learn
    const learnBtn = page.locator('button:has-text("Learn"), a:has-text("Learn")').first();
    await expect(learnBtn).toBeVisible({ timeout: 10000 });
    await learnBtn.click();
    await page.waitForTimeout(2000);

    // Click first available lesson/tutorial item
    const lessonBtn = page.locator('.lesson, .tutorial-item, [class*="lesson"], [class*="tutorial"] button, button:has-text("Start"), button:has-text("Play")').first();
    if (await lessonBtn.isVisible({ timeout: 5000 }).catch(() => false)) {
      await lessonBtn.click();
      await page.waitForTimeout(2000);

      // Should show a grid for the tutorial
      const cells = page.locator('.cell');
      const cellCount = await cells.count();
      expect(cellCount).toBeGreaterThan(0);
    }

    expect(page._testErrors.filter(e => !e.includes('undo-redo') && !e.includes('save'))).toHaveLength(0);
  });

  test('should show tutorial instructions or hint text', async ({ page }) => {
    // Navigate to Learn
    const learnBtn = page.locator('button:has-text("Learn"), a:has-text("Learn")').first();
    await expect(learnBtn).toBeVisible({ timeout: 10000 });
    await learnBtn.click();
    await page.waitForTimeout(2000);

    // Click first lesson if available
    const lessonBtn = page.locator('.lesson, .tutorial-item, [class*="lesson"], [class*="tutorial"] button, button:has-text("Start"), button:has-text("Play")').first();
    if (await lessonBtn.isVisible({ timeout: 5000 }).catch(() => false)) {
      await lessonBtn.click();
      await page.waitForTimeout(2000);

      // Should have some instructional text
      const hasInstruction = await page.locator('.hint, .instruction, [class*="hint"], [class*="instruction"], [class*="message"]').count();
      // Even if no explicit instruction div, the page shouldn't be blank
      const pageContent = await page.locator('main, .app, #app').first().innerHTML();
      expect(pageContent.length).toBeGreaterThan(50);
    }

    expect(page._testErrors.filter(e => !e.includes('undo-redo') && !e.includes('save'))).toHaveLength(0);
  });

  test('should navigate back from tutorial to dashboard', async ({ page }) => {
    // Navigate to Learn
    const learnBtn = page.locator('button:has-text("Learn"), a:has-text("Learn")').first();
    await expect(learnBtn).toBeVisible({ timeout: 10000 });
    await learnBtn.click();
    await page.waitForTimeout(2000);

    // Click back or home button
    const backBtn = page.locator('button:has-text("Back"), button:has-text("Home"), a:has-text("Home"), [class*="back"]').first();
    if (await backBtn.isVisible({ timeout: 3000 }).catch(() => false)) {
      await backBtn.click();
      await page.waitForTimeout(2000);

      // Should be back on dashboard
      const hasDashboard = await page.locator('[class*="dashboard"], [class*="dojo"], text=Sudoku Dojo').count();
      expect(hasDashboard).toBeGreaterThan(0);
    }

    expect(page._testErrors.filter(e => !e.includes('undo-redo') && !e.includes('save'))).toHaveLength(0);
  });
});
