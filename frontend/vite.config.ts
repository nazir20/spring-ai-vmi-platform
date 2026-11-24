import react from '@vitejs/plugin-react'
import { defineConfig } from 'vite'

const apiProxyTarget = process.env.ORBIT_API_PROXY_TARGET

export default defineConfig({
  plugins: [react()],
  server: {
    proxy: apiProxyTarget
      ? { '/api': { target: apiProxyTarget, changeOrigin: true } }
      : undefined,
  },
})
