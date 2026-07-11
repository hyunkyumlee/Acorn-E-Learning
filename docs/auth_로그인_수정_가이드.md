# Auth/로그인 수정 가이드

작성자: 이현겸  
기준일: 2026-07-09  
기준 문서: DB 명세 v1.8, `docs/ddl/Knowva_DDL.sql`, `docs/ddl/Knowva_sample_data.sql`

## 1. 확정 정책

DB 정책이 바뀌어서 로그인/회원가입 흐름도 같이 바꿔야 한다.

| 가입 방식 | 계정 생성 기준 |
|---|---|
| 이메일 가입 | 이메일 인증 완료 후 새 `users` 계정 생성 |
| Google 가입 | `provider='google' + provider_user_id` 기준 새 `users` 계정 생성 |
| GitHub 가입 | `provider='github' + provider_user_id` 기준 새 `users` 계정 생성 |
| 이메일 중복 | 기존 회원 이메일과 같아도 계정 병합/연동하지 않고 새 계정 생성 |

`users.email`은 로그인 ID가 아니라 표시/연락용이다.  
계정 식별자는 아래 둘만 사용한다.

| 인증 수단 | 식별자 |
|---|---|
| 이메일 로그인 | `user_credentials.login_email` |
| 소셜 로그인 | `social_accounts.provider + social_accounts.provider_user_id` |

## 2. 수정 대상 파일

| 파일 | 수정 내용 |
|---|---|
| `ELearning/src/main/java/com/acorn/elearning/auth/model/UserCredential.java` | `loginEmail`, `emailVerifiedAt` 필드 추가 |
| `ELearning/src/main/java/com/acorn/elearning/auth/mapper/UserCredentialMapper.java` | `findByEmail`을 `findByLoginEmail` 기준으로 변경 |
| `ELearning/src/main/resources/mappers/auth/UserCredentialMapper.xml` | `user_credentials.login_email`, `email_verified_at` 반영 |
| `ELearning/src/main/java/com/acorn/elearning/auth/service/AuthService.java` | 이메일 회원가입/로그인 기준 변경 |
| `ELearning/src/main/java/com/acorn/elearning/auth/service/OAuthService.java` | pending link 제거, OAuth 미연동이면 새 계정 생성 |
| `ELearning/src/main/java/com/acorn/elearning/auth/model/SocialAccount.java` | `providerEmailVerified` 필드 추가 |
| `ELearning/src/main/resources/mappers/auth/SocialAccountMapper.xml` | `provider_email_verified` select/insert/update 반영 |
| `ELearning/src/main/java/com/acorn/elearning/auth/controller/AuthController.java` | 로그인 후 `consumePendingLink` 호출 제거 |
| `ELearning/src/main/resources/templates/auth/login.html` | `linkPending` 관련 안내가 있으면 제거 |
| `ELearning/src/main/resources/templates/settings/social.html` | 기존 계정 연동 UI면 정책에 맞게 수정 또는 숨김 |

## 3. AuthService 수정

현재 방식은 `users.email` 중복을 막고 있어서 새 정책과 맞지 않는다.

기존 로직:

```java
if (userMapper.findByEmail(email).isPresent()) {
    throw new BusinessException(ErrorCode.AUTH_EMAIL_DUPLICATED);
}
```

새 정책에서는 `users.email` 중복을 허용하므로 이 검사는 제거한다.  
대신 이메일 회원가입 중복 검사는 `user_credentials.login_email` 기준으로 한다.

```java
if (userCredentialMapper.findByLoginEmail(email).isPresent()) {
    throw new BusinessException(ErrorCode.AUTH_EMAIL_DUPLICATED);
}
```

회원가입 시 credential 생성은 아래 기준으로 바꾼다.

```java
UserCredential credential = new UserCredential();
credential.setUserId(user.getUserId());
credential.setLoginEmail(email);
credential.setPasswordHash(passwordEncoder.encode(rawPassword));
credential.setEmailVerifiedAt(LocalDateTime.now());
userCredentialMapper.insert(credential);
```

로그인도 `users.email`이 아니라 `user_credentials.login_email`로 조회한다.

```java
LoginUserRow row = userCredentialMapper.findByLoginEmail(email)
        .orElseThrow(() -> new BusinessException(ErrorCode.AUTH_INVALID_CREDENTIALS));
```

## 4. OAuthService 수정

현재 흐름은 미연동 소셜이면 pending link로 기존 계정에 붙이는 구조다.  
새 정책에서는 이 흐름을 제거한다.

제거할 흐름:

