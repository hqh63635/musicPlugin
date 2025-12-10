import { defineConfig } from 'vite';
import vue from '@vitejs/plugin-vue';
import { resolve } from 'path';
import svgLoader from 'vite-svg-loader';

// https://vite.dev/config/
export default defineConfig({
  plugins: [vue(), svgLoader()],
  resolve: {
    alias: {
      '@': resolve(__dirname, './src'),
    },
  },
  server: {
    host: '0.0.0.0',
    port: 5173,
    // 配置代理解决跨域问题
    proxy: {
      // 通用QQ音乐API代理 - c.y.qq.com
      '/api': {
        target: 'https://c.y.qq.com',
        changeOrigin: true,
        rewrite: path => path.replace(/^\/api/, ''),
      },
      // QQ Music API proxy for u.y.qq.com (musicu.fcg)
      '/musicu': {
        target: 'https://u.y.qq.com',
        changeOrigin: true,
        rewrite: path => path.replace(/^\/musicu/, '/cgi-bin/musicu.fcg'),
      },
      // Fallback proxy for any other QQ domains
      '/qqapi': {
        target: 'https://u.y.qq.com',
        changeOrigin: true,
        rewrite: path => path.replace(/^\/qqapi/, ''),
      },
      // 海棠音乐API代理
      '/haitang': {
        target: 'http://musicapi.haitangw.net',
        changeOrigin: true,
        rewrite: path => path.replace(/^\/haitang/, ''),
      },
      '/qqmusic': {
        target: 'https://u6.y.qq.com',
        changeOrigin: true,
        rewrite: path => path.replace(/^\/qqmusic/, ''),
      },
    },
  },
});
