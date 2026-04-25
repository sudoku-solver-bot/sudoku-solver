<template>
  <div
    class="certificate-overlay"
    @click.self="$emit('close')"
  >
    <div
      ref="certEl"
      class="certificate-card"
    >
      <div class="cert-border">
        <div class="cert-inner">
          <div class="cert-ornament">
            ✨🏆✨
          </div>
          <h1 class="cert-title">
            Certificate of Achievement
          </h1>
          <div class="cert-divider" />
          <p class="cert-subtitle">
            This certifies mastery of
          </p>
          <h2 class="cert-technique">
            {{ technique }}
          </h2>
          <div class="cert-belt-row">
            <span
              class="cert-belt-badge"
              :style="{ background: beltColor }"
            >
              {{ beltEmoji }}
            </span>
            <span class="cert-belt-name">{{ beltName }}</span>
          </div>
          <div class="cert-divider" />
          <p class="cert-dojo">
            Sudoku Dojo
          </p>
          <p class="cert-date">
            {{ formattedDate }}
          </p>
          <div class="cert-footer">
            <span>{{ tutorialsCompleted }} / {{ totalTutorials }} lessons completed</span>
          </div>
        </div>
      </div>

      <!-- Actions -->
      <div class="cert-actions">
        <button
          class="cert-btn download"
          @click="downloadCert"
        >
          🖼️ Save Image
        </button>
        <button
          class="cert-btn print"
          @click="printCert"
        >
          🖨️ Print
        </button>
        <button
          class="cert-btn share"
          @click="shareCert"
        >
          📤 Share
        </button>
        <button
          class="cert-btn close"
          @click="$emit('close')"
        >
          ✓ Done
        </button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">

import { ref, computed } from 'vue'
import { generateCertificateImage, downloadCertificateImage } from '../certificate-image'

interface Props {
  technique: string
  beltName: string
  beltEmoji?: string
  beltColor?: string
  tutorialsCompleted?: number
  totalTutorials?: number
}
const props = withDefaults(defineProps<Props>(), {
  beltEmoji: '🟡',
  beltColor: '#f1c40f',
  tutorialsCompleted: 0,
  totalTutorials: 20
})
const emit = defineEmits<{ close: [] }>()

const certEl = ref(null)

const formattedDate = computed(() => {
  return new Date().toLocaleDateString('en-US', {
    year: 'numeric', month: 'long', day: 'numeric'
  })
})

const downloadCert = () => {
  const dataUrl = generateCertificateImage({
    technique: props.technique,
    beltName: props.beltName,
    beltEmoji: props.beltEmoji,
    beltColor: props.beltColor,
    tutorialsCompleted: props.tutorialsCompleted,
    totalTutorials: props.totalTutorials
  })
  downloadCertificateImage(dataUrl, props.beltName)
}

const printCert = () => {
  const printWindow = window.open('', '_blank')
  printWindow.document.write(`
    <!DOCTYPE html>
    <html>
    <head>
      <title>Sudoku Dojo Certificate - ${props.technique}</title>
      <style>
        * { margin: 0; padding: 0; box-sizing: border-box; }
        body {
          display: flex; align-items: center; justify-content: center;
          min-height: 100vh; background: #f5f5f5;
          font-family: Georgia, 'Times New Roman', serif;
        }
        .cert {
          width: 700px; padding: 40px; background: white;
          border: 4px double #c9a94e; border-radius: 16px;
          text-align: center; box-shadow: 0 4px 20px rgba(0,0,0,0.1);
        }
        .ornament { font-size: 36px; margin-bottom: 16px; }
        h1 { font-size: 32px; color: #2c3e50; margin-bottom: 8px; }
        .divider { height: 2px; background: linear-gradient(90deg, transparent, #c9a94e, transparent); margin: 16px 0; }
        .subtitle { font-size: 16px; color: #7f8c8d; margin-bottom: 8px; }
        .technique { font-size: 28px; color: #c9a94e; margin: 12px 0; }
        .belt { display: flex; align-items: center; justify-content: center; gap: 12px; margin: 16px 0; }
        .belt-badge { width: 48px; height: 48px; border-radius: 50%; display: flex; align-items: center; justify-content: center; font-size: 24px; border: 2px solid #ccc; }
        .belt-name { font-size: 20px; color: #555; }
        .dojo { font-size: 14px; color: #999; margin-top: 16px; text-transform: uppercase; letter-spacing: 2px; }
        .date { font-size: 14px; color: #aaa; margin-top: 4px; }
        .footer { font-size: 12px; color: #bbb; margin-top: 12px; }
        @media print { body { background: white; } .cert { box-shadow: none; border: 3px double #c9a94e; } }
      </style>
    </head>
    <body>
      <div class="cert">
        <div class="ornament">✨🏆✨</div>
        <h1>Certificate of Achievement</h1>
        <div class="divider"></div>
        <p class="subtitle">This certifies mastery of</p>
        <h2 class="technique">${props.technique}</h2>
        <div class="belt">
          <span class="belt-badge" style="background:${props.beltColor}">${props.beltEmoji}</span>
          <span class="belt-name">${props.beltName}</span>
        </div>
        <div class="divider"></div>
        <p class="dojo">Sudoku Dojo</p>
        <p class="date">${formattedDate.value}</p>
        <p class="footer">${props.tutorialsCompleted} / ${props.totalTutorials} lessons completed</p>
      </div>
    </body>
    </html>
  `)
  printWindow.document.close()
  printWindow.print()
}

