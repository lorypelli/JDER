const { createApp, h, reactive } = Vue;

const typescale = (name) => ({
    fontSize: `var(--mdui-typescale-${name}-size)`,
    lineHeight: `var(--mdui-typescale-${name}-line-height)`,
});

const muted = { color: 'rgb(var(--mdui-color-on-surface-variant))' };
const primary = { color: 'rgb(var(--mdui-color-primary))' };
const error = { color: 'rgb(var(--mdui-color-error))' };
const rule = '0.0625rem solid rgb(var(--mdui-color-outline-variant))';

const column = (gap) => ({ display: 'flex', flexDirection: 'column', gap });

const wrap = (gap) => ({ display: 'flex', flexWrap: 'wrap', gap });

const card = (basis) => ({ flex: `1 1 ${basis}`, minWidth: 0 });

const styles = {
    none: { display: 'none' },
    brand: { color: 'inherit', textDecoration: 'none' },
    link: primary,
    glyph: { fill: 'currentColor' },
    icon: { fill: 'currentColor', width: '1.5rem', height: '1.5rem' },
    rays: { fill: 'none', stroke: 'currentColor', strokeWidth: 2 },
    hidden: {
        position: 'absolute',
        width: '0.0625rem',
        height: '0.0625rem',
        overflow: 'hidden',
        clipPath: 'inset(50%)',
        whiteSpace: 'nowrap',
    },
    appBar: { position: 'sticky' },
    swatch: {
        display: 'block',
        width: '1.5rem',
        height: '1.5rem',
        border: '0.0625rem solid rgb(var(--mdui-color-outline))',
        borderRadius: '50%',
    },
    menu: { maxHeight: '60vh', overflowY: 'auto' },
    main: {
        ...column('3rem'),
        flex: 1,
        alignSelf: 'center',
        width: 'min(68.75rem, 100% - 2 * var(--page-padding))',
        padding: '1.5rem 0 3rem',
    },
    section: column('1rem'),
    heading: typescale('headline-small'),
    hero: {
        ...column('1rem'),
        alignItems: 'center',
        textAlign: 'center',
    },
    heroIcon: { width: '4.5rem', height: '4.5rem' },
    heroTitle: typescale('display-medium'),
    heroSubtitle: { ...muted, ...typescale('title-large'), maxWidth: '45rem' },
    heroDescription: { ...muted, maxWidth: '47.5rem' },
    heroActions: { ...wrap('0.75rem'), justifyContent: 'center' },
    grid: wrap('1rem'),
    chips: wrap('0.5rem'),
    cardIcon: primary,
    cardTitle: typescale('title-medium'),
    cardText: { ...muted, ...typescale('body-medium') },
    featureCard: {
        ...card('18.75rem'),
        display: 'grid',
        gridTemplateColumns: '1.5rem 1fr',
        alignContent: 'start',
        gap: '0.25rem 1rem',
        padding: '1rem',
    },
    featureIcon: { ...primary, gridRow: 'span 2' },
    wide: { flex: '1 1 100%' },
    downloadCard: {
        ...card('18.75rem'),
        ...column('0.75rem'),
        alignItems: 'flex-start',
        padding: '1.5rem',
    },
    downloadText: { ...muted, ...typescale('body-medium'), flex: 1 },
    screenshotCard: { ...card('15rem'), display: 'block' },
    screenshot: {
        display: 'block',
        width: '100%',
        height: 'auto',
        aspectRatio: '16 / 9',
        objectFit: 'cover',
    },
    screenshotTitle: {
        ...typescale('title-medium'),
        display: 'block',
        padding: '0.75rem 1rem',
    },
    form: {
        ...column('1rem'),
        position: 'relative',
        maxWidth: '40rem',
        padding: '1.5rem',
    },
    formText: muted,
    formFields: column('1rem'),
    invisible: { visibility: 'hidden' },
    outcome: {
        ...column('1rem'),
        position: 'absolute',
        inset: 0,
        alignItems: 'center',
        justifyContent: 'center',
        padding: '1.5rem',
        textAlign: 'center',
    },
    outcomeIcon: { width: '4rem', height: '4rem' },
    outcomeTitle: typescale('headline-small'),
    footer: {
        ...muted,
        padding: '1.5rem var(--page-padding)',
        borderTop: rule,
        fontSize: 'var(--mdui-typescale-body-medium-size)',
        textAlign: 'center',
    },
};

