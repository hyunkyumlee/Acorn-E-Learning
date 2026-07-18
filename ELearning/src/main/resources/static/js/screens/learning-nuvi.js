/* =========================================================================
   learning-nuvi.js — 로드맵 누비: 완료한 행성 → 다음 행성 비행 연출
   - 로드맵에 누비를 상주시키지 않는다. 축하 연출이 닫히는 순간(knowva:celebration-closed)
     딱 한 번 등장 → 비행 → 다음 행성 안으로 사라지고 끝난다. 그냥 새로고침한 화면에는 누비가 없다.
   - 출발 = 방금 완료한 행성. 서버가 다시 그린 화면이라 .is-current는 이미 다음 행성이므로,
     출발점은 목적지 앞의 마지막 .is-done에서 찾는다. 없으면(=보여줄 이동이 없으면) 연출을 건너뛴다.
   - 3단: 솟아오름(행성 속에서 빛과 함께 부풀며 빠져나온다) → 비행 → 잠입(다음 행성/관문 안으로
     빨려 들어가며 작아진다). 행성 중심에서 나고 드는데도 행성 아트를 가리지 않는 이유는
     그 구간의 누비가 작고, 커질 때는 이미 행성 밖으로 나와 있기 때문이다.
   - 경로 = 두 노드를 잇는 3차 베지에. 양 끝 제어점을 수직으로 두면 맵의 S자 길을 따라 휘어 난다.
   - 누비 자체의 움직임 = 비행 중 포즈 PNG 교대(팔이 움직임) + 상하 흔들림 + 진행 방향 기울기.
   - 좌표·크기·기울기·빛(--nuvi-glow)은 전부 여기서 매 프레임 준다. CSS에는 시간이 없다
     → 두 곳의 길이가 어긋날 일이 없다.
   - 맵은 세로로 길어 두 행성이 한 화면에 안 들어온다 → 비행 동안 stage를 따라 스크롤(카메라 추적)한다.
     같은 stage를 로드맵의 관성 스크롤과 동시에 건드리지 않도록 knowva:roadmap-camera-taken으로 양보받는다.
   - prefers-reduced-motion: reduce면 연출을 아예 실행하지 않는다(움직임이 곧 내용이라 정지본은 의미가 없다).
   ========================================================================= */
