(() => {
  const body = document.body;
  const basePath = normalizeBase(body.dataset.basePath || "./");
  const screenId = body.dataset.screen || "index";

  const routes = [
    ["sr-001", "SR-001", "웰컴/튜토리얼", "screens/welcome/index.html", "welcome", "비로그인 소개, 튜토리얼, 가입/로그인 유도"],
    ["sr-002-login", "SR-002", "로그인", "screens/auth/login.html", "auth", "이메일/소셜 로그인, 상태 유지 안내"],
    ["sr-002-signup", "SR-002", "회원가입", "screens/auth/signup.html", "auth", "가입, 관심 과목, 목표 설정"],
    ["sr-003", "SR-003", "학습 메인", "screens/learning/main.html", "learning", "Mission Control Roadmap"],
    ["sr-004", "SR-004", "초기 설정", "screens/learning/onboarding.html", "learning", "언어, 목표, 시작점 선택"],
    ["sr-005", "SR-005", "이론 학습", "screens/learning/curriculum.html", "learning", "짧은 개념, 예시 코드, 바로 풀기"],
    ["sr-006", "SR-006", "일반 문제풀이", "screens/learning/practice.html", "learning", "short loop, 즉시 피드백, AI 미사용"],
    ["sr-007", "SR-007", "오답 복습", "screens/learning/review.html", "learning", "복습 mission, 반복 오답, 해설"],
    ["sr-008", "SR-008", "코딩테스트", "screens/exam/coding-test.html", "learning", "좌우 2분할, 3문항 일괄 제출, AI 채점"],
    ["sr-009", "SR-009", "AI 분석", "screens/analysis/index.html", "analysis", "기본 분석과 premium 분석 분리"],
    ["sr-010", "SR-010", "커뮤니티 홈", "screens/community/index.html", "community", "자유게시판 진입, 인기글, 커뮤니티 프로필"],
    ["sr-010-board", "SR-010", "게시판 글 목록", "screens/community/board.html", "community", "언어별 게시판, 전체/자유/질문/정보 필터"],
    ["sr-010-write", "SR-010", "게시글 작성", "screens/community/write.html", "community", "게시판 안에서 진입하는 글쓰기 전용 화면"],
    ["sr-010-profile", "SR-010", "커뮤니티 프로필", "screens/community/profile.html", "community", "나의 블로그형 커뮤니티 프로필"],
    ["sr-011", "SR-011", "결제", "screens/payment/index.html", "analysis", "더미 결제, 결제 상태, 분석 접근"],
    ["sr-012", "SR-012", "마이페이지", "screens/mypage/index.html", "mypage", "학습/커뮤니티 활동 요약"],
    ["sr-013", "SR-013", "관리자", "screens/admin/dashboard.html", "admin", "관리자 홈, 통계, 운영 관리"],
    ["sr-014", "SR-014", "랭킹", "screens/ranking/index.html", "learning", "출석 제외 통합 랭킹"],
    ["sr-015", "SR-015", "설정", "screens/settings/index.html", "settings", "회원 정보, 보안, 소셜, 시스템, 결제"]
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
        <a class="brand header-logo" href="${href("index.html")}" aria-label="Knowva UI index">로고</a>
        <nav class="topbar-nav" aria-label="main navigation">${links}</nav>
        <div class="topbar-actions">
          <button class="header-icon theme-toggle" type="button" data-theme-toggle aria-pressed="false" aria-label="다크모드로 전환"><span class="theme-icon" aria-hidden="true"></span></button>
          <a class="header-icon settings-icon${activeClass(activeMenu === "settings")}" href="${href("screens/settings/index.html")}" aria-label="설정"><span aria-hidden="true">⚙</span></a>
          <a class="profile-pill${activeClass(activeMenu === "mypage")}" href="${href("screens/mypage/index.html")}">프로필</a>
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
      ["info", "정보"]
    ];
    return `<nav class="community-filters" aria-label="게시판 필터">${filters.map(([id, label]) =>
      `<a class="badge${activeClass(id === active)}" href="${href(`screens/community/board.html#${id}`)}">${label}</a>`
    ).join("")}</nav>`;
  }

  function communityRail(active = "java") {
    const languages = [
      ["java", "Java", "자유 · 질문 · 정보"],
      ["sql", "SQL", "자유 · 질문 · 정보"],
      ["web", "HTML/CSS/JS", "자유 · 질문 · 정보"],
      ["python", "Python", "자유 · 질문 · 정보"]
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
          ["SQL JOIN 시각화 자료", "정보 · 댓글 8 · 좋아요 33 · 스크랩 21"],
          ["오늘 학습 일지", "자유 · 댓글 5 · 좋아요 17"],
          ["반복문 문제 풀이 팁", "정보 · 댓글 3 · 스크랩 14"]
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
                ${list([["orbit.dev", "Java 답변 18개 · 좋아요 420"], ["signal.sql", "SQL 정보글 12개 · 스크랩 88"]])}
              </article>
            </section>
          </main>
        </section>`);
    },

    "sr-010-board"() {
      return shell("community", `
        ${pageHero("SR-010 · Java 게시판", "게시판을 선택하면 글 목록 화면으로 이동", "전체/자유/질문/정보는 이 화면 안에서 글 목록 필터처럼 동작한다.", button("글쓰기", "screens/community/write.html", "primary"))}
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
            <label>게시판<select class="field"><option>Java · 자유 게시판</option><option>Java · 질문 게시판</option><option>Java · 정보 게시판</option></select></label>
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
      return shell("settings", `
        <section class="settings-overlay-demo">
          <div class="dimmed-quiz">
            <div class="quiz-top"><span class="close-link">×</span><strong>3 / 10</strong>${progress(30)}</div>
            <h2>다음 중 GoF 디자인 패턴에 대한 설명으로 옳지 않은 것은?</h2>
            <div class="choice-list">
              ${choiceRow("A", "Singleton 패턴은 클래스의 인스턴스가 오직 하나만 생성되도록 보장한다.")}
              ${choiceRow("B", "Factory Method 패턴은 객체 생성을 서브 클래스에 위임한다.")}
              ${choiceRow("C", "Observer 패턴은 상태 변화가 관련 객체들에게 전파되도록 한다.")}
            </div>
          </div>
          <div class="settings-dialog">
            <aside class="settings-menu">
              <a href="${href("screens/learning/practice.html")}" class="close-link">×</a>
              <h2>설정</h2>
              ${list([["회원 정보", "닉네임, 관심 과목, 목표"], ["이메일/비밀번호", "인증 정보 관리"], ["소셜 계정", "Google/Kakao 연동"], ["시스템", "테마, 알림, 접근성"], ["결제 정보", "더미 결제 상태"], ["로그아웃", "현재 세션 종료"]])}
            </aside>
            ${tabs("settings", [
              { id: "profile", label: "회원 정보", html: `<div class="settings-summary"><strong>nova_learner</strong><span>Java backend · 하루 20분 목표 · Level 12</span><span class="badge">프로필 공개</span></div><form class="form-grid"><label>닉네임<input class="field" value="nova_learner"></label><label>관심 과목<input class="field" value="Java backend"></label><label>목표<input class="field" value="하루 20분"></label><div class="button-row">${button("저장", "screens/settings/index.html", "primary")}</div></form>` },
              { id: "security", label: "이메일/비밀번호", html: `<form class="form-grid"><label>이메일<input class="field" value="learner@k***.dev"></label><label>새 비밀번호<input class="field" type="password" value="password"></label><label>새 비밀번호 확인<input class="field" type="password" value="password"></label><div class="button-row">${button("변경", "screens/settings/index.html", "primary")}</div></form>` },
              { id: "social", label: "소셜 계정", html: `<div class="two-col">${card("Google", "연동됨 · c***@gmail.com", "Connected")}${card("Kakao", "연동 가능", "Available", button("연동", "screens/settings/index.html"))}</div>` },
              { id: "system", label: "시스템 설정", html: `<div class="three-col">${card("테마", "헤더의 모드 전환과 저장된 사용자 선호를 화면 전체에 적용한다.", "Theme")}${card("알림", "mission, gate unlock, 분석 완료 알림.", "Notification")}${card("접근성", "reduced motion, focus ring, label 유지.", "A11y")}</div>` },
              { id: "payment", label: "결제 정보", html: `${card("Premium 상태", "더미 결제 완료. 실제 PG 결제 없음.", "Dummy paid", button("결제 화면", "screens/payment/index.html", "primary"))}` },
              { id: "logout", label: "로그아웃", html: `${card("로그아웃", "현재 세션을 종료하고 로그인 화면으로 이동한다.", "Session", button("로그아웃", "screens/auth/login.html", "warn"))}` }
            ])}
          </div>
        </section>`, { showTopbar: false });
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
    document.querySelectorAll("[data-theme-toggle]").forEach((button) => {
      button.addEventListener("click", () => {
        const nextTheme = body.classList.contains("theme-dark") ? "light" : "dark";
        applyTheme(nextTheme);
        storeTheme(nextTheme);
      });
    });
  }

  function render() {
    const app = document.getElementById("app");
    const renderer = pages[screenId] || pages.index;
    app.innerHTML = renderer();
    document.title = screenId === "index" ? "Knowva UI Route Index" : `${route.sr} ${route.title} · Knowva UI HTML2`;
    initTheme();
    initTabs();
  }

  render();
})();
