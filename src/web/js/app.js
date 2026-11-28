const isMacPlatform = () => navigator.platform.startsWith('Mac');

const browserLanguage = () => navigator.language;

const localStorageItem = (key) => localStorage.getItem(key);

const setLocalStorageItem = (key, value) => localStorage.setItem(key, value);

const focusApp = () => {
    document
        .querySelector('#app > div > div')
        .shadowRoot.querySelector('canvas')
        .focus();
};

const syncCursor = () => applyCursor(readSaved());

const writeClipboard = (text, onFailed) => {
    navigator.clipboard.writeText(text).catch(() => onFailed());
};

const warnBeforeUnload = (hasUnsavedChanges) => {
    addEventListener('beforeunload', (event) => {
        if (hasUnsavedChanges()) {
            event.preventDefault();
        }
    });
};

const isDocumentFullscreen = () => document.fullscreenElement != null;

const toggleDocumentFullscreen = () => {
    const request = isDocumentFullscreen()
        ? document.exitFullscreen()
        : document.documentElement.requestFullscreen();
    request.catch(() => null);
};

const onFullscreenChange = (callback) => {
    document.addEventListener('fullscreenchange', () => callback());
};

const pickTextFile = (accept, onPicked, onFailed) => {
    const input = document.createElement('input');
    input.type = 'file';
    input.accept = accept;
    input.addEventListener('change', () => {
        const [file] = input.files;
        file?.text().then(
            (text) => onPicked(file.name, text),
            (error) => onFailed(String(error)),
        );
    });
    input.click();
};

const downloadBlob = (name, type, bytes) => {
    const url = URL.createObjectURL(new Blob([bytes], { type }));
    const link = document.createElement('a');
    link.href = url;
    link.download = name;
    link.click();
    setTimeout(() => URL.revokeObjectURL(url), 60_000);
};
