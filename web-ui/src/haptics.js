// Haptic feedback utility
export function vibrate(pattern = 10) {
  try {
    if (navigator.vibrate) navigator.vibrate(pattern)
  } catch (e) {}
}

export const haptics = {
  tap: () => vibrate(10),
  success: () => vibrate([10, 50, 10]),
  error: () => vibrate([50, 30, 50]),
  medium: () => vibrate(20),
}
