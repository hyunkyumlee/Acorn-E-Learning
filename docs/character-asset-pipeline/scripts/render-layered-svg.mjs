#!/usr/bin/env node
import { createRequire } from "node:module";
import { existsSync, mkdirSync, readFileSync, rmSync, writeFileSync } from "node:fs";
import { homedir } from "node:os";
import { dirname, join, resolve } from "node:path";
import { spawnSync } from "node:child_process";

const require = createRequire(import.meta.url);

function parseArgs(argv) {
  const args = {
    frames: 72,
    fps: 24,
    size: 512,
    spriteEvery: 6,
    keepFrames: false,
  };

  for (let index = 2; index < argv.length; index += 1) {
    const token = argv[index];
    const next = argv[index + 1];
    if (token === "--source" && next) {
      args.source = next;
      index += 1;
    } else if (token === "--out" && next) {
      args.out = next;
      index += 1;
    } else if (token === "--slug" && next) {
      args.slug = next;
      index += 1;
    } else if (token === "--frames" && next) {
      args.frames = Number.parseInt(next, 10);
      index += 1;
    } else if (token === "--fps" && next) {
      args.fps = Number.parseInt(next, 10);
      index += 1;
    } else if (token === "--size" && next) {
      args.size = Number.parseInt(next, 10);
      index += 1;
    } else if (token === "--sprite-every" && next) {
      args.spriteEvery = Number.parseInt(next, 10);
      index += 1;
    } else if (token === "--keep-frames") {
      args.keepFrames = true;
    } else if (token === "--help") {
      printHelp();
      process.exit(0);
    } else {
      throw new Error(`Unknown or incomplete argument: ${token}`);
    }
  }

  if (!args.source || !args.out || !args.slug) {
    printHelp();
    throw new Error("Missing required --source, --out, or --slug");
  }
  if (!Number.isFinite(args.frames) || args.frames < 12) throw new Error("--frames must be >= 12");
  if (!Number.isFinite(args.fps) || args.fps < 1) throw new Error("--fps must be >= 1");
  if (!Number.isFinite(args.size) || args.size < 64) throw new Error("--size must be >= 64");
  if (!Number.isFinite(args.spriteEvery) || args.spriteEvery < 1) throw new Error("--sprite-every must be >= 1");

  return args;
}

function printHelp() {
  console.log(`Usage:
node render-layered-svg.mjs --source mascot-source.svg --out exports --slug mascot

Options:
  --frames 72          Number of animation frames
  --fps 24             Output animation frame rate
  --size 512           Square render size
  --sprite-every 6     Use every Nth frame in sprite strip
  --keep-frames        Keep temporary PNG frames`);
}

function loadPlaywright() {
  const candidates = [
    process.env.PLAYWRIGHT_NODE_MODULES,
    process.env.CODEX_NODE_MODULES,
    join(homedir(), ".cache/codex-runtimes/codex-primary-runtime/dependencies/node/node_modules"),
    join(process.cwd(), "node_modules"),
  ].filter(Boolean);

  try {
    return require("playwright");
  } catch {
    for (const candidate of candidates) {
      try {
        return require(join(candidate, "playwright"));
      } catch {
        // try next candidate
      }
    }
  }

  throw new Error("Cannot find playwright. In Codex Desktop, call load_workspace_dependencies and use the bundled Node modules.");
}

function run(command, args) {
  const result = spawnSync(command, args, { encoding: "utf8" });
  if (result.status !== 0) {
    const stderr = result.stderr || result.stdout || "no output";
    throw new Error(`${command} failed: ${stderr}`);
  }
}

function setGroupAttribute(svg, id, name, value) {
  const pattern = new RegExp(`(<g\\\\s+id=["']${id}["'][^>]*)(>)`);
  return svg.replace(pattern, (_match, start, end) => {
    const attrPattern = new RegExp(`\\\\s${name}=["'][^"']*["']`);
    const clean = start.replace(attrPattern, "");
    return `${clean} ${name}="${value}"${end}`;
  });
}

function readNumber(svg, id, attribute, fallback) {
  const pattern = new RegExp(`id=["']${id}["'][^>]*\\\\s${attribute}=["']([^"']+)["']`);
  const match = svg.match(pattern);
  if (!match) return fallback;
  const value = Number.parseFloat(match[1]);
  return Number.isFinite(value) ? value : fallback;
}

