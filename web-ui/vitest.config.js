import { defineConfig } from 'vitest/config'
import vue from '@vitejs/plugin-vue'
import path from 'path'

export default defineConfig({
  plugins: [
    vue(),
    {
      name: 'mock-pwa-register',
      resolveId(id) {
        if (id === 'virtual:pwa-register/vue') {
          return 'virtual:pwa-register/vue'
        }
      },
      load(id) {
        if (id === 'virtual:pwa-register/vue') {
          return `
            export function useRegisterSW() {
              return { updateSW: () => Promise.resolve() }
            }
          `
        }
      }
    }
  ],
  resolve: {
    alias: {
      '@': path.resolve(__dirname, './src')
    }
  },
  test: {
    environment: 'happy-dom',
    exclude: [
      'node_modules/**',
      'dist/**',
      'e2e/**'
    ],
    coverage: {
      provider: 'v8',
      reporter: ['text', 'json', 'html'],
      exclude: [
        'node_modules/',
        'dist/',
        '**/*.test.{js,ts}',
        '**/src/main.js'
      ]
    }
  }
})
/* trigger */
