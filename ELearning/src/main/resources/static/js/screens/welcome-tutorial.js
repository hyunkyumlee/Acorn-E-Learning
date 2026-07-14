// SR-001 튜토리얼 — 웰컴 페이지 '안에서' 화면을 전환한다(별도 route/템플릿/매핑 없음).
// "튜토리얼 시작" → 웰컴 뷰를 감추고 튜토리얼 뷰로 교체. 슬라이드는 1장씩(제목+문장) 이동.
(function () {
    function ready(fn) {
        if (document.readyState !== 'loading') fn();
        else document.addEventListener('DOMContentLoaded', fn);
    }

    ready(function () {
        var openBtn = document.querySelector('[data-tutorial-open]');
        var welcomeView = document.querySelector('[data-welcome-view]');
        var tutorialView = document.querySelector('[data-tutorial-view]');
        var root = document.querySelector('[data-tut-root]');
        if (!openBtn || !welcomeView || !tutorialView || !root) return;

        var closeBtn = tutorialView.querySelector('[data-tutorial-close]');
        var slides = Array.prototype.slice.call(root.querySelectorAll('.tut-slide'));
        var dots = Array.prototype.slice.call(root.querySelectorAll('[data-tut-goto]'));
        var prevBtn = root.querySelector('[data-tut-prev]');
        var nextBtn = root.querySelector('[data-tut-next]');
        var liveRegion = root.querySelector('[data-tut-live]');
        var indexLabel = root.querySelector('[data-tut-index]');
        var mediaImg = root.querySelector('[data-tut-img]');
        var mediaLabel = root.querySelector('[data-tut-media-label]');
        var nuviEl = root.querySelector('[data-tut-nuvi]');
        if (!slides.length || !prevBtn || !nextBtn) return;

        var total = slides.length;
        var signupHref = nextBtn.getAttribute('data-signup-href') || '/signup';
        var current = 1;

        // 단계별 서비스 이미지 (data-img 있으면 <img>, 없으면 placeholder 라벨)
        function updateMedia(activeSlide) {
            if (!mediaImg) return;
            var src = activeSlide.getAttribute('data-img');
            if (src) {
                mediaImg.src = src;
                mediaImg.alt = activeSlide.querySelector('.tut-title').textContent + ' 안내 이미지';
                mediaImg.hidden = false;
                if (mediaLabel) mediaLabel.hidden = true;
            } else {
                mediaImg.hidden = true;
                if (mediaLabel) mediaLabel.hidden = false;
            }
        }

        // 단계별 누비 좌표/포즈 갱신 (data-nuvi-x/y/pose 없으면 숨김)
        function updateNuvi(activeSlide) {
            if (!nuviEl) return;
            var x = activeSlide.getAttribute('data-nuvi-x');
            var y = activeSlide.getAttribute('data-nuvi-y');
            var pose = activeSlide.getAttribute('data-nuvi-pose');
            if (!x || !y || !pose) {
                nuviEl.hidden = true;
                return;
            }
            nuviEl.hidden = false;
            nuviEl.style.left = x + '%';
            nuviEl.style.top = y + '%';
            nuviEl.setAttribute('data-pose', pose);
        }

        function render() {
            var activeSlide = slides[0];
            slides.forEach(function (slide) {
                var step = Number(slide.getAttribute('data-step'));
                var active = step === current;
                slide.classList.toggle('is-active', active);
                slide.hidden = !active;
                if (active) activeSlide = slide;
            });
            dots.forEach(function (dot) {
                var step = Number(dot.getAttribute('data-tut-goto'));
                var active = step === current;
                dot.classList.toggle('is-active', active);
                dot.setAttribute('aria-selected', String(active));
            });
            var isFirst = current === 1;
            prevBtn.disabled = isFirst;
            prevBtn.setAttribute('aria-disabled', String(isFirst));
            var isLast = current === total;
            nextBtn.textContent = isLast ? '시작하기' : '다음';
            if (indexLabel) indexLabel.textContent = String(current);
            if (liveRegion) liveRegion.textContent = current + ' / ' + total + '단계';
            updateMedia(activeSlide);
            updateNuvi(activeSlide);
        }

        function goTo(step) {
            current = Math.min(total, Math.max(1, step));
            render();
        }

        // ----- 화면 전환 (같은 페이지 내 뷰 교체) -----
        function openTutorial() {
            welcomeView.hidden = true;      // 웰컴 뷰 감춤
            tutorialView.hidden = false;    // 튜토리얼 뷰 표시(+페이드)
            current = 1;
            render();
            window.scrollTo(0, 0);
            root.focus();
        }
        function closeTutorial() {
            tutorialView.hidden = true;
            welcomeView.hidden = false;
            window.scrollTo(0, 0);
            openBtn.focus();                // 포커스 복귀(접근성)
        }

        openBtn.addEventListener('click', openTutorial);
        if (closeBtn) closeBtn.addEventListener('click', closeTutorial);

        prevBtn.addEventListener('click', function () { goTo(current - 1); });
        nextBtn.addEventListener('click', function () {
            if (current < total) goTo(current + 1);           // 마지막 전: 다음 슬라이드
            else window.location.href = signupHref;            // 마지막: 회원가입 이동
        });
        dots.forEach(function (dot) {
            dot.addEventListener('click', function () { goTo(Number(dot.getAttribute('data-tut-goto'))); });
        });

        // 키보드: ESC → 웰컴 복귀, ← / → → 단계 이동
        tutorialView.addEventListener('keydown', function (e) {
            if (e.key === 'Escape') closeTutorial();
            else if (e.key === 'ArrowLeft') goTo(current - 1);
            else if (e.key === 'ArrowRight') goTo(current + 1);
        });

        render();   // 초기(숨겨진 상태에서도 1단계로 세팅)
    });
})();