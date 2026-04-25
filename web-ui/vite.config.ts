import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import { VitePWA } from 'vite-plugin-pwa'

export default defineConfig({
  plugins: [
    vue(),
    VitePWA({
      registerType: 'prompt',
      includeAssets: ['assets/icon-192.png'],
      manifest: {
        name: 'Sudoku Solver',
        short_name: 'Sudoku',
        description: 'Learn Sudoku techniques step-by-step with belt progression',
        theme_color: '#667eea',
        background_color: '#667eea',
        display: 'standalone',
        orientation: 'portrait',
        icons: [
          { src: 'assets/icon-192.png', sizes: '192x192', type: 'image/png' },
          { src: 'assets/icon-512.png', sizes: '512x512', type: 'image/png' }
        ]
      },
      workbox: {
        globPatterns: ['**/*.{js,css,html,ico,png,svg}'],
        runtimeCaching: [
          {
            urlPattern: /\/api\/v1\/daily/,
            handler: 'NetworkFirst',
            options: {
              cacheName: 'daily-api',
              expiration: { maxAgeSeconds: 3600 }
            }
          },
          {
            urlPattern: /\/api\/v1\/tutorials/,
            handler: 'CacheFirst',
            options: {
              cacheName: 'tutorials-api',
              expiration: { maxAgeSeconds: 86400 }
            }
          }
        ]
      }
    })
  ],
  build: {
    outDir: '../web/src/main/resources/static',
    emptyOutDir: true
  },
  server: {
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true
      }
    }
  }
})
