// Sound effects using Web Audio API — no external files needed
let audioCtx = null

function getCtx() {
  if (!audioCtx) {
    audioCtx = new (window.AudioContext || window.webkitAudioContext)()
  }
  return audioCtx
}

function playTone(freq, duration, type = 'sine', volume = 0.15) {
  try {
    const ctx = getCtx()
    const osc = ctx.createOscillator()
    const gain = ctx.createGain()
    osc.type = type
    osc.frequency.value = freq
    gain.gain.setValueAtTime(volume, ctx.currentTime)
    gain.gain.exponentialRampToValueAtTime(0.001, ctx.currentTime + duration)
    osc.connect(gain)
    gain.connect(ctx.destination)
    osc.start(ctx.currentTime)
    osc.stop(ctx.currentTime + duration)
  } catch (e) {
    // Silently fail — audio not critical
  }
}

export const sounds = {
  // Cell placement — short click
  place() { playTone(800, 0.05, 'sine', 0.1) },

  // Correct placement — pleasant ding
  correct() { playTone(1200, 0.15, 'sine', 0.12) },

  // Wrong placement — low buzz
  wrong() { playTone(200, 0.2, 'square', 0.08) },

  // Hint — gentle chime
  hint() {
    playTone(880, 0.1, 'sine', 0.1)
    setTimeout(() => playTone(1100, 0.15, 'sine', 0.1), 100)
  },

  // Puzzle solved — triumphant fanfare
  solved() {
    playTone(523, 0.15, 'sine', 0.12) // C
    setTimeout(() => playTone(659, 0.15, 'sine', 0.12), 120) // E
    setTimeout(() => playTone(784, 0.15, 'sine', 0.12), 240) // G
    setTimeout(() => playTone(1047, 0.3, 'sine', 0.15), 360) // C octave
  },

  // Achievement unlocked — sparkle
  achievement() {
    playTone(1200, 0.08, 'sine', 0.1)
    setTimeout(() => playTone(1500, 0.08, 'sine', 0.1), 80)
    setTimeout(() => playTone(1800, 0.15, 'sine', 0.12), 160)
  },

  // Tutorial step — soft ping
  step() { playTone(660, 0.08, 'sine', 0.08) },

  // Error — short beep
  error() { playTone(300, 0.12, 'square', 0.06) },

  // Button click — subtle tap
  click() { playTone(1000, 0.03, 'sine', 0.05) },
}

// Check if sound is enabled
const SOUND_KEY = 'sudoku-dojo-sound'

export function isSoundEnabled() {
  return localStorage.getItem(SOUND_KEY) !== 'false'
}

export function setSoundEnabled(enabled) {
  localStorage.setItem(SOUND_KEY, enabled ? 'true' : 'false')
}

// Play sound only if enabled
export function playSound(name) {
  if (isSoundEnabled() && sounds[name]) {
    sounds[name]()
  }
}
