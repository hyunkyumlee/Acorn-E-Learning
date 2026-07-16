import { Editor, defaultValueCtx, editorViewCtx, rootCtx } from '@milkdown/kit/core';
import {
  commonmark,
  createCodeBlockCommand,
  insertHrCommand,
  toggleEmphasisCommand,
  toggleStrongCommand,
  turnIntoTextCommand,
  wrapInBlockquoteCommand,
  wrapInBulletListCommand,
  wrapInHeadingCommand,
  wrapInOrderedListCommand,
} from '@milkdown/kit/preset/commonmark';
import { gfm } from '@milkdown/kit/preset/gfm';
import { imageBlockComponent, imageBlockConfig } from '@milkdown/kit/component/image-block';
import { clipboard } from '@milkdown/kit/plugin/clipboard';
import { history } from '@milkdown/kit/plugin/history';
import { listener, listenerCtx } from '@milkdown/kit/plugin/listener';
import { SlashProvider, slashFactory } from '@milkdown/kit/plugin/slash';
import { upload, uploadConfig } from '@milkdown/kit/plugin/upload';
import { newlineInCode } from '@milkdown/kit/prose/commands';
import { Fragment } from '@milkdown/kit/prose/model';
import { TextSelection } from '@milkdown/kit/prose/state';
import { callCommand, insert } from '@milkdown/kit/utils';

const toolbarItems = [
  { command: 'strong', label: 'B', title: '굵게' },
  { command: 'emphasis', label: 'I', title: '기울임꼴' },
  { command: 'heading-1', label: 'H1', title: '제목 1' },
  { command: 'heading-2', label: 'H2', title: '제목 2' },
  { command: 'heading-3', label: 'H3', title: '제목 3' },
  { command: 'bullet', label: '•', title: '글머리 목록' },
  { command: 'ordered', label: '1.', title: '번호 목록' },
  { command: 'quote', label: '“', title: '인용문' },
  { command: 'code', label: '</>', title: '코드 블록' },
  { command: 'divider', label: '—', title: '구분선' },
  { command: 'image', label: 'IMG', title: '이미지 추가' },
];

const slashCommands = [
  { command: 'text', label: '텍스트', description: '기본 단락을 작성합니다.', icon: 'T' },
  { command: 'heading-1', label: '제목 1', description: '큰 섹션 제목을 만듭니다.', icon: 'H1' },
  { command: 'heading-2', label: '제목 2', description: '중간 섹션 제목을 만듭니다.', icon: 'H2' },
  { command: 'heading-3', label: '제목 3', description: '작은 섹션 제목을 만듭니다.', icon: 'H3' },
  { command: 'bullet', label: '글머리 목록', description: '순서 없는 목록을 만듭니다.', icon: '•' },
  { command: 'ordered', label: '번호 목록', description: '순서 있는 목록을 만듭니다.', icon: '1.' },
  { command: 'quote', label: '인용문', description: '인용 블록을 만듭니다.', icon: '“' },
  { command: 'code', label: '코드 블록', description: '코드를 입력할 블록을 만듭니다.', icon: '</>' },
  { command: 'divider', label: '구분선', description: '내용 사이에 구분선을 넣습니다.', icon: '—' },
  { command: 'image', label: '이미지', description: '본문 안에 이미지를 추가합니다.', icon: 'IMG' },
];

const communitySlash = slashFactory('community');

function getEditorCommand(command) {
  switch (command) {
    case 'strong': return callCommand(toggleStrongCommand.key);
    case 'emphasis': return callCommand(toggleEmphasisCommand.key);
    case 'text': return callCommand(turnIntoTextCommand.key);
    case 'heading-1': return callCommand(wrapInHeadingCommand.key, 1);
    case 'heading-2': return callCommand(wrapInHeadingCommand.key, 2);
    case 'heading-3': return callCommand(wrapInHeadingCommand.key, 3);
    case 'bullet': return callCommand(wrapInBulletListCommand.key);
    case 'ordered': return callCommand(wrapInOrderedListCommand.key);
    case 'quote': return callCommand(wrapInBlockquoteCommand.key);
    case 'code': return callCommand(createCodeBlockCommand.key);
    case 'divider': return callCommand(insertHrCommand.key);
    default: return null;
  }
}

function runEditorCommand(editor, command) {
  const editorCommand = getEditorCommand(command);
  if (!editorCommand) return false;
  return editor.action(editorCommand);
}

