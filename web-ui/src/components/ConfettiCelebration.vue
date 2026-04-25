<template>
  <div
    v-if="visible"
    class="confetti-container"
    @click="$emit('done')"
  >
    <canvas
      ref="canvas"
      class="confetti-canvas"
    />
    <div
      class="celebration-text"
      :class="{ visible: showText }"
    >
      <div class="celebration-emoji">
        🎉
      </div>
      <h2>Puzzle Complete!</h2>
      <div class="celebration-stats">
        <span v-if="time">⏱️ {{ time }}</span>
        <span v-if="mistakes === 0">✨ Perfect!</span>
        <span v-else>❌ {{ mistakes }} mistake{{ mistakes > 1 ? 's' : '' }}</span>
        <span v-if="hints === 0">🧠 No hints</span>
        <span v-else>💡 {{ hints }} hint{{ hints > 1 ? 's' : '' }}</span>
      </div>
      <button
        class="celebration-btn"
        @click="$emit('done')"
      >
        Continue
      </button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, watch, nextTick } from 'vue'

const emit = defineEmits<{ done: [] }>()

interface Props {
  visible?: boolean
  time?: string
  mistakes?: number
  hints?: number
}
const props = withDefaults(defineProps<Props>(), {
  visible: false,
  time: '',
  mistakes: 0,
  hints: 0
})

const canvas = ref<HTMLCanvasElement | null>(null)
    const showText = ref<boolean>(false)
    let animationId: number | null = null

    const colors = ['#4285f4', '#34a853', '#fbbc05', '#ea4335', '#ff6d00', '#ab47bc']

    interface Particle {
      x: number; y: number; w: number; h: number;
      color: string; vx: number; vy: number;
      rotation: number; rotationSpeed: number; opacity: number
    }

    const startConfetti = async (): Promise<void> => {
      showText.value = false
      await nextTick()

      const cvs = canvas.value
      if (!cvs) return

      cvs.width = window.innerWidth
      cvs.height = window.innerHeight
      const ctx = cvs.getContext('2d')

      const particles: Particle[] = []
      for (let i = 0; i < 150; i++) {
        particles.push({
          x: Math.random() * cvs.width,
          y: Math.random() * cvs.height - cvs.height,
          w: Math.random() * 10 + 5,
          h: Math.random() * 6 + 3,
          color: colors[Math.floor(Math.random() * colors.length)],
          vx: (Math.random() - 0.5) * 4,
          vy: Math.random() * 3 + 2,
          rotation: Math.random() * 360,
          rotationSpeed: (Math.random() - 0.5) * 10,
          opacity: 1
        })
      }

      let frame: number = 0
      const animate = (): void => {
        ctx.clearRect(0, 0, cvs.width, cvs.height)
        frame++

        let alive = false
        for (const p of particles) {
          p.x += p.vx
          p.vy += 0.05 // gravity
          p.y += p.vy
          p.rotation += p.rotationSpeed

          if (frame > 120) p.opacity -= 0.01
          if (p.opacity <= 0) continue
          alive = true

          ctx.save()
          ctx.translate(p.x, p.y)
          ctx.rotate((p.rotation * Math.PI) / 180)
          ctx.globalAlpha = Math.max(0, p.opacity)
          ctx.fillStyle = p.color
          ctx.fillRect(-p.w / 2, -p.h / 2, p.w, p.h)
          ctx.restore()
        }

        if (frame === 30) showText.value = true

        if (alive && frame < 300) {
          animationId = requestAnimationFrame(animate)
        }
      }

      animate()
    }

    const stopConfetti = (): void => {
      if (animationId) {
        cancelAnimationFrame(animationId)
        animationId = null
      }
      showText.value = false
    }

    watch(() => props.visible, (v: boolean) => {
      if (v) startConfetti()
      else stopConfetti()
    })
</script>

<style scoped>
.confetti-container {
  position: fixed;
  top: 0; left: 0; right: 0; bottom: 0;
  z-index: 2000;
  display: flex;
  align-items: center;
  justify-content: center;
}

.confetti-canvas {
  position: absolute;
  top: 0; left: 0;
  width: 100%;
  height: 100%;
  pointer-events: none;
}

.celebration-text {
  position: relative;
  z-index: 1;
  background: white;
  border-radius: 24px;
  padding: 32px 40px;
  text-align: center;
  box-shadow: 0 20px 60px rgba(0,0,0,0.3);
  opacity: 0;
  transform: scale(0.8);
  transition: all 0.4s cubic-bezier(0.34, 1.56, 0.64, 1);
}

.celebration-text.visible {
  opacity: 1;
  transform: scale(1);
}

.celebration-emoji {
  font-size: 48px;
  animation: bounce 0.6s ease infinite alternate;
}

@keyframes bounce {
  from { transform: translateY(0); }
  to { transform: translateY(-10px); }
}

.celebration-text h2 {
  font-size: 28px;
  margin: 12px 0 16px;
  color: #333;
}

.celebration-stats {
  display: flex;
  gap: 16px;
  justify-content: center;
  flex-wrap: wrap;
  margin-bottom: 20px;
  font-size: 14px;
  color: #666;
}

.celebration-btn {
  background: #4285f4;
  color: white;
  border: none;
  padding: 12px 32px;
  border-radius: 12px;
  font-size: 16px;
  font-weight: 600;
  cursor: pointer;
  transition: transform 0.2s;
}

.celebration-btn:hover {
  transform: scale(1.05);
}
</style>

