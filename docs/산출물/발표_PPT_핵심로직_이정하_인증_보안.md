# Knowva 발표 PPT용 핵심 로직 — 이정하 (인증 · 보안 · 세션 · OAuth · 웰컴/튜토리얼)

> 기준: 각 `##`는 PPT 한 장이다. 로그인부터 자동 로그인, 소셜 연동, 비밀번호 재설정까지 **인증 상태가 만들어지고, 검증되고, 무효화되는 흐름**만 남겼다.

## 1. 로그인은 계정 상태·비밀번호를 검증하고, 실패 사유를 노출하지 않는다

- **구현 파일**: `ELearning/src/main/java/com/acorn/elearning/auth/service/AuthService.java`, `SessionService.java`, `auth/controller/AuthController.java`
- **핵심 가치**: 이메일 존재 여부와 비밀번호 오류를 같은 메시지로 응답해 계정 탐색(enumeration)을 막고, 로그아웃은 세션 자체를 폐기해 세션 고정 공격을 차단한다.

### 핵심 설명

- 이메일 미존재와 비밀번호 불일치를 모두 `AUTH_INVALID_CREDENTIALS` 하나로 응답한다 — 어떤 이메일이 가입돼 있는지 알아낼 수 없다.
- 비밀번호 검증 전에 계정 상태를 먼저 확인해 정지 계정은 `AUTH_SUSPENDED`로 분리한다.
- 로그아웃은 `removeAttribute`가 아니라 `session.invalidate()`로 세션 ID 자체를 폐기한다 (session fixation 방지).
- 로그인 후 redirect 파라미터는 `/`로 시작하고 `//`가 아닌 내부 경로만 허용한다 (open redirect 방지).

### PPT 코드 발췌

> 위치: `auth/service/AuthService.java` **60~72행** (`login`)

```java
private UserSessionResponse login(HttpSession session, String email, String rawPassword) {
    LoginUserRow row = userCredentialMapper.findByLoginEmail(email)
            .orElseThrow(() -> new BusinessException(ErrorCode.AUTH_INVALID_CREDENTIALS));
    if (!STATUS_ACTIVE.equals(row.getStatus())) {
        throw new BusinessException(ErrorCode.AUTH_SUSPENDED);
    }
    if (!passwordEncoder.matches(rawPassword, row.getPasswordHash())) {
        throw new BusinessException(ErrorCode.AUTH_INVALID_CREDENTIALS);
    }

    SessionUser sessionUser = toSessionUser(row);
    sessionService.saveUser(session, sessionUser);
    return sessionService.toLoginResponse(sessionUser);
}
```

> 위치: `auth/service/SessionService.java` **34~37행** (`logout`) + `auth/controller/AuthController.java` **139~145행** (`safeRedirect`)

```java
public void logout(HttpSession session) {
    // removeAttribute만 하면 세션 ID가 유지돼 세션 고정(session fixation) 공격에 취약 → 세션 자체를 폐기
    session.invalidate();
}

private String safeRedirect(String redirect, String fallback) {
    if (redirect != null && !redirect.isBlank()
            && redirect.startsWith("/") && !redirect.startsWith("//")) {
        return redirect;
    }
    return fallback;
}
```

### 발표 포인트

> “로그인 성공보다 실패 응답이 더 중요하다. 어떤 이메일이 가입돼 있는지, 왜 실패했는지를 밖에서 구분할 수 없게 만들고, 세션은 만들 때가 아니라 버릴 때 확실히 버린다.”

---

## 2. 인터셉터 체인이 매 요청마다 세션을 DB 최신 상태로 재검증

- **구현 파일**: `ELearning/src/main/java/com/acorn/elearning/security/LoginRequiredInterceptor.java`, `config/WebMvcConfig.java`
- **핵심 가치**: 로그인 시점의 세션 정보를 믿지 않는다. 관리자가 계정을 정지시키면 이미 로그인돼 있던 사용자도 다음 요청에서 즉시 차단된다.

### 핵심 설명