const icons = {
    download: ['M5 20h14v-2H5v2zM19 9h-4V3H9v6H5l7 7 7-7z'],
    error: [
        'M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm1 15h-2v-2h2v2zm0-4h-2V7h2v6z',
    ],
    check: [
        'M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm-2 15-5-5 1.41-1.41L10 14.17l7.59-7.59L19 8l-9 9z',
    ],
    book: [
        'M21 5c-1.11-.35-2.33-.5-3.5-.5-1.95 0-4.05.4-5.5 1.5-1.45-1.1-3.55-1.5-5.5-1.5S2.45 4.9 1 6v14.65c0 .25.25.5.5.5.1 0 .15-.05.25-.05C3.1 20.45 5.05 20 6.5 20c1.95 0 4.05.4 5.5 1.5 1.35-.85 3.8-1.5 5.5-1.5 1.65 0 3.35.3 4.75 1.05.1.05.15.05.25.05.25 0 .5-.25.5-.5V6c-.6-.45-1.25-.75-2-1zm0 13.5c-1.1-.35-2.3-.5-3.5-.5-1.7 0-4.15.65-5.5 1.5V8c1.35-.85 3.8-1.5 5.5-1.5 1.2 0 2.4.15 3.5.5v11.5z',
        'M17.5 10.5c.88 0 1.73.09 2.5.26V9.24c-.79-.15-1.64-.24-2.5-.24-1.7 0-3.24.29-4.5.83v1.66c1.13-.64 2.7-.99 4.5-.99zM13 12.49v1.66c1.13-.64 2.7-.99 4.5-.99.88 0 1.73.09 2.5.26V11.9c-.79-.15-1.64-.24-2.5-.24-1.7 0-3.24.3-4.5.83zm4.5 1.84c-1.7 0-3.24.29-4.5.83v1.66c1.13-.64 2.7-.99 4.5-.99.88 0 1.73.09 2.5.26v-1.52c-.79-.16-1.64-.24-2.5-.24z',
    ],
    back: ['M20 11H7.83l5.59-5.59L12 4l-8 8 8 8 1.41-1.41L7.83 13H20v-2z'],
    browser: [
        'M19 4H5c-1.11 0-2 .9-2 2v12c0 1.1.89 2 2 2h4v-2H5V8h14v10h-4v2h4c1.1 0 2-.9 2-2V6c0-1.1-.89-2-2-2zm-7 6-4 4h3v6h2v-6h3l-4-4z',
    ],
    code: [
        'M9.4 16.6 4.8 12l4.6-4.6L8 6l-6 6 6 6 1.4-1.4zm5.2 0 4.6-4.6-4.6-4.6L16 6l6 6-6 6-1.4-1.4z',
    ],
};

const repository = 'https://github.com/LoryPelli/JDER';

const contactUrl =
    'https://polpomail.com/f/fca7ad6c4a2f04c13c3876ab8f4acc573be0';

const state = reactive({ ...saved });

const contact = reactive({
    name: '',
    email: '',
    message: '',
    gotcha: '',
    sending: false,
    result: null,
    round: 0,
});

const svg = (paths, props = {}) =>
    h(
        'svg',
        {
            viewBox: '0 0 24 24',
            ...props,
            style: [styles.glyph, props.style],
        },
        paths.map((d) => h('path', { d })),
    );

const icon = (paths) => svg(paths, { style: styles.icon });

const hidden = (text) => h('span', { style: styles.hidden }, text);

