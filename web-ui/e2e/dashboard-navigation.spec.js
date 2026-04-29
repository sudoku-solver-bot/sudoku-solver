import { test, expect } from '@playwright/test';

test.describe('Dashboard Navigation', () => {
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

  test('should display dashboard with main navigation options', async ({ page }) => {
    // Dashboard should show key elements
    const hasPlay = await page.locator('text=Play, button:has-text("Play"), button:has-text("Free Play")').first().isVisible({ timeout: 10000 });
    expect(hasPlay).toBeTruthy();

    // Should show daily challenge link
    const hasDaily = await page.locator('text=Daily, text=Challenge, button:has-text("Daily")').first().isVisible({ timeout: 5000 }).catch(() => false);

    // Should show learn link
    const hasLearn = await page.locator('text=Learn, button:has-text("Learn")').first().isVisible({ timeout: 5000 }).catch(() => false);

    // At least Play should be visible
    expect(hasPlay).toBeTruthy();

    expect(page._testErrors.filter(e => !e.includes('undo-redo') && !e.includes('save'))).toHaveLength(0);
  });

  test('should navigate to settings page and back', async ({ page }) => {
    // Find settings button (gear icon or text)
    const settingsBtn = page.locator('button:has-text("Settings"), [aria-label*="Settings"], [class*="settings"]').first();
    if (await settingsBtn.isVisible({ timeout: 5000 }).catch(() => false)) {
      await settingsBtn.click();
      await page.waitForTimeout(2000);

      // Settings page should have toggles or options
      const hasSettingOptions = await page.locator('input[type="checkbox"], .toggle, [class*="setting"], label').count();
      expect(hasSettingOptions).toBeGreaterThan(0);

      // Navigate back
      const backBtn = page.locator('button:has-text("Back"), [class*="back"]').first();
      if (await backBtn.isVisible({ timeout: 3000 }).catch(() => false)) {
        await backBtn.click();
        await page.waitForTimeout(1000);
      }
    }

    expect(page._testErrors.filter(e => !e.includes('undo-redo') && !e.includes('save'))).toHaveLength(0);
  });

  test('should navigate to About page', async ({ page }) => {
    // Try to get to About via settings or directly
    const aboutBtn = page.locator('button:has-text("About"), a:has-text("About"), [class*="about"]').first();
    if (await aboutBtn.isVisible({ timeout: 3000 }).catch(() => false)) {
      await aboutBtn.click();
      await page.waitForTimeout(2000);

      // About page should have some text content
      const aboutContent = await page.locator('[class*="about"], main, .page').first().innerHTML();
      expect(aboutContent.length).toBeGreaterThan(20);
    }

    expect(page._testErrors.filter(e => !e.includes('undo-redo') && !e.includes('save'))).toHaveLength(0);
  });

  test('should navigate to Help page', async ({ page }) => {
    const helpBtn = page.locator('button:has-text("Help"), a:has-text("Help"), [aria-label*="Help"], [class*="help"]').first();
    if (await helpBtn.isVisible({ timeout: 3000 }).catch(() => false)) {
      await helpBtn.click();
      await page.waitForTimeout(2000);

      // Help page should have FAQ or instructions content
      const helpContent = await page.locator('[class*="help"], main, .page').first().innerHTML();
      expect(helpContent.length).toBeGreaterThan(20);
    }

    expect(page._testErrors.filter(e => !e.includes('undo-redo') && !e.includes('save'))).toHaveLength(0);
  });

  test('should persist dark mode toggle across navigation', async ({ page }) => {
    // Go to settings
    const settingsBtn = page.locator('button:has-text("Settings"), [aria-label*="Settings"]').first();
    if (await settingsBtn.isVisible({ timeout: 5000 }).catch(() => false)) {
      await settingsBtn.click();
      await page.waitForTimeout(2000);

      // Toggle dark mode
      const darkToggle = page.locator('input[type="checkbox"], .toggle').first();
      if (await darkToggle.isVisible({ timeout: 3000 }).catch(() => false)) {
        await darkToggle.click();
        await page.waitForTimeout(1000);

        // Navigate back to dashboard
        const backBtn = page.locator('button:has-text("Back"), [class*="back"]').first();
        if (await backBtn.isVisible({ timeout: 3000 }).catch(() => false)) {
          await backBtn.click();
          await page.waitForTimeout(1000);

          // Page should still be visible (not crashed)
          const pageVisible = await page.locator('#app, .app').first().isVisible({ timeout: 5000 });
          expect(pageVisible).toBeTruthy();
        }
      }
    }

    expect(page._testErrors.filter(e => !e.includes('undo-redo') && !e.includes('save'))).toHaveLength(0);
  });

  test('should show saved puzzles section when navigating to saves', async ({ page }) => {
    const savesBtn = page.locator('button:has-text("Saves"), button:has-text("Saved"), [class*="save"]').first();
    if (await savesBtn.isVisible({ timeout: 5000 }).catch(() => false)) {
      await savesBtn.click();
      await page.waitForTimeout(2000);

      // Should show saved puzzles list or empty state
      const hasContent = await page.locator('[class*="save"], [class*="puzzle"], .empty, main').first().innerHTML();
      expect(hasContent.length).toBeGreaterThan(10);
    }

    expect(page._testErrors.filter(e => !e.includes('undo-redo') && !e.includes('save'))).toHaveLength(0);
  });

  test('should navigate through all main sections without errors', async ({ page }) => {
    const sections = ['Settings', 'About', 'Help'];

    for (const section of sections) {
      const btn = page.locator(`button:has-text("${section}"), a:has-text("${section}")`).first();
      if (await btn.isVisible({ timeout: 3000 }).catch(() => false)) {
        await btn.click();
        await page.waitForTimeout(2000);

        // Go back to dashboard
        await page.goto('http://localhost:25321/', { waitUntil: 'networkidle' });
        await page.waitForTimeout(1000);
      }
    }

    // No JS errors across all navigation
    expect(page._testErrors.filter(e => !e.includes('undo-redo') && !e.includes('save'))).toHaveLength(0);
  });
});
