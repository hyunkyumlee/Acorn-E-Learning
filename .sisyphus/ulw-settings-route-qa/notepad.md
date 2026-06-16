# ULW Settings Route QA

## Objective
설정 화면에서 quiz/practice/quizrunner 맥락이 잘못 연결되어 있는지 검증하고, 실제 연결이면 분리한다. 전체 ui-html2 화면 route 연결성도 브라우저로 검증한다.

## Tier
HEAVY: route/wiring + settings flow + 전체 화면 연결성 검증. 3개 이상 surface와 browser QA 필요.

## Initial Finding
- `screens/settings/index.html` routes to `sr-015`.
- `sr-015` currently renders `settings-overlay-demo` with `dimmed-quiz`.
- The close link inside the settings dialog points to `screens/learning/practice.html`.
- `settingsModalPage()` also renders `dimmed-quiz` and a close link to `screens/learning/practice.html`.

## Planned Fix
- Make `sr-015`/`settingsModalPage()` render the normal settings profile screen instead of a quiz-backed overlay.
- Keep specific settings pages (`profile/security/social/system/payment`) as direct settings routes.
- Update local docs text that still describes SR-015 as "학습 중 설정 overlay".

## Evidence
- Failing-first before fix: `sr-015` renderer contained `settings-overlay-demo`, `dimmed-quiz`, and a close link to `screens/learning/practice.html`; `common/components/settings-dialog.html` also linked close to `../../screens/learning/practice.html`.
- Static after fix: `node --check docs/화면 설계/ui-html2/common/js/app.js` passed.
- Static after fix: grep for `settings-overlay-demo|dimmed-quiz|학습 중 설정 overlay` in `app.js`, `settings-dialog.html`, and `README.md` returned no settings renderer/doc hits.
- Browser after fix: `.sisyphus/ulw-settings-route-qa/evidence/full-route-qa.md` records 41 routes, 82 desktop/mobile route checks, 41 internal link targets, 0 route failures, 0 link failures.
- Settings flow after fix: clicking the topbar settings icon from `screens/learning/main.html` lands on `/ui-html2/screens/settings/index.html`; `hasSettingsPage=true`, `hasDimmedQuiz=false`, `hasSettingsOverlay=false`, `practiceLinksInSettings=0`, active nav `회원 정보`.
- Screenshot: `.sisyphus/ulw-settings-route-qa/evidence/settings-index-desktop.png`.
- Cleanup: Playwright browser/context/pages closed; no QA server spawned; no headless Chrome process left.

## Reviewer Round 1
- Result: REJECTION.
- Blocker 1: `review-diff.patch` did not include CSS and untracked screen HTML files, so the route additions were not auditable.
- Blocker 2: `full-route-qa.json` only had aggregate counts and failure arrays, not route-by-route and link-by-link detail.

## Reviewer Round 2 Evidence
- `review-diff.patch` regenerated as full UI evidence: tracked `ui-html2` diff (`review-diff-tracked-ui-html2.patch`, 1690 lines) + untracked screen HTML no-index patch (`review-diff-untracked-screens.patch`, 420 lines), combined 2110 lines.
- `review-ui-html2-status.txt` records tracked modified files plus untracked screen HTML wrappers.
- `screen-route-sha256.txt` records SHA-256 for all 41 screen HTML files.
- `full-route-qa.json` regenerated with detailed `routeResults` (82 entries) and `linkResults` (41 entries), not just counts.
- Re-run summary: route failures 0, link failures 0, settings flow still `hasDimmedQuiz=false`, `hasSettingsOverlay=false`, `practiceLinksInSettings=0`.

## Reviewer Round 2
- Result: UNCONDITIONAL APPROVAL.
- Approval notes: `settings/index.html` maps to `sr-015`; `settingsModalPage()` now returns `settingsProfilePage()` and no longer renders quiz overlay/background or practice close link. `settings-dialog.html` close link points to mypage. `README.md` no longer describes SR-015 as overlay. Detailed route/link QA covers 82/82 route checks and 41/41 link targets with zero failures.
