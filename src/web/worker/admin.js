import EmailReplyParser from 'email-reply-parser';
import { Hono } from 'hono';
import { deleteCookie, getCookie, setCookie } from 'hono/cookie';
import { csrf } from 'hono/csrf';
import { HTTPException } from 'hono/http-exception';
import { jwt, sign, verifyWithJwks } from 'hono/jwt';
import PostalMime, { addressParser, decodeWords } from 'postal-mime';
import { LogLevel, WorkerMailer } from 'worker-mailer';

const issuer = 'https://polpomail.com';
const audience = 'jder-admin';
const duration = 3 * 24 * 60 * 60;
const mailbox = 'support@jder.it';
const smtpHost = 'mx.polpomail.com';
const formAddress = 'no-reply@mx.polpomail.com';
const formSender = new RegExp(
    `^"?(.*?)\\s*\\(web form\\).*<${RegExp.escape(formAddress)}>$`,
    'i',
);
const replyAddress = /^[^\s<>"]+@[^\s<>"]+$/;
const signature = /^t=(\d+),v1=([0-9a-f]{64})$/;
const tolerance = 5 * 60;
const receipt = {
    name: 'JDER Support',
    text: 'Hi,\n\nthank you for your feedback! We have received your message and will get back to you as soon as possible.\n\nIf you have any further questions or want to add something, just reply to this email.\n\nJDER Support',
    headers: { 'Auto-Submitted': 'auto-replied' },
};
const automatic = /^(?!no\s*(?:;|$)).+/i;
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

const smtp = async ({ env }, emails) => {
    const mailer = await WorkerMailer.connect({
        host: smtpHost,
        port: 465,
        secure: true,
        authType: 'plain',
        logLevel: LogLevel.NONE,
        credentials: { username: mailbox, password: env.POLPO_ACCESS_TOKEN },
    }).catch(() => {
        throw new HTTPException(502);
    });
    try {
        await Promise.all(
            emails.map(({ name, ...mail }) =>
                mailer.send({ from: { name, email: mailbox }, ...mail }),
            ),
        );
    } catch {
        throw new HTTPException(502);
    } finally {
        await mailer.close();
    }
};

const redirectUri = (c) => new URL('/admin/callback', c.req.url).href;

const isForm = (from = '') => formSender.test(from);

const isName = (name) => senderName.test(name);

const submitter = (subject) => {
    const group = subject.slice(subject.lastIndexOf('(') + 1).split(')')[0];
    const addresses = addressParser(group).filter(({ address }) => address);
    return addresses.length == 1 ? addresses[0].address : undefined;
};

const sender = (subject = '') => {
    const address = submitter(subject);
    const [, ...name] = subject
        .slice(0, subject.lastIndexOf('('))
        .split(' from ');
    return { address, name: address && name.join(' from ').trim() };
};

const parse = async (c, id) => {
    const response = await polpo(c, `messages/${id}/raw`);
    return PostalMime.parse(await response.arrayBuffer());
};

const headline = (subject = '', title) =>
    subject.slice(Math.max(0, subject.indexOf(title)));

const read = async (c, id) => {
    const email = await parse(c, id);
    const from = decodeWords(
        email.headers.find(({ key }) => key == 'from')?.value || '',
    );
    if (!isForm(from)) {
        throw new HTTPException(404);
    }
    const title = from.match(formSender)[1];
    return {
        id,
        text: email.text,
        html: email.html,
        messageId: email.messageId,
        date: email.date,
        title,
        reply: {
            to: email.replyTo?.[0]?.address || submitter(email.subject || ''),
            subject: headline(email.subject, title),
        },
    };
};

const mark = (c, flags, id) =>
    polpo(c, `messages/${id}`, {
        method: 'PATCH',
        body: JSON.stringify(flags),
    }).catch(() => {});

const fromMailbox = (from) => addressParser(from)[0]?.address == mailbox;

const listing = async (c, params, since = 0, offset = 0) => {
    const query = new URLSearchParams({ ...params, limit: 50, offset });
    const response = await polpo(c, `messages?${query}`);
    const { messages, total } = await response.json();
    const next = offset + messages.length;
    const reached = messages.some(({ date }) => Date.parse(date) < since);
    return messages.length && next < total && !reached
        ? [...messages, ...(await listing(c, params, since, next))]
        : messages;
};

