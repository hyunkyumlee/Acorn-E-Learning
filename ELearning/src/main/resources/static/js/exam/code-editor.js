import {basicSetup, EditorView, indentLess, indentMore, java, keymap, oneDark} from "../vendor/codemirror.bundle.js";

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

const enableTemporaryTabFocus = (view) => {
  view.setTabFocusMode(2000);
  return true;
};

export const createJavaCodeEditor = ({textarea, editorHost, onChange}) => {
  if (!textarea || !editorHost) {
    return null;
  }

  const editorView = new EditorView({
    doc: textarea.value,
    extensions: [
      basicSetup,
      java(),
      oneDark,
      knowvaEditorTheme,
      keymap.of([
        {key: "Escape", run: enableTemporaryTabFocus},
        {key: "Tab", run: indentMore, shift: indentLess},
      ]),
      EditorView.updateListener.of((update) => {
        if (update.docChanged) {
          onChange?.(update.state.doc.toString());
        }
      }),
    ],
    parent: editorHost,
  });

  const focusEditor = () => {
    requestAnimationFrame(() => editorView.focus());
  };

  editorHost.addEventListener("mousedown", focusEditor);
  const editorContent = editorHost.querySelector(".cm-content");
  const labelledBy = editorHost.getAttribute("aria-labelledby");
  const describedBy = textarea.getAttribute("aria-describedby");
  if (editorContent && labelledBy) {
    editorContent.setAttribute("aria-labelledby", labelledBy);
  }
  if (editorContent && describedBy) {
    editorContent.setAttribute("aria-describedby", describedBy);
  }
  textarea.tabIndex = -1;
  textarea.setAttribute("aria-hidden", "true");

  return {
    getValue: () => editorView.state.doc.toString(),
    focus: () => editorView.focus(),
    destroy: () => {
      editorHost.removeEventListener("mousedown", focusEditor);
      editorView.destroy();
    },
  };
};
