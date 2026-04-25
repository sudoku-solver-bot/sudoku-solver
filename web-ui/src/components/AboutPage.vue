<template>
  <div
    class="about-page"
    :class="{ dark: isDark }"
  >
    <div class="about-header">
      <button
        class="back-btn"
        @click="$emit('exit')"
      >
        ← Back
      </button>
      <h2>About</h2>
    </div>

    <div class="about-content">
      <div class="logo-section">
        <div class="app-icon">
          🧩
        </div>
        <h1>Sudoku Dojo</h1>
        <p class="tagline">
          Learn, Practice & Master Sudoku
        </p>
      </div>

      <div class="description">
        <p>
          Sudoku Dojo is a free, open-source Sudoku puzzle app designed to help you
          improve your solving skills. Whether you're a beginner learning basic techniques
          or an expert tackling diabolical puzzles, we've got you covered.
        </p>
      </div>

      <div class="features">
        <div class="feature-item">
          <span class="feature-icon">📅</span>
          <div>
            <strong>Daily Challenge</strong>
            <p>A new puzzle every day with streak tracking</p>
          </div>
        </div>
        <div class="feature-item">
          <span class="feature-icon">📚</span>
          <div>
            <strong>Learn Techniques</strong>
            <p>Step-by-step lessons from basic to advanced</p>
          </div>
        </div>
        <div class="feature-item">
          <span class="feature-icon">💡</span>
          <div>
            <strong>Smart Hints</strong>
            <p>Get hints that teach you <em>why</em>, not just <em>what</em></p>
          </div>
        </div>
        <div class="feature-item">
          <span class="feature-icon">✏️</span>
          <div>
            <strong>Pencil Marks</strong>
            <p>Track candidates like you would on paper</p>
          </div>
        </div>
      </div>

      <div class="qr-section">
        <p class="qr-label">
          Scan to share with friends!
        </p>
        <div class="qr-wrapper">
          <canvas
            ref="qrCanvas"
            class="qr-canvas"
          />
        </div>
        <p class="qr-url">
          {{ siteUrl }}
        </p>
      </div>

      <div class="credits">
        <p>Built with ❤️ using Vue.js & Kotlin</p>
        <p class="version">
          v1.0.0
        </p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'

const props = defineProps({
  isDark: { type: Boolean, default: false }
})

const emit = defineEmits(['exit'])
const qrCanvas = ref(null)
const siteUrl = window.location.origin

// Minimal QR code generator (Level M, alphanumeric)
onMounted(() => {
  generateQR(qrCanvas.value, siteUrl)
})

function generateQR(canvas, text) {
  // Use a simple approach: fetch QR from a free API or render inline
  const ctx = canvas.getContext('2d')
  const size = 200
  canvas.width = size
  canvas.height = size

  // Use QR Server API to get the QR code image
  const img = new Image()
  img.crossOrigin = 'anonymous'
  img.onload = () => {
    ctx.fillStyle = props.isDark ? '#1e1e1e' : '#ffffff'
    ctx.fillRect(0, 0, size, size)
    ctx.drawImage(img, 0, 0, size, size)
  }
  img.onerror = () => {
    // Fallback: show URL text
    ctx.fillStyle = props.isDark ? '#e0e0e0' : '#333'
    ctx.font = '12px sans-serif'
    ctx.textAlign = 'center'
    ctx.fillText('QR Code', size / 2, size / 2)
  }
  img.src = `https://api.qrserver.com/v1/create-qr-code/?size=${size}x${size}&data=${encodeURIComponent(text)}&bgcolor=${props.isDark ? '1e1e1e' : 'ffffff'}&color=${props.isDark ? 'e0e0e0' : '000000'}`
}
</script>

<style scoped>
.about-page {
  max-width: 500px;
  margin: 0 auto;
  padding: 16px;
  animation: fadeIn 0.3s ease;
}

@keyframes fadeIn {
  from { opacity: 0; transform: translateY(10px); }
  to { opacity: 1; transform: translateY(0); }
}

.about-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 20px;
}

.back-btn {
  background: none;
  border: none;
  font-size: 16px;
  color: #4285f4;
  cursor: pointer;
  padding: 4px 8px;
  border-radius: 6px;
}

.back-btn:hover {
  background: #e8f0fe;
}

.about-page.dark .back-btn {
  color: #81c995;
}

.about-page.dark .back-btn:hover {
  background: #2d2d2d;
}

.about-content {
  text-align: center;
}

.logo-section {
  margin-bottom: 24px;
}

.app-icon {
  font-size: 48px;
  margin-bottom: 8px;
}

.logo-section h1 {
  font-size: 24px;
  margin: 0;
  color: #333;
}

.about-page.dark .logo-section h1 {
  color: #e0e0e0;
}

.tagline {
  color: #666;
  margin: 4px 0 0;
  font-size: 14px;
}

.about-page.dark .tagline {
  color: #aaa;
}

.description {
  text-align: left;
  margin: 0 0 24px;
  padding: 16px;
  background: #f8f9fa;
  border-radius: 12px;
}

.description p {
  margin: 0;
  font-size: 14px;
  line-height: 1.6;
  color: #444;
}

.about-page.dark .description {
  background: #2a2a2a;
}

.about-page.dark .description p {
  color: #ccc;
}

.features {
  text-align: left;
  margin-bottom: 24px;
}

.feature-item {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  padding: 12px 0;
  border-bottom: 1px solid #eee;
}

.about-page.dark .feature-item {
  border-color: #333;
}

.feature-icon {
  font-size: 24px;
  flex-shrink: 0;
}

.feature-item strong {
  display: block;
  font-size: 14px;
  margin-bottom: 2px;
}

.feature-item p {
  margin: 0;
  font-size: 13px;
  color: #666;
}

.about-page.dark .feature-item p {
  color: #999;
}

.qr-section {
  margin: 24px 0;
  padding: 20px;
  background: #f8f9fa;
  border-radius: 12px;
}

.about-page.dark .qr-section {
  background: #2a2a2a;
}

.qr-label {
  font-size: 14px;
  font-weight: 600;
  margin: 0 0 12px;
  color: #333;
}

.about-page.dark .qr-label {
  color: #e0e0e0;
}

.qr-wrapper {
  display: flex;
  justify-content: center;
  margin-bottom: 8px;
}

.qr-canvas {
  border-radius: 8px;
  image-rendering: pixelated;
}

.qr-url {
  font-size: 11px;
  color: #888;
  margin: 8px 0 0;
  word-break: break-all;
}

.credits {
  margin-top: 20px;
  padding-top: 16px;
  border-top: 1px solid #eee;
}

.about-page.dark .credits {
  border-color: #333;
}

.credits p {
  margin: 4px 0;
  font-size: 13px;
  color: #888;
}

.version {
  font-size: 11px !important;
  color: #aaa !important;
}
</style>