function moveFocusOutsideFinalCodeBlock(view, event) {
  if (event.button !== 0 || !(event.target instanceof HTMLElement) || event.target.closest('pre')) return false;

  const codeBlocks = Array.from(view.dom.querySelectorAll('pre'));
  const finalCodeBlockElement = codeBlocks[codeBlocks.length - 1];
  const codeBlockNode = view.state.doc.lastChild;
  const paragraph = view.state.schema.nodes.paragraph;
  if (!finalCodeBlockElement || codeBlockNode?.type.name !== 'code_block' || !paragraph) return false;
  if (event.clientY <= finalCodeBlockElement.getBoundingClientRect().bottom) return false;

  const paragraphPosition = view.state.doc.content.size;

  const transaction = view.state.tr.insert(paragraphPosition, paragraph.create());
  view.dispatch(transaction
    .setSelection(TextSelection.create(transaction.doc, paragraphPosition + 1))
    .scrollIntoView());
  view.focus();
  event.preventDefault();
  return true;
}

function addCodeBlockBlankClickHandler(mount, editor) {
  mount.addEventListener('mousedown', (event) => {
    const moved = editor.action((ctx) => moveFocusOutsideFinalCodeBlock(ctx.get(editorViewCtx), event));
    if (moved) event.stopImmediatePropagation();
  }, true);
}

function removeSlashTrigger(editor) {
  return editor.action((ctx) => {
    const view = ctx.get(editorViewCtx);
    const { from } = view.state.selection;
    if (from <= 1) return false;
    view.dispatch(view.state.tr.deleteRange(from - 1, from).scrollIntoView());
    view.focus();
    return true;
  });
}

function runSlashCommand(editor, command) {
  const editorCommand = getEditorCommand(command);
  if (!editorCommand) return false;
  return editor.action((ctx) => {
    const view = ctx.get(editorViewCtx);
    const { from } = view.state.selection;
    if (from <= 1) return false;
    view.dispatch(view.state.tr.deleteRange(from - 1, from).scrollIntoView());
    view.focus();
    return editorCommand(ctx);
  });
}

async function uploadInlineImage(uploadUrl, file) {
  if (!file || !file.type.startsWith('image/')) {
    throw new Error('PNG, JPG, WEBP 이미지만 본문에 넣을 수 있습니다.');
  }
  const formData = new FormData();
  formData.append('image', file, file.name);
  const response = await fetch(uploadUrl, {
    method: 'POST', body: formData, credentials: 'same-origin', headers: { Accept: 'application/json' },
  });
  const payload = await response.json().catch(() => null);
  if (!response.ok || !payload?.success || !payload?.data?.url) {
    throw new Error(payload?.error?.detail || payload?.message || '이미지 업로드에 실패했습니다.');
  }
  return payload.data.url;
}

function setEditorError(shell, message) {
  const error = shell.querySelector('[data-community-editor-error]');
  if (error) {
    error.hidden = !message;
    error.textContent = message || '';
  }
}

function addToolbar(shell, mount, editor, uploadImage) {
  const toolbar = document.createElement('div');
  toolbar.className = 'community-editor-toolbar';
  toolbar.setAttribute('aria-label', '기본 모드 본문 서식 도구');
  const uploadInput = document.createElement('input');
  uploadInput.type = 'file';
  uploadInput.accept = 'image/png,image/jpeg,image/webp';
  uploadInput.hidden = true;
  toolbar.append(uploadInput);

  toolbarItems.forEach(({ command, label, title }) => {
    const button = document.createElement('button');
    button.type = 'button';
    button.className = 'community-editor-tool';
    button.textContent = label;
    button.title = title;
    button.setAttribute('aria-label', title);
    button.addEventListener('mousedown', (event) => event.preventDefault());
    button.addEventListener('click', () => command === 'image' ? uploadInput.click() : runEditorCommand(editor, command));
    toolbar.append(button);
  });

  uploadInput.addEventListener('change', async () => {
    const [file] = uploadInput.files || [];
    if (!file) return;
    try {
      const url = await uploadImage(file);
      editor.action(insert(`![${file.name}](${url})`));
      setEditorError(shell, '');
    } catch (error) {
      setEditorError(shell, error instanceof Error ? error.message : '이미지 업로드에 실패했습니다.');
    } finally {
      uploadInput.value = '';
    }
  });
  shell.prepend(toolbar);
  return { openImagePicker: () => uploadInput.click(), setVisible: (visible) => { toolbar.hidden = !visible; } };
}

