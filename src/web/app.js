import { createApp } from 'https://cdnjs.cloudflare.com/ajax/libs/vue/3.5.45/vue.esm-browser.prod.min.js';
import 'https://cdnjs.cloudflare.com/ajax/libs/mdui/2.1.5/mdui.esm.min.js';
import { palettes } from './palettes.js';
import {
    applyPalette,
    applyTheme,
    paletteKey,
    saved,
    store,
    themeKey,
} from './theme.js';

const app = createApp({
    data: () => ({ ...saved, palettes }),
    methods: {
        toggleTheme() {
            this.isDark = !this.isDark;
            store(themeKey, this.isDark ? 'dark' : 'light');
            applyTheme(this.isDark);
        },
        choose(id) {
            this.selected = palettes.find((palette) => palette.id == id);
            store(paletteKey, id);
            applyPalette(this.selected);
        },
        screenshot(key) {
            return `/screenshots/${key}-${this.isDark ? 'dark' : 'light'}.png`;
        },
    },
});

app.config.compilerOptions.isCustomElement = (tag) => tag.startsWith('mdui-');

app.mount('#app');