const link = (href, text) =>
    h('a', { href, target: '_blank', style: styles.link }, text);

const section = (title, content) =>
    h('section', { style: styles.section }, [
        h('h2', { style: styles.heading }, title),
        content,
    ]);

const toggleTheme = () => {
    state.isDark = !state.isDark;
    saveTheme(state.isDark);
    applyTheme(state.isDark);
};

const choose = (id) => {
    state.selected = palettes.find((palette) => palette.id == id);
    savePalette(id);
    applyPalette(state.selected);
};

const paletteMenu = () =>
    h('mdui-dropdown', { placement: 'bottom-end' }, [
        h('mdui-button-icon', { slot: 'trigger' }, [
            h('span', {
                style: [
                    styles.swatch,
                    { backgroundColor: 'var(--palette-swatch)' },
                ],
            }),
            hidden('Palette Colori'),
        ]),
        h(
            'mdui-menu',
            {
                selects: 'single',
                value: state.selected.id,
                style: styles.menu,
                onChange: (event) => choose(event.target.value),
            },
            palettes.map((palette) =>
                h('mdui-menu-item', { value: palette.id }, [
                    h('span', {
                        slot: 'icon',
                        style: [
                            styles.swatch,
                            { backgroundColor: `#${palette.light[0]}` },
                        ],
                    }),
                    palette.name,
                ]),
            ),
        ),
    ]);

const themeButton = () =>
    h('mdui-button-icon', { onClick: toggleTheme }, [
        h('svg', { viewBox: '0 0 24 24', style: styles.icon }, [
            h('g', { style: state.isDark && styles.none }, [
                h('circle', { cx: 12, cy: 12, r: 4.2 }),
                h('path', {
                    style: styles.rays,
                    d: 'M18 12h4.2M16.24 16.24l2.97 2.97M12 18v4.2M7.76 16.24l-2.97 2.97M6 12H1.8M7.76 7.76 4.79 4.79M12 6V1.8M16.24 7.76l2.97-2.97',
                }),
            ]),
            h('path', {
                style: !state.isDark && styles.none,
                d: 'M12 3c-4.97 0-9 4.03-9 9s4.03 9 9 9 9-4.03 9-9c0-.46-.04-.92-.1-1.36-.98 1.37-2.58 2.26-4.4 2.26-2.98 0-5.4-2.42-5.4-5.4 0-1.81.89-3.42 2.26-4.4-.44-.06-.9-.1-1.36-.1z',
            }),
        ]),
        hidden(state.isDark ? 'Tema Chiaro' : 'Tema Scuro'),
    ]);

const appBar = ({ start = [], links = [] }) =>
    h(
        'mdui-top-app-bar',
        { 'scroll-behavior': 'elevate', style: styles.appBar },
        [
            ...start,
            h('mdui-top-app-bar-title', [
                h('a', { href: '/', style: styles.brand }, 'JDER'),
            ]),
            ...links,
            paletteMenu(),
            themeButton(),
        ],
    );

const hero = ({ subtitle, title, description, actions = [] }) =>
    h('section', { style: styles.hero }, [
        h('img', {
            style: styles.heroIcon,
            src: 'jder_icon.png',
            alt: 'Logo di JDER',
            width: 72,
            height: 72,
            loading: 'lazy',
            decoding: 'async',
        }),
        h('h1', { style: styles.heroTitle }, 'JDER'),
        h('p', { style: styles.heroSubtitle }, subtitle),
        h('h2', { style: styles.heading }, title),
        h('p', { style: styles.heroDescription }, description),
        ...actions,
    ]);

const footer = () =>
    h('footer', { style: styles.footer }, [
        h('p', [
            '© 2025-2026 LoryPelli - ',
            link(repository, 'GitHub Repository'),
        ]),
    ]);

const page = ({ start, links, hero: heroOptions, content }) => [
    appBar({ start, links }),
    h('main', { style: styles.main }, [hero(heroOptions), ...content]),
    footer(),
];