function addSlashMenu(shell, mount, editor, openImagePicker) {
  const menu = document.createElement('div');
  menu.className = 'community-editor-slash-menu';
  menu.dataset.show = 'false';
  menu.setAttribute('role', 'dialog');
  menu.setAttribute('aria-label', 'Markdown 블록 추가 메뉴');
  const heading = document.createElement('p');
  heading.className = 'community-editor-slash-heading';
  heading.textContent = '블록 추가';
  menu.append(heading);
  const list = document.createElement('div');
  list.className = 'community-editor-slash-list';
  menu.append(list);
  const commandButtons = [];
  let activeCommandIndex = -1;

  const slashProvider = new SlashProvider({
    content: menu,
    root: document.body,
    debounce: 0,
    floatingUIOptions: { strategy: 'fixed' },
    shouldShow(view) {
      return shell.dataset.communityEditorMode === 'markdown'
        && slashProvider.getContent(view) === '/';
    },
  });

  const setActiveCommand = (index) => {
    activeCommandIndex = index;
    commandButtons.forEach((button, buttonIndex) => {
      const active = buttonIndex === index;
      button.classList.toggle('is-active', active);
      button.setAttribute('aria-selected', String(active));
    });
  };
  const runSlashMenuCommand = (item) => {
    slashProvider.hide();
    if (item.command === 'image') {
      removeSlashTrigger(editor);
      openImagePicker();
      return;
    }
    runSlashCommand(editor, item.command);
  };

  slashProvider.onShow = () => {
    if (activeCommandIndex < 0) setActiveCommand(0);
  };
  slashProvider.onHide = () => setActiveCommand(-1);

  slashCommands.forEach((item) => {
    const button = document.createElement('button');
    button.type = 'button';
    button.className = 'community-editor-slash-command';
    const icon = document.createElement('span');
    icon.className = 'community-editor-slash-icon';
    icon.textContent = item.icon;
    const copy = document.createElement('span');
    copy.className = 'community-editor-slash-copy';
    const label = document.createElement('strong');
    label.textContent = item.label;
    const description = document.createElement('small');
    description.textContent = item.description;
    copy.append(label, description);
    button.append(icon, copy);
    button.addEventListener('mousedown', (event) => {
      event.preventDefault();
      runSlashMenuCommand(item);
    });
    list.append(button);
    commandButtons.push(button);
  });

  mount.addEventListener('keydown', (event) => {
    const menuVisible = menu.dataset.show === 'true';
    if (!menuVisible) {
      if (event.key === 'Enter'
        && !event.isComposing
        && !event.metaKey
        && !event.ctrlKey
        && !event.altKey
        && !event.shiftKey) {
        let insertedNewline = false;
        editor.action((ctx) => {
          const view = ctx.get(editorViewCtx);
          insertedNewline = newlineInCode(view.state, (transaction) => view.dispatch(transaction));
          return insertedNewline;
        });
        if (insertedNewline) {
          event.preventDefault();
          event.stopImmediatePropagation();
          return;
        }
      }
      if (event.key === 'Escape') slashProvider.hide();
      return;
    }

    if (event.key === 'ArrowDown' || event.key === 'ArrowUp') {
      event.preventDefault();
      const direction = event.key === 'ArrowDown' ? 1 : -1;
      const nextIndex = activeCommandIndex < 0
        ? (direction > 0 ? 0 : commandButtons.length - 1)
        : (activeCommandIndex + direction + commandButtons.length) % commandButtons.length;
      setActiveCommand(nextIndex);
      return;
    }

    if (event.key === 'Enter') {
      event.preventDefault();
      const commandIndex = activeCommandIndex < 0 ? 0 : activeCommandIndex;
      runSlashMenuCommand(slashCommands[commandIndex]);
      return;
    }

    if (event.key === 'Escape') {
      event.preventDefault();
      slashProvider.hide();
    }
  }, true);

  const configureSlash = (ctx) => {
    ctx.set(communitySlash.key, {
      view: () => ({
        update: slashProvider.update,
        destroy: slashProvider.destroy,
      }),
    });
    return () => slashProvider.destroy();
  };

  return {
    configureSlash,
    sync: () => {
      editor.action((ctx) => {
        slashProvider.update(ctx.get(editorViewCtx));
        return false;
      });
    },
  };
}

function addModeSwitch(shell, mount, toolbar, onModeChange) {
  const switcher = document.createElement('div');
  switcher.className = 'community-editor-mode-switch';
  switcher.setAttribute('role', 'group');
  switcher.setAttribute('aria-label', '에디터 입력 모드');
  const modes = [
    { mode: 'basic', label: '기본 모드', help: '기본 모드에서는 위 옵션 메뉴를 눌러 본문 서식을 적용할 수 있습니다.' },
    { mode: 'markdown', label: 'Markdown 모드', help: 'Markdown 모드에서는 빈 줄에서 /를 입력해 블록 메뉴를 열 수 있습니다.' },
  ];
  const help = document.getElementById(shell.getAttribute('aria-describedby'));
  const setMode = (mode) => {
    shell.dataset.communityEditorMode = mode;
    toolbar.setVisible(mode === 'basic');
    switcher.querySelectorAll('button').forEach((button) => {
      const active = button.dataset.mode === mode;
      button.classList.toggle('is-active', active);
      button.setAttribute('aria-pressed', String(active));
    });
    const selectedMode = modes.find((item) => item.mode === mode);
    if (help && selectedMode) help.textContent = selectedMode.help;
    mount.querySelector('.ProseMirror')?.focus();
    onModeChange();
  };
  modes.forEach(({ mode, label }) => {
    const button = document.createElement('button');
    button.type = 'button';
    button.dataset.mode = mode;
    button.textContent = label;
    button.addEventListener('click', () => setMode(mode));
    switcher.append(button);
  });
  shell.prepend(switcher);
  setMode('basic');
}

