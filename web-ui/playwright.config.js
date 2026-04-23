import { defineConfig } from '@playwright/test'

export default defineConfig({
  testDir: './e2e',
  timeout: 60000,
  use: {
    baseURL: 'http://localhost:25321',
    screenshot: 'on',
    viewport: { width: 1280, height: 800 },
  },
  projects: [
    {
      name: 'desktop-chrome',
      use: {
        browserName: 'chromium',
        contextOptions: { deviceScaleFactor: 1 },
      },
    },
    {
      name: 'mobile',
      use: {
        browserName: 'chromium',
        viewport: { width: 375, height: 812 },
        isMobile: true,
        hasTouch: true,
      },
    },
  ],
  retries: 1,
  reporter: [['html', { open: 'never' }]],
})