- 보호 경로(`/learning/**`, `/settings/**` 등)는 세션 유무만 보지 않고, 매 요청 DB에서 계정 상태를 다시 조회한다.
- 정지·탈퇴로 바뀐 계정은 세션을 무효화하고 로그인 화면으로 보낸다.
- 재검증에 성공하면 최신 사용자 정보(닉네임·프리미엄 여부 등)로 세션을 갱신해 화면과 DB가 어긋나지 않는다.
- 인터셉터 등록 순서를 이용해 remember-me 복원 → 게스트 전용 → 로그인 필수 → 관리자 필수 순으로 겹겹이 검사한다.

### PPT 코드 발췌

> 위치: `security/LoginRequiredInterceptor.java` **24~49행** (`preHandle` — 발췌에서는 26~28행의 `enforce` 개발용 플래그 분기 생략)

```java
@Override
public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
    SessionUser sessionUser = currentUser(request);
    if (sessionUser == null) {
        response.sendRedirect("/login?redirect=" + encodedCurrentPath(request));
        return false;
    }

    // 정지된 계정이면 세션 무효화 후 로그인으로
    Optional<SessionUser> refreshed = authService.revalidate(sessionUser.userId());
    if (refreshed.isEmpty()) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        response.sendRedirect("/login?redirect=" + encodedCurrentPath(request));
        return false;
    }
    request.getSession(false).setAttribute(SessionUser.SESSION_KEY, refreshed.get());
    return true;
}
```

> 위치: `config/WebMvcConfig.java` **26~51행** (`addInterceptors` — 발췌에서는 loginRequired 경로 목록 일부 축약)

```java
// WebMvcConfig — 순서가 곧 보안 정책: 복원이 가장 먼저, 권한 검사는 그 뒤
registry.addInterceptor(rememberMeInterceptor)
        .addPathPatterns("/**").excludePathPatterns("/health");
registry.addInterceptor(guestOnlyInterceptor)
        .addPathPatterns("/login", "/signup", "/password/forgot", "/password/reset");
registry.addInterceptor(loginRequiredInterceptor)
        .addPathPatterns("/learning/**", "/exams/**", "/settings/**", /* ... */ "/mypage");
registry.addInterceptor(adminRequiredInterceptor)
        .addPathPatterns("/admin/**", "/api/admin/**");
```

### 발표 포인트

> “세션은 로그인 순간의 스냅샷일 뿐이다. 요청마다 DB 상태로 재검증하기 때문에 계정 정지가 실시간으로 반영되고, 인터셉터 등록 순서 자체가 인증 파이프라인이 된다.”

---

## 3. DB 없는 자동 로그인 — HMAC 서명 + 버전으로 위조·재사용 차단

- **구현 파일**: `ELearning/src/main/java/com/acorn/elearning/security/RememberMeCookie.java`, `RememberMeInterceptor.java`, `auth/service/AuthService.java`
- **핵심 가치**: 토큰 테이블 없이 `userId.version.HMAC` 쿠키만으로 위조를 막고, 비밀번호를 바꾸면 모든 기기의 자동 로그인이 한 번에 무효화된다.

### 핵심 설명

- 쿠키 값은 `userId.version.HMAC-SHA256(userId:version)` — 서버 비밀키 없이는 한 글자도 위조할 수 없다.
- version은 `password_updated_at`의 epoch 초. 비밀번호를 변경하면 값이 바뀌므로 이전에 발급된 모든 쿠키가 자동 폐기된다.
- 복원 시에도 계정이 `ACTIVE`인지, 쿠키 version이 현재 계정 version과 같은지 이중 확인한다.
- HMAC 비밀키는 기본값 없이 `@Value`로 강제 주입 — 미설정이면 앱이 기동하지 않는다 (fail-fast, 하드코딩 금지).

### PPT 코드 발췌

> 위치: `security/RememberMeCookie.java` **28~37행** (`issue`) + **42~55행** (`resolve`)

