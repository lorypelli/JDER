const storageKey = (key) =>
    `jder-${key.replace(/[A-Z]/g, (letter) => `-${letter.toLowerCase()}`)}`;

const themeKey = storageKey('darkTheme');
const paletteKey = storageKey('palette');
const [defaultPaletteId] = shared.defaultPalette;

const root = document.documentElement;

const store = (key, value) => {
    try {
        return value == null
            ? localStorage.getItem(key)
            : localStorage.setItem(key, value);
    } catch {
        return null;
    }
};

const palettes = shared.palettes.map((row) => {
    const [id, name, , light, dark] = row.split('|');
    return { id, name, light: light.split(' '), dark: dark.split(' ') };
});

const modes = ['light', 'dark'];
const roles = ['primary', 'secondary', 'tertiary'].flatMap((name) => [
    name,
    `on-${name}`,
    `${name}-container`,
    `on-${name}-container`,
]);
const containerElevations = {
    'surface-container-low': 1,
    'surface-container': 3,
    'surface-container-high': 6,
    'surface-container-highest': 12,
};

const toRgb = (hex) =>
    [0, 2, 4].map((start) => Number.parseInt(hex.slice(start, start + 2), 16));

const surfaceOf = (mode) =>
    getComputedStyle(root)
        .getPropertyValue(`--mdui-color-surface-${mode}`)
        .split(',')
        .map(Number);

const tint = (surface, primary, elevation) => {
    const alpha =
        Math.round(((4.5 * Math.log(elevation + 1) + 2) / 100) * 255) / 255;
    return surface.map((channel, index) =>
        Math.round(channel + (primary[index] - channel) * alpha),
    );
};

const applyPalette = (palette) => {
    for (const mode of modes) {
        const colors = Object.fromEntries(
            roles.map((role, index) => [role, toRgb(palette[mode][index])]),
        );
        for (const [role, elevation] of Object.entries(containerElevations)) {
            colors[role] = tint(surfaceOf(mode), colors.primary, elevation);
        }
        colors['inverse-primary'] = toRgb(
            palette[mode == 'light' ? 'dark' : 'light'][0],
        );
        colors['surface-tint-color'] = colors.primary;
        for (const [role, rgb] of Object.entries(colors)) {
            root.style.setProperty(
                `--mdui-color-${role}-${mode}`,
                rgb.join(', '),
            );
        }
    }
    root.style.setProperty('--palette-swatch', `#${palette.light[0]}`);
};

const applyTheme = (isDark) => {
    root.classList.toggle('mdui-theme-dark', isDark);
    root.classList.toggle('mdui-theme-light', !isDark);
};

const [cursorSize, cursorPadding, cursorStroke] =
    shared.cursorMetrics.map(Number);

const cursorImage = (path, hotspot, color, outline) => {
    const canvas = document.createElement('canvas');
    canvas.width = cursorSize;
    canvas.height = cursorSize;
    const context = canvas.getContext('2d');
    const shape = new Path2D(path);
    context.translate(cursorPadding, cursorPadding);
    context.lineJoin = 'round';
    context.lineWidth = cursorStroke;
    context.strokeStyle = `#${outline}`;
    context.stroke(shape);
    context.fillStyle = `#${color}`;
    context.fill(shape);
    return `url("${canvas.toDataURL()}") ${hotspot}, default`;
};

let cursorImages = {};
const cursorRoles = new Map();

const showCursor = (element, role) => {
    element.style.cursor = cursorImages[role] ?? cursorImages.default;
};

const textInputs = 'textarea, input:not([type="checkbox"], [type="radio"])';

const replaceCursor = (element) => {
    const isNewInput = element.matches(textInputs) && !cursorRoles.has(element);
    const role = isNewInput ? 'text' : getComputedStyle(element).cursor;
    if (!role.startsWith('url')) {
        cursorRoles.set(element, role);
        showCursor(element, role);
    }
};

const cursorObserver = new MutationObserver((records) =>
    records.map(({ target }) => replaceCursor(target)),
);

addEventListener('pointerover', (event) => {
    const [element] = event.composedPath();
    cursorObserver.observe(element, { attributeFilter: ['style'] });
    replaceCursor(element);
});

const applyCursor = ({ isDark, selected }) => {
    const [color, outline] = selected[isDark ? 'dark' : 'light'];
    cursorImages = Object.fromEntries(
        shared.cursors.map((row) => {
            const [role, path, x, y] = row.split('|');
            return [role, cursorImage(path, `${x} ${y}`, color, outline)];
        }),
    );
    showCursor(root, 'default');
    cursorRoles.forEach((role, element) => showCursor(element, role));
};

const readSaved = () => ({
    isDark: store(themeKey) == 'true',
    selected:
        palettes.find(({ id }) => id == store(paletteKey)) ||
        palettes.find(({ id }) => id == defaultPaletteId),
});

const saved = readSaved();

applyTheme(saved.isDark);
applyPalette(saved.selected);
applyCursor(saved);
