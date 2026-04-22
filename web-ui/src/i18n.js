// Lightweight i18n — English + Traditional Chinese
import { ref, computed } from 'vue'

const locale = ref(localStorage.getItem('sudoku-locale') || 'en')

const translations = {
  en: {
    // Header
    appTitle: '🧩 Sudoku Solver',
    home: '🏠',
    daily: '📅',
    learn: '📚',
    settings: '⚙️',
    leaderboard: '🏆',
    saves: '💾',
    darkMode: '🌙',

    // Dashboard
    dashboardTitle: 'Sudoku Dojo',
    dashboardSubtitle: 'Learn. Practice. Master.',
    play: '▶ Play',
    dailyChallenge: '📅 Daily Challenge',
    learnTechniques: '📚 Learn Techniques',
    quickSolve: '🧩 Quick Solve',

    // Game
    progress: 'Progress',
    time: 'Time',
    mistakes: 'Mistakes',
    hints: 'Hints',
    filled: 'Filled',
    solve: '🧩 Solve',
    clear: '🗑️ Clear',
    import: '📥 Import',
    share: '🔗 Share Puzzle',
    getHint: '💡 Get a Hint!',
    pencilMarks: 'Pencil Marks',
    newPuzzle: 'New Puzzle:',
    easy: 'Easy',
    medium: 'Medium',
    hard: 'Hard',
    undo: 'Undo',
    redo: 'Redo',

    // Settings
    settingsTitle: '⚙️ Settings',
    accessibility: 'Accessibility',
    colorBlind: 'Color-blind friendly',
    colorBlindDesc: 'Uses patterns + distinct colors for highlights',
    highContrast: 'High contrast',
    highContrastDesc: 'Stronger borders and text for visibility',
    darkModeLabel: 'Dark mode',
    darkModeDesc: 'Easier on the eyes at night',
    soundEffects: 'Sound effects',
    soundEffectsDesc: 'Audio feedback for actions',
    boardTheme: 'Board Theme',
    data: 'Data',
    resetProgress: 'Reset progress',
    resetProgressDesc: 'Clear all saved lesson progress and streaks',
    about: 'About',
    aboutText: 'Sudoku Dojo — Learn Sudoku step by step',
    back: '← Back',

    // Import
    importTitle: '📥 Import Puzzle',
    importDesc: 'Paste a Sudoku puzzle below. Use . or 0 for empty cells.',
    singleLine: 'Single Line',
    grid9x9: '9×9 Grid',
    loadExample: 'Load Example',
    cancel: 'Cancel',

    // Achievements
    achievementsTitle: '🏆 Achievements',
    statsTitle: '📊 Statistics',

    // Belt levels
    whiteBelt: 'White Belt',
    yellowBelt: 'Yellow Belt',
    orangeBelt: 'Orange Belt',
    greenBelt: 'Green Belt',
    blueBelt: 'Blue Belt',
    purpleBelt: 'Purple Belt',
    brownBelt: 'Brown Belt',
    blackBelt: 'Black Belt',

    // Messages
    puzzleImported: 'Puzzle imported! Tap Solve or solve it yourself.',
    puzzleGenerated: 'Generated {difficulty} puzzle!',
    puzzleSolved: 'Solved in {time}ms',
    noSolution: 'No solution found',
    linkCopied: 'Link Copied!',
    shareText: 'Share this link to challenge someone!',
    sharedLoaded: 'Shared puzzle loaded! Solve it yourself or tap Solve.',
    puzzleComplete: 'Puzzle Complete!',
    perfect: '✨ Perfect!',
    noHints: '🧠 No hints',
    continueBtn: 'Continue',
    resetAll: 'Reset All Statistics',
    exportCSV: '📥 Export as CSV',
    savePuzzle: '💾 Save Current Puzzle',
    loadBtn: '▶ Load',
    savedPuzzles: '💾 Saved Puzzles',
    noSavedPuzzles: 'No saved puzzles yet!',
    saveHint: 'Click "Save Puzzle" while playing to save your progress.',

    // Difficulty
    EASY: 'Easy',
    MEDIUM: 'Medium',
    HARD: 'Hard',
    EXPERT: 'Expert',
    MASTER: 'Master',

    language: 'Language',
    english: 'English',
    chinese: '中文',
    japanese: '日本語'
  },

  'zh-Hant': {
    // Header
    appTitle: '🧩 數獨求解器',
    home: '🏠',
    daily: '📅',
    learn: '📚',
    settings: '⚙️',
    leaderboard: '🏆',
    saves: '💾',
    darkMode: '🌙',

    // Dashboard
    dashboardTitle: '數獨道場',
    dashboardSubtitle: '學習。練習。精通。',
    play: '▶ 開始',
    dailyChallenge: '📅 每日挑戰',
    learnTechniques: '📚 學習技巧',
    quickSolve: '🧩 快速求解',

    // Game
    progress: '進度',
    time: '時間',
    mistakes: '錯誤',
    hints: '提示',
    filled: '已填',
    solve: '🧩 求解',
    clear: '🗑️ 清除',
    import: '📥 匯入',
    share: '🔗 分享謎題',
    getHint: '💡 獲取提示！',
    pencilMarks: '鉛筆標記',
    newPuzzle: '新謎題：',
    easy: '簡單',
    medium: '中等',
    hard: '困難',
    undo: '復原',
    redo: '重做',

    // Settings
    settingsTitle: '⚙️ 設定',
    accessibility: '無障礙',
    colorBlind: '色盲友善模式',
    colorBlindDesc: '使用圖案和明確顏色標記',
    highContrast: '高對比度',
    highContrastDesc: '更強的邊框和文字',
    darkModeLabel: '深色模式',
    darkModeDesc: '夜間使用更舒適',
    soundEffects: '音效',
    soundEffectsDesc: '操作時的音頻反饋',
    boardTheme: '棋盤主題',
    data: '資料',
    resetProgress: '重置進度',
    resetProgressDesc: '清除所有已保存的課程進度和連續記錄',
    about: '關於',
    aboutText: '數獨道場 — 一步步學習數獨',
    back: '← 返回',

    // Import
    importTitle: '📥 匯入謎題',
    importDesc: '在下方貼上數獨謎題。用 . 或 0 表示空格。',
    singleLine: '單行',
    grid9x9: '9×9 格子',
    loadExample: '載入範例',
    cancel: '取消',

    // Achievements
    achievementsTitle: '🏆 成就',
    statsTitle: '📊 統計',

    // Belt levels
    whiteBelt: '白帶',
    yellowBelt: '黃帶',
    orangeBelt: '橙帶',
    greenBelt: '綠帶',
    blueBelt: '藍帶',
    purpleBelt: '紫帶',
    brownBelt: '棕帶',
    blackBelt: '黑帶',

    // Messages
    puzzleImported: '謎題已匯入！點擊求解或自己解題。',
    puzzleGenerated: '已生成{difficulty}謎題！',
    puzzleSolved: '在{time}毫秒內解決',
    noSolution: '找不到解答',
    linkCopied: '連結已複製！',
    shareText: '分享此連結來挑戰他人！',
    sharedLoaded: '已載入分享的謎題！自己解或點擊求解。',
    puzzleComplete: '謎題完成！',
    perfect: '✨ 完美！',
    noHints: '🧠 無提示',
    continueBtn: '繼續',
    resetAll: '重置所有統計',
    exportCSV: '📥 匯出CSV',
    savePuzzle: '💾 保存當前謎題',
    loadBtn: '▶ 載入',
    savedPuzzles: '💾 已保存的謎題',
    noSavedPuzzles: '還沒有保存的謎題！',
    saveHint: '遊戲中點擊「保存謎題」來保存進度。',

    // Difficulty
    EASY: '簡單',
    MEDIUM: '中等',
    HARD: '困難',
    EXPERT: '專家',
    MASTER: '大師',

    language: '語言',
    english: 'English',
    chinese: '中文',
    japanese: '日本語'
  },

  ja: {
    // Header
    appTitle: '🧩 数独ソルバー',
    home: '🏠',
    daily: '📅',
    learn: '📚',
    settings: '⚙️',
    leaderboard: '🏆',
    saves: '💾',
    darkMode: '🌙',

    // Dashboard
    dashboardTitle: '数独道場',
    dashboardSubtitle: '学ぶ。練習する。極める。',
    play: '▶ プレイ',
    dailyChallenge: '📅 デイリーチャレンジ',
    learnTechniques: '📚 テクニックを学ぶ',
    quickSolve: '🧩 クイックソルブ',

    // Game
    progress: '進捗',
    time: '時間',
    mistakes: 'ミス',
    hints: 'ヒント',
    filled: '記入済',
    solve: '🧩 解く',
    clear: '🗑️ クリア',
    import: '📥 インポート',
    share: '🔗 パズルを共有',
    getHint: '💡 ヒントを見る！',
    pencilMarks: 'ペンシルメモ',
    newPuzzle: '新しいパズル：',
    easy: '簡単',
    medium: '普通',
    hard: '難しい',
    undo: '元に戻す',
    redo: 'やり直す',

    // Settings
    settingsTitle: '⚙️ 設定',
    accessibility: 'アクセシビリティ',
    colorBlind: '色覚サポート',
    colorBlindDesc: 'パターンと識別しやすい色を使用',
    highContrast: 'ハイコントラスト',
    highContrastDesc: '見やすさを向上する太い枠線とテキスト',
    darkModeLabel: 'ダークモード',
    darkModeDesc: '夜間の使用時に目に優しい',
    soundEffects: 'サウンドエフェクト',
    soundEffectsDesc: '操作時の音声フィードバック',
    boardTheme: 'ボードテーマ',
    data: 'データ',
    resetProgress: '進捗をリセット',
    resetProgressDesc: 'すべてのレッスンの進捗とストリークをクリア',
    about: 'について',
    aboutText: '数独道場 — 一步步に数独を学ぶ',
    back: '← 戻る',

    // Import
    importTitle: '📥 パズルをインポート',
    importDesc: '下に数独パズルを貼り付けてください。. または 0 を空マスに使用。',
    singleLine: '1行入力',
    grid9x9: '9×9 グリッド',
    loadExample: '例を読み込む',
    cancel: 'キャンセル',

    // Achievements
    achievementsTitle: '🏆 実績',
    statsTitle: '📊 統計',

    // Belt levels
    whiteBelt: '白帯',
    yellowBelt: '黄帯',
    orangeBelt: '橙帯',
    greenBelt: '緑帯',
    blueBelt: '青帯',
    purpleBelt: '紫帯',
    brownBelt: '茶帯',
    blackBelt: '黒帯',

    // Messages
    puzzleImported: 'パズルをインポートしました！解くをタップするか自分で解いてください。',
    puzzleGenerated: '{difficulty}パズルを生成しました！',
    puzzleSolved: '{time}msで解けました',
    noSolution: '解が見つかりません',
    linkCopied: 'リンクをコピーしました！',
    shareText: 'このリンクを共有して誰かをチャレンジ！',
    sharedLoaded: '共有パズルを読み込みました！自分で解くか解くをタップ。',
    puzzleComplete: 'パズル完成！',
    perfect: '✨ パーフェクト！',
    noHints: '🧠 ヒントなし',
    continueBtn: '続ける',
    resetAll: 'すべての統計をリセット',
    exportCSV: '📥 CSVでエクスポート',
    savePuzzle: '💾 現在のパズルを保存',
    loadBtn: '▶ 読み込む',
    savedPuzzles: '💾 保存したパズル',
    noSavedPuzzles: '保存したパズルはまだありません！',
    saveHint: 'プレイ中に「パズルを保存」をクリックして進捗を保存。',

    // Difficulty
    EASY: '簡単',
    MEDIUM: '普通',
    HARD: '難しい',
    EXPERT: 'エキスパート',
    MASTER: 'マスター',

    language: '言語',
    english: 'English',
    chinese: '中文',
    japanese: '日本語'
  }
}

export function useI18n() {
  const t = (key) => {
    return translations[locale.value]?.[key] || translations.en[key] || key
  }

  const setLocale = (l) => {
    locale.value = l
    localStorage.setItem('sudoku-locale', l)
  }

  const currentLocale = computed(() => locale.value)

  return { t, setLocale, currentLocale }
}

export { locale }