```java
// 발급: 발급 시점의 계정 버전을 토큰에 심는다
public void issue(HttpServletResponse response, Long userId, long version) {
    String token = userId + "." + version + "." + sign(userId + ":" + version);
    Cookie cookie = new Cookie(COOKIE_NAME, token);
    cookie.setHttpOnly(true);
    cookie.setPath("/");
    cookie.setMaxAge(MAX_AGE_SECONDS); // 30일
    response.addCookie(cookie);
}

// 검증: 서명 불일치 = 위조
public Token resolve(HttpServletRequest request) {
    for (Cookie c : request.getCookies()) {
        if (!COOKIE_NAME.equals(c.getName())) continue;
        String[] parts = c.getValue().split("\\.");
        if (parts.length != 3) return null;
        if (!sign(parts[0] + ":" + parts[1]).equals(parts[2])) return null;
        return new Token(Long.parseLong(parts[0]), Long.parseLong(parts[1]));
    }
    return null;
}
```

> 위치: `auth/service/AuthService.java` **96~101행** (`restoreSession`) + **103~106행** (`currentTokenVersion`)

```java
// 복원: 상태·버전이 모두 맞을 때만 세션 재생성 (비번 변경 후 옛 쿠키 거부)
public void restoreSession(HttpSession session, Long userId, long tokenVersion) {
    userMapper.findById(userId)
            .filter(user -> STATUS_ACTIVE.equals(user.getStatus()))
            .filter(user -> tokenVersion == currentTokenVersion(userId))
            .ifPresent(user -> sessionService.saveUser(session, toSessionUser(user)));
}

public long currentTokenVersion(Long userId) {
    return userCredentialMapper.findByUserId(userId)
            .map(c -> c.getPasswordUpdatedAt() == null
                    ? 0L : c.getPasswordUpdatedAt().toEpochSecond(ZoneOffset.UTC))
            .orElse(0L);
}
```

### 발표 포인트

> “자동 로그인 토큰을 DB에 저장하지 않았다. 서명이 위조를 막고, 비밀번호 변경 시각이 곧 토큰 버전이 되어 ‘비번을 바꾸면 모든 기기에서 로그아웃’이 별도 코드 없이 성립한다.”

---

## 4. OAuth는 state로 CSRF를 막고, 소셜 가입을 2단계로 분리

- **구현 파일**: `ELearning/src/main/java/com/acorn/elearning/auth/service/OAuthService.java`, `config/OAuthProperties.java`
- **핵심 가치**: 소셜 인증 성공 즉시 회원을 만들지 않는다. provider 정보를 세션에만 보관한 뒤 닉네임·관심 과목을 입력받아 가입을 완성하고, 탈퇴 계정의 재가입까지 같은 흐름으로 처리한다.

### 핵심 설명

- 인가 요청마다 `SecureRandom` state를 세션에 저장하고, callback에서 `provider:state`가 정확히 일치해야만 진행한다 (CSRF·콜백 위조 방지).
- 이미 연동된 소셜이면 즉시 로그인, 미연동이면 DB에 아무것도 만들지 않고 `PendingSocialSignup` record를 세션에 임시 저장 후 가입 화면으로 보낸다.
- 탈퇴(WITHDRAWN) 계정의 소셜 row는 삭제하지 않고, 재가입 시 새 회원으로 repoint해 유니크 제약 충돌 없이 재가입을 허용한다.
- 소셜 연결 해제 시 “마지막 로그인 수단”인지 검사한다 — 비밀번호도 없고 남은 소셜도 없으면 해제를 거부해 계정이 잠기는 것을 막는다.

### PPT 코드 발췌

> 위치: `auth/service/OAuthService.java` **82~118행** (`handleLoginCallback` — 발췌에서는 탈퇴 분기·이메일 대체값 생성부 축약)

```java
// callback: state 검증 → 연동 계정이면 로그인, 아니면 가입 대기 상태로
validateState(session, provider, state);
session.removeAttribute(OAUTH_STATE_KEY);

OAuthUserInfo info = fetchUserInfo(provider, cfg, code);
SocialAccount linked = socialAccountMapper
        .findByProviderAndProviderUserId(provider, info.providerUserId()).orElse(null);
if (linked != null && !STATUS_WITHDRAWN.equals(owner.getStatus())) {
    sessionService.saveUser(session, toSessionUser(owner));
    return sessionUser.defaultRedirectPath();
}

// 신규·재가입 → DB 생성 없이 세션에만 임시 보관 후 회원가입 화면으로
PendingSocialSignup pending = new PendingSocialSignup(
        provider, info.providerUserId(), resolvedEmail, info.name(), info.emailVerified());
session.setAttribute(PENDING_SOCIAL_SIGNUP_KEY, pending);
return "/oauth/signup";
```

