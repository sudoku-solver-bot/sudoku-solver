import { defineConfig } from '@playwright/test'

export default defineConfig({
  testDir: './e2e',
  testMatch: '**/splash.spec.js',
  timeout: 60000,
  use: {
    baseURL: 'http://localhost:25321',
    screenshot: 'on',
    viewport: { width: 1280, height: 800 },
  },
  projects: [
    {
      name: 'desktop-chrome',
      use: { browserName: 'chromium' },
    },
  ],
  retries: 1,
  reporter: [['html', { open: 'never' }]],
})
