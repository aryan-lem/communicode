import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';

export default defineConfig({
  plugins: [react()],
  resolve: {
    alias: {
      'sockjs-client': 'sockjs-client/dist/sockjs.min.js',
    }
  },
  define: {
    global: 'window',
  },
  server: {
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
      },
      '/code-editor-ws': {
        target: 'http://localhost:8080',
        changeOrigin: true,
        ws: true,
      }
    }
  }
});