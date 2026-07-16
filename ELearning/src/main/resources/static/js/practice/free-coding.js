import {createJavaCodeEditor} from "../exam/code-editor.js";

const form = document.querySelector("[data-free-coding-form]");

if (form) {
  const source = form.querySelector(".free-coding-source");
  const editorHost = form.querySelector("[data-free-coding-editor]");
  const input = form.querySelector("[data-free-coding-input]");
  const runButton = form.querySelector("[data-free-coding-run]");
  const outputPanel = form.querySelector("[data-free-coding-output]");
  const statusText = outputPanel?.querySelector("[data-free-coding-status]");
  const timeText = outputPanel?.querySelector("[data-free-coding-time]");
  const outputText = outputPanel?.querySelector("[data-free-coding-output-text]");
  let editor = null;

  try {
    editor = createJavaCodeEditor({
      textarea: source,
      editorHost,
      onChange: (code) => {
        source.value = code;
      },
    });
    if (editor) {
      form.classList.add("is-enhanced");
      requestAnimationFrame(() => editor.focus());
    }
  } catch (error) {
    source?.focus();
  }

  const currentSource = () => {
    if (editor && source) {
      source.value = editor.getValue();
    }
    return source?.value || "";
  };

  const renderOutput = (state, status, elapsedMs, output) => {
    if (!outputPanel || !statusText || !timeText || !outputText) {
      return;
    }
    outputPanel.hidden = false;
    outputPanel.dataset.state = state;
    statusText.textContent = status;
    timeText.textContent = elapsedMs;
    outputText.textContent = output || "(출력 없음)";
  };

  const run = async () => {
    const code = currentSource();
    if (!code.trim()) {
      renderOutput("danger", "실행 불가", "-", "실행할 코드가 필요합니다.");
      editor?.focus();
      return;
    }

    if (!runButton) {
      return;
    }
    runButton.disabled = true;
    renderOutput("pending", "실행 중", "-", "Java 코드를 실행하고 있습니다.");

    try {
      const response = await fetch(form.dataset.runUrl, {
        method: "POST",
        credentials: "same-origin",
        headers: {
          "Accept": "application/json",
          "Content-Type": "application/json",
        },
        body: JSON.stringify({source: code, input: input?.value || ""}),
      });
      const payload = await response.json().catch(() => null);
      if (!response.ok || !payload?.success) {
        throw new Error(payload?.error?.detail || payload?.message || "코드 실행 요청에 실패했습니다.");
      }

      const data = payload.data;
      renderOutput(
        data.success ? "success" : "danger",
        data.message || "실행 완료",
        `${data.elapsedMs}ms`,
        data.output,
      );
    } catch (error) {
      renderOutput("danger", "실행 실패", "-", error instanceof Error ? error.message : "코드 실행 요청에 실패했습니다.");
    } finally {
      runButton.disabled = false;
    }
  };

  form.addEventListener("submit", (event) => {
    event.preventDefault();
    run();
  });

  document.addEventListener("keydown", (event) => {
    if ((event.metaKey || event.ctrlKey) && event.key === "Enter" && form.contains(document.activeElement)) {
      event.preventDefault();
      run();
    }
  });
}