function withFrameTransforms(source, index, total) {
  const p = index / total;
  const theta = p * Math.PI * 2;
  const floatY = Math.sin(theta) * -10;
  const shadowScale = 1 + Math.sin(theta) * 0.04;
  const bodyScaleY = 1 + Math.sin(theta + Math.PI / 2) * 0.025;
  const bodyScaleX = 1 - Math.sin(theta + Math.PI / 2) * 0.014;
  const leftAnt = -15 + Math.sin(theta + 0.5) * 7;
  const rightAnt = 15 + Math.sin(theta + 1.8) * 7;
  const leftArm = 26 + Math.sin(theta + 0.4) * 8;
  const rightArm = -26 + Math.sin(theta + 2.0) * 8;
  const starScale = 1 + Math.sin(theta * 2) * 0.08;
  const blinkDistance = Math.min(Math.abs(p - 0.22), Math.abs(p - 0.66), Math.abs(p - 1.22));
  const eyeScale = blinkDistance < 0.022 ? 0.18 : 1;
  const cheekOpacity = 0.9 + Math.max(0, Math.sin(theta * 2)) * 0.1;
  const leftEyeX = readNumber(source, "left-eye-white", "cx", 202);
  const leftEyeY = readNumber(source, "left-eye-white", "cy", 247);
  const rightEyeX = readNumber(source, "right-eye-white", "cx", 310);
  const rightEyeY = readNumber(source, "right-eye-white", "cy", 247);

  let svg = source;
  svg = setGroupAttribute(svg, "shadow", "transform", `translate(0 ${-floatY * 0.25}) scale(${shadowScale} 1)`);
  svg = setGroupAttribute(svg, "mascot", "transform", `translate(0 ${floatY})`);
  svg = setGroupAttribute(svg, "left-antenna", "transform", `rotate(${leftAnt} 184 148)`);
  svg = setGroupAttribute(svg, "right-antenna", "transform", `rotate(${rightAnt} 328 148)`);
  svg = setGroupAttribute(svg, "body", "transform", `translate(256 276) scale(${bodyScaleX} ${bodyScaleY}) translate(-256 -276)`);
  svg = setGroupAttribute(svg, "left-arm", "transform", `rotate(${leftArm} 125 304)`);
  svg = setGroupAttribute(svg, "right-arm", "transform", `rotate(${rightArm} 387 304)`);
  svg = setGroupAttribute(svg, "left-eye", "transform", `translate(${leftEyeX} ${leftEyeY}) scale(1 ${eyeScale}) translate(${-leftEyeX} ${-leftEyeY})`);
  svg = setGroupAttribute(svg, "right-eye", "transform", `translate(${rightEyeX} ${rightEyeY}) scale(1 ${eyeScale}) translate(${-rightEyeX} ${-rightEyeY})`);
  svg = setGroupAttribute(svg, "cheeks", "opacity", cheekOpacity.toFixed(3));
  svg = setGroupAttribute(svg, "star-badge", "transform", `translate(256 392) scale(${starScale}) translate(-256 -392)`);
  return svg;
}

async function renderFrames({ source, out, slug, frames, size }) {
  const { chromium } = loadPlaywright();
  const browser = await chromium.launch({
    headless: true,
    executablePath: existsSync("/Applications/Google Chrome.app/Contents/MacOS/Google Chrome")
      ? "/Applications/Google Chrome.app/Contents/MacOS/Google Chrome"
      : undefined,
  });
  const page = await browser.newPage({ viewport: { width: size, height: size }, deviceScaleFactor: 1 });
  await page.setContent(`<canvas id="c" width="${size}" height="${size}"></canvas>`);

  const sourceSvg = readFileSync(source, "utf8");
  const framesDir = join(out, "tmp-idle-frames");
  rmSync(framesDir, { recursive: true, force: true });
  mkdirSync(framesDir, { recursive: true });

  for (let frame = 0; frame < frames; frame += 1) {
    const svg = withFrameTransforms(sourceSvg, frame, frames);
    const dataUrl = `data:image/svg+xml;base64,${Buffer.from(svg).toString("base64")}`;
    const pngBase64 = await page.evaluate(async ({ url, renderSize }) => {
      const canvas = document.getElementById("c");
      const ctx = canvas.getContext("2d");
      ctx.clearRect(0, 0, renderSize, renderSize);
      const img = new Image();
      img.src = url;
      await img.decode();
      ctx.drawImage(img, 0, 0, renderSize, renderSize);
      return canvas.toDataURL("image/png").split(",")[1];
    }, { url: dataUrl, renderSize: size });
    writeFileSync(join(framesDir, `frame_${String(frame).padStart(3, "0")}.png`), Buffer.from(pngBase64, "base64"));
  }

  writeFileSync(join(out, `${slug}-poster.png`), readFileSync(join(framesDir, "frame_000.png")));
  await browser.close();
  return framesDir;
}

function encodeOutputs({ framesDir, out, slug, fps, size, spriteEvery, keepFrames }) {
  const inputPattern = join(framesDir, "frame_%03d.png");
  run("ffmpeg", ["-y", "-framerate", String(fps), "-i", inputPattern, "-plays", "0", join(out, `${slug}-idle.apng`)]);
  run("ffmpeg", ["-y", "-framerate", String(fps), "-i", inputPattern, "-c:v", "libvpx-vp9", "-pix_fmt", "yuva420p", "-auto-alt-ref", "0", "-b:v", "0", "-crf", "32", join(out, `${slug}-idle.webm`)]);
  run("ffmpeg", ["-y", "-framerate", String(fps), "-i", inputPattern, "-vf", `fps=${fps},scale=${size}:-1:flags=lanczos,split[s0][s1];[s0]palettegen=reserve_transparent=1[p];[s1][p]paletteuse=alpha_threshold=128`, "-loop", "0", join(out, `${slug}-idle.gif`)]);
  run("ffmpeg", ["-y", "-framerate", String(fps), "-i", inputPattern, "-vf", `select=not(mod(n\\,${spriteEvery})),scale=160:160,tile=12x1`, "-frames:v", "1", "-update", "1", join(out, `${slug}-idle-sprite.png`)]);
  if (!keepFrames) rmSync(framesDir, { recursive: true, force: true });
}

async function main() {
  const args = parseArgs(process.argv);
  const source = resolve(args.source);
  const out = resolve(args.out);
  if (!existsSync(source)) throw new Error(`Source SVG not found: ${source}`);
  mkdirSync(out, { recursive: true });
  const framesDir = await renderFrames({ ...args, source, out });
  encodeOutputs({ ...args, framesDir, out });
  console.log(JSON.stringify({
    source,
    out,
    files: [
      join(out, `${args.slug}-poster.png`),
      join(out, `${args.slug}-idle.webm`),
      join(out, `${args.slug}-idle.apng`),
      join(out, `${args.slug}-idle.gif`),
      join(out, `${args.slug}-idle-sprite.png`),
    ],
  }, null, 2));
}

main().catch((error) => {
  console.error(error.message);
  process.exit(1);
});
