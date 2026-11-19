process.loadEnvFile('.env.local');

export default ({ mode }) => ({
    appType: 'mpa',
    base: mode == 'production' ? '/app/' : '/',
    publicDir: mode != 'production' && process.env.VITE_SITE_DIR,
});