> 위치: `auth/service/OAuthService.java` **250~257행** (`deleteSocialAccount` 내부)

```java
// 연결 해제: 유일한 로그인 수단이면 거부 (최소 인증수단 유지 룰)
long activeSocialCount = accounts.stream()
        .filter(a -> Boolean.TRUE.equals(a.getIsActive())).count();
boolean hasPassword = userCredentialMapper.findByUserId(sessionUser.userId())
        .map(c -> c.getPasswordHash() != null && !c.getPasswordHash().isBlank())
        .orElse(false);

if (activeSocialCount <= 1 && !hasPassword) {
    throw new BusinessException(ErrorCode.AUTH_FORBIDDEN,
            "이 소셜 계정은 유일한 로그인 수단이라 해제할 수 없습니다. "
            + "먼저 비밀번호를 설정하거나 다른 소셜 계정을 연결하세요.");
}
```

### 발표 포인트

> “OAuth는 로그인 버튼 하나가 아니라 상태 기계다. state 검증, 가입 대기, 탈퇴 재가입, 연결 해제 각각에 ‘계정을 잃어버릴 수 없게 하는’ 규칙을 심었다.”

---

## 5. 비밀번호 재설정 — 해시로 저장하는 일회용 토큰과 무효화 연쇄

- **구현 파일**: `ELearning/src/main/java/com/acorn/elearning/auth/service/PasswordResetService.java`, `common/validation/PasswordPolicy.java`
- **핵심 가치**: DB가 유출돼도 재설정 링크를 복원할 수 없고, 재설정에 성공하는 순간 기존 자동 로그인 쿠키까지 전부 무효화된다.

### 핵심 설명

- 토큰은 256bit `SecureRandom` → URL-safe Base64로 발급하고, DB에는 SHA-256 해시만 저장한다 (원문은 메일 링크에만 존재).
- `used_at IS NULL` 조건이 걸린 UPDATE로 일회용을 보장한다 — 동시에 두 번 제출돼도 정확히 한 번만 성공한다.
- 60초 재전송 쿨다운으로 메일 폭탄을 막고, 정지·탈퇴 계정에도 “전송됨”과 동일한 응답을 줘 계정 상태를 노출하지 않는다.
- 새 비밀번호는 공통 `PasswordPolicy`로 닉네임·이메일 아이디 포함 여부까지 검사하고, 저장 시 `password_updated_at` 갱신으로 remember-me 토큰 버전이 바뀌어 기존 쿠키가 전부 폐기된다.

### PPT 코드 발췌

> 위치: `auth/service/PasswordResetService.java` **91~108행** (`issueToken`)

```java
// 발급: 쿨다운 → 기존 토큰 무효화 → 해시만 저장, 원문은 메일 링크로
private String issueToken(Long userId) {
    PasswordResetToken latest = passwordResetTokenMapper.findLatestActiveByUserId(userId).orElse(null);
    if (latest != null && latest.getCreatedAt()
            .plusSeconds(RESEND_COOLDOWN_SECONDS).isAfter(LocalDateTime.now())) {
        return null; // 60초 내 재요청 → 메일 폭탄 방지
    }
    passwordResetTokenMapper.invalidateActiveByUserId(userId); // 유효 링크는 항상 최신 1개만

    byte[] bytes = new byte[TOKEN_BYTES]; // 256bit
    secureRandom.nextBytes(bytes);
    String token = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);

    PasswordResetToken row = new PasswordResetToken();
    row.setUserId(userId);
    row.setTokenHash(sha256Hex(token)); // DB에는 해시만
    row.setExpiresAt(LocalDateTime.now().plusMinutes(tokenTtlMinutes)); // 30분
    passwordResetTokenMapper.insert(row);
    return token;
}
```

