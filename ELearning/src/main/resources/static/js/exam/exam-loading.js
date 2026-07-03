(() => {
  const loadingModal = document.querySelector("[data-exam-loading-modal]");

  if (!loadingModal) {
    return;
  }

  const loadingTitle = loadingModal.querySelector("[data-exam-loading-title]");
  const loadingMessage = loadingModal.querySelector("[data-exam-loading-message]");

  const showLoadingModal = (form) => {
    if (loadingTitle) {
      loadingTitle.textContent = form.dataset.loadingTitle || "처리 중입니다";
    }

    if (loadingMessage) {
      loadingMessage.textContent = form.dataset.loadingMessage || "잠시만 기다려 주세요.";
    }

    form.querySelectorAll("button, input[type='submit']").forEach((control) => {
      control.disabled = true;
    });

    loadingModal.hidden = false;
    loadingModal.setAttribute("aria-hidden", "false");
  };

  document.querySelectorAll("form[data-loading-form]").forEach((form) => {
    form.addEventListener("submit", (event) => {
      if (event.defaultPrevented) {
        return;
      }

      if (!form.checkValidity()) {
        form.reportValidity();
        return;
      }

      showLoadingModal(form);
    });
  });
})();