async function initializeEditor(shell) {
  const source = shell.querySelector('.community-editor-source');
  const mount = shell.querySelector('.community-editor-mount');
  const form = shell.closest('form');
  const draftIdInput = form?.querySelector('[name="draftPostId"]');
  let uploadUrl = shell.dataset.uploadUrl || '';
  const createDraftUrl = shell.dataset.createDraftUrl;
  const uploadUrlTemplate = shell.dataset.uploadUrlTemplate;
  if (!source || !mount || !form || (!uploadUrl && (!createDraftUrl || !uploadUrlTemplate))) return;

  const ensureUploadUrl = async () => {
    if (uploadUrl) return uploadUrl;
    const response = await fetch(createDraftUrl, { method: 'POST', credentials: 'same-origin', headers: { Accept: 'application/json' } });
    const payload = await response.json().catch(() => null);
    const postId = payload?.data?.postId;
    if (!response.ok || !payload?.success || !postId || !draftIdInput) {
      throw new Error(payload?.error?.detail || payload?.message || '이미지 작성 준비에 실패했습니다.');
    }
    draftIdInput.value = postId;
    uploadUrl = uploadUrlTemplate.replace('POST_ID_PLACEHOLDER', postId);
    return uploadUrl;
  };
  const uploadImage = async (file) => uploadInlineImage(await ensureUploadUrl(), file);
  const configureCommunityEditor = (ctx) => {
    ctx.get(listenerCtx).markdownUpdated((_, markdown) => { source.value = markdown.trim(); });
    ctx.update(imageBlockConfig.key, (config) => ({
      ...config, imageIcon: '', captionIcon: '', uploadButton: '이미지 선택', confirmButton: '확인',
      uploadPlaceholderText: '이미지를 드래그하거나 선택해 주세요.', captionPlaceholderText: '이미지 설명',
      onUpload: uploadImage, maxWidth: 900, maxHeight: 720,
    }));
    ctx.update(uploadConfig.key, (config) => ({
      ...config,
      uploader: async (files, schema) => {
        const imageFiles = Array.from(files).filter((file) => file.type.startsWith('image/'));
        if (!imageFiles.length) return Fragment.empty;
        const urls = await Promise.all(imageFiles.map(uploadImage));
        const imageType = schema.nodes['image-block'];
        return urls.map((url, index) => imageType.create({ src: url, caption: imageFiles[index].name, ratio: 1 }));
      },
    }));
    return () => {};
  };

  let editor;
  let openImagePicker = () => {};
  let slashMenu;
  try {
    editor = Editor.make()
      .config((ctx) => { ctx.set(rootCtx, mount); ctx.set(defaultValueCtx, source.value); });
    slashMenu = addSlashMenu(shell, mount, editor, () => openImagePicker());
    editor
      .config(slashMenu.configureSlash)
      .use(commonmark).use(gfm).use(imageBlockComponent).use(history).use(clipboard).use(upload).use(listener).use(configureCommunityEditor).use(communitySlash);
    await editor.create();
    addCodeBlockBlankClickHandler(mount, editor);
  } catch (error) {
    setEditorError(shell, '에디터를 불러오지 못했습니다. 아래 입력창으로 내용을 작성해 주세요.');
    return;
  }

  source.required = false;
  source.classList.add('is-enhanced');
  shell.classList.add('is-ready');
  mount.classList.add('milkdown');
  const toolbar = addToolbar(shell, mount, editor, uploadImage);
  openImagePicker = toolbar.openImagePicker;
  addModeSwitch(shell, mount, toolbar, slashMenu.sync);
  form.addEventListener('submit', (event) => {
    if (!source.value.trim()) {
      event.preventDefault();
      setEditorError(shell, '내용을 입력해 주세요.');
      mount.focus();
    }
  });
}

document.querySelectorAll('[data-community-editor]').forEach((shell) => {
  const error = document.createElement('p');
  error.className = 'community-editor-error';
  error.dataset.communityEditorError = '';
  error.hidden = true;
  error.setAttribute('role', 'alert');
  shell.append(error);
  initializeEditor(shell);
});
