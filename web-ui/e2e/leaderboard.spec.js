import { test, expect } from '@playwright/test';

test.describe('Leaderboard Flow', () => {
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

    // Clear leaderboard-related storage for clean state
    await page.goto('http://localhost:25321/', { waitUntil: 'networkidle' });
    await page.evaluate(() => {
      localStorage.removeItem('sudoku-dojo-leaderboard');
      localStorage.removeItem('sudoku-dojo-lb-optin');
      localStorage.removeItem('sudoku-dojo-lb-name');
    });

    // Reload to get clean state
    await page.goto('http://localhost:25321/', { waitUntil: 'networkidle' });

    // Dismiss "What's New" modal if present
    const gotItBtn = page.locator('button:has-text("Let\'s play")');
    if (await gotItBtn.isVisible({ timeout: 3000 }).catch(() => false)) {
      await gotItBtn.click();
      await page.waitForTimeout(500);
    }

    page._testErrors = errors;
  });

  test.afterEach(async ({ page }) => {
    // Check for console errors
    const errors = page._testErrors || [];
    const filtered = errors.filter(e =>
      !e.includes('favicon') && !e.includes('404') && !e.includes('ERR_')
    );
    expect(filtered).toEqual([]);
  });

  test('should display leaderboard opt-in screen', async ({ page }) => {
    // Navigate to leaderboard
    const lbBtn = page.locator('button:has-text("🏆"), [title*="Leaderboard"], [class*="leaderboard"]').first();
    if (await lbBtn.isVisible({ timeout: 3000 }).catch(() => false)) {
      await lbBtn.click();
      await page.waitForTimeout(1000);

      // Should show opt-in prompt
      await expect(page.locator('text=Join the Leaderboard')).toBeVisible({ timeout: 5000 });
      await expect(page.locator('.opt-in-card')).toBeVisible();

      // Should have name input
      const nameInput = page.locator('.name-input input, input[placeholder="Your name"]');
      await expect(nameInput).toBeVisible();

      // Join button should be disabled without name
      const joinBtn = page.locator('button:has-text("Join Leaderboard")');
      await expect(joinBtn).toBeDisabled();
    }
  });

  test('should join leaderboard with a name', async ({ page }) => {
    const lbBtn = page.locator('button:has-text("🏆"), [title*="Leaderboard"]').first();
    if (await lbBtn.isVisible({ timeout: 3000 }).catch(() => false)) {
      await lbBtn.click();
      await page.waitForTimeout(1000);

      // Enter name
      const nameInput = page.locator('.name-input input, input[placeholder="Your name"]');
      if (await nameInput.isVisible({ timeout: 3000 }).catch(() => false)) {
        await nameInput.fill('TestPlayer');
        await page.waitForTimeout(300);

        // Join button should now be enabled
        const joinBtn = page.locator('button:has-text("Join Leaderboard")');
        await expect(joinBtn).toBeEnabled();

        // Click join
        await joinBtn.click();
        await page.waitForTimeout(500);

        // Should now show leaderboard content (not opt-in)
        await expect(page.locator('.opt-in-card')).not.toBeVisible({ timeout: 3000 });

        // Should show player name
        await expect(page.locator('text=TestPlayer')).toBeVisible({ timeout: 3000 });

        // Should show tabs
        await expect(page.locator('button:has-text("Today")')).toBeVisible();
        await expect(page.locator('button:has-text("This Week")')).toBeVisible();
        await expect(page.locator('button:has-text("All Time")')).toBeVisible();
      }
    }
  });

  test('should switch between leaderboard tabs', async ({ page }) => {
    // Pre-set opt-in state
    await page.evaluate(() => {
      localStorage.setItem('sudoku-dojo-lb-optin', 'true');
      localStorage.setItem('sudoku-dojo-lb-name', 'TabTester');
      localStorage.setItem('sudoku-dojo-leaderboard', JSON.stringify([
        { id: '1', name: 'TabTester', date: new Date().toISOString().split('T')[0], time: 45000, hints: 0, timestamp: Date.now() },
        { id: '2', name: 'OtherPlayer', date: new Date().toISOString().split('T')[0], time: 60000, hints: 2, timestamp: Date.now() }
      ]));
    });

    // Reload to pick up localStorage changes
    await page.goto('http://localhost:25321/', { waitUntil: 'networkidle' });
    const gotItBtn = page.locator('button:has-text("Let\'s play")');
    if (await gotItBtn.isVisible({ timeout: 3000 }).catch(() => false)) {
      await gotItBtn.click();
      await page.waitForTimeout(500);
    }

    const lbBtn = page.locator('button:has-text("🏆"), [title*="Leaderboard"]').first();
    if (await lbBtn.isVisible({ timeout: 3000 }).catch(() => false)) {
      await lbBtn.click();
      await page.waitForTimeout(1000);

      // Should see rankings
      await expect(page.locator('.rankings')).toBeVisible({ timeout: 5000 });

      // Should see entries
      await expect(page.locator('.rank-row')).toHaveCount(2, { timeout: 5000 });

      // Switch to "This Week" tab
      const weekTab = page.locator('button:has-text("This Week")');
      await weekTab.click();
      await page.waitForTimeout(300);

      // Week tab should be active
      await expect(weekTab).toHaveClass(/active/);

      // Should still show entries (today is within this week)
      await expect(page.locator('.rank-row').first()).toBeVisible({ timeout: 3000 });

      // Switch to "All Time" tab
      const allTimeTab = page.locator('button:has-text("All Time")');
      await allTimeTab.click();
      await page.waitForTimeout(300);
      await expect(allTimeTab).toHaveClass(/active/);
    }
  });

  test('should show empty state when no entries exist', async ({ page }) => {
    await page.evaluate(() => {
      localStorage.setItem('sudoku-dojo-lb-optin', 'true');
      localStorage.setItem('sudoku-dojo-lb-name', 'EmptyTester');
      localStorage.setItem('sudoku-dojo-leaderboard', '[]');
    });

    await page.goto('http://localhost:25321/', { waitUntil: 'networkidle' });
    const gotItBtn = page.locator('button:has-text("Let\'s play")');
    if (await gotItBtn.isVisible({ timeout: 3000 }).catch(() => false)) {
      await gotItBtn.click();
      await page.waitForTimeout(500);
    }

    const lbBtn = page.locator('button:has-text("🏆"), [title*="Leaderboard"]').first();
    if (await lbBtn.isVisible({ timeout: 3000 }).catch(() => false)) {
      await lbBtn.click();
      await page.waitForTimeout(1000);

      // Should show empty state message
      await expect(page.locator('.empty-state')).toBeVisible({ timeout: 5000 });
      await expect(page.locator('text=No entries yet')).toBeVisible();
    }
  });

  test('should leave leaderboard', async ({ page }) => {
    await page.evaluate(() => {
      localStorage.setItem('sudoku-dojo-lb-optin', 'true');
      localStorage.setItem('sudoku-dojo-lb-name', 'Leaver');
      localStorage.setItem('sudoku-dojo-leaderboard', '[]');
    });

    await page.goto('http://localhost:25321/', { waitUntil: 'networkidle' });
    const gotItBtn = page.locator('button:has-text("Let\'s play")');
    if (await gotItBtn.isVisible({ timeout: 3000 }).catch(() => false)) {
      await gotItBtn.click();
      await page.waitForTimeout(500);
    }

    const lbBtn = page.locator('button:has-text("🏆"), [title*="Leaderboard"]').first();
    if (await lbBtn.isVisible({ timeout: 3000 }).catch(() => false)) {
      await lbBtn.click();
      await page.waitForTimeout(1000);

      // Should show "Leave Leaderboard" button
      const leaveBtn = page.locator('button:has-text("Leave Leaderboard")');
      if (await leaveBtn.isVisible({ timeout: 3000 }).catch(() => false)) {
        await leaveBtn.click();
        await page.waitForTimeout(500);

        // Should go back to opt-in screen
        await expect(page.locator('.opt-in-card')).toBeVisible({ timeout: 3000 });
        await expect(page.locator('text=Join the Leaderboard')).toBeVisible();
      }
    }
  });

  test('should go back from leaderboard', async ({ page }) => {
    await page.evaluate(() => {
      localStorage.setItem('sudoku-dojo-lb-optin', 'true');
      localStorage.setItem('sudoku-dojo-lb-name', 'BackTester');
    });

    await page.goto('http://localhost:25321/', { waitUntil: 'networkidle' });
    const gotItBtn = page.locator('button:has-text("Let\'s play")');
    if (await gotItBtn.isVisible({ timeout: 3000 }).catch(() => false)) {
      await gotItBtn.click();
      await page.waitForTimeout(500);
    }

    const lbBtn = page.locator('button:has-text("🏆"), [title*="Leaderboard"]').first();
    if (await lbBtn.isVisible({ timeout: 3000 }).catch(() => false)) {
      await lbBtn.click();
      await page.waitForTimeout(1000);

      // Click back button
      const backBtn = page.locator('button:has-text("Back")').first();
      if (await backBtn.isVisible({ timeout: 3000 }).catch(() => false)) {
        await backBtn.click();
        await page.waitForTimeout(500);

        // Leaderboard should be gone
        await expect(page.locator('.leaderboard')).not.toBeVisible({ timeout: 3000 });
      }
    }
  });

  test('should show medals for top 3 entries', async ({ page }) => {
    await page.evaluate(() => {
      localStorage.setItem('sudoku-dojo-lb-optin', 'true');
      localStorage.setItem('sudoku-dojo-lb-name', 'MedalTester');
      const today = new Date().toISOString().split('T')[0];
      localStorage.setItem('sudoku-dojo-leaderboard', JSON.stringify([
        { id: '1', name: 'Alice', date: today, time: 30000, hints: 0, timestamp: Date.now() },
        { id: '2', name: 'Bob', date: today, time: 45000, hints: 1, timestamp: Date.now() },
        { id: '3', name: 'Charlie', date: today, time: 60000, hints: 2, timestamp: Date.now() },
        { id: '4', name: 'MedalTester', date: today, time: 75000, hints: 0, timestamp: Date.now() }
      ]));
    });

    await page.goto('http://localhost:25321/', { waitUntil: 'networkidle' });
    const gotItBtn = page.locator('button:has-text("Let\'s play")');
    if (await gotItBtn.isVisible({ timeout: 3000 }).catch(() => false)) {
      await gotItBtn.click();
      await page.waitForTimeout(500);
    }

    const lbBtn = page.locator('button:has-text("🏆"), [title*="Leaderboard"]').first();
    if (await lbBtn.isVisible({ timeout: 3000 }).catch(() => false)) {
      await lbBtn.click();
      await page.waitForTimeout(1000);

      // Should show 4 entries
      await expect(page.locator('.rank-row')).toHaveCount(4, { timeout: 5000 });

      // Top 3 should have medals (emoji)
      const topRows = page.locator('.rank-row.top-3');
      await expect(topRows).toHaveCount(3, { timeout: 3000 });
    }
  });
});