const boxes = async (c) => {
    const response = await polpo(c, 'mailboxes');
    const { mailboxes } = await response.json();
    return mailboxes
        .filter(({ role }) => !/^(?:drafts|trash)$/.test(role))
        .map(({ name }) => name);
};

const related = (candidates, { messageId, reply, date }) => {
    const loose = candidates.filter(
        ({ email, refs }) =>
            !refs &&
            email.subject?.includes(reply.subject) &&
            Date.parse(email.date) >= Date.parse(date),
    );
    const grow = (known, joined) => {
        const found = candidates.filter(
            (candidate) =>
                !joined.includes(candidate) &&
                [...known].some((id) => candidate.refs.includes(id)),
        );
        return found.length
            ? grow(
                  new Set([
                      ...known,
                      ...found.map(({ email }) => email.messageId),
                  ]),
                  [...joined, ...found],
              )
            : joined;
    };
    return grow(
        new Set([messageId, ...loose.map(({ email }) => email.messageId)]),
        loose,
    );
};

const conversation = async (c, message) => {
    const { messageId, reply } = message;
    const lists = await Promise.all(
        (await boxes(c)).map((box) =>
            listing(c, { mailbox: box, q: reply.to }),
        ),
    );
    const candidates = await Promise.all(
        lists
            .flat()
            .filter(({ from }) => !isForm(from))
            .map(async ({ id, mailbox: box, seen }) => {
                const email = await parse(c, id);
                const refs =
                    `${email.inReplyTo ?? ''} ${email.references ?? ''}`.trim();
                return { id, box, seen, email, refs };
            }),
    );
    const thread = related(
        candidates.filter(({ email }) => email.messageId != messageId),
        message,
    ).sort((a, b) => Date.parse(a.email.date) - Date.parse(b.email.date));
    await Promise.all(
        thread
            .filter(
                ({ box, email }) =>
                    box == 'INBOX' && email.from.address == mailbox,
            )
            .map(({ id }) => mark(c, { seen: true, mailbox: 'Sent' }, id)),
    );
    const unique = [
        ...new Map(thread.map((item) => [item.email.messageId, item])).values(),
    ];
    const parser = new EmailReplyParser();
    return unique.map(({ id, seen, email }) => {
        const mine = email.from.address == mailbox;
        return {
            id,
            messageId: email.messageId,
            mine,
            auto: automatic.test(
                email.headers.find(({ key }) => key == 'auto-submitted')
                    ?.value ?? 'no',
            ),
            unread: !mine && !seen,
            name: email.from.name || email.from.address,
            to: email.to?.[0]?.address,
            date: email.date,
            text: parser.parseReply(email.text ?? '').trim(),
        };
    });
};

const activity = async (c, forms) => {
    const since = Math.min(...forms.map(({ date }) => Date.parse(date)));
    const lists = forms.length
        ? await Promise.all(
              (await boxes(c)).map((box) =>
                  listing(c, { mailbox: box }, since),
              ),
          )
        : [];
    const others = lists.flat().filter(({ from }) => !isForm(from));
    return ({ date, from, subject, seen }) => {
        const { address } = sender(subject);
        const original = headline(subject, from.match(formSender)[1]);
        const thread = others.filter(
            (other) =>
                address &&
                `${other.from} ${other.to}`
                    .toLowerCase()
                    .includes(address.toLowerCase()) &&
                Date.parse(other.date) >= Date.parse(date) &&
                other.subject.includes(original),
        );
        const latest = Math.max(
            Date.parse(date),
            ...thread.map((other) => Date.parse(other.date)),
        );
        return {
            updated: new Date(latest).toISOString(),
            seen:
                seen &&
                thread.every((other) => other.seen || fromMailbox(other.from)),
        };
    };
};

