const fs = require("fs");
const path = require("path");
const { chromium } = require("playwright");

const repoRoot = "/Users/hyunkyumlee/Documents/Acorn-E-Learning";
const docsRoot = path.join(repoRoot, "docs");
const uiRoot = path.join(docsRoot, "ui-html2");
const outputRoot = path.join(repoRoot, ".sisyphus/ulw-ui-html2-qa/evidence");
const screenshotRoot = path.join(outputRoot, "screenshots");
const baseUrl = process.env.KNOWVA_QA_BASE_URL || "http://127.0.0.1:4174/ui-html2";
const chromePath = "/Applications/Google Chrome.app/Contents/MacOS/Google Chrome";

const desktop = { name: "desktop", width: 1440, height: 1100 };
const mobile = { name: "mobile", width: 390, height: 1000 };
const keyMobileScreens = new Set([
  "screens/welcome/index.html",
  "screens/learning/main.html",
  "screens/community/index.html",
  "screens/community/board.html",
  "screens/mypage/index.html",
  "screens/settings/index.html",
]);

function walk(dir) {
  return fs.readdirSync(dir, { withFileTypes: true }).flatMap((entry) => {
    const absolute = path.join(dir, entry.name);
    if (entry.isDirectory()) return walk(absolute);
    return [absolute];
  });
}

function screenFiles() {
  return walk(path.join(uiRoot, "screens"))
    .filter((file) => file.endsWith(".html"))
    .sort();
}

function slug(value) {
  return value.replace(/[^a-z0-9]+/gi, "_").replace(/^_+|_+$/g, "").toLowerCase();
}

function rel(file) {
  return path.relative(uiRoot, file);
}

function toUrl(relativePath) {
  return `${baseUrl}/${relativePath.replaceAll(path.sep, "/")}`;
}

function parseRgb(value) {
  const match = String(value || "").match(/rgba?\(([^)]+)\)/);
  if (!match) return null;
  const parts = match[1].split(",").map((part) => Number.parseFloat(part.trim()));
  if (parts.length < 3 || parts.some((part, index) => index < 3 && Number.isNaN(part))) return null;
  const alpha = parts.length >= 4 ? parts[3] : 1;
  if (alpha === 0) return null;
  return { r: parts[0], g: parts[1], b: parts[2], a: alpha };
}

function luminance(rgb) {
  const convert = (value) => {
    const channel = value / 255;
    return channel <= 0.03928 ? channel / 12.92 : ((channel + 0.055) / 1.055) ** 2.4;
  };
  return 0.2126 * convert(rgb.r) + 0.7152 * convert(rgb.g) + 0.0722 * convert(rgb.b);
}

function contrast(a, b) {
  const light = Math.max(luminance(a), luminance(b));
  const dark = Math.min(luminance(a), luminance(b));
  return (light + 0.05) / (dark + 0.05);
}

async function collectPageIssues(page, theme) {
  return page.evaluate(({ theme }) => {
    const doc = document.documentElement;
    const body = document.body;
    const rootTextLength = body.innerText.trim().length;
    const app = document.querySelector("#app");
    const overflowX = Math.max(doc.scrollWidth, body.scrollWidth) - doc.clientWidth;
    const activeScreen = body.dataset.screen || "unknown";
    const clipped = [];
    const candidates = Array.from(document.querySelectorAll("a,button,h1,h2,h3,p,strong,span,b,dt,dd,label,input,select,textarea"));

    for (const el of candidates) {
      const box = el.getBoundingClientRect();
      const style = getComputedStyle(el);
      if (box.width <= 0 || box.height <= 0 || style.visibility === "hidden" || style.display === "none") continue;
      const text = (el.innerText || el.value || el.getAttribute("aria-label") || "").trim();
      if (!text) continue;
      if (el.scrollWidth > el.clientWidth + 2 || el.scrollHeight > el.clientHeight + 2) {
        clipped.push({
          tag: el.tagName.toLowerCase(),
          className: el.className || "",
          text: text.slice(0, 80),
          clientWidth: el.clientWidth,
          scrollWidth: el.scrollWidth,
          clientHeight: el.clientHeight,
          scrollHeight: el.scrollHeight,
        });
      }
    }

    const darkSurfaceWarnings = [];
    if (theme === "dark") {
      const bodyBackground = getComputedStyle(body).backgroundColor;
      const bodyBg = bodyBackground;
      const surfaces = Array.from(document.querySelectorAll(".page-hero,.card,.panel,.table-card,.state-card,.community-rail,.community-board,.mypage-profile-panel,.mypage-card,.mypage-calendar-panel,.mypage-split-card,.mypage-community-panel,.settings-dialog,.roadmap-board,.status-panel"));
      for (const el of surfaces) {
        const style = getComputedStyle(el);
        const background = style.backgroundColor;
        const border = style.borderColor;
        if (!background || background === "rgba(0, 0, 0, 0)" || background === "transparent") continue;
        darkSurfaceWarnings.push({
          selector: `${el.tagName.toLowerCase()}.${String(el.className || "").split(/\s+/).filter(Boolean).slice(0, 3).join(".")}`,
          background,
          border,
          bodyBackground: bodyBg,
        });
      }
    }

    const links = Array.from(document.querySelectorAll("a[href]")).map((a) => ({
      text: (a.innerText || a.getAttribute("aria-label") || "").trim().slice(0, 80),
      href: a.href,
      raw: a.getAttribute("href"),
    }));
    const images = Array.from(document.querySelectorAll("img[src]")).map((img) => ({
      className: img.className || "",
      src: img.currentSrc || img.src,
      raw: img.getAttribute("src"),
      complete: img.complete,
      naturalWidth: img.naturalWidth,
      naturalHeight: img.naturalHeight,
    }));

    return {
      title: document.title,
      activeScreen,
      rootTextLength,
      appChildren: app ? app.children.length : 0,
      height: Math.max(body.scrollHeight, doc.scrollHeight),
      overflowX,
      clipped,
      darkSurfaceWarnings,
      links,
      images,
    };
  }, { theme });
}

