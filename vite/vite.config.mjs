process.loadEnvFile('.env.local');
process.env.VITE_SRC = process.env.ENTRY_PATH;

const wrangler = 'http://localhost:3000';

export default async ({ command }) => {
    if (command == 'serve') {
        const { unstable_startWorker } = await import('wrangler');
        await unstable_startWorker({
            config: `${process.env.PWD}/wrangler.json`,
        });
    }
    return {
        appType: 'mpa',
        base: '/app/',
        publicDir: command == 'serve' && `${process.env.PWD}/src/web`,
        server: { proxy: { '^(?!/app)': wrangler } },
    };
};
