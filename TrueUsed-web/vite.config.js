import vue from '@vitejs/plugin-vue'
import path from 'path'
import UnoCSS from 'unocss/vite'
import { defineConfig, loadEnv } from 'vite'

export default defineConfig(({ mode }) => {
  const env = loadEnv(mode, process.cwd(), '')
  const apiProxyTarget = env.VITE_API_PROXY_TARGET || 'http://localhost:8081'

  return {
    plugins: [vue(), UnoCSS()],
    build: {
      chunkSizeWarningLimit: 600,
      rollupOptions: {
        output: {
          manualChunks(id) {
            if (!id.includes('node_modules')) return

            if (id.includes('chart.js')) return 'vendor-chart'
            if (id.includes('lucide')) return 'vendor-icons'
            if (
              id.includes('/vue/') ||
              id.includes('/@vue/') ||
              id.includes('pinia') ||
              id.includes('vue-router')
            ) {
              return 'vendor-vue'
            }
            if (id.includes('vant') || id.includes('@vant')) return 'vendor-ui'
            if (id.includes('@stomp') || id.includes('sockjs') || id.includes('socketjs')) {
              return 'vendor-realtime'
            }

            return 'vendor-misc'
          },
        },
      },
    },
    resolve: {
      alias: {
        '@': path.resolve(__dirname, 'src'),
      },
    },
    server: {
      port: 5173,
      host: '0.0.0.0',
      allowedHosts: true,
      open: true,
      proxy: {
        // 将 /api 代理到后端 Spring Boot
        '/api': {
          target: apiProxyTarget,
          changeOrigin: true,
          ws: true,
        },
      },
    },
  }
})
