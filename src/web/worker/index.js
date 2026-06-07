export default {
    async fetch(request, env) {
        const accept = request.headers.get('Accept') || '';
        const url = new URL(request.url);
        if (
            accept.includes('text/markdown') &&
            (url.pathname == '/' || url.pathname == '/index.html')
        ) {
            const mdUrl = new URL('/index.md', request.url);
            const mdResponse = await env.ASSETS.fetch(new Request(mdUrl.toString()));
            if (mdResponse.ok) {
                const text = await mdResponse.text();
                const tokenCount = Math.ceil(text.length / 4);
                return new Response(text, {
                    status: 200,
                    headers: {
                        'Content-Type': 'text/markdown; charset=utf-8',
                        'X-Markdown-Tokens': tokenCount.toString(),
                        'Vary': 'Accept',
                    },
                });
            }
        }
        const response = await env.ASSETS.fetch(request);
        if (url.pathname == '/download') {
            const ghUrl = 'https://github.com/LoryPelli/JDER/releases/download/JDER/JDER.jar';
            const ghResponse = await fetch(ghUrl);
            if (!ghResponse.ok) {
                return Response.redirect('https://github.com/LoryPelli/JDER/releases/latest', 302);
            }
            return new Response(ghResponse.body, {
                status: ghResponse.status,
                statusText: ghResponse.statusText,
                headers: ghResponse.headers,
            });
        }
        if (url.pathname == '/' || url.pathname == '/index.html') {
            const headers = new Headers(response.headers);
            headers.set(
                'Link',
                '<https://github.com/LoryPelli/JDER>; rel="service-doc", </sitemap.xml>; rel="sitemap"',
            );
            headers.set('Vary', 'Accept');
            return new Response(response.body, {
                status: response.status,
                statusText: response.statusText,
                headers,
            });
        }
        if (!response.ok) {
            return Response.redirect(new URL('/', request.url).toString(), 302);
        }
        return response;
    },
};
