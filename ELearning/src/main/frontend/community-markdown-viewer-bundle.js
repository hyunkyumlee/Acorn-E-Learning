import DOMPurify from 'dompurify';
import { marked } from 'marked';

const LOCAL_IMAGE_PATH = /^\/community\/attachments\/\d+\/file$/;

function isSafeLink(value) {
  try {
    const url = new URL(value, window.location.origin);
    return ['http:', 'https:', 'mailto:'].includes(url.protocol);
  } catch {
    return false;
  }
}

function isSafeInlineImage(value) {
  try {
    const url = new URL(value, window.location.origin);
    return url.origin === window.location.origin && LOCAL_IMAGE_PATH.test(url.pathname);
  } catch {
    return false;
  }
}

function applyImageResize(image) {
  const ratio = Number(image.alt);
  if (!Number.isFinite(ratio) || ratio <= 0 || ratio === 1) {
    return;
  }

  const updateHeight = () => {
    if (!image.naturalWidth || !image.naturalHeight || !image.clientWidth) {
      return;
    }
    const height = Math.max(100, Math.round(image.clientWidth * (image.naturalHeight / image.naturalWidth) * ratio));
    image.style.height = `${height}px`;
    image.style.objectFit = 'cover';
  };

  image.alt = '';
  if (image.complete) {
    updateHeight();
  } else {
    image.addEventListener('load', updateHeight, { once: true });
  }
  new ResizeObserver(updateHeight).observe(image);
}

function renderMarkdown(source) {
  const targetSelector = source.dataset.target;
  const target = targetSelector ? document.querySelector(targetSelector) : null;
  if (!target) {
    return;
  }

  const html = marked.parse(source.value || '', {
    breaks: true,
    gfm: true,
  });
  target.innerHTML = DOMPurify.sanitize(html, {
    USE_PROFILES: { html: true },
    FORBID_TAGS: ['button', 'embed', 'form', 'iframe', 'input', 'math', 'object', 'script', 'style', 'svg'],
  });

  target.querySelectorAll('a').forEach((anchor) => {
    if (!isSafeLink(anchor.getAttribute('href') || '')) {
      anchor.removeAttribute('href');
      return;
    }
    if (anchor.protocol === 'http:' || anchor.protocol === 'https:') {
      anchor.rel = 'noopener noreferrer';
      anchor.target = '_blank';
    }
  });

  target.querySelectorAll('img').forEach((image) => {
    if (!isSafeInlineImage(image.getAttribute('src') || '')) {
      image.remove();
      return;
    }
    image.loading = 'lazy';
    image.decoding = 'async';
    applyImageResize(image);
  });
}

document.querySelectorAll('[data-community-markdown-source]').forEach(renderMarkdown);