const send = async () => {
    const body = new FormData();
    body.append('name', contact.name);
    body.append('email', contact.email);
    body.append('message', contact.message);
    body.append('_gotcha', contact.gotcha);
    contact.sending = true;
    try {
        const response = await fetch(contactUrl, {
            method: 'POST',
            headers: { Accept: 'application/json' },
            body,
        });
        if (!response.ok) {
            throw new Error(response.statusText);
        }
        contact.result = 'sent';
    } catch {
        contact.result = 'failed';
    } finally {
        contact.sending = false;
    }
};

const restart = () => {
    Object.assign(contact, { name: '', email: '', message: '', result: null });
    contact.round++;
};

const retry = () => {
    contact.result = null;
};

const submit = (event) => {
    event.preventDefault();
    const fields = [...event.target.querySelectorAll('mdui-text-field')];
    const valid = fields.map((field) => field.reportValidity());
    if (!contact.sending && valid.every(Boolean)) {
        send();
    }
};

const field = (key, props) =>
    h('mdui-text-field', {
        variant: 'outlined',
        required: true,
        name: key,
        value: contact[key],
        disabled: contact.sending,
        onInput: (event) => {
            contact[key] = event.target.value;
        },
        ...props,
    });

const outcomes = {
    sent: {
        icon: icons.check,
        color: primary,
        title: 'Messaggio inviato',
        text: 'Grazie per avermi scritto!',
        action: 'Nuovo messaggio',
        onClick: restart,
    },
    failed: {
        icon: icons.error,
        color: error,
        title: 'Invio non riuscito',
        text: 'Qualcosa è andato storto, ma il tuo messaggio è ancora qui: riprova tra poco.',
        action: 'Riprova',
        onClick: retry,
    },
};

const outcome = ({ icon: paths, color, title, text, action, onClick }) =>
    h('div', { style: styles.outcome }, [
        svg(paths, { style: [color, styles.outcomeIcon] }),
        h('h3', { style: styles.outcomeTitle }, title),
        h('p', { style: styles.formText }, text),
        h('mdui-button', { variant: 'outlined', onClick }, action),
    ]);

const contactForm = () =>
    h('mdui-card', { variant: 'outlined', style: styles.form }, [
        h(
            'form',
            {
                key: contact.round,
                style: [styles.formFields, contact.result && styles.invisible],
                inert: !!contact.result,
                onSubmit: submit,
            },
            [
                h(
                    'p',
                    { style: styles.formText },
                    'Hai una domanda, un suggerimento o hai trovato un errore? Scrivimi.',
                ),
                field('name', { label: 'Nome', autocomplete: 'name' }),
                field('email', {
                    label: 'Email',
                    type: 'email',
                    autocomplete: 'email',
                }),
                field('message', { label: 'Messaggio', rows: 5 }),
                h('input', {
                    type: 'text',
                    name: '_gotcha',
                    value: contact.gotcha,
                    tabindex: -1,
                    autocomplete: 'off',
                    style: styles.none,
                    onInput: (event) => {
                        contact.gotcha = event.target.value;
                    },
                }),
                h(
                    'mdui-button',
                    {
                        type: 'submit',
                        variant: 'filled',
                        loading: contact.sending,
                        fullWidth: true,
                    },
                    'Invia',
                ),
            ],
        ),
        contact.result && outcome(outcomes[contact.result]),
    ]);

const mount = (view) => createApp({ render: view }).mount('#app');

