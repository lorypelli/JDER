import { palettes } from './palettes.js';

export const themeKey = 'jder-theme';
export const paletteKey = 'jder-palette';
const defaultPaletteId = 'Blue';
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

const root = document.documentElement;

export const store = (key, value) => {
    try {
        return value == null
            ? localStorage.getItem(key)
            : localStorage.setItem(key, value);
    } catch {
        return null;
    }
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

export const applyPalette = (palette) => {
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

export const applyTheme = (isDark) => {
    root.classList.toggle('mdui-theme-dark', isDark);
    root.classList.toggle('mdui-theme-light', !isDark);
};

export const saved = {
    isDark: store(themeKey) == 'dark',
    selected:
        palettes.find(({ id }) => id == store(paletteKey)) ??
        palettes.find(({ id }) => id == defaultPaletteId),
};

applyPalette(saved.selected);
applyTheme(saved.isDark);
