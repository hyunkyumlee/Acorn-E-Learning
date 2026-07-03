import {basicSetup, EditorView} from "https://esm.sh/codemirror";
import {indentLess, indentMore} from "https://esm.sh/@codemirror/commands";
import {java} from "https://esm.sh/@codemirror/lang-java";
import {oneDark} from "https://esm.sh/@codemirror/theme-one-dark";

const form = document.querySelector("[data-code-run-form]");
const runButton = document.querySelector("[data-code-run-button]");
const resultPanel = document.querySelector("[data-code-run-result]");
const finalSubmitModal = document.querySelector("[data-final-submit-modal]");

if (form && runButton && resultPanel) {
  const textarea = form.querySelector("textarea[name='answerText']");
  const editorHost = form.querySelector("[data-code-editor]");
  const submitButton = document.querySelector("[data-code-submit-button]");
  const statusText = resultPanel.querySelector("[data-code-run-status]");
  const timeText = resultPanel.querySelector("[data-code-run-time]");
  const detailText = resultPanel.querySelector("[data-code-run-detail]");
  let editorView = null;
  const initialAnswerText = textarea?.value || "";
  const alreadySubmitted = form.dataset.answerSubmitted === "true";

  const updateSubmitState = (code) => {
    if (!submitButton) {
      return;
    }
    const changed = code !== initialAnswerText;
    submitButton.disabled = alreadySubmitted && !changed;
    submitButton.textContent = alreadySubmitted && !changed ? "제출됨" : (alreadySubmitted ? "다시 제출" : "제출");
  };
  const insertSoftTab = (view) => {
    const hasSelection = view.state.selection.ranges.some((range) => !range.empty);
    if (hasSelection) {
      return indentMore(view);
    }
    view.dispatch(view.state.replaceSelection("    "));
    return true;
  };
  const handleEditorTab = (event) => {
    if (event.key !== "Tab" || !editorView) {
      return;
    }
    event.preventDefault();
    event.stopPropagation();
    if (event.shiftKey) {
      indentLess(editorView);
      return;
    }
    insertSoftTab(editorView);
  };

  const knowvaEditorTheme = EditorView.theme({
    "&": {
      minHeight: "420px",
    },
    ".cm-scroller": {
      fontFamily: "\"SFMono-Regular\", Consolas, \"Liberation Mono\", monospace",
      lineHeight: "1.55",
    },
    ".cm-content": {
      minHeight: "420px",
      padding: "16px",
      caretColor: "var(--primary)",
    },
    ".cm-gutters": {
      borderRight: "1px solid rgba(148, 163, 184, 0.22)",
    },
    "&.cm-focused": {
      outline: "none",
    },
  });

  if (textarea && editorHost) {
    editorView = new EditorView({
      doc: textarea.value,
      extensions: [
        basicSetup,
        java(),
        oneDark,
        knowvaEditorTheme,
        EditorView.updateListener.of((update) => {
          if (update.docChanged) {
            textarea.value = update.state.doc.toString();
            updateSubmitState(textarea.value);
          }
        }),
      ],
      parent: editorHost,
    });
    editorHost.addEventListener("keydown", handleEditorTab, true);
    editorHost.addEventListener("mousedown", () => {
      requestAnimationFrame(() => editorView?.focus());
    });
    form.classList.add("is-enhanced");
  }
  textarea?.addEventListener("input", () => updateSubmitState(textarea.value));
  updateSubmitState(initialAnswerText);

  const syncTextarea = () => {
    if (editorView && textarea) {
      textarea.value = editorView.state.doc.toString();
    }
    return textarea?.value || "";
  };

  const renderResult = (state, status, elapsedMs, detail) => {
    resultPanel.hidden = false;
    resultPanel.dataset.state = state;
    statusText.textContent = status;
    timeText.textContent = elapsedMs;
    detailText.textContent = detail;
  };

  const caseSummary = (data) => {
    const failedCase = (data.cases || []).find((testCase) => !testCase.passed);
    const lines = [`${data.passedCount} / ${data.totalCount} 테스트 통과`];
    if (failedCase) {
      lines.push(`입력: ${failedCase.input || "(없음)"}`);
      lines.push(`기대 출력: ${failedCase.expectedOutput || "(없음)"}`);
      lines.push(`실제 출력: ${failedCase.actualOutput || "(없음)"}`);
      if (failedCase.errorMessage) {
        lines.push(`오류: ${failedCase.errorMessage}`);
      }
    }
    return lines.join("\n");
  };

  form.addEventListener("submit", (event) => {
    const code = syncTextarea();
    if (!code.trim()) {
      event.preventDefault();
      renderResult("danger", "제출 불가", "-", "답안 코드가 필요합니다.");
      editorView?.focus();
    }
  });

  runButton.addEventListener("click", async () => {
    const code = syncTextarea();
    if (!code.trim()) {
      renderResult("danger", "실행 불가", "-", "실행할 코드가 필요합니다.");
      editorView?.focus();
      return;
    }

    runButton.disabled = true;
    renderResult("pending", "실행 중", "-", "테스트케이스를 실행하고 있습니다.");

    try {
      const response = await fetch(form.dataset.testRunUrl, {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
        },
        body: JSON.stringify({answerText: code}),
      });
      const payload = await response.json();
      if (!response.ok || !payload.success) {
        const message = payload.error?.detail || payload.message || "실행 테스트 요청에 실패했습니다.";
        throw new Error(message);
      }
      const data = payload.data;
      renderResult(
        data.passed ? "success" : "danger",
        data.message,
        `${data.elapsedMs}ms`,
        caseSummary(data),
      );
    } catch (error) {
      renderResult("danger", "실행 실패", "-", error.message);
    } finally {
      runButton.disabled = false;
    }
  });
}

if (finalSubmitModal) {
  const closeButton = finalSubmitModal.querySelector("[data-final-submit-close]");
  closeButton?.addEventListener("click", () => {
    finalSubmitModal.hidden = true;
  });
}
