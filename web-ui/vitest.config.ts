import { defineConfig } from 'vitest/config'
import vue from '@vitejs/plugin-vue'
import path from 'path'

// Mock virtual:pwa-register/vue for tests (only exists at build time with VitePWA)
const pwaMockPlugin = {
  name: 'virtual:pwa-mock',
  resolveId(id) {
    if (id === 'virtual:pwa-register/vue') return 'virtual:pwa-register/vue'
    return null
  },
  load(id) {
    if (id === 'virtual:pwa-register/vue') {
      return `
        import { ref } from 'vue'
        export function useRegisterSW() {
          return { needRefresh: ref(false), offlineReady: ref(false), updateSW: async () => {} }
        }
      `
    }
    return null
  }
}

export default defineConfig({
  plugins: [vue(), pwaMockPlugin],
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
