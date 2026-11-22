import { Hono } from 'hono';
import admin from './admin.js';

const repository = 'https://github.com/LoryPelli/JDER';

const app = new Hono();

app.get('/download/JDER.jar', async (c) => {
    const response = await fetch(
        `${repository}/releases/download/JDER/JDER.jar`,
        { method: c.req.method },
    );
    if (!response.ok) {
        return c.redirect(`${repository}/releases/latest`);
    }
    return response;
});

app.get('/', async (c) => {
    if (c.req.header('Accept')?.includes('text/markdown')) {
        const markdown = await c.env.ASSETS.fetch(
            new URL('/index.md', c.req.url),
        );
        if (markdown.ok) {
            const text = await markdown.text();
            return c.body(text, 200, {
                'Content-Type': 'text/markdown; charset=utf-8',
                'Content-Signal': 'search=yes, ai-input=yes, ai-train=yes',
                'X-Markdown-Tokens': Math.ceil(text.length / 4).toString(),
                Vary: 'Accept',
            });
        }
    }
    const asset = await c.env.ASSETS.fetch(c.req.raw);
    const response = new Response(asset.body, asset);
    response.headers.set(
        'Link',
        `<${repository}>; rel="service-doc", </sitemap.xml>; rel="sitemap", </index.md>; rel="alternate"; type="text/markdown"`,
    );
    response.headers.set('Vary', 'Accept');
    return response;
});

app.route('/admin', admin);

export default app;
