(() => {
  const root = document.documentElement;
  const toggle = document.getElementById('theme-toggle');
  const themeMeta = document.querySelector('meta[name="theme-color"]');
  const storageKey = 'harbor-site-theme';
  const systemTheme = window.matchMedia('(prefers-color-scheme: light)').matches ? 'light' : 'dark';
  const savedTheme = localStorage.getItem(storageKey);

  function applyTheme(theme) {
    root.dataset.theme = theme;
    themeMeta?.setAttribute('content', theme === 'light' ? '#f2f8f8' : '#060c10');
    if (toggle) {
      const next = theme === 'light' ? 'dark' : 'light';
      toggle.setAttribute('aria-label', `Switch to ${next} theme`);
      toggle.setAttribute('title', `Switch to ${next} theme`);
    }
  }

  applyTheme(savedTheme === 'light' || savedTheme === 'dark' ? savedTheme : systemTheme);

  toggle?.addEventListener('click', () => {
    const next = root.dataset.theme === 'light' ? 'dark' : 'light';
    localStorage.setItem(storageKey, next);
    applyTheme(next);
  });
})();
