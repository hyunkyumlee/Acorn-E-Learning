# Knowva UI HTML2

Figma `사용자` 파일, Notion `화면 설계`, `요구사항 명세서`, `디자인 명세`, 그리고 `docs/figma export/사용자` PNG export 기준으로 다시 만든 정적 HTML 산출물이다.

## 반영 기준

- 최종 visual system: `SR-001 E-L1 Daylight Orbit`, light mode first.
- Figma 범위: `Page 1`, dev mode root link, `docs/figma export/사용자` 전체 PNG export.
- 화면 범위: SR-001부터 SR-015까지. 2026.06.16 기준 추가 export 반영으로 SR-007, SR-010, SR-011, SR-013, SR-015 상태 화면을 route 단위로 보강.
- 캐릭터/비즈니스 asset은 제외. planet, orbit, signal visual language만 사용.
- Spring Boot 전환을 고려해서 route file은 얇게 두고, 공통 shell/component는 `common/`에 분리.
- PNG export는 대부분 화면 상태/배치 검산용 기준으로 사용했다. 단, `헤더.png`는 navigation bar 기준 asset으로 topbar에 `data-export-source`와 reference image를 연결해 parity를 추적한다.

## 구조

- `index.html`: 개발/검수용 route index. 실제 SR-001 웰컴 화면은 `screens/welcome/index.html`.
- `common/css/styles.css`: 공통 layout/component CSS.
- `common/js/app.js`: 정적 prototype 렌더링과 tab 상태 전환.
- `common/components/`: Thymeleaf fragment 전환용 snippet.
- `screens/`: SR별 route HTML.

## Route 단위

| SR | 파일 | 구현 포인트 |
|---|---|---|
| SR-001 | `screens/welcome/index.html` | 실제 웰컴 페이지, 튜토리얼 5단계, 가입/로그인 CTA, Google/Kakao 소셜 로그인 CTA |
| SR-002 | `screens/auth/login.html`, `screens/auth/signup.html` | 이메일/Google/Kakao 인증 |
| SR-003 | `screens/learning/main.html` | Mission Control Roadmap |
| SR-004 | `screens/learning/onboarding.html` | 언어, 목표, 시작점, 레벨 테스트 |
| SR-005 | `screens/learning/curriculum.html` | 짧은 이론, 예시 코드, 저장/완료 |
| SR-006 | `screens/learning/practice.html` | 문제 유형별 상태, 정답/오답/완료 |
| SR-007 | `screens/learning/review.html`, `screens/learning/review-list.html` | 오답 복습 요약, 3열 오답 목록, 해설, 다시 풀기 |
| SR-008 | `screens/exam/coding-test.html` | 응시 조건, split editor, 제출, AI 대기 |
| SR-009 | `screens/analysis/index.html` | 기본/구매유도/Premium 분석 |
| SR-010 | `screens/community/index.html`, `screens/community/board.html`, `screens/community/detail.html`, `screens/community/write.html`, `screens/community/edit.html`, `screens/community/profile.html` | 자유/질문/공부 일지 게시판, 글 목록, 상세, 댓글/대댓글, 신고, 작성/수정, 커뮤니티 프로필 |
| SR-011 | `screens/payment/index.html`, `screens/payment/card.html`, `screens/payment/bank.html`, `screens/payment/complete.html`, `screens/payment/recommendations.html` | 결제 수단 선택, 신용카드, 무통장 입금, 결제 완료, 과목 콘텐츠 추천 |
| SR-012 | `screens/mypage/index.html` | 출석, 시험, 저장, 좋아요, 작성 글 |
| SR-013 | `screens/admin/login.html`, `screens/admin/dashboard.html`, `screens/admin/stats.html`, `screens/admin/courses.html`, `screens/admin/theory.html`, `screens/admin/problems.html`, `screens/admin/users.html`, `screens/admin/community.html`, `screens/admin/reports.html`, `screens/admin/notices.html` | 관리자 로그인, 홈, 통계, 학습 콘텐츠/사용자/커뮤니티/신고/공지 관리 |
| SR-014 | `screens/ranking/index.html` | 출석 제외 통합 랭킹 |
| SR-015 | `screens/settings/index.html`, `screens/settings/profile.html`, `screens/settings/security.html`, `screens/settings/social.html`, `screens/settings/system.html`, `screens/settings/payment.html` | 독립 설정 홈, 회원 정보, 이메일/비밀번호, 소셜 계정, 시스템 설정, 결제 정보 |