const deliver = async (c, message, thread, { name, text, headers }) => {
    const { messageId, title, reply } = message;
    const ids = thread.map((item) => item.messageId);
    const copy = {
        name: title,
        to: reply.to,
        subject: reply.subject,
        text: message.text,
        html: message.html,
        headers: {
            'Message-ID': messageId,
            Date: new Date(message.date).toUTCString(),
        },
    };
    const answer = {
        name,
        to: reply.to,
        bcc: mailbox,
        subject: reply.subject,
        text,
        headers: {
            ...headers,
            'In-Reply-To': ids.at(-1) || messageId,
            References: [messageId, ...ids].join(' '),
        },
    };
    await smtp(c, thread.length ? [answer] : [copy, answer]);
    await mark(c, { answered: true }, message.id);
};

const signed = async ({ req, env }, body) => {
    const [time, digest] =
        req.header('X-Polpo-Signature')?.match(signature)?.slice(1) ?? [];
    if (!time || Math.abs(Date.now() / 1000 - time) > tolerance) {
        return false;
    }
    const key = await crypto.subtle.importKey(
        'raw',
        new TextEncoder().encode(env.POLPO_WEBHOOK_TOKEN),
        { name: 'HMAC', hash: 'SHA-256' },
        false,
        ['verify'],
    );
    return crypto.subtle.verify(
        'HMAC',
        key,
        Uint8Array.fromHex(digest),
        new TextEncoder().encode(`${time}.${body}`),
    );
};

const identify = async (c) => {
    const [state, nonce, verifier] = (
        getCookie(c, 'jder-login', cookie.prefix) || ''
    ).split('.');
    const { code, state: returned, iss } = c.req.query();
    if (!state || returned != state || iss != issuer || !code) {
        throw new Error('invalid callback');
    }
    const { POLPO_CLIENT_ID, POLPO_CLIENT_SECRET } = c.env;
    const response = await fetch(`${issuer}/oauth/token`, {
        method: 'POST',
        headers: {
            Authorization: `Basic ${btoa(`${POLPO_CLIENT_ID}:${POLPO_CLIENT_SECRET}`)}`,
        },
        body: new URLSearchParams({
            grant_type: 'authorization_code',
            code,
            redirect_uri: redirectUri(c),
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
        redirect_uri: redirectUri(c),
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
    const forms = messages.filter(({ from }) => isForm(from));
    const update = await activity(c, forms);
    return c.json({
        messages: forms.map((message) => ({
            ...message,
            ...sender(message.subject),
            ...update(message),
        })),
        next: messages.length && next < total ? next : null,
    });
});

admin.get('/api/messages/:id{[0-9]+}', async (c) => {
    const message = await read(c, c.req.param('id'));
    const thread = message.reply.to ? await conversation(c, message) : [];
    await Promise.all([
        mark(c, { seen: true }, message.id),
        ...thread
            .filter(({ unread }) => unread)
            .map(({ id }) => mark(c, { seen: true }, id)),
    ]);
    return c.json({ ...message, conversation: thread });
});

admin.post('/api/messages/:id{[0-9]+}/reply', async (c) => {
    const { text, name } = await c.req.json().catch(() => ({}));
    const message = await read(c, c.req.param('id'));
    const sender = typeof name == 'string' ? name.trim() : '';
    if (
        typeof text != 'string' ||
        !text.trim() ||
        !replyAddress.test(message.reply.to) ||
        !isName(sender)
    ) {
        throw new HTTPException(400);
    }
    await deliver(c, message, await conversation(c, message), {
        name: sender,
        text,
    });
    return c.json({
        mine: true,
        auto: false,
        unread: false,
        name: sender,
        to: message.reply.to,
        text,
        date: new Date().toISOString(),
    });
});

admin.post('/webhook', async (c) => {
    const body = await c.req.text();
    if (!(await signed(c, body))) {
        throw new HTTPException(401);
    }
    const { event, message } = JSON.parse(body);
    if (event != 'message.received') {
        return c.body(null, 204);
    }
    const form = await read(c, message.id).catch((error) => {
        if (error.status != 404) {
            throw error;
        }
    });
    if (form && replyAddress.test(form.reply.to)) {
        const thread = await conversation(c, form);
        if (!thread.length) {
            await deliver(c, form, thread, receipt);
        }
    }
    return c.body(null, 204);
});

export default admin;
