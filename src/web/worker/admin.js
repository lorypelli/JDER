import { Hono } from 'hono';
import { deleteCookie, getCookie, setCookie } from 'hono/cookie';
import { csrf } from 'hono/csrf';
import { HTTPException } from 'hono/http-exception';
import { jwt, sign, verifyWithJwks } from 'hono/jwt';
import PostalMime, { addressParser, decodeWords } from 'postal-mime';
import { WorkerMailer } from 'worker-mailer';

const issuer = 'https://polpomail.com';
const audience = 'jder-admin';
const duration = 3 * 24 * 60 * 60;
const mailbox = 'support@jder.it';
const smtpHost = 'mx.polpomail.com';
const formAddress = 'no-reply@mx.polpomail.com';
const formSender = /^"?(.*?)\s*\(web form\).*<no-reply@mx\.polpomail\.com>$/i;
const replyAddress = /^[^\s<>"]+@[^\s<>"]+$/;
const senderName = /^(?:(?!["<>\\])[ -~]){1,50}$/;
const cookie = {
    prefix: 'host',
    httpOnly: true,
    secure: true,
    sameSite: 'Strict',
};

const base64url = (bytes) =>
    new Uint8Array(bytes).toBase64({
        alphabet: 'base64url',
        omitPadding: true,
    });

const random = () => base64url(crypto.getRandomValues(new Uint8Array(32)));

const polpo = async ({ env }, path, init) => {
    const response = await fetch(`${issuer}/api/v1/${path}`, {
        ...init,
        headers: {
            Authorization: `Bearer ${env.POLPO_ACCESS_TOKEN}`,
            'Content-Type': 'application/json',
        },
    });
    if (!response.ok) {
        throw new HTTPException(response.status == 404 ? 404 : 502);
    }
    return response;
};

const smtp = async ({ env }, { name, to, subject, text }) => {
    const mailer = await WorkerMailer.connect({
        host: smtpHost,
        port: 465,
        secure: true,
        authType: 'plain',
        credentials: { username: mailbox, password: env.POLPO_ACCESS_TOKEN },
    }).catch(() => {
        throw new HTTPException(502);
    });
    try {
        await mailer.send({
            from: { name, email: mailbox },
            to,
            subject,
            text,
        });
    } catch {
        throw new HTTPException(502);
    } finally {
        await mailer.close();
    }
};

const isForm = (from = '') => formSender.test(from);

const isName = (name) => senderName.test(name);

const submitter = (subject) => {
    const group = subject.slice(subject.lastIndexOf('(') + 1).split(')')[0];
    const addresses = addressParser(group).filter(({ address }) => address);
    return addresses.length == 1 ? addresses[0].address : undefined;
};

const read = async (c) => {
    const response = await polpo(c, `messages/${c.req.param('id')}/raw`);
    const email = await PostalMime.parse(await response.arrayBuffer());
    const from = decodeWords(
        email.headers.find(({ key }) => key == 'from')?.value || '',
    );
    if (!isForm(from)) {
        throw new HTTPException(404);
    }
    return {
        text: email.text,
        html: email.html,
        reply: {
            to: email.replyTo?.[0]?.address || submitter(email.subject || ''),
            subject: `Re: ${from.match(formSender)[1]}`,
        },
    };
};

const mark = (c, flags) =>
    polpo(c, `messages/${c.req.param('id')}`, {
        method: 'PATCH',
        body: JSON.stringify(flags),
    }).catch(() => {});

const identify = async (c) => {
    const [state, nonce, verifier] = (
        getCookie(c, 'jder-login', cookie.prefix) || ''
    ).split('.');
    const { code, state: returned, iss } = c.req.query();
    if (!state || returned != state || iss != issuer || !code) {
        throw new Error('invalid callback');
    }
    const { POLPO_CLIENT_ID, POLPO_CLIENT_SECRET, POLPO_REDIRECT_URI } = c.env;
    const response = await fetch(`${issuer}/oauth/token`, {
        method: 'POST',
        headers: {
            Authorization: `Basic ${btoa(`${POLPO_CLIENT_ID}:${POLPO_CLIENT_SECRET}`)}`,
        },
        body: new URLSearchParams({
            grant_type: 'authorization_code',
            code,
            redirect_uri: POLPO_REDIRECT_URI,
            code_verifier: verifier,
        }),
    });
    const { id_token } = await response.json();
    const claims = await verifyWithJwks(id_token, {
        jwks_uri: `${issuer}/oauth/jwks.json`,
        allowedAlgorithms: ['RS256'],
        verification: { iss: issuer, aud: POLPO_CLIENT_ID, iat: false },
    });
    if (
        claims.nonce != nonce ||
        !claims.email_verified ||
        !claims.email.endsWith('@jder.it')
    ) {
        throw new Error('not allowed');
    }
    return claims;
};

const admin = new Hono();

admin.use(csrf());

admin.get('/login', async (c) => {
    const [state, nonce, verifier] = [random(), random(), random()];
    deleteCookie(c, 'jder-denied', cookie);
    setCookie(c, 'jder-login', `${state}.${nonce}.${verifier}`, {
        ...cookie,
        sameSite: 'Lax',
        maxAge: 5 * 60,
    });
    const challenge = await crypto.subtle.digest(
        'SHA-256',
        new TextEncoder().encode(verifier),
    );
    const query = new URLSearchParams({
        response_type: 'code',
        client_id: c.env.POLPO_CLIENT_ID,
        redirect_uri: c.env.POLPO_REDIRECT_URI,
        scope: 'openid email',
        state,
        nonce,
        code_challenge: base64url(challenge),
        code_challenge_method: 'S256',
    });
    return c.redirect(`${issuer}/oauth/authorize?${query}`);
});

admin.get('/callback', async (c) => {
    try {
        const { sub, email } = await identify(c);
        const exp = Math.floor(Date.now() / 1000) + duration;
        const token = await sign(
            { sub, email, aud: audience, exp },
            c.env.POLPO_CLIENT_SECRET,
        );
        setCookie(c, 'jder-admin', token, { ...cookie, maxAge: duration });
    } catch {
        setCookie(c, 'jder-denied', '1', { ...cookie, maxAge: 15 });
    }
    deleteCookie(c, 'jder-login', cookie);
    return c.redirect('/admin');
});

admin.post('/logout', (c) => {
    deleteCookie(c, 'jder-admin', cookie);
    return c.body(null, 204);
});

admin.use('/api/*', async (c, next) => {
    c.header('Cache-Control', 'no-store');
    if (getCookie(c, 'jder-denied', cookie.prefix)) {
        deleteCookie(c, 'jder-denied', cookie);
        return c.body(null, 403);
    }
    const session = jwt({
        secret: c.env.POLPO_CLIENT_SECRET,
        alg: 'HS256',
        cookie: { key: 'jder-admin', prefixOptions: cookie.prefix },
        verification: { aud: audience },
    });
    await session(c, next);
});

admin.get('/api/messages', async (c) => {
    const offset = Number(c.req.query('offset') || 0);
    if (!Number.isInteger(offset) || offset < 0) {
        throw new HTTPException(400);
    }
    const query = new URLSearchParams({
        mailbox: 'INBOX',
        q: formAddress,
        limit: 50,
        offset,
    });
    const response = await polpo(c, `messages?${query}`);
    const { messages, total } = await response.json();
    const next = offset + messages.length;
    return c.json({
        messages: messages.filter(({ from }) => isForm(from)),
        next: messages.length && next < total ? next : null,
    });
});

admin.get('/api/messages/:id{[0-9]+}', async (c) => {
    const message = await read(c);
    await mark(c, { seen: true });
    return c.json(message);
});

admin.post('/api/messages/:id{[0-9]+}/reply', async (c) => {
    const { text, name } = await c.req.json().catch(() => ({}));
    const { to, subject } = (await read(c)).reply;
    const sender = typeof name == 'string' ? name.trim() : '';
    if (
        typeof text != 'string' ||
        !text.trim() ||
        !replyAddress.test(to) ||
        !isName(sender)
    ) {
        throw new HTTPException(400);
    }
    await smtp(c, { name: sender, to, subject, text });
    await mark(c, { answered: true });
    return c.json({
        name: sender,
        address: mailbox,
        to,
        subject,
        text,
        date: new Date().toISOString(),
    });
});

export default admin;
