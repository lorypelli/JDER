const { createApp, h, reactive } = Vue;

const typescale = (name) => ({
    fontSize: `var(--mdui-typescale-${name}-size)`,
    lineHeight: `var(--mdui-typescale-${name}-line-height)`,
});

const muted = { color: 'rgb(var(--mdui-color-on-surface-variant))' };

const primary = { color: 'rgb(var(--mdui-color-primary))' };

const error = { color: 'rgb(var(--mdui-color-error))' };

const rule = '0.0625rem solid rgb(var(--mdui-color-outline-variant))';

const contentWidth = 'min(68.75rem, 100% - 2 * var(--page-padding))';

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
        width: contentWidth,
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
    form: {
        ...column('1rem'),
        position: 'relative',
        padding: '1.5rem',
    },
    grid: wrap('1rem'),
    cardTitle: typescale('title-medium'),
    cardText: { ...muted, ...typescale('body-medium') },
    formText: muted,
    formFields: column('1rem'),
    invisible: { visibility: 'hidden' },
    outcome: {
        ...column('1rem'),
        position: 'absolute',
        inset: '1.5rem',
        alignItems: 'center',
        justifyContent: 'center',
        textAlign: 'center',
    },
    outcomeIcon: { width: '4rem', height: '4rem' },
    outcomeTitle: typescale('headline-small'),
    footer: {
        ...muted,
        display: 'flex',
        justifyContent: 'center',
        alignItems: 'center',
        minHeight: '4.5rem',
        borderTop: rule,
        fontSize: 'var(--mdui-typescale-body-medium-size)',
        textAlign: 'center',
    },
    footerText: { width: contentWidth },
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
};

const repository = 'https://github.com/LoryPelli/JDER';

const state = reactive({ ...saved });

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

const adminButton = () =>
    h('mdui-button', { variant: 'text', href: '/admin' }, 'Admin');

const appBar = ({ start = [], links = [], account = adminButton() }) =>
    h(
        'mdui-top-app-bar',
        { 'scroll-behavior': 'elevate', style: styles.appBar },
        [
            ...start,
            h('mdui-top-app-bar-title', [
                h('a', { href: '/', style: styles.brand }, 'JDER'),
            ]),
            ...links,
            account,
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
        h('p', { style: styles.footerText }, [
            '© 2025-2026 LoryPelli - ',
            link(repository, 'GitHub Repository'),
        ]),
    ]);

const page = ({ start, links, account, hero: heroOptions, content }) => [
    appBar({ start, links, account }),
    h('main', { style: styles.main }, [hero(heroOptions), ...content]),
    footer(),
];

const submitter = (model, action) => (event) => {
    event.preventDefault();
    const fields = [...event.target.querySelectorAll('mdui-text-field')];
    const valid = fields.map((field) => field.reportValidity());
    if (!model.sending && valid.every(Boolean)) {
        action();
    }
};

const field = (model, key, props) =>
    h('mdui-text-field', {
        variant: 'outlined',
        required: true,
        name: key,
        value: model[key],
        disabled: model.sending,
        onInput: (event) => {
            model[key] = event.target.value;
        },
        ...props,
    });

const outcome = ({ icon: paths, color, title, text, action, onClick }) =>
    h('div', { style: styles.outcome }, [
        svg(paths, { style: [color, styles.outcomeIcon] }),
        h('h3', { style: styles.outcomeTitle }, title),
        h('p', { style: styles.formText }, text),
        h('mdui-button', { variant: 'outlined', onClick }, action),
    ]);

const mount = (view) => createApp({ render: view }).mount('#app');

const homeButton = () =>
    h('mdui-button-icon', { href: '/' }, [
        icon(icons.back),
        hidden('Torna alla home'),
    ]);
