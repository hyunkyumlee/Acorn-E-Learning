# Knowva 발표 PPT용 핵심 로직 — 공통 (`common/`)

> 기준: 각 `##`는 PPT 한 장이다. 코드 블록은 실제 구현을 발표 분량으로만 발췌했다. 왼쪽에는 **핵심 설명**, 오른쪽에는 **코드**를 배치하면 된다.

## 1. 하나의 예외 정책으로 HTML 화면과 JSON API를 동시에 처리

- **구현 파일**: `ELearning/src/main/java/com/acorn/elearning/common/exception/GlobalExceptionHandler.java`, `ErrorCode.java`, `response/ApiResponse.java`
- **핵심 가치**: 같은 서비스 예외라도 브라우저 화면에는 오류 페이지를, AJAX/API 호출에는 표준 JSON 오류를 반환한다.

### 핵심 설명

- 도메인 서비스는 `BusinessException + ErrorCode`만 던지고, HTTP 상태·오류 코드·사용자 메시지 결정은 공통 계층으로 모았다.
- `Accept` header의 우선순위와 quality 값을 읽어 HTML 요청인지 JSON 요청인지 구분한다.
- validation 오류는 field별 메시지로, 예상하지 못한 오류는 내부 예외 내용을 노출하지 않는 공통 오류 코드로 응답한다.
- 화면과 API가 섞인 MVC 구조에서도 오류 응답 형식을 일관되게 유지한다.

### PPT 코드 발췌

```java
@ExceptionHandler(BusinessException.class)
Object handleBusinessException(BusinessException exception,
        HttpServletRequest request) {
    ErrorCode errorCode = exception.errorCode();

    if (acceptsHtml(request)) {
        String errorView = switch (errorCode.status()) {
            case NOT_FOUND -> "error/404";
            case FORBIDDEN, UNAUTHORIZED -> "error/403";
            default -> "error/500";
        };
        return errorPage(errorView, errorCode.status());
    }

    return ResponseEntity.status(errorCode.status())
            .contentType(MediaType.APPLICATION_JSON)
            .body(ApiResponse.fail(
                    exception.getMessage(),
                    errorCode.code(),
                    exception.getMessage()));
}
```

### 발표 포인트

> “기능마다 예외 처리 방식을 따로 만들지 않았다. 공통 오류 정책이 화면과 API의 표현 방식만 분리하고, 서비스는 비즈니스 오류 자체에만 집중한다.”

---

## 2. 세션 기반 one-time token으로 중복 POST 요청 차단

- **구현 파일**: `ELearning/src/main/java/com/acorn/elearning/common/idempotency/IdempotencyTokenService.java`
- **적용 지점**: 커뮤니티 게시글 발행, AI 코딩테스트 생성·답안 저장, 결제 화면 등
- **핵심 가치**: 새로고침·더블 클릭·느린 네트워크로 동일 form이 반복 제출되는 문제를 공통 방식으로 막는다.

### 핵심 설명

- form을 열 때 UUID token을 세션에 발급하고, POST 시 token을 한 번만 소비한다.
- 이미 소비한 token 또는 token이 없는 요청은 `COMMON_IDEMPOTENCY_*` 오류로 중단한다.
- token 저장소는 세션별 `ConcurrentHashMap`이므로 사용자 세션 사이에 섞이지 않는다.
- 각 도메인은 token 검증을 재구현하지 않고 controller에서 공통 서비스를 호출한다.

### PPT 코드 발췌

```java
public IdempotencyToken issue(String formType,
        HttpSession session, Long userId) {
    String value = UUID.randomUUID().toString();
    IdempotencyToken token = new IdempotencyToken(
            value, formType, userId, LocalDateTime.now());
    tokens(session).put(value, token);
    return token;
}

public void requireAndConsume(String tokenValue,
        String requestHash, HttpSession session) {
    if (!hasText(tokenValue)) {
        throw new BusinessException(
                ErrorCode.COMMON_IDEMPOTENCY_KEY_REQUIRED);
    }
    if (tokens(session).remove(tokenValue) == null) {
        throw new BusinessException(
                ErrorCode.COMMON_IDEMPOTENCY_CONFLICT);
    }
}
```

### 발표 포인트