const features = [
    {
        title: 'Diagramma E/R',
        text: 'Crea entità, relazioni con cardinalità completa, attributi (chiave, multivalore, derivati, composti) ed entità deboli',
        icon: 'M14 9v2h-3V9H8.5V7H11V1H4v6h2.5v2H4v6h2.5v2H4v6h7v-6H8.5v-2H11v-2h3v2h7V9h-7z',
    },
    {
        title: 'Schema Logico',
        text: "Il passo dopo l'E/R: tabelle e colonne con chiavi primarie ed esterne, collegate da frecce che evitano le altre tabelle",
        icon: 'M21 10h-8.35A5.99 5.99 0 0 0 7 6c-3.31 0-6 2.69-6 6s2.69 6 6 6a5.99 5.99 0 0 0 5.65-4H13l2 2 2-2 2 2 4-4.04L21 10zM7 15c-1.65 0-3-1.35-3-3s1.35-3 3-3 3 1.35 3 3-1.35 3-3 3z',
    },
    {
        title: 'Diagramma delle Classi',
        text: 'Classi, interfacce, enumerazioni e classi astratte con attributi, metodi, visibilità e 6 tipi di relazione UML',
        icon: 'M4 7v2c0 .55-.45 1-1 1H2v4h1c.55 0 1 .45 1 1v2c0 1.65 1.35 3 3 3h3v-2H7c-.55 0-1-.45-1-1v-2c0-1.3-.84-2.42-2-2.83v-.34C5.16 11.42 6 10.3 6 9V7c0-.55.45-1 1-1h3V4H7C5.35 4 4 5.35 4 7zm17 3c-.55 0-1-.45-1-1V7c0-1.65-1.35-3-3-3h-3v2h3c.55 0 1 .45 1 1v2c0 1.3.84 2.42 2 2.83v.34c-1.16.41-2 1.52-2 2.83v2c0 .55-.45 1-1 1h-3v2h3c1.65 0 3-1.35 3-3v-2c0-.55.45-1 1-1h1v-4h-1z',
    },
    {
        title: 'Diagramma Use Case',
        text: "Attori, casi d'uso, confini di sistema e relazioni include/extend/generalizzazione con frecce UML corrette",
        icon: 'M12 12c2.21 0 4-1.79 4-4s-1.79-4-4-4-4 1.79-4 4 1.79 4 4 4zm0 2c-2.67 0-8 1.34-8 4v2h16v-2c0-2.66-5.33-4-8-4z',
    },
    {
        title: 'Diagramma di Sequenza',
        text: 'Linee di vita, messaggi sincroni, asincroni, di risposta, creazione e distruzione, barre di attivazione automatiche e frammenti combinati',
        icon: 'm18 12 4-4-4-4v3H3v2h15zM6 12l-4 4 4 4v-3h15v-2H6z',
    },
    {
        title: 'Diagramma Logico',
        text: 'Porte logiche AND, OR, NOT, NAND, NOR, XOR e XNOR con simulazione in tempo reale, tabella di verità ed espressioni booleane',
        icon: 'M15 9H9v6h6V9zm-2 4h-2v-2h2v2zm8-2V9h-2V7c0-1.1-.9-2-2-2h-2V3h-2v2h-2V3H9v2H7c-1.1 0-2 .9-2 2v2H3v2h2v2H3v2h2v2c0 1.1.9 2 2 2h2v2h2v-2h2v2h2v-2h2c1.1 0 2-.9 2-2v-2h2v-2h-2v-2h2zm-4 6H7V7h10v10z',
    },
    {
        title: 'Macchina di Mealy',
        text: 'Stati e transizioni con ingresso/uscita, stato iniziale e autoanelli, con tabella di transizione e simulazione di una sequenza di ingressi',
        icon: 'M12 4V1L8 5l4 4V6c3.31 0 6 2.69 6 6 0 1.01-.25 1.97-.7 2.8l1.46 1.46A7.93 7.93 0 0 0 20 12c0-4.42-3.58-8-8-8zm0 14c-3.31 0-6-2.69-6-6 0-1.01.25-1.97.7-2.8L5.24 7.74A7.93 7.93 0 0 0 4 12c0 4.42 3.58 8 8 8v3l4-4-4-4v3z',
    },
    {
        title: 'Macchina di Moore',
        text: 'Stati con nome e uscita, controlli su determinismo e transizioni mancanti e simulazione passo per passo della sequenza di uscita',
        icon: 'M20 10h-3V8.86c1.72-.45 3-2 3-3.86h-3V4c0-.55-.45-1-1-1H8c-.55 0-1 .45-1 1v1H4c0 1.86 1.28 3.41 3 3.86V10H4c0 1.86 1.28 3.41 3 3.86V15H4c0 1.86 1.28 3.41 3 3.86V20c0 .55.45 1 1 1h8c.55 0 1-.45 1-1v-1.14c1.72-.45 3-2 3-3.86h-3v-1.14c1.72-.45 3-2 3-3.86zm-8 9a2 2 0 1 1-.001-3.999A2 2 0 0 1 12 19zm0-5a2 2 0 1 1-.001-3.999A2 2 0 0 1 12 14zm0-5a2 2 0 0 1-2-2c0-1.11.89-2 2-2a2 2 0 1 1 0 4z',
    },
    {
        title: 'Interfaccia Material 3',
        text: 'Design pulito con tema chiaro/scuro, 20 palette di colori ufficiali selezionabili in tempo reale e interfaccia in italiano o in inglese',
        icon: 'M12 2C6.49 2 2 6.49 2 12s4.49 10 10 10a2.5 2.5 0 0 0 2.5-2.5c0-.61-.23-1.2-.64-1.67a.528.528 0 0 1-.13-.33c0-.28.22-.5.5-.5H16c3.31 0 6-2.69 6-6 0-4.96-4.49-9-10-9zm5.5 11c-.83 0-1.5-.67-1.5-1.5s.67-1.5 1.5-1.5 1.5.67 1.5 1.5-.67 1.5-1.5 1.5zm-3-4c-.83 0-1.5-.67-1.5-1.5S13.67 6 14.5 6s1.5.67 1.5 1.5S15.33 9 14.5 9zM5 11.5c0-.83.67-1.5 1.5-1.5s1.5.67 1.5 1.5S7.33 13 6.5 13 5 12.33 5 11.5zm6-4c0 .83-.67 1.5-1.5 1.5S8 8.33 8 7.5 8.67 6 9.5 6s1.5.67 1.5 1.5z',
    },
    {
        title: 'Salva ed Esporta',
        text: 'Salva i tuoi diagrammi in formato JSON o esportali come immagine PNG ad alta qualità',
        icon: 'M17 3H5a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14c1.1 0 2-.9 2-2V7l-4-4zm-5 16c-1.66 0-3-1.34-3-3s1.34-3 3-3 3 1.34 3 3-1.34 3-3 3zm3-10H5V5h10v4z',
    },
    {
        title: 'Scorciatoie e Controlli',
        text: 'Undo/Redo, zoom verso il cursore, pan trascinando la lavagna e scorciatoie da tastiera per un workflow efficiente',
        icon: 'M20 5H4c-1.1 0-1.99.9-1.99 2L2 17c0 1.1.9 2 2 2h16c1.1 0 2-.9 2-2V7c0-1.1-.9-2-2-2zm-9 3h2v2h-2V8zm0 3h2v2h-2v-2zM8 8h2v2H8V8zm0 3h2v2H8v-2zm-1 2H5v-2h2v2zm0-3H5V8h2v2zm9 7H8v-2h8v2zm0-4h-2v-2h2v2zm0-3h-2V8h2v2zm3 3h-2v-2h2v2zm0-3h-2V8h2v2z',
    },
    {
        title: 'Note e Annotazioni',
        text: 'Aggiungi note e commenti testuali liberamente sul canvas in tutti i tipi di diagramma',
        icon: 'M19 3H4.99C3.89 3 3 3.9 3 5l.01 14c0 1.1.89 2 1.99 2h10l6-6V5c0-1.1-.9-2-2-2zM7 8h10v2H7V8zm5 6H7v-2h5v2zm2 5.5V14h5.5L14 19.5z',
    },
    {
        title: 'Multipiattaforma',
        text: 'Scritto in Kotlin con Compose Multiplatform: gira su Windows, macOS e Linux con un solo JAR, oppure direttamente nel browser senza installare nulla',
        icon: 'M4 6h18V4H4c-1.1 0-2 .9-2 2v11H0v3h14v-3H4V6zm19 2h-6c-.55 0-1 .45-1 1v10c0 .55.45 1 1 1h6c.55 0 1-.45 1-1V9c0-.55-.45-1-1-1zm-1 9h-4v-7h4v7z',
        wide: true,
    },
];

