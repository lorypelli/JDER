import { defineConfig } from 'vite';

export default defineConfig({
    base: '/app/',
    build: {
        rollupOptions: {
            input: 'index.html',
        },
    },
});
