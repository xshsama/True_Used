import vue from '@vitejs/plugin-vue'
import path from 'path'
import UnoCSS from 'unocss/vite'
import { defineConfig, loadEnv } from 'vite'
import { visualizer } from 'rollup-plugin-visualizer'
import viteCompression from 'vite-plugin-compression' // 1. 引入压缩插件

export default defineConfig(({ mode }) => {
  const env = loadEnv(mode, process.cwd(), '')
  const apiProxyTarget = env.VITE_API_PROXY_TARGET || 'http://localhost:8081'

  return {
    plugins: [
      vue(), 
      UnoCSS(),
      visualizer({
        open: true,
        filename: 'stats.html',
        gzipSize: true,
        brotliSize: true
      }),
      // 2. 启用 Gzip 压缩配置
      viteCompression({
        verbose: true,     // 是否在控制台输出压缩结果
        disable: false,    // 是否禁用
        threshold: 10240,  // 体积大于 10KB 的文件才进行压缩
        algorithm: 'gzip', // 压缩算法
        ext: '.gz',        // 生成的文件后缀
      })
    ],
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
        '/api': {
          target: apiProxyTarget,
          changeOrigin: true,
          ws: true,
        },
      },
    },
  }
})