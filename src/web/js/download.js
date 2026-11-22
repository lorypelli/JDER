Object.assign(styles, {
    cardIcon: primary,
    downloadCard: {
        ...card('18.75rem'),
        ...column('0.75rem'),
        alignItems: 'flex-start',
        padding: '1.5rem',
    },
    downloadText: { ...muted, ...typescale('body-medium'), flex: 1 },
});

Object.assign(icons, {
    code: [
        'M9.4 16.6 4.8 12l4.6-4.6L8 6l-6 6 6 6 1.4-1.4zm5.2 0 4.6-4.6-4.6-4.6L16 6l6 6-6 6-1.4-1.4z',
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
        start: [homeButton()],
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

mount(download);