> 위치: `auth/service/PasswordResetService.java` **116~136행** (`resetPassword` — 발췌에서는 credential/닉네임 조회부 축약)

```java
// 사용: 조건부 UPDATE 한 방으로 일회용 보장 (동시 제출 경쟁 안전)
if (passwordResetTokenMapper.markUsed(row.getTokenId()) != 1) {
    throw new BusinessException(ErrorCode.AUTH_RESET_TOKEN_USED);
}

if (PasswordPolicy.containsProfileInfo(newPassword, nickname, credential.getLoginEmail())) {
    throw new BusinessException(ErrorCode.AUTH_PASSWORD_TOO_GUESSABLE);
}

credential.setPasswordHash(passwordEncoder.encode(newPassword));
// update()가 password_updated_at = NOW()를 함께 갱신
// → remember-me 토큰 버전이 달라져 기존 자동 로그인 쿠키가 전부 무효화된다
userCredentialMapper.update(credential);
```

### 발표 포인트

> “재설정 토큰은 비밀번호와 같은 등급으로 다뤘다. 해시 저장, 30분 만료, 조건부 UPDATE 일회용, 그리고 재설정 성공이 곧 전 기기 자동 로그아웃으로 이어지는 무효화 연쇄까지가 하나의 설계다.”

---

## 6. 웰컴·튜토리얼 — 온보딩 단계를 서버가 데이터로 제공

- **구현 파일**: `ELearning/src/main/java/com/acorn/elearning/auth/controller/WelcomeController.java`, `auth/view/TutorialStepView.java`
- **핵심 가치**: 튜토리얼의 단계·문구·마스코트(누비) 포즈·하이라이트 좌표를 화면 JS에 하드코딩하지 않고 서버 모델로 내려, 숫자만 고치면 온보딩 전체가 바뀐다.

### 핵심 설명

- `/`와 `/welcome` 진입 시 로그인 상태면 role별 홈(`/admin` 또는 `/learning`)으로 즉시 redirect — 웰컴은 게스트 전용이다.
- remember-me 인터셉터가 컨트롤러보다 먼저 실행되므로, 쿠키가 유효한 사용자는 `/`로 들어와도 자동으로 자기 홈에 도착한다.
- 튜토리얼 5단계는 `TutorialStepView` 리스트로 정의 — 각 단계가 스크린샷, 누비 포즈(WAVING/READING 등), 말풍선, 하이라이트 박스 좌표를 갖는다.
- 지금은 상수 리스트지만 구조가 데이터이므로 추후 DB·설정 이동이 쉽다.

### PPT 코드 발췌

> 위치: `auth/controller/WelcomeController.java` **16~34행** (`TUTORIAL_STEPS` — 발췌에서는 5단계 중 1·3단계만 표시) + **48~56행** (`resolveWelcomeView`)

```java
private static final List<TutorialStepView> TUTORIAL_STEPS = List.of(
        TutorialStepView.of(1, "로드맵 기반 학습",
                "학습 순서를 따라가며 목표를 하나씩 달성하고 실력을 쌓아보세요",
                "/assets/images/tutorial/1-learning-roadmap.png", 88, 30, "WAVING",
                20, 21, 58, 71, "학습 진행도를 한 눈에 볼 수 있어요!"),
        TutorialStepView.of(3, "레벨 코딩 테스트",
                "학습한 내용을 바탕으로 실력을 점검하고 다음 단계에 도전하세요",
                "/assets/images/tutorial/3-codingtest.png", 50, 15, "TELESCOPE",
                1, 27, 96, 51, "AI가 만든 문제로 공부가 잘 됐는지 확인해봐요!")
        /* ... 총 5단계 ... */
);

private String resolveWelcomeView(HttpSession session, Model model) {
    SessionUser sessionUser = currentUser(session);
    if (sessionUser != null) {
        return "redirect:" + sessionUser.defaultRedirectPath(); // 로그인 상태 → role별 홈
    }
    model.addAttribute("tutorialSteps", TUTORIAL_STEPS); // 서버가 튜토리얼 단계 제공
    return "welcome/index";
}
```

### 발표 포인트

