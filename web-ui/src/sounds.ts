// Sound effects using Web Audio API — no external files needed
let audioCtx: AudioContext | null = null

function getCtx(): AudioContext {
  if (!audioCtx) {
    audioCtx = new (window.AudioContext || (window as unknown as { webkitAudioContext: typeof AudioContext }).webkitAudioContext)()
  }
  return audioCtx
}

type OscillatorType = 'sine' | 'square' | 'sawtoose' | 'triangle'

function playTone(freq: number, duration: number, type: OscillatorType = 'sine', volume = 0.15): void {
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
  } catch (_) { /* audio not critical */ }
}

export const sounds: Record<string, () => void> = {
  place() { playTone(800, 0.05, 'sine', 0.1) },
  correct() { playTone(1200, 0.15, 'sine', 0.12) },
  wrong() { playTone(200, 0.2, 'square', 0.08) },
  hint() {
    playTone(880, 0.1, 'sine', 0.1)
    setTimeout(() => playTone(1100, 0.15, 'sine', 0.1), 100)
  },
  solved() {
    playTone(523, 0.15, 'sine', 0.12)
    setTimeout(() => playTone(659, 0.15, 'sine', 0.12), 120)
    setTimeout(() => playTone(784, 0.15, 'sine', 0.12), 240)
    setTimeout(() => playTone(1047, 0.3, 'sine', 0.15), 360)
  },
  achievement() {
    playTone(1200, 0.08, 'sine', 0.1)
    setTimeout(() => playTone(1500, 0.08, 'sine', 0.1), 80)
    setTimeout(() => playTone(1800, 0.15, 'sine', 0.12), 160)
  },
  step() { playTone(660, 0.08, 'sine', 0.08) },
  error() { playTone(300, 0.12, 'square', 0.06) },
  click() { playTone(1000, 0.03, 'sine', 0.05) },
}

const SOUND_KEY = 'sudoku-dojo-sound'

export function isSoundEnabled(): boolean {
  return localStorage.getItem(SOUND_KEY) !== 'false'
}

export function setSoundEnabled(enabled: boolean): void {
  localStorage.setItem(SOUND_KEY, enabled ? 'true' : 'false')
}

export function playSound(name: string): void {
  if (isSoundEnabled() && sounds[name]) {
    sounds[name]()
  }
}
