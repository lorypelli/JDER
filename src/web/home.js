import { h } from 'vue';
import { icon, icons, link, mount, page, section, state, svg } from './site.js';
import { styles } from './styles.js';

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
        text: 'Scritto in Kotlin con Compose Multiplatform: gira su Windows, macOS e Linux con un solo JAR',
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

const technologies = ['Kotlin', 'Compose Multiplatform', 'Material 3', 'Java'];

const links = () => [
    h('nav', { class: 'app-bar-links' }, [
        h('mdui-button', { variant: 'text', href: '/download' }, 'Scarica'),
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

mount(() =>
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
        ],
    }),
);