> “온보딩도 인증 흐름의 일부다. 자동 로그인 복원 → role별 redirect → 게스트에게만 튜토리얼이 한 줄기로 이어지고, 튜토리얼 자체는 코드가 아닌 데이터라 언제든 고칠 수 있다.”

---

# 부록: 슬라이드별 화면 캡쳐 매핑

> 각 슬라이드는 **왼쪽 = 화면 캡쳐(현상), 오른쪽 = 코드 발췌(원리)** 구성을 기본으로 한다.
> 보안 로직은 화면에 "보이지 않는 것"이 핵심이므로, 캡쳐는 결과 화면보다 **에러 메시지·주소창·개발자도구**를 함께 담는 것이 설득력 있다.

## 슬라이드 1 — 로그인·세션

| 캡쳐 | 화면/URL | 연출 방법 |
|---|---|---|
| ① 로그인 실패 | `/login` (`auth/login.html`) | 존재하는 이메일 + 틀린 비번, 없는 이메일 두 번 시도해 **에러 메시지가 똑같은 것**을 나란히 캡쳐 |

**연결 방법**: ①의 동일한 에러 문구 두 장을 위아래로 놓고 → `AUTH_INVALID_CREDENTIALS` 한 곳으로 모이는 `login()` 코드에 화살표. "화면은 구분해 주지 않는다 = 코드가 구분하지 않기 때문"으로 잇는다. `safeRedirect`는 캡쳐 없이 코드 발췌만 두고 발표에서 "redirect 파라미터도 내부 경로만 허용한다" 한 줄로 언급.

## 슬라이드 2 — 인터셉터 세션 재검증

| 캡쳐 | 화면/URL | 연출 방법 |
|---|---|---|
| ① 비로그인 접근 차단 | 주소창 | 로그아웃 상태로 `/learning` 접근 → `/login?redirect=%2Flearning`으로 튕긴 **주소창(redirect 파라미터 포함)** 캡쳐 |
| ② 정지 계정 실시간 차단 | `/admin/users` (`admin/adminUsers.html`) + 사용자 브라우저 | 브라우저 2개: A(사용자)는 로그인된 채 두고, B(관리자)에서 A 계정 정지 → A가 아무 메뉴나 클릭하는 순간 로그인으로 튕기는 장면을 전/후 2컷 |

**연결 방법**: ②의 "관리자 정지 클릭 → 사용자 튕김" 2컷 사이에 `revalidate()` 코드 블록을 끼워 넣어 **매 요청 DB 재조회가 그 사이에 있다**는 흐름도로 구성. ①은 `encodedCurrentPath`가 만든 redirect 파라미터와 주소창을 같은 색으로 하이라이트.

## 슬라이드 3 — 자동 로그인 (remember-me)

| 캡쳐 | 화면/URL | 연출 방법 |
|---|---|---|
| ① 체크박스 | `/login` | "로그인 상태 유지" 체크박스 부분 확대 캡쳐 |
| ② 쿠키 실물 | 개발자도구 | F12 → Application → Cookies → `REMEMBER_ME` 값이 `7.1753142400.f3ab...`처럼 **점 3조각(userId.version.서명)**으로 보이는 화면 |
| ③ 비번 변경 후 무효화 | 개발자도구 + `/login` | 비밀번호 변경 → 브라우저 재시작(세션 소멸) → 쿠키는 남아 있는데 로그인 화면이 뜨는 장면 |

**연결 방법**: ②의 쿠키 값 3조각에 각각 라벨(userId / version / HMAC)을 달고 `issue()` 코드의 `userId + "." + version + "." + sign(...)` 줄과 1:1 색 매칭. ③은 `restoreSession`의 `filter(tokenVersion == currentTokenVersion)` 줄에 화살표 — "쿠키는 살아 있지만 버전이 달라 거부됐다".

## 슬라이드 4 — OAuth 2단계 가입

