/* =========================================================================
   learning-nuvi.js — 로드맵 누비: 완료한 행성 → 다음 행성 비행 연출 (스프라이트 판)
   - 로드맵에 누비를 상주시키지 않는다. 축하 연출이 닫히는 순간(knowva:celebration-closed)
     딱 한 번 등장 → 비행 → 다음 행성/관문 안으로 빛과 함께 흡수되고 끝난다.
   - 두 가지 등장:
       hop   : 행성을 하나 끝냈다. 방금 끝낸 행성 속에서 빛과 함께 솟아 다음 행성으로 날아가,
               도착해 놀라고 둘러보고 감탄한 뒤 그 안으로 흡수된다.
       level : 관문을 넘어 새 판이 열렸다. 관문 빛 포털로 빨려들어 → 새 로드맵/행성이 열리며
               카메라가 전체를 보여줬다(줌아웃) 첫 행성으로 다가가(줌인) 흡수된다.
   - 누비 자체 = 두 벌의 스프라이트로 그린다.
       cruise strip(가로 8프레임, 좌/우) : 실제로 빠르게 날 때. 속도로 안테나가 뒤로 젖는다.
       pose atlas (4×2 반응 포즈, 좌/우) : 멈춤·점화·감속·놀람·감탄·정지·입장 등 정지/리액션.
     둘은 같은 순간에 겹치지 않게 opacity를 교차한다.
   - 좌표·크기·기울기·빛·프레임은 전부 여기서 매 프레임 준다. CSS엔 시간이 없다(두 곳 길이 어긋남 방지).
   - 맵은 세로로 길어 두 행성이 한 화면에 안 들어온다 → hop은 stage를 따라 스크롤(카메라 추적),
     level은 .roadmap-layer를 transform으로 줌한다. 로드맵의 관성 스크롤과 다투지 않게
     knowva:roadmap-camera-taken으로 카메라를 넘겨받고, 끝나면 반납한다.
   - prefers-reduced-motion: reduce면 연출을 아예 실행하지 않는다(움직임이 곧 내용이라 정지본은 무의미).
   ========================================================================= */
