process.loadEnvFile('.env.local');
process.env.VITE_SRC = process.env.ENTRY_PATH;

export default ({ mode }) => ({
    appType: 'mpa',
    base: mode == 'production' ? '/app/' : '/',
    publicDir: mode != 'production' && `${process.env.PWD}/src/web`,
});
