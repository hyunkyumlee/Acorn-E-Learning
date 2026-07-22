# Knowva 발표 PPT용 핵심 로직 — 이정하 (인증 · 보안 · 세션 · OAuth · 웰컴/튜토리얼)

> 기준: 각 `##`는 PPT 한 장이다. 사용자가 서비스를 처음 만나는 웰컴 화면부터 로그인, 자동 로그인, 회원가입, 소셜 연동, 비밀번호 재설정까지 — **인증 상태가 만들어지고, 검증되고, 무효화되는 흐름** 순서로 정리했다.
> 슬라이드는 **왼쪽 = 화면 캡쳐(현상), 오른쪽 = 코드 발췌(원리)** 구성을 기본으로 한다. 보안 로직은 화면에 "보이지 않는 것"이 핵심이므로, 캡쳐는 결과 화면보다 **에러 메시지·주소창·개발자도구**를 함께 담는 것이 설득력 있다.

## 1. 웰컴·튜토리얼 — 온보딩 단계를 서버가 데이터로 제공

- **구현 파일**: `ELearning/src/main/java/com/acorn/elearning/auth/controller/WelcomeController.java`, `auth/view/TutorialStepView.java`
- **핵심 가치**: 튜토리얼의 단계·문구·마스코트(누비) 포즈·하이라이트 좌표를 화면 JS에 하드코딩하지 않고 서버 모델로 내려, 숫자만 고치면 온보딩 전체가 바뀐다.

### 핵심 설명

- `/`와 `/welcome` 진입 시 로그인 상태면 role별 홈(`/admin` 또는 `/learning`)으로 즉시 redirect — 웰컴은 게스트 전용이다.
- remember-me 인터셉터가 컨트롤러보다 먼저 실행되므로, 쿠키가 유효한 사용자는 `/`로 들어와도 자동으로 자기 홈에 도착한다.

### PPT 코드 발췌

> 위치: `auth/controller/WelcomeController.java` **48~56행** (`resolveWelcomeView`)

```java
private String resolveWelcomeView(HttpSession session, Model model) {
    SessionUser sessionUser = currentUser(session);
    if (sessionUser != null) {
        return "redirect:" + sessionUser.defaultRedirectPath(); // 로그인 상태 → role별 홈
    }
    model.addAttribute("tutorialSteps", TUTORIAL_STEPS);
    return "welcome/index";
}
```

### 발표 포인트

> “온보딩도 인증 흐름의 일부다. 자동 로그인 복원 → role별 redirect → 게스트에게만 튜토리얼이 한 줄기로 이어지고, 튜토리얼 자체는 코드가 아닌 데이터라 언제든 고칠 수 있다.”

### 화면 캡쳐 매핑 - 생략

---

## 2. 로그인은 계정 상태·비밀번호를 검증하고, 실패 사유를 노출하지 않는다

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

### 화면 캡쳐 매핑

| 캡쳐 | 화면/URL | 연출 방법 |
|---|---|---|
| ① 로그인 실패 | `/login` (`auth/login.html`) | 존재하는 이메일 + 틀린 비번, 없는 이메일 두 번 시도해 **에러 메시지가 똑같은 것**을 나란히 캡쳐 |

**연결 방법**: ①의 동일한 에러 문구 두 장을 위아래로 놓고 → `AUTH_INVALID_CREDENTIALS` 한 곳으로 모이는 `login()` 코드에 화살표. "화면은 구분해 주지 않는다 = 코드가 구분하지 않기 때문"으로 잇는다. `safeRedirect`는 캡쳐 없이 코드 발췌만 두고 발표에서 "redirect 파라미터도 내부 경로만 허용한다" 한 줄로 언급.

---

## 3. 인터셉터 체인이 매 요청마다 세션을 DB 최신 상태로 재검증

- **구현 파일**: `ELearning/src/main/java/com/acorn/elearning/security/LoginRequiredInterceptor.java`, `config/WebMvcConfig.java`
- **핵심 가치**: 로그인 시점의 세션 정보를 믿지 않는다. 관리자가 계정을 정지시키면 이미 로그인돼 있던 사용자도 다음 요청에서 즉시 차단된다.

### 핵심 설명