const screenshots = [
    {
        key: 'er',
        title: 'Diagramma E/R',
        alt: "studenti, corsi e docenti di un'università",
    },
    {
        key: 'relational',
        title: 'Schema Logico',
        alt: "tabelle, chiavi primarie e chiavi esterne di un'università",
    },
    {
        key: 'class',
        title: 'Diagramma delle Classi',
        alt: 'e-commerce con ereditarietà, interfacce ed enumerazioni',
    },
    {
        key: 'usecase',
        title: 'Diagramma Use Case',
        alt: 'sistema di gestione di una biblioteca',
    },
    {
        key: 'sequence',
        title: 'Diagramma di Sequenza',
        alt: 'prenotazione di un libro in una biblioteca',
    },
    {
        key: 'logic',
        title: 'Diagramma Logico',
        alt: 'sommatore completo con porte XOR, AND e OR e valori simulati',
    },
    {
        key: 'mealy',
        title: 'Macchina di Mealy',
        alt: 'riconoscitore della sequenza 101 con le uscite sulle transizioni',
    },
    {
        key: 'moore',
        title: 'Macchina di Moore',
        alt: 'riconoscitore della sequenza 101 con le uscite negli stati',
    },
];

const technologies = [
    'Kotlin',
    'Compose Multiplatform',
    'Material 3',
    'Java',
    'WebAssembly',
];

