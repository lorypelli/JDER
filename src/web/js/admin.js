const { render } = Vue;

Object.assign(styles, {
    panel: { ...column('1rem'), padding: '1.5rem' },
    start: { alignSelf: 'flex-start' },
    center: { alignSelf: 'center' },
    unread: { fontWeight: 700 },
    messageHeader: { display: 'flex', alignItems: 'center', gap: '0.5rem' },
    messageTitle: typescale('title-large'),
    messageText: { whiteSpace: 'pre-wrap', overflowWrap: 'anywhere' },
    failure: { position: 'relative', minHeight: '18rem' },
    sentReply: {
        ...column('0.5rem'),
        padding: '1rem',
        border: rule,
        borderRadius: '0.75rem',
    },
    email: {
        display: 'block',
        width: '100%',
        minHeight: '12rem',
        border: 0,
        borderRadius: '0.75rem',
        background: '#fff',
        colorScheme: 'light',
    },
});

Object.assign(icons, {
    reply: ['M10 9V5l-7 7 7 7v-4.1c5 0 8.5 1.6 11 5.1-1-5-4-10-11-11z'],
});

const nameKey = 'jder-reply-name';

const inbox = reactive({
    view: 'loading',
    signedIn: false,
    denied: false,
    messages: [],
    next: 0,
    loading: false,
    message: null,
});

const composer = reactive({
    name: store(nameKey) || '',
    draft: '',
    sending: false,
    result: null,
    round: 0,
});

const logoutButton = () =>
    h('mdui-button', { variant: 'text', onClick: logout }, 'Esci');

const api = async (path, options) => {
    const response = await fetch(`/admin/${path}`, options);
    if (!response.ok) {
        throw response;
    }
    return response;
};

const fail = ({ status }) => {
    if (status == 401 || status == 403) {
        Object.assign(inbox, {
            view: 'signedOut',
            signedIn: false,
            denied: status == 403,
        });
    } else {
        inbox.view = 'failed';
    }
};

const loadMessages = async () => {
    inbox.loading = true;
    try {
        const response = await api(`api/messages?offset=${inbox.next}`);
        const { messages, next } = await response.json();
        inbox.messages.push(...messages);
        Object.assign(inbox, { view: 'list', signedIn: true, next });
    } catch (reason) {
        fail(reason);
    } finally {
        inbox.loading = false;
    }
};

const reloadMessages = () => {
    Object.assign(inbox, { view: 'loading', messages: [], next: 0 });
    loadMessages();
};

const openMessage = async (item) => {
    inbox.view = 'loading';
    try {
        const response = await api(`api/messages/${item.id}`);
        const message = await response.json();
        item.seen = true;
        Object.assign(inbox, {
            view: 'message',
            message: { ...message, item, sent: [] },
        });
        Object.assign(composer, { draft: '', result: null });
    } catch (reason) {
        fail(reason);
    }
};

const closeMessage = () => {
    inbox.view = 'list';
};

const retryReply = () => {
    composer.result = null;
};

const sendReply = async () => {
    const { item, sent } = inbox.message;
    composer.sending = true;
    try {
        const response = await api(`api/messages/${item.id}/reply`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ text: composer.draft, name: composer.name }),
        });
        sent.push(await response.json());
        item.answered = true;
        Object.assign(composer, {
            name: composer.name.trim(),
            draft: '',
            result: 'sent',
        });
        store(nameKey, composer.name);
        composer.round++;
    } catch (reason) {
        composer.result = 'failed';
        if (reason.status == 401) {
            fail(reason);
        }
    } finally {
        composer.sending = false;
    }
};

const logout = async () => {
    try {
        await api('logout', { method: 'POST' });
        Object.assign(inbox, { view: 'signedOut', signedIn: false });
    } catch (reason) {
        fail(reason);
    }
};

const submitReply = submitter(composer, sendReply);

const formatDate = (value) =>
    new Date(value).toLocaleString('it-IT', {
        dateStyle: 'short',
        timeStyle: 'short',
    });

const replyOutcomes = {
    sent: {
        icon: icons.check,
        color: primary,
        title: 'Risposta inviata',
        text: 'La risposta è partita da support@jder.it con il nome scelto.',
        action: 'Nuova risposta',
    },
    failed: {
        icon: icons.error,
        color: error,
        title: 'Invio non riuscito',
        text: 'Qualcosa è andato storto, ma la tua risposta è ancora qui: riprova tra poco.',
        action: 'Riprova',
    },
};

const notice = (children) =>
    h('mdui-card', { variant: 'outlined', style: styles.panel }, children);

const signIn = () =>
    notice([
        h(
            'p',
            { style: styles.formText },
            'Accedi con un account Polpo Mail di jder.it per leggere i messaggi arrivati a support@jder.it e rispondere.',
        ),
        inbox.denied &&
            h(
                'p',
                { style: error },
                'Accesso non riuscito: riprova con un account di jder.it.',
            ),
        h(
            'mdui-button',
            { variant: 'filled', href: '/admin/login', style: styles.start },
            'Accedi con Polpo Mail',
        ),
    ]);

