const syncTheme = () => {
    const { isDark, selected } = readSaved();
    applyTheme(isDark);
    applyPalette(selected);
};