async function audit() {
  fs.mkdirSync(screenshotRoot, { recursive: true });
  const files = screenFiles();
  const report = {
    generatedAt: new Date().toISOString(),
    baseUrl,
    counts: {
      screenHtml: files.length,
      componentHtml: walk(path.join(uiRoot, "common/components")).filter((file) => file.endsWith(".html")).length,
    },
    pages: [],
    linkChecks: [],
    failures: [],
  };

  const browser = await chromium.launch({
    executablePath: chromePath,
    headless: true,
  });

  for (const file of files) {
    const relativePath = rel(file);
    const viewports = [desktop];
    if (keyMobileScreens.has(relativePath)) viewports.push(mobile);

    for (const viewport of viewports) {
      for (const theme of ["light", "dark"]) {
        const context = await browser.newContext({
          viewport: { width: viewport.width, height: viewport.height },
          deviceScaleFactor: 1,
        });
        await context.addInitScript((mode) => {
          localStorage.setItem("knowva-ui-theme", mode);
        }, theme);

        const page = await context.newPage();
        const consoleErrors = [];
        const pageErrors = [];
        page.on("console", (msg) => {
          if (["error", "warning"].includes(msg.type())) {
            consoleErrors.push(`${msg.type()}: ${msg.text()}`);
          }
        });
        page.on("pageerror", (error) => {
          pageErrors.push(error.message);
        });

        const url = toUrl(relativePath);
        const response = await page.goto(url, { waitUntil: "networkidle" });
        await page.waitForSelector("#app > *", { timeout: 5000 });
        const snapshot = await collectPageIssues(page, theme);
        const screenshotName = `${slug(relativePath)}__${viewport.name}__${theme}.png`;
        await page.screenshot({
          path: path.join(screenshotRoot, screenshotName),
          fullPage: true,
        });

        if (!response || response.status() >= 400) {
          report.failures.push({ type: "http", relativePath, viewport: viewport.name, theme, status: response && response.status() });
        }
        if (snapshot.rootTextLength < 120 || snapshot.appChildren === 0) {
          report.failures.push({ type: "blank_or_too_sparse", relativePath, viewport: viewport.name, theme, snapshot });
        }
        if (snapshot.overflowX > 2) {
          report.failures.push({ type: "horizontal_overflow", relativePath, viewport: viewport.name, theme, overflowX: snapshot.overflowX });
        }
        const brokenImages = snapshot.images.filter((img) => !img.complete || img.naturalWidth <= 0 || img.naturalHeight <= 0);
        if (brokenImages.length > 0) {
          report.failures.push({ type: "broken_image", relativePath, viewport: viewport.name, theme, brokenImages });
        }
        if (snapshot.clipped.length > 0) {
          report.failures.push({ type: "clipped_text", relativePath, viewport: viewport.name, theme, clipped: snapshot.clipped.slice(0, 12) });
        }
        if (consoleErrors.length || pageErrors.length) {
          report.failures.push({ type: "runtime_error", relativePath, viewport: viewport.name, theme, consoleErrors, pageErrors });
        }

        report.pages.push({
          relativePath,
          url,
          viewport: viewport.name,
          theme,
          httpStatus: response ? response.status() : null,
          screenshot: path.relative(repoRoot, path.join(screenshotRoot, screenshotName)),
          title: snapshot.title,
          activeScreen: snapshot.activeScreen,
          rootTextLength: snapshot.rootTextLength,
          height: snapshot.height,
          overflowX: snapshot.overflowX,
          clippedCount: snapshot.clipped.length,
          consoleErrors,
          pageErrors,
          darkSurfaceSample: snapshot.darkSurfaceWarnings.slice(0, 16),
          links: snapshot.links,
          images: snapshot.images,
        });

        if (viewport.name === "desktop" && theme === "light") {
          const uniqueLinks = new Map();
          for (const link of snapshot.links) {
            if (!link.href.startsWith(baseUrl.split("/ui-html2")[0])) continue;
            const linkUrl = new URL(link.href);
            linkUrl.hash = "";
            const normalized = linkUrl.toString();
            if (!uniqueLinks.has(normalized)) uniqueLinks.set(normalized, link);
          }

          for (const [normalized, link] of uniqueLinks) {
            const linkResponse = await page.request.get(normalized);
            const status = linkResponse.status();
            report.linkChecks.push({ source: relativePath, text: link.text, raw: link.raw, url: normalized, status });
            if (status >= 400) {
              report.failures.push({ type: "broken_link", source: relativePath, text: link.text, raw: link.raw, url: normalized, status });
            }
          }
        }

        await context.close();
      }
    }
  }

  await browser.close();

  const summary = {
    generatedAt: report.generatedAt,
    counts: report.counts,
    pageRuns: report.pages.length,
    linkChecks: report.linkChecks.length,
    failures: report.failures,
  };

  fs.writeFileSync(path.join(outputRoot, "ui-audit-report.json"), `${JSON.stringify(report, null, 2)}\n`);
  fs.writeFileSync(path.join(outputRoot, "ui-audit-summary.json"), `${JSON.stringify(summary, null, 2)}\n`);
  console.log(JSON.stringify(summary, null, 2));
}

audit().catch((error) => {
  console.error(error);
  process.exit(1);
});