const links = () => [
    h('nav', { class: 'app-bar-links' }, [
        h('mdui-button', { variant: 'text', href: '/download' }, 'Scarica'),
        h('mdui-button', { variant: 'text', href: '/app/' }, 'Versione Web'),
        h(
            'mdui-button',
            { variant: 'text', href: '/guida.pdf', target: '_blank' },
            'Guida',
        ),
    ]),
];

const actions = () => [
    h('div', { style: styles.heroActions }, [
        h('mdui-button', { variant: 'filled', href: '/download' }, [
            svg(icons.download, { slot: 'icon' }),
            'Scarica Risorse',
        ]),
        h('mdui-button', { variant: 'tonal', href: '/app/' }, [
            svg(icons.browser, { slot: 'icon' }),
            'Apri nel Browser',
        ]),
        h(
            'mdui-button',
            { variant: 'outlined', href: '/guida.pdf', target: '_blank' },
            [svg(icons.book, { slot: 'icon' }), 'Guida Utente'],
        ),
    ]),
];

const feature = ({ title, text, icon: path, wide }) =>
    h(
        'mdui-card',
        {
            variant: 'outlined',
            style: [styles.featureCard, wide && styles.wide],
        },
        [
            h('span', { style: styles.featureIcon }, [icon([path])]),
            h('h3', { style: styles.cardTitle }, title),
            h('p', { style: styles.cardText }, text),
        ],
    );

const screenshot = ({ key, title, alt }) => {
    const image = (mode, theme) =>
        h('img', {
            style: [
                styles.screenshot,
                state.isDark != (mode == 'dark') && styles.none,
            ],
            src: `/screenshots/${key}-${mode}.png`,
            alt: `${title} in JDER (tema ${theme}): ${alt}`,
            width: 1600,
            height: 900,
            loading: 'lazy',
            decoding: 'async',
        });
    return h(
        'mdui-card',
        {
            variant: 'outlined',
            style: styles.screenshotCard,
            href: `/screenshots/${key}-${state.isDark ? 'dark' : 'light'}.png`,
            target: '_blank',
        },
        [
            image('light', 'chiaro'),
            image('dark', 'scuro'),
            h('span', { style: styles.screenshotTitle }, title),
        ],
    );
};