> “중복 방지는 결제에만 필요한 기능이 아니다. 게시글, 답안, 시험 생성처럼 상태를 바꾸는 모든 form에 같은 one-time token 패턴을 적용했다.”

---

## 3. AI provider 호출을 공통 boundary에서 제한·구조화

- **구현 파일**: `ELearning/src/main/java/com/acorn/elearning/common/ai/ChatGptApiClient.java`
- **핵심 가치**: AI 기능마다 HTTP 호출, timeout, JSON 파싱, 설정 검증을 중복 구현하지 않는다.

### 핵심 설명

- provider 활성화 여부, provider 종류, model, API key 설정을 호출 전에 검증한다.
- 모든 요청에 30초 timeout과 JSON-only response format을 적용한다.
- HTTP 상태가 2xx가 아니거나 응답 content가 비어 있으면 공통 `BusinessException`으로 변환한다.
- interrupt 발생 시 thread interrupt 상태를 다시 설정해 서버 thread 제어 규칙을 지킨다.

### PPT 코드 발췌

```java
public ChatGptResponse send(ChatGptRequest request) {
    requireConfiguration();
    try {
        String requestBody = objectMapper.writeValueAsString(body(request));
        HttpRequest httpRequest = HttpRequest.newBuilder()
                .uri(URI.create(normalizedBaseUrl() + "/chat/completions"))
                .timeout(TIMEOUT)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

        HttpResponse<String> response = httpClient.send(
                httpRequest, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new BusinessException(ErrorCode.COMMON_INTERNAL_ERROR);
        }
        String content = extractContent(response.body());
        return new ChatGptResponse(
                "SUCCESS", provider, baseUrl, model,
                request.purpose(), content, response.body(),
                Map.of("promptVersion", request.promptVersion()));
    } catch (InterruptedException exception) {
        Thread.currentThread().interrupt();
        throw new BusinessException(ErrorCode.COMMON_INTERNAL_ERROR);
    }
}
```

### 발표 포인트

> “AI 출제와 AI 분석은 서로 다른 기능이지만 외부 LLM을 호출하는 위험 지점은 같다. 공통 client 하나가 설정 검증, timeout, JSON 응답 검증을 책임진다.”

---

## 4. AI 응답에서 시스템 코드 칭찬·개발 용어를 제거

- **구현 파일**: `ELearning/src/main/java/com/acorn/elearning/common/ai/AiGeneratedTextSanitizer.java`
- **핵심 가치**: AI가 기본 starter code나 내부 구현 용어를 학습자의 실력으로 칭찬하거나 화면에 노출하지 않도록 후처리한다.

### 핵심 설명

- 문장 단위로 AI 응답을 나눈 뒤, starter code 관련 용어와 칭찬 표현이 함께 있는 문장을 제거한다.
- `TODO`, 구현 예시 등 사용자에게 보여줄 필요가 없는 개발 용어를 제거한다.
- 사용자 화면에 맞게 “요구사항”을 “조건”처럼 이해하기 쉬운 표현으로 통일한다.
- LLM의 자유로운 문장 생성과 서비스가 보장해야 할 사용자 경험을 분리한 안전망이다.

### PPT 코드 발췌

```java
public static String cleanUserFacingAiText(String text) {
    String sanitized = removeStarterCodePraise(text);
    if (sanitized.isBlank()) {
        return "";
    }
    return SENTENCE_BREAK.splitAsStream(sanitized)
            .map(String::strip)
            .map(AiGeneratedTextSanitizer::replaceUserFacingTerms)
            .filter(sentence -> !sentence.isBlank())
            .filter(sentence -> !containsAny(
                    sentence, HIDDEN_DEVELOPMENT_TERMS))
            .reduce("", AiGeneratedTextSanitizer::joinSentence)
            .strip();
}

public static boolean isStarterCodePraise(String text) {
    return containsAny(text, GENERIC_STARTER_PRAISE_TERMS)
            || (containsAny(text, PRAISE_TERMS)
                && containsAny(text, STARTER_CODE_TERMS));
}
```

### 발표 포인트

> “AI 응답을 그대로 화면에 표시하지 않는다. 시스템이 제공한 코드를 학습자 실력으로 오인하지 않도록, 생성 후 사용자 관점의 문장만 남긴다.”