- 보호 경로(`/learning/**`, `/settings/**` 등)는 세션 유무만 보지 않고, 매 요청 DB에서 계정 상태를 다시 조회한다.
- 정지·탈퇴로 바뀐 계정은 세션을 무효화하고 로그인 화면으로 보낸다 (버그 #7: 세션이 DB 최신 상태를 반영하지 않던 문제의 근본 수정).
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

### 화면 캡쳐 매핑

| 캡쳐 | 화면/URL | 연출 방법 |
|---|---|---|
| ① 비로그인 접근 차단 | 주소창 | 로그아웃 상태로 `/learning` 접근 → `/login?redirect=%2Flearning`으로 튕긴 **주소창(redirect 파라미터 포함)** 캡쳐 |

**연결 방법**: ①은 `encodedCurrentPath`가 만든 redirect 파라미터와 주소창을 같은 색으로 하이라이트.

---

## 4. DB 없는 자동 로그인 — HMAC 서명 + 버전으로 위조·재사용 차단

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

### 화면 캡쳐 매핑

| 캡쳐 | 화면/URL | 연출 방법 |
|---|---|---|
| ① 체크박스 | `/login` | "로그인 상태 유지" 체크박스 부분 확대 캡쳐 |
| ② 쿠키 실물 | 개발자도구 | F12 → Application → Cookies → `REMEMBER_ME` 값이 `7.1753142400.f3ab...`처럼 **점 3조각(userId.version.서명)**으로 보이는 화면 |

**연결 방법**: ②의 쿠키 값 3조각에 각각 라벨(userId / version / HMAC)을 달고 `issue()` 코드의 `userId + "." + version + "." + sign(...)` 줄과 1:1 색 매칭. 비밀번호 변경 시 무효화는 이미지 없이 `restoreSession`의 `filter(tokenVersion == currentTokenVersion)` 줄을 가리키며 말로 설명.

---

## 5. 회원가입 — 폼 단계 검증과 4개 테이블 원자적 생성

- **구현 파일**: `ELearning/src/main/java/com/acorn/elearning/auth/service/AuthService.java`, `auth/form/SignupForm.java`, `common/validation/StrongPassword.java`, `PasswordPolicy.java`
- **핵심 가치**: 비밀번호 강도·프로필 포함 여부를 폼 단계에서 선언적으로 검증하고, 회원 하나가 만들어질 때 필요한 4개 테이블 row를 하나의 트랜잭션으로 생성한다 — 반쯤 만들어진 계정이 존재할 수 없다.

### 핵심 설명

- `@StrongPassword`(8~16자, 대·소문자+숫자+특수문자)와 `@AssertTrue` 커스텀 검증(비밀번호에 닉네임·이메일 아이디 포함 금지, 비밀번호 확인 일치)을 폼 클래스에 선언 — 컨트롤러는 `@Valid` 한 줄로 끝난다.
- 검증 규칙의 실체는 공통 `PasswordPolicy` 하나 — 회원가입·비밀번호 변경·재설정이 모두 같은 규칙을 쓴다.
- 이메일은 `user_credentials`, 닉네임은 `users`에서 각각 중복 검사 후 진행한다.
- `@Transactional` 안에서 `users`(계정) → `user_credentials`(BCrypt 해시) → `user_settings`(테마·알림 기본값) → `user_learning_profile`(관심 과목, BRONZE 레벨) 4개 row를 생성 — 중간에 실패하면 전부 롤백된다.

### PPT 코드 발췌

> 위치: `auth/form/SignupForm.java` **14~31행** (검증 선언부)

```java
public class SignupForm {
    @NotBlank @Email private String email;
    @NotBlank @StrongPassword private String password;   // 8~16자, 대소문자+숫자+특수문자
    @NotBlank private String confirmPassword;
    @NotBlank @Size(min = 2, max = 50) private String nickname;
    private Long primarySubjectId;
    private String learningGoal;

    @AssertTrue(message = "비밀번호에 닉네임이나 이메일 아이디를 포함할 수 없습니다.")
    public boolean isPasswordNotContainingProfile() {
        return !PasswordPolicy.containsProfileInfo(password, nickname, email);
    }

    @AssertTrue(message = "비밀번호 확인이 일치하지 않습니다.")
    public boolean isPasswordConfirmed() {
        if (password == null || confirmPassword == null) return true;
        return password.equals(confirmPassword);
    }
}
```

> 위치: `auth/service/AuthService.java` **115~155행** (`signup` — 발췌에서는 setter 나열 일부 축약)

```java
private UserSessionResponse signup(HttpSession session, String email, String rawPassword,
        String nickname, Long primarySubjectId, String learningGoal) {
    if (userCredentialMapper.findByLoginEmail(email).isPresent()) {
        throw new BusinessException(ErrorCode.AUTH_EMAIL_DUPLICATED);
    }
    if (userMapper.existByNickname(nickname)) {
        throw new BusinessException(ErrorCode.AUTH_NICKNAME_DUPLICATED);
    }

    User user = new User();                       // 1) users: 계정 본체
    user.setRole(SessionUser.ROLE_USER);
    user.setStatus(STATUS_ACTIVE);
    userMapper.insert(user);

    UserCredential credential = new UserCredential();  // 2) 비밀번호는 BCrypt 해시로만 저장
    credential.setUserId(user.getUserId());
    credential.setPasswordHash(passwordEncoder.encode(rawPassword));
    userCredentialMapper.insert(credential);

    UserSetting setting = new UserSetting();      // 3) user_settings: 테마·알림 기본값
    setting.setTheme("SYSTEM");
    userSettingMapper.insert(setting);

    UserLearningProfile profile = new UserLearningProfile();  // 4) 학습 프로필: BRONZE 시작
    profile.setPrimarySubjectId(primarySubjectId);
    profile.setCurrentLevelCode("BRONZE");
    userLearningProfileMapper.insert(profile);
    // @Transactional — 넷 중 하나라도 실패하면 전부 롤백
    return sessionService.toSignupResponse(toSessionUser(user));
}
```

### 발표 포인트

> “회원가입은 INSERT 네 번이 아니라 트랜잭션 하나다. 비밀번호 규칙은 어노테이션으로 선언하고 실체는 PasswordPolicy 한 곳에 모아, 가입·변경·재설정 어디서든 같은 기준으로 검사한다.”

### 화면 캡쳐 매핑

| 캡쳐 | 화면/URL | 연출 방법 |
|---|---|---|
| ① 검증 에러 | `/signup` (`auth/signup.html`) | 약한 비밀번호(예: `abc123`)와 닉네임이 들어간 비밀번호를 각각 제출해 **서로 다른 검증 에러 문구** 2컷 |

**연결 방법**: ① 에러 나는 상황을 각각 표시
 - 비밀번호에 닉네임이나 이메일 포함
 - 비밀번호 규칙을 어김
 - 비밀번호와 비밀번호 확인이 다르게 입력됨
 - 약한 비밀번호 입력

---

## 6. 소셜 로그인(OAuth) — 위조된 요청은 state로 거르고, 가입은 2단계로 나눈다

- **구현 파일**: `ELearning/src/main/java/com/acorn/elearning/auth/service/OAuthService.java`, `config/OAuthProperties.java`
- **핵심 가치**: 사용자에게는 버튼 한 번이지만, 서버는 위조 검증 → 기존 회원 확인 → 가입 대기 → 가입 완료의 여러 단계를 거친다. 어느 단계에서 멈추거나 실수해도 계정이 꼬이지 않는다.

### 핵심 설명

- 소셜 로그인을 시작할 때마다 서버가 난수(state)를 만들어 세션에 저장해 두고, 구글/깃허브에서 돌아왔을 때 같은 값인지 대조한다 — 값이 다르면 누군가 위조한 요청이므로 즉시 중단한다.
- 이미 우리 서비스에 연동된 소셜 계정이면 그 자리에서 바로 로그인된다.
- 처음 온 소셜 계정이면 **바로 회원을 만들지 않는다**. 구글에서 받은 정보(이메일·이름)를 세션에만 잠시 보관하고, 닉네임·관심 과목 입력까지 마쳐야 그때 DB에 회원이 생긴다 — 가입 화면에서 나가버려도 반쪽짜리 계정이 남지 않는다.
- 탈퇴했던 사람이 같은 소셜 계정으로 다시 가입할 때도 "이미 사용 중인 계정" 에러가 나지 않는다 — 옛 연동 기록을 새 회원에게 다시 연결해 재가입을 자연스럽게 허용한다.

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

### 발표 포인트

> “소셜 로그인은 버튼 한 번처럼 보이지만, 실제로는 위조 검증 → 가입 대기 → 계정 연결까지 여러 단계다. 어느 단계에서 이탈하거나 실수해도 계정이 꼬이지 않도록 단계마다 안전장치를 넣었다.”

### 화면 캡쳐 매핑

| 캡쳐 | 화면/URL | 연출 방법 |
|---|---|---|
| ① 소셜 로그인 버튼 | `/login` (`auth/login.html`) | 로그인 페이지의 Google/GitHub 로고 버튼 영역 확대 캡쳐 |

**연결 방법**: ①의 버튼 캡쳐에서 `handleLoginCallback` 코드로 화살표를 이어 "버튼 하나 뒤에서 위조 검증 → 연동 확인 → 가입 대기 저장이 일어난다"로 설명. 2단계 가입 화면은 이미지 없이 코드 발췌로만 전달.

---

## 7. 비밀번호 재설정 — 해시로 저장하는 일회용 토큰과 무효화 연쇄

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

### 화면 캡쳐 매핑

| 캡쳐 | 화면/URL | 연출 방법 |
|---|---|---|
| ① 요청 화면 + 요청 완료 화면 | `/password/forgot` (`auth/password-forgot.html`) | 이메일 입력 화면 + 해당 이메일로 url 전송 완료 메시지 |
| ② 재설정 메일 | 메일함 | 링크 URL의 `?token=` 43자 토큰 하이라이트 |
| ③ 재설정 화면 | `/password/reset?token=...` (`auth/password-reset.html`) | 새 비밀번호 입력 화면 |
| ④ 재사용 거부 | 같은 링크 재클릭 | 재설정 완료 후 **같은 메일 링크를 다시 클릭** → 토큰 사용됨/무효 에러 화면 |
|⑤ 재설정 완료 화면| 로그인 페이지 |로그인 페이지에 비번 변경 완료 문구|

**연결 방법**: ②의 메일 토큰과 (선택)의 DB `token_hash`를 나란히 놓고 `sha256Hex(token)` 코드로 잇는다 — "DB가 털려도 링크를 못 만든다". ④는 `markUsed(...) != 1` 조건부 UPDATE 코드와 연결해 "한 번 쓰면 끝, 동시에 눌러도 한 번"을 설명.
