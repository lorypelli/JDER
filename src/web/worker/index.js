export default {
    async fetch(request, env) {
        const accept = request.headers.get('Accept') || '';
        const url = new URL(request.url);
        if (url.pathname == '/download/JDER.jar') {
            const ghUrl =
                'https://github.com/LoryPelli/JDER/releases/download/JDER/JDER.jar';
            const ghResponse = await fetch(ghUrl, { method: request.method });
            if (!ghResponse.ok) {
                return Response.redirect(
                    'https://github.com/LoryPelli/JDER/releases/latest',
                    302,
                );
            }
            return new Response(ghResponse.body, {
                status: ghResponse.status,
                statusText: ghResponse.statusText,
                headers: ghResponse.headers,
            });
        }
        if (accept.includes('text/markdown') && url.pathname == '/') {
            const mdUrl = new URL('/index.md', request.url);
            const mdResponse = await env.ASSETS.fetch(
                new Request(mdUrl.toString()),
            );
            if (mdResponse.ok) {
                const text = await mdResponse.text();
                const tokenCount = Math.ceil(text.length / 4);
                return new Response(text, {
                    status: 200,
                    headers: {
                        'Content-Type': 'text/markdown; charset=utf-8',
                        'Content-Signal':
                            'search=yes, ai-input=yes, ai-train=yes',
                        'X-Markdown-Tokens': tokenCount.toString(),
                        Vary: 'Accept',
                    },
                });
            }
        }
        const response = await env.ASSETS.fetch(request);
        if (url.pathname == '/') {
            const headers = new Headers(response.headers);
            headers.set(
                'Link',
                '<https://github.com/LoryPelli/JDER>; rel="service-doc", </sitemap.xml>; rel="sitemap", </index.md>; rel="alternate"; type="text/markdown"',
            );
            headers.set('Vary', 'Accept');
            return new Response(response.body, {
                status: response.status,
                statusText: response.statusText,
                headers,
            });
        }
        return response;
    },
};
