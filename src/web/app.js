const app = Vue.createApp({
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
