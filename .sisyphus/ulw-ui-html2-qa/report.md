# ui-html2 ULW QA Report

## Scope
- Target: `docs/ui-html2`
- Export source: `docs/figma export`
- Notion sources reviewed: `화면 설계`, `요구사항 명세서`, `디자인 명세`
- Date: 2026-06-15

## Inventory
- Figma export images: 82
- Non-image export file: `.DS_Store`
- Explicit SR labels found: SR-001, SR-002, SR-003, SR-004, SR-005, SR-006, SR-007, SR-008, SR-009, SR-011, SR-012, SR-014, SR-015
- Missing explicit SR labels in export filenames: SR-010, SR-013
- HTML screens under `docs/ui-html2/screens`: 19
- Shared component fragments under `docs/ui-html2/common/components`: 7

## Browser QA
- Render runs: 50
- Local link checks: 105
- HTTP failures: 0
- Broken local links: 0
- Console/page errors: 0
- Horizontal overflow: 0
- Screenshot evidence: `.sisyphus/ulw-ui-html2-qa/evidence/screenshots`
- Raw report: `.sisyphus/ulw-ui-html2-qa/evidence/ui-audit-report.json`

## Passed Checks
- SR-001 through SR-015 are represented in the HTML package.
- SR-010 is split into home, board, write, and profile entry points.
- Community home does not show a write form under the home content.
- Board page has separate write buttons outside the popular-post list.
- SR-012 mypage follows the export structure: profile panel, three summary cards, attendance calendar, recent learning, recent exam, community activity.
- Dark mode keeps the space theme while making section/card boundaries more readable.
- SR-014 ranking copy explicitly says attendance is not reflected in ranking score.
- General learning/practice screens state AI is unused; AI is limited to exam/analysis/admin AI-test context.

## Resolved Findings
1. Resolved: SR-010 profile no longer links directly to the write screen.
   - File: `docs/ui-html2/common/js/app.js`
   - Result: profile page now routes to the Java board. Write entry remains only on the board screen.

2. Resolved: topbar now tracks `헤더.png` as the navigation reference.
   - File: `docs/ui-html2/common/js/app.js`
   - File: `docs/ui-html2/common/components/topbar.html`
   - Result: topbar includes `data-export-source="docs/figma export/사용자/헤더.png"` and a reference image that loaded successfully in browser QA.

3. Resolved: shared component fragments no longer use root-absolute hrefs.
   - Files: `docs/ui-html2/common/components/topbar.html`, `docs/ui-html2/common/components/community-shell.html`, `docs/ui-html2/common/components/learning-console.html`, `docs/ui-html2/common/components/quiz-runner.html`, `docs/ui-html2/common/components/settings-dialog.html`, `docs/ui-html2/common/components/screen-sidebar.html`
   - Result: static fallback hrefs point to local prototype screens and Thymeleaf `th:href` attributes provide Spring context-path-safe routes.

## Verdict
The package is acceptable from the current evidence. Browser QA after the G002 fixes rendered 50 light/dark desktop/mobile runs with 104 local link checks, no broken links, no broken images, no runtime errors, no horizontal overflow, and no clipped text failures. SR-010 write-flow, header parity, and Thymeleaf fragment routing blockers are resolved.
