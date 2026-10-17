const storageKey = "jder-theme";
let toggle;

const setTheme = (isDark) => {
  document.documentElement.dataset.theme = isDark ? "dark" : "light";
  toggle.querySelector(".theme-toggle-icon").textContent = isDark ? "☀️" : "🌙";
  toggle.querySelector(".theme-toggle-label").textContent = isDark
    ? "Tema chiaro"
    : "Tema scuro";
};

const toggleTheme = () => {
  const nextIsDark = document.documentElement.dataset.theme != "dark";
  setTheme(nextIsDark);
  localStorage.setItem(storageKey, nextIsDark ? "dark" : "light");
};

window.onload = () => {
  toggle = document.querySelector(".theme-toggle");
  setTheme(localStorage.getItem(storageKey) == "dark");
  toggle.addEventListener("click", toggleTheme);
};
