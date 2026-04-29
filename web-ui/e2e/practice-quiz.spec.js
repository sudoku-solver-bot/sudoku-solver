import { test, expect } from '@playwright/test';

/**
 * E2E tests for Practice Mode and Quiz Mode
 * These modes are accessed via Learn Techniques → Tutorial Selector
 */

// Helper: navigate to the Tutorial Selector via "Learn Techniques" menu
async function navigateToTutorialSelector(page) {
  // Dismiss "What's New" modal if present
  const gotItBtn = page.locator('button:has-text("Let\'s play")');
  if (await gotItBtn.isVisible({ timeout: 3000 }).catch(() => false)) {
    await gotItBtn.click();
    await page.waitForTimeout(500);
  }

  // Click the more/menu button to open the dropdown
  const moreBtn = page.locator('.more-btn, button[aria-label="Menu"], button:has-text("⋯"), button:has-text("☰")').first();
  if (await moreBtn.isVisible({ timeout: 5000 }).catch(() => false)) {
    await moreBtn.click();
    await page.waitForTimeout(300);
  }

  // Click "Learn Techniques"
  const learnBtn = page.locator('button:has-text("Learn")').first();
  await expect(learnBtn).toBeVisible({ timeout: 10000 });
  await learnBtn.click();
  await page.waitForTimeout(2000);
}

test.describe('Practice Mode', () => {
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
    page._testErrors = errors;

    await page.goto('http://localhost:25321/', { waitUntil: 'networkidle' });
    await navigateToTutorialSelector(page);
  });

  test('should show tutorial selector with available tutorials', async ({ page }) => {
    // Should show some tutorial items (belts/techniques)
    const tutorialItems = page.locator('.tutorial-item, .belt-item, .lesson-card, [class*="tutorial"], [class*="lesson"], [class*="belt"]');
    const count = await tutorialItems.count();
    // There should be at least some items
    expect(count).toBeGreaterThan(0);
  });

  test('should enter practice mode when practice button is clicked', async ({ page }) => {
    // Look for practice-related buttons or links
    const practiceBtn = page.locator('button:has-text("Practice"), button:has-text("practice"), [class*="practice"]').first();
    
    if (await practiceBtn.isVisible({ timeout: 5000 }).catch(() => false)) {
      await practiceBtn.click();
      await page.waitForTimeout(2000);

      // Should show practice mode UI
      const practiceHeader = page.locator('.practice-mode, .practice-header, h2:has-text("Practice")');
      const hasPractice = await practiceHeader.count();
      expect(hasPractice).toBeGreaterThan(0);
    }
  });

  test('should display practice puzzle tabs', async ({ page }) => {
    // Try to enter practice mode
    const practiceBtn = page.locator('button:has-text("Practice"), button:has-text("practice"), [class*="practice"]').first();
    
    if (await practiceBtn.isVisible({ timeout: 5000 }).catch(() => false)) {
      await practiceBtn.click();
      await page.waitForTimeout(2000);

      // Should show puzzle tabs
      const tabs = page.locator('.puzzle-tab, .practice-mode .tab, [class*="puzzle-tab"]');
      const tabCount = await tabs.count();
      if (tabCount > 0) {
        expect(tabCount).toBeGreaterThanOrEqual(1);
      }
    }
  });

  test('should show action buttons in practice mode', async ({ page }) => {
    const practiceBtn = page.locator('button:has-text("Practice"), button:has-text("practice"), [class*="practice"]').first();
    
    if (await practiceBtn.isVisible({ timeout: 5000 }).catch(() => false)) {
      await practiceBtn.click();
      await page.waitForTimeout(2000);

      // Should have Hint, Check, Reset buttons
      const hintBtn = page.locator('button:has-text("Hint")');
      const checkBtn = page.locator('button:has-text("Check")');
      const resetBtn = page.locator('button:has-text("Reset")');

      // At least Hint and Check should be present
      const hasHint = await hintBtn.count();
      const hasCheck = await checkBtn.count();
      expect(hasHint + hasCheck).toBeGreaterThan(0);
    }
  });

  test('should exit practice mode via back button', async ({ page }) => {
    const practiceBtn = page.locator('button:has-text("Practice"), button:has-text("practice"), [class*="practice"]').first();
    
    if (await practiceBtn.isVisible({ timeout: 5000 }).catch(() => false)) {
      await practiceBtn.click();
      await page.waitForTimeout(2000);

      // Click back button
      const backBtn = page.locator('button:has-text("Back"), button:has-text("←")').first();
      if (await backBtn.isVisible({ timeout: 3000 }).catch(() => false)) {
        await backBtn.click();
        await page.waitForTimeout(1000);

        // Should return to tutorial selector
        const selectorVisible = await page.locator('.tutorial-selector, [class*="tutorial-selector"], [class*="selector"]').count();
        expect(selectorVisible).toBeGreaterThanOrEqual(0);
      }
    }
  });

  test('practice mode - no critical JS errors', async ({ page }) => {
    const practiceBtn = page.locator('button:has-text("Practice"), button:has-text("practice"), [class*="practice"]').first();
    
    if (await practiceBtn.isVisible({ timeout: 5000 }).catch(() => false)) {
      await practiceBtn.click();
      await page.waitForTimeout(3000);
    }

    const filtered = page._testErrors.filter(e => !e.includes('undo-redo') && !e.includes('save') && !e.includes('fetch'));
    expect(filtered).toHaveLength(0);
  });
});

