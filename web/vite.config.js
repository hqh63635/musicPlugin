import { defineConfig } from 'vite';
import vue from '@vitejs/plugin-vue';
import { resolve } from 'path';
import svgLoader from 'vite-svg-loader';

export default defineConfig({
  plugins: [vue(), svgLoader(), {
    name: 'changqing-resolve',
    configureServer(server) {
      server.middlewares.use('/changqing-resolve', async (req, res) => {
        try {
          const params = new URL(req.url, 'http://localhost').searchParams;
          const query = new URLSearchParams({ type: 'mp3', id: params.get('id') || '', level: params.get('level') || 'exhigh' });
          const upstream = await fetch(`http://175.27.166.236/kgqq1/qq.php?${query}`, {
            redirect: 'manual', signal: AbortSignal.timeout(15000),
          });
          const location = upstream.headers.get('location');
          await upstream.body?.cancel();
          if (!location || ![301, 302, 303, 307, 308].includes(upstream.status)) throw new Error('No audio redirect');
          const url = new URL(location, 'http://175.27.166.236').href;
          if (!/^https?:\/\//.test(url)) throw new Error('Invalid audio URL');
          res.setHeader('Content-Type', 'application/json');
          res.setHeader('Cache-Control', 'no-store');
          res.end(JSON.stringify({ url }));
        } catch {
          res.statusCode = 502;
          res.end('Failed to resolve audio URL');
        }
      });
    },
  }],
  resolve: {
    alias: {
      '@': resolve(__dirname, './src'),
    },
  },
  server: {
    host: '0.0.0.0',
    port: 5173,
    proxy: {
      '/changqing': {
        target: 'http://175.27.166.236',
        changeOrigin: true,
        rewrite: path => path.replace(/^\/changqing/, ''),
        timeout: 60000,
        proxyTimeout: 60000,
      },
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
