import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'
import tailwindcss from '@tailwindcss/vite'

export default defineConfig({
  plugins: [react(), tailwindcss()],
  server: {
    host: true,
    // Previews are served through a wildcard proxy (project-<id>.previews.<domain>)
    allowedHosts: true,
  },
})
