import { configDefaults, defineConfig } from 'vitest/config';
import react from '@vitejs/plugin-react';

export default defineConfig({
  plugins: [react()],
  server: { port: 5173 },
  test: {
    environment: 'jsdom',
    setupFiles: './src/test-setup.js',
    // Playwright specs run separately through the test:e2e script.
    exclude: [...configDefaults.exclude, '**/e2e/**'],
  },
});
