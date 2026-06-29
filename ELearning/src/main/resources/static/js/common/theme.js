(function () {
  var storageKey = "knowva-ui-theme";
  var root = document.documentElement;

  function storedTheme() {
    try {
      return localStorage.getItem(storageKey);
    } catch (error) {
      return null;
    }
  }

  function preferredTheme() {
    if (storedTheme() === "dark" || storedTheme() === "light") {
      return storedTheme();
    }
    return window.matchMedia && window.matchMedia("(prefers-color-scheme: dark)").matches ? "dark" : "light";
  }

  function applyTheme(mode) {
    var isDark = mode === "dark";
    root.dataset.theme = isDark ? "knowva-ion-dark" : "knowva-ion-light";
    root.style.colorScheme = isDark ? "dark" : "light";
    if (document.body) {
      document.body.classList.toggle("theme-dark", isDark);
    }
    document.querySelectorAll("[data-theme-toggle]").forEach(function (button) {
      button.setAttribute("aria-pressed", String(isDark));
      button.setAttribute("aria-label", isDark ? "라이트모드로 전환" : "다크모드로 전환");
    });
  }

  function persistTheme(mode) {
    try {
      localStorage.setItem(storageKey, mode);
    } catch (error) {
      return;
    }
  }

  function activateCurrentNav() {
    var path = window.location.pathname.replace(/\/$/, "") || "/";
    document.querySelectorAll("[data-nav-prefix], [data-nav-exact]").forEach(function (link) {
      var prefix = link.getAttribute("data-nav-prefix");
      var exact = link.getAttribute("data-nav-exact");
      var active = exact ? path === exact : path === prefix || path.indexOf(prefix + "/") === 0;
      link.classList.toggle("is-active", active);
      if (active) {
        link.setAttribute("aria-current", "page");
      } else {
        link.removeAttribute("aria-current");
      }
    });
  }

  applyTheme(preferredTheme());

  document.addEventListener("DOMContentLoaded", function () {
    applyTheme(preferredTheme());
    activateCurrentNav();

    document.addEventListener("click", function (event) {
      var button = event.target.closest("[data-theme-toggle]");
      if (!button) {
        return;
      }
      var nextTheme = root.dataset.theme === "knowva-ion-dark" ? "light" : "dark";
      persistTheme(nextTheme);
      applyTheme(nextTheme);
    });
  });
})();
