PetiteVue.createApp({
    ...saved,
    palettes,
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
}).mount(document.body);
