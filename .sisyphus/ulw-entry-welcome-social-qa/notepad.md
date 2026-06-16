# ULW Entry / Welcome / Social Login QA Notepad

## Objective

- `docs/ui-html2/index.html`는 개발/검수용 화면 진입점으로 명확히 구분한다.
- `docs/ui-html2/screens/welcome/index.html`는 실제 SR-001 웰컴페이지로 유지한다.
- SR-001 웰컴페이지에 Google/Kakao 소셜 로그인 CTA를 추가한다.

## Tier

- HEAVY
- 근거: 사용자가 `ulw`를 명시했고, 이전 결과물 불만족 후 첫 진입/auth-facing UI 의미를 보정하는 작업이다.

## Criteria

- C001: 브라우저에서 `index.html`과 `screens/welcome/index.html`가 서로 다른 목적의 화면으로 읽힌다.
- C002: SR-001 웰컴에 Google/Kakao CTA가 보이고, `data-social-login` 마커와 로컬 200 링크를 가진다.
- C003: 기존 전체 화면 audit가 실패 없이 통과하고 reviewer gate가 APPROVE/CLEAR를 준다.

## Evidence Log

- pass: failing-first browser evidence captured at `.sisyphus/ulw-entry-welcome-social-qa/evidence/targeted-before.json`
  - `entryDistinct=false`
  - `welcomeSocialCount=0`
- pass: implementation touched `docs/ui-html2/index.html`, `docs/ui-html2/common/js/app.js`, `docs/ui-html2/common/css/styles.css`, `docs/ui-html2/README.md`
- pass: targeted browser QA captured at `.sisyphus/ulw-entry-welcome-social-qa/evidence/targeted-after.json`
  - `entryDistinct=true`
  - `welcomeDistinct=true`
  - `desktopSampleLayout=true`
  - `mobileCollapsed=true`
  - `socialPresent=true`
  - `socialLinksOk=true`
  - `noOverflow=true`
  - screenshots: `entry-desktop-light.png`, `entry-desktop-dark.png`, `welcome-desktop-light.png`, `welcome-desktop-dark.png`, `welcome-mobile-light.png`, `welcome-mobile-dark.png`
- pass: full route audit captured at `.sisyphus/ulw-ui-html2-qa/evidence/ui-audit-summary.json`
  - `screenHtml=19`
  - `componentHtml=7`
  - `pageRuns=50`
  - `linkChecks=104`
  - `failures=[]`
- pass: final reviewer returned `APPROVE - architect status CLEAR`
  - sample-like desktop two-column layout: clear
  - social login retained: clear
  - mobile collapse/no overflow: clear
  - dark mode legibility: clear
- pass: cleanup
  - local server on `127.0.0.1:4174` stopped
  - `lsof -nP -iTCP:4174 -sTCP:LISTEN` returned empty