const shareCert = async () => {
  // Try sharing the certificate image file first
  const dataUrl = generateCertificateImage({
    technique: props.technique,
    beltName: props.beltName,
    beltEmoji: props.beltEmoji,
    beltColor: props.beltColor,
    tutorialsCompleted: props.tutorialsCompleted,
    totalTutorials: props.totalTutorials
  })
  const text = `🏆 Sudoku Dojo Certificate!\n\nI earned my ${props.beltName} in ${props.technique}!\n\n${props.beltEmoji} ${props.tutorialsCompleted}/${props.totalTutorials} lessons complete\n\nPlay at: https://sudoku-solver-r5y8.onrender.com`

  if (navigator.share && navigator.canShare) {
    try {
      // Try to share with image file
      const blob = await (await fetch(dataUrl)).blob()
      const file = new File([blob], `sudoku-dojo-${props.beltName.toLowerCase().replace(/\s+/g, '-')}-certificate.png`, { type: 'image/png' })
      const shareData = { title: 'Sudoku Dojo Certificate', text, files: [file] }
      if (navigator.canShare(shareData)) {
        await navigator.share(shareData)
        return
      }
    } catch (e) { /* cancelled or not supported */ }
    // Fallback: share text only
    try {
      await navigator.share({ title: 'Sudoku Dojo Certificate', text })
    } catch (e) { /* cancelled */ }
  } else {
    // No Web Share API — download image + copy text
    downloadCertificateImage(dataUrl, props.beltName)
    await navigator.clipboard.writeText(text)
    alert('Certificate image saved! Text copied to clipboard.')
  }
}
</script>

<style scoped>
.certificate-overlay {
  position: fixed; top: 0; left: 0; right: 0; bottom: 0;
  background: rgba(0,0,0,0.75);
  display: flex; align-items: center; justify-content: center;
  z-index: 1000; animation: fadeIn 0.3s ease;
}

@keyframes fadeIn {
  from { opacity: 0; }
  to { opacity: 1; }
}

.certificate-card {
  animation: popIn 0.4s ease;
}

@keyframes popIn {
  from { transform: scale(0.85); opacity: 0; }
  to { transform: scale(1); opacity: 1; }
}

.cert-border {
  background: linear-gradient(135deg, #c9a94e, #f0d78c, #c9a94e);
  border-radius: 20px; padding: 6px;
}

.cert-inner {
  background: #fffef8;
  border-radius: 16px; padding: 32px; text-align: center;
  max-width: 420px;
}

.cert-ornament { font-size: 32px; margin-bottom: 12px; }

.cert-title {
  font-family: Georgia, 'Times New Roman', serif;
  font-size: 26px; color: #2c3e50; margin-bottom: 8px;
}

.cert-divider {
  height: 2px;
  background: linear-gradient(90deg, transparent, #c9a94e, transparent);
  margin: 16px 0;
}

.cert-subtitle { font-size: 14px; color: #7f8c8d; margin-bottom: 4px; }

.cert-technique {
  font-family: Georgia, serif;
  font-size: 22px; color: #c9a94e; margin: 8px 0;
}

.cert-belt-row {
  display: flex; align-items: center; justify-content: center;
  gap: 10px; margin: 12px 0;
}

.cert-belt-badge {
  width: 40px; height: 40px; border-radius: 50%;
  display: flex; align-items: center; justify-content: center;
  font-size: 20px; border: 2px solid rgba(0,0,0,0.15);
}

.cert-belt-name { font-size: 18px; color: #555; font-weight: 600; }

.cert-dojo {
  font-size: 12px; color: #999; text-transform: uppercase;
  letter-spacing: 2px; margin-top: 12px;
}

.cert-date { font-size: 13px; color: #aaa; margin-top: 4px; }

.cert-footer { font-size: 11px; color: #bbb; margin-top: 8px; }

.cert-actions {
  display: flex; gap: 8px; justify-content: center; margin-top: 16px;
}

.cert-btn {
  padding: 10px 20px; border: none; border-radius: 10px;
  font-size: 14px; font-weight: 600; cursor: pointer; transition: all 0.2s;
}

.cert-btn.download { background: #c9a94e; color: white; }
.cert-btn.download:hover { background: #b8952e; }
.cert-btn.print { background: white; color: #333; }
.cert-btn.print:hover { background: #f0f0f0; }
.cert-btn.share { background: #4285f4; color: white; }
.cert-btn.share:hover { background: #3367d6; }
.cert-btn.close { background: #34a853; color: white; }
.cert-btn.close:hover { background: #2d9249; }

@media (max-width: 500px) {
  .cert-inner { padding: 20px; max-width: 320px; }
  .cert-title { font-size: 20px; }
  .cert-technique { font-size: 18px; }
}
</style>
