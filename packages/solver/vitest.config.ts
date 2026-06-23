import { defineConfig } from 'vitest/config'

export default defineConfig({
  test: {
    include: ['tests/**/*.test.ts'],
    exclude: ['tests/api/**'],  // API tests require a running server; run manually
    environment: 'node',
    globals: true
  }
})
