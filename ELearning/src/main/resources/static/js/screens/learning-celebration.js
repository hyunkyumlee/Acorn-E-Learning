/* =========================================================================
   learning-celebration.js — 행성 완료 · 레벨 달성 축하 연출 닫기
   - 연출을 띄울지는 서버가 정한다(세션 기준선 비교). 여기서는 닫기만 다룬다.
   - 닫는 방법: 버튼 / 카드 바깥 클릭 / ESC.
   - 열릴 때 CTA로 포커스를 옮기고, 닫으면 배경 스크롤을 되돌린다.
   - JS가 없거나 실패하면 CSS만으로도 카드가 보이고 CTA는 그대로 눌린다(가둠 방지).
   ========================================================================= */
(function () {
  "use strict";

  function init() {
    var overlay = document.querySelector("[data-celebrate]");
    if (!overlay) {
      return;
    }

    var cta = overlay.querySelector("[data-celebrate-close]");

    function close() {
      overlay.remove();
      document.body.style.overflow = "";
      document.removeEventListener("keydown", onKeydown);
    }

    function onKeydown(e) {
      if (e.key === "Escape") {
        close();
      }
    }

    // 연출이 떠 있는 동안에는 뒤 페이지가 따라 움직이지 않게 한다.
    document.body.style.overflow = "hidden";
    document.addEventListener("keydown", onKeydown);

    overlay.addEventListener("click", function (e) {
      // 카드 안을 누른 것은 닫기가 아니다. 바깥(오버레이 자신)을 눌렀을 때만 닫는다.
      if (e.target === overlay) {
        close();
      }
    });

    if (cta) {
      // CTA는 스크립트가 없을 때를 대비한 링크다. 스크립트가 살아 있으면 이동하지 않고 그 자리에서 닫는다.
      cta.addEventListener("click", function (e) {
        e.preventDefault();
        close();
      });
      cta.focus();
    }
  }

  if (document.readyState === "loading") {
    document.addEventListener("DOMContentLoaded", init);
  } else {
    init();
  }
})();
