import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

// https://vite.dev/config/
export default defineConfig({
  plugins: [react()],
  server: {
    host: true,
    port: 3000,
    strictPort: true
  },
  // Remove proxy configuration as it's not needed for production
  // Proxy is only for local development and conflicts with production environment variables
})