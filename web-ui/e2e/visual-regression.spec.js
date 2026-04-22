import { test, expect } from '@playwright/test'

/**
 * Visual regression tests for Sudoku Dojo.
 *
 * These tests capture screenshots of key pages/states and compare them
 * against stored baselines. A diff > 3% fails the test, signalling
 * an unexpected visual change.
 *
 * Run locally:
 *   cd web-ui && npx playwright test --config playwright.config.js
 *
 * Update baselines after intentional changes:
 *   npx playwright test --config playwright.config.js --update-snapshots
 */

test.describe('Visual Regression — Desktop', () => {
  test.use({ viewport: { width: 1280, height: 800 } })

  test('dashboard page', async ({ page }) => {
    await page.goto('/', { waitUntil: 'networkidle' })
    // Wait for splash to disappear and dashboard to render
    await page.locator('#splash').waitFor({ state: 'hidden', timeout: 10_000 })
    await page.waitForTimeout(1_000) // let animations settle
    await expect(page).toHaveScreenshot('dashboard-desktop.png', { fullPage: true })
  })

  test('settings page', async ({ page }) => {
    await page.goto('/', { waitUntil: 'networkidle' })
    await page.locator('#splash').waitFor({ state: 'hidden', timeout: 10_000 })
    await page.waitForTimeout(500)

    // Navigate to settings (gear icon)
    const settingsBtn = page.locator('[aria-label*="Settings"], [title*="Settings"], button:has-text("⚙")').first()
    if (await settingsBtn.isVisible({ timeout: 3_000 }).catch(() => false)) {
      await settingsBtn.click()
      await page.waitForTimeout(500)
    }

    await expect(page).toHaveScreenshot('settings-desktop.png', { fullPage: true })
  })

  test('game board — easy puzzle', async ({ page }) => {
    await page.goto('/', { waitUntil: 'networkidle' })
    await page.locator('#splash').waitFor({ state: 'hidden', timeout: 10_000 })
    await page.waitForTimeout(500)

    // Start an Easy game
    const easyBtn = page.locator('button:has-text("Easy")').first()
    if (await easyBtn.isVisible({ timeout: 3_000 }).catch(() => false)) {
      await easyBtn.click()
    } else {
      // Try Play button then difficulty selector
      const playBtn = page.locator('button:has-text("Play")').first()
      if (await playBtn.isVisible({ timeout: 3_000 }).catch(() => false)) {
        await playBtn.click()
        await page.waitForTimeout(500)
      }
    }

    // Wait for grid to render
    const grid = page.locator('.grid, [class*="grid"]').first()
    await expect(grid).toBeVisible({ timeout: 10_000 })
    await page.waitForTimeout(500) // let rendering settle

    await expect(page).toHaveScreenshot('game-easy-desktop.png', { fullPage: true })
  })
})

test.describe('Visual Regression — Mobile', () => {
  test.use({ viewport: { width: 375, height: 812 }, isMobile: true, hasTouch: true })

  test('dashboard on mobile', async ({ page }) => {
    await page.goto('/', { waitUntil: 'networkidle' })
    await page.locator('#splash').waitFor({ state: 'hidden', timeout: 10_000 })
    await page.waitForTimeout(1_000)

    await expect(page).toHaveScreenshot('dashboard-mobile.png', { fullPage: true })
  })

  test('game board on mobile', async ({ page }) => {
    await page.goto('/', { waitUntil: 'networkidle' })
    await page.locator('#splash').waitFor({ state: 'hidden', timeout: 10_000 })
    await page.waitForTimeout(500)

    // Try to start a game
    const easyBtn = page.locator('button:has-text("Easy")').first()
    if (await easyBtn.isVisible({ timeout: 3_000 }).catch(() => false)) {
      await easyBtn.click()
    } else {
      const playBtn = page.locator('button:has-text("Play")').first()
      if (await playBtn.isVisible({ timeout: 3_000 }).catch(() => false)) {
        await playBtn.click()
        await page.waitForTimeout(500)
      }
    }

    const grid = page.locator('.grid, [class*="grid"]').first()
    await expect(grid).toBeVisible({ timeout: 10_000 })
    await page.waitForTimeout(500)

    await expect(page).toHaveScreenshot('game-mobile.png', { fullPage: true })
  })
})
