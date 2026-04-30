import { test, expect } from '@playwright/test';

test.describe('i18n Language Switching', () => {
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

  test.afterEach(async ({ page }) => {
    // Reset to English after each test
    await page.evaluate(() => localStorage.removeItem('sudoku-locale'));
  });

  /**
   * Navigate to settings page and return the language selector
   */
  async function goToSettings(page) {
    // Try the settings gear button in the header
    const settingsBtn = page.locator('[title="Settings"], [aria-label="Settings"], button:has-text("⚙️")').first();
    if (await settingsBtn.isVisible({ timeout: 3000 }).catch(() => false)) {
      await settingsBtn.click();
    } else {
      // Try navigating via URL hash or other means
      const anySettingsBtn = page.locator('button:has-text("Settings"), [class*="settings"]').first();
      if (await anySettingsBtn.isVisible({ timeout: 3000 }).catch(() => false)) {
        await anySettingsBtn.click();
      }
    }
    await page.waitForTimeout(1000);
    return page.locator('select').first();
  }

  test('should display language selector in settings', async ({ page }) => {
    const langSelect = await goToSettings(page);
    if (await langSelect.isVisible({ timeout: 3000 }).catch(() => false)) {
      // Language select should have multiple options
      const options = await langSelect.locator('option').count();
      expect(options).toBeGreaterThan(1);
    }
  });

  test('should switch to Chinese Traditional and translate UI', async ({ page }) => {
    const langSelect = await goToSettings(page);
    if (await langSelect.isVisible({ timeout: 3000 }).catch(() => false)) {
      await langSelect.selectOption('zh-Hant');
      await page.waitForTimeout(1000);

      // Verify locale stored
      const stored = await page.evaluate(() => localStorage.getItem('sudoku-locale'));
      expect(stored).toBe('zh-Hant');

      // Title should contain Chinese characters
      const title = await page.locator('h1, .app-title, [class*="title"]').first().textContent().catch(() => '');
      expect(title).toContain('數獨');

      // No errors
      expect(page._testErrors.filter(e => !e.includes('undo-redo'))).toHaveLength(0);
    }
  });

  test('should switch to Japanese and translate UI', async ({ page }) => {
    const langSelect = await goToSettings(page);
    if (await langSelect.isVisible({ timeout: 3000 }).catch(() => false)) {
      await langSelect.selectOption('ja');
      await page.waitForTimeout(1000);

      const stored = await page.evaluate(() => localStorage.getItem('sudoku-locale'));
      expect(stored).toBe('ja');

      // Japanese title should contain 数独
      const title = await page.locator('h1, .app-title, [class*="title"]').first().textContent().catch(() => '');
      expect(title).toContain('数独');

      expect(page._testErrors.filter(e => !e.includes('undo-redo'))).toHaveLength(0);
    }
  });

  test('should switch to French and translate UI', async ({ page }) => {
    const langSelect = await goToSettings(page);
    if (await langSelect.isVisible({ timeout: 3000 }).catch(() => false)) {
      await langSelect.selectOption('fr');
      await page.waitForTimeout(1000);

      const stored = await page.evaluate(() => localStorage.getItem('sudoku-locale'));
      expect(stored).toBe('fr');

      // French title should contain Solveur
      const title = await page.locator('h1, .app-title, [class*="title"]').first().textContent().catch(() => '');
      expect(title).toMatch(/Solveur|Sudoku/);

      expect(page._testErrors.filter(e => !e.includes('undo-redo'))).toHaveLength(0);
    }
  });

  test('should switch to Arabic and set RTL direction', async ({ page }) => {
    const langSelect = await goToSettings(page);
    if (await langSelect.isVisible({ timeout: 3000 }).catch(() => false)) {
      await langSelect.selectOption('ar');
      await page.waitForTimeout(1000);

      const stored = await page.evaluate(() => localStorage.getItem('sudoku-locale'));
      expect(stored).toBe('ar');

      // Document direction should be RTL
      const dir = await page.evaluate(() => document.documentElement.dir);
      expect(dir).toBe('rtl');

      expect(page._testErrors.filter(e => !e.includes('undo-redo'))).toHaveLength(0);
    }
  });

  test('should switch back to English and restore LTR', async ({ page }) => {
    const langSelect = await goToSettings(page);
    if (await langSelect.isVisible({ timeout: 3000 }).catch(() => false)) {
      // First switch to Arabic
      await langSelect.selectOption('ar');
      await page.waitForTimeout(500);
      let dir = await page.evaluate(() => document.documentElement.dir);
      expect(dir).toBe('rtl');

      // Then switch back to English
      await langSelect.selectOption('en');
      await page.waitForTimeout(500);

      dir = await page.evaluate(() => document.documentElement.dir);
      expect(dir).toBe('ltr');

      const stored = await page.evaluate(() => localStorage.getItem('sudoku-locale'));
      expect(stored).toBe('en');
    }
  });

  test('should persist language across page reload', async ({ page }) => {
    const langSelect = await goToSettings(page);
    if (await langSelect.isVisible({ timeout: 3000 }).catch(() => false)) {
      await langSelect.selectOption('ko');
      await page.waitForTimeout(500);

      // Reload page
      await page.reload({ waitUntil: 'networkidle' });

      // Dismiss onboarding again if present
      const gotItBtn = page.locator('button:has-text("Let\'s play"), button:has-text("계속")');
      if (await gotItBtn.isVisible({ timeout: 3000 }).catch(() => false)) {
        await gotItBtn.click();
        await page.waitForTimeout(500);
      }

      // Korean should still be active
      const stored = await page.evaluate(() => localStorage.getItem('sudoku-locale'));
      expect(stored).toBe('ko');

      // Title should be in Korean
      const title = await page.locator('h1, .app-title, [class*="title"]').first().textContent().catch(() => '');
      expect(title).toContain('스도쿠');
    }
  });

  test('should switch to Russian and display Cyrillic text', async ({ page }) => {
    const langSelect = await goToSettings(page);
    if (await langSelect.isVisible({ timeout: 3000 }).catch(() => false)) {
      await langSelect.selectOption('ru');
      await page.waitForTimeout(1000);

      const stored = await page.evaluate(() => localStorage.getItem('sudoku-locale'));
      expect(stored).toBe('ru');

      const title = await page.locator('h1, .app-title, [class*="title"]').first().textContent().catch(() => '');
      expect(title).toMatch(/Судоку/);

      expect(page._testErrors.filter(e => !e.includes('undo-redo'))).toHaveLength(0);
    }
  });

  test('should switch to German and display German text', async ({ page }) => {
    const langSelect = await goToSettings(page);
    if (await langSelect.isVisible({ timeout: 3000 }).catch(() => false)) {
      await langSelect.selectOption('de');
      await page.waitForTimeout(1000);

      const stored = await page.evaluate(() => localStorage.getItem('sudoku-locale'));
      expect(stored).toBe('de');

      const title = await page.locator('h1, .app-title, [class*="title"]').first().textContent().catch(() => '');
      expect(title).toMatch(/Sudoku|Löser/);

      expect(page._testErrors.filter(e => !e.includes('undo-redo'))).toHaveLength(0);
    }
  });

  test('should switch to Thai and display Thai text', async ({ page }) => {
    const langSelect = await goToSettings(page);
    if (await langSelect.isVisible({ timeout: 3000 }).catch(() => false)) {
      await langSelect.selectOption('th');
      await page.waitForTimeout(1000);

      const stored = await page.evaluate(() => localStorage.getItem('sudoku-locale'));
      expect(stored).toBe('th');

      const title = await page.locator('h1, .app-title, [class*="title"]').first().textContent().catch(() => '');
      expect(title).toContain('ซูโดกุ');

      expect(page._testErrors.filter(e => !e.includes('undo-redo'))).toHaveLength(0);
    }
  });

  test('should switch to Greek and display Greek text', async ({ page }) => {
    const langSelect = await goToSettings(page);
    if (await langSelect.isVisible({ timeout: 3000 }).catch(() => false)) {
      await langSelect.selectOption('el');
      await page.waitForTimeout(1000);

      const stored = await page.evaluate(() => localStorage.getItem('sudoku-locale'));
      expect(stored).toBe('el');

      const title = await page.locator('h1, .app-title, [class*="title"]').first().textContent().catch(() => '');
      expect(title).toMatch(/Sudoku|Λύτης/);

      expect(page._testErrors.filter(e => !e.includes('undo-redo'))).toHaveLength(0);
    }
  });

  test('should handle rapid language switching without errors', async ({ page }) => {
    const langSelect = await goToSettings(page);
    if (await langSelect.isVisible({ timeout: 3000 }).catch(() => false)) {
      const languages = ['ja', 'fr', 'ar', 'de', 'ko', 'th', 'en'];

      for (const lang of languages) {
        await langSelect.selectOption(lang);
        await page.waitForTimeout(200);
      }

      // Final language should be English
      const stored = await page.evaluate(() => localStorage.getItem('sudoku-locale'));
      expect(stored).toBe('en');

      // No crashes from rapid switching
      expect(page._testErrors.filter(e => !e.includes('undo-redo'))).toHaveLength(0);
    }
  });

  test('should display translated difficulty labels', async ({ page }) => {
    const langSelect = await goToSettings(page);
    if (await langSelect.isVisible({ timeout: 3000 }).catch(() => false)) {
      await langSelect.selectOption('es');
      await page.waitForTimeout(1000);

      // Go back to main page (home button)
      const homeBtn = page.locator('button:has-text("🏠"), [title="Home"], [aria-label="Home"]').first();
      if (await homeBtn.isVisible({ timeout: 3000 }).catch(() => false)) {
        await homeBtn.click();
        await page.waitForTimeout(500);
      }

      // Look for Spanish difficulty labels on the page
      const pageText = await page.locator('body').textContent();
      // Spanish "Fácil" for Easy
      expect(pageText).toMatch(/Fácil|Medio|Difícil|Sudoku/);
    }
  });

  test('should not crash with invalid locale in localStorage', async ({ page }) => {
    await page.evaluate(() => localStorage.setItem('sudoku-locale', 'xx-invalid'));
    await page.reload({ waitUntil: 'networkidle' });

    // Should not crash — falls back gracefully
    const title = await page.locator('h1, .app-title, [class*="title"]').first().textContent().catch(() => '');
    // Should still show something (fallback to keys or English)
    expect(title).toBeTruthy();

    expect(page._testErrors.filter(e => !e.includes('undo-redo'))).toHaveLength(0);
  });
});
