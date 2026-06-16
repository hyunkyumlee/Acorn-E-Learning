<link rel="stylesheet" href="./global.css">

# Knowva Ion Light Theme CSS 사용 문서

이 문서는 `global.css`를 직접 불러와 확정된 `SR-001 E-L1 Daylight Orbit` 라이트 모드를 적용하는 샘플이다.

## 적용 기준

| 항목 | 값 |
|---|---|
| 최종 테마 | SR-001 E-L1 Daylight Orbit |
| 핵심 인상 | paper/ice light surface, electric cyan CTA, lime orbit signal |
| Primary CTA | `#00e5ff` 배경 + `#00161a` 텍스트 |
| 보조 포인트 | `#b6ff4d`, `#4f7cff` |
| Dark business color | `#070a18`, `#111833`, `#06131f`는 텍스트/깊이감/보조 dark token으로 유지 |
| Radius | `8px` |
| Font | `Pretendard`, `SUIT`, `Apple SD Gothic Neo`, `Noto Sans KR`, sans-serif |

## 실제 적용 샘플

<section class="kv-theme" data-theme="knowva-ion-light">
  <div class="kv-shell">
    <header class="kv-topbar">
      <a class="kv-brand" href="#">
        <span class="kv-brand-mark"></span>
        <span class="kv-brand-title">
          <strong>Knowva</strong>
          <span>Daylight Orbit</span>
        </span>
      </a>
      <nav class="kv-nav" aria-label="sample navigation">
        <a class="is-active" href="#">웰컴</a>
        <a href="#">튜토리얼</a>
        <a href="#">로그인</a>
      </nav>
    </header>

    <main class="kv-hero">
      <section class="kv-copy">
        <span class="kv-eyebrow"><i class="kv-dot"></i> 밝은 학습 궤도</span>
        <h1 class="kv-title">코딩 여정을<br>환한 궤도로.</h1>
        <p class="kv-lead">dark space의 무게를 덜고, Knowva의 cyan/lime business color를 밝은 paper surface 위에서 더 선명하게 보여준다.</p>
        <div class="kv-actions">
          <a class="kv-button kv-button-primary" href="#">튜토리얼 시작</a>
          <a class="kv-button" href="#">바로 로그인하기</a>
        </div>
        <div class="kv-login-row" aria-label="social login options">
          <a class="kv-button kv-button-ghost" href="#">Google로 계속</a>
          <a class="kv-button kv-button-ghost" href="#">Kakao로 계속</a>
        </div>
      </section>

      <section class="kv-visual" aria-label="Knowva daylight orbit visual">
        <div class="kv-stage"></div>
        <div class="kv-orbit"></div>
        <div class="kv-planet"></div>
        <div class="kv-moon kv-moon-one"></div>
        <div class="kv-moon kv-moon-two"></div>
        <aside class="kv-guide-card">
          <strong>Nova Guide</strong>
          <span>오늘의 mission과 test gate를 밝은 panel 위에서 먼저 보여준다.</span>
        </aside>
      </section>
    </main>
  </div>
</section>

## 개발 사용 규칙

- 페이지 루트에는 `.kv-theme`와 `data-theme="knowva-ion-light"`를 같이 둔다.
- 색상은 `--kv-color-*` token만 사용한다. 컴포넌트에서 raw hex를 반복하지 않는다.
- `#070a18`, `#111833`, `#06131f`는 배경 primary가 아니라 text/depth/dark accent로 쓴다.
- CTA는 `.kv-button kv-button-primary`를 기본으로 사용한다.
- panel은 `.kv-stage`, `.kv-guide-card`처럼 밝은 glass surface로 처리한다.
- `#4f7cff` 위에 흰색 본문을 올리지 않는다. contrast가 낮으므로 glow, border, orbit accent 용도로 제한한다.
- animation은 `transform`, `opacity` 중심으로 제한하고 `prefers-reduced-motion`을 반드시 유지한다.
