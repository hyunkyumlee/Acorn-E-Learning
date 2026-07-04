(() => {
  const loadingModal = document.querySelector("[data-exam-loading-modal]");

  if (!loadingModal) {
    return;
  }

  const loadingTitle = loadingModal.querySelector("[data-exam-loading-title]");
  const loadingMessage = loadingModal.querySelector("[data-exam-loading-message]");
  const editorLoading = loadingModal.hasAttribute("data-editor-loading-modal");
  let editorLoadingTimer = null;

  const setLoadingContent = (title, message) => {
    if (loadingTitle) {
      loadingTitle.textContent = title || "처리 중입니다";
    }

    if (loadingMessage) {
      loadingMessage.textContent = message || "잠시만 기다려 주세요.";
    }
  };

  const hideLoadingModal = () => {
    loadingModal.hidden = true;
    loadingModal.setAttribute("aria-hidden", "true");
    loadingModal.dataset.state = "";
  };

  const showLoadingModal = (form) => {
    setLoadingContent(form.dataset.loadingTitle, form.dataset.loadingMessage);

    form.querySelectorAll("button, input[type='submit']").forEach((control) => {
      control.disabled = true;
    });

    loadingModal.hidden = false;
    loadingModal.setAttribute("aria-hidden", "false");
  };

  if (editorLoading && !loadingModal.hidden) {
    editorLoadingTimer = window.setTimeout(() => {
      if (loadingModal.hidden) {
        return;
      }

      loadingModal.dataset.state = "error";
      setLoadingContent(
        "코드 에디터를 불러오지 못했습니다",
        "네트워크 상태를 확인한 뒤 새로고침해 주세요."
      );
    }, 15000);
  }

  document.addEventListener("knowva:code-editor-ready", () => {
    if (!editorLoading) {
      return;
    }

    if (editorLoadingTimer) {
      window.clearTimeout(editorLoadingTimer);
    }
    hideLoadingModal();
  });

  document.addEventListener("knowva:code-editor-failed", () => {
    if (!editorLoading) {
      return;
    }

    if (editorLoadingTimer) {
      window.clearTimeout(editorLoadingTimer);
    }
    loadingModal.dataset.state = "error";
    setLoadingContent(
      "코드 에디터를 불러오지 못했습니다",
      "잠시 후 새로고침하거나 네트워크 상태를 확인해 주세요."
    );
  });

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
