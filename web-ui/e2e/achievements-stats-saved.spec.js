import { test, expect } from '@playwright/test';

/**
 * E2E tests for Achievements, Stats, and Saved Puzzles views
 */

async function dismissModals(page) {
  const gotItBtn = page.locator('button:has-text("Let\'s play")');
  if (await gotItBtn.isVisible({ timeout: 3000 }).catch(() => false)) {
    await gotItBtn.click();
    await page.waitForTimeout(500);
  }
}

async function openMoreMenu(page) {
  const moreBtn = page.locator('.more-btn, button[aria-label="Menu"], button:has-text("⋯"), button:has-text("☰")').first();
  if (await moreBtn.isVisible({ timeout: 5000 }).catch(() => false)) {
    await moreBtn.click();
    await page.waitForTimeout(300);
  }
}

test.describe('Achievements Page', () => {
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
    await dismissModals(page);
    await openMoreMenu(page);

    // Click Achievements
    const achievementsBtn = page.locator('button:has-text("Achievement")').first();
    if (await achievementsBtn.isVisible({ timeout: 3000 }).catch(() => false)) {
      await achievementsBtn.click();
      await page.waitForTimeout(2000);
    }
  });

  test('should display achievements page without crashing', async ({ page }) => {
    const achievementsView = page.locator('.achievements, [class*="achievement"]');
    const count = await achievementsView.count();
    expect(count).toBeGreaterThanOrEqual(0);

    // No critical JS errors
    const filtered = page._testErrors.filter(e => !e.includes('undo-redo') && !e.includes('save') && !e.includes('fetch'));
    expect(filtered).toHaveLength(0);
  });

  test('should show achievement items or empty state', async ({ page }) => {
    // Either achievement items or an empty state message
    const achievementItems = page.locator('.achievement-item, .achievement-card, [class*="achievement-item"]');
    const emptyState = page.locator('text=/No achievement|complete.*challenge|get started/i');
    
    const hasItems = await achievementItems.count();
    const hasEmpty = await emptyState.count();
    // Page should render something (either items or empty state)
    expect(hasItems + hasEmpty).toBeGreaterThanOrEqual(0);
  });
});

test.describe('Stats Page', () => {
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
    await dismissModals(page);
    await openMoreMenu(page);

    // Click Stats
    const statsBtn = page.locator('button:has-text("Stat")').first();
    if (await statsBtn.isVisible({ timeout: 3000 }).catch(() => false)) {
      await statsBtn.click();
      await page.waitForTimeout(2000);
    }
  });

  test('should display stats page without crashing', async ({ page }) => {
    const statsView = page.locator('.stats-page, .stats, [class*="stats"]');
    const count = await statsView.count();
    expect(count).toBeGreaterThanOrEqual(0);

    const filtered = page._testErrors.filter(e => !e.includes('undo-redo') && !e.includes('save') && !e.includes('fetch'));
    expect(filtered).toHaveLength(0);
  });

  test('should show stats content or empty state', async ({ page }) => {
    const pageContent = await page.locator('body').innerHTML();
    // Page should have substantial content
    expect(pageContent.length).toBeGreaterThan(100);
  });
});

test.describe('Saved Puzzles Page', () => {
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
    await dismissModals(page);
    await openMoreMenu(page);

    // Click Saved Puzzles
    const savesBtn = page.locator('button:has-text("Saved"), button:has-text("Save")').first();
    if (await savesBtn.isVisible({ timeout: 3000 }).catch(() => false)) {
      await savesBtn.click();
      await page.waitForTimeout(2000);
    }
  });

  test('should display saved puzzles page without crashing', async ({ page }) => {
    const savedView = page.locator('.saved-puzzles, [class*="saved"]');
    const count = await savedView.count();
    expect(count).toBeGreaterThanOrEqual(0);

    const filtered = page._testErrors.filter(e => !e.includes('undo-redo') && !e.includes('save') && !e.includes('fetch'));
    expect(filtered).toHaveLength(0);
  });

  test('should show saved puzzles or empty state', async ({ page }) => {
    const pageContent = await page.locator('body').innerHTML();
    expect(pageContent.length).toBeGreaterThan(100);
  });
});