```java
session.setAttribute(PENDING_SOCIAL_KEY, ...);
return "/login?linkPending=...";
consumePendingLink(...);
```

새 OAuth callback 흐름:

```text
/oauth/{provider}/callback
-> provider + provider_user_id 조회
-> 있으면 해당 user_id 로그인
-> 없으면 users 새 row 생성
-> social_accounts 새 row 생성
-> user_settings 생성
-> user_learning_profiles 생성
-> 바로 로그인
```

provider는 반드시 소문자로 저장한다.

```java
provider = provider.toLowerCase(Locale.ROOT);
```

소셜 계정 생성 시에는 아래 필드를 저장한다.

```java
SocialAccount account = new SocialAccount();
account.setUserId(userId);
account.setProvider(provider);
account.setProviderUserId(info.providerUserId());
account.setProviderEmail(info.email());
account.setProviderEmailVerified(info.emailVerified());
account.setIsActive(true);
socialAccountMapper.insert(account);
```

소셜 가입용 `users.email`은 provider email이 있으면 그대로 넣고, 없으면 fallback을 사용한다.

```java
String email = (info.email() != null && !info.email().isBlank())
        ? info.email()
        : provider + "_" + info.providerUserId() + "@social.knowva.local";
```

중요: 이 이메일이 기존 `users.email`과 같아도 검사하지 말고 새 계정을 생성한다.

## 5. SocialAccountMapper 수정

`provider_email_verified` 컬럼이 추가됐으므로 model/resultMap/insert/update에 반영한다.

예시:

```xml
<result property="providerEmailVerified" column="provider_email_verified" />
```

```sql
insert into social_accounts (
  user_id,
  provider,
  provider_user_id,
  provider_email,
  provider_email_verified,
  is_active,
  connected_at
)
values (
  #{userId},
  #{provider},
  #{providerUserId},
  #{providerEmail},
  #{providerEmailVerified},
  #{isActive},
  NOW()
)
```

## 6. DB 기준

이미 문서/DDL은 아래 기준으로 수정되어 있다.

```text
users.email
- UNIQUE 아님
- 중복 허용
- 로그인 식별자 아님

user_credentials.login_email
- UNIQUE
- 이메일 로그인 ID

social_accounts(provider, provider_user_id)
- UNIQUE
- 소셜 로그인 ID

social_accounts.provider_email
- 중복 허용
- 계정 병합 기준 아님
```

## 7. 테스트 시나리오

| 시나리오 | 기대 결과 |
|---|---|
| `learner@knowva.local` 이메일 가입 | 이메일 계정 1개 생성 |
| 같은 이메일로 Google 가입 | 새 `user_id` 생성 |
| 같은 이메일로 GitHub 가입 | 또 다른 새 `user_id` 생성 |
| Google 재로그인 | 기존 Google `user_id`로 로그인 |
| GitHub 재로그인 | 기존 GitHub `user_id`로 로그인 |
| 이메일 로그인 | `user_credentials.login_email` 계정으로만 로그인 |
| 마이페이지 | 현재 로그인한 `user_id` 기준 데이터만 표시 |
| 설정/소셜 계정 화면 | 현재 계정의 social account만 표시 |

## 8. 주의사항

- `users.email`로 계정을 찾거나 중복 검사하지 않는다.
- 소셜 이메일과 이메일 계정의 이메일이 같아도 자동 병합하지 않는다.
- `consumePendingLink` 기반 자동 연동 흐름은 제거한다.
- provider 값은 `google`, `github` 소문자로 통일한다.
- 같은 이메일이어도 학습 진행도, 결제, 프로필, 분석 데이터는 `user_id`별로 분리된다.

## 9. 구현 후 확인할 쿼리

```sql
SELECT email, COUNT(*) AS cnt
FROM users
GROUP BY email
HAVING COUNT(*) > 1;
```

`learner@knowva.local` 같은 이메일이 여러 `user_id`로 나와야 정상이다.

```sql
SELECT
  u.user_id,
  u.email,
  CASE WHEN uc.user_id IS NULL THEN 0 ELSE 1 END AS has_password,
  sa.provider,
  sa.provider_email,
  sa.provider_email_verified
FROM users u
LEFT JOIN user_credentials uc ON uc.user_id = u.user_id
LEFT JOIN social_accounts sa ON sa.user_id = u.user_id
WHERE u.email = 'learner@knowva.local'
ORDER BY u.user_id;
```

기대 형태:

| 계정 | has_password | provider |
|---|---:|---|
| 이메일 가입 계정 | 1 | `NULL` |
| Google 가입 계정 | 0 | `google` |
| GitHub 가입 계정 | 0 | `github` |