const home = () =>
    page({
        links: links(),
        hero: {
            subtitle:
                'Editor di Diagrammi ER, Schemi Logici, Use Case, Class, Sequenza, Logici e Macchine a Stati',
            title: 'Il Tuo Editor di Diagrammi Moderno',
            description: [
                'Nuova versione del software di ',
                link(
                    'https://ballini.it/Software/ProgER',
                    'Alessandro Ballini',
                ),
                ', poi successivamente ripresa da ',
                link('https://gianvitopio.wordpress.com/jder', 'Gianvito Pio'),
                ', con una veste grafica più moderna e correzione di vari errori, riscritta completamente in Kotlin e Compose',
            ],
            actions: actions(),
        },
        content: [
            section(
                'Caratteristiche Principali',
                h('div', { style: styles.grid }, features.map(feature)),
            ),
            section(
                'Screenshot',
                h('div', { style: styles.grid }, screenshots.map(screenshot)),
            ),
            section(
                'Tecnologie',
                h(
                    'div',
                    { style: styles.chips },
                    technologies.map((name) => h('mdui-chip', name)),
                ),
            ),
            section('Contatti', contactForm()),
        ],
    });

const resource = ({ title, text, paths, button }) =>
    h('mdui-card', { variant: 'outlined', style: styles.downloadCard }, [
        h('span', { style: styles.cardIcon }, [icon(paths)]),
        h('h3', { style: styles.cardTitle }, title),
        h('p', { style: styles.downloadText }, text),
        button,
    ]);

const download = () =>
    page({
        start: [
            h('mdui-button-icon', { href: '/' }, [
                icon(icons.back),
                hidden('Torna alla home'),
            ]),
        ],
        hero: {
            subtitle: 'Scarica JDER, la guida e le risorse',
            title: 'Scarica Tutto Ciò che Ti Serve',
            description:
                "Qui trovi i link per scaricare l'applicazione, la guida utente e le risorse ufficiali di JDER.",
        },
        content: [
            section(
                'Risorse Disponibili',
                h('div', { style: styles.grid }, [
                    resource({
                        title: 'Applicazione JDER',
                        text: "Scarica l'eseguibile Java di JDER per usare l'editor su Windows, macOS e Linux.",
                        paths: icons.download,
                        button: h(
                            'mdui-button',
                            { variant: 'filled', href: '/download/JDER.jar' },
                            'Scarica JDER',
                        ),
                    }),
                    resource({
                        title: 'Versione Web',
                        text: 'Usa JDER direttamente nel browser, senza installare Java: i diagrammi si aprono e si salvano come file sul tuo computer.',
                        paths: icons.browser,
                        button: h(
                            'mdui-button',
                            { variant: 'outlined', href: '/app/' },
                            'Apri nel browser',
                        ),
                    }),
                    resource({
                        title: 'Guida Utente',
                        text: 'Scarica la guida completa in formato PDF per scoprire tutte le funzionalità di JDER.',
                        paths: icons.book,
                        button: h(
                            'mdui-button',
                            {
                                variant: 'outlined',
                                href: '/guida.pdf',
                                target: '_blank',
                            },
                            'Apri la guida',
                        ),
                    }),
                    resource({
                        title: 'Codice Sorgente',
                        text: 'Accedi al repository GitHub per scaricare il codice sorgente, contribuire o compilare JDER da te.',
                        paths: icons.code,
                        button: h(
                            'mdui-button',
                            {
                                variant: 'outlined',
                                href: repository,
                                target: '_blank',
                            },
                            'Vai al repository',
                        ),
                    }),
                ]),
            ),
        ],
    });

const pages = { '/': home, '/download': download };

mount(pages[location.pathname]);