const failure = () =>
    h('mdui-card', { variant: 'outlined', style: styles.failure }, [
        outcome({
            icon: icons.error,
            color: error,
            title: 'Caricamento non riuscito',
            text: 'Qualcosa è andato storto e non è stato possibile caricare i messaggi: riprova tra poco.',
            action: 'Riprova',
            onClick: reloadMessages,
        }),
    ]);

const messageItem = (item) =>
    h(
        'mdui-list-item',
        {
            headlineLine: 1,
            descriptionLine: 1,
            onClick: () => openMessage(item),
        },
        [
            h(
                'span',
                { style: !item.seen && styles.unread },
                item.subject || 'Senza oggetto',
            ),
            h('span', { slot: 'description' }, formatDate(item.date)),
            item.answered &&
                h('span', { slot: 'end-icon' }, [
                    icon(icons.reply),
                    hidden('Risposto'),
                ]),
        ],
    );

const messageList = () =>
    h('div', { style: styles.section }, [
        inbox.messages.length
            ? h('mdui-card', { variant: 'outlined' }, [
                  h('mdui-list', inbox.messages.map(messageItem)),
              ])
            : h('p', { style: styles.formText }, 'Nessun messaggio.'),
        inbox.next !== null &&
            h(
                'mdui-button',
                {
                    variant: 'outlined',
                    loading: inbox.loading,
                    style: styles.start,
                    onClick: loadMessages,
                },
                'Carica altri',
            ),
    ]);

const replyForm = () => {
    const { to, subject } = inbox.message.reply;
    return h('mdui-card', { variant: 'outlined', style: styles.form }, [
        h(
            'form',
            {
                key: composer.round,
                style: [styles.formFields, composer.result && styles.invisible],
                inert: !!composer.result,
                onSubmit: submitReply,
            },
            [
                h('p', { style: styles.formText }, `A ${to} · ${subject}`),
                field(composer, 'name', {
                    label: 'Nome con cui rispondere',
                    autocomplete: 'name',
                    maxlength: 50,
                }),
                field(composer, 'draft', { label: 'Risposta', rows: 5 }),
                h(
                    'p',
                    { style: styles.formText },
                    'La risposta partirà da support@jder.it con il nome scelto.',
                ),
                h(
                    'mdui-button',
                    {
                        type: 'submit',
                        variant: 'filled',
                        loading: composer.sending,
                        fullWidth: true,
                    },
                    'Invia',
                ),
            ],
        ),
        composer.result &&
            outcome({
                ...replyOutcomes[composer.result],
                onClick: retryReply,
            }),
    ]);
};

const sentReply = ({ name, to, date, text }) =>
    h('div', { style: styles.sentReply }, [
        h(
            'p',
            { style: styles.cardText },
            `Risposta di ${name} a ${to} · ${formatDate(date)}`,
        ),
        h('p', { style: styles.messageText }, text),
    ]);

const showEmail = (frame, html) => {
    const { body, documentElement } = frame.contentDocument;
    render(h('div', { innerHTML: html }), body);
    body.querySelectorAll('*').forEach((element) => {
        element.style.fontFamily = '"Roboto", sans-serif';
    });
    new ResizeObserver(() => {
        frame.style.height = 0;
        frame.style.height = `${documentElement.scrollHeight}px`;
    }).observe(body);
};

const email = (html) =>
    h('iframe', {
        key: inbox.message.item.id,
        sandbox: 'allow-same-origin',
        src: '/email',
        style: styles.email,
        onLoad: ({ target }) => showEmail(target, html),
    });

const messageBody = ({ html, text }) => {
    if (html) {
        return email(html);
    }
    if (text) {
        return h('p', { style: styles.messageText }, text);
    }
    return h(
        'p',
        { style: styles.cardText },
        'Questo messaggio non ha contenuto da mostrare.',
    );
};

const messageView = () => {
    const { item, reply } = inbox.message;
    return [
        h('mdui-card', { variant: 'outlined', style: styles.panel }, [
            h('div', { style: styles.messageHeader }, [
                h('mdui-button-icon', { onClick: closeMessage }, [
                    icon(icons.back),
                    hidden('Torna ai messaggi'),
                ]),
                h(
                    'h3',
                    { style: styles.messageTitle },
                    item.subject || 'Senza oggetto',
                ),
            ]),
            h('p', { style: styles.cardText }, formatDate(item.date)),
            messageBody(inbox.message),
            ...inbox.message.sent.map(sentReply),
            !reply.to &&
                h(
                    'p',
                    { style: error },
                    'Non è stato trovato un indirizzo a cui rispondere.',
                ),
        ]),
        reply.to && replyForm(),
    ];
};

const views = {
    loading: () => h('mdui-circular-progress', { style: styles.center }),
    signedOut: signIn,
    failed: failure,
    list: messageList,
    message: messageView,
};

const admin = () =>
    page({
        start: [homeButton()],
        account: inbox.signedIn && logoutButton(),
        hero: {
            subtitle: 'Area riservata',
            title: 'Messaggi di Supporto',
            description:
                'Leggi i messaggi arrivati a support@jder.it dal modulo di contatto e rispondi direttamente da qui.',
        },
        content: [section('Posta in Arrivo', views[inbox.view]())],
    });

mount(admin);
loadMessages();
