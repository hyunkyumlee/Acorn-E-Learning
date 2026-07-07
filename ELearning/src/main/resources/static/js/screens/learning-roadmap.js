/* =========================================================================
   learning-roadmap.js — SR-003 학습 메인 중앙 로드맵(세로 우주 맵) 인터랙션
   - 노드 등장: stage 뷰포트 진입 시 하나씩 .is-visible (transform/opacity).
   - 관성 스무스 스크롤: 이 stage의 휠에만 lerp 적용 → 부드럽게 미끄러지는 슬라이드.
     * 로드맵 창에만 적용(페이지 전체 스크롤은 건드리지 않음).
     * 맵 위/아래 끝에서 더 밀면 preventDefault 안 하고 페이지에 양보(가둠 방지).
   - 현재 학습 노드를 stage 안에서 가운데로(같은 관성으로 미끄러져 이동).
   - prefers-reduced-motion: reduce면 관성/등장 애니 없이 네이티브 스크롤 + 즉시 표시.
   - JS 없거나 미지원이면 CSS base(항상 표시)로 안전 폴백.
   ========================================================================= */
(function () {
  "use strict";

  function init() {
    var stage = document.querySelector("[data-roadmap-stage]");
    if (!stage) {
      return;
    }

    var nodes = stage.querySelectorAll(".roadmap-node");
    if (!nodes.length) {
      return;
    }

    var prefersReduced =
      window.matchMedia &&
      window.matchMedia("(prefers-reduced-motion: reduce)").matches;

    /* ---- 1) 노드 등장 ---- */
    if (prefersReduced || !("IntersectionObserver" in window)) {
      nodes.forEach(function (node) {
        node.classList.add("is-visible");
      });
    } else {
      var observer = new IntersectionObserver(
        function (entries) {
          entries.forEach(function (entry) {
            if (entry.isIntersecting) {
              entry.target.classList.add("is-visible");
              observer.unobserve(entry.target);
            }
          });
        },
        { root: stage, rootMargin: "60px 0px", threshold: 0.15 }
      );
      nodes.forEach(function (node) {
        observer.observe(node);
      });
    }

    /* ---- 2a) 스크롤 진행 게이지 업데이트 (reduce 여부와 무관하게 항상) ---- */
    var progressBar = stage.querySelector(".roadmap-progress > i");
    function updateProgress() {
      if (!progressBar) {
        return;
      }
      var m = stage.scrollHeight - stage.clientHeight;
      progressBar.style.width = (m > 0 ? (stage.scrollTop / m) * 100 : 0) + "%";
    }
    stage.addEventListener("scroll", updateProgress, { passive: true });
    updateProgress();

    /* ---- 2a-2) 로드맵 양 끝에서 페이지 전체를 "착" 정렬 ----
       내부 stage는 화면보다 커서 위/아래가 뷰포트 밖으로 나간다.
       - 맵 끝(게이트)까지 내리면 페이지를 문서 하단으로 스냅 → 게이트가 다 보인다.
       - 맵 처음까지 올리면 페이지를 문서 상단으로 스냅(하단의 대칭) → 상단 UI가 다 보인다.
       내부 스크롤은 그대로 두고, 끝 구간에 "도달"한 순간에만 1회 페이지를 정렬한다. */
    var snappedBottom = false;
    var snappedTop = true; // 로드는 상단에서 시작 → 상단을 떠났다 되돌아올 때만 스냅
    function snapPageToRoadmapEnd() {
      var m = stage.scrollHeight - stage.clientHeight;
      if (m <= 0) {
        return;
      }
      // 맵 끝에 "가까워지면"(마지막 구간 진입) 미리 페이지를 하단으로 정렬한다.
      // 내부 관성 글라이드(EASE 0.06)가 완전히 끝나길 기다리지 않아 반응이 자연스럽게 따라온다.
      var lead = Math.max(140, m * 0.2);
      if (stage.scrollTop >= m - lead) {
        if (!snappedBottom) {
          snappedBottom = true;
          window.scrollTo({
            top: document.documentElement.scrollHeight,
            behavior: prefersReduced ? "auto" : "smooth",
          });
        }
      } else {
        snappedBottom = false; // 끝 구간에서 벗어나면 다시 스냅 가능하도록 해제
      }
      // 맵 처음 근처 → 페이지 상단으로 정렬 (하단 스냅의 대칭)
      if (stage.scrollTop <= lead) {
        if (!snappedTop) {
          snappedTop = true;
          window.scrollTo({
            top: 0,
            behavior: prefersReduced ? "auto" : "smooth",
          });
        }
      } else {
        snappedTop = false; // 상단 구간에서 벗어나면 다시 스냅 가능하도록 해제
      }
    }
    stage.addEventListener("scroll", snapPageToRoadmapEnd, { passive: true });

    /* ---- 2b) 관성 스무스 스크롤 (stage 휠 전용) ---- */
    var smoothTo = null; // 현재 노드 이동에서 재사용

    if (!prefersReduced) {
      var EASE = 0.06; // 낮을수록 더 길게 미끄러짐(슬라이드 티 확실히 남)
      var WHEEL = 1.15; // 휠 한 번당 이동량(글라이드 거리 키움)
      var target = stage.scrollTop;
      var pos = stage.scrollTop;
      var raf = null;
      var smoothing = false;

      function maxY() {
        return stage.scrollHeight - stage.clientHeight;
      }
      function clamp(v) {
        return Math.max(0, Math.min(v, maxY()));
      }
      function loop() {
        pos += (target - pos) * EASE;
        if (Math.abs(target - pos) < 0.5) {
          pos = target;
          stage.scrollTop = Math.round(pos);
          raf = null;
          smoothing = false;
          return;
        }
        stage.scrollTop = Math.round(pos);
        raf = requestAnimationFrame(loop);
      }
      function kick() {
        if (raf === null) {
          smoothing = true;
          raf = requestAnimationFrame(loop);
        }
      }

      stage.addEventListener(
        "wheel",
        function (e) {
          var atTop = stage.scrollTop <= 0;
          var atBottom = stage.scrollTop >= maxY() - 1;
          // 맵 끝에서 더 밀면 페이지 스크롤에 양보(가둠 방지)
          if ((e.deltaY < 0 && atTop) || (e.deltaY > 0 && atBottom)) {
            return;
          }
          e.preventDefault();
          var d = e.deltaMode === 1 ? e.deltaY * 16 : e.deltaY;
          target = clamp(target + d * WHEEL);
          kick();
        },
        { passive: false }
      );

      // 네이티브/키보드/프로그램 스크롤이 나면 target 동기화(어긋남 방지)
      stage.addEventListener(
        "scroll",
        function () {
          if (!smoothing) {
            pos = target = stage.scrollTop;
          }
        },
        { passive: true }
      );

      smoothTo = function (top) {
        target = clamp(top);
        kick();
      };
    }

    /* ---- 3) 현재 학습 노드를 가운데로 (같은 관성으로 미끄러짐) ---- */
    var current = stage.querySelector(".roadmap-node.is-current");
    if (current) {
      requestAnimationFrame(function () {
        var top =
          current.offsetTop - stage.clientHeight / 2 + current.offsetHeight / 2;
        top = Math.max(0, top);
        if (smoothTo) {
          smoothTo(top);
        } else {
          stage.scrollTop = top;
        }
      });
    }
  }

  if (document.readyState === "loading") {
    document.addEventListener("DOMContentLoaded", init);
  } else {
    init();
  }
})();
