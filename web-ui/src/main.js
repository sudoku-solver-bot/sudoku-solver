import { createApp } from 'vue'
import App from './App.vue'
import './splash.css'

// Remove splash after app mounts
const removeSplash = () => {
  const splash = document.getElementById('splash')
  if (splash) {
    splash.classList.add('fade-out')
    setTimeout(() => splash.remove(), 500)
  }
}

const app = createApp(App)
app.mount('#app')
removeSplash()