| 캡쳐 | 화면/URL | 연출 방법 |
|---|---|---|
| ① 소셜 로그인 버튼 | `/login` | Google/GitHub 버튼 영역 |
| ② state 파라미터 | 개발자도구 Network 탭 (또는 GitHub 인가 화면 주소창) | ⚠️ 구글은 동의 화면 진입 직후 내부 URL로 바꿔버려 주소창에 `state`가 안 남는다. 두 가지 중 택1: **(a)** F12 → Network → "Preserve log" 체크 후 구글 버튼 클릭 → `/oauth/google` 요청의 302 응답 `Location` 헤더(인가 URL 전체)와, 로그인 완료 후 돌아오는 `/oauth/google/callback?code=...&state=...` 요청 URL — **보내는 state와 돌아온 state가 같은 값**임을 두 캡쳐로 보여준다. **(b)** 더 쉬운 방법: GitHub 로그인 사용 — `github.com/login/oauth/authorize?...&state=...`는 주소창에 state가 그대로 보인다 |
| ③ 소셜 회원가입 | `/oauth/signup` (`auth/signup.html` 소셜 분기) | 이메일이 읽기전용으로 채워지고 닉네임·관심과목만 입력받는 화면 — "아직 DB에 없다" 강조 |
| ④ 최소 인증수단 거부 | `/settings/social` (`settings/social.html`) | 비밀번호 없는 소셜 계정으로 마지막 소셜 연결 해제 시도 → "유일한 로그인 수단이라 해제할 수 없습니다" 에러 캡쳐 |

**연결 방법**: ①→②→③을 플로우 화살표로 잇고, ③ 옆에 `PendingSocialSignup` 세션 저장 코드를 붙여 "화면 사이의 상태는 DB가 아니라 세션에 있다"로 설명. ④는 에러 문구 캡쳐와 `activeSocialCount <= 1 && !hasPassword` 조건식을 나란히 — 문구가 코드에서 그대로 나온 것임을 보여준다.

## 슬라이드 5 — 비밀번호 재설정

| 캡쳐 | 화면/URL | 연출 방법 |
|---|---|---|
| ① 요청 화면 | `/password/forgot` (`auth/password-forgot.html`) | 이메일 입력 화면 |
| ② 재설정 메일 | 메일함 | 링크 URL의 `?token=` 43자 토큰 하이라이트 |
| ③ 재설정 화면 | `/password/reset?token=...` (`auth/password-reset.html`) | 새 비밀번호 입력 화면 |
| ④ 재사용 거부 | 같은 링크 재클릭 | 재설정 완료 후 **같은 메일 링크를 다시 클릭** → 토큰 사용됨/무효 에러 화면 |
| (선택) DB 대조 | DB 클라이언트 | `password_reset_tokens`의 `token_hash` 컬럼 — 메일의 토큰과 **다른 값(해시)**임을 나란히 |

**연결 방법**: ②의 메일 토큰과 (선택)의 DB `token_hash`를 나란히 놓고 `sha256Hex(token)` 코드로 잇는다 — "DB가 털려도 링크를 못 만든다". ④는 `markUsed(...) != 1` 조건부 UPDATE 코드와 연결해 "한 번 쓰면 끝, 동시에 눌러도 한 번"을 설명.

## 슬라이드 6 — 웰컴·튜토리얼

| 캡쳐 | 화면/URL | 연출 방법 |
|---|---|---|
| ① 웰컴 + 튜토리얼 | `/` (`welcome/index.html`) | 튜토리얼 오버레이가 뜬 상태 — 누비 캐릭터·말풍선·하이라이트 박스가 모두 보이는 단계(1단계 로드맵 권장) |
| ② 자동 redirect | 주소창 2컷 | remember-me 쿠키가 있는 브라우저에서 `/` 입력 → `/learning`(또는 `/admin`)으로 바뀐 주소창 전/후 |

**연결 방법**: ①의 캡쳐에서 누비 위치·말풍선·하이라이트 박스에 점선 박스를 치고, `TutorialStepView.of(1, ..., 88, 30, "WAVING", 20, 21, 58, 71, "...")`의 **숫자 인자와 1:1로 선을 연결** — "화면의 모든 좌표가 서버 데이터 한 줄"이 이 슬라이드의 한 방이다. ②는 슬라이드 2·3과 수미상관으로 "인증 흐름의 시작과 끝" 마무리 멘트용.
