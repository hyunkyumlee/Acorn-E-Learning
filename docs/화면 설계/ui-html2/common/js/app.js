(() => {
  const body = document.body;
  const basePath = normalizeBase(body.dataset.basePath || "./");
  const screenId = body.dataset.screen || "index";
  const forcedTheme = ["dark", "light"].includes(body.dataset.forceTheme) ? body.dataset.forceTheme : "";

  const routes = [
    ["sr-001", "SR-001", "웰컴/튜토리얼", "screens/welcome/index.html", "welcome", "비로그인 소개, 튜토리얼, 가입/로그인 유도"],
    ["sr-002-login", "SR-002", "로그인", "screens/auth/login.html", "auth", "이메일/소셜 로그인, 상태 유지 안내"],
    ["sr-002-signup", "SR-002", "회원가입", "screens/auth/signup.html", "auth", "가입, 관심 과목, 목표 설정"],
    ["sr-003", "SR-003", "학습 메인", "screens/learning/main.html", "learning", "Mission Control Roadmap"],
    ["sr-004", "SR-004", "초기 설정", "screens/learning/onboarding.html", "learning", "언어, 목표, 시작점 선택"],
    ["sr-005", "SR-005", "이론 학습", "screens/learning/curriculum.html", "learning", "짧은 개념, 예시 코드, 바로 풀기"],
    ["sr-006", "SR-006", "일반 문제풀이", "screens/learning/practice.html", "learning", "short loop, 즉시 피드백, AI 미사용"],
    ["sr-007", "SR-007", "오답 복습", "screens/learning/review.html", "learning", "오답 복습 요약, 다시 풀기, 반복 오답"],
    ["sr-007-list", "SR-007", "오답 목록", "screens/learning/review-list.html", "learning", "오답 목록 3열 카드, 문제 내용 확인"],
    ["sr-008", "SR-008", "코딩테스트", "screens/exam/coding-test.html", "learning", "좌우 2분할, 3문항 일괄 제출, AI 채점"],
    ["sr-009", "SR-009", "AI 분석", "screens/analysis/index.html", "analysis", "기본 분석과 premium 분석 분리"],
    ["sr-010", "SR-010", "커뮤니티 홈", "screens/community/index.html", "community", "자유게시판 진입, 인기글, 커뮤니티 프로필"],
    ["sr-010-board", "SR-010", "게시판 글 목록", "screens/community/board.html", "community", "언어별 게시판, 전체/자유/질문/공부 일지 필터"],
    ["sr-010-detail", "SR-010", "게시글 상세", "screens/community/detail.html", "community", "본문, 댓글, 대댓글, 신고"],
    ["sr-010-write", "SR-010", "게시글 작성", "screens/community/write.html", "community", "게시판 안에서 진입하는 글쓰기 전용 화면"],
    ["sr-010-edit", "SR-010", "게시글 수정", "screens/community/edit.html", "community", "작성 후 수정 화면"],
    ["sr-010-profile", "SR-010", "커뮤니티 프로필", "screens/community/profile.html", "community", "나의 블로그형 커뮤니티 프로필"],
    ["sr-011", "SR-011", "결제 선택", "screens/payment/index.html", "analysis", "Premium 결제 수단 선택"],
    ["sr-011-card", "SR-011", "신용카드 결제", "screens/payment/card.html", "analysis", "카드 정보 입력, 주문 정보"],
    ["sr-011-bank", "SR-011", "무통장 입금", "screens/payment/bank.html", "analysis", "계좌 안내, 입금자 정보"],
    ["sr-011-complete", "SR-011", "결제 완료", "screens/payment/complete.html", "analysis", "결제 완료 후 홈 이동"],
    ["sr-011-recommend", "SR-011", "콘텐츠 추천", "screens/payment/recommendations.html", "analysis", "과목 영상, 기사, 개발 콘텐츠 추천"],
    ["sr-012", "SR-012", "마이페이지", "screens/mypage/index.html", "mypage", "학습/커뮤니티 활동 요약"],
    ["sr-013", "SR-013", "관리자 홈", "screens/admin/dashboard.html", "admin", "운영 요약, 빠른 이동, 최근 항목"],
    ["sr-013-login", "SR-013", "관리자 로그인", "screens/admin/login.html", "admin", "ROLE_ADMIN 로그인"],
    ["sr-013-stats", "SR-013", "관리자 통계", "screens/admin/stats.html", "admin", "필터, 통계 카드, 그래프, 테이블"],
    ["sr-013-courses", "SR-013", "과목/커리큘럼 관리", "screens/admin/courses.html", "admin", "과목, 커리큘럼, 활성화 관리"],
    ["sr-013-theory", "SR-013", "이론 자료 관리", "screens/admin/theory.html", "admin", "이론 자료 CRUD"],
    ["sr-013-problems", "SR-013", "일반 문제 관리", "screens/admin/problems.html", "admin", "문제, 정답, 해설, 난이도 관리"],
    ["sr-013-users", "SR-013", "사용자 관리", "screens/admin/users.html", "admin", "사용자 상태와 권한 관리"],
    ["sr-013-community", "SR-013", "커뮤니티 관리", "screens/admin/community.html", "admin", "게시글/댓글 운영 관리"],
    ["sr-013-reports", "SR-013", "신고 관리", "screens/admin/reports.html", "admin", "신고 처리와 조치 기록"],
    ["sr-013-notices", "SR-013", "공지사항 관리", "screens/admin/notices.html", "admin", "공지 등록, 수정, 공개 상태"],
    ["sr-014", "SR-014", "랭킹", "screens/ranking/index.html", "learning", "출석 제외 통합 랭킹"],
    ["sr-015", "SR-015", "설정", "screens/settings/index.html", "settings", "독립 설정 홈"],
    ["sr-015-profile", "SR-015", "회원 정보", "screens/settings/profile.html", "settings", "닉네임, 이메일, 가입일, 학습 언어"],
    ["sr-015-security", "SR-015", "이메일/비밀번호", "screens/settings/security.html", "settings", "로그인 이메일과 비밀번호 변경"],
    ["sr-015-social", "SR-015", "연동된 소셜 계정", "screens/settings/social.html", "settings", "Google/Kakao/Naver 연결 상태"],
    ["sr-015-system", "SR-015", "시스템 설정", "screens/settings/system.html", "settings", "알림, 테마, 접근성"],
    ["sr-015-payment", "SR-015", "결제 정보", "screens/settings/payment.html", "settings", "Premium 이용권과 결제 내역"],
    ["sample-learning-main", "EX-001", "학습 메인 예시", "screens/samples/learning-main.html", "learning", "AI티 제거 예시 · 학습 화면"],
    ["sample-community-board", "EX-002", "커뮤니티 게시판 예시", "screens/samples/community-board.html", "community", "AI티 제거 예시 · 커뮤니티"],
    ["sample-settings-index", "EX-003", "설정 예시", "screens/samples/settings-index.html", "settings", "AI티 제거 예시 · 설정"],
    ["sample-admin-stats", "EX-004", "관리자 통계 예시", "screens/samples/admin-stats.html", "admin", "AI티 제거 예시 · 관리자 통계"],
    ["sample-dark-orbital-ink", "EX-D1", "다크 샘플 A", "screens/samples/dark/orbital-ink.html", "learning", "Ion Cyan 우주 다크 샘플 · 기본안"],
    ["sample-dark-deep-observatory", "EX-D2", "다크 샘플 B", "screens/samples/dark/deep-observatory.html", "learning", "Ion Cyan 우주 다크 샘플 · 깊은 배경"],
    ["sample-dark-moon-dust", "EX-D3", "다크 샘플 C", "screens/samples/dark/moon-dust.html", "learning", "Ion Cyan 우주 다크 샘플 · 차분한 배경"]
  ].map(([id, sr, title, path, menu, desc]) => ({ id, sr, title, path, menu, desc }));

  const topNav = [
    ["learning", "학습", "screens/learning/main.html"],
    ["analysis", "분석", "screens/analysis/index.html"],
    ["community", "커뮤니티", "screens/community/index.html"],
    ["mypage", "마이페이지", "screens/mypage/index.html"]
  ];

  const route = routes.find((item) => item.id === screenId) || routes[0];

  function normalizeBase(value) {
    return value.endsWith("/") ? value : value + "/";
  }

  function href(path) {
    return basePath + path;
  }

  function exportHref(path) {
    return encodeURI(basePath + "../figma export/" + path);
  }

  function activeClass(condition) {
    return condition ? " is-active" : "";
  }

  function readStoredTheme() {
    if (forcedTheme) return forcedTheme;
    try {
      return localStorage.getItem("knowva-ui-theme") === "dark" ? "dark" : "light";
    } catch {
      return "light";
    }
  }

  function storeTheme(mode) {
    try {
      localStorage.setItem("knowva-ui-theme", mode);
      return true;
    } catch {
      return false;
    }
  }

  function applyTheme(mode) {
    const isDark = mode === "dark";
    body.classList.toggle("theme-dark", isDark);
    body.dataset.mode = isDark ? "dark" : "light";
    body.dataset.theme = isDark ? "knowva-ion-dark" : "knowva-ion-light";
    document.querySelectorAll("[data-theme-toggle]").forEach((button) => {
      button.setAttribute("aria-pressed", String(isDark));
      button.setAttribute("aria-label", isDark ? "라이트모드로 전환" : "다크모드로 전환");
    });
  }

  function topbar(activeMenu) {
    const links = topNav.map(([menu, label, path]) =>
      `<a class="nav-link${activeClass(menu === activeMenu)}" href="${href(path)}">${label}</a>`
    ).join("");
    const headerExportSrc = exportHref("사용자/헤더.png");

    return `
      <header class="topbar" data-export-source="docs/figma export/사용자/헤더.png">
        <img class="topbar-export-reference" src="${headerExportSrc}" alt="" aria-hidden="true" loading="eager">
        <a class="brand header-logo" href="${href("index.html")}" aria-label="Knowva UI index">
          <span class="brand-mark" aria-hidden="true">K</span>
          <span class="brand-copy"><strong>Knowva</strong><small>Learning Orbit</small></span>
        </a>
        <nav class="topbar-nav" aria-label="main navigation">${links}</nav>
        <div class="topbar-actions">
          <button class="header-icon theme-toggle" type="button" data-theme-toggle aria-pressed="false" aria-label="다크모드로 전환"><span class="theme-icon" aria-hidden="true"></span></button>
          <a class="header-icon settings-icon${activeClass(activeMenu === "settings")}" href="${href("screens/settings/index.html")}" aria-label="설정"><span aria-hidden="true">⚙</span></a>
          <a class="profile-pill${activeClass(activeMenu === "mypage")}" href="${href("screens/mypage/index.html")}" aria-label="마이페이지">
            <span class="profile-dot" aria-hidden="true">NL</span><span class="profile-label">프로필</span>
          </a>
        </div>
      </header>`;
  }

  function screenNav() {
    return `<nav class="screen-nav" aria-label="screen requirements">${
      routes.filter((item) => !item.id.includes("signup")).map((item) => `
        <a class="${activeClass(item.id === screenId || (screenId === "sr-002-signup" && item.id === "sr-002-login"))}" href="${href(item.path)}">
          <span>${item.sr}</span><small>${item.title}</small>
        </a>`).join("")
    }</nav>`;
  }

  function shell(activeMenu, content, options = {}) {
    const showScreenNav = options.showScreenNav === true;
    const showTopbar = options.showTopbar !== false;
    return `
      <div class="app-shell">
        ${showTopbar ? topbar(activeMenu) : ""}
        ${showScreenNav ? `<div class="layout">${screenNav()}<main class="content">${content}</main></div>` : `<main class="content">${content}</main>`}
      </div>`;
  }

  function pageHero(kicker, title, lead, actions = "") {
    return `
      <section class="page-hero compact">
        <span class="kicker"><i class="dot"></i>${kicker}</span>
        <h1 class="title small">${title}</h1>
        <p class="lead">${lead}</p>
        ${actions ? `<div class="button-row" style="margin-top:18px">${actions}</div>` : ""}
      </section>`;
  }

  function button(label, path, cls = "") {
    return `<a class="button ${cls}" href="${href(path)}">${label}</a>`;
  }

  function card(title, body, meta = "", action = "") {
    return `<article class="card">${meta ? `<span class="tag">${meta}</span>` : ""}<h3>${title}</h3><p>${body}</p>${action}</article>`;
  }

  function metric(value, label) {
    return `<div class="metric"><strong>${value}</strong><span>${label}</span></div>`;
  }

  function progress(value) {
    return `<div class="progress" aria-label="progress ${value}%"><span style="width:${value}%"></span></div>`;
  }

  function progressLine(label, value, detail = "") {
    return `<div class="progress-line"><div><strong>${label}</strong><span>${detail || value + "%"}</span></div>${progress(value)}</div>`;
  }

  function courseButton(label, detail, active = false) {
    return `<a class="course-button${activeClass(active)}" href="${href("screens/learning/main.html")}"><strong>${label}</strong><span>${detail}</span></a>`;
  }

  function choiceRow(label, text, state = "") {
    return `<button class="choice-row ${state}" type="button"><b>${label}</b><span>${text}</span></button>`;
  }

  function stepCard(index, title, desc, state = "") {
    return `<article class="step-card ${state}"><span>${index}</span><div><strong>${title}</strong><p>${desc}</p></div></article>`;
  }

  function statTile(label, value, tone = "") {
    return `<div class="stat-tile ${tone}"><span>${label}</span><strong>${value}</strong></div>`;
  }

  function barChart(values) {
    return `<div class="bar-chart" aria-label="growth chart">${values.map((value) => `<i style="height:${value}%"></i>`).join("")}</div>`;
  }

  function tabs(scope, items) {
    return `
      <section class="panel" data-tab-scope="${scope}">
        <div class="tabs" role="tablist" aria-label="${scope}">
          ${items.map((item, index) => `<button class="tab${activeClass(index === 0)}" type="button" role="tab" id="${scope}-${item.id}-tab" aria-controls="${scope}-${item.id}-panel" aria-selected="${index === 0}" data-tab="${item.id}">${item.label}</button>`).join("")}
        </div>
        ${items.map((item, index) => `<div class="tab-panel${activeClass(index === 0)}" role="tabpanel" id="${scope}-${item.id}-panel" aria-labelledby="${scope}-${item.id}-tab" data-tab-panel="${item.id}"${index === 0 ? "" : " hidden"}>${item.html}</div>`).join("")}
      </section>`;
  }

  function list(rows) {
    return `<div class="list">${rows.map((row) => `<div class="list-row"><div><strong>${row[0]}</strong><p>${row[1]}</p></div>${row[2] || ""}</div>`).join("")}</div>`;
  }

  function communityFilters(active = "free") {
    const filters = [
      ["all", "전체"],
      ["free", "자유"],
      ["question", "질문"],
      ["study-log", "공부 일지"]
    ];
    return `<nav class="community-filters" aria-label="게시판 필터">${filters.map(([id, label]) =>
      `<a class="badge${activeClass(id === active)}" href="${href(`screens/community/board.html#${id}`)}">${label}</a>`
    ).join("")}</nav>`;
  }

  function communityRail(active = "java") {
    const languages = [
      ["java", "Java", "자유 · 질문 · 공부 일지"],
      ["sql", "SQL", "자유 · 질문 · 공부 일지"],
      ["web", "HTML/CSS/JS", "자유 · 질문 · 공부 일지"],
      ["python", "Python", "자유 · 질문 · 공부 일지"]
    ];
    return `<aside class="community-rail">
      <h2>게시판</h2>
      <nav class="community-nav" aria-label="언어별 게시판">
        ${languages.map(([id, title, desc]) => `<a class="community-nav-item${activeClass(id === active)}" href="${href(`screens/community/board.html#${id}`)}"><strong>${title}</strong><span>${desc}</span></a>`).join("")}
      </nav>
      <article class="community-profile-mini">
        <span class="avatar"></span>
        <div><strong>nova_learner</strong><p>작성 글 12 · 스크랩 7 · 좋아요 31</p></div>
        ${button("내 커뮤니티 프로필", "screens/community/profile.html")}
      </article>
    </aside>`;
  }

  function postRows(type = "free") {
    const rows = type === "my"
      ? [
          ["오늘 학습 일지", "자유 · 댓글 5 · 좋아요 17 · 공개"],
          ["조건문 복습 질문", "질문 · 답변 2 · 해결됨"],
          ["오답 복습 기록", "자유 · 비공개 · 수정 가능"]
        ]
      : [
          ["조건문 edge case 정리", "질문 · 댓글 12 · 좋아요 48 · 스크랩 9"],
          ["SQL JOIN 시각화 자료", "공부 일지 · 댓글 8 · 좋아요 33 · 스크랩 21"],
          ["오늘 학습 일지", "자유 · 댓글 5 · 좋아요 17"],
          ["반복문 문제 풀이 팁", "공부 일지 · 댓글 3 · 스크랩 14"]
        ];
    return `<div class="post-list">${rows.map(([title, meta]) => `<a class="post-row" href="${href("screens/community/board.html#detail")}"><strong>${title}</strong><span>${meta}</span></a>`).join("")}</div>`;
  }

  const pages = {
    index() {
      const cards = routes.map((item) => `
        <a class="route-card" href="${href(item.path)}">
          <span class="tag">${item.sr}</span>
          <strong>${item.title}</strong>
          <span>${item.desc}</span>
        </a>`).join("");

      return shell("index", `
        <section class="page-hero route-index-hero" data-entry-label="route-index">
          <div class="hero-grid">
            <div>
              <span class="kicker"><i class="dot"></i>Developer review entry · Route index</span>
              <h1 class="title">Knowva screen route index</h1>
              <p class="lead">이 화면은 SR-001 웰컴 페이지가 아니라 개발/검수용 진입점이다. 각 route card로 SR-001~SR-015 정적 화면을 열어 Figma, Notion 화면 설계, 요구사항 명세 기준을 빠르게 확인한다.</p>
              <div class="button-row" style="margin-top:20px">
                ${button("SR-001 실제 화면 열기", "screens/welcome/index.html", "primary")}
                ${button("학습 메인 보기", "screens/learning/main.html")}
              </div>
            </div>
            <div class="visual-stage">
              <div class="orbit"></div><div class="big-planet"></div>
              <article class="card guide"><span class="tag">Route QA</span><h3>화면 진입점 검수</h3><p>이 index는 prototype shell과 공통 component 연결을 검수하는 개발용 map이며, 실제 사용자 시작 화면은 SR-001 route에서 확인한다.</p></article>
            </div>
          </div>
        </section>
        <section class="index-grid">${cards}</section>`, { noSidebar: true });
    },

    "sr-001"() {
      return shell("welcome", `
        <section class="welcome-start welcome-launch">
          <div class="welcome-copy">
            <span class="tag">SR-001 · Welcome</span>
            <h1>Knowva</h1>
            <p>처음 방문한 사용자는 튜토리얼과 서비스 소개만 확인하고, 학습 기록이 필요한 기능은 가입/로그인 후 시작한다.</p>
            <div class="button-row">
              ${button("튜토리얼 시작", "screens/welcome/index.html#tutorial", "primary")}
              ${button("바로 로그인하기", "screens/auth/login.html")}
              ${button("회원가입", "screens/auth/signup.html", "ghost")}
            </div>
            <div class="social-login-row" aria-label="소셜 로그인">
              <a class="button social-login google" href="${href("screens/auth/login.html#google")}" data-social-login="google">Google로 계속</a>
              <a class="button social-login kakao" href="${href("screens/auth/login.html#kakao")}" data-social-login="kakao">Kakao로 계속</a>
            </div>
          </div>
          <div class="welcome-visual" aria-label="Knowva welcome visual">
            <div class="welcome-orbit"><div class="big-planet"></div><div class="orbit"></div></div>
            <div class="welcome-moon one"></div>
            <div class="welcome-moon two"></div>
          </div>
        </section>
        <section id="tutorial" class="three-col">
          ${card("1. 목표 선택", "Java, SQL, HTML/CSS/JS, Python 중 시작할 궤도를 고른다.", "Tutorial 1")}
          ${card("2. 짧은 이론", "긴 문서 대신 현재 node에 필요한 개념만 먼저 보여준다.", "Tutorial 2")}
          ${card("3. 문제풀이 loop", "객관식, 빈칸, 코드 예측을 짧게 반복하고 즉시 피드백을 준다.", "Tutorial 3")}
          ${card("4. Test Gate", "진행률 조건을 충족하면 코딩테스트 gate가 열린다.", "Tutorial 4")}
          ${card("5. Signal 분석", "시험과 학습 기록을 바탕으로 다음 학습 방향을 제안한다.", "Tutorial 5", button("가입하고 시작", "screens/auth/signup.html", "primary"))}
          ${card("비로그인 경계", "문제풀이, 학습 기록, 프로필 설정은 로그인 후 가능하다는 안내를 명확히 둔다.", "Access rule")}
        </section>`, { showTopbar: false });
    },

    "sr-002-login"() {
      return shell("auth", `
        ${pageHero("SR-002 · 로그인", "다시 이어서 학습하기", "이메일 로그인과 Google/Kakao 진입을 분리하고, 로그인 후 학습 메인으로 이동한다.")}
        <section class="auth-grid">
          <div class="panel">
            <h2>로그인 후 이동 흐름</h2>
            <div class="timeline" style="margin-top:16px">
              ${["이메일 또는 소셜 계정 확인", "세션 유지 안내", "연속 학습 메시지 표시", "SR-003 학습 메인 진입"].map((txt, i) => `<div class="timeline-step"><b>${i + 1}</b><p>${txt}</p></div>`).join("")}
            </div>
          </div>
          <form class="form-panel">
            <h3>로그인</h3>
            <div class="form-grid">
              <input class="field" value="learner@knowva.dev" aria-label="email">
              <input class="field" value="••••••••" aria-label="password">
              <label class="footer-note"><input type="checkbox" checked> 로그인 상태 유지</label>
              <div class="button-row">${button("학습 메인으로", "screens/learning/main.html", "primary")}${button("회원가입", "screens/auth/signup.html")}</div>
              <div class="two-col">${card("Google", "Google 계정으로 계속", "Social")}${card("Kakao", "Kakao 계정으로 계속", "Social")}</div>
            </div>
          </form>
        </section>`, { showTopbar: false });
    },

    "sr-002-signup"() {
      return shell("auth", `
        ${pageHero("SR-002 · 회원가입", "학습 기록을 저장할 계정 만들기", "닉네임, 관심 과목, 목표를 가입 흐름에서 받되 초기 설정 화면으로 이어질 수 있게 가볍게 구성한다.")}
        <section class="two-col">
          <form class="form-panel">
            <h3>이메일 가입</h3>
            <div class="form-grid">
              <input class="field" value="nova_learner" aria-label="nickname">
              <input class="field" value="learner@knowva.dev" aria-label="email">
              <input class="field" value="Java backend" aria-label="interest">
              ${button("초기 설정으로 이동", "screens/learning/onboarding.html", "primary")}
            </div>
          </form>
          <div class="panel">
            <h2>가입 시점의 제안 보강</h2>
            ${list([
              ["학습 목표", "하루 20분, 주 5회, 코딩테스트 준비 중 하나를 선택하게 한다."],
              ["관심 과목", "과목은 onboarding에서 다시 바꿀 수 있음을 안내한다."],
              ["소셜 로그인", "Google/Kakao 가입은 같은 사용자 profile setup으로 이어진다."]
            ])}
          </div>
        </section>`, { showTopbar: false });
    },

    "sr-003"() {
      return shell("learning", `
        <section class="learning-console">
          <aside class="course-rail">
            <a class="brand-block" href="${href("index.html")}"><span class="brand-mark"></span><strong>Knowva</strong></a>
            <div class="rail-group">
              <span class="rail-title">과목 선택</span>
              ${courseButton("JAVA", "현재 학습 중", true)}
              ${courseButton("CSS", "다음 추천 과목")}
              ${courseButton("SQL", "기초 진행 중")}
            </div>
            <div class="rail-group">
              <span class="rail-title">학습 진행률</span>
              ${progressLine("반복문까지", 30, "3 / 10")}
              ${progressLine("전체 Java", 68, "68%")}
            </div>
            <a class="rank-link" href="${href("screens/ranking/index.html")}">리더보드 <strong>통합 15위</strong></a>
          </aside>
          <section class="roadmap-board">
            <div class="mission-banner">오늘의 목표: 반복문</div>
            <div class="roadmap-map">
              ${roadNode("완료", "JAVA 입문", "기초 문법 복습 가능", "done")}
              ${roadNode("완료", "변수와 자료형", "문제풀이 10/10", "done")}
              ${roadNode("현재", "조건문", "일반 문제 3/10 · 오늘 이어서 풀기", "current")}
              ${roadNode("Gate", "반복문", "이전 단원 7문제 완료 시 unlock", "gate")}
              ${roadNode("잠김", "배열", "반복문 완료 후 개방", "locked")}
            </div>
            <div class="button-row">
              ${button("학습 시작", "screens/learning/curriculum.html", "primary")}
              ${button("이전 단원 문제풀기", "screens/learning/practice.html")}
              ${button("오답 복습", "screens/learning/review.html")}
            </div>
          </section>
          <aside class="status-stack">
            <article class="status-panel">
              <h2>단원 진행 현황</h2>
              ${progressLine("[학습] 완료", 100, "100%")}
              ${progressLine("[문제풀이]", 30, "3 / 10")}
              <p class="divider-note">시험 gate까지: 7문제</p>
              <p>다음 단원: 배열 잠김</p>
            </article>
            <article class="status-panel">
              <h2>내 순위</h2>
              <div class="rank-list"><span>통합</span><strong>15위</strong></div>
              <div class="rank-list"><span>Java</span><strong>8위</strong></div>
              <div class="rank-list"><span>알고리즘</span><strong>12위</strong></div>
            </article>
            <article class="status-panel accent">
              <h2>연속 학습</h2>
              <p>3일째 유지 중. 출석은 랭킹 점수에 반영하지 않는다.</p>
            </article>
          </aside>
        </section>`);
    },

    "sr-004"() {
      return shell("learning", `
        ${pageHero("SR-004 · 초기 설정", "언어, 목표, 시작점을 빠르게 고르기", "Figma의 언어 선택, 시작점 선택, 테스트, 결과 화면을 하나의 onboarding flow로 묶었다.")}
        <section class="setup-board">
          <div class="setup-steps">
            ${stepCard("01", "언어 선택", "Java를 기본 선택 상태로 표시", "done")}
            ${stepCard("02", "목표 선택", "취업 준비와 개념 보강을 함께 선택", "current")}
            ${stepCard("03", "시작점 선택", "기초 시작 또는 레벨 테스트", "")}
          </div>
          <div class="language-grid">
            ${["Java", "CSS", "SQL", "Python"].map((name, index) => `<button class="language-card${activeClass(index === 0)}" type="button"><strong>${name}</strong><span>${index === 0 ? "선택됨" : "선택 가능"}</span></button>`).join("")}
          </div>
        </section>
        ${tabs("level-test", [
          { id: "start", label: "기초 시작", html: `<div class="decision-panel">${card("기초부터 시작", "레벨 테스트 없이 Java 입문 node부터 시작한다.", "Recommended", button("학습 메인으로", "screens/learning/main.html", "primary"))}${progressLine("초기 roadmap", 0, "0 / 10")}</div>` },
          { id: "test", label: "레벨 테스트", html: `<div class="test-preview"><div>${stepCard("Q1", "객관식", "문법 이해도 확인", "current")}${stepCard("Q2", "코드 결과 예측", "실행 흐름 확인")}${stepCard("Q3", "마지막 문제", "결과 산정")}</div>${card("결과 적용", "초기 level, 추천 단원, gate 조건을 계산하고 SR-003으로 이동한다. AI는 사용하지 않는다.", "Result", button("결과 적용", "screens/learning/main.html", "primary"))}</div>` }
        ])}`, { showTopbar: false });
    },

    "sr-005"() {
      return shell("learning", `
        ${pageHero("SR-005 · 커리큘럼/이론 학습", "짧은 개념에서 바로 문제로", "Figma의 이론 학습, 저장, 마지막 이론, 완료 표시 상태를 lesson page 내부 상태로 통합했다.")}
        <section class="lesson-player">
          <aside class="lesson-nav">
            <strong>JAVA · 조건문</strong>
            ${progressLine("이론", 100, "완료")}
            ${progressLine("문제풀이", 30, "3 / 10")}
            ${progressLine("시험 gate", 68, "68%")}
            <a class="button" href="${href("screens/learning/main.html")}">학습 메인</a>
          </aside>
          <article class="lesson-sheet">
            <span class="tag">Java · 조건문</span>
            <h2>if/else는 조건에 따라 실행 경로를 나누는 문법</h2>
            <p>현재 roadmap node에서 필요한 정의, 예시, 주의점만 먼저 보여준다. 저장 상태와 완료 상태는 하단 action에서 분리한다.</p>
            <pre class="code-pane">if (score >= 60) {
  pass = true;
} else {
  pass = false;
}</pre>
            <div class="lesson-callout">
              <strong>주의</strong>
              <p><code>&gt;=</code>는 기준값을 포함하고, <code>&gt;</code>는 포함하지 않는다.</p>
            </div>
            <div class="button-row">${button("바로 문제 풀기", "screens/learning/practice.html", "primary")}${button("학습 내용 저장", "screens/learning/curriculum.html")}${button("다음 단원", "screens/learning/main.html")}</div>
          </article>
          <aside class="lesson-state">
            <h2>학습 상태</h2>
            ${list([
              ["이론 저장", "중간 저장 후 같은 node로 복귀 가능"],
              ["마지막 이론", "다음 행동은 일반 문제풀이로 고정"],
              ["완료 표시", "roadmap node가 completed 상태로 전환"]
            ])}
          </aside>
        </section>`, { showTopbar: false });
    },

    "sr-006"() {
      return shell("learning", `
        <section class="focus-intro">
          <span class="tag">SR-006 · 일반 문제풀이 · AI 미사용</span>
          <strong>상단 진행률, 중앙 문제, 하단 action을 고정한 quiz runner</strong>
          <a class="button" href="${href("screens/learning/main.html")}">학습 메인</a>
        </section>
        ${tabs("practice-states", [
          { id: "quiz", label: "이론 문제", html: practiceCard("다음 중 GoF (Gang of Four) 디자인 패턴에 대한 설명으로 옳지 않은 것은?", ["Singleton 패턴은 클래스의 인스턴스가 오직 하나만 생성되도록 보장한다.", "Factory Method 패턴은 객체 생성을 서브 클래스에 위임한다.", "Observer 패턴은 한 객체의 상태 변화가 관련 객체들에게 자동으로 전파되도록 한다.", "Strategy 패턴은 알고리즘을 정의하고 런타임에 교체될 수 없도록 고정한다."], "확인") },
          { id: "blank", label: "빈칸 채우기", html: practiceCard("점수가 60 이상이면 pass가 true가 되도록 빈칸을 채워라.", ["score >= 60", "score == 0", "score < 30", "score != 60"], "제출", "A") },
          { id: "code", label: "간단 코드", html: `<div class="quiz-frame code-mode"><div class="quiz-top"><a class="close-link" href="${href("screens/learning/main.html")}">×</a><strong>4 / 10</strong>${progress(40)}</div><div class="code-workbench"><article><h2>간단 코드 작성</h2><p>age가 19 이상이면 adult를 true로 바꾸는 코드를 작성한다.</p><pre class="code-pane">let adult = false;
if (age >= 19) {
  adult = true;
}</pre></article><aside><h3>실행 결과</h3><p>테스트 전입니다.</p><button class="button primary" type="button">실행</button></aside></div></div>` },
          { id: "correct", label: "정답", html: `<div class="feedback-screen success"><strong>정답입니다</strong><p>해설을 확인한 뒤 다음 문제로 넘어간다.</p><div class="button-row">${button("다음 문제", "screens/learning/practice.html", "primary")}${button("해설 보기", "screens/learning/review.html")}</div></div>` },
          { id: "wrong", label: "오답", html: `<div class="feedback-screen danger"><strong>다시 확인하세요</strong><p>오답 queue에 자동 저장하고, 같은 유형을 다시 풀 수 있게 한다.</p><div class="button-row">${button("다시 풀기", "screens/learning/practice.html", "primary")}${button("오답 복습", "screens/learning/review.html")}</div></div>` },
          { id: "done", label: "완료", html: `<div class="feedback-screen complete"><strong>10문항 완료</strong><p>진행률, XP, gate 조건을 갱신했다. 다음 단원 또는 오답 복습으로 이동한다.</p><div class="button-row">${button("학습 메인", "screens/learning/main.html", "primary")}${button("다음 단원", "screens/learning/curriculum.html")}</div></div>` }
        ])}`, { showTopbar: false });
    },

    "sr-007"() {
      return shell("learning", `
        ${pageHero("SR-007 · 오답 복습", "오답을 복습 mission으로 전환", "250:66의 오답 복습 목록/상세 화면을 반영했다. 실패 이력이 아니라 다음 행동으로 읽히게 구성한다.")}
        <section class="two-col">
          <div class="panel">
            <h2>오답 목록</h2>
            ${list([
              ["조건문 edge case", "반복 오답 2회 · 추천 복습"],
              ["비교 연산자", "최근 오답 · 해설 확인 가능"],
              ["중첩 if", "코딩테스트 전 복습 권장"]
            ])}
          </div>
          <div class="panel">
            <h2>해설과 다시 풀기</h2>
            <p>조건이 같은 값일 때 <code>&gt;=</code>와 <code>&gt;</code>의 차이를 확인해야 한다.</p>
            <pre class="code-pane">score >= 60 // 60점 포함
score > 60  // 60점 제외</pre>
            <div class="button-row">${button("다시 풀기", "screens/learning/practice.html", "primary")}${button("학습 메인", "screens/learning/main.html")}</div>
          </div>
        </section>`);
    },

    "sr-008"() {
      return shell("learning", `
        <section class="focus-intro exam-intro">
          <span class="tag">SR-008 · 시험/코딩테스트</span>
          <strong>3문항을 풀고 한 번에 제출</strong>
          ${button("분석 결과 보기", "screens/analysis/index.html")}
        </section>
        ${tabs("exam-states", [
          { id: "ready", label: "응시 가능", html: examPane("응시 조건 달성", "Java Level 2 gate가 열렸다. 3문항을 모두 푼 뒤 제출한다.", true) },
          { id: "locked", label: "응시 조건 미달", html: card("진행률 80% 달성 시 unlock", "현재 68%. 추천 복습과 일반 문제풀이를 먼저 완료해야 한다.", "Gate locked", button("일반 문제풀이", "screens/learning/practice.html", "primary")) },
          { id: "submit", label: "제출", html: examPane("제출 전 확인", "3문항 답안이 모두 작성되었다. 제출 후 AI 채점 대기 상태로 전환된다.", true, "일괄 제출") },
          { id: "waiting", label: "AI 채점 대기", html: card("AI 채점 중", "빈 화면 대신 처리 중 상태, 예상 대기 시간, 실패 시 재시도 안내를 제공한다.", "NFR-005", button("결과 분석으로", "screens/analysis/index.html", "primary")) }
        ])}`, { showTopbar: false });
    },

    "sr-009"() {
      return shell("analysis", `
        ${pageHero("SR-009 · AI 분석 리포트", "기본 분석은 무료, 상세 분석은 premium", "Figma export의 기본/구매유도/premium 화면을 리포트 페이지로 정리했다.")}
        <section class="report-summary">
          ${statTile("총학습일", "45일")}
          ${statTile("총문제수", "326개")}
          ${statTile("평균정답률", "78%", "cyan")}
          ${statTile("현재레벨", "Lv.12", "lime")}
        </section>
        <section class="analysis-grid">
          <article class="analysis-card">
            <h2>성장 그래프</h2>
            ${barChart([24, 38, 54, 72, 76, 80, 92])}
          </article>
          <article class="analysis-card">
            <h2>분야별 역량분석</h2>
            <p>기초 문법 영역의 학습 성취도가 높게 나타났다. 반복문 활용 능력 향상을 위해 추가 문제 풀이를 권장한다.</p>
          </article>
          <article class="analysis-card wide">
            <h2>최근 응시 테스트 · 반복문 단원평가</h2>
            <div class="result-metrics">${statTile("점수", "78점")}${statTile("정답", "8 / 10")}${statTile("응시일", "2026.06.15")}</div>
            <p>최근 응시한 반복문 단원 평가를 완료했다. 평가 결과를 바탕으로 학습 현황을 확인하고 다음 단계 진입을 준비한다.</p>
          </article>
        </section>
        ${tabs("analysis", [
          { id: "free", label: "기본", html: `<div class="locked-preview"><div class="preview-card"><h3>AI 종합분석</h3><p>분석 그래프, 강점, 취약점, 오답 분석은 premium에서 열린다.</p></div><div class="preview-card"><h3>AI 추천 로드맵</h3><p>다음 학습 단원과 복습 순서를 제안한다.</p></div></div>` },
          { id: "upsell", label: "구매 유도", html: `<div class="premium-cta"><strong>Premium 보기</strong><p>문항별 채점 근거, 취약점, 추천 roadmap을 더미 결제 후 확인한다.</p>${button("더미 결제", "screens/payment/index.html", "primary")}</div>` },
          { id: "premium", label: "Premium", html: `<div class="three-col">${card("오답 유형", "경계값 판단 누락 42%, 조건 순서 오류 28%.", "Detail")}${card("다음 학습", "반복문 전 조건문 복습 mission 1회 권장.", "Recommendation")}${card("AI 고지", "AI 판단 기반 분석이며 최종 평가는 학습/시험 기록과 함께 봐야 한다.", "Notice")}</div>` }
        ])}`);
    },

    "sr-010"() {
      return shell("community", `
        ${pageHero("SR-010 · 자유게시판", "커뮤니티 진입 화면은 자유게시판", "Figma export의 SR-010 자유게시판 화면을 홈으로 두고, 글쓰기와 내 커뮤니티 프로필은 별도 화면으로 이동시킨다.")}
        <section class="community-layout">
          ${communityRail("java")}
          <main class="community-board">
            <div class="search-strip">
              <input class="field" value="조건문" aria-label="커뮤니티 검색">
              <button class="button primary" type="button">검색</button>
            </div>
            ${communityFilters("free")}
            <section class="board-grid">
              <article class="panel">
                <h2>자유게시판 인기글</h2>
                ${postRows("free")}
              </article>
              <article class="panel">
                <div class="community-home-actions">
                  ${button("Java 게시판으로 이동", "screens/community/board.html#java", "primary")}
                  ${button("내 커뮤니티 프로필", "screens/community/profile.html")}
                </div>
                <h2>인기 작성자</h2>
                ${list([["orbit.dev", "Java 답변 18개 · 좋아요 420"], ["signal.sql", "SQL 공부 일지 12개 · 스크랩 88"]])}
              </article>
            </section>
          </main>
        </section>`);
    },

    "sr-010-board"() {
      return shell("community", `
        ${pageHero("SR-010 · Java 게시판", "게시판을 선택하면 글 목록 화면으로 이동", "전체/자유/질문/공부 일지는 이 화면 안에서 글 목록 필터처럼 동작한다.", button("글쓰기", "screens/community/write.html", "primary"))}
        <section class="community-layout">
          ${communityRail("java")}
          <main class="community-board">
            <div class="board-toolbar">
              <div><span class="tag">Java</span><h2>글 목록</h2></div>
              ${button("글쓰기", "screens/community/write.html", "primary")}
            </div>
            ${communityFilters("all")}
            <div class="search-strip">
              <input class="field" value="검색어를 입력하세요" aria-label="게시판 검색">
              <button class="button" type="button">검색</button>
            </div>
            ${postRows("free")}
            <section class="two-col">
              ${card("게시글 상세", "글을 선택하면 댓글, 좋아요, 스크랩, 신고 action을 같은 상세 route에서 처리한다.", "Detail")}
              ${card("게시판 이동", "SQL, HTML/CSS/JS, Python 게시판도 같은 글 목록 template을 사용한다.", "Shared template")}
            </section>
          </main>
        </section>`);
    },

    "sr-010-write"() {
      return shell("community", `
        ${pageHero("SR-010 · 글쓰기", "게시판 안에서 글쓰기 화면으로 진입", "홈 하단에 노출하지 않고, 언어별 게시판의 글쓰기 버튼을 눌렀을 때만 열린다.")}
        <section class="write-layout">
          <aside class="write-context">
            <span class="tag">Java · 자유</span>
            <h2>작성 위치</h2>
            <p>현재 선택한 언어/게시판 context를 유지한 채 글을 작성한다.</p>
            ${button("글 목록으로", "screens/community/board.html#free")}
          </aside>
          <form class="panel form-grid">
            <label>게시판<select class="field"><option>Java · 자유 게시판</option><option>Java · 질문 게시판</option><option>Java · 공부 일지 게시판</option></select></label>
            <label>제목<input class="field" value="조건문 학습 기록 공유"></label>
            <label>내용<textarea class="field">오늘 조건문 문제를 풀면서 헷갈렸던 부분과 해결 과정을 정리합니다.</textarea></label>
            <div class="button-row">${button("등록", "screens/community/board.html#free", "primary")}${button("취소", "screens/community/board.html#free")}</div>
          </form>
        </section>`);
    },

    "sr-010-profile"() {
      return shell("community", `
        ${pageHero("SR-010 · 나의 커뮤니티 프로필", "작성 글이 모이는 개인 블로그형 공간", "커뮤니티 홈의 프로필 버튼에서 이동한다. 마이페이지와 분리해서 커뮤니티 활동만 보여준다.")}
        <section class="community-profile-page">
          <aside class="community-profile-hero">
            <span class="avatar large"></span>
            <h2>nova_learner.log</h2>
            <p>Java backend 학습 기록과 질문 답변을 모아둔 커뮤니티 프로필</p>
            <div class="metric-row">${metric("12", "작성 글")}${metric("7", "스크랩")}${metric("31", "받은 좋아요")}</div>
            ${button("마이페이지", "screens/mypage/index.html")}
          </aside>
          <main class="panel">
            <div class="board-toolbar">
              <div><span class="tag">Pinned</span><h2>대표 글</h2></div>
              ${button("Java 게시판으로 이동", "screens/community/board.html#java", "primary")}
            </div>
            ${postRows("my")}
            <div class="two-col" style="margin-top:18px">
              ${card("활동 배지", "답변 10개 이상, Java 질문 해결률 72%.", "Profile")}
              ${card("커뮤니티 소개", "오답을 기록하고 같은 문제를 겪는 학습자에게 답변을 남긴다.", "Bio")}
            </div>
          </main>
        </section>`);
    },

    "sr-011"() {
      return shell("analysis", `
        ${pageHero("SR-011 · 더미 결제", "실제 PG 없이 premium 접근 상태만 기록", "Figma의 신용카드 결제, 무통장 입금, 결제 완료 화면을 더미 결제 상태로 구분했다.")}
        <section class="payment-grid">
          <aside class="payment-summary">
            <span class="tag">Premium Signal</span>
            <h2>상세 분석 접근</h2>
            <p>문항별 AI 채점 근거, 취약점, 추천 복습 단원 상세를 활성화한다.</p>
            ${statTile("결제 방식", "Dummy")}
            ${statTile("실제 PG", "호출 없음", "lime")}
          </aside>
          <form class="payment-form">
            <h2>더미 결제</h2>
            <label>결제 수단<select class="field"><option>신용카드 더미</option><option>무통장 입금 더미</option></select></label>
            <label>카드 번호<input class="field" value="**** **** **** 4242"></label>
            <label>결제 메모<input class="field" value="Premium 분석 테스트 결제"></label>
            <div class="button-row">${button("더미 결제 완료", "screens/payment/index.html#complete", "primary")}${button("분석으로 돌아가기", "screens/analysis/index.html")}</div>
          </form>
          <article id="complete" class="payment-status">
            <h2>결제 상태</h2>
            ${list([["대기", "결제 요청 생성"], ["완료", "상태 paid 기록"], ["접근", "SR-009 Premium 분석 활성화"]])}
            <p>실제 결제는 발생하지 않는다. 결제 상태만 더미 데이터로 기록한다.</p>
            <div class="button-row">${button("Premium 분석 보기", "screens/analysis/index.html", "primary")}${button("설정 결제 정보", "screens/settings/index.html")}</div>
          </article>
        </section>`);
    },

    "sr-012"() {
      return shell("mypage", `
        <section class="mypage-page">
          <header class="mypage-heading">
            <h1>마이페이지</h1>
            <p>나의 학습 활동과 기록을 한눈에 확인하세요.</p>
          </header>

          <section class="mypage-profile-panel">
            <div class="mypage-profile-avatar" aria-label="프로필 아바타"><span>NL</span></div>
            <div class="mypage-profile-copy">
              <h2>이름</h2>
              <p>꾸준히 성장하는 개발자가 되고 싶어요!</p>
              <a class="mini-button" href="${href("screens/settings/index.html")}">프로필 편집</a>
            </div>
            <div class="mypage-profile-divider"></div>
            <dl class="mypage-profile-meta">
              <div><dt>현재 레벨</dt><dd>Lv.5</dd></div>
              <div><dt>대표 과목</dt><dd>Java</dd></div>
            </dl>
          </section>

          <section class="mypage-summary-grid">
            <article class="mypage-stat-card">
              <span class="mypage-icon book" aria-hidden="true"></span>
              <div><h2>연속 출석</h2><strong>10일</strong><p>계속해서 학습을 이어가세요</p></div>
            </article>
            <article class="mypage-stat-card">
              <span class="mypage-icon test" aria-hidden="true"></span>
              <div><h2>최근 시험 결과</h2><strong>합격</strong><p>SQL Lv.5</p></div>
            </article>
            <article class="mypage-stat-card">
              <span class="mypage-icon ranking" aria-hidden="true"></span>
              <div><h2>전체 랭킹</h2><strong>10위</strong><p>50,000점</p></div>
            </article>
          </section>

          <section class="mypage-section attendance-section">
            <div class="mypage-section-head">
              <h2>출석 캘린더</h2>
              <div class="month-control"><span>2026년 6월</span><button type="button" aria-label="이전 달">&lt;</button><button type="button" aria-label="다음 달">&gt;</button></div>
            </div>
            <div class="attendance-grid">
              <div class="calendar-box">
                <div class="calendar-orbit-head">
                  <strong>6월 출석</strong>
                  <span>10일 연속</span>
                </div>
                <div class="calendar-weekdays" aria-hidden="true">
                  <span>월</span><span>화</span><span>수</span><span>목</span><span>금</span><span>토</span><span>일</span>
                </div>
                <div class="calendar-days" aria-label="2026년 6월 출석 캘린더">
                  <span>1</span><span>2</span><span>3</span><span>4</span><span>5</span><span class="attended">6</span><span class="attended">7</span>
                  <span class="attended">8</span><span class="attended">9</span><span class="attended">10</span><span class="attended">11</span><span class="attended">12</span><span class="attended">13</span><span class="attended">14</span>
                  <span class="today attended">15</span><span>16</span><span>17</span><span>18</span><span>19</span><span>20</span><span>21</span>
                  <span>22</span><span>23</span><span>24</span><span>25</span><span>26</span><span>27</span><span>28</span>
                  <span>29</span><span>30</span><span class="muted" aria-hidden="true"></span><span class="muted" aria-hidden="true"></span><span class="muted" aria-hidden="true"></span><span class="muted" aria-hidden="true"></span><span class="muted" aria-hidden="true"></span>
                </div>
              </div>
              <article class="streak-card">
                <span class="circle-icon streak">10</span>
                <div>
                  <h3>10일 연속 출석 중!</h3>
                  <p>조금만 더 힘내요!</p>
                </div>
                <div class="attendance-progress">
                  <span>이번 달 출석률</span>
                  <div class="mini-progress"><i style="width:50%"></i></div>
                  <strong>50%</strong>
                </div>
                <dl><dt>총 출석일</dt><dd>25일</dd></dl>
              </article>
            </div>
          </section>

          <section class="mypage-detail-grid">
            <article class="mypage-section list-section">
              <div class="mypage-section-head"><h2>최근 학습 현황</h2><a class="mini-button" href="${href("screens/learning/main.html")}">전체보기</a></div>
              <div class="learning-status-list">
                <div class="learning-status-row"><span class="language-thumb java">J</span><strong>Java</strong><span>Lv.1</span><div class="mini-progress"><i style="width:30%"></i></div><b>30%</b></div>
                <div class="learning-status-row"><span class="language-thumb python">Py</span><strong>Python</strong><span>Lv.3</span><div class="mini-progress"><i style="width:80%"></i></div><b>80%</b></div>
                <div class="learning-status-row"><span class="language-thumb sql">SQL</span><strong>SQL</strong><span>Lv.6</span><div class="mini-progress"><i style="width:50%"></i></div><b>50%</b></div>
              </div>
            </article>
            <article class="mypage-section list-section">
              <div class="mypage-section-head"><h2>최근 시험 결과</h2><a class="mini-button" href="${href("screens/exam/coding-test.html")}">전체보기</a></div>
              <div class="exam-result-list">
                <div class="exam-result-row"><strong>SQL</strong><span>Lv.5</span><b class="result-badge">합격</b></div>
                <div class="exam-result-row"><strong>Python</strong><span>Lv.2</span><b class="result-badge">재시</b></div>
              </div>
            </article>
          </section>

          <section class="mypage-section community-activity-section">
            <h2>커뮤니티 활동</h2>
            <div class="community-activity-grid">
              <article class="community-activity-card">
                <span class="mypage-icon heart" aria-hidden="true"></span>
                <h3>좋아요 누른 글</h3>
                <a class="mini-button" href="${href("screens/community/board.html")}">보기</a>
                <strong>10개</strong>
                <p>관심 있는 게시글 모음</p>
              </article>
              <article class="community-activity-card">
                <span class="mypage-icon bookmark" aria-hidden="true"></span>
                <h3>스크랩한 글</h3>
                <a class="mini-button" href="${href("screens/community/board.html")}">보기</a>
                <strong>10개</strong>
                <p>나중에 다시 볼 글 모음</p>
              </article>
              <article class="community-activity-card">
                <span class="mypage-icon pencil" aria-hidden="true"></span>
                <h3>작성한 글</h3>
                <a class="mini-button" href="${href("screens/community/profile.html")}">보기</a>
                <strong>10개</strong>
                <p>내가 작성한 게시글</p>
              </article>
            </div>
          </section>
        </section>`);
    },

    "sr-013"() {
      return shell("admin", `
        ${pageHero("SR-013 · 관리자", "ROLE_ADMIN 단일 권한 운영 화면", "Figma의 관리자 로그인, 홈, 통계, 과목/커리큘럼, 이론 자료, 일반 문제, 사용자, 커뮤니티, 신고, 공지사항 관리 화면을 한 shell로 정리했다.")}
        <section class="admin-grid">
          <aside class="admin-rail">
            <div class="admin-login-card">
              <span class="tag">ROLE_ADMIN</span>
              <strong>관리자 로그인 완료</strong>
              <small>admin@knowva.local</small>
            </div>
            <h2>Admin Nav</h2>
            ${list([["홈", "운영 요약"], ["통계", "일별 · 월별 · 과목별"], ["콘텐츠", "이론 자료 · 일반 문제"], ["운영", "사용자 · 커뮤니티 · 신고 · 공지"]])}
          </aside>
          <div class="panel">
            <div class="three-col">${metric("1,284", "오늘 문제풀이")}${metric("63", "AI 시험 제출")}${metric("8", "신고 대기")}</div>
            <div class="table-card" style="margin-top:18px">
              <table class="table"><thead><tr><th>관리 영역</th><th>상태</th><th>다음 작업</th></tr></thead><tbody>
                <tr><td>일반 문제</td><td>활성 312개</td><td>난이도 태그 점검</td></tr>
                <tr><td>커뮤니티 신고</td><td>대기 8건</td><td>처리 결과 기록</td></tr>
                <tr><td>공지사항</td><td>게시 3건</td><td>신규 공지 등록</td></tr>
              </tbody></table>
            </div>
          </div>
        </section>
        ${tabs("admin", [
          { id: "stats", label: "통계", html: `<div class="three-col">${card("일별", "오늘 문제풀이 1,284건, 시험 제출 63건.", "Daily")}${card("월별", "이번 달 활성 사용자 842명.", "Monthly")}${card("과목별", "Java 48%, SQL 24%, Python 18%.", "Subject")}</div>` },
          { id: "content", label: "콘텐츠", html: list([["이론 자료 관리", "과목/커리큘럼별 등록, 조회, 수정, 삭제"], ["일반 문제 관리", "문제 유형, 정답, 해설, 난이도, 활성화 상태 관리"], ["AI 시험 문제", "AI 생성 문제는 관리자 검수 없이 바로 출제"]]) },
          { id: "ops", label: "운영", html: list([["사용자 관리", "사용자 상태와 권한 확인"], ["커뮤니티 관리", "게시글, 댓글, 신고 처리"], ["공지사항", "공지 등록, 수정, 게시 상태 관리"]]) }
        ])}`);
    },

    "sr-014"() {
      return shell("learning", `
        ${pageHero("SR-014 · 랭킹", "출석 제외 통합 랭킹", "문제풀이, 시험 점수, 일일 학습 횟수를 기준으로 통합 랭킹을 제공한다. 출석은 점수에서 제외한다.")}
        <section class="panel">
          <div class="button-row" style="justify-content:space-between"><h2>주간 랭킹</h2><span class="badge">출석은 랭킹 점수에 반영되지 않습니다</span></div>
          <div class="filter-row"><span class="badge">통합</span><span class="badge">Java</span><span class="badge">SQL</span><span class="badge">주간</span><span class="badge">등급별</span></div>
          <div class="table-card"><table class="table"><thead><tr><th>순위</th><th>학습자</th><th>과목</th><th>점수 기준</th><th>점수</th></tr></thead><tbody>
            <tr><td>1</td><td>orbit.dev</td><td>Java</td><td>문제풀이 48 · 시험 2 · 학습 5</td><td>8,420</td></tr>
            <tr><td>18</td><td>nova_learner</td><td>Java</td><td>문제풀이 22 · 시험 1 · 학습 3</td><td>4,180</td></tr>
            <tr><td>19</td><td>signal.sql</td><td>SQL</td><td>문제풀이 20 · 시험 1 · 학습 4</td><td>4,050</td></tr>
          </tbody></table></div>
        </section>`);
    },

    "sr-015"() {
      return settingsProfilePage();
    }
  };

  function roadNode(label, title, desc, state) {
    return `<div class="road-node"><span class="planet ${state}"></span><div><strong>${title}</strong><p>${desc}</p></div><span class="badge">${label}</span></div>`;
  }

  function practiceCard(question, options, cta, selected = "") {
    return `<div class="quiz-frame">
      <div class="quiz-top"><a class="close-link" href="${href("screens/learning/main.html")}">×</a><strong>3 / 10</strong>${progress(30)}</div>
      <article class="quiz-question">
        <h2>${question}</h2>
        <div class="choice-list">${options.map((opt, i) => {
          const label = String.fromCharCode(65 + i);
          return choiceRow(label, opt, selected === label ? "selected" : "");
        }).join("")}</div>
      </article>
      <div class="quiz-bottom">
        <button class="button" type="button">건너뛰기</button>
        <p>건너뛰기 누르면 오답 처리됩니다</p>
        <button class="button primary" type="button">${cta}</button>
      </div>
    </div>`;
  }

  function examPane(title, desc, enabled, cta = "풀이 저장") {
    return `<div class="exam-studio">
      <aside class="exam-problem">
        <span class="tag">${enabled ? "응시 조건 달성" : "응시 조건 미달"}</span>
        <h2>${title}</h2>
        <p>${desc}</p>
        <dl>
          <div><dt>입력</dt><dd>3 5</dd></div>
          <div><dt>출력</dt><dd>8</dd></div>
          <div><dt>제한사항</dt><dd>정수만 사용 · Scanner 가능</dd></div>
        </dl>
        <div class="button-row"><button class="button" type="button">이전 문제</button><button class="button" type="button">다음 문제</button></div>
        ${progressLine("진행률", 33, "1 / 3")}
      </aside>
      <section class="exam-editor">
        <div class="editor-pane"><h2>코드 입력</h2><pre class="code-pane">public boolean canPass(int score) {
  if (score >= 60) {
    return true;
  }
  return false;
}</pre></div>
        <div class="result-pane"><h2>코드 실행 결과</h2><p>${enabled ? "아직 실행하지 않았습니다." : "응시 조건을 달성해야 실행할 수 있습니다."}</p></div>
        <div class="button-row">${enabled ? `<button class="button primary" type="button">${cta}</button>` : ""}${button("학습 메인", "screens/learning/main.html")}</div>
      </section>
    </div>`;
  }

  function figmaAction(label, path, tone = "") {
    const className = tone === "primary" ? "button primary" : tone === "danger" ? "button warn" : "button";
    return `<a class="${className}" href="${href(path)}">${label}</a>`;
  }

  function figmaHeader(title = "헤더 부분 입니다.") {
    return `<div class="figma-header-bar"><span class="tag">Knowva</span><strong>${title}</strong></div>`;
  }

  function reviewCard(title, lead, rows, action) {
    return `<article class="figma-review-card">
      <h2>${title}</h2>
      <p>${lead}</p>
      <div class="figma-review-box">${rows.map((row) => `<span>· ${row}</span>`).join("")}</div>
      ${action}
    </article>`;
  }

  function reviewSummaryPage() {
    return shell("learning", `
      <section class="figma-review-screen">
        <header class="figma-review-head">
          <div><h1>오답 복습</h1><p>틀린 문제를 확인하고 복습을 시작하세요.</p></div>
          ${figmaAction("끝내기", "screens/learning/main.html")}
        </header>
        <div class="figma-review-grid">
          ${reviewCard("오답 목록 확인", "최근에 틀린 문제를 한눈에 확인합니다.", ["GoF 디자인 패턴 - Singleton", "REST API 상태 코드", "자료구조 - 해시 테이블"], figmaAction("오답 목록 보기", "screens/learning/review-list.html"))}
          ${reviewCard("다시 풀기", "틀린 문제를 다시 풀어 실력을 키웁니다.", ["복습 대기 6문제", "오늘 복습 완료 2문제", "평균 정답률 68%"], figmaAction("다시 풀기 시작", "screens/learning/practice.html"))}
          ${reviewCard("해설 확인", "틀린 문제의 해설을 바로 확인합니다.", ["Strategy 패턴 해설", "HTTP 404 vs 500", "해시 충돌 처리 방법"], figmaAction("해설 모아보기", "screens/learning/review-list.html"))}
          ${reviewCard("반복 오답 표시", "여러 번 틀린 문제를 우선 복습합니다.", ["반복 오답 3문제", "Observer 패턴 (3회)", "JPA 연관관계 (2회)"], figmaAction("반복 오답 풀기", "screens/learning/practice.html"))}
        </div>
      </section>`, { showTopbar: false });
  }

  function reviewListPage() {
    const cards = [
      ["GoF 디자인 패턴 - Singleton", "Singleton 패턴의 정의를 빈칸에 작성하세요.", "객관식"],
      ["REST API 상태 코드", "404와 500의 차이를 설명하세요.", "서술형"],
      ["자료구조 - 해시 테이블", "충돌 처리 방식을 고르세요.", "객관식"],
      ["SQL JOIN", "LEFT JOIN 결과를 예측하세요.", "코드 결과"],
      ["JPA 연관관계", "N+1 발생 조건을 찾으세요.", "개념"],
      ["CSS 선택자", "우선순위가 높은 선택자를 고르세요.", "객관식"],
      ["Python 반복문", "출력 결과를 예측하세요.", "코드 결과"],
      ["Java 예외 처리", "checked exception을 구분하세요.", "개념"],
      ["HTML form", "label과 input 연결을 확인하세요.", "접근성"]
    ];

    return shell("learning", `
      <section class="figma-review-screen">
        <header class="figma-review-head">
          <div><h1>오답 복습</h1><p>틀린 문제를 확인하고 복습을 시작하세요.</p></div>
          ${figmaAction("끝내기", "screens/learning/main.html")}
        </header>
        <article class="figma-review-list-panel">
          <h2>오답 목록 확인</h2>
          <p>최근에 틀린 문제를 한눈에 확인합니다.</p>
          <div class="figma-wrong-grid">
            ${cards.map(([meta, title, type], index) => `<a class="figma-wrong-card" href="${href("screens/learning/practice.html")}">
              <span>· ${meta}</span>
              <strong>${title}</strong>
              <em>${type}</em>
              <small>${index + 1}번 오답</small>
            </a>`).join("")}
          </div>
          ${figmaAction("뒤로 가기", "screens/learning/review.html")}
        </article>
      </section>`, { showTopbar: false });
  }

  const figmaCourses = [
    ["java", "JAVA"],
    ["sql", "SQL"],
    ["python", "Python"],
    ["web", "HTML CSS"]
  ];

  const boardKinds = [
    ["free", "자유 게시판"],
    ["question", "질문 게시판"],
    ["study-log", "공부 일지"]
  ];

  function figmaCourseRail(active = "java") {
    return `<aside class="figma-course-rail">
      <a class="hamburger" href="${href("screens/community/index.html")}" aria-label="커뮤니티 홈"><span></span><span></span><span></span></a>
      ${figmaCourses.map(([id, label]) => `<a class="figma-course-pill${activeClass(id === active)}" href="${href("screens/community/board.html")}">${label}</a>`).join("")}
    </aside>`;
  }

  function figmaBoardTabs(active = "free") {
    return `<nav class="figma-board-tabs" aria-label="게시판 종류">
      ${boardKinds.map(([id, label]) => `<a class="${activeClass(id === active)}" href="${href("screens/community/board.html#")}${id}">${label}</a>`).join("")}
    </nav>`;
  }

  function figmaPostList() {
    const posts = [
      ["조건문 edge case 정리", "김자바", "방금 전", "댓글 12", "48"],
      ["SQL JOIN 시각화 자료", "signal.sql", "10분 전", "댓글 8", "33"],
      ["오늘 학습 일지", "nova_learner", "22분 전", "댓글 5", "17"],
      ["반복문 문제 풀이 팁", "loop.dev", "1시간 전", "댓글 3", "14"]
    ];

    return `<div class="post-list">
      ${posts.map(([title, author, time, replies, likes]) => `<a class="post-row community-post-row" href="${href("screens/community/detail.html")}">
        <strong>${title}</strong>
        <span>${author} · ${time} · ${replies} · 좋아요 ${likes}</span>
      </a>`).join("")}
    </div>`;
  }

  function figmaHotPanel() {
    return `<aside class="panel figma-hot-panel">
      <span class="tag">HOT</span>
      <a href="${href("screens/community/detail.html")}">Java 컬렉션 정리</a>
      <a href="${href("screens/community/detail.html")}">면접 질문 복습</a>
      <a href="${href("screens/community/detail.html")}">공부 일지 공유</a>
    </aside>`;
  }

  function communityFigmaShell(content, activeCourse = "java", title = "자유게시판", lead = "학습 중 떠오른 이야기와 공부 기록을 자유롭게 나눕니다.") {
    return shell("community", `
      ${pageHero("SR-010 · 커뮤니티", title, lead)}
      <section class="community-layout figma-community-page">
        ${communityRail(activeCourse)}
        <main class="community-board figma-community-main">
          ${content}
        </main>
      </section>`);
  }

  function communityHomePage() {
    return communityFigmaShell(`
      <section class="figma-board-stage">
        <div class="search-strip">
          <input class="field" value="검색어를 입력하세요" aria-label="커뮤니티 검색">
          ${figmaAction("글쓰기", "screens/community/write.html", "primary")}
        </div>
        ${communityFilters("free")}
        <div class="board-grid figma-board-grid">
          <article class="panel figma-board-panel">
            <h2>자유 게시판</h2>
            <p>학습 중 떠오른 이야기와 공부 기록을 자유롭게 나눕니다.</p>
            ${figmaPostList()}
          </article>
          ${figmaHotPanel()}
        </div>
      </section>`);
  }

  function communityBoardPage() {
    return communityFigmaShell(`
      <section class="figma-board-stage">
        <div class="search-strip">
          <input class="field" value="검색어를 입력하세요" aria-label="게시판 검색">
          ${figmaAction("글쓰기", "screens/community/write.html", "primary")}
        </div>
        ${communityFilters("free")}
        <div class="board-grid figma-board-grid">
          <article class="panel figma-board-panel wide">
            <div class="figma-board-title-row">
              <div><span class="tag">Java</span><h2>자유 게시판</h2><p>목록에서 글을 선택하면 댓글과 신고가 가능한 상세 화면으로 이동합니다.</p></div>
              ${figmaAction("내 프로필", "screens/community/profile.html")}
            </div>
            ${figmaPostList()}
          </article>
          ${figmaHotPanel()}
        </div>
      </section>`);
  }

  function communityDetailPage() {
    return communityFigmaShell(`
      <section class="figma-detail-stage">
        <article class="panel figma-post-detail">
          <div class="figma-post-detail-head">
            <div><span class="tag">자유 게시판</span><h2>조건문 edge case 정리</h2><p>nova_learner · 2026.06.16 · 조회 128</p></div>
            <div class="figma-action-stack">${figmaAction("수정", "screens/community/edit.html")}${figmaAction("신고", "screens/admin/reports.html", "danger")}</div>
          </div>
          <div class="figma-post-body">
            오늘 조건문 문제를 풀면서 null, empty string, boundary value를 나눠서 생각해야 한다는 걸 정리했습니다. 같은 유형을 풀 때 테스트 케이스를 먼저 적으면 실수가 줄었습니다.
          </div>
          <div class="figma-post-actions">${figmaAction("좋아요", "screens/community/detail.html")}${figmaAction("스크랩", "screens/community/detail.html")}${figmaAction("댓글 달기", "screens/community/detail.html#comments", "primary")}</div>
        </article>
        <section id="comments" class="panel figma-comment-panel">
          <h2>댓글</h2>
          <div class="figma-comment"><strong>signal.sql</strong><p>boundary case를 따로 적어둔 부분이 좋네요.</p><a href="${href("screens/community/detail.html")}">대댓글</a></div>
          <div class="figma-comment nested"><strong>nova_learner</strong><p>다음 글에는 테스트 케이스 표도 같이 올릴게요.</p><a href="${href("screens/community/detail.html")}">신고</a></div>
          <form class="figma-comment-write">
            <input class="field" value="댓글을 입력하세요" aria-label="댓글 입력">
            <button class="button primary" type="button">작성</button>
          </form>
        </section>
      </section>`, "java", "게시글 상세", "본문, 댓글, 대댓글, 신고 action을 한 화면에서 확인합니다.");
  }

  function communityWritePage(mode = "write") {
    const isEdit = mode === "edit";
    return communityFigmaShell(`
      <section class="figma-write-stage">
        <article class="panel figma-write-panel">
          <div class="figma-board-title-row">
            <div><h2>${isEdit ? "글 수정" : "글 작성"}</h2><p>JAVA 자유 게시판에 게시할 내용을 작성합니다.</p></div>
            ${figmaAction("목록", "screens/community/board.html")}
          </div>
          <form class="figma-form-grid">
            <label>작성자<input class="field" value="nova_learner"></label>
            <label>게시판<select class="field"><option>자유 게시판</option><option>질문 게시판</option><option>공부 일지</option></select></label>
            <label class="span-2">제목<input class="field" value="${isEdit ? "조건문 edge case 정리" : "오늘의 Java 학습 일지"}"></label>
            <label class="span-2">내용<textarea class="field">${isEdit ? "조건문 문제를 풀면서 boundary case를 정리했습니다." : "학습한 내용과 질문을 정리합니다."}</textarea></label>
          </form>
          <div class="figma-post-actions">${figmaAction(isEdit ? "수정" : "등록", "screens/community/detail.html", "primary")}${figmaAction("취소", "screens/community/board.html")}</div>
        </article>
      </section>`, "java", isEdit ? "글 수정" : "글 작성", "선택한 언어/게시판 context를 유지한 채 게시글을 작성합니다.");
  }

  function communityProfilePage() {
    return communityFigmaShell(`
      <section class="figma-profile-stage">
        <article class="panel figma-profile-card">
          <span class="avatar large"></span>
          <h2>nova_learner.log</h2>
          <p>Java backend 학습 기록과 질문 답변을 모아둔 커뮤니티 프로필</p>
          <div class="figma-profile-metrics">${metric("12", "작성 글")}${metric("7", "스크랩")}${metric("31", "받은 좋아요")}</div>
        </article>
        <article class="panel figma-board-panel">
          <h2>내가 작성한 글</h2>
          ${figmaPostList()}
        </article>
      </section>`, "java", "나의 커뮤니티 프로필", "커뮤니티 활동만 모아서 보여주는 개인 학습 로그입니다.");
  }

  function paymentStep(step) {
    return `<div class="figma-payment-step"><strong>${step}</strong><span></span></div>`;
  }

  function orderPanel() {
    return `<aside class="figma-order-panel">
      <h2>상품 정보</h2>
      <dl>
        <div><dt>주문 번호</dt><dd>KNV-20260616-001</dd></div>
        <div><dt>주문 일시</dt><dd>2026.06.16</dd></div>
        <div><dt>상품명</dt><dd>상세 분석 이용권</dd></div>
        <div><dt>결제 금액</dt><dd>9,900원</dd></div>
      </dl>
    </aside>`;
  }

  function paymentShell(content, step = "1 / 2") {
    return shell("analysis", `<section class="figma-payment-page">${paymentStep(step)}${content}</section>`, { showTopbar: false });
  }

  function paymentChoicePage() {
    return paymentShell(`
      <header class="figma-payment-head"><h1>결제 진행</h1><p>Premium 상세 분석 이용권 결제 수단을 선택합니다.</p></header>
      <section class="figma-payment-choice">
        <article class="figma-method-card">
          <span>VISA</span>
          <h2>신용카드 결제</h2>
          <p>카드 정보를 입력하고 결제를 완료합니다.</p>
          ${figmaAction("신용카드", "screens/payment/card.html", "primary")}
        </article>
        <article class="figma-method-card">
          <span>BANK</span>
          <h2>무통장 입금</h2>
          <p>입금자명과 계좌 정보를 확인합니다.</p>
          ${figmaAction("무통장 입금", "screens/payment/bank.html")}
        </article>
        ${orderPanel()}
      </section>`);
  }

  function paymentCardPage() {
    return paymentShell(`
      <header class="figma-payment-head"><h1>결제 진행</h1><p>신용카드 정보를 입력합니다.</p></header>
      <section class="figma-payment-layout">
        <form class="figma-payment-form">
          <div class="figma-method-row">${figmaAction("VISA", "screens/payment/card.html", "primary")}${figmaAction("무통장 입금", "screens/payment/bank.html")}</div>
          <div class="figma-form-grid">
            <label>성<input class="field" value="Lee"></label>
            <label>이름<input class="field" value="Jeongha"></label>
            <label class="span-2">카드 번호<input class="field" value="4242 4242 4242 4242"></label>
            <label>생년 월일<input class="field" value="2000.01.01"></label>
            <label>CVC<input class="field" value="123"></label>
          </div>
          ${figmaAction("결제하기", "screens/payment/complete.html", "primary")}
        </form>
        ${orderPanel()}
      </section>`);
  }

  function paymentBankPage() {
    return paymentShell(`
      <header class="figma-payment-head"><h1>결제 진행</h1><p>무통장 입금 정보를 확인합니다.</p></header>
      <section class="figma-payment-layout">
        <form class="figma-payment-form">
          <div class="figma-method-row">${figmaAction("신용카드", "screens/payment/card.html")}${figmaAction("무통장 입금", "screens/payment/bank.html", "primary")}</div>
          <div class="figma-form-grid">
            <label>입금자명<input class="field" value="이정하"></label>
            <label>입금 금액<input class="field" value="9,900원"></label>
            <label class="span-2">입금 계좌<input class="field" value="국민 123456-01-987654 KNOWVA"></label>
          </div>
          ${figmaAction("결제하기", "screens/payment/complete.html", "primary")}
        </form>
        ${orderPanel()}
      </section>`);
  }

  function paymentCompletePage() {
    return paymentShell(`
      <section class="figma-payment-complete">
        <h1>결제가 완료 되었습니다!</h1>
        <p>상세 분석 이용권이 활성화되었습니다.</p>
        <div class="figma-complete-mark">OK</div>
        ${figmaAction("홈으로", "screens/analysis/index.html", "primary")}
      </section>`, "2 / 2");
  }

  function recommendPage() {
    const resourceColumn = (title, rows) => `<article class="figma-resource-column"><h2>${title}</h2>${rows.map((row) => `<a href="${href("screens/payment/recommendations.html")}"><span></span><strong>${row}</strong><small>추천 콘텐츠</small></a>`).join("")}</article>`;
    return shell("analysis", `
      <section class="figma-resource-page">
        ${figmaCourseRail("java")}
        <main>
          ${figmaHeader("과목 콘텐츠 추천")}
          <div class="figma-resource-grid">
            ${resourceColumn("JAVA 영상 추천", ["객체지향 기초", "Spring MVC 흐름", "예외 처리 전략"])}
            ${resourceColumn("기사 추천", ["개발자 학습 루틴", "백엔드 테스트 전략", "API 설계 사례"])}
            ${resourceColumn("개발 콘텐츠 추천", ["공식 문서 읽기", "오픈소스 코드 보기", "미니 프로젝트"])}
          </div>
        </main>
      </section>`, { showTopbar: false });
  }

  const adminNavItems = [
    ["sr-013", "관리자 홈", "screens/admin/dashboard.html"],
    ["sr-013-stats", "통계", "screens/admin/stats.html"],
    ["sr-013-courses", "과목·커리큘럼 관리", "screens/admin/courses.html"],
    ["sr-013-theory", "이론 자료 관리", "screens/admin/theory.html"],
    ["sr-013-problems", "일반 문제 관리", "screens/admin/problems.html"],
    ["sr-013-users", "사용자 관리", "screens/admin/users.html"],
    ["sr-013-community", "커뮤니티 관리", "screens/admin/community.html"],
    ["sr-013-reports", "신고 관리", "screens/admin/reports.html"],
    ["sr-013-notices", "공지사항 관리", "screens/admin/notices.html"]
  ];

  function adminShell(active, title, content) {
    return shell("admin", `
      <section class="figma-admin-page">
        <aside class="figma-admin-sidebar">
          <strong>KNOWVA 관리자</strong>
          <nav>${adminNavItems.map(([id, label, path]) => `<a class="${activeClass(id === active)}" href="${href(path)}">${label}</a>`).join("")}</nav>
          <a class="figma-admin-logout" href="${href("screens/auth/login.html")}">로그아웃</a>
        </aside>
        <main class="figma-admin-main">
          <header class="figma-admin-top"><h1>${title}</h1><span>관리자 계정 | 오늘 날짜 | 알림</span></header>
          ${content}
        </main>
      </section>`, { showTopbar: false });
  }

  function adminMetricCards(items) {
    return `<section class="figma-admin-metrics">${items.map(([label, value, sub]) => `<article><span>${label}</span><strong>${value}</strong><small>${sub}</small></article>`).join("")}</section>`;
  }

  function adminBars(labels) {
    return `<div class="figma-admin-chart">${labels.map((label, index) => `<i style="height:${38 + (index * 13) % 48}%"><span>${label}</span></i>`).join("")}</div>`;
  }

  function adminTable(headers, rows) {
    return `<div class="figma-admin-table"><table><thead><tr>${headers.map((head) => `<th>${head}</th>`).join("")}</tr></thead><tbody>${rows.map((row) => `<tr>${row.map((cell) => `<td>${cell}</td>`).join("")}</tr>`).join("")}</tbody></table></div>`;
  }

  function adminHomePage() {
    return adminShell("sr-013", "관리자 홈", `
      ${adminMetricCards([["전체 사용자 수", "2,847", "12% 지난달 대비"], ["오늘 학습량", "153", "8% 어제 대비"], ["오늘 문제풀이 수", "1,024", "15% 어제 대비"], ["신고 대기 수", "5", "처리 대기 중"]])}
      <section class="figma-admin-two">
        <article><h2>일별 사용자 접속량</h2>${adminBars(["6/9", "6/10", "6/11", "6/12", "6/13", "6/14", "6/15"])}</article>
        <article><h2>과목별 학습 완료 수</h2>${adminBars(["Java", "Python", "SQL", "HTML/CSS", "JS", "자료구조"])}</article>
      </section>
      <section class="figma-admin-quick">
        ${figmaAction("문제 등록", "screens/admin/problems.html", "primary")}
        ${figmaAction("이론 자료 등록", "screens/admin/theory.html")}
        ${figmaAction("신고 확인", "screens/admin/reports.html")}
        ${figmaAction("공지 작성", "screens/admin/notices.html")}
      </section>
      <section class="figma-admin-two">
        ${adminTable(["ID", "내용 요약", "처리 상태", "일시"], [["#R001", "Java 게시글 부적절한 내용", "처리 완료", "06/15"], ["#R002", "욕설 댓글 신고", "검토 중", "06/14"], ["#R003", "광고성 게시글 신고", "대기", "06/13"]])}
        ${adminTable(["ID", "제목", "상태", "작성일"], [["#N001", "KNOWVA 이용약관 변경 안내", "공개", "06/15"], ["#N002", "6월 시스템 점검 안내", "공개", "06/10"], ["#N003", "신규 과목 업데이트 안내", "공개", "06/05"]])}
      </section>`);
  }

  function adminStatsPage() {
    return adminShell("sr-013-stats", "통계", `
      <section class="figma-admin-filters"><input class="field" value="일별 / 월별"><input class="field" value="과목별 선택"><input class="field" value="기간 선택"></section>
      ${adminMetricCards([["가입자 수", "0", ""], ["활성 사용자 수", "0", ""], ["학습 완료 수", "0", ""], ["문제풀이 수", "0", ""], ["시험 응시 수", "0", ""]])}
      <section class="figma-admin-three">
        <article>일별 사용량 그래프</article><article>과목별 학습량 그래프</article><article>문제풀이 / 시험 점수 추이</article>
      </section>
      <h2 class="figma-admin-section-title">통계 테이블</h2>
      ${adminTable(["날짜", "과목", "학습 수", "문제풀이 수", "시험 응시 수", "평균 점수"], [["2026-06-12", "Java", "150", "320", "45", "78.5"], ["2026-06-11", "Python", "132", "290", "38", "81.2"], ["2026-06-10", "SQL", "98", "210", "52", "74.0"], ["2026-06-09", "HTML/CSS", "175", "380", "61", "82.7"]])}
      <div class="figma-pagination"><span>&lt;</span><b>1</b><span>2</span><span>3</span><span>...</span><span>10</span><span>&gt;</span></div>`);
  }

  function adminManagePage(active, title, config) {
    return adminShell(active, title, `
      <section class="figma-admin-filters">${config.filters.map((item) => `<input class="field" value="${item}">`).join("")}${figmaAction(config.cta, config.path, "primary")}</section>
      ${adminTable(config.headers, config.rows)}
      <section class="figma-admin-form">
        <h2>${config.formTitle}</h2>
        <div class="figma-form-grid">
          ${config.fields.map((field, index) => `<label class="${index === 1 ? "span-2" : ""}">${field}<input class="field" value=""></label>`).join("")}
          <label class="span-2">상세 내용<textarea class="field"></textarea></label>
        </div>
        <div class="figma-post-actions">${figmaAction("저장", config.path, "primary")}${figmaAction("취소", config.path)}${figmaAction("삭제", config.path, "danger")}</div>
      </section>`);
  }

  function adminLoginPage() {
    return shell("admin", `
      <section class="figma-admin-login">
        <form>
          <h1>KNOWVA 관리자 로그인</h1>
          <label>관리자 이메일<input class="field" value="admin@knowva.local"></label>
          <label>비밀번호<input class="field" type="password" value="password"></label>
          ${figmaAction("로그인", "screens/admin/dashboard.html", "primary")}
        </form>
      </section>`, { showTopbar: false });
  }

  const adminConfigs = {
    "sr-013-courses": ["과목·커리큘럼 관리", { path: "screens/admin/courses.html", cta: "+ 과목 등록", filters: ["과목명", "활성화 상태"], headers: ["ID", "과목", "커리큘럼 수", "활성화", "관리"], rows: [["#C001", "Java", "12", "활성", "수정"], ["#C002", "SQL", "8", "활성", "수정"], ["#C003", "Python", "9", "비활성", "수정"]], formTitle: "과목 / 커리큘럼 등록 폼", fields: ["과목명", "커리큘럼명", "정렬 순서", "활성화 상태"] }],
    "sr-013-theory": ["이론 자료 관리", { path: "screens/admin/theory.html", cta: "+ 자료 등록", filters: ["과목", "커리큘럼", "활성화 상태"], headers: ["자료 ID", "과목", "제목", "상태", "관리"], rows: [["#T001", "Java", "조건문 개념", "활성", "수정"], ["#T002", "SQL", "JOIN 기초", "활성", "수정"], ["#T003", "HTML/CSS", "Flexbox", "비활성", "수정"]], formTitle: "이론 자료 등록 / 수정 폼", fields: ["과목", "제목", "노출 순서", "활성화 상태"] }],
    "sr-013-problems": ["일반 학습 문제 관리", { path: "screens/admin/problems.html", cta: "+ 문제 등록", filters: ["과목", "커리큘럼", "문제 유형", "난이도", "활성화 상태"], headers: ["문제 ID", "과목", "커리큘럼", "유형", "문제 내용 요약", "난이도", "활성화", "관리"], rows: [["#001", "Java", "변수와 자료형", "객관식", "정수형 변수 선언 키워드", "하", "활성", "수정"], ["#002", "Java", "조건문", "빈칸", "if 문 조건식", "중", "활성", "수정"], ["#003", "Python", "반복문", "코드 결과", "출력 결과 예측", "중", "활성", "수정"]], formTitle: "문제 등록 / 수정 폼", fields: ["문제 유형 선택", "문제 내용 입력", "선택지 입력 영역", "정답 입력", "난이도 선택"] }],
    "sr-013-users": ["사용자 관리", { path: "screens/admin/users.html", cta: "사용자 검색", filters: ["이메일", "권한", "상태"], headers: ["사용자 ID", "닉네임", "이메일", "권한", "상태", "가입일", "관리"], rows: [["#U001", "nova_learner", "jeongha@example.com", "USER", "활성", "2026.06.01", "상세"], ["#U002", "orbit.dev", "orbit@example.com", "PREMIUM", "활성", "2026.05.18", "상세"]], formTitle: "사용자 상태 변경", fields: ["사용자", "권한", "상태", "메모"] }],
    "sr-013-community": ["커뮤니티 관리", { path: "screens/admin/community.html", cta: "게시글 검색", filters: ["게시판", "작성자", "상태"], headers: ["게시글 ID", "게시판", "제목", "작성자", "댓글", "상태", "관리"], rows: [["#P001", "자유", "조건문 edge case", "nova_learner", "12", "공개", "상세"], ["#P002", "공부 일지", "SQL JOIN 정리", "signal.sql", "8", "공개", "상세"]], formTitle: "게시글 운영 처리", fields: ["게시글", "상태", "처리 사유", "관리자 메모"] }],
    "sr-013-reports": ["신고 관리", { path: "screens/admin/reports.html", cta: "신고 검색", filters: ["신고 유형", "처리 상태", "기간"], headers: ["신고 ID", "대상", "유형", "내용 요약", "처리 상태", "일시", "관리"], rows: [["#R001", "게시글", "부적절한 내용", "욕설 포함", "검토 중", "06/15", "처리"], ["#R002", "댓글", "광고", "외부 링크 반복", "대기", "06/14", "처리"]], formTitle: "신고 처리 폼", fields: ["신고 대상", "처리 상태", "조치 유형", "처리 사유"] }],
    "sr-013-notices": ["공지사항 관리", { path: "screens/admin/notices.html", cta: "+ 공지 작성", filters: ["공개 상태", "기간"], headers: ["공지 ID", "제목", "상태", "작성일", "관리"], rows: [["#N001", "KNOWVA 이용약관 변경 안내", "공개", "06/15", "수정"], ["#N002", "6월 시스템 점검 안내", "공개", "06/10", "수정"], ["#N003", "신규 과목 업데이트 안내", "공개", "06/05", "수정"]], formTitle: "공지사항 등록 / 수정 폼", fields: ["제목", "공개 상태", "게시 시작일", "게시 종료일"] }]
  };

  const settingsNavItems = [
    ["sr-015-profile", "회원 정보", "screens/settings/profile.html"],
    ["sr-015-security", "이메일 / 비밀번호", "screens/settings/security.html"],
    ["sr-015-social", "연동된 소셜 계정", "screens/settings/social.html"],
    ["sr-015-system", "시스템 설정", "screens/settings/system.html"],
    ["sr-015-payment", "결제 정보", "screens/settings/payment.html"]
  ];

  function settingsSidebar(active) {
    return `<aside class="figma-settings-sidebar">
      <h1>설정</h1>
      <nav>${settingsNavItems.map(([id, label, path]) => `<a class="${activeClass(id === active)}" href="${href(path)}">${label}</a>`).join("")}</nav>
    </aside>`;
  }

  function settingsPage(active, title, lead, content) {
    return shell("settings", `
      <section class="figma-settings-page">
        <a class="figma-logout" href="${href("screens/auth/login.html")}">로그아웃</a>
        ${settingsSidebar(active)}
        <main class="figma-settings-main">
          <article class="figma-settings-panel">
            <h1>${title}</h1>
            <p>${lead}</p>
            ${content}
          </article>
        </main>
      </section>`, { showTopbar: false });
  }

  function settingsField(label, value) {
    return `<div class="figma-setting-row"><strong>${label}</strong><span>${value}</span></div>`;
  }

  function settingsModalPage() {
    return settingsProfilePage();
  }

  function settingsProfilePage() {
    return settingsPage("sr-015-profile", "회원 정보", "프로필과 기본 계정 정보를 확인하고 수정합니다.", `<section class="figma-settings-box">${settingsField("닉네임", "이정하")}${settingsField("이메일", "jeongha@example.com")}${settingsField("가입일", "2026.06.01")}${settingsField("학습 언어", "Java, HTML, Spring")}${figmaAction("저장", "screens/settings/profile.html", "primary")}</section>`);
  }

  function settingsSecurityPage() {
    return settingsPage("sr-015-security", "이메일 / 비밀번호", "로그인에 사용하는 이메일과 비밀번호를 관리합니다.", `<section class="figma-settings-box">${settingsField("이메일", "jeongha@example.com")}${settingsField("비밀번호", "••••••••••")}${figmaAction("비밀번호 변경", "screens/settings/security.html", "primary")}</section>`);
  }

  function settingsSocialPage() {
    return settingsPage("sr-015-social", "연동된 소셜 계정", "외부 계정 연결 상태를 확인합니다.", `<section class="figma-social-grid">${card("Google", "jeongha@gmail.com 계정이 연결되어 있습니다.", "연동됨")}${card("Kakao", "kakao_jeongha 계정이 연결되어 있습니다.", "연동됨")}${card("Naver", "연동 가능한 상태입니다.", "미연동", figmaAction("연동", "screens/settings/social.html"))}</section>`);
  }

  function settingsSystemPage() {
    return settingsPage("sr-015-system", "시스템 설정", "알림, 화면 모드, 접근성 옵션을 관리합니다.", `<section class="figma-settings-box">${settingsField("학습 알림", "켜짐")}${settingsField("테마", "시스템 설정 따름")}${settingsField("모션 줄이기", "꺼짐")}${figmaAction("저장", "screens/settings/system.html", "primary")}</section>`);
  }

  function settingsPaymentPage() {
    return settingsPage("sr-015-payment", "결제 정보", "결제 내역과 이용 중인 서비스를 확인합니다.", `<section class="figma-settings-box"><div class="figma-payment-plan"><strong>상세 분석 이용권</strong><span>결제일 2026.06.05 · 9,900원 · 더미 결제</span>${figmaAction("결제 내역", "screens/payment/complete.html")}</div><div class="figma-payment-history"><span>2026.06.05</span><strong>상세 분석 이용권</strong><b>9,900원</b></div><div class="figma-payment-history"><span>2026.05.12</span><strong>상세 분석 이용권</strong><b>9,900원</b></div></section>`);
  }

  const sampleRoutes = [
    ["sample-learning-main", "학습", "screens/samples/learning-main.html"],
    ["sample-community-board", "커뮤니티", "screens/samples/community-board.html"],
    ["sample-settings-index", "설정", "screens/samples/settings-index.html"],
    ["sample-admin-stats", "관리자", "screens/samples/admin-stats.html"]
  ];

  const sampleDarkRoutes = [
    ["sample-dark-orbital-ink", "A balanced cyan", "screens/samples/dark/orbital-ink.html"],
    ["sample-dark-deep-observatory", "B deep cyan", "screens/samples/dark/deep-observatory.html"],
    ["sample-dark-moon-dust", "C quiet cyan", "screens/samples/dark/moon-dust.html"]
  ];

  function sampleSwitcher(active) {
    return `<nav class="sample-switcher" aria-label="예시 화면 이동">
      <span>Design refresh samples</span>
      ${sampleRoutes.map(([id, label, path]) => `<a class="${activeClass(id === active)}" href="${href(path)}">${label}</a>`).join("")}
    </nav>`;
  }

  function sampleDarkSwitcher(active) {
    return `<nav class="sample-switcher sample-dark-switcher" aria-label="다크 샘플 이동">
      <span>Ion Cyan dark samples</span>
      ${sampleDarkRoutes.map(([id, label, path]) => `<a class="${activeClass(id === active)}" href="${href(path)}">${label}</a>`).join("")}
    </nav>`;
  }

  function sampleTag(label, tone = "") {
    return `<span class="sample-tag ${tone}">${label}</span>`;
  }

  function sampleKpi(label, value, change = "") {
    return `<article class="sample-kpi"><span>${label}</span><strong>${value}</strong>${change ? `<small>${change}</small>` : ""}</article>`;
  }

  function sampleBars(values, labels) {
    return `<div class="sample-bars">${values.map((value, index) => `<i style="height:${value}%"><span>${labels[index]}</span></i>`).join("")}</div>`;
  }

  function sampleMiniChart(values) {
    return `<div class="sample-mini-chart" aria-hidden="true">${values.map((value) => `<i style="height:${value}%"></i>`).join("")}</div>`;
  }

  function sampleDarkSwatches(swatches) {
    return `<div class="sample-dark-swatches" aria-label="palette">
      ${swatches.map(([label, tone]) => `<span style="--swatch:${tone}"><i></i>${label}</span>`).join("")}
    </div>`;
  }

  function sampleLearningMainPage() {
    const lessons = [
      ["완료", "Java 입문", "개념 4개 · 문제 10/10", "done"],
      ["완료", "변수와 자료형", "정답률 92% · 오답 1개", "done"],
      ["진행 중", "조건문", "오늘 3문제 진행 · 7문제 남음", "active"],
      ["대기", "반복문", "조건문 80% 완료 시 열림", "locked"],
      ["잠김", "배열", "반복문 완료 후 추천", "muted"]
    ];

    return shell("learning", `
      <section class="sample-page sample-learning">
        ${sampleSwitcher("sample-learning-main")}
        <div class="sample-learning-layout">
          <aside class="sample-side-panel">
            <div class="sample-profile-line"><span class="sample-avatar">NL</span><div><strong>nova_learner</strong><small>Java backend track</small></div></div>
            <div class="sample-side-section">
              <span class="sample-eyebrow">Current course</span>
              <h2>Java 기초</h2>
              ${progressLine("조건문 단원", 68, "68%")}
              ${progressLine("시험 gate", 72, "7 / 10")}
            </div>
            <div class="sample-course-list">
              <a class="is-active" href="${href("screens/samples/learning-main.html")}"><strong>Java</strong><span>오늘 계속 학습</span></a>
              <a href="${href("screens/learning/main.html")}"><strong>SQL</strong><span>다음 추천</span></a>
              <a href="${href("screens/learning/main.html")}"><strong>Python</strong><span>기초 준비</span></a>
            </div>
          </aside>

          <main class="sample-workspace">
            <header class="sample-section-head">
              <div>
                ${sampleTag("학습 메인 예시", "active")}
                <h1>오늘은 조건문을 끝내고 반복문 gate를 연다</h1>
                <p>색은 CTA와 진행 상태에만 남기고, 나머지는 실제 학습 상태가 먼저 보이도록 밀도를 높인 예시다.</p>
              </div>
              <a class="button primary" href="${href("screens/learning/curriculum.html")}">학습 시작</a>
            </header>

            <section class="sample-mission-card">
              <div>
                <span class="sample-eyebrow">Today mission</span>
                <h2>조건문 문제 7개 마저 풀기</h2>
                <p>완료하면 반복문 이론과 문제풀이가 열린다.</p>
              </div>
              <div class="sample-mission-meter"><strong>7</strong><span>문제 남음</span></div>
            </section>

            <section class="sample-lesson-list" aria-label="학습 로드맵">
              ${lessons.map(([state, title, meta, tone]) => `<article class="sample-lesson-row ${tone}">
                <span class="sample-node"></span>
                <div><strong>${title}</strong><small>${meta}</small></div>
                ${sampleTag(state, tone)}
              </article>`).join("")}
            </section>
          </main>

          <aside class="sample-insight-panel">
            <article>
              <span class="sample-eyebrow">Learning signal</span>
              <h2>정답률은 안정적, 속도는 느림</h2>
              <p>조건 분기 문제에서 평균 풀이 시간이 18% 길다.</p>
              ${sampleMiniChart([42, 48, 55, 61, 58, 64, 72])}
            </article>
            <article>
              <h3>다음 행동</h3>
              <ul>
                <li>조건문 edge case 3문제</li>
                <li>비교 연산자 오답 1개 복습</li>
                <li>반복문 gate까지 7문제</li>
              </ul>
            </article>
          </aside>
        </div>
      </section>`);
  }

  function sampleCommunityBoardPage() {
    const threads = [
      ["질문", "조건문 edge case 정리", "답변 대기 · 댓글 12 · 좋아요 48", "nova_learner", "12분 전"],
      ["공부 일지", "SQL JOIN 시각화 자료", "스크랩 21 · 댓글 8 · 좋아요 33", "signal.sql", "28분 전"],
      ["자유", "오늘 학습 일지", "Java 반복문 복습 · 댓글 5", "loop.dev", "1시간 전"],
      ["공부 일지", "반복문 문제 풀이 팁", "해결됨 · 스크랩 14", "orbit.dev", "2시간 전"],
      ["질문", "Scanner 입력 예외 처리", "답변 2 · 채택 완료", "java.seed", "어제"]
    ];

    return shell("community", `
      <section class="sample-page sample-community">
        ${sampleSwitcher("sample-community-board")}
        <div class="sample-community-layout">
          <aside class="sample-side-panel">
            <h1>커뮤니티</h1>
            <nav class="sample-nav-list" aria-label="게시판">
              <a class="is-active" href="${href("screens/samples/community-board.html")}"><strong>Java</strong><span>자유 · 질문 · 공부 일지</span></a>
              <a href="${href("screens/community/board.html")}"><strong>SQL</strong><span>JOIN · 튜닝 · 일지</span></a>
              <a href="${href("screens/community/board.html")}"><strong>HTML/CSS/JS</strong><span>레이아웃 · 접근성</span></a>
              <a href="${href("screens/community/board.html")}"><strong>Python</strong><span>문법 · 자동화</span></a>
            </nav>
            <article class="sample-compact-card">
              <strong>내 활동</strong>
              <span>작성 12 · 스크랩 7 · 좋아요 31</span>
              <a class="button" href="${href("screens/community/profile.html")}">프로필</a>
            </article>
          </aside>

          <main class="sample-feed-panel">
            <header class="sample-feed-head">
              <div>
                ${sampleTag("Java", "active")}
                <h1>Java 게시판</h1>
                <p>검색, 필터, 상태, 참여 수를 첫 화면에서 바로 확인한다.</p>
              </div>
              <a class="button primary" href="${href("screens/community/write.html")}">글쓰기</a>
            </header>
            <div class="sample-toolbar">
              <input class="field" value="조건문, 반복문, Scanner" aria-label="게시글 검색">
              <button class="button" type="button">검색</button>
              <button class="button" type="button">최신순</button>
            </div>
            <nav class="sample-filter-row" aria-label="게시글 필터">
              ${sampleTag("전체", "active")}
              ${sampleTag("자유")}
              ${sampleTag("질문")}
              ${sampleTag("공부 일지")}
              ${sampleTag("답변 대기")}
            </nav>
            <section class="sample-thread-list">
              ${threads.map(([kind, title, meta, author, time]) => `<a class="sample-thread-row" href="${href("screens/community/detail.html")}">
                ${sampleTag(kind, kind === "질문" ? "amber" : kind === "공부 일지" ? "green" : "")}
                <div><strong>${title}</strong><span>${meta}</span></div>
                <small>${author}<br>${time}</small>
              </a>`).join("")}
            </section>
          </main>

          <aside class="sample-hot-panel">
            <h2>Hot</h2>
            ${["Java 컬렉션 정리", "면접 질문 복습", "공부 일지 공유"].map((item) => `<a href="${href("screens/community/detail.html")}">${item}</a>`).join("")}
            <div class="sample-divider"></div>
            <h2>이번 주 답변자</h2>
            ${list([["orbit.dev", "채택 18"], ["signal.sql", "채택 12"], ["loop.dev", "채택 9"]])}
          </aside>
        </div>
      </section>`);
  }

  function sampleSettingsIndexPage() {
    const rows = [
      ["닉네임", "이정하", "공개 프로필에 표시", "변경"],
      ["이메일", "jeongha@example.com", "로그인 및 알림 수신", "확인됨"],
      ["학습 언어", "Java, SQL", "추천 roadmap 기준", "관리"],
      ["비밀번호", "마지막 변경 2026.06.01", "보안 설정", "변경"],
      ["소셜 계정", "Google, Kakao 연결됨", "간편 로그인", "관리"],
      ["Premium", "상세 분석 이용권 활성", "2026.07.05 갱신 예정", "결제"]
    ];

    return shell("settings", `
      <section class="sample-page sample-settings">
        ${sampleSwitcher("sample-settings-index")}
        <div class="sample-settings-layout">
          <aside class="sample-side-panel">
            <h1>설정</h1>
            <nav class="sample-nav-list" aria-label="설정 메뉴">
              <a class="is-active" href="${href("screens/samples/settings-index.html")}">계정 정보</a>
              <a href="${href("screens/settings/security.html")}">이메일 / 비밀번호</a>
              <a href="${href("screens/settings/social.html")}">소셜 계정</a>
              <a href="${href("screens/settings/system.html")}">시스템 설정</a>
              <a href="${href("screens/settings/payment.html")}">결제 정보</a>
            </nav>
            <a class="sample-quiet-link" href="${href("screens/auth/login.html")}">로그아웃</a>
          </aside>

          <main class="sample-settings-main">
            <header class="sample-section-head compact">
              <div>
                ${sampleTag("설정 예시")}
                <h1>계정 정보</h1>
                <p>큰 form card 대신 실제 설정처럼 row, 상태, inline action을 중심으로 재구성했다.</p>
              </div>
              <span class="sample-save-state">마지막 저장 09:30</span>
            </header>
            <section class="sample-settings-list">
              ${rows.map(([label, value, meta, action]) => `<article class="sample-setting-row">
                <div><strong>${label}</strong><span>${meta}</span></div>
                <p>${value}</p>
                <button class="button" type="button">${action}</button>
              </article>`).join("")}
            </section>
            <section class="sample-danger-zone">
              <div><strong>회원 탈퇴</strong><span>탈퇴 전 학습 기록 보관/삭제 정책을 안내한다.</span></div>
              <button class="button warn" type="button">탈퇴 요청</button>
            </section>
          </main>
        </div>
      </section>`, { showTopbar: false });
  }

  function sampleAdminStatsPage() {
    const tableRows = [
      ["2026-06-16", "Java", "184", "412", "58", "81.4"],
      ["2026-06-15", "Java", "171", "386", "52", "79.8"],
      ["2026-06-14", "SQL", "132", "301", "41", "83.2"],
      ["2026-06-13", "Python", "119", "244", "35", "77.6"],
      ["2026-06-12", "HTML/CSS", "156", "328", "46", "80.1"]
    ];

    return shell("admin", `
      <section class="sample-page sample-admin">
        ${sampleSwitcher("sample-admin-stats")}
        <div class="sample-admin-layout">
          <aside class="sample-side-panel sample-admin-nav">
            <strong>KNOWVA 관리자</strong>
            <nav class="sample-nav-list" aria-label="관리자 메뉴">
              <a href="${href("screens/admin/dashboard.html")}">관리자 홈</a>
              <a class="is-active" href="${href("screens/samples/admin-stats.html")}">통계</a>
              <a href="${href("screens/admin/courses.html")}">과목 관리</a>
              <a href="${href("screens/admin/problems.html")}">문제 관리</a>
              <a href="${href("screens/admin/reports.html")}">신고 관리</a>
            </nav>
          </aside>
          <main class="sample-admin-main">
            <header class="sample-section-head compact">
              <div>
                ${sampleTag("관리자 통계 예시", "active")}
                <h1>학습 운영 통계</h1>
                <p>placeholder 0과 빈 그래프 대신 기간, 지표, 추세, 갱신 시각을 보이게 만든 예시다.</p>
              </div>
              <span class="sample-save-state">09:30 updated</span>
            </header>
            <section class="sample-admin-filters">
              <button class="button primary" type="button">최근 7일</button>
              <button class="button" type="button">과목: 전체</button>
              <button class="button" type="button">CSV Export</button>
            </section>
            <section class="sample-admin-kpis">
              ${sampleKpi("가입자 수", "2,847", "+12% MoM")}
              ${sampleKpi("활성 사용자", "842", "+6% WoW")}
              ${sampleKpi("학습 완료", "1,284", "오늘 184")}
              ${sampleKpi("문제풀이", "2,931", "정답률 79.8%")}
            </section>
            <section class="sample-admin-charts">
              <article><h2>일별 사용량</h2>${sampleBars([42, 55, 48, 72, 64, 81, 76], ["6/10", "6/11", "6/12", "6/13", "6/14", "6/15", "6/16"])}</article>
              <article><h2>과목별 학습량</h2>${sampleBars([82, 44, 61, 38], ["Java", "SQL", "Python", "Web"])}</article>
              <article><h2>시험 점수 추이</h2>${sampleMiniChart([48, 56, 62, 59, 68, 74, 82])}<p>평균 점수 81.4 · 전주 대비 +3.2</p></article>
            </section>
            ${adminTable(["날짜", "과목", "학습 수", "문제풀이 수", "시험 응시 수", "평균 점수"], tableRows)}
          </main>
        </div>
      </section>`, { showTopbar: false });
  }

  const sampleDarkOptions = {
    "sample-dark-orbital-ink": {
      id: "sample-dark-orbital-ink",
      className: "orbital-ink",
      label: "A안 · 추천",
      title: "Balanced Ion Dark",
      subtitle: "Ion Cyan을 대표색으로 쓰고 green과 amber는 상태 보조에만 둔 기본 다크 톤.",
      principle: "CTA와 활성 상태는 cyan, 완료/진행은 green, 퀘스트/주의는 amber로 제한해서 우주 배경이 네온처럼 보이지 않게 한다.",
      accent: "Ion Cyan + role accents",
      swatches: [["Space", "#0b0d12"], ["Panel", "#171b22"], ["Ion Cyan", "#22b8c8"], ["Progress", "#58b947"], ["Quest", "#f2b84b"]]
    },
    "sample-dark-deep-observatory": {
      id: "sample-dark-deep-observatory",
      className: "deep-observatory",
      label: "B안",
      title: "Deep Ion Space",
      subtitle: "더 깊은 남색 블랙 배경 위에 Ion Cyan을 낮은 채도로 올린 집중형 톤.",
      principle: "배경 대비는 높이되 accent는 세 역할로만 제한한다. 과목/메뉴 구분은 색보다 구조와 밀도로 먼저 읽히게 둔다.",
      accent: "Ion Cyan + role accents",
      swatches: [["Night", "#0a0f19"], ["Panel", "#161b26"], ["Ion Cyan", "#22b8c8"], ["Progress", "#58b947"], ["Quest", "#f2b84b"]]
    },
    "sample-dark-moon-dust": {
      id: "sample-dark-moon-dust",
      className: "moon-dust",
      label: "C안",
      title: "Quiet Ion Space",
      subtitle: "채도를 가장 낮춘 charcoal 배경에 Ion Cyan CTA만 또렷하게 둔 차분한 톤.",
      principle: "장시간 학습 화면에서 시각 피로를 줄이고, 상태 색은 완료와 퀘스트처럼 의미가 있는 곳에만 둔다.",
      accent: "Ion Cyan + role accents",
      swatches: [["Dust", "#101014"], ["Panel", "#1d1b22"], ["Ion Cyan", "#22b8c8"], ["Progress", "#58b947"], ["Quest", "#f2b84b"]]
    }
  };

  function sampleDarkOptionPage(id) {
    const option = sampleDarkOptions[id];

    return shell("learning", `
      <section class="sample-page sample-dark-page sample-dark-theme ${option.className}">
        ${sampleDarkSwitcher(id)}
        <header class="sample-dark-hero">
          <div>
            ${sampleTag(option.label, "dark")}
            <h1>${option.title}</h1>
            <p>${option.subtitle}</p>
          </div>
          <aside class="sample-dark-token-card">
            <strong>${option.accent} palette</strong>
            ${sampleDarkSwatches(option.swatches)}
          </aside>
        </header>

        <div class="sample-dark-layout">
          <aside class="sample-dark-panel sample-dark-side">
            <div class="sample-profile-line">
              <span class="sample-avatar">KV</span>
              <div><strong>knowva_dark</strong><small>Java backend track</small></div>
            </div>
            <div class="sample-side-section">
              <span class="sample-eyebrow">Current course</span>
              <h2>Java 조건문</h2>
              ${progressLine("오늘 학습", 68, "68%")}
              ${progressLine("시험 gate", 72, "7 / 10")}
            </div>
            <div class="sample-dark-orbit-map" aria-hidden="true">
              <span></span><span></span><span></span>
            </div>
          </aside>

          <main class="sample-dark-panel sample-dark-main">
            <header class="sample-section-head compact">
              <div>
                ${sampleTag("오늘 학습", "active")}
                <h2>조건문을 끝내고 반복문 gate 열기</h2>
                <p>${option.principle}</p>
              </div>
              <a class="button primary" href="${href("screens/learning/curriculum.html")}">학습 시작</a>
            </header>

            <section class="sample-dark-mission">
              <div>
                <span class="sample-eyebrow">Mission</span>
                <h3>조건문 문제 7개</h3>
                <p>완료하면 반복문 이론과 문제풀이가 열린다.</p>
              </div>
              <div class="sample-mission-meter"><strong>7</strong><span>문제 남음</span></div>
            </section>

            <section class="sample-lesson-list" aria-label="학습 로드맵">
              <article class="sample-lesson-row done"><span class="sample-node"></span><div><strong>Java 입문</strong><small>개념 4개 · 문제 10/10</small></div>${sampleTag("완료", "done")}</article>
              <article class="sample-lesson-row active"><span class="sample-node"></span><div><strong>조건문</strong><small>오늘 3문제 진행 · 7문제 남음</small></div>${sampleTag("진행 중", "active")}</article>
              <article class="sample-lesson-row locked"><span class="sample-node"></span><div><strong>반복문</strong><small>조건문 80% 완료 시 열림</small></div>${sampleTag("대기", "muted")}</article>
            </section>
          </main>

          <aside class="sample-dark-panel sample-dark-right">
            <article>
              <span class="sample-eyebrow">Signal</span>
              <h2>속도는 느리고 정답률은 안정적</h2>
              ${sampleMiniChart([42, 48, 55, 61, 58, 64, 72])}
            </article>
            <article class="sample-dark-community-row">
              ${sampleTag("질문", "active")}
              <div><strong>조건문 edge case 정리</strong><small>댓글 12 · 좋아요 48 · 12분 전</small></div>
            </article>
            <article class="sample-setting-row">
              <div><strong>테마</strong><span>다크 샘플 고정</span></div>
              <p>${option.title}</p>
              <button class="button" type="button">보기</button>
            </article>
            <article class="sample-kpi">
              <span>오늘 완료</span>
              <strong>184</strong>
              <small>평균 점수 81.4</small>
            </article>
          </aside>
        </div>
      </section>`);
  }

  Object.assign(pages, {
    "sr-007": reviewSummaryPage,
    "sr-007-list": reviewListPage,
    "sr-010": communityHomePage,
    "sr-010-board": communityBoardPage,
    "sr-010-detail": communityDetailPage,
    "sr-010-write": () => communityWritePage("write"),
    "sr-010-edit": () => communityWritePage("edit"),
    "sr-010-profile": communityProfilePage,
    "sr-011": paymentChoicePage,
    "sr-011-card": paymentCardPage,
    "sr-011-bank": paymentBankPage,
    "sr-011-complete": paymentCompletePage,
    "sr-011-recommend": recommendPage,
    "sr-013": adminHomePage,
    "sr-013-login": adminLoginPage,
    "sr-013-stats": adminStatsPage,
    "sr-013-courses": () => adminManagePage("sr-013-courses", adminConfigs["sr-013-courses"][0], adminConfigs["sr-013-courses"][1]),
    "sr-013-theory": () => adminManagePage("sr-013-theory", adminConfigs["sr-013-theory"][0], adminConfigs["sr-013-theory"][1]),
    "sr-013-problems": () => adminManagePage("sr-013-problems", adminConfigs["sr-013-problems"][0], adminConfigs["sr-013-problems"][1]),
    "sr-013-users": () => adminManagePage("sr-013-users", adminConfigs["sr-013-users"][0], adminConfigs["sr-013-users"][1]),
    "sr-013-community": () => adminManagePage("sr-013-community", adminConfigs["sr-013-community"][0], adminConfigs["sr-013-community"][1]),
    "sr-013-reports": () => adminManagePage("sr-013-reports", adminConfigs["sr-013-reports"][0], adminConfigs["sr-013-reports"][1]),
    "sr-013-notices": () => adminManagePage("sr-013-notices", adminConfigs["sr-013-notices"][0], adminConfigs["sr-013-notices"][1]),
    "sr-015": settingsModalPage,
    "sr-015-profile": settingsProfilePage,
    "sr-015-security": settingsSecurityPage,
    "sr-015-social": settingsSocialPage,
    "sr-015-system": settingsSystemPage,
    "sr-015-payment": settingsPaymentPage,
    "sample-learning-main": sampleLearningMainPage,
    "sample-community-board": sampleCommunityBoardPage,
    "sample-settings-index": sampleSettingsIndexPage,
    "sample-admin-stats": sampleAdminStatsPage,
    "sample-dark-orbital-ink": () => sampleDarkOptionPage("sample-dark-orbital-ink"),
    "sample-dark-deep-observatory": () => sampleDarkOptionPage("sample-dark-deep-observatory"),
    "sample-dark-moon-dust": () => sampleDarkOptionPage("sample-dark-moon-dust")
  });

  function initTabs() {
    document.querySelectorAll("[data-tab-scope]").forEach((scope) => {
      scope.querySelectorAll("[data-tab]").forEach((tab) => {
        tab.addEventListener("click", () => {
          const id = tab.dataset.tab;
          scope.querySelectorAll("[data-tab]").forEach((item) => {
            const isActive = item === tab;
            item.classList.toggle("is-active", isActive);
            item.setAttribute("aria-selected", String(isActive));
          });
          scope.querySelectorAll("[data-tab-panel]").forEach((panel) => {
            const isActive = panel.dataset.tabPanel === id;
            panel.classList.toggle("is-active", isActive);
            panel.hidden = !isActive;
          });
        });
      });
    });
  }

  function initTheme() {
    applyTheme(readStoredTheme());
    if (forcedTheme) {
      document.querySelectorAll("[data-theme-toggle]").forEach((button) => {
        button.disabled = true;
      });
      return;
    }
    document.querySelectorAll("[data-theme-toggle]").forEach((button) => {
      button.addEventListener("click", () => {
        const nextTheme = body.classList.contains("theme-dark") ? "light" : "dark";
        applyTheme(nextTheme);
        storeTheme(nextTheme);
      });
    });
  }

  function initFieldNames() {
    document.querySelectorAll("input, select, textarea").forEach((field, index) => {
      if (!field.id) field.id = `${screenId}-field-${index + 1}`;
      if (!field.name) field.name = field.id;
    });
  }

  function render() {
    const app = document.getElementById("app");
    const renderer = pages[screenId] || pages.index;
    app.innerHTML = renderer();
    document.title = screenId === "index" ? "Knowva UI Route Index" : `${route.sr} ${route.title} · Knowva UI HTML2`;
    initFieldNames();
    initTheme();
    initTabs();
  }

  render();
})();
