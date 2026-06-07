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
                        'x-markdown-tokens': tokenCount.toString(),
                        'Vary': 'Accept',
                    },
                });
            }
        }
        const response = await env.ASSETS.fetch(request);
        if (!response.ok) {
            return Response.redirect(new URL('/', request.url).toString(), 302);
        }
        if (url.pathname == '/guida.pdf') {
            const newResponse = new Response(response.body, response);
            newResponse.headers.set('Content-Disposition', 'inline; filename="guida.pdf"');
            return newResponse;
        }
        if (url.pathname == '/class.png') {
            const newResponse = new Response(response.body, response);
            newResponse.headers.set('Content-Disposition', 'inline; filename="class.png"');
            return newResponse;
        }
        if (url.pathname == '/er.png') {
            const newResponse = new Response(response.body, response);
            newResponse.headers.set('Content-Disposition', 'inline; filename="er.png"');
            return newResponse;
        }
        if (url.pathname == '/usecase.png') {
            const newResponse = new Response(response.body, response);
            newResponse.headers.set('Content-Disposition', 'inline; filename="usecase.png"');
            return newResponse;
        }
        if (url.pathname == '/download') {
            const url = 'https://github.com/LoryPelli/JDER/releases/download/JDER/JDER.jar';
            const res = await fetch(url);
            if (!res.ok) {
                return Response.redirect('https://github.com/LoryPelli/JDER/releases/latest', 302);
            }
            const newResponse = new Response(res.body, res);
            newResponse.headers.set('Content-Disposition', 'attachment; filename="JDER.jar"');
            return newResponse;
        }
        if (url.pathname == '/' || url.pathname == '/index.html') {
            const newResponse = new Response(response.body, response);
            newResponse.headers.set(
                'Link',
                '<https://github.com/LoryPelli/JDER>; rel="service-doc", </sitemap.xml>; rel="sitemap"',
            );
            newResponse.headers.set('Vary', 'Accept');
            return newResponse;
        }
        return response;
    },
};
