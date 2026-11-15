const typescale = (name) => ({
    fontSize: `var(--mdui-typescale-${name}-size)`,
    lineHeight: `var(--mdui-typescale-${name}-line-height)`,
});

const muted = { color: 'rgb(var(--mdui-color-on-surface-variant))' };
const primary = { color: 'rgb(var(--mdui-color-primary))' };
const rule = '0.0625rem solid rgb(var(--mdui-color-outline-variant))';

const column = (gap) => ({ display: 'flex', flexDirection: 'column', gap });

const wrap = (gap) => ({ display: 'flex', flexWrap: 'wrap', gap });

const card = (basis) => ({ flex: `1 1 ${basis}`, minWidth: 0 });

export const styles = {
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
    footer: {
        ...muted,
        padding: '1.5rem var(--page-padding)',
        borderTop: rule,
        fontSize: 'var(--mdui-typescale-body-medium-size)',
        textAlign: 'center',
    },
};