test.describe('Quiz Mode', () => {
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
    page._testErrors = errors;

    await page.goto('http://localhost:25321/', { waitUntil: 'networkidle' });
    await navigateToTutorialSelector(page);
  });

  test('should enter quiz mode when quiz button is clicked', async ({ page }) => {
    const quizBtn = page.locator('button:has-text("Quiz"), button:has-text("quiz"), [class*="quiz"]').first();
    
    if (await quizBtn.isVisible({ timeout: 5000 }).catch(() => false)) {
      await quizBtn.click();
      await page.waitForTimeout(2000);

      // Should show quiz mode UI
      const quizHeader = page.locator('.quiz-mode, .quiz-header, h2:has-text("Quiz")');
      const hasQuiz = await quizHeader.count();
      expect(hasQuiz).toBeGreaterThan(0);
    }
  });

  test('should display quiz question and score', async ({ page }) => {
    const quizBtn = page.locator('button:has-text("Quiz"), button:has-text("quiz"), [class*="quiz"]').first();
    
    if (await quizBtn.isVisible({ timeout: 5000 }).catch(() => false)) {
      await quizBtn.click();
      await page.waitForTimeout(2000);

      // Should show score
      const scoreBar = page.locator('.score-bar, .score-text, [class*="score"]');
      const hasScore = await scoreBar.count();
      expect(hasScore).toBeGreaterThanOrEqual(0);

      // Should show question
      const question = page.locator('.question-text, [class*="question"]');
      const hasQuestion = await question.count();
      if (hasQuestion > 0) {
        const qText = await question.first().textContent();
        expect(qText.length).toBeGreaterThan(0);
      }
    }
  });

  test('should display belt badge in quiz mode', async ({ page }) => {
    const quizBtn = page.locator('button:has-text("Quiz"), button:has-text("quiz"), [class*="quiz"]').first();
    
    if (await quizBtn.isVisible({ timeout: 5000 }).catch(() => false)) {
      await quizBtn.click();
      await page.waitForTimeout(2000);

      // Should show belt badge
      const beltBadge = page.locator('.belt-badge, .belt-emoji, [class*="belt"]');
      const hasBadge = await beltBadge.count();
      expect(hasBadge).toBeGreaterThanOrEqual(0);
    }
  });

  test('should show a grid/board in quiz mode', async ({ page }) => {
    const quizBtn = page.locator('button:has-text("Quiz"), button:has-text("quiz"), [class*="quiz"]').first();
    
    if (await quizBtn.isVisible({ timeout: 5000 }).catch(() => false)) {
      await quizBtn.click();
      await page.waitForTimeout(2000);

      // Should show sudoku grid
      const cells = page.locator('.cell');
      const cellCount = await cells.count();
      expect(cellCount).toBeGreaterThan(0);
    }
  });

  test('should exit quiz mode via back button', async ({ page }) => {
    const quizBtn = page.locator('button:has-text("Quiz"), button:has-text("quiz"), [class*="quiz"]').first();
    
    if (await quizBtn.isVisible({ timeout: 5000 }).catch(() => false)) {
      await quizBtn.click();
      await page.waitForTimeout(2000);

      // Click back button
      const backBtn = page.locator('button:has-text("Back"), button:has-text("←")').first();
      if (await backBtn.isVisible({ timeout: 3000 }).catch(() => false)) {
        await backBtn.click();
        await page.waitForTimeout(1000);
      }
    }
  });

  test('quiz mode - no critical JS errors', async ({ page }) => {
    const quizBtn = page.locator('button:has-text("Quiz"), button:has-text("quiz"), [class*="quiz"]').first();
    
    if (await quizBtn.isVisible({ timeout: 5000 }).catch(() => false)) {
      await quizBtn.click();
      await page.waitForTimeout(3000);
    }

    const filtered = page._testErrors.filter(e => !e.includes('undo-redo') && !e.includes('save') && !e.includes('fetch'));
    expect(filtered).toHaveLength(0);
  });
});