(function () {
  "use strict";

  var EMERGE_MS = 460; // 행성 속에서 빛과 함께 솟아오름
  var FLY_MS = 1800; // 비행
  var ENTRY_MS = 1150; // 새 판으로 날아 들어오는 거리는 행성 사이보다 짧다
  var STAY_MS = 520; // 목적지 앞에서 잠깐 멈춤(도착 포즈를 보여 주는 시간)
  var DIVE_MS = 560; // 다음 행성 안으로 잠입
  var GATE_MS = 820; // 관문은 더 깊이 빨려 들어간다
  var FRAME_MS = 300; // 비행 중 포즈 교대 간격
  var TILT_MAX = 16; // 진행 방향으로 기우는 최대 각도(도)
  var MIN_SCALE = 0.18; // 행성 속에 있을 때의 크기(0이면 빛만 남아 튀어나오는 티가 안 난다)
  var GATE_SCALE = 0.06; // 관문은 점처럼 사라진다

  function init() {
    var nuvi = document.querySelector("[data-roadmap-nuvi]");
    var img = nuvi && nuvi.querySelector("[data-roadmap-nuvi-img]");
    var layer = nuvi && nuvi.closest(".roadmap-layer");
    var stage = nuvi && nuvi.closest("[data-roadmap-stage]");
    if (!nuvi || !img || !layer || !stage) {
      return;
    }

    if (
      window.matchMedia &&
      window.matchMedia("(prefers-reduced-motion: reduce)").matches
    ) {
      return;
    }

    // 포즈: 등장/도착은 한 장, 비행 중에는 두 장을 번갈아 팔이 움직이게 한다.
    var poseLaunch = nuvi.dataset.nuviLaunch;
    var poseFlyA = nuvi.dataset.nuviFlyA;
    var poseFlyB = nuvi.dataset.nuviFlyB;
    var poseArrive = nuvi.dataset.nuviArrive;
    if (!poseLaunch || !poseFlyA || !poseFlyB || !poseArrive) {
      return;
    }
    // 비행 중에 처음 불러오면 한 프레임이 빈다 → 미리 받아둔다.
    // 마크업의 초기 src(= poseLaunch)는 브라우저가 이미 받았으므로 뺀다.
    [poseFlyA, poseFlyB, poseArrive].forEach(function (src, i, all) {
      if (src === poseLaunch || all.indexOf(src) !== i) {
        return;
      }
      var pre = new Image();
      pre.src = src;
    });

    /** 목적지 = 지금 학습할 행성. 없으면 응시 가능해진 관문.
        지나온 레벨(관문을 넘어 다음 레벨이 열린 판)에는 순차 잠금이 없어 다음 행성이 current가 아니라
        open이고 관문도 이미 passed다 → 앞의 둘로는 아무것도 못 찾아 연출이 통째로 빠진다. 축하는 그
        판에서도 뜨므로 누비만 안 나오는 어긋남이 생긴다. 노드는 planet_no 순으로 그려지고 앞에서부터
        연속으로 완료되므로, 첫 open이 곧 방금 마친 행성의 다음이다.
        지나온 레벨의 마지막 행성을 마치면 open도 남지 않는다(전부 done + 관문 passed) → 그 판의 끝인
        관문을 목적지로 둔다. 이미 통과한 관문이라 안내가 아니라 완주 표시지만, 축하만 뜨고 누비가
        빠지는 것보다 이어짐이 맞다. 관문이 locked인 판은 앞의 current가 먼저 잡히므로 여기까지 안 온다. */
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
        if (node.classList.contains("is-done")) {
          return node;
        }
        node = node.previousElementSibling;
      }
      return null;
    }

    /** 노드의 행성/관문 그림 중심을 맵 레이어 좌표(px)로 구한다.
        offset 값만 쓰는 이유: 노드에는 중심 정렬용 translate(-50%,-50%)와 호버 확대가 걸려 있어
        getBoundingClientRect는 그 변형까지 섞인 값을 준다. 레이아웃 좌표가 필요하다. */
    function center(node) {
      var art = node.querySelector(".roadmap-planet") || node;
      return {
        x: node.offsetLeft - node.offsetWidth / 2 + art.offsetLeft + art.offsetWidth / 2,
        y: node.offsetTop - node.offsetHeight / 2 + art.offsetTop + art.offsetHeight / 2,
        w: art.offsetWidth,
        h: art.offsetHeight,
      };
    }

    /** 3차 베지에 값과 미분(한 축씩). */
    function bez(p0, p1, p2, p3, t) {
      var u = 1 - t;
      return u * u * u * p0 + 3 * u * u * t * p1 + 3 * u * t * t * p2 + t * t * t * p3;
    }
    function bezSlope(p0, p1, p2, p3, t) {
      var u = 1 - t;
      return 3 * u * u * (p1 - p0) + 6 * u * t * (p2 - p1) + 3 * t * t * (p3 - p2);
    }

    /** 두 점을 잇는 경로. 제어점의 x를 양 끝과 같게 두면 접선이 수직이 되어,
        위아래로 빠져나갔다 들어오는 맵의 S자 길처럼 휜다. */
    function curve(a, b) {
      var dy = (b.y - a.y) * 0.45;
      var x = [a.x, a.x, b.x, b.x];
      var y = [a.y, a.y + dy, b.y - dy, b.y];
      return {
        x: function (t) { return bez(x[0], x[1], x[2], x[3], t); },
        y: function (t) { return bez(y[0], y[1], y[2], y[3], t); },
        /** 수직 대비 진행 방향 각도 → 누비가 도는 쪽으로 살짝 기운다. */
        tilt: function (t) {
          var deg =
            (Math.atan2(
              bezSlope(x[0], x[1], x[2], x[3], t),
              bezSlope(y[0], y[1], y[2], y[3], t)
            ) *
              180) /
            Math.PI *
            0.35;
          return Math.max(-TILT_MAX, Math.min(TILT_MAX, deg));
        },
      };
    }

    function lerp(p, q, t) {
      return { x: p.x + (q.x - p.x) * t, y: p.y + (q.y - p.y) * t };
    }
    function easeInOut(t) {
      return t < 0.5 ? 4 * t * t * t : 1 - Math.pow(-2 * t + 2, 3) / 2;
    }
    function easeOut(t) {
      return 1 - Math.pow(1 - t, 3);
    }
    function easeIn(t) {
      return t * t * t;
    }
    /** 살짝 지나쳤다 돌아오는 부풀기 — 행성이 뱉어낸 듯한 탄력. */
    function easeOutBack(t) {
      var c = 1.70158;
      return 1 + (c + 1) * Math.pow(t - 1, 3) + c * Math.pow(t - 1, 2);
    }

    /** 반짝임 한 번. c에서 정점, 폭 w 밖은 0.
        구간 전체에 걸친 완만한 곡선(예: easeOut)을 빛에 주면 빛이 계속 붙어 있어 "번쩍"이 아니라
        "켜 둔 조명"이 된다. 창처럼 좁게 튀었다 0으로 떨어져야 반짝임으로 읽힌다. */
    function twinkle(t, c, w) {
      var d = (t - c) / w;
      if (d <= -1 || d >= 1) {
        return 0;
      }
      var x = 1 - d * d;
      return x * x;
    }

    var size = 64;
    var camMargin = 64; // 창 가장자리와 띄울 거리 — 목적지 그림 크기로 run()이 정한다
    var cameraOwned = false; // 로드맵에서 카메라를 넘겨받은 동안만 맵을 스크롤한다
    var aborted = false; // 레이아웃이 바뀌어 경로가 무의미해지면 세운다
    var poseTimer = null;
    var layerWidth = 0; // 연출을 시작할 때의 맵 폭 — 경로 좌표가 유효한지 판단하는 기준

    /** 지금 어느 구간인지 DOM에 남긴다. 연출이 3초 안에 끝나 눈으로 좇기 어려워
        (그리고 CSS class로는 구간이 드러나지 않아) 확인할 때 붙잡을 손잡이가 필요하다. */
    function phase(name) {
      if (name) {
        nuvi.dataset.nuviPhase = name;
        return;
      }
      delete nuvi.dataset.nuviPhase;
    }

    /** 맵 레이어 좌표를 요소 중심에 맞춰 얹는다(transform 한 줄 — top/left는 건드리지 않는다).
        빛은 세기 하나(glow)를 받아 세 겹으로 나눠 준다. 셋을 같은 값으로 두면 한 덩어리로 부풀어
        올라 원형 얼룩이 된다 → 코어는 정점에서만 튀고(제곱), 빛살은 먼저 서고 늦게 지며(제곱근 쪽),
        부스러기는 정점 순간에만 흩어진다(세제곱). 회전은 빛살이 살아 있는 동안 조금씩 돈다. */
    function draw(x, y, tilt, scale, glow) {
      nuvi.style.transform =
        "translate3d(" + (x - size / 2) + "px," + (y - size / 2) + "px,0)" +
        " rotate(" + tilt + "deg) scale(" + scale + ")";
      nuvi.style.setProperty("--nuvi-glow", String(glow));
      nuvi.style.setProperty("--nuvi-flash", String(Math.pow(glow, 2)));
      nuvi.style.setProperty("--nuvi-rays", String(Math.pow(glow, 0.65)));
      nuvi.style.setProperty("--nuvi-rays-scale", String(0.35 + glow * 0.95));
      nuvi.style.setProperty("--nuvi-motes", String(Math.pow(glow, 3)));
      nuvi.style.setProperty("--nuvi-spin", glow * 70 + "deg");
    }

    /** 누비를 stage 한가운데에 두도록 맵을 스크롤한다(카메라 추적).
        기준은 연출 내내 누비 한 점뿐이다 — 구간마다 기준을 바꾸면(예: 잠입만 목적지 기준)
        기준이 바뀌는 프레임에 맵이 그 차이만큼 한 번에 튄다. */
    function camera(y) {
      if (!cameraOwned) {
        return; // 사용자가 직접 굴리기 시작했다 → 카메라는 사용자 것
      }
      var max = stage.scrollHeight - stage.clientHeight;
      var top = layer.offsetTop + y - stage.clientHeight / 2;
      stage.scrollTop = Math.max(0, Math.min(top, max));

      // stage는 창보다 높다 → 아래쪽이 창 밖으로 접혀 있다. 맵 끝(관문)처럼 stage를 끝까지 굴려도
      // 누비를 가운데로 못 데려오는 구간에서는 stage만 굴려서는 연출이 창 밖에서 벌어진다.
      // 그때만 페이지도 같이 민다. 창 안에 여유 있게 들어와 있으면 페이지는 건드리지 않는다
      // (행성 사이 이동은 여기 걸리지 않는다 = 페이지가 가만히 있다).
      // 여유(camMargin)를 누비 크기가 아니라 목적지 그림 크기로 잡는 이유: 누비만 창 안에 걸치게 하면
      // 누비가 목적지 한가운데에 닿는 마지막 순간에 정작 빨려 들어가는 행성/관문이 창 밑단에 잘려
      // 무엇에 들어가는지가 안 보인다.
      var vy = stage.getBoundingClientRect().top + (layer.offsetTop + y - stage.scrollTop);
      var margin = camMargin;
      var over = 0;
      if (vy > window.innerHeight - margin) {
        over = vy - (window.innerHeight - margin);
      } else if (vy < margin) {
        over = vy - margin;
      }
      if (over) {
        window.scrollBy(0, over);
      }
    }

    function releaseCamera() {
      if (!cameraOwned) {
        return;
      }
      cameraOwned = false;
      document.dispatchEvent(new CustomEvent("knowva:roadmap-camera-released"));
    }

    // 로드맵이 "사용자가 직접 굴렸다"고 알려주면 따라가기를 멈춘다(비행 자체는 계속).
    document.addEventListener("knowva:roadmap-camera-released", function () {
      cameraOwned = false;
    });

    function hide() {
      phase(null);
      releaseCamera();
      if (poseTimer !== null) {
        window.clearInterval(poseTimer);
        poseTimer = null;
      }
      nuvi.hidden = true;
      nuvi.classList.remove("is-visible");
      nuvi.style.transform = "";
      nuvi.style.removeProperty("--nuvi-size");
      // draw()가 얹은 빛 변수는 전부 걷는다 — 하나라도 남기면 다음 등장의 첫 프레임에 지난 연출의
      // 밝기가 묻어난다.
      ["glow", "flash", "rays", "rays-scale", "motes", "spin"].forEach(function (k) {
        nuvi.style.removeProperty("--nuvi-" + k);
      });
    }

    /** 한 구간을 rAF로 돌린다. onFrame(t)는 0→1을 받고, 끝나면 done()을 부른다.
        중간에 레이아웃이 바뀌면(aborted) 그 자리에서 연출을 접는다. */
    function tween(ms, onFrame, done) {
      var start = null;
      function step(now) {
        if (aborted) {
          hide();
          return;
        }
        if (start === null) {
          start = now;
        }
        var t = Math.min(1, (now - start) / ms);
        onFrame(t);
        if (t < 1) {
          window.requestAnimationFrame(step);
          return;
        }
        done();
      }
      window.requestAnimationFrame(step);
    }

    /** 1단 — 행성 속에서 반짝하며 솟아오른다.
        빛은 표면을 뚫는 순간(t≈0.44)에 한 번 크게 튀고, 다 빠져나온 뒤(t≈0.76) 작게 한 번 더 튄다.
        두 번 튀어야 "반짝"으로 읽힌다 — 한 번만 부풀렸다 꺼지면 조명을 켰다 끈 것처럼 보인다.
        시작(행성 속이라 안 보임)과 끝(빛날 이유 없음)은 0이다. */
    function emerge(plan, next) {
      phase("emerge");
      img.src = poseLaunch;
      tween(
        EMERGE_MS,
        function (t) {
          var p = lerp(plan.from, plan.launchAt, easeOut(t));
          var scale = MIN_SCALE + (1 - MIN_SCALE) * easeOutBack(t);
          var spark = Math.max(twinkle(t, 0.44, 0.34), 0.5 * twinkle(t, 0.76, 0.16));
          draw(p.x, p.y, 0, scale, spark);
          camera(p.y);
        },
        next
      );
    }

    /** 2단 — 비행. 포즈 교대 = 누비가 팔을 움직이며 나아가는 것처럼 보이게 하는 부분. */
    function fly(plan, next) {
      phase("fly");
      // 첫 교대가 poseFlyA다 — 등장 포즈(poseLaunch)와 같은 장으로 바꾸면 비행 시작 후
      // 한 박자(FRAME_MS×2) 동안 그림이 그대로라 미끄러지는 정지 이미지로 보인다.
      var frame = 0;
      poseTimer = window.setInterval(function () {
        frame += 1;
        img.src = frame % 2 ? poseFlyA : poseFlyB;
      }, FRAME_MS);

      tween(
        plan.flyMs,
        function (t) {
          var e = easeInOut(t);
          var y = plan.path.y(e);
          // 상하 흔들림: 비행 내내 누비가 살짝 떴다 가라앉는다.
          var bob = Math.sin(t * Math.PI * 5) * (size * 0.07);
          draw(plan.path.x(e), y + bob, plan.path.tilt(e), 1, 0);
          camera(y);
        },
        function () {
          window.clearInterval(poseTimer);
          poseTimer = null;
          next();
        }
      );
    }

    /** 3단 — 목적지 안으로 잠입. 표면에 닿는 순간 반짝하고 잠긴다.
        빛을 easeOut(t)처럼 올려 두면 잠입 내내 켜져 있어 커다란 얼룩이 따라다닌다 → 닿는 순간
        (t≈0.72)에 크게, 그 앞(t≈0.4)에 작게 두 번만 튄다. 관문은 더 깊이, 회전을 얹어 빨려 들어간다. */
    function dive(plan) {
      var ms = plan.gate ? GATE_MS : DIVE_MS;
      var endScale = plan.gate ? GATE_SCALE : MIN_SCALE;
      var spin = plan.gate ? 180 : 0;
      phase("arrive");
      img.src = poseArrive;
      draw(plan.arriveAt.x, plan.arriveAt.y, 0, 1, 0);
      camera(plan.arriveAt.y);

      window.setTimeout(function () {
        if (aborted) {
          hide();
          return;
        }
        phase(plan.gate ? "gate" : "dive");
        tween(
          ms,
          function (t) {
            var e = easeIn(t);
            var p = lerp(plan.arriveAt, plan.to, e);
            var spark = Math.max(twinkle(t, 0.72, 0.32), 0.45 * twinkle(t, 0.4, 0.18));
            draw(p.x, p.y, spin * e, 1 - (1 - endScale) * e, spark);
            camera(p.y);
          },
          hide
        );
      }, STAY_MS);
    }

    /** 두 가지 등장이 있다.
        - hop  : 행성을 하나 끝냈다. 방금 끝낸 행성에서 솟아나 다음 행성/관문으로 간다.
        - level: 관문을 넘어 새 판이 열렸다. 이 판엔 끝낸 행성이 없으니 솟아날 데도 없다
                 → 맵 위쪽에서 날아 들어와 첫 행성으로 들어간다. */
    function planLevelEntry(b) {
      var arriveAt = { x: b.x, y: b.y - b.h * 0.4 };
      // 맵 밖(y<0)은 stage가 잘라내므로 맨 위를 넘지 않는 선에서 띄운다.
      var drop = Math.min(360, Math.max(160, b.h * 2.2));
      var from = { x: b.x, y: Math.max(0, arriveAt.y - drop) };
      return {
        gate: false,
        flyMs: ENTRY_MS,
        needsEmerge: false,
        launchAt: from,
        arriveAt: arriveAt,
        to: { x: b.x, y: b.y },
        path: curve(from, arriveAt),
      };
    }

    function planHop(target, origin, b) {
      var a = center(origin);
      if (!a.w || !a.h) {
        return null;
      }
      // 완료한 행성 속에서 나와 목적지 쪽 옆면으로 빠져나온 뒤(그래야 행성 아트도 "완료" 배지도
      // 가리지 않는다), 다음 행성 위에 닿았다가 그 안으로 들어간다.
      var dir = b.x < a.x ? -1 : 1;
      var launchAt = { x: a.x + dir * a.w * 0.55, y: a.y + a.h * 0.18 };
      var arriveAt = { x: b.x, y: b.y - b.h * 0.4 };
      return {
        gate: target.classList.contains("is-gate"),
        flyMs: FLY_MS,
        needsEmerge: true,
        from: { x: a.x, y: a.y },
        launchAt: launchAt,
        arriveAt: arriveAt,
        to: { x: b.x, y: b.y },
        path: curve(launchAt, arriveAt),
      };
    }

    function run(e) {
      if (!nuvi.hidden) {
        return; // 이미 날고 있으면 중복 실행 금지
      }
      aborted = false;
      var celebrateType = e && e.detail && e.detail.type;
      // 과목 완주(COURSE)는 그 자체가 피날레라 누비를 움직이지 않는다. 이 판의 GOLD 관문은 아직
      // is-ready(응시 가능)라 findTarget이 그리로 잡아 누비가 관문으로 날아가 버리므로 여기서 접는다.
      if (celebrateType === "COURSE") {
        return;
      }
      var isLevelEntry = celebrateType === "LEVEL";
      var target = findTarget();
      if (!target) {
        return;
      }
      var b = center(target);
      // 행성 그림이 아직 안 들어왔으면 높이가 0이라 중심이 엉뚱하게 잡힌다 → 연출을 접는다.
      if (!b.w || !b.h) {
        return;
      }

      var plan;
      if (isLevelEntry) {
        plan = planLevelEntry(b);
      } else {
        var origin = findOrigin(target);
        if (!origin) {
          return; // 방금 완료한 행성을 못 찾음 = 보여줄 이동이 없다.
        }
        plan = planHop(target, origin, b);
      }
      if (!plan) {
        return;
      }

      size = Math.round(Math.max(44, Math.min(96, b.w * 0.55)));
      // 카메라가 창 안에 붙잡는 것은 누비 한 점뿐이라, 여유를 목적지 그림의 반(0.5h)으로 잡으면
      // 누비가 아직 목적지 위(arriveAt = 중심에서 0.4h 위)에 있는 잠입 구간에서 그림 아래가 잘린다.
      // → 그 간격까지 더한 1.0h로 잡아야 잠입 내내 목적지 전체가 보인다.
      // 창이 낮으면 그만큼만 요구한다(여유가 창 절반을 넘으면 위아래 조건이 서로를 밀어 카메라가 떤다).
      camMargin = Math.min(
        Math.max(size, Math.round(b.h)),
        Math.round(window.innerHeight * 0.4)
      );
      nuvi.style.setProperty("--nuvi-size", size + "px");
      layerWidth = layer.offsetWidth;

      // 로드맵의 관성 스크롤과 stage를 두고 다투지 않게 카메라를 넘겨받는다.
      cameraOwned = true;
      document.dispatchEvent(new CustomEvent("knowva:roadmap-camera-taken"));

      // 빛은 0에서 시작한다. 여기서 1로 그려 두면 emerge 첫 프레임(sin(0)=0)이 바로 덮어써
      // 한 프레임짜리 번쩍임만 남는다.
      var head = plan.needsEmerge ? plan.from : plan.launchAt;
      draw(head.x, head.y, 0, plan.needsEmerge ? MIN_SCALE : 1, 0);
      camera(head.y);
      nuvi.hidden = false;
      void nuvi.offsetWidth; // 등장 fade가 첫 프레임부터 걸리도록 강제 reflow
      nuvi.classList.add("is-visible");

      function toFlight() {
        fly(plan, function () {
          dive(plan);
        });
      }
      if (plan.needsEmerge) {
        emerge(plan, toFlight);
        return;
      }
      toFlight();
    }

    document.addEventListener("knowva:celebration-closed", run);

    // 노드는 % 좌표라 맵 폭이 바뀌면 새 px 위치로 재배치된다. 미리 잰 경로는 그 순간 무의미해져
    // 누비가 빈 공간에 내려앉는다 → 연출을 중간에 접는다(짧은 1회성이라 다시 재지 않는다).
    // 폭을 직접 비교하는 이유: resize는 맵과 상관없는 일로도 뜬다(모바일 주소창 접힘, 세로 크기만
    // 바뀌는 창 조절, 개발자도구 열기). 그때마다 접으면 멀쩡한 연출이 사라진다.
    window.addEventListener(
      "resize",
      function () {
        if (!nuvi.hidden && layer.offsetWidth !== layerWidth) {
          aborted = true;
        }
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
