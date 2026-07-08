// 온보딩 결과 → 학습 메인 이동 오버레이.
// 결과 화면의 [data-launch] 링크를 누르면 바로 이동하지 않고, 로고+로딩바 오버레이를
// 잠깐 보여준 뒤 원래 href로 이동한다. (CSS·keyframes는 screens/onboarding.css)
(function () {
  function ready(fn) {
    if (document.readyState !== 'loading') fn();
    else document.addEventListener('DOMContentLoaded', fn);
  }

  ready(function () {
    var overlay = document.getElementById('onbLaunching');
    var triggers = document.querySelectorAll('[data-launch]');
    if (!overlay || !triggers.length) return;

    triggers.forEach(function (el) {
      el.addEventListener('click', function (e) {
        var href = el.getAttribute('href');
        if (!href) return;
        e.preventDefault();
        overlay.classList.add('is-on');
        overlay.setAttribute('aria-hidden', 'false');
        // 로딩바가 한 바퀴 도는 시간만큼 보여주고 이동
        setTimeout(function () { window.location.href = href; }, 1100);
      });
    });
  });
})();
