import { defineConfig } from 'vite';
import vue from '@vitejs/plugin-vue';
import { resolve } from 'path';
import svgLoader from 'vite-svg-loader';

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
    proxy: {
      '/api/music': {
        target: 'http://103.79.184.97',
        changeOrigin: true,
        timeout: 60000,
        proxyTimeout: 60000,
      },
      '/zddyr': {
        target: 'https://yy.zddyr.top',
        changeOrigin: true,
        rewrite: path => path.replace(/^\/zddyr/, ''),
        timeout: 60000,
        proxyTimeout: 60000,
      },
      '/api': {
        target: 'https://c.y.qq.com',
        changeOrigin: true,
        rewrite: path => path.replace(/^\/api/, ''),
        headers: {
          Referer: 'https://y.qq.com/',
          Host: 'c.y.qq.com',
        },
      },
      '/musicu': {
        target: 'https://u.y.qq.com',
        changeOrigin: true,
        rewrite: path => path.replace(/^\/musicu/, '/cgi-bin/musicu.fcg'),
      },
      '/qqapi': {
        target: 'https://u.y.qq.com',
        changeOrigin: true,
        rewrite: path => path.replace(/^\/qqapi/, ''),
      },
      '/haitang': {
        target: 'https://musicapi.haitangw.net',
        changeOrigin: true,
        rewrite: path => path.replace(/^\/haitang/, ''),
        headers: {
          Referer: 'https://musicapi.haitangw.net',
          Host: 'musicapi.haitangw.net',
        },
      },
      '/qqmusic': {
        target: 'https://u6.y.qq.com',
        changeOrigin: true,
        rewrite: path => path.replace(/^\/qqmusic/, ''),
      },
      '/qzone': {
        target: 'http://i.y.qq.com',
        changeOrigin: true,
        rewrite: path => path.replace(/^\/qzone/, ''),
        headers: {
          Referer: 'http://i.y.qq.com/',
          Host: 'i.y.qq.com',
        },
      },
      '/luoxue': {
        target: 'https://lxmusicapi.onrender.com',
        changeOrigin: true,
        rewrite: path => path.replace(/^\/luoxue/, ''),
        headers: {
          Referer: 'https://lxmusicapi.onrender.com',
          Host: 'lxmusicapi.onrender.com',
        },
      },
    },
  },
  build: {
    assetsInclude: ['**/*.json'],
  },
});