(function () {
  "use strict";

  // ── 전체 길이(ms). 샘플과 동일한 리듬. ─────────────────────────────
  var ROUTE_TOTAL_MS = 6360; // hop: 솟음~흡수 전체
  var LEVEL_TOTAL_MS = 3800; // level: 새 로드맵 첫 행성에서 등장~첫 행성 흡수

  // cruise strip 프레임별 노출 시간(마지막 한 장만 길게 = 착지 직전 여운)
  var FRAME_TIMINGS = [115, 115, 115, 115, 115, 115, 115, 150];
  var FRAME_CYCLE = FRAME_TIMINGS.reduce(function (s, v) { return s + v; }, 0);

  // pose atlas 각 포즈의 알파 중심을 같은 anchor로 맞추는 보정(512 셀 기준 px). 안 맞추면 포즈가 튄다.
  var POSE_OFFSETS = [
    [16, 17.5], [2.5, -38.5], [9, -34], [-23, 15.5],
    [19, -6], [9.5, 33], [23, 17], [-8, -5]
  ];

  function clamp(v, a, b) { return Math.max(a, Math.min(b, v)); }
  function lerp(a, b, t) { return a + (b - a) * t; }
  function lerpP(p, q, t) { return { x: p.x + (q.x - p.x) * t, y: p.y + (q.y - p.y) * t }; }
  function easeInCubic(t) { return t * t * t; }
  function easeOutCubic(t) { return 1 - Math.pow(1 - t, 3); }
  function easeOutQuint(t) { return 1 - Math.pow(1 - t, 5); }
  function easeInOutCubic(t) { return t < 0.5 ? 4 * t * t * t : 1 - Math.pow(-2 * t + 2, 3) / 2; }
  function easeOutBack(t) {
    var c = 1.45;
    return 1 + (c + 1) * Math.pow(t - 1, 3) + c * Math.pow(t - 1, 2);
  }
  /** 반짝임 한 번. c에서 정점, 폭 w 밖은 0. 좁게 튀었다 0으로 떨어져야 "번쩍"으로 읽힌다. */
  function twinkle(t, c, w) {
    var d = (t - c) / w;
    if (d <= -1 || d >= 1) { return 0; }
    var x = 1 - d * d;
    return x * x;
  }
  /** 3차 베지에 값과 미분(한 축). */
  function cubic(a, b, c, d, t) {
    var u = 1 - t;
    return u * u * u * a + 3 * u * u * t * b + 3 * u * t * t * c + t * t * t * d;
  }
  function slope(a, b, c, d, t) {
    var u = 1 - t;
    return 3 * u * u * (b - a) + 6 * u * t * (c - b) + 3 * t * t * (d - c);
  }

  function init() {
    var nuvi = document.querySelector("[data-roadmap-nuvi]");
    var pose = nuvi && nuvi.querySelector("[data-nuvi-pose]");
    var cruiseClip = nuvi && nuvi.querySelector("[data-nuvi-cruise-clip]");
    var cruiseStrip = nuvi && nuvi.querySelector("[data-nuvi-cruise-strip]");
    var layer = nuvi && nuvi.closest(".roadmap-layer");
    var stage = nuvi && nuvi.closest("[data-roadmap-stage]");
    if (!nuvi || !pose || !cruiseClip || !cruiseStrip || !layer || !stage) {
      return;
    }

    if (
      window.matchMedia &&
      window.matchMedia("(prefers-reduced-motion: reduce)").matches
    ) {
      return;
    }

    // 좌/우 스프라이트 경로(마크업 data-*). 하나라도 없으면 연출 자체를 뺀다.
    var POSE = { right: nuvi.dataset.nuviPoseRight, left: nuvi.dataset.nuviPoseLeft };
    var CRUISE = { right: nuvi.dataset.nuviCruiseRight, left: nuvi.dataset.nuviCruiseLeft };
    if (!POSE.right || !POSE.left || !CRUISE.right || !CRUISE.left) {
      return;
    }
    // 비행 중 처음 불러오면 한 프레임 빈다 → 미리 받아둔다.
    [POSE.left, CRUISE.right, CRUISE.left].forEach(function (src) {
      var pre = new Image();
      pre.src = src;
    });

    // 포털/흡수 빛 필드는 노드와 같은 좌표계(layer-local)에 얹어야 하므로 layer의 자식으로 만든다.
    var levelGate = document.createElement("div");
    levelGate.className = "roadmap-nuvi-gate";
    levelGate.setAttribute("aria-hidden", "true");
    var absorb = document.createElement("div");
    absorb.className = "roadmap-nuvi-absorb";
    absorb.setAttribute("aria-hidden", "true");
    absorb.innerHTML =
      '<span class="rn-absorb-halo"></span>' +
      '<span class="rn-absorb-ring rn-absorb-ring-a"></span>' +
      '<span class="rn-absorb-ring rn-absorb-ring-b"></span>' +
      '<span class="rn-absorb-rays"></span>' +
      '<span class="rn-absorb-core"></span>' +
      '<span class="rn-absorb-motes"></span>';
    layer.insertBefore(levelGate, nuvi);
    layer.insertBefore(absorb, nuvi);

    // ── 목적지/출발점 찾기(현행 로직 유지) ───────────────────────────
    /** 목적지 = 지금 학습할 행성. 없으면 응시 가능해진 관문/첫 open/이미 통과한 관문. */
    function findTarget() {
      return (
        layer.querySelector(".roadmap-node.is-current") ||
        layer.querySelector(".roadmap-node.is-gate.is-ready") ||
        layer.querySelector(".roadmap-node.is-open") ||
        layer.querySelector(".roadmap-node.is-gate.is-passed")
      );
    }
    /** 출발점 = 목적지 바로 앞의 완료한 행성(= 방금 끝낸 행성). */
    function findOrigin(target) {
      var node = target.previousElementSibling;
      while (node) {
        if (node.classList.contains("is-done")) { return node; }
        node = node.previousElementSibling;
      }
      return null;
    }
    /** 노드의 행성/관문 그림 중심을 맵 레이어 좌표(px)로. offset만 쓰는 이유:
        노드엔 중심정렬 translate와 호버 확대가 걸려 getBoundingClientRect가 그 변형까지 섞기 때문. */
    function center(node) {
      var art = node.querySelector(".roadmap-planet") || node;
      return {
        x: node.offsetLeft - node.offsetWidth / 2 + art.offsetLeft + art.offsetWidth / 2,
        y: node.offsetTop - node.offsetHeight / 2 + art.offsetTop + art.offsetHeight / 2,
        w: art.offsetWidth,
        h: art.offsetHeight
      };
    }

    // ── 상태 ─────────────────────────────────────────────────────────
    var size = 96;          // 누비 한 변 px(목적지 크기로 run()이 정한다)
    var camMargin = 96;
    var cameraOwned = false;
    var aborted = false;
    var worldScale = 1;     // level 줌 중 layer transform scale(hop=1). draw()가 크기를 이 값으로 나눈다.
    var geometry = null;
    var layerWidth = 0;
    var animationId = 0;
    var currentPhase = "";
    var currentDrawY = 0;   // 방금 그린 누비의 세로 좌표(layer-local) — hop 카메라 추적 기준

    function phase(name) {
      if (name) { nuvi.dataset.nuviPhase = name; return; }
      delete nuvi.dataset.nuviPhase;
    }
    function setPhase(text) {
      if (text === currentPhase) { return; }
      currentPhase = text;
      phase(text);
    }

    // ── 스프라이트 프레임 ────────────────────────────────────────────
    /** pose atlas(4×2, 512 셀) 한 장을 그린다. opacity>0이면 cruise는 draw에서 끈다. */
    function setPoseFrame(index, opacity, direction) {
      var col = index % 4;
      var row = Math.floor(index / 4);
      var off = POSE_OFFSETS[index];
      var xOff = direction === "left" ? -off[0] : off[0];
      pose.style.backgroundImage = "url('" + (direction === "left" ? POSE.left : POSE.right) + "')";
      pose.style.backgroundPosition = (col * 100 / 3) + "% " + (row * 100) + "%";
      pose.style.opacity = String(opacity);
      pose.style.transform =
        "translate3d(" + (xOff / 512 * 100) + "%," + (off[1] / 512 * 100) + "%,0)";
    }
    function spriteFrameAt(time) {
      var cursor = ((time % FRAME_CYCLE) + FRAME_CYCLE) % FRAME_CYCLE;
      var elapsed = 0;
      for (var i = 0; i < FRAME_TIMINGS.length; i += 1) {
        elapsed += FRAME_TIMINGS[i];
        if (cursor < elapsed) { return i; }
      }
      return 7;
    }
    /** cruise strip(가로 8프레임) 한 칸을 clip 창에 맞춰 밀어 보여준다. */
    function setCruiseFrame(direction, time, opacity) {
      var frame = spriteFrameAt(time);
      cruiseStrip.src = direction === "left" ? CRUISE.left : CRUISE.right;
      cruiseStrip.style.transform = "translate3d(" + (-frame * 12.5) + "%,0,0)";
      cruiseClip.style.opacity = String(opacity);
    }
    function hideCruise() { cruiseClip.style.opacity = "0"; }

    // ── 몸통 배치 + 빛 ───────────────────────────────────────────────
    /** 누비를 layer-local 좌표(x,y)에 중심 맞춰 얹는다. level 줌 중이면 크기를 worldScale로 나눠
        화면상 크기를 유지한다(위치는 layer가 함께 스케일하므로 노드와 어긋나지 않는다). */
    function draw(x, y, tilt, scale, thrust, brake, flash, trailAngle, opacity) {
      currentDrawY = y;
      var s = scale / worldScale;
      nuvi.style.transform =
        "translate3d(" + (x - size / 2) + "px," + (y - size / 2) + "px,0) " +
        "rotate(" + tilt + "deg) scale(" + s + ")";
      nuvi.style.setProperty("--nuvi-thrust", String(thrust || 0));
      nuvi.style.setProperty("--nuvi-brake", String(brake || 0));
      nuvi.style.setProperty("--nuvi-flash", String(flash || 0));
      nuvi.style.setProperty("--nuvi-trail", (trailAngle === undefined ? 155 : trailAngle) + "deg");
      nuvi.style.opacity = String(opacity === undefined ? 1 : opacity);
    }
    /** 흡수/솟음 순간의 몸통 발광(밝기·채도·번짐·그림자). */
    function travellerLight(glow, progress) {
      var g = clamp(glow, 0, 1);
      var p = clamp(progress || 0, 0, 1);
      nuvi.style.setProperty("--nuvi-brightness", String(1 + g * 0.54 + p * 0.12));
      nuvi.style.setProperty("--nuvi-saturate", String(1 + g * 0.22));
      nuvi.style.setProperty("--nuvi-soft", (Math.max(0, p - 0.86) * 6) + "px");
      nuvi.style.setProperty("--nuvi-shadow", (g * 24) + "px");
    }

    /** 흡수 빛 필드(halo/core/rays/motes/2ring)를 point(layer 좌표)에 지름 diameter로 그린다.
        p=진행(0~1), spark=정점 펄스. 여섯 겹이 서로 다른 박자로 튀어 얼룩이 아니라 반짝임이 된다. */
    function setAbsorb(active, point, diameter, p, spark) {
      if (!active) {
        absorb.style.setProperty("--rn-op", "0");
        travellerLight(0, 0);
        return;
      }
      var u = clamp(p, 0, 1);
      var pulse = clamp(spark, 0, 1);
      var ending = 1 - clamp((u - 0.91) / 0.09, 0, 1);
      var halo = clamp(0.16 + u * 0.28 + pulse * 0.68, 0, 1) * ending;
      var core = Math.pow(pulse, 2) * ending;
      var rays = Math.pow(pulse, 0.65) * ending;
      var motes = Math.pow(pulse, 3) * ending;
      var ring = clamp(twinkle(u, 0.62, 0.55) + pulse * 0.28, 0, 1) * ending;
      var ringScale = 0.56 + easeOutCubic(u) * 0.64;
      var opacity = clamp(Math.max(halo, ring, pulse * 0.84), 0, 1);
      var d = diameter / worldScale; // 줌 중에도 화면상 크기 유지
      absorb.style.width = d + "px";
      absorb.style.height = d + "px";
      absorb.style.transform =
        "translate3d(" + (point.x - d / 2) + "px," + (point.y - d / 2) + "px,0)";
      absorb.style.setProperty("--rn-op", String(opacity));
      absorb.style.setProperty("--rn-halo", String(halo));
      absorb.style.setProperty("--rn-core", String(core));
      absorb.style.setProperty("--rn-rays", String(rays));
      absorb.style.setProperty("--rn-motes", String(motes));
      absorb.style.setProperty("--rn-ring", String(ring));
      absorb.style.setProperty("--rn-ring-scale", String(ringScale));
      absorb.style.setProperty("--rn-halo-scale", String(0.72 + ringScale * 0.28));
      absorb.style.setProperty("--rn-core-scale", String(0.46 + core * 0.84));
      absorb.style.setProperty("--rn-ring-b-scale", String(ringScale * 0.82));
      absorb.style.setProperty("--rn-motes-scale", String(0.62 + ringScale * 0.38));
      absorb.style.setProperty("--rn-spin", (u * 76) + "deg");
      absorb.style.setProperty("--rn-spin-rev", (-u * 53) + "deg");
      travellerLight(pulse, u);
    }

    /** 관문 빛 포털을 point에 그린다(level 포털 구간). */
    function setGate(point, sizePx, opacity, scale, glow, spin) {
      var d = sizePx / worldScale;
      levelGate.style.width = d + "px";
      levelGate.style.height = d + "px";
      levelGate.style.transform =
        "translate3d(" + (point.x - d / 2) + "px," + (point.y - d / 2) + "px,0) scale(" + scale + ")";
      levelGate.style.setProperty("--rn-gate-op", String(opacity));
      levelGate.style.setProperty("--rn-gate-glow", String(glow));
      levelGate.style.setProperty("--rn-gate-spin", spin + "deg");
    }

    // ── 카메라 ───────────────────────────────────────────────────────
    /** hop: 누비를 stage 한가운데로 데려오게 stage(필요하면 페이지)를 스크롤한다. */
    function camera(y) {
      if (!cameraOwned) { return; }
      var max = stage.scrollHeight - stage.clientHeight;
      var top = layer.offsetTop + y - stage.clientHeight / 2;
      stage.scrollTop = Math.max(0, Math.min(top, max));
      var vy = stage.getBoundingClientRect().top + (layer.offsetTop + y - stage.scrollTop);
      var over = 0;
      if (vy > window.innerHeight - camMargin) {
        over = vy - (window.innerHeight - camMargin);
      } else if (vy < camMargin) {
        over = vy - camMargin;
      }
      if (over) { window.scrollBy(0, over); }
    }
    /** level: layer 자체를 scale+translateY로 줌한다(stage는 위로 고정). */
    function setLayerView(scale, offsetY) {
      worldScale = scale;
      stage.scrollTop = 0;
      layer.style.transformOrigin = "50% 0";
      layer.style.transform = "translate3d(0," + offsetY + "px,0) scale(" + scale + ")";
    }
    /** level: 노드들이 blur→선명, 흐릿→또렷하게 순서대로 열린다. */
    function setNodeReveal(progress) {
      var nodes = layer.querySelectorAll(".roadmap-node");
      for (var i = 0; i < nodes.length; i += 1) {
        var local = clamp((progress * 1.55) - i * 0.11, 0, 1);
        var eased = easeOutCubic(local);
        nodes[i].style.setProperty("--intro-op", String(eased));
        nodes[i].style.setProperty("--intro-blur", (8 * (1 - eased)) + "px");
      }
    }
    function clearNodeReveal() {
      var nodes = layer.querySelectorAll(".roadmap-node");
      for (var i = 0; i < nodes.length; i += 1) {
        nodes[i].style.removeProperty("--intro-op");
        nodes[i].style.removeProperty("--intro-blur");
      }
    }

    function releaseCamera() {
      if (!cameraOwned) { return; }
      cameraOwned = false;
      document.dispatchEvent(new CustomEvent("knowva:roadmap-camera-released"));
    }
    document.addEventListener("knowva:roadmap-camera-released", function () {
      cameraOwned = false;
    });

    function hide() {
      phase(null);
      releaseCamera();
      worldScale = 1;
      nuvi.hidden = true;
      nuvi.classList.remove("is-visible");
      nuvi.style.transform = "";
      nuvi.style.opacity = "";
      hideCruise();
      setAbsorb(false);
      absorb.style.setProperty("--rn-op", "0");
      levelGate.style.setProperty("--rn-gate-op", "0");
      stage.classList.remove("is-level-entry");
      clearNodeReveal();
      // level 줌으로 남은 layer transform을 걷고, 목적지 행성이 가운데 오게 스크롤을 되돌린다.
      layer.style.transform = "";
      layer.style.transformOrigin = "";
      if (geometry && geometry.restoreY != null) {
        var max = stage.scrollHeight - stage.clientHeight;
        stage.scrollTop = clamp(geometry.restoreY - stage.clientHeight / 2, 0, Math.max(0, max));
      }
      ["thrust", "brake", "flash", "brightness", "saturate", "soft", "shadow"].forEach(function (k) {
        nuvi.style.removeProperty("--nuvi-" + k);
      });
    }

    /** 한 구간 rAF. onFrame(t): 0→1. aborted면 즉시 접는다. */
    function tween(ms, onFrame, done) {
      var start = null;
      function step(now) {
        if (aborted) { hide(); return; }
        if (start === null) { start = now; }
        var t = Math.min(1, (now - start) / ms);
        onFrame(t);
        if (t < 1) { window.requestAnimationFrame(step); return; }
        done();
      }
      window.requestAnimationFrame(step);
    }

    // ── 경로(hop) 기하 ───────────────────────────────────────────────
    /** 두 점을 잇는 3차 베지에. 제어점 x를 양 끝과 같게 두면 접선이 수직 → 맵 S자 길처럼 휜다. */
    function buildRouteGeometry(origin, target) {
      var a = center(origin);
      var b = center(target);
      var dir = b.x < a.x ? -1 : 1;
      // 완료 행성 옆면으로 빠져나온 뒤(아트/배지 안 가림), 목적지 위에 닿았다가 그 안으로.
      var p0 = { x: a.x + dir * a.w * 0.5, y: a.y + a.h * 0.16 };
      var p3 = { x: b.x, y: b.y - b.h * 0.4 };
      var dy = (p3.y - p0.y) * 0.45;
      var gx = [p0.x, p0.x, p3.x, p3.x];
      var gy = [p0.y, p0.y + dy, p3.y - dy, p3.y];
      return {
        mode: "route",
        gate: target.classList.contains("is-gate"),
        direction: b.x < a.x ? "left" : "right",
        source: a,
        target: b,
        p0: p0,
        p3: p3,
        gx: gx,
        gy: gy,
        restoreY: b.y
      };
    }
    function routePoint(t) {
      var g = geometry;
      return { x: cubic(g.gx[0], g.gx[1], g.gx[2], g.gx[3], t), y: cubic(g.gy[0], g.gy[1], g.gy[2], g.gy[3], t) };
    }
    function routeAngle(t) {
      var g = geometry;
      var vx = slope(g.gx[0], g.gx[1], g.gx[2], g.gx[3], t);
      var vy = slope(g.gy[0], g.gy[1], g.gy[2], g.gy[3], t);
      return Math.atan2(vy, vx) * 180 / Math.PI;
    }
    function bank(t) {
      var g = geometry;
      var vx = slope(g.gx[0], g.gx[1], g.gx[2], g.gx[3], t);
      var vy = slope(g.gy[0], g.gy[1], g.gy[2], g.gy[3], t);
      return clamp(Math.atan2(vx, vy) * 180 / Math.PI * 0.18, -6, 6);
    }

    /** 흡수(솟음의 역). from→target으로 당겨지며 작아지고 빛으로 사라진다. */
    function renderAbsorption(progress, from, target, direction, isGate, startBank) {
      var u = clamp(progress, 0, 1);
      var pull = easeInCubic(u);
      var dx = target.x - from.x;
      var dy = target.y - from.y;
      var length = Math.max(1, Math.hypot(dx, dy));
      var normal = { x: -dy / length, y: dx / length };
      var arc = Math.sin(u * Math.PI) * (1 - u) * Math.min(10, length * 0.08);
      var point = {
        x: lerp(from.x, target.x, pull) + normal.x * arc,
        y: lerp(from.y, target.y, pull) + normal.y * arc
      };
      var spark = Math.max(0.45 * twinkle(u, 0.4, 0.18), twinkle(u, 0.72, 0.32));
      var endScale = isGate ? 0.06 : 0.12;
      var scale = 1 - (1 - endScale) * pull;
      var opacity = 1 - easeInCubic(clamp((u - 0.84) / 0.16, 0, 1));
      var spin = isGate ? 180 * pull : 0;
      var diameter = clamp(target.w * 1.2, 124, 280);
      var angle = Math.atan2(dy, dx) * 180 / Math.PI;
      setPoseFrame(7, 1, direction);
      hideCruise();
      setAbsorb(u < 1, target, diameter, u, spark);
      draw(point.x, point.y, lerp(startBank || 0, 0, pull) + spin, scale, 0.08 * (1 - u), 0, spark, angle + 180, opacity);
    }

    // ── 경로(hop) 타임라인 ───────────────────────────────────────────
    function renderRoute(elapsed) {
      var t = clamp(elapsed, 0, ROUTE_TOTAL_MS);
      var g = geometry;
      var point, angle, blend, e;

      if (t < 520) { // 출발 · 행성 중심에서 빛과 함께 솟아오름
        setPhase("emerge");
        var et = t / 520;
        var emerge = easeOutBack(et);
        var spark = Math.max(twinkle(et, 0.44, 0.34), 0.5 * twinkle(et, 0.76, 0.16));
        point = lerpP({ x: g.source.x, y: g.source.y }, g.p0, emerge);
        setAbsorb(et < 1, g.source, clamp(g.source.w * 1.2, 124, 280), et, spark);
        setPoseFrame(0, 1, g.direction);
        hideCruise();
        draw(point.x, point.y, bank(0) * emerge * 0.4, 0.12 + 0.88 * emerge, 0.06 * emerge, 0, spark, 155, clamp(et / 0.16, 0, 1));
        return;
      }
      if (t < 5480) { setAbsorb(false); }

      if (t < 780) { // 점화 · 뒤로 꾹
        setPhase("ignition");
        var ig = easeOutCubic((t - 520) / 260);
        point = routePoint(0.025 * ig);
        angle = routeAngle(0.02);
        setPoseFrame(1, 1, g.direction);
        hideCruise();
        draw(point.x, point.y, bank(0.02), 1, 0.3 + ig * 0.6, 0, 0.24 * twinkle(ig, 0.62, 0.5), angle + 180);
        return;
      }
      if (t < 1300) { // 출발 가속 · 쑤욱
        setPhase("launch");
        var lc = easeOutQuint((t - 780) / 520);
        var lt = 0.025 + lc * 0.175;
        point = routePoint(lt);
        angle = routeAngle(lt);
        setPoseFrame(2, 1, g.direction);
        hideCruise();
        draw(point.x, point.y, bank(lt), 1 + Math.sin(lc * Math.PI) * 0.025, 0.94, 0, 0, angle + 180);
        return;
      }
      if (t < 3300) { // 순항 · 속도에 안테나 뒤로 후욱 (cruise strip)
        setPhase("cruise");
        var cr = (t - 1300) / 2000;
        var rt = 0.2 + easeInOutCubic(cr) * 0.62;
        point = routePoint(rt);
        angle = routeAngle(rt);
        blend = clamp((t - 1300) / 120, 0, 1);
        setPoseFrame(2, 1 - blend, g.direction);
        setCruiseFrame(g.direction, t - 1300, blend);
        draw(point.x, point.y, bank(rt), 1, 0.72 + Math.sin(cr * Math.PI * 4) * 0.06, 0, 0, angle + 180);
        return;
      }
      if (t < 3840) { // 감속 · 끽—
        setPhase("brake");
        var br = (t - 3300) / 540;
        var start = routePoint(0.82);
        var tv = { x: slope(g.gx[0], g.gx[1], g.gx[2], g.gx[3], 1), y: slope(g.gy[0], g.gy[1], g.gy[2], g.gy[3], 1) };
        var tl = Math.max(1, Math.hypot(tv.x, tv.y));
        var un = { x: tv.x / tl, y: tv.y / tl };
        var over = { x: g.p3.x + un.x * 14, y: g.p3.y + un.y * 14 };
        point = lerpP(start, over, easeOutQuint(br));
        angle = Math.atan2(tv.y, tv.x) * 180 / Math.PI;
        blend = clamp((t - 3300) / 130, 0, 1);
        setCruiseFrame(g.direction, 2000 + (t - 3300), 1 - blend);
        setPoseFrame(5, blend, g.direction);
        draw(point.x, point.y, bank(lerp(0.82, 1, br)) + Math.sin(br * Math.PI) * 2.2, 1 + Math.sin(br * Math.PI) * 0.025, Math.max(0, 0.45 - br * 0.5), Math.sin(br * Math.PI * 0.94), 0, angle + 180);
        return;
      }
      if (t < 4260) { // 반동 · 톡
        setPhase("recoil");
        var rc = (t - 3840) / 420;
        var ev = { x: slope(g.gx[0], g.gx[1], g.gx[2], g.gx[3], 1), y: slope(g.gy[0], g.gy[1], g.gy[2], g.gy[3], 1) };
        var el = Math.max(1, Math.hypot(ev.x, ev.y));
        var eu = { x: ev.x / el, y: ev.y / el };
        var damp = Math.exp(-4.6 * rc) * Math.cos(rc * Math.PI * 2.2);
        point = { x: g.p3.x + eu.x * 14 * damp, y: g.p3.y + eu.y * 14 * damp };
        angle = Math.atan2(ev.y, ev.x) * 180 / Math.PI;
        setPoseFrame(6, 1, g.direction);
        hideCruise();
        draw(point.x, point.y, bank(1) - 3.5 * damp, 1 - 0.02 * damp, 0, Math.max(0, 0.54 * (1 - rc)), 0.28 * twinkle(rc, 0.35, 0.3), angle + 180);
        return;
      }
      if (t < 4480) { // 발견 · 어?
        setPhase("discover");
        var dc = (t - 4260) / 220;
        setPoseFrame(6, 1, g.direction);
        hideCruise();
        draw(g.p3.x, g.p3.y, bank(1) * (1 - dc), 1 + 0.035 * twinkle(dc, 0.36, 0.72), 0, 0, 0.6 * twinkle(dc, 0.4, 0.54), 155);
        return;
      }
      if (t < 4860) { // 신기해 · 둘러보기
        setPhase("wonder");
        var wd = (t - 4480) / 380;
        var sway = Math.sin(wd * Math.PI * 2);
        setPoseFrame(1, 1, g.direction);
        hideCruise();
        draw(g.p3.x, g.p3.y + sway * 2.4, sway * 1.5, 1 + 0.012 * Math.sin(wd * Math.PI), 0, 0, 0.18 * twinkle(wd, 0.55, 0.46), 155);
        return;
      }
      if (t < 5220) { // 감탄 · 여기구나!
        setPhase("delight");
        var dl = (t - 4860) / 360;
        setPoseFrame(2, 1, g.direction);
        hideCruise();
        draw(g.p3.x, g.p3.y - Math.sin(dl * Math.PI) * 3, Math.sin(dl * Math.PI) * -1.2, 1 + Math.sin(dl * Math.PI) * 0.022, 0, 0, 0.3 * twinkle(dl, 0.45, 0.5), 155);
        return;
      }
      if (t < 5480) { // 도착 · 정지
        setPhase("settle");
        var st = (t - 5220) / 260;
        var bounce = Math.exp(-5 * st) * Math.cos(st * Math.PI * 2);
        setPoseFrame(7, 1, g.direction);
        hideCruise();
        draw(g.p3.x, g.p3.y + bounce * 2.2, -bounce * 1.5, 1, 0, 0, 0, 155);
        return;
      }
      // 입장 · 빛나며 흡수
      setPhase(g.gate ? "gate" : "dive");
      renderAbsorption((t - 5480) / 880, g.p3, g.target, g.direction, g.gate, bank(1));
    }

    // ── 레벨 진입 기하/타임라인 ──────────────────────────────────────
    // 전 레벨 관문으로 들어가 사라지는 건 직전 로드맵의 관문 hop이 담당한다.
    // 새 로드맵에서는 첫 행성 위에서 빛과 함께 "나타나" 놀라고 감탄한 뒤 자리잡는다.
    // 맵은 배율 그대로(줌/축소 없음) — 카메라만 첫 행성으로 맞춘다.
    function buildLevelGeometry(first) {
      var b = center(first);
      return { mode: "level", first: b, restoreY: b.y };
    }

    function renderLevelEntry(elapsed) {
      var t = clamp(elapsed, 0, LEVEL_TOTAL_MS);
      var g = geometry;
      var b = g.first;
      var appear = { x: b.x, y: b.y - b.h * 0.4 }; // 첫 행성 살짝 위 = 누비가 자리잡는 지점
      var progress;

      if (t < 900) { // 등장 · 첫 행성 위에서 빛과 함께 나타난다(위에서 살짝 내려오며 솟음)
        setPhase("appear");
        var et = t / 900;
        var em = easeOutBack(et);
        var spark = Math.max(twinkle(et, 0.42, 0.34), 0.5 * twinkle(et, 0.78, 0.16));
        setAbsorb(et < 1, appear, clamp(b.w * 1.0, 120, 240), et, spark);
        setPoseFrame(0, 1, "right");
        hideCruise();
        var y = lerp(b.y - b.h * 0.95, appear.y, em);
        draw(appear.x, y, 0, 0.12 + 0.88 * em, 0.06 * em, 0, spark, 155, clamp(et / 0.16, 0, 1));
        return;
      }
      setAbsorb(false);

      if (t < 1400) { // 어? 여기가 새 로드맵! · 놀람
        setPhase("surprise");
        progress = (t - 900) / 500;
        var sb = Math.exp(-4.8 * progress) * Math.cos(progress * Math.PI * 2.2);
        setPoseFrame(6, 1, "right");
        hideCruise();
        draw(appear.x + sb * 3, appear.y - Math.sin(progress * Math.PI) * 3.4, sb * -2, 1 + 0.035 * twinkle(progress, 0.42, 0.58), 0, 0, 0.3 * twinkle(progress, 0.48, 0.52), 155, 1);
        return;
      }
      if (t < 2100) { // 신기해 · 새 로드맵 둘러보기
        setPhase("wonder");
        progress = (t - 1400) / 700;
        var sway = Math.sin(progress * Math.PI * 2);
        setPoseFrame(progress < 0.52 ? 1 : 2, 1, "right");
        hideCruise();
        draw(appear.x + sway * 2.6, appear.y + Math.sin(progress * Math.PI * 4) * 1.8, sway * 1.8, 1 + 0.012 * Math.sin(progress * Math.PI), 0, 0, 0, 155, 1);
        return;
      }
      if (t < 2600) { // 감탄 · 여기서 시작이구나!
        setPhase("delight");
        progress = (t - 2100) / 500;
        setPoseFrame(2, 1, "right");
        hideCruise();
        draw(appear.x, appear.y - Math.sin(progress * Math.PI) * 4, -Math.sin(progress * Math.PI) * 1.6, 1 + Math.sin(progress * Math.PI) * 0.025, 0, 0, 0, 155, 1);
        return;
      }
      if (t < 2900) { // 정지 · 첫 행성 앞에 자리잡음
        setPhase("settle");
        progress = (t - 2600) / 300;
        var stl = Math.exp(-5 * progress) * Math.cos(progress * Math.PI * 2);
        setPoseFrame(7, 1, "right");
        hideCruise();
        draw(appear.x, appear.y + stl * 2.4, -stl * 1.5, 1, 0, 0, 0, 155, 1);
        return;
      }
      // 첫 행성 입장 · 빛나며 행성 안으로 흡수(hop 도착 흡수와 같은 모션)
      setPhase("dive");
      renderAbsorption((t - 2900) / 900, appear, g.first, "right", false, 0);
    }

    function render(elapsed) {
      if (geometry.mode === "level") { renderLevelEntry(elapsed); return; }
      renderRoute(elapsed);
    }

    function play() {
      window.cancelAnimationFrame(animationId);
      var total = geometry.mode === "level" ? LEVEL_TOTAL_MS : ROUTE_TOTAL_MS;
      var started = null;
      currentPhase = "";
      function tick(now) {
        if (aborted) { hide(); return; }
        if (started === null) { started = now; }
        var elapsed = now - started;
        render(elapsed);
        // stage를 스크롤해 누비를 창 가운데로 따라간다(hop=비행 추적, level=첫 행성에 고정).
        camera(currentDrawY);
        if (elapsed < total) { animationId = window.requestAnimationFrame(tick); return; }
        hide();
      }
      animationId = window.requestAnimationFrame(tick);
    }

    function run(e) {
      if (!nuvi.hidden) { return; } // 이미 날고 있으면 중복 실행 금지
      aborted = false;
      var type = e && e.detail && e.detail.type;
      if (type === "COURSE") { return; } // 과목 완주는 피날레라 누비를 움직이지 않는다
      var isLevel = type === "LEVEL";

      var target = findTarget();
      if (!target) { return; }
      var b = center(target);
      if (!b.w || !b.h) { return; } // 행성 그림이 아직 안 들어옴 → 접는다

      if (isLevel) {
        geometry = buildLevelGeometry(target);
      } else {
        var origin = findOrigin(target);
        if (!origin) { return; } // 방금 완료한 행성을 못 찾음 = 보여줄 이동 없음
        var oa = center(origin);
        if (!oa.w || !oa.h) { return; }
        geometry = buildRouteGeometry(origin, target);
      }

      size = Math.round(Math.max(64, Math.min(132, b.w * 0.9)));
      camMargin = Math.min(Math.max(size, Math.round(b.h)), Math.round(window.innerHeight * 0.4));
      nuvi.style.setProperty("--nuvi-size", size + "px");
      layerWidth = layer.offsetWidth;

      cameraOwned = true;
      document.dispatchEvent(new CustomEvent("knowva:roadmap-camera-taken"));

      nuvi.hidden = false;
      void nuvi.offsetWidth; // 등장 fade가 첫 프레임부터 걸리도록 강제 reflow
      nuvi.classList.add("is-visible");
      play();
    }

    document.addEventListener("knowva:celebration-closed", run);

    // 첫 방문(진입 행성): 관문 통과 축하가 없어도 누비가 첫 행성 위로 빛과 함께 등장하는 연출을
    // 한 번 재생한다. 서버가 진입 시점(레벨의 첫 행성·미시작)에만 data-entry-welcome을 준다.
    // 축하 오버레이가 떠 있으면 그 닫힘(celebration-closed)이 등장/이동을 맡으므로 여기선 건드리지 않는다.
    if (nuvi.dataset.entryWelcome === "1" && !document.querySelector("[data-celebrate]")) {
      var welcomeTries = 0;
      var tryWelcome = function () {
        if (aborted || !nuvi.hidden) { return; } // 다른 트리거로 이미 날고 있으면 그만둔다
        var target = findTarget();
        var art = target && (target.querySelector(".roadmap-planet") || target);
        if (target && art && art.offsetWidth > 0 && art.offsetHeight > 0) { // 행성 그림의 너비·높이가 레이아웃된 뒤에만 시작
          run({ detail: { type: "LEVEL" } });
          return;
        }
        if (welcomeTries++ < 60) { window.requestAnimationFrame(tryWelcome); }
      };
      window.requestAnimationFrame(tryWelcome);
    }

    // 노드는 % 좌표라 맵 폭이 바뀌면 경로가 무의미해진다 → 연출을 접는다(짧은 1회성이라 다시 재지 않는다).
    // 폭을 직접 비교: resize는 맵과 무관한 일로도 뜬다(주소창 접힘, 세로만 조절, 개발자도구).
    window.addEventListener(
      "resize",
      function () {
        if (!nuvi.hidden && layer.offsetWidth !== layerWidth) { aborted = true; }
      },
      { passive: true }
    );
  }

  if (document.readyState === "loading") {
    document.addEventListener("DOMContentLoaded", init);
  } else {
    init();
  }
})();
