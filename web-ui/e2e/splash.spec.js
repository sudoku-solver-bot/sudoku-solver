import { test, expect } from '@playwright/test';

test.describe('Sudoku Dojo - App Load', () => {
  test('splash screen should disappear after app loads', async ({ page }) => {
    // Collect all JS errors during page lifecycle
    const errors = [];
    page.on('console', msg => {
      if (msg.type() === 'error') errors.push(msg.text());
    });
    page.on('pageerror', err => errors.push(err.message));

    await page.goto('http://localhost:25321/', { waitUntil: 'networkidle' });

    // Wait up to 10s for splash to disappear
    const splash = page.locator('#splash');
    await expect(splash).not.toBeVisible({ timeout: 10000 });

    // App container should have content
    const app = page.locator('#app');
    await expect(app).toBeVisible();

    // Should see some game content (dashboard or board)
    const hasContent = await page.locator('text=Sudoku').count() > 0;
    expect(hasContent).toBeTruthy();

    // Report any console errors
    if (errors.length > 0) {
      console.error('Console errors:', errors);
    }
  });

  test('should show dashboard with play button', async ({ page }) => {
    await page.goto('http://localhost:25321/', { waitUntil: 'networkidle' });

    // Dashboard should be visible
    const playButton = page.locator('button:has-text("Play"), [class*="play"]');
    await expect(playButton.first()).toBeVisible({ timeout: 10000 });
  });
});

test.describe('App Mount - No JS Errors', () => {
  test('should mount without any JavaScript errors', async ({ page }) => {
    const jsErrors = [];

    // Capture ALL errors before navigation
    page.on('pageerror', error => {
      jsErrors.push({
        message: error.message,
        stack: error.stack,
      });
    });

    // Also catch console errors that indicate runtime failures
    page.on('console', msg => {
      if (msg.type() === 'error') {
        const text = msg.text();
        // Filter out known benign errors (e.g., favicon, service worker)
        if (!text.includes('favicon') && !text.includes('404')) {
          jsErrors.push({ message: `console.error: ${text}` });
        }
      }
    });

    await page.goto('http://localhost:25321/', { waitUntil: 'networkidle' });

    // Wait for the app to fully mount and render
    await page.waitForTimeout(3000);

    // Check that the Vue app actually rendered something
    const appContent = await page.locator('#app').innerHTML();
    expect(appContent.length).toBeGreaterThan(100);

    // Fail if ANY JS errors occurred during mount
    if (jsErrors.length > 0) {
      const errorDetails = jsErrors
        .map((e, i) => `  ${i + 1}. ${e.message}`)
        .join('\n');
      throw new Error(`App mounted with ${jsErrors.length} JS error(s):\n${errorDetails}`);
    }
  });
});

test.describe('Grid Rendering', () => {
  test('should render puzzle grid when starting a game', async ({ page }) => {
    // Track JS errors
    const errors = [];
    page.on('pageerror', err => errors.push(err.message));

    await page.goto('http://localhost:25321/', { waitUntil: 'networkidle' });

    // Wait for dashboard to load
    await page.waitForTimeout(2000);

    // Click Play to start a game (try multiple selectors)
    const playButton = page.locator('button:has-text("Play")').first();
    if (await playButton.isVisible({ timeout: 5000 }).catch(() => false)) {
      await playButton.click();
    }

    // Wait for grid to appear
    const grid = page.locator('.grid, [class*="grid"]');
    await expect(grid.first()).toBeVisible({ timeout: 10000 });

    // Grid should have 81 cells
    const cells = page.locator('.cell');
    const cellCount = await cells.count();
    expect(cellCount).toBe(81);

    // No JS errors during gameplay render
    expect(errors).toHaveLength(0);
  });
});
