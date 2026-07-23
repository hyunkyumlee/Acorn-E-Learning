/*
  Knowva Java curriculum learning data (MySQL 8)
  Source: 프로젝트자료/Knowva_Java_이론.md and java_Quiz.md
  Prerequisite: run Knowva_DDL.sql first.
  Scope: 15 planets, 150 lessons, 1,500 practice problems.
  Node IDs follow Knowva_sample_data.sql: Bronze 1-5, Silver 11-15, Gold 21-25.
*/

USE elearning;

SET NAMES utf8mb4;
START TRANSACTION;

-- One-time repair for the earlier generated 1001-1015 node range. Existing user records are cleared only for that obsolete range.
DELETE attendance FROM attendance_records attendance JOIN practice_set_attempts attempt ON attempt.set_attempt_id = attendance.qualified_set_attempt_id WHERE attempt.node_id BETWEEN 1001 AND 1015;
DELETE wrong_answer FROM wrong_answers wrong_answer JOIN practice_problems problem ON problem.problem_id = wrong_answer.problem_id WHERE problem.node_id BETWEEN 1001 AND 1015;
DELETE submission FROM practice_submissions submission JOIN practice_problems problem ON problem.problem_id = submission.problem_id WHERE problem.node_id BETWEEN 1001 AND 1015;
DELETE item FROM practice_set_items item JOIN practice_problems problem ON problem.problem_id = item.problem_id WHERE problem.node_id BETWEEN 1001 AND 1015;
DELETE FROM practice_set_attempts WHERE node_id BETWEEN 1001 AND 1015;
DELETE choice_row FROM problem_choices choice_row JOIN practice_problems problem ON problem.problem_id = choice_row.problem_id WHERE problem.node_id BETWEEN 1001 AND 1015;
DELETE FROM practice_problems WHERE node_id BETWEEN 1001 AND 1015;
DELETE bookmark FROM lesson_bookmarks bookmark JOIN lessons lesson ON lesson.lesson_id = bookmark.lesson_id WHERE lesson.node_id BETWEEN 1001 AND 1015;
DELETE lesson_progress FROM user_lesson_progress lesson_progress JOIN lessons lesson ON lesson.lesson_id = lesson_progress.lesson_id WHERE lesson.node_id BETWEEN 1001 AND 1015;
DELETE FROM learning_progress WHERE node_id BETWEEN 1001 AND 1015;
DELETE FROM lessons WHERE node_id BETWEEN 1001 AND 1015;
DELETE FROM curriculum_nodes WHERE node_id BETWEEN 1001 AND 1015;

-- Authored Java curriculum uses the existing sample-data node ranges; re-running updates the same records.
SELECT subject_id INTO @java_subject_id FROM subjects WHERE subject_code = 'JAVA';

INSERT INTO curriculum_nodes (node_id, subject_id, parent_node_id, level_code, node_type, planet_no, title, description, sort_order, gate_condition, is_active)
VALUES
  (1, @java_subject_id, NULL, 'BRONZE', 'PLANET', 1, 'Java 기본 구조', 'Java Bronze 학습 과정: Java 기본 구조', 1, NULL, 1),
  (2, @java_subject_id, NULL, 'BRONZE', 'PLANET', 2, '변수와 자료형', 'Java Bronze 학습 과정: 변수와 자료형', 2, 'planet 1 complete', 1),
  (3, @java_subject_id, NULL, 'BRONZE', 'PLANET', 3, '연산자', 'Java Bronze 학습 과정: 연산자', 3, 'planet 2 complete', 1),
  (4, @java_subject_id, NULL, 'BRONZE', 'PLANET', 4, '조건문과 반복문', 'Java Bronze 학습 과정: 조건문과 반복문', 4, 'planet 3 complete', 1),
  (5, @java_subject_id, NULL, 'BRONZE', 'PLANET', 5, '배열과 문자열', 'Java Bronze 학습 과정: 배열과 문자열', 5, 'planet 4 complete', 1),
  (11, @java_subject_id, NULL, 'SILVER', 'PLANET', 1, '메서드와 프로그램 분리', 'Java Silver 학습 과정: 메서드와 프로그램 분리', 1, 'previous level complete', 1),
  (12, @java_subject_id, NULL, 'SILVER', 'PLANET', 2, '객체지향과 클래스', 'Java Silver 학습 과정: 객체지향과 클래스', 2, 'planet 1 complete', 1),
  (13, @java_subject_id, NULL, 'SILVER', 'PLANET', 3, '상속과 다형성', 'Java Silver 학습 과정: 상속과 다형성', 3, 'planet 2 complete', 1),
  (14, @java_subject_id, NULL, 'SILVER', 'PLANET', 4, '인터페이스와 추상화', 'Java Silver 학습 과정: 인터페이스와 추상화', 4, 'planet 3 complete', 1),
  (15, @java_subject_id, NULL, 'SILVER', 'PLANET', 5, '예외 처리와 파일 입출력', 'Java Silver 학습 과정: 예외 처리와 파일 입출력', 5, 'planet 4 complete', 1),
  (21, @java_subject_id, NULL, 'GOLD', 'PLANET', 1, '컬렉션 프레임워크', 'Java Gold 학습 과정: 컬렉션 프레임워크', 1, 'previous level complete', 1),
  (22, @java_subject_id, NULL, 'GOLD', 'PLANET', 2, '제네릭과 람다', 'Java Gold 학습 과정: 제네릭과 람다', 2, 'planet 1 complete', 1),
  (23, @java_subject_id, NULL, 'GOLD', 'PLANET', 3, 'Stream과 데이터 처리', 'Java Gold 학습 과정: Stream과 데이터 처리', 3, 'planet 2 complete', 1),
  (24, @java_subject_id, NULL, 'GOLD', 'PLANET', 4, '날짜·문자열·유틸리티', 'Java Gold 학습 과정: 날짜·문자열·유틸리티', 4, 'planet 3 complete', 1),
  (25, @java_subject_id, NULL, 'GOLD', 'PLANET', 5, '종합 응용 프로젝트', 'Java Gold 학습 과정: 종합 응용 프로젝트', 5, 'planet 4 complete', 1)
ON DUPLICATE KEY UPDATE subject_id = VALUES(subject_id), level_code = VALUES(level_code), planet_no = VALUES(planet_no), title = VALUES(title), description = VALUES(description), sort_order = VALUES(sort_order), gate_condition = VALUES(gate_condition), is_active = VALUES(is_active), updated_at = CURRENT_TIMESTAMP;

INSERT INTO lessons (lesson_id, node_id, title, summary, content, example_code, sort_order, is_active, required_for_completion, created_by)
VALUES
  (101, 1, 'Java와 JVM', 'Java는 객체지향 언어이며 웹·서버·앱 등 여러 환경에서 사용됩니다. Java 프로그램은 사람이 읽을 수 있는 소스 코드로 작성하지만, 운영체제가 이 소스를 직접 실행하지는 않습니다. 먼저 Java 컴파일러가 소스 코드를 JVM이 이해할 수 있는 바이트코드로 바꿉니다.', 'Java는 객체지향 언어이며 웹·서버·앱 등 여러 환경에서 사용됩니다. Java 프로그램은 사람이 읽을 수 있는 소스 코드로 작성하지만, 운영체제가 이 소스를 직접 실행하지는 않습니다. 먼저 Java 컴파일러가 소스 코드를 JVM이 이해할 수 있는 바이트코드로 바꿉니다.

JVM(Java Virtual Machine)은 바이트코드를 실제 컴퓨터에서 실행하는 가상 실행 환경입니다. Windows·macOS·Linux는 서로 다르지만, 각 운영체제에 맞는 JVM이 설치되어 있으면 같은 바이트코드를 실행할 수 있습니다. 이것이 Java가 여러 운영체제에서 널리 사용되는 핵심 이유입니다.

실행 흐름은 **소스 코드 작성 → 컴파일 → 바이트코드 생성 → JVM 실행**입니다. 소스 파일은 `.java`, 컴파일 결과는 `.class` 확장자를 사용합니다. 컴파일 단계에서 문법 오류를 먼저 발견하고, 실행 단계에서 프로그램의 실제 동작을 확인합니다.
```java
public class Hello { public static void main(String[] args) { System.out.println("Hello"); } }
```

```console
Hello
```


```text
Hello.java  -- javac -->  Hello.class  -- JVM -->  Hello 출력
```

이 흐름에서 `Hello.java`는 작성한 소스 파일, `Hello.class`는 컴파일된 바이트코드 파일이며, 마지막 단계에서 JVM이 main 메서드를 실행해 `Hello`를 출력합니다.

JVM은 소스 코드를 작성하는 도구가 아니라 바이트코드를 실행하는 환경입니다. `.java` 파일을 바로 `java` 명령으로 실행하는 것이 아니라, 일반적인 흐름에서는 먼저 컴파일해야 합니다.

Java는 사람이 작성한 소스 코드를 바로 운영체제가 실행하지 않습니다. 컴파일러가 공통 형식의 바이트코드로 바꾸고, 각 환경에 맞는 JVM이 이를 실행합니다. 따라서 ‘Write Once, Run Anywhere’는 **JVM이 준비된 환경에서** 성립합니다.

JVM은 메모리 관리와 바이트코드 검증도 담당합니다. 개발자는 운영체제별 실행 파일을 각각 만들기보다 하나의 바이트코드를 배포할 수 있으며, 사용자는 자신의 환경에 맞는 JVM을 통해 프로그램을 실행합니다.', 'public class Hello { public static void main(String[] args) { System.out.println("Hello"); } }', 1, 1, 1, NULL),
  (102, 1, '개발 환경', 'Java 개발 환경은 프로젝트 폴더, 소스 파일, JDK, 실행 설정으로 구성됩니다. 프로젝트 안에서는 보통 `src` 아래에 Java 소스 파일을 두고, JDK의 도구로 컴파일·실행합니다.', 'Java 개발 환경은 프로젝트 폴더, 소스 파일, JDK, 실행 설정으로 구성됩니다. 프로젝트 안에서는 보통 `src` 아래에 Java 소스 파일을 두고, JDK의 도구로 컴파일·실행합니다.
```text
프로젝트 └─ src └─ Hello.java
```


JDK는 개발자가 코드를 만들기 위한 묶음이고, JRE는 만들어진 프로그램을 실행하기 위한 묶음입니다. JVM은 JRE 안에서 바이트코드를 실제 실행하는 핵심 구성 요소입니다. 개발 환경 설치 여부와 실행 환경 설치 여부를 구분해 판단합니다.

프로젝트 폴더에는 소스 코드뿐 아니라 컴파일 결과와 설정 파일도 구분해 보관합니다. IDE를 사용하면 작성·컴파일·실행을 버튼으로 처리할 수 있지만, 내부에서는 JDK 명령이 같은 순서로 동작합니다.', NULL, 2, 1, 1, NULL),
  (103, 1, '클래스와 소스 파일', '클래스는 데이터와 기능을 담는 Java 코드의 기본 단위입니다. 클래스 이름은 대문자로 시작하는 PascalCase를 권장합니다. public 최상위 클래스가 있으면 파일 이름은 클래스 이름과 같아야 합니다.', '클래스는 데이터와 기능을 담는 Java 코드의 기본 단위입니다. 클래스 이름은 대문자로 시작하는 PascalCase를 권장합니다. public 최상위 클래스가 있으면 파일 이름은 클래스 이름과 같아야 합니다.
```java
public class Welcome { }
```


클래스 이름은 보통 대문자로 시작하는 PascalCase를 사용합니다. 한 파일에 여러 클래스를 둘 수는 있지만 public 최상위 클래스는 하나만 둘 수 있으며, 파일 이름은 그 public 클래스 이름과 일치해야 합니다.

클래스 본문에는 변수 역할을 하는 필드와 기능을 수행하는 메서드를 작성합니다. 처음에는 클래스 하나에 main 메서드를 두고 시작하지만, 프로그램이 커지면 역할에 따라 여러 클래스로 나누게 됩니다.', 'public class Welcome { }', 3, 1, 1, NULL),
  (104, 1, 'main 메서드', '콘솔 Java 프로그램은 main 메서드에서 시작합니다. `public`은 외부 실행기의 접근을, `static`은 객체 없이 시작할 수 있음을, `void`는 반환값이 없음을 뜻합니다. `args`는 실행 명령에서 전달한 문자열 값입니다.', '콘솔 Java 프로그램은 main 메서드에서 시작합니다. `public`은 외부 실행기의 접근을, `static`은 객체 없이 시작할 수 있음을, `void`는 반환값이 없음을 뜻합니다. `args`는 실행 명령에서 전달한 문자열 값입니다.
```java
public static void main(String[] args) { System.out.println(args.length); }
```


`main`의 `String[] args`는 실행할 때 전달한 문자열 인자를 받는 배열입니다. 처음에는 인자를 사용하지 않아도 괜찮지만, 메서드의 형태를 임의로 바꾸면 Java 실행기가 시작점을 찾지 못합니다.

main 메서드는 프로그램 전체에서 반드시 하나만 존재해야 하는 것은 아니지만, 실행 대상으로 선택한 클래스에는 올바른 main이 있어야 합니다. args에 저장된 값은 모두 문자열이므로 숫자로 쓰려면 변환이 필요합니다.', 'public static void main(String[] args) { System.out.println(args.length); }', 4, 1, 1, NULL),
  (105, 1, '실행문과 세미콜론', '변수 선언·대입·메서드 호출 같은 실행문 끝에는 세미콜론을 씁니다. 클래스·메서드·if처럼 중괄호로 끝나는 블록 선언 뒤에는 세미콜론을 붙이지 않습니다. 누락 오류는 다음 줄에서 발견되는 것처럼 보일 수 있습니다.', '변수 선언·대입·메서드 호출 같은 실행문 끝에는 세미콜론을 씁니다. 클래스·메서드·if처럼 중괄호로 끝나는 블록 선언 뒤에는 세미콜론을 붙이지 않습니다. 누락 오류는 다음 줄에서 발견되는 것처럼 보일 수 있습니다.
```java
int score = 70;
System.out.println(score);
```

```console
70
```


세미콜론은 한 실행문이 끝났음을 나타냅니다. 변수 선언, 대입, 메서드 호출에는 필요하지만 if·for·메서드 선언 뒤의 중괄호 블록에는 쓰지 않습니다. 오류 메시지가 세미콜론 누락 줄보다 다음 줄에 표시될 수도 있습니다.

여러 실행문을 같은 줄에 쓸 수도 있지만 한 줄에 하나씩 작성하면 오류 위치를 찾기 쉽습니다. 세미콜론은 문장의 끝을 표시하므로 문자열 안의 세미콜론과 실제 문장 종료 기호를 구분해야 합니다.', 'int score = 70;
System.out.println(score);', 5, 1, 1, NULL),
  (106, 1, '콘솔 출력', '`print`는 줄바꿈 없이, `println`은 줄바꿈 후 출력합니다. `printf`는 `%s`, `%d`, `%.2f`처럼 형식을 지정합니다. 출력은 코드 결과 확인과 디버깅의 가장 기본적인 방법입니다.', '`print`는 줄바꿈 없이, `println`은 줄바꿈 후 출력합니다. `printf`는 `%s`, `%d`, `%.2f`처럼 형식을 지정합니다. 출력은 코드 결과 확인과 디버깅의 가장 기본적인 방법입니다.
```java
System.out.print("Java ");
System.out.println("Bronze");
System.out.printf("점수: %d%n", 80);
```

```console
Java Bronze
점수: 80
```


`print`와 `println`의 차이는 줄바꿈 여부입니다. `printf`는 `%d`, `%s`, `%.2f`처럼 형식을 지정해 값을 출력할 수 있습니다. 학습 중에는 값 변화와 반복 결과를 확인하는 디버깅 도구로도 사용합니다.

printf의 서식과 전달값 개수가 맞지 않으면 원하는 결과를 얻기 어렵습니다. `%n`은 운영체제에 맞는 줄바꿈을 넣고, `%.2f`는 실수를 소수 둘째 자리까지 표시하는 등 출력 모양을 제어합니다.', 'System.out.print("Java ");
System.out.println("Bronze");
System.out.printf("점수: %d%n", 80);', 6, 1, 1, NULL),
  (107, 1, '주석', '`//` 뒤는 한 줄 주석, `/* ... */`는 여러 줄 주석입니다. 주석은 실행되지 않으며 코드의 이유·제약을 설명할 때 씁니다. 코드와 달라진 주석은 버그를 만들 수 있으므로 실제 의미를 유지합니다.', '`//` 뒤는 한 줄 주석, `/* ... */`는 여러 줄 주석입니다. 주석은 실행되지 않으며 코드의 이유·제약을 설명할 때 씁니다. 코드와 달라진 주석은 버그를 만들 수 있으므로 실제 의미를 유지합니다.
```java
// 임시 점수 확인
System.out.println(80); // 화면에는 점수만 출력
```

```console
80
```


주석은 컴파일러가 실행 코드로 처리하지 않습니다. 임시로 한 줄을 실행에서 제외하거나, 왜 특정 조건이 필요한지 설명할 때 유용합니다. 그러나 오래된 주석이 실제 코드와 달라지면 오히려 혼란을 줍니다.

좋은 주석은 코드가 무엇을 하는지 반복하기보다 왜 이 처리가 필요한지를 알려 줍니다. 학습 중에는 특정 문장을 주석 처리한 전후의 출력 결과를 비교하면 실행 흐름을 이해하는 데 도움이 됩니다.', '// 임시 점수 확인
System.out.println(80); // 화면에는 점수만 출력', 7, 1, 1, NULL),
  (108, 1, '코드 들여쓰기', '중괄호는 클래스·메서드·조건문·반복문의 실행 범위를 정합니다. 들여쓰기는 실행 규칙은 아니지만 어느 문장이 어떤 블록에 속하는지 보여 줍니다. 한 블록 안에서는 같은 깊이를 같은 들여쓰기로 유지합니다.', '중괄호는 클래스·메서드·조건문·반복문의 실행 범위를 정합니다. 들여쓰기는 실행 규칙은 아니지만 어느 문장이 어떤 블록에 속하는지 보여 줍니다. 한 블록 안에서는 같은 깊이를 같은 들여쓰기로 유지합니다.
```java
if (true) {
    System.out.println("안쪽");
}
System.out.println("바깥");
```

```console
안쪽
바깥
```


블록 안에서는 같은 깊이의 문장을 같은 수준으로 들여씁니다. 조건문 안에 반복문이 들어가면 한 단계 더 들여써야 합니다. 보기 좋은 구조는 중괄호가 짝을 이루는지와 코드가 어느 블록에 속하는지를 쉽게 보여 줍니다.

중첩 블록이 생길 때마다 보통 공백 네 칸을 추가합니다. 닫는 중괄호는 블록을 시작한 문장과 같은 깊이에 두면 if·for·메서드의 범위를 빠르게 구분할 수 있습니다.', 'if (true) {
    System.out.println("안쪽");
}
System.out.println("바깥");', 8, 1, 1, NULL),
  (109, 1, '컴파일과 실행', '`javac Hello.java`는 소스를 컴파일해 `.class` 파일을 만들고, `java Hello`는 클래스를 실행합니다.', '`javac Hello.java`는 소스를 컴파일해 `.class` 파일을 만들고, `java Hello`는 클래스를 실행합니다.
```text
javac Hello.java
java Hello
```


소스가 수정되면 기존 class 파일이 자동으로 바뀌는 것이 아니므로 다시 컴파일해야 합니다. 컴파일에 성공해도 논리적으로 잘못된 출력이 나올 수 있으며, 이 경우 코드를 추적해 원인을 찾아야 합니다.', NULL, 9, 1, 1, NULL),
  (110, 1, '오류 읽기', '문법 오류는 세미콜론 누락·잘못된 변수명처럼 컴파일 전에 발견됩니다. 런타임 오류는 컴파일 뒤 실행 중 발생합니다. 오류 메시지에서는 파일명·줄 번호·오류 종류를 먼저 확인하고 해당 줄과 바로 앞줄을 함께 봅니다.', '문법 오류는 세미콜론 누락·잘못된 변수명처럼 컴파일 전에 발견됩니다. 런타임 오류는 컴파일 뒤 실행 중 발생합니다. 오류 메시지에서는 파일명·줄 번호·오류 종류를 먼저 확인하고 해당 줄과 바로 앞줄을 함께 봅니다.
```java
int score = 80 // 세미콜론 누락: 컴파일 오류
```


컴파일 오류는 실행 전에 발견되는 문법·타입 문제이고, 런타임 오류는 컴파일은 성공했지만 실행 중 발생하는 문제입니다. 패키지가 있는 클래스는 실행할 때 패키지명을 포함한 완전한 클래스 이름을 사용합니다.

첫 번째 오류가 뒤쪽에 여러 오류를 연쇄적으로 만들 수 있으므로 가장 위의 오류부터 해결합니다. 컴파일 오류는 코드를 수정해야 하고, 실행 오류는 어떤 입력과 실행 경로에서 발생했는지도 함께 확인합니다.', 'int score = 80 // 세미콜론 누락: 컴파일 오류', 10, 1, 1, NULL),
  (201, 2, '변수 선언·초기화·재대입', '변수는 값을 저장하는 이름표입니다. `자료형 이름 = 값;`으로 선언·초기화하고, 같은 자료형 값으로 다시 대입할 수 있습니다. 지역 변수는 값을 넣기 전에 읽을 수 없습니다.', '변수는 값을 저장하는 이름표입니다. `자료형 이름 = 값;`으로 선언·초기화하고, 같은 자료형 값으로 다시 대입할 수 있습니다. 지역 변수는 값을 넣기 전에 읽을 수 없습니다.
```java
int score = 80;
score = 90;
```


변수를 사용하기 전에는 반드시 선언해야 하며, 지역 변수는 값을 넣기 전에 읽을 수 없습니다. 같은 변수에 새 값을 대입하면 이전 값은 덮어써집니다. 변수명은 영문자·숫자·밑줄·달러 기호를 쓸 수 있지만 숫자로 시작할 수 없습니다.

변수의 사용 범위는 선언된 블록 안으로 제한됩니다. 같은 범위에서 같은 이름을 다시 선언할 수 없으며, 대입할 때는 변수의 자료형과 값의 자료형이 호환되어야 합니다.', 'int score = 80;
score = 90;', 1, 1, 1, NULL),
  (202, 2, '정수 자료형', '정수는 byte, short, int, long으로 표현합니다. 일반적인 횟수·점수는 int를 쓰고, int 범위를 넘는 큰 수는 long과 `L` 접미사를 사용합니다. 범위를 넘으면 값이 순환할 수 있습니다.', '정수는 byte, short, int, long으로 표현합니다. 일반적인 횟수·점수는 int를 쓰고, int 범위를 넘는 큰 수는 long과 `L` 접미사를 사용합니다. 범위를 넘으면 값이 순환할 수 있습니다.
```java
int count = 10;
long amount = 3_000_000_000L;
```


정수 자료형은 표현 가능한 범위가 정해져 있습니다. 범위를 넘는 값을 int에 넣거나 int 계산 결과가 범위를 넘으면 값이 순환할 수 있습니다. 사람 수나 횟수는 보통 int, 매우 큰 식별값·시간은 long을 고려합니다.

byte와 short는 작은 범위를 표현하지만 산술 연산에서는 대부분 int로 변환됩니다. 숫자 사이의 밑줄은 읽기 편하게 할 뿐 값에는 영향을 주지 않으며 숫자의 시작이나 끝에는 둘 수 없습니다.', 'int count = 10;
long amount = 3_000_000_000L;', 2, 1, 1, NULL),
  (203, 2, '실수 자료형', '소수점 값은 보통 double을 사용합니다. float는 메모리를 더 적게 쓰지만 리터럴 뒤에 `f`가 필요합니다. 실수는 이진 표현 오차가 있을 수 있어 금액의 정확한 비교에는 주의합니다.', '소수점 값은 보통 double을 사용합니다. float는 메모리를 더 적게 쓰지만 리터럴 뒤에 `f`가 필요합니다. 실수는 이진 표현 오차가 있을 수 있어 금액의 정확한 비교에는 주의합니다.
```java
double average = 87.5;
float rate = 0.5f;
```


double은 많은 소수 계산에 편리하지만 0.1처럼 이진수로 정확히 표현되지 않는 값에서는 미세한 오차가 생길 수 있습니다. 금액처럼 정확성이 중요한 값은 상황에 따라 최소 단위 정수나 BigDecimal을 사용합니다.

정수 하나를 실수로 바꾼 뒤 나누면 소수 결과를 얻을 수 있습니다. 계산 중간이 이미 정수 나눗셈으로 끝난 뒤 double에 저장하면 사라진 소수 부분은 되돌아오지 않습니다.', 'double average = 87.5;
float rate = 0.5f;', 3, 1, 1, NULL),
  (204, 2, 'char와 문자 코드', 'char는 문자 하나를 저장하며 내부적으로 문자 코드값을 가집니다. char가 산술 연산에 들어가면 정수처럼 계산될 수 있습니다. 문자열은 char 여러 개를 단순히 합친 자료형이 아닙니다.', 'char는 문자 하나를 저장하며 내부적으로 문자 코드값을 가집니다. char가 산술 연산에 들어가면 정수처럼 계산될 수 있습니다. 문자열은 char 여러 개를 단순히 합친 자료형이 아닙니다.
```java
char letter = ''A'';
System.out.println(letter + 1); // 66
```

```console
66
```


char는 UTF-16 코드 단위 하나를 저장합니다. char를 숫자와 계산하면 문자 코드값으로 변환될 수 있습니다. 예를 들어 `''A'' + 1`은 문자 B가 아니라 정수 66이 됩니다.

문자 코드값을 char로 강제 변환하면 해당 코드에 대응하는 문자를 얻을 수 있습니다. 다만 모든 화면의 글자 하나가 항상 char 하나로 표현되는 것은 아니므로 입문 단계에서는 기본 영문자와 숫자 문자를 중심으로 다룹니다.', 'char letter = ''A'';
System.out.println(letter + 1); // 66', 4, 1, 1, NULL),
  (205, 2, 'boolean과 조건값', 'boolean은 true 또는 false만 저장합니다. 비교 연산 결과를 변수에 저장해 조건을 읽기 좋게 만들 수 있습니다. Java는 숫자 0/1을 boolean으로 자동 변환하지 않습니다.', 'boolean은 true 또는 false만 저장합니다. 비교 연산 결과를 변수에 저장해 조건을 읽기 좋게 만들 수 있습니다. Java는 숫자 0/1을 boolean으로 자동 변환하지 않습니다.
```java
boolean passed = score >= 70;
```


boolean 변수에는 비교식의 결과를 저장할 수 있습니다. 예를 들어 `score >= 70`은 true 또는 false가 됩니다. 조건을 의미 있는 boolean 변수로 분리하면 긴 if문을 읽기 쉬워집니다.

상태를 저장한 boolean 변수는 그대로 if 조건에 사용할 수 있습니다. `if (passed == true)`도 가능하지만 `if (passed)`가 더 간결하며, 부정할 때는 `if (!passed)`로 표현합니다.', 'boolean passed = score >= 70;', 5, 1, 1, NULL),
  (206, 2, 'String과 문자열 연결', 'String은 텍스트를 저장하며 `+`로 다른 값과 연결할 수 있습니다. 문자열 내용 비교에는 `equals()`를 사용합니다. 문자열이 먼저 등장하면 이후 `+`는 계산보다 연결로 처리될 수 있습니다.', 'String은 텍스트를 저장하며 `+`로 다른 값과 연결할 수 있습니다. 문자열 내용 비교에는 `equals()`를 사용합니다. 문자열이 먼저 등장하면 이후 `+`는 계산보다 연결로 처리될 수 있습니다.
```java
System.out.println("합계: " + (10 + 20));
```

```console
합계: 30
```


String은 참조 자료형이지만 Bronze에서는 텍스트 값으로 사용하면 됩니다. 문자열의 내용이 같은지 비교할 때는 `==` 대신 `equals()`를 사용합니다. `+` 연산은 왼쪽부터 수행되므로 숫자 계산에는 괄호를 사용합니다.

String 메서드를 호출한 결과로 새로운 문자열이 만들어질 수 있지만 원래 변수의 값은 자동으로 바뀌지 않습니다. 문자열과 null을 비교할 가능성이 있으면 null 여부를 먼저 확인해야 합니다.', 'System.out.println("합계: " + (10 + 20));', 6, 1, 1, NULL),
  (207, 2, 'final 상수', 'final 변수는 한 번 값이 정해지면 재대입할 수 없습니다. 공통 기준값에는 대문자·밑줄 이름을 사용하면 의미가 분명합니다. 상수는 숫자를 코드 곳곳에 직접 쓰는 문제를 줄입니다.', 'final 변수는 한 번 값이 정해지면 재대입할 수 없습니다. 공통 기준값에는 대문자·밑줄 이름을 사용하면 의미가 분명합니다. 상수는 숫자를 코드 곳곳에 직접 쓰는 문제를 줄입니다.
```java
final int PASS_SCORE = 70;
```


final은 변수가 가리키는 값을 다시 바꾸지 못하게 합니다. 상수는 코드 곳곳에 숫자를 직접 쓰는 대신 의미 있는 이름으로 기준을 관리하게 해 줍니다. 예를 들어 `PASS_SCORE`를 바꾸면 모든 판정에 같은 기준이 적용됩니다.

상수는 프로그램의 규칙을 한곳에서 관리하게 해 줍니다. 합격 점수나 최대 인원처럼 의미 있는 기준을 상수로 두면 숫자의 의미가 분명하고, 기준 변경 시 수정할 위치도 줄어듭니다.', 'final int PASS_SCORE = 70;', 7, 1, 1, NULL),
  (208, 2, '자동 타입 변환', '작은 범위의 값은 큰 범위의 변수에 자동으로 저장할 수 있습니다. 예를 들어 int는 double로 변환됩니다. 값이 넓은 표현 범위로 이동하므로 보통 명시적 캐스팅이 필요 없습니다.', '작은 범위의 값은 큰 범위의 변수에 자동으로 저장할 수 있습니다. 예를 들어 int는 double로 변환됩니다. 값이 넓은 표현 범위로 이동하므로 보통 명시적 캐스팅이 필요 없습니다.
```java
int count = 10;
double result = count;
```


자동 변환은 일반적으로 byte → short → int → long → float → double 방향에서 일어납니다. 단, long에서 float로의 변환은 가능해도 모든 정수 자릿값이 정확히 유지된다는 뜻은 아닙니다.

char도 int 이상의 숫자 타입으로 자동 변환될 수 있습니다. 반대로 long이나 double처럼 넓은 타입의 값을 좁은 타입 변수에 넣을 때는 자동 변환되지 않으므로 컴파일 단계에서 확인됩니다.', 'int count = 10;
double result = count;', 8, 1, 1, NULL),
  (209, 2, '강제 타입 변환', '큰 범위에서 작은 범위로 옮길 때 `(자료형)` 캐스팅을 사용합니다. 실수를 int로 바꾸면 소수점은 버려지고, byte 범위를 벗어나면 다른 값으로 바뀔 수 있습니다.', '큰 범위에서 작은 범위로 옮길 때 `(자료형)` 캐스팅을 사용합니다. 실수를 int로 바꾸면 소수점은 버려지고, byte 범위를 벗어나면 다른 값으로 바뀔 수 있습니다.
```java
int whole = (int) 12.9;
```


강제 변환은 개발자가 값 손실 가능성을 알고 변환하겠다는 표시입니다. 소수점 손실뿐 아니라 byte처럼 범위가 작은 타입에서는 값이 전혀 다른 음수로 바뀔 수 있습니다. 입력값을 변환하기 전 범위를 확인하는 습관이 좋습니다.

캐스팅은 문법을 통과시키는 기능이지 값을 안전하게 보장하는 기능은 아닙니다. 변환 전 값이 대상 자료형의 범위 안에 있는지 확인하고, 소수점 처리가 필요하면 반올림·버림 기준을 먼저 정해야 합니다.', 'int whole = (int) 12.9;', 9, 1, 1, NULL),
  (210, 2, '자료형 선택 종합', '값의 성격에 맞는 자료형을 고르는 것이 중요합니다. 이름은 String, 개수는 int, 평균은 double, 상태는 boolean처럼 모델링합니다. 의미 있는 변수명은 코드와 문제 풀이 모두에서 오류를 줄입니다.', '값의 성격에 맞는 자료형을 고르는 것이 중요합니다. 이름은 String, 개수는 int, 평균은 double, 상태는 boolean처럼 모델링합니다. 의미 있는 변수명은 코드와 문제 풀이 모두에서 오류를 줄입니다.
```java
String userName = "하늘";
boolean isPremium = true;
```


변수명은 동사보다 저장한 대상을 드러내는 명사를 주로 사용합니다. boolean은 `is`, `has`, `can`처럼 상태가 드러나는 이름이 좋습니다. 한 변수에 서로 다른 의미의 값을 재사용하지 않아야 오류를 줄일 수 있습니다.

전화번호나 학번처럼 계산하지 않는 숫자는 String이 더 적절할 수 있습니다. 저장할 값의 범위, 소수 필요 여부, 계산 여부, 참·거짓 여부를 차례로 판단해 자료형을 선택합니다.', 'String userName = "하늘";
boolean isPremium = true;', 10, 1, 1, NULL),
  (301, 3, '대입·복합 대입', '`=`는 오른쪽 계산 결과를 왼쪽 변수에 저장합니다. `+=`, `-=`, `*=`처럼 기존 값에 계산을 더하는 복합 대입도 있습니다. 대입은 비교가 아니라 값 변경입니다.', '`=`는 오른쪽 계산 결과를 왼쪽 변수에 저장합니다. `+=`, `-=`, `*=`처럼 기존 값에 계산을 더하는 복합 대입도 있습니다. 대입은 비교가 아니라 값 변경입니다.
```java
int point = 10;
point += 5;
```


대입식 오른쪽에는 계산식, 메서드 호출, 다른 변수가 올 수 있습니다. `x += 3`은 `x = x + 3`과 비슷한 의미지만, 복합 대입은 왼쪽 변수의 자료형에 맞춰 처리되는 특징이 있습니다.

복합 대입은 현재 값을 읽고 계산한 뒤 다시 같은 변수에 저장합니다. 누적 점수나 수량 변경에 편리하지만, 코드 중간에서 값이 바뀐다는 사실을 놓치지 않도록 실행 순서대로 추적해야 합니다.', 'int point = 10;
point += 5;', 1, 1, 1, NULL),
  (302, 3, '산술·나머지 연산', '`+ - * / %`로 계산합니다. int끼리 나누면 몫만 남고, `%`는 나머지를 구합니다. 0으로 정수를 나누면 오류가 발생합니다.', '`+ - * / %`로 계산합니다. int끼리 나누면 몫만 남고, `%`는 나머지를 구합니다. 0으로 정수를 나누면 오류가 발생합니다.
```java
int quotient = 7 / 2;
int remainder = 7 % 2;
```


정수끼리의 나눗셈은 몫만 남기고, 하나라도 실수면 실수 나눗셈이 됩니다. 0으로 정수를 나누면 ArithmeticException이 발생합니다. 나머지 연산은 나눗셈과 함께 규칙을 만들 때 중요합니다.

연산 결과의 자료형은 피연산자의 자료형에 영향을 받습니다. 나눗셈 전에 어느 값이 int이고 어느 값이 double인지 확인하고, 나머지는 홀짝뿐 아니라 자릿수 분리와 순환 규칙에도 사용할 수 있습니다.', 'int quotient = 7 / 2;
int remainder = 7 % 2;', 2, 1, 1, NULL),
  (303, 3, '전위·후위 증감', '`++x`는 먼저 증가하고 식에 사용하며, `x++`는 현재 값을 사용한 뒤 증가합니다. 반복문에서는 증감식으로 자주 쓰고, 복잡한 한 줄 계산에는 섞지 않는 것이 좋습니다.', '`++x`는 먼저 증가하고 식에 사용하며, `x++`는 현재 값을 사용한 뒤 증가합니다. 반복문에서는 증감식으로 자주 쓰고, 복잡한 한 줄 계산에는 섞지 않는 것이 좋습니다.
```java
int x = 5;
int y = x++;
```


전위 증감은 값을 바꾼 후 식에 사용하고, 후위 증감은 현재 값을 식에 사용한 후 바꿉니다. 한 줄에 여러 증감 연산을 섞으면 이해와 유지보수가 어려우므로 반복문 증감식 외에는 단순한 대입으로 쓰는 것도 좋습니다.

증감 연산자가 단독 문장으로 사용되면 전위와 후위의 최종 결과는 같습니다. 다른 대입이나 출력과 한 식에 섞였을 때만 사용 시점 차이가 드러나므로 변수값을 단계별로 기록해야 합니다.', 'int x = 5;
int y = x++;', 3, 1, 1, NULL),
  (304, 3, '비교 연산자', '비교 연산자는 boolean 결과를 만듭니다. `>`, `<`, `>=`, `<=`, `==`, `!=`를 사용하며, 범위는 두 비교식을 논리 연산자로 연결합니다.', '비교 연산자는 boolean 결과를 만듭니다. `>`, `<`, `>=`, `<=`, `==`, `!=`를 사용하며, 범위는 두 비교식을 논리 연산자로 연결합니다.
```java
boolean valid = score >= 0 && score <= 100;
```


비교 연산자는 항상 boolean을 반환합니다. 숫자는 크기 비교가 가능하고, char도 코드값 기준으로 비교할 수 있습니다. 문자열은 참조 비교인 `==` 대신 `equals()`를 사용해야 내용 비교가 됩니다.

경계값이 포함되는 조건에는 `>=` 또는 `<=`를 사용합니다. 두 실수의 정확한 같음 비교는 표현 오차 때문에 주의해야 하며, 문자열 내용 비교에는 equals 메서드를 사용합니다.', 'boolean valid = score >= 0 && score <= 100;', 4, 1, 1, NULL),
  (305, 3, '논리·단락 평가', '`&&`는 둘 다 참일 때, `||`는 하나라도 참일 때 참입니다. `&&` 왼쪽이 false거나 `||` 왼쪽이 true면 오른쪽을 계산하지 않는 단락 평가가 일어납니다.', '`&&`는 둘 다 참일 때, `||`는 하나라도 참일 때 참입니다. `&&` 왼쪽이 false거나 `||` 왼쪽이 true면 오른쪽을 계산하지 않는 단락 평가가 일어납니다.
```java
boolean usable = name != null && name.length() > 0;
```


`&&`와 `||`는 단락 평가를 합니다. `&&`의 왼쪽이 false면 오른쪽을 계산하지 않고, `||`의 왼쪽이 true면 오른쪽을 계산하지 않습니다. null 검사 뒤 메서드를 호출할 때 이 성질이 안전장치가 됩니다.

복잡한 조건은 괄호로 묶어 의미를 드러냅니다. 회원이면서 결제를 완료한 경우처럼 동시에 필요한 조건은 &&, 관리자이거나 작성자인 경우처럼 하나만 만족해도 되는 조건은 ||가 알맞습니다.', 'boolean usable = name != null && name.length() > 0;', 5, 1, 1, NULL),
  (306, 3, '문자열 연결 순서', '문자열이 포함되면 `+`는 왼쪽부터 텍스트 연결로 처리됩니다. 숫자 합계를 문장에 넣을 때는 계산 부분을 괄호로 묶습니다.', '문자열이 포함되면 `+`는 왼쪽부터 텍스트 연결로 처리됩니다. 숫자 합계를 문장에 넣을 때는 계산 부분을 괄호로 묶습니다.
```java
System.out.println("결과: " + 2 + 3);
System.out.println("결과: " + (2 + 3));
```

```console
결과: 23
결과: 5
```


문자열이 하나라도 포함된 `+` 식은 왼쪽부터 문자열 연결로 처리됩니다. 계산 결과를 문장에 넣을 때는 `(a + b)`처럼 숫자 계산 부분을 괄호로 묶으면 의도가 명확합니다.

괄호는 숫자 계산을 문자열 연결보다 먼저 수행하게 합니다. 여러 값을 출력할 때 중간 공백이나 구분 기호도 문자열로 직접 넣어야 하며, 출력 결과를 글자 단위로 예상해 보는 것이 좋습니다.', 'System.out.println("결과: " + 2 + 3);
System.out.println("결과: " + (2 + 3));', 6, 1, 1, NULL),
  (307, 3, '우선순위와 괄호', '곱셈·나눗셈이 덧셈·뺄셈보다 먼저 계산되고, 비교 결과 뒤에 논리 연산이 적용됩니다. 모든 우선순위를 외우기보다 의도가 불분명한 식에 괄호를 씁니다.', '곱셈·나눗셈이 덧셈·뺄셈보다 먼저 계산되고, 비교 결과 뒤에 논리 연산이 적용됩니다. 모든 우선순위를 외우기보다 의도가 불분명한 식에 괄호를 씁니다.
```java
int price = (basePrice - discount) * quantity;
```


산술, 비교, 논리, 대입 연산은 각각 우선순위가 다릅니다. 모든 우선순위를 외우기보다, 혼동될 수 있는 식에 괄호를 쓰는 습관이 더 중요합니다. 조건식은 계산 결과를 별도 변수로 나누면 검증하기 쉽습니다.

같은 우선순위의 연산자는 일반적으로 왼쪽부터 계산하지만 대입처럼 방향이 다른 경우도 있습니다. 실무 코드에서는 우선순위 지식에만 의존하지 않고 괄호와 중간 변수를 사용해 의도를 명확히 합니다.', 'int price = (basePrice - discount) * quantity;', 7, 1, 1, NULL),
  (308, 3, '삼항 연산자', '`조건 ? 값1 : 값2`는 간단한 선택 결과를 변수에 저장할 때 사용합니다. 여러 문장을 실행하거나 조건이 복잡하면 if-else가 더 읽기 좋습니다.', '`조건 ? 값1 : 값2`는 간단한 선택 결과를 변수에 저장할 때 사용합니다. 여러 문장을 실행하거나 조건이 복잡하면 if-else가 더 읽기 좋습니다.
```java
String result = score >= 70 ? "통과" : "재도전";
```


삼항 연산자는 값 하나를 선택해 변수에 넣거나 출력할 때 적합합니다. 두 갈래 안에서 여러 문장을 실행해야 한다면 if-else를 사용합니다. 참·거짓 위치를 바꾸면 결과도 반대가 됩니다.

삼항 연산자의 두 결과 값은 서로 호환되는 자료형이어야 합니다. 결과를 바로 출력할 수도 있고 변수에 저장할 수도 있으며, 단순한 상태 문구나 최솟값 선택에 적합합니다.', 'String result = score >= 70 ? "통과" : "재도전";', 8, 1, 1, NULL),
  (309, 3, '나머지 활용', '나머지는 홀짝, 배수, 일정 주기 판단에 사용합니다. 예를 들어 3의 배수는 `number % 3 == 0`으로 판별합니다. 반복 번호에 따라 다른 작업을 하게 만들 수도 있습니다.', '나머지는 홀짝, 배수, 일정 주기 판단에 사용합니다. 예를 들어 3의 배수는 `number % 3 == 0`으로 판별합니다. 반복 번호에 따라 다른 작업을 하게 만들 수도 있습니다.
```java
boolean even = number % 2 == 0;
```


나머지는 홀짝뿐 아니라 N번째마다 실행할 작업, 자릿수 분리, 주기적 이벤트에 쓸 수 있습니다. 예를 들어 반복 번호가 3으로 나누어떨어질 때만 메시지를 출력할 수 있습니다.

10으로 나눈 나머지는 정수의 마지막 자릿수를 구하는 데 사용할 수 있습니다. 반복 인덱스를 일정 열 수로 나눈 나머지를 이용하면 표의 줄바꿈 위치처럼 주기적인 처리를 만들 수 있습니다.', 'boolean even = number % 2 == 0;', 9, 1, 1, NULL),
  (310, 3, '연산자 종합', '실전 코드는 계산, 조건 판정, 메시지 생성을 함께 사용합니다. 한 식에 모두 넣기보다 계산값과 조건값을 변수로 분리하면 오류를 찾기 쉽습니다.', '실전 코드는 계산, 조건 판정, 메시지 생성을 함께 사용합니다. 한 식에 모두 넣기보다 계산값과 조건값을 변수로 분리하면 오류를 찾기 쉽습니다.
```java
int score = 76;
boolean passed = score >= 70;
System.out.println(passed ? "통과" : "재도전");
```

```console
통과
```


문제를 풀 때는 값을 계산하는 부분, 조건을 판단하는 부분, 결과를 만드는 부분을 분리해 읽습니다. 코드 입력 문제에서는 먼저 필요한 변수와 자료형을 정하고, 계산식을 검증한 뒤 조건문을 붙입니다.

종합식은 먼저 괄호 안 계산, 산술 계산, 비교, 논리 판단 순서로 나누어 읽습니다. 할인 금액처럼 계산 순서가 결과에 영향을 주는 문제에서는 정수 나눗셈과 자료형 변환도 함께 확인합니다.', 'int score = 76;
boolean passed = score >= 70;
System.out.println(passed ? "통과" : "재도전");', 10, 1, 1, NULL),
  (401, 4, 'if문', 'if문은 조건이 true일 때만 블록을 실행합니다. 조건에는 boolean 값이나 비교식이 와야 하며, 한 줄이어도 중괄호를 쓰는 편이 안전합니다.', 'if문은 조건이 true일 때만 블록을 실행합니다. 조건에는 boolean 값이나 비교식이 와야 하며, 한 줄이어도 중괄호를 쓰는 편이 안전합니다.
```java
if (score >= 70) { System.out.println("통과"); }
```


if문 조건은 true일 때만 본문이 실행됩니다. 조건문을 작성하기 전 ‘언제 실행해야 하는가’를 문장으로 먼저 정하면 비교 연산자를 올바르게 고르기 쉽습니다. 중괄호는 본문이 한 줄이어도 쓰는 편이 안전합니다.

서로 독립적인 조건을 각각 검사하려면 if문을 여러 개 사용합니다. 앞 조건이 참일 때 뒤 조건을 검사하지 않으려면 else-if 구조가 필요하므로 요구사항의 조건 관계를 먼저 판단합니다.', 'if (score >= 70) { System.out.println("통과"); }', 1, 1, 1, NULL),
  (402, 4, 'if-else문', 'if-else는 두 경우 중 하나를 반드시 처리합니다. 로그인 여부나 합격 여부처럼 서로 배타적인 상황에 알맞습니다. 공통 코드는 조건문 밖에 두어 중복을 줄입니다.', 'if-else는 두 경우 중 하나를 반드시 처리합니다. 로그인 여부나 합격 여부처럼 서로 배타적인 상황에 알맞습니다. 공통 코드는 조건문 밖에 두어 중복을 줄입니다.
```java
if (loggedIn) { System.out.println("환영"); } else { System.out.println("로그인 필요"); }
```


if-else는 두 경우가 서로 배타적일 때 사용합니다. 두 블록에서 공통으로 실행할 코드는 조건문 밖에 두면 중복이 줄어듭니다. 사용자 상태에 따라 다른 안내를 보여 주는 기능에 적합합니다.

두 블록에서 만들어진 변수를 바깥에서도 사용하려면 조건문 전에 변수를 선언하고 각 블록에서 값을 대입합니다. 조건 경계에서 어느 블록이 선택되는지 직접 값을 넣어 확인합니다.', 'if (loggedIn) { System.out.println("환영"); } else { System.out.println("로그인 필요"); }', 2, 1, 1, NULL),
  (403, 4, 'else-if문', '여러 구간을 판단할 때 else-if를 사용합니다. 위에서부터 한 번만 검사하므로 높은 기준부터 작성해야 합니다. 마지막 else는 나머지 모든 경우를 처리합니다.', '여러 구간을 판단할 때 else-if를 사용합니다. 위에서부터 한 번만 검사하므로 높은 기준부터 작성해야 합니다. 마지막 else는 나머지 모든 경우를 처리합니다.
```java
if (score >= 90) { grade = "A"; } else if (score >= 80) { grade = "B"; } else { grade = "C"; }
```


else-if 체인은 위에서 아래로 한 번만 검사합니다. `score >= 70`을 먼저 두면 90점도 그 조건에서 걸리므로, 높은 등급 기준부터 작성해야 합니다. 마지막 else는 예상하지 못한 값을 처리하는 기본값 역할도 합니다.

각 조건이 겹치지 않도록 범위를 설계하면 순서 때문에 생기는 오류를 줄일 수 있습니다. 마지막 else는 앞 조건에 포함되지 않은 값 전체를 처리하므로 잘못된 입력 처리에도 활용됩니다.', 'if (score >= 90) { grade = "A"; } else if (score >= 80) { grade = "B"; } else { grade = "C"; }', 3, 1, 1, NULL),
  (404, 4, 'switch문', 'switch문은 하나의 값이 여러 case 중 무엇과 같은지 비교합니다. 화살표 문법은 break 누락 문제를 줄입니다. 메뉴 번호·요일처럼 정해진 값 분기에 적합합니다.', 'switch문은 하나의 값이 여러 case 중 무엇과 같은지 비교합니다. 화살표 문법은 break 누락 문제를 줄입니다. 메뉴 번호·요일처럼 정해진 값 분기에 적합합니다.
```java
switch (menu) { case 1 -> System.out.println("조회"); default -> System.out.println("오류"); }
```


switch문은 하나의 표현식 결과를 case 값과 비교합니다. 화살표 문법(`->`)은 break 없이 해당 문장만 실행해 초보자에게 안전합니다. 전통적인 콜론 문법에서는 break 누락 시 다음 case까지 실행될 수 있습니다.

전통적인 콜론 문법에서는 break가 없으면 다음 case까지 이어서 실행됩니다. 화살표 문법은 한 case의 실행 범위를 명확히 하며, 여러 case가 같은 결과일 때 값을 묶어 작성할 수도 있습니다.', 'switch (menu) { case 1 -> System.out.println("조회"); default -> System.out.println("오류"); }', 4, 1, 1, NULL),
  (405, 4, 'for문', 'for문은 초기식·조건식·증감식으로 반복 횟수를 관리합니다. 시작값, 끝 조건, 변화량을 분리해 읽으면 출력 횟수를 계산할 수 있습니다.', 'for문은 초기식·조건식·증감식으로 반복 횟수를 관리합니다. 시작값, 끝 조건, 변화량을 분리해 읽으면 출력 횟수를 계산할 수 있습니다.
```java
for (int i = 1; i <= 3; i++) { System.out.println(i); }
```

```console
1
2
3
```


for문의 초기식은 보통 반복 횟수 변수 선언, 조건식은 계속 여부, 증감식은 다음 반복 준비입니다. 시작값·끝값·증감 방향을 각각 확인하면 반복 횟수와 출력 범위를 쉽게 계산할 수 있습니다.

반복 변수는 반복문 안에서만 쓰는 지역 변수로 선언하는 경우가 많습니다. 1씩 증가하는 반복뿐 아니라 2씩 증가하거나 감소하는 반복도 가능하며, 조건이 계속 참이면 무한 반복이 됩니다.', 'for (int i = 1; i <= 3; i++) { System.out.println(i); }', 5, 1, 1, NULL),
  (406, 4, 'while문', 'while문은 조건을 먼저 검사하므로 조건이 false면 본문을 실행하지 않습니다. 종료 조건에 쓰이는 값은 반복 안에서 반드시 바뀌어야 합니다.', 'while문은 조건을 먼저 검사하므로 조건이 false면 본문을 실행하지 않습니다. 종료 조건에 쓰이는 값은 반복 안에서 반드시 바뀌어야 합니다.
```java
int count = 1;
while (count <= 3) { System.out.println(count); count++; }
```

```console
1
2
3
```


while문은 반복 전 조건을 검사하므로 조건이 처음부터 false면 한 번도 실행하지 않습니다. 입력을 받을 때 종료 명령이 들어올 때까지 반복하는 구조에 적합합니다. 종료 조건을 코드에서 눈에 띄게 관리합니다.

반복 전에 필요한 초기값을 준비하고, 본문에서 상태를 변경한 뒤 조건을 다시 검사합니다. 입력값에 따라 종료되는 반복은 종료값과 정상 처리값을 명확하게 나누어 설계합니다.', 'int count = 1;
while (count <= 3) { System.out.println(count); count++; }', 6, 1, 1, NULL),
  (407, 4, 'do-while문', 'do-while문은 본문을 한 번 실행한 뒤 조건을 검사합니다. 사용자가 최소 한 번 봐야 하는 메뉴나 입력 화면에 적합합니다. while 끝의 세미콜론을 빼뜨리지 않습니다.', 'do-while문은 본문을 한 번 실행한 뒤 조건을 검사합니다. 사용자가 최소 한 번 봐야 하는 메뉴나 입력 화면에 적합합니다. while 끝의 세미콜론을 빼뜨리지 않습니다.
```java
do { System.out.println("메뉴"); } while (false);
```

```console
메뉴
```


do-while문은 사용자가 최소 한 번 수행해야 하는 작업에 적합합니다. 본문 끝에 세미콜론이 필요하다는 문법 특징이 있습니다. 조건은 마지막에 검사하므로 일반 while과 실행 횟수가 달라질 수 있습니다.

본문을 먼저 실행하는 특성 때문에 사용자 입력을 받고 그 입력을 검사하는 흐름에 적합합니다. 일반 while문으로 바꿀 때는 첫 실행을 별도로 작성해야 같은 동작이 됩니다.', 'do { System.out.println("메뉴"); } while (false);', 7, 1, 1, NULL),
  (408, 4, 'break와 continue', 'break는 가장 가까운 반복문 또는 switch문을 즉시 끝냅니다. continue는 현재 반복의 남은 문장을 건너뛰고 다음 반복으로 이동합니다. 검색 완료에는 break, 제외할 값 처리에는 continue를 사용합니다.', 'break는 가장 가까운 반복문 또는 switch문을 즉시 끝냅니다. continue는 현재 반복의 남은 문장을 건너뛰고 다음 반복으로 이동합니다. 검색 완료에는 break, 제외할 값 처리에는 continue를 사용합니다.
```java
if (number == target) { break; }
```


break는 검색 대상에서 원하는 값을 발견했을 때 불필요한 반복을 멈추게 합니다. 중첩 반복문 안의 break는 가장 안쪽 반복문만 끝냅니다. 바깥 반복문까지 끝내려면 구조를 나누거나 별도 방법을 고려해야 합니다.

continue는 현재 차례만 건너뛰므로 반복문 자체는 계속됩니다. 홀수만 출력하거나 잘못된 데이터를 제외할 때 적합합니다. for문에서는 continue 뒤에도 증감식이 실행됩니다.

중첩 반복에서 두 문장은 가장 가까운 반복문에만 영향을 줍니다. continue 이후의 코드는 실행되지 않으므로 누적이나 증감 문장의 위치에 따라 결과와 종료 여부가 달라질 수 있습니다.', 'if (number == target) { break; }', 8, 1, 1, NULL),
  (409, 4, '중첩 제어문', '조건문 안에 조건문을 넣거나 반복문 안에 반복문을 넣는 것을 중첩 제어문이라고 합니다. 바깥 반복이 한 번 돌 때 안쪽 반복이 모두 실행되는 순서를 표로 추적하면 이해하기 쉽습니다.', '조건문 안에 조건문을 넣거나 반복문 안에 반복문을 넣는 것을 중첩 제어문이라고 합니다. 바깥 반복이 한 번 돌 때 안쪽 반복이 모두 실행되는 순서를 표로 추적하면 이해하기 쉽습니다.
```java
for (int row = 1; row <= 2; row++) {
    for (int col = 1; col <= 3; col++) { System.out.println(row + "," + col); }
}
```

```console
1,1
1,2
1,3
2,1
2,2
2,3
```


바깥 반복 횟수와 안쪽 반복 횟수를 곱하면 전체 실행 횟수를 구할 수 있습니다. 중첩이 깊어질수록 변수 이름을 row·col처럼 역할에 맞게 지어야 흐름을 구분하기 쉽습니다.', 'for (int row = 1; row <= 2; row++) {
    for (int col = 1; col <= 3; col++) { System.out.println(row + "," + col); }
}', 9, 1, 1, NULL),
  (410, 4, '제어문 종합', '제어문 문제는 반복마다 변수값을 표로 추적하면 정확합니다. 입력의 정상값·경계값·잘못된 값을 나눠 생각하고, 종료 조건이 분명한지 확인합니다.', '제어문 문제는 반복마다 변수값을 표로 추적하면 정확합니다. 입력의 정상값·경계값·잘못된 값을 나눠 생각하고, 종료 조건이 분명한지 확인합니다.
```java
for (int score : scores) { if (score >= 70) { System.out.println(score); } }
```


제어문 종합 문제에서는 입력값별 흐름을 표로 추적하면 좋습니다. 반복마다 i, 누적값, 조건 결과가 어떻게 바뀌는지 적어 보면 코드 예측 오류가 줄어듭니다. 먼저 정상·경계·예외 입력을 나눠 생각합니다.

메뉴 프로그램은 반복문으로 메뉴를 계속 보여 주고 조건문이나 switch로 선택을 처리합니다. 종료 메뉴에서는 반복을 끝내고, 잘못된 값에는 안내 메시지를 출력하도록 흐름을 나눕니다.', 'for (int score : scores) { if (score >= 70) { System.out.println(score); } }', 10, 1, 1, NULL),
  (501, 5, '배열의 개념', '배열은 같은 자료형 값을 순서대로 저장하는 구조이며, 배열 변수는 그 묶음을 가리킵니다. 크기는 생성한 뒤 바꿀 수 없습니다. 여러 점수나 이름처럼 같은 종류의 데이터에 사용합니다.', '배열은 같은 자료형 값을 순서대로 저장하는 구조이며, 배열 변수는 그 묶음을 가리킵니다. 크기는 생성한 뒤 바꿀 수 없습니다. 여러 점수나 이름처럼 같은 종류의 데이터에 사용합니다.
```java
int[] scores = {80, 90, 70};
```


배열 변수는 실제 값 묶음 자체가 아니라 배열 객체를 가리킵니다. 배열을 다른 변수에 대입하면 배열을 복사하는 것이 아니라 같은 배열을 함께 가리킵니다. 배열 크기는 생성 후 변경할 수 없습니다.

배열 변수 두 개가 같은 배열을 가리키면 한 변수를 통해 요소를 변경했을 때 다른 변수에서도 변경된 값이 보입니다. 배열 자체를 복사하려면 새 배열을 만들고 요소를 별도로 옮겨야 합니다.', 'int[] scores = {80, 90, 70};', 1, 1, 1, NULL),
  (502, 5, '배열 선언·생성·초기값', '배열은 선언 후 `new 자료형[길이]`로 만들거나 선언과 동시에 값을 넣습니다. int 배열은 0, boolean 배열은 false, 참조 배열은 null로 시작합니다.', '배열은 선언 후 `new 자료형[길이]`로 만들거나 선언과 동시에 값을 넣습니다. int 배열은 0, boolean 배열은 false, 참조 배열은 null로 시작합니다.
```java
int[] numbers = new int[3];
numbers[0] = 10;
```


`{값, 값}` 초기화 문법은 선언과 동시에 쓸 수 있습니다. 이미 선언한 변수에는 `new int[]{...}` 형식을 사용해야 합니다. 배열 종류에 따라 초기값은 정수 0, 실수 0.0, boolean false, 참조 null입니다.

배열 생성 시 길이는 0 이상이어야 하며 변수나 계산식으로 정할 수도 있습니다. 중괄호 초기화는 요소 개수로 길이가 결정되고, 생성 뒤에는 길이를 늘릴 수 없습니다.', 'int[] numbers = new int[3];
numbers[0] = 10;', 2, 1, 1, NULL),
  (503, 5, '인덱스와 예외', '배열 첫 요소의 인덱스는 0이고 마지막은 `length - 1`입니다. 음수나 length와 같은 인덱스는 범위를 벗어나 실행 중 오류가 납니다.', '배열 첫 요소의 인덱스는 0이고 마지막은 `length - 1`입니다. 음수나 length와 같은 인덱스는 범위를 벗어나 실행 중 오류가 납니다.
```java
System.out.println(scores[0]);
```


인덱스는 첫 번째 요소가 0, 마지막 요소가 `length - 1`입니다. 사용자에게 보여 주는 ‘1번째’와 코드 인덱스 0을 혼동하지 않도록 변환이 필요할 수 있습니다. 범위 오류는 실행 중 발생합니다.

사용자가 1번째라고 입력한 값을 배열 인덱스로 쓸 때는 보통 1을 빼야 합니다. 접근 전에 인덱스가 0 이상이고 length보다 작은지 검사하면 범위 예외를 막을 수 있습니다.', 'System.out.println(scores[0]);', 3, 1, 1, NULL),
  (504, 5, 'length 속성', '배열 길이는 `array.length`로 읽습니다. String의 `length()`와 달리 괄호가 없습니다. 역순 처리에서는 `length - 1`부터 시작합니다.', '배열 길이는 `array.length`로 읽습니다. String의 `length()`와 달리 괄호가 없습니다. 역순 처리에서는 `length - 1`부터 시작합니다.
```java
for (int i = scores.length - 1; i >= 0; i--) { System.out.println(scores[i]); }
```


배열의 `length`는 필드처럼 괄호 없이 사용합니다. String의 `length()`와 구별해야 합니다. 마지막 요소 접근과 역순 반복에서 `length - 1`을 정확히 쓰는 것이 핵심입니다.

length는 배열 생성 시 정해진 크기를 반환하며 실제로 의미 있는 값이 몇 개 들어 있는지와는 다를 수 있습니다. 일부 칸만 사용한다면 별도의 count 변수가 필요합니다.', 'for (int i = scores.length - 1; i >= 0; i--) { System.out.println(scores[i]); }', 4, 1, 1, NULL),
  (505, 5, '일반 for문 배열 순회', '일반 for문은 인덱스가 필요할 때 사용합니다. 요소 위치 출력, 특정 위치 수정, 가장 큰 값의 위치 저장에 적합합니다. 조건은 항상 `i < array.length`를 우선 사용합니다.', '일반 for문은 인덱스가 필요할 때 사용합니다. 요소 위치 출력, 특정 위치 수정, 가장 큰 값의 위치 저장에 적합합니다. 조건은 항상 `i < array.length`를 우선 사용합니다.
```java
for (int i = 0; i < scores.length; i++) { System.out.println(i + ": " + scores[i]); }
```


일반 for문은 현재 인덱스가 필요할 때 강점이 있습니다. 예를 들어 가장 큰 값의 위치를 저장하거나, 특정 인덱스 요소를 수정할 수 있습니다. 반복 조건을 length에 맞추면 데이터 개수가 달라져도 같은 코드가 작동합니다.

배열 요소를 수정할 때는 `array[i]`에 새 값을 대입합니다. 인덱스를 함께 출력하면 값의 위치를 확인할 수 있고, 앞 요소와 다음 요소를 비교할 때도 일반 for문이 적합합니다.', 'for (int i = 0; i < scores.length; i++) { System.out.println(i + ": " + scores[i]); }', 5, 1, 1, NULL),
  (506, 5, '향상된 for문', '향상된 for문은 모든 요소를 읽는 경우 간단합니다. 반복 변수는 요소값 복사본이므로 그 변수에 새 값을 넣어도 원본 배열은 수정되지 않습니다. 인덱스가 필요하면 일반 for문을 사용합니다.', '향상된 for문은 모든 요소를 읽는 경우 간단합니다. 반복 변수는 요소값 복사본이므로 그 변수에 새 값을 넣어도 원본 배열은 수정되지 않습니다. 인덱스가 필요하면 일반 for문을 사용합니다.
```java
for (int score : scores) { System.out.println(score); }
```


향상된 for문은 모든 요소를 앞에서부터 읽기 쉽지만, 현재 위치를 알 수 없고 반복 변수에 값을 대입해도 원본 배열은 바뀌지 않습니다. 읽기·합계·출력처럼 위치가 필요 없는 작업에 사용합니다.

향상된 for문은 처음부터 끝까지 한 방향으로 순회합니다. 역순 처리, 일부 구간 처리, 인덱스 기반 수정에는 적합하지 않으므로 작업 목적에 따라 반복 방식을 선택합니다.', 'for (int score : scores) { System.out.println(score); }', 6, 1, 1, NULL),
  (507, 5, '배열 계산', '합계는 반복 전 0으로 초기화하고 반복 중 요소를 더합니다. 평균은 합계를 길이로 나누며, 소수점 평균은 나누기 전에 double로 변환합니다. 최댓값은 첫 요소를 기준으로 더 큰 값을 만날 때 갱신합니다.', '합계는 반복 전 0으로 초기화하고 반복 중 요소를 더합니다. 평균은 합계를 길이로 나누며, 소수점 평균은 나누기 전에 double로 변환합니다. 최댓값은 첫 요소를 기준으로 더 큰 값을 만날 때 갱신합니다.
```java
int sum = 0;
for (int score : scores) { sum += score; }
double average = (double) sum / scores.length;
```


누적 변수는 반복 전에 한 번 초기화하고, 반복 안에서 각 요소를 더합니다. 평균은 빈 배열일 때 0으로 나눌 수 없으므로 길이를 확인해야 합니다. 평균을 double로 만들려면 나누기 전에 한쪽을 double로 변환합니다.

최댓값 문제는 ‘현재까지의 최댓값’ 변수를 유지하는 방식입니다. 첫 요소를 초기값으로 쓰면 음수 배열에서도 안전하지만, 배열이 비어 있지 않다는 전제가 필요합니다. 위치도 필요하면 maxIndex 변수를 함께 둡니다.

최솟값도 첫 요소를 기준으로 더 작은 값을 만날 때 갱신합니다. 합계가 int 범위를 넘을 가능성이 있으면 long 누적 변수를 사용하고, 평균 계산 전 배열 길이가 0인지 검사합니다.', 'int sum = 0;
for (int score : scores) { sum += score; }
double average = (double) sum / scores.length;', 7, 1, 1, NULL),
  (508, 5, 'String 기본', 'String은 여러 글자를 다루는 텍스트 자료형입니다. 큰따옴표로 값을 만들고 `+`로 텍스트와 값을 연결할 수 있습니다. 문자열 길이는 `length()`로 확인합니다.', 'String은 여러 글자를 다루는 텍스트 자료형입니다. 큰따옴표로 값을 만들고 `+`로 텍스트와 값을 연결할 수 있습니다. 문자열 길이는 `length()`로 확인합니다.
```java
String title = "Java";
System.out.println("과목: " + title);
System.out.println(title.length());
```

```console
과목: Java
4
```


문자열은 빈 문자열 `""`일 수 있으며 null과는 다릅니다. 문자열 연결이 반복되면 매번 새로운 문자열이 만들어질 수 있지만, Bronze에서는 값 연결과 출력 결과를 정확히 이해하는 데 집중합니다.', 'String title = "Java";
System.out.println("과목: " + title);
System.out.println(title.length());', 8, 1, 1, NULL),
  (509, 5, 'String 메서드', '문자열 길이는 `length()`, 특정 문자는 `charAt(index)`, 내용 비교는 `equals()`입니다. String의 인덱스도 0부터 시작합니다. `==`는 문자열 내용이 아니라 참조 비교이므로 일반적인 내용 비교에 쓰지 않습니다.', '문자열 길이는 `length()`, 특정 문자는 `charAt(index)`, 내용 비교는 `equals()`입니다. String의 인덱스도 0부터 시작합니다. `==`는 문자열 내용이 아니라 참조 비교이므로 일반적인 내용 비교에 쓰지 않습니다.
```java
String name = "Java";
boolean same = name.equals("Java");
```


String의 인덱스도 0부터 시작하며 `charAt()`은 범위를 벗어나면 예외가 발생합니다. `equals()`는 내용을 비교하고, `equalsIgnoreCase()`는 대소문자를 무시해 비교합니다. 문자열은 일반적으로 바뀌지 않는 값이므로 메서드 결과를 새 변수에 받아야 할 때가 많습니다.

`charAt(0)`은 첫 문자를 반환하고 `charAt(length()-1)`은 마지막 문자를 반환합니다. 대소문자를 구분하지 않는 비교에는 equalsIgnoreCase를 사용할 수 있으며 메서드 이름의 괄호를 빠뜨리지 않습니다.', 'String name = "Java";
boolean same = name.equals("Java");', 9, 1, 1, NULL),
  (510, 5, '배열·문자열 종합 검색', '이름 목록에서 원하는 이름을 찾을 때는 배열을 순회하고 각 요소를 equals로 비교합니다. 찾았는지 여부를 boolean으로 기록하면 검색 실패 메시지도 명확하게 처리할 수 있습니다.', '이름 목록에서 원하는 이름을 찾을 때는 배열을 순회하고 각 요소를 equals로 비교합니다. 찾았는지 여부를 boolean으로 기록하면 검색 실패 메시지도 명확하게 처리할 수 있습니다.
```java
boolean found = false;
for (String name : names) {
    if (name.equals(keyword)) { found = true; System.out.println("찾음: " + name); }
}
```


배열과 문자열을 함께 쓰면 여러 이름·제목·점수를 저장하고 필요한 값을 찾을 수 있습니다. 검색은 각 요소를 순회하면서 equals로 비교하고, 찾으면 결과를 표시합니다. 찾지 못한 경우의 메시지도 따로 처리합니다.

검색 중 값을 찾으면 위치나 해당 문자열을 저장할 수 있습니다. 중복된 값이 있을 때 첫 항목만 찾을지 모두 찾을지에 따라 break 사용 여부가 달라지므로 요구사항을 먼저 정합니다.

---

# Java Silver 이론 학습 자료', 'boolean found = false;
for (String name : names) {
    if (name.equals(keyword)) { found = true; System.out.println("찾음: " + name); }
}', 10, 1, 1, NULL),
  (1101, 11, '메서드 역할', '메서드는 특정 작업을 하나의 이름으로 묶은 코드 단위입니다. 반복되는 계산이나 출력을 메서드로 분리하면 같은 코드를 여러 번 작성하지 않아도 되고, main 메서드는 전체 실행 순서만 보여 줄 수 있습니다.', '메서드는 특정 작업을 하나의 이름으로 묶은 코드 단위입니다. 반복되는 계산이나 출력을 메서드로 분리하면 같은 코드를 여러 번 작성하지 않아도 되고, main 메서드는 전체 실행 순서만 보여 줄 수 있습니다.

메서드 이름은 수행하는 동작이 드러나도록 짓습니다. 한 메서드가 입력·계산·출력을 모두 담당해 지나치게 길어지면 역할별 메서드로 다시 나누는 것이 좋습니다.
```java
static void greet() {
    System.out.println("안녕하세요");
}

greet();
greet();
```

```console
안녕하세요
안녕하세요
```', 'static void greet() {
    System.out.println("안녕하세요");
}

greet();
greet();', 1, 1, 1, NULL),
  (1102, 11, '메서드 선언', '메서드 선언에는 접근 범위, 정적 여부, 반환형, 메서드 이름, 매개변수 목록이 들어갑니다. 호출할 때 전달한 값은 선언된 매개변수에 순서대로 저장됩니다.', '메서드 선언에는 접근 범위, 정적 여부, 반환형, 메서드 이름, 매개변수 목록이 들어갑니다. 호출할 때 전달한 값은 선언된 매개변수에 순서대로 저장됩니다.

반환형과 실제로 반환하는 값의 자료형은 호환되어야 합니다. 매개변수의 개수나 자료형이 선언과 다르면 호출할 수 없습니다.
```java
static int add(int left, int right) {
    return left + right;
}

int result = add(3, 4);
System.out.println(result);
```

```console
7
```', 'static int add(int left, int right) {
    return left + right;
}

int result = add(3, 4);
System.out.println(result);', 2, 1, 1, NULL),
  (1103, 11, 'void 메서드', '`void`는 호출한 곳에 값을 돌려주지 않는다는 뜻입니다. 화면 출력, 객체 상태 변경, 파일 저장처럼 작업 자체가 목적인 메서드에 사용합니다.', '`void`는 호출한 곳에 값을 돌려주지 않는다는 뜻입니다. 화면 출력, 객체 상태 변경, 파일 저장처럼 작업 자체가 목적인 메서드에 사용합니다.

void 메서드에서도 `return;`을 사용해 실행을 일찍 끝낼 수 있지만, return 뒤에 값을 작성할 수는 없습니다.
```java
static void printPositive(int number) {
    if (number <= 0) return;
    System.out.println(number);
}

printPositive(-1);
printPositive(5);
```

```console
5
```', 'static void printPositive(int number) {
    if (number <= 0) return;
    System.out.println(number);
}

printPositive(-1);
printPositive(5);', 3, 1, 1, NULL),
  (1104, 11, '반환값', '반환값은 메서드가 계산한 결과를 호출한 곳으로 전달합니다. `return`이 실행되면 메서드는 즉시 끝나며, 반환된 값은 변수에 저장하거나 다른 계산에 바로 사용할 수 있습니다.', '반환값은 메서드가 계산한 결과를 호출한 곳으로 전달합니다. `return`이 실행되면 메서드는 즉시 끝나며, 반환된 값은 변수에 저장하거나 다른 계산에 바로 사용할 수 있습니다.

반환형이 void가 아니라면 모든 정상 실행 경로에서 해당 자료형의 값을 반환해야 합니다.
```java
static boolean isEven(int number) {
    return number % 2 == 0;
}

System.out.println(isEven(8));
System.out.println(isEven(7));
```

```console
true
false
```', 'static boolean isEven(int number) {
    return number % 2 == 0;
}

System.out.println(isEven(8));
System.out.println(isEven(7));', 4, 1, 1, NULL),
  (1105, 11, '매개변수', '매개변수는 메서드가 작업에 필요한 값을 받는 변수입니다. Java는 호출할 때 인자의 값을 매개변수로 복사하므로, 기본형 매개변수를 메서드 안에서 바꿔도 호출한 쪽 변수는 바뀌지 않습니다.', '매개변수는 메서드가 작업에 필요한 값을 받는 변수입니다. Java는 호출할 때 인자의 값을 매개변수로 복사하므로, 기본형 매개변수를 메서드 안에서 바꿔도 호출한 쪽 변수는 바뀌지 않습니다.

매개변수가 많아지면 호출 순서를 혼동하기 쉬우므로 서로 관련된 값은 객체로 묶는 방법도 고려합니다.
```java
static void change(int value) {
    value = 100;
}

int number = 10;
change(number);
System.out.println(number);
```

```console
10
```', 'static void change(int value) {
    value = 100;
}

int number = 10;
change(number);
System.out.println(number);', 5, 1, 1, NULL),
  (1106, 11, '지역 변수 범위', '메서드 안에서 선언한 지역 변수는 해당 블록 안에서만 사용할 수 있습니다. 메서드 호출이 끝나면 그 호출에서 사용한 지역 변수도 더 이상 사용할 수 없습니다.', '메서드 안에서 선언한 지역 변수는 해당 블록 안에서만 사용할 수 있습니다. 메서드 호출이 끝나면 그 호출에서 사용한 지역 변수도 더 이상 사용할 수 없습니다.

서로 다른 메서드에는 같은 이름의 지역 변수가 있어도 충돌하지 않습니다. 변수 범위를 작게 유지하면 예상하지 못한 값 변경을 줄일 수 있습니다.
```java
static void first() {
    int value = 10;
    System.out.println(value);
}

static void second() {
    int value = 20;
    System.out.println(value);
}
```', 'static void first() {
    int value = 10;
    System.out.println(value);
}

static void second() {
    int value = 20;
    System.out.println(value);
}', 6, 1, 1, NULL),
  (1107, 11, '메서드 오버로딩', '오버로딩은 같은 클래스 안에서 메서드 이름은 같고 매개변수의 개수·자료형·순서가 다른 메서드를 여러 개 선언하는 기능입니다. 비슷한 작업을 여러 입력 형태로 제공할 때 유용합니다.', '오버로딩은 같은 클래스 안에서 메서드 이름은 같고 매개변수의 개수·자료형·순서가 다른 메서드를 여러 개 선언하는 기능입니다. 비슷한 작업을 여러 입력 형태로 제공할 때 유용합니다.

반환형만 다르게 선언하는 것은 오버로딩이 아닙니다. 컴파일러가 호출 인자만 보고 어느 메서드인지 결정할 수 있어야 합니다.
```java
static int add(int a, int b) { return a + b; }
static double add(double a, double b) { return a + b; }

System.out.println(add(2, 3));
System.out.println(add(1.5, 2.0));
```

```console
5
3.5
```', 'static int add(int a, int b) { return a + b; }
static double add(double a, double b) { return a + b; }

System.out.println(add(2, 3));
System.out.println(add(1.5, 2.0));', 7, 1, 1, NULL),
  (1108, 11, '재귀 기초', '재귀 메서드는 자기 자신을 다시 호출합니다. 반복을 끝내는 종료 조건과 문제의 크기를 줄이는 재귀 호출이 반드시 필요합니다. 종료 조건이 없으면 호출이 계속 쌓여 오류가 발생합니다.', '재귀 메서드는 자기 자신을 다시 호출합니다. 반복을 끝내는 종료 조건과 문제의 크기를 줄이는 재귀 호출이 반드시 필요합니다. 종료 조건이 없으면 호출이 계속 쌓여 오류가 발생합니다.

단순 반복은 for문이 더 읽기 쉬울 수 있으므로, 계층 구조나 재귀적으로 정의된 문제에서 제한적으로 사용합니다.
```java
static int sum(int number) {
    if (number <= 1) return number;
    return number + sum(number - 1);
}

System.out.println(sum(4));
```

```console
10
```', 'static int sum(int number) {
    if (number <= 1) return number;
    return number + sum(number - 1);
}

System.out.println(sum(4));', 8, 1, 1, NULL),
  (1109, 11, '배열 매개변수', '배열을 메서드에 전달하면 배열 객체를 가리키는 참조값이 복사됩니다. 따라서 메서드 안에서 배열 요소를 변경하면 호출한 쪽에서도 변경된 요소가 보입니다.', '배열을 메서드에 전달하면 배열 객체를 가리키는 참조값이 복사됩니다. 따라서 메서드 안에서 배열 요소를 변경하면 호출한 쪽에서도 변경된 요소가 보입니다.

배열 변수 자체에 새 배열을 대입하는 것과 기존 배열 요소를 바꾸는 것은 서로 다른 동작입니다.
```java
static void doubleFirst(int[] values) {
    values[0] *= 2;
}

int[] numbers = {5, 10};
doubleFirst(numbers);
System.out.println(numbers[0]);
```

```console
10
```', 'static void doubleFirst(int[] values) {
    values[0] *= 2;
}

int[] numbers = {5, 10};
doubleFirst(numbers);
System.out.println(numbers[0]);', 9, 1, 1, NULL),
  (1110, 11, '메서드 종합', '프로그램을 입력, 검증, 계산, 출력 메서드로 나누면 각 기능을 따로 읽고 테스트할 수 있습니다. 메서드는 한 가지 책임에 집중하고, 이름과 반환값만으로 사용 방법을 이해할 수 있게 설계합니다.', '프로그램을 입력, 검증, 계산, 출력 메서드로 나누면 각 기능을 따로 읽고 테스트할 수 있습니다. 메서드는 한 가지 책임에 집중하고, 이름과 반환값만으로 사용 방법을 이해할 수 있게 설계합니다.

작은 메서드를 조합하면 main 메서드가 업무 흐름을 설명하는 목차처럼 보입니다.
```java
static int calculateAverage(int total, int count) {
    if (count == 0) return 0;
    return total / count;
}

int average = calculateAverage(240, 3);
System.out.println(average >= 70 ? "통과" : "재도전");
```

```console
통과
```', 'static int calculateAverage(int total, int count) {
    if (count == 0) return 0;
    return total / count;
}

int average = calculateAverage(240, 3);
System.out.println(average >= 70 ? "통과" : "재도전");', 10, 1, 1, NULL),
  (1201, 12, '객체지향 개념', '객체지향 프로그래밍은 데이터와 그 데이터를 다루는 기능을 객체로 묶어 프로그램을 구성하는 방식입니다. 객체는 상태를 필드로 저장하고 동작을 메서드로 제공합니다.', '객체지향 프로그래밍은 데이터와 그 데이터를 다루는 기능을 객체로 묶어 프로그램을 구성하는 방식입니다. 객체는 상태를 필드로 저장하고 동작을 메서드로 제공합니다.

현실의 학생·상품·계좌처럼 책임이 분명한 대상을 클래스로 표현하면 관련 코드가 한곳에 모여 변경하기 쉬워집니다.
```java
class Counter {
    int count;
    void increase() { count++; }
}
```', 'class Counter {
    int count;
    void increase() { count++; }
}', 1, 1, 1, NULL),
  (1202, 12, '클래스 선언', '클래스는 객체를 만들기 위한 설계도입니다. 클래스 이름, 필드, 생성자, 메서드를 선언하고 `new`를 사용해 실제 객체를 생성합니다.', '클래스는 객체를 만들기 위한 설계도입니다. 클래스 이름, 필드, 생성자, 메서드를 선언하고 `new`를 사용해 실제 객체를 생성합니다.

한 클래스로 여러 객체를 만들 수 있으며 각 객체는 자신만의 인스턴스 필드값을 가집니다.
```java
class Student {
    String name;
}

Student student = new Student();
student.name = "민수";
System.out.println(student.name);
```

```console
민수
```', 'class Student {
    String name;
}

Student student = new Student();
student.name = "민수";
System.out.println(student.name);', 2, 1, 1, NULL),
  (1203, 12, '필드', '필드는 객체의 상태를 저장하는 클래스 내부 변수입니다. 지역 변수와 달리 객체가 생성될 때 기본값으로 초기화되며 객체가 존재하는 동안 값을 유지합니다.', '필드는 객체의 상태를 저장하는 클래스 내부 변수입니다. 지역 변수와 달리 객체가 생성될 때 기본값으로 초기화되며 객체가 존재하는 동안 값을 유지합니다.

필드는 보통 private으로 감추고 메서드를 통해 안전하게 변경합니다.
```java
class Account {
    int balance;
}

Account account = new Account();
System.out.println(account.balance);
```

```console
0
```', 'class Account {
    int balance;
}

Account account = new Account();
System.out.println(account.balance);', 3, 1, 1, NULL),
  (1204, 12, '인스턴스 메서드', '인스턴스 메서드는 특정 객체에 소속되어 객체의 필드에 접근할 수 있습니다. 호출할 때는 `객체.메서드()` 형식을 사용합니다.', '인스턴스 메서드는 특정 객체에 소속되어 객체의 필드에 접근할 수 있습니다. 호출할 때는 `객체.메서드()` 형식을 사용합니다.

같은 메서드라도 어느 객체에서 호출했는지에 따라 서로 다른 필드값을 사용합니다.
```java
class Account {
    int balance;
    void deposit(int amount) { balance += amount; }
}

Account account = new Account();
account.deposit(5000);
System.out.println(account.balance);
```

```console
5000
```', 'class Account {
    int balance;
    void deposit(int amount) { balance += amount; }
}

Account account = new Account();
account.deposit(5000);
System.out.println(account.balance);', 4, 1, 1, NULL),
  (1205, 12, '객체 생성', '`new`는 메모리에 객체를 만들고 그 객체의 참조를 반환합니다. 참조 변수는 객체 자체가 아니라 객체를 찾아갈 수 있는 값을 저장합니다.', '`new`는 메모리에 객체를 만들고 그 객체의 참조를 반환합니다. 참조 변수는 객체 자체가 아니라 객체를 찾아갈 수 있는 값을 저장합니다.

두 변수가 같은 객체를 참조하면 한 변수로 변경한 내용이 다른 변수에서도 보입니다.
```java
Student first = new Student();
Student second = first;
second.name = "영희";
System.out.println(first.name);
```

```console
영희
```', 'Student first = new Student();
Student second = first;
second.name = "영희";
System.out.println(first.name);', 5, 1, 1, NULL),
  (1206, 12, '생성자', '생성자는 객체 생성 직후 필드를 초기화합니다. 클래스와 이름이 같고 반환형이 없으며, 생성자를 직접 선언하지 않으면 매개변수 없는 기본 생성자가 제공됩니다.', '생성자는 객체 생성 직후 필드를 초기화합니다. 클래스와 이름이 같고 반환형이 없으며, 생성자를 직접 선언하지 않으면 매개변수 없는 기본 생성자가 제공됩니다.

생성자 매개변수를 사용하면 필수값을 가진 상태로 객체를 만들 수 있습니다.
```java
class Product {
    String name;
    Product(String name) { this.name = name; }
}

Product product = new Product("키보드");
System.out.println(product.name);
```

```console
키보드
```', 'class Product {
    String name;
    Product(String name) { this.name = name; }
}

Product product = new Product("키보드");
System.out.println(product.name);', 6, 1, 1, NULL),
  (1207, 12, 'this', '`this`는 현재 메서드나 생성자가 실행되고 있는 객체 자신을 가리킵니다. 필드와 매개변수 이름이 같을 때 `this.name`으로 필드를 구분합니다.', '`this`는 현재 메서드나 생성자가 실행되고 있는 객체 자신을 가리킵니다. 필드와 매개변수 이름이 같을 때 `this.name`으로 필드를 구분합니다.

`this(...)`는 같은 클래스의 다른 생성자를 호출하며 생성자 첫 문장에 작성해야 합니다.
```java
class User {
    String name;
    User(String name) { this.name = name; }
}
```', 'class User {
    String name;
    User(String name) { this.name = name; }
}', 7, 1, 1, NULL),
  (1208, 12, '접근 제어', '접근 제어자는 클래스와 멤버를 사용할 수 있는 범위를 정합니다. public은 어디서나, private은 선언한 클래스 내부에서만 접근할 수 있습니다.', '접근 제어자는 클래스와 멤버를 사용할 수 있는 범위를 정합니다. public은 어디서나, private은 선언한 클래스 내부에서만 접근할 수 있습니다.

필드를 private으로 보호하면 외부 코드가 잘못된 값을 직접 넣는 일을 막을 수 있습니다.
```java
class Account {
    private int balance;
    public int getBalance() { return balance; }
}
```', 'class Account {
    private int balance;
    public int getBalance() { return balance; }
}', 8, 1, 1, NULL),
  (1209, 12, '캡슐화', '캡슐화는 객체 내부 상태를 감추고 공개된 메서드로만 다루게 하는 설계입니다. setter나 업무 메서드에서 입력값을 검증하면 객체가 잘못된 상태가 되는 것을 막습니다.', '캡슐화는 객체 내부 상태를 감추고 공개된 메서드로만 다루게 하는 설계입니다. setter나 업무 메서드에서 입력값을 검증하면 객체가 잘못된 상태가 되는 것을 막습니다.

단순히 모든 필드에 getter와 setter를 만드는 것보다 객체가 수행해야 할 동작을 메서드로 제공하는 편이 좋습니다.
```java
void withdraw(int amount) {
    if (amount > 0 && amount <= balance) balance -= amount;
}
```', 'void withdraw(int amount) {
    if (amount > 0 && amount <= balance) balance -= amount;
}', 9, 1, 1, NULL),
  (1210, 12, '클래스 종합', '클래스를 설계할 때는 객체가 저장할 상태, 수행할 동작, 생성 시 필요한 값을 정합니다. 필드는 감추고 생성자와 메서드가 유효한 상태를 유지하게 합니다.
```java
class Student {
 private final String name;
 private int score;
 Student(String name, int score) { this.name = name; this.score = score; }
 boolean isPassed() { return score >= 70; }
}', '클래스를 설계할 때는 객체가 저장할 상태, 수행할 동작, 생성 시 필요한 값을 정합니다. 필드는 감추고 생성자와 메서드가 유효한 상태를 유지하게 합니다.
```java
class Student {
    private final String name;
    private int score;
    Student(String name, int score) { this.name = name; this.score = score; }
    boolean isPassed() { return score >= 70; }
}

Student student = new Student("민수", 85);
System.out.println(student.isPassed());
```

```console
true
```', 'class Student {
    private final String name;
    private int score;
    Student(String name, int score) { this.name = name; this.score = score; }
    boolean isPassed() { return score >= 70; }
}

Student student = new Student("민수", 85);
System.out.println(student.isPassed());', 10, 1, 1, NULL),
  (1301, 13, '상속 개념', '상속은 기존 클래스의 필드와 메서드를 새 클래스가 이어받는 기능입니다. 공통 기능은 부모 클래스에 두고, 자식 클래스는 자신에게 필요한 기능을 추가하거나 재정의합니다.', '상속은 기존 클래스의 필드와 메서드를 새 클래스가 이어받는 기능입니다. 공통 기능은 부모 클래스에 두고, 자식 클래스는 자신에게 필요한 기능을 추가하거나 재정의합니다.

상속은 단순 코드 복사를 위한 기능이 아니라 자식이 부모의 한 종류라는 관계가 성립할 때 사용합니다.
```java
class Animal { void eat() { System.out.println("먹기"); } }
class Dog extends Animal { }

new Dog().eat();
```

```console
먹기
```', 'class Animal { void eat() { System.out.println("먹기"); } }
class Dog extends Animal { }

new Dog().eat();', 1, 1, 1, NULL),
  (1302, 13, 'extends', '클래스 선언에 `extends 부모클래스`를 작성하면 상속 관계가 만들어집니다. Java 클래스는 하나의 클래스만 직접 상속할 수 있습니다.', '클래스 선언에 `extends 부모클래스`를 작성하면 상속 관계가 만들어집니다. Java 클래스는 하나의 클래스만 직접 상속할 수 있습니다.

private 멤버는 자식 클래스에서 직접 접근할 수 없고 부모의 공개 메서드를 통해 사용합니다.
```java
class Vehicle { int speed = 10; }
class Car extends Vehicle { void accelerate() { speed += 10; } }

Car car = new Car();
car.accelerate();
System.out.println(car.speed);
```

```console
20
```', 'class Vehicle { int speed = 10; }
class Car extends Vehicle { void accelerate() { speed += 10; } }

Car car = new Car();
car.accelerate();
System.out.println(car.speed);', 2, 1, 1, NULL),
  (1303, 13, 'super', '`super`는 현재 객체의 부모 부분을 가리킵니다. `super.field`와 `super.method()`로 부모 멤버를 명확히 선택하고, `super(...)`로 부모 생성자를 호출합니다.', '`super`는 현재 객체의 부모 부분을 가리킵니다. `super.field`와 `super.method()`로 부모 멤버를 명확히 선택하고, `super(...)`로 부모 생성자를 호출합니다.

자식 생성자는 부모 생성자를 먼저 실행해 부모 부분을 초기화합니다.
```java
class Person { String name; Person(String name) { this.name = name; } }
class Student extends Person { Student(String name) { super(name); } }

System.out.println(new Student("수진").name);
```

```console
수진
```', 'class Person { String name; Person(String name) { this.name = name; } }
class Student extends Person { Student(String name) { super(name); } }

System.out.println(new Student("수진").name);', 3, 1, 1, NULL),
  (1304, 13, '메서드 오버라이딩', '오버라이딩은 자식 클래스가 부모의 인스턴스 메서드를 같은 선언 형태로 다시 구현하는 기능입니다. `@Override`를 붙이면 잘못된 재정의를 컴파일러가 확인해 줍니다.
```java
class Animal { void sound() { System.out.println("소리"); } }
class Dog extends Animal {
 @Override void sound() { System.out.println("멍멍"); }
}', '오버라이딩은 자식 클래스가 부모의 인스턴스 메서드를 같은 선언 형태로 다시 구현하는 기능입니다. `@Override`를 붙이면 잘못된 재정의를 컴파일러가 확인해 줍니다.
```java
class Animal { void sound() { System.out.println("소리"); } }
class Dog extends Animal {
    @Override void sound() { System.out.println("멍멍"); }
}

new Dog().sound();
```

```console
멍멍
```

오버라이딩하려면 메서드 이름과 매개변수 목록이 부모와 같아야 하며, 접근 범위를 부모보다 좁힐 수 없습니다. 부모가 public이면 자식도 public으로 선언합니다.

`@Override`가 없어도 오버라이딩은 동작하지만, 이름이나 매개변수를 잘못 쓰면 새로운 메서드로 처리되어 버그를 찾기 어렵습니다. 재정의 의도가 있는 메서드에는 항상 붙이는 습관이 좋습니다.', 'class Animal { void sound() { System.out.println("소리"); } }
class Dog extends Animal {
    @Override void sound() { System.out.println("멍멍"); }
}

new Dog().sound();', 4, 1, 1, NULL),
  (1305, 13, '다형성', '다형성은 부모 타입 변수 하나로 여러 자식 객체를 다루는 성질입니다. 호출할 수 있는 멤버는 변수 타입을 기준으로 확인하지만, 오버라이딩 메서드는 실제 객체의 구현이 실행됩니다.
```java
class Animal {
 void sound() { System.out.println("동물 소리"); }
}
class Dog extends Animal {
 @Override void sound() { System.out.println("멍멍"); }
}', '다형성은 부모 타입 변수 하나로 여러 자식 객체를 다루는 성질입니다. 호출할 수 있는 멤버는 변수 타입을 기준으로 확인하지만, 오버라이딩 메서드는 실제 객체의 구현이 실행됩니다.
```java
class Animal {
    void sound() { System.out.println("동물 소리"); }
}
class Dog extends Animal {
    @Override void sound() { System.out.println("멍멍"); }
}

Animal animal = new Dog();
animal.sound();
```

```console
멍멍
```

부모 타입 변수로는 부모에 선언된 멤버만 호출할 수 있습니다. 자식이 새로 추가한 메서드를 쓰려면 다운캐스팅이 필요하므로, 공통 동작은 부모 타입에 선언해 두는 것이 좋습니다.

다형성은 새로운 자식 클래스가 추가되어도 사용하는 코드를 바꾸지 않아도 되게 해 줍니다. 배열·컬렉션·매개변수를 부모 타입으로 선언하면 여러 구현을 같은 방식으로 처리할 수 있습니다.', 'class Animal {
    void sound() { System.out.println("동물 소리"); }
}
class Dog extends Animal {
    @Override void sound() { System.out.println("멍멍"); }
}

Animal animal = new Dog();
animal.sound();', 5, 1, 1, NULL),
  (1306, 13, '업캐스팅', '자식 객체를 부모 타입 변수에 저장하는 업캐스팅은 자동으로 이루어집니다. 공통 부모 타입의 배열이나 매개변수로 여러 자식 객체를 처리할 수 있습니다.', '자식 객체를 부모 타입 변수에 저장하는 업캐스팅은 자동으로 이루어집니다. 공통 부모 타입의 배열이나 매개변수로 여러 자식 객체를 처리할 수 있습니다.
```java
Animal[] animals = {new Dog(), new Cat()};
for (Animal animal : animals) animal.sound();
```

업캐스팅 후에도 실제 객체는 바뀌지 않으므로 오버라이딩된 메서드는 자식의 구현이 실행됩니다. 참조 타입은 호출할 수 있는 멤버의 범위만 결정합니다.

서로 다른 자식 객체를 하나의 부모 타입 배열에 담으면 반복문 하나로 공통 동작을 실행할 수 있습니다. 요소마다 실제 타입에 맞는 오버라이딩 결과가 나옵니다.', 'Animal[] animals = {new Dog(), new Cat()};
for (Animal animal : animals) animal.sound();', 6, 1, 1, NULL),
  (1307, 13, '다운캐스팅', '부모 타입 참조를 자식 타입으로 바꾸는 다운캐스팅에는 명시적 캐스팅이 필요합니다. 실제 객체가 해당 자식 타입이 아니면 ClassCastException이 발생합니다.
```java
class Animal {
 void sound() { System.out.println("동물 소리"); }
}
class Dog extends Animal {
 @Override void sound() { System.out.println("멍멍"); }
}', '부모 타입 참조를 자식 타입으로 바꾸는 다운캐스팅에는 명시적 캐스팅이 필요합니다. 실제 객체가 해당 자식 타입이 아니면 ClassCastException이 발생합니다.
```java
class Animal {
    void sound() { System.out.println("동물 소리"); }
}
class Dog extends Animal {
    @Override void sound() { System.out.println("멍멍"); }
}

Animal animal = new Dog();
Dog dog = (Dog) animal;
dog.sound();
```

```console
멍멍
```

다운캐스팅 전에는 instanceof로 실제 타입을 확인하는 것이 안전합니다. 확인 없이 캐스팅하면 실행 중 ClassCastException으로 프로그램이 중단될 수 있습니다.

다운캐스팅이 자주 필요하다면 설계를 다시 살펴볼 신호일 수 있습니다. 공통 동작을 부모 타입이나 인터페이스로 끌어올리면 캐스팅 없이 다형성으로 처리할 수 있습니다.', 'class Animal {
    void sound() { System.out.println("동물 소리"); }
}
class Dog extends Animal {
    @Override void sound() { System.out.println("멍멍"); }
}

Animal animal = new Dog();
Dog dog = (Dog) animal;
dog.sound();', 7, 1, 1, NULL),
  (1308, 13, 'instanceof', '`instanceof`는 객체를 특정 타입으로 안전하게 사용할 수 있는지 확인합니다. null에 사용하면 예외가 아니라 false를 반환합니다.', '`instanceof`는 객체를 특정 타입으로 안전하게 사용할 수 있는지 확인합니다. null에 사용하면 예외가 아니라 false를 반환합니다.
```java
if (animal instanceof Dog dog) {
    dog.sound();
}
```

instanceof 패턴 매칭 문법을 사용하면 타입 확인과 캐스팅을 한 번에 처리할 수 있습니다. 조건이 참일 때만 변수(dog)가 만들어지므로 별도의 캐스팅 문장이 필요 없습니다.

instanceof는 상속 관계 전체를 확인하므로 자식 객체는 부모 타입 검사에서도 true가 됩니다. 세밀한 분기가 필요하면 더 구체적인 타입부터 검사해야 합니다.', 'if (animal instanceof Dog dog) {
    dog.sound();
}', 8, 1, 1, NULL),
  (1309, 13, 'final', 'final 클래스는 상속할 수 없고, final 메서드는 자식 클래스에서 오버라이딩할 수 없습니다. 변경되면 안 되는 설계 규칙을 명확하게 표현합니다.', 'final 클래스는 상속할 수 없고, final 메서드는 자식 클래스에서 오버라이딩할 수 없습니다. 변경되면 안 되는 설계 규칙을 명확하게 표현합니다.
```java
final class Utility { }
// class Child extends Utility { } // 컴파일 오류
```

final 클래스는 더 이상 확장하면 안 되는 완성된 설계임을 나타냅니다. String처럼 자바 표준 라이브러리의 핵심 클래스도 final로 선언되어 있습니다.

final 메서드는 자식이 동작을 바꾸면 안 되는 핵심 규칙에 사용합니다. 무분별하게 붙이면 확장성이 떨어지므로 변경을 막아야 할 분명한 이유가 있을 때만 사용합니다.', 'final class Utility { }
// class Child extends Utility { } // 컴파일 오류', 9, 1, 1, NULL),
  (1310, 13, '상속 종합', '상속 구조에서는 부모에 진짜 공통인 상태와 동작만 둡니다. 자식별 차이는 오버라이딩하고, 사용하는 코드는 가능한 부모 타입에 의존하면 구현 추가가 쉬워집니다.
```java
static void playSound(Animal animal) { animal.sound(); }', '상속 구조에서는 부모에 진짜 공통인 상태와 동작만 둡니다. 자식별 차이는 오버라이딩하고, 사용하는 코드는 가능한 부모 타입에 의존하면 구현 추가가 쉬워집니다.
```java
static void playSound(Animal animal) { animal.sound(); }

playSound(new Dog());
playSound(new Cat());
```

playSound처럼 부모 타입 매개변수를 받는 메서드는 새 자식 클래스가 추가되어도 수정할 필요가 없습니다. 확장에는 열려 있고 기존 코드 수정에는 닫혀 있는 구조가 됩니다.

상속 구조를 설계할 때는 ‘자식은 부모의 한 종류인가’를 먼저 확인합니다. 코드 재사용만이 목적이라면 상속보다 다른 객체를 필드로 포함하는 방법이 적합할 수 있습니다.', 'static void playSound(Animal animal) { animal.sound(); }

playSound(new Dog());
playSound(new Cat());', 10, 1, 1, NULL),
  (1401, 14, '인터페이스 역할', '인터페이스는 구현 클래스가 제공해야 할 기능의 규칙을 선언합니다. 사용하는 코드는 구체 클래스 대신 인터페이스 타입을 바라보므로 구현을 교체하거나 테스트용 객체를 넣기 쉬워집니다.', '인터페이스는 구현 클래스가 제공해야 할 기능의 규칙을 선언합니다. 사용하는 코드는 구체 클래스 대신 인터페이스 타입을 바라보므로 구현을 교체하거나 테스트용 객체를 넣기 쉬워집니다.
```java
interface Printer { void print(String text); }
class ConsolePrinter implements Printer {
    public void print(String text) { System.out.println(text); }
}
```

인터페이스에는 구현 코드 없이 기능의 이름·입력·반환만 선언합니다. 사용하는 쪽은 ‘무엇을 할 수 있는가’만 알면 되고 ‘어떻게 하는가’는 구현 클래스가 책임집니다.

하나의 인터페이스를 여러 클래스가 구현하면 상황에 따라 구현을 바꿔 끼울 수 있습니다. 콘솔 출력 구현을 파일 출력 구현으로 교체해도 사용하는 코드는 그대로입니다.', 'interface Printer { void print(String text); }
class ConsolePrinter implements Printer {
    public void print(String text) { System.out.println(text); }
}', 1, 1, 1, NULL),
  (1402, 14, 'implements', '클래스는 `implements` 뒤에 구현할 인터페이스를 작성합니다. 구체 클래스는 인터페이스의 모든 추상 메서드를 public으로 구현해야 합니다.', '클래스는 `implements` 뒤에 구현할 인터페이스를 작성합니다. 구체 클래스는 인터페이스의 모든 추상 메서드를 public으로 구현해야 합니다.
```java
interface RunnableTask { void run(); }
class DownloadTask implements RunnableTask {
    public void run() { System.out.println("다운로드"); }
}
```

한 클래스는 여러 인터페이스를 쉼표로 나열해 동시에 구현할 수 있습니다. 클래스 상속이 하나로 제한되는 것과 달리 역할은 여러 개를 가질 수 있습니다.

추상 메서드를 하나라도 구현하지 않으면 컴파일 오류가 발생합니다. 인터페이스의 메서드는 기본적으로 public이므로 구현 메서드에 public을 빠뜨리지 않도록 주의합니다.', 'interface RunnableTask { void run(); }
class DownloadTask implements RunnableTask {
    public void run() { System.out.println("다운로드"); }
}', 2, 1, 1, NULL),
  (1403, 14, '추상 메서드', '추상 메서드는 실행 본문 없이 메서드 선언만 제공합니다. 인터페이스를 구현하는 클래스마다 같은 기능 이름을 유지하면서 서로 다른 동작을 작성할 수 있습니다.', '추상 메서드는 실행 본문 없이 메서드 선언만 제공합니다. 인터페이스를 구현하는 클래스마다 같은 기능 이름을 유지하면서 서로 다른 동작을 작성할 수 있습니다.
```java
interface Calculator { int calculate(int a, int b); }
class Adder implements Calculator {
    public int calculate(int a, int b) { return a + b; }
}
```

추상 메서드는 본문 대신 세미콜론으로 선언을 끝냅니다. 구현 클래스마다 같은 이름의 메서드가 서로 다른 계산을 수행할 수 있습니다.

선언과 구현이 분리되면 기능 규칙과 실제 처리를 따로 검토할 수 있습니다. Calculator 인터페이스 하나로 덧셈·뺄셈·곱셈 구현 클래스를 계속 추가할 수 있습니다.', 'interface Calculator { int calculate(int a, int b); }
class Adder implements Calculator {
    public int calculate(int a, int b) { return a + b; }
}', 3, 1, 1, NULL),
  (1404, 14, '인터페이스 다형성', '인터페이스 타입 변수에는 해당 인터페이스를 구현한 여러 객체를 저장할 수 있습니다. 호출 시 실제 객체가 구현한 메서드가 선택됩니다.', '인터페이스 타입 변수에는 해당 인터페이스를 구현한 여러 객체를 저장할 수 있습니다. 호출 시 실제 객체가 구현한 메서드가 선택됩니다.
```java
Printer printer = new ConsolePrinter();
printer.print("출력");
```

인터페이스 타입 변수는 구현 클래스가 무엇인지 몰라도 선언된 기능을 호출할 수 있습니다. 실제로 실행되는 코드는 대입된 객체의 구현입니다.

메서드 매개변수를 인터페이스 타입으로 선언하면 어떤 구현 객체든 전달할 수 있습니다. 테스트할 때는 실제 구현 대신 검증용 구현을 넣기도 합니다.', 'Printer printer = new ConsolePrinter();
printer.print("출력");', 4, 1, 1, NULL),
  (1405, 14, 'default 메서드', 'default 메서드는 인터페이스에 기본 구현을 제공합니다. 기존 구현 클래스를 모두 수정하지 않고 공통 기능을 추가할 수 있으며, 구현 클래스가 필요하면 다시 오버라이딩할 수 있습니다.', 'default 메서드는 인터페이스에 기본 구현을 제공합니다. 기존 구현 클래스를 모두 수정하지 않고 공통 기능을 추가할 수 있으며, 구현 클래스가 필요하면 다시 오버라이딩할 수 있습니다.
```java
interface Greeter {
    default void greet() { System.out.println("안녕하세요"); }
}
class UserGreeter implements Greeter { }
```

default 메서드가 있어도 인터페이스는 여전히 객체를 직접 만들 수 없습니다. 구현 클래스가 오버라이딩하지 않으면 기본 구현이 그대로 사용됩니다.

이미 배포된 인터페이스에 새 추상 메서드를 추가하면 모든 구현 클래스가 깨집니다. default 메서드는 이런 상황에서 기존 코드를 보호하면서 기능을 추가하는 수단입니다.', 'interface Greeter {
    default void greet() { System.out.println("안녕하세요"); }
}
class UserGreeter implements Greeter { }', 5, 1, 1, NULL),
  (1406, 14, 'static 메서드', '인터페이스의 static 메서드는 객체가 아닌 인터페이스 자체에 속합니다. 구현 클래스에 상속되지 않으므로 반드시 인터페이스 이름으로 호출합니다.', '인터페이스의 static 메서드는 객체가 아닌 인터페이스 자체에 속합니다. 구현 클래스에 상속되지 않으므로 반드시 인터페이스 이름으로 호출합니다.
```java
interface MathUtil {
    static int twice(int value) { return value * 2; }
}
System.out.println(MathUtil.twice(5));
```

```console
10
```

static 메서드는 구현 객체 없이 사용할 수 있는 보조 기능에 적합합니다. 인터페이스와 관련된 검증·변환 유틸리티를 한곳에 모아 둘 수 있습니다.

구현 클래스 이름이나 객체 참조로는 인터페이스의 static 메서드를 호출할 수 없습니다. 반드시 `MathUtil.twice(5)`처럼 인터페이스 이름을 사용해야 합니다.', 'interface MathUtil {
    static int twice(int value) { return value * 2; }
}
System.out.println(MathUtil.twice(5));', 6, 1, 1, NULL),
  (1407, 14, '인터페이스 상속', '인터페이스는 `extends`로 하나 이상의 인터페이스를 상속할 수 있습니다. 하위 인터페이스를 구현하는 클래스는 상속받은 모든 추상 메서드를 구현해야 합니다.', '인터페이스는 `extends`로 하나 이상의 인터페이스를 상속할 수 있습니다. 하위 인터페이스를 구현하는 클래스는 상속받은 모든 추상 메서드를 구현해야 합니다.
```java
interface Reader { void read(); }
interface Writer { void write(); }
interface Editor extends Reader, Writer { }
```

클래스와 달리 인터페이스는 여러 부모 인터페이스를 동시에 상속할 수 있습니다. Editor를 구현하는 클래스는 read와 write를 모두 구현해야 합니다.

인터페이스 상속은 작은 역할을 조합해 큰 역할을 만들 때 유용합니다. 읽기 기능만 필요한 코드에는 Reader 타입만 전달해 접근 범위를 제한할 수 있습니다.', 'interface Reader { void read(); }
interface Writer { void write(); }
interface Editor extends Reader, Writer { }', 7, 1, 1, NULL),
  (1408, 14, '추상 클래스', '추상 클래스는 `abstract`로 선언하며 객체를 직접 만들 수 없습니다. 필드·생성자·일반 메서드와 추상 메서드를 함께 가질 수 있어 공통 상태와 일부 구현을 공유할 때 적합합니다.', '추상 클래스는 `abstract`로 선언하며 객체를 직접 만들 수 없습니다. 필드·생성자·일반 메서드와 추상 메서드를 함께 가질 수 있어 공통 상태와 일부 구현을 공유할 때 적합합니다.
```java
abstract class Shape { abstract double area(); }
class Square extends Shape {
    double side = 3;
    double area() { return side * side; }
}
```

추상 클래스는 필드·생성자·구현된 메서드를 가질 수 있어 공통 상태를 공유할 때 적합합니다. 인터페이스는 역할 계약, 추상 클래스는 부분 구현 공유가 주 목적입니다.

추상 클래스도 클래스이므로 하나만 상속할 수 있습니다. 상태 공유가 필요 없다면 여러 개를 구현할 수 있는 인터페이스가 더 유연한 선택입니다.', 'abstract class Shape { abstract double area(); }
class Square extends Shape {
    double side = 3;
    double area() { return side * side; }
}', 8, 1, 1, NULL),
  (1409, 14, '구현체 교체', '서비스가 인터페이스를 통해 기능을 사용하면 생성자에서 전달하는 객체만 바꿔 동작을 교체할 수 있습니다. 결제·알림·저장 방식처럼 구현이 여러 개인 기능에 활용합니다.', '서비스가 인터페이스를 통해 기능을 사용하면 생성자에서 전달하는 객체만 바꿔 동작을 교체할 수 있습니다. 결제·알림·저장 방식처럼 구현이 여러 개인 기능에 활용합니다.
```java
interface Sender { void send(); }
static void notify(Sender sender) { sender.send(); }
```

서비스가 구체 클래스를 직접 생성하면 구현을 바꿀 때마다 서비스 코드를 수정해야 합니다. 인터페이스 타입으로 전달받으면 교체할 때 만들어 넣는 객체만 달라집니다.

이 구조는 실무 프레임워크의 의존성 주입으로 이어지는 기본기입니다. 문자 알림을 이메일 알림으로 바꾸는 요구가 와도 Sender 구현 클래스만 추가하면 됩니다.', 'interface Sender { void send(); }
static void notify(Sender sender) { sender.send(); }', 9, 1, 1, NULL),
  (1410, 14, '추상화 종합', '추상화는 사용하는 쪽에 필요한 기능만 공개하고 구현 세부사항을 숨깁니다. 인터페이스는 역할 계약, 구현 클래스는 실제 처리, 서비스는 인터페이스를 통한 흐름 제어를 담당하게 나눌 수 있습니다.', '추상화는 사용하는 쪽에 필요한 기능만 공개하고 구현 세부사항을 숨깁니다. 인터페이스는 역할 계약, 구현 클래스는 실제 처리, 서비스는 인터페이스를 통한 흐름 제어를 담당하게 나눌 수 있습니다.
```java
interface Payment { int pay(int amount); }
class CouponPayment implements Payment {
    public int pay(int amount) { return amount - 1000; }
}
Payment payment = new CouponPayment();
System.out.println(payment.pay(5000));
```

```console
4000
```

결제 수단이 늘어나도 Payment 인터페이스를 구현한 클래스를 추가할 뿐, 결제를 사용하는 코드는 바뀌지 않습니다. 역할과 구현을 나누는 것이 추상화의 핵심 효과입니다.

추상화 수준을 정할 때는 사용하는 쪽에 필요한 최소 기능만 인터페이스에 남깁니다. 너무 많은 메서드를 선언하면 구현 클래스마다 불필요한 구현이 강제됩니다.', 'interface Payment { int pay(int amount); }
class CouponPayment implements Payment {
    public int pay(int amount) { return amount - 1000; }
}
Payment payment = new CouponPayment();
System.out.println(payment.pay(5000));', 10, 1, 1, NULL),
  (1501, 15, '예외 개념', '예외는 프로그램 실행 중 정상 흐름을 방해하는 상황입니다. 잘못된 숫자 변환, 배열 범위 초과, null 접근 등이 대표적이며 예외 종류와 발생 조건을 알면 적절히 복구할 수 있습니다.', '예외는 프로그램 실행 중 정상 흐름을 방해하는 상황입니다. 잘못된 숫자 변환, 배열 범위 초과, null 접근 등이 대표적이며 예외 종류와 발생 조건을 알면 적절히 복구할 수 있습니다.

오류(Error)는 메모리 부족처럼 JVM 수준에서 발생해 프로그램이 스스로 복구하기 어려운 문제이고, 예외(Exception)는 코드에서 미리 대비하고 처리할 수 있는 문제입니다. 애플리케이션 코드에서 try-catch로 다루는 대상은 주로 예외입니다.
```java
int value = Integer.parseInt("Java");
```', 'int value = Integer.parseInt("Java");', 1, 1, 1, NULL),
  (1502, 15, 'try-catch', '예외가 발생할 수 있는 코드를 try에 두고, 처리할 예외 타입과 복구 코드를 catch에 작성합니다. 예외가 발생하면 try의 남은 문장은 건너뛰고 일치하는 catch로 이동합니다.', '예외가 발생할 수 있는 코드를 try에 두고, 처리할 예외 타입과 복구 코드를 catch에 작성합니다. 예외가 발생하면 try의 남은 문장은 건너뛰고 일치하는 catch로 이동합니다.
```java
try {
    System.out.println(10 / 0);
} catch (ArithmeticException e) {
    System.out.println("0으로 나눌 수 없습니다");
}
```

```console
0으로 나눌 수 없습니다
```

catch 블록을 여러 개 두면 예외 종류별로 다른 복구를 할 수 있습니다. 구체적인 예외를 위에, 넓은 범위의 예외를 아래에 배치해야 모든 catch가 의미를 가집니다.

예외가 발생하지 않으면 catch 블록은 실행되지 않고 try 다음 코드로 진행합니다. try 범위는 실제로 예외가 날 수 있는 문장으로 좁게 유지하는 것이 좋습니다.', 'try {
    System.out.println(10 / 0);
} catch (ArithmeticException e) {
    System.out.println("0으로 나눌 수 없습니다");
}', 2, 1, 1, NULL),
  (1503, 15, 'finally', 'finally 블록은 예외 발생 여부와 관계없이 실행됩니다. 파일·네트워크 연결 같은 자원을 정리하는 코드에 사용하지만, 가능하면 자동 자원 관리 문법을 우선합니다.', 'finally 블록은 예외 발생 여부와 관계없이 실행됩니다. 파일·네트워크 연결 같은 자원을 정리하는 코드에 사용하지만, 가능하면 자동 자원 관리 문법을 우선합니다.
```java
try { System.out.println("작업"); }
finally { System.out.println("정리"); }
```

```console
작업
정리
```

try 안에서 return을 하더라도 finally 블록은 실행됩니다. 그래서 자원 반납처럼 반드시 수행해야 하는 정리 작업을 맡기기에 적합합니다.

Java 7부터는 try-with-resources 문법이 자원 닫기를 자동으로 처리해 줍니다. 새로 작성하는 코드에서는 finally에서 직접 close를 호출하기보다 이 문법을 우선 사용합니다.', 'try { System.out.println("작업"); }
finally { System.out.println("정리"); }', 3, 1, 1, NULL),
  (1504, 15, 'throws', '`throws`는 메서드에서 처리하지 않은 예외를 호출한 곳으로 전달한다고 선언합니다. 호출자는 다시 throws로 넘기거나 try-catch로 처리해야 합니다.', '`throws`는 메서드에서 처리하지 않은 예외를 호출한 곳으로 전달한다고 선언합니다. 호출자는 다시 throws로 넘기거나 try-catch로 처리해야 합니다.
```java
static String load() throws IOException {
    return Files.readString(Path.of("data.txt"));
}
```

throws는 예외를 처리하는 것이 아니라 처리 책임을 호출자에게 넘기는 선언입니다. 예외를 어느 계층에서 처리할지 정하는 설계 판단이 함께 필요합니다.

checked 예외를 던지는 메서드를 호출하면 컴파일러가 처리 여부를 강제합니다. main까지 계속 throws로 넘기면 결국 프로그램이 스택 추적을 출력하며 종료됩니다.', 'static String load() throws IOException {
    return Files.readString(Path.of("data.txt"));
}', 4, 1, 1, NULL),
  (1505, 15, '예외 종류', 'checked 예외는 컴파일러가 처리 여부를 확인하고, unchecked 예외는 RuntimeException 계열로 실행 중 잘못된 값이나 로직에서 주로 발생합니다.', 'checked 예외는 컴파일러가 처리 여부를 확인하고, unchecked 예외는 RuntimeException 계열로 실행 중 잘못된 값이나 로직에서 주로 발생합니다.
```java
String text = null;
System.out.println(text.length());
```

checked 예외는 파일·네트워크처럼 코드가 정상이어도 외부 환경 때문에 실패할 수 있는 작업에 사용됩니다. 컴파일 단계에서 try-catch나 throws를 강제합니다.

unchecked 예외는 null 접근, 잘못된 인덱스처럼 대부분 코드 결함이 원인입니다. 모든 곳에서 잡기보다 원인이 되는 로직을 수정하는 것이 우선입니다.', 'String text = null;
System.out.println(text.length());', 5, 1, 1, NULL),
  (1506, 15, '사용자 정의 예외', '업무 규칙 위반을 명확하게 표현하려면 사용자 정의 예외를 만들 수 있습니다. 예외 이름에는 어떤 문제가 발생했는지 드러내고 메시지에 원인을 담습니다.', '업무 규칙 위반을 명확하게 표현하려면 사용자 정의 예외를 만들 수 있습니다. 예외 이름에는 어떤 문제가 발생했는지 드러내고 메시지에 원인을 담습니다.
```java
class InsufficientBalanceException extends RuntimeException {
    InsufficientBalanceException(String message) { super(message); }
}
```

표준 예외로 상황이 충분히 설명되면 그대로 사용하고, 업무 규칙 위반처럼 도메인 고유의 문제만 사용자 정의 예외로 만듭니다. 이름만 봐도 원인을 알 수 있게 짓습니다.

생성자에서 super(message)로 메시지를 전달하면 getMessage()로 원인을 확인할 수 있습니다. 잔액 부족 예외라면 부족한 금액 같은 정보를 필드로 함께 담을 수도 있습니다.', 'class InsufficientBalanceException extends RuntimeException {
    InsufficientBalanceException(String message) { super(message); }
}', 6, 1, 1, NULL),
  (1507, 15, '파일 경로', 'Path는 파일이나 디렉터리의 위치를 표현합니다. 상대 경로는 프로그램 실행 위치를 기준으로 하고, 절대 경로는 루트부터 전체 위치를 나타냅니다.', 'Path는 파일이나 디렉터리의 위치를 표현합니다. 상대 경로는 프로그램 실행 위치를 기준으로 하고, 절대 경로는 루트부터 전체 위치를 나타냅니다.
```java
Path path = Path.of("data", "scores.txt");
System.out.println(path);
```

```console
data/scores.txt
```

경로 구분자는 운영체제에 따라 다르게 출력됩니다. macOS·Linux에서는 `data/scores.txt`, Windows에서는 `data\\scores.txt`로 표시되지만 `Path.of("data", "scores.txt")`처럼 이름을 나누어 전달하면 같은 코드가 모든 운영체제에서 동작합니다.', 'Path path = Path.of("data", "scores.txt");
System.out.println(path);', 7, 1, 1, NULL),
  (1508, 15, '파일 읽기', '작은 텍스트 파일은 `Files.readString`으로 전체 내용을 읽을 수 있습니다. 큰 파일이나 줄 단위 처리가 필요하면 BufferedReader를 사용합니다.', '작은 텍스트 파일은 `Files.readString`으로 전체 내용을 읽을 수 있습니다. 큰 파일이나 줄 단위 처리가 필요하면 BufferedReader를 사용합니다.
```java
String content = Files.readString(Path.of("memo.txt"));
System.out.println(content);
```

파일이 없거나 접근 권한이 없으면 IOException이 발생하므로 읽기 코드는 예외 처리와 함께 작성합니다. 읽기 전에 Files.exists로 존재 여부를 확인할 수도 있습니다.

Files.readString은 파일 전체를 한 번에 메모리로 가져오므로 작은 설정·메모 파일에 적합합니다. 줄 단위 처리가 필요하면 Files.readAllLines나 BufferedReader를 사용합니다.', 'String content = Files.readString(Path.of("memo.txt"));
System.out.println(content);', 8, 1, 1, NULL),
  (1509, 15, '파일 쓰기', '`Files.writeString`은 문자열을 파일에 기록합니다. 기본 동작은 기존 내용을 덮어쓸 수 있으므로 이어 쓰기 여부와 문자 인코딩을 명확히 정합니다.', '`Files.writeString`은 문자열을 파일에 기록합니다. 기본 동작은 기존 내용을 덮어쓸 수 있으므로 이어 쓰기 여부와 문자 인코딩을 명확히 정합니다.
```java
Files.writeString(Path.of("memo.txt"), "Java 학습");
```

기존 파일이 있으면 내용이 통째로 교체되므로, 이어 쓰려면 StandardOpenOption.APPEND 옵션을 지정해야 합니다. 저장 전에 덮어쓰기 여부를 분명히 결정합니다.

인코딩을 지정하지 않으면 UTF-8이 사용됩니다. 다른 프로그램과 파일을 주고받을 때는 양쪽이 같은 인코딩을 쓰는지 확인해야 한글 깨짐을 막을 수 있습니다.', 'Files.writeString(Path.of("memo.txt"), "Java 학습");', 9, 1, 1, NULL),
  (1510, 15, '예외·파일 종합', '파일 처리는 경로 준비, 읽기·쓰기, 예외 처리 순서로 구성합니다. try-with-resources를 사용하면 작업이 끝날 때 자원을 자동으로 닫을 수 있습니다.', '파일 처리는 경로 준비, 읽기·쓰기, 예외 처리 순서로 구성합니다. try-with-resources를 사용하면 작업이 끝날 때 자원을 자동으로 닫을 수 있습니다.
```java
try {
    String text = Files.readString(Path.of("memo.txt"));
    System.out.println(text.length());
} catch (IOException e) {
    System.out.println("파일을 확인하세요");
}
```

파일 처리 흐름은 실패 지점이 많으므로 어떤 단계에서 실패했는지 구분해 안내하면 좋습니다. 경로 문제인지 권한 문제인지에 따라 해결 방법이 다릅니다.

예외를 잡은 뒤 아무것도 하지 않고 지나가면 문제를 숨기게 됩니다. 최소한 원인을 출력하거나 기본값으로 복구하는 등 의미 있는 처리를 남겨야 합니다.

---

# Java Gold 이론 학습 자료', 'try {
    String text = Files.readString(Path.of("memo.txt"));
    System.out.println(text.length());
} catch (IOException e) {
    System.out.println("파일을 확인하세요");
}', 10, 1, 1, NULL),
  (2101, 21, '컬렉션 개요', '컬렉션 프레임워크는 여러 객체를 저장하고 처리하는 표준 자료구조입니다. 배열과 달리 크기를 유연하게 바꿀 수 있고 List·Set·Map처럼 목적에 맞는 구조를 선택할 수 있습니다.', '컬렉션 프레임워크는 여러 객체를 저장하고 처리하는 표준 자료구조입니다. 배열과 달리 크기를 유연하게 바꿀 수 있고 List·Set·Map처럼 목적에 맞는 구조를 선택할 수 있습니다.
```java
List<String> names = new ArrayList<>();
names.add("민수");
names.add("지민");
System.out.println(names.size());
```

```console
2
```

컬렉션은 제네릭과 함께 사용해 저장할 타입을 지정합니다. List<String>처럼 선언하면 다른 타입을 넣는 실수를 컴파일 단계에서 막을 수 있습니다.

선언은 List 같은 인터페이스 타입으로, 생성은 ArrayList 같은 구현체로 하는 것이 관례입니다. 나중에 구현체를 바꿔도 사용하는 코드가 흔들리지 않습니다.', 'List<String> names = new ArrayList<>();
names.add("민수");
names.add("지민");
System.out.println(names.size());', 1, 1, 1, NULL),
  (2102, 21, 'List', 'List는 입력 순서를 유지하고 중복을 허용합니다. 인덱스로 요소를 조회·수정할 수 있어 순서가 중요한 목록에 적합합니다.', 'List는 입력 순서를 유지하고 중복을 허용합니다. 인덱스로 요소를 조회·수정할 수 있어 순서가 중요한 목록에 적합합니다.
```java
List<String> list = new ArrayList<>(List.of("A", "B", "A"));
System.out.println(list.get(1));
```

```console
B
```

get·set처럼 인덱스 기반 메서드가 List의 핵심입니다. 배열과 마찬가지로 범위를 벗어난 인덱스는 IndexOutOfBoundsException이 발생합니다.

List.of로 만든 목록은 요소를 추가·삭제할 수 없는 불변 목록입니다. 변경이 필요하면 예제처럼 new ArrayList<>(...)로 감싸서 사용합니다.', 'List<String> list = new ArrayList<>(List.of("A", "B", "A"));
System.out.println(list.get(1));', 2, 1, 1, NULL),
  (2103, 21, 'ArrayList', 'ArrayList는 내부 배열을 이용하는 List 구현체입니다. 인덱스 조회는 빠르지만 중간 삽입·삭제는 뒤 요소 이동이 필요할 수 있습니다.', 'ArrayList는 내부 배열을 이용하는 List 구현체입니다. 인덱스 조회는 빠르지만 중간 삽입·삭제는 뒤 요소 이동이 필요할 수 있습니다.
```java
List<Integer> numbers = new ArrayList<>();
numbers.add(10); numbers.add(20); numbers.set(0, 15);
System.out.println(numbers);
```

```console
[15, 20]
```

add는 목록 끝에 요소를 추가하고, set은 기존 위치의 값을 교체합니다. remove는 인덱스 또는 값으로 삭제할 수 있어 정수 목록에서는 어느 쪽으로 해석되는지 주의해야 합니다.

크기는 size()로 확인하며 배열의 length와 이름이 다릅니다. 인덱스 조회가 많은 작업에는 ArrayList가 유리하고, 맨 앞 삽입·삭제가 잦으면 다른 구현체를 고려합니다.', 'List<Integer> numbers = new ArrayList<>();
numbers.add(10); numbers.add(20); numbers.set(0, 15);
System.out.println(numbers);', 3, 1, 1, NULL),
  (2104, 21, 'Set', 'Set은 중복 요소를 허용하지 않습니다. 포함 여부 확인과 고유값 관리에 적합하며 일반적으로 인덱스가 없습니다.', 'Set은 중복 요소를 허용하지 않습니다. 포함 여부 확인과 고유값 관리에 적합하며 일반적으로 인덱스가 없습니다.
```java
Set<String> tags = new HashSet<>();
tags.add("java"); tags.add("java");
System.out.println(tags.size());
```

```console
1
```

이미 있는 요소를 add하면 저장되지 않고 false가 반환됩니다. 오류가 발생하는 것이 아니므로 중복 여부를 알아야 한다면 반환값을 확인합니다.

방문한 사용자 집합, 사용된 쿠폰 코드처럼 ‘있는지 없는지’가 중요한 데이터에 적합합니다. contains 검사는 일반적으로 List보다 빠릅니다.', 'Set<String> tags = new HashSet<>();
tags.add("java"); tags.add("java");
System.out.println(tags.size());', 4, 1, 1, NULL),
  (2105, 21, 'HashSet', 'HashSet은 해시값을 이용해 요소를 저장하고 검색합니다. 저장 순서를 보장하지 않으며, 사용자 객체의 중복 기준에는 equals와 hashCode가 함께 필요합니다.', 'HashSet은 해시값을 이용해 요소를 저장하고 검색합니다. 저장 순서를 보장하지 않으며, 사용자 객체의 중복 기준에는 equals와 hashCode가 함께 필요합니다.
```java
Set<Integer> values = new HashSet<>(List.of(3, 1, 3, 2));
System.out.println(values.contains(2));
```

```console
true
```

직접 만든 클래스를 HashSet에 저장하려면 equals와 hashCode를 함께 재정의해야 합니다. 그렇지 않으면 내용이 같은 객체가 중복으로 저장될 수 있습니다.

입력 순서를 유지해야 하면 LinkedHashSet, 정렬된 순서가 필요하면 TreeSet을 사용합니다. 요구사항에 따라 Set 구현체를 선택합니다.', 'Set<Integer> values = new HashSet<>(List.of(3, 1, 3, 2));
System.out.println(values.contains(2));', 5, 1, 1, NULL),
  (2106, 21, 'Map', 'Map은 고유한 키와 값의 쌍을 저장합니다. 같은 키로 다시 저장하면 기존 값이 교체되며 키를 통해 값을 빠르게 찾습니다.', 'Map은 고유한 키와 값의 쌍을 저장합니다. 같은 키로 다시 저장하면 기존 값이 교체되며 키를 통해 값을 빠르게 찾습니다.
```java
Map<String, Integer> scores = new HashMap<>();
scores.put("민수", 80); scores.put("민수", 90);
System.out.println(scores.get("민수"));
```

```console
90
```

keySet·values·entrySet으로 키, 값, 쌍 전체를 순회할 수 있습니다. 키의 존재 여부는 containsKey로 확인합니다.

같은 키로 put하면 값이 교체되는 성질은 점수 갱신 같은 기능에 그대로 활용할 수 있습니다. 이전 값이 필요하면 put의 반환값을 받아 둡니다.', 'Map<String, Integer> scores = new HashMap<>();
scores.put("민수", 80); scores.put("민수", 90);
System.out.println(scores.get("민수"));', 6, 1, 1, NULL),
  (2107, 21, 'HashMap', 'HashMap은 대표적인 Map 구현체로 키의 저장 순서를 보장하지 않습니다. 존재하지 않는 키는 null을 반환하며 `getOrDefault`로 기본값을 지정할 수 있습니다.', 'HashMap은 대표적인 Map 구현체로 키의 저장 순서를 보장하지 않습니다. 존재하지 않는 키는 null을 반환하며 `getOrDefault`로 기본값을 지정할 수 있습니다.
```java
Map<String, Integer> counts = new HashMap<>();
counts.put("java", 2);
System.out.println(counts.getOrDefault("sql", 0));
```

```console
0
```

getOrDefault는 키가 없을 때 null 대신 기본값을 돌려주므로 개수 세기에 유용합니다. `counts.put(key, counts.getOrDefault(key, 0) + 1)` 형태가 대표적입니다.

get의 null 반환값을 그대로 계산에 사용하면 NullPointerException이 발생합니다. 키 존재가 불확실한 조회에는 getOrDefault나 containsKey 확인을 먼저 둡니다.', 'Map<String, Integer> counts = new HashMap<>();
counts.put("java", 2);
System.out.println(counts.getOrDefault("sql", 0));', 7, 1, 1, NULL),
  (2108, 21, 'Iterator', 'Iterator는 컬렉션 요소를 순서대로 방문하는 표준 방법입니다. 반복 중 안전하게 삭제하려면 컬렉션의 remove 대신 iterator의 remove를 사용합니다.', 'Iterator는 컬렉션 요소를 순서대로 방문하는 표준 방법입니다. 반복 중 안전하게 삭제하려면 컬렉션의 remove 대신 iterator의 remove를 사용합니다.
```java
Iterator<Integer> it = numbers.iterator();
while (it.hasNext()) if (it.next() < 0) it.remove();
```

for-each 반복 중에 컬렉션의 remove를 호출하면 ConcurrentModificationException이 발생할 수 있습니다. 반복 중 삭제는 반드시 iterator의 remove를 사용합니다.

hasNext로 다음 요소가 있는지 확인하고 next로 요소를 가져오는 두 단계가 기본 흐름입니다. 요소가 없는데 next를 호출하면 예외가 발생합니다.', 'Iterator<Integer> it = numbers.iterator();
while (it.hasNext()) if (it.next() < 0) it.remove();', 8, 1, 1, NULL),
  (2109, 21, '정렬', 'Comparable은 객체의 기본 정렬 기준을, Comparator는 상황별 정렬 기준을 정의합니다. Comparator를 조합하면 여러 필드 기준 정렬도 만들 수 있습니다.', 'Comparable은 객체의 기본 정렬 기준을, Comparator는 상황별 정렬 기준을 정의합니다. Comparator를 조합하면 여러 필드 기준 정렬도 만들 수 있습니다.
```java
List<Integer> values = new ArrayList<>(List.of(3, 1, 2));
values.sort(Comparator.reverseOrder());
System.out.println(values);
```

```console
[3, 2, 1]
```

직접 만든 클래스는 Comparable의 compareTo를 구현하면 기본 정렬 기준이 생깁니다. 이름순·점수순처럼 기준이 여러 개라면 Comparator를 상황별로 만듭니다.

Comparator.comparing에 thenComparing을 연결하면 1차·2차 정렬 기준을 조합할 수 있고, reversed()로 내림차순 전환도 가능합니다.', 'List<Integer> values = new ArrayList<>(List.of(3, 1, 2));
values.sort(Comparator.reverseOrder());
System.out.println(values);', 9, 1, 1, NULL),
  (2110, 21, '컬렉션 종합', '자료구조는 요구사항에 맞게 선택합니다. 순서·중복이면 List, 고유값이면 Set, 키 기반 조회면 Map이 기본 선택입니다.', '자료구조는 요구사항에 맞게 선택합니다. 순서·중복이면 List, 고유값이면 Set, 키 기반 조회면 Map이 기본 선택입니다.
```java
Map<String, List<Integer>> scores = new HashMap<>();
scores.put("민수", List.of(80, 90));
System.out.println(scores.get("민수").get(1));
```

```console
90
```

예제처럼 Map의 값에 List를 넣으면 한 키에 여러 값을 저장하는 구조를 만들 수 있습니다. 학생별 점수 목록, 게시글별 댓글 목록 같은 관계 표현에 자주 사용합니다.

자료구조 선택이 코드 전체의 복잡도를 좌우합니다. 검색 위주라면 Map, 순서 있는 출력이라면 List를 먼저 검토하고, 필요하면 두 구조를 함께 사용합니다.', 'Map<String, List<Integer>> scores = new HashMap<>();
scores.put("민수", List.of(80, 90));
System.out.println(scores.get("민수").get(1));', 10, 1, 1, NULL),
  (2201, 22, '제네릭 필요성', '제네릭은 저장하거나 전달할 타입을 컴파일 시점에 지정해 잘못된 타입 사용을 막습니다. Object 기반 코드에 필요한 강제 형변환도 줄어듭니다.', '제네릭은 저장하거나 전달할 타입을 컴파일 시점에 지정해 잘못된 타입 사용을 막습니다. Object 기반 코드에 필요한 강제 형변환도 줄어듭니다.
```java
List<String> names = new ArrayList<>();
names.add("Java");
String name = names.get(0);
```

제네릭이 없던 Object 기반 컬렉션은 꺼낼 때마다 형변환이 필요했고, 잘못된 타입은 실행 중에야 발견됐습니다. 제네릭은 이 확인을 컴파일 시점으로 앞당깁니다.

List<String>에 정수를 넣으려 하면 즉시 컴파일 오류가 발생합니다. 실행 후가 아니라 코드 작성 단계에서 실수를 발견하는 것이 제네릭의 핵심 가치입니다.', 'List<String> names = new ArrayList<>();
names.add("Java");
String name = names.get(0);', 1, 1, 1, NULL),
  (2202, 22, '제네릭 클래스', '클래스 이름 뒤의 타입 매개변수는 객체 생성 시 실제 타입으로 정해집니다. 같은 설계를 여러 자료형에 안전하게 재사용할 수 있습니다.', '클래스 이름 뒤의 타입 매개변수는 객체 생성 시 실제 타입으로 정해집니다. 같은 설계를 여러 자료형에 안전하게 재사용할 수 있습니다.
```java
class Box<T> { T value; Box(T value) { this.value = value; } }
Box<Integer> box = new Box<>(10);
System.out.println(box.value);
```

```console
10
```

Box<Integer>와 Box<String>은 같은 클래스로 만들지만 서로 다른 타입을 안전하게 다룹니다. 타입별로 클래스를 복사해 만들 필요가 없습니다.

타입 매개변수에는 기본형을 쓸 수 없으므로 int 대신 Integer 같은 래퍼 클래스를 사용합니다. 오토박싱 덕분에 값 대입은 자연스럽게 처리됩니다.', 'class Box<T> { T value; Box(T value) { this.value = value; } }
Box<Integer> box = new Box<>(10);
System.out.println(box.value);', 2, 1, 1, NULL),
  (2203, 22, '제네릭 메서드', '제네릭 메서드는 메서드 자체에 타입 매개변수를 선언합니다. 클래스의 제네릭 여부와 관계없이 여러 타입을 처리할 수 있습니다.', '제네릭 메서드는 메서드 자체에 타입 매개변수를 선언합니다. 클래스의 제네릭 여부와 관계없이 여러 타입을 처리할 수 있습니다.
```java
static <T> T first(List<T> values) { return values.get(0); }
System.out.println(first(List.of("A", "B")));
```

```console
A
```

메서드 선언의 <T>는 반환형 앞에 작성합니다. 호출할 때는 인자의 타입을 보고 컴파일러가 T를 추론하므로 대부분 타입을 명시하지 않아도 됩니다.

문자열 목록을 전달하면 T가 String으로, 정수 목록을 전달하면 Integer로 결정됩니다. 하나의 메서드가 모든 타입의 목록에 재사용됩니다.', 'static <T> T first(List<T> values) { return values.get(0); }
System.out.println(first(List.of("A", "B")));', 3, 1, 1, NULL),
  (2204, 22, '와일드카드', '`? extends T`는 T의 하위 타입을 읽는 용도, `? super T`는 T 값을 넣는 용도에 적합합니다. 어떤 타입인지 완전히 알 수 없는 `?`에는 안전한 작업만 허용됩니다.', '`? extends T`는 T의 하위 타입을 읽는 용도, `? super T`는 T 값을 넣는 용도에 적합합니다. 어떤 타입인지 완전히 알 수 없는 `?`에는 안전한 작업만 허용됩니다.
```java
static double sum(List<? extends Number> values) {
    return values.stream().mapToDouble(Number::doubleValue).sum();
}
```

List<Integer>는 List<Number>의 하위 타입이 아니므로 그대로 전달할 수 없습니다. `? extends Number`를 쓰면 Integer·Double 목록을 모두 받을 수 있습니다.

읽기 위주면 extends, 쓰기 위주면 super라는 기준을 기억하면 선택이 쉬워집니다. 읽기와 쓰기가 모두 필요하면 와일드카드 없이 정확한 타입을 사용합니다.', 'static double sum(List<? extends Number> values) {
    return values.stream().mapToDouble(Number::doubleValue).sum();
}', 4, 1, 1, NULL),
  (2205, 22, '람다식', '람다식은 함수형 인터페이스의 단일 추상 메서드 구현을 간결하게 표현합니다. 매개변수, 화살표, 실행식 또는 블록으로 구성합니다.', '람다식은 함수형 인터페이스의 단일 추상 메서드 구현을 간결하게 표현합니다. 매개변수, 화살표, 실행식 또는 블록으로 구성합니다.
```java
Predicate<Integer> positive = number -> number > 0;
System.out.println(positive.test(5));
```

```console
true
```

람다는 이름 없는 메서드 구현을 값처럼 전달하는 문법입니다. 매개변수가 하나면 괄호를, 본문이 한 식이면 중괄호와 return을 생략할 수 있습니다.

익명 클래스로 여러 줄에 걸쳐 작성하던 구현이 한 줄로 줄어듭니다. 다만 본문이 길고 복잡해지면 별도 메서드로 분리하고 메서드 참조로 전달하는 편이 읽기 좋습니다.', 'Predicate<Integer> positive = number -> number > 0;
System.out.println(positive.test(5));', 5, 1, 1, NULL),
  (2206, 22, '함수형 인터페이스', '함수형 인터페이스는 추상 메서드가 하나인 인터페이스입니다. `@FunctionalInterface`는 조건 위반을 컴파일러가 확인하게 합니다.', '함수형 인터페이스는 추상 메서드가 하나인 인터페이스입니다. `@FunctionalInterface`는 조건 위반을 컴파일러가 확인하게 합니다.
```java
@FunctionalInterface
interface Operation { int apply(int a, int b); }
Operation add = (a, b) -> a + b;
```

람다식은 함수형 인터페이스 타입 변수에만 대입할 수 있습니다. 추상 메서드가 두 개 이상이면 람다가 어떤 메서드의 구현인지 알 수 없기 때문입니다.

직접 만들기 전에 java.util.function 패키지의 표준 인터페이스를 먼저 찾아봅니다. Predicate·Function·Consumer·Supplier로 대부분의 상황을 해결할 수 있습니다.', '@FunctionalInterface
interface Operation { int apply(int a, int b); }
Operation add = (a, b) -> a + b;', 6, 1, 1, NULL),
  (2207, 22, '메서드 참조', '메서드 참조는 람다가 기존 메서드를 그대로 호출할 때 `클래스::메서드` 형태로 줄입니다. 입력과 반환 구조가 함수형 인터페이스와 맞아야 합니다.', '메서드 참조는 람다가 기존 메서드를 그대로 호출할 때 `클래스::메서드` 형태로 줄입니다. 입력과 반환 구조가 함수형 인터페이스와 맞아야 합니다.
```java
List<String> names = List.of("Java", "SQL");
names.forEach(System.out::println);
```

```console
Java
SQL
```

`System.out::println`은 `s -> System.out.println(s)`와 같은 의미입니다. 람다가 하는 일이 기존 메서드 호출뿐이라면 메서드 참조가 더 간결합니다.

정적 메서드는 클래스::메서드, 인스턴스 메서드는 객체::메서드 형태를 사용합니다. `String::length`처럼 첫 매개변수가 호출 대상이 되는 형태도 있습니다.', 'List<String> names = List.of("Java", "SQL");
names.forEach(System.out::println);', 7, 1, 1, NULL),
  (2208, 22, 'Predicate와 Function', 'Predicate는 값을 검사해 boolean을 반환하고, Function은 입력값을 다른 값으로 변환합니다. 컬렉션 필터와 변환에 자주 사용합니다.', 'Predicate는 값을 검사해 boolean을 반환하고, Function은 입력값을 다른 값으로 변환합니다. 컬렉션 필터와 변환에 자주 사용합니다.
```java
Predicate<String> longName = s -> s.length() >= 4;
Function<String, Integer> length = String::length;
System.out.println(longName.test("Java") + ", " + length.apply("SQL"));
```

```console
true, 3
```

Predicate는 test, Function은 apply로 실행합니다. and·or·negate로 Predicate를 조합하고, andThen으로 Function을 연결할 수 있습니다.

스트림의 filter는 Predicate를, map은 Function을 받습니다. 이 두 인터페이스에 익숙해지면 다음 행성의 Stream 코드를 읽고 쓰기가 훨씬 수월해집니다.', 'Predicate<String> longName = s -> s.length() >= 4;
Function<String, Integer> length = String::length;
System.out.println(longName.test("Java") + ", " + length.apply("SQL"));', 8, 1, 1, NULL),
  (2209, 22, 'Consumer와 Supplier', 'Consumer는 값을 받아 소비하고 반환하지 않으며, Supplier는 입력 없이 값을 제공합니다. 출력·저장 동작과 지연 생성에 활용합니다.', 'Consumer는 값을 받아 소비하고 반환하지 않으며, Supplier는 입력 없이 값을 제공합니다. 출력·저장 동작과 지연 생성에 활용합니다.
```java
Consumer<String> printer = System.out::println;
Supplier<Integer> supplier = () -> 100;
printer.accept(String.valueOf(supplier.get()));
```

```console
100
```

Consumer는 accept로 값을 받아 처리만 하고, Supplier는 get으로 값을 만들어 돌려줍니다. 입력과 출력의 방향이 서로 반대인 인터페이스입니다.

Supplier는 실제로 필요할 때까지 값 생성을 미루는 지연 실행에 활용됩니다. 스트림의 forEach가 Consumer를 받는 대표적인 사용처입니다.', 'Consumer<String> printer = System.out::println;
Supplier<Integer> supplier = () -> 100;
printer.accept(String.valueOf(supplier.get()));', 9, 1, 1, NULL),
  (2210, 22, '제네릭·람다 종합', '제네릭으로 타입 안전한 구조를 만들고 람다로 변경 가능한 조건이나 동작을 전달할 수 있습니다. 알고리즘과 세부 조건을 분리하는 데 효과적입니다.', '제네릭으로 타입 안전한 구조를 만들고 람다로 변경 가능한 조건이나 동작을 전달할 수 있습니다. 알고리즘과 세부 조건을 분리하는 데 효과적입니다.
```java
static <T> List<T> filter(List<T> values, Predicate<T> rule) {
    return values.stream().filter(rule).toList();
}
System.out.println(filter(List.of(1, 2, 3), n -> n >= 2));
```

```console
[2, 3]
```

filter 메서드는 어떤 타입의 목록이든, 어떤 조건이든 받아서 처리합니다. 자료를 순회하는 공통 알고리즘과 상황마다 달라지는 조건이 분리된 구조입니다.

조건이 바뀌어도 filter 메서드는 수정하지 않고 호출하는 쪽의 람다만 바꿉니다. 이 분리가 다음 행성에서 배우는 Stream API의 설계 원리이기도 합니다.', 'static <T> List<T> filter(List<T> values, Predicate<T> rule) {
    return values.stream().filter(rule).toList();
}
System.out.println(filter(List.of(1, 2, 3), n -> n >= 2));', 10, 1, 1, NULL),
  (2301, 23, 'Stream 개요', 'Stream은 컬렉션 데이터를 선언적인 단계로 처리합니다. 원본을 직접 변경하지 않고 중간 연산을 연결한 뒤 최종 연산에서 결과를 만듭니다.', 'Stream은 컬렉션 데이터를 선언적인 단계로 처리합니다. 원본을 직접 변경하지 않고 중간 연산을 연결한 뒤 최종 연산에서 결과를 만듭니다.
```java
long count = List.of(1, 2, 3).stream().count();
System.out.println(count);
```

```console
3
```

스트림은 filter·map 같은 중간 연산을 이어 붙이고 count·toList 같은 최종 연산으로 마무리합니다. 최종 연산이 없으면 중간 연산은 실행되지 않습니다.

스트림은 한 번 소비하면 재사용할 수 없으므로 다시 처리하려면 새 스트림을 만들어야 합니다. 원본 컬렉션은 그대로 남아 있어 몇 번이든 새로 만들 수 있습니다.', 'long count = List.of(1, 2, 3).stream().count();
System.out.println(count);', 1, 1, 1, NULL),
  (2302, 23, 'filter', 'filter는 Predicate 조건이 true인 요소만 다음 단계로 전달합니다. 여러 filter를 연결하면 조건을 단계별로 표현할 수 있습니다.', 'filter는 Predicate 조건이 true인 요소만 다음 단계로 전달합니다. 여러 filter를 연결하면 조건을 단계별로 표현할 수 있습니다.
```java
List<Integer> result = List.of(1, 2, 3, 4).stream()
        .filter(n -> n % 2 == 0).toList();
System.out.println(result);
```

```console
[2, 4]
```

filter의 조건은 요소를 ‘남길’ 조건입니다. 짝수를 제거하는 것이 아니라 짝수만 남긴다는 방향을 혼동하지 않아야 합니다.

조건에 맞는 요소가 하나도 없으면 예외가 아니라 빈 결과가 나옵니다. 빈 목록일 가능성을 이후 처리에서 고려하면 안전한 코드가 됩니다.', 'List<Integer> result = List.of(1, 2, 3, 4).stream()
        .filter(n -> n % 2 == 0).toList();
System.out.println(result);', 2, 1, 1, NULL),
  (2303, 23, 'map', 'map은 각 요소를 다른 값으로 변환합니다. 요소 개수는 유지되지만 자료형은 달라질 수 있습니다.', 'map은 각 요소를 다른 값으로 변환합니다. 요소 개수는 유지되지만 자료형은 달라질 수 있습니다.
```java
List<Integer> lengths = List.of("Java", "SQL").stream()
        .map(String::length).toList();
System.out.println(lengths);
```

```console
[4, 3]
```

map의 변환 함수는 요소 하나를 받아 하나를 돌려줍니다. 문자열 목록에서 길이 목록을 얻는 것처럼 결과 스트림의 요소 타입이 달라질 수 있습니다.

filter와 map을 함께 쓸 때는 먼저 걸러서 요소 수를 줄인 뒤 변환하는 것이 일반적으로 효율적입니다. 연산 순서에 따라 결과가 달라질 수도 있으므로 의미도 함께 확인합니다.', 'List<Integer> lengths = List.of("Java", "SQL").stream()
        .map(String::length).toList();
System.out.println(lengths);', 3, 1, 1, NULL),
  (2304, 23, 'sorted', 'sorted는 요소를 기본 순서나 Comparator 기준으로 정렬합니다. 정렬 결과는 새 스트림으로 전달되며 원본 목록은 바뀌지 않습니다.', 'sorted는 요소를 기본 순서나 Comparator 기준으로 정렬합니다. 정렬 결과는 새 스트림으로 전달되며 원본 목록은 바뀌지 않습니다.
```java
List<Integer> sorted = List.of(3, 1, 2).stream().sorted().toList();
System.out.println(sorted);
```

```console
[1, 2, 3]
```

인자 없는 sorted는 요소의 기본 정렬 기준(Comparable)을 사용합니다. 기본 기준이 없는 사용자 정의 객체는 Comparator를 전달해야 합니다.

sorted는 모든 요소를 모아야 정렬할 수 있으므로 다른 중간 연산보다 비용이 큽니다. limit와 함께 쓰면 상위 N개 추출 같은 처리가 간단해집니다.', 'List<Integer> sorted = List.of(3, 1, 2).stream().sorted().toList();
System.out.println(sorted);', 4, 1, 1, NULL),
  (2305, 23, 'distinct와 limit', 'distinct는 중복을 제거하고 limit는 앞에서 지정한 개수만 남깁니다. 연산 순서에 따라 최종 결과가 달라질 수 있습니다.', 'distinct는 중복을 제거하고 limit는 앞에서 지정한 개수만 남깁니다. 연산 순서에 따라 최종 결과가 달라질 수 있습니다.
```java
List<Integer> result = List.of(1, 1, 2, 3).stream()
        .distinct().limit(2).toList();
System.out.println(result);
```

```console
[1, 2]
```

distinct는 equals 기준으로 중복을 판단합니다. 사용자 정의 객체에서 의도대로 동작하려면 equals와 hashCode 재정의가 필요합니다.

예제의 순서를 바꿔 limit(2) 뒤에 distinct를 두면 [1, 1]에서 중복이 제거되어 [1]이 됩니다. 연산을 나열한 순서가 곧 처리 순서입니다.', 'List<Integer> result = List.of(1, 1, 2, 3).stream()
        .distinct().limit(2).toList();
System.out.println(result);', 5, 1, 1, NULL),
  (2306, 23, 'count·sum·average', '숫자 전용 스트림은 합계와 평균 같은 집계 연산을 제공합니다. average는 데이터가 없을 수 있어 OptionalDouble로 결과를 표현합니다.', '숫자 전용 스트림은 합계와 평균 같은 집계 연산을 제공합니다. average는 데이터가 없을 수 있어 OptionalDouble로 결과를 표현합니다.
```java
int sum = List.of(10, 20, 30).stream().mapToInt(Integer::intValue).sum();
double avg = List.of(10, 20, 30).stream().mapToInt(Integer::intValue).average().orElse(0);
System.out.println(sum + ", " + avg);
```

```console
60, 20.0
```

일반 스트림에는 sum·average가 없으므로 mapToInt 같은 변환으로 숫자 전용 스트림(IntStream)을 만들어야 합니다. count는 일반 스트림에서도 사용할 수 있습니다.

average().orElse(0)은 데이터가 없을 때 0을 기본값으로 쓴다는 뜻입니다. 빈 목록의 평균을 0으로 볼지, 별도로 안내할지는 요구사항에 따라 정합니다.', 'int sum = List.of(10, 20, 30).stream().mapToInt(Integer::intValue).sum();
double avg = List.of(10, 20, 30).stream().mapToInt(Integer::intValue).average().orElse(0);
System.out.println(sum + ", " + avg);', 6, 1, 1, NULL),
  (2307, 23, 'reduce', 'reduce는 여러 요소를 하나의 값으로 누적합니다. 초기값과 두 값을 합치는 규칙을 제공하며 합계·곱·문자열 결합에 사용할 수 있습니다.', 'reduce는 여러 요소를 하나의 값으로 누적합니다. 초기값과 두 값을 합치는 규칙을 제공하며 합계·곱·문자열 결합에 사용할 수 있습니다.
```java
int product = List.of(2, 3, 4).stream().reduce(1, (a, b) -> a * b);
System.out.println(product);
```

```console
24
```

reduce의 첫 인자는 계산의 시작값입니다. 곱셈이므로 1을 사용했고, 덧셈이라면 0처럼 결과에 영향을 주지 않는 값을 사용합니다.

합계·최댓값처럼 자주 쓰는 누적은 sum·max 같은 전용 메서드가 이미 있습니다. reduce는 전용 메서드가 없는 사용자 정의 누적 규칙에 사용합니다.', 'int product = List.of(2, 3, 4).stream().reduce(1, (a, b) -> a * b);
System.out.println(product);', 7, 1, 1, NULL),
  (2308, 23, 'collect', 'collect는 스트림 결과를 List·Set·Map 같은 자료구조로 모읍니다. 키가 중복될 수 있는 Map 수집에는 병합 규칙이 필요합니다.', 'collect는 스트림 결과를 List·Set·Map 같은 자료구조로 모읍니다. 키가 중복될 수 있는 Map 수집에는 병합 규칙이 필요합니다.
```java
Set<String> set = List.of("A", "B", "A").stream().collect(Collectors.toSet());
System.out.println(set.size());
```

```console
2
```

toList·toSet 외에 Collectors.toMap으로 Map을 만들 수 있습니다. 같은 키가 두 번 나오면 예외가 발생하므로 병합 규칙을 세 번째 인자로 전달합니다.

Collectors.joining을 사용하면 문자열 목록을 구분자로 연결한 하나의 문자열을 만들 수 있습니다. 쉼표로 구분된 이름 목록 출력에 편리합니다.', 'Set<String> set = List.of("A", "B", "A").stream().collect(Collectors.toSet());
System.out.println(set.size());', 8, 1, 1, NULL),
  (2309, 23, 'groupingBy', 'groupingBy는 기준 함수의 결과가 같은 요소를 Map의 같은 그룹으로 묶습니다. 그룹별 개수·합계 같은 집계와 함께 사용할 수 있습니다.', 'groupingBy는 기준 함수의 결과가 같은 요소를 Map의 같은 그룹으로 묶습니다. 그룹별 개수·합계 같은 집계와 함께 사용할 수 있습니다.
```java
Map<Integer, List<String>> groups = List.of("A", "BB", "CC").stream()
        .collect(Collectors.groupingBy(String::length));
System.out.println(groups.get(2));
```

```console
[BB, CC]
```

groupingBy의 결과는 Map이며 키는 기준 함수의 반환값, 값은 해당 그룹의 요소 목록입니다. 등급별 학생 목록, 카테고리별 상품 목록에 그대로 적용됩니다.

두 번째 인자로 Collectors.counting()을 전달하면 그룹별 목록 대신 그룹별 개수를 얻습니다. 집계 방식만 바꿔 다양한 통계를 만들 수 있습니다.', 'Map<Integer, List<String>> groups = List.of("A", "BB", "CC").stream()
        .collect(Collectors.groupingBy(String::length));
System.out.println(groups.get(2));', 9, 1, 1, NULL),
  (2310, 23, 'Stream 종합', '스트림은 필터링, 변환, 정렬, 수집 순서로 읽으면 이해하기 쉽습니다. 중간 연산은 최종 연산이 호출될 때 실제로 처리됩니다.', '스트림은 필터링, 변환, 정렬, 수집 순서로 읽으면 이해하기 쉽습니다. 중간 연산은 최종 연산이 호출될 때 실제로 처리됩니다.
```java
List<String> result = List.of("sql", "java", "web").stream()
        .filter(s -> s.length() >= 4).map(String::toUpperCase).sorted().toList();
System.out.println(result);
```

```console
[JAVA]
```

긴 스트림 체인은 단계마다 어떤 데이터가 흐르는지 확인하며 읽습니다. 예제는 [sql, java, web] → 길이 필터 → [java] → 대문자 변환 → [JAVA] → 정렬 순서로 진행됩니다.

반복문으로도 같은 결과를 만들 수 있지만 스트림은 ‘무엇을 할지’가 연산 이름으로 드러납니다. 다만 중간값 추적이 필요한 복잡한 로직은 반복문이 더 디버깅하기 쉬울 수 있습니다.', 'List<String> result = List.of("sql", "java", "web").stream()
        .filter(s -> s.length() >= 4).map(String::toUpperCase).sorted().toList();
System.out.println(result);', 10, 1, 1, NULL),
  (2401, 24, 'LocalDate', 'LocalDate는 시간대 없이 연·월·일을 표현합니다. 불변 객체이므로 날짜 계산 메서드는 원본을 바꾸지 않고 새 객체를 반환합니다.', 'LocalDate는 시간대 없이 연·월·일을 표현합니다. 불변 객체이므로 날짜 계산 메서드는 원본을 바꾸지 않고 새 객체를 반환합니다.
```java
LocalDate date = LocalDate.of(2026, 7, 23);
System.out.println(date.plusDays(1));
```

```console
2026-07-24
```

plusDays 같은 계산 메서드는 새 객체를 반환하므로 결과를 변수에 받아야 합니다. date.plusDays(1)만 호출하고 date를 계속 쓰면 값은 그대로입니다.

getYear·getMonthValue·getDayOfWeek로 날짜 요소를 읽고, isBefore·isAfter로 날짜를 비교합니다. 존재하지 않는 날짜를 of로 만들면 예외가 발생합니다.', 'LocalDate date = LocalDate.of(2026, 7, 23);
System.out.println(date.plusDays(1));', 1, 1, 1, NULL),
  (2402, 24, 'LocalDateTime', 'LocalDateTime은 날짜와 시각을 함께 표현하지만 시간대 정보는 없습니다. 서버 간 절대 시각 교환에는 Instant나 시간대 타입을 고려합니다.', 'LocalDateTime은 날짜와 시각을 함께 표현하지만 시간대 정보는 없습니다. 서버 간 절대 시각 교환에는 Instant나 시간대 타입을 고려합니다.
```java
LocalDateTime time = LocalDateTime.of(2026, 7, 23, 10, 30);
System.out.println(time.getHour());
```

```console
10
```

LocalDate와 LocalTime을 합친 형태로, 예약 시각처럼 날짜와 시각이 함께 필요한 값에 사용합니다. toLocalDate·toLocalTime으로 각각 분리할 수도 있습니다.

시간대 정보가 없으므로 같은 값이라도 나라마다 실제 시점이 다릅니다. 글로벌 서비스의 시각 기록에는 Instant나 ZonedDateTime으로 기준을 명확히 합니다.', 'LocalDateTime time = LocalDateTime.of(2026, 7, 23, 10, 30);
System.out.println(time.getHour());', 2, 1, 1, NULL),
  (2403, 24, 'DateTimeFormatter', 'DateTimeFormatter는 날짜·시간을 문자열로 표시하거나 문자열을 날짜 객체로 변환합니다. 패턴의 대소문자는 의미가 다르므로 `MM`과 `mm`을 구분합니다.', 'DateTimeFormatter는 날짜·시간을 문자열로 표시하거나 문자열을 날짜 객체로 변환합니다. 패턴의 대소문자는 의미가 다르므로 `MM`과 `mm`을 구분합니다.
```java
DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
System.out.println(LocalDate.of(2026, 7, 23).format(formatter));
```

```console
2026-07-23
```

MM은 월, mm은 분을 의미하므로 바꿔 쓰면 값이 뒤섞인 문자열이 만들어집니다. HH는 24시간제, hh는 12시간제 시각입니다.

parse로 문자열을 날짜로 변환할 때 형식이 맞지 않으면 DateTimeParseException이 발생합니다. 사용자 입력을 변환할 때는 예외 처리를 함께 둡니다.', 'DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
System.out.println(LocalDate.of(2026, 7, 23).format(formatter));', 3, 1, 1, NULL),
  (2404, 24, 'StringBuilder', 'StringBuilder는 문자열을 여러 번 이어 붙일 때 내부 버퍼를 사용해 불필요한 String 객체 생성을 줄입니다. append·insert·delete로 내용을 변경합니다.', 'StringBuilder는 문자열을 여러 번 이어 붙일 때 내부 버퍼를 사용해 불필요한 String 객체 생성을 줄입니다. append·insert·delete로 내용을 변경합니다.
```java
StringBuilder builder = new StringBuilder("Java");
builder.append(" ").append("Gold");
System.out.println(builder);
```

```console
Java Gold
```

String의 + 연결은 반복문 안에서 매번 새 객체를 만들지만 StringBuilder는 내부 버퍼에 이어 붙입니다. 반복 횟수가 많을수록 차이가 커집니다.

최종 문자열은 toString()으로 얻습니다. append는 자기 자신을 반환하므로 예제처럼 여러 append를 연결해 쓸 수 있습니다.', 'StringBuilder builder = new StringBuilder("Java");
builder.append(" ").append("Gold");
System.out.println(builder);', 4, 1, 1, NULL),
  (2405, 24, '정규표현식', '정규표현식은 문자열 형식과 패턴을 검사합니다. 숫자·이메일·아이디 형식 검증에 유용하지만 실제 업무 규칙은 길이와 허용 범위까지 함께 확인해야 합니다.', '정규표현식은 문자열 형식과 패턴을 검사합니다. 숫자·이메일·아이디 형식 검증에 유용하지만 실제 업무 규칙은 길이와 허용 범위까지 함께 확인해야 합니다.
```java
boolean valid = "12345".matches("\\\\d+");
System.out.println(valid);
```

```console
true
```

`\\\\d`는 숫자 하나, `+`는 1회 이상 반복, `{2,4}`는 2~4회 반복을 뜻합니다. 자주 쓰는 패턴 몇 가지만 익혀도 입력 형식 검증 대부분을 처리할 수 있습니다.

matches는 문자열 전체가 패턴과 일치해야 true입니다. 복잡한 규칙을 정규표현식 하나에 몰아넣기보다 길이 검사 같은 단순 조건과 나누면 읽기 쉽습니다.', 'boolean valid = "12345".matches("\\\\d+");
System.out.println(valid);', 5, 1, 1, NULL),
  (2406, 24, 'Optional', 'Optional은 값이 없을 수 있음을 타입으로 표현합니다. 무조건 get을 호출하기보다 orElse·orElseGet·map을 사용해 값이 없는 경우를 처리합니다.', 'Optional은 값이 없을 수 있음을 타입으로 표현합니다. 무조건 get을 호출하기보다 orElse·orElseGet·map을 사용해 값이 없는 경우를 처리합니다.
```java
Optional<String> value = Optional.empty();
System.out.println(value.orElse("없음"));
```

```console
없음
```

값이 없는데 get을 호출하면 예외가 발생하므로 확인 없는 get은 피합니다. isPresent 확인 후 get을 쓰기보다 orElse 계열이 간결합니다.

Optional은 주로 메서드 반환형으로 사용해 ‘결과가 없을 수 있음’을 호출자에게 알립니다. 필드나 매개변수 타입으로는 사용하지 않는 것이 일반적인 관례입니다.', 'Optional<String> value = Optional.empty();
System.out.println(value.orElse("없음"));', 6, 1, 1, NULL),
  (2407, 24, 'BigDecimal', 'BigDecimal은 금액처럼 정확한 십진 계산이 필요한 경우 사용합니다. double에서 만들기보다 문자열 생성자를 사용하고, 비교에는 equals보다 compareTo가 적절한 경우가 많습니다.', 'BigDecimal은 금액처럼 정확한 십진 계산이 필요한 경우 사용합니다. double에서 만들기보다 문자열 생성자를 사용하고, 비교에는 equals보다 compareTo가 적절한 경우가 많습니다.
```java
BigDecimal total = new BigDecimal("0.1").add(new BigDecimal("0.2"));
System.out.println(total);
```

```console
0.3
```

new BigDecimal(0.1)처럼 double을 직접 전달하면 이미 오차가 포함된 값이 저장됩니다. 반드시 문자열 "0.1"로 생성해야 정확한 값이 됩니다.

equals는 값과 소수 자릿수까지 비교하므로 0.10과 0.1을 다르다고 판단합니다. 크기 비교에는 compareTo를 사용하고, 나눗셈에는 반올림 방식 지정이 필요할 수 있습니다.', 'BigDecimal total = new BigDecimal("0.1").add(new BigDecimal("0.2"));
System.out.println(total);', 7, 1, 1, NULL),
  (2408, 24, 'Math와 Random', 'Math는 절댓값·최댓값·반올림 같은 수학 기능을 제공합니다. Random이나 ThreadLocalRandom으로 범위 안의 난수를 만들 수 있습니다.', 'Math는 절댓값·최댓값·반올림 같은 수학 기능을 제공합니다. Random이나 ThreadLocalRandom으로 범위 안의 난수를 만들 수 있습니다.
```java
System.out.println(Math.max(10, 20));
System.out.println(Math.abs(-5));
```

```console
20
5
```

Math.round는 반올림, Math.floor는 내림, Math.ceil은 올림입니다. Math.pow와 Math.sqrt로 거듭제곱과 제곱근도 계산할 수 있습니다.

Random의 nextInt(6)은 0부터 5까지의 난수를 만들므로 주사위라면 1을 더해야 합니다. 범위 계산에서 1 차이 실수가 흔하니 경계값을 직접 확인합니다.', 'System.out.println(Math.max(10, 20));
System.out.println(Math.abs(-5));', 8, 1, 1, NULL),
  (2409, 24, 'Objects', 'Objects 유틸리티는 null 안전 비교, 필수값 검사, 해시 계산 등을 제공합니다. `requireNonNull`은 필수값이 null일 때 즉시 예외를 발생시킵니다.', 'Objects 유틸리티는 null 안전 비교, 필수값 검사, 해시 계산 등을 제공합니다. `requireNonNull`은 필수값이 null일 때 즉시 예외를 발생시킵니다.
```java
System.out.println(Objects.equals(null, null));
System.out.println(Objects.equals("A", null));
```

```console
true
false
```

Objects.equals는 두 값이 모두 null이어도 예외 없이 비교합니다. a.equals(b)에서 a가 null이면 예외가 발생하는 문제를 피할 수 있습니다.

requireNonNull은 생성자나 메서드 시작에서 필수 인자를 검증할 때 사용합니다. 잘못된 값을 일찍 실패시켜야 원인 추적이 쉬워집니다.', 'System.out.println(Objects.equals(null, null));
System.out.println(Objects.equals("A", null));', 9, 1, 1, NULL),
  (2410, 24, '유틸리티 종합', '날짜·금액·문자열 입력은 각각 적합한 전용 타입으로 변환한 뒤 검증합니다. 화면 표시 형식과 내부 계산 타입을 분리하면 오류를 줄일 수 있습니다.', '날짜·금액·문자열 입력은 각각 적합한 전용 타입으로 변환한 뒤 검증합니다. 화면 표시 형식과 내부 계산 타입을 분리하면 오류를 줄일 수 있습니다.
```java
LocalDate start = LocalDate.of(2026, 7, 23);
LocalDate end = start.plusDays(7);
long days = ChronoUnit.DAYS.between(start, end);
System.out.println(days);
```

```console
7
```

ChronoUnit.DAYS.between은 시작일을 포함하고 종료일을 제외한 일수를 계산합니다. D-day 계산이라면 이 기준이 요구사항과 맞는지 확인합니다.

사용자 입력은 문자열로 들어오므로 날짜는 LocalDate, 금액은 BigDecimal로 변환한 뒤 검증하고 계산합니다. 출력할 때만 다시 형식화된 문자열로 바꿉니다.', 'LocalDate start = LocalDate.of(2026, 7, 23);
LocalDate end = start.plusDays(7);
long days = ChronoUnit.DAYS.between(start, end);
System.out.println(days);', 10, 1, 1, NULL),
  (2501, 25, '요구사항 분석', '요구사항 분석은 프로그램이 제공할 기능, 입력값, 출력 결과, 예외 상황을 구현 전에 정리하는 과정입니다. 기능을 등록·조회·수정·삭제처럼 작은 단위로 나누고 각 기능의 성공 조건을 명확하게 작성합니다.', '요구사항 분석은 프로그램이 제공할 기능, 입력값, 출력 결과, 예외 상황을 구현 전에 정리하는 과정입니다. 기능을 등록·조회·수정·삭제처럼 작은 단위로 나누고 각 기능의 성공 조건을 명확하게 작성합니다.

경계값과 잘못된 입력까지 미리 정하면 구현 중 조건이 빠지는 일을 줄일 수 있습니다.
```java
record Requirement(String input, String output) { }
Requirement requirement = new Requirement("점수 0~100", "통과 여부");
System.out.println(requirement.output());
```

```console
통과 여부
```', 'record Requirement(String input, String output) { }
Requirement requirement = new Requirement("점수 0~100", "통과 여부");
System.out.println(requirement.output());', 1, 1, 1, NULL),
  (2502, 25, '도메인 모델링', '도메인 모델링은 프로그램에서 다루는 학생·강의·주문 같은 개념을 클래스로 표현하는 과정입니다. 각 클래스가 가져야 할 상태와 책임, 객체 사이의 관계를 정합니다.', '도메인 모델링은 프로그램에서 다루는 학생·강의·주문 같은 개념을 클래스로 표현하는 과정입니다. 각 클래스가 가져야 할 상태와 책임, 객체 사이의 관계를 정합니다.

화면에 보이는 모든 값을 한 클래스에 넣기보다 실제 업무 개념을 기준으로 나눕니다.
```java
class Course {
    private final String title;
    Course(String title) { this.title = title; }
    String title() { return title; }
}
System.out.println(new Course("Java").title());
```

```console
Java
```', 'class Course {
    private final String title;
    Course(String title) { this.title = title; }
    String title() { return title; }
}
System.out.println(new Course("Java").title());', 2, 1, 1, NULL),
  (2503, 25, '메뉴 흐름', '콘솔 메뉴는 반복문으로 계속 표시하고 switch로 선택 기능을 실행합니다. 종료 번호를 선택하면 반복을 끝내고, 범위 밖 입력에는 오류 안내를 제공합니다.', '콘솔 메뉴는 반복문으로 계속 표시하고 switch로 선택 기능을 실행합니다. 종료 번호를 선택하면 반복을 끝내고, 범위 밖 입력에는 오류 안내를 제공합니다.
```java
int menu = 2;
switch (menu) {
    case 1 -> System.out.println("등록");
    case 2 -> System.out.println("조회");
    default -> System.out.println("잘못된 메뉴");
}
```

```console
조회
```

메뉴 반복은 while(true)와 종료 case의 break 조합 또는 실행 여부 boolean 변수로 구현합니다. 종료 조건을 한곳에 모으면 흐름이 명확해집니다.

숫자 입력 자리에 문자가 들어오는 경우처럼 잘못된 입력에도 프로그램이 죽지 않고 다시 안내하도록 처리합니다. default 분기가 안내 메시지를 담당합니다.', 'int menu = 2;
switch (menu) {
    case 1 -> System.out.println("등록");
    case 2 -> System.out.println("조회");
    default -> System.out.println("잘못된 메뉴");
}', 3, 1, 1, NULL),
  (2504, 25, '데이터 저장', '작은 콘솔 프로젝트에서는 List나 Map을 메모리 저장소로 사용할 수 있습니다. 고유 번호로 빠르게 찾으려면 Map, 입력 순서 목록이 중요하면 List가 적합합니다.', '작은 콘솔 프로젝트에서는 List나 Map을 메모리 저장소로 사용할 수 있습니다. 고유 번호로 빠르게 찾으려면 Map, 입력 순서 목록이 중요하면 List가 적합합니다.
```java
Map<Long, String> courses = new HashMap<>();
courses.put(1L, "Java");
System.out.println(courses.get(1L));
```

```console
Java
```

고유 번호를 키로 쓰면 수정·삭제 대상 찾기가 간단해집니다. 번호는 등록할 때마다 1씩 증가시키는 변수로 관리할 수 있습니다.

메모리 저장소는 프로그램 종료와 함께 사라진다는 한계를 인식하고 설계합니다. 저장소 접근 코드를 한곳에 모아 두면 나중에 파일이나 DB로 바꾸기 쉽습니다.', 'Map<Long, String> courses = new HashMap<>();
courses.put(1L, "Java");
System.out.println(courses.get(1L));', 4, 1, 1, NULL),
  (2505, 25, '등록 기능', '등록 기능은 입력값 검증, 중복 확인, 객체 생성, 저장 순서로 구현합니다. 실패했는데 일부 데이터만 저장되지 않도록 검증을 먼저 마친 뒤 상태를 변경합니다.', '등록 기능은 입력값 검증, 중복 확인, 객체 생성, 저장 순서로 구현합니다. 실패했는데 일부 데이터만 저장되지 않도록 검증을 먼저 마친 뒤 상태를 변경합니다.
```java
static boolean register(Map<String, Integer> scores, String name, int score) {
    if (name.isBlank() || score < 0 || score > 100 || scores.containsKey(name)) return false;
    scores.put(name, score);
    return true;
}
```

검증에 실패했을 때 어떤 조건 때문인지 구분해 안내하면 사용자가 바로잡기 쉽습니다. boolean 대신 실패 사유를 반환하는 설계로 확장할 수도 있습니다.

검증을 모두 통과한 뒤에만 상태를 바꾼다는 원칙이 핵심입니다. 검증 중간에 일부 데이터가 먼저 저장되면 실패 시 어중간한 상태가 남습니다.', 'static boolean register(Map<String, Integer> scores, String name, int score) {
    if (name.isBlank() || score < 0 || score > 100 || scores.containsKey(name)) return false;
    scores.put(name, score);
    return true;
}', 5, 1, 1, NULL),
  (2506, 25, '조회·검색 기능', '조회 기능은 전체 목록과 조건 검색을 구분합니다. 검색 결과가 없을 수 있으므로 Optional이나 빈 목록으로 결과를 명확하게 표현합니다.', '조회 기능은 전체 목록과 조건 검색을 구분합니다. 검색 결과가 없을 수 있으므로 Optional이나 빈 목록으로 결과를 명확하게 표현합니다.
```java
List<String> result = List.of("Java", "SQL", "JavaScript").stream()
        .filter(title -> title.contains("Java")).toList();
System.out.println(result);
```

```console
[Java, JavaScript]
```

검색 결과가 없는 것은 오류가 아니라 정상 결과 중 하나입니다. 빈 목록이면 ‘결과 없음’ 안내를 출력하고 프로그램은 계속 진행합니다.

대소문자 구분 없이 찾으려면 양쪽을 toLowerCase로 맞춰 비교합니다. 부분 일치는 contains, 정확한 일치는 equals로 요구사항에 맞게 선택합니다.', 'List<String> result = List.of("Java", "SQL", "JavaScript").stream()
        .filter(title -> title.contains("Java")).toList();
System.out.println(result);', 6, 1, 1, NULL),
  (2507, 25, '수정·삭제 기능', '수정과 삭제는 대상 존재 여부와 권한을 먼저 확인합니다. 수정 가능한 값만 변경하고 삭제가 다른 데이터에 미치는 영향도 함께 고려합니다.', '수정과 삭제는 대상 존재 여부와 권한을 먼저 확인합니다. 수정 가능한 값만 변경하고 삭제가 다른 데이터에 미치는 영향도 함께 고려합니다.
```java
Map<Long, String> courses = new HashMap<>();
courses.put(1L, "Java 기초");
courses.replace(1L, "Java 심화");
System.out.println(courses.get(1L));
```

```console
Java 심화
```

존재하지 않는 번호에 대한 수정·삭제 요청은 예외 대신 안내 메시지로 처리하는 편이 콘솔 프로그램에 자연스럽습니다. containsKey로 먼저 확인합니다.

삭제는 되돌리기 어렵다는 점을 고려해 정말 삭제할지 한 번 더 확인하는 절차를 둘 수 있습니다. 연관 데이터가 있다면 함께 정리할지도 결정해야 합니다.', 'Map<Long, String> courses = new HashMap<>();
courses.put(1L, "Java 기초");
courses.replace(1L, "Java 심화");
System.out.println(courses.get(1L));', 7, 1, 1, NULL),
  (2508, 25, '파일 영속화', '메모리 데이터는 프로그램이 끝나면 사라지므로 파일에 저장해 다음 실행에서 다시 불러올 수 있습니다. 한 줄의 필드 구분 규칙과 문자 인코딩을 정하고 저장·복원 양쪽에서 같은 규칙을 사용합니다.', '메모리 데이터는 프로그램이 끝나면 사라지므로 파일에 저장해 다음 실행에서 다시 불러올 수 있습니다. 한 줄의 필드 구분 규칙과 문자 인코딩을 정하고 저장·복원 양쪽에서 같은 규칙을 사용합니다.
```java
List<String> lines = List.of("1,Java", "2,SQL");
Files.write(Path.of("courses.csv"), lines, StandardCharsets.UTF_8);
```

쉼표로 구분하는 형식이라면 데이터 안에 쉼표가 들어올 경우의 규칙이 필요합니다. 저장 형식은 단순할수록 복원 코드도 단순해집니다.

프로그램 시작 시 파일이 없으면 빈 저장소로 시작하고, 종료나 변경 시점에 저장합니다. 읽기·쓰기 모두 IOException 처리가 필요합니다.', 'List<String> lines = List.of("1,Java", "2,SQL");
Files.write(Path.of("courses.csv"), lines, StandardCharsets.UTF_8);', 8, 1, 1, NULL),
  (2509, 25, '테스트·디버깅', '테스트는 정상값뿐 아니라 빈 값, 최솟값·최댓값, 중복값, 존재하지 않는 대상도 확인합니다. 실패한 입력과 기대 결과를 고정하면 수정 후 같은 문제가 다시 생겼는지 검증할 수 있습니다.', '테스트는 정상값뿐 아니라 빈 값, 최솟값·최댓값, 중복값, 존재하지 않는 대상도 확인합니다. 실패한 입력과 기대 결과를 고정하면 수정 후 같은 문제가 다시 생겼는지 검증할 수 있습니다.
```java
static boolean isValidScore(int score) { return score >= 0 && score <= 100; }
System.out.println(isValidScore(0));
System.out.println(isValidScore(101));
```

```console
true
false
```

버그를 수정한 뒤에는 원래 문제가 사라졌는지와 함께 기존에 되던 기능이 깨지지 않았는지도 확인합니다. 확인용 입력 목록을 만들어 두면 반복 검증이 빨라집니다.

출력문으로 중간값을 확인하는 것이 가장 기본적인 디버깅입니다. 예상값과 실제값이 갈라지는 지점을 찾으면 원인은 그 직전 코드에 있습니다.', 'static boolean isValidScore(int score) { return score >= 0 && score <= 100; }
System.out.println(isValidScore(0));
System.out.println(isValidScore(101));', 9, 1, 1, NULL),
  (2510, 25, '최종 미니 프로젝트', '최종 프로젝트는 요구사항, 모델, 저장소, 서비스, 입출력 역할을 나누어 완성합니다. 먼저 등록·조회 같은 핵심 흐름을 만들고 검증·수정·파일 저장을 단계적으로 추가합니다.', '최종 프로젝트는 요구사항, 모델, 저장소, 서비스, 입출력 역할을 나누어 완성합니다. 먼저 등록·조회 같은 핵심 흐름을 만들고 검증·수정·파일 저장을 단계적으로 추가합니다.

한 메서드에 모든 기능을 넣지 않고 기능별로 분리하며, 각 기능에 정상·실패 예제를 준비해 실행 결과를 확인합니다.
```java
Map<String, Integer> scores = new HashMap<>();
register(scores, "민수", 85);
double average = scores.values().stream().mapToInt(Integer::intValue).average().orElse(0);
System.out.println(scores + ", 평균=" + average);
```

```console
{민수=85}, 평균=85.0
```', 'Map<String, Integer> scores = new HashMap<>();
register(scores, "민수", 85);
double average = scores.values().stream().mapToInt(Integer::intValue).average().orElse(0);
System.out.println(scores + ", 평균=" + average);', 10, 1, 1, NULL)
ON DUPLICATE KEY UPDATE node_id = VALUES(node_id), title = VALUES(title), summary = VALUES(summary), content = VALUES(content), example_code = VALUES(example_code), sort_order = VALUES(sort_order), is_active = VALUES(is_active), required_for_completion = VALUES(required_for_completion), created_by = VALUES(created_by), updated_at = CURRENT_TIMESTAMP;

INSERT INTO practice_problems (problem_id, subject_id, node_id, lesson_id, problem_type, question, answer_text, explanation, difficulty_code, created_by, is_active)
VALUES
  (10101, @java_subject_id, 1, 101, 'MULTIPLE_CHOICE', 'JVM의 역할로 옳은 것은?', '바이트코드를 실제 컴퓨터에서 실행한다', '컴파일은 컴파일러(javac)의 역할이고, JVM은 바이트코드를 실행하는 가상 실행 환경입니다.', 'BRONZE', NULL, 1),
  (10102, @java_subject_id, 1, 101, 'MULTIPLE_CHOICE', 'Java 프로그램의 실행 흐름으로 올바른 것은?', '소스 작성 → 컴파일 → 바이트코드 생성 → JVM 실행', '사람이 작성한 소스를 컴파일러가 바이트코드로 바꾸고, JVM이 이를 실행합니다.', 'BRONZE', NULL, 1),
  (10103, @java_subject_id, 1, 101, 'MULTIPLE_CHOICE', '다음 코드를 컴파일하고 실행한 결과는?
```java
public class Hello {
    public static void main(String[] args) {
        System.out.println("Hello");
    }
}
```', 'Hello', 'println은 큰따옴표 안의 내용만 출력합니다. 따옴표 자체는 출력되지 않습니다.', 'BRONZE', NULL, 1),
  (10104, @java_subject_id, 1, 101, 'MULTIPLE_CHOICE', '`Hello.java`를 컴파일하면 만들어지는 파일은?', 'Hello.class', '컴파일 결과는 바이트코드가 담긴 `.class` 파일입니다.', 'BRONZE', NULL, 1),
  (10105, @java_subject_id, 1, 101, 'SHORT_ANSWER', '''Write Once, Run Anywhere''가 성립하려면 각 운영체제에 무엇이 설치되어 있어야 하는지 쓰시오.', 'JVM', '운영체제마다 그에 맞는 JVM이 있으면 같은 바이트코드를 실행할 수 있습니다.', 'BRONZE', NULL, 1),
  (10106, @java_subject_id, 1, 101, 'SHORT_ANSWER', '사람이 읽을 수 있는 `.java` 소스 코드를 JVM이 이해할 수 있는 바이트코드로 바꾸는 프로그램을 무엇이라 하는지 쓰시오.', '컴파일러(javac)', '컴파일러가 소스 코드를 바이트코드로 변환합니다.', 'BRONZE', NULL, 1),
  (10107, @java_subject_id, 1, 101, 'FILL_BLANK', '빈칸에 들어갈 단어를 쓰시오.
```java
public class Hello {
    public static void ____(String[] args) { }
}
```', 'main', '콘솔 Java 프로그램은 main 메서드에서 시작합니다.', 'BRONZE', NULL, 1),
  (10108, @java_subject_id, 1, 101, 'CODE_OUTPUT', '다음 문장의 출력 결과를 쓰시오.
```java
System.out.println("Java");
```', 'Java', '큰따옴표 안의 텍스트가 그대로 출력됩니다.', 'BRONZE', NULL, 1),
  (10109, @java_subject_id, 1, 101, 'FILL_BLANK', '소스 파일의 확장자는 `____`, 컴파일 결과 파일의 확장자는 `____`이다. 순서대로 쓰시오.', '.java, .class', '소스는 `.java`, 컴파일된 바이트코드는 `.class`를 사용합니다.', 'BRONZE', NULL, 1),
  (10110, @java_subject_id, 1, 101, 'CODE_SHORT', '콘솔에 `Hello`를 출력하고 줄바꿈하는 실행문 한 줄을 작성하시오.', '`System.out.println("Hello");`', 'println은 출력 후 줄을 바꿉니다.', 'BRONZE', NULL, 1),
  (10201, @java_subject_id, 1, 102, 'MULTIPLE_CHOICE', 'JDK에 대한 설명으로 옳은 것은?', '개발자가 코드를 만들기 위한 도구 묶음으로 컴파일러를 포함한다', 'JDK는 개발 도구 묶음, JRE는 실행 환경 묶음, JVM은 실행 핵심 구성 요소입니다.', 'BRONZE', NULL, 1),
  (10202, @java_subject_id, 1, 102, 'MULTIPLE_CHOICE', 'JDK·JRE·JVM의 포함 관계로 올바른 것은?', 'JDK ⊃ JRE ⊃ JVM', '개발 묶음(JDK) 안에 실행 묶음(JRE)이 있고, 그 안에서 JVM이 바이트코드를 실행합니다.', 'BRONZE', NULL, 1),
  (10203, @java_subject_id, 1, 102, 'MULTIPLE_CHOICE', '프로젝트에서 Java 소스 파일을 두는 폴더의 관례적인 이름은?', 'src', '보통 `src` 폴더 아래에 소스 파일을 둡니다.', 'BRONZE', NULL, 1),
  (10204, @java_subject_id, 1, 102, 'MULTIPLE_CHOICE', 'IDE에서 실행 버튼을 눌렀을 때 내부에서 일어나는 순서로 올바른 것은?', '컴파일 → 실행', 'IDE도 내부에서는 JDK 명령으로 컴파일한 뒤 실행하는 같은 순서로 동작합니다.', 'BRONZE', NULL, 1),
  (10205, @java_subject_id, 1, 102, 'SHORT_ANSWER', '개발은 하지 않고 만들어진 Java 프로그램을 실행만 하려는 사용자에게 필요한 묶음의 이름을 쓰시오.', 'JRE', 'JRE는 프로그램 실행을 위한 묶음입니다.', 'BRONZE', NULL, 1),
  (10206, @java_subject_id, 1, 102, 'SHORT_ANSWER', 'JRE 안에서 바이트코드를 실제로 실행하는 핵심 구성 요소의 이름을 쓰시오.', 'JVM', 'JVM이 바이트코드 실행을 담당합니다.', 'BRONZE', NULL, 1),
  (10207, @java_subject_id, 1, 102, 'FILL_BLANK', '빈칸에 들어갈 폴더 이름을 쓰시오.
```text
프로젝트 └─ ____ └─ Hello.java
```', 'src', '소스 파일은 관례적으로 src 폴더에 둡니다.', 'BRONZE', NULL, 1),
  (10208, @java_subject_id, 1, 102, 'FILL_BLANK', 'JDK에 포함된, Java 소스 코드를 컴파일하는 명령어(도구)의 이름을 쓰시오.', 'javac', 'javac가 `.java`를 `.class`로 컴파일합니다.', 'BRONZE', NULL, 1),
  (10209, @java_subject_id, 1, 102, 'CODE_OUTPUT', '`Hello.java`를 한 번도 컴파일하지 않은 상태에서 `java Hello`를 실행하면 어떻게 되는지 쓰시오.', '클래스(바이트코드) 파일을 찾지 못해 오류가 발생한다', '실행 대상인 `.class` 파일이 없으므로 실행할 수 없습니다.', 'BRONZE', NULL, 1),
  (10210, @java_subject_id, 1, 102, 'CODE_SHORT', '`Hello.java`를 컴파일하는 명령어 한 줄을 작성하시오.', '`javac Hello.java`', 'javac 뒤에 소스 파일 이름을 확장자까지 적습니다.', 'BRONZE', NULL, 1),
  (10301, @java_subject_id, 1, 103, 'MULTIPLE_CHOICE', 'public 최상위 클래스와 소스 파일 이름의 관계로 옳은 것은?', '파일 이름은 public 클래스 이름과 같아야 한다', 'public 최상위 클래스가 있으면 파일 이름이 그 클래스 이름과 일치해야 합니다.', 'BRONZE', NULL, 1),
  (10302, @java_subject_id, 1, 103, 'MULTIPLE_CHOICE', '클래스 이름 규칙(관례)에 가장 알맞은 것은?', 'StudentInfo', '클래스 이름은 대문자로 시작하는 PascalCase를 권장합니다.', 'BRONZE', NULL, 1),
  (10303, @java_subject_id, 1, 103, 'MULTIPLE_CHOICE', '`Hello.java` 파일 안에 `public class Welcome { }`만 작성하고 컴파일하면?', '컴파일 오류가 발생한다', 'public 최상위 클래스 이름(Welcome)과 파일 이름(Hello)이 달라 컴파일 오류입니다.', 'BRONZE', NULL, 1),
  (10304, @java_subject_id, 1, 103, 'MULTIPLE_CHOICE', '어디서나 접근할 수 있는 클래스를 선언하려 한다. 빈칸에 들어갈 키워드는?
```java
____ class Student { }
```', 'public', 'public은 접근 범위를 넓게 여는 키워드입니다.', 'BRONZE', NULL, 1),
  (10305, @java_subject_id, 1, 103, 'SHORT_ANSWER', '한 소스 파일에 둘 수 있는 public 최상위 클래스는 최대 몇 개인지 쓰시오.', '1개', '여러 클래스를 둘 수는 있지만 public 최상위 클래스는 하나만 가능합니다.', 'BRONZE', NULL, 1),
  (10306, @java_subject_id, 1, 103, 'SHORT_ANSWER', '클래스 본문을 구성하는 두 가지 요소로, 상태를 저장하는 것과 기능을 수행하는 것의 이름을 각각 쓰시오.', '필드, 메서드', '필드는 데이터(상태), 메서드는 기능(동작)을 담당합니다.', 'BRONZE', NULL, 1),
  (10307, @java_subject_id, 1, 103, 'FILL_BLANK', '빈칸에 들어갈 키워드를 쓰시오.
```java
public ____ Car { }
```', 'class', '클래스 선언은 class 키워드로 시작합니다.', 'BRONZE', NULL, 1),
  (10308, @java_subject_id, 1, 103, 'CODE_SHORT', '이름이 `Book`인 public 클래스를 빈 본문으로 선언하는 코드 한 줄을 작성하시오.', '`public class Book { }`', '클래스 이름은 대문자로 시작하고 본문은 중괄호로 감쌉니다.', 'BRONZE', NULL, 1),
  (10309, @java_subject_id, 1, 103, 'CODE_OUTPUT', '`public class Score`가 저장되어야 하는 소스 파일의 이름을 확장자까지 쓰시오.', 'Score.java', 'public 클래스 이름과 파일 이름이 같아야 합니다.', 'BRONZE', NULL, 1),
  (10310, @java_subject_id, 1, 103, 'FILL_BLANK', '클래스의 본문 범위를 감싸는 한 쌍의 기호를 쓰시오.', '중괄호 `{ }`', '클래스·메서드의 본문은 중괄호로 범위를 정합니다.', 'BRONZE', NULL, 1),
  (10401, @java_subject_id, 1, 104, 'MULTIPLE_CHOICE', 'main 메서드에 `static`이 붙는 이유로 옳은 것은?', '객체를 만들지 않고도 실행을 시작할 수 있어야 하기 때문에', 'static이 있어야 Java 실행기가 객체 생성 없이 시작점을 호출할 수 있습니다.', 'BRONZE', NULL, 1),
  (10402, @java_subject_id, 1, 104, 'MULTIPLE_CHOICE', 'main 선언에서 `void`의 의미는?', '반환값이 없다', 'void는 호출한 곳에 돌려주는 값이 없다는 뜻입니다.', 'BRONZE', NULL, 1),
  (10403, @java_subject_id, 1, 104, 'MULTIPLE_CHOICE', '`java Hello a b c`로 실행했을 때 다음 코드의 출력은?
```java
public static void main(String[] args) {
    System.out.println(args.length);
}
```', '3', '실행 명령에서 전달한 인자 a, b, c 세 개가 args 배열에 담깁니다.', 'BRONZE', NULL, 1),
  (10404, @java_subject_id, 1, 104, 'MULTIPLE_CHOICE', '`String[] args`에서 args의 자료형은?', '문자열 배열', 'args는 실행 시 전달된 문자열들을 담는 배열입니다.', 'BRONZE', NULL, 1),
  (10405, @java_subject_id, 1, 104, 'SHORT_ANSWER', '콘솔 Java 프로그램이 실행을 시작하는 메서드의 이름을 쓰시오.', 'main', '실행 대상 클래스의 main 메서드가 시작점입니다.', 'BRONZE', NULL, 1),
  (10406, @java_subject_id, 1, 104, 'SHORT_ANSWER', 'args에 저장된 값은 모두 문자열이다. 이 값을 숫자로 계산에 사용하려면 무엇이 필요한지 쓰시오.', '숫자로의 변환', 'args의 값은 String이므로 숫자로 쓰려면 변환해야 합니다.', 'BRONZE', NULL, 1),
  (10407, @java_subject_id, 1, 104, 'FILL_BLANK', '빈칸에 들어갈 키워드를 쓰시오.
```java
public static ____ main(String[] args) { }
```', 'void', 'main은 반환값이 없으므로 void로 선언합니다.', 'BRONZE', NULL, 1),
  (10408, @java_subject_id, 1, 104, 'FILL_BLANK', '빈칸에 들어갈 키워드를 쓰시오.
```java
public ____ void main(String[] args) { }
```', 'static', '객체 없이 시작할 수 있도록 static을 붙입니다.', 'BRONZE', NULL, 1),
  (10409, @java_subject_id, 1, 104, 'CODE_SHORT', 'main 메서드의 선언부(본문 제외)를 한 줄로 작성하시오.', '`public static void main(String[] args)`', '형태를 임의로 바꾸면 실행기가 시작점을 찾지 못합니다.', 'BRONZE', NULL, 1),
  (10410, @java_subject_id, 1, 104, 'CODE_OUTPUT', '메서드 이름을 `Main`(대문자)으로 바꾸어 컴파일한 뒤 이 클래스를 실행하면 어떻게 되는지 쓰시오.', '시작점(main)을 찾지 못해 실행 오류가 발생한다', 'Java는 대소문자를 구분하므로 Main은 시작점으로 인정되지 않습니다.', 'BRONZE', NULL, 1),
  (10501, @java_subject_id, 1, 105, 'MULTIPLE_CHOICE', '끝에 세미콜론을 붙여야 하는 것은?', '변수 선언·대입·메서드 호출 같은 실행문', '실행문에는 세미콜론이 필요하고, 중괄호로 끝나는 블록 선언에는 붙이지 않습니다.', 'BRONZE', NULL, 1),
  (10502, @java_subject_id, 1, 105, 'MULTIPLE_CHOICE', '세미콜론 누락 오류에 대한 설명으로 옳은 것은?', '다음 줄에서 발견되는 것처럼 표시될 수 있다', '컴파일러가 다음 줄을 읽고 나서야 문제를 인식하는 경우가 있습니다.', 'BRONZE', NULL, 1),
  (10503, @java_subject_id, 1, 105, 'MULTIPLE_CHOICE', '다음 코드를 컴파일하면?
```java
int a = 10
System.out.println(a);
```', '컴파일 오류', '첫 줄 끝에 세미콜론이 없어 컴파일 단계에서 오류가 발생합니다.', 'BRONZE', NULL, 1),
  (10504, @java_subject_id, 1, 105, 'MULTIPLE_CHOICE', '세미콜론이 필요 **없는** 위치는?', '`if (x > 0) { ... }`의 닫는 중괄호 뒤', 'if 블록은 중괄호로 끝나는 구조라 세미콜론을 붙이지 않습니다.', 'BRONZE', NULL, 1),
  (10505, @java_subject_id, 1, 105, 'SHORT_ANSWER', '하나의 실행문이 끝났음을 나타내는 기호를 쓰시오.', '세미콜론(;)', '실행문의 끝은 세미콜론으로 표시합니다.', 'BRONZE', NULL, 1),
  (10506, @java_subject_id, 1, 105, 'SHORT_ANSWER', '여러 실행문을 같은 줄에 쓰는 것이 가능한지, 일반적으로 권장되는 방식은 무엇인지 쓰시오.', '가능하지만 한 줄에 하나씩 작성하는 것을 권장', '한 줄에 하나씩 쓰면 오류 위치를 찾기 쉽습니다.', 'BRONZE', NULL, 1),
  (10507, @java_subject_id, 1, 105, 'FILL_BLANK', '빈칸에 들어갈 기호를 쓰시오.
```java
System.out.println(10)____
```', ';', '메서드 호출은 실행문이므로 세미콜론으로 끝냅니다.', 'BRONZE', NULL, 1),
  (10508, @java_subject_id, 1, 105, 'CODE_OUTPUT', '다음 코드의 출력 결과를 쓰시오.
```java
int score = 80;
System.out.println(score);
```', '80', '변수에 저장된 값 80이 출력됩니다.', 'BRONZE', NULL, 1),
  (10509, @java_subject_id, 1, 105, 'CODE_SHORT', '정수 변수 `age`를 선언하면서 20을 저장하는 실행문 한 줄을 작성하시오.', '`int age = 20;`', '선언과 초기화를 한 실행문으로 작성하고 세미콜론으로 끝냅니다.', 'BRONZE', NULL, 1),
  (10510, @java_subject_id, 1, 105, 'CODE_OUTPUT', '다음처럼 실행문 두 개를 한 줄에 작성하면 컴파일이 되는지 쓰시오.
```java
int a = 1; int b = 2;
```', '정상 컴파일된다', '각 실행문이 세미콜론으로 구분되면 한 줄에 여러 개를 써도 문법상 문제없습니다.', 'BRONZE', NULL, 1),
  (10601, @java_subject_id, 1, 106, 'MULTIPLE_CHOICE', '`print`와 `println`의 차이로 옳은 것은?', 'println은 출력 후 줄을 바꾼다', '두 메서드의 차이는 출력 후 줄바꿈 여부입니다.', 'BRONZE', NULL, 1),
  (10602, @java_subject_id, 1, 106, 'MULTIPLE_CHOICE', 'printf 서식 중 정수를 출력하는 것은?', '%d', '%d는 정수, %s는 문자열, %.2f는 소수 둘째 자리 실수, %n은 줄바꿈입니다.', 'BRONZE', NULL, 1),
  (10603, @java_subject_id, 1, 106, 'MULTIPLE_CHOICE', '다음 코드의 출력 결과는?
```java
System.out.print("Java ");
System.out.println("Bronze");
System.out.print("Go");
```', 'Java Bronze / Go (두 줄)', 'print는 줄바꿈이 없고 println 뒤에서만 줄이 바뀌므로 `Java Bronze` 다음 줄에 `Go`가 출력됩니다.', 'BRONZE', NULL, 1),
  (10604, @java_subject_id, 1, 106, 'MULTIPLE_CHOICE', '`System.out.printf("%s", "Java");`의 출력 결과는?', 'Java', '%s 자리에 전달값 "Java"가 들어가 출력됩니다.', 'BRONZE', NULL, 1),
  (10605, @java_subject_id, 1, 106, 'SHORT_ANSWER', 'printf에서 운영체제에 맞는 줄바꿈을 넣는 서식 문자를 쓰시오.', '%n', '%n은 실행 환경에 맞는 줄바꿈으로 처리됩니다.', 'BRONZE', NULL, 1),
  (10606, @java_subject_id, 1, 106, 'SHORT_ANSWER', '실수를 소수 둘째 자리까지 표시하는 printf 서식을 쓰시오.', '%.2f', '`%.2f`는 소수점 아래 두 자리까지 출력합니다.', 'BRONZE', NULL, 1),
  (10607, @java_subject_id, 1, 106, 'CODE_OUTPUT', '다음 문장의 출력 결과를 쓰시오.
```java
System.out.printf("점수: %d%n", 90);
```', '점수: 90', '%d 자리에 90이 들어가고 %n으로 줄바꿈합니다.', 'BRONZE', NULL, 1),
  (10608, @java_subject_id, 1, 106, 'FILL_BLANK', '출력 후 줄바꿈까지 하도록 빈칸에 들어갈 메서드 이름을 쓰시오.
```java
System.out.____("안녕");
```', 'println', '줄바꿈이 필요하면 println을 사용합니다.', 'BRONZE', NULL, 1),
  (10609, @java_subject_id, 1, 106, 'CODE_SHORT', '`Hello Java`를 출력하고 줄바꿈하는 문장 한 줄을 작성하시오.', '`System.out.println("Hello Java");`', '텍스트는 큰따옴표로 감싸 println에 전달합니다.', 'BRONZE', NULL, 1),
  (10610, @java_subject_id, 1, 106, 'CODE_OUTPUT', '다음 코드의 출력 결과를 쓰시오.
```java
System.out.print("Java");
System.out.print(17);
```', 'Java17', 'print는 줄바꿈 없이 이어서 출력합니다.', 'BRONZE', NULL, 1),
  (10701, @java_subject_id, 1, 107, 'MULTIPLE_CHOICE', '주석에 대한 설명으로 옳은 것은?', '컴파일러가 실행 코드로 처리하지 않으며 설명을 남길 때 쓴다', '주석은 실행되지 않으며 코드의 이유·제약을 설명할 때 사용합니다.', 'BRONZE', NULL, 1),
  (10702, @java_subject_id, 1, 107, 'MULTIPLE_CHOICE', '좋은 주석에 가장 가까운 것은?', '왜 이 처리가 필요한지 이유를 적는다', '좋은 주석은 ''무엇''의 반복이 아니라 ''왜''를 설명합니다. 코드와 달라진 주석은 혼란을 줍니다.', 'BRONZE', NULL, 1),
  (10703, @java_subject_id, 1, 107, 'MULTIPLE_CHOICE', '다음 코드의 출력 결과는?
```java
// System.out.println(1);
System.out.println(2);
```', '2', '첫 줄은 주석 처리되어 실행되지 않습니다.', 'BRONZE', NULL, 1),
  (10704, @java_subject_id, 1, 107, 'MULTIPLE_CHOICE', '여러 줄 주석을 만드는 기호 쌍은?', '/* ... */', '`/*`로 시작해 `*/`로 끝나는 범위가 여러 줄 주석입니다.', 'BRONZE', NULL, 1),
  (10705, @java_subject_id, 1, 107, 'SHORT_ANSWER', '한 줄 주석을 시작하는 기호를 쓰시오.', '//', '`//` 뒤부터 그 줄 끝까지 주석입니다.', 'BRONZE', NULL, 1),
  (10706, @java_subject_id, 1, 107, 'SHORT_ANSWER', '코드가 수정되었는데 주석을 옛날 내용 그대로 두면 어떤 문제가 생기는지 한 문장으로 쓰시오.', '코드와 다른 설명이 남아 읽는 사람에게 혼란(버그)을 만들 수 있다', '주석은 실제 코드의 의미와 일치하게 유지해야 합니다.', 'BRONZE', NULL, 1),
  (10707, @java_subject_id, 1, 107, 'CODE_OUTPUT', '다음 코드의 출력 결과를 쓰시오.
```java
/* System.out.println("A"); */
System.out.println("B");
```', 'B', '여러 줄 주석 안의 코드는 실행되지 않습니다.', 'BRONZE', NULL, 1),
  (10708, @java_subject_id, 1, 107, 'FILL_BLANK', '빈칸에 들어갈 기호를 쓰시오.
```java
____ 이 줄은 실행되지 않는 설명입니다
```', '//', '한 줄 주석은 //로 시작합니다.', 'BRONZE', NULL, 1),
  (10709, @java_subject_id, 1, 107, 'CODE_SHORT', '`System.out.println("hi");` 문장을 한 줄 주석으로 처리한 코드를 작성하시오.', '`// System.out.println("hi");`', '문장 앞에 //를 붙이면 실행에서 제외됩니다.', 'BRONZE', NULL, 1),
  (10710, @java_subject_id, 1, 107, 'CODE_OUTPUT', '다음 코드의 출력 결과를 쓰시오.
```java
System.out.println(80); // 화면에는 점수만 출력
```', '80', '실행문 뒤의 주석은 출력에 영향을 주지 않습니다.', 'BRONZE', NULL, 1),
  (10801, @java_subject_id, 1, 108, 'MULTIPLE_CHOICE', '들여쓰기에 대한 설명으로 옳은 것은?', '들여쓰기는 실행 규칙은 아니지만 코드가 어느 블록에 속하는지 보여 준다', 'Java에서 블록 범위는 중괄호가 정하고, 들여쓰기는 가독성을 위한 관례입니다.', 'BRONZE', NULL, 1),
  (10802, @java_subject_id, 1, 108, 'MULTIPLE_CHOICE', '닫는 중괄호 `}`의 관례적인 위치는?', '블록을 시작한 문장과 같은 깊이', '시작 문장과 같은 깊이에 두면 블록 범위를 빠르게 구분할 수 있습니다.', 'BRONZE', NULL, 1),
  (10803, @java_subject_id, 1, 108, 'MULTIPLE_CHOICE', '다음 코드의 출력 결과는?
```java
if (true) {
    System.out.println("안쪽");
}
System.out.println("바깥");
```', '안쪽 다음 줄에 바깥', '조건이 true라 블록 안이 실행되고, 블록 밖 문장은 항상 실행됩니다.', 'BRONZE', NULL, 1),
  (10804, @java_subject_id, 1, 108, 'MULTIPLE_CHOICE', '조건문 안에 반복문이 들어가면 반복문 본문의 들여쓰기는?', '한 단계 더 깊게', '중첩 블록이 생길 때마다 한 단계(보통 공백 4칸)씩 더 들여씁니다.', 'BRONZE', NULL, 1),
  (10805, @java_subject_id, 1, 108, 'SHORT_ANSWER', '클래스·메서드·조건문·반복문의 실행 범위를 정하는 기호를 쓰시오.', '중괄호 `{ }`', '블록 범위는 들여쓰기가 아니라 중괄호가 결정합니다.', 'BRONZE', NULL, 1),
  (10806, @java_subject_id, 1, 108, 'SHORT_ANSWER', '중첩 블록이 생길 때 한 단계에 관례적으로 추가하는 공백 수를 쓰시오.', '4칸', '보통 공백 네 칸을 한 단계로 사용합니다.', 'BRONZE', NULL, 1),
  (10807, @java_subject_id, 1, 108, 'CODE_OUTPUT', '다음 코드의 출력 결과를 쓰시오.
```java
if (false) {
    System.out.println("A");
}
System.out.println("B");
```', 'B', '조건이 false라 블록 안은 실행되지 않고 블록 밖 문장만 실행됩니다.', 'BRONZE', NULL, 1),
  (10808, @java_subject_id, 1, 108, 'FILL_BLANK', '여는 중괄호 `{`와 짝을 이루어 블록을 끝내는 기호를 쓰시오.', '}', '중괄호는 반드시 짝을 이뤄야 합니다.', 'BRONZE', NULL, 1),
  (10809, @java_subject_id, 1, 108, 'CODE_SHORT', '`score >= 70`일 때 `통과`를 출력하는 if문을 중괄호를 포함해 한 줄로 작성하시오.', '`if (score >= 70) { System.out.println("통과"); }`', '본문이 한 줄이어도 중괄호를 쓰는 편이 안전합니다.', 'BRONZE', NULL, 1),
  (10810, @java_subject_id, 1, 108, 'CODE_OUTPUT', '들여쓰기를 전혀 하지 않은 코드는 컴파일이 되는지, 어떤 문제가 남는지 쓰시오.', '컴파일은 정상적으로 되지만 읽기 어려워진다', '들여쓰기는 문법이 아니라 가독성 규칙입니다.', 'BRONZE', NULL, 1),
  (10901, @java_subject_id, 1, 109, 'MULTIPLE_CHOICE', '`javac` 명령의 역할은?', '소스를 컴파일해 .class 파일을 만든다', 'javac는 컴파일, java는 실행 명령입니다.', 'BRONZE', NULL, 1),
  (10902, @java_subject_id, 1, 109, 'MULTIPLE_CHOICE', '소스 코드를 수정한 뒤에 대한 설명으로 옳은 것은?', '다시 컴파일해야 수정 내용이 반영된다', '소스 수정만으로는 기존 바이트코드가 바뀌지 않으므로 재컴파일이 필요합니다.', 'BRONZE', NULL, 1),
  (10903, @java_subject_id, 1, 109, 'MULTIPLE_CHOICE', '컴파일된 `Hello` 클래스를 실행하는 올바른 명령은?', 'java Hello', '실행할 때는 확장자 없이 클래스 이름만 적습니다.', 'BRONZE', NULL, 1),
  (10904, @java_subject_id, 1, 109, 'MULTIPLE_CHOICE', '`javac Hello.java`가 성공하면 무엇이 만들어지는가?', 'Hello.class', '컴파일 성공 시 바이트코드 파일 Hello.class가 생성됩니다.', 'BRONZE', NULL, 1),
  (10905, @java_subject_id, 1, 109, 'SHORT_ANSWER', '컴파일에 성공했는데도 실행 결과가 의도와 다르게 나오는 오류를 무엇이라 부르는지 쓰시오.', '논리 오류', '문법은 맞지만 코드의 논리가 잘못된 경우로, 코드를 추적해 원인을 찾아야 합니다.', 'BRONZE', NULL, 1),
  (10906, @java_subject_id, 1, 109, 'SHORT_ANSWER', '`.class` 파일에 담겨 있는, JVM이 실행하는 코드의 이름을 쓰시오.', '바이트코드', '컴파일 결과물이 바이트코드입니다.', 'BRONZE', NULL, 1),
  (10907, @java_subject_id, 1, 109, 'FILL_BLANK', '빈칸에 들어갈 명령어를 쓰시오.
```text
____ Hello.java   (컴파일)
```', 'javac', '컴파일은 javac 명령으로 합니다.', 'BRONZE', NULL, 1),
  (10908, @java_subject_id, 1, 109, 'FILL_BLANK', '빈칸에 들어갈 명령어를 쓰시오.
```text
____ Hello   (실행)
```', 'java', '실행은 java 명령으로 합니다.', 'BRONZE', NULL, 1),
  (10909, @java_subject_id, 1, 109, 'CODE_SHORT', '`Score.java`를 컴파일한 뒤 실행하는 두 명령을 순서대로 작성하시오.', '`javac Score.java` → `java Score`', '컴파일에는 확장자를 쓰고, 실행에는 쓰지 않습니다.', 'BRONZE', NULL, 1),
  (10910, @java_subject_id, 1, 109, 'CODE_OUTPUT', '소스 코드를 수정하고 재컴파일하지 않은 채 `java` 명령으로 실행하면 어떤 결과가 나오는지 쓰시오.', '수정 전 코드(기존 .class)의 결과가 출력된다', '실행 대상은 소스가 아니라 컴파일된 바이트코드입니다.', 'BRONZE', NULL, 1),
  (11001, @java_subject_id, 1, 110, 'MULTIPLE_CHOICE', '컴파일 오류와 런타임 오류의 차이로 옳은 것은?', '컴파일 오류는 실행 전, 런타임 오류는 실행 중 발견된다', '문법·타입 문제는 컴파일 단계에서, 실행 경로의 문제는 실행 중에 발견됩니다.', 'BRONZE', NULL, 1),
  (11002, @java_subject_id, 1, 110, 'MULTIPLE_CHOICE', '컴파일 오류가 여러 개 표시될 때 올바른 해결 순서는?', '가장 위(첫 번째) 오류부터', '첫 번째 오류가 뒤쪽에 연쇄 오류를 만들 수 있으므로 위에서부터 해결합니다.', 'BRONZE', NULL, 1),
  (11003, @java_subject_id, 1, 110, 'MULTIPLE_CHOICE', '다음 코드를 컴파일·실행하면?
```java
int zero = 0;
System.out.println(10 / zero);
```', '실행 중 오류(ArithmeticException)', '컴파일은 통과하지만 실행 중 0으로 나누어 ArithmeticException이 발생합니다.', 'BRONZE', NULL, 1),
  (11004, @java_subject_id, 1, 110, 'MULTIPLE_CHOICE', '세미콜론 누락은 어떤 종류의 오류인가?', '컴파일(문법) 오류', '문법 오류는 컴파일 전에 발견됩니다.', 'BRONZE', NULL, 1),
  (11005, @java_subject_id, 1, 110, 'SHORT_ANSWER', '오류 메시지를 읽을 때 가장 먼저 확인해야 할 정보 두 가지를 쓰시오.', '파일명과 줄 번호(그리고 오류 종류)', '해당 줄과 바로 앞줄을 함께 확인합니다.', 'BRONZE', NULL, 1),
  (11006, @java_subject_id, 1, 110, 'SHORT_ANSWER', '컴파일은 성공했지만 프로그램 실행 중에 발생하는 오류를 무엇이라 부르는지 쓰시오.', '런타임 오류(실행 오류)', '어떤 입력·실행 경로에서 발생했는지 함께 확인해야 합니다.', 'BRONZE', NULL, 1),
  (11007, @java_subject_id, 1, 110, 'CODE_OUTPUT', '다음 코드를 컴파일하면 어떤 단계에서 어떤 오류가 발생하는지 쓰시오.
```java
int score = 80
```', '컴파일 단계에서 문법 오류(세미콜론 누락)', '실행문 끝에 세미콜론이 없어 컴파일이 실패합니다.', 'BRONZE', NULL, 1),
  (11008, @java_subject_id, 1, 110, 'CODE_OUTPUT', '다음 문장을 실행하면 어떻게 되는지 쓰시오.
```java
System.out.println(10 / 0);
```', '실행 중 ArithmeticException이 발생한다', '정수를 0으로 나누면 실행 중 예외가 발생합니다.', 'BRONZE', NULL, 1),
  (11009, @java_subject_id, 1, 110, 'FILL_BLANK', '문법 오류는 ____ 단계에서, 0으로 나누기 같은 오류는 ____ 단계에서 발견된다. 순서대로 쓰시오.', '컴파일, 실행(런타임)', '발견 시점이 두 오류를 구분하는 기준입니다.', 'BRONZE', NULL, 1),
  (11010, @java_subject_id, 1, 110, 'CODE_SHORT', '오류가 있는 문장 `System.out.printLn("hi");`를 올바르게 고쳐 쓰시오.', '`System.out.println("hi");`', '메서드 이름은 대소문자까지 정확해야 합니다(printLn → println).', 'BRONZE', NULL, 1),
  (20101, @java_subject_id, 2, 201, 'MULTIPLE_CHOICE', '변수에 대한 설명으로 옳은 것은?', '값을 저장하는 이름표로, 사용하기 전에 선언해야 한다', '변수는 값을 저장하는 이름표이며 사용 전 선언이 필요합니다. 재대입은 가능합니다.', 'BRONZE', NULL, 1),
  (20102, @java_subject_id, 2, 201, 'MULTIPLE_CHOICE', '변수 이름으로 사용할 수 **없는** 것은?', '1score', '변수명은 숫자로 시작할 수 없습니다.', 'BRONZE', NULL, 1),
  (20103, @java_subject_id, 2, 201, 'MULTIPLE_CHOICE', '다음 코드의 출력 결과는?
```java
int score = 80;
score = 90;
System.out.println(score);
```', '90', '새 값을 대입하면 이전 값은 덮어써집니다.', 'BRONZE', NULL, 1),
  (20104, @java_subject_id, 2, 201, 'MULTIPLE_CHOICE', '빈칸에 들어갈 자료형은?
```java
____ age = 20;
```', 'int', '20 같은 정수를 저장하는 기본 자료형은 int입니다.', 'BRONZE', NULL, 1),
  (20105, @java_subject_id, 2, 201, 'SHORT_ANSWER', '지역 변수는 값을 넣기 전에 읽을 수 있는지 쓰시오.', '없다', '지역 변수는 초기화하기 전에는 읽을 수 없습니다(컴파일 오류).', 'BRONZE', NULL, 1),
  (20106, @java_subject_id, 2, 201, 'SHORT_ANSWER', '같은 범위(블록) 안에서 같은 이름의 변수를 다시 선언할 수 있는지 쓰시오.', '없다', '같은 범위에서 같은 이름의 재선언은 허용되지 않습니다.', 'BRONZE', NULL, 1),
  (20107, @java_subject_id, 2, 201, 'CODE_OUTPUT', '다음 코드의 출력 결과를 쓰시오.
```java
int a = 5;
a = 7;
a = 9;
System.out.println(a);
```', '9', '마지막에 대입한 값만 남습니다.', 'BRONZE', NULL, 1),
  (20108, @java_subject_id, 2, 201, 'FILL_BLANK', '빈칸에 들어갈 기호를 쓰시오.
```java
int count ____ 10;
```', '=', '대입 연산자 =로 값을 저장합니다.', 'BRONZE', NULL, 1),
  (20109, @java_subject_id, 2, 201, 'CODE_SHORT', '정수 변수 `score`를 선언하면서 100을 저장하는 문장 한 줄을 작성하시오.', '`int score = 100;`', '선언과 초기화를 한 번에 작성할 수 있습니다.', 'BRONZE', NULL, 1),
  (20110, @java_subject_id, 2, 201, 'CODE_OUTPUT', '다음 코드를 컴파일하면 어떻게 되는지 쓰시오.
```java
int x;
System.out.println(x);
```', '컴파일 오류가 발생한다', '지역 변수는 값을 넣기 전에 읽을 수 없습니다.', 'BRONZE', NULL, 1),
  (20201, @java_subject_id, 2, 202, 'MULTIPLE_CHOICE', '일반적인 횟수·점수를 저장할 때 주로 쓰는 정수 자료형은?', 'int', '일반적인 정수 값에는 int를 기본으로 사용합니다.', 'BRONZE', NULL, 1),
  (20202, @java_subject_id, 2, 202, 'MULTIPLE_CHOICE', 'int 범위를 넘는 큰 정수를 저장하는 방법으로 옳은 것은?', 'long과 L 접미사', '큰 정수는 long 자료형과 L 접미사를 사용합니다.', 'BRONZE', NULL, 1),
  (20203, @java_subject_id, 2, 202, 'MULTIPLE_CHOICE', '다음 코드의 출력 결과는?
```java
long amount = 3_000_000_000L;
System.out.println(amount);
```', '3000000000', '숫자 사이 밑줄은 읽기 편의용일 뿐 값·출력에는 포함되지 않습니다.', 'BRONZE', NULL, 1),
  (20204, @java_subject_id, 2, 202, 'MULTIPLE_CHOICE', '빈칸에 들어갈 접미사는?
```java
long big = 10000000000____;
```', 'L', 'int 범위를 넘는 리터럴에는 L 접미사가 필요합니다.', 'BRONZE', NULL, 1),
  (20205, @java_subject_id, 2, 202, 'SHORT_ANSWER', 'Java의 정수 자료형 네 가지를 모두 쓰시오.', 'byte, short, int, long', '표현 범위가 작은 것부터 byte → short → int → long입니다.', 'BRONZE', NULL, 1),
  (20206, @java_subject_id, 2, 202, 'SHORT_ANSWER', 'byte와 short가 산술 연산에 사용될 때 대부분 변환되는 자료형을 쓰시오.', 'int', '작은 정수 타입은 산술 연산에서 대부분 int로 변환됩니다.', 'BRONZE', NULL, 1),
  (20207, @java_subject_id, 2, 202, 'CODE_OUTPUT', '다음 코드의 출력 결과를 쓰시오.
```java
int count = 2_000;
System.out.println(count);
```', '2000', '밑줄은 값에 영향을 주지 않습니다.', 'BRONZE', NULL, 1),
  (20208, @java_subject_id, 2, 202, 'FILL_BLANK', '매우 큰 식별값이나 시간을 저장할 때 고려하는 정수 자료형을 쓰시오.', 'long', 'int 범위를 넘을 수 있는 값에는 long을 사용합니다.', 'BRONZE', NULL, 1),
  (20209, @java_subject_id, 2, 202, 'CODE_SHORT', 'long 변수 `population`에 8000000000(80억)을 저장하는 문장 한 줄을 작성하시오.', '`long population = 8000000000L;`', 'int 범위를 넘으므로 L 접미사가 필요합니다.', 'BRONZE', NULL, 1),
  (20210, @java_subject_id, 2, 202, 'CODE_OUTPUT', 'int 변수에 범위를 넘는 계산 결과가 저장되면 값이 어떻게 되는지 쓰시오.', '값이 순환하여 전혀 다른 값(음수 등)이 될 수 있다', '정수 자료형은 표현 범위를 넘으면 값이 순환합니다.', 'BRONZE', NULL, 1),
  (20301, @java_subject_id, 2, 203, 'MULTIPLE_CHOICE', '소수점 값을 저장할 때 기본적으로 사용하는 자료형은?', 'double', '실수는 보통 double을 사용합니다.', 'BRONZE', NULL, 1),
  (20302, @java_subject_id, 2, 203, 'MULTIPLE_CHOICE', 'float 리터럴에 붙여야 하는 접미사는?', 'f', '`float rate = 0.5f;`처럼 f 접미사가 필요합니다.', 'BRONZE', NULL, 1),
  (20303, @java_subject_id, 2, 203, 'MULTIPLE_CHOICE', '다음 코드의 출력 결과는?
```java
double average = 87.5;
System.out.println(average);
```', '87.5', 'double 변수에 저장된 실수 값이 그대로 출력됩니다.', 'BRONZE', NULL, 1),
  (20304, @java_subject_id, 2, 203, 'MULTIPLE_CHOICE', '빈칸에 들어갈 접미사는?
```java
float rate = 0.5____;
```', 'f', '소수 리터럴은 기본이 double이므로 float에는 f가 필요합니다.', 'BRONZE', NULL, 1),
  (20305, @java_subject_id, 2, 203, 'SHORT_ANSWER', '금액처럼 정확성이 중요한 값에 double 대신 고려할 수 있는 방법을 한 가지 쓰시오.', '최소 단위 정수 또는 BigDecimal 사용', '0.1처럼 이진수로 정확히 표현되지 않는 값은 오차가 생길 수 있습니다.', 'BRONZE', NULL, 1),
  (20306, @java_subject_id, 2, 203, 'SHORT_ANSWER', 'double과 float 중 메모리를 더 적게 쓰는 자료형을 쓰시오.', 'float', 'float는 메모리를 더 적게 쓰지만 정밀도도 낮습니다.', 'BRONZE', NULL, 1),
  (20307, @java_subject_id, 2, 203, 'CODE_OUTPUT', '다음 코드의 출력 결과를 쓰시오.
```java
int a = 7 / 2;
double b = a;
System.out.println(b);
```', '3.0', '정수 나눗셈으로 이미 소수가 사라진 뒤 double에 저장해도 소수 부분은 되돌아오지 않습니다.', 'BRONZE', NULL, 1),
  (20308, @java_subject_id, 2, 203, 'FILL_BLANK', '빈칸에 들어갈 기본 실수 자료형을 쓰시오.
```java
____ average = 87.5;
```', 'double', '소수 리터럴의 기본 자료형은 double입니다.', 'BRONZE', NULL, 1),
  (20309, @java_subject_id, 2, 203, 'CODE_SHORT', 'double 변수 `height`에 172.5를 저장하는 문장 한 줄을 작성하시오.', '`double height = 172.5;`', '실수는 double로 선언합니다.', 'BRONZE', NULL, 1),
  (20310, @java_subject_id, 2, 203, 'CODE_OUTPUT', '다음 문장의 출력 결과를 쓰시오.
```java
System.out.println(7 / 2.0);
```', '3.5', '하나라도 실수면 실수 나눗셈이 되어 소수 결과가 나옵니다.', 'BRONZE', NULL, 1),
  (20401, @java_subject_id, 2, 204, 'MULTIPLE_CHOICE', 'char 값을 감싸는 기호는?', '작은따옴표 '' ''', '문자 하나는 작은따옴표, 문자열은 큰따옴표를 사용합니다.', 'BRONZE', NULL, 1),
  (20402, @java_subject_id, 2, 204, 'MULTIPLE_CHOICE', 'char에 대한 설명으로 옳은 것은?', '문자 하나를 저장하며 내부적으로 문자 코드값을 가진다', 'char는 문자 하나를 저장하고 내부적으로 코드값(UTF-16 코드 단위)을 가집니다.', 'BRONZE', NULL, 1),
  (20403, @java_subject_id, 2, 204, 'MULTIPLE_CHOICE', '다음 코드의 출력 결과는?
```java
char letter = ''A'';
System.out.println(letter + 1);
```', '66', 'char가 산술 연산에 들어가면 코드값(A=65)으로 계산되어 66이 출력됩니다.', 'BRONZE', NULL, 1),
  (20404, @java_subject_id, 2, 204, 'MULTIPLE_CHOICE', '빈칸에 들어갈 자료형은?
```java
____ grade = ''B'';
```', 'char', '작은따옴표로 감싼 문자 하나는 char에 저장합니다.', 'BRONZE', NULL, 1),
  (20405, @java_subject_id, 2, 204, 'SHORT_ANSWER', '`''A'' + 1`의 결과가 문자 B가 아니라 66이 되는 이유를 한 문장으로 쓰시오.', 'char가 산술 연산에서 문자 코드값(정수)으로 변환되어 계산되기 때문', '''A''의 코드값 65에 1을 더한 정수 66이 결과입니다.', 'BRONZE', NULL, 1),
  (20406, @java_subject_id, 2, 204, 'SHORT_ANSWER', '`"A"`(큰따옴표)는 어떤 자료형의 값인지 쓰시오.', 'String', '큰따옴표는 문자열, 작은따옴표는 char입니다.', 'BRONZE', NULL, 1),
  (20407, @java_subject_id, 2, 204, 'CODE_OUTPUT', '다음 코드의 출력 결과를 쓰시오.
```java
char ch = ''B'';
System.out.println(ch);
```', 'B', 'char 값을 그대로 출력하면 문자가 표시됩니다.', 'BRONZE', NULL, 1),
  (20408, @java_subject_id, 2, 204, 'FILL_BLANK', '문자 A를 char 변수에 저장하려 한다. 빈칸을 채우시오.
```java
char c = ____;
```', '''A''', '작은따옴표로 문자 하나를 감쌉니다.', 'BRONZE', NULL, 1),
  (20409, @java_subject_id, 2, 204, 'CODE_SHORT', 'char 변수 `initial`에 문자 K를 저장하는 문장 한 줄을 작성하시오.', '`char initial = ''K'';`', '문자 리터럴은 작은따옴표를 사용합니다.', 'BRONZE', NULL, 1),
  (20410, @java_subject_id, 2, 204, 'CODE_OUTPUT', '다음 문장의 출력 결과를 쓰시오.
```java
System.out.println((char) 66);
```', 'B', '코드값 66을 char로 강제 변환하면 대응하는 문자 B가 됩니다.', 'BRONZE', NULL, 1),
  (20501, @java_subject_id, 2, 205, 'MULTIPLE_CHOICE', 'boolean 변수에 저장할 수 있는 값은?', 'true와 false', 'boolean은 true 또는 false만 저장합니다.', 'BRONZE', NULL, 1),
  (20502, @java_subject_id, 2, 205, 'MULTIPLE_CHOICE', 'Java에서 숫자 0과 1을 boolean으로 자동 변환하는지에 대한 설명으로 옳은 것은?', '자동 변환되지 않는다', 'Java는 숫자를 boolean으로 자동 변환하지 않습니다.', 'BRONZE', NULL, 1),
  (20503, @java_subject_id, 2, 205, 'MULTIPLE_CHOICE', '다음 코드의 출력 결과는?
```java
int score = 85;
boolean passed = score >= 70;
System.out.println(passed);
```', 'true', '85 >= 70은 참이므로 true가 저장·출력됩니다.', 'BRONZE', NULL, 1),
  (20504, @java_subject_id, 2, 205, 'MULTIPLE_CHOICE', '빈칸에 들어갈 자료형은?
```java
____ done = false;
```', 'boolean', 'true/false는 boolean에 저장합니다.', 'BRONZE', NULL, 1),
  (20505, @java_subject_id, 2, 205, 'SHORT_ANSWER', '`if (passed == true)`보다 간결하게 같은 의미를 표현하는 방법을 쓰시오.', '`if (passed)`', 'boolean 변수는 그대로 조건에 사용할 수 있습니다.', 'BRONZE', NULL, 1),
  (20506, @java_subject_id, 2, 205, 'SHORT_ANSWER', 'boolean 변수 `passed`의 반대(부정)를 표현하는 식을 쓰시오.', '`!passed`', '! 연산자로 boolean 값을 부정합니다.', 'BRONZE', NULL, 1),
  (20507, @java_subject_id, 2, 205, 'CODE_OUTPUT', '다음 코드의 출력 결과를 쓰시오.
```java
boolean b = 10 < 5;
System.out.println(b);
```', 'false', '10 < 5는 거짓입니다.', 'BRONZE', NULL, 1),
  (20508, @java_subject_id, 2, 205, 'FILL_BLANK', '참을 뜻하는 값을 빈칸에 쓰시오.
```java
boolean isOpen = ____;
```', 'true', 'boolean의 참 값은 true입니다.', 'BRONZE', NULL, 1),
  (20509, @java_subject_id, 2, 205, 'CODE_SHORT', '`score`가 70 이상인지의 결과를 boolean 변수 `passed`에 저장하는 문장 한 줄을 작성하시오.', '`boolean passed = score >= 70;`', '비교식의 결과를 boolean 변수에 저장할 수 있습니다.', 'BRONZE', NULL, 1),
  (20510, @java_subject_id, 2, 205, 'CODE_OUTPUT', '다음 코드의 출력 결과를 쓰시오.
```java
boolean r = !(3 > 1);
System.out.println(r);
```', 'false', '3 > 1은 true이고 이를 부정하면 false입니다.', 'BRONZE', NULL, 1),
  (20601, @java_subject_id, 2, 206, 'MULTIPLE_CHOICE', '문자열의 내용이 같은지 비교할 때 사용하는 것은?', 'equals()', '==는 참조 비교이므로 내용 비교에는 equals()를 사용합니다.', 'BRONZE', NULL, 1),
  (20602, @java_subject_id, 2, 206, 'MULTIPLE_CHOICE', 'String 값을 감싸는 기호는?', '큰따옴표 " "', '문자열은 큰따옴표로 만듭니다.', 'BRONZE', NULL, 1),
  (20603, @java_subject_id, 2, 206, 'MULTIPLE_CHOICE', '다음 문장의 출력 결과는?
```java
System.out.println("합계: " + (10 + 20));
```', '합계: 30', '괄호 안 숫자 계산(30)이 먼저 수행된 뒤 문자열과 연결됩니다.', 'BRONZE', NULL, 1),
  (20604, @java_subject_id, 2, 206, 'MULTIPLE_CHOICE', '빈칸에 들어갈 자료형은?
```java
____ name = "하늘";
```', 'String', '큰따옴표로 감싼 텍스트는 String입니다.', 'BRONZE', NULL, 1),
  (20605, @java_subject_id, 2, 206, 'SHORT_ANSWER', '문자열 비교에 ==를 쓰지 않는 이유를 한 문장으로 쓰시오.', '==는 내용이 아니라 참조를 비교하기 때문', '내용 비교에는 equals()를 사용합니다.', 'BRONZE', NULL, 1),
  (20606, @java_subject_id, 2, 206, 'SHORT_ANSWER', '문자열 변수가 null일 가능성이 있을 때 메서드를 호출하기 전에 먼저 해야 할 일을 쓰시오.', 'null 여부 확인', 'null에 메서드를 호출하면 실행 중 오류가 발생합니다.', 'BRONZE', NULL, 1),
  (20607, @java_subject_id, 2, 206, 'CODE_OUTPUT', '다음 문장의 출력 결과를 쓰시오.
```java
System.out.println("점수: " + 90);
```', '점수: 90', '문자열과 숫자를 +로 연결하면 숫자가 텍스트로 붙습니다.', 'BRONZE', NULL, 1),
  (20608, @java_subject_id, 2, 206, 'FILL_BLANK', '빈칸에 들어갈 메서드 이름을 쓰시오.
```java
String s = "Java";
boolean same = s.____("Java");
```', 'equals', '내용 비교는 equals 메서드로 합니다.', 'BRONZE', NULL, 1),
  (20609, @java_subject_id, 2, 206, 'CODE_SHORT', 'String 변수 `title`에 "Knowva"를 저장하는 문장 한 줄을 작성하시오.', '`String title = "Knowva";`', '문자열은 큰따옴표로 감싸 저장합니다.', 'BRONZE', NULL, 1),
  (20610, @java_subject_id, 2, 206, 'CODE_OUTPUT', '다음 코드의 출력 결과를 쓰시오.
```java
String a = "10";
System.out.println(a + 20);
```', '1020', '"10"은 문자열이므로 +는 계산이 아니라 연결로 처리됩니다.', 'BRONZE', NULL, 1),
  (20701, @java_subject_id, 2, 207, 'MULTIPLE_CHOICE', 'final 변수의 특징으로 옳은 것은?', '한 번 값이 정해지면 재대입할 수 없다', 'final 변수는 재대입이 금지됩니다.', 'BRONZE', NULL, 1),
  (20702, @java_subject_id, 2, 207, 'MULTIPLE_CHOICE', '상수 이름의 관례로 알맞은 것은?', 'PASS_SCORE', '상수는 대문자와 밑줄을 사용한 이름을 관례로 씁니다.', 'BRONZE', NULL, 1),
  (20703, @java_subject_id, 2, 207, 'MULTIPLE_CHOICE', '다음 코드를 컴파일하면?
```java
final int MAX = 10;
MAX = 20;
```', '컴파일 오류', 'final 변수에 재대입하면 컴파일 오류입니다.', 'BRONZE', NULL, 1),
  (20704, @java_subject_id, 2, 207, 'MULTIPLE_CHOICE', '빈칸에 들어갈 키워드는?
```java
____ int PASS_SCORE = 70;
```', 'final', 'Java에서 상수는 final 키워드로 만듭니다.', 'BRONZE', NULL, 1),
  (20705, @java_subject_id, 2, 207, 'SHORT_ANSWER', '숫자를 코드 곳곳에 직접 쓰는 대신 상수를 사용하면 좋은 점을 한 가지 쓰시오.', '숫자의 의미가 분명해지고, 기준 변경 시 한 곳만 수정하면 된다', '상수는 프로그램의 규칙을 한곳에서 관리하게 해 줍니다.', 'BRONZE', NULL, 1),
  (20706, @java_subject_id, 2, 207, 'SHORT_ANSWER', '재대입할 수 없는 변수를 만드는 키워드를 쓰시오.', 'final', 'final이 붙은 변수는 값이 한 번 정해지면 바꿀 수 없습니다.', 'BRONZE', NULL, 1),
  (20707, @java_subject_id, 2, 207, 'CODE_OUTPUT', '다음 코드의 출력 결과를 쓰시오.
```java
final int LIMIT = 5;
System.out.println(LIMIT);
```', '5', 'final 변수도 읽기는 자유롭게 할 수 있습니다.', 'BRONZE', NULL, 1),
  (20708, @java_subject_id, 2, 207, 'FILL_BLANK', '빈칸에 들어갈 기호를 쓰시오.
```java
final int MAX_USER ____ 100;
```', '=', '상수도 대입 연산자로 값을 정합니다(단 한 번만).', 'BRONZE', NULL, 1),
  (20709, @java_subject_id, 2, 207, 'CODE_SHORT', '합격 점수 70을 담는 상수 `PASS_SCORE`를 선언하는 문장 한 줄을 작성하시오.', '`final int PASS_SCORE = 70;`', 'final + 대문자·밑줄 이름이 상수의 기본 형태입니다.', 'BRONZE', NULL, 1),
  (20710, @java_subject_id, 2, 207, 'CODE_OUTPUT', '다음 코드를 컴파일하면 어떻게 되는지 쓰시오.
```java
final double PI = 3.14;
PI = 3.141;
```', '컴파일 오류가 발생한다', 'final 변수에는 재대입할 수 없습니다.', 'BRONZE', NULL, 1),
  (20801, @java_subject_id, 2, 208, 'MULTIPLE_CHOICE', '자동 타입 변환이 일어나는 방향으로 옳은 것은?', '작은 범위 → 큰 범위', '값이 넓은 표현 범위로 이동할 때 자동 변환됩니다.', 'BRONZE', NULL, 1),
  (20802, @java_subject_id, 2, 208, 'MULTIPLE_CHOICE', '자동 변환의 일반적인 순서로 올바른 것은?', 'byte → short → int → long → float → double', '작은 범위에서 큰 범위 방향으로 자동 변환됩니다.', 'BRONZE', NULL, 1),
  (20803, @java_subject_id, 2, 208, 'MULTIPLE_CHOICE', '다음 코드의 출력 결과는?
```java
int count = 10;
double result = count;
System.out.println(result);
```', '10.0', 'int가 double로 자동 변환되어 10.0이 출력됩니다.', 'BRONZE', NULL, 1),
  (20804, @java_subject_id, 2, 208, 'MULTIPLE_CHOICE', 'int 값이 자동 변환되어 저장될 수 **있는** 자료형은?', 'long', 'int보다 범위가 넓은 long으로는 자동 변환됩니다.', 'BRONZE', NULL, 1),
  (20805, @java_subject_id, 2, 208, 'SHORT_ANSWER', 'char가 자동 변환될 수 있는 방향을 쓰시오.', 'int 이상의 숫자 타입', 'char도 코드값 기준으로 int 이상 타입에 자동 변환될 수 있습니다.', 'BRONZE', NULL, 1),
  (20806, @java_subject_id, 2, 208, 'SHORT_ANSWER', 'double 값을 int 변수에 그대로 대입하면 어떻게 되는지 쓰시오.', '컴파일 오류가 발생한다', '넓은 타입 → 좁은 타입은 자동 변환되지 않습니다.', 'BRONZE', NULL, 1),
  (20807, @java_subject_id, 2, 208, 'CODE_OUTPUT', '다음 코드의 출력 결과를 쓰시오.
```java
int n = 100;
long l = n;
System.out.println(l);
```', '100', 'int → long은 자동 변환됩니다.', 'BRONZE', NULL, 1),
  (20808, @java_subject_id, 2, 208, 'CODE_OUTPUT', '다음 코드의 출력 결과를 쓰시오.
```java
double d = 5;
System.out.println(d);
```', '5.0', '정수 5가 double로 자동 변환되어 5.0으로 저장됩니다.', 'BRONZE', NULL, 1),
  (20809, @java_subject_id, 2, 208, 'CODE_SHORT', 'int 변수 `num`의 값을 double 변수 `result`에 대입하는 문장 한 줄을 작성하시오.', '`double result = num;`', '작은 범위 → 큰 범위이므로 캐스팅 없이 대입할 수 있습니다.', 'BRONZE', NULL, 1),
  (20810, @java_subject_id, 2, 208, 'CODE_OUTPUT', '다음 코드를 컴파일하면 어떻게 되는지 쓰시오.
```java
int i = 3.5;
```', '컴파일 오류가 발생한다', 'double(3.5) → int는 자동 변환되지 않으므로 캐스팅이 필요합니다.', 'BRONZE', NULL, 1),
  (20901, @java_subject_id, 2, 209, 'MULTIPLE_CHOICE', '강제 타입 변환(캐스팅)의 문법으로 옳은 것은?', '(자료형) 값', '`(int) 12.9`처럼 괄호 안에 자료형을 적습니다.', 'BRONZE', NULL, 1),
  (20902, @java_subject_id, 2, 209, 'MULTIPLE_CHOICE', '캐스팅의 의미로 가장 알맞은 것은?', '값 손실 가능성을 알고 변환하겠다는 개발자의 표시', '캐스팅은 문법을 통과시킬 뿐 값의 안전을 보장하지 않습니다.', 'BRONZE', NULL, 1),
  (20903, @java_subject_id, 2, 209, 'MULTIPLE_CHOICE', '다음 코드의 출력 결과는?
```java
int whole = (int) 12.9;
System.out.println(whole);
```', '12', '실수를 int로 캐스팅하면 소수점이 버려집니다(반올림 아님).', 'BRONZE', NULL, 1),
  (20904, @java_subject_id, 2, 209, 'MULTIPLE_CHOICE', '빈칸에 들어갈 자료형은?
```java
int n = (____) 3.7;
```', 'int', 'int 변수에 넣으려면 (int)로 캐스팅합니다.', 'BRONZE', NULL, 1),
  (20905, @java_subject_id, 2, 209, 'SHORT_ANSWER', '실수를 int로 캐스팅하면 소수점 부분이 어떻게 되는지 쓰시오.', '버려진다', '반올림이 아니라 소수점 이하를 잘라냅니다.', 'BRONZE', NULL, 1),
  (20906, @java_subject_id, 2, 209, 'SHORT_ANSWER', 'byte처럼 범위가 작은 타입으로 캐스팅할 때 생길 수 있는 위험을 쓰시오.', '범위를 벗어나면 전혀 다른 값(음수 등)으로 바뀔 수 있다', '변환 전 값이 대상 자료형 범위 안인지 확인해야 합니다.', 'BRONZE', NULL, 1),
  (20907, @java_subject_id, 2, 209, 'CODE_OUTPUT', '다음 코드의 출력 결과를 쓰시오.
```java
double d = 9.99;
int i = (int) d;
System.out.println(i);
```', '9', '소수점 이하 .99가 버려집니다.', 'BRONZE', NULL, 1),
  (20908, @java_subject_id, 2, 209, 'FILL_BLANK', '빈칸에 들어갈 자료형을 쓰시오.
```java
long big = 10L;
int small = (____) big;
```', 'int', 'long → int는 강제 캐스팅이 필요합니다.', 'BRONZE', NULL, 1),
  (20909, @java_subject_id, 2, 209, 'CODE_SHORT', 'double 값 3.8을 int 변수 `n`에 캐스팅해 저장하는 문장 한 줄을 작성하시오.', '`int n = (int) 3.8;`', '결과는 소수점이 버려진 3입니다.', 'BRONZE', NULL, 1),
  (20910, @java_subject_id, 2, 209, 'CODE_OUTPUT', '다음 문장의 출력 결과를 쓰시오.
```java
System.out.println((int) 7.5 + 1);
```', '8', '(int) 7.5는 7이고 여기에 1을 더해 8입니다.', 'BRONZE', NULL, 1),
  (21001, @java_subject_id, 2, 210, 'MULTIPLE_CHOICE', '전화번호를 저장하기에 가장 알맞은 자료형은?', 'String', '계산하지 않는 숫자(전화번호·학번)는 String이 더 적절합니다.', 'BRONZE', NULL, 1),
  (21002, @java_subject_id, 2, 210, 'MULTIPLE_CHOICE', 'boolean 변수 이름 관례로 가장 알맞은 것은?', 'isPremium', 'boolean은 is·has·can처럼 상태가 드러나는 이름이 좋습니다.', 'BRONZE', NULL, 1),
  (21003, @java_subject_id, 2, 210, 'MULTIPLE_CHOICE', '다음 코드의 출력 결과는?
```java
String name = "하늘";
boolean premium = true;
System.out.println(name + ":" + premium);
```', '하늘:true', '문자열 연결에서 boolean 값은 true/false 텍스트로 붙습니다.', 'BRONZE', NULL, 1),
  (21004, @java_subject_id, 2, 210, 'MULTIPLE_CHOICE', '평균 점수(소수점 포함)를 저장할 자료형으로 알맞은 것은?', 'double', '소수가 필요한 값은 double을 사용합니다.', 'BRONZE', NULL, 1),
  (21005, @java_subject_id, 2, 210, 'SHORT_ANSWER', '나이를 저장하기에 알맞은 자료형을 쓰시오.', 'int', '소수가 필요 없는 일반적인 정수 값은 int를 사용합니다.', 'BRONZE', NULL, 1),
  (21006, @java_subject_id, 2, 210, 'SHORT_ANSWER', '한 변수에 서로 다른 의미의 값을 재사용하면 안 되는 이유를 한 문장으로 쓰시오.', '값의 의미가 뒤섞여 오류를 만들기 쉽기 때문', '변수 하나는 한 가지 의미의 값만 담아야 합니다.', 'BRONZE', NULL, 1),
  (21007, @java_subject_id, 2, 210, 'CODE_OUTPUT', '다음 코드의 출력 결과를 쓰시오.
```java
int age = 14;
double height = 150.5;
System.out.println(age + "세 " + height);
```', '14세 150.5', '숫자들이 문자열과 연결되어 한 문장으로 출력됩니다.', 'BRONZE', NULL, 1),
  (21008, @java_subject_id, 2, 210, 'FILL_BLANK', '빈칸에 들어갈 자료형을 쓰시오.
```java
____ isPremium = true;
```', 'boolean', '참·거짓 상태는 boolean에 저장합니다.', 'BRONZE', NULL, 1),
  (21009, @java_subject_id, 2, 210, 'CODE_SHORT', '사용자 이름 "민수"를 저장하는 String 변수 `userName`을 선언하는 문장 한 줄을 작성하시오.', '`String userName = "민수";`', '이름은 텍스트이므로 String을 사용합니다.', 'BRONZE', NULL, 1),
  (21010, @java_subject_id, 2, 210, 'CODE_OUTPUT', '다음 코드의 출력 결과를 쓰시오.
```java
String id = "1001";
System.out.println(id + 1);
```', '10011', 'id는 문자열이므로 +1은 계산이 아니라 연결이 됩니다. 계산하지 않는 숫자를 String으로 둘 때 주의할 점입니다.', 'BRONZE', NULL, 1),
  (30101, @java_subject_id, 3, 301, 'MULTIPLE_CHOICE', '`=` 연산자의 의미로 옳은 것은?', '오른쪽 계산 결과를 왼쪽 변수에 저장한다', '=는 비교가 아니라 대입(값 저장)입니다. 비교는 ==를 사용합니다.', 'BRONZE', NULL, 1),
  (30102, @java_subject_id, 3, 301, 'MULTIPLE_CHOICE', '`x += 3`과 비슷한 의미의 식은?', 'x = x + 3', '복합 대입은 기존 값에 계산을 더해 다시 저장합니다.', 'BRONZE', NULL, 1),
  (30103, @java_subject_id, 3, 301, 'MULTIPLE_CHOICE', '다음 코드의 출력 결과는?
```java
int point = 10;
point += 5;
System.out.println(point);
```', '15', '10에 5를 더한 15가 다시 저장됩니다.', 'BRONZE', NULL, 1),
  (30104, @java_subject_id, 3, 301, 'MULTIPLE_CHOICE', 'point에서 5를 빼서 다시 저장하려 한다. 빈칸에 들어갈 기호는?
```java
point ____= 5;
```', '-', '`point -= 5;`는 point = point - 5와 비슷한 의미입니다.', 'BRONZE', NULL, 1),
  (30105, @java_subject_id, 3, 301, 'SHORT_ANSWER', '대입식의 오른쪽에 올 수 있는 것을 두 가지 이상 쓰시오.', '계산식, 메서드 호출, 다른 변수', '오른쪽을 먼저 계산한 결과가 왼쪽에 저장됩니다.', 'BRONZE', NULL, 1),
  (30106, @java_subject_id, 3, 301, 'SHORT_ANSWER', '`=`와 `==`의 차이를 한 문장으로 쓰시오.', '=는 대입(값 저장), ==는 비교(같은지 확인)', '조건문에서 = 를 쓰지 않도록 주의합니다.', 'BRONZE', NULL, 1),
  (30107, @java_subject_id, 3, 301, 'CODE_OUTPUT', '다음 코드의 출력 결과를 쓰시오.
```java
int x = 4;
x *= 2;
System.out.println(x);
```', '8', '4에 2를 곱한 8이 다시 저장됩니다.', 'BRONZE', NULL, 1),
  (30108, @java_subject_id, 3, 301, 'FILL_BLANK', 'a가 5가 되도록 빈칸에 들어갈 복합 대입 연산자를 쓰시오.
```java
int a = 3;
a ____ 2;
```', '+=', '3 + 2 = 5가 되도록 +=를 사용합니다.', 'BRONZE', NULL, 1),
  (30109, @java_subject_id, 3, 301, 'CODE_SHORT', '변수 `total`에 현재 값에서 10을 더해 다시 저장하는 문장을 복합 대입으로 작성하시오.', '`total += 10;`', '누적 계산에는 복합 대입이 편리합니다.', 'BRONZE', NULL, 1),
  (30110, @java_subject_id, 3, 301, 'CODE_OUTPUT', '다음 코드의 출력 결과를 쓰시오.
```java
int n = 7;
n -= 3;
n += 1;
System.out.println(n);
```', '5', '7 → 4 → 5 순서로 값이 바뀝니다. 실행 순서대로 추적합니다.', 'BRONZE', NULL, 1),
  (30201, @java_subject_id, 3, 302, 'MULTIPLE_CHOICE', 'int끼리 나눗셈한 결과에 대한 설명으로 옳은 것은?', '몫만 남는다', '정수끼리 나누면 몫만 남고 소수 부분은 사라집니다.', 'BRONZE', NULL, 1),
  (30202, @java_subject_id, 3, 302, 'MULTIPLE_CHOICE', '정수를 0으로 나누면 어떻게 되는가?', '실행 중 ArithmeticException 발생', '0으로 정수를 나누면 실행 중 예외가 발생합니다.', 'BRONZE', NULL, 1),
  (30203, @java_subject_id, 3, 302, 'MULTIPLE_CHOICE', '다음 코드의 출력 결과는?
```java
System.out.println(7 / 2);
System.out.println(7 % 2);
```', '3과 1', '7/2는 몫 3, 7%2는 나머지 1입니다.', 'BRONZE', NULL, 1),
  (30204, @java_subject_id, 3, 302, 'MULTIPLE_CHOICE', '나머지를 구하는 연산자는?', '%', '%는 나눗셈의 나머지를 구합니다.', 'BRONZE', NULL, 1),
  (30205, @java_subject_id, 3, 302, 'SHORT_ANSWER', '나눗셈에서 피연산자 중 하나라도 실수면 결과는 어떻게 되는지 쓰시오.', '실수 나눗셈이 되어 소수 결과가 나온다', '연산 결과의 자료형은 피연산자에 영향을 받습니다.', 'BRONZE', NULL, 1),
  (30206, @java_subject_id, 3, 302, 'SHORT_ANSWER', '나머지 연산이 활용되는 예를 두 가지 쓰시오.', '홀짝 판별, 배수 판별(자릿수 분리, 순환 규칙 등)', '`n % 2 == 0`이면 짝수입니다.', 'BRONZE', NULL, 1),
  (30207, @java_subject_id, 3, 302, 'CODE_OUTPUT', '다음 문장의 출력 결과를 쓰시오.
```java
System.out.println(10 % 3);
```', '1', '10을 3으로 나눈 나머지는 1입니다.', 'BRONZE', NULL, 1),
  (30208, @java_subject_id, 3, 302, 'CODE_OUTPUT', '다음 문장의 출력 결과를 쓰시오.
```java
System.out.println(2 + 3 * 4);
```', '14', '곱셈이 덧셈보다 먼저 계산됩니다.', 'BRONZE', NULL, 1),
  (30209, @java_subject_id, 3, 302, 'CODE_SHORT', '정수 `number`의 마지막 자릿수를 구해 int 변수 `last`에 저장하는 문장 한 줄을 작성하시오.', '`int last = number % 10;`', '10으로 나눈 나머지가 마지막 자릿수입니다.', 'BRONZE', NULL, 1),
  (30210, @java_subject_id, 3, 302, 'CODE_OUTPUT', '다음 문장의 출력 결과를 쓰시오.
```java
System.out.println(9 / 2.0);
```', '4.5', '2.0이 실수이므로 실수 나눗셈이 됩니다.', 'BRONZE', NULL, 1),
  (30301, @java_subject_id, 3, 303, 'MULTIPLE_CHOICE', '`++x`와 `x++`의 차이로 옳은 것은?', '++x는 먼저 증가시킨 뒤 사용하고, x++는 사용한 뒤 증가시킨다', '전위는 증가 후 사용, 후위는 사용 후 증가입니다.', 'BRONZE', NULL, 1),
  (30302, @java_subject_id, 3, 303, 'MULTIPLE_CHOICE', '`x++;`와 `++x;`를 각각 단독 문장으로 실행했을 때 최종 결과는?', '같다', '단독 문장에서는 사용 시점 차이가 드러나지 않아 결과가 같습니다.', 'BRONZE', NULL, 1),
  (30303, @java_subject_id, 3, 303, 'MULTIPLE_CHOICE', '다음 코드의 출력 결과는?
```java
int x = 5;
int y = x++;
System.out.println(x + " " + y);
```', '6 5', '후위이므로 y에는 현재 값 5가 저장된 뒤 x가 6으로 증가합니다.', 'BRONZE', NULL, 1),
  (30304, @java_subject_id, 3, 303, 'MULTIPLE_CHOICE', '1 감소시키는 증감 연산자는?', '--', '--는 값을 1 줄입니다.', 'BRONZE', NULL, 1),
  (30305, @java_subject_id, 3, 303, 'SHORT_ANSWER', '한 줄에 여러 증감 연산을 섞어 쓰면 생기는 문제를 한 문장으로 쓰시오.', '이해와 유지보수가 어려워진다', '반복문 증감식 외에는 단순한 대입으로 쓰는 것도 좋습니다.', 'BRONZE', NULL, 1),
  (30306, @java_subject_id, 3, 303, 'SHORT_ANSWER', 'for문에서 증감 연산자가 주로 쓰이는 위치(용도)를 쓰시오.', '증감식(반복 변수 증가·감소)', '`for (int i = 0; i < n; i++)` 형태로 자주 사용합니다.', 'BRONZE', NULL, 1),
  (30307, @java_subject_id, 3, 303, 'CODE_OUTPUT', '다음 코드의 출력 결과를 쓰시오.
```java
int a = 3;
int b = ++a;
System.out.println(b);
```', '4', '전위이므로 a가 4로 증가한 뒤 b에 저장됩니다.', 'BRONZE', NULL, 1),
  (30308, @java_subject_id, 3, 303, 'CODE_OUTPUT', '다음 코드의 출력 결과를 쓰시오.
```java
int n = 10;
n--;
System.out.println(n);
```', '9', '단독 문장의 감소 연산으로 10에서 9가 됩니다.', 'BRONZE', NULL, 1),
  (30309, @java_subject_id, 3, 303, 'CODE_SHORT', '변수 `count`를 1 증가시키는 문장을 증감 연산자로 작성하시오.', '`count++;`', '`++count;`도 단독 문장으로는 같은 결과입니다.', 'BRONZE', NULL, 1),
  (30310, @java_subject_id, 3, 303, 'CODE_OUTPUT', '다음 코드의 출력 결과를 두 줄로 쓰시오.
```java
int i = 1;
System.out.println(i++);
System.out.println(i);
```', '1 그리고 2', '첫 줄은 사용 후 증가라 1이 출력되고, 증가된 값 2가 다음 줄에 출력됩니다.', 'BRONZE', NULL, 1),
  (30401, @java_subject_id, 3, 304, 'MULTIPLE_CHOICE', '비교 연산의 결과 자료형은?', 'boolean', '비교 연산자는 항상 true 또는 false를 반환합니다.', 'BRONZE', NULL, 1),
  (30402, @java_subject_id, 3, 304, 'MULTIPLE_CHOICE', '문자열의 내용 비교에 사용하는 것은?', 'equals()', '==는 참조 비교이므로 내용 비교에는 equals()를 사용합니다.', 'BRONZE', NULL, 1),
  (30403, @java_subject_id, 3, 304, 'MULTIPLE_CHOICE', '다음 문장의 출력 결과는?
```java
System.out.println(5 >= 5);
```', 'true', '>=는 경계값(같은 값)을 포함합니다.', 'BRONZE', NULL, 1),
  (30404, @java_subject_id, 3, 304, 'MULTIPLE_CHOICE', '''같지 않다''를 뜻하는 연산자는?', '!=', '!=는 두 값이 다르면 true입니다.', 'BRONZE', NULL, 1),
  (30405, @java_subject_id, 3, 304, 'SHORT_ANSWER', '두 실수를 ==로 정확히 비교하는 것이 위험한 이유를 쓰시오.', '실수는 이진 표현 오차가 있을 수 있기 때문', '실수의 정확한 같음 비교는 주의가 필요합니다.', 'BRONZE', NULL, 1),
  (30406, @java_subject_id, 3, 304, 'SHORT_ANSWER', '경계값이 포함되는 조건을 만들 때 사용하는 연산자를 쓰시오.', '>= 또는 <=', '이상·이하 조건은 등호가 포함된 비교 연산자를 씁니다.', 'BRONZE', NULL, 1),
  (30407, @java_subject_id, 3, 304, 'CODE_OUTPUT', '다음 코드의 출력 결과를 쓰시오.
```java
int a = 3, b = 7;
System.out.println(a != b);
```', 'true', '3과 7은 다르므로 true입니다.', 'BRONZE', NULL, 1),
  (30408, @java_subject_id, 3, 304, 'FILL_BLANK', 'score가 0 이상이면서 100 이하인 조건이 되도록 빈칸을 채우시오.
```java
boolean valid = score >= 0 ____ score <= 100;
```', '&&', '두 비교식을 &&로 연결해 범위 조건을 만듭니다.', 'BRONZE', NULL, 1),
  (30409, @java_subject_id, 3, 304, 'CODE_SHORT', '`age`가 14 이상인지의 결과를 boolean 변수 `canJoin`에 저장하는 문장 한 줄을 작성하시오.', '`boolean canJoin = age >= 14;`', '비교식 결과를 그대로 boolean 변수에 저장합니다.', 'BRONZE', NULL, 1),
  (30410, @java_subject_id, 3, 304, 'CODE_OUTPUT', '다음 문장의 출력 결과를 쓰시오.
```java
System.out.println(''A'' < ''B'');
```', 'true', 'char는 문자 코드값 기준으로 비교되며 A(65)가 B(66)보다 작습니다.', 'BRONZE', NULL, 1),
  (30501, @java_subject_id, 3, 305, 'MULTIPLE_CHOICE', '`&&`가 true가 되는 조건은?', '둘 다 참이면', '&&는 둘 다 참일 때만 참입니다.', 'BRONZE', NULL, 1),
  (30502, @java_subject_id, 3, 305, 'MULTIPLE_CHOICE', '단락 평가에 대한 설명으로 옳은 것은?', '&&의 왼쪽이 false면 오른쪽을 계산하지 않는다', '&& 왼쪽이 false거나 || 왼쪽이 true면 오른쪽은 계산하지 않습니다.', 'BRONZE', NULL, 1),
  (30503, @java_subject_id, 3, 305, 'MULTIPLE_CHOICE', '다음 문장의 출력 결과는?
```java
System.out.println(true || false);
```', 'true', '||는 하나라도 참이면 참입니다.', 'BRONZE', NULL, 1),
  (30504, @java_subject_id, 3, 305, 'MULTIPLE_CHOICE', 'boolean 값을 반대로 만드는 부정 연산자는?', '!', '!true는 false, !false는 true입니다.', 'BRONZE', NULL, 1),
  (30505, @java_subject_id, 3, 305, 'SHORT_ANSWER', '''관리자이거나 작성자인 경우''처럼 하나만 만족해도 되는 조건에 알맞은 논리 연산자를 쓰시오.', '||', '동시에 필요한 조건은 &&, 하나만 만족해도 되는 조건은 ||입니다.', 'BRONZE', NULL, 1),
  (30506, @java_subject_id, 3, 305, 'SHORT_ANSWER', '`name != null && name.length() > 0`에서 &&의 단락 평가가 주는 안전 효과를 한 문장으로 쓰시오.', 'name이 null이면 오른쪽 length() 호출을 하지 않아 오류를 막는다', 'null 검사 뒤 메서드를 호출할 때 단락 평가가 안전장치가 됩니다.', 'BRONZE', NULL, 1),
  (30507, @java_subject_id, 3, 305, 'CODE_OUTPUT', '다음 문장의 출력 결과를 쓰시오.
```java
System.out.println(!(5 > 3));
```', 'false', '5 > 3은 true이고 이를 부정하면 false입니다.', 'BRONZE', NULL, 1),
  (30508, @java_subject_id, 3, 305, 'CODE_OUTPUT', '다음 코드의 출력 결과를 쓰시오.
```java
int s = 85;
System.out.println(s >= 70 && s <= 100);
```', 'true', '두 조건 모두 참이므로 &&의 결과는 true입니다.', 'BRONZE', NULL, 1),
  (30509, @java_subject_id, 3, 305, 'CODE_SHORT', '회원이면서(isMember) 결제를 완료한(isPaid) 경우인지의 결과를 boolean 변수 `ok`에 저장하는 문장 한 줄을 작성하시오.', '`boolean ok = isMember && isPaid;`', '동시에 만족해야 하는 조건은 &&로 연결합니다.', 'BRONZE', NULL, 1),
  (30510, @java_subject_id, 3, 305, 'CODE_OUTPUT', '다음 문장을 실행하면 오류 없이 출력되는지, 출력된다면 결과는 무엇인지 쓰시오.
```java
System.out.println(false && (10 / 0 > 1));
```', 'false가 출력된다(오류 없음)', '왼쪽이 false라 단락 평가로 오른쪽(0으로 나누기)을 계산하지 않습니다.', 'BRONZE', NULL, 1),
  (30601, @java_subject_id, 3, 306, 'MULTIPLE_CHOICE', '문자열이 포함된 `+` 연산에 대한 설명으로 옳은 것은?', '왼쪽부터 차례대로 처리되며 문자열을 만나면 연결로 처리된다', '+ 연산은 왼쪽부터 수행되며 문자열이 등장하면 이후는 연결로 처리됩니다.', 'BRONZE', NULL, 1),
  (30602, @java_subject_id, 3, 306, 'MULTIPLE_CHOICE', '문장 안에 숫자 합계를 넣을 때 계산이 먼저 되게 하는 방법은?', '계산 부분을 괄호로 묶는다', '`"합계: " + (a + b)`처럼 괄호로 계산을 먼저 수행합니다.', 'BRONZE', NULL, 1),
  (30603, @java_subject_id, 3, 306, 'MULTIPLE_CHOICE', '다음 문장의 출력 결과는?
```java
System.out.println("결과: " + 2 + 3);
```', '결과: 23', '왼쪽부터 문자열 연결로 처리되어 2와 3이 텍스트로 붙습니다.', 'BRONZE', NULL, 1),
  (30604, @java_subject_id, 3, 306, 'MULTIPLE_CHOICE', '출력이 `결과: 5`가 되도록 빈칸에 들어갈 기호는?
```java
System.out.println("결과: " + (2 ____ 3));
```', '+', '괄호 안에서 2 + 3 = 5가 먼저 계산됩니다.', 'BRONZE', NULL, 1),
  (30605, @java_subject_id, 3, 306, 'SHORT_ANSWER', '`"합: " + a + b`가 의도(합계 출력)와 다르게 출력될 수 있는 이유를 쓰시오.', '왼쪽부터 문자열 연결로 처리되어 a와 b가 각각 텍스트로 붙기 때문', '계산 부분은 괄호로 묶어야 합니다.', 'BRONZE', NULL, 1),
  (30606, @java_subject_id, 3, 306, 'SHORT_ANSWER', '여러 값을 출력할 때 값 사이에 공백을 넣는 방법을 쓰시오.', '공백을 문자열(" ")로 직접 넣는다', '중간 공백·구분 기호도 문자열로 연결해야 합니다.', 'BRONZE', NULL, 1),
  (30607, @java_subject_id, 3, 306, 'CODE_OUTPUT', '다음 문장의 출력 결과를 쓰시오.
```java
System.out.println(1 + 2 + "번");
```', '3번', '문자열보다 앞에 있는 1 + 2는 숫자 계산으로 먼저 처리됩니다.', 'BRONZE', NULL, 1),
  (30608, @java_subject_id, 3, 306, 'CODE_OUTPUT', '다음 문장의 출력 결과를 쓰시오.
```java
System.out.println("번호" + 1 + 2);
```', '번호12', '문자열이 먼저 등장하면 이후 +는 모두 연결로 처리됩니다.', 'BRONZE', NULL, 1),
  (30609, @java_subject_id, 3, 306, 'CODE_SHORT', '변수 a와 b의 합을 `합계: 값` 형태로 출력하는 문장 한 줄을 작성하시오.', '`System.out.println("합계: " + (a + b));`', '괄호로 숫자 계산을 먼저 수행하게 합니다.', 'BRONZE', NULL, 1),
  (30610, @java_subject_id, 3, 306, 'CODE_OUTPUT', '다음 문장의 출력 결과를 쓰시오.
```java
System.out.println("A" + 1 * 2);
```', 'A2', '곱셈이 연결보다 우선순위가 높아 1 * 2가 먼저 계산됩니다.', 'BRONZE', NULL, 1),
  (30701, @java_subject_id, 3, 307, 'MULTIPLE_CHOICE', '산술 연산자 우선순위로 옳은 것은?', '곱셈·나눗셈이 덧셈·뺄셈보다 먼저', '곱셈·나눗셈이 먼저 계산됩니다.', 'BRONZE', NULL, 1),
  (30702, @java_subject_id, 3, 307, 'MULTIPLE_CHOICE', '연산자 우선순위에 대한 바람직한 태도는?', '혼동될 수 있는 식에는 괄호를 써서 의도를 드러낸다', '괄호와 중간 변수로 의도를 명확히 하는 것이 더 중요합니다.', 'BRONZE', NULL, 1),
  (30703, @java_subject_id, 3, 307, 'MULTIPLE_CHOICE', '다음 문장의 출력 결과는?
```java
System.out.println((2 + 3) * 4);
```', '20', '괄호 안 2 + 3 = 5가 먼저 계산되고 4를 곱해 20입니다.', 'BRONZE', NULL, 1),
  (30704, @java_subject_id, 3, 307, 'MULTIPLE_CHOICE', '할인 후 금액에 수량을 곱하려 한다. 빈칸에 들어갈 기호는?
```java
int price = (basePrice - discount) ____ quantity;
```', '*', '할인 적용된 단가에 수량을 곱합니다.', 'BRONZE', NULL, 1),
  (30705, @java_subject_id, 3, 307, 'SHORT_ANSWER', '같은 우선순위의 산술 연산자는 일반적으로 어느 방향부터 계산되는지 쓰시오.', '왼쪽부터', '단, 대입처럼 방향이 다른 연산도 있습니다.', 'BRONZE', NULL, 1),
  (30706, @java_subject_id, 3, 307, 'SHORT_ANSWER', '비교 연산과 논리 연산 중 먼저 계산되는 것을 쓰시오.', '비교 연산', '비교 결과(boolean)에 논리 연산이 적용됩니다.', 'BRONZE', NULL, 1),
  (30707, @java_subject_id, 3, 307, 'CODE_OUTPUT', '다음 문장의 출력 결과를 쓰시오.
```java
System.out.println(10 - 4 - 3);
```', '3', '왼쪽부터 계산되어 (10 - 4) - 3 = 3입니다.', 'BRONZE', NULL, 1),
  (30708, @java_subject_id, 3, 307, 'CODE_OUTPUT', '다음 문장의 출력 결과를 쓰시오.
```java
System.out.println(3 > 2 && 1 > 2);
```', 'false', '비교가 먼저 계산되어 true && false = false입니다.', 'BRONZE', NULL, 1),
  (30709, @java_subject_id, 3, 307, 'CODE_SHORT', 'basePrice에서 discount를 뺀 값에 quantity를 곱한 결과를 int 변수 `price`에 저장하는 문장을 괄호를 사용해 작성하시오.', '`int price = (basePrice - discount) * quantity;`', '괄호가 없으면 곱셈이 먼저 계산되어 의도와 달라집니다.', 'BRONZE', NULL, 1),
  (30710, @java_subject_id, 3, 307, 'CODE_OUTPUT', '다음 문장의 출력 결과를 쓰시오.
```java
System.out.println(100 / 10 * 2);
```', '20', '같은 우선순위는 왼쪽부터 계산되어 (100 / 10) * 2 = 20입니다.', 'BRONZE', NULL, 1),
  (30801, @java_subject_id, 3, 308, 'MULTIPLE_CHOICE', '삼항 연산자의 형태로 옳은 것은?', '조건 ? 값1 : 값2', '조건이 참이면 값1, 거짓이면 값2가 선택됩니다.', 'BRONZE', NULL, 1),
  (30802, @java_subject_id, 3, 308, 'MULTIPLE_CHOICE', '두 갈래에서 여러 문장을 실행해야 할 때 알맞은 것은?', 'if-else문', '삼항은 값 하나를 선택할 때 적합하고, 여러 문장은 if-else가 좋습니다.', 'BRONZE', NULL, 1),
  (30803, @java_subject_id, 3, 308, 'MULTIPLE_CHOICE', '다음 코드의 출력 결과는?
```java
int score = 76;
System.out.println(score >= 70 ? "통과" : "재도전");
```', '통과', '76 >= 70이 참이므로 앞의 값 "통과"가 선택됩니다.', 'BRONZE', NULL, 1),
  (30804, @java_subject_id, 3, 308, 'MULTIPLE_CHOICE', '빈칸에 들어갈 기호는?
```java
String r = score >= 60 ____ "합격" : "불합격";
```', '?', '조건 뒤에는 ?가 옵니다.', 'BRONZE', NULL, 1),
  (30805, @java_subject_id, 3, 308, 'SHORT_ANSWER', '삼항 연산자에서 참·거짓 값의 위치를 서로 바꾸면 결과가 어떻게 되는지 쓰시오.', '반대가 된다', '조건이 같아도 값의 위치가 바뀌면 선택 결과가 뒤집힙니다.', 'BRONZE', NULL, 1),
  (30806, @java_subject_id, 3, 308, 'SHORT_ANSWER', '삼항 연산자의 두 결과 값이 갖춰야 할 자료형 조건을 쓰시오.', '서로 호환되는 자료형이어야 한다', '두 값이 호환되지 않으면 컴파일되지 않습니다.', 'BRONZE', NULL, 1),
  (30807, @java_subject_id, 3, 308, 'CODE_OUTPUT', '다음 코드의 출력 결과를 쓰시오.
```java
int a = 3, b = 7;
System.out.println(a > b ? a : b);
```', '7', 'a > b가 거짓이므로 뒤의 값 b(7)가 선택됩니다. 최댓값 선택 패턴입니다.', 'BRONZE', NULL, 1),
  (30808, @java_subject_id, 3, 308, 'CODE_OUTPUT', '다음 코드에서 r에 저장되는 값을 쓰시오.
```java
int n = 4;
String r = n % 2 == 0 ? "짝수" : "홀수";
```', '짝수', '4 % 2 == 0은 참이므로 "짝수"가 저장됩니다.', 'BRONZE', NULL, 1),
  (30809, @java_subject_id, 3, 308, 'CODE_SHORT', 'score가 70 이상이면 "통과", 아니면 "재도전"을 String 변수 `result`에 저장하는 문장을 삼항 연산자로 작성하시오.', '`String result = score >= 70 ? "통과" : "재도전";`', '값 하나를 선택해 저장하는 삼항의 대표 용례입니다.', 'BRONZE', NULL, 1),
  (30810, @java_subject_id, 3, 308, 'CODE_OUTPUT', '다음 문장의 출력 결과를 쓰시오.
```java
System.out.println(10 > 5 ? 1 : 2);
```', '1', '10 > 5가 참이므로 앞의 값 1이 출력됩니다.', 'BRONZE', NULL, 1),
  (30901, @java_subject_id, 3, 309, 'MULTIPLE_CHOICE', '짝수를 판별하는 식으로 옳은 것은?', 'n % 2 == 0', '2로 나눈 나머지가 0이면 짝수입니다.', 'BRONZE', NULL, 1),
  (30902, @java_subject_id, 3, 309, 'MULTIPLE_CHOICE', 'number가 3의 배수인지 판별하는 식은?', 'number % 3 == 0', '3으로 나눈 나머지가 0이면 3의 배수입니다.', 'BRONZE', NULL, 1),
  (30903, @java_subject_id, 3, 309, 'MULTIPLE_CHOICE', '다음 코드의 출력 결과는?
```java
int n = 9;
System.out.println(n % 2 == 0);
```', 'false', '9는 홀수이므로 나머지가 1이라 false입니다.', 'BRONZE', NULL, 1),
  (30904, @java_subject_id, 3, 309, 'MULTIPLE_CHOICE', '마지막 자릿수를 구하려 한다. 빈칸에 들어갈 기호는?
```java
int last = number ____ 10;
```', '%', '10으로 나눈 나머지가 마지막 자릿수입니다.', 'BRONZE', NULL, 1),
  (30905, @java_subject_id, 3, 309, 'SHORT_ANSWER', '반복 번호가 N번째마다 특정 작업을 실행하고 싶을 때 나머지를 활용하는 조건식의 형태를 쓰시오.', '`번호 % N == 0`', 'N으로 나누어떨어질 때마다 실행하면 주기적 처리가 됩니다.', 'BRONZE', NULL, 1),
  (30906, @java_subject_id, 3, 309, 'SHORT_ANSWER', '`145 % 10`의 결과와 그 의미를 쓰시오.', '5, 정수 145의 마지막 자릿수', '10으로 나눈 나머지는 일의 자리 숫자입니다.', 'BRONZE', NULL, 1),
  (30907, @java_subject_id, 3, 309, 'CODE_OUTPUT', '다음 문장의 출력 결과를 쓰시오.
```java
System.out.println(145 % 10);
```', '5', '145를 10으로 나눈 나머지는 5입니다.', 'BRONZE', NULL, 1),
  (30908, @java_subject_id, 3, 309, 'CODE_OUTPUT', '다음 문장의 출력 결과를 쓰시오.
```java
System.out.println(12 % 3 == 0 ? "배수" : "아님");
```', '배수', '12는 3으로 나누어떨어지므로 "배수"가 선택됩니다.', 'BRONZE', NULL, 1),
  (30909, @java_subject_id, 3, 309, 'CODE_SHORT', 'number가 5의 배수인지의 결과를 boolean 변수 `isMultiple`에 저장하는 문장 한 줄을 작성하시오.', '`boolean isMultiple = number % 5 == 0;`', '나머지가 0이면 배수입니다.', 'BRONZE', NULL, 1),
  (30910, @java_subject_id, 3, 309, 'CODE_OUTPUT', '다음 문장의 출력 결과를 쓰시오.
```java
System.out.println(7 % 7);
```', '0', '자기 자신으로 나누면 나머지는 0입니다.', 'BRONZE', NULL, 1),
  (31001, @java_subject_id, 3, 310, 'MULTIPLE_CHOICE', '복잡한 식을 다룰 때 권장되는 방법은?', '계산값과 조건값을 변수로 분리한다', '변수로 분리하면 오류를 찾기 쉽습니다.', 'BRONZE', NULL, 1),
  (31002, @java_subject_id, 3, 310, 'MULTIPLE_CHOICE', '종합식을 읽는 올바른 순서는?', '괄호 안 계산 → 산술 → 비교 → 논리', '괄호, 산술, 비교, 논리 순서로 나누어 읽습니다.', 'BRONZE', NULL, 1),
  (31003, @java_subject_id, 3, 310, 'MULTIPLE_CHOICE', '다음 코드의 출력 결과는?
```java
int score = 76;
boolean passed = score >= 70;
System.out.println(passed ? "통과" : "재도전");
```', '통과', '조건 결과를 변수로 분리한 뒤 삼항으로 출력하는 패턴입니다.', 'BRONZE', NULL, 1),
  (31004, @java_subject_id, 3, 310, 'MULTIPLE_CHOICE', '총액이 10000을 초과하는지 검사하려 한다. 빈칸에 들어갈 연산자는?
```java
int total = price * count;
boolean over = total ____ 10000;
```', '>', '''초과''는 경계값을 포함하지 않으므로 >를 사용합니다.', 'BRONZE', NULL, 1),
  (31005, @java_subject_id, 3, 310, 'SHORT_ANSWER', '할인 금액처럼 계산 순서가 결과에 영향을 주는 문제에서 나눗셈과 관련해 함께 확인해야 할 것을 쓰시오.', '정수 나눗셈 여부와 자료형 변환', 'int 나눗셈은 소수를 버리므로 계산 순서·타입을 확인합니다.', 'BRONZE', NULL, 1),
  (31006, @java_subject_id, 3, 310, 'SHORT_ANSWER', '코드 입력 문제를 풀 때 권장되는 접근 순서를 쓰시오.', '필요한 변수와 자료형 결정 → 계산식 검증 → 조건문 작성', '단계를 나누면 실수를 줄일 수 있습니다.', 'BRONZE', NULL, 1),
  (31007, @java_subject_id, 3, 310, 'CODE_OUTPUT', '다음 코드의 출력 결과를 쓰시오.
```java
int price = 3000, count = 3;
System.out.println("합계: " + (price * count));
```', '합계: 9000', '괄호 안 곱셈이 먼저 계산된 뒤 문자열과 연결됩니다.', 'BRONZE', NULL, 1),
  (31008, @java_subject_id, 3, 310, 'CODE_OUTPUT', '다음 코드의 출력 결과를 쓰시오.
```java
int s = 65;
System.out.println(s >= 70 && s % 2 == 0);
```', 'false', '왼쪽 조건(65 >= 70)이 false이므로 전체가 false입니다.', 'BRONZE', NULL, 1),
  (31009, @java_subject_id, 3, 310, 'CODE_SHORT', '총점 `total`과 인원 `count`로 소수점이 있는 평균을 double 변수 `avg`에 저장하는 문장 한 줄을 작성하시오.', '`double avg = (double) total / count;`', '나누기 전에 한쪽을 double로 변환해야 소수 평균이 나옵니다.', 'BRONZE', NULL, 1),
  (31010, @java_subject_id, 3, 310, 'CODE_OUTPUT', '다음 코드의 출력 결과를 쓰시오.
```java
int a = 10, b = 3;
System.out.println(a / b + "." + a % b);
```', '3.1', 'a/b=3(몫), a%b=1(나머지)이 문자열 "."과 연결되어 3.1로 출력됩니다.', 'BRONZE', NULL, 1),
  (40101, @java_subject_id, 4, 401, 'MULTIPLE_CHOICE', 'if문 본문이 실행되는 조건은?', '조건이 true일 때', 'if문은 조건이 true일 때만 블록을 실행합니다.', 'BRONZE', NULL, 1),
  (40102, @java_subject_id, 4, 401, 'MULTIPLE_CHOICE', 'if문의 조건식 자리에 올 수 있는 것은?', 'boolean 값이나 비교식', 'Java의 if 조건에는 boolean 결과가 와야 합니다.', 'BRONZE', NULL, 1),
  (40103, @java_subject_id, 4, 401, 'MULTIPLE_CHOICE', '다음 코드의 출력 결과는?
```java
int score = 80;
if (score >= 70) {
    System.out.println("통과");
}
```', '통과', '80 >= 70이 참이므로 블록이 실행됩니다.', 'BRONZE', NULL, 1),
  (40104, @java_subject_id, 4, 401, 'MULTIPLE_CHOICE', 'if문 조건식을 감싸는 기호는?', '괄호 ( )', '조건식은 소괄호로 감싸고, 본문은 중괄호로 감쌉니다.', 'BRONZE', NULL, 1),
  (40105, @java_subject_id, 4, 401, 'SHORT_ANSWER', '본문이 한 줄이어도 중괄호를 쓰는 것이 권장되는 이유를 쓰시오.', '나중에 문장을 추가할 때 생기는 실수를 막을 수 있어 안전하기 때문', '중괄호가 없으면 첫 문장만 조건에 묶입니다.', 'BRONZE', NULL, 1),
  (40106, @java_subject_id, 4, 401, 'SHORT_ANSWER', '서로 독립적인 조건을 각각 검사하려면 if문을 어떻게 구성해야 하는지 쓰시오.', 'if문을 여러 개(각각) 사용한다', '앞 조건이 참일 때 뒤를 검사하지 않으려면 else-if를 씁니다.', 'BRONZE', NULL, 1),
  (40107, @java_subject_id, 4, 401, 'CODE_OUTPUT', '다음 코드의 출력 결과를 쓰시오.
```java
int score = 60;
if (score >= 70) {
    System.out.println("통과");
}
System.out.println("끝");
```', '끝', '조건이 거짓이라 블록은 건너뛰고 블록 밖 문장만 실행됩니다.', 'BRONZE', NULL, 1),
  (40108, @java_subject_id, 4, 401, 'FILL_BLANK', 'age가 14 이상일 때 실행되도록 빈칸에 들어갈 연산자를 쓰시오.
```java
if (age ____ 14) { }
```', '>=', '''이상''은 경계값을 포함하므로 >=를 사용합니다.', 'BRONZE', NULL, 1),
  (40109, @java_subject_id, 4, 401, 'CODE_SHORT', '`point`가 0보다 크면 "사용 가능"을 출력하는 if문을 중괄호를 포함해 한 줄로 작성하시오.', '`if (point > 0) { System.out.println("사용 가능"); }`', '조건은 소괄호, 본문은 중괄호로 감쌉니다.', 'BRONZE', NULL, 1),
  (40110, @java_subject_id, 4, 401, 'CODE_OUTPUT', '다음 코드의 출력 결과를 쓰시오.
```java
if (true) { System.out.println("A"); }
if (true) { System.out.println("B"); }
```', 'A와 B 모두 출력된다', '독립된 if문은 각각 검사되어 둘 다 실행됩니다.', 'BRONZE', NULL, 1),
  (40201, @java_subject_id, 4, 402, 'MULTIPLE_CHOICE', 'if-else가 알맞은 상황은?', '서로 배타적인 두 경우 중 하나를 반드시 처리할 때', '로그인 여부·합격 여부처럼 두 갈래 상황에 적합합니다.', 'BRONZE', NULL, 1),
  (40202, @java_subject_id, 4, 402, 'MULTIPLE_CHOICE', '두 블록에서 공통으로 실행할 코드의 위치로 알맞은 것은?', '조건문 밖', '공통 코드는 조건문 밖에 두면 중복이 줄어듭니다.', 'BRONZE', NULL, 1),
  (40203, @java_subject_id, 4, 402, 'MULTIPLE_CHOICE', '다음 코드의 출력 결과는?
```java
boolean loggedIn = false;
if (loggedIn) {
    System.out.println("환영");
} else {
    System.out.println("로그인 필요");
}
```', '로그인 필요', '조건이 false이므로 else 블록이 실행됩니다.', 'BRONZE', NULL, 1),
  (40204, @java_subject_id, 4, 402, 'MULTIPLE_CHOICE', '빈칸에 들어갈 키워드는?
```java
if (score >= 70) { } ____ { }
```', 'else', '거짓일 때의 갈래는 else로 작성합니다.', 'BRONZE', NULL, 1),
  (40205, @java_subject_id, 4, 402, 'SHORT_ANSWER', '두 블록에서 각각 만든 값을 조건문 밖에서도 사용하려면 어떻게 해야 하는지 쓰시오.', '조건문 전에 변수를 선언하고 각 블록에서 값을 대입한다', '블록 안에서 선언한 변수는 블록 밖에서 쓸 수 없습니다.', 'BRONZE', NULL, 1),
  (40206, @java_subject_id, 4, 402, 'SHORT_ANSWER', '조건 경계에서 어느 블록이 선택되는지 확인하는 방법을 쓰시오.', '경계값을 직접 넣어 확인한다', '예: 조건이 >= 70이면 70을 넣어 확인합니다.', 'BRONZE', NULL, 1),
  (40207, @java_subject_id, 4, 402, 'CODE_OUTPUT', '다음 코드의 출력 결과를 쓰시오.
```java
int n = 5;
if (n % 2 == 0) {
    System.out.println("짝수");
} else {
    System.out.println("홀수");
}
```', '홀수', '5 % 2는 1이므로 조건이 거짓이라 else가 실행됩니다.', 'BRONZE', NULL, 1),
  (40208, @java_subject_id, 4, 402, 'FILL_BLANK', '빈칸에 들어갈 키워드를 쓰시오.
```java
if (n % 2 == 0) {
    System.out.println("짝수");
} ____ {
    System.out.println("홀수");
}
```', 'else', '거짓일 때의 갈래는 else 블록으로 작성합니다. 조건이 필요하면 else if를 씁니다.', 'BRONZE', NULL, 1),
  (40209, @java_subject_id, 4, 402, 'CODE_SHORT', 'score가 70 이상이면 "통과", 아니면 "재도전"을 출력하는 if-else문을 한 줄로 작성하시오.', '`if (score >= 70) { System.out.println("통과"); } else { System.out.println("재도전"); }`', '배타적인 두 경우를 각각의 블록으로 처리합니다.', 'BRONZE', NULL, 1),
  (40210, @java_subject_id, 4, 402, 'CODE_OUTPUT', '다음 코드의 출력 결과를 쓰시오.
```java
int a = 10;
if (a > 5) {
    System.out.println("크다");
} else {
    System.out.println("작다");
}
```', '크다', '10 > 5가 참이므로 if 블록이 실행됩니다.', 'BRONZE', NULL, 1),
  (40301, @java_subject_id, 4, 403, 'MULTIPLE_CHOICE', 'else-if 체인의 검사 방식으로 옳은 것은?', '위에서부터 검사해 처음 참이 된 블록 하나만 실행한다', '위에서부터 한 번만 검사하며, 참인 블록 하나만 실행됩니다.', 'BRONZE', NULL, 1),
  (40302, @java_subject_id, 4, 403, 'MULTIPLE_CHOICE', '마지막 else의 역할로 옳은 것은?', '앞의 어떤 조건에도 해당하지 않는 나머지 모든 경우를 처리한다', '마지막 else는 기본값·잘못된 입력 처리 역할도 합니다.', 'BRONZE', NULL, 1),
  (40303, @java_subject_id, 4, 403, 'MULTIPLE_CHOICE', 'score가 85일 때 다음 코드에서 grade에 저장되는 값은?
```java
String grade;
if (score >= 90) { grade = "A"; }
else if (score >= 80) { grade = "B"; }
else { grade = "C"; }
```', 'B', '첫 조건(>=90)은 거짓, 둘째 조건(>=80)이 참이므로 B입니다.', 'BRONZE', NULL, 1),
  (40304, @java_subject_id, 4, 403, 'MULTIPLE_CHOICE', '빈칸에 들어갈 키워드는?
```java
if (score >= 90) { }
____ if (score >= 80) { }
```', 'else', '여러 구간 판단은 else if로 연결합니다.', 'BRONZE', NULL, 1),
  (40305, @java_subject_id, 4, 403, 'SHORT_ANSWER', '등급 판정에서 `score >= 70` 조건을 맨 위에 두면 안 되는 이유를 쓰시오.', '90점도 그 조건에서 걸려 버리므로 높은 기준부터 작성해야 한다', 'else-if는 위에서부터 한 번만 검사합니다.', 'BRONZE', NULL, 1),
  (40306, @java_subject_id, 4, 403, 'SHORT_ANSWER', '각 조건이 겹치지 않도록 범위를 설계하면 좋은 점을 쓰시오.', '조건 순서 때문에 생기는 오류를 줄일 수 있다', '겹치지 않는 범위는 순서에 덜 민감합니다.', 'BRONZE', NULL, 1),
  (40307, @java_subject_id, 4, 403, 'CODE_OUTPUT', '다음 코드의 출력 결과를 쓰시오.
```java
int t = 30;
if (t >= 28) { System.out.println("더움"); }
else if (t >= 15) { System.out.println("보통"); }
else { System.out.println("추움"); }
```', '더움', '첫 조건이 참이므로 아래 조건은 검사하지 않습니다.', 'BRONZE', NULL, 1),
  (40308, @java_subject_id, 4, 403, 'CODE_OUTPUT', '3번 문제의 코드에서 score가 95라면 grade에 저장되는 값을 쓰시오.', 'A', '첫 조건(95 >= 90)이 참이므로 A가 저장됩니다.', 'BRONZE', NULL, 1),
  (40309, @java_subject_id, 4, 403, 'CODE_SHORT', '90 이상이면 "A", 80 이상이면 "B", 나머지는 "C"를 grade에 저장하는 else-if 체인을 한 줄로 작성하시오.', '`if (score >= 90) { grade = "A"; } else if (score >= 80) { grade = "B"; } else { grade = "C"; }`', '높은 기준부터 작성합니다.', 'BRONZE', NULL, 1),
  (40310, @java_subject_id, 4, 403, 'CODE_OUTPUT', '3번 문제의 코드에서 score가 79라면 grade에 저장되는 값을 쓰시오.', 'C', '두 조건 모두 거짓이므로 마지막 else가 실행됩니다.', 'BRONZE', NULL, 1),
  (40401, @java_subject_id, 4, 404, 'MULTIPLE_CHOICE', 'switch문이 적합한 상황은?', '메뉴 번호·요일처럼 정해진 값에 따른 분기', '하나의 값이 여러 case 중 무엇과 같은지 비교할 때 적합합니다.', 'BRONZE', NULL, 1),
  (40402, @java_subject_id, 4, 404, 'MULTIPLE_CHOICE', '화살표(`->`) 문법의 장점으로 옳은 것은?', 'break 누락으로 다음 case까지 실행되는 문제를 줄인다', '화살표 문법은 해당 문장만 실행하므로 break가 필요 없습니다.', 'BRONZE', NULL, 1),
  (40403, @java_subject_id, 4, 404, 'MULTIPLE_CHOICE', '다음 코드의 출력 결과는?
```java
int menu = 1;
switch (menu) {
    case 1 -> System.out.println("조회");
    default -> System.out.println("오류");
}
```', '조회', 'menu가 1이므로 case 1이 실행됩니다.', 'BRONZE', NULL, 1),
  (40404, @java_subject_id, 4, 404, 'MULTIPLE_CHOICE', '어떤 case에도 해당하지 않을 때 실행되는 갈래를 만드는 키워드는?', 'default', 'default는 나머지 모든 값을 처리합니다.', 'BRONZE', NULL, 1),
  (40405, @java_subject_id, 4, 404, 'SHORT_ANSWER', '전통적인 콜론(:) 문법에서 break를 빠뜨리면 어떻게 되는지 쓰시오.', '다음 case까지 이어서 실행된다', '콜론 문법의 대표적인 실수 지점입니다.', 'BRONZE', NULL, 1),
  (40406, @java_subject_id, 4, 404, 'SHORT_ANSWER', 'switch문으로 분기하기에 적합한 값의 예를 두 가지 쓰시오.', '메뉴 번호, 요일', '정해진 값 목록과 하나씩 비교하는 상황에 적합합니다.', 'BRONZE', NULL, 1),
  (40407, @java_subject_id, 4, 404, 'CODE_OUTPUT', '3번 문제의 코드에서 menu가 9라면 출력 결과를 쓰시오.', '오류', '일치하는 case가 없어 default가 실행됩니다.', 'BRONZE', NULL, 1),
  (40408, @java_subject_id, 4, 404, 'FILL_BLANK', '빈칸에 들어갈 변수 이름을 쓰시오.
```java
int menu = 2;
switch (____) {
    case 2 -> System.out.println("등록");
}
```', 'menu', 'switch 괄호에는 비교할 값(변수)을 넣습니다.', 'BRONZE', NULL, 1),
  (40409, @java_subject_id, 4, 404, 'CODE_SHORT', 'day가 1이면 "월요일"을 출력하는 case 한 줄을 화살표 문법으로 작성하시오.', '`case 1 -> System.out.println("월요일");`', '화살표 뒤 문장 하나만 실행됩니다.', 'BRONZE', NULL, 1),
  (40410, @java_subject_id, 4, 404, 'CODE_OUTPUT', '다음 코드의 출력 결과를 쓰시오.
```java
switch (2) {
    case 1 -> System.out.println("A");
    case 2 -> System.out.println("B");
    default -> System.out.println("C");
}
```', 'B', '값 2와 일치하는 case 2만 실행됩니다.', 'BRONZE', NULL, 1),
  (40501, @java_subject_id, 4, 405, 'MULTIPLE_CHOICE', 'for문을 구성하는 세 요소는?', '초기식·조건식·증감식', '`for (초기식; 조건식; 증감식)` 구조입니다.', 'BRONZE', NULL, 1),
  (40502, @java_subject_id, 4, 405, 'MULTIPLE_CHOICE', 'for문의 조건이 계속 참이면 어떻게 되는가?', '무한 반복이 된다', '조건이 거짓이 되지 않으면 반복이 끝나지 않습니다.', 'BRONZE', NULL, 1),
  (40503, @java_subject_id, 4, 405, 'MULTIPLE_CHOICE', '다음 코드의 출력 결과는?
```java
for (int i = 1; i <= 3; i++) {
    System.out.println(i);
}
```', '1 2 3 (세 줄)', '1부터 시작해 3 이하인 동안 1씩 증가하며 출력합니다.', 'BRONZE', NULL, 1),
  (40504, @java_subject_id, 4, 405, 'MULTIPLE_CHOICE', '빈칸에 들어갈 증감식은?
```java
for (int i = 0; i < 5; ____) { }
```', 'i++', '다음 반복을 준비하는 증감식 자리입니다.', 'BRONZE', NULL, 1),
  (40505, @java_subject_id, 4, 405, 'SHORT_ANSWER', '반복 횟수를 계산하려면 for문에서 무엇을 확인해야 하는지 세 가지를 쓰시오.', '시작값, 끝 조건, 증감(변화량·방향)', '세 가지를 분리해 읽으면 출력 범위를 계산할 수 있습니다.', 'BRONZE', NULL, 1),
  (40506, @java_subject_id, 4, 405, 'SHORT_ANSWER', '반복 변수를 for문 안에서 선언하는 이유를 쓰시오.', '반복문 안에서만 쓰는 지역 변수로 범위를 제한하기 위해', '변수 범위를 작게 유지하면 실수를 줄일 수 있습니다.', 'BRONZE', NULL, 1),
  (40507, @java_subject_id, 4, 405, 'CODE_OUTPUT', '다음 코드의 출력 결과를 쓰시오.
```java
for (int i = 0; i < 3; i++) {
    System.out.print("*");
}
```', '***', 'print는 줄바꿈 없이 별 세 개를 이어 출력합니다.', 'BRONZE', NULL, 1),
  (40508, @java_subject_id, 4, 405, 'CODE_OUTPUT', '다음 for문이 처음 출력하는 값을 쓰시오.
```java
for (int i = 5; i >= 1; i--) {
    System.out.println(i);
}
```', '5', '감소 반복은 시작값 5부터 출력합니다.', 'BRONZE', NULL, 1),
  (40509, @java_subject_id, 4, 405, 'CODE_SHORT', '1부터 10까지 출력하는 for문을 한 줄로 작성하시오.', '`for (int i = 1; i <= 10; i++) { System.out.println(i); }`', '시작 1, 조건 <= 10, 증가 1의 조합입니다.', 'BRONZE', NULL, 1),
  (40510, @java_subject_id, 4, 405, 'CODE_OUTPUT', '다음 코드가 출력하는 값을 모두 쓰시오.
```java
for (int i = 2; i <= 6; i += 2) {
    System.out.println(i);
}
```', '2, 4, 6', '2씩 증가하는 반복도 가능합니다.', 'BRONZE', NULL, 1),
  (40601, @java_subject_id, 4, 406, 'MULTIPLE_CHOICE', 'while문의 조건 검사 시점은?', '본문 실행 전', 'while문은 반복 전에 조건을 먼저 검사합니다.', 'BRONZE', NULL, 1),
  (40602, @java_subject_id, 4, 406, 'MULTIPLE_CHOICE', 'while 조건이 처음부터 false면 어떻게 되는가?', '한 번도 실행되지 않는다', '조건을 먼저 검사하므로 본문이 실행되지 않을 수 있습니다.', 'BRONZE', NULL, 1),
  (40603, @java_subject_id, 4, 406, 'MULTIPLE_CHOICE', '다음 코드의 출력 결과는?
```java
int count = 1;
while (count <= 3) {
    System.out.println(count);
    count++;
}
```', '1 2 3 (세 줄)', 'count가 1, 2, 3일 때 출력되고 4가 되면 종료됩니다.', 'BRONZE', NULL, 1),
  (40604, @java_subject_id, 4, 406, 'MULTIPLE_CHOICE', 'while 조건식 자리에 와야 하는 것은?', 'boolean 결과가 나오는 식', '조건식은 boolean이어야 합니다.', 'BRONZE', NULL, 1),
  (40605, @java_subject_id, 4, 406, 'SHORT_ANSWER', '종료 조건에 쓰이는 값이 반복 안에서 반드시 지켜야 할 조건을 쓰시오.', '반복 안에서 값이 바뀌어야 한다', '바뀌지 않으면 무한 반복이 됩니다.', 'BRONZE', NULL, 1),
  (40606, @java_subject_id, 4, 406, 'SHORT_ANSWER', 'while문이 적합한 상황의 예를 한 가지 쓰시오.', '종료 명령이 들어올 때까지 입력을 반복하는 구조', '횟수가 아니라 조건 기반 반복에 적합합니다.', 'BRONZE', NULL, 1),
  (40607, @java_subject_id, 4, 406, 'CODE_OUTPUT', '다음 코드의 출력 결과를 쓰시오.
```java
int n = 10;
while (n < 5) {
    System.out.println(n);
}
System.out.println("끝");
```', '끝', '조건이 처음부터 거짓이라 본문은 실행되지 않습니다.', 'BRONZE', NULL, 1),
  (40608, @java_subject_id, 4, 406, 'CODE_OUTPUT', '다음 코드의 출력 결과를 쓰시오.
```java
int i = 0;
while (i < 2) {
    i++;
}
System.out.println(i);
```', '2', 'i가 0→1→2로 증가하고 조건이 거짓이 되어 종료됩니다.', 'BRONZE', NULL, 1),
  (40609, @java_subject_id, 4, 406, 'CODE_SHORT', 'count가 5 미만인 동안 count를 1씩 증가시키는 while문을 한 줄로 작성하시오.', '`while (count < 5) { count++; }`', '조건에 쓰인 값이 본문에서 변경됩니다.', 'BRONZE', NULL, 1),
  (40610, @java_subject_id, 4, 406, 'CODE_OUTPUT', '3번 문제의 코드에서 `count++;`를 빠뜨리면 어떻게 되는지 쓰시오.', '조건이 계속 참이라 무한 반복이 된다', '종료 조건 값이 바뀌지 않으면 반복이 끝나지 않습니다.', 'BRONZE', NULL, 1),
  (40701, @java_subject_id, 4, 407, 'MULTIPLE_CHOICE', 'do-while문의 특징으로 옳은 것은?', '본문을 한 번 실행한 뒤 조건을 검사한다', 'do-while은 최소 한 번은 본문을 실행합니다.', 'BRONZE', NULL, 1),
  (40702, @java_subject_id, 4, 407, 'MULTIPLE_CHOICE', 'do-while 문법의 특징으로 옳은 것은?', 'while 뒤 조건식 끝에 세미콜론이 필요하다', '`} while (조건);`처럼 끝에 세미콜론을 빼뜨리지 않아야 합니다.', 'BRONZE', NULL, 1),
  (40703, @java_subject_id, 4, 407, 'MULTIPLE_CHOICE', '다음 코드의 출력 결과는?
```java
do {
    System.out.println("메뉴");
} while (false);
```', '메뉴 (한 번)', '조건이 false여도 본문은 최소 한 번 실행됩니다.', 'BRONZE', NULL, 1),
  (40704, @java_subject_id, 4, 407, 'MULTIPLE_CHOICE', '빈칸에 들어갈 기호는?
```java
do { } while (choice != 0)____
```', ';', 'do-while은 조건식 뒤 세미콜론으로 끝납니다.', 'BRONZE', NULL, 1),
  (40705, @java_subject_id, 4, 407, 'SHORT_ANSWER', 'do-while문이 적합한 상황의 예를 한 가지 쓰시오.', '사용자가 최소 한 번은 봐야 하는 메뉴·입력 화면', '본문을 먼저 실행하는 특성 때문입니다.', 'BRONZE', NULL, 1),
  (40706, @java_subject_id, 4, 407, 'SHORT_ANSWER', 'while문과 do-while문의 실행 횟수가 달라지는 경우를 쓰시오.', '조건이 처음부터 false일 때', 'while은 0번, do-while은 1번 실행됩니다.', 'BRONZE', NULL, 1),
  (40707, @java_subject_id, 4, 407, 'CODE_OUTPUT', '다음 코드의 출력 결과를 쓰시오.
```java
int i = 5;
do {
    System.out.println(i);
    i++;
} while (i < 3);
```', '5', '본문이 한 번 실행된 뒤 조건(6 < 3)이 거짓이라 종료됩니다.', 'BRONZE', NULL, 1),
  (40708, @java_subject_id, 4, 407, 'CODE_OUTPUT', '다음 코드의 출력 결과를 쓰시오.
```java
int i = 1;
do {
    System.out.print(i);
    i++;
} while (i <= 3);
```', '123', '1, 2, 3이 줄바꿈 없이 이어 출력됩니다.', 'BRONZE', NULL, 1),
  (40709, @java_subject_id, 4, 407, 'CODE_SHORT', '"안내"를 최소 한 번 출력하고 flag가 true인 동안 반복하는 do-while문을 한 줄로 작성하시오.', '`do { System.out.println("안내"); } while (flag);`', '끝의 세미콜론을 빼뜨리지 않습니다.', 'BRONZE', NULL, 1),
  (40710, @java_subject_id, 4, 407, 'CODE_OUTPUT', '`while (false) { System.out.println("A"); }`와 `do { System.out.println("A"); } while (false);`의 출력 차이를 쓰시오.', '앞은 아무것도 출력하지 않고, 뒤는 A를 한 번 출력한다', '조건 검사 시점의 차이입니다.', 'BRONZE', NULL, 1),
  (40801, @java_subject_id, 4, 408, 'MULTIPLE_CHOICE', 'break의 동작으로 옳은 것은?', '가장 가까운 반복문 또는 switch문을 즉시 끝낸다', 'break는 가장 가까운 반복문·switch를 종료합니다.', 'BRONZE', NULL, 1),
  (40802, @java_subject_id, 4, 408, 'MULTIPLE_CHOICE', 'continue의 동작으로 옳은 것은?', '현재 반복의 남은 문장을 건너뛰고 다음 반복으로 이동한다', 'continue는 현재 차례만 건너뛰고 반복 자체는 계속됩니다.', 'BRONZE', NULL, 1),
  (40803, @java_subject_id, 4, 408, 'MULTIPLE_CHOICE', '다음 코드가 출력하는 값을 모두 고르면?
```java
for (int i = 1; i <= 5; i++) {
    if (i == 3) { break; }
    System.out.println(i);
}
```', '1 2', 'i가 3일 때 break로 반복이 끝나 1, 2만 출력됩니다.', 'BRONZE', NULL, 1),
  (40804, @java_subject_id, 4, 408, 'MULTIPLE_CHOICE', '제외할 값만 건너뛰고 반복을 계속하려 할 때 사용하는 키워드는?', 'continue', '잘못된 데이터 제외 등에는 continue가 적합합니다.', 'BRONZE', NULL, 1),
  (40805, @java_subject_id, 4, 408, 'SHORT_ANSWER', '중첩 반복문 안의 break가 영향을 주는 범위를 쓰시오.', '가장 안쪽(가장 가까운) 반복문만', '바깥 반복문까지 끝내려면 구조를 나누는 등 별도 방법이 필요합니다.', 'BRONZE', NULL, 1),
  (40806, @java_subject_id, 4, 408, 'SHORT_ANSWER', '검색 대상에서 원하는 값을 찾았을 때 불필요한 반복을 멈추는 데 알맞은 키워드를 쓰시오.', 'break', '검색 완료에는 break, 제외 처리에는 continue를 사용합니다.', 'BRONZE', NULL, 1),
  (40807, @java_subject_id, 4, 408, 'CODE_OUTPUT', '다음 코드가 출력하는 값을 모두 쓰시오.
```java
for (int i = 1; i <= 5; i++) {
    if (i == 3) { continue; }
    System.out.println(i);
}
```', '1, 2, 4, 5', 'i가 3일 때만 건너뛰고 반복은 계속됩니다.', 'BRONZE', NULL, 1),
  (40808, @java_subject_id, 4, 408, 'CODE_OUTPUT', '다음 코드의 출력 결과를 쓰시오.
```java
for (int i = 1; i <= 4; i++) {
    if (i % 2 == 0) { continue; }
    System.out.print(i);
}
```', '13', '짝수는 건너뛰고 홀수 1, 3만 이어 출력됩니다.', 'BRONZE', NULL, 1),
  (40809, @java_subject_id, 4, 408, 'CODE_SHORT', 'number가 target과 같으면 반복을 종료하는 if문을 한 줄로 작성하시오.', '`if (number == target) { break; }`', '검색 성공 시 반복을 멈추는 전형적인 패턴입니다.', 'BRONZE', NULL, 1),
  (40810, @java_subject_id, 4, 408, 'CODE_OUTPUT', 'for문에서 continue가 실행된 뒤 증감식은 실행되는지 쓰시오.', '실행된다', 'for문에서는 continue 뒤에도 증감식이 실행되어 다음 반복으로 넘어갑니다.', 'BRONZE', NULL, 1),
  (40901, @java_subject_id, 4, 409, 'MULTIPLE_CHOICE', '중첩 제어문에 대한 설명으로 옳은 것은?', '조건문 안에 조건문, 반복문 안에 반복문을 넣을 수 있다', '제어문은 서로 중첩해 사용할 수 있습니다.', 'BRONZE', NULL, 1),
  (40902, @java_subject_id, 4, 409, 'MULTIPLE_CHOICE', '중첩 반복문의 전체 실행 횟수를 구하는 방법은?', '바깥 반복 횟수 × 안쪽 반복 횟수', '바깥 1회당 안쪽이 전부 실행되므로 곱합니다.', 'BRONZE', NULL, 1),
  (40903, @java_subject_id, 4, 409, 'MULTIPLE_CHOICE', '다음 코드가 출력하는 줄 수는?
```java
for (int row = 1; row <= 2; row++) {
    for (int col = 1; col <= 3; col++) {
        System.out.println(row + "," + col);
    }
}
```', '6줄', '2 × 3 = 6줄이 출력됩니다.', 'BRONZE', NULL, 1),
  (40904, @java_subject_id, 4, 409, 'MULTIPLE_CHOICE', '중첩 반복에서 행·열을 다룰 때 변수 이름 관례로 알맞은 짝은?', 'row·col', '역할이 드러나는 이름(row, col)을 쓰면 흐름 구분이 쉽습니다.', 'BRONZE', NULL, 1),
  (40905, @java_subject_id, 4, 409, 'SHORT_ANSWER', '바깥 반복이 한 번 돌 때 안쪽 반복은 어떻게 실행되는지 쓰시오.', '안쪽 반복이 처음부터 끝까지 모두 실행된다', '이 순서를 표로 추적하면 이해하기 쉽습니다.', 'BRONZE', NULL, 1),
  (40906, @java_subject_id, 4, 409, 'SHORT_ANSWER', '중첩 반복의 실행 흐름을 정확히 이해하는 데 도움이 되는 방법을 쓰시오.', '반복마다 변수값을 표로 추적한다', 'row·col 값의 변화를 적어 보면 순서가 보입니다.', 'BRONZE', NULL, 1),
  (40907, @java_subject_id, 4, 409, 'CODE_OUTPUT', '다음 코드의 출력 결과를 쓰시오.
```java
for (int i = 1; i <= 2; i++) {
    for (int j = 1; j <= 2; j++) {
        System.out.print(i * j + " ");
    }
}
```', '1 2 2 4', '(1,1)(1,2)(2,1)(2,2) 순서로 곱이 출력됩니다.', 'BRONZE', NULL, 1),
  (40908, @java_subject_id, 4, 409, 'CODE_OUTPUT', '3번 문제의 코드가 가장 먼저 출력하는 한 줄을 쓰시오.', '1,1', '바깥 row=1일 때 안쪽 col=1부터 시작합니다.', 'BRONZE', NULL, 1),
  (40909, @java_subject_id, 4, 409, 'CODE_SHORT', '별(*) 3개를 한 줄에 이어 출력하는 안쪽 for문을 한 줄로 작성하시오.', '`for (int j = 1; j <= 3; j++) { System.out.print("*"); }`', '바깥 반복과 결합하면 별 사각형을 만들 수 있습니다.', 'BRONZE', NULL, 1),
  (40910, @java_subject_id, 4, 409, 'CODE_OUTPUT', '다음 코드의 출력 결과를 쓰시오.
```java
for (int i = 1; i <= 3; i++) {
    if (i % 2 == 1) {
        System.out.print(i);
    }
}
```', '13', '반복문 안 조건문이 홀수일 때만 출력합니다.', 'BRONZE', NULL, 1),
  (41001, @java_subject_id, 4, 410, 'MULTIPLE_CHOICE', '제어문 문제를 정확히 푸는 방법으로 가장 알맞은 것은?', '반복마다 변수값·조건 결과를 표로 추적한다', 'i, 누적값, 조건 결과를 적어 보면 예측 오류가 줄어듭니다.', 'BRONZE', NULL, 1),
  (41002, @java_subject_id, 4, 410, 'MULTIPLE_CHOICE', '입력을 고려할 때 나누어 생각해야 할 세 가지는?', '정상값·경계값·잘못된 값', '정상·경계·예외 입력을 나눠 검토합니다.', 'BRONZE', NULL, 1),
  (41003, @java_subject_id, 4, 410, 'MULTIPLE_CHOICE', '다음 코드가 출력하는 값을 모두 고르면?
```java
int[] scores = {80, 65, 90};
for (int score : scores) {
    if (score >= 70) {
        System.out.println(score);
    }
}
```', '80, 90', '70 이상인 80과 90만 출력됩니다.', 'BRONZE', NULL, 1),
  (41004, @java_subject_id, 4, 410, 'MULTIPLE_CHOICE', '메뉴 프로그램의 기본 구조는 반복문으로 메뉴를 계속 보여 주고, 선택 처리는 무엇으로 하는가?', '조건문이나 switch', '반복 + 분기(조건문/switch) 조합이 메뉴 프로그램의 뼈대입니다.', 'BRONZE', NULL, 1),
  (41005, @java_subject_id, 4, 410, 'SHORT_ANSWER', '메뉴 프로그램에서 종료 메뉴를 선택했을 때 해야 할 처리를 쓰시오.', '반복을 끝낸다(break 등)', '종료 조건이 분명해야 프로그램이 정상 종료됩니다.', 'BRONZE', NULL, 1),
  (41006, @java_subject_id, 4, 410, 'SHORT_ANSWER', '잘못된 메뉴 값이 입력됐을 때 바람직한 처리를 쓰시오.', '안내 메시지를 출력한다', '프로그램이 죽지 않고 다시 안내하도록 처리합니다.', 'BRONZE', NULL, 1),
  (41007, @java_subject_id, 4, 410, 'CODE_OUTPUT', '다음 코드의 출력 결과를 쓰시오.
```java
int sum = 0;
for (int i = 1; i <= 4; i++) {
    sum += i;
}
System.out.println(sum);
```', '10', '1+2+3+4 = 10이 누적됩니다.', 'BRONZE', NULL, 1),
  (41008, @java_subject_id, 4, 410, 'CODE_OUTPUT', '다음 코드의 출력 결과를 쓰시오.
```java
for (int i = 1; i <= 5; i++) {
    if (i == 4) { break; }
    System.out.print(i);
}
```', '123', 'i가 4일 때 반복이 끝나 1, 2, 3만 출력됩니다.', 'BRONZE', NULL, 1),
  (41009, @java_subject_id, 4, 410, 'CODE_SHORT', '1부터 100까지 중 3의 배수만 출력하는 for문을 한 줄로 작성하시오.', '`for (int i = 1; i <= 100; i++) { if (i % 3 == 0) { System.out.println(i); } }`', '반복문 안에 조건문(나머지 판별)을 중첩합니다.', 'BRONZE', NULL, 1),
  (41010, @java_subject_id, 4, 410, 'CODE_OUTPUT', '다음 코드의 출력 결과를 쓰시오.
```java
int c = 0;
while (c < 3) {
    c++;
    if (c == 2) { continue; }
    System.out.print(c);
}
```', '13', 'c가 2일 때만 출력을 건너뛰어 1과 3이 출력됩니다.', 'BRONZE', NULL, 1),
  (50101, @java_subject_id, 5, 501, 'MULTIPLE_CHOICE', '배열에 대한 설명으로 옳은 것은?', '같은 자료형 값을 순서대로 저장하며 크기는 생성 후 바꿀 수 없다', '배열은 같은 타입 값의 묶음이고 크기가 고정입니다.', 'BRONZE', NULL, 1),
  (50102, @java_subject_id, 5, 501, 'MULTIPLE_CHOICE', '배열 변수를 다른 변수에 대입하면 어떻게 되는가?', '두 변수가 같은 배열을 함께 가리킨다', '배열 변수는 배열 객체를 가리키므로 대입은 복사가 아닙니다.', 'BRONZE', NULL, 1),
  (50103, @java_subject_id, 5, 501, 'MULTIPLE_CHOICE', '다음 코드의 출력 결과는?
```java
int[] scores = {80, 90, 70};
System.out.println(scores[0]);
```', '80', '인덱스 0은 첫 번째 요소입니다.', 'BRONZE', NULL, 1),
  (50104, @java_subject_id, 5, 501, 'MULTIPLE_CHOICE', '빈칸에 들어갈 기호는?
```java
int____ scores = {80, 90};
```', '[ ]', '배열 선언은 `int[]`처럼 대괄호를 사용합니다.', 'BRONZE', NULL, 1),
  (50105, @java_subject_id, 5, 501, 'SHORT_ANSWER', '배열의 크기를 생성한 뒤에 바꿀 수 있는지 쓰시오.', '없다', '배열 크기는 생성 시 정해지며 변경할 수 없습니다.', 'BRONZE', NULL, 1),
  (50106, @java_subject_id, 5, 501, 'SHORT_ANSWER', '배열 자체를 복사하려면 어떻게 해야 하는지 쓰시오.', '새 배열을 만들고 요소를 별도로 옮긴다', '변수 대입은 같은 배열을 가리키게 할 뿐입니다.', 'BRONZE', NULL, 1),
  (50107, @java_subject_id, 5, 501, 'CODE_OUTPUT', '다음 코드의 출력 결과를 쓰시오.
```java
int[] a = {1, 2};
int[] b = a;
b[0] = 9;
System.out.println(a[0]);
```', '9', 'a와 b가 같은 배열을 가리키므로 b로 바꾼 값이 a에서도 보입니다.', 'BRONZE', NULL, 1),
  (50108, @java_subject_id, 5, 501, 'FILL_BLANK', '빈칸에 들어갈 자료형을 쓰시오. (실수 세 개를 담는 배열)
```java
____[] heights = {150.5, 162.0, 171.3};
```', 'double', '실수 값을 담는 배열은 double[]로 선언합니다.', 'BRONZE', NULL, 1),
  (50109, @java_subject_id, 5, 501, 'CODE_SHORT', '80, 90, 70을 담는 int 배열 `scores`를 선언과 동시에 초기화하는 문장 한 줄을 작성하시오.', '`int[] scores = {80, 90, 70};`', '선언과 동시에 중괄호 초기화를 쓸 수 있습니다.', 'BRONZE', NULL, 1),
  (50110, @java_subject_id, 5, 501, 'CODE_OUTPUT', '다음 코드의 출력 결과를 쓰시오.
```java
int[] x = {5};
int[] y = x;
y[0] = 7;
System.out.println(x[0] + y[0]);
```', '14', '같은 배열을 가리키므로 x[0]도 7이 되어 7 + 7 = 14입니다.', 'BRONZE', NULL, 1),
  (50201, @java_subject_id, 5, 502, 'MULTIPLE_CHOICE', '`new int[3]`으로 만든 배열 요소의 초기값은?', '0', 'int 배열은 0으로 초기화됩니다.', 'BRONZE', NULL, 1),
  (50202, @java_subject_id, 5, 502, 'MULTIPLE_CHOICE', 'boolean 배열의 초기값은?', 'false', 'boolean 배열은 false, 참조 배열은 null로 시작합니다.', 'BRONZE', NULL, 1),
  (50203, @java_subject_id, 5, 502, 'MULTIPLE_CHOICE', '다음 코드의 출력 결과는?
```java
int[] numbers = new int[3];
System.out.println(numbers[1]);
```', '0', '값을 넣지 않은 int 배열 요소는 0입니다.', 'BRONZE', NULL, 1),
  (50204, @java_subject_id, 5, 502, 'MULTIPLE_CHOICE', '빈칸에 들어갈 키워드는?
```java
int[] numbers = ____ int[5];
```', 'new', '배열 생성에는 new 키워드를 사용합니다.', 'BRONZE', NULL, 1),
  (50205, @java_subject_id, 5, 502, 'SHORT_ANSWER', 'String 배열처럼 참조 타입 배열의 초기값을 쓰시오.', 'null', '참조 배열은 null로 초기화됩니다.', 'BRONZE', NULL, 1),
  (50206, @java_subject_id, 5, 502, 'SHORT_ANSWER', '이미 선언한 배열 변수에 값 목록을 대입하려면 어떤 형식을 써야 하는지 쓰시오.', '`new int[]{...}` 형식', '`{값, 값}`만 쓰는 문법은 선언과 동시에만 가능합니다.', 'BRONZE', NULL, 1),
  (50207, @java_subject_id, 5, 502, 'CODE_OUTPUT', '다음 코드의 출력 결과를 쓰시오.
```java
String[] names = new String[2];
System.out.println(names[0]);
```', 'null', '참조 배열의 초기값은 null입니다.', 'BRONZE', NULL, 1),
  (50208, @java_subject_id, 5, 502, 'CODE_OUTPUT', '다음 배열의 길이를 쓰시오.
```java
int[] a = {1, 2, 3};
```', '3', '중괄호 초기화는 요소 개수로 길이가 결정됩니다.', 'BRONZE', NULL, 1),
  (50209, @java_subject_id, 5, 502, 'CODE_SHORT', '길이가 5인 int 배열 `nums`를 생성하는 문장 한 줄을 작성하시오.', '`int[] nums = new int[5];`', 'new 자료형[길이] 형식으로 생성합니다.', 'BRONZE', NULL, 1),
  (50210, @java_subject_id, 5, 502, 'CODE_OUTPUT', '다음 코드의 출력 결과를 쓰시오.
```java
double[] d = new double[2];
System.out.println(d[0]);
```', '0.0', '실수 배열의 초기값은 0.0입니다.', 'BRONZE', NULL, 1),
  (50301, @java_subject_id, 5, 503, 'MULTIPLE_CHOICE', '배열 첫 요소의 인덱스는?', '0', '인덱스는 0부터 시작합니다.', 'BRONZE', NULL, 1),
  (50302, @java_subject_id, 5, 503, 'MULTIPLE_CHOICE', '배열 마지막 요소의 인덱스는?', 'length - 1', '길이가 3이면 마지막 인덱스는 2입니다.', 'BRONZE', NULL, 1),
  (50303, @java_subject_id, 5, 503, 'MULTIPLE_CHOICE', '다음 코드를 실행하면?
```java
int[] scores = {10, 20, 30};
System.out.println(scores[3]);
```', '실행 중 오류(범위 초과 예외)', '유효 인덱스는 0~2이므로 3은 범위를 벗어나 실행 중 예외가 발생합니다.', 'BRONZE', NULL, 1),
  (50304, @java_subject_id, 5, 503, 'MULTIPLE_CHOICE', '세 번째 요소에 접근하려 한다. 빈칸에 들어갈 값은?
```java
System.out.println(scores[____]);
```', '2', '세 번째 요소의 인덱스는 2입니다.', 'BRONZE', NULL, 1),
  (50305, @java_subject_id, 5, 503, 'SHORT_ANSWER', '사용자가 ''1번째''라고 입력한 값을 배열 인덱스로 쓰려면 어떻게 변환해야 하는지 쓰시오.', '1을 뺀다', '사용자 기준 1번째는 인덱스 0입니다.', 'BRONZE', NULL, 1),
  (50306, @java_subject_id, 5, 503, 'SHORT_ANSWER', '배열 범위 오류(잘못된 인덱스 접근)는 어느 시점에 발견되는지 쓰시오.', '실행 중', '범위 초과는 컴파일이 아니라 실행 중 예외로 발생합니다.', 'BRONZE', NULL, 1),
  (50307, @java_subject_id, 5, 503, 'CODE_OUTPUT', '다음 코드의 출력 결과를 쓰시오.
```java
int[] a = {5, 6, 7};
System.out.println(a[1]);
```', '6', '인덱스 1은 두 번째 요소입니다.', 'BRONZE', NULL, 1),
  (50308, @java_subject_id, 5, 503, 'CODE_OUTPUT', '배열에 인덱스 -1로 접근하면 어떻게 되는지 쓰시오.', '실행 중 범위 초과 예외가 발생한다', '음수 인덱스도 범위를 벗어난 접근입니다.', 'BRONZE', NULL, 1),
  (50309, @java_subject_id, 5, 503, 'CODE_SHORT', '`scores` 배열의 첫 번째 요소를 출력하는 문장 한 줄을 작성하시오.', '`System.out.println(scores[0]);`', '첫 요소는 인덱스 0으로 접근합니다.', 'BRONZE', NULL, 1),
  (50310, @java_subject_id, 5, 503, 'FILL_BLANK', '접근 전 인덱스가 유효한지 검사하려 한다. 빈칸에 들어갈 연산자를 쓰시오.
```java
if (index >= 0 && index ____ scores.length) { }
```', '<', '유효 인덱스는 0 이상, length 미만입니다.', 'BRONZE', NULL, 1),
  (50401, @java_subject_id, 5, 504, 'MULTIPLE_CHOICE', '배열의 길이를 읽는 올바른 문법은?', 'array.length', '배열의 length는 필드처럼 괄호 없이 사용합니다.', 'BRONZE', NULL, 1),
  (50402, @java_subject_id, 5, 504, 'MULTIPLE_CHOICE', '배열의 length와 String의 길이 확인 방법의 차이는?', '배열은 length(괄호 없음), String은 length()(괄호 있음)', '배열은 속성, String은 메서드라는 차이를 기억합니다.', 'BRONZE', NULL, 1),
  (50403, @java_subject_id, 5, 504, 'MULTIPLE_CHOICE', '다음 코드의 출력 결과는?
```java
int[] scores = {10, 20, 30};
System.out.println(scores.length);
```', '3', '요소가 3개이므로 length는 3입니다.', 'BRONZE', NULL, 1),
  (50404, @java_subject_id, 5, 504, 'MULTIPLE_CHOICE', '마지막 요소에 접근하려 한다. 빈칸에 들어갈 단어는?
```java
System.out.println(scores[scores.____ - 1]);
```', 'length', '마지막 인덱스는 length - 1입니다.', 'BRONZE', NULL, 1),
  (50405, @java_subject_id, 5, 504, 'SHORT_ANSWER', 'length가 반환하는 값과 ''실제로 의미 있는 값의 개수''가 다를 수 있는 이유를 쓰시오.', 'length는 생성 시 정해진 크기를 반환할 뿐, 일부 칸만 사용할 수 있기 때문', '일부만 사용한다면 별도의 count 변수가 필요합니다.', 'BRONZE', NULL, 1),
  (50406, @java_subject_id, 5, 504, 'SHORT_ANSWER', '배열을 역순으로 처리할 때 시작 인덱스를 쓰시오.', 'length - 1', '역순 반복은 마지막 인덱스부터 시작합니다.', 'BRONZE', NULL, 1),
  (50407, @java_subject_id, 5, 504, 'CODE_OUTPUT', '다음 코드의 출력 결과를 쓰시오.
```java
int[] a = new int[4];
System.out.println(a.length);
```', '4', '생성 시 지정한 길이가 반환됩니다.', 'BRONZE', NULL, 1),
  (50408, @java_subject_id, 5, 504, 'CODE_OUTPUT', '다음 코드의 출력 결과를 쓰시오.
```java
int[] scores = {10, 20, 30};
System.out.println(scores[scores.length - 1]);
```', '30', 'length - 1(인덱스 2)은 마지막 요소입니다.', 'BRONZE', NULL, 1),
  (50409, @java_subject_id, 5, 504, 'CODE_SHORT', '배열 `nums`의 길이를 출력하는 문장 한 줄을 작성하시오.', '`System.out.println(nums.length);`', '배열 길이는 괄호 없이 length로 읽습니다.', 'BRONZE', NULL, 1),
  (50410, @java_subject_id, 5, 504, 'CODE_OUTPUT', '다음 코드의 출력 결과를 쓰시오.
```java
int[] s = {1, 2, 3};
for (int i = s.length - 1; i >= 0; i--) {
    System.out.print(s[i] + " ");
}
```', '3 2 1', 'length - 1부터 0까지 감소하며 역순 출력합니다.', 'BRONZE', NULL, 1),
  (50501, @java_subject_id, 5, 505, 'MULTIPLE_CHOICE', '일반 for문이 배열 순회에서 강점을 갖는 경우는?', '인덱스(현재 위치)가 필요할 때', '위치 출력, 특정 위치 수정, 최댓값 위치 저장 등에 적합합니다.', 'BRONZE', NULL, 1),
  (50502, @java_subject_id, 5, 505, 'MULTIPLE_CHOICE', '배열 순회 조건으로 권장되는 형태는?', 'i < array.length', '`i < length`로 해야 범위 초과가 없습니다.', 'BRONZE', NULL, 1),
  (50503, @java_subject_id, 5, 505, 'MULTIPLE_CHOICE', '다음 코드의 출력 결과는?
```java
int[] s = {10, 20};
for (int i = 0; i < s.length; i++) {
    System.out.println(i + ": " + s[i]);
}
```', '0: 10 다음 줄 1: 20', '인덱스와 요소를 함께 출력하는 패턴입니다.', 'BRONZE', NULL, 1),
  (50504, @java_subject_id, 5, 505, 'MULTIPLE_CHOICE', '빈칸에 들어갈 연산자는?
```java
for (int i = 0; i ____ arr.length; i++) { }
```', '<', 'length와 같아지는 순간 반복을 멈춰야 합니다.', 'BRONZE', NULL, 1),
  (50505, @java_subject_id, 5, 505, 'SHORT_ANSWER', '배열 요소를 수정하는 방법을 쓰시오.', '`array[i]`에 새 값을 대입한다', '인덱스로 접근해 대입하면 요소가 수정됩니다.', 'BRONZE', NULL, 1),
  (50506, @java_subject_id, 5, 505, 'SHORT_ANSWER', '앞 요소와 다음 요소를 비교하는 작업에 알맞은 반복 방식을 쓰시오.', '일반 for문(인덱스 기반)', '인덱스가 있어야 이웃 요소에 접근할 수 있습니다.', 'BRONZE', NULL, 1),
  (50507, @java_subject_id, 5, 505, 'CODE_OUTPUT', '다음 코드의 출력 결과를 쓰시오.
```java
int[] a = {1, 2, 3};
for (int i = 0; i < a.length; i++) {
    a[i] *= 2;
}
System.out.println(a[2]);
```', '6', '모든 요소가 2배가 되어 a[2]는 6입니다.', 'BRONZE', NULL, 1),
  (50508, @java_subject_id, 5, 505, 'CODE_OUTPUT', '순회 조건을 `i <= a.length`로 쓰면 어떤 문제가 생기는지 쓰시오.', '마지막 반복에서 범위 초과 예외가 발생한다', '인덱스 length는 유효 범위를 벗어납니다.', 'BRONZE', NULL, 1),
  (50509, @java_subject_id, 5, 505, 'CODE_SHORT', '배열 `scores`의 모든 요소를 인덱스와 함께 출력하는 일반 for문을 한 줄로 작성하시오.', '`for (int i = 0; i < scores.length; i++) { System.out.println(i + ": " + scores[i]); }`', 'length 기반 조건이면 데이터 개수가 달라져도 같은 코드가 동작합니다.', 'BRONZE', NULL, 1),
  (50510, @java_subject_id, 5, 505, 'FILL_BLANK', '두 번째 요소를 100으로 바꾸려 한다. 빈칸에 들어갈 값을 쓰시오.
```java
scores[____] = 100;
```', '1', '두 번째 요소의 인덱스는 1입니다.', 'BRONZE', NULL, 1),
  (50601, @java_subject_id, 5, 506, 'MULTIPLE_CHOICE', '향상된 for문의 특징으로 옳은 것은?', '모든 요소를 앞에서부터 읽기 쉽지만 현재 위치는 알 수 없다', '위치가 필요 없는 읽기 작업에 적합합니다.', 'BRONZE', NULL, 1),
  (50602, @java_subject_id, 5, 506, 'MULTIPLE_CHOICE', '향상된 for문의 반복 변수에 새 값을 대입하면?', '원본 배열은 바뀌지 않는다', '반복 변수는 요소값의 복사본입니다.', 'BRONZE', NULL, 1),
  (50603, @java_subject_id, 5, 506, 'MULTIPLE_CHOICE', '다음 코드의 출력 결과는?
```java
int[] s = {70, 80};
for (int v : s) {
    System.out.println(v);
}
```', '70 80 (두 줄)', '앞에서부터 모든 요소를 순서대로 읽습니다.', 'BRONZE', NULL, 1),
  (50604, @java_subject_id, 5, 506, 'MULTIPLE_CHOICE', '빈칸에 들어갈 기호는?
```java
for (int score ____ scores) { }
```', ':', '향상된 for문은 콜론(:)으로 배열을 지정합니다.', 'BRONZE', NULL, 1),
  (50605, @java_subject_id, 5, 506, 'SHORT_ANSWER', '향상된 for문이 적합하지 **않은** 작업을 두 가지 쓰시오.', '역순 처리, 일부 구간 처리(인덱스 기반 수정)', '한 방향 전체 순회만 가능합니다.', 'BRONZE', NULL, 1),
  (50606, @java_subject_id, 5, 506, 'SHORT_ANSWER', '향상된 for문이 적합한 작업의 예를 두 가지 쓰시오.', '모든 요소 읽기·합계·출력', '위치가 필요 없는 작업에 적합합니다.', 'BRONZE', NULL, 1),
  (50607, @java_subject_id, 5, 506, 'CODE_OUTPUT', '다음 코드의 출력 결과를 쓰시오.
```java
for (int v : new int[]{1, 2, 3}) {
    System.out.print(v);
}
```', '123', '요소 1, 2, 3이 줄바꿈 없이 이어 출력됩니다.', 'BRONZE', NULL, 1),
  (50608, @java_subject_id, 5, 506, 'CODE_OUTPUT', '다음 코드의 출력 결과를 쓰시오.
```java
int[] a = {5};
for (int v : a) {
    v = 10;
}
System.out.println(a[0]);
```', '5', '반복 변수 v는 복사본이라 원본 배열은 바뀌지 않습니다.', 'BRONZE', NULL, 1),
  (50609, @java_subject_id, 5, 506, 'CODE_SHORT', 'String 배열 `names`의 모든 이름을 출력하는 향상된 for문을 한 줄로 작성하시오.', '`for (String name : names) { System.out.println(name); }`', '요소 타입 String을 반복 변수 타입으로 씁니다.', 'BRONZE', NULL, 1),
  (50610, @java_subject_id, 5, 506, 'FILL_BLANK', 'names가 String 배열일 때 빈칸에 들어갈 자료형을 쓰시오.
```java
for (____ name : names) { }
```', 'String', '반복 변수의 자료형은 배열 요소의 자료형과 같아야 합니다.', 'BRONZE', NULL, 1),
  (50701, @java_subject_id, 5, 507, 'MULTIPLE_CHOICE', '합계 계산에서 누적 변수의 올바른 처리 방법은?', '반복 전에 한 번 0으로 초기화한다', '반복 안에서 초기화하면 누적이 되지 않습니다.', 'BRONZE', NULL, 1),
  (50702, @java_subject_id, 5, 507, 'MULTIPLE_CHOICE', '소수점 평균을 얻는 올바른 방법은?', '나누기 전에 한쪽을 double로 변환한다', '정수 나눗셈이 끝난 뒤에는 사라진 소수를 되돌릴 수 없습니다.', 'BRONZE', NULL, 1),
  (50703, @java_subject_id, 5, 507, 'MULTIPLE_CHOICE', '다음 코드에서 sum의 최종 값은?
```java
int[] s = {80, 90, 70};
int sum = 0;
for (int score : s) {
    sum += score;
}
```', '240', '80 + 90 + 70 = 240입니다.', 'BRONZE', NULL, 1),
  (50704, @java_subject_id, 5, 507, 'MULTIPLE_CHOICE', '소수점 평균이 나오도록 빈칸에 들어갈 자료형은?
```java
double average = (____) sum / s.length;
```', 'double', '나누기 전에 double로 변환해야 실수 나눗셈이 됩니다.', 'BRONZE', NULL, 1),
  (50705, @java_subject_id, 5, 507, 'SHORT_ANSWER', '평균을 계산하기 전에 배열에 대해 확인해야 할 것을 쓰시오.', '길이가 0인지(빈 배열인지)', '빈 배열이면 0으로 나눌 수 없습니다.', 'BRONZE', NULL, 1),
  (50706, @java_subject_id, 5, 507, 'SHORT_ANSWER', '최댓값을 구할 때 최댓값 변수의 초기값으로 알맞은 것을 쓰시오.', '배열의 첫 요소', '첫 요소로 시작하면 음수 배열에서도 안전합니다.', 'BRONZE', NULL, 1),
  (50707, @java_subject_id, 5, 507, 'CODE_OUTPUT', 'sum이 240이고 배열 길이가 3일 때 다음 식의 결과를 쓰시오.
```java
double average = (double) sum / 3;
```', '80.0', 'double 변환 후 나누어 실수 평균이 나옵니다.', 'BRONZE', NULL, 1),
  (50708, @java_subject_id, 5, 507, 'CODE_OUTPUT', '다음 코드의 출력 결과를 쓰시오.
```java
int sum = 0;
for (int v : new int[]{1, 2, 3, 4}) {
    sum += v;
}
System.out.println(sum);
```', '10', '1+2+3+4가 누적되어 10입니다.', 'BRONZE', NULL, 1),
  (50709, @java_subject_id, 5, 507, 'CODE_SHORT', '향상된 for문으로 `scores`의 모든 요소를 `sum`에 누적하는 반복문을 한 줄로 작성하시오.', '`for (int score : scores) { sum += score; }`', '위치가 필요 없는 합계에는 향상된 for문이 간단합니다.', 'BRONZE', NULL, 1),
  (50710, @java_subject_id, 5, 507, 'CODE_OUTPUT', '배열 {3, 9, 5}에서 첫 요소를 초기값으로 두고 더 큰 값을 만날 때 갱신하는 최댓값 계산의 결과를 쓰시오.', '9', '3 → 9(갱신) → 5(유지) 순서로 진행되어 9가 남습니다.', 'BRONZE', NULL, 1),
  (50801, @java_subject_id, 5, 508, 'MULTIPLE_CHOICE', '문자열의 길이를 확인하는 올바른 방법은?', 'text.length()', 'String은 length() 메서드(괄호 있음)를 사용합니다.', 'BRONZE', NULL, 1),
  (50802, @java_subject_id, 5, 508, 'MULTIPLE_CHOICE', '빈 문자열 `""`과 null의 관계는?', '서로 다르다', '""는 길이 0의 문자열이고 null은 값이 없는 상태입니다.', 'BRONZE', NULL, 1),
  (50803, @java_subject_id, 5, 508, 'MULTIPLE_CHOICE', '다음 코드의 출력 결과는?
```java
String title = "Java";
System.out.println(title.length());
```', '4', 'J, a, v, a 네 글자이므로 4입니다.', 'BRONZE', NULL, 1),
  (50804, @java_subject_id, 5, 508, 'MULTIPLE_CHOICE', '빈칸에 들어갈 기호는?
```java
System.out.println("과목: " ____ title);
```', '+', '문자열 연결은 + 연산자를 사용합니다.', 'BRONZE', NULL, 1),
  (50805, @java_subject_id, 5, 508, 'SHORT_ANSWER', 'String과 char의 차이를 한 문장으로 쓰시오.', 'String은 여러 글자(텍스트), char는 문자 하나를 저장한다', '큰따옴표는 String, 작은따옴표는 char입니다.', 'BRONZE', NULL, 1),
  (50806, @java_subject_id, 5, 508, 'SHORT_ANSWER', '문자열 연결을 반복하면 내부적으로 어떤 일이 생길 수 있는지 쓰시오.', '매번 새로운 문자열이 만들어질 수 있다', 'Bronze에서는 연결 결과를 정확히 이해하는 데 집중합니다.', 'BRONZE', NULL, 1),
  (50807, @java_subject_id, 5, 508, 'CODE_OUTPUT', '다음 코드의 출력 결과를 쓰시오.
```java
String title = "Java";
System.out.println("과목: " + title);
```', '과목: Java', '문자열끼리 +로 연결됩니다.', 'BRONZE', NULL, 1),
  (50808, @java_subject_id, 5, 508, 'CODE_OUTPUT', '다음 문장의 출력 결과를 쓰시오.
```java
System.out.println("안녕".length());
```', '2', '''안''과 ''녕'' 두 글자입니다.', 'BRONZE', NULL, 1),
  (50809, @java_subject_id, 5, 508, 'CODE_SHORT', 'String 변수 `title`의 길이를 출력하는 문장 한 줄을 작성하시오.', '`System.out.println(title.length());`', 'String의 길이는 length() 메서드로 확인합니다.', 'BRONZE', NULL, 1),
  (50810, @java_subject_id, 5, 508, 'CODE_OUTPUT', '다음 코드의 출력 결과를 쓰시오.
```java
String s = "";
System.out.println(s.length());
```', '0', '빈 문자열의 길이는 0이며 null과 달리 메서드 호출이 가능합니다.', 'BRONZE', NULL, 1),
  (50901, @java_subject_id, 5, 509, 'MULTIPLE_CHOICE', '문자열의 내용이 같은지 비교하는 메서드는?', 'equals()', '==는 참조 비교이므로 내용 비교에는 equals()를 씁니다.', 'BRONZE', NULL, 1),
  (50902, @java_subject_id, 5, 509, 'MULTIPLE_CHOICE', '대소문자를 무시하고 비교하는 메서드는?', 'equalsIgnoreCase()', 'equalsIgnoreCase는 대소문자를 무시해 비교합니다.', 'BRONZE', NULL, 1),
  (50903, @java_subject_id, 5, 509, 'MULTIPLE_CHOICE', '다음 코드의 출력 결과는?
```java
String name = "Java";
System.out.println(name.charAt(0));
```', 'J', 'charAt(0)은 첫 번째 문자를 반환합니다.', 'BRONZE', NULL, 1),
  (50904, @java_subject_id, 5, 509, 'MULTIPLE_CHOICE', '빈칸에 들어갈 메서드 이름은?
```java
boolean same = name.____("Java");
```', 'equals', '내용 비교는 equals 메서드입니다.', 'BRONZE', NULL, 1),
  (50905, @java_subject_id, 5, 509, 'SHORT_ANSWER', 'String의 인덱스는 몇부터 시작하는지 쓰시오.', '0', '배열과 마찬가지로 0부터 시작합니다.', 'BRONZE', NULL, 1),
  (50906, @java_subject_id, 5, 509, 'SHORT_ANSWER', 'charAt에 범위를 벗어난 인덱스를 전달하면 어떻게 되는지 쓰시오.', '실행 중 예외가 발생한다', '문자열 인덱스도 범위 검사가 필요합니다.', 'BRONZE', NULL, 1),
  (50907, @java_subject_id, 5, 509, 'CODE_OUTPUT', '다음 문장의 출력 결과를 쓰시오.
```java
System.out.println("Java".charAt(3));
```', 'a', '인덱스 3은 네 번째 문자 a입니다.', 'BRONZE', NULL, 1),
  (50908, @java_subject_id, 5, 509, 'CODE_OUTPUT', '다음 문장의 출력 결과를 쓰시오.
```java
System.out.println("java".equalsIgnoreCase("JAVA"));
```', 'true', '대소문자를 무시하면 두 문자열은 같습니다.', 'BRONZE', NULL, 1),
  (50909, @java_subject_id, 5, 509, 'CODE_SHORT', 'String 변수 `name`의 마지막 문자를 출력하는 문장 한 줄을 작성하시오.', '`System.out.println(name.charAt(name.length() - 1));`', '마지막 인덱스는 length() - 1입니다.', 'BRONZE', NULL, 1),
  (50910, @java_subject_id, 5, 509, 'CODE_OUTPUT', '다음 문장의 출력 결과를 쓰시오.
```java
System.out.println("Java".equals("java"));
```', 'false', 'equals는 대소문자를 구분해 비교합니다.', 'BRONZE', NULL, 1),
  (51001, @java_subject_id, 5, 510, 'MULTIPLE_CHOICE', '이름 목록에서 원하는 이름을 찾는 올바른 방법은?', '배열을 순회하며 각 요소를 equals로 비교한다', '문자열 요소 비교에는 equals를 사용합니다.', 'BRONZE', NULL, 1),
  (51002, @java_subject_id, 5, 510, 'MULTIPLE_CHOICE', '검색 성공 여부를 기록하기에 알맞은 자료형은?', 'boolean', 'found 같은 boolean 변수로 기록하면 실패 메시지 처리가 명확해집니다.', 'BRONZE', NULL, 1),
  (51003, @java_subject_id, 5, 510, 'MULTIPLE_CHOICE', 'keyword가 "지민"일 때 다음 코드의 출력 결과는?
```java
String[] names = {"민수", "지민"};
boolean found = false;
for (String name : names) {
    if (name.equals(keyword)) {
        found = true;
        System.out.println("찾음: " + name);
    }
}
```', '찾음: 지민', '"지민"과 내용이 같은 요소를 찾았을 때만 출력됩니다.', 'BRONZE', NULL, 1),
  (51004, @java_subject_id, 5, 510, 'MULTIPLE_CHOICE', '빈칸에 들어갈 메서드 이름은?
```java
if (name.____(keyword)) { }
```', 'equals', '문자열 내용 비교는 equals입니다.', 'BRONZE', NULL, 1),
  (51005, @java_subject_id, 5, 510, 'SHORT_ANSWER', '중복된 값이 있을 때 첫 항목만 찾고 싶다면 찾은 직후 무엇을 사용해야 하는지 쓰시오.', 'break', '모두 찾을지 첫 항목만 찾을지에 따라 break 사용 여부가 달라집니다.', 'BRONZE', NULL, 1),
  (51006, @java_subject_id, 5, 510, 'SHORT_ANSWER', '검색 실패(찾지 못한 경우) 메시지는 언제 어떻게 처리하는지 쓰시오.', '반복이 끝난 뒤 found 값을 확인해 처리한다', 'found가 false면 "없음" 메시지를 출력합니다.', 'BRONZE', NULL, 1),
  (51007, @java_subject_id, 5, 510, 'CODE_OUTPUT', '3번 문제의 코드에서 keyword가 "철수"라면 found의 최종 값을 쓰시오.', 'false', '일치하는 요소가 없어 found는 그대로 false입니다.', 'BRONZE', NULL, 1),
  (51008, @java_subject_id, 5, 510, 'CODE_OUTPUT', '다음 코드의 출력 결과를 쓰시오.
```java
String[] a = {"A", "B", "A"};
int count = 0;
for (String s : a) {
    if (s.equals("A")) {
        count++;
    }
}
System.out.println(count);
```', '2', '"A"가 두 번 등장하므로 count는 2입니다.', 'BRONZE', NULL, 1),
  (51009, @java_subject_id, 5, 510, 'CODE_SHORT', '순회 중인 `name`이 `keyword`와 내용이 같으면 `found`를 true로 만드는 if문을 한 줄로 작성하시오.', '`if (name.equals(keyword)) { found = true; }`', '검색의 핵심 비교·기록 패턴입니다.', 'BRONZE', NULL, 1),
  (51010, @java_subject_id, 5, 510, 'CODE_OUTPUT', '다음 코드에서 keyword와 일치하는 요소가 없을 때 출력 결과를 쓰시오.
```java
System.out.println(found ? "찾음" : "없음");
```', '없음', 'found가 false이므로 삼항 연산자의 뒤 값이 선택됩니다.', 'BRONZE', NULL, 1),
  (110101, @java_subject_id, 11, 1101, 'MULTIPLE_CHOICE', '메서드로 기능을 분리했을 때의 장점으로 옳은 것은?', '같은 코드를 반복 작성하지 않고 재사용할 수 있다', '반복 기능을 메서드로 분리하면 재사용할 수 있고 main은 실행 흐름만 보여 줍니다.', 'SILVER', NULL, 1),
  (110102, @java_subject_id, 11, 1101, 'MULTIPLE_CHOICE', '다음 코드의 출력 결과는?
```java
static void greet() { System.out.println("안녕하세요"); }
// main에서
greet();
greet();
```', '안녕하세요 두 번', '호출한 횟수만큼 메서드 본문이 실행됩니다.', 'SILVER', NULL, 1),
  (110103, @java_subject_id, 11, 1101, 'MULTIPLE_CHOICE', '반환값이 없는 greet 메서드 선언의 빈칸에 들어갈 키워드는?
```java
static ____ greet() { System.out.println("안녕하세요"); }
```', 'void', '돌려주는 값이 없으면 void로 선언합니다.', 'SILVER', NULL, 1),
  (110104, @java_subject_id, 11, 1101, 'MULTIPLE_CHOICE', '메서드를 선언만 하고 한 번도 호출하지 않으면?', '아무것도 출력되지 않는다', '메서드는 호출해야 실행됩니다.', 'SILVER', NULL, 1),
  (110105, @java_subject_id, 11, 1101, 'SHORT_ANSWER', '메서드 이름을 지을 때 무엇이 드러나도록 지어야 하는지 쓰시오.', '수행하는 동작', '이름만으로 기능을 알 수 있어야 합니다.', 'SILVER', NULL, 1),
  (110106, @java_subject_id, 11, 1101, 'FILL_BLANK', 'greet 메서드를 호출하는 문장이 되도록 빈칸을 채우시오.
```java
greet____;
```', '()', '호출에는 괄호가 필요합니다.', 'SILVER', NULL, 1),
  (110107, @java_subject_id, 11, 1101, 'CODE_OUTPUT', 'greet()를 세 번 호출하면 "안녕하세요"는 몇 번 출력되는지 쓰시오.', '3번', '호출 횟수만큼 실행됩니다.', 'SILVER', NULL, 1),
  (110108, @java_subject_id, 11, 1101, 'CODE_SHORT', '"환영합니다"를 출력하는 static void 메서드 `welcome`을 한 줄로 선언하시오.', '`static void welcome() { System.out.println("환영합니다"); }`', '반환값이 없으므로 void입니다.', 'SILVER', NULL, 1),
  (110109, @java_subject_id, 11, 1101, 'CODE_OUTPUT', '다음 코드의 출력 결과를 쓰시오. (greet는 "B"를 출력하는 메서드)
```java
System.out.print("A");
greet();
```', 'AB', '문장 순서대로 A가 출력된 뒤 greet 본문이 실행됩니다.', 'SILVER', NULL, 1),
  (110110, @java_subject_id, 11, 1101, 'CODE_SHORT', 'welcome 메서드를 호출하는 문장 한 줄을 작성하시오.', '`welcome();`', '메서드 이름과 괄호, 세미콜론으로 호출합니다.', 'SILVER', NULL, 1),
  (110201, @java_subject_id, 11, 1102, 'MULTIPLE_CHOICE', '메서드 선언에 포함되는 요소로 옳은 묶음은?', '반환형·이름·매개변수 목록', '접근 범위·정적 여부·반환형·이름·매개변수가 선언에 들어갑니다.', 'SILVER', NULL, 1),
  (110202, @java_subject_id, 11, 1102, 'MULTIPLE_CHOICE', '다음 코드의 출력 결과는?
```java
static int add(int left, int right) { return left + right; }
System.out.println(add(3, 4));
```', '7', '전달값 3, 4가 매개변수에 저장되어 합 7이 반환됩니다.', 'SILVER', NULL, 1),
  (110203, @java_subject_id, 11, 1102, 'MULTIPLE_CHOICE', '빈칸에 들어갈 키워드는?
```java
static int add(int a, int b) { ____ a + b; }
```', 'return', '계산 결과는 return으로 반환합니다.', 'SILVER', NULL, 1),
  (110204, @java_subject_id, 11, 1102, 'MULTIPLE_CHOICE', '`add(int, int)`에 `add("3", 4)`처럼 문자열을 전달하면?', '컴파일 오류', '매개변수의 자료형이 선언과 다르면 호출할 수 없습니다.', 'SILVER', NULL, 1),
  (110205, @java_subject_id, 11, 1102, 'SHORT_ANSWER', '반환형과 실제 반환하는 값의 자료형은 어떤 관계여야 하는지 쓰시오.', '호환되어야 한다', 'int 반환형이면 int로 쓸 수 있는 값을 반환해야 합니다.', 'SILVER', NULL, 1),
  (110206, @java_subject_id, 11, 1102, 'FILL_BLANK', '정수를 반환하도록 빈칸에 들어갈 반환형을 쓰시오.
```java
static ____ add(int a, int b) { return a + b; }
```', 'int', '반환 값의 자료형을 반환형 자리에 적습니다.', 'SILVER', NULL, 1),
  (110207, @java_subject_id, 11, 1102, 'CODE_OUTPUT', '다음 코드의 출력 결과를 쓰시오.
```java
int result = add(10, 20);
System.out.println(result);
```', '30', '반환값 30이 변수에 저장되어 출력됩니다.', 'SILVER', NULL, 1),
  (110208, @java_subject_id, 11, 1102, 'CODE_SHORT', '두 정수의 곱을 반환하는 `multiply` 메서드를 한 줄로 선언하시오.', '`static int multiply(int a, int b) { return a * b; }`', '반환형 int, 매개변수 두 개의 구조입니다.', 'SILVER', NULL, 1),
  (110209, @java_subject_id, 11, 1102, 'CODE_OUTPUT', '매개변수가 두 개인 add에 `add(1, 2, 3)`처럼 세 개를 전달하면 어떻게 되는지 쓰시오.', '컴파일 오류가 발생한다', '매개변수 개수가 선언과 다르면 호출할 수 없습니다.', 'SILVER', NULL, 1),
  (110210, @java_subject_id, 11, 1102, 'FILL_BLANK', '빈칸에 들어갈 메서드 이름을 쓰시오.
```java
int result = ____(3, 4); // 두 수의 합
```', 'add', '선언된 이름으로 호출합니다.', 'SILVER', NULL, 1),
  (110301, @java_subject_id, 11, 1103, 'MULTIPLE_CHOICE', 'void의 의미로 옳은 것은?', '호출한 곳에 값을 돌려주지 않는다', 'void는 반환값이 없다는 뜻입니다.', 'SILVER', NULL, 1),
  (110302, @java_subject_id, 11, 1103, 'MULTIPLE_CHOICE', '다음 코드의 출력 결과는?
```java
static void printPositive(int number) {
    if (number <= 0) return;
    System.out.println(number);
}
printPositive(-1);
printPositive(5);
```', '5', '-1은 return으로 일찍 끝나고 5만 출력됩니다.', 'SILVER', NULL, 1),
  (110303, @java_subject_id, 11, 1103, 'MULTIPLE_CHOICE', '실행을 일찍 끝내도록 빈칸에 들어갈 키워드는?
```java
if (number <= 0) ____;
```', 'return', 'void 메서드에서도 return;으로 일찍 끝낼 수 있습니다.', 'SILVER', NULL, 1),
  (110304, @java_subject_id, 11, 1103, 'MULTIPLE_CHOICE', 'void 메서드 안에서 `return 5;`를 작성하면?', '컴파일 오류', 'void 메서드의 return 뒤에는 값을 쓸 수 없습니다.', 'SILVER', NULL, 1),
  (110305, @java_subject_id, 11, 1103, 'SHORT_ANSWER', 'void 메서드가 적합한 작업의 예를 한 가지 쓰시오.', '화면 출력(객체 상태 변경, 파일 저장 등 작업 자체가 목적인 일)', '결과값이 아니라 동작이 목적일 때 사용합니다.', 'SILVER', NULL, 1),
  (110306, @java_subject_id, 11, 1103, 'CODE_OUTPUT', '`printPositive(0)`을 호출하면 어떻게 되는지 쓰시오.', '아무것도 출력되지 않는다', '0은 `number <= 0` 조건에 걸려 return됩니다.', 'SILVER', NULL, 1),
  (110307, @java_subject_id, 11, 1103, 'FILL_BLANK', '빈칸에 들어갈 반환형을 쓰시오.
```java
static ____ printMessage() { System.out.println("안내"); }
```', 'void', '출력만 하고 반환값이 없습니다.', 'SILVER', NULL, 1),
  (110308, @java_subject_id, 11, 1103, 'CODE_SHORT', 'n이 음수면 메서드를 즉시 끝내는 문장 한 줄을 작성하시오.', '`if (n < 0) return;`', '조기 종료 패턴입니다.', 'SILVER', NULL, 1),
  (110309, @java_subject_id, 11, 1103, 'CODE_OUTPUT', 'return;이 실행된 뒤 그 호출에서 메서드의 남은 문장은 어떻게 되는지 쓰시오.', '실행되지 않는다', 'return은 메서드 실행을 즉시 끝냅니다.', 'SILVER', NULL, 1),
  (110310, @java_subject_id, 11, 1103, 'CODE_SHORT', '"완료"를 출력하는 void 메서드 `printDone`을 한 줄로 선언하시오.', '`static void printDone() { System.out.println("완료"); }`', '출력 전용 void 메서드입니다.', 'SILVER', NULL, 1),
  (110401, @java_subject_id, 11, 1104, 'MULTIPLE_CHOICE', 'return이 실행되면 어떻게 되는가?', '메서드가 즉시 끝나고 값이 호출한 곳으로 전달된다', 'return은 종료와 값 전달을 동시에 수행합니다.', 'SILVER', NULL, 1),
  (110402, @java_subject_id, 11, 1104, 'MULTIPLE_CHOICE', '다음 코드의 출력 결과는?
```java
static boolean isEven(int number) { return number % 2 == 0; }
System.out.println(isEven(8));
```', 'true', '8 % 2 == 0은 참입니다.', 'SILVER', NULL, 1),
  (110403, @java_subject_id, 11, 1104, 'MULTIPLE_CHOICE', '빈칸에 들어갈 연산자는?
```java
static boolean isEven(int n) { return n % 2 ____ 0; }
```', '==', '나머지가 0과 같은지 비교합니다.', 'SILVER', NULL, 1),
  (110404, @java_subject_id, 11, 1104, 'MULTIPLE_CHOICE', '`isEven(7)`의 반환값은?', 'false', '7 % 2는 1이므로 거짓입니다.', 'SILVER', NULL, 1),
  (110405, @java_subject_id, 11, 1104, 'SHORT_ANSWER', '반환형이 void가 아닌 메서드의 모든 정상 실행 경로가 지켜야 할 규칙을 쓰시오.', '해당 자료형의 값을 반환해야 한다', '반환 없는 경로가 있으면 컴파일 오류입니다.', 'SILVER', NULL, 1),
  (110406, @java_subject_id, 11, 1104, 'CODE_OUTPUT', 'add가 두 수의 합을 반환할 때 다음 코드에서 r의 값을 쓰시오.
```java
int r = add(2, 3) * 2;
```', '10', '반환값 5를 다른 계산에 바로 사용할 수 있습니다.', 'SILVER', NULL, 1),
  (110407, @java_subject_id, 11, 1104, 'FILL_BLANK', 'divide가 int를 반환할 때 빈칸에 들어갈 자료형을 쓰시오.
```java
____ half = divide(10, 2);
```', 'int', '반환값을 저장할 변수는 반환형과 호환되어야 합니다.', 'SILVER', NULL, 1),
  (110408, @java_subject_id, 11, 1104, 'CODE_SHORT', '점수가 70 이상인지 반환하는 `isPassed` 메서드를 한 줄로 선언하시오.', '`static boolean isPassed(int score) { return score >= 70; }`', '비교식 결과를 그대로 반환합니다.', 'SILVER', NULL, 1),
  (110409, @java_subject_id, 11, 1104, 'CODE_OUTPUT', '같은 메서드 안에서 return이 실행된 뒤의 문장은 어떻게 되는지 쓰시오.', '실행되지 않는다', 'return 즉시 메서드가 끝납니다.', 'SILVER', NULL, 1),
  (110410, @java_subject_id, 11, 1104, 'CODE_OUTPUT', '다음 문장의 출력 결과를 쓰시오.
```java
System.out.println(isEven(10));
```', 'true', '10은 짝수입니다.', 'SILVER', NULL, 1),
  (110501, @java_subject_id, 11, 1105, 'MULTIPLE_CHOICE', 'Java의 인자 전달 방식으로 옳은 것은?', '인자의 값이 매개변수로 복사된다', 'Java는 값을 복사해 전달합니다.', 'SILVER', NULL, 1),
  (110502, @java_subject_id, 11, 1105, 'MULTIPLE_CHOICE', '다음 코드의 출력 결과는?
```java
static void change(int value) { value = 100; }
int number = 10;
change(number);
System.out.println(number);
```', '10', '매개변수는 복사본이므로 호출한 쪽 변수는 바뀌지 않습니다.', 'SILVER', NULL, 1),
  (110503, @java_subject_id, 11, 1105, 'MULTIPLE_CHOICE', '빈칸에 들어갈 매개변수 이름은?
```java
static void greet(String ____) { System.out.println("안녕 " + name); }
```', 'name', '본문에서 사용하는 이름(name)으로 선언해야 합니다.', 'SILVER', NULL, 1),
  (110504, @java_subject_id, 11, 1105, 'MULTIPLE_CHOICE', '위 greet에 `greet("민수")`를 호출한 출력 결과는?', '안녕 민수', '전달값이 매개변수 name에 저장되어 출력됩니다.', 'SILVER', NULL, 1),
  (110505, @java_subject_id, 11, 1105, 'SHORT_ANSWER', '기본형 매개변수를 메서드 안에서 바꾸면 호출한 쪽 변수는 어떻게 되는지 쓰시오.', '바뀌지 않는다', '값 복사 전달이기 때문입니다.', 'SILVER', NULL, 1),
  (110506, @java_subject_id, 11, 1105, 'CODE_OUTPUT', '다음 코드의 출력 결과를 쓰시오.
```java
static void twice(int v) { v *= 2; }
int n = 3;
twice(n);
System.out.println(n);
```', '3', '복사본 v만 6이 되고 원본 n은 그대로입니다.', 'SILVER', NULL, 1),
  (110507, @java_subject_id, 11, 1105, 'CODE_SHORT', '`printInfo(String name, int age)`를 이름 "민수", 나이 14로 호출하는 문장을 작성하시오.', '`printInfo("민수", 14);`', '인자는 선언된 순서·자료형에 맞춰 전달합니다.', 'SILVER', NULL, 1),
  (110508, @java_subject_id, 11, 1105, 'CODE_SHORT', '이름과 나이를 받아 `이름 나이` 형태로 출력하는 printInfo 메서드를 한 줄로 선언하시오.', '`static void printInfo(String name, int age) { System.out.println(name + " " + age); }`', '매개변수 여러 개는 쉼표로 구분합니다.', 'SILVER', NULL, 1),
  (110509, @java_subject_id, 11, 1105, 'CODE_OUTPUT', '`printInfo(String, int)`에 `printInfo(14, "민수")`처럼 순서를 바꿔 전달하면 어떻게 되는지 쓰시오.', '컴파일 오류가 발생한다', '인자의 순서·자료형이 선언과 맞아야 합니다.', 'SILVER', NULL, 1),
  (110510, @java_subject_id, 11, 1105, 'CODE_OUTPUT', '2번 문제의 change 메서드 내부에서 `value = 100;` 직후 value를 출력하면 얼마인지 쓰시오.', '100', '메서드 안의 복사본은 변경된 값을 가집니다.', 'SILVER', NULL, 1),
  (110601, @java_subject_id, 11, 1106, 'MULTIPLE_CHOICE', '지역 변수의 수명으로 옳은 것은?', '선언된 블록(호출) 안에서만 유지된다', '메서드 호출이 끝나면 그 호출의 지역 변수도 사라집니다.', 'SILVER', NULL, 1),
  (110602, @java_subject_id, 11, 1106, 'MULTIPLE_CHOICE', '다음 코드의 출력 결과는?
```java
static void first() { int value = 10; System.out.println(value); }
static void second() { int value = 20; System.out.println(value); }
first();
second();
```', '10 20', '서로 다른 메서드의 같은 이름 지역 변수는 충돌하지 않습니다.', 'SILVER', NULL, 1),
  (110603, @java_subject_id, 11, 1106, 'MULTIPLE_CHOICE', '서로 다른 메서드에 같은 이름의 지역 변수를 두면?', '충돌하지 않는다', '각 메서드의 지역 변수는 독립적입니다.', 'SILVER', NULL, 1),
  (110604, @java_subject_id, 11, 1106, 'MULTIPLE_CHOICE', 'first() 안의 value를 second()에서 참조하면?', '컴파일 오류', '지역 변수는 선언된 메서드 밖에서 사용할 수 없습니다.', 'SILVER', NULL, 1),
  (110605, @java_subject_id, 11, 1106, 'SHORT_ANSWER', '변수 범위를 작게 유지했을 때의 장점을 쓰시오.', '예상하지 못한 값 변경을 줄일 수 있다', '범위가 좁을수록 추적이 쉽습니다.', 'SILVER', NULL, 1),
  (110606, @java_subject_id, 11, 1106, 'CODE_OUTPUT', 'if 블록 안에서 선언한 변수를 블록 밖에서 사용하면 어떻게 되는지 쓰시오.', '컴파일 오류가 발생한다', '블록 안 선언은 블록 안에서만 유효합니다.', 'SILVER', NULL, 1),
  (110607, @java_subject_id, 11, 1106, 'FILL_BLANK', '지역 변수는 선언된 ____ 안에서만 사용할 수 있다. 빈칸을 채우시오.', '블록', '중괄호 범위가 변수의 사용 범위입니다.', 'SILVER', NULL, 1),
  (110608, @java_subject_id, 11, 1106, 'CODE_OUTPUT', '메서드 호출이 끝난 뒤 그 호출에서 사용한 지역 변수는 어떻게 되는지 쓰시오.', '더 이상 사용할 수 없다(사라진다)', '호출 단위로 생성·소멸됩니다.', 'SILVER', NULL, 1),
  (110609, @java_subject_id, 11, 1106, 'CODE_SHORT', 'main 안에 지역 변수 `count`를 0으로 선언하는 문장 한 줄을 작성하시오.', '`int count = 0;`', '지역 변수는 사용 전 초기화가 필요합니다.', 'SILVER', NULL, 1),
  (110610, @java_subject_id, 11, 1106, 'CODE_OUTPUT', '2번 문제에서 first()의 value와 second()의 value는 같은 변수인지 쓰시오.', '서로 다른 변수다', '이름만 같을 뿐 독립된 지역 변수입니다.', 'SILVER', NULL, 1),
  (110701, @java_subject_id, 11, 1107, 'MULTIPLE_CHOICE', '오버로딩의 성립 조건으로 옳은 것은?', '이름이 같고 매개변수의 개수·자료형·순서가 다르다', '반환형만 다른 선언은 오버로딩이 아닙니다.', 'SILVER', NULL, 1),
  (110702, @java_subject_id, 11, 1107, 'MULTIPLE_CHOICE', '다음 코드의 출력 결과는?
```java
static int add(int a, int b) { return a + b; }
static double add(double a, double b) { return a + b; }
System.out.println(add(2, 3));
```', '5', '정수 인자이므로 int 버전이 선택됩니다.', 'SILVER', NULL, 1),
  (110703, @java_subject_id, 11, 1107, 'MULTIPLE_CHOICE', '위 코드에서 `add(1.5, 2.0)`의 출력 결과는?', '3.5', '실수 인자이므로 double 버전이 선택됩니다.', 'SILVER', NULL, 1),
  (110704, @java_subject_id, 11, 1107, 'MULTIPLE_CHOICE', '매개변수는 같고 반환형만 다른 두 메서드를 선언하면?', '컴파일 오류가 발생한다', '호출 인자만으로 어느 메서드인지 결정할 수 있어야 합니다.', 'SILVER', NULL, 1),
  (110705, @java_subject_id, 11, 1107, 'SHORT_ANSWER', '컴파일러가 오버로딩된 메서드 중 하나를 선택하는 기준을 쓰시오.', '호출 인자(개수·자료형·순서)', '인자만 보고 결정할 수 있어야 합니다.', 'SILVER', NULL, 1),
  (110706, @java_subject_id, 11, 1107, 'FILL_BLANK', 'double 버전 add가 되도록 빈칸에 들어갈 자료형을 쓰시오.
```java
static double add(____ a, double b) { return a + b; }
```', 'double', '매개변수 자료형이 int 버전과 달라야 오버로딩입니다.', 'SILVER', NULL, 1),
  (110707, @java_subject_id, 11, 1107, 'CODE_OUTPUT', '`print(String)`과 `print(int)`가 선언되어 있을 때 `print(10)`은 어느 버전이 실행되는지 쓰시오.', 'print(int) 버전', '인자 10의 자료형과 일치하는 메서드가 선택됩니다.', 'SILVER', NULL, 1),
  (110708, @java_subject_id, 11, 1107, 'CODE_SHORT', '문자열 하나를 받아 출력하는 `show` 메서드를 한 줄로 선언하시오. (정수 버전 show와 오버로딩 관계)', '`static void show(String text) { System.out.println(text); }`', '이름은 같고 매개변수 자료형이 다르면 오버로딩입니다.', 'SILVER', NULL, 1),
  (110709, @java_subject_id, 11, 1107, 'CODE_OUTPUT', '매개변수 순서만 (int, String)과 (String, int)로 다른 두 메서드는 오버로딩이 성립하는지 쓰시오.', '성립한다', '매개변수 순서가 다르면 서로 다른 시그니처입니다.', 'SILVER', NULL, 1),
  (110710, @java_subject_id, 11, 1107, 'CODE_OUTPUT', '2번 문제의 코드에서 `add(2, 3.0)`의 출력 결과를 쓰시오.', '5.0', 'int 2가 double로 자동 변환되어 double 버전이 선택됩니다.', 'SILVER', NULL, 1),
  (110801, @java_subject_id, 11, 1108, 'MULTIPLE_CHOICE', '재귀 메서드에 반드시 필요한 두 가지는?', '종료 조건과 문제를 줄이는 재귀 호출', '종료 조건이 없으면 호출이 계속 쌓여 오류가 발생합니다.', 'SILVER', NULL, 1),
  (110802, @java_subject_id, 11, 1108, 'MULTIPLE_CHOICE', '다음 코드의 출력 결과는?
```java
static int sum(int number) {
    if (number <= 1) return number;
    return number + sum(number - 1);
}
System.out.println(sum(4));
```', '10', '4 + 3 + 2 + 1 = 10입니다.', 'SILVER', NULL, 1),
  (110803, @java_subject_id, 11, 1108, 'MULTIPLE_CHOICE', '종료 조건의 빈칸에 들어갈 것은?
```java
if (number <= 1) return ____;
```', 'number', '1 이하에서는 number 자신을 반환해야 sum(1)=1, sum(0)=0이 됩니다.', 'SILVER', NULL, 1),
  (110804, @java_subject_id, 11, 1108, 'MULTIPLE_CHOICE', '종료 조건이 없는 재귀 메서드를 실행하면?', '호출이 계속 쌓여 오류가 발생한다', '호출이 무한히 쌓여 실행 중 오류(StackOverflowError)가 발생합니다.', 'SILVER', NULL, 1),
  (110805, @java_subject_id, 11, 1108, 'SHORT_ANSWER', '단순 반복 작업에는 재귀보다 무엇이 더 읽기 쉬운지 쓰시오.', 'for문(반복문)', '재귀는 계층 구조나 재귀적으로 정의된 문제에 제한적으로 사용합니다.', 'SILVER', NULL, 1),
  (110806, @java_subject_id, 11, 1108, 'CODE_OUTPUT', '`sum(1)`의 반환값을 쓰시오.', '1', '종료 조건에서 number 자신을 반환합니다.', 'SILVER', NULL, 1),
  (110807, @java_subject_id, 11, 1108, 'CODE_OUTPUT', '`sum(3)`의 계산 과정을 펼쳐 결과를 쓰시오.', '3 + 2 + 1 = 6', '문제의 크기를 1씩 줄이며 누적합니다.', 'SILVER', NULL, 1),
  (110808, @java_subject_id, 11, 1108, 'FILL_BLANK', '문제 크기를 줄이는 호출이 되도록 빈칸에 들어갈 기호를 쓰시오.
```java
return number + sum(number ____ 1);
```', '-', '매 호출마다 크기가 줄어야 종료 조건에 도달합니다.', 'SILVER', NULL, 1),
  (110809, @java_subject_id, 11, 1108, 'CODE_SHORT', '카운트다운 재귀 메서드에서 n이 0이면 즉시 끝나는 종료 조건 한 줄을 작성하시오.', '`if (n == 0) return;`', '재귀의 첫 줄은 종료 조건입니다.', 'SILVER', NULL, 1),
  (110810, @java_subject_id, 11, 1108, 'CODE_OUTPUT', '2번 문제의 sum에 `sum(0)`을 호출하면 반환값이 얼마인지 쓰시오.', '0', '`number <= 1`에서 number(0)를 반환하므로 무한 재귀 없이 0입니다.', 'SILVER', NULL, 1),
  (110901, @java_subject_id, 11, 1109, 'MULTIPLE_CHOICE', '배열을 메서드에 전달하면 무엇이 복사되는가?', '배열 객체를 가리키는 참조값', '참조가 복사되므로 요소 변경은 호출한 쪽에서도 보입니다.', 'SILVER', NULL, 1),
  (110902, @java_subject_id, 11, 1109, 'MULTIPLE_CHOICE', '다음 코드의 출력 결과는?
```java
static void doubleFirst(int[] values) { values[0] *= 2; }
int[] numbers = {5, 10};
doubleFirst(numbers);
System.out.println(numbers[0]);
```', '10', '같은 배열을 가리키므로 5가 2배가 되어 10입니다.', 'SILVER', NULL, 1),
  (110903, @java_subject_id, 11, 1109, 'MULTIPLE_CHOICE', '빈칸에 들어갈 매개변수 이름은?
```java
static void doubleFirst(int[] ____) { values[0] *= 2; }
```', 'values', '본문에서 사용하는 이름과 일치해야 합니다.', 'SILVER', NULL, 1),
  (110904, @java_subject_id, 11, 1109, 'MULTIPLE_CHOICE', '메서드 안에서 배열 매개변수에 새 배열을 대입하면 호출한 쪽 배열은?', '그대로 유지된다', '지역 변수인 매개변수만 새 배열을 가리키게 됩니다.', 'SILVER', NULL, 1),
  (110905, @java_subject_id, 11, 1109, 'SHORT_ANSWER', '''배열 요소 변경''과 ''배열 변수에 새 배열 대입''의 차이를 한 문장으로 쓰시오.', '요소 변경은 호출한 쪽에도 반영되지만, 새 배열 대입은 메서드 안에서만 유효하다', '참조 복사의 성질입니다.', 'SILVER', NULL, 1),
  (110906, @java_subject_id, 11, 1109, 'CODE_OUTPUT', '다음 코드의 출력 결과를 쓰시오.
```java
static void set(int[] a) { a[1] = 99; }
int[] n = {1, 2};
set(n);
System.out.println(n[1]);
```', '99', '요소 변경은 원본 배열에 반영됩니다.', 'SILVER', NULL, 1),
  (110907, @java_subject_id, 11, 1109, 'FILL_BLANK', 'numbers 배열을 전달하도록 빈칸을 채우시오.
```java
doubleFirst(____);
```', 'numbers', '배열 변수 이름만 전달합니다.', 'SILVER', NULL, 1),
  (110908, @java_subject_id, 11, 1109, 'CODE_SHORT', '배열의 첫 요소를 0으로 만드는 `clearFirst` 메서드를 한 줄로 선언하시오.', '`static void clearFirst(int[] arr) { arr[0] = 0; }`', '요소 변경은 호출한 쪽에 반영됩니다.', 'SILVER', NULL, 1),
  (110909, @java_subject_id, 11, 1109, 'CODE_OUTPUT', '다음 코드의 출력 결과를 쓰시오.
```java
static void replace(int[] a) { a = new int[]{9}; }
int[] n = {1};
replace(n);
System.out.println(n[0]);
```', '1', '새 배열 대입은 메서드 안의 매개변수에만 적용됩니다.', 'SILVER', NULL, 1),
  (110910, @java_subject_id, 11, 1109, 'CODE_OUTPUT', 'int 매개변수와 int[] 매개변수의 변경 결과 차이를 한 문장으로 쓰시오.', 'int는 복사본이라 원본이 안 바뀌고, 배열은 참조 공유라 요소 변경이 원본에 반영된다', '기본형과 참조형 전달의 차이입니다.', 'SILVER', NULL, 1),
  (111001, @java_subject_id, 11, 1110, 'MULTIPLE_CHOICE', '메서드를 나누는 바람직한 기준은?', '한 가지 책임에 집중하도록', '이름과 반환값만으로 사용법을 이해할 수 있게 설계합니다.', 'SILVER', NULL, 1),
  (111002, @java_subject_id, 11, 1110, 'MULTIPLE_CHOICE', '다음 코드의 출력 결과는?
```java
static int calculateAverage(int total, int count) {
    if (count == 0) return 0;
    return total / count;
}
System.out.println(calculateAverage(240, 3));
```', '80', '240 / 3 = 80입니다.', 'SILVER', NULL, 1),
  (111003, @java_subject_id, 11, 1110, 'MULTIPLE_CHOICE', '0으로 나누기를 방지하는 가드의 빈칸에 들어갈 값은?
```java
if (count == 0) return ____;
```', '0', '인원이 0이면 0을 반환해 예외를 예방합니다.', 'SILVER', NULL, 1),
  (111004, @java_subject_id, 11, 1110, 'MULTIPLE_CHOICE', 'average가 80일 때 `average >= 70 ? "통과" : "재도전"`의 결과는?', '통과', '조건이 참이므로 "통과"가 선택됩니다.', 'SILVER', NULL, 1),
  (111005, @java_subject_id, 11, 1110, 'SHORT_ANSWER', '작은 메서드를 조합하면 main 메서드는 무엇처럼 보이게 되는지 쓰시오.', '업무 흐름을 설명하는 목차', 'main에는 실행 순서만 남습니다.', 'SILVER', NULL, 1),
  (111006, @java_subject_id, 11, 1110, 'CODE_OUTPUT', '`calculateAverage(0, 0)`의 반환값을 쓰시오.', '0', 'count가 0이므로 가드 조건에서 0이 반환됩니다.', 'SILVER', NULL, 1),
  (111007, @java_subject_id, 11, 1110, 'FILL_BLANK', '빈칸에 들어갈 메서드 이름을 쓰시오.
```java
int average = ____(240, 3);
```', 'calculateAverage', '선언된 이름으로 호출합니다.', 'SILVER', NULL, 1),
  (111008, @java_subject_id, 11, 1110, 'CODE_SHORT', '두 값 중 큰 값을 반환하는 `max` 메서드를 삼항 연산자로 한 줄 선언하시오.', '`static int max(int a, int b) { return a > b ? a : b; }`', '값 선택에는 삼항 연산자가 간결합니다.', 'SILVER', NULL, 1),
  (111009, @java_subject_id, 11, 1110, 'CODE_OUTPUT', '`max(3, 7)`의 반환값을 쓰시오.', '7', '3 > 7이 거짓이므로 b가 반환됩니다.', 'SILVER', NULL, 1),
  (111010, @java_subject_id, 11, 1110, 'CODE_SHORT', '점수가 0~100 범위인지 반환하는 `isValidScore` 메서드를 한 줄로 선언하시오.', '`static boolean isValidScore(int s) { return s >= 0 && s <= 100; }`', '검증 기능을 메서드로 분리한 형태입니다.', 'SILVER', NULL, 1),
  (120101, @java_subject_id, 12, 1201, 'MULTIPLE_CHOICE', '객체를 구성하는 두 요소로 옳은 것은?', '상태(필드)와 동작(메서드)', '객체는 상태를 필드로 저장하고 동작을 메서드로 제공합니다.', 'SILVER', NULL, 1),
  (120102, @java_subject_id, 12, 1201, 'MULTIPLE_CHOICE', '다음 코드의 출력 결과는?
```java
class Counter { int count; void increase() { count++; } }
Counter c = new Counter();
c.increase();
c.increase();
System.out.println(c.count);
```', '2', 'increase가 두 번 실행되어 count는 2입니다.', 'SILVER', NULL, 1),
  (120103, @java_subject_id, 12, 1201, 'MULTIPLE_CHOICE', '빈칸에 들어갈 연산자는?
```java
void increase() { count____; }
```', '++', '상태를 1 증가시키는 동작입니다.', 'SILVER', NULL, 1),
  (120104, @java_subject_id, 12, 1201, 'MULTIPLE_CHOICE', 'Counter 객체 두 개를 만들어 각각 increase를 한 번씩 호출하면 각 객체의 count는?', '각각 1', '객체마다 자신의 필드값을 가집니다.', 'SILVER', NULL, 1),
  (120105, @java_subject_id, 12, 1201, 'SHORT_ANSWER', '클래스로 표현하기 좋은 대상의 예를 한 가지 쓰시오.', '학생·상품·계좌처럼 책임이 분명한 대상', '관련 코드가 한곳에 모여 변경하기 쉬워집니다.', 'SILVER', NULL, 1),
  (120106, @java_subject_id, 12, 1201, 'FILL_BLANK', '상태를 저장하는 필드가 되도록 빈칸을 채우시오.
```java
class Counter { int ____; }
```', 'count', '필드는 객체의 상태를 저장합니다.', 'SILVER', NULL, 1),
  (120107, @java_subject_id, 12, 1201, 'CODE_OUTPUT', '방금 생성한 Counter 객체의 count 초기값을 쓰시오.', '0', 'int 필드는 기본값 0으로 초기화됩니다.', 'SILVER', NULL, 1),
  (120108, @java_subject_id, 12, 1201, 'CODE_SHORT', 'int 필드 count를 가진 Counter 클래스를 한 줄로 선언하시오.', '`class Counter { int count; }`', '클래스에 상태(필드)를 선언한 최소 형태입니다.', 'SILVER', NULL, 1),
  (120109, @java_subject_id, 12, 1201, 'CODE_OUTPUT', '서로 다른 두 Counter 객체는 count 값을 공유하는지 쓰시오.', '공유하지 않는다(각자 자신의 값을 가진다)', '인스턴스 필드는 객체별로 존재합니다.', 'SILVER', NULL, 1),
  (120110, @java_subject_id, 12, 1201, 'CODE_SHORT', 'counter 객체의 increase 메서드를 호출하는 문장 한 줄을 작성하시오.', '`counter.increase();`', '객체.메서드() 형식으로 호출합니다.', 'SILVER', NULL, 1),
  (120201, @java_subject_id, 12, 1202, 'MULTIPLE_CHOICE', '클래스와 객체의 관계로 옳은 것은?', '클래스는 설계도, 객체는 그 설계도로 만든 실물이다', '한 클래스로 여러 객체를 만들 수 있습니다.', 'SILVER', NULL, 1),
  (120202, @java_subject_id, 12, 1202, 'MULTIPLE_CHOICE', '다음 코드의 출력 결과는?
```java
class Student { String name; }
Student student = new Student();
student.name = "민수";
System.out.println(student.name);
```', '민수', '필드에 대입한 값이 출력됩니다.', 'SILVER', NULL, 1),
  (120203, @java_subject_id, 12, 1202, 'MULTIPLE_CHOICE', '빈칸에 들어갈 키워드는?
```java
Student student = ____ Student();
```', 'new', '객체 생성에는 new를 사용합니다.', 'SILVER', NULL, 1),
  (120204, @java_subject_id, 12, 1202, 'MULTIPLE_CHOICE', '한 클래스로 객체를 여러 개 만들면?', '가능하며 각 객체는 자신만의 필드값을 가진다', '객체마다 인스턴스 필드가 독립적입니다.', 'SILVER', NULL, 1),
  (120205, @java_subject_id, 12, 1202, 'SHORT_ANSWER', '클래스 선언에 포함될 수 있는 구성 요소 세 가지를 쓰시오.', '필드, 생성자, 메서드', '상태·초기화·동작을 담당합니다.', 'SILVER', NULL, 1),
  (120206, @java_subject_id, 12, 1202, 'FILL_BLANK', '빈칸에 들어갈 키워드를 쓰시오.
```java
____ Student { String name; }
```', 'class', '클래스 선언은 class 키워드로 시작합니다.', 'SILVER', NULL, 1),
  (120207, @java_subject_id, 12, 1202, 'CODE_OUTPUT', '다음 코드에서 b.name의 값을 쓰시오.
```java
Student a = new Student();
Student b = new Student();
a.name = "A";
System.out.println(b.name);
```', 'null', 'a와 b는 서로 다른 객체이므로 b의 필드는 초기값 그대로입니다.', 'SILVER', NULL, 1),
  (120208, @java_subject_id, 12, 1202, 'CODE_SHORT', 'name 필드를 가진 Student 클래스를 한 줄로 선언하시오.', '`class Student { String name; }`', '상태 하나를 가진 최소 클래스입니다.', 'SILVER', NULL, 1),
  (120209, @java_subject_id, 12, 1202, 'CODE_OUTPUT', '참조 변수 student에 실제로 저장되는 것이 무엇인지 쓰시오.', '객체를 찾아갈 수 있는 참조(주소)', '변수는 객체 자체가 아니라 참조를 저장합니다.', 'SILVER', NULL, 1),
  (120210, @java_subject_id, 12, 1202, 'CODE_SHORT', 'Student 객체를 만들어 student 변수에 저장하는 문장 한 줄을 작성하시오.', '`Student student = new Student();`', 'new가 객체를 만들고 참조를 반환합니다.', 'SILVER', NULL, 1),
  (120301, @java_subject_id, 12, 1203, 'MULTIPLE_CHOICE', '필드와 지역 변수의 차이로 옳은 것은?', '필드는 객체 생성 시 기본값으로 초기화된다', '지역 변수와 달리 필드는 기본값으로 초기화되고 객체가 존재하는 동안 유지됩니다.', 'SILVER', NULL, 1),
  (120302, @java_subject_id, 12, 1203, 'MULTIPLE_CHOICE', '다음 코드의 출력 결과는?
```java
class Account { int balance; }
Account account = new Account();
System.out.println(account.balance);
```', '0', 'int 필드의 기본값은 0입니다.', 'SILVER', NULL, 1),
  (120303, @java_subject_id, 12, 1203, 'MULTIPLE_CHOICE', '잔액 상태를 저장하는 필드가 되도록 빈칸을 채우시오.
```java
class Account { int ____; }
```', 'balance', '필드 이름은 저장하는 대상(잔액)이 드러나야 합니다.', 'SILVER', NULL, 1),
  (120304, @java_subject_id, 12, 1203, 'MULTIPLE_CHOICE', 'String 필드의 초기값은?', 'null', '참조 타입 필드는 null로 초기화됩니다.', 'SILVER', NULL, 1),
  (120305, @java_subject_id, 12, 1203, 'SHORT_ANSWER', '필드를 보통 private으로 감추는 이유를 쓰시오.', '외부 코드가 잘못된 값을 직접 넣는 일을 막기 위해', '메서드를 통해 안전하게 변경하게 합니다.', 'SILVER', NULL, 1),
  (120306, @java_subject_id, 12, 1203, 'CODE_OUTPUT', 'boolean 필드의 초기값을 쓰시오.', 'false', 'boolean 필드는 false로 초기화됩니다.', 'SILVER', NULL, 1),
  (120307, @java_subject_id, 12, 1203, 'FILL_BLANK', 'account 객체의 balance 필드에 1000을 대입하는 문장의 빈칸을 채우시오.
```java
account.____ = 1000;
```', 'balance', '객체.필드 형식으로 접근합니다.', 'SILVER', NULL, 1),
  (120308, @java_subject_id, 12, 1203, 'CODE_SHORT', 'int 필드 balance를 가진 Account 클래스를 한 줄로 선언하시오.', '`class Account { int balance; }`', '잔액 상태를 가진 최소 클래스입니다.', 'SILVER', NULL, 1),
  (120309, @java_subject_id, 12, 1203, 'CODE_OUTPUT', '두 Account 객체의 balance는 서로 어떤 관계인지 쓰시오.', '각자 독립적으로 유지된다', '인스턴스 필드는 객체별로 존재합니다.', 'SILVER', NULL, 1),
  (120310, @java_subject_id, 12, 1203, 'CODE_OUTPUT', 'double 필드의 초기값을 쓰시오.', '0.0', '실수 필드는 0.0으로 초기화됩니다.', 'SILVER', NULL, 1),
  (120401, @java_subject_id, 12, 1204, 'MULTIPLE_CHOICE', '인스턴스 메서드 호출 형식으로 옳은 것은?', '객체.메서드()', '인스턴스 메서드는 객체를 통해 호출합니다.', 'SILVER', NULL, 1),
  (120402, @java_subject_id, 12, 1204, 'MULTIPLE_CHOICE', '다음 코드의 출력 결과는?
```java
class Account { int balance; void deposit(int amount) { balance += amount; } }
Account account = new Account();
account.deposit(5000);
System.out.println(account.balance);
```', '5000', 'deposit이 balance에 5000을 더합니다.', 'SILVER', NULL, 1),
  (120403, @java_subject_id, 12, 1204, 'MULTIPLE_CHOICE', '빈칸에 들어갈 복합 대입 연산자는?
```java
void deposit(int amount) { balance ____ amount; }
```', '+=', '입금은 잔액에 금액을 더합니다.', 'SILVER', NULL, 1),
  (120404, @java_subject_id, 12, 1204, 'MULTIPLE_CHOICE', '같은 메서드를 서로 다른 객체에서 호출하면?', '호출한 객체의 필드값을 사용한다', '어느 객체에서 호출했는지에 따라 사용하는 필드가 다릅니다.', 'SILVER', NULL, 1),
  (120405, @java_subject_id, 12, 1204, 'SHORT_ANSWER', '같은 인스턴스 메서드가 객체마다 다른 결과를 내는 이유를 쓰시오.', '각 객체가 자신의 필드값을 사용하기 때문', '인스턴스 메서드는 소속 객체의 상태에 접근합니다.', 'SILVER', NULL, 1),
  (120406, @java_subject_id, 12, 1204, 'CODE_OUTPUT', 'deposit(1000)과 deposit(2000)을 연이어 호출한 뒤 balance를 쓰시오.', '3000', '1000 + 2000이 누적됩니다.', 'SILVER', NULL, 1),
  (120407, @java_subject_id, 12, 1204, 'FILL_BLANK', '5000을 입금하는 호출이 되도록 빈칸을 채우시오.
```java
account.____(5000);
```', 'deposit', '객체.메서드(인자) 형식입니다.', 'SILVER', NULL, 1),
  (120408, @java_subject_id, 12, 1204, 'CODE_SHORT', '잔액을 출력하는 인스턴스 메서드 `printBalance`를 한 줄로 선언하시오.', '`void printBalance() { System.out.println(balance); }`', '인스턴스 메서드는 필드에 바로 접근할 수 있습니다.', 'SILVER', NULL, 1),
  (120409, @java_subject_id, 12, 1204, 'CODE_OUTPUT', 'a.deposit(1000)을 호출했을 때 다른 객체 b의 balance를 쓰시오.', '0', 'b의 필드는 영향을 받지 않습니다.', 'SILVER', NULL, 1),
  (120410, @java_subject_id, 12, 1204, 'CODE_SHORT', 'account 객체에서 withdraw 메서드를 1000으로 호출하는 문장을 작성하시오.', '`account.withdraw(1000);`', '객체를 통해 인스턴스 메서드를 호출합니다.', 'SILVER', NULL, 1),
  (120501, @java_subject_id, 12, 1205, 'MULTIPLE_CHOICE', 'new의 역할로 옳은 것은?', '메모리에 객체를 만들고 참조를 반환한다', 'new는 객체 생성과 참조 반환을 담당합니다.', 'SILVER', NULL, 1),
  (120502, @java_subject_id, 12, 1205, 'MULTIPLE_CHOICE', '다음 코드의 출력 결과는?
```java
Student first = new Student();
Student second = first;
second.name = "영희";
System.out.println(first.name);
```', '영희', '두 변수가 같은 객체를 참조하므로 변경이 함께 보입니다.', 'SILVER', NULL, 1),
  (120503, @java_subject_id, 12, 1205, 'MULTIPLE_CHOICE', 'second가 first와 같은 객체를 가리키도록 빈칸을 채우시오.
```java
Student second = ____;
```', 'first', '참조를 대입하면 같은 객체를 가리킵니다.', 'SILVER', NULL, 1),
  (120504, @java_subject_id, 12, 1205, 'MULTIPLE_CHOICE', '참조 변수에 저장되는 것은?', '객체를 찾아갈 수 있는 참조', '변수는 객체가 아니라 참조를 저장합니다.', 'SILVER', NULL, 1),
  (120505, @java_subject_id, 12, 1205, 'SHORT_ANSWER', '두 변수가 같은 객체를 참조할 때 한 변수로 변경한 내용은 다른 변수에서 어떻게 보이는지 쓰시오.', '변경된 내용이 그대로 보인다', '같은 객체를 공유하기 때문입니다.', 'SILVER', NULL, 1),
  (120506, @java_subject_id, 12, 1205, 'CODE_OUTPUT', 'first와 second를 각각 new로 만들면 한쪽 변경이 다른 쪽에 영향을 주는지 쓰시오.', '영향을 주지 않는다', '서로 다른 객체이기 때문입니다.', 'SILVER', NULL, 1),
  (120507, @java_subject_id, 12, 1205, 'FILL_BLANK', '빈칸에 들어갈 클래스 이름을 쓰시오.
```java
Product p = new ____();
```', 'Product', 'new 뒤에는 생성할 클래스 이름과 괄호를 씁니다.', 'SILVER', NULL, 1),
  (120508, @java_subject_id, 12, 1205, 'CODE_SHORT', 'Account 객체를 만들어 acc 변수에 저장하는 문장 한 줄을 작성하시오.', '`Account acc = new Account();`', '기본 생성자 호출로 객체를 만듭니다.', 'SILVER', NULL, 1),
  (120509, @java_subject_id, 12, 1205, 'CODE_OUTPUT', 'null이 저장된 참조 변수로 메서드를 호출하면 어떻게 되는지 쓰시오.', '실행 중 오류(NullPointerException)가 발생한다', '참조할 객체가 없기 때문입니다.', 'SILVER', NULL, 1),
  (120510, @java_subject_id, 12, 1205, 'CODE_OUTPUT', '2번 문제의 코드에서 `first == second`의 결과를 쓰시오.', 'true', '같은 객체를 가리키는 두 참조는 ==로 같습니다.', 'SILVER', NULL, 1),
  (120601, @java_subject_id, 12, 1206, 'MULTIPLE_CHOICE', '생성자의 특징으로 옳은 것은?', '클래스와 이름이 같고 반환형이 없다', '생성자는 클래스 이름과 같고 반환형 표기가 없습니다.', 'SILVER', NULL, 1),
  (120602, @java_subject_id, 12, 1206, 'MULTIPLE_CHOICE', '다음 코드의 출력 결과는?
```java
class Product { String name; Product(String name) { this.name = name; } }
Product product = new Product("키보드");
System.out.println(product.name);
```', '키보드', '생성자가 필드를 초기화합니다.', 'SILVER', NULL, 1),
  (120603, @java_subject_id, 12, 1206, 'MULTIPLE_CHOICE', '빈칸에 들어갈 키워드는?
```java
Product(String name) { ____.name = name; }
```', 'this', 'this.name은 필드, name은 매개변수입니다.', 'SILVER', NULL, 1),
  (120604, @java_subject_id, 12, 1206, 'MULTIPLE_CHOICE', '생성자를 하나도 선언하지 않으면?', '매개변수 없는 기본 생성자가 제공된다', '기본 생성자가 자동으로 제공됩니다.', 'SILVER', NULL, 1),
  (120605, @java_subject_id, 12, 1206, 'SHORT_ANSWER', '생성자에 매개변수를 두는 장점을 쓰시오.', '필수값을 가진 상태로 객체를 만들 수 있다', '생성 직후부터 유효한 상태가 됩니다.', 'SILVER', NULL, 1),
  (120606, @java_subject_id, 12, 1206, 'CODE_OUTPUT', '`new Product("마우스")`로 만든 객체의 name을 쓰시오.', '마우스', '전달한 인자가 필드에 저장됩니다.', 'SILVER', NULL, 1),
  (120607, @java_subject_id, 12, 1206, 'FILL_BLANK', '빈칸에 들어갈 생성자 이름을 쓰시오. (클래스 이름 Product)
```java
____(String name) { this.name = name; }
```', 'Product', '생성자 이름은 클래스 이름과 같아야 합니다.', 'SILVER', NULL, 1),
  (120608, @java_subject_id, 12, 1206, 'CODE_SHORT', 'title을 받아 필드에 저장하는 Book 생성자를 한 줄로 작성하시오.', '`Book(String title) { this.title = title; }`', 'this로 필드와 매개변수를 구분합니다.', 'SILVER', NULL, 1),
  (120609, @java_subject_id, 12, 1206, 'CODE_OUTPUT', '매개변수 있는 생성자만 선언한 클래스에서 `new Product()`를 호출하면 어떻게 되는지 쓰시오.', '컴파일 오류가 발생한다', '생성자를 직접 선언하면 기본 생성자는 제공되지 않습니다.', 'SILVER', NULL, 1),
  (120610, @java_subject_id, 12, 1206, 'CODE_SHORT', '"키보드"를 전달해 Product 객체를 만드는 문장 한 줄을 작성하시오.', '`Product product = new Product("키보드");`', '생성자 인자와 함께 객체를 만듭니다.', 'SILVER', NULL, 1),
  (120701, @java_subject_id, 12, 1207, 'MULTIPLE_CHOICE', 'this가 가리키는 것은?', '현재 메서드나 생성자가 실행되고 있는 객체 자신', 'this는 객체 자신을 가리킵니다.', 'SILVER', NULL, 1),
  (120702, @java_subject_id, 12, 1207, 'MULTIPLE_CHOICE', '`this.name = name;`이 실행되면?', '필드에 매개변수 값이 저장된다', 'this.name은 필드, name은 매개변수입니다.', 'SILVER', NULL, 1),
  (120703, @java_subject_id, 12, 1207, 'MULTIPLE_CHOICE', '빈칸에 들어갈 기호는?
```java
this.name ____ name;
```', '=', '매개변수 값을 필드에 대입합니다.', 'SILVER', NULL, 1),
  (120704, @java_subject_id, 12, 1207, 'MULTIPLE_CHOICE', '필드와 매개변수 이름이 같을 때 this 없이 `name = name;`을 쓰면?', '매개변수가 자기 자신에 대입되어 필드는 그대로다', '가까운 이름(매개변수)이 우선이라 필드는 초기화되지 않습니다.', 'SILVER', NULL, 1),
  (120705, @java_subject_id, 12, 1207, 'SHORT_ANSWER', '`this(...)`의 용도와 작성 위치 규칙을 쓰시오.', '같은 클래스의 다른 생성자를 호출하며, 생성자의 첫 문장에 작성해야 한다', '생성자 중복 코드를 줄이는 문법입니다.', 'SILVER', NULL, 1),
  (120706, @java_subject_id, 12, 1207, 'CODE_OUTPUT', '다음 코드에서 u.name의 값을 쓰시오.
```java
class User { String name; User(String name) { this.name = name; } }
User u = new User("수진");
```', '수진', '생성자에서 this로 필드에 저장했습니다.', 'SILVER', NULL, 1),
  (120707, @java_subject_id, 12, 1207, 'FILL_BLANK', '필드에 저장되도록 빈칸을 채우시오.
```java
____.score = score;
```', 'this', '이름이 같을 때 this로 필드를 지정합니다.', 'SILVER', NULL, 1),
  (120708, @java_subject_id, 12, 1207, 'CODE_SHORT', '이름이 같은 필드와 매개변수를 구분해 age를 저장하는 대입문 한 줄을 작성하시오.', '`this.age = age;`', 'this가 붙은 쪽이 필드입니다.', 'SILVER', NULL, 1),
  (120709, @java_subject_id, 12, 1207, 'CODE_OUTPUT', '필드와 매개변수 이름이 다르면 this를 반드시 써야 하는지 쓰시오.', '반드시 쓰지 않아도 된다(생략 가능)', '구분이 필요할 때 this가 필수입니다.', 'SILVER', NULL, 1),
  (120710, @java_subject_id, 12, 1207, 'CODE_SHORT', 'name을 받아 필드에 저장하는 User 생성자 전체를 한 줄로 작성하시오.', '`User(String name) { this.name = name; }`', '생성자 + this 조합의 기본 형태입니다.', 'SILVER', NULL, 1),
  (120801, @java_subject_id, 12, 1208, 'MULTIPLE_CHOICE', 'private 멤버의 접근 범위는?', '선언한 클래스 내부에서만', 'private은 선언한 클래스 안에서만 접근할 수 있습니다.', 'SILVER', NULL, 1),
  (120802, @java_subject_id, 12, 1208, 'MULTIPLE_CHOICE', 'balance가 private일 때 외부에서 `account.balance`로 접근하면?', '컴파일 오류', 'private 필드는 외부에서 직접 접근할 수 없습니다.', 'SILVER', NULL, 1),
  (120803, @java_subject_id, 12, 1208, 'MULTIPLE_CHOICE', '외부 직접 접근을 막는 빈칸의 키워드는?
```java
____ int balance;
```', 'private', '필드 보호에는 private을 사용합니다.', 'SILVER', NULL, 1),
  (120804, @java_subject_id, 12, 1208, 'MULTIPLE_CHOICE', 'private 필드 balance를 외부에서 읽는 방법은?', 'public 메서드 getBalance()를 통해 읽는다', '공개 메서드를 통해 안전하게 읽습니다.', 'SILVER', NULL, 1),
  (120805, @java_subject_id, 12, 1208, 'SHORT_ANSWER', '필드를 private으로 보호했을 때의 효과를 쓰시오.', '외부 코드가 잘못된 값을 직접 넣는 일을 막는다', '변경 통로를 메서드로 제한합니다.', 'SILVER', NULL, 1),
  (120806, @java_subject_id, 12, 1208, 'CODE_OUTPUT', 'public 메서드는 어디에서 호출할 수 있는지 쓰시오.', '어디서나', 'public은 접근 제한이 없습니다.', 'SILVER', NULL, 1),
  (120807, @java_subject_id, 12, 1208, 'FILL_BLANK', '빈칸에 들어갈 메서드 이름을 쓰시오.
```java
public int ____() { return balance; }
```', 'getBalance', '필드를 읽는 공개 메서드(getter)입니다.', 'SILVER', NULL, 1),
  (120808, @java_subject_id, 12, 1208, 'CODE_SHORT', 'private String 필드 name을 선언하는 문장 한 줄을 작성하시오.', '`private String name;`', '접근 제어자 + 자료형 + 이름 순서입니다.', 'SILVER', NULL, 1),
  (120809, @java_subject_id, 12, 1208, 'CODE_OUTPUT', 'private 메서드를 클래스 외부에서 호출하면 어떻게 되는지 쓰시오.', '컴파일 오류가 발생한다', 'private은 메서드에도 적용됩니다.', 'SILVER', NULL, 1),
  (120810, @java_subject_id, 12, 1208, 'CODE_SHORT', 'private 필드 balance의 getter를 한 줄로 작성하시오.', '`public int getBalance() { return balance; }`', '읽기 전용 공개 통로입니다.', 'SILVER', NULL, 1),
  (120901, @java_subject_id, 12, 1209, 'MULTIPLE_CHOICE', '캡슐화에 대한 설명으로 옳은 것은?', '내부 상태를 감추고 공개된 메서드로만 다루게 한다', '검증을 거친 메서드로만 상태를 바꾸게 하는 설계입니다.', 'SILVER', NULL, 1),
  (120902, @java_subject_id, 12, 1209, 'MULTIPLE_CHOICE', 'balance가 1000일 때 다음 withdraw(2000) 호출 후 balance는?
```java
void withdraw(int amount) {
    if (amount > 0 && amount <= balance) balance -= amount;
}
```', '1000', '잔액보다 큰 금액은 조건에 걸려 변화가 없습니다.', 'SILVER', NULL, 1),
  (120903, @java_subject_id, 12, 1209, 'MULTIPLE_CHOICE', '빈칸에 들어갈 논리 연산자는?
```java
if (amount > 0 ____ amount <= balance) balance -= amount;
```', '&&', '두 검증 조건을 동시에 만족해야 출금합니다.', 'SILVER', NULL, 1),
  (120904, @java_subject_id, 12, 1209, 'MULTIPLE_CHOICE', 'balance가 1000일 때 withdraw(500) 호출 후 balance는?', '500', '검증을 통과해 500이 차감됩니다.', 'SILVER', NULL, 1),
  (120905, @java_subject_id, 12, 1209, 'SHORT_ANSWER', '모든 필드에 getter·setter를 만드는 것보다 나은 설계 방법을 쓰시오.', '객체가 수행해야 할 동작을 메서드로 제공한다', 'deposit·withdraw처럼 업무 동작 중심으로 공개합니다.', 'SILVER', NULL, 1),
  (120906, @java_subject_id, 12, 1209, 'CODE_OUTPUT', 'setter에 검증이 있으면 잘못된 값 전달 시 어떻게 되는지 쓰시오.', '저장되지 않는다(객체가 잘못된 상태가 되는 것을 막는다)', '검증이 캡슐화의 핵심 효과입니다.', 'SILVER', NULL, 1),
  (120907, @java_subject_id, 12, 1209, 'FILL_BLANK', '출금 동작이 되도록 빈칸에 들어갈 복합 대입 연산자를 쓰시오.
```java
balance ____ amount;
```', '-=', '잔액에서 금액을 뺍니다.', 'SILVER', NULL, 1),
  (120908, @java_subject_id, 12, 1209, 'CODE_SHORT', '0 이상일 때만 점수를 저장하는 setScore 메서드를 한 줄로 작성하시오.', '`void setScore(int score) { if (score >= 0) this.score = score; }`', '검증 후 저장하는 setter입니다.', 'SILVER', NULL, 1),
  (120909, @java_subject_id, 12, 1209, 'CODE_OUTPUT', '검증 없는 public 필드에 외부가 직접 대입하면 어떤 위험이 있는지 쓰시오.', '객체가 잘못된 상태(음수 잔액 등)가 될 수 있다', '캡슐화가 필요한 이유입니다.', 'SILVER', NULL, 1),
  (120910, @java_subject_id, 12, 1209, 'CODE_SHORT', '양수이고 잔액 이하일 때만 출금하는 withdraw 메서드를 한 줄로 작성하시오.', '`void withdraw(int amount) { if (amount > 0 && amount <= balance) balance -= amount; }`', '검증과 상태 변경을 한 메서드에 캡슐화했습니다.', 'SILVER', NULL, 1),
  (121001, @java_subject_id, 12, 1210, 'MULTIPLE_CHOICE', '클래스를 설계할 때 정해야 할 것으로 옳은 묶음은?', '저장할 상태·수행할 동작·생성 시 필요한 값', '상태(필드)·동작(메서드)·초기값(생성자)을 정합니다.', 'SILVER', NULL, 1),
  (121002, @java_subject_id, 12, 1210, 'MULTIPLE_CHOICE', '다음 코드의 출력 결과는?
```java
class Student {
    private final String name;
    private int score;
    Student(String name, int score) { this.name = name; this.score = score; }
    boolean isPassed() { return score >= 70; }
}
Student student = new Student("민수", 85);
System.out.println(student.isPassed());
```', 'true', '85 >= 70이 참입니다.', 'SILVER', NULL, 1),
  (121003, @java_subject_id, 12, 1210, 'MULTIPLE_CHOICE', '빈칸에 들어갈 연산자는?
```java
boolean isPassed() { return score ____ 70; }
```', '>=', '70점을 포함해 통과입니다.', 'SILVER', NULL, 1),
  (121004, @java_subject_id, 12, 1210, 'MULTIPLE_CHOICE', '`new Student("지수", 60).isPassed()`의 결과는?', 'false', '60 >= 70이 거짓입니다.', 'SILVER', NULL, 1),
  (121005, @java_subject_id, 12, 1210, 'SHORT_ANSWER', '필드를 감추고 생성자와 메서드가 함께 지켜야 할 목표를 쓰시오.', '객체가 유효한 상태를 유지하게 한다', '생성 시점부터 검증된 상태를 만듭니다.', 'SILVER', NULL, 1),
  (121006, @java_subject_id, 12, 1210, 'CODE_OUTPUT', '`private final String name` 필드는 생성 후 이름을 바꿀 수 있는지 쓰시오.', '바꿀 수 없다', 'final 필드는 재대입이 불가능합니다.', 'SILVER', NULL, 1),
  (121007, @java_subject_id, 12, 1210, 'FILL_BLANK', '빈칸에 들어갈 매개변수 이름을 쓰시오.
```java
Student(String name, int score) { this.name = name; this.score = ____; }
```', 'score', '매개변수 값을 필드에 저장합니다.', 'SILVER', NULL, 1),
  (121008, @java_subject_id, 12, 1210, 'CODE_SHORT', 'score가 70 이상인지 반환하는 isPassed 메서드를 한 줄로 작성하시오.', '`boolean isPassed() { return score >= 70; }`', '객체의 상태로 판단하는 동작입니다.', 'SILVER', NULL, 1),
  (121009, @java_subject_id, 12, 1210, 'CODE_OUTPUT', '생성자에서 입력값을 검증하면 어떤 효과가 있는지 쓰시오.', '잘못된 상태로 객체가 만들어지는 것을 막는다', '유효한 상태의 시작점을 보장합니다.', 'SILVER', NULL, 1),
  (121010, @java_subject_id, 12, 1210, 'CODE_SHORT', '"민수", 85로 Student 객체를 만들어 student에 저장하는 문장 한 줄을 작성하시오.', '`Student student = new Student("민수", 85);`', '생성자 인자 순서에 맞춰 전달합니다.', 'SILVER', NULL, 1),
  (130101, @java_subject_id, 13, 1301, 'MULTIPLE_CHOICE', '상속을 사용하기에 알맞은 관계는?', '자식이 부모의 한 종류일 때', '상속은 단순 코드 복사가 아니라 ''한 종류'' 관계가 성립할 때 사용합니다.', 'SILVER', NULL, 1),
  (130102, @java_subject_id, 13, 1301, 'MULTIPLE_CHOICE', '다음 코드의 출력 결과는?
```java
class Animal { void eat() { System.out.println("먹기"); } }
class Dog extends Animal { }
new Dog().eat();
```', '먹기', 'Dog는 부모의 eat 메서드를 이어받습니다.', 'SILVER', NULL, 1),
  (130103, @java_subject_id, 13, 1301, 'MULTIPLE_CHOICE', '빈칸에 들어갈 키워드는?
```java
class Dog ____ Animal { }
```', 'extends', '클래스 상속은 extends로 선언합니다.', 'SILVER', NULL, 1),
  (130104, @java_subject_id, 13, 1301, 'MULTIPLE_CHOICE', '자식 클래스는 부모의 필드와 메서드를 어떻게 사용하는가?', '이어받아 그대로 사용 가능', '상속으로 부모의 멤버를 이어받습니다.', 'SILVER', NULL, 1),
  (130105, @java_subject_id, 13, 1301, 'SHORT_ANSWER', '여러 자식이 함께 쓰는 공통 기능은 어디에 두어야 하는지 쓰시오.', '부모 클래스', '공통은 부모에, 차이는 자식에 둡니다.', 'SILVER', NULL, 1),
  (130106, @java_subject_id, 13, 1301, 'CODE_OUTPUT', 'Dog가 eat을 재정의하지 않고 호출하면 어떤 코드가 실행되는지 쓰시오.', '부모(Animal)의 eat이 실행된다', '재정의가 없으면 이어받은 구현을 사용합니다.', 'SILVER', NULL, 1),
  (130107, @java_subject_id, 13, 1301, 'FILL_BLANK', 'Animal을 상속하는 클래스 이름이 Cat이 되도록 빈칸을 채우시오.
```java
class ____ extends Animal { }
```', 'Cat', '자식 클래스 이름 자리입니다.', 'SILVER', NULL, 1),
  (130108, @java_subject_id, 13, 1301, 'CODE_SHORT', 'Animal을 상속하는 Cat 클래스를 빈 본문으로 한 줄 선언하시오.', '`class Cat extends Animal { }`', 'extends 뒤에 부모 클래스를 적습니다.', 'SILVER', NULL, 1),
  (130109, @java_subject_id, 13, 1301, 'CODE_OUTPUT', '상속이 ''단순 코드 복사 기능''인지 아닌지, 한 문장으로 쓰시오.', '아니다. 자식이 부모의 한 종류라는 관계가 성립할 때 사용하는 기능이다', '관계가 어색하면 상속 대신 다른 방법을 씁니다.', 'SILVER', NULL, 1),
  (130110, @java_subject_id, 13, 1301, 'CODE_SHORT', 'Dog 객체를 만들어 eat을 호출하는 문장 한 줄을 작성하시오.', '`new Dog().eat();`', '생성과 호출을 이어 쓸 수 있습니다.', 'SILVER', NULL, 1),
  (130201, @java_subject_id, 13, 1302, 'MULTIPLE_CHOICE', 'Java 클래스가 직접 상속할 수 있는 클래스의 수는?', '하나', 'Java 클래스는 단일 상속만 지원합니다.', 'SILVER', NULL, 1),
  (130202, @java_subject_id, 13, 1302, 'MULTIPLE_CHOICE', '다음 코드의 출력 결과는?
```java
class Vehicle { int speed = 10; }
class Car extends Vehicle { void accelerate() { speed += 10; } }
Car car = new Car();
car.accelerate();
System.out.println(car.speed);
```', '20', '부모의 speed(10)에 10이 더해집니다.', 'SILVER', NULL, 1),
  (130203, @java_subject_id, 13, 1302, 'MULTIPLE_CHOICE', '빈칸에 들어갈 키워드는?
```java
class Car ____ Vehicle { }
```', 'extends', '상속 선언은 extends입니다.', 'SILVER', NULL, 1),
  (130204, @java_subject_id, 13, 1302, 'MULTIPLE_CHOICE', '부모의 private 멤버를 자식 클래스에서 직접 접근하면?', '컴파일 오류', 'private 멤버는 자식에서도 직접 접근할 수 없습니다.', 'SILVER', NULL, 1),
  (130205, @java_subject_id, 13, 1302, 'SHORT_ANSWER', '부모의 private 멤버를 자식이 사용하는 방법을 쓰시오.', '부모의 공개(public) 메서드를 통해 사용한다', '직접 접근 대신 공개 통로를 이용합니다.', 'SILVER', NULL, 1),
  (130206, @java_subject_id, 13, 1302, 'CODE_OUTPUT', '2번 코드에서 accelerate를 두 번 호출하면 speed는 얼마인지 쓰시오.', '30', '10 → 20 → 30으로 누적됩니다.', 'SILVER', NULL, 1),
  (130207, @java_subject_id, 13, 1302, 'FILL_BLANK', '가속 동작이 되도록 빈칸에 들어갈 복합 대입 연산자를 쓰시오.
```java
void accelerate() { speed ____ 10; }
```', '+=', '기존 속도에 10을 더합니다.', 'SILVER', NULL, 1),
  (130208, @java_subject_id, 13, 1302, 'CODE_SHORT', 'Vehicle을 상속하는 Bike 클래스를 빈 본문으로 한 줄 선언하시오.', '`class Bike extends Vehicle { }`', '부모의 speed 필드를 그대로 이어받습니다.', 'SILVER', NULL, 1),
  (130209, @java_subject_id, 13, 1302, 'CODE_OUTPUT', '`class Car extends Vehicle, Machine`처럼 두 클래스를 동시에 상속하면 어떻게 되는지 쓰시오.', '컴파일 오류가 발생한다', '클래스 다중 상속은 허용되지 않습니다.', 'SILVER', NULL, 1),
  (130210, @java_subject_id, 13, 1302, 'CODE_OUTPUT', 'Car 객체에서 부모 필드 speed를 사용할 수 있는지 쓰시오.', '사용할 수 있다', 'public/기본 접근의 부모 필드는 상속되어 사용 가능합니다.', 'SILVER', NULL, 1),
  (130301, @java_subject_id, 13, 1303, 'MULTIPLE_CHOICE', '`super(...)`의 역할은?', '부모 생성자를 호출한다', '자식 생성자는 부모 생성자를 먼저 실행해 부모 부분을 초기화합니다.', 'SILVER', NULL, 1),
  (130302, @java_subject_id, 13, 1303, 'MULTIPLE_CHOICE', '다음 코드의 출력 결과는?
```java
class Person { String name; Person(String name) { this.name = name; } }
class Student extends Person { Student(String name) { super(name); } }
System.out.println(new Student("수진").name);
```', '수진', 'super(name)이 부모 생성자에 이름을 전달합니다.', 'SILVER', NULL, 1),
  (130303, @java_subject_id, 13, 1303, 'MULTIPLE_CHOICE', '빈칸에 들어갈 키워드는?
```java
Student(String name) { ____(name); }
```', 'super', '부모 생성자 호출은 super(...)입니다.', 'SILVER', NULL, 1),
  (130304, @java_subject_id, 13, 1303, 'MULTIPLE_CHOICE', '자식 생성자에서 부모 부분의 초기화 순서는?', '부모 먼저', '부모 생성자가 먼저 실행됩니다.', 'SILVER', NULL, 1),
  (130305, @java_subject_id, 13, 1303, 'SHORT_ANSWER', '`super.method()`를 사용하는 목적을 쓰시오.', '부모의 멤버(메서드)를 명확히 선택해 호출하기 위해', '오버라이딩된 상황에서 부모 구현을 부를 때 씁니다.', 'SILVER', NULL, 1),
  (130306, @java_subject_id, 13, 1303, 'CODE_OUTPUT', '부모에 매개변수 있는 생성자만 있는데 자식 생성자에서 super 호출을 생략하면 어떻게 되는지 쓰시오.', '컴파일 오류가 발생한다', '부모에 기본 생성자가 없으면 명시적으로 super(...)를 호출해야 합니다.', 'SILVER', NULL, 1),
  (130307, @java_subject_id, 13, 1303, 'FILL_BLANK', '부모의 sound 메서드를 호출하도록 빈칸을 채우시오.
```java
super.____();
```', 'sound', 'super.메서드() 형식입니다.', 'SILVER', NULL, 1),
  (130308, @java_subject_id, 13, 1303, 'CODE_SHORT', 'name을 부모 생성자로 전달하는 Student 생성자를 한 줄로 작성하시오.', '`Student(String name) { super(name); }`', '부모 초기화를 위임합니다.', 'SILVER', NULL, 1),
  (130309, @java_subject_id, 13, 1303, 'CODE_OUTPUT', '`super(name)`은 생성자 안 어느 위치에 있어야 하는지 쓰시오.', '첫 문장', '부모 초기화가 가장 먼저 일어나야 합니다.', 'SILVER', NULL, 1),
  (130310, @java_subject_id, 13, 1303, 'CODE_SHORT', '부모의 name 필드를 super로 출력하는 문장 한 줄을 작성하시오.', '`System.out.println(super.name);`', 'super.필드로 부모 멤버를 명시적으로 선택합니다.', 'SILVER', NULL, 1),
  (130401, @java_subject_id, 13, 1304, 'MULTIPLE_CHOICE', '오버라이딩의 정의로 옳은 것은?', '자식이 부모의 인스턴스 메서드를 같은 선언 형태로 다시 구현하는 것', '매개변수가 다르면 오버로딩입니다.', 'SILVER', NULL, 1),
  (130402, @java_subject_id, 13, 1304, 'MULTIPLE_CHOICE', '다음 코드의 출력 결과는?
```java
class Animal { void sound() { System.out.println("소리"); } }
class Dog extends Animal {
    @Override void sound() { System.out.println("멍멍"); }
}
new Dog().sound();
```', '멍멍', '자식이 재정의한 구현이 실행됩니다.', 'SILVER', NULL, 1),
  (130403, @java_subject_id, 13, 1304, 'MULTIPLE_CHOICE', '재정의 검증 애너테이션의 빈칸을 채우면?
```java
____ void sound() { System.out.println("멍멍"); }
```', '@Override', '@Override는 잘못된 재정의를 컴파일러가 확인하게 합니다.', 'SILVER', NULL, 1),
  (130404, @java_subject_id, 13, 1304, 'MULTIPLE_CHOICE', '`@Override`를 붙였는데 메서드 이름을 sownd로 잘못 쓰면?', '컴파일 오류가 발생한다', '재정의 대상이 없으므로 컴파일러가 잡아 줍니다.', 'SILVER', NULL, 1),
  (130405, @java_subject_id, 13, 1304, 'SHORT_ANSWER', '@Override가 없어도 오버라이딩은 동작하지만, 붙이는 습관이 권장되는 이유를 쓰시오.', '이름·매개변수를 잘못 쓰면 새 메서드로 처리되는 버그를 컴파일 단계에서 잡을 수 있어서', '재정의 의도를 컴파일러가 검증합니다.', 'SILVER', NULL, 1),
  (130406, @java_subject_id, 13, 1304, 'CODE_OUTPUT', 'sound를 재정의하지 않은 자식 Cat에서 `new Cat().sound()`를 호출하면 출력 결과를 쓰시오.', '소리', '재정의가 없으면 부모 구현이 실행됩니다.', 'SILVER', NULL, 1),
  (130407, @java_subject_id, 13, 1304, 'FILL_BLANK', '재정의 메서드 이름의 빈칸을 채우시오.
```java
@Override void ____() { System.out.println("멍멍"); }
```', 'sound', '부모와 같은 이름·매개변수여야 합니다.', 'SILVER', NULL, 1),
  (130408, @java_subject_id, 13, 1304, 'CODE_SHORT', 'sound를 "야옹" 출력으로 재정의하는 메서드를 한 줄로 작성하시오.', '`@Override void sound() { System.out.println("야옹"); }`', '같은 선언 형태 + 다른 구현입니다.', 'SILVER', NULL, 1),
  (130409, @java_subject_id, 13, 1304, 'CODE_OUTPUT', '부모가 public인 메서드를 자식이 접근 범위를 좁혀 재정의하면 어떻게 되는지 쓰시오.', '컴파일 오류가 발생한다', '오버라이딩은 접근 범위를 좁힐 수 없습니다.', 'SILVER', NULL, 1),
  (130410, @java_subject_id, 13, 1304, 'CODE_OUTPUT', '이름은 같지만 매개변수 목록이 다르게 선언하면 오버라이딩인지 무엇인지 쓰시오.', '오버라이딩이 아니라 오버로딩이다', '시그니처가 다르면 별개의 메서드입니다.', 'SILVER', NULL, 1),
  (130501, @java_subject_id, 13, 1305, 'MULTIPLE_CHOICE', '다형성에 대한 설명으로 옳은 것은?', '부모 타입 변수 하나로 여러 자식 객체를 다룰 수 있다', '부모 타입 참조로 여러 자식 구현을 처리합니다.', 'SILVER', NULL, 1),
  (130502, @java_subject_id, 13, 1305, 'MULTIPLE_CHOICE', '다음 코드의 출력 결과는?
```java
Animal animal = new Dog(); // Dog는 sound를 "멍멍"으로 재정의
animal.sound();
```', '멍멍', '오버라이딩 메서드는 실제 객체(Dog)의 구현이 실행됩니다.', 'SILVER', NULL, 1),
  (130503, @java_subject_id, 13, 1305, 'MULTIPLE_CHOICE', '부모 타입 변수 선언의 빈칸을 채우면?
```java
____ animal = new Dog();
```', 'Animal', '부모 타입 변수에 자식 객체를 저장합니다.', 'SILVER', NULL, 1),
  (130504, @java_subject_id, 13, 1305, 'MULTIPLE_CHOICE', '호출할 수 있는 멤버의 범위를 결정하는 기준은?', '변수(참조) 타입', '호출 가능 여부는 변수 타입, 실행 구현은 실제 객체 기준입니다.', 'SILVER', NULL, 1),
  (130505, @java_subject_id, 13, 1305, 'SHORT_ANSWER', '오버라이딩된 메서드가 실행될 때 어느 쪽 구현이 선택되는지 쓰시오.', '실제 객체의 구현', '참조 타입이 아니라 실제 객체가 기준입니다.', 'SILVER', NULL, 1),
  (130506, @java_subject_id, 13, 1305, 'CODE_OUTPUT', 'Cat이 sound를 "야옹"으로 재정의했을 때 다음 코드의 출력 결과를 쓰시오.
```java
Animal a = new Cat();
a.sound();
```', '야옹', '실제 객체 Cat의 구현이 실행됩니다.', 'SILVER', NULL, 1),
  (130507, @java_subject_id, 13, 1305, 'FILL_BLANK', '빈칸에 들어갈 클래스 이름을 쓰시오.
```java
Animal a = new ____(); // 강아지 객체
```', 'Dog', '자식 객체를 부모 타입에 저장하는 다형성 형태입니다.', 'SILVER', NULL, 1),
  (130508, @java_subject_id, 13, 1305, 'CODE_SHORT', '부모 타입 Animal 변수 animal에 Dog 객체를 저장하는 문장 한 줄을 작성하시오.', '`Animal animal = new Dog();`', '업캐스팅이 자동으로 일어납니다.', 'SILVER', NULL, 1),
  (130509, @java_subject_id, 13, 1305, 'CODE_OUTPUT', 'Dog에만 있는 bark()를 Animal 타입 변수로 호출하면 어떻게 되는지 쓰시오.', '컴파일 오류가 발생한다', '변수 타입(Animal)에 선언되지 않은 멤버는 호출할 수 없습니다.', 'SILVER', NULL, 1),
  (130510, @java_subject_id, 13, 1305, 'CODE_OUTPUT', '새 자식 클래스가 추가될 때 부모 타입을 사용하는 기존 코드는 어떻게 되는지 쓰시오.', '바꾸지 않아도 된다', '다형성의 핵심 이점입니다.', 'SILVER', NULL, 1),
  (130601, @java_subject_id, 13, 1306, 'MULTIPLE_CHOICE', '업캐스팅에 대한 설명으로 옳은 것은?', '자식 객체를 부모 타입 변수에 저장하는 것으로 자동으로 이루어진다', '자식 → 부모 방향은 자동입니다.', 'SILVER', NULL, 1),
  (130602, @java_subject_id, 13, 1306, 'MULTIPLE_CHOICE', '다음 코드의 실행 결과로 옳은 것은? (Dog는 "멍멍", Cat은 "야옹" 재정의)
```java
Animal[] animals = {new Dog(), new Cat()};
for (Animal animal : animals) animal.sound();
```', '멍멍 야옹', '요소마다 실제 타입의 오버라이딩 결과가 실행됩니다.', 'SILVER', NULL, 1),
  (130603, @java_subject_id, 13, 1306, 'MULTIPLE_CHOICE', '빈칸에 들어갈 클래스 이름은?
```java
Animal[] animals = {new Dog(), new ____()};
```', 'Cat', '서로 다른 자식 객체를 한 배열에 담습니다.', 'SILVER', NULL, 1),
  (130604, @java_subject_id, 13, 1306, 'MULTIPLE_CHOICE', '업캐스팅 후 실제 객체는?', '바뀌지 않는다', '참조 타입만 부모로 다뤄질 뿐 객체는 그대로입니다.', 'SILVER', NULL, 1),
  (130605, @java_subject_id, 13, 1306, 'SHORT_ANSWER', '업캐스팅 후 참조 타입이 결정하는 것은 무엇인지 쓰시오.', '호출할 수 있는 멤버의 범위', '실행 구현은 여전히 실제 객체 기준입니다.', 'SILVER', NULL, 1),
  (130606, @java_subject_id, 13, 1306, 'CODE_OUTPUT', '2번 문제에서 배열 순회 시 각 요소의 sound 실행 기준을 쓰시오.', '각 요소의 실제 타입(Dog·Cat)의 오버라이딩 구현', '반복문 하나로 여러 구현을 처리합니다.', 'SILVER', NULL, 1),
  (130607, @java_subject_id, 13, 1306, 'FILL_BLANK', '공통 타입 배열 선언의 빈칸을 채우시오.
```java
____[] animals = {new Dog(), new Cat()};
```', 'Animal', '공통 부모 타입 배열로 여러 자식을 담습니다.', 'SILVER', NULL, 1),
  (130608, @java_subject_id, 13, 1306, 'CODE_SHORT', 'Dog와 Cat을 담는 Animal 배열을 한 줄로 선언·초기화하시오.', '`Animal[] animals = {new Dog(), new Cat()};`', '업캐스팅이 자동 적용됩니다.', 'SILVER', NULL, 1),
  (130609, @java_subject_id, 13, 1306, 'CODE_OUTPUT', '업캐스팅에 명시적 캐스팅 문법이 필요한지 쓰시오.', '필요 없다(자동)', '넓은 타입으로의 이동은 자동입니다.', 'SILVER', NULL, 1),
  (130610, @java_subject_id, 13, 1306, 'CODE_OUTPUT', '부모 타입 매개변수를 받는 메서드에 자식 객체를 전달할 수 있는지 쓰시오.', '전달할 수 있다', '매개변수 전달에서도 업캐스팅이 자동으로 일어납니다.', 'SILVER', NULL, 1),
  (130701, @java_subject_id, 13, 1307, 'MULTIPLE_CHOICE', '다운캐스팅에 대한 설명으로 옳은 것은?', '명시적 캐스팅이 필요하다', '부모 → 자식 방향은 (자식타입) 캐스팅을 명시해야 합니다.', 'SILVER', NULL, 1),
  (130702, @java_subject_id, 13, 1307, 'MULTIPLE_CHOICE', '다음 코드의 출력 결과는? (Dog는 "멍멍" 재정의)
```java
Animal animal = new Dog();
Dog dog = (Dog) animal;
dog.sound();
```', '멍멍', '실제 객체가 Dog이므로 캐스팅이 성공합니다.', 'SILVER', NULL, 1),
  (130703, @java_subject_id, 13, 1307, 'MULTIPLE_CHOICE', '빈칸에 들어갈 타입은?
```java
Dog dog = (____) animal;
```', 'Dog', '변환할 대상 자식 타입을 괄호에 적습니다.', 'SILVER', NULL, 1),
  (130704, @java_subject_id, 13, 1307, 'MULTIPLE_CHOICE', '실제 객체가 Cat인데 `(Dog)`로 캐스팅하면?', '실행 중 ClassCastException', '실제 타입이 다르면 실행 중 예외가 발생합니다.', 'SILVER', NULL, 1),
  (130705, @java_subject_id, 13, 1307, 'SHORT_ANSWER', '다운캐스팅 전에 안전을 확인하는 방법을 쓰시오.', 'instanceof로 실제 타입을 확인한다', '확인 없는 캐스팅은 프로그램 중단 위험이 있습니다.', 'SILVER', NULL, 1),
  (130706, @java_subject_id, 13, 1307, 'CODE_OUTPUT', '`Dog d = (Dog) new Animal();`을 실행하면 어떻게 되는지 쓰시오.', '실행 중 ClassCastException이 발생한다', '실제 객체가 Dog가 아니기 때문입니다.', 'SILVER', NULL, 1),
  (130707, @java_subject_id, 13, 1307, 'FILL_BLANK', '빈칸에 들어갈 변수 이름을 쓰시오.
```java
Dog dog = (Dog) ____; // 부모 타입 변수 animal을 변환
```', 'animal', '변환할 참조를 지정합니다.', 'SILVER', NULL, 1),
  (130708, @java_subject_id, 13, 1307, 'CODE_SHORT', 'animal을 Dog 타입으로 캐스팅해 dog 변수에 저장하는 문장 한 줄을 작성하시오.', '`Dog dog = (Dog) animal;`', '명시적 다운캐스팅 문법입니다.', 'SILVER', NULL, 1),
  (130709, @java_subject_id, 13, 1307, 'CODE_OUTPUT', '다운캐스팅이 코드 곳곳에서 자주 필요하다면 무엇을 의심해야 하는지 쓰시오.', '설계를 다시 살펴볼 신호(공통 동작을 부모·인터페이스로 끌어올릴 것)', '다형성으로 해결할 수 있는 경우가 많습니다.', 'SILVER', NULL, 1),
  (130710, @java_subject_id, 13, 1307, 'CODE_OUTPUT', '다운캐스팅이 성공하기 위한 조건을 쓰시오.', '실제 객체가 해당 자식 타입이어야 한다', '참조 타입이 아니라 실제 객체가 기준입니다.', 'SILVER', NULL, 1),
  (130801, @java_subject_id, 13, 1308, 'MULTIPLE_CHOICE', 'instanceof의 반환값 자료형은?', 'boolean', '해당 타입으로 안전하게 사용할 수 있는지 true/false로 알려 줍니다.', 'SILVER', NULL, 1),
  (130802, @java_subject_id, 13, 1308, 'MULTIPLE_CHOICE', '`null instanceof Dog`의 결과는?', 'false', 'null에 instanceof를 사용하면 예외가 아니라 false입니다.', 'SILVER', NULL, 1),
  (130803, @java_subject_id, 13, 1308, 'MULTIPLE_CHOICE', '패턴 매칭 문법의 빈칸을 채우면?
```java
if (animal ____ Dog dog) { dog.sound(); }
```', 'instanceof', 'instanceof 패턴 매칭은 확인과 캐스팅을 한 번에 합니다.', 'SILVER', NULL, 1),
  (130804, @java_subject_id, 13, 1308, 'MULTIPLE_CHOICE', '패턴 매칭의 변수 dog는 언제 만들어지는가?', '조건이 참일 때만', '조건이 참일 때만 변수가 유효합니다.', 'SILVER', NULL, 1),
  (130805, @java_subject_id, 13, 1308, 'SHORT_ANSWER', 'instanceof 패턴 매칭 문법의 장점을 쓰시오.', '타입 확인과 캐스팅을 한 번에 처리해 별도 캐스팅 문장이 필요 없다', 'Java 16+ 문법입니다.', 'SILVER', NULL, 1),
  (130806, @java_subject_id, 13, 1308, 'CODE_OUTPUT', '`new Dog() instanceof Animal`의 결과를 쓰시오.', 'true', 'instanceof는 상속 관계 전체를 확인하므로 자식 객체는 부모 검사에서도 true입니다.', 'SILVER', NULL, 1),
  (130807, @java_subject_id, 13, 1308, 'FILL_BLANK', '패턴 매칭 변수 이름의 빈칸을 채우시오.
```java
if (a instanceof Cat ____) { }
```', 'cat', '조건 참일 때 사용할 변수 이름입니다.', 'SILVER', NULL, 1),
  (130808, @java_subject_id, 13, 1308, 'CODE_SHORT', 'animal이 Cat이면 sound를 호출하는 if문을 패턴 매칭으로 한 줄 작성하시오.', '`if (animal instanceof Cat cat) { cat.sound(); }`', '확인+캐스팅+사용을 한 번에 합니다.', 'SILVER', NULL, 1),
  (130809, @java_subject_id, 13, 1308, 'CODE_OUTPUT', '부모·자식 타입을 모두 검사해야 할 때 어느 타입부터 검사해야 하는지 쓰시오.', '더 구체적인(자식) 타입부터', '부모 검사가 먼저면 자식도 걸려 버립니다.', 'SILVER', NULL, 1),
  (130810, @java_subject_id, 13, 1308, 'CODE_OUTPUT', 'instanceof 결과가 false면 패턴 매칭 if 블록은 어떻게 되는지 쓰시오.', '실행되지 않는다', '변수도 만들어지지 않습니다.', 'SILVER', NULL, 1),
  (130901, @java_subject_id, 13, 1309, 'MULTIPLE_CHOICE', 'final 클래스의 특징은?', '상속할 수 없다', 'final 클래스는 더 이상 확장하면 안 되는 완성된 설계임을 나타냅니다.', 'SILVER', NULL, 1),
  (130902, @java_subject_id, 13, 1309, 'MULTIPLE_CHOICE', '다음 코드를 컴파일하면?
```java
final class Utility { }
class Child extends Utility { }
```', '컴파일 오류', 'final 클래스는 상속할 수 없습니다.', 'SILVER', NULL, 1),
  (130903, @java_subject_id, 13, 1309, 'MULTIPLE_CHOICE', '상속을 막는 빈칸의 키워드는?
```java
____ class Utility { }
```', 'final', 'final이 상속을 제한합니다.', 'SILVER', NULL, 1),
  (130904, @java_subject_id, 13, 1309, 'MULTIPLE_CHOICE', 'final 메서드를 자식 클래스에서 오버라이딩하면?', '컴파일 오류', 'final 메서드는 재정의할 수 없습니다.', 'SILVER', NULL, 1),
  (130905, @java_subject_id, 13, 1309, 'SHORT_ANSWER', 'final 메서드는 어떤 경우에 사용하는지 쓰시오.', '자식이 동작을 바꾸면 안 되는 핵심 규칙에 사용', '변경을 막아야 할 분명한 이유가 있을 때만 씁니다.', 'SILVER', NULL, 1),
  (130906, @java_subject_id, 13, 1309, 'CODE_OUTPUT', '자바 표준 라이브러리의 String 클래스를 상속할 수 있는지 쓰시오.', '상속할 수 없다(final 클래스)', '핵심 클래스는 final로 보호되어 있습니다.', 'SILVER', NULL, 1),
  (130907, @java_subject_id, 13, 1309, 'FILL_BLANK', '재정의 금지 메서드 선언의 빈칸을 채우시오.
```java
final ____ sound() { System.out.println("소리"); }
```', 'void', 'final + 반환형 + 이름 순서입니다.', 'SILVER', NULL, 1),
  (130908, @java_subject_id, 13, 1309, 'CODE_SHORT', '상속할 수 없는 Utility 클래스를 빈 본문으로 한 줄 선언하시오.', '`final class Utility { }`', 'final 클래스 선언 형태입니다.', 'SILVER', NULL, 1),
  (130909, @java_subject_id, 13, 1309, 'CODE_OUTPUT', 'final을 무분별하게 붙이면 어떤 문제가 생기는지 쓰시오.', '확장성이 떨어진다', '필요한 곳에만 제한을 둡니다.', 'SILVER', NULL, 1),
  (130910, @java_subject_id, 13, 1309, 'CODE_OUTPUT', 'final 클래스의 객체 생성은 가능한지 쓰시오.', '가능하다', 'final은 상속만 금지할 뿐 객체 생성과는 무관합니다.', 'SILVER', NULL, 1),
  (131001, @java_subject_id, 13, 1310, 'MULTIPLE_CHOICE', '부모 클래스에 두어야 할 것은?', '진짜 공통인 상태와 동작', '자식별 차이는 오버라이딩으로 표현합니다.', 'SILVER', NULL, 1),
  (131002, @java_subject_id, 13, 1310, 'MULTIPLE_CHOICE', '다음 코드의 출력 결과는? (Dog는 "멍멍" 재정의)
```java
static void playSound(Animal animal) { animal.sound(); }
playSound(new Dog());
```', '멍멍', '전달된 실제 객체의 구현이 실행됩니다.', 'SILVER', NULL, 1),
  (131003, @java_subject_id, 13, 1310, 'MULTIPLE_CHOICE', '빈칸에 들어갈 매개변수 타입은?
```java
static void playSound(____ animal) { animal.sound(); }
```', 'Animal', '부모 타입 매개변수로 모든 자식을 받습니다.', 'SILVER', NULL, 1),
  (131004, @java_subject_id, 13, 1310, 'MULTIPLE_CHOICE', '`playSound(new Cat())`의 출력 결과는? (Cat은 "야옹" 재정의)', '야옹', 'Cat의 오버라이딩 구현이 실행됩니다.', 'SILVER', NULL, 1),
  (131005, @java_subject_id, 13, 1310, 'SHORT_ANSWER', '코드 재사용만이 목적일 때 상속의 대안을 쓰시오.', '다른 객체를 필드로 포함하는 방법(구성)', '''한 종류'' 관계가 아니면 포함이 적합할 수 있습니다.', 'SILVER', NULL, 1),
  (131006, @java_subject_id, 13, 1310, 'CODE_OUTPUT', '새 자식 Bird가 추가되어도 playSound 메서드는 수정이 필요한지 쓰시오.', '필요 없다', '확장에는 열려 있고 수정에는 닫힌 구조입니다.', 'SILVER', NULL, 1),
  (131007, @java_subject_id, 13, 1310, 'FILL_BLANK', '빈칸에 들어갈 클래스 이름을 쓰시오.
```java
playSound(new ____()); // 강아지 소리 재생
```', 'Dog', '자식 객체를 그대로 전달합니다.', 'SILVER', NULL, 1),
  (131008, @java_subject_id, 13, 1310, 'CODE_SHORT', 'Animal을 받아 sound를 호출하는 playSound 메서드를 한 줄로 선언하시오.', '`static void playSound(Animal animal) { animal.sound(); }`', '부모 타입에 의존하는 다형성 활용 메서드입니다.', 'SILVER', NULL, 1),
  (131009, @java_subject_id, 13, 1310, 'CODE_OUTPUT', '사용하는 코드가 가능한 부모 타입에 의존하면 어떤 장점이 있는지 쓰시오.', '새 구현(자식) 추가가 쉬워진다', '기존 코드 수정 없이 확장할 수 있습니다.', 'SILVER', NULL, 1),
  (131010, @java_subject_id, 13, 1310, 'CODE_SHORT', 'playSound에 Dog 객체를 전달해 호출하는 문장 한 줄을 작성하시오.', '`playSound(new Dog());`', '업캐스팅으로 자동 전달됩니다.', 'SILVER', NULL, 1),
  (140101, @java_subject_id, 14, 1401, 'MULTIPLE_CHOICE', '인터페이스에 대한 설명으로 옳은 것은?', '구현 클래스가 제공해야 할 기능의 규칙(계약)을 선언한다', '사용하는 코드는 구체 클래스 대신 인터페이스를 바라봅니다.', 'SILVER', NULL, 1),
  (140102, @java_subject_id, 14, 1401, 'MULTIPLE_CHOICE', '다음 코드의 출력 결과는?
```java
interface Printer { void print(String text); }
class ConsolePrinter implements Printer {
    public void print(String text) { System.out.println(text); }
}
new ConsolePrinter().print("출력");
```', '출력', '구현 클래스의 print가 실행됩니다.', 'SILVER', NULL, 1),
  (140103, @java_subject_id, 14, 1401, 'MULTIPLE_CHOICE', '빈칸에 들어갈 반환형은?
```java
interface Printer { ____ print(String text); }
```', 'void', '출력만 하는 기능 선언입니다.', 'SILVER', NULL, 1),
  (140104, @java_subject_id, 14, 1401, 'MULTIPLE_CHOICE', '구현을 ConsolePrinter에서 FilePrinter로 교체하면 인터페이스를 사용하는 코드는?', '그대로 사용할 수 있다', '인터페이스 타입에 의존하면 구현 교체가 쉽습니다.', 'SILVER', NULL, 1),
  (140105, @java_subject_id, 14, 1401, 'SHORT_ANSWER', '인터페이스를 사용하는 쪽이 알아야 하는 것은 ''무엇을 할 수 있는가''와 ''어떻게 하는가'' 중 무엇인지 쓰시오.', '무엇을 할 수 있는가', '''어떻게''는 구현 클래스의 책임입니다.', 'SILVER', NULL, 1),
  (140106, @java_subject_id, 14, 1401, 'FILL_BLANK', '빈칸에 들어갈 키워드를 쓰시오.
```java
class ConsolePrinter ____ Printer { }
```', 'implements', '인터페이스 구현은 implements로 선언합니다.', 'SILVER', NULL, 1),
  (140107, @java_subject_id, 14, 1401, 'CODE_OUTPUT', '`new Printer()`처럼 인터페이스로 직접 객체를 만들면 어떻게 되는지 쓰시오.', '컴파일 오류가 발생한다', '인터페이스는 직접 생성할 수 없습니다.', 'SILVER', NULL, 1),
  (140108, @java_subject_id, 14, 1401, 'CODE_SHORT', '`void save(String data)` 하나를 선언한 Storage 인터페이스를 한 줄로 작성하시오.', '`interface Storage { void save(String data); }`', '기능 계약만 선언합니다.', 'SILVER', NULL, 1),
  (140109, @java_subject_id, 14, 1401, 'CODE_OUTPUT', '하나의 인터페이스를 여러 클래스가 구현할 수 있는지 쓰시오.', '구현할 수 있다', '상황에 따라 구현을 바꿔 끼울 수 있습니다.', 'SILVER', NULL, 1),
  (140110, @java_subject_id, 14, 1401, 'CODE_SHORT', 'Printer 타입 변수 printer에 ConsolePrinter 객체를 저장하는 문장 한 줄을 작성하시오.', '`Printer printer = new ConsolePrinter();`', '인터페이스 타입으로 구현 객체를 다룹니다.', 'SILVER', NULL, 1),
  (140201, @java_subject_id, 14, 1402, 'MULTIPLE_CHOICE', '한 클래스가 구현할 수 있는 인터페이스의 수는?', '쉼표로 나열해 여러 개 가능', '클래스 상속(하나)과 달리 인터페이스는 여러 개 구현할 수 있습니다.', 'SILVER', NULL, 1),
  (140202, @java_subject_id, 14, 1402, 'MULTIPLE_CHOICE', '다음 코드의 출력 결과는?
```java
interface RunnableTask { void run(); }
class DownloadTask implements RunnableTask {
    public void run() { System.out.println("다운로드"); }
}
new DownloadTask().run();
```', '다운로드', '구현된 run이 실행됩니다.', 'SILVER', NULL, 1),
  (140203, @java_subject_id, 14, 1402, 'MULTIPLE_CHOICE', '빈칸에 들어갈 키워드는?
```java
class DownloadTask ____ RunnableTask { }
```', 'implements', '클래스가 인터페이스를 구현할 때는 implements입니다.', 'SILVER', NULL, 1),
  (140204, @java_subject_id, 14, 1402, 'MULTIPLE_CHOICE', '인터페이스의 추상 메서드를 하나라도 구현하지 않으면?', '컴파일 오류가 발생한다', '구체 클래스는 모든 추상 메서드를 구현해야 합니다.', 'SILVER', NULL, 1),
  (140205, @java_subject_id, 14, 1402, 'SHORT_ANSWER', '구현 메서드에 public을 붙여야 하는 이유를 쓰시오.', '인터페이스의 메서드는 기본적으로 public이기 때문', '접근 범위를 좁힐 수 없습니다.', 'SILVER', NULL, 1),
  (140206, @java_subject_id, 14, 1402, 'FILL_BLANK', 'run 구현이 되도록 빈칸을 채우시오.
```java
public void ____() { System.out.println("업로드"); }
```', 'run', '인터페이스에 선언된 이름 그대로 구현합니다.', 'SILVER', NULL, 1),
  (140207, @java_subject_id, 14, 1402, 'CODE_OUTPUT', '구현 메서드에서 public을 빼면 어떻게 되는지 쓰시오.', '컴파일 오류가 발생한다', '접근 범위를 좁히는 것이 되어 허용되지 않습니다.', 'SILVER', NULL, 1),
  (140208, @java_subject_id, 14, 1402, 'CODE_SHORT', 'RunnableTask를 구현해 "업로드"를 출력하는 UploadTask 클래스를 한 줄로 작성하시오.', '`class UploadTask implements RunnableTask { public void run() { System.out.println("업로드"); } }`', '같은 계약의 다른 구현입니다.', 'SILVER', NULL, 1),
  (140209, @java_subject_id, 14, 1402, 'CODE_OUTPUT', '`implements A, B`로 두 인터페이스를 구현하는 클래스의 의무를 쓰시오.', '두 인터페이스의 추상 메서드를 모두 구현해야 한다', '여러 역할을 동시에 가질 수 있습니다.', 'SILVER', NULL, 1),
  (140210, @java_subject_id, 14, 1402, 'FILL_BLANK', '두 인터페이스를 구현하도록 빈칸에 들어갈 기호를 쓰시오.
```java
class Multi implements A____ B { }
```', ',', '인터페이스 목록은 쉼표로 구분합니다.', 'SILVER', NULL, 1),
  (140301, @java_subject_id, 14, 1403, 'MULTIPLE_CHOICE', '추상 메서드의 형태로 옳은 것은?', '실행 본문 없이 선언만 하고 세미콜론으로 끝낸다', '선언과 구현을 분리하는 문법입니다.', 'SILVER', NULL, 1),
  (140302, @java_subject_id, 14, 1403, 'MULTIPLE_CHOICE', '다음 코드의 출력 결과는?
```java
interface Calculator { int calculate(int a, int b); }
class Adder implements Calculator {
    public int calculate(int a, int b) { return a + b; }
}
System.out.println(new Adder().calculate(2, 3));
```', '5', 'Adder의 덧셈 구현이 실행됩니다.', 'SILVER', NULL, 1),
  (140303, @java_subject_id, 14, 1403, 'MULTIPLE_CHOICE', '추상 메서드 선언이 되도록 빈칸에 들어갈 기호는?
```java
interface Calculator { int calculate(int a, int b)____ }
```', ';', '본문 대신 세미콜론으로 끝냅니다.', 'SILVER', NULL, 1),
  (140304, @java_subject_id, 14, 1403, 'MULTIPLE_CHOICE', '같은 Calculator를 구현한 Subtractor(뺄셈)와 Adder(덧셈)는?', '같은 이름으로 서로 다른 계산을 한다', '같은 기능 이름을 유지하며 다른 동작을 작성할 수 있습니다.', 'SILVER', NULL, 1),
  (140305, @java_subject_id, 14, 1403, 'SHORT_ANSWER', '선언과 구현을 분리하면 얻는 장점을 쓰시오.', '기능 규칙과 실제 처리를 따로 검토할 수 있다', '구현 클래스를 계속 추가할 수 있습니다.', 'SILVER', NULL, 1),
  (140306, @java_subject_id, 14, 1403, 'FILL_BLANK', 'Calculator 구현 뺄셈 메서드의 빈칸을 채우시오.
```java
public int ____(int a, int b) { return a - b; }
```', 'calculate', '인터페이스에 선언된 이름을 유지합니다.', 'SILVER', NULL, 1),
  (140307, @java_subject_id, 14, 1403, 'CODE_OUTPUT', 'Subtractor(뺄셈 구현)의 `calculate(5, 2)` 결과를 쓰시오.', '3', '구현에 따라 5 - 2가 계산됩니다.', 'SILVER', NULL, 1),
  (140308, @java_subject_id, 14, 1403, 'CODE_SHORT', 'Calculator를 구현해 곱셈하는 Multiplier 클래스를 한 줄로 작성하시오.', '`class Multiplier implements Calculator { public int calculate(int a, int b) { return a * b; } }`', '같은 계약의 곱셈 구현입니다.', 'SILVER', NULL, 1),
  (140309, @java_subject_id, 14, 1403, 'CODE_OUTPUT', 'Multiplier의 `calculate(3, 4)` 결과를 쓰시오.', '12', '곱셈 구현이 실행됩니다.', 'SILVER', NULL, 1),
  (140310, @java_subject_id, 14, 1403, 'CODE_OUTPUT', '인터페이스의 일반 추상 메서드에 본문을 작성하면 어떻게 되는지 쓰시오.', '컴파일 오류가 발생한다(기본 구현은 default 메서드로 제공)', '추상 메서드는 선언만 가능합니다.', 'SILVER', NULL, 1),
  (140401, @java_subject_id, 14, 1404, 'MULTIPLE_CHOICE', '인터페이스 타입 변수에 저장할 수 있는 것은?', '해당 인터페이스를 구현한 객체', '구현 객체를 인터페이스 타입으로 다룹니다.', 'SILVER', NULL, 1),
  (140402, @java_subject_id, 14, 1404, 'MULTIPLE_CHOICE', '다음 코드의 출력 결과는?
```java
Printer printer = new ConsolePrinter(); // print는 전달값을 출력
printer.print("출력");
```', '출력', '실제 객체(ConsolePrinter)의 구현이 실행됩니다.', 'SILVER', NULL, 1),
  (140403, @java_subject_id, 14, 1404, 'MULTIPLE_CHOICE', '빈칸에 들어갈 타입은?
```java
____ printer = new ConsolePrinter();
```', 'Printer', '인터페이스 타입으로 선언합니다.', 'SILVER', NULL, 1),
  (140404, @java_subject_id, 14, 1404, 'MULTIPLE_CHOICE', '호출 시 실제로 실행되는 코드는?', '대입된 객체의 구현', '다형성의 동작 원리입니다.', 'SILVER', NULL, 1),
  (140405, @java_subject_id, 14, 1404, 'SHORT_ANSWER', '메서드 매개변수를 인터페이스 타입으로 선언하면 얻는 장점을 쓰시오.', '어떤 구현 객체든 전달할 수 있다', '테스트 시 검증용 구현을 넣을 수도 있습니다.', 'SILVER', NULL, 1),
  (140406, @java_subject_id, 14, 1404, 'CODE_OUTPUT', 'FilePrinter 구현으로 교체할 때 printer를 사용하는 코드는 어떻게 되는지 쓰시오.', '그대로 유지된다', '대입하는 객체만 바뀝니다.', 'SILVER', NULL, 1),
  (140407, @java_subject_id, 14, 1404, 'FILL_BLANK', '빈칸에 들어갈 매개변수 타입을 쓰시오.
```java
static void use(____ printer) { printer.print("출력"); }
```', 'Printer', '인터페이스 타입 매개변수입니다.', 'SILVER', NULL, 1),
  (140408, @java_subject_id, 14, 1404, 'CODE_SHORT', 'Printer를 받아 print를 호출하는 use 메서드를 한 줄로 선언하시오.', '`static void use(Printer printer) { printer.print("출력"); }`', '구현과 무관하게 동작하는 코드입니다.', 'SILVER', NULL, 1),
  (140409, @java_subject_id, 14, 1404, 'CODE_OUTPUT', '테스트할 때 실제 구현 대신 무엇을 전달할 수 있는지 쓰시오.', '검증용(테스트용) 구현 객체', '인터페이스 다형성의 실무 활용입니다.', 'SILVER', NULL, 1),
  (140410, @java_subject_id, 14, 1404, 'CODE_SHORT', 'use 메서드에 ConsolePrinter를 전달해 호출하는 문장 한 줄을 작성하시오.', '`use(new ConsolePrinter());`', '구현 객체를 인터페이스 매개변수로 전달합니다.', 'SILVER', NULL, 1),
  (140501, @java_subject_id, 14, 1405, 'MULTIPLE_CHOICE', 'default 메서드에 대한 설명으로 옳은 것은?', '인터페이스에 기본 구현을 제공한다', '구현 클래스가 오버라이딩하지 않으면 기본 구현이 사용됩니다.', 'SILVER', NULL, 1),
  (140502, @java_subject_id, 14, 1405, 'MULTIPLE_CHOICE', '다음 코드의 출력 결과는?
```java
interface Greeter { default void greet() { System.out.println("안녕하세요"); } }
class UserGreeter implements Greeter { }
new UserGreeter().greet();
```', '안녕하세요', '구현이 없으면 default 기본 구현이 실행됩니다.', 'SILVER', NULL, 1),
  (140503, @java_subject_id, 14, 1405, 'MULTIPLE_CHOICE', '빈칸에 들어갈 키워드는?
```java
____ void greet() { System.out.println("안녕하세요"); }
```', 'default', '기본 구현은 default 키워드로 선언합니다.', 'SILVER', NULL, 1),
  (140504, @java_subject_id, 14, 1405, 'MULTIPLE_CHOICE', '구현 클래스가 default 메서드를 오버라이딩할 수 있는가?', '가능하다', '필요하면 다시 재정의할 수 있습니다.', 'SILVER', NULL, 1),
  (140505, @java_subject_id, 14, 1405, 'SHORT_ANSWER', 'default 메서드가 특히 유용한 상황을 쓰시오.', '이미 배포된 인터페이스에 기존 구현 클래스를 깨지 않고 기능을 추가할 때', '새 추상 메서드 추가는 모든 구현을 깨뜨립니다.', 'SILVER', NULL, 1),
  (140506, @java_subject_id, 14, 1405, 'CODE_OUTPUT', 'greet를 오버라이딩한 클래스에서 호출하면 어느 구현이 실행되는지 쓰시오.', '오버라이딩한 자기 구현', '재정의가 기본 구현보다 우선합니다.', 'SILVER', NULL, 1),
  (140507, @java_subject_id, 14, 1405, 'FILL_BLANK', '빈칸에 들어갈 반환형을 쓰시오.
```java
interface Greeter { default ____ greet() { System.out.println("안녕"); } }
```', 'void', 'default + 반환형 + 이름 순서입니다.', 'SILVER', NULL, 1),
  (140508, @java_subject_id, 14, 1405, 'CODE_SHORT', '"안녕하세요"를 출력하는 default 메서드 greet를 한 줄로 선언하시오.', '`default void greet() { System.out.println("안녕하세요"); }`', '인터페이스 안의 기본 구현입니다.', 'SILVER', NULL, 1),
  (140509, @java_subject_id, 14, 1405, 'CODE_OUTPUT', 'default 메서드가 있어도 인터페이스로 직접 객체를 만들 수 있는지 쓰시오.', '만들 수 없다', '여전히 구현 클래스가 필요합니다.', 'SILVER', NULL, 1),
  (140510, @java_subject_id, 14, 1405, 'CODE_OUTPUT', '배포된 인터페이스에 새 ''추상 메서드''를 추가하면 기존 구현 클래스는 어떻게 되는지 쓰시오.', '컴파일 오류가 발생한다(깨진다)', '이런 상황의 보호 수단이 default 메서드입니다.', 'SILVER', NULL, 1),
  (140601, @java_subject_id, 14, 1406, 'MULTIPLE_CHOICE', '인터페이스 static 메서드의 소속은?', '인터페이스 자체', '구현 클래스에 상속되지 않습니다.', 'SILVER', NULL, 1),
  (140602, @java_subject_id, 14, 1406, 'MULTIPLE_CHOICE', '다음 코드의 출력 결과는?
```java
interface MathUtil { static int twice(int value) { return value * 2; } }
System.out.println(MathUtil.twice(5));
```', '10', '인터페이스 이름으로 호출해 5 * 2 = 10입니다.', 'SILVER', NULL, 1),
  (140603, @java_subject_id, 14, 1406, 'MULTIPLE_CHOICE', '빈칸에 들어갈 키워드는?
```java
____ int twice(int value) { return value * 2; }
```', 'static', '인터페이스 소속 메서드는 static으로 선언합니다.', 'SILVER', NULL, 1),
  (140604, @java_subject_id, 14, 1406, 'MULTIPLE_CHOICE', '구현 클래스 이름으로 인터페이스 static 메서드를 호출하면?', '컴파일 오류', '반드시 인터페이스 이름으로 호출해야 합니다.', 'SILVER', NULL, 1),
  (140605, @java_subject_id, 14, 1406, 'SHORT_ANSWER', '인터페이스 static 메서드가 적합한 용도를 쓰시오.', '구현 객체 없이 쓰는 보조 기능(검증·변환 유틸리티)', '관련 기능을 한곳에 모아 둘 수 있습니다.', 'SILVER', NULL, 1),
  (140606, @java_subject_id, 14, 1406, 'CODE_OUTPUT', '`MathUtil.twice(3)`의 결과를 쓰시오.', '6', '3 * 2입니다.', 'SILVER', NULL, 1),
  (140607, @java_subject_id, 14, 1406, 'FILL_BLANK', '빈칸에 들어갈 인터페이스 이름을 쓰시오.
```java
System.out.println(____.twice(5));
```', 'MathUtil', '인터페이스 이름으로 호출합니다.', 'SILVER', NULL, 1),
  (140608, @java_subject_id, 14, 1406, 'CODE_SHORT', '값의 절반을 반환하는 static 메서드 half를 한 줄로 선언하시오.', '`static int half(int value) { return value / 2; }`', '인터페이스 안의 유틸리티 메서드입니다.', 'SILVER', NULL, 1),
  (140609, @java_subject_id, 14, 1406, 'CODE_OUTPUT', '구현 객체의 참조로 인터페이스 static 메서드를 호출할 수 있는지 쓰시오.', '호출할 수 없다', '상속되지 않기 때문입니다.', 'SILVER', NULL, 1),
  (140610, @java_subject_id, 14, 1406, 'CODE_OUTPUT', '`MathUtil.half(10)`의 결과를 쓰시오.', '5', '10 / 2입니다.', 'SILVER', NULL, 1),
  (140701, @java_subject_id, 14, 1407, 'MULTIPLE_CHOICE', '인터페이스 상속에 대한 설명으로 옳은 것은?', 'extends로 하나 이상의 인터페이스를 상속할 수 있다', '클래스와 달리 인터페이스는 다중 상속이 가능합니다.', 'SILVER', NULL, 1),
  (140702, @java_subject_id, 14, 1407, 'MULTIPLE_CHOICE', 'Editor를 구현하는 클래스의 의무는?
```java
interface Reader { void read(); }
interface Writer { void write(); }
interface Editor extends Reader, Writer { }
```', 'read와 write 모두 구현', '상속받은 모든 추상 메서드를 구현해야 합니다.', 'SILVER', NULL, 1),
  (140703, @java_subject_id, 14, 1407, 'MULTIPLE_CHOICE', '빈칸에 들어갈 키워드는?
```java
interface Editor ____ Reader, Writer { }
```', 'extends', '인터페이스 간 상속은 extends입니다.', 'SILVER', NULL, 1),
  (140704, @java_subject_id, 14, 1407, 'MULTIPLE_CHOICE', 'Editor 구현 객체를 Reader 타입 변수에 저장할 수 있는가?', '가능', 'Editor는 Reader의 하위 타입입니다.', 'SILVER', NULL, 1),
  (140705, @java_subject_id, 14, 1407, 'SHORT_ANSWER', '작은 역할 인터페이스를 조합하는 설계의 장점을 쓰시오.', '필요한 역할 타입만 전달해 접근 범위를 제한할 수 있다', '읽기만 필요한 코드에는 Reader만 전달합니다.', 'SILVER', NULL, 1),
  (140706, @java_subject_id, 14, 1407, 'FILL_BLANK', '두 부모 인터페이스를 나열하도록 빈칸에 들어갈 기호를 쓰시오.
```java
interface Editor extends Reader____ Writer { }
```', ',', '쉼표로 여러 인터페이스를 상속합니다.', 'SILVER', NULL, 1),
  (140707, @java_subject_id, 14, 1407, 'CODE_OUTPUT', 'Editor 구현 클래스가 read만 구현하고 write를 빠뜨리면 어떻게 되는지 쓰시오.', '컴파일 오류가 발생한다', '상속받은 모든 추상 메서드가 대상입니다.', 'SILVER', NULL, 1),
  (140708, @java_subject_id, 14, 1407, 'CODE_SHORT', 'Reader와 Writer를 상속하는 Editor 인터페이스를 한 줄로 선언하시오.', '`interface Editor extends Reader, Writer { }`', '역할 조합 인터페이스입니다.', 'SILVER', NULL, 1),
  (140709, @java_subject_id, 14, 1407, 'CODE_OUTPUT', '읽기 기능만 필요한 메서드에 전달할 가장 적절한 타입을 쓰시오.', 'Reader', '필요한 최소 역할만 노출합니다.', 'SILVER', NULL, 1),
  (140710, @java_subject_id, 14, 1407, 'CODE_SHORT', 'Editor를 구현하며 read·write를 빈 본문으로 구현한 SimpleEditor를 한 줄로 작성하시오.', '`class SimpleEditor implements Editor { public void read() { } public void write() { } }`', '두 추상 메서드를 모두 구현했습니다.', 'SILVER', NULL, 1),
  (140801, @java_subject_id, 14, 1408, 'MULTIPLE_CHOICE', '추상 클래스에 대한 설명으로 옳은 것은?', '필드·생성자·일반 메서드와 추상 메서드를 함께 가질 수 있다', '공통 상태와 일부 구현을 공유할 때 적합합니다.', 'SILVER', NULL, 1),
  (140802, @java_subject_id, 14, 1408, 'MULTIPLE_CHOICE', '다음 코드의 출력 결과는?
```java
abstract class Shape { abstract double area(); }
class Square extends Shape {
    double side = 3;
    double area() { return side * side; }
}
System.out.println(new Square().area());
```', '9.0', '3 * 3 = 9.0입니다.', 'SILVER', NULL, 1),
  (140803, @java_subject_id, 14, 1408, 'MULTIPLE_CHOICE', '빈칸에 들어갈 키워드는?
```java
____ class Shape { abstract double area(); }
```', 'abstract', '추상 클래스는 abstract로 선언합니다.', 'SILVER', NULL, 1),
  (140804, @java_subject_id, 14, 1408, 'MULTIPLE_CHOICE', '`new Shape()`를 작성하면?', '컴파일 오류', '추상 클래스는 직접 생성할 수 없습니다.', 'SILVER', NULL, 1),
  (140805, @java_subject_id, 14, 1408, 'SHORT_ANSWER', '인터페이스와 추상 클래스의 주 목적 차이를 한 문장으로 쓰시오.', '인터페이스는 역할 계약, 추상 클래스는 공통 상태·부분 구현 공유가 주 목적이다', '상태 공유 필요 여부가 선택 기준입니다.', 'SILVER', NULL, 1),
  (140806, @java_subject_id, 14, 1408, 'CODE_OUTPUT', '추상 클래스는 몇 개까지 상속할 수 있는지 쓰시오.', '하나', '추상 클래스도 클래스이므로 단일 상속입니다.', 'SILVER', NULL, 1),
  (140807, @java_subject_id, 14, 1408, 'FILL_BLANK', '추상 메서드 선언의 빈칸에 들어갈 반환형을 쓰시오.
```java
abstract ____ area();
```', 'double', '넓이는 실수로 반환합니다.', 'SILVER', NULL, 1),
  (140808, @java_subject_id, 14, 1408, 'CODE_SHORT', '반지름 r 필드가 있다고 할 때 area를 원 넓이로 구현하는 메서드를 한 줄로 작성하시오.', '`double area() { return 3.14 * r * r; }`', '자식이 추상 메서드를 구현합니다.', 'SILVER', NULL, 1),
  (140809, @java_subject_id, 14, 1408, 'CODE_OUTPUT', '자식이 추상 메서드를 구현하지 않으면 그 자식은 어떻게 선언되어야 하는지 쓰시오.', '자신도 abstract로 선언되어야 한다', '구현 의무가 다음 자식으로 넘어갑니다.', 'SILVER', NULL, 1),
  (140810, @java_subject_id, 14, 1408, 'CODE_OUTPUT', '상태 공유가 필요 없다면 추상 클래스와 인터페이스 중 무엇이 더 유연한지 쓰시오.', '인터페이스', '여러 개를 구현할 수 있어 유연합니다.', 'SILVER', NULL, 1),
  (140901, @java_subject_id, 14, 1409, 'MULTIPLE_CHOICE', '서비스가 구체 클래스를 직접 생성할 때의 문제는?', '구현을 바꿀 때마다 서비스 코드를 수정해야 한다', '인터페이스 타입으로 전달받으면 교체 시 만들어 넣는 객체만 달라집니다.', 'SILVER', NULL, 1),
  (140902, @java_subject_id, 14, 1409, 'MULTIPLE_CHOICE', '다음 코드에서 notify(sender)가 실행하는 것은?
```java
interface Sender { void send(); }
static void notify(Sender sender) { sender.send(); }
```', '전달된 구현 객체의 send', '전달된 객체의 구현이 실행됩니다.', 'SILVER', NULL, 1),
  (140903, @java_subject_id, 14, 1409, 'MULTIPLE_CHOICE', '빈칸에 들어갈 매개변수 타입은?
```java
static void notify(____ sender) { sender.send(); }
```', 'Sender', '인터페이스 타입으로 받아야 구현 교체가 쉽습니다.', 'SILVER', NULL, 1),
  (140904, @java_subject_id, 14, 1409, 'MULTIPLE_CHOICE', '새 구현 SmsSender가 추가될 때 notify 메서드는?', '수정할 필요 없다', '전달하는 객체만 바꾸면 됩니다.', 'SILVER', NULL, 1),
  (140905, @java_subject_id, 14, 1409, 'SHORT_ANSWER', '이 구조가 실무 프레임워크에서 이어지는 개념의 이름을 쓰시오.', '의존성 주입', '생성자에서 전달하는 객체만 바꿔 동작을 교체합니다.', 'SILVER', NULL, 1),
  (140906, @java_subject_id, 14, 1409, 'FILL_BLANK', 'Sender 인터페이스의 빈칸에 들어갈 메서드 이름을 쓰시오.
```java
interface Sender { void ____(); }
```', 'send', '알림 발송 기능 계약입니다.', 'SILVER', NULL, 1),
  (140907, @java_subject_id, 14, 1409, 'CODE_OUTPUT', '문자 알림을 이메일 알림으로 바꿀 때 무엇만 달라지는지 쓰시오.', '전달(생성)하는 구현 객체', '사용하는 코드는 그대로입니다.', 'SILVER', NULL, 1),
  (140908, @java_subject_id, 14, 1409, 'CODE_SHORT', '"메일 발송"을 출력하는 MailSender 구현 클래스를 한 줄로 작성하시오.', '`class MailSender implements Sender { public void send() { System.out.println("메일 발송"); } }`', 'Sender 계약의 한 구현입니다.', 'SILVER', NULL, 1),
  (140909, @java_subject_id, 14, 1409, 'CODE_OUTPUT', '`notify(new MailSender())`의 출력 결과를 쓰시오.', '메일 발송', 'MailSender의 send가 실행됩니다.', 'SILVER', NULL, 1),
  (140910, @java_subject_id, 14, 1409, 'CODE_SHORT', 'notify에 MailSender를 전달해 호출하는 문장 한 줄을 작성하시오.', '`notify(new MailSender());`', '구현 객체를 주입하는 호출입니다.', 'SILVER', NULL, 1),
  (141001, @java_subject_id, 14, 1410, 'MULTIPLE_CHOICE', '추상화의 핵심 효과로 옳은 것은?', '사용하는 쪽에 필요한 기능만 공개하고 구현 세부사항을 숨긴다', '역할 계약(인터페이스)과 실제 처리(구현)를 나눕니다.', 'SILVER', NULL, 1),
  (141002, @java_subject_id, 14, 1410, 'MULTIPLE_CHOICE', '다음 코드의 출력 결과는?
```java
interface Payment { int pay(int amount); }
class CouponPayment implements Payment {
    public int pay(int amount) { return amount - 1000; }
}
Payment payment = new CouponPayment();
System.out.println(payment.pay(5000));
```', '4000', '쿠폰 구현이 1000을 할인해 4000입니다.', 'SILVER', NULL, 1),
  (141003, @java_subject_id, 14, 1410, 'MULTIPLE_CHOICE', '빈칸에 들어갈 메서드 이름은?
```java
interface Payment { int ____(int amount); }
```', 'pay', '결제 기능 계약입니다.', 'SILVER', NULL, 1),
  (141004, @java_subject_id, 14, 1410, 'MULTIPLE_CHOICE', '`Payment payment = new CouponPayment();` 대입이 가능한 이유는?', 'CouponPayment가 Payment를 구현했기 때문', '구현 객체는 인터페이스 타입 변수에 저장할 수 있습니다.', 'SILVER', NULL, 1),
  (141005, @java_subject_id, 14, 1410, 'SHORT_ANSWER', '인터페이스에 너무 많은 메서드를 선언하면 생기는 문제를 쓰시오.', '구현 클래스마다 불필요한 구현이 강제된다', '사용하는 쪽에 필요한 최소 기능만 남깁니다.', 'SILVER', NULL, 1),
  (141006, @java_subject_id, 14, 1410, 'CODE_OUTPUT', 'CouponPayment의 `pay(3000)` 결과를 쓰시오.', '2000', '3000 - 1000입니다.', 'SILVER', NULL, 1),
  (141007, @java_subject_id, 14, 1410, 'FILL_BLANK', '빈칸에 들어갈 구현 클래스 이름을 쓰시오.
```java
Payment payment = new ____(); // 쿠폰 할인 결제
```', 'CouponPayment', '결제 수단 선택은 객체 생성에서 결정됩니다.', 'SILVER', NULL, 1),
  (141008, @java_subject_id, 14, 1410, 'CODE_SHORT', '금액을 그대로 반환하는 CashPayment 구현 클래스를 한 줄로 작성하시오.', '`class CashPayment implements Payment { public int pay(int amount) { return amount; } }`', '결제 수단이 늘어나도 사용하는 코드는 그대로입니다.', 'SILVER', NULL, 1),
  (141009, @java_subject_id, 14, 1410, 'CODE_OUTPUT', 'CashPayment의 `pay(5000)` 결과를 쓰시오.', '5000', '할인 없는 구현입니다.', 'SILVER', NULL, 1),
  (141010, @java_subject_id, 14, 1410, 'CODE_SHORT', 'payment의 pay(5000) 결과를 출력하는 문장 한 줄을 작성하시오.', '`System.out.println(payment.pay(5000));`', '인터페이스를 통한 호출입니다.', 'SILVER', NULL, 1),
  (150101, @java_subject_id, 15, 1501, 'MULTIPLE_CHOICE', '오류(Error)와 예외(Exception)의 차이로 옳은 것은?', '오류는 JVM 수준의 복구 어려운 문제, 예외는 코드에서 대비·처리할 수 있는 문제다', '애플리케이션 코드에서 try-catch로 다루는 대상은 주로 예외입니다.', 'SILVER', NULL, 1),
  (150102, @java_subject_id, 15, 1501, 'MULTIPLE_CHOICE', '다음 문장을 실행하면?
```java
int value = Integer.parseInt("Java");
```', '실행 중 예외(NumberFormatException) 발생', '숫자가 아닌 문자열 변환은 실행 중 예외를 일으킵니다.', 'SILVER', NULL, 1),
  (150103, @java_subject_id, 15, 1501, 'MULTIPLE_CHOICE', '문자열을 정수로 변환하는 빈칸의 메서드는?
```java
int value = Integer.____("123");
```', 'parseInt', 'Integer.parseInt가 문자열을 int로 변환합니다.', 'SILVER', NULL, 1),
  (150104, @java_subject_id, 15, 1501, 'MULTIPLE_CHOICE', '`Integer.parseInt("123")`의 결과는?', '123', '올바른 숫자 형식은 정상 변환됩니다.', 'SILVER', NULL, 1),
  (150105, @java_subject_id, 15, 1501, 'SHORT_ANSWER', '예외의 대표적인 예를 두 가지 쓰시오.', '잘못된 숫자 변환, 배열 범위 초과(null 접근 등)', '예외 종류와 발생 조건을 알면 복구할 수 있습니다.', 'SILVER', NULL, 1),
  (150106, @java_subject_id, 15, 1501, 'CODE_OUTPUT', 'null이 저장된 String 변수에 length()를 호출하면 발생하는 예외 이름을 쓰시오.', 'NullPointerException', 'null 접근의 대표 예외입니다.', 'SILVER', NULL, 1),
  (150107, @java_subject_id, 15, 1501, 'FILL_BLANK', '배열 범위를 벗어난 접근에서 발생하는 예외 이름을 쓰시오.', 'ArrayIndexOutOfBoundsException', '유효 인덱스 검사로 예방합니다.', 'SILVER', NULL, 1),
  (150108, @java_subject_id, 15, 1501, 'CODE_OUTPUT', '예외가 발생했는데 아무 처리도 없으면 프로그램은 어떻게 되는지 쓰시오.', '스택 추적을 출력하며 비정상 종료된다', '처리하지 않은 예외는 프로그램을 중단시킵니다.', 'SILVER', NULL, 1),
  (150109, @java_subject_id, 15, 1501, 'CODE_SHORT', '문자열 "80"을 int로 변환해 score에 저장하는 문장 한 줄을 작성하시오.', '`int score = Integer.parseInt("80");`', '형식이 잘못되면 예외가 발생할 수 있는 지점입니다.', 'SILVER', NULL, 1),
  (150110, @java_subject_id, 15, 1501, 'CODE_OUTPUT', '애플리케이션 코드의 try-catch가 주로 다루는 대상이 Error와 Exception 중 무엇인지 쓰시오.', 'Exception(예외)', 'Error는 코드로 복구하기 어렵습니다.', 'SILVER', NULL, 1),
  (150201, @java_subject_id, 15, 1502, 'MULTIPLE_CHOICE', 'try와 catch의 역할로 옳은 것은?', 'try에 예외 가능 코드, catch에 처리(복구) 코드', '예외가 발생하면 일치하는 catch로 이동합니다.', 'SILVER', NULL, 1),
  (150202, @java_subject_id, 15, 1502, 'MULTIPLE_CHOICE', '다음 코드의 출력 결과는?
```java
try {
    System.out.println(10 / 0);
} catch (ArithmeticException e) {
    System.out.println("0으로 나눌 수 없습니다");
}
```', '0으로 나눌 수 없습니다', '예외가 catch에서 처리되어 안내가 출력됩니다.', 'SILVER', NULL, 1),
  (150203, @java_subject_id, 15, 1502, 'MULTIPLE_CHOICE', '빈칸에 들어갈 키워드는?
```java
try { } ____ (ArithmeticException e) { }
```', 'catch', '처리할 예외 타입은 catch에 작성합니다.', 'SILVER', NULL, 1),
  (150204, @java_subject_id, 15, 1502, 'MULTIPLE_CHOICE', 'try 안에서 예외가 발생하면 try의 남은 문장은?', '건너뛴다', '발생 지점 이후는 건너뛰고 catch로 이동합니다.', 'SILVER', NULL, 1),
  (150205, @java_subject_id, 15, 1502, 'SHORT_ANSWER', 'catch 블록 여러 개를 배치할 때의 순서 규칙을 쓰시오.', '구체적인 예외를 위에, 넓은 범위 예외를 아래에', '순서가 바뀌면 아래 catch가 의미를 잃습니다.', 'SILVER', NULL, 1),
  (150206, @java_subject_id, 15, 1502, 'CODE_OUTPUT', '예외가 발생하지 않으면 catch 블록은 어떻게 되는지 쓰시오.', '실행되지 않는다', 'try 다음 코드로 진행합니다.', 'SILVER', NULL, 1),
  (150207, @java_subject_id, 15, 1502, 'FILL_BLANK', '예외 가능 코드를 감싸는 빈칸의 키워드를 쓰시오.
```java
____ { System.out.println(10 / 0); } catch (ArithmeticException e) { }
```', 'try', '위험 코드는 try 블록에 둡니다.', 'SILVER', NULL, 1),
  (150208, @java_subject_id, 15, 1502, 'CODE_SHORT', '`Integer.parseInt(text)`를 try-catch로 감싸 실패 시 "숫자가 아닙니다"를 출력하는 코드를 한 줄로 작성하시오.', '`try { int n = Integer.parseInt(text); } catch (NumberFormatException e) { System.out.println("숫자가 아닙니다"); }`', '입력 변환의 전형적인 보호 패턴입니다.', 'SILVER', NULL, 1),
  (150209, @java_subject_id, 15, 1502, 'CODE_OUTPUT', 'catch에서 예외를 처리한 뒤 프로그램은 어떻게 되는지 쓰시오.', '중단되지 않고 계속 진행된다', '복구가 try-catch의 목적입니다.', 'SILVER', NULL, 1),
  (150210, @java_subject_id, 15, 1502, 'CODE_OUTPUT', 'try 블록의 범위는 넓게 잡는 것과 좁게 잡는 것 중 무엇이 권장되는지 쓰시오.', '실제 예외가 날 수 있는 문장으로 좁게', '원인 파악이 쉬워집니다.', 'SILVER', NULL, 1),
  (150301, @java_subject_id, 15, 1503, 'MULTIPLE_CHOICE', 'finally 블록의 특징은?', '예외 발생 여부와 관계없이 실행된다', '자원 정리 코드에 사용합니다.', 'SILVER', NULL, 1),
  (150302, @java_subject_id, 15, 1503, 'MULTIPLE_CHOICE', '다음 코드의 출력 순서는?
```java
try { System.out.println("작업"); }
finally { System.out.println("정리"); }
```', '작업 → 정리', 'try 실행 후 finally가 실행됩니다.', 'SILVER', NULL, 1),
  (150303, @java_subject_id, 15, 1503, 'MULTIPLE_CHOICE', '빈칸에 들어갈 키워드는?
```java
try { } ____ { System.out.println("정리"); }
```', 'finally', '정리 블록은 finally입니다.', 'SILVER', NULL, 1),
  (150304, @java_subject_id, 15, 1503, 'MULTIPLE_CHOICE', 'try 안에서 return을 해도 finally는?', '실행된다', '그래서 반드시 수행할 정리 작업을 맡기기에 적합합니다.', 'SILVER', NULL, 1),
  (150305, @java_subject_id, 15, 1503, 'SHORT_ANSWER', '새로 작성하는 코드에서 finally의 직접 close보다 우선 사용해야 할 문법을 쓰시오.', 'try-with-resources', 'Java 7부터 자원 닫기를 자동으로 처리합니다.', 'SILVER', NULL, 1),
  (150306, @java_subject_id, 15, 1503, 'CODE_OUTPUT', '예외 발생 + catch 처리 + finally가 모두 있을 때 실행 순서를 쓰시오.', 'try(예외 발생 지점까지) → catch → finally', 'finally는 마지막에 반드시 실행됩니다.', 'SILVER', NULL, 1),
  (150307, @java_subject_id, 15, 1503, 'FILL_BLANK', 'try-catch-finally 구조의 마지막 빈칸을 채우시오.
```java
try { } catch (Exception e) { } ____ { }
```', 'finally', '정리 블록은 catch 뒤에 둡니다.', 'SILVER', NULL, 1),
  (150308, @java_subject_id, 15, 1503, 'CODE_SHORT', '"종료"를 출력하는 finally 블록을 한 줄로 작성하시오.', '`finally { System.out.println("종료"); }`', '항상 실행되는 마무리 코드입니다.', 'SILVER', NULL, 1),
  (150309, @java_subject_id, 15, 1503, 'CODE_OUTPUT', 'finally 블록의 대표적인 용도를 쓰시오.', '파일·네트워크 연결 같은 자원 정리', '예외 여부와 무관하게 정리가 보장됩니다.', 'SILVER', NULL, 1),
  (150310, @java_subject_id, 15, 1503, 'CODE_OUTPUT', 'catch 없이 try-finally만 사용하는 것이 가능한지 쓰시오.', '가능하다', '처리 대신 정리만 보장하는 구조도 유효합니다.', 'SILVER', NULL, 1),
  (150401, @java_subject_id, 15, 1504, 'MULTIPLE_CHOICE', 'throws의 의미로 옳은 것은?', '처리하지 않은 예외를 호출한 곳으로 전달한다고 선언한다', '처리 책임을 호출자에게 넘기는 선언입니다.', 'SILVER', NULL, 1),
  (150402, @java_subject_id, 15, 1504, 'MULTIPLE_CHOICE', 'throws IOException이 선언된 메서드를 호출하는 쪽의 의무는?', 'try-catch로 처리하거나 다시 throws로 넘긴다', 'checked 예외는 처리 여부를 컴파일러가 강제합니다.', 'SILVER', NULL, 1),
  (150403, @java_subject_id, 15, 1504, 'MULTIPLE_CHOICE', '빈칸에 들어갈 키워드는?
```java
static String load() ____ IOException {
    return Files.readString(Path.of("data.txt"));
}
```', 'throws', '메서드 선언부의 예외 전달 선언은 throws입니다.', 'SILVER', NULL, 1),
  (150404, @java_subject_id, 15, 1504, 'MULTIPLE_CHOICE', 'main까지 throws로 계속 넘긴 checked 예외가 실제로 발생하면?', '스택 추적을 출력하며 프로그램이 종료된다', '끝까지 처리되지 않은 예외는 프로그램을 중단시킵니다.', 'SILVER', NULL, 1),
  (150405, @java_subject_id, 15, 1504, 'SHORT_ANSWER', 'throws는 예외를 ''처리''하는 것인지 아닌지, 한 문장으로 쓰시오.', '처리하는 것이 아니라 처리 책임을 호출자에게 넘기는 선언이다', '어느 계층에서 처리할지 설계 판단이 필요합니다.', 'SILVER', NULL, 1),
  (150406, @java_subject_id, 15, 1504, 'CODE_OUTPUT', 'checked 예외를 던지는 메서드를 처리 없이 호출하면 어떻게 되는지 쓰시오.', '컴파일 오류가 발생한다', '컴파일러가 처리를 강제합니다.', 'SILVER', NULL, 1),
  (150407, @java_subject_id, 15, 1504, 'FILL_BLANK', '빈칸에 들어갈 예외 타입을 쓰시오.
```java
void read() throws ____ { Files.readString(Path.of("a.txt")); }
```', 'IOException', '파일 읽기는 IOException을 던질 수 있습니다.', 'SILVER', NULL, 1),
  (150408, @java_subject_id, 15, 1504, 'CODE_SHORT', 'IOException을 던진다고 선언한 load 메서드의 선언부를 한 줄로 작성하시오.', '`static String load() throws IOException`', '선언부에 throws를 명시합니다.', 'SILVER', NULL, 1),
  (150409, @java_subject_id, 15, 1504, 'CODE_OUTPUT', 'load를 호출하는 메서드가 직접 처리하지 않고 다시 넘기는 방법을 쓰시오.', '자신의 선언부에도 throws를 선언한다', '책임을 상위로 계속 전달할 수 있습니다.', 'SILVER', NULL, 1),
  (150410, @java_subject_id, 15, 1504, 'CODE_OUTPUT', 'unchecked 예외도 반드시 throws로 선언해야 하는지 쓰시오.', '반드시 선언할 필요는 없다', '컴파일러 강제는 checked 예외에만 적용됩니다.', 'SILVER', NULL, 1),
  (150501, @java_subject_id, 15, 1505, 'MULTIPLE_CHOICE', 'checked 예외의 특징은?', '컴파일러가 처리 여부를 확인한다', 'try-catch나 throws가 강제됩니다.', 'SILVER', NULL, 1),
  (150502, @java_subject_id, 15, 1505, 'MULTIPLE_CHOICE', '다음 코드를 실행하면?
```java
String text = null;
System.out.println(text.length());
```', '실행 중 NullPointerException', 'null 접근은 실행 중 발생하는 unchecked 예외입니다.', 'SILVER', NULL, 1),
  (150503, @java_subject_id, 15, 1505, 'MULTIPLE_CHOICE', 'unchecked 예외는 ____Exception 계열이다. 빈칸에 들어갈 것은?', 'Runtime', 'RuntimeException 계열이 unchecked입니다.', 'SILVER', NULL, 1),
  (150504, @java_subject_id, 15, 1505, 'MULTIPLE_CHOICE', 'IOException은 어느 쪽인가?', 'checked', '파일·네트워크 관련 예외는 대부분 checked입니다.', 'SILVER', NULL, 1),
  (150505, @java_subject_id, 15, 1505, 'SHORT_ANSWER', 'checked 예외가 적합한 상황을 쓰시오.', '코드가 정상이어도 외부 환경(파일·네트워크) 때문에 실패할 수 있는 작업', '호출자에게 대비를 강제합니다.', 'SILVER', NULL, 1),
  (150506, @java_subject_id, 15, 1505, 'CODE_OUTPUT', 'NullPointerException은 checked와 unchecked 중 무엇인지 쓰시오.', 'unchecked', 'RuntimeException 계열입니다.', 'SILVER', NULL, 1),
  (150507, @java_subject_id, 15, 1505, 'CODE_OUTPUT', 'unchecked 예외에 대한 우선적인 대응 방법을 쓰시오.', '모든 곳에서 잡기보다 원인이 되는 로직을 수정한다', '대부분 코드 결함이 원인입니다.', 'SILVER', NULL, 1),
  (150508, @java_subject_id, 15, 1505, 'CODE_OUTPUT', 'ArithmeticException(0으로 나누기)은 어느 쪽인지 쓰시오.', 'unchecked', '실행 중 잘못된 값·로직에서 발생합니다.', 'SILVER', NULL, 1),
  (150509, @java_subject_id, 15, 1505, 'CODE_SHORT', 'null 검사를 통해 NullPointerException을 예방하며 길이를 출력하는 if문을 한 줄로 작성하시오.', '`if (text != null) { System.out.println(text.length()); }`', '원인 차단이 우선입니다.', 'SILVER', NULL, 1),
  (150510, @java_subject_id, 15, 1505, 'CODE_OUTPUT', '컴파일 단계에서 try-catch나 throws가 강제되는 쪽은 어느 예외인지 쓰시오.', 'checked 예외', 'unchecked는 강제되지 않습니다.', 'SILVER', NULL, 1),
  (150601, @java_subject_id, 15, 1506, 'MULTIPLE_CHOICE', '사용자 정의 예외를 만드는 목적은?', '업무 규칙 위반을 명확하게 표현하기 위해', '이름에 어떤 문제인지 드러내고 메시지에 원인을 담습니다.', 'SILVER', NULL, 1),
  (150602, @java_subject_id, 15, 1506, 'MULTIPLE_CHOICE', '`throw new InsufficientBalanceException("잔액 부족");`이 실행되면?', '해당 예외가 발생한다', 'throw는 예외를 실제로 발생시키는 문장입니다.', 'SILVER', NULL, 1),
  (150603, @java_subject_id, 15, 1506, 'MULTIPLE_CHOICE', '빈칸에 들어갈 키워드는?
```java
class InsufficientBalanceException ____ RuntimeException { }
```', 'extends', '예외 클래스를 상속해 정의합니다.', 'SILVER', NULL, 1),
  (150604, @java_subject_id, 15, 1506, 'MULTIPLE_CHOICE', '예외 객체의 getMessage()가 반환하는 것은?', '생성 시 전달한 메시지', 'super(message)로 전달한 메시지를 확인할 수 있습니다.', 'SILVER', NULL, 1),
  (150605, @java_subject_id, 15, 1506, 'SHORT_ANSWER', '사용자 정의 예외의 이름을 짓는 기준을 쓰시오.', '이름만 봐도 어떤 문제가 발생했는지 드러나게', 'InsufficientBalanceException처럼 짓습니다.', 'SILVER', NULL, 1),
  (150606, @java_subject_id, 15, 1506, 'CODE_OUTPUT', '생성자에서 super(message)를 호출하는 이유를 쓰시오.', '부모 예외에 메시지를 전달해 getMessage()로 확인할 수 있게 하기 위해', '원인 전달의 표준 통로입니다.', 'SILVER', NULL, 1),
  (150607, @java_subject_id, 15, 1506, 'FILL_BLANK', '빈칸에 들어갈 키워드를 쓰시오.
```java
InsufficientBalanceException(String message) { ____(message); }
```', 'super', '부모 생성자로 메시지를 넘깁니다.', 'SILVER', NULL, 1),
  (150608, @java_subject_id, 15, 1506, 'CODE_SHORT', 'RuntimeException을 상속하고 메시지를 받는 InvalidScoreException을 한 줄로 정의하시오.', '`class InvalidScoreException extends RuntimeException { InvalidScoreException(String message) { super(message); } }`', '사용자 정의 예외의 기본 형태입니다.', 'SILVER', NULL, 1),
  (150609, @java_subject_id, 15, 1506, 'CODE_OUTPUT', '표준 예외로 상황이 충분히 설명될 때는 어떻게 해야 하는지 쓰시오.', '표준 예외를 그대로 사용한다', '도메인 고유 문제만 직접 정의합니다.', 'SILVER', NULL, 1),
  (150610, @java_subject_id, 15, 1506, 'CODE_SHORT', 'amount가 balance보다 크면 잔액 부족 예외를 던지는 if문을 한 줄로 작성하시오.', '`if (amount > balance) throw new InsufficientBalanceException("잔액 부족");`', '업무 규칙 위반을 예외로 표현합니다.', 'SILVER', NULL, 1),
  (150701, @java_subject_id, 15, 1507, 'MULTIPLE_CHOICE', '상대 경로의 기준은?', '프로그램 실행 위치', '절대 경로는 루트부터 전체 위치를 나타냅니다.', 'SILVER', NULL, 1),
  (150702, @java_subject_id, 15, 1507, 'MULTIPLE_CHOICE', 'macOS·Linux에서 다음 코드의 출력 결과는?
```java
Path path = Path.of("data", "scores.txt");
System.out.println(path);
```', 'data/scores.txt', '경로 구분자는 운영체제에 따라 다르며 macOS·Linux는 /입니다.', 'SILVER', NULL, 1),
  (150703, @java_subject_id, 15, 1507, 'MULTIPLE_CHOICE', '빈칸에 들어갈 메서드는?
```java
Path path = Path.____("data", "scores.txt");
```', 'of', 'Path.of로 경로 객체를 만듭니다.', 'SILVER', NULL, 1),
  (150704, @java_subject_id, 15, 1507, 'MULTIPLE_CHOICE', '같은 코드를 Windows에서 실행하면 구분자는?', '\\', 'Windows는 역슬래시로 표시됩니다.', 'SILVER', NULL, 1),
  (150705, @java_subject_id, 15, 1507, 'SHORT_ANSWER', '절대 경로가 나타내는 범위를 쓰시오.', '루트부터 전체 위치', '상대 경로는 실행 위치 기준입니다.', 'SILVER', NULL, 1),
  (150706, @java_subject_id, 15, 1507, 'CODE_OUTPUT', '`Path.of("data", "scores.txt")`처럼 이름을 나누어 전달하는 장점을 쓰시오.', '같은 코드가 모든 운영체제에서 동작한다', '구분자를 직접 쓰지 않아도 됩니다.', 'SILVER', NULL, 1),
  (150707, @java_subject_id, 15, 1507, 'FILL_BLANK', '빈칸에 들어갈 타입을 쓰시오.
```java
____ path = Path.of("memo.txt");
```', 'Path', '경로는 Path 타입으로 표현합니다.', 'SILVER', NULL, 1),
  (150708, @java_subject_id, 15, 1507, 'CODE_SHORT', 'data 폴더 안 memo.txt를 가리키는 Path를 만드는 문장 한 줄을 작성하시오.', '`Path path = Path.of("data", "memo.txt");`', '폴더와 파일 이름을 나누어 전달합니다.', 'SILVER', NULL, 1),
  (150709, @java_subject_id, 15, 1507, 'CODE_OUTPUT', 'Path 객체가 파일의 ''내용''을 담고 있는지 쓰시오.', '담고 있지 않다(위치만 표현)', '내용은 Files의 읽기 메서드로 가져옵니다.', 'SILVER', NULL, 1),
  (150710, @java_subject_id, 15, 1507, 'CODE_OUTPUT', '`System.out.println(path)`가 출력하는 것을 쓰시오.', '경로 문자열', 'OS에 맞는 구분자로 표시됩니다.', 'SILVER', NULL, 1),
  (150801, @java_subject_id, 15, 1508, 'MULTIPLE_CHOICE', 'Files.readString이 적합한 경우는?', '작은 텍스트 파일 전체 읽기', '큰 파일이나 줄 단위 처리는 BufferedReader를 사용합니다.', 'SILVER', NULL, 1),
  (150802, @java_subject_id, 15, 1508, 'MULTIPLE_CHOICE', '존재하지 않는 파일에 readString을 호출하면?', 'IOException 발생', '파일이 없거나 권한이 없으면 IOException이 발생합니다.', 'SILVER', NULL, 1),
  (150803, @java_subject_id, 15, 1508, 'MULTIPLE_CHOICE', '빈칸에 들어갈 메서드는?
```java
String content = Files.____(Path.of("memo.txt"));
```', 'readString', '파일 전체를 문자열로 읽습니다.', 'SILVER', NULL, 1),
  (150804, @java_subject_id, 15, 1508, 'MULTIPLE_CHOICE', '줄 단위 처리가 필요할 때 알맞은 도구는?', 'BufferedReader나 readAllLines', '줄 단위·대용량 처리는 별도 도구를 사용합니다.', 'SILVER', NULL, 1),
  (150805, @java_subject_id, 15, 1508, 'SHORT_ANSWER', '읽기 전에 파일 존재 여부를 확인하는 메서드를 쓰시오.', 'Files.exists', '예외 처리와 함께 안전장치로 쓸 수 있습니다.', 'SILVER', NULL, 1),
  (150806, @java_subject_id, 15, 1508, 'CODE_OUTPUT', 'readString의 반환 자료형을 쓰시오.', 'String', '파일 전체 내용이 문자열로 반환됩니다.', 'SILVER', NULL, 1),
  (150807, @java_subject_id, 15, 1508, 'FILL_BLANK', '파일 기능이 담긴 클래스 이름을 빈칸에 쓰시오.
```java
String content = ____.readString(Path.of("memo.txt"));
```', 'Files', 'java.nio.file.Files의 정적 메서드입니다.', 'SILVER', NULL, 1),
  (150808, @java_subject_id, 15, 1508, 'CODE_SHORT', 'memo.txt 전체를 읽어 content에 저장하는 문장 한 줄을 작성하시오.', '`String content = Files.readString(Path.of("memo.txt"));`', '작은 텍스트 파일 읽기의 기본형입니다.', 'SILVER', NULL, 1),
  (150809, @java_subject_id, 15, 1508, 'CODE_OUTPUT', '매우 큰 파일을 readString으로 읽으면 어떤 부담이 생기는지 쓰시오.', '전체가 한 번에 메모리로 올라와 부담이 크다', '대용량은 줄 단위 처리를 고려합니다.', 'SILVER', NULL, 1),
  (150810, @java_subject_id, 15, 1508, 'CODE_OUTPUT', '읽은 content의 글자 수를 확인하는 방법을 쓰시오.', 'content.length()', '문자열 메서드로 확인합니다.', 'SILVER', NULL, 1),
  (150901, @java_subject_id, 15, 1509, 'MULTIPLE_CHOICE', 'Files.writeString의 기본 동작은?', '기존 내용을 덮어쓴다', '이어 쓰기가 필요하면 옵션을 지정해야 합니다.', 'SILVER', NULL, 1),
  (150902, @java_subject_id, 15, 1509, 'MULTIPLE_CHOICE', '같은 파일에 writeString을 두 번 실행하면 파일 내용은?', '마지막 내용만 남는다', '기본 동작이 덮어쓰기이기 때문입니다.', 'SILVER', NULL, 1),
  (150903, @java_subject_id, 15, 1509, 'MULTIPLE_CHOICE', '빈칸에 들어갈 메서드는?
```java
Files.____(Path.of("memo.txt"), "Java 학습");
```', 'writeString', '문자열을 파일에 기록합니다.', 'SILVER', NULL, 1),
  (150904, @java_subject_id, 15, 1509, 'MULTIPLE_CHOICE', '기존 내용 뒤에 이어 쓰려면 지정해야 하는 옵션은?', 'StandardOpenOption.APPEND', 'APPEND 옵션으로 이어 쓰기를 지정합니다.', 'SILVER', NULL, 1),
  (150905, @java_subject_id, 15, 1509, 'SHORT_ANSWER', 'writeString에서 인코딩을 지정하지 않으면 기본으로 사용되는 인코딩을 쓰시오.', 'UTF-8', '다른 프로그램과 주고받을 때는 양쪽 인코딩을 확인합니다.', 'SILVER', NULL, 1),
  (150906, @java_subject_id, 15, 1509, 'CODE_OUTPUT', '존재하지 않는 파일에 writeString을 호출하면 어떻게 되는지 쓰시오.', '파일이 새로 만들어진다', '기본 동작으로 생성 후 기록합니다.', 'SILVER', NULL, 1),
  (150907, @java_subject_id, 15, 1509, 'FILL_BLANK', '"안녕"을 기록하도록 빈칸을 채우시오.
```java
Files.writeString(path, ____);
```', '"안녕"', '기록할 문자열을 전달합니다.', 'SILVER', NULL, 1),
  (150908, @java_subject_id, 15, 1509, 'CODE_SHORT', 'memo.txt에 "Java 학습"을 기록하는 문장 한 줄을 작성하시오.', '`Files.writeString(Path.of("memo.txt"), "Java 학습");`', '파일 쓰기의 기본형입니다.', 'SILVER', NULL, 1),
  (150909, @java_subject_id, 15, 1509, 'CODE_OUTPUT', '한글 깨짐을 막으려면 파일을 주고받는 양쪽이 무엇을 맞춰야 하는지 쓰시오.', '같은 문자 인코딩', '저장·복원 양쪽에서 같은 규칙을 사용합니다.', 'SILVER', NULL, 1),
  (150910, @java_subject_id, 15, 1509, 'CODE_OUTPUT', 'writeString이 던질 수 있는 대표 예외를 쓰시오.', 'IOException', '쓰기도 외부 환경 실패가 가능한 작업입니다.', 'SILVER', NULL, 1),
  (151001, @java_subject_id, 15, 1510, 'MULTIPLE_CHOICE', '파일 처리의 올바른 구성 순서는?', '경로 준비 → 읽기·쓰기 → 예외 처리', '경로 준비, 작업, 예외 처리 순서로 구성합니다.', 'SILVER', NULL, 1),
  (151002, @java_subject_id, 15, 1510, 'MULTIPLE_CHOICE', 'memo.txt가 없을 때 다음 코드의 출력 결과는?
```java
try {
    String text = Files.readString(Path.of("memo.txt"));
    System.out.println(text.length());
} catch (IOException e) {
    System.out.println("파일을 확인하세요");
}
```', '파일을 확인하세요', 'IOException이 catch에서 처리됩니다.', 'SILVER', NULL, 1),
  (151003, @java_subject_id, 15, 1510, 'MULTIPLE_CHOICE', '빈칸에 들어갈 예외 타입은?
```java
} catch (____ e) { System.out.println("파일을 확인하세요"); }
```', 'IOException', '파일 작업의 실패는 IOException으로 처리합니다.', 'SILVER', NULL, 1),
  (151004, @java_subject_id, 15, 1510, 'MULTIPLE_CHOICE', 'try-with-resources를 사용하면 얻는 이점은?', '작업이 끝날 때 자원이 자동으로 닫힌다', '자원 정리를 문법이 보장합니다.', 'SILVER', NULL, 1),
  (151005, @java_subject_id, 15, 1510, 'SHORT_ANSWER', '예외를 잡은 뒤 아무것도 하지 않고 지나가면 어떤 문제가 생기는지 쓰시오.', '문제를 숨기게 된다', '최소한 원인 출력이나 기본값 복구 등 의미 있는 처리를 남깁니다.', 'SILVER', NULL, 1),
  (151006, @java_subject_id, 15, 1510, 'CODE_OUTPUT', 'memo.txt가 존재하면 2번 문제의 코드는 무엇을 출력하는지 쓰시오.', '파일 내용의 글자 수', '정상 경로에서는 length()가 출력됩니다.', 'SILVER', NULL, 1),
  (151007, @java_subject_id, 15, 1510, 'CODE_OUTPUT', 'catch 블록에서 예외의 원인 메시지를 확인하는 메서드를 쓰시오.', 'e.getMessage()', '원인을 출력해 문제를 숨기지 않습니다.', 'SILVER', NULL, 1),
  (151008, @java_subject_id, 15, 1510, 'CODE_SHORT', 'memo.txt를 안전하게 읽어 출력하고 실패 시 "파일을 확인하세요"를 출력하는 try-catch를 한 줄로 작성하시오.', '`try { System.out.println(Files.readString(Path.of("memo.txt"))); } catch (IOException e) { System.out.println("파일을 확인하세요"); }`', '읽기와 복구를 묶은 형태입니다.', 'SILVER', NULL, 1),
  (151009, @java_subject_id, 15, 1510, 'CODE_OUTPUT', '예외 처리에서 ''의미 있는 처리''의 예를 한 가지 쓰시오.', '원인 출력 또는 기본값으로 복구', '사용자 안내도 좋은 처리입니다.', 'SILVER', NULL, 1),
  (151010, @java_subject_id, 15, 1510, 'CODE_OUTPUT', '경로 문제와 권한 문제를 구분해 안내하면 좋은 이유를 쓰시오.', '원인에 따라 해결 방법이 다르기 때문', '실패 지점을 구분하면 대응이 빨라집니다.', 'SILVER', NULL, 1),
  (210101, @java_subject_id, 21, 2101, 'MULTIPLE_CHOICE', '배열과 비교한 컬렉션의 특징으로 옳은 것은?', '크기를 유연하게 바꿀 수 있고 목적별 구조(List·Set·Map)를 선택할 수 있다', '컬렉션 프레임워크는 여러 객체를 저장·처리하는 표준 자료구조입니다.', 'GOLD', NULL, 1),
  (210102, @java_subject_id, 21, 2101, 'MULTIPLE_CHOICE', '다음 코드의 출력 결과는?
```java
List<String> names = new ArrayList<>();
names.add("민수");
names.add("지민");
System.out.println(names.size());
```', '2', '요소 두 개가 추가되어 size는 2입니다.', 'GOLD', NULL, 1),
  (210103, @java_subject_id, 21, 2101, 'MULTIPLE_CHOICE', '빈칸에 들어갈 구현체는?
```java
List<String> names = new ____<>();
```', 'ArrayList', '선언은 인터페이스(List), 생성은 구현체(ArrayList)로 합니다.', 'GOLD', NULL, 1),
  (210104, @java_subject_id, 21, 2101, 'MULTIPLE_CHOICE', '`List<String>`에 정수를 add하면?', '컴파일 오류', '제네릭이 잘못된 타입 저장을 컴파일 단계에서 막습니다.', 'GOLD', NULL, 1),
  (210105, @java_subject_id, 21, 2101, 'CODE_OUTPUT', 'size()가 반환하는 것을 쓰시오.', '저장된 요소의 개수', '배열의 length에 해당하는 역할입니다.', 'GOLD', NULL, 1),
  (210106, @java_subject_id, 21, 2101, 'FILL_BLANK', '인터페이스 타입으로 선언하도록 빈칸을 채우시오.
```java
____<String> names = new ArrayList<>();
```', 'List', '구현체 교체가 쉬운 선언 방식입니다.', 'GOLD', NULL, 1),
  (210107, @java_subject_id, 21, 2101, 'CODE_SHORT', 'String을 담는 ArrayList를 names 변수에 생성하는 문장 한 줄을 작성하시오.', '`List<String> names = new ArrayList<>();`', '관례적인 선언·생성 형태입니다.', 'GOLD', NULL, 1),
  (210108, @java_subject_id, 21, 2101, 'CODE_OUTPUT', '선언은 List, 생성은 ArrayList로 하는 관례의 장점을 쓰시오.', '나중에 구현체를 바꿔도 사용하는 코드가 흔들리지 않는다', '인터페이스에 의존하는 설계입니다.', 'GOLD', NULL, 1),
  (210109, @java_subject_id, 21, 2101, 'FILL_BLANK', '"민수"를 추가하도록 빈칸을 채우시오.
```java
names.____("민수");
```', 'add', '요소 추가는 add 메서드입니다.', 'GOLD', NULL, 1),
  (210110, @java_subject_id, 21, 2101, 'CODE_OUTPUT', '비어 있는 리스트의 size() 결과를 쓰시오.', '0', '요소가 없으면 0입니다.', 'GOLD', NULL, 1),
  (210201, @java_subject_id, 21, 2102, 'MULTIPLE_CHOICE', 'List의 특징으로 옳은 것은?', '입력 순서를 유지하고 중복을 허용한다', '순서가 중요한 목록에 적합합니다.', 'GOLD', NULL, 1),
  (210202, @java_subject_id, 21, 2102, 'MULTIPLE_CHOICE', '다음 코드의 출력 결과는?
```java
List<String> list = new ArrayList<>(List.of("A", "B", "A"));
System.out.println(list.get(1));
```', 'B', '인덱스 1은 두 번째 요소 B입니다.', 'GOLD', NULL, 1),
  (210203, @java_subject_id, 21, 2102, 'MULTIPLE_CHOICE', '인덱스로 요소를 조회하는 빈칸의 메서드는?
```java
list.____(1);
```', 'get', 'List는 get(인덱스)로 조회합니다.', 'GOLD', NULL, 1),
  (210204, @java_subject_id, 21, 2102, 'MULTIPLE_CHOICE', 'List.of("A", "B", "A")처럼 중복 값을 전달하면?', '중복이 허용되어 그대로 만들어진다', 'List는 중복을 허용합니다.', 'GOLD', NULL, 1),
  (210205, @java_subject_id, 21, 2102, 'CODE_OUTPUT', '`List.of("A")`로 만든 목록에 add를 호출하면 어떻게 되는지 쓰시오.', '실행 중 오류가 발생한다(불변 목록)', 'List.of는 수정할 수 없는 목록을 만듭니다.', 'GOLD', NULL, 1),
  (210206, @java_subject_id, 21, 2102, 'FILL_BLANK', '빈칸에 들어갈 메서드를 쓰시오.
```java
List<String> list = new ArrayList<>(List.____("A", "B"));
```', 'of', 'List.of로 초기 목록을 만들어 감쌉니다.', 'GOLD', NULL, 1),
  (210207, @java_subject_id, 21, 2102, 'CODE_SHORT', '불변 목록 ["A", "B"]를 수정 가능한 ArrayList로 감싸는 문장 한 줄을 작성하시오.', '`List<String> list = new ArrayList<>(List.of("A", "B"));`', '변경이 필요하면 ArrayList로 감쌉니다.', 'GOLD', NULL, 1),
  (210208, @java_subject_id, 21, 2102, 'CODE_OUTPUT', '크기 3인 리스트에 get(5)를 호출하면 발생하는 예외 이름을 쓰시오.', 'IndexOutOfBoundsException', '배열처럼 범위 검사가 필요합니다.', 'GOLD', NULL, 1),
  (210209, @java_subject_id, 21, 2102, 'CODE_OUTPUT', '"A"를 두 번 add하면 리스트에 어떻게 저장되는지 쓰시오.', '둘 다 저장된다', 'List는 중복을 허용합니다.', 'GOLD', NULL, 1),
  (210210, @java_subject_id, 21, 2102, 'CODE_SHORT', 'list의 첫 번째 요소를 출력하는 문장 한 줄을 작성하시오.', '`System.out.println(list.get(0));`', '인덱스 0이 첫 요소입니다.', 'GOLD', NULL, 1),
  (210301, @java_subject_id, 21, 2103, 'MULTIPLE_CHOICE', 'ArrayList의 특성으로 옳은 것은?', '인덱스 조회가 빠르지만 중간 삽입·삭제는 뒤 요소 이동이 필요할 수 있다', '내부 배열 기반 구현체의 특성입니다.', 'GOLD', NULL, 1),
  (210302, @java_subject_id, 21, 2103, 'MULTIPLE_CHOICE', '다음 코드의 출력 결과는?
```java
List<Integer> numbers = new ArrayList<>();
numbers.add(10);
numbers.add(20);
numbers.set(0, 15);
System.out.println(numbers);
```', '[15, 20]', 'set은 해당 위치의 값을 교체합니다.', 'GOLD', NULL, 1),
  (210303, @java_subject_id, 21, 2103, 'MULTIPLE_CHOICE', '기존 위치의 값을 교체하는 빈칸의 메서드는?
```java
numbers.____(0, 15);
```', 'set', 'set(인덱스, 값)이 교체입니다.', 'GOLD', NULL, 1),
  (210304, @java_subject_id, 21, 2103, 'MULTIPLE_CHOICE', '`System.out.println(numbers)`처럼 리스트를 바로 출력하면?', '[요소, 요소] 형태로 출력된다', '리스트는 대괄호 형태로 내용이 출력됩니다.', 'GOLD', NULL, 1),
  (210305, @java_subject_id, 21, 2103, 'CODE_OUTPUT', '2번 문제 코드에서 numbers.size()의 결과를 쓰시오.', '2', 'set은 개수를 바꾸지 않습니다.', 'GOLD', NULL, 1),
  (210306, @java_subject_id, 21, 2103, 'FILL_BLANK', '요소를 끝에 추가하도록 빈칸을 채우시오.
```java
numbers.____(10);
```', 'add', 'add는 목록 끝에 추가합니다.', 'GOLD', NULL, 1),
  (210307, @java_subject_id, 21, 2103, 'CODE_SHORT', 'Integer를 담는 ArrayList를 numbers에 생성하는 문장 한 줄을 작성하시오.', '`List<Integer> numbers = new ArrayList<>();`', '기본형 int 대신 Integer를 사용합니다.', 'GOLD', NULL, 1),
  (210308, @java_subject_id, 21, 2103, 'CODE_OUTPUT', '[15, 20]에서 remove(0)을 호출한 뒤 리스트를 쓰시오.', '[20]', '인덱스 0 요소가 삭제되고 뒤가 당겨집니다.', 'GOLD', NULL, 1),
  (210309, @java_subject_id, 21, 2103, 'CODE_OUTPUT', '리스트의 크기를 확인하는 메서드 이름(배열의 length와 비교)을 쓰시오.', 'size()', '배열은 length, 리스트는 size()입니다.', 'GOLD', NULL, 1),
  (210310, @java_subject_id, 21, 2103, 'CODE_SHORT', 'numbers의 0번 요소를 99로 교체하는 문장 한 줄을 작성하시오.', '`numbers.set(0, 99);`', '인덱스 기반 교체입니다.', 'GOLD', NULL, 1),
  (210401, @java_subject_id, 21, 2104, 'MULTIPLE_CHOICE', 'Set의 특징으로 옳은 것은?', '중복 요소를 허용하지 않으며 일반적으로 인덱스가 없다', '포함 여부 확인과 고유값 관리에 적합합니다.', 'GOLD', NULL, 1),
  (210402, @java_subject_id, 21, 2104, 'MULTIPLE_CHOICE', '다음 코드의 출력 결과는?
```java
Set<String> tags = new HashSet<>();
tags.add("java");
tags.add("java");
System.out.println(tags.size());
```', '1', '중복 add는 저장되지 않아 크기는 1입니다.', 'GOLD', NULL, 1),
  (210403, @java_subject_id, 21, 2104, 'MULTIPLE_CHOICE', '빈칸에 들어갈 구현체는?
```java
Set<String> tags = new ____<>();
```', 'HashSet', 'Set의 대표 구현체는 HashSet입니다.', 'GOLD', NULL, 1),
  (210404, @java_subject_id, 21, 2104, 'MULTIPLE_CHOICE', '이미 있는 요소를 add하면 반환값은?', 'false', '오류 없이 false가 반환되고 저장만 되지 않습니다.', 'GOLD', NULL, 1),
  (210405, @java_subject_id, 21, 2104, 'CODE_OUTPUT', '위 tags에서 `contains("java")`의 결과를 쓰시오.', 'true', '포함 여부 확인이 Set의 주 용도입니다.', 'GOLD', NULL, 1),
  (210406, @java_subject_id, 21, 2104, 'FILL_BLANK', '포함 여부를 확인하도록 빈칸을 채우시오.
```java
tags.____("java");
```', 'contains', 'contains는 boolean을 반환합니다.', 'GOLD', NULL, 1),
  (210407, @java_subject_id, 21, 2104, 'CODE_SHORT', 'String을 담는 HashSet을 tags에 생성하는 문장 한 줄을 작성하시오.', '`Set<String> tags = new HashSet<>();`', '선언은 Set, 생성은 HashSet입니다.', 'GOLD', NULL, 1),
  (210408, @java_subject_id, 21, 2104, 'CODE_OUTPUT', '중복 요소를 add했을 때 오류가 발생하는지 쓰시오.', '발생하지 않는다(저장만 되지 않음)', '중복 여부는 add의 반환값으로 확인합니다.', 'GOLD', NULL, 1),
  (210409, @java_subject_id, 21, 2104, 'CODE_OUTPUT', 'Set이 적합한 데이터의 예를 한 가지 쓰시오.', '사용된 쿠폰 코드(방문 사용자 집합 등 고유값)', '''있는지 없는지''가 중요한 데이터입니다.', 'GOLD', NULL, 1),
  (210410, @java_subject_id, 21, 2104, 'CODE_SHORT', 'tags에 "java"를 추가하는 문장 한 줄을 작성하시오.', '`tags.add("java");`', 'Set도 add로 요소를 추가합니다.', 'GOLD', NULL, 1),
  (210501, @java_subject_id, 21, 2105, 'MULTIPLE_CHOICE', 'HashSet의 특징으로 옳은 것은?', '해시값으로 저장·검색하며 저장 순서를 보장하지 않는다', '순서가 필요하면 LinkedHashSet·TreeSet을 고려합니다.', 'GOLD', NULL, 1),
  (210502, @java_subject_id, 21, 2105, 'MULTIPLE_CHOICE', '다음 코드의 출력 결과는?
```java
Set<Integer> values = new HashSet<>(List.of(3, 1, 3, 2));
System.out.println(values.contains(2));
```', 'true', '2가 포함되어 있으므로 true입니다.', 'GOLD', NULL, 1),
  (210503, @java_subject_id, 21, 2105, 'MULTIPLE_CHOICE', '포함 여부 확인 메서드의 빈칸을 채우면?
```java
values.____(2)
```', 'contains', 'contains로 포함 여부를 확인합니다.', 'GOLD', NULL, 1),
  (210504, @java_subject_id, 21, 2105, 'MULTIPLE_CHOICE', 'List.of(3, 1, 3, 2)로 만든 HashSet의 size는?', '3', '중복된 3이 하나로 합쳐져 {3, 1, 2}입니다.', 'GOLD', NULL, 1),
  (210505, @java_subject_id, 21, 2105, 'CODE_OUTPUT', '직접 만든 클래스를 HashSet에 저장할 때 중복 판단을 위해 재정의해야 하는 두 메서드를 쓰시오.', 'equals와 hashCode', '둘 다 재정의해야 의도대로 중복이 제거됩니다.', 'GOLD', NULL, 1),
  (210506, @java_subject_id, 21, 2105, 'FILL_BLANK', '입력 순서를 유지하는 Set 구현체 이름을 쓰시오.', 'LinkedHashSet', '순서 요구사항에 따라 구현체를 선택합니다.', 'GOLD', NULL, 1),
  (210507, @java_subject_id, 21, 2105, 'CODE_SHORT', 'List.of(3, 1, 2)로 HashSet을 만들어 values에 저장하는 문장 한 줄을 작성하시오.', '`Set<Integer> values = new HashSet<>(List.of(3, 1, 2));`', '컬렉션을 전달해 초기화할 수 있습니다.', 'GOLD', NULL, 1),
  (210508, @java_subject_id, 21, 2105, 'CODE_OUTPUT', '정렬된 순서가 필요할 때 사용하는 Set 구현체를 쓰시오.', 'TreeSet', '정렬 유지가 필요하면 TreeSet입니다.', 'GOLD', NULL, 1),
  (210509, @java_subject_id, 21, 2105, 'CODE_OUTPUT', 'HashSet의 저장 순서와 출력 순서 관계를 쓰시오.', '다를 수 있다(순서 비보장)', '해시 기반 구조의 특성입니다.', 'GOLD', NULL, 1),
  (210510, @java_subject_id, 21, 2105, 'CODE_SHORT', 'values에 5가 포함되어 있는지 출력하는 문장 한 줄을 작성하시오.', '`System.out.println(values.contains(5));`', 'contains 결과(boolean)를 출력합니다.', 'GOLD', NULL, 1),
  (210601, @java_subject_id, 21, 2106, 'MULTIPLE_CHOICE', 'Map의 특징으로 옳은 것은?', '고유한 키와 값의 쌍을 저장하며 같은 키로 다시 저장하면 값이 교체된다', '키를 통해 값을 빠르게 찾는 구조입니다.', 'GOLD', NULL, 1),
  (210602, @java_subject_id, 21, 2106, 'MULTIPLE_CHOICE', '다음 코드의 출력 결과는?
```java
Map<String, Integer> scores = new HashMap<>();
scores.put("민수", 80);
scores.put("민수", 90);
System.out.println(scores.get("민수"));
```', '90', '같은 키로 put하면 기존 값이 교체됩니다.', 'GOLD', NULL, 1),
  (210603, @java_subject_id, 21, 2106, 'MULTIPLE_CHOICE', '키·값을 저장하는 빈칸의 메서드는?
```java
scores.____("민수", 80);
```', 'put', 'Map은 put으로 저장합니다.', 'GOLD', NULL, 1),
  (210604, @java_subject_id, 21, 2106, 'MULTIPLE_CHOICE', '존재하지 않는 키로 get을 호출하면?', 'null', '없는 키는 null을 반환합니다.', 'GOLD', NULL, 1),
  (210605, @java_subject_id, 21, 2106, 'CODE_OUTPUT', '위 scores에서 `containsKey("민수")`의 결과를 쓰시오.', 'true', '키 존재 여부는 containsKey로 확인합니다.', 'GOLD', NULL, 1),
  (210606, @java_subject_id, 21, 2106, 'FILL_BLANK', '값을 조회하도록 빈칸을 채우시오.
```java
scores.____("민수");
```', 'get', '키로 값을 조회합니다.', 'GOLD', NULL, 1),
  (210607, @java_subject_id, 21, 2106, 'CODE_SHORT', 'String 키와 Integer 값을 담는 HashMap을 scores에 생성하는 문장 한 줄을 작성하시오.', '`Map<String, Integer> scores = new HashMap<>();`', '선언은 Map, 생성은 HashMap입니다.', 'GOLD', NULL, 1),
  (210608, @java_subject_id, 21, 2106, 'CODE_OUTPUT', '같은 키로 put하는 성질을 활용할 수 있는 기능의 예를 쓰시오.', '점수 갱신(최신 값으로 교체)', '이전 값이 필요하면 put의 반환값을 받아 둡니다.', 'GOLD', NULL, 1),
  (210609, @java_subject_id, 21, 2106, 'CODE_OUTPUT', '키 전체를 순회할 때 사용하는 메서드를 쓰시오.', 'keySet()', 'values()·entrySet()으로 값·쌍도 순회할 수 있습니다.', 'GOLD', NULL, 1),
  (210610, @java_subject_id, 21, 2106, 'CODE_SHORT', 'scores에 "지민"의 점수 95를 저장하는 문장 한 줄을 작성하시오.', '`scores.put("지민", 95);`', 'put(키, 값) 형식입니다.', 'GOLD', NULL, 1),
  (210701, @java_subject_id, 21, 2107, 'MULTIPLE_CHOICE', 'HashMap의 특징으로 옳은 것은?', '키 순서를 보장하지 않으며 없는 키는 null을 반환한다', 'getOrDefault로 기본값을 지정할 수 있습니다.', 'GOLD', NULL, 1),
  (210702, @java_subject_id, 21, 2107, 'MULTIPLE_CHOICE', '다음 코드의 출력 결과는?
```java
Map<String, Integer> counts = new HashMap<>();
counts.put("java", 2);
System.out.println(counts.getOrDefault("sql", 0));
```', '0', '"sql" 키가 없으므로 기본값 0이 반환됩니다.', 'GOLD', NULL, 1),
  (210703, @java_subject_id, 21, 2107, 'MULTIPLE_CHOICE', '기본값 조회 메서드의 빈칸을 채우면?
```java
counts.____("sql", 0)
```', 'getOrDefault', '키가 없을 때 두 번째 인자가 반환됩니다.', 'GOLD', NULL, 1),
  (210704, @java_subject_id, 21, 2107, 'MULTIPLE_CHOICE', '없는 키의 get 결과(null)를 그대로 계산에 사용하면?', 'NullPointerException 발생', 'null을 산술에 쓰면 실행 중 예외가 발생합니다.', 'GOLD', NULL, 1),
  (210705, @java_subject_id, 21, 2107, 'CODE_OUTPUT', '위 counts에서 `getOrDefault("java", 0)`의 결과를 쓰시오.', '2', '키가 있으면 저장된 값이 반환됩니다.', 'GOLD', NULL, 1),
  (210706, @java_subject_id, 21, 2107, 'FILL_BLANK', '개수 세기 코드의 빈칸에 들어갈 기호를 쓰시오.
```java
counts.put(key, counts.getOrDefault(key, 0) ____ 1);
```', '+', '현재 개수에 1을 더해 다시 저장합니다.', 'GOLD', NULL, 1),
  (210707, @java_subject_id, 21, 2107, 'CODE_SHORT', '단어 word의 등장 횟수를 1 올리는 문장 한 줄을 작성하시오.', '`counts.put(word, counts.getOrDefault(word, 0) + 1);`', 'getOrDefault를 활용한 대표 패턴입니다.', 'GOLD', NULL, 1),
  (210708, @java_subject_id, 21, 2107, 'CODE_OUTPUT', '키 존재가 불확실한 조회에서 NullPointerException을 막는 방법 두 가지 중 하나를 쓰시오.', 'getOrDefault 사용(또는 containsKey로 먼저 확인)', 'null 반환 가능성을 차단합니다.', 'GOLD', NULL, 1),
  (210709, @java_subject_id, 21, 2107, 'CODE_OUTPUT', '키 순서 유지가 필요할 때의 대응을 쓰시오.', 'LinkedHashMap 같은 다른 구현체를 사용한다', 'HashMap은 순서를 보장하지 않습니다.', 'GOLD', NULL, 1),
  (210710, @java_subject_id, 21, 2107, 'CODE_SHORT', 'counts에서 "sql"을 기본값 0으로 조회해 출력하는 문장 한 줄을 작성하시오.', '`System.out.println(counts.getOrDefault("sql", 0));`', '없는 키도 안전하게 출력합니다.', 'GOLD', NULL, 1),
  (210801, @java_subject_id, 21, 2108, 'MULTIPLE_CHOICE', '반복 중 요소를 안전하게 삭제하는 방법은?', 'iterator의 remove 호출', '반복 중 컬렉션의 remove는 예외를 일으킬 수 있습니다.', 'GOLD', NULL, 1),
  (210802, @java_subject_id, 21, 2108, 'MULTIPLE_CHOICE', 'for-each 반복 중에 컬렉션의 remove를 호출하면 발생할 수 있는 예외는?', 'ConcurrentModificationException', '반복 중 구조 변경이 감지되면 발생합니다.', 'GOLD', NULL, 1),
  (210803, @java_subject_id, 21, 2108, 'MULTIPLE_CHOICE', '빈칸에 들어갈 메서드는?
```java
while (it.____()) { if (it.next() < 0) it.remove(); }
```', 'hasNext', 'hasNext로 다음 요소 존재를 확인합니다.', 'GOLD', NULL, 1),
  (210804, @java_subject_id, 21, 2108, 'MULTIPLE_CHOICE', '요소가 없는데 next()를 호출하면?', '예외 발생', '반드시 hasNext 확인 후 next를 호출합니다.', 'GOLD', NULL, 1),
  (210805, @java_subject_id, 21, 2108, 'CODE_OUTPUT', 'it.remove()가 삭제하는 대상을 쓰시오.', '마지막으로 next()가 반환한 요소', 'next 호출 후에만 remove가 유효합니다.', 'GOLD', NULL, 1),
  (210806, @java_subject_id, 21, 2108, 'FILL_BLANK', 'iterator를 얻도록 빈칸을 채우시오.
```java
Iterator<Integer> it = numbers.____();
```', 'iterator', '컬렉션의 iterator() 메서드로 얻습니다.', 'GOLD', NULL, 1),
  (210807, @java_subject_id, 21, 2108, 'CODE_SHORT', 'it로 순회하며 음수를 삭제하는 while문을 한 줄로 작성하시오.', '`while (it.hasNext()) { if (it.next() < 0) { it.remove(); } }`', '반복 중 안전 삭제의 표준 패턴입니다.', 'GOLD', NULL, 1),
  (210808, @java_subject_id, 21, 2108, 'CODE_OUTPUT', 'hasNext()의 반환 자료형을 쓰시오.', 'boolean', '다음 요소 존재 여부입니다.', 'GOLD', NULL, 1),
  (210809, @java_subject_id, 21, 2108, 'CODE_OUTPUT', 'next()의 역할을 쓰시오.', '다음 요소를 반환하고 위치를 이동한다', '확인(hasNext)과 이동(next)의 두 단계 흐름입니다.', 'GOLD', NULL, 1),
  (210810, @java_subject_id, 21, 2108, 'CODE_SHORT', 'numbers 리스트의 iterator를 it 변수에 얻는 문장 한 줄을 작성하시오.', '`Iterator<Integer> it = numbers.iterator();`', '표준 순회 도구를 얻는 문장입니다.', 'GOLD', NULL, 1),
  (210901, @java_subject_id, 21, 2109, 'MULTIPLE_CHOICE', 'Comparable과 Comparator의 차이로 옳은 것은?', 'Comparable은 객체의 기본 정렬 기준, Comparator는 상황별 정렬 기준', '기본 기준은 compareTo, 상황별 기준은 Comparator로 만듭니다.', 'GOLD', NULL, 1),
  (210902, @java_subject_id, 21, 2109, 'MULTIPLE_CHOICE', '다음 코드의 출력 결과는?
```java
List<Integer> values = new ArrayList<>(List.of(3, 1, 2));
values.sort(Comparator.reverseOrder());
System.out.println(values);
```', '[3, 2, 1]', 'reverseOrder는 내림차순 기준입니다.', 'GOLD', NULL, 1),
  (210903, @java_subject_id, 21, 2109, 'MULTIPLE_CHOICE', '내림차순 기준의 빈칸을 채우면?
```java
values.sort(Comparator.____());
```', 'reverseOrder', 'Comparator.reverseOrder()가 내림차순입니다.', 'GOLD', NULL, 1),
  (210904, @java_subject_id, 21, 2109, 'MULTIPLE_CHOICE', '`values.sort(Comparator.naturalOrder())`의 결과는? (values = [3, 1, 2])', '[1, 2, 3]', 'naturalOrder는 오름차순 기준입니다.', 'GOLD', NULL, 1),
  (210905, @java_subject_id, 21, 2109, 'CODE_OUTPUT', '직접 만든 클래스에 기본 정렬 기준을 부여하려면 어떤 인터페이스의 어떤 메서드를 구현해야 하는지 쓰시오.', 'Comparable의 compareTo', '기본 기준이 생기면 인자 없는 정렬이 가능합니다.', 'GOLD', NULL, 1),
  (210906, @java_subject_id, 21, 2109, 'FILL_BLANK', '1차 기준에 2차 기준을 연결하는 메서드 이름을 쓰시오.
```java
Comparator.comparing(...).____(...)
```', 'thenComparing', '여러 필드 기준 정렬을 조합합니다.', 'GOLD', NULL, 1),
  (210907, @java_subject_id, 21, 2109, 'CODE_SHORT', 'values를 내림차순으로 정렬하는 문장 한 줄을 작성하시오.', '`values.sort(Comparator.reverseOrder());`', '상황별 기준을 전달하는 정렬입니다.', 'GOLD', NULL, 1),
  (210908, @java_subject_id, 21, 2109, 'CODE_OUTPUT', '기본 정렬 기준(Comparable)이 없는 사용자 객체 리스트를 기준 없이 정렬하면 어떻게 되는지 쓰시오.', '오류가 발생한다(Comparator를 전달해야 한다)', '기준이 없으면 정렬할 수 없습니다.', 'GOLD', NULL, 1),
  (210909, @java_subject_id, 21, 2109, 'CODE_OUTPUT', 'reversed()의 역할을 쓰시오.', '기존 Comparator의 순서를 반대로 뒤집는다', '오름차순 기준을 내림차순으로 전환합니다.', 'GOLD', NULL, 1),
  (210910, @java_subject_id, 21, 2109, 'CODE_SHORT', 'values를 오름차순으로 정렬하는 문장 한 줄을 작성하시오.', '`values.sort(Comparator.naturalOrder());`', '자연 순서(오름차순) 정렬입니다.', 'GOLD', NULL, 1),
  (211001, @java_subject_id, 21, 2110, 'MULTIPLE_CHOICE', '자료구조 선택 기준으로 옳은 것은?', '순서·중복이면 List, 고유값이면 Set, 키 기반 조회면 Map', '요구사항에 맞는 구조 선택이 기본입니다.', 'GOLD', NULL, 1),
  (211002, @java_subject_id, 21, 2110, 'MULTIPLE_CHOICE', '다음 코드의 출력 결과는?
```java
Map<String, List<Integer>> scores = new HashMap<>();
scores.put("민수", List.of(80, 90));
System.out.println(scores.get("민수").get(1));
```', '90', '민수의 점수 목록에서 인덱스 1은 90입니다.', 'GOLD', NULL, 1),
  (211003, @java_subject_id, 21, 2110, 'MULTIPLE_CHOICE', '한 키에 여러 값을 저장하도록 빈칸을 채우면?
```java
Map<String, ____<Integer>> scores = new HashMap<>();
```', 'List', '값 자리에 List를 넣으면 키 하나에 여러 값을 담습니다.', 'GOLD', NULL, 1),
  (211004, @java_subject_id, 21, 2110, 'MULTIPLE_CHOICE', '학생별 점수 ''목록''을 저장해야 할 때 알맞은 구조는?', 'Map<String, List<Integer>>', '키(학생) → 값(점수 목록) 구조입니다.', 'GOLD', NULL, 1),
  (211005, @java_subject_id, 21, 2110, 'CODE_OUTPUT', '2번 코드에서 `scores.get("민수")`가 반환하는 것을 쓰시오.', '점수 목록 [80, 90]', '값으로 저장된 List가 반환됩니다.', 'GOLD', NULL, 1),
  (211006, @java_subject_id, 21, 2110, 'FILL_BLANK', '점수 목록을 만들도록 빈칸을 채우시오.
```java
scores.put("민수", List.____(80, 90));
```', 'of', 'List.of로 목록을 만들어 값으로 넣습니다.', 'GOLD', NULL, 1),
  (211007, @java_subject_id, 21, 2110, 'CODE_SHORT', '학생 이름 → 점수 목록 구조의 Map을 scores에 생성하는 문장 한 줄을 작성하시오.', '`Map<String, List<Integer>> scores = new HashMap<>();`', '게시글별 댓글 목록 같은 관계 표현에도 쓰입니다.', 'GOLD', NULL, 1),
  (211008, @java_subject_id, 21, 2110, 'CODE_OUTPUT', '검색(조회) 위주의 요구사항에서 우선 검토할 자료구조를 쓰시오.', 'Map', '키 기반 조회가 빠릅니다.', 'GOLD', NULL, 1),
  (211009, @java_subject_id, 21, 2110, 'CODE_OUTPUT', '저장되지 않은 학생 이름으로 get을 호출한 결과를 쓰시오.', 'null', '없는 키 조회는 null입니다.', 'GOLD', NULL, 1),
  (211010, @java_subject_id, 21, 2110, 'CODE_SHORT', 'scores에 "민수"의 점수 목록 [80, 90]을 저장하는 문장 한 줄을 작성하시오.', '`scores.put("민수", List.of(80, 90));`', '키 하나에 목록 하나를 저장합니다.', 'GOLD', NULL, 1),
  (220101, @java_subject_id, 22, 2201, 'MULTIPLE_CHOICE', '제네릭의 핵심 가치는?', '잘못된 타입 사용을 컴파일 시점에 발견', 'Object 기반 코드의 강제 형변환과 런타임 오류를 줄입니다.', 'GOLD', NULL, 1),
  (220102, @java_subject_id, 22, 2201, 'MULTIPLE_CHOICE', '`List<String> names`에 `names.add(1)`을 작성하면?', '컴파일 오류', '지정한 타입이 아니면 즉시 컴파일 오류입니다.', 'GOLD', NULL, 1),
  (220103, @java_subject_id, 22, 2201, 'MULTIPLE_CHOICE', '문자열만 저장하도록 빈칸을 채우면?
```java
List<____> names = new ArrayList<>();
```', 'String', '저장할 타입을 제네릭으로 지정합니다.', 'GOLD', NULL, 1),
  (220104, @java_subject_id, 22, 2201, 'MULTIPLE_CHOICE', '제네릭 리스트에서 `String name = names.get(0);`을 쓸 때 형변환은?', '필요 없다', '타입이 보장되므로 강제 형변환이 줄어듭니다.', 'GOLD', NULL, 1),
  (220105, @java_subject_id, 22, 2201, 'CODE_OUTPUT', '제네릭이 없던 Object 기반 컬렉션의 문제 두 가지 중 하나를 쓰시오.', '꺼낼 때마다 형변환이 필요하다(잘못된 타입이 실행 중에야 발견된다)', '제네릭은 이 확인을 컴파일로 앞당깁니다.', 'GOLD', NULL, 1),
  (220106, @java_subject_id, 22, 2201, 'FILL_BLANK', '다이아몬드 문법의 빈칸을 채우시오.
```java
List<String> names = new ArrayList____();
```', '<>', '생성부 타입은 <>로 생략(추론)할 수 있습니다.', 'GOLD', NULL, 1),
  (220107, @java_subject_id, 22, 2201, 'CODE_SHORT', 'String 리스트를 names에 선언·생성하는 문장 한 줄을 작성하시오.', '`List<String> names = new ArrayList<>();`', '제네릭 기본 형태입니다.', 'GOLD', NULL, 1),
  (220108, @java_subject_id, 22, 2201, 'CODE_OUTPUT', '제네릭 덕분에 잘못된 타입이 발견되는 시점을 쓰시오.', '컴파일 시점(코드 작성 단계)', '실행 전 발견이 핵심 가치입니다.', 'GOLD', NULL, 1),
  (220109, @java_subject_id, 22, 2201, 'FILL_BLANK', '"Java"를 추가하는 문장의 빈칸을 채우시오.
```java
names.____("Java");
```', 'add', '지정 타입 값만 추가할 수 있습니다.', 'GOLD', NULL, 1),
  (220110, @java_subject_id, 22, 2201, 'CODE_SHORT', 'names의 첫 요소를 캐스팅 없이 name 변수에 저장하는 문장 한 줄을 작성하시오.', '`String name = names.get(0);`', '제네릭이 타입을 보장합니다.', 'GOLD', NULL, 1),
  (220201, @java_subject_id, 22, 2202, 'MULTIPLE_CHOICE', '제네릭 클래스의 타입 매개변수가 실제 타입으로 정해지는 시점은?', '객체 생성 시', '`new Box<Integer>(...)`처럼 생성 시 지정합니다.', 'GOLD', NULL, 1),
  (220202, @java_subject_id, 22, 2202, 'MULTIPLE_CHOICE', '다음 코드의 출력 결과는?
```java
class Box<T> { T value; Box(T value) { this.value = value; } }
Box<Integer> box = new Box<>(10);
System.out.println(box.value);
```', '10', 'T가 Integer로 정해지고 10이 저장됩니다.', 'GOLD', NULL, 1),
  (220203, @java_subject_id, 22, 2202, 'MULTIPLE_CHOICE', '타입 매개변수 선언의 빈칸을 채우면?
```java
class Box<____> { T value; }
```', 'T', '클래스 이름 뒤 <T>로 선언합니다.', 'GOLD', NULL, 1),
  (220204, @java_subject_id, 22, 2202, 'MULTIPLE_CHOICE', '`Box<Integer>`에 문자열을 전달하면?', '컴파일 오류', '지정 타입 외 값은 컴파일 단계에서 막힙니다.', 'GOLD', NULL, 1),
  (220205, @java_subject_id, 22, 2202, 'CODE_OUTPUT', '`Box<String> b = new Box<>("A");`에서 b.value의 값을 쓰시오.', 'A', '같은 클래스가 String 타입으로도 재사용됩니다.', 'GOLD', NULL, 1),
  (220206, @java_subject_id, 22, 2202, 'FILL_BLANK', '빈칸에 들어갈 타입 인자를 쓰시오.
```java
Box<____> box = new Box<>(10);
```', 'Integer', '기본형 int 대신 래퍼 클래스 Integer를 씁니다.', 'GOLD', NULL, 1),
  (220207, @java_subject_id, 22, 2202, 'CODE_SHORT', '값을 받는 생성자를 포함한 제네릭 Box 클래스를 한 줄로 선언하시오.', '`class Box<T> { T value; Box(T value) { this.value = value; } }`', '타입 매개변수 T를 필드·생성자에 사용합니다.', 'GOLD', NULL, 1),
  (220208, @java_subject_id, 22, 2202, 'CODE_OUTPUT', '타입 인자로 기본형 int를 직접 쓸 수 있는지 쓰시오.', '쓸 수 없다(Integer 같은 래퍼 클래스 사용)', '오토박싱으로 값 대입은 자연스럽게 처리됩니다.', 'GOLD', NULL, 1),
  (220209, @java_subject_id, 22, 2202, 'CODE_OUTPUT', 'Box<Integer>와 Box<String>은 클래스를 몇 번 작성해 만든 것인지 쓰시오.', '한 번(같은 클래스의 재사용)', '타입별 복사가 필요 없습니다.', 'GOLD', NULL, 1),
  (220210, @java_subject_id, 22, 2202, 'CODE_SHORT', '"Java"를 담는 Box를 box 변수에 생성하는 문장 한 줄을 작성하시오.', '`Box<String> box = new Box<>("Java");`', 'T가 String으로 정해집니다.', 'GOLD', NULL, 1),
  (220301, @java_subject_id, 22, 2203, 'MULTIPLE_CHOICE', '제네릭 메서드의 <T> 선언 위치는?', '반환형 앞', '`static <T> T first(...)`처럼 반환형 앞에 작성합니다.', 'GOLD', NULL, 1),
  (220302, @java_subject_id, 22, 2203, 'MULTIPLE_CHOICE', '다음 코드의 출력 결과는?
```java
static <T> T first(List<T> values) { return values.get(0); }
System.out.println(first(List.of("A", "B")));
```', 'A', '첫 요소 "A"가 반환됩니다.', 'GOLD', NULL, 1),
  (220303, @java_subject_id, 22, 2203, 'MULTIPLE_CHOICE', '빈칸에 들어갈 반환형은?
```java
static <T> ____ first(List<T> values) { return values.get(0); }
```', 'T', '요소 타입 그대로 반환합니다.', 'GOLD', NULL, 1),
  (220304, @java_subject_id, 22, 2203, 'MULTIPLE_CHOICE', '`first(List.of(1, 2))` 호출에서 T는 무엇으로 추론되는가?', 'Integer', '인자의 타입을 보고 컴파일러가 추론합니다.', 'GOLD', NULL, 1),
  (220305, @java_subject_id, 22, 2203, 'CODE_OUTPUT', '호출할 때 대부분 타입을 명시하지 않아도 되는 이유를 쓰시오.', '컴파일러가 인자의 타입을 보고 T를 추론하기 때문', '타입 추론이 제네릭 메서드의 편의성입니다.', 'GOLD', NULL, 1),
  (220306, @java_subject_id, 22, 2203, 'FILL_BLANK', '타입 매개변수 선언이 되도록 빈칸을 채우시오.
```java
static ____ T first(List<T> values) { return values.get(0); }
```', '<T>', '반환형 앞의 <T>가 선언부입니다.', 'GOLD', NULL, 1),
  (220307, @java_subject_id, 22, 2203, 'CODE_SHORT', '리스트의 마지막 요소를 반환하는 제네릭 메서드 last를 한 줄로 선언하시오.', '`static <T> T last(List<T> values) { return values.get(values.size() - 1); }`', '어떤 타입 리스트든 재사용됩니다.', 'GOLD', NULL, 1),
  (220308, @java_subject_id, 22, 2203, 'CODE_OUTPUT', '`last(List.of("A", "B"))`의 결과를 쓰시오.', 'B', '마지막 인덱스의 요소입니다.', 'GOLD', NULL, 1),
  (220309, @java_subject_id, 22, 2203, 'CODE_OUTPUT', '하나의 제네릭 메서드가 여러 타입을 처리할 수 있는지 쓰시오.', '처리할 수 있다', '클래스의 제네릭 여부와 무관하게 동작합니다.', 'GOLD', NULL, 1),
  (220310, @java_subject_id, 22, 2203, 'CODE_SHORT', 'first(List.of("A", "B"))의 결과를 출력하는 문장 한 줄을 작성하시오.', '`System.out.println(first(List.of("A", "B")));`', '호출과 출력을 결합한 형태입니다.', 'GOLD', NULL, 1),
  (220401, @java_subject_id, 22, 2204, 'MULTIPLE_CHOICE', '와일드카드 사용 기준으로 옳은 것은?', '읽기 용도는 ? extends T, 넣기 용도는 ? super T', '읽기 extends, 쓰기 super가 기본 기준입니다.', 'GOLD', NULL, 1),
  (220402, @java_subject_id, 22, 2204, 'MULTIPLE_CHOICE', '다음 코드의 출력 결과는?
```java
static double sum(List<? extends Number> values) {
    return values.stream().mapToDouble(Number::doubleValue).sum();
}
System.out.println(sum(List.of(1, 2.5)));
```', '3.5', 'Integer와 Double 모두 Number 하위라 합계 3.5가 계산됩니다.', 'GOLD', NULL, 1),
  (220403, @java_subject_id, 22, 2204, 'MULTIPLE_CHOICE', 'Number 하위 타입 목록을 읽기 용도로 받도록 빈칸을 채우면?
```java
static double sum(List<? ____ Number> values)
```', 'extends', '하위 타입을 읽는 용도는 extends입니다.', 'GOLD', NULL, 1),
  (220404, @java_subject_id, 22, 2204, 'MULTIPLE_CHOICE', '`List<Integer>`를 `List<Number>` 매개변수에 그대로 전달하면?', '컴파일 오류', 'List<Integer>는 List<Number>의 하위 타입이 아닙니다. 와일드카드가 필요한 이유입니다.', 'GOLD', NULL, 1),
  (220405, @java_subject_id, 22, 2204, 'CODE_OUTPUT', '`sum(List.of(10, 20))`의 결과를 쓰시오.', '30.0', 'doubleValue 합계라 실수로 반환됩니다.', 'GOLD', NULL, 1),
  (220406, @java_subject_id, 22, 2204, 'FILL_BLANK', '값을 넣는 용도의 와일드카드가 되도록 빈칸을 채우시오.
```java
List<? ____ Integer> target
```', 'super', 'T 값을 넣는 용도에는 super가 적합합니다.', 'GOLD', NULL, 1),
  (220407, @java_subject_id, 22, 2204, 'CODE_SHORT', 'Number 하위 목록의 합계를 구하는 sum 메서드의 선언부를 한 줄로 작성하시오.', '`static double sum(List<? extends Number> values)`', 'Integer·Double 목록을 모두 받을 수 있습니다.', 'GOLD', NULL, 1),
  (220408, @java_subject_id, 22, 2204, 'CODE_OUTPUT', '타입을 알 수 없는 `List<?>`에 null 외의 요소를 추가할 수 있는지 쓰시오.', '추가할 수 없다', '안전한 작업만 허용됩니다.', 'GOLD', NULL, 1),
  (220409, @java_subject_id, 22, 2204, 'CODE_OUTPUT', '`? extends` 목록에 새 요소를 추가할 수 있는지 쓰시오.', '추가할 수 없다(읽기 용도)', '실제 타입을 확정할 수 없기 때문입니다.', 'GOLD', NULL, 1),
  (220410, @java_subject_id, 22, 2204, 'CODE_SHORT', 'sum의 본문(스트림 합계 반환문)을 한 줄로 작성하시오.', '`return values.stream().mapToDouble(Number::doubleValue).sum();`', 'Number의 doubleValue로 합산합니다.', 'GOLD', NULL, 1),
  (220501, @java_subject_id, 22, 2205, 'MULTIPLE_CHOICE', '람다식에 대한 설명으로 옳은 것은?', '함수형 인터페이스의 단일 추상 메서드 구현을 간결하게 표현한다', '매개변수, 화살표, 실행식(블록)으로 구성됩니다.', 'GOLD', NULL, 1),
  (220502, @java_subject_id, 22, 2205, 'MULTIPLE_CHOICE', '다음 코드의 출력 결과는?
```java
Predicate<Integer> positive = number -> number > 0;
System.out.println(positive.test(5));
```', 'true', '5 > 0이 참입니다.', 'GOLD', NULL, 1),
  (220503, @java_subject_id, 22, 2205, 'MULTIPLE_CHOICE', '람다 화살표의 빈칸을 채우면?
```java
Predicate<Integer> positive = number ____ number > 0;
```', '->', 'Java 람다는 -> 를 사용합니다.', 'GOLD', NULL, 1),
  (220504, @java_subject_id, 22, 2205, 'MULTIPLE_CHOICE', '`positive.test(-3)`의 결과는?', 'false', '-3 > 0은 거짓입니다.', 'GOLD', NULL, 1),
  (220505, @java_subject_id, 22, 2205, 'CODE_OUTPUT', '매개변수가 하나일 때 생략할 수 있는 것을 쓰시오.', '괄호', '`n -> ...`처럼 쓸 수 있습니다.', 'GOLD', NULL, 1),
  (220506, @java_subject_id, 22, 2205, 'FILL_BLANK', '짝수 판별 람다의 빈칸을 채우시오.
```java
Predicate<Integer> even = n -> n % 2 ____ 0;
```', '==', '나머지가 0이면 짝수입니다.', 'GOLD', NULL, 1),
  (220507, @java_subject_id, 22, 2205, 'CODE_SHORT', '짝수인지 판별하는 Predicate<Integer> even을 람다로 한 줄 작성하시오.', '`Predicate<Integer> even = n -> n % 2 == 0;`', '조건 하나를 값처럼 전달할 수 있습니다.', 'GOLD', NULL, 1),
  (220508, @java_subject_id, 22, 2205, 'CODE_OUTPUT', '`even.test(4)`의 결과를 쓰시오.', 'true', '4는 짝수입니다.', 'GOLD', NULL, 1),
  (220509, @java_subject_id, 22, 2205, 'CODE_OUTPUT', '본문이 한 개의 식일 때 생략할 수 있는 두 가지를 쓰시오.', '중괄호와 return', '식의 결과가 그대로 반환됩니다.', 'GOLD', NULL, 1),
  (220510, @java_subject_id, 22, 2205, 'CODE_SHORT', '길이가 5 이상인지 판별하는 Predicate<String>을 람다로 한 줄 작성하시오.', '`Predicate<String> longText = s -> s.length() >= 5;`', '문자열 조건도 같은 방식입니다.', 'GOLD', NULL, 1),
  (220601, @java_subject_id, 22, 2206, 'MULTIPLE_CHOICE', '함수형 인터페이스의 조건은?', '추상 메서드가 정확히 하나여야 한다', '람다가 어떤 메서드의 구현인지 유일해야 합니다.', 'GOLD', NULL, 1),
  (220602, @java_subject_id, 22, 2206, 'MULTIPLE_CHOICE', '다음 코드의 출력 결과는?
```java
@FunctionalInterface
interface Operation { int apply(int a, int b); }
Operation add = (a, b) -> a + b;
System.out.println(add.apply(2, 3));
```', '5', '람다 구현이 2 + 3을 계산합니다.', 'GOLD', NULL, 1),
  (220603, @java_subject_id, 22, 2206, 'MULTIPLE_CHOICE', '조건 위반을 컴파일러가 확인하게 하는 애너테이션의 빈칸은?
```java
@____
interface Operation { int apply(int a, int b); }
```', 'FunctionalInterface', '@FunctionalInterface가 단일 추상 메서드 조건을 검증합니다.', 'GOLD', NULL, 1),
  (220604, @java_subject_id, 22, 2206, 'MULTIPLE_CHOICE', '@FunctionalInterface가 붙은 인터페이스에 추상 메서드를 두 개 선언하면?', '컴파일 오류', '조건 위반을 컴파일러가 잡아 줍니다.', 'GOLD', NULL, 1),
  (220605, @java_subject_id, 22, 2206, 'CODE_OUTPUT', '`Operation sub = (a, b) -> a - b;`일 때 `sub.apply(5, 2)`의 결과를 쓰시오.', '3', '같은 인터페이스에 다른 동작을 담을 수 있습니다.', 'GOLD', NULL, 1),
  (220606, @java_subject_id, 22, 2206, 'FILL_BLANK', '람다 문법이 되도록 빈칸을 채우시오.
```java
Operation add = (a, b) ____ a + b;
```', '->', '매개변수와 본문을 화살표로 잇습니다.', 'GOLD', NULL, 1),
  (220607, @java_subject_id, 22, 2206, 'CODE_SHORT', '곱셈을 수행하는 Operation 람다를 multiply 변수에 한 줄로 작성하시오.', '`Operation multiply = (a, b) -> a * b;`', '동작만 바꿔 담는 함수형 스타일입니다.', 'GOLD', NULL, 1),
  (220608, @java_subject_id, 22, 2206, 'CODE_OUTPUT', '`multiply.apply(3, 4)`의 결과를 쓰시오.', '12', '3 * 4입니다.', 'GOLD', NULL, 1),
  (220609, @java_subject_id, 22, 2206, 'CODE_OUTPUT', '람다식을 대입할 수 있는 타입의 조건을 쓰시오.', '함수형 인터페이스(추상 메서드 하나) 타입', '일반 클래스 타입에는 대입할 수 없습니다.', 'GOLD', NULL, 1),
  (220610, @java_subject_id, 22, 2206, 'CODE_SHORT', 'int 두 개를 받아 int를 반환하는 apply를 가진 함수형 인터페이스 Operation을 한 줄로 선언하시오.', '`@FunctionalInterface interface Operation { int apply(int a, int b); }`', '직접 만들기 전에 표준 인터페이스를 먼저 찾는 것도 좋습니다.', 'GOLD', NULL, 1),
  (220701, @java_subject_id, 22, 2207, 'MULTIPLE_CHOICE', '메서드 참조를 쓸 수 있는 조건은?', '람다가 기존 메서드를 그대로 호출할 때', '입력·반환 구조가 함수형 인터페이스와 맞아야 합니다.', 'GOLD', NULL, 1),
  (220702, @java_subject_id, 22, 2207, 'MULTIPLE_CHOICE', '다음 코드의 출력 결과는?
```java
List<String> names = List.of("Java", "SQL");
names.forEach(System.out::println);
```', 'Java 다음 줄 SQL', '각 요소가 println으로 한 줄씩 출력됩니다.', 'GOLD', NULL, 1),
  (220703, @java_subject_id, 22, 2207, 'MULTIPLE_CHOICE', '빈칸에 들어갈 메서드 이름은?
```java
names.forEach(System.out::____);
```', 'println', '메서드 참조는 괄호 없이 이름만 적습니다.', 'GOLD', NULL, 1),
  (220704, @java_subject_id, 22, 2207, 'MULTIPLE_CHOICE', '`String::length`와 같은 의미의 람다는?', 's -> s.length()', '첫 매개변수가 호출 대상이 되는 형태입니다.', 'GOLD', NULL, 1),
  (220705, @java_subject_id, 22, 2207, 'CODE_OUTPUT', 'forEach의 출력 순서는 무엇을 따르는지 쓰시오.', '리스트의 저장 순서', 'List는 순서를 유지합니다.', 'GOLD', NULL, 1),
  (220706, @java_subject_id, 22, 2207, 'FILL_BLANK', '빈칸에 들어갈 메서드 이름을 쓰시오.
```java
Function<String, Integer> len = String::____;
```', 'length', '문자열 길이를 구하는 메서드 참조입니다.', 'GOLD', NULL, 1),
  (220707, @java_subject_id, 22, 2207, 'CODE_SHORT', 'names의 모든 요소를 println 메서드 참조로 출력하는 문장 한 줄을 작성하시오.', '`names.forEach(System.out::println);`', '람다보다 간결한 표현입니다.', 'GOLD', NULL, 1),
  (220708, @java_subject_id, 22, 2207, 'CODE_OUTPUT', '`len.apply("Java")`의 결과를 쓰시오.', '4', '"Java"의 길이입니다.', 'GOLD', NULL, 1),
  (220709, @java_subject_id, 22, 2207, 'CODE_OUTPUT', '정적 메서드 참조의 형식을 쓰시오.', '클래스::메서드', '인스턴스 메서드는 객체::메서드 형태도 가능합니다.', 'GOLD', NULL, 1),
  (220710, @java_subject_id, 22, 2207, 'CODE_SHORT', 'Integer.parseInt를 메서드 참조로 담는 Function<String, Integer>를 한 줄로 작성하시오.', '`Function<String, Integer> parse = Integer::parseInt;`', '정적 메서드 참조의 대표 예입니다.', 'GOLD', NULL, 1),
  (220801, @java_subject_id, 22, 2208, 'MULTIPLE_CHOICE', 'Predicate와 Function의 실행 메서드는?', 'test / apply', 'Predicate는 boolean 검사, Function은 값 변환입니다.', 'GOLD', NULL, 1),
  (220802, @java_subject_id, 22, 2208, 'MULTIPLE_CHOICE', '다음 코드의 출력 결과는?
```java
Predicate<String> longName = s -> s.length() >= 4;
Function<String, Integer> length = String::length;
System.out.println(longName.test("Java") + ", " + length.apply("SQL"));
```', 'true, 3', '"Java" 길이 4는 조건 만족, "SQL" 길이는 3입니다.', 'GOLD', NULL, 1),
  (220803, @java_subject_id, 22, 2208, 'MULTIPLE_CHOICE', '빈칸에 들어갈 메서드는?
```java
Predicate<String> longName = s -> s.____() >= 4;
```', 'length', '문자열 길이는 length()입니다.', 'GOLD', NULL, 1),
  (220804, @java_subject_id, 22, 2208, 'MULTIPLE_CHOICE', '스트림의 filter가 받는 함수형 인터페이스는?', 'Predicate', 'filter는 조건 검사(Predicate), map은 변환(Function)을 받습니다.', 'GOLD', NULL, 1),
  (220805, @java_subject_id, 22, 2208, 'CODE_OUTPUT', '`length.apply("Java")`의 결과를 쓰시오.', '4', '문자열을 정수(길이)로 변환합니다.', 'GOLD', NULL, 1),
  (220806, @java_subject_id, 22, 2208, 'FILL_BLANK', '빈칸에 들어갈 반환 타입을 쓰시오.
```java
Function<String, ____> length = String::length;
```', 'Integer', '입력 String, 출력 Integer인 변환입니다.', 'GOLD', NULL, 1),
  (220807, @java_subject_id, 22, 2208, 'CODE_SHORT', '문자열을 대문자로 변환하는 Function<String, String>을 메서드 참조로 한 줄 작성하시오.', '`Function<String, String> upper = String::toUpperCase;`', '변환 함수의 대표 예입니다.', 'GOLD', NULL, 1),
  (220808, @java_subject_id, 22, 2208, 'CODE_OUTPUT', '`upper.apply("java")`의 결과를 쓰시오.', 'JAVA', '대문자 변환 결과입니다.', 'GOLD', NULL, 1),
  (220809, @java_subject_id, 22, 2208, 'CODE_OUTPUT', 'Predicate의 negate()가 하는 일을 쓰시오.', '조건을 반전시킨다', 'and·or로 조건 조합도 가능합니다.', 'GOLD', NULL, 1),
  (220810, @java_subject_id, 22, 2208, 'CODE_SHORT', '음수인지 판별하는 Predicate<Integer>를 람다로 한 줄 작성하시오.', '`Predicate<Integer> negative = n -> n < 0;`', '필터 조건으로 바로 사용할 수 있습니다.', 'GOLD', NULL, 1),
  (220901, @java_subject_id, 22, 2209, 'MULTIPLE_CHOICE', 'Consumer와 Supplier의 차이로 옳은 것은?', 'Consumer는 값을 받아 소비하고 반환하지 않으며, Supplier는 입력 없이 값을 제공한다', '입력과 출력의 방향이 서로 반대입니다.', 'GOLD', NULL, 1),
  (220902, @java_subject_id, 22, 2209, 'MULTIPLE_CHOICE', '다음 코드의 출력 결과는?
```java
Consumer<String> printer = System.out::println;
Supplier<Integer> supplier = () -> 100;
printer.accept(String.valueOf(supplier.get()));
```', '100', 'supplier가 만든 100이 문자열로 변환되어 출력됩니다.', 'GOLD', NULL, 1),
  (220903, @java_subject_id, 22, 2209, 'MULTIPLE_CHOICE', '빈칸에 들어갈 기호는?
```java
Supplier<Integer> supplier = () ____ 100;
```', '->', '입력 없는 람다는 빈 괄호와 화살표로 씁니다.', 'GOLD', NULL, 1),
  (220904, @java_subject_id, 22, 2209, 'MULTIPLE_CHOICE', 'Consumer의 실행 메서드는?', 'accept', 'Consumer는 accept, Supplier는 get입니다.', 'GOLD', NULL, 1),
  (220905, @java_subject_id, 22, 2209, 'CODE_OUTPUT', '`supplier.get()`의 결과를 쓰시오.', '100', '입력 없이 값을 제공합니다.', 'GOLD', NULL, 1),
  (220906, @java_subject_id, 22, 2209, 'FILL_BLANK', '출력 Consumer의 빈칸을 채우시오.
```java
Consumer<String> printer = System.out::____;
```', 'println', '값을 받아 출력만 하는 소비 동작입니다.', 'GOLD', NULL, 1),
  (220907, @java_subject_id, 22, 2209, 'CODE_SHORT', '"완료"를 제공하는 Supplier<String>을 done 변수에 한 줄로 작성하시오.', '`Supplier<String> done = () -> "완료";`', '지연 생성에 활용할 수 있습니다.', 'GOLD', NULL, 1),
  (220908, @java_subject_id, 22, 2209, 'CODE_OUTPUT', '`done.get()`의 결과를 쓰시오.', '완료', '호출 시점에 값이 만들어집니다.', 'GOLD', NULL, 1),
  (220909, @java_subject_id, 22, 2209, 'CODE_OUTPUT', '스트림의 forEach가 받는 함수형 인터페이스를 쓰시오.', 'Consumer', '각 요소를 소비(출력·저장)합니다.', 'GOLD', NULL, 1),
  (220910, @java_subject_id, 22, 2209, 'CODE_SHORT', '문자열을 출력하는 Consumer<String>을 printer 변수에 한 줄로 작성하시오.', '`Consumer<String> printer = System.out::println;`', '메서드 참조로 간결하게 작성합니다.', 'GOLD', NULL, 1),
  (221001, @java_subject_id, 22, 2210, 'MULTIPLE_CHOICE', '제네릭과 람다를 조합했을 때의 효과는?', '공통 알고리즘과 상황별 조건(동작)을 분리할 수 있다', '알고리즘은 제네릭으로, 조건은 람다로 전달합니다.', 'GOLD', NULL, 1),
  (221002, @java_subject_id, 22, 2210, 'MULTIPLE_CHOICE', '다음 코드의 출력 결과는?
```java
static <T> List<T> filter(List<T> values, Predicate<T> rule) {
    return values.stream().filter(rule).toList();
}
System.out.println(filter(List.of(1, 2, 3), n -> n >= 2));
```', '[2, 3]', '2 이상인 요소만 남습니다.', 'GOLD', NULL, 1),
  (221003, @java_subject_id, 22, 2210, 'MULTIPLE_CHOICE', '조건을 받는 매개변수 타입의 빈칸은?
```java
static <T> List<T> filter(List<T> values, ____<T> rule)
```', 'Predicate', '요소를 남길지 검사하는 조건입니다.', 'GOLD', NULL, 1),
  (221004, @java_subject_id, 22, 2210, 'MULTIPLE_CHOICE', '조건이 바뀔 때 filter 메서드 본문은?', '수정할 필요 없다', '호출하는 쪽의 람다만 바꿉니다.', 'GOLD', NULL, 1),
  (221005, @java_subject_id, 22, 2210, 'CODE_OUTPUT', '`filter(List.of("a", "bb"), s -> s.length() >= 2)`의 결과를 쓰시오.', '[bb]', '같은 메서드가 String 리스트에도 동작합니다.', 'GOLD', NULL, 1),
  (221006, @java_subject_id, 22, 2210, 'FILL_BLANK', '빈칸에 들어갈 스트림 연산을 쓰시오.
```java
return values.stream().____(rule).toList();
```', 'filter', 'Predicate를 필터 조건으로 전달합니다.', 'GOLD', NULL, 1),
  (221007, @java_subject_id, 22, 2210, 'CODE_SHORT', '제네릭 filter 메서드 전체를 한 줄로 작성하시오.', '`static <T> List<T> filter(List<T> values, Predicate<T> rule) { return values.stream().filter(rule).toList(); }`', '타입 안전 + 조건 분리 구조입니다.', 'GOLD', NULL, 1),
  (221008, @java_subject_id, 22, 2210, 'CODE_OUTPUT', '이 ''알고리즘과 조건 분리'' 구조가 설계 원리로 이어지는 다음 행성의 API 이름을 쓰시오.', 'Stream API', 'filter·map 등이 같은 원리로 동작합니다.', 'GOLD', NULL, 1),
  (221009, @java_subject_id, 22, 2210, 'CODE_OUTPUT', '`filter(List.of(1, 2, 3), n -> n > 5)`의 결과를 쓰시오.', '[] (빈 목록)', '조건에 맞는 요소가 없으면 빈 결과입니다.', 'GOLD', NULL, 1),
  (221010, @java_subject_id, 22, 2210, 'CODE_SHORT', 'filter로 [1, 2, 3, 4]에서 짝수만 걸러 evens에 저장하는 문장 한 줄을 작성하시오.', '`List<Integer> evens = filter(List.of(1, 2, 3, 4), n -> n % 2 == 0);`', '조건만 바꿔 재사용하는 호출입니다.', 'GOLD', NULL, 1),
  (230101, @java_subject_id, 23, 2301, 'MULTIPLE_CHOICE', '스트림 연산 구조로 옳은 것은?', '중간 연산을 연결하고 최종 연산에서 결과를 만들며, 최종 연산이 없으면 중간 연산은 실행되지 않는다', '원본을 직접 변경하지 않고 선언적인 단계로 처리합니다.', 'GOLD', NULL, 1),
  (230102, @java_subject_id, 23, 2301, 'MULTIPLE_CHOICE', '다음 코드의 출력 결과는?
```java
long count = List.of(1, 2, 3).stream().count();
System.out.println(count);
```', '3', '요소 개수 3이 반환됩니다.', 'GOLD', NULL, 1),
  (230103, @java_subject_id, 23, 2301, 'MULTIPLE_CHOICE', '스트림을 만드는 빈칸의 메서드는?
```java
long count = list.____().count();
```', 'stream', '컬렉션의 stream()으로 스트림을 시작합니다.', 'GOLD', NULL, 1),
  (230104, @java_subject_id, 23, 2301, 'MULTIPLE_CHOICE', '한 번 최종 연산까지 소비한 스트림을 다시 사용하면?', '사용할 수 없다(새 스트림을 만들어야 한다)', '스트림은 일회용입니다.', 'GOLD', NULL, 1),
  (230105, @java_subject_id, 23, 2301, 'CODE_OUTPUT', '스트림 처리 후 원본 컬렉션은 어떻게 되는지 쓰시오.', '변경되지 않는다', '그래서 몇 번이든 새 스트림을 만들 수 있습니다.', 'GOLD', NULL, 1),
  (230106, @java_subject_id, 23, 2301, 'FILL_BLANK', 'count()의 반환 자료형을 쓰시오.', 'long', '개수는 long으로 반환됩니다.', 'GOLD', NULL, 1),
  (230107, @java_subject_id, 23, 2301, 'CODE_SHORT', 'names 리스트의 요소 개수를 세어 count에 저장하는 문장 한 줄을 작성하시오.', '`long count = names.stream().count();`', '최종 연산 count의 기본형입니다.', 'GOLD', NULL, 1),
  (230108, @java_subject_id, 23, 2301, 'CODE_OUTPUT', '중간 연산만 연결하고 최종 연산을 호출하지 않으면 어떻게 되는지 쓰시오.', '중간 연산이 실제로 실행되지 않는다', '지연 실행이 스트림의 특성입니다.', 'GOLD', NULL, 1),
  (230109, @java_subject_id, 23, 2301, 'CODE_OUTPUT', '같은 컬렉션에서 스트림을 여러 번 만들 수 있는지 쓰시오.', '만들 수 있다', '원본이 남아 있기 때문입니다.', 'GOLD', NULL, 1),
  (230110, @java_subject_id, 23, 2301, 'CODE_SHORT', '빈 리스트의 count 결과를 출력하는 문장 한 줄을 작성하시오.', '`System.out.println(List.of().stream().count());`', '결과는 0입니다.', 'GOLD', NULL, 1),
  (230201, @java_subject_id, 23, 2302, 'MULTIPLE_CHOICE', 'filter의 조건이 의미하는 것은?', '다음 단계로 남길 요소의 조건', '조건이 true인 요소만 전달됩니다. 방향을 혼동하지 않아야 합니다.', 'GOLD', NULL, 1),
  (230202, @java_subject_id, 23, 2302, 'MULTIPLE_CHOICE', '다음 코드의 출력 결과는?
```java
List<Integer> result = List.of(1, 2, 3, 4).stream()
        .filter(n -> n % 2 == 0).toList();
System.out.println(result);
```', '[2, 4]', '짝수만 남습니다.', 'GOLD', NULL, 1),
  (230203, @java_subject_id, 23, 2302, 'MULTIPLE_CHOICE', '짝수 조건의 빈칸을 채우면?
```java
.filter(n -> n % 2 ____ 0)
```', '==', '나머지가 0인 요소를 남깁니다.', 'GOLD', NULL, 1),
  (230204, @java_subject_id, 23, 2302, 'MULTIPLE_CHOICE', '조건에 맞는 요소가 하나도 없으면?', '빈 결과가 나온다', '빈 목록도 정상적인 결과입니다.', 'GOLD', NULL, 1),
  (230205, @java_subject_id, 23, 2302, 'CODE_OUTPUT', '`List.of(1, 2, 3, 4)`에 `filter(n -> n > 2)`를 적용한 결과를 쓰시오.', '[3, 4]', '2보다 큰 요소만 남습니다.', 'GOLD', NULL, 1),
  (230206, @java_subject_id, 23, 2302, 'FILL_BLANK', '빈칸에 들어갈 연산 이름을 쓰시오.
```java
.____(n -> n % 2 == 0)
```', 'filter', '조건 선택 연산입니다.', 'GOLD', NULL, 1),
  (230207, @java_subject_id, 23, 2302, 'CODE_SHORT', 'scores에서 70점 이상만 남겨 passed에 저장하는 문장 한 줄을 작성하시오.', '`List<Integer> passed = scores.stream().filter(s -> s >= 70).toList();`', '통과자 필터링 패턴입니다.', 'GOLD', NULL, 1),
  (230208, @java_subject_id, 23, 2302, 'CODE_OUTPUT', 'filter를 두 번 연결하면 조건이 어떻게 적용되는지 쓰시오.', '단계별로 순서대로 적용된다', '두 조건을 모두 만족하는 요소만 남습니다.', 'GOLD', NULL, 1),
  (230209, @java_subject_id, 23, 2302, 'CODE_OUTPUT', 'filter가 원본 리스트를 변경하는지 쓰시오.', '변경하지 않는다', '결과는 새로 만들어집니다.', 'GOLD', NULL, 1),
  (230210, @java_subject_id, 23, 2302, 'CODE_SHORT', 'names에서 빈 문자열을 제외해 valid에 저장하는 문장 한 줄을 작성하시오.', '`List<String> valid = names.stream().filter(s -> !s.isEmpty()).toList();`', '부정 조건 필터입니다.', 'GOLD', NULL, 1),
  (230301, @java_subject_id, 23, 2303, 'MULTIPLE_CHOICE', 'map 연산의 특징으로 옳은 것은?', '각 요소를 다른 값으로 변환하며 개수는 유지된다', '자료형은 달라질 수 있습니다.', 'GOLD', NULL, 1),
  (230302, @java_subject_id, 23, 2303, 'MULTIPLE_CHOICE', '다음 코드의 출력 결과는?
```java
List<Integer> lengths = List.of("Java", "SQL").stream()
        .map(String::length).toList();
System.out.println(lengths);
```', '[4, 3]', '각 문자열이 길이로 변환됩니다.', 'GOLD', NULL, 1),
  (230303, @java_subject_id, 23, 2303, 'MULTIPLE_CHOICE', '길이 변환의 빈칸을 채우면?
```java
.map(String::____)
```', 'length', '문자열 → 길이 변환입니다.', 'GOLD', NULL, 1),
  (230304, @java_subject_id, 23, 2303, 'MULTIPLE_CHOICE', 'map의 결과 스트림 타입은 원본과?', '달라질 수 있다', 'String → Integer처럼 타입이 바뀔 수 있습니다.', 'GOLD', NULL, 1),
  (230305, @java_subject_id, 23, 2303, 'CODE_OUTPUT', '`List.of("a", "b")`에 `map(s -> s.toUpperCase())`를 적용한 결과를 쓰시오.', '[A, B]', '각 요소가 대문자로 변환됩니다.', 'GOLD', NULL, 1),
  (230306, @java_subject_id, 23, 2303, 'FILL_BLANK', '빈칸에 들어갈 연산 이름을 쓰시오.
```java
.____(String::toUpperCase)
```', 'map', '요소 변환 연산입니다.', 'GOLD', NULL, 1),
  (230307, @java_subject_id, 23, 2303, 'CODE_SHORT', 'names를 모두 대문자로 변환해 upper에 저장하는 문장 한 줄을 작성하시오.', '`List<String> upper = names.stream().map(String::toUpperCase).toList();`', '변환 파이프라인의 기본형입니다.', 'GOLD', NULL, 1),
  (230308, @java_subject_id, 23, 2303, 'CODE_OUTPUT', 'filter와 map을 함께 쓸 때 일반적으로 효율적인 순서를 쓰시오.', '먼저 filter로 요소 수를 줄인 뒤 map으로 변환', '순서에 따라 의미가 달라질 수도 있으니 함께 확인합니다.', 'GOLD', NULL, 1),
  (230309, @java_subject_id, 23, 2303, 'CODE_OUTPUT', 'map 적용 후 요소 개수는 어떻게 되는지 쓰시오.', '동일하게 유지된다', '하나가 하나로 변환됩니다.', 'GOLD', NULL, 1),
  (230310, @java_subject_id, 23, 2303, 'CODE_SHORT', 'names의 길이 목록을 lengths에 저장하는 문장 한 줄을 작성하시오.', '`List<Integer> lengths = names.stream().map(String::length).toList();`', 'String → Integer 변환입니다.', 'GOLD', NULL, 1),
  (230401, @java_subject_id, 23, 2304, 'MULTIPLE_CHOICE', '인자 없는 sorted()가 사용하는 기준은?', '요소의 기본 정렬 기준(Comparable)', '기본 기준이 없는 사용자 정의 객체는 Comparator를 전달해야 합니다.', 'GOLD', NULL, 1),
  (230402, @java_subject_id, 23, 2304, 'MULTIPLE_CHOICE', '다음 코드의 출력 결과는?
```java
List<Integer> sorted = List.of(3, 1, 2).stream().sorted().toList();
System.out.println(sorted);
```', '[1, 2, 3]', '기본(오름차순) 기준으로 정렬됩니다.', 'GOLD', NULL, 1),
  (230403, @java_subject_id, 23, 2304, 'MULTIPLE_CHOICE', '정렬 연산의 빈칸을 채우면?
```java
.____(Comparator.reverseOrder())
```', 'sorted', '스트림의 정렬 연산은 sorted입니다.', 'GOLD', NULL, 1),
  (230404, @java_subject_id, 23, 2304, 'MULTIPLE_CHOICE', 'sorted 후 원본 목록은?', '바뀌지 않는다', '정렬 결과는 새 스트림으로 전달됩니다.', 'GOLD', NULL, 1),
  (230405, @java_subject_id, 23, 2304, 'CODE_OUTPUT', '`List.of(3, 1, 2)`에 `sorted(Comparator.reverseOrder())`를 적용한 결과를 쓰시오.', '[3, 2, 1]', '내림차순 기준입니다.', 'GOLD', NULL, 1),
  (230406, @java_subject_id, 23, 2304, 'FILL_BLANK', '내림차순 기준의 빈칸을 채우시오.
```java
.sorted(Comparator.____())
```', 'reverseOrder', '역순 기준을 전달합니다.', 'GOLD', NULL, 1),
  (230407, @java_subject_id, 23, 2304, 'CODE_SHORT', 'values를 오름차순 정렬해 sorted에 저장하는 문장 한 줄을 작성하시오.', '`List<Integer> sorted = values.stream().sorted().toList();`', '기본 기준 정렬입니다.', 'GOLD', NULL, 1),
  (230408, @java_subject_id, 23, 2304, 'CODE_OUTPUT', '기본 정렬 기준이 없는 사용자 정의 객체를 인자 없는 sorted로 정렬하면 어떻게 되는지 쓰시오.', '실행 중 오류가 발생한다(Comparator 전달 필요)', 'Comparable 미구현 객체는 기준이 없습니다.', 'GOLD', NULL, 1),
  (230409, @java_subject_id, 23, 2304, 'CODE_OUTPUT', 'sorted와 limit를 조합하면 만들 수 있는 처리의 예를 쓰시오.', '정렬 후 상위 N개 추출', '랭킹 상위권 추출 같은 처리입니다.', 'GOLD', NULL, 1),
  (230410, @java_subject_id, 23, 2304, 'CODE_SHORT', 'values를 내림차순 정렬해 desc에 저장하는 문장 한 줄을 작성하시오.', '`List<Integer> desc = values.stream().sorted(Comparator.reverseOrder()).toList();`', 'Comparator를 전달한 정렬입니다.', 'GOLD', NULL, 1),
  (230501, @java_subject_id, 23, 2305, 'MULTIPLE_CHOICE', 'distinct의 중복 판단 기준은?', 'equals 기준', '사용자 정의 객체는 equals·hashCode 재정의가 필요합니다.', 'GOLD', NULL, 1),
  (230502, @java_subject_id, 23, 2305, 'MULTIPLE_CHOICE', '다음 코드의 출력 결과는?
```java
List<Integer> result = List.of(1, 1, 2, 3).stream()
        .distinct().limit(2).toList();
System.out.println(result);
```', '[1, 2]', '중복 제거 후 [1, 2, 3]에서 앞의 2개만 남습니다.', 'GOLD', NULL, 1),
  (230503, @java_subject_id, 23, 2305, 'MULTIPLE_CHOICE', '중복 제거 연산의 빈칸을 채우면?
```java
.____().limit(2)
```', 'distinct', 'distinct가 중복을 제거합니다.', 'GOLD', NULL, 1),
  (230504, @java_subject_id, 23, 2305, 'MULTIPLE_CHOICE', '연산 순서를 바꿔 `limit(2).distinct()`로 쓰면 결과는? (원본 [1, 1, 2, 3])', '[1]', '먼저 [1, 1]로 자른 뒤 중복이 제거되어 [1]입니다. 나열 순서가 곧 처리 순서입니다.', 'GOLD', NULL, 1),
  (230505, @java_subject_id, 23, 2305, 'CODE_OUTPUT', '`List.of(1, 1, 1)`에 distinct를 적용한 결과를 쓰시오.', '[1]', '같은 값은 하나만 남습니다.', 'GOLD', NULL, 1),
  (230506, @java_subject_id, 23, 2305, 'FILL_BLANK', '개수 제한 연산의 빈칸을 채우시오.
```java
.distinct().____(2)
```', 'limit', '앞에서 지정한 개수만 남깁니다.', 'GOLD', NULL, 1),
  (230507, @java_subject_id, 23, 2305, 'CODE_SHORT', 'nums에서 중복을 제거하고 앞의 3개만 r에 저장하는 문장 한 줄을 작성하시오.', '`List<Integer> r = nums.stream().distinct().limit(3).toList();`', '두 연산의 조합입니다.', 'GOLD', NULL, 1),
  (230508, @java_subject_id, 23, 2305, 'CODE_OUTPUT', '`limit(0)`의 결과를 쓰시오.', '빈 결과([])', '0개만 남기므로 비어 있습니다.', 'GOLD', NULL, 1),
  (230509, @java_subject_id, 23, 2305, 'CODE_OUTPUT', '사용자 정의 객체에서 distinct가 의도대로 동작하기 위한 요건을 쓰시오.', 'equals와 hashCode 재정의', '중복 판단 기준을 제공해야 합니다.', 'GOLD', NULL, 1),
  (230510, @java_subject_id, 23, 2305, 'CODE_SHORT', 'names에서 중복만 제거해 unique에 저장하는 문장 한 줄을 작성하시오.', '`List<String> unique = names.stream().distinct().toList();`', '중복 제거 단독 사용입니다.', 'GOLD', NULL, 1),
  (230601, @java_subject_id, 23, 2306, 'MULTIPLE_CHOICE', 'sum·average를 사용하려면 필요한 것은?', 'mapToInt 등으로 숫자 전용 스트림(IntStream)으로 변환', 'count는 일반 스트림에서도 가능합니다.', 'GOLD', NULL, 1),
  (230602, @java_subject_id, 23, 2306, 'MULTIPLE_CHOICE', '다음 코드의 출력 결과는?
```java
int sum = List.of(10, 20, 30).stream().mapToInt(Integer::intValue).sum();
double avg = List.of(10, 20, 30).stream().mapToInt(Integer::intValue).average().orElse(0);
System.out.println(sum + ", " + avg);
```', '60, 20.0', '합 60, 평균 20.0입니다.', 'GOLD', NULL, 1),
  (230603, @java_subject_id, 23, 2306, 'MULTIPLE_CHOICE', '숫자 전용 스트림 변환의 빈칸을 채우면?
```java
.____(Integer::intValue).sum()
```', 'mapToInt', 'mapToInt가 IntStream을 만듭니다.', 'GOLD', NULL, 1),
  (230604, @java_subject_id, 23, 2306, 'MULTIPLE_CHOICE', 'average()의 반환 타입은?', 'OptionalDouble', '데이터가 없을 수 있어 OptionalDouble로 표현합니다.', 'GOLD', NULL, 1),
  (230605, @java_subject_id, 23, 2306, 'CODE_OUTPUT', '`List.of(10, 20, 30)`의 sum 결과를 쓰시오.', '60', '10 + 20 + 30입니다.', 'GOLD', NULL, 1),
  (230606, @java_subject_id, 23, 2306, 'FILL_BLANK', '빈 데이터 기본값 처리의 빈칸을 채우시오.
```java
.average().____(0)
```', 'orElse', '값이 없으면 0을 사용합니다.', 'GOLD', NULL, 1),
  (230607, @java_subject_id, 23, 2306, 'CODE_SHORT', 'scores의 합계를 sum에 저장하는 문장 한 줄을 작성하시오.', '`int sum = scores.stream().mapToInt(Integer::intValue).sum();`', 'IntStream 변환 후 집계합니다.', 'GOLD', NULL, 1),
  (230608, @java_subject_id, 23, 2306, 'CODE_OUTPUT', '빈 리스트에서 `average().orElse(0)`의 결과를 쓰시오.', '0.0', '기본값이 반환됩니다. 0으로 볼지 별도 안내할지는 요구사항에 따릅니다.', 'GOLD', NULL, 1),
  (230609, @java_subject_id, 23, 2306, 'CODE_OUTPUT', 'count는 숫자 전용 스트림 변환 없이 사용할 수 있는지 쓰시오.', '사용할 수 있다', 'count는 일반 스트림의 최종 연산입니다.', 'GOLD', NULL, 1),
  (230610, @java_subject_id, 23, 2306, 'CODE_SHORT', 'scores의 평균을 avg에 저장(빈 목록이면 0)하는 문장 한 줄을 작성하시오.', '`double avg = scores.stream().mapToInt(Integer::intValue).average().orElse(0);`', '평균 집계의 기본형입니다.', 'GOLD', NULL, 1),
  (230701, @java_subject_id, 23, 2307, 'MULTIPLE_CHOICE', 'reduce의 첫 번째 인자의 의미는?', '계산의 시작값', '곱셈이면 1, 덧셈이면 0처럼 결과에 영향을 주지 않는 값을 씁니다.', 'GOLD', NULL, 1),
  (230702, @java_subject_id, 23, 2307, 'MULTIPLE_CHOICE', '다음 코드의 출력 결과는?
```java
int product = List.of(2, 3, 4).stream().reduce(1, (a, b) -> a * b);
System.out.println(product);
```', '24', '1 × 2 × 3 × 4 = 24입니다.', 'GOLD', NULL, 1),
  (230703, @java_subject_id, 23, 2307, 'MULTIPLE_CHOICE', '곱 누적의 빈칸을 채우면?
```java
.reduce(1, (a, b) -> a ____ b)
```', '*', '두 값을 곱해 누적합니다.', 'GOLD', NULL, 1),
  (230704, @java_subject_id, 23, 2307, 'MULTIPLE_CHOICE', '덧셈 누적에서 시작값으로 알맞은 것은?', '0', '0은 덧셈 결과에 영향을 주지 않습니다.', 'GOLD', NULL, 1),
  (230705, @java_subject_id, 23, 2307, 'CODE_OUTPUT', '`List.of(1, 2, 3).stream().reduce(0, (a, b) -> a + b)`의 결과를 쓰시오.', '6', '합계 누적입니다.', 'GOLD', NULL, 1),
  (230706, @java_subject_id, 23, 2307, 'FILL_BLANK', '누적 연산의 빈칸을 채우시오.
```java
.____(0, (a, b) -> a + b)
```', 'reduce', '여러 요소를 하나의 값으로 누적합니다.', 'GOLD', NULL, 1),
  (230707, @java_subject_id, 23, 2307, 'CODE_SHORT', 'nums의 모든 요소의 곱을 product에 저장하는 문장 한 줄을 작성하시오.', '`int product = nums.stream().reduce(1, (a, b) -> a * b);`', '시작값 1의 곱 누적입니다.', 'GOLD', NULL, 1),
  (230708, @java_subject_id, 23, 2307, 'CODE_OUTPUT', '빈 스트림에서 `reduce(1, (a, b) -> a * b)`의 결과를 쓰시오.', '1(시작값)', '누적할 요소가 없으면 시작값이 반환됩니다.', 'GOLD', NULL, 1),
  (230709, @java_subject_id, 23, 2307, 'CODE_OUTPUT', '합계·최댓값처럼 자주 쓰는 누적에 reduce 대신 권장되는 것을 쓰시오.', 'sum·max 같은 전용 메서드', 'reduce는 전용 메서드가 없는 사용자 정의 누적에 씁니다.', 'GOLD', NULL, 1),
  (230710, @java_subject_id, 23, 2307, 'CODE_SHORT', 'names의 문자열을 모두 이어 붙여 joined에 저장하는 reduce 문장 한 줄을 작성하시오.', '`String joined = names.stream().reduce("", (a, b) -> a + b);`', '문자열 결합 누적입니다.', 'GOLD', NULL, 1),
  (230801, @java_subject_id, 23, 2308, 'MULTIPLE_CHOICE', 'Collectors.toMap에서 같은 키가 두 번 나오면?', '예외가 발생하므로 병합 규칙을 세 번째 인자로 전달한다', '병합 함수가 없으면 중복 키에서 예외가 발생합니다.', 'GOLD', NULL, 1),
  (230802, @java_subject_id, 23, 2308, 'MULTIPLE_CHOICE', '다음 코드의 출력 결과는?
```java
Set<String> set = List.of("A", "B", "A").stream().collect(Collectors.toSet());
System.out.println(set.size());
```', '2', 'Set 수집으로 중복 "A"가 제거되어 2입니다.', 'GOLD', NULL, 1),
  (230803, @java_subject_id, 23, 2308, 'MULTIPLE_CHOICE', 'Set으로 모으는 빈칸을 채우면?
```java
.collect(Collectors.____())
```', 'toSet', '결과를 Set 자료구조로 수집합니다.', 'GOLD', NULL, 1),
  (230804, @java_subject_id, 23, 2308, 'MULTIPLE_CHOICE', 'Collectors.joining의 용도는?', '문자열 목록을 구분자로 연결한 하나의 문자열 생성', '쉼표로 구분된 이름 목록 출력 등에 편리합니다.', 'GOLD', NULL, 1),
  (230805, @java_subject_id, 23, 2308, 'CODE_OUTPUT', 'toSet 수집에서 중복 요소는 어떻게 되는지 쓰시오.', '제거된다', 'Set의 특성이 적용됩니다.', 'GOLD', NULL, 1),
  (230806, @java_subject_id, 23, 2308, 'FILL_BLANK', '빈칸에 들어갈 클래스 이름을 쓰시오.
```java
.collect(____.toSet())
```', 'Collectors', '수집기 모음 클래스입니다.', 'GOLD', NULL, 1),
  (230807, @java_subject_id, 23, 2308, 'CODE_SHORT', 'names를 ", "로 연결한 문자열을 result에 저장하는 문장 한 줄을 작성하시오.', '`String result = names.stream().collect(Collectors.joining(", "));`', 'joining 수집기입니다.', 'GOLD', NULL, 1),
  (230808, @java_subject_id, 23, 2308, 'CODE_OUTPUT', '`List.of("A", "B")`를 joining(", ")으로 수집한 결과를 쓰시오.', 'A, B', '구분자로 연결된 한 문자열입니다.', 'GOLD', NULL, 1),
  (230809, @java_subject_id, 23, 2308, 'CODE_OUTPUT', '`.toList()`와 `.collect(Collectors.toList())`의 결과 관계를 쓰시오.', '같은 결과(List 수집)', 'toList()는 간결한 축약형입니다.', 'GOLD', NULL, 1),
  (230810, @java_subject_id, 23, 2308, 'CODE_SHORT', 'names를 Set으로 수집해 set에 저장하는 문장 한 줄을 작성하시오.', '`Set<String> set = names.stream().collect(Collectors.toSet());`', '자료구조 변환 수집입니다.', 'GOLD', NULL, 1),
  (230901, @java_subject_id, 23, 2309, 'MULTIPLE_CHOICE', 'groupingBy의 결과 구조는?', '기준 함수의 반환값을 키로, 해당 그룹의 요소 목록을 값으로 갖는 Map', '등급별 학생 목록, 카테고리별 상품 목록에 그대로 적용됩니다.', 'GOLD', NULL, 1),
  (230902, @java_subject_id, 23, 2309, 'MULTIPLE_CHOICE', '다음 코드의 출력 결과는?
```java
Map<Integer, List<String>> groups = List.of("A", "BB", "CC").stream()
        .collect(Collectors.groupingBy(String::length));
System.out.println(groups.get(2));
```', '[BB, CC]', '길이 2 그룹에 BB와 CC가 묶입니다.', 'GOLD', NULL, 1),
  (230903, @java_subject_id, 23, 2309, 'MULTIPLE_CHOICE', '그룹화 수집기의 빈칸을 채우면?
```java
Collectors.____(String::length)
```', 'groupingBy', '기준 함수로 그룹을 만듭니다.', 'GOLD', NULL, 1),
  (230904, @java_subject_id, 23, 2309, 'MULTIPLE_CHOICE', 'groupingBy의 두 번째 인자로 Collectors.counting()을 전달하면?', '그룹별 개수', '집계 방식만 바꿔 통계를 만듭니다.', 'GOLD', NULL, 1),
  (230905, @java_subject_id, 23, 2309, 'CODE_OUTPUT', '2번 코드에서 `groups.get(1)`의 결과를 쓰시오.', '[A]', '길이 1 그룹입니다.', 'GOLD', NULL, 1),
  (230906, @java_subject_id, 23, 2309, 'FILL_BLANK', '길이 기준 그룹화의 빈칸을 채우시오.
```java
.collect(Collectors.groupingBy(String::____))
```', 'length', '기준 함수가 키를 만듭니다.', 'GOLD', NULL, 1),
  (230907, @java_subject_id, 23, 2309, 'CODE_SHORT', 'names를 길이별로 그룹화해 groups에 저장하는 문장 한 줄을 작성하시오.', '`Map<Integer, List<String>> groups = names.stream().collect(Collectors.groupingBy(String::length));`', '그룹화의 기본형입니다.', 'GOLD', NULL, 1),
  (230908, @java_subject_id, 23, 2309, 'CODE_OUTPUT', '존재하지 않는 그룹 키로 get을 호출한 결과를 쓰시오.', 'null', 'Map의 없는 키 조회와 같습니다.', 'GOLD', NULL, 1),
  (230909, @java_subject_id, 23, 2309, 'CODE_OUTPUT', '''등급별 학생 목록''을 만들기에 적합한 스트림 연산을 쓰시오.', 'groupingBy', '조건별 그룹화의 대표 용례입니다.', 'GOLD', NULL, 1),
  (230910, @java_subject_id, 23, 2309, 'CODE_SHORT', 'names를 길이별 ''개수''로 집계해 counts에 저장하는 문장 한 줄을 작성하시오.', '`Map<Integer, Long> counts = names.stream().collect(Collectors.groupingBy(String::length, Collectors.counting()));`', '두 번째 인자로 집계 방식을 바꿉니다.', 'GOLD', NULL, 1),
  (231001, @java_subject_id, 23, 2310, 'MULTIPLE_CHOICE', '스트림 체인을 읽는 권장 순서는?', '필터링 → 변환 → 정렬 → 수집', '단계마다 어떤 데이터가 흐르는지 확인하며 읽습니다.', 'GOLD', NULL, 1),
  (231002, @java_subject_id, 23, 2310, 'MULTIPLE_CHOICE', '다음 코드의 출력 결과는?
```java
List<String> result = List.of("sql", "java", "web").stream()
        .filter(s -> s.length() >= 4).map(String::toUpperCase).sorted().toList();
System.out.println(result);
```', '[JAVA]', '길이 4 이상은 java뿐이고 대문자 변환·정렬을 거쳐 [JAVA]입니다.', 'GOLD', NULL, 1),
  (231003, @java_subject_id, 23, 2310, 'MULTIPLE_CHOICE', '대문자 변환 단계의 빈칸을 채우면?
```java
.filter(s -> s.length() >= 4).____(String::toUpperCase)
```', 'map', '변환은 map입니다.', 'GOLD', NULL, 1),
  (231004, @java_subject_id, 23, 2310, 'MULTIPLE_CHOICE', '중간 연산이 실제로 처리되는 시점은?', '최종 연산이 호출될 때', '지연 실행 특성입니다.', 'GOLD', NULL, 1),
  (231005, @java_subject_id, 23, 2310, 'CODE_OUTPUT', '`List.of("sql", "java", "web")`에 `filter(s -> s.length() >= 4)`만 적용한 결과를 쓰시오.', '[java]', '4글자 이상은 java뿐입니다.', 'GOLD', NULL, 1),
  (231006, @java_subject_id, 23, 2310, 'FILL_BLANK', '정렬 단계의 빈칸을 채우시오.
```java
.map(String::toUpperCase).____().toList();
```', 'sorted', '변환 후 정렬 단계입니다.', 'GOLD', NULL, 1),
  (231007, @java_subject_id, 23, 2310, 'CODE_SHORT', 'words에서 길이 4 이상만 대문자로 변환·정렬해 result에 저장하는 문장 한 줄을 작성하시오.', '`List<String> result = words.stream().filter(s -> s.length() >= 4).map(String::toUpperCase).sorted().toList();`', '필터→변환→정렬→수집 체인입니다.', 'GOLD', NULL, 1),
  (231008, @java_subject_id, 23, 2310, 'CODE_OUTPUT', '반복문과 비교한 스트림의 장점을 쓰시오.', '''무엇을 할지''가 연산 이름으로 드러난다', '선언적 표현이 가독성을 높입니다.', 'GOLD', NULL, 1),
  (231009, @java_subject_id, 23, 2310, 'CODE_OUTPUT', '중간값 추적이 필요한 복잡한 로직에서 유리할 수 있는 방식을 쓰시오.', '반복문', '디버깅 편의성은 반복문이 나을 수 있습니다.', 'GOLD', NULL, 1),
  (231010, @java_subject_id, 23, 2310, 'CODE_SHORT', 'scores에서 70 이상만 오름차순 정렬해 top에 저장하는 문장 한 줄을 작성하시오.', '`List<Integer> top = scores.stream().filter(s -> s >= 70).sorted().toList();`', '필터와 정렬의 조합입니다.', 'GOLD', NULL, 1),
  (240101, @java_subject_id, 24, 2401, 'MULTIPLE_CHOICE', 'LocalDate의 특성으로 옳은 것은?', '불변 객체라서 계산 메서드는 새 객체를 반환한다', '시간대 없이 연·월·일만 표현하는 불변 객체입니다.', 'GOLD', NULL, 1),
  (240102, @java_subject_id, 24, 2401, 'MULTIPLE_CHOICE', '다음 코드의 출력 결과는?
```java
LocalDate date = LocalDate.of(2026, 7, 23);
System.out.println(date.plusDays(1));
```', '2026-07-24', '하루 뒤 날짜의 새 객체가 반환됩니다.', 'GOLD', NULL, 1),
  (240103, @java_subject_id, 24, 2401, 'MULTIPLE_CHOICE', '날짜 생성 메서드의 빈칸을 채우면?
```java
LocalDate date = LocalDate.____(2026, 7, 23);
```', 'of', 'LocalDate.of(연, 월, 일)입니다.', 'GOLD', NULL, 1),
  (240104, @java_subject_id, 24, 2401, 'MULTIPLE_CHOICE', '`date.plusDays(1)`만 호출하고 결과를 받지 않으면 date는?', '그대로다', '불변 객체이므로 결과를 변수에 받아야 합니다.', 'GOLD', NULL, 1),
  (240105, @java_subject_id, 24, 2401, 'CODE_OUTPUT', '2026-07-23에 `minusDays(1)`을 적용한 결과를 쓰시오.', '2026-07-22', '하루 전 날짜의 새 객체입니다.', 'GOLD', NULL, 1),
  (240106, @java_subject_id, 24, 2401, 'FILL_BLANK', '7일 후 날짜를 구하도록 빈칸을 채우시오.
```java
LocalDate end = date.____(7);
```', 'plusDays', '일 단위 덧셈 메서드입니다.', 'GOLD', NULL, 1),
  (240107, @java_subject_id, 24, 2401, 'CODE_SHORT', '오늘 날짜를 today에 저장하는 문장 한 줄을 작성하시오.', '`LocalDate today = LocalDate.now();`', '현재 날짜는 now()로 얻습니다.', 'GOLD', NULL, 1),
  (240108, @java_subject_id, 24, 2401, 'CODE_OUTPUT', '`LocalDate.of(2026, 2, 30)`처럼 존재하지 않는 날짜를 만들면 어떻게 되는지 쓰시오.', '실행 중 예외가 발생한다', '유효하지 않은 날짜는 생성 시점에 거부됩니다.', 'GOLD', NULL, 1),
  (240109, @java_subject_id, 24, 2401, 'CODE_OUTPUT', 'isBefore·isAfter의 반환 자료형을 쓰시오.', 'boolean', '날짜 비교 결과입니다.', 'GOLD', NULL, 1),
  (240110, @java_subject_id, 24, 2401, 'CODE_SHORT', '2026년 7월 23일 LocalDate를 date에 생성하는 문장 한 줄을 작성하시오.', '`LocalDate date = LocalDate.of(2026, 7, 23);`', '연·월·일을 순서대로 전달합니다.', 'GOLD', NULL, 1),
  (240201, @java_subject_id, 24, 2402, 'MULTIPLE_CHOICE', 'LocalDateTime의 특성으로 옳은 것은?', '날짜와 시각을 함께 표현하지만 시간대 정보는 없다', '서버 간 절대 시각 교환에는 Instant나 시간대 타입을 고려합니다.', 'GOLD', NULL, 1),
  (240202, @java_subject_id, 24, 2402, 'MULTIPLE_CHOICE', '다음 코드의 출력 결과는?
```java
LocalDateTime time = LocalDateTime.of(2026, 7, 23, 10, 30);
System.out.println(time.getHour());
```', '10', '시(hour) 부분인 10이 반환됩니다.', 'GOLD', NULL, 1),
  (240203, @java_subject_id, 24, 2402, 'MULTIPLE_CHOICE', '10시 30분이 되도록 빈칸을 채우면?
```java
LocalDateTime.of(2026, 7, 23, 10, ____)
```', '30', '인자는 연, 월, 일, 시, 분 순서입니다.', 'GOLD', NULL, 1),
  (240204, @java_subject_id, 24, 2402, 'MULTIPLE_CHOICE', '서버 간 절대 시각 교환에 더 적합한 타입은?', 'Instant(또는 ZonedDateTime)', '시간대 기준이 명확한 타입을 사용합니다.', 'GOLD', NULL, 1),
  (240205, @java_subject_id, 24, 2402, 'CODE_OUTPUT', '2번 코드에서 `time.getMinute()`의 결과를 쓰시오.', '30', '분 부분입니다.', 'GOLD', NULL, 1),
  (240206, @java_subject_id, 24, 2402, 'FILL_BLANK', '시(hour)를 읽는 메서드의 빈칸을 채우시오.
```java
time.____()
```', 'getHour', '요소별 get 메서드가 제공됩니다.', 'GOLD', NULL, 1),
  (240207, @java_subject_id, 24, 2402, 'CODE_SHORT', '2026-07-23 10:30을 표현하는 LocalDateTime을 time에 생성하는 문장 한 줄을 작성하시오.', '`LocalDateTime time = LocalDateTime.of(2026, 7, 23, 10, 30);`', '날짜와 시각을 함께 생성합니다.', 'GOLD', NULL, 1),
  (240208, @java_subject_id, 24, 2402, 'CODE_OUTPUT', '`time.toLocalDate()`가 반환하는 것을 쓰시오.', '날짜 부분(LocalDate)', 'toLocalTime()으로 시각 부분도 분리할 수 있습니다.', 'GOLD', NULL, 1),
  (240209, @java_subject_id, 24, 2402, 'CODE_OUTPUT', '시간대 정보가 없어서 생기는 문제를 한 문장으로 쓰시오.', '같은 값이라도 나라마다 실제 시점이 다르다', '글로벌 서비스 기록에는 기준이 필요합니다.', 'GOLD', NULL, 1),
  (240210, @java_subject_id, 24, 2402, 'CODE_SHORT', '현재 일시를 now에 저장하는 문장 한 줄을 작성하시오.', '`LocalDateTime now = LocalDateTime.now();`', '현재 날짜+시각입니다.', 'GOLD', NULL, 1),
  (240301, @java_subject_id, 24, 2403, 'MULTIPLE_CHOICE', '패턴 문자 MM과 mm의 의미는?', 'MM은 월, mm은 분', '대소문자에 따라 의미가 다르므로 구분해야 합니다.', 'GOLD', NULL, 1),
  (240302, @java_subject_id, 24, 2403, 'MULTIPLE_CHOICE', '다음 코드의 출력 결과는?
```java
DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
System.out.println(LocalDate.of(2026, 7, 23).format(formatter));
```', '2026-07-23', '패턴대로 월·일이 두 자리로 표시됩니다.', 'GOLD', NULL, 1),
  (240303, @java_subject_id, 24, 2403, 'MULTIPLE_CHOICE', '포맷터 생성의 빈칸을 채우면?
```java
DateTimeFormatter.____("yyyy-MM-dd")
```', 'ofPattern', '패턴 문자열로 포맷터를 만듭니다.', 'GOLD', NULL, 1),
  (240304, @java_subject_id, 24, 2403, 'MULTIPLE_CHOICE', 'parse할 문자열이 패턴과 맞지 않으면 발생하는 예외는?', 'DateTimeParseException', '사용자 입력 변환에는 예외 처리를 함께 둡니다.', 'GOLD', NULL, 1),
  (240305, @java_subject_id, 24, 2403, 'CODE_OUTPUT', 'HH와 hh의 차이를 쓰시오.', 'HH는 24시간제, hh는 12시간제', '시각 패턴도 대소문자를 구분합니다.', 'GOLD', NULL, 1),
  (240306, @java_subject_id, 24, 2403, 'FILL_BLANK', '날짜를 문자열로 바꾸는 메서드의 빈칸을 채우시오.
```java
date.____(formatter)
```', 'format', '반대 방향(문자열→날짜)은 parse입니다.', 'GOLD', NULL, 1),
  (240307, @java_subject_id, 24, 2403, 'CODE_SHORT', '"yyyy-MM-dd" 패턴의 포맷터를 formatter에 생성하는 문장 한 줄을 작성하시오.', '`DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");`', '형식화·파싱에 재사용합니다.', 'GOLD', NULL, 1),
  (240308, @java_subject_id, 24, 2403, 'CODE_OUTPUT', '패턴 "yyyy/MM/dd"로 2026-07-23을 형식화한 결과를 쓰시오.', '2026/07/23', '구분 문자도 패턴을 따릅니다.', 'GOLD', NULL, 1),
  (240309, @java_subject_id, 24, 2403, 'CODE_OUTPUT', '사용자 입력을 parse로 변환할 때 함께 두어야 할 것을 쓰시오.', '예외 처리', '형식 불일치는 DateTimeParseException이 됩니다.', 'GOLD', NULL, 1),
  (240310, @java_subject_id, 24, 2403, 'CODE_SHORT', 'date를 formatter로 형식화해 출력하는 문장 한 줄을 작성하시오.', '`System.out.println(date.format(formatter));`', '표시용 문자열 변환입니다.', 'GOLD', NULL, 1),
  (240401, @java_subject_id, 24, 2404, 'MULTIPLE_CHOICE', 'StringBuilder를 쓰는 이유로 옳은 것은?', '내부 버퍼로 이어 붙여 불필요한 String 객체 생성을 줄이기 위해', '반복 연결이 많을수록 효과가 커집니다.', 'GOLD', NULL, 1),
  (240402, @java_subject_id, 24, 2404, 'MULTIPLE_CHOICE', '다음 코드의 출력 결과는?
```java
StringBuilder builder = new StringBuilder("Java");
builder.append(" ").append("Gold");
System.out.println(builder);
```', 'Java Gold', '공백과 "Gold"가 이어 붙습니다.', 'GOLD', NULL, 1),
  (240403, @java_subject_id, 24, 2404, 'MULTIPLE_CHOICE', '이어 붙이기 메서드의 빈칸을 채우면?
```java
builder.____(" ").append("Gold");
```', 'append', 'append·insert·delete로 내용을 변경합니다.', 'GOLD', NULL, 1),
  (240404, @java_subject_id, 24, 2404, 'MULTIPLE_CHOICE', 'StringBuilder에서 최종 String을 얻는 방법은?', 'toString() 호출', 'toString()이 최종 문자열을 만듭니다.', 'GOLD', NULL, 1),
  (240405, @java_subject_id, 24, 2404, 'CODE_OUTPUT', 'append를 연달아 이어 쓸 수 있는 이유를 쓰시오.', 'append가 자기 자신(StringBuilder)을 반환하기 때문', '메서드 체이닝이 가능합니다.', 'GOLD', NULL, 1),
  (240406, @java_subject_id, 24, 2404, 'FILL_BLANK', '빈칸에 들어갈 클래스 이름을 쓰시오.
```java
StringBuilder builder = new ____("Java");
```', 'StringBuilder', '초기 문자열로 생성할 수 있습니다.', 'GOLD', NULL, 1),
  (240407, @java_subject_id, 24, 2404, 'CODE_SHORT', 'builder 끝에 "!"를 붙이는 문장 한 줄을 작성하시오.', '`builder.append("!");`', 'append로 내용을 추가합니다.', 'GOLD', NULL, 1),
  (240408, @java_subject_id, 24, 2404, 'CODE_OUTPUT', '반복문 안 String + 연결의 문제점을 쓰시오.', '매번 새로운 문자열 객체가 만들어진다', '반복 횟수가 많을수록 비용이 커집니다.', 'GOLD', NULL, 1),
  (240409, @java_subject_id, 24, 2404, 'CODE_OUTPUT', '`System.out.println(builder)`가 출력하는 것을 쓰시오.', 'builder에 담긴 문자열 내용', '내부적으로 toString이 사용됩니다.', 'GOLD', NULL, 1),
  (240410, @java_subject_id, 24, 2404, 'CODE_SHORT', 'builder의 내용을 String으로 변환해 result에 저장하는 문장 한 줄을 작성하시오.', '`String result = builder.toString();`', '최종 문자열 추출입니다.', 'GOLD', NULL, 1),
  (240501, @java_subject_id, 24, 2405, 'MULTIPLE_CHOICE', 'matches 메서드가 true를 반환하는 조건은?', '문자열 전체가 패턴과 일치할 때', 'matches는 전체 일치 검사입니다.', 'GOLD', NULL, 1),
  (240502, @java_subject_id, 24, 2405, 'MULTIPLE_CHOICE', '다음 코드의 출력 결과는?
```java
boolean valid = "12345".matches("\\\\d+");
System.out.println(valid);
```', 'true', '모두 숫자이므로 패턴과 일치합니다.', 'GOLD', NULL, 1),
  (240503, @java_subject_id, 24, 2405, 'MULTIPLE_CHOICE', '패턴 검사 메서드의 빈칸을 채우면?
```java
"12345".____("\\\\d+")
```', 'matches', '문자열의 matches로 형식을 검사합니다.', 'GOLD', NULL, 1),
  (240504, @java_subject_id, 24, 2405, 'MULTIPLE_CHOICE', '`"12a45".matches("\\\\d+")`의 결과는?', 'false', '문자 a가 섞여 전체 일치가 아닙니다.', 'GOLD', NULL, 1),
  (240505, @java_subject_id, 24, 2405, 'CODE_OUTPUT', '패턴 `\\\\d`가 의미하는 것을 쓰시오.', '숫자 하나', '+는 1회 이상 반복입니다.', 'GOLD', NULL, 1),
  (240506, @java_subject_id, 24, 2405, 'FILL_BLANK', '패턴 `{2,4}`의 의미를 쓰시오.', '2~4회 반복', '자주 쓰는 패턴 몇 가지로 대부분의 형식 검증이 가능합니다.', 'GOLD', NULL, 1),
  (240507, @java_subject_id, 24, 2405, 'CODE_SHORT', 'input이 숫자로만 이루어졌는지 검사해 valid에 저장하는 문장 한 줄을 작성하시오.', '`boolean valid = input.matches("\\\\d+");`', '숫자 형식 검증의 기본형입니다.', 'GOLD', NULL, 1),
  (240508, @java_subject_id, 24, 2405, 'CODE_OUTPUT', '`"abc".matches("[a-z]+")`의 결과를 쓰시오.', 'true', '소문자만으로 이루어져 전체 일치합니다.', 'GOLD', NULL, 1),
  (240509, @java_subject_id, 24, 2405, 'CODE_OUTPUT', '복잡한 업무 규칙을 정규표현식 하나에 몰아넣는 것보다 권장되는 방법을 쓰시오.', '길이 검사 같은 단순 조건과 나누어 검증한다', '읽기 쉬운 검증 코드가 됩니다.', 'GOLD', NULL, 1),
  (240510, @java_subject_id, 24, 2405, 'CODE_SHORT', 'input이 정확히 3자리 숫자인지 검사해 ok에 저장하는 문장 한 줄을 작성하시오.', '`boolean ok = input.matches("\\\\d{3}");`', '자릿수 반복 패턴입니다.', 'GOLD', NULL, 1),
  (240601, @java_subject_id, 24, 2406, 'MULTIPLE_CHOICE', 'Optional의 목적으로 옳은 것은?', '값이 없을 수 있음을 타입으로 표현한다', '무조건 get보다 orElse 계열로 값 없음을 처리합니다.', 'GOLD', NULL, 1),
  (240602, @java_subject_id, 24, 2406, 'MULTIPLE_CHOICE', '다음 코드의 출력 결과는?
```java
Optional<String> value = Optional.empty();
System.out.println(value.orElse("없음"));
```', '없음', '값이 없으므로 기본값이 반환됩니다.', 'GOLD', NULL, 1),
  (240603, @java_subject_id, 24, 2406, 'MULTIPLE_CHOICE', '기본값 처리의 빈칸을 채우면?
```java
value.____("없음")
```', 'orElse', 'orElse가 값 없음의 기본값을 제공합니다.', 'GOLD', NULL, 1),
  (240604, @java_subject_id, 24, 2406, 'MULTIPLE_CHOICE', '값이 없는 Optional에 get을 호출하면?', '예외 발생', '확인 없는 get은 피해야 합니다.', 'GOLD', NULL, 1),
  (240605, @java_subject_id, 24, 2406, 'CODE_OUTPUT', '`Optional.of("A").orElse("없음")`의 결과를 쓰시오.', 'A', '값이 있으면 그 값이 반환됩니다.', 'GOLD', NULL, 1),
  (240606, @java_subject_id, 24, 2406, 'FILL_BLANK', '빈 Optional을 만드는 빈칸을 채우시오.
```java
Optional<String> value = Optional.____();
```', 'empty', '값 없음을 표현하는 객체입니다.', 'GOLD', NULL, 1),
  (240607, @java_subject_id, 24, 2406, 'CODE_SHORT', 'value의 값을 기본값 "없음"으로 출력하는 문장 한 줄을 작성하시오.', '`System.out.println(value.orElse("없음"));`', '값 없음의 안전한 처리입니다.', 'GOLD', NULL, 1),
  (240608, @java_subject_id, 24, 2406, 'CODE_OUTPUT', 'Optional을 주로 사용하는 위치(관례)를 쓰시오.', '메서드 반환형', '필드나 매개변수 타입으로는 쓰지 않는 것이 일반적입니다.', 'GOLD', NULL, 1),
  (240609, @java_subject_id, 24, 2406, 'CODE_OUTPUT', 'isPresent()의 반환 자료형을 쓰시오.', 'boolean', '값 존재 여부를 알려 줍니다.', 'GOLD', NULL, 1),
  (240610, @java_subject_id, 24, 2406, 'CODE_SHORT', '"Java"를 담은 Optional을 value에 생성하는 문장 한 줄을 작성하시오.', '`Optional<String> value = Optional.of("Java");`', '값이 있는 Optional 생성입니다.', 'GOLD', NULL, 1),
  (240701, @java_subject_id, 24, 2407, 'MULTIPLE_CHOICE', 'BigDecimal을 문자열 생성자로 만들어야 하는 이유는?', 'double을 직접 전달하면 이미 오차가 포함된 값이 저장되기 때문', '정확한 십진 계산이 목적이므로 "0.1"처럼 문자열로 생성합니다.', 'GOLD', NULL, 1),
  (240702, @java_subject_id, 24, 2407, 'MULTIPLE_CHOICE', '다음 코드의 출력 결과는?
```java
BigDecimal total = new BigDecimal("0.1").add(new BigDecimal("0.2"));
System.out.println(total);
```', '0.3', '십진 기반 계산이라 정확히 0.3입니다.', 'GOLD', NULL, 1),
  (240703, @java_subject_id, 24, 2407, 'MULTIPLE_CHOICE', '덧셈 메서드의 빈칸을 채우면?
```java
new BigDecimal("0.1").____(new BigDecimal("0.2"))
```', 'add', 'BigDecimal 연산은 add·subtract·multiply 등 메서드로 합니다.', 'GOLD', NULL, 1),
  (240704, @java_subject_id, 24, 2407, 'MULTIPLE_CHOICE', 'equals로 0.10과 0.1을 비교하면?', '다르다고 판단', 'equals는 값과 소수 자릿수까지 비교합니다.', 'GOLD', NULL, 1),
  (240705, @java_subject_id, 24, 2407, 'CODE_OUTPUT', 'compareTo로 0.10과 0.1을 비교한 결과(정수)를 쓰시오.', '0(같음)', '크기 비교에는 compareTo가 적절합니다.', 'GOLD', NULL, 1),
  (240706, @java_subject_id, 24, 2407, 'FILL_BLANK', '정확한 0.1이 저장되도록 빈칸을 채우시오.
```java
new BigDecimal(____)
```', '"0.1"', '문자열 생성자를 사용합니다.', 'GOLD', NULL, 1),
  (240707, @java_subject_id, 24, 2407, 'CODE_SHORT', '0.1과 0.2를 정확히 더해 total에 저장하는 문장 한 줄을 작성하시오.', '`BigDecimal total = new BigDecimal("0.1").add(new BigDecimal("0.2"));`', '금액 계산의 기본형입니다.', 'GOLD', NULL, 1),
  (240708, @java_subject_id, 24, 2407, 'CODE_OUTPUT', '`new BigDecimal(0.1)`처럼 double을 직접 전달하면 어떤 문제가 있는지 쓰시오.', '이진 오차가 포함된 값이 그대로 저장된다', '반드시 문자열로 생성합니다.', 'GOLD', NULL, 1),
  (240709, @java_subject_id, 24, 2407, 'CODE_OUTPUT', 'BigDecimal의 크기 비교에 권장되는 메서드를 쓰시오.', 'compareTo', 'equals는 자릿수까지 따지므로 크기 비교에 부적합할 수 있습니다.', 'GOLD', NULL, 1),
  (240710, @java_subject_id, 24, 2407, 'CODE_SHORT', '1000원짜리 3개의 금액을 price에 계산하는 문장 한 줄을 작성하시오.', '`BigDecimal price = new BigDecimal("1000").multiply(new BigDecimal("3"));`', '곱셈도 메서드로 수행합니다.', 'GOLD', NULL, 1),
  (240801, @java_subject_id, 24, 2408, 'MULTIPLE_CHOICE', 'Math의 round·floor·ceil의 의미를 순서대로 옳게 나열한 것은?', '반올림·내림·올림', 'round는 반올림, floor는 내림, ceil은 올림입니다.', 'GOLD', NULL, 1),
  (240802, @java_subject_id, 24, 2408, 'MULTIPLE_CHOICE', '다음 코드의 출력 결과는?
```java
System.out.println(Math.max(10, 20));
System.out.println(Math.abs(-5));
```', '20과 5', '최댓값 20, 절댓값 5입니다.', 'GOLD', NULL, 1),
  (240803, @java_subject_id, 24, 2408, 'MULTIPLE_CHOICE', '두 값 중 큰 값을 구하는 빈칸을 채우면?
```java
Math.____(10, 20)
```', 'max', 'Math.max가 큰 값을 반환합니다.', 'GOLD', NULL, 1),
  (240804, @java_subject_id, 24, 2408, 'MULTIPLE_CHOICE', '`new Random().nextInt(6)`이 만드는 난수의 범위는?', '0~5', '0부터 5까지이므로 주사위라면 1을 더해야 합니다.', 'GOLD', NULL, 1),
  (240805, @java_subject_id, 24, 2408, 'CODE_OUTPUT', '`Math.abs(-5)`의 결과를 쓰시오.', '5', '절댓값입니다.', 'GOLD', NULL, 1),
  (240806, @java_subject_id, 24, 2408, 'FILL_BLANK', '절댓값 메서드의 빈칸을 채우시오.
```java
Math.____(-5)
```', 'abs', 'absolute value의 약자입니다.', 'GOLD', NULL, 1),
  (240807, @java_subject_id, 24, 2408, 'CODE_SHORT', '1~6 주사위 값을 dice에 저장하는 문장 한 줄을 작성하시오.', '`int dice = new Random().nextInt(6) + 1;`', '0~5에 1을 더해 1~6을 만듭니다.', 'GOLD', NULL, 1),
  (240808, @java_subject_id, 24, 2408, 'CODE_OUTPUT', '`Math.min(3, 7)`의 결과를 쓰시오.', '3', '작은 값을 반환합니다.', 'GOLD', NULL, 1),
  (240809, @java_subject_id, 24, 2408, 'CODE_OUTPUT', 'nextInt(6)만으로 주사위가 안 되는 이유를 쓰시오.', '결과가 0~5라서 1을 더해야 한다', '범위 계산의 1 차이 실수를 조심합니다.', 'GOLD', NULL, 1),
  (240810, @java_subject_id, 24, 2408, 'CODE_SHORT', '16의 제곱근을 root에 저장하는 문장 한 줄을 작성하시오.', '`double root = Math.sqrt(16);`', '결과는 4.0입니다.', 'GOLD', NULL, 1),
  (240901, @java_subject_id, 24, 2409, 'MULTIPLE_CHOICE', 'Objects.equals의 장점은?', '두 값이 모두 null이어도 예외 없이 비교할 수 있다', 'a.equals(b)에서 a가 null이면 예외가 나는 문제를 피합니다.', 'GOLD', NULL, 1),
  (240902, @java_subject_id, 24, 2409, 'MULTIPLE_CHOICE', '다음 코드의 출력 결과는?
```java
System.out.println(Objects.equals(null, null));
System.out.println(Objects.equals("A", null));
```', 'true와 false', 'null끼리는 같고, 값과 null은 다릅니다.', 'GOLD', NULL, 1),
  (240903, @java_subject_id, 24, 2409, 'MULTIPLE_CHOICE', 'null 안전 비교의 빈칸을 채우면?
```java
Objects.____(a, b)
```', 'equals', 'Objects.equals가 null 안전 비교입니다.', 'GOLD', NULL, 1),
  (240904, @java_subject_id, 24, 2409, 'MULTIPLE_CHOICE', 'a가 null일 때 `a.equals(b)`를 호출하면?', 'NullPointerException 발생', 'null에 메서드를 호출할 수 없습니다.', 'GOLD', NULL, 1),
  (240905, @java_subject_id, 24, 2409, 'CODE_OUTPUT', '`Objects.equals("A", "A")`의 결과를 쓰시오.', 'true', '내용이 같은 두 문자열입니다.', 'GOLD', NULL, 1),
  (240906, @java_subject_id, 24, 2409, 'FILL_BLANK', '필수값 검사의 빈칸을 채우시오.
```java
Objects.____(name)
```', 'requireNonNull', 'null이면 즉시 예외를 발생시킵니다.', 'GOLD', NULL, 1),
  (240907, @java_subject_id, 24, 2409, 'CODE_SHORT', 'a와 b를 null 안전하게 비교해 same에 저장하는 문장 한 줄을 작성하시오.', '`boolean same = Objects.equals(a, b);`', 'null 걱정 없는 비교입니다.', 'GOLD', NULL, 1),
  (240908, @java_subject_id, 24, 2409, 'CODE_OUTPUT', '`Objects.requireNonNull(null)`을 호출하면 어떻게 되는지 쓰시오.', '즉시 예외가 발생한다', '필수값 위반을 일찍 알립니다.', 'GOLD', NULL, 1),
  (240909, @java_subject_id, 24, 2409, 'CODE_OUTPUT', '잘못된 값을 ''일찍 실패''시키는 것의 장점을 쓰시오.', '원인 추적이 쉬워진다', '늦게 발견할수록 추적이 어렵습니다.', 'GOLD', NULL, 1),
  (240910, @java_subject_id, 24, 2409, 'CODE_SHORT', '생성자에서 name 필수값을 검사하며 필드에 저장하는 문장 한 줄을 작성하시오.', '`this.name = Objects.requireNonNull(name);`', '생성 시점 검증 패턴입니다.', 'GOLD', NULL, 1),
  (241001, @java_subject_id, 24, 2410, 'MULTIPLE_CHOICE', '사용자 입력 처리의 권장 원칙은?', '적합한 전용 타입으로 변환한 뒤 검증하고, 표시 형식과 내부 계산 타입을 분리한다', '화면 표시와 내부 계산을 분리하면 오류가 줄어듭니다.', 'GOLD', NULL, 1),
  (241002, @java_subject_id, 24, 2410, 'MULTIPLE_CHOICE', '다음 코드의 출력 결과는?
```java
LocalDate start = LocalDate.of(2026, 7, 23);
LocalDate end = start.plusDays(7);
long days = ChronoUnit.DAYS.between(start, end);
System.out.println(days);
```', '7', '7일 차이가 계산됩니다.', 'GOLD', NULL, 1),
  (241003, @java_subject_id, 24, 2410, 'MULTIPLE_CHOICE', '일수 계산의 빈칸을 채우면?
```java
ChronoUnit.____.between(start, end)
```', 'DAYS', 'ChronoUnit.DAYS가 일 단위 차이입니다.', 'GOLD', NULL, 1),
  (241004, @java_subject_id, 24, 2410, 'MULTIPLE_CHOICE', 'between의 날짜 포함 기준은?', '시작 포함, 종료 제외', 'D-day 계산 시 요구사항과 맞는지 확인해야 합니다.', 'GOLD', NULL, 1),
  (241005, @java_subject_id, 24, 2410, 'CODE_OUTPUT', '같은 날짜끼리 between을 계산한 결과를 쓰시오.', '0', '차이가 없습니다.', 'GOLD', NULL, 1),
  (241006, @java_subject_id, 24, 2410, 'FILL_BLANK', '시작일에서 7일 뒤를 구하도록 빈칸을 채우시오.
```java
LocalDate end = start.____(7);
```', 'plusDays', '새 객체가 반환됩니다.', 'GOLD', NULL, 1),
  (241007, @java_subject_id, 24, 2410, 'CODE_SHORT', 'start와 end 사이의 일수를 days에 저장하는 문장 한 줄을 작성하시오.', '`long days = ChronoUnit.DAYS.between(start, end);`', '기간 계산의 기본형입니다.', 'GOLD', NULL, 1),
  (241008, @java_subject_id, 24, 2410, 'CODE_OUTPUT', '금액 입력을 변환할 전용 타입을 쓰시오.', 'BigDecimal', '정확한 십진 계산이 필요한 값입니다.', 'GOLD', NULL, 1),
  (241009, @java_subject_id, 24, 2410, 'CODE_OUTPUT', '날짜 입력을 변환할 전용 타입을 쓰시오.', 'LocalDate', '문자열 그대로 두지 않고 전용 타입으로 다룹니다.', 'GOLD', NULL, 1),
  (241010, @java_subject_id, 24, 2410, 'CODE_SHORT', 'start의 7일 뒤 날짜를 end에 저장하는 문장 한 줄을 작성하시오.', '`LocalDate end = start.plusDays(7);`', '불변 객체 계산 결과를 변수에 받습니다.', 'GOLD', NULL, 1),
  (250101, @java_subject_id, 25, 2501, 'MULTIPLE_CHOICE', '요구사항 분석에서 구현 전에 정리해야 할 것으로 옳은 묶음은?', '기능·입력값·출력 결과·예외 상황', '기능을 등록·조회·수정·삭제처럼 작은 단위로 나누고 성공 조건을 명확히 합니다.', 'GOLD', NULL, 1),
  (250102, @java_subject_id, 25, 2501, 'MULTIPLE_CHOICE', '다음 코드의 출력 결과는?
```java
record Requirement(String input, String output) { }
Requirement requirement = new Requirement("점수 0~100", "통과 여부");
System.out.println(requirement.output());
```', '통과 여부', 'record는 필드 이름과 같은 조회 메서드를 제공합니다.', 'GOLD', NULL, 1),
  (250103, @java_subject_id, 25, 2501, 'MULTIPLE_CHOICE', 'record 선언의 빈칸을 채우면?
```java
record Requirement(String input, String ____) { }
```', 'output', '구성 요소 이름을 선언합니다.', 'GOLD', NULL, 1),
  (250104, @java_subject_id, 25, 2501, 'MULTIPLE_CHOICE', 'record의 필드 값을 읽는 방법은?', 'output() 같은 이름 메서드', '필드 이름 그대로의 메서드가 자동 생성됩니다.', 'GOLD', NULL, 1),
  (250105, @java_subject_id, 25, 2501, 'CODE_OUTPUT', '`new Requirement("a", "b").input()`의 결과를 쓰시오.', 'a', '첫 구성 요소가 반환됩니다.', 'GOLD', NULL, 1),
  (250106, @java_subject_id, 25, 2501, 'FILL_BLANK', '빈칸에 들어갈 키워드를 쓰시오.
```java
____ Requirement(String input, String output) { }
```', 'record', '데이터 보관용 간결한 선언입니다.', 'GOLD', NULL, 1),
  (250107, @java_subject_id, 25, 2501, 'CODE_SHORT', '입력 "점수 0~100", 출력 "통과 여부"의 Requirement를 requirement에 생성하는 문장 한 줄을 작성하시오.', '`Requirement requirement = new Requirement("점수 0~100", "통과 여부");`', '요구사항을 데이터로 정리한 형태입니다.', 'GOLD', NULL, 1),
  (250108, @java_subject_id, 25, 2501, 'CODE_OUTPUT', '경계값과 잘못된 입력을 미리 정하면 얻는 효과를 쓰시오.', '구현 중 조건이 빠지는 일을 줄일 수 있다', '분석 단계의 핵심 목적입니다.', 'GOLD', NULL, 1),
  (250109, @java_subject_id, 25, 2501, 'CODE_OUTPUT', '기능을 나누는 대표적인 작은 단위 네 가지를 쓰시오.', '등록·조회·수정·삭제', '각 기능의 성공 조건을 명확하게 작성합니다.', 'GOLD', NULL, 1),
  (250110, @java_subject_id, 25, 2501, 'CODE_SHORT', 'requirement의 output을 출력하는 문장 한 줄을 작성하시오.', '`System.out.println(requirement.output());`', 'record 조회 메서드 호출입니다.', 'GOLD', NULL, 1),
  (250201, @java_subject_id, 25, 2502, 'MULTIPLE_CHOICE', '도메인 모델링의 기준으로 옳은 것은?', '실제 업무 개념을 기준으로 클래스를 나눈다', '학생·강의·주문 같은 개념 단위로 상태와 책임을 정합니다.', 'GOLD', NULL, 1),
  (250202, @java_subject_id, 25, 2502, 'MULTIPLE_CHOICE', '다음 코드의 출력 결과는?
```java
class Course {
    private final String title;
    Course(String title) { this.title = title; }
    String title() { return title; }
}
System.out.println(new Course("Java").title());
```', 'Java', '생성 시 저장한 제목이 반환됩니다.', 'GOLD', NULL, 1),
  (250203, @java_subject_id, 25, 2502, 'MULTIPLE_CHOICE', '생성 후 제목이 바뀌지 않도록 빈칸을 채우면?
```java
private ____ String title;
```', 'final', 'final 필드는 재대입할 수 없습니다.', 'GOLD', NULL, 1),
  (250204, @java_subject_id, 25, 2502, 'MULTIPLE_CHOICE', 'final 필드 title에 새 값을 대입하면?', '컴파일 오류', '불변 상태로 유지됩니다.', 'GOLD', NULL, 1),
  (250205, @java_subject_id, 25, 2502, 'CODE_OUTPUT', '2번 코드에서 title() 메서드가 반환하는 것을 쓰시오.', '저장된 제목(title 필드 값)', '읽기 전용 조회 메서드입니다.', 'GOLD', NULL, 1),
  (250206, @java_subject_id, 25, 2502, 'FILL_BLANK', '필드에 저장되도록 빈칸을 채우시오.
```java
Course(String title) { ____.title = title; }
```', 'this', '필드와 매개변수를 구분합니다.', 'GOLD', NULL, 1),
  (250207, @java_subject_id, 25, 2502, 'CODE_SHORT', 'title 필드를 반환하는 title() 메서드를 한 줄로 작성하시오.', '`String title() { return title; }`', '상태 조회 동작입니다.', 'GOLD', NULL, 1),
  (250208, @java_subject_id, 25, 2502, 'CODE_OUTPUT', '화면의 모든 값을 한 클래스에 넣는 설계가 바람직하지 않은 이유를 쓰시오.', '업무 개념이 뒤섞여 변경하기 어려워진다', '개념 기준 분리가 원칙입니다.', 'GOLD', NULL, 1),
  (250209, @java_subject_id, 25, 2502, 'CODE_OUTPUT', '모델링에서 각 클래스에 대해 정해야 할 세 가지를 쓰시오.', '가져야 할 상태, 책임(동작), 객체 사이의 관계', '도메인 모델의 구성 요소입니다.', 'GOLD', NULL, 1),
  (250210, @java_subject_id, 25, 2502, 'CODE_SHORT', '"Java" 제목의 Course를 만들어 제목을 출력하는 문장 한 줄을 작성하시오.', '`System.out.println(new Course("Java").title());`', '생성과 조회를 결합한 문장입니다.', 'GOLD', NULL, 1),
  (250301, @java_subject_id, 25, 2503, 'MULTIPLE_CHOICE', '콘솔 메뉴 프로그램의 기본 구조는?', '반복문으로 메뉴를 계속 표시하고 switch로 선택 기능을 실행', '종료 번호 선택 시 반복을 끝냅니다.', 'GOLD', NULL, 1),
  (250302, @java_subject_id, 25, 2503, 'MULTIPLE_CHOICE', '다음 코드의 출력 결과는?
```java
int menu = 2;
switch (menu) {
    case 1 -> System.out.println("등록");
    case 2 -> System.out.println("조회");
    default -> System.out.println("잘못된 메뉴");
}
```', '조회', 'menu 값 2와 일치하는 case가 실행됩니다.', 'GOLD', NULL, 1),
  (250303, @java_subject_id, 25, 2503, 'MULTIPLE_CHOICE', '범위 밖 입력 처리의 빈칸을 채우면?
```java
____ -> System.out.println("잘못된 메뉴");
```', 'default', 'default가 안내 메시지를 담당합니다.', 'GOLD', NULL, 1),
  (250304, @java_subject_id, 25, 2503, 'MULTIPLE_CHOICE', 'menu가 7이면 위 코드의 출력은?', '잘못된 메뉴', '일치하는 case가 없어 default가 실행됩니다.', 'GOLD', NULL, 1),
  (250305, @java_subject_id, 25, 2503, 'CODE_OUTPUT', 'menu가 1일 때 2번 코드의 출력을 쓰시오.', '등록', 'case 1이 실행됩니다.', 'GOLD', NULL, 1),
  (250306, @java_subject_id, 25, 2503, 'FILL_BLANK', '비교할 값이 들어가도록 빈칸을 채우시오.
```java
switch (____) { case 1 -> System.out.println("등록"); }
```', 'menu', '선택 값을 switch에 전달합니다.', 'GOLD', NULL, 1),
  (250307, @java_subject_id, 25, 2503, 'CODE_SHORT', 'menu가 3이면 "종료"를 출력하는 case를 화살표 문법으로 한 줄 작성하시오.', '`case 3 -> System.out.println("종료");`', '종료 메뉴 분기입니다.', 'GOLD', NULL, 1),
  (250308, @java_subject_id, 25, 2503, 'CODE_OUTPUT', '종료 메뉴를 선택했을 때 반복문은 어떻게 처리해야 하는지 쓰시오.', '반복을 끝낸다(break 등)', '종료 조건을 한곳에 모으면 흐름이 명확합니다.', 'GOLD', NULL, 1),
  (250309, @java_subject_id, 25, 2503, 'CODE_OUTPUT', '잘못된 입력이 들어와도 프로그램이 지켜야 할 동작을 쓰시오.', '죽지 않고 안내 후 다시 메뉴를 보여 준다', 'default 분기가 안내를 담당합니다.', 'GOLD', NULL, 1),
  (250310, @java_subject_id, 25, 2503, 'CODE_SHORT', 'while(true) 메뉴 반복에서 menu가 0이면 반복을 끝내는 if문을 한 줄로 작성하시오.', '`if (menu == 0) { break; }`', '종료 조건 처리입니다.', 'GOLD', NULL, 1),
  (250401, @java_subject_id, 25, 2504, 'MULTIPLE_CHOICE', '고유 번호로 빠르게 찾아야 하는 메모리 저장소에 적합한 구조는?', 'Map', '입력 순서 목록이 중요하면 List가 적합합니다.', 'GOLD', NULL, 1),
  (250402, @java_subject_id, 25, 2504, 'MULTIPLE_CHOICE', '다음 코드의 출력 결과는?
```java
Map<Long, String> courses = new HashMap<>();
courses.put(1L, "Java");
System.out.println(courses.get(1L));
```', 'Java', '키 1L의 값이 조회됩니다.', 'GOLD', NULL, 1),
  (250403, @java_subject_id, 25, 2504, 'MULTIPLE_CHOICE', '고유 번호 키 타입의 빈칸을 채우면?
```java
Map<____, String> courses = new HashMap<>();
```', 'Long', '제네릭에는 래퍼 타입 Long을 씁니다.', 'GOLD', NULL, 1),
  (250404, @java_subject_id, 25, 2504, 'MULTIPLE_CHOICE', '저장되지 않은 번호로 get을 호출하면?', 'null', '없는 키 조회는 null입니다.', 'GOLD', NULL, 1),
  (250405, @java_subject_id, 25, 2504, 'CODE_OUTPUT', 'courses에 put(2L, "SQL")을 추가하면 size()는 얼마인지 쓰시오.', '2', '서로 다른 키 두 개가 저장됩니다.', 'GOLD', NULL, 1),
  (250406, @java_subject_id, 25, 2504, 'FILL_BLANK', '저장 메서드의 빈칸을 채우시오.
```java
courses.____(1L, "Java");
```', 'put', 'Map 저장은 put입니다.', 'GOLD', NULL, 1),
  (250407, @java_subject_id, 25, 2504, 'CODE_SHORT', '번호 → 강의명 구조의 HashMap을 courses에 생성하는 문장 한 줄을 작성하시오.', '`Map<Long, String> courses = new HashMap<>();`', '메모리 저장소의 기본형입니다.', 'GOLD', NULL, 1),
  (250408, @java_subject_id, 25, 2504, 'CODE_OUTPUT', '메모리 저장소의 한계를 쓰시오.', '프로그램이 끝나면 데이터가 사라진다', '저장소 접근 코드를 한곳에 모으면 나중에 파일·DB로 바꾸기 쉽습니다.', 'GOLD', NULL, 1),
  (250409, @java_subject_id, 25, 2504, 'CODE_OUTPUT', '입력 순서 목록이 중요할 때 적합한 자료구조를 쓰시오.', 'List', '요구사항에 따라 구조를 선택합니다.', 'GOLD', NULL, 1),
  (250410, @java_subject_id, 25, 2504, 'CODE_SHORT', 'courses에 번호 1L로 "Java"를 저장하는 문장 한 줄을 작성하시오.', '`courses.put(1L, "Java");`', '등록 순서에 따라 번호를 1씩 증가시켜 관리할 수 있습니다.', 'GOLD', NULL, 1),
  (250501, @java_subject_id, 25, 2505, 'MULTIPLE_CHOICE', '등록 기능의 올바른 구현 순서는?', '입력값 검증 → 중복 확인 → 객체 생성 → 저장', '검증을 먼저 마친 뒤에만 상태를 변경합니다.', 'GOLD', NULL, 1),
  (250502, @java_subject_id, 25, 2505, 'MULTIPLE_CHOICE', '다음 register에 `register(scores, "", 50)`을 호출한 결과는?
```java
static boolean register(Map<String, Integer> scores, String name, int score) {
    if (name.isBlank() || score < 0 || score > 100 || scores.containsKey(name)) return false;
    scores.put(name, score);
    return true;
}
```', 'false', '빈 이름이 검증에 걸려 저장 없이 false입니다.', 'GOLD', NULL, 1),
  (250503, @java_subject_id, 25, 2505, 'MULTIPLE_CHOICE', '빈 이름 검사의 빈칸을 채우면?
```java
if (name.____() || score < 0 ...) return false;
```', 'isBlank', 'isBlank는 공백뿐인 문자열도 걸러 냅니다.', 'GOLD', NULL, 1),
  (250504, @java_subject_id, 25, 2505, 'MULTIPLE_CHOICE', '이미 등록된 이름으로 register를 호출하면?', 'false가 반환되고 저장되지 않는다', 'containsKey 중복 확인에 걸립니다.', 'GOLD', NULL, 1),
  (250505, @java_subject_id, 25, 2505, 'CODE_OUTPUT', '첫 등록으로 `register(scores, "민수", 85)`를 호출한 반환값을 쓰시오.', 'true', '검증을 모두 통과해 저장됩니다.', 'GOLD', NULL, 1),
  (250506, @java_subject_id, 25, 2505, 'FILL_BLANK', '중복 확인 메서드의 빈칸을 채우시오.
```java
scores.____(name)
```', 'containsKey', '키 존재 여부로 중복을 확인합니다.', 'GOLD', NULL, 1),
  (250507, @java_subject_id, 25, 2505, 'CODE_SHORT', '점수가 0 미만이거나 100 초과인지 검사하는 조건식을 작성하시오.', '`score < 0 || score > 100`', '범위 밖 값을 거르는 조건입니다.', 'GOLD', NULL, 1),
  (250508, @java_subject_id, 25, 2505, 'CODE_OUTPUT', '상태 변경 전에 검증을 모두 마쳐야 하는 이유를 쓰시오.', '실패했는데 일부 데이터만 저장되는 어중간한 상태를 막기 위해', '등록 기능의 핵심 원칙입니다.', 'GOLD', NULL, 1),
  (250509, @java_subject_id, 25, 2505, 'CODE_OUTPUT', '`register(scores, "민수", 101)`의 반환값을 쓰시오.', 'false', '점수 범위 검증에 걸립니다.', 'GOLD', NULL, 1),
  (250510, @java_subject_id, 25, 2505, 'CODE_SHORT', '검증 통과 후 이름과 점수를 저장하는 문장 한 줄을 작성하시오.', '`scores.put(name, score);`', '마지막 단계에서만 상태를 바꿉니다.', 'GOLD', NULL, 1),
  (250601, @java_subject_id, 25, 2506, 'MULTIPLE_CHOICE', '검색 결과가 없는 상황에 대한 올바른 태도는?', '정상 결과 중 하나로 보고 빈 목록이나 Optional로 표현한다', '''결과 없음'' 안내 후 프로그램은 계속 진행합니다.', 'GOLD', NULL, 1),
  (250602, @java_subject_id, 25, 2506, 'MULTIPLE_CHOICE', '다음 코드의 출력 결과는?
```java
List<String> result = List.of("Java", "SQL", "JavaScript").stream()
        .filter(title -> title.contains("Java")).toList();
System.out.println(result);
```', '[Java, JavaScript]', '"Java"를 포함하는 두 제목이 남습니다.', 'GOLD', NULL, 1),
  (250603, @java_subject_id, 25, 2506, 'MULTIPLE_CHOICE', '부분 일치 검색의 빈칸을 채우면?
```java
.filter(title -> title.____("Java"))
```', 'contains', '부분 일치는 contains, 정확한 일치는 equals입니다.', 'GOLD', NULL, 1),
  (250604, @java_subject_id, 25, 2506, 'MULTIPLE_CHOICE', '검색 결과가 없으면 result는?', '빈 목록', '빈 목록도 정상 결과입니다.', 'GOLD', NULL, 1),
  (250605, @java_subject_id, 25, 2506, 'CODE_OUTPUT', '2번 코드에서 "SQL"로 검색하면 결과를 쓰시오.', '[SQL]', 'SQL만 포함 조건을 만족합니다.', 'GOLD', NULL, 1),
  (250606, @java_subject_id, 25, 2506, 'FILL_BLANK', '대소문자 구분 없는 검색을 위해 양쪽에 적용할 메서드를 쓰시오.', 'toLowerCase', '양쪽을 같은 케이스로 맞춰 비교합니다.', 'GOLD', NULL, 1),
  (250607, @java_subject_id, 25, 2506, 'CODE_SHORT', 'titles에서 keyword를 포함하는 제목만 result에 저장하는 문장 한 줄을 작성하시오.', '`List<String> result = titles.stream().filter(t -> t.contains(keyword)).toList();`', '부분 일치 검색의 기본형입니다.', 'GOLD', NULL, 1),
  (250608, @java_subject_id, 25, 2506, 'CODE_OUTPUT', '조회 기능에서 구분해 구현해야 할 두 가지를 쓰시오.', '전체 목록 조회와 조건 검색', '요구사항이 다른 두 기능입니다.', 'GOLD', NULL, 1),
  (250609, @java_subject_id, 25, 2506, 'CODE_OUTPUT', '"python"으로 검색했을 때 결과와 사용자 처리 방법을 쓰시오.', '빈 목록이 나오며 "결과 없음"을 안내한다', '오류가 아닌 정상 흐름입니다.', 'GOLD', NULL, 1),
  (250610, @java_subject_id, 25, 2506, 'CODE_SHORT', 'result가 비어 있으면 "결과 없음"을 출력하는 if문을 한 줄로 작성하시오.', '`if (result.isEmpty()) { System.out.println("결과 없음"); }`', '빈 결과 안내 처리입니다.', 'GOLD', NULL, 1),
  (250701, @java_subject_id, 25, 2507, 'MULTIPLE_CHOICE', '수정·삭제 전에 먼저 확인해야 할 것은?', '대상의 존재 여부와 권한', '수정 가능한 값만 변경하고, 삭제의 영향도 함께 고려합니다.', 'GOLD', NULL, 1),
  (250702, @java_subject_id, 25, 2507, 'MULTIPLE_CHOICE', '다음 코드의 출력 결과는?
```java
Map<Long, String> courses = new HashMap<>();
courses.put(1L, "Java 기초");
courses.replace(1L, "Java 심화");
System.out.println(courses.get(1L));
```', 'Java 심화', 'replace가 기존 키의 값을 교체합니다.', 'GOLD', NULL, 1),
  (250703, @java_subject_id, 25, 2507, 'MULTIPLE_CHOICE', '값 교체 메서드의 빈칸을 채우면?
```java
courses.____(1L, "Java 심화");
```', 'replace', 'Map의 교체는 replace입니다.', 'GOLD', NULL, 1),
  (250704, @java_subject_id, 25, 2507, 'MULTIPLE_CHOICE', '존재하지 않는 키로 replace를 호출하면?', '아무 변경 없이 null이 반환된다', '기존 키가 있을 때만 교체됩니다.', 'GOLD', NULL, 1),
  (250705, @java_subject_id, 25, 2507, 'CODE_OUTPUT', 'remove(1L) 호출 후 get(1L)의 결과를 쓰시오.', 'null', '삭제된 키는 조회되지 않습니다.', 'GOLD', NULL, 1),
  (250706, @java_subject_id, 25, 2507, 'FILL_BLANK', '삭제 메서드의 빈칸을 채우시오.
```java
courses.____(1L);
```', 'remove', '키로 항목을 삭제합니다.', 'GOLD', NULL, 1),
  (250707, @java_subject_id, 25, 2507, 'CODE_SHORT', 'id가 존재할 때만 삭제하는 if문을 한 줄로 작성하시오.', '`if (courses.containsKey(id)) { courses.remove(id); }`', '존재 확인 후 삭제 패턴입니다.', 'GOLD', NULL, 1),
  (250708, @java_subject_id, 25, 2507, 'CODE_OUTPUT', '삭제 전에 한 번 더 확인하는 절차를 두는 이유를 쓰시오.', '삭제는 되돌리기 어렵기 때문', '콘솔에서도 재확인 안내가 유용합니다.', 'GOLD', NULL, 1),
  (250709, @java_subject_id, 25, 2507, 'CODE_OUTPUT', '삭제 시 연관 데이터에 대해 결정해야 할 것을 쓰시오.', '함께 정리할지 여부', '삭제가 다른 데이터에 미치는 영향을 고려합니다.', 'GOLD', NULL, 1),
  (250710, @java_subject_id, 25, 2507, 'CODE_SHORT', 'id의 강의명을 "Java 심화"로 수정하는 문장 한 줄을 작성하시오.', '`courses.replace(id, "Java 심화");`', '존재 확인 후 호출하면 안전합니다.', 'GOLD', NULL, 1),
  (250801, @java_subject_id, 25, 2508, 'MULTIPLE_CHOICE', '파일 저장·복원에서 지켜야 할 원칙은?', '한 줄의 필드 구분 규칙과 인코딩을 정하고 양쪽에서 같은 규칙 사용', '저장 형식은 단순할수록 복원 코드도 단순해집니다.', 'GOLD', NULL, 1),
  (250802, @java_subject_id, 25, 2508, 'MULTIPLE_CHOICE', '다음 코드를 실행하면?
```java
List<String> lines = List.of("1,Java", "2,SQL");
Files.write(Path.of("courses.csv"), lines, StandardCharsets.UTF_8);
```', 'courses.csv 파일이 만들어져 두 줄이 저장된다', '줄 목록이 파일로 기록됩니다.', 'GOLD', NULL, 1),
  (250803, @java_subject_id, 25, 2508, 'MULTIPLE_CHOICE', 'UTF-8 인코딩 지정의 빈칸을 채우면?
```java
Files.write(Path.of("courses.csv"), lines, StandardCharsets.____);
```', 'UTF_8', 'StandardCharsets.UTF_8 상수를 사용합니다.', 'GOLD', NULL, 1),
  (250804, @java_subject_id, 25, 2508, 'MULTIPLE_CHOICE', '쉼표 구분 형식에서 데이터 안에 쉼표가 들어오면?', '필드 구분이 깨질 수 있어 처리 규칙이 필요하다', '구분자 충돌은 형식 설계에서 고려할 문제입니다.', 'GOLD', NULL, 1),
  (250805, @java_subject_id, 25, 2508, 'CODE_OUTPUT', '프로그램 시작 시 저장 파일이 없으면 어떻게 처리하는지 쓰시오.', '빈 저장소로 시작한다', '종료나 변경 시점에 저장합니다.', 'GOLD', NULL, 1),
  (250806, @java_subject_id, 25, 2508, 'FILL_BLANK', '줄 목록을 만들도록 빈칸을 채우시오.
```java
List<String> lines = List.____("1,Java", "2,SQL");
```', 'of', '저장할 줄들을 목록으로 준비합니다.', 'GOLD', NULL, 1),
  (250807, @java_subject_id, 25, 2508, 'CODE_SHORT', 'lines를 courses.csv에 UTF-8로 저장하는 문장 한 줄을 작성하시오.', '`Files.write(Path.of("courses.csv"), lines, StandardCharsets.UTF_8);`', '파일 영속화의 기본형입니다.', 'GOLD', NULL, 1),
  (250808, @java_subject_id, 25, 2508, 'CODE_OUTPUT', '파일 읽기·쓰기에서 공통으로 처리해야 할 예외를 쓰시오.', 'IOException', '외부 환경 실패 가능성이 있는 작업입니다.', 'GOLD', NULL, 1),
  (250809, @java_subject_id, 25, 2508, 'CODE_OUTPUT', '"1,Java" 한 줄을 번호와 제목으로 분리하는 방법을 쓰시오.', '쉼표로 split한다', '저장 규칙 그대로 복원합니다.', 'GOLD', NULL, 1),
  (250810, @java_subject_id, 25, 2508, 'CODE_SHORT', 'line을 쉼표로 분리해 parts 배열에 저장하는 문장 한 줄을 작성하시오.', '`String[] parts = line.split(",");`', '복원 단계의 분리 코드입니다.', 'GOLD', NULL, 1),
  (250901, @java_subject_id, 25, 2509, 'MULTIPLE_CHOICE', '테스트에서 확인해야 할 입력으로 옳은 묶음은?', '정상값과 빈 값·최솟값·최댓값·중복값·존재하지 않는 대상', '경계값·예외 상황까지 검증해야 합니다.', 'GOLD', NULL, 1),
  (250902, @java_subject_id, 25, 2509, 'MULTIPLE_CHOICE', '다음 코드의 출력 결과는?
```java
static boolean isValidScore(int score) { return score >= 0 && score <= 100; }
System.out.println(isValidScore(0));
System.out.println(isValidScore(101));
```', 'true와 false', '0은 경계값으로 유효, 101은 범위 밖입니다.', 'GOLD', NULL, 1),
  (250903, @java_subject_id, 25, 2509, 'MULTIPLE_CHOICE', '범위 검증 조건의 빈칸을 채우면?
```java
return score >= 0 ____ score <= 100;
```', '&&', '두 조건을 동시에 만족해야 유효합니다.', 'GOLD', NULL, 1),
  (250904, @java_subject_id, 25, 2509, 'MULTIPLE_CHOICE', '`isValidScore(100)`의 결과는?', 'true', '최댓값 경계 100은 포함됩니다.', 'GOLD', NULL, 1),
  (250905, @java_subject_id, 25, 2509, 'CODE_OUTPUT', '`isValidScore(-1)`의 결과를 쓰시오.', 'false', '최솟값 경계 밖입니다.', 'GOLD', NULL, 1),
  (250906, @java_subject_id, 25, 2509, 'FILL_BLANK', '반환형이 들어가도록 빈칸을 채우시오.
```java
static ____ isValidScore(int score) { return score >= 0 && score <= 100; }
```', 'boolean', '검증 결과는 참·거짓입니다.', 'GOLD', NULL, 1),
  (250907, @java_subject_id, 25, 2509, 'CODE_SHORT', 'isValidScore 메서드 전체를 한 줄로 작성하시오.', '`static boolean isValidScore(int score) { return score >= 0 && score <= 100; }`', '경계 포함 범위 검증입니다.', 'GOLD', NULL, 1),
  (250908, @java_subject_id, 25, 2509, 'CODE_OUTPUT', '버그 수정 후 원래 문제 외에 추가로 확인해야 할 것을 쓰시오.', '기존에 되던 기능이 깨지지 않았는지', '확인용 입력 목록을 만들어 두면 반복 검증이 빨라집니다.', 'GOLD', NULL, 1),
  (250909, @java_subject_id, 25, 2509, 'CODE_OUTPUT', '가장 기본적인 디버깅 방법과 원인 위치 찾는 요령을 쓰시오.', '출력문으로 중간값을 확인하고, 예상값과 실제값이 갈라지는 지점 직전 코드를 본다', '갈라지는 지점이 단서입니다.', 'GOLD', NULL, 1),
  (250910, @java_subject_id, 25, 2509, 'CODE_SHORT', '경계값 0의 검증 결과를 출력하는 문장 한 줄을 작성하시오.', '`System.out.println(isValidScore(0));`', '경계값 테스트 실행입니다.', 'GOLD', NULL, 1),
  (251001, @java_subject_id, 25, 2510, 'MULTIPLE_CHOICE', '최종 프로젝트에서 나누어야 할 역할로 옳은 묶음은?', '요구사항·모델·저장소·서비스·입출력', '한 메서드에 모든 기능을 넣지 않고 기능별로 분리합니다.', 'GOLD', NULL, 1),
  (251002, @java_subject_id, 25, 2510, 'MULTIPLE_CHOICE', 'register가 정상 등록한 뒤 다음 코드의 출력 결과는?
```java
Map<String, Integer> scores = new HashMap<>();
register(scores, "민수", 85);
double average = scores.values().stream().mapToInt(Integer::intValue).average().orElse(0);
System.out.println(scores + ", 평균=" + average);
```', '{민수=85}, 평균=85.0', '등록된 한 명의 평균은 85.0입니다.', 'GOLD', NULL, 1),
  (251003, @java_subject_id, 25, 2510, 'MULTIPLE_CHOICE', '평균 계산 파이프라인의 빈칸을 채우면?
```java
scores.values().stream().____(Integer::intValue).average()
```', 'mapToInt', '숫자 전용 스트림으로 변환해야 average를 쓸 수 있습니다.', 'GOLD', NULL, 1),
  (251004, @java_subject_id, 25, 2510, 'MULTIPLE_CHOICE', '프로젝트 개발의 권장 순서는?', '등록·조회 같은 핵심 흐름을 먼저 만들고 검증·수정·파일 저장을 단계적으로 추가', '핵심 흐름 우선, 단계적 확장이 원칙입니다.', 'GOLD', NULL, 1),
  (251005, @java_subject_id, 25, 2510, 'CODE_OUTPUT', '빈 저장소에서 `average().orElse(0)`의 결과를 쓰시오.', '0.0', '데이터가 없으면 기본값이 사용됩니다.', 'GOLD', NULL, 1),
  (251006, @java_subject_id, 25, 2510, 'FILL_BLANK', '빈 데이터 기본값 처리의 빈칸을 채우시오.
```java
.average().____(0);
```', 'orElse', 'OptionalDouble의 기본값 처리입니다.', 'GOLD', NULL, 1),
  (251007, @java_subject_id, 25, 2510, 'CODE_SHORT', 'scores 값들의 평균을 average에 저장하는 문장 한 줄을 작성하시오.', '`double average = scores.values().stream().mapToInt(Integer::intValue).average().orElse(0);`', '저장소 통계 계산입니다.', 'GOLD', NULL, 1),
  (251008, @java_subject_id, 25, 2510, 'CODE_OUTPUT', '한 메서드에 모든 기능을 넣는 설계를 피해야 하는 이유를 쓰시오.', '기능별로 읽고 테스트할 수 없게 되기 때문', '기능별 분리가 유지보수의 기본입니다.', 'GOLD', NULL, 1),
  (251009, @java_subject_id, 25, 2510, 'CODE_OUTPUT', '각 기능마다 준비해 두어야 할 실행 예제 두 종류를 쓰시오.', '정상 예제와 실패 예제', '실행 결과를 확인하며 완성합니다.', 'GOLD', NULL, 1),
  (251010, @java_subject_id, 25, 2510, 'CODE_SHORT', 'scores에 "민수" 85점을 등록하는 register 호출 문장 한 줄을 작성하시오.', '`register(scores, "민수", 85);`', '핵심 흐름(등록)의 실행문입니다.', 'GOLD', NULL, 1)
ON DUPLICATE KEY UPDATE subject_id = VALUES(subject_id), node_id = VALUES(node_id), lesson_id = VALUES(lesson_id), problem_type = VALUES(problem_type), question = VALUES(question), answer_text = VALUES(answer_text), explanation = VALUES(explanation), difficulty_code = VALUES(difficulty_code), created_by = VALUES(created_by), is_active = VALUES(is_active), updated_at = CURRENT_TIMESTAMP;

-- Replace the sample-data choices completely so obsolete A/B/C/D rows cannot appear alongside authored choices.
DELETE choice_row FROM problem_choices choice_row
JOIN practice_problems problem ON problem.problem_id = choice_row.problem_id
WHERE problem.node_id IN (1, 2, 3, 4, 5, 11, 12, 13, 14, 15, 21, 22, 23, 24, 25);

INSERT INTO problem_choices (problem_id, choice_label, choice_text, is_correct, sort_order)
VALUES
  (10101, '①', '소스 코드를 바이트코드로 컴파일한다', 0, 1),
  (10101, '②', '바이트코드를 실제 컴퓨터에서 실행한다', 1, 2),
  (10101, '③', '소스 코드를 편집하는 도구다', 0, 3),
  (10101, '④', '운영체제를 설치하는 프로그램이다', 0, 4),
  (10102, '①', '소스 작성 → JVM 실행 → 컴파일', 0, 1),
  (10102, '②', '컴파일 → 소스 작성 → JVM 실행', 0, 2),
  (10102, '③', '소스 작성 → 컴파일 → 바이트코드 생성 → JVM 실행', 1, 3),
  (10102, '④', '바이트코드 작성 → 컴파일 → JVM 실행', 0, 4),
  (10103, '①', 'Hello', 1, 1),
  (10103, '②', '"Hello"', 0, 2),
  (10103, '③', '컴파일 오류', 0, 3),
  (10103, '④', '아무것도 출력되지 않는다', 0, 4),
  (10104, '①', 'Hello.exe', 0, 1),
  (10104, '②', 'Hello.class', 1, 2),
  (10104, '③', 'Hello.byte', 0, 3),
  (10104, '④', 'Hello.jvm', 0, 4),
  (10201, '①', '만들어진 프로그램을 실행만 하기 위한 묶음이다', 0, 1),
  (10201, '②', '개발자가 코드를 만들기 위한 도구 묶음으로 컴파일러를 포함한다', 1, 2),
  (10201, '③', '바이트코드를 실행하는 가상 머신 그 자체다', 0, 3),
  (10201, '④', '소스 코드를 저장하는 폴더 이름이다', 0, 4),
  (10202, '①', 'JVM ⊃ JRE ⊃ JDK', 0, 1),
  (10202, '②', 'JRE ⊃ JDK ⊃ JVM', 0, 2),
  (10202, '③', 'JDK ⊃ JRE ⊃ JVM', 1, 3),
  (10202, '④', '세 가지는 서로 포함 관계가 없다', 0, 4),
  (10203, '①', 'bin', 0, 1),
  (10203, '②', 'lib', 0, 2),
  (10203, '③', 'src', 1, 3),
  (10203, '④', 'out', 0, 4),
  (10204, '①', '실행 → 컴파일', 0, 1),
  (10204, '②', '컴파일 → 실행', 1, 2),
  (10204, '③', '컴파일만 한다', 0, 3),
  (10204, '④', '실행만 한다', 0, 4),
  (10301, '①', '파일 이름은 자유롭게 지어도 된다', 0, 1),
  (10301, '②', '파일 이름은 public 클래스 이름과 같아야 한다', 1, 2),
  (10301, '③', '파일 이름은 반드시 소문자로 시작해야 한다', 0, 3),
  (10301, '④', '파일 이름은 main이어야 한다', 0, 4),
  (10302, '①', 'studentInfo', 0, 1),
  (10302, '②', 'student_info', 0, 2),
  (10302, '③', 'STUDENTINFO', 0, 3),
  (10302, '④', 'StudentInfo', 1, 4),
  (10303, '①', '정상 컴파일된다', 0, 1),
  (10303, '②', 'Welcome.class가 만들어진다', 0, 2),
  (10303, '③', '컴파일 오류가 발생한다', 1, 3),
  (10303, '④', 'Hello.class가 만들어진다', 0, 4),
  (10304, '①', 'static', 0, 1),
  (10304, '②', 'void', 0, 2),
  (10304, '③', 'public', 1, 3),
  (10304, '④', 'main', 0, 4),
  (10401, '①', '반환값이 없기 때문에', 0, 1),
  (10401, '②', '객체를 만들지 않고도 실행을 시작할 수 있어야 하기 때문에', 1, 2),
  (10401, '③', '외부에서 접근을 막기 위해서', 0, 3),
  (10401, '④', '문자열 인자를 받기 위해서', 0, 4),
  (10402, '①', '매개변수가 없다', 0, 1),
  (10402, '②', '반환값이 없다', 1, 2),
  (10402, '③', '오류가 없다', 0, 3),
  (10402, '④', '객체가 없다', 0, 4),
  (10403, '①', '0', 0, 1),
  (10403, '②', '1', 0, 2),
  (10403, '③', '3', 1, 3),
  (10403, '④', '컴파일 오류', 0, 4),
  (10404, '①', '문자열 하나', 0, 1),
  (10404, '②', '문자열 배열', 1, 2),
  (10404, '③', '정수 배열', 0, 3),
  (10404, '④', '문자 하나', 0, 4),
  (10501, '①', '클래스 선언의 중괄호 뒤', 0, 1),
  (10501, '②', '메서드 선언의 중괄호 뒤', 0, 2),
  (10501, '③', '변수 선언·대입·메서드 호출 같은 실행문', 1, 3),
  (10501, '④', 'if문 블록의 중괄호 뒤', 0, 4),
  (10502, '①', '항상 누락된 바로 그 줄에 표시된다', 0, 1),
  (10502, '②', '다음 줄에서 발견되는 것처럼 표시될 수 있다', 1, 2),
  (10502, '③', '실행 중에만 발견된다', 0, 3),
  (10502, '④', '오류가 발생하지 않는다', 0, 4),
  (10503, '①', '10 출력', 0, 1),
  (10503, '②', '아무것도 출력 안 됨', 0, 2),
  (10503, '③', '컴파일 오류', 1, 3),
  (10503, '④', '실행 중 오류', 0, 4),
  (10504, '①', '`int x = 5` 뒤', 0, 1),
  (10504, '②', '`System.out.println(x)` 뒤', 0, 2),
  (10504, '③', '`score = 90` 뒤', 0, 3),
  (10504, '④', '`if (x > 0) { ... }`의 닫는 중괄호 뒤', 1, 4),
  (10601, '①', 'print는 숫자만 출력한다', 0, 1),
  (10601, '②', 'println은 출력 후 줄을 바꾼다', 1, 2),
  (10601, '③', 'print는 형식을 지정할 수 있다', 0, 3),
  (10601, '④', '둘은 완전히 같다', 0, 4),
  (10602, '①', '%s', 0, 1),
  (10602, '②', '%d', 1, 2),
  (10602, '③', '%.2f', 0, 3),
  (10602, '④', '%n', 0, 4),
  (10603, '①', 'Java Bronze Go (한 줄)', 0, 1),
  (10603, '②', 'Java Bronze / Go (두 줄)', 1, 2),
  (10603, '③', 'Java / Bronze / Go (세 줄)', 0, 3),
  (10603, '④', 'JavaBronzeGo', 0, 4),
  (10604, '①', '%s', 0, 1),
  (10604, '②', '"Java"', 0, 2),
  (10604, '③', 'Java', 1, 3),
  (10604, '④', '컴파일 오류', 0, 4),
  (10701, '①', '컴파일러가 실행 코드로 처리한다', 0, 1),
  (10701, '②', '프로그램 실행 속도를 높인다', 0, 2),
  (10701, '③', '컴파일러가 실행 코드로 처리하지 않으며 설명을 남길 때 쓴다', 1, 3),
  (10701, '④', '반드시 영어로 작성해야 한다', 0, 4),
  (10702, '①', '코드가 무엇을 하는지 그대로 반복해서 적는다', 0, 1),
  (10702, '②', '왜 이 처리가 필요한지 이유를 적는다', 1, 2),
  (10702, '③', '가능한 한 모든 줄에 주석을 단다', 0, 3),
  (10702, '④', '코드가 바뀌어도 주석은 그대로 둔다', 0, 4),
  (10703, '①', '1', 0, 1),
  (10703, '②', '2', 1, 2),
  (10703, '③', '1과 2', 0, 3),
  (10703, '④', '컴파일 오류', 0, 4),
  (10704, '①', '// ... //', 0, 1),
  (10704, '②', '/* ... */', 1, 2),
  (10704, '③', '< ... >', 0, 3),
  (10704, '④', '# ... #', 0, 4),
  (10801, '①', '들여쓰기가 틀리면 컴파일 오류가 발생한다', 0, 1),
  (10801, '②', '들여쓰기는 실행 규칙은 아니지만 코드가 어느 블록에 속하는지 보여 준다', 1, 2),
  (10801, '③', '들여쓰기는 실행 속도에 영향을 준다', 0, 3),
  (10801, '④', '들여쓰기는 주석의 한 종류다', 0, 4),
  (10802, '①', '블록을 시작한 문장과 같은 깊이', 1, 1),
  (10802, '②', '항상 맨 왼쪽 끝', 0, 2),
  (10802, '③', '블록 안쪽과 같은 깊이', 0, 3),
  (10802, '④', '아무 곳이나 관계없다', 0, 4),
  (10803, '①', '안쪽', 0, 1),
  (10803, '②', '바깥', 0, 2),
  (10803, '③', '안쪽 다음 줄에 바깥', 1, 3),
  (10803, '④', '아무것도 출력되지 않는다', 0, 4),
  (10804, '①', '조건문과 같은 깊이', 0, 1),
  (10804, '②', '한 단계 더 깊게', 1, 2),
  (10804, '③', '맨 왼쪽으로', 0, 3),
  (10804, '④', '들여쓰기를 없앤다', 0, 4),
  (10901, '①', '클래스를 실행한다', 0, 1),
  (10901, '②', '소스를 컴파일해 .class 파일을 만든다', 1, 2),
  (10901, '③', '소스 파일을 삭제한다', 0, 3),
  (10901, '④', '프로젝트 폴더를 만든다', 0, 4),
  (10902, '①', '기존 .class 파일이 자동으로 바뀐다', 0, 1),
  (10902, '②', '다시 컴파일해야 수정 내용이 반영된다', 1, 2),
  (10902, '③', '실행만 다시 하면 반영된다', 0, 3),
  (10902, '④', '수정하면 .class 파일이 삭제된다', 0, 4),
  (10903, '①', 'java Hello.java', 0, 1),
  (10903, '②', 'java Hello.class', 0, 2),
  (10903, '③', 'java Hello', 1, 3),
  (10903, '④', 'javac Hello', 0, 4),
  (10904, '①', 'Hello.exe', 0, 1),
  (10904, '②', 'Hello.class', 1, 2),
  (10904, '③', 'Hello.txt', 0, 3),
  (10904, '④', '아무것도 만들어지지 않는다', 0, 4),
  (11001, '①', '컴파일 오류는 실행 중, 런타임 오류는 컴파일 중 발견된다', 0, 1),
  (11001, '②', '컴파일 오류는 실행 전, 런타임 오류는 실행 중 발견된다', 1, 2),
  (11001, '③', '둘 다 실행 중에만 발견된다', 0, 3),
  (11001, '④', '둘 다 컴파일 전에 발견된다', 0, 4),
  (11002, '①', '가장 아래 오류부터', 0, 1),
  (11002, '②', '가장 위(첫 번째) 오류부터', 1, 2),
  (11002, '③', '무작위로', 0, 3),
  (11002, '④', '개수가 줄어들 때까지 재컴파일만 반복', 0, 4),
  (11003, '①', '0 출력', 0, 1),
  (11003, '②', '컴파일 오류', 0, 2),
  (11003, '③', '실행 중 오류(ArithmeticException)', 1, 3),
  (11003, '④', '10 출력', 0, 4),
  (11004, '①', '컴파일(문법) 오류', 1, 1),
  (11004, '②', '런타임 오류', 0, 2),
  (11004, '③', '논리 오류', 0, 3),
  (11004, '④', '오류가 아니다', 0, 4),
  (20101, '①', '값을 저장하는 이름표로, 사용하기 전에 선언해야 한다', 1, 1),
  (20101, '②', '한 번 저장하면 값을 바꿀 수 없다', 0, 2),
  (20101, '③', '선언 없이 바로 사용할 수 있다', 0, 3),
  (20101, '④', '이름은 반드시 숫자로 시작해야 한다', 0, 4),
  (20102, '①', 'score', 0, 1),
  (20102, '②', 'userName', 0, 2),
  (20102, '③', '1score', 1, 3),
  (20102, '④', 'my_score', 0, 4),
  (20103, '①', '80', 0, 1),
  (20103, '②', '90', 1, 2),
  (20103, '③', '8090', 0, 3),
  (20103, '④', '컴파일 오류', 0, 4),
  (20104, '①', 'String', 0, 1),
  (20104, '②', 'char', 0, 2),
  (20104, '③', 'boolean', 0, 3),
  (20104, '④', 'int', 1, 4),
  (20201, '①', 'byte', 0, 1),
  (20201, '②', 'short', 0, 2),
  (20201, '③', 'int', 1, 3),
  (20201, '④', 'float', 0, 4),
  (20202, '①', 'double과 f 접미사', 0, 1),
  (20202, '②', 'long과 L 접미사', 1, 2),
  (20202, '③', 'short와 S 접미사', 0, 3),
  (20202, '④', 'int를 두 개 사용', 0, 4),
  (20203, '①', '3_000_000_000', 0, 1),
  (20203, '②', '3000000000', 1, 2),
  (20203, '③', '컴파일 오류', 0, 3),
  (20203, '④', '30억', 0, 4),
  (20204, '①', 'f', 0, 1),
  (20204, '②', 'D', 0, 2),
  (20204, '③', 'L', 1, 3),
  (20204, '④', '필요 없다', 0, 4),
  (20301, '①', 'int', 0, 1),
  (20301, '②', 'double', 1, 2),
  (20301, '③', 'char', 0, 3),
  (20301, '④', 'boolean', 0, 4),
  (20302, '①', 'L', 0, 1),
  (20302, '②', 'd', 0, 2),
  (20302, '③', 'f', 1, 3),
  (20302, '④', 's', 0, 4),
  (20303, '①', '87', 0, 1),
  (20303, '②', '87.5', 1, 2),
  (20303, '③', '88', 0, 3),
  (20303, '④', '컴파일 오류', 0, 4),
  (20304, '①', 'L', 0, 1),
  (20304, '②', 'f', 1, 2),
  (20304, '③', '%', 0, 3),
  (20304, '④', '필요 없다', 0, 4),
  (20401, '①', '큰따옴표 " "', 0, 1),
  (20401, '②', '작은따옴표 '' ''', 1, 2),
  (20401, '③', '중괄호 { }', 0, 3),
  (20401, '④', '괄호 ( )', 0, 4),
  (20402, '①', '여러 글자를 저장할 수 있다', 0, 1),
  (20402, '②', '문자 하나를 저장하며 내부적으로 문자 코드값을 가진다', 1, 2),
  (20402, '③', 'true와 false를 저장한다', 0, 3),
  (20402, '④', '소수점 값을 저장한다', 0, 4),
  (20403, '①', 'B', 0, 1),
  (20403, '②', 'A1', 0, 2),
  (20403, '③', '66', 1, 3),
  (20403, '④', '컴파일 오류', 0, 4),
  (20404, '①', 'String', 0, 1),
  (20404, '②', 'char', 1, 2),
  (20404, '③', 'int', 0, 3),
  (20404, '④', 'byte', 0, 4),
  (20501, '①', '0과 1', 0, 1),
  (20501, '②', 'true와 false', 1, 2),
  (20501, '③', '"true"와 "false"', 0, 3),
  (20501, '④', '모든 정수', 0, 4),
  (20502, '①', '0은 false, 1은 true로 자동 변환된다', 0, 1),
  (20502, '②', '자동 변환되지 않는다', 1, 2),
  (20502, '③', '캐스팅하면 변환된다', 0, 3),
  (20502, '④', '문자열을 거치면 변환된다', 0, 4),
  (20503, '①', '85', 0, 1),
  (20503, '②', 'true', 1, 2),
  (20503, '③', 'false', 0, 3),
  (20503, '④', 'passed', 0, 4),
  (20504, '①', 'int', 0, 1),
  (20504, '②', 'String', 0, 2),
  (20504, '③', 'boolean', 1, 3),
  (20504, '④', 'char', 0, 4),
  (20601, '①', '==', 0, 1),
  (20601, '②', 'equals()', 1, 2),
  (20601, '③', '=', 0, 3),
  (20601, '④', 'length()', 0, 4),
  (20602, '①', '작은따옴표 '' ''', 0, 1),
  (20602, '②', '큰따옴표 " "', 1, 2),
  (20602, '③', '중괄호 { }', 0, 3),
  (20602, '④', '대괄호 [ ]', 0, 4),
  (20603, '①', '합계: 1020', 0, 1),
  (20603, '②', '합계: 30', 1, 2),
  (20603, '③', '30', 0, 3),
  (20603, '④', '합계: 10 + 20', 0, 4),
  (20604, '①', 'char', 0, 1),
  (20604, '②', 'String', 1, 2),
  (20604, '③', 'int', 0, 3),
  (20604, '④', 'boolean', 0, 4),
  (20701, '①', '값을 여러 번 바꿀 수 있다', 0, 1),
  (20701, '②', '한 번 값이 정해지면 재대입할 수 없다', 1, 2),
  (20701, '③', '선언만 하면 자동으로 0이 저장된다', 0, 3),
  (20701, '④', '문자열만 저장할 수 있다', 0, 4),
  (20702, '①', 'passScore', 0, 1),
  (20702, '②', 'pass_score', 0, 2),
  (20702, '③', 'PASS_SCORE', 1, 3),
  (20702, '④', 'PassScore', 0, 4),
  (20703, '①', '20이 저장된다', 0, 1),
  (20703, '②', '10이 유지된다', 0, 2),
  (20703, '③', '컴파일 오류', 1, 3),
  (20703, '④', '실행 중 오류', 0, 4),
  (20704, '①', 'static', 0, 1),
  (20704, '②', 'final', 1, 2),
  (20704, '③', 'const', 0, 3),
  (20704, '④', 'fixed', 0, 4),
  (20801, '①', '큰 범위 → 작은 범위', 0, 1),
  (20801, '②', '작은 범위 → 큰 범위', 1, 2),
  (20801, '③', '방향과 무관하게 항상 일어난다', 0, 3),
  (20801, '④', '자동 변환은 존재하지 않는다', 0, 4),
  (20802, '①', 'double → float → long → int', 0, 1),
  (20802, '②', 'byte → short → int → long → float → double', 1, 2),
  (20802, '③', 'int → char → boolean', 0, 3),
  (20802, '④', 'String → int → double', 0, 4),
  (20803, '①', '10', 0, 1),
  (20803, '②', '10.0', 1, 2),
  (20803, '③', '컴파일 오류', 0, 3),
  (20803, '④', '0', 0, 4),
  (20804, '①', 'byte', 0, 1),
  (20804, '②', 'short', 0, 2),
  (20804, '③', 'long', 1, 3),
  (20804, '④', 'char', 0, 4),
  (20901, '①', '값 (자료형)', 0, 1),
  (20901, '②', '(자료형) 값', 1, 2),
  (20901, '③', '자료형 → 값', 0, 3),
  (20901, '④', 'cast(값)', 0, 4),
  (20902, '①', '값을 안전하게 보장하는 기능', 0, 1),
  (20902, '②', '값 손실 가능성을 알고 변환하겠다는 개발자의 표시', 1, 2),
  (20902, '③', '값을 자동으로 반올림하는 기능', 0, 3),
  (20902, '④', '변수 이름을 바꾸는 기능', 0, 4),
  (20903, '①', '12.9', 0, 1),
  (20903, '②', '13', 0, 2),
  (20903, '③', '12', 1, 3),
  (20903, '④', '컴파일 오류', 0, 4),
  (20904, '①', 'double', 0, 1),
  (20904, '②', 'long', 0, 2),
  (20904, '③', 'int', 1, 3),
  (20904, '④', 'String', 0, 4),
  (21001, '①', 'int', 0, 1),
  (21001, '②', 'long', 0, 2),
  (21001, '③', 'String', 1, 3),
  (21001, '④', 'double', 0, 4),
  (21002, '①', 'premium', 0, 1),
  (21002, '②', 'isPremium', 1, 2),
  (21002, '③', 'premiumValue', 0, 3),
  (21002, '④', 'PREMIUM', 0, 4),
  (21003, '①', '하늘:true', 1, 1),
  (21003, '②', '하늘:1', 0, 2),
  (21003, '③', 'name:premium', 0, 3),
  (21003, '④', '컴파일 오류', 0, 4),
  (21004, '①', 'int', 0, 1),
  (21004, '②', 'boolean', 0, 2),
  (21004, '③', 'char', 0, 3),
  (21004, '④', 'double', 1, 4),
  (30101, '①', '두 값이 같은지 비교한다', 0, 1),
  (30101, '②', '오른쪽 계산 결과를 왼쪽 변수에 저장한다', 1, 2),
  (30101, '③', '왼쪽 값을 오른쪽에 저장한다', 0, 3),
  (30101, '④', '두 값을 더한다', 0, 4),
  (30102, '①', 'x = 3', 0, 1),
  (30102, '②', 'x = x + 3', 1, 2),
  (30102, '③', 'x == 3', 0, 3),
  (30102, '④', 'x + 3', 0, 4),
  (30103, '①', '5', 0, 1),
  (30103, '②', '10', 0, 2),
  (30103, '③', '15', 1, 3),
  (30103, '④', '105', 0, 4),
  (30104, '①', '+', 0, 1),
  (30104, '②', '-', 1, 2),
  (30104, '③', '*', 0, 3),
  (30104, '④', '%', 0, 4),
  (30201, '①', '소수점까지 계산된다', 0, 1),
  (30201, '②', '몫만 남는다', 1, 2),
  (30201, '③', '나머지만 남는다', 0, 3),
  (30201, '④', '항상 0이 된다', 0, 4),
  (30202, '①', '0이 출력된다', 0, 1),
  (30202, '②', '컴파일 오류', 0, 2),
  (30202, '③', '실행 중 ArithmeticException 발생', 1, 3),
  (30202, '④', '무한대가 출력된다', 0, 4),
  (30203, '①', '3.5와 1', 0, 1),
  (30203, '②', '3과 1', 1, 2),
  (30203, '③', '3과 5', 0, 3),
  (30203, '④', '4와 1', 0, 4),
  (30204, '①', '/', 0, 1),
  (30204, '②', '*', 0, 2),
  (30204, '③', '%', 1, 3),
  (30204, '④', '#', 0, 4),
  (30301, '①', '차이가 전혀 없다', 0, 1),
  (30301, '②', '++x는 먼저 증가시킨 뒤 사용하고, x++는 사용한 뒤 증가시킨다', 1, 2),
  (30301, '③', '++x는 2씩, x++는 1씩 증가한다', 0, 3),
  (30301, '④', 'x++만 반복문에서 쓸 수 있다', 0, 4),
  (30302, '①', '서로 다르다', 0, 1),
  (30302, '②', '같다', 1, 2),
  (30302, '③', 'x++만 증가한다', 0, 3),
  (30302, '④', '++x만 증가한다', 0, 4),
  (30303, '①', '5 5', 0, 1),
  (30303, '②', '6 6', 0, 2),
  (30303, '③', '6 5', 1, 3),
  (30303, '④', '5 6', 0, 4),
  (30304, '①', '++', 0, 1),
  (30304, '②', '--', 1, 2),
  (30304, '③', '-=', 0, 3),
  (30304, '④', '%%', 0, 4),
  (30401, '①', 'int', 0, 1),
  (30401, '②', 'String', 0, 2),
  (30401, '③', 'boolean', 1, 3),
  (30401, '④', 'double', 0, 4),
  (30402, '①', '==', 0, 1),
  (30402, '②', 'equals()', 1, 2),
  (30402, '③', '>=', 0, 3),
  (30402, '④', '!=', 0, 4),
  (30403, '①', 'true', 1, 1),
  (30403, '②', 'false', 0, 2),
  (30403, '③', '5', 0, 3),
  (30403, '④', '컴파일 오류', 0, 4),
  (30404, '①', '=!', 0, 1),
  (30404, '②', '!=', 1, 2),
  (30404, '③', '<>', 0, 3),
  (30404, '④', '==', 0, 4),
  (30501, '①', '하나라도 참이면', 0, 1),
  (30501, '②', '둘 다 참이면', 1, 2),
  (30501, '③', '둘 다 거짓이면', 0, 3),
  (30501, '④', '왼쪽만 참이면', 0, 4),
  (30502, '①', '&&의 왼쪽이 false면 오른쪽을 계산하지 않는다', 1, 1),
  (30502, '②', '&&는 항상 양쪽을 모두 계산한다', 0, 2),
  (30502, '③', '||의 왼쪽이 true면 오른쪽만 계산한다', 0, 3),
  (30502, '④', '단락 평가는 산술 연산에서 일어난다', 0, 4),
  (30503, '①', 'true', 1, 1),
  (30503, '②', 'false', 0, 2),
  (30503, '③', '1', 0, 3),
  (30503, '④', '컴파일 오류', 0, 4),
  (30504, '①', '~', 0, 1),
  (30504, '②', '!', 1, 2),
  (30504, '③', '^', 0, 3),
  (30504, '④', 'not', 0, 4),
  (30601, '①', '항상 숫자 계산이 먼저다', 0, 1),
  (30601, '②', '왼쪽부터 차례대로 처리되며 문자열을 만나면 연결로 처리된다', 1, 2),
  (30601, '③', '오른쪽부터 계산된다', 0, 3),
  (30601, '④', '컴파일 오류가 발생한다', 0, 4),
  (30602, '①', '계산 부분을 괄호로 묶는다', 1, 1),
  (30602, '②', '세미콜론을 추가한다', 0, 2),
  (30602, '③', '큰따옴표를 없앤다', 0, 3),
  (30602, '④', '방법이 없다', 0, 4),
  (30603, '①', '결과: 5', 0, 1),
  (30603, '②', '결과: 23', 1, 2),
  (30603, '③', '결과: 2 + 3', 0, 3),
  (30603, '④', '5', 0, 4),
  (30604, '①', '*', 0, 1),
  (30604, '②', '+', 1, 2),
  (30604, '③', '%', 0, 3),
  (30604, '④', '/', 0, 4),
  (30701, '①', '덧셈·뺄셈이 곱셈·나눗셈보다 먼저', 0, 1),
  (30701, '②', '곱셈·나눗셈이 덧셈·뺄셈보다 먼저', 1, 2),
  (30701, '③', '모두 같은 우선순위', 0, 3),
  (30701, '④', '오른쪽부터 계산', 0, 4),
  (30702, '①', '모든 우선순위를 외워 괄호 없이 쓴다', 0, 1),
  (30702, '②', '혼동될 수 있는 식에는 괄호를 써서 의도를 드러낸다', 1, 2),
  (30702, '③', '우선순위는 실행할 때마다 바뀌므로 신경 쓰지 않는다', 0, 3),
  (30702, '④', '항상 한 줄에 모든 계산을 넣는다', 0, 4),
  (30703, '①', '14', 0, 1),
  (30703, '②', '20', 1, 2),
  (30703, '③', '24', 0, 3),
  (30703, '④', '11', 0, 4),
  (30704, '①', '+', 0, 1),
  (30704, '②', '-', 0, 2),
  (30704, '③', '*', 1, 3),
  (30704, '④', '%', 0, 4),
  (30801, '①', '조건 ? 값1 : 값2', 1, 1),
  (30801, '②', '조건 : 값1 ? 값2', 0, 2),
  (30801, '③', '값1 ? 조건 : 값2', 0, 3),
  (30801, '④', '조건 ?? 값1 :: 값2', 0, 4),
  (30802, '①', '삼항 연산자', 0, 1),
  (30802, '②', 'if-else문', 1, 2),
  (30802, '③', '대입 연산자', 0, 3),
  (30802, '④', '나머지 연산자', 0, 4),
  (30803, '①', '통과', 1, 1),
  (30803, '②', '재도전', 0, 2),
  (30803, '③', 'true', 0, 3),
  (30803, '④', '76', 0, 4),
  (30804, '①', ':', 0, 1),
  (30804, '②', '?', 1, 2),
  (30804, '③', '!', 0, 3),
  (30804, '④', ';', 0, 4),
  (30901, '①', 'n % 2 == 1', 0, 1),
  (30901, '②', 'n % 2 == 0', 1, 2),
  (30901, '③', 'n / 2 == 0', 0, 3),
  (30901, '④', 'n * 2 == 0', 0, 4),
  (30902, '①', 'number % 3 == 0', 1, 1),
  (30902, '②', 'number / 3 == 0', 0, 2),
  (30902, '③', 'number == 3', 0, 3),
  (30902, '④', 'number % 3 == 3', 0, 4),
  (30903, '①', 'true', 0, 1),
  (30903, '②', 'false', 1, 2),
  (30903, '③', '1', 0, 3),
  (30903, '④', '9', 0, 4),
  (30904, '①', '/', 0, 1),
  (30904, '②', '%', 1, 2),
  (30904, '③', '*', 0, 3),
  (30904, '④', '-', 0, 4),
  (31001, '①', '한 식에 모든 계산·조건을 넣는다', 0, 1),
  (31001, '②', '계산값과 조건값을 변수로 분리한다', 1, 2),
  (31001, '③', '괄호를 절대 쓰지 않는다', 0, 3),
  (31001, '④', '우선순위를 모두 외운 뒤 한 줄로 쓴다', 0, 4),
  (31002, '①', '논리 → 비교 → 산술 → 괄호', 0, 1),
  (31002, '②', '괄호 안 계산 → 산술 → 비교 → 논리', 1, 2),
  (31002, '③', '왼쪽 값만 확인', 0, 3),
  (31002, '④', '대입부터 확인', 0, 4),
  (31003, '①', '재도전', 0, 1),
  (31003, '②', 'true', 0, 2),
  (31003, '③', '통과', 1, 3),
  (31003, '④', '76', 0, 4),
  (31004, '①', '>=', 0, 1),
  (31004, '②', '>', 1, 2),
  (31004, '③', '==', 0, 3),
  (31004, '④', '!=', 0, 4),
  (40101, '①', '조건이 false일 때', 0, 1),
  (40101, '②', '조건이 true일 때', 1, 2),
  (40101, '③', '항상 실행된다', 0, 3),
  (40101, '④', '컴파일할 때', 0, 4),
  (40102, '①', '정수 값', 0, 1),
  (40102, '②', '문자열', 0, 2),
  (40102, '③', 'boolean 값이나 비교식', 1, 3),
  (40102, '④', '아무 값이나 가능', 0, 4),
  (40103, '①', '통과', 1, 1),
  (40103, '②', '아무것도 출력되지 않는다', 0, 2),
  (40103, '③', '80', 0, 3),
  (40103, '④', '컴파일 오류', 0, 4),
  (40104, '①', '중괄호 { }', 0, 1),
  (40104, '②', '대괄호 [ ]', 0, 2),
  (40104, '③', '괄호 ( )', 1, 3),
  (40104, '④', '큰따옴표 " "', 0, 4),
  (40201, '①', '세 가지 이상 갈래가 필요할 때', 0, 1),
  (40201, '②', '서로 배타적인 두 경우 중 하나를 반드시 처리할 때', 1, 2),
  (40201, '③', '반복이 필요할 때', 0, 3),
  (40201, '④', '조건 없이 실행할 때', 0, 4),
  (40202, '①', 'if 블록 안에만', 0, 1),
  (40202, '②', 'else 블록 안에만', 0, 2),
  (40202, '③', '두 블록에 복사', 0, 3),
  (40202, '④', '조건문 밖', 1, 4),
  (40203, '①', '환영', 0, 1),
  (40203, '②', '로그인 필요', 1, 2),
  (40203, '③', '둘 다 출력', 0, 3),
  (40203, '④', '아무것도 출력 안 됨', 0, 4),
  (40204, '①', 'elseif', 0, 1),
  (40204, '②', 'other', 0, 2),
  (40204, '③', 'else', 1, 3),
  (40204, '④', 'default', 0, 4),
  (40301, '①', '모든 조건을 검사한 뒤 참인 블록을 전부 실행한다', 0, 1),
  (40301, '②', '위에서부터 검사해 처음 참이 된 블록 하나만 실행한다', 1, 2),
  (40301, '③', '아래에서부터 검사한다', 0, 3),
  (40301, '④', '무작위 순서로 검사한다', 0, 4),
  (40302, '①', '첫 번째 조건을 다시 검사한다', 0, 1),
  (40302, '②', '앞의 어떤 조건에도 해당하지 않는 나머지 모든 경우를 처리한다', 1, 2),
  (40302, '③', '프로그램을 종료한다', 0, 3),
  (40302, '④', '반복을 시작한다', 0, 4),
  (40303, '①', 'A', 0, 1),
  (40303, '②', 'B', 1, 2),
  (40303, '③', 'C', 0, 3),
  (40303, '④', '아무것도 저장되지 않는다', 0, 4),
  (40304, '①', 'and', 0, 1),
  (40304, '②', 'or', 0, 2),
  (40304, '③', 'else', 1, 3),
  (40304, '④', 'then', 0, 4),
  (40401, '①', '실수 범위 비교', 0, 1),
  (40401, '②', '메뉴 번호·요일처럼 정해진 값에 따른 분기', 1, 2),
  (40401, '③', '반복 작업', 0, 3),
  (40401, '④', '예외 처리', 0, 4),
  (40402, '①', '실행 속도가 빨라진다', 0, 1),
  (40402, '②', 'break 누락으로 다음 case까지 실행되는 문제를 줄인다', 1, 2),
  (40402, '③', 'case를 무한히 만들 수 있다', 0, 3),
  (40402, '④', '조건식을 쓸 수 있다', 0, 4),
  (40403, '①', '조회', 1, 1),
  (40403, '②', '오류', 0, 2),
  (40403, '③', '조회와 오류 모두', 0, 3),
  (40403, '④', '컴파일 오류', 0, 4),
  (40404, '①', 'else', 0, 1),
  (40404, '②', 'final', 0, 2),
  (40404, '③', 'default', 1, 3),
  (40404, '④', 'other', 0, 4),
  (40501, '①', '조건식·본문·세미콜론', 0, 1),
  (40501, '②', '초기식·조건식·증감식', 1, 2),
  (40501, '③', '시작·중간·끝', 0, 3),
  (40501, '④', '선언·비교·출력', 0, 4),
  (40502, '①', '한 번만 실행된다', 0, 1),
  (40502, '②', '컴파일 오류', 0, 2),
  (40502, '③', '무한 반복이 된다', 1, 3),
  (40502, '④', '자동으로 멈춘다', 0, 4),
  (40503, '①', '1 2 3 (세 줄)', 1, 1),
  (40503, '②', '0 1 2 (세 줄)', 0, 2),
  (40503, '③', '1 2 3 4 (네 줄)', 0, 3),
  (40503, '④', '3 2 1 (세 줄)', 0, 4),
  (40504, '①', 'i--', 0, 1),
  (40504, '②', 'i++', 1, 2),
  (40504, '③', 'i == 5', 0, 3),
  (40504, '④', 'i = 0', 0, 4),
  (40601, '①', '본문 실행 후', 0, 1),
  (40601, '②', '본문 실행 전', 1, 2),
  (40601, '③', '프로그램 종료 시', 0, 3),
  (40601, '④', '검사하지 않는다', 0, 4),
  (40602, '①', '한 번 실행된다', 0, 1),
  (40602, '②', '무한 반복된다', 0, 2),
  (40602, '③', '한 번도 실행되지 않는다', 1, 3),
  (40602, '④', '컴파일 오류', 0, 4),
  (40603, '①', '1 2 3 (세 줄)', 1, 1),
  (40603, '②', '1 2 (두 줄)', 0, 2),
  (40603, '③', '0 1 2 (세 줄)', 0, 3),
  (40603, '④', '무한 반복', 0, 4),
  (40604, '①', '정수 값', 0, 1),
  (40604, '②', 'boolean 결과가 나오는 식', 1, 2),
  (40604, '③', '문자열', 0, 3),
  (40604, '④', '세미콜론', 0, 4),
  (40701, '①', '조건을 먼저 검사한다', 0, 1),
  (40701, '②', '본문을 한 번 실행한 뒤 조건을 검사한다', 1, 2),
  (40701, '③', '본문을 실행하지 않을 수도 있다', 0, 3),
  (40701, '④', '조건 없이 무한 반복한다', 0, 4),
  (40702, '①', 'while 뒤 조건식 끝에 세미콜론이 필요하다', 1, 1),
  (40702, '②', '중괄호를 쓸 수 없다', 0, 2),
  (40702, '③', '조건식이 필요 없다', 0, 3),
  (40702, '④', 'do 앞에 세미콜론이 필요하다', 0, 4),
  (40703, '①', '아무것도 출력 안 됨', 0, 1),
  (40703, '②', '메뉴 (한 번)', 1, 2),
  (40703, '③', '메뉴 (무한 반복)', 0, 3),
  (40703, '④', '컴파일 오류', 0, 4),
  (40704, '①', ':', 0, 1),
  (40704, '②', ';', 1, 2),
  (40704, '③', '.', 0, 3),
  (40704, '④', '없음', 0, 4),
  (40801, '①', '프로그램 전체를 종료한다', 0, 1),
  (40801, '②', '가장 가까운 반복문 또는 switch문을 즉시 끝낸다', 1, 2),
  (40801, '③', '현재 반복만 건너뛴다', 0, 3),
  (40801, '④', '반복을 처음부터 다시 시작한다', 0, 4),
  (40802, '①', '반복문을 완전히 끝낸다', 0, 1),
  (40802, '②', '현재 반복의 남은 문장을 건너뛰고 다음 반복으로 이동한다', 1, 2),
  (40802, '③', '조건 검사를 생략한다', 0, 3),
  (40802, '④', '반복 변수를 0으로 만든다', 0, 4),
  (40803, '①', '1 2', 1, 1),
  (40803, '②', '1 2 3', 0, 2),
  (40803, '③', '1 2 4 5', 0, 3),
  (40803, '④', '1 2 3 4 5', 0, 4),
  (40804, '①', 'break', 0, 1),
  (40804, '②', 'return', 0, 2),
  (40804, '③', 'continue', 1, 3),
  (40804, '④', 'exit', 0, 4),
  (40901, '①', '조건문 안에는 조건문을 넣을 수 없다', 0, 1),
  (40901, '②', '조건문 안에 조건문, 반복문 안에 반복문을 넣을 수 있다', 1, 2),
  (40901, '③', '반복문은 최대 두 개까지만 중첩할 수 있다', 0, 3),
  (40901, '④', '중첩하면 컴파일 오류가 발생한다', 0, 4),
  (40902, '①', '바깥 반복 횟수 + 안쪽 반복 횟수', 0, 1),
  (40902, '②', '바깥 반복 횟수 × 안쪽 반복 횟수', 1, 2),
  (40902, '③', '안쪽 반복 횟수만 센다', 0, 3),
  (40902, '④', '바깥 반복 횟수만 센다', 0, 4),
  (40903, '①', '2줄', 0, 1),
  (40903, '②', '3줄', 0, 2),
  (40903, '③', '5줄', 0, 3),
  (40903, '④', '6줄', 1, 4),
  (40904, '①', 'a·b', 0, 1),
  (40904, '②', 'row·col', 1, 2),
  (40904, '③', 'x1·x2', 0, 3),
  (40904, '④', 'i1·i2', 0, 4),
  (41001, '①', '출력만 보고 감으로 판단한다', 0, 1),
  (41001, '②', '반복마다 변수값·조건 결과를 표로 추적한다', 1, 2),
  (41001, '③', '코드를 반만 읽는다', 0, 3),
  (41001, '④', '무조건 실행해 본다', 0, 4),
  (41002, '①', '정상값·경계값·잘못된 값', 1, 1),
  (41002, '②', '큰 값·작은 값·중간 값', 0, 2),
  (41002, '③', '문자·숫자·기호', 0, 3),
  (41002, '④', '처음·중간·끝', 0, 4),
  (41003, '①', '80, 65, 90', 0, 1),
  (41003, '②', '80, 90', 1, 2),
  (41003, '③', '65', 0, 3),
  (41003, '④', '90', 0, 4),
  (41004, '①', '주석', 0, 1),
  (41004, '②', '조건문이나 switch', 1, 2),
  (41004, '③', '상수', 0, 3),
  (41004, '④', '캐스팅', 0, 4),
  (50101, '①', '서로 다른 자료형을 섞어 저장한다', 0, 1),
  (50101, '②', '같은 자료형 값을 순서대로 저장하며 크기는 생성 후 바꿀 수 없다', 1, 2),
  (50101, '③', '크기를 언제든 늘릴 수 있다', 0, 3),
  (50101, '④', '값을 하나만 저장할 수 있다', 0, 4),
  (50102, '①', '배열이 복사된다', 0, 1),
  (50102, '②', '두 변수가 같은 배열을 함께 가리킨다', 1, 2),
  (50102, '③', '원본 배열이 삭제된다', 0, 3),
  (50102, '④', '컴파일 오류가 발생한다', 0, 4),
  (50103, '①', '80', 1, 1),
  (50103, '②', '90', 0, 2),
  (50103, '③', '70', 0, 3),
  (50103, '④', '0', 0, 4),
  (50104, '①', '( )', 0, 1),
  (50104, '②', '[ ]', 1, 2),
  (50104, '③', '{ }', 0, 3),
  (50104, '④', '< >', 0, 4),
  (50201, '①', '1', 0, 1),
  (50201, '②', '0', 1, 2),
  (50201, '③', 'null', 0, 3),
  (50201, '④', '값이 없다', 0, 4),
  (50202, '①', 'true', 0, 1),
  (50202, '②', 'false', 1, 2),
  (50202, '③', '0', 0, 3),
  (50202, '④', 'null', 0, 4),
  (50203, '①', '1', 0, 1),
  (50203, '②', '0', 1, 2),
  (50203, '③', 'null', 0, 3),
  (50203, '④', '실행 중 오류', 0, 4),
  (50204, '①', 'make', 0, 1),
  (50204, '②', 'class', 0, 2),
  (50204, '③', 'new', 1, 3),
  (50204, '④', 'array', 0, 4),
  (50301, '①', '1', 0, 1),
  (50301, '②', '0', 1, 2),
  (50301, '③', '-1', 0, 3),
  (50301, '④', 'length', 0, 4),
  (50302, '①', 'length', 0, 1),
  (50302, '②', 'length + 1', 0, 2),
  (50302, '③', 'length - 1', 1, 3),
  (50302, '④', '0', 0, 4),
  (50303, '①', '30 출력', 0, 1),
  (50303, '②', '0 출력', 0, 2),
  (50303, '③', '컴파일 오류', 0, 3),
  (50303, '④', '실행 중 오류(범위 초과 예외)', 1, 4),
  (50304, '①', '3', 0, 1),
  (50304, '②', '2', 1, 2),
  (50304, '③', '1', 0, 3),
  (50304, '④', '0', 0, 4),
  (50401, '①', 'array.length()', 0, 1),
  (50401, '②', 'array.length', 1, 2),
  (50401, '③', 'array.size()', 0, 3),
  (50401, '④', 'length(array)', 0, 4),
  (50402, '①', '둘 다 괄호가 필요하다', 0, 1),
  (50402, '②', '배열은 length(괄호 없음), String은 length()(괄호 있음)', 1, 2),
  (50402, '③', '배열은 length(), String은 length', 0, 3),
  (50402, '④', '차이가 없다', 0, 4),
  (50403, '①', '2', 0, 1),
  (50403, '②', '3', 1, 2),
  (50403, '③', '30', 0, 3),
  (50403, '④', '0', 0, 4),
  (50404, '①', 'size', 0, 1),
  (50404, '②', 'count', 0, 2),
  (50404, '③', 'length', 1, 3),
  (50404, '④', 'index', 0, 4),
  (50501, '①', '인덱스(현재 위치)가 필요할 때', 1, 1),
  (50501, '②', '모든 요소를 읽기만 할 때', 0, 2),
  (50501, '③', '배열이 비어 있을 때', 0, 3),
  (50501, '④', '요소가 하나일 때', 0, 4),
  (50502, '①', 'i <= array.length', 0, 1),
  (50502, '②', 'i < array.length', 1, 2),
  (50502, '③', 'i == array.length', 0, 3),
  (50502, '④', 'i > array.length', 0, 4),
  (50503, '①', '10 20', 0, 1),
  (50503, '②', '0: 10 다음 줄 1: 20', 1, 2),
  (50503, '③', '1: 10 다음 줄 2: 20', 0, 3),
  (50503, '④', '0: 20 다음 줄 1: 10', 0, 4),
  (50504, '①', '<=', 0, 1),
  (50504, '②', '<', 1, 2),
  (50504, '③', '==', 0, 3),
  (50504, '④', '>=', 0, 4),
  (50601, '①', '인덱스를 자유롭게 쓸 수 있다', 0, 1),
  (50601, '②', '모든 요소를 앞에서부터 읽기 쉽지만 현재 위치는 알 수 없다', 1, 2),
  (50601, '③', '역순으로 순회한다', 0, 3),
  (50601, '④', '요소를 직접 수정할 수 있다', 0, 4),
  (50602, '①', '원본 배열이 수정된다', 0, 1),
  (50602, '②', '원본 배열은 바뀌지 않는다', 1, 2),
  (50602, '③', '컴파일 오류가 발생한다', 0, 3),
  (50602, '④', '배열 크기가 변한다', 0, 4),
  (50603, '①', '70 80 (두 줄)', 1, 1),
  (50603, '②', '0 1 (두 줄)', 0, 2),
  (50603, '③', '80 70 (두 줄)', 0, 3),
  (50603, '④', '컴파일 오류', 0, 4),
  (50604, '①', ';', 0, 1),
  (50604, '②', ':', 1, 2),
  (50604, '③', '=', 0, 3),
  (50604, '④', 'in', 0, 4),
  (50701, '①', '반복 안에서 매번 0으로 초기화한다', 0, 1),
  (50701, '②', '반복 전에 한 번 0으로 초기화한다', 1, 2),
  (50701, '③', '초기화하지 않는다', 0, 3),
  (50701, '④', '반복 후에 초기화한다', 0, 4),
  (50702, '①', '합계를 먼저 나눈 뒤 double에 저장한다', 0, 1),
  (50702, '②', '나누기 전에 한쪽을 double로 변환한다', 1, 2),
  (50702, '③', '평균에 캐스팅은 필요 없다', 0, 3),
  (50702, '④', 'int끼리 나눠도 소수가 유지된다', 0, 4),
  (50703, '①', '80', 0, 1),
  (50703, '②', '240', 1, 2),
  (50703, '③', '3', 0, 3),
  (50703, '④', '0', 0, 4),
  (50704, '①', 'int', 0, 1),
  (50704, '②', 'long', 0, 2),
  (50704, '③', 'double', 1, 3),
  (50704, '④', 'char', 0, 4),
  (50801, '①', 'text.length', 0, 1),
  (50801, '②', 'text.length()', 1, 2),
  (50801, '③', 'text.size()', 0, 3),
  (50801, '④', 'length(text)', 0, 4),
  (50802, '①', '같은 값이다', 0, 1),
  (50802, '②', '서로 다르다', 1, 2),
  (50802, '③', '둘 다 오류다', 0, 3),
  (50802, '④', '비교할 수 없다', 0, 4),
  (50803, '①', '3', 0, 1),
  (50803, '②', '4', 1, 2),
  (50803, '③', '5', 0, 3),
  (50803, '④', 'Java', 0, 4),
  (50804, '①', '*', 0, 1),
  (50804, '②', '+', 1, 2),
  (50804, '③', '&', 0, 3),
  (50804, '④', ',', 0, 4),
  (50901, '①', '==', 0, 1),
  (50901, '②', 'equals()', 1, 2),
  (50901, '③', 'charAt()', 0, 3),
  (50901, '④', 'length()', 0, 4),
  (50902, '①', 'equals()', 0, 1),
  (50902, '②', 'equalsIgnoreCase()', 1, 2),
  (50902, '③', 'compare()', 0, 3),
  (50902, '④', 'matchCase()', 0, 4),
  (50903, '①', 'J', 1, 1),
  (50903, '②', 'a', 0, 2),
  (50903, '③', 'Java', 0, 3),
  (50903, '④', '0', 0, 4),
  (50904, '①', 'length', 0, 1),
  (50904, '②', 'charAt', 0, 2),
  (50904, '③', 'equals', 1, 3),
  (50904, '④', 'print', 0, 4),
  (51001, '①', '배열을 순회하며 각 요소를 ==로 비교한다', 0, 1),
  (51001, '②', '배열을 순회하며 각 요소를 equals로 비교한다', 1, 2),
  (51001, '③', '배열 전체를 한 번에 비교한다', 0, 3),
  (51001, '④', '인덱스만 비교한다', 0, 4),
  (51002, '①', 'int', 0, 1),
  (51002, '②', 'String', 0, 2),
  (51002, '③', 'boolean', 1, 3),
  (51002, '④', 'char', 0, 4),
  (51003, '①', '찾음: 민수', 0, 1),
  (51003, '②', '찾음: 지민', 1, 2),
  (51003, '③', '아무것도 출력 안 됨', 0, 3),
  (51003, '④', '찾음: 민수 지민', 0, 4),
  (51004, '①', '==', 0, 1),
  (51004, '②', 'equals', 1, 2),
  (51004, '③', 'charAt', 0, 3),
  (51004, '④', 'length', 0, 4),
  (110101, '①', '프로그램이 항상 빨라진다', 0, 1),
  (110101, '②', '같은 코드를 반복 작성하지 않고 재사용할 수 있다', 1, 2),
  (110101, '③', '변수를 쓰지 않아도 된다', 0, 3),
  (110101, '④', '컴파일이 생략된다', 0, 4),
  (110102, '①', '안녕하세요 한 번', 0, 1),
  (110102, '②', '안녕하세요 두 번', 1, 2),
  (110102, '③', 'greet 두 번', 0, 3),
  (110102, '④', '출력 없음', 0, 4),
  (110103, '①', 'int', 0, 1),
  (110103, '②', 'String', 0, 2),
  (110103, '③', 'void', 1, 3),
  (110103, '④', 'null', 0, 4),
  (110104, '①', '자동으로 한 번 실행된다', 0, 1),
  (110104, '②', '아무것도 출력되지 않는다', 1, 2),
  (110104, '③', '컴파일 오류', 0, 3),
  (110104, '④', '무한 반복', 0, 4),
  (110201, '①', '반환형·이름·매개변수 목록', 1, 1),
  (110201, '②', '클래스 이름·파일 이름', 0, 2),
  (110201, '③', '조건식·증감식', 0, 3),
  (110201, '④', '배열·인덱스', 0, 4),
  (110202, '①', '34', 0, 1),
  (110202, '②', '7', 1, 2),
  (110202, '③', 'add(3, 4)', 0, 3),
  (110202, '④', '컴파일 오류', 0, 4),
  (110203, '①', 'give', 0, 1),
  (110203, '②', 'out', 0, 2),
  (110203, '③', 'return', 1, 3),
  (110203, '④', 'send', 0, 4),
  (110204, '①', '7이 출력된다', 0, 1),
  (110204, '②', '34가 출력된다', 0, 2),
  (110204, '③', '컴파일 오류', 1, 3),
  (110204, '④', '실행 중 오류', 0, 4),
  (110301, '①', '매개변수가 없다', 0, 1),
  (110301, '②', '호출한 곳에 값을 돌려주지 않는다', 1, 2),
  (110301, '③', '오류가 없다', 0, 3),
  (110301, '④', '객체를 만들 수 없다', 0, 4),
  (110302, '①', '-1과 5', 0, 1),
  (110302, '②', '5', 1, 2),
  (110302, '③', '-1', 0, 3),
  (110302, '④', '출력 없음', 0, 4),
  (110303, '①', 'break', 0, 1),
  (110303, '②', 'continue', 0, 2),
  (110303, '③', 'return', 1, 3),
  (110303, '④', 'exit', 0, 4),
  (110304, '①', '5가 반환된다', 0, 1),
  (110304, '②', '컴파일 오류', 1, 2),
  (110304, '③', '무시된다', 0, 3),
  (110304, '④', '실행 중 오류', 0, 4),
  (110401, '①', '메서드가 즉시 끝나고 값이 호출한 곳으로 전달된다', 1, 1),
  (110401, '②', '메서드가 처음부터 다시 실행된다', 0, 2),
  (110401, '③', '다음 반복으로 넘어간다', 0, 3),
  (110401, '④', '프로그램이 종료된다', 0, 4),
  (110402, '①', 'true', 1, 1),
  (110402, '②', 'false', 0, 2),
  (110402, '③', '8', 0, 3),
  (110402, '④', '0', 0, 4),
  (110403, '①', '=', 0, 1),
  (110403, '②', '==', 1, 2),
  (110403, '③', '!=', 0, 3),
  (110403, '④', '>=', 0, 4),
  (110404, '①', 'true', 0, 1),
  (110404, '②', 'false', 1, 2),
  (110404, '③', '1', 0, 3),
  (110404, '④', '7', 0, 4),
  (110501, '①', '변수 자체가 전달된다', 0, 1),
  (110501, '②', '인자의 값이 매개변수로 복사된다', 1, 2),
  (110501, '③', '참조만 전달되어 기본형도 원본이 바뀐다', 0, 3),
  (110501, '④', '이름이 같아야 전달된다', 0, 4),
  (110502, '①', '100', 0, 1),
  (110502, '②', '10', 1, 2),
  (110502, '③', '0', 0, 3),
  (110502, '④', '컴파일 오류', 0, 4),
  (110503, '①', 'text', 0, 1),
  (110503, '②', 'name', 1, 2),
  (110503, '③', 'value', 0, 3),
  (110503, '④', 'args', 0, 4),
  (110504, '①', '안녕 name', 0, 1),
  (110504, '②', '안녕 민수', 1, 2),
  (110504, '③', '민수', 0, 3),
  (110504, '④', '안녕', 0, 4),
  (110601, '①', '프로그램이 끝날 때까지 유지된다', 0, 1),
  (110601, '②', '선언된 블록(호출) 안에서만 유지된다', 1, 2),
  (110601, '③', '클래스가 있는 동안 유지된다', 0, 3),
  (110601, '④', '한 번 선언하면 모든 메서드에서 사용된다', 0, 4),
  (110602, '①', '10 10', 0, 1),
  (110602, '②', '20 20', 0, 2),
  (110602, '③', '10 20', 1, 3),
  (110602, '④', '컴파일 오류', 0, 4),
  (110603, '①', '컴파일 오류', 0, 1),
  (110603, '②', '충돌하지 않는다', 1, 2),
  (110603, '③', '마지막 선언만 유효', 0, 3),
  (110603, '④', '실행 중 오류', 0, 4),
  (110604, '①', '10이 출력된다', 0, 1),
  (110604, '②', '0이 출력된다', 0, 2),
  (110604, '③', '컴파일 오류', 1, 3),
  (110604, '④', 'null 출력', 0, 4),
  (110701, '①', '이름이 다르고 매개변수가 같다', 0, 1),
  (110701, '②', '이름이 같고 매개변수의 개수·자료형·순서가 다르다', 1, 2),
  (110701, '③', '반환형만 다르다', 0, 3),
  (110701, '④', '접근 제어자만 다르다', 0, 4),
  (110702, '①', '5', 1, 1),
  (110702, '②', '5.0', 0, 2),
  (110702, '③', '컴파일 오류', 0, 3),
  (110702, '④', '23', 0, 4),
  (110703, '①', '3', 0, 1),
  (110703, '②', '3.5', 1, 2),
  (110703, '③', '컴파일 오류', 0, 3),
  (110703, '④', '1.52.0', 0, 4),
  (110704, '①', '오버로딩이 된다', 0, 1),
  (110704, '②', '컴파일 오류가 발생한다', 1, 2),
  (110704, '③', '마지막 선언이 유효하다', 0, 3),
  (110704, '④', '실행 시 무작위 선택된다', 0, 4),
  (110801, '①', '반복문과 배열', 0, 1),
  (110801, '②', '종료 조건과 문제를 줄이는 재귀 호출', 1, 2),
  (110801, '③', '전역 변수와 상수', 0, 3),
  (110801, '④', '예외 처리와 파일', 0, 4),
  (110802, '①', '4', 0, 1),
  (110802, '②', '10', 1, 2),
  (110802, '③', '24', 0, 3),
  (110802, '④', '무한 반복', 0, 4),
  (110803, '①', '0', 0, 1),
  (110803, '②', 'number', 1, 2),
  (110803, '③', 'sum(number)', 0, 3),
  (110803, '④', '-1', 0, 4),
  (110804, '①', '0이 반환된다', 0, 1),
  (110804, '②', '정상 종료된다', 0, 2),
  (110804, '③', '호출이 계속 쌓여 오류가 발생한다', 1, 3),
  (110804, '④', '컴파일 오류', 0, 4),
  (110901, '①', '배열 전체', 0, 1),
  (110901, '②', '배열 객체를 가리키는 참조값', 1, 2),
  (110901, '③', '첫 요소만', 0, 3),
  (110901, '④', '아무것도 복사되지 않는다', 0, 4),
  (110902, '①', '5', 0, 1),
  (110902, '②', '10', 1, 2),
  (110902, '③', '20', 0, 3),
  (110902, '④', '0', 0, 4),
  (110903, '①', 'nums', 0, 1),
  (110903, '②', 'values', 1, 2),
  (110903, '③', 'array', 0, 3),
  (110903, '④', 'data', 0, 4),
  (110904, '①', '새 배열로 바뀐다', 0, 1),
  (110904, '②', '그대로 유지된다', 1, 2),
  (110904, '③', '삭제된다', 0, 3),
  (110904, '④', '컴파일 오류', 0, 4),
  (111001, '①', '코드 길이 10줄마다', 0, 1),
  (111001, '②', '한 가지 책임에 집중하도록', 1, 2),
  (111001, '③', '무조건 클래스당 한 개', 0, 3),
  (111001, '④', '반환형별로', 0, 4),
  (111002, '①', '0', 0, 1),
  (111002, '②', '80', 1, 2),
  (111002, '③', '240', 0, 3),
  (111002, '④', '컴파일 오류', 0, 4),
  (111003, '①', '1', 0, 1),
  (111003, '②', '-1', 0, 2),
  (111003, '③', '0', 1, 3),
  (111003, '④', 'count', 0, 4),
  (111004, '①', '통과', 1, 1),
  (111004, '②', '재도전', 0, 2),
  (111004, '③', 'true', 0, 3),
  (111004, '④', '80', 0, 4),
  (120101, '①', '상태(필드)와 동작(메서드)', 1, 1),
  (120101, '②', '조건문과 반복문', 0, 2),
  (120101, '③', '배열과 인덱스', 0, 3),
  (120101, '④', '컴파일과 실행', 0, 4),
  (120102, '①', '0', 0, 1),
  (120102, '②', '1', 0, 2),
  (120102, '③', '2', 1, 3),
  (120102, '④', '컴파일 오류', 0, 4),
  (120103, '①', '--', 0, 1),
  (120103, '②', '++', 1, 2),
  (120103, '③', '==', 0, 3),
  (120103, '④', '**', 0, 4),
  (120104, '①', '둘 다 2', 0, 1),
  (120104, '②', '각각 1', 1, 2),
  (120104, '③', '한쪽만 2', 0, 3),
  (120104, '④', '둘 다 0', 0, 4),
  (120201, '①', '클래스는 설계도, 객체는 그 설계도로 만든 실물이다', 1, 1),
  (120201, '②', '클래스와 객체는 같은 말이다', 0, 2),
  (120201, '③', '객체가 설계도, 클래스가 실물이다', 0, 3),
  (120201, '④', '클래스는 변수의 다른 이름이다', 0, 4),
  (120202, '①', 'null', 0, 1),
  (120202, '②', '민수', 1, 2),
  (120202, '③', 'name', 0, 3),
  (120202, '④', '컴파일 오류', 0, 4),
  (120203, '①', 'make', 0, 1),
  (120203, '②', 'create', 0, 2),
  (120203, '③', 'new', 1, 3),
  (120203, '④', 'this', 0, 4),
  (120204, '①', '만들 수 없다', 0, 1),
  (120204, '②', '가능하며 각 객체는 자신만의 필드값을 가진다', 1, 2),
  (120204, '③', '모두 같은 필드값을 공유한다', 0, 3),
  (120204, '④', '두 개까지만 가능하다', 0, 4),
  (120301, '①', '필드는 초기화 없이 읽으면 컴파일 오류다', 0, 1),
  (120301, '②', '필드는 객체 생성 시 기본값으로 초기화된다', 1, 2),
  (120301, '③', '지역 변수는 객체가 있는 동안 유지된다', 0, 3),
  (120301, '④', '차이가 없다', 0, 4),
  (120302, '①', '0', 1, 1),
  (120302, '②', 'null', 0, 2),
  (120302, '③', '컴파일 오류', 0, 3),
  (120302, '④', '값이 없다는 오류', 0, 4),
  (120303, '①', 'deposit', 0, 1),
  (120303, '②', 'balance', 1, 2),
  (120303, '③', 'account', 0, 3),
  (120303, '④', 'money()', 0, 4),
  (120304, '①', '""', 0, 1),
  (120304, '②', 'null', 1, 2),
  (120304, '③', '"null"', 0, 3),
  (120304, '④', '0', 0, 4),
  (120401, '①', '메서드(객체)', 0, 1),
  (120401, '②', '객체.메서드()', 1, 2),
  (120401, '③', '클래스::메서드', 0, 3),
  (120401, '④', 'new 메서드()', 0, 4),
  (120402, '①', '0', 0, 1),
  (120402, '②', '5000', 1, 2),
  (120402, '③', 'null', 0, 3),
  (120402, '④', '컴파일 오류', 0, 4),
  (120403, '①', '-=', 0, 1),
  (120403, '②', '+=', 1, 2),
  (120403, '③', '*=', 0, 3),
  (120403, '④', '==', 0, 4),
  (120404, '①', '항상 같은 결과가 나온다', 0, 1),
  (120404, '②', '호출한 객체의 필드값을 사용한다', 1, 2),
  (120404, '③', '마지막 객체의 필드만 바뀐다', 0, 3),
  (120404, '④', '컴파일 오류', 0, 4),
  (120501, '①', '클래스를 삭제한다', 0, 1),
  (120501, '②', '메모리에 객체를 만들고 참조를 반환한다', 1, 2),
  (120501, '③', '필드를 초기화하지 않는다', 0, 3),
  (120501, '④', '메서드를 실행한다', 0, 4),
  (120502, '①', 'null', 0, 1),
  (120502, '②', '영희', 1, 2),
  (120502, '③', 'first', 0, 3),
  (120502, '④', '컴파일 오류', 0, 4),
  (120503, '①', 'new Student()', 0, 1),
  (120503, '②', 'first', 1, 2),
  (120503, '③', 'null', 0, 3),
  (120503, '④', 'Student', 0, 4),
  (120504, '①', '객체 자체', 0, 1),
  (120504, '②', '객체를 찾아갈 수 있는 참조', 1, 2),
  (120504, '③', '필드값의 복사본', 0, 3),
  (120504, '④', '클래스 이름', 0, 4),
  (120601, '①', '반환형이 반드시 void다', 0, 1),
  (120601, '②', '클래스와 이름이 같고 반환형이 없다', 1, 2),
  (120601, '③', '이름을 자유롭게 짓는다', 0, 3),
  (120601, '④', '호출할 수 없다', 0, 4),
  (120602, '①', 'null', 0, 1),
  (120602, '②', '키보드', 1, 2),
  (120602, '③', 'name', 0, 3),
  (120602, '④', '컴파일 오류', 0, 4),
  (120603, '①', 'super', 0, 1),
  (120603, '②', 'this', 1, 2),
  (120603, '③', 'new', 0, 3),
  (120603, '④', 'static', 0, 4),
  (120604, '①', '객체를 만들 수 없다', 0, 1),
  (120604, '②', '매개변수 없는 기본 생성자가 제공된다', 1, 2),
  (120604, '③', '컴파일 오류', 0, 3),
  (120604, '④', '필드가 생성되지 않는다', 0, 4),
  (120701, '①', '부모 클래스', 0, 1),
  (120701, '②', '현재 메서드나 생성자가 실행되고 있는 객체 자신', 1, 2),
  (120701, '③', '새로 만들 객체', 0, 3),
  (120701, '④', 'main 메서드', 0, 4),
  (120702, '①', '매개변수에 필드값이 저장된다', 0, 1),
  (120702, '②', '필드에 매개변수 값이 저장된다', 1, 2),
  (120702, '③', '아무 일도 일어나지 않는다', 0, 3),
  (120702, '④', '컴파일 오류', 0, 4),
  (120703, '①', '==', 0, 1),
  (120703, '②', '=', 1, 2),
  (120703, '③', '+=', 0, 3),
  (120703, '④', ':', 0, 4),
  (120704, '①', '필드에 저장된다', 0, 1),
  (120704, '②', '매개변수가 자기 자신에 대입되어 필드는 그대로다', 1, 2),
  (120704, '③', '컴파일 오류', 0, 3),
  (120704, '④', '실행 중 오류', 0, 4),
  (120801, '①', '어디서나', 0, 1),
  (120801, '②', '같은 패키지', 0, 2),
  (120801, '③', '선언한 클래스 내부에서만', 1, 3),
  (120801, '④', '자식 클래스까지', 0, 4),
  (120802, '①', '0이 출력된다', 0, 1),
  (120802, '②', '컴파일 오류', 1, 2),
  (120802, '③', 'null이 출력된다', 0, 3),
  (120802, '④', '실행 중 오류', 0, 4),
  (120803, '①', 'public', 0, 1),
  (120803, '②', 'static', 0, 2),
  (120803, '③', 'private', 1, 3),
  (120803, '④', 'final', 0, 4),
  (120804, '①', '직접 접근한다', 0, 1),
  (120804, '②', 'public 메서드 getBalance()를 통해 읽는다', 1, 2),
  (120804, '③', '읽을 수 없다', 0, 3),
  (120804, '④', '클래스를 복사한다', 0, 4),
  (120901, '①', '모든 필드를 public으로 공개한다', 0, 1),
  (120901, '②', '내부 상태를 감추고 공개된 메서드로만 다루게 한다', 1, 2),
  (120901, '③', '클래스를 하나로 합친다', 0, 3),
  (120901, '④', '상속을 금지한다', 0, 4),
  (120902, '①', '-1000', 0, 1),
  (120902, '②', '0', 0, 2),
  (120902, '③', '1000', 1, 3),
  (120902, '④', '2000', 0, 4),
  (120903, '①', '||', 0, 1),
  (120903, '②', '&&', 1, 2),
  (120903, '③', '!', 0, 3),
  (120903, '④', '==', 0, 4),
  (120904, '①', '1000', 0, 1),
  (120904, '②', '500', 1, 2),
  (120904, '③', '0', 0, 3),
  (120904, '④', '1500', 0, 4),
  (121001, '①', '저장할 상태·수행할 동작·생성 시 필요한 값', 1, 1),
  (121001, '②', '파일 이름·폴더 위치', 0, 2),
  (121001, '③', '출력 형식·색상', 0, 3),
  (121001, '④', '반복 횟수·조건식', 0, 4),
  (121002, '①', 'true', 1, 1),
  (121002, '②', 'false', 0, 2),
  (121002, '③', '85', 0, 3),
  (121002, '④', '민수', 0, 4),
  (121003, '①', '>', 0, 1),
  (121003, '②', '>=', 1, 2),
  (121003, '③', '==', 0, 3),
  (121003, '④', '<=', 0, 4),
  (121004, '①', 'true', 0, 1),
  (121004, '②', 'false', 1, 2),
  (121004, '③', '60', 0, 3),
  (121004, '④', '컴파일 오류', 0, 4),
  (130101, '①', '자식이 부모의 한 종류일 때', 1, 1),
  (130101, '②', '코드 복사가 귀찮을 때', 0, 2),
  (130101, '③', '두 클래스가 무관할 때', 0, 3),
  (130101, '④', '파일을 줄이고 싶을 때', 0, 4),
  (130102, '①', '먹기', 1, 1),
  (130102, '②', 'eat', 0, 2),
  (130102, '③', '컴파일 오류', 0, 3),
  (130102, '④', '출력 없음', 0, 4),
  (130103, '①', 'implements', 0, 1),
  (130103, '②', 'extends', 1, 2),
  (130103, '③', 'inherits', 0, 3),
  (130103, '④', 'super', 0, 4),
  (130104, '①', '다시 선언해야 사용 가능', 0, 1),
  (130104, '②', '이어받아 그대로 사용 가능', 1, 2),
  (130104, '③', '사용할 수 없다', 0, 3),
  (130104, '④', '메서드만 이어받는다', 0, 4),
  (130201, '①', '제한 없음', 0, 1),
  (130201, '②', '두 개', 0, 2),
  (130201, '③', '하나', 1, 3),
  (130201, '④', '없음', 0, 4),
  (130202, '①', '10', 0, 1),
  (130202, '②', '20', 1, 2),
  (130202, '③', '0', 0, 3),
  (130202, '④', '컴파일 오류', 0, 4),
  (130203, '①', 'implements', 0, 1),
  (130203, '②', 'extends', 1, 2),
  (130203, '③', 'include', 0, 3),
  (130203, '④', 'import', 0, 4),
  (130204, '①', '정상 접근', 0, 1),
  (130204, '②', '컴파일 오류', 1, 2),
  (130204, '③', 'null 반환', 0, 3),
  (130204, '④', '실행 중 오류', 0, 4),
  (130301, '①', '자기 자신을 다시 생성한다', 0, 1),
  (130301, '②', '부모 생성자를 호출한다', 1, 2),
  (130301, '③', '자식 생성자를 호출한다', 0, 3),
  (130301, '④', '객체를 삭제한다', 0, 4),
  (130302, '①', 'null', 0, 1),
  (130302, '②', '수진', 1, 2),
  (130302, '③', 'name', 0, 3),
  (130302, '④', '컴파일 오류', 0, 4),
  (130303, '①', 'this', 0, 1),
  (130303, '②', 'super', 1, 2),
  (130303, '③', 'new', 0, 3),
  (130303, '④', 'parent', 0, 4),
  (130304, '①', '자식 먼저', 0, 1),
  (130304, '②', '부모 먼저', 1, 2),
  (130304, '③', '순서 없음', 0, 3),
  (130304, '④', '동시에', 0, 4),
  (130401, '①', '이름은 같고 매개변수가 다른 메서드를 여러 개 두는 것', 0, 1),
  (130401, '②', '자식이 부모의 인스턴스 메서드를 같은 선언 형태로 다시 구현하는 것', 1, 2),
  (130401, '③', '부모 메서드를 삭제하는 것', 0, 3),
  (130401, '④', 'static 메서드를 복사하는 것', 0, 4),
  (130402, '①', '소리', 0, 1),
  (130402, '②', '멍멍', 1, 2),
  (130402, '③', '소리 멍멍', 0, 3),
  (130402, '④', '컴파일 오류', 0, 4),
  (130403, '①', '@Override', 1, 1),
  (130403, '②', '@Main', 0, 2),
  (130403, '③', '@Super', 0, 3),
  (130403, '④', '@Final', 0, 4),
  (130404, '①', '새 메서드로 처리된다', 0, 1),
  (130404, '②', '컴파일 오류가 발생한다', 1, 2),
  (130404, '③', '부모 메서드가 실행된다', 0, 3),
  (130404, '④', '실행 중 오류', 0, 4),
  (130501, '①', '부모 타입 변수 하나로 여러 자식 객체를 다룰 수 있다', 1, 1),
  (130501, '②', '자식 타입 변수로 부모 객체를 다룬다', 0, 2),
  (130501, '③', '모든 메서드가 static이 된다', 0, 3),
  (130501, '④', '필드가 사라진다', 0, 4),
  (130502, '①', '동물 소리', 0, 1),
  (130502, '②', '멍멍', 1, 2),
  (130502, '③', '컴파일 오류', 0, 3),
  (130502, '④', '출력 없음', 0, 4),
  (130503, '①', 'Dog', 0, 1),
  (130503, '②', 'Animal', 1, 2),
  (130503, '③', 'void', 0, 3),
  (130503, '④', 'Object[]', 0, 4),
  (130504, '①', '실제 객체 타입', 0, 1),
  (130504, '②', '변수(참조) 타입', 1, 2),
  (130504, '③', '메서드 개수', 0, 3),
  (130504, '④', '필드 개수', 0, 4),
  (130601, '①', '명시적 캐스팅이 필요하다', 0, 1),
  (130601, '②', '자식 객체를 부모 타입 변수에 저장하는 것으로 자동으로 이루어진다', 1, 2),
  (130601, '③', '실행 중 예외가 발생할 수 있다', 0, 3),
  (130601, '④', '실제 객체가 부모 객체로 바뀐다', 0, 4),
  (130602, '①', '멍멍 야옹', 1, 1),
  (130602, '②', '동물 소리 두 번', 0, 2),
  (130602, '③', '야옹 멍멍', 0, 3),
  (130602, '④', '컴파일 오류', 0, 4),
  (130603, '①', 'Animal[]', 0, 1),
  (130603, '②', 'Cat', 1, 2),
  (130603, '③', 'void', 0, 3),
  (130603, '④', 'sound', 0, 4),
  (130604, '①', '부모 객체로 바뀐다', 0, 1),
  (130604, '②', '바뀌지 않는다', 1, 2),
  (130604, '③', '삭제된다', 0, 3),
  (130604, '④', '복사된다', 0, 4),
  (130701, '①', '자동으로 이루어진다', 0, 1),
  (130701, '②', '명시적 캐스팅이 필요하다', 1, 2),
  (130701, '③', '항상 안전하다', 0, 3),
  (130701, '④', '컴파일이 불가능하다', 0, 4),
  (130702, '①', '동물 소리', 0, 1),
  (130702, '②', '멍멍', 1, 2),
  (130702, '③', '컴파일 오류', 0, 3),
  (130702, '④', '실행 중 오류', 0, 4),
  (130703, '①', 'Animal', 0, 1),
  (130703, '②', 'Dog', 1, 2),
  (130703, '③', 'Object', 0, 3),
  (130703, '④', 'void', 0, 4),
  (130704, '①', '정상 변환', 0, 1),
  (130704, '②', '컴파일 오류', 0, 2),
  (130704, '③', '실행 중 ClassCastException', 1, 3),
  (130704, '④', 'null 반환', 0, 4),
  (130801, '①', 'int', 0, 1),
  (130801, '②', 'String', 0, 2),
  (130801, '③', 'boolean', 1, 3),
  (130801, '④', 'Object', 0, 4),
  (130802, '①', 'true', 0, 1),
  (130802, '②', 'false', 1, 2),
  (130802, '③', '실행 중 예외', 0, 3),
  (130802, '④', '컴파일 오류', 0, 4),
  (130803, '①', 'instanceof', 1, 1),
  (130803, '②', 'extends', 0, 2),
  (130803, '③', 'implements', 0, 3),
  (130803, '④', 'equals', 0, 4),
  (130804, '①', '항상', 0, 1),
  (130804, '②', '조건이 참일 때만', 1, 2),
  (130804, '③', '조건이 거짓일 때만', 0, 3),
  (130804, '④', '컴파일 시', 0, 4),
  (130901, '①', '객체를 만들 수 없다', 0, 1),
  (130901, '②', '상속할 수 없다', 1, 2),
  (130901, '③', '메서드를 가질 수 없다', 0, 3),
  (130901, '④', '필드를 가질 수 없다', 0, 4),
  (130902, '①', '정상 컴파일', 0, 1),
  (130902, '②', '컴파일 오류', 1, 2),
  (130902, '③', '실행 중 오류', 0, 3),
  (130902, '④', '경고만 발생', 0, 4),
  (130903, '①', 'static', 0, 1),
  (130903, '②', 'private', 0, 2),
  (130903, '③', 'final', 1, 3),
  (130903, '④', 'abstract', 0, 4),
  (130904, '①', '정상 동작', 0, 1),
  (130904, '②', '컴파일 오류', 1, 2),
  (130904, '③', '부모 구현이 실행됨', 0, 3),
  (130904, '④', '실행 중 오류', 0, 4),
  (131001, '①', '모든 자식의 개별 기능', 0, 1),
  (131001, '②', '진짜 공통인 상태와 동작', 1, 2),
  (131001, '③', '사용하지 않는 코드', 0, 3),
  (131001, '④', 'main 메서드', 0, 4),
  (131002, '①', '동물 소리', 0, 1),
  (131002, '②', '멍멍', 1, 2),
  (131002, '③', '컴파일 오류', 0, 3),
  (131002, '④', '출력 없음', 0, 4),
  (131003, '①', 'Dog', 0, 1),
  (131003, '②', 'Cat', 0, 2),
  (131003, '③', 'Animal', 1, 3),
  (131003, '④', 'String', 0, 4),
  (131004, '①', '멍멍', 0, 1),
  (131004, '②', '야옹', 1, 2),
  (131004, '③', '동물 소리', 0, 3),
  (131004, '④', '컴파일 오류', 0, 4),
  (140101, '①', '구현 코드를 반드시 포함한다', 0, 1),
  (140101, '②', '구현 클래스가 제공해야 할 기능의 규칙(계약)을 선언한다', 1, 2),
  (140101, '③', '객체를 직접 만들 수 있다', 0, 3),
  (140101, '④', '필드 상속이 목적이다', 0, 4),
  (140102, '①', 'print', 0, 1),
  (140102, '②', '출력', 1, 2),
  (140102, '③', '컴파일 오류', 0, 3),
  (140102, '④', '출력 없음', 0, 4),
  (140103, '①', 'int', 0, 1),
  (140103, '②', 'String', 0, 2),
  (140103, '③', 'void', 1, 3),
  (140103, '④', 'Printer', 0, 4),
  (140104, '①', '모두 수정해야 한다', 0, 1),
  (140104, '②', '그대로 사용할 수 있다', 1, 2),
  (140104, '③', '삭제해야 한다', 0, 3),
  (140104, '④', '컴파일 오류', 0, 4),
  (140201, '①', '하나만', 0, 1),
  (140201, '②', '두 개까지', 0, 2),
  (140201, '③', '쉼표로 나열해 여러 개 가능', 1, 3),
  (140201, '④', '구현할 수 없다', 0, 4),
  (140202, '①', 'run', 0, 1),
  (140202, '②', '다운로드', 1, 2),
  (140202, '③', '컴파일 오류', 0, 3),
  (140202, '④', '출력 없음', 0, 4),
  (140203, '①', 'extends', 0, 1),
  (140203, '②', 'implements', 1, 2),
  (140203, '③', 'interface', 0, 3),
  (140203, '④', 'abstract', 0, 4),
  (140204, '①', '기본 구현이 사용된다', 0, 1),
  (140204, '②', '컴파일 오류가 발생한다', 1, 2),
  (140204, '③', '실행 중 오류', 0, 3),
  (140204, '④', '무시된다', 0, 4),
  (140301, '①', '본문을 중괄호로 작성한다', 0, 1),
  (140301, '②', '실행 본문 없이 선언만 하고 세미콜론으로 끝낸다', 1, 2),
  (140301, '③', 'private으로 선언한다', 0, 3),
  (140301, '④', '반환형이 없다', 0, 4),
  (140302, '①', '23', 0, 1),
  (140302, '②', '5', 1, 2),
  (140302, '③', '6', 0, 3),
  (140302, '④', '컴파일 오류', 0, 4),
  (140303, '①', '{ }', 0, 1),
  (140303, '②', ';', 1, 2),
  (140303, '③', ':', 0, 3),
  (140303, '④', '->', 0, 4),
  (140304, '①', '같은 동작을 한다', 0, 1),
  (140304, '②', '같은 이름으로 서로 다른 계산을 한다', 1, 2),
  (140304, '③', '공존할 수 없다', 0, 3),
  (140304, '④', '컴파일 오류', 0, 4),
  (140401, '①', '해당 인터페이스를 구현한 객체', 1, 1),
  (140401, '②', '아무 객체나', 0, 2),
  (140401, '③', '기본형 값', 0, 3),
  (140401, '④', '클래스 이름', 0, 4),
  (140402, '①', 'printer', 0, 1),
  (140402, '②', '출력', 1, 2),
  (140402, '③', '컴파일 오류', 0, 3),
  (140402, '④', '출력 없음', 0, 4),
  (140403, '①', 'void', 0, 1),
  (140403, '②', 'Printer', 1, 2),
  (140403, '③', 'String', 0, 3),
  (140403, '④', 'Console', 0, 4),
  (140404, '①', '인터페이스의 선언', 0, 1),
  (140404, '②', '대입된 객체의 구현', 1, 2),
  (140404, '③', '컴파일러가 만든 코드', 0, 3),
  (140404, '④', '실행되지 않는다', 0, 4),
  (140501, '①', '인터페이스에 기본 구현을 제공한다', 1, 1),
  (140501, '②', '구현 클래스에서 사용할 수 없다', 0, 2),
  (140501, '③', '반드시 오버라이딩해야 한다', 0, 3),
  (140501, '④', 'static과 같다', 0, 4),
  (140502, '①', '컴파일 오류', 0, 1),
  (140502, '②', '안녕하세요', 1, 2),
  (140502, '③', 'greet', 0, 3),
  (140502, '④', '출력 없음', 0, 4),
  (140503, '①', 'static', 0, 1),
  (140503, '②', 'default', 1, 2),
  (140503, '③', 'final', 0, 3),
  (140503, '④', 'abstract', 0, 4),
  (140504, '①', '불가능하다', 0, 1),
  (140504, '②', '가능하다', 1, 2),
  (140504, '③', '컴파일 오류', 0, 3),
  (140504, '④', 'static일 때만 가능', 0, 4),
  (140601, '①', '구현 객체', 0, 1),
  (140601, '②', '구현 클래스', 0, 2),
  (140601, '③', '인터페이스 자체', 1, 3),
  (140601, '④', 'main 메서드', 0, 4),
  (140602, '①', '5', 0, 1),
  (140602, '②', '10', 1, 2),
  (140602, '③', '25', 0, 3),
  (140602, '④', '컴파일 오류', 0, 4),
  (140603, '①', 'default', 0, 1),
  (140603, '②', 'static', 1, 2),
  (140603, '③', 'final', 0, 3),
  (140603, '④', 'void', 0, 4),
  (140604, '①', '정상 호출', 0, 1),
  (140604, '②', '컴파일 오류', 1, 2),
  (140604, '③', '실행 중 오류', 0, 3),
  (140604, '④', '0이 반환된다', 0, 4),
  (140701, '①', 'implements로 하나만 상속한다', 0, 1),
  (140701, '②', 'extends로 하나 이상의 인터페이스를 상속할 수 있다', 1, 2),
  (140701, '③', '상속할 수 없다', 0, 3),
  (140701, '④', '클래스를 상속한다', 0, 4),
  (140702, '①', 'read만 구현', 0, 1),
  (140702, '②', 'write만 구현', 0, 2),
  (140702, '③', 'read와 write 모두 구현', 1, 3),
  (140702, '④', '아무것도 구현 안 해도 됨', 0, 4),
  (140703, '①', 'implements', 0, 1),
  (140703, '②', 'extends', 1, 2),
  (140703, '③', 'include', 0, 3),
  (140703, '④', 'union', 0, 4),
  (140704, '①', '불가능', 0, 1),
  (140704, '②', '가능', 1, 2),
  (140704, '③', '캐스팅 필요', 0, 3),
  (140704, '④', '컴파일 오류', 0, 4),
  (140801, '①', '객체를 직접 만들 수 있다', 0, 1),
  (140801, '②', '필드·생성자·일반 메서드와 추상 메서드를 함께 가질 수 있다', 1, 2),
  (140801, '③', '추상 메서드만 가질 수 있다', 0, 3),
  (140801, '④', '여러 개를 동시에 상속할 수 있다', 0, 4),
  (140802, '①', '3.0', 0, 1),
  (140802, '②', '6.0', 0, 2),
  (140802, '③', '9.0', 1, 3),
  (140802, '④', '컴파일 오류', 0, 4),
  (140803, '①', 'final', 0, 1),
  (140803, '②', 'abstract', 1, 2),
  (140803, '③', 'static', 0, 3),
  (140803, '④', 'interface', 0, 4),
  (140804, '①', '정상 생성', 0, 1),
  (140804, '②', '컴파일 오류', 1, 2),
  (140804, '③', 'area가 0인 객체 생성', 0, 3),
  (140804, '④', '실행 중 오류', 0, 4),
  (140901, '①', '컴파일이 느려진다', 0, 1),
  (140901, '②', '구현을 바꿀 때마다 서비스 코드를 수정해야 한다', 1, 2),
  (140901, '③', '객체가 생성되지 않는다', 0, 3),
  (140901, '④', '상속이 불가능해진다', 0, 4),
  (140902, '①', '인터페이스 선언', 0, 1),
  (140902, '②', '전달된 구현 객체의 send', 1, 2),
  (140902, '③', '아무것도 실행 안 함', 0, 3),
  (140902, '④', '컴파일 오류', 0, 4),
  (140903, '①', 'MailSender', 0, 1),
  (140903, '②', 'Sender', 1, 2),
  (140903, '③', 'void', 0, 3),
  (140903, '④', 'String', 0, 4),
  (140904, '①', '수정해야 한다', 0, 1),
  (140904, '②', '수정할 필요 없다', 1, 2),
  (140904, '③', '삭제해야 한다', 0, 3),
  (140904, '④', '오버로딩해야 한다', 0, 4),
  (141001, '①', '코드가 무조건 짧아진다', 0, 1),
  (141001, '②', '사용하는 쪽에 필요한 기능만 공개하고 구현 세부사항을 숨긴다', 1, 2),
  (141001, '③', '실행 속도가 빨라진다', 0, 3),
  (141001, '④', '클래스 수가 줄어든다', 0, 4),
  (141002, '①', '5000', 0, 1),
  (141002, '②', '4000', 1, 2),
  (141002, '③', '1000', 0, 3),
  (141002, '④', '컴파일 오류', 0, 4),
  (141003, '①', 'price', 0, 1),
  (141003, '②', 'pay', 1, 2),
  (141003, '③', 'send', 0, 3),
  (141003, '④', 'calc', 0, 4),
  (141004, '①', 'CouponPayment가 Payment를 구현했기 때문', 1, 1),
  (141004, '②', '자동 캐스팅이 금지되어 있기 때문', 0, 2),
  (141004, '③', '둘이 같은 클래스이기 때문', 0, 3),
  (141004, '④', '불가능하다', 0, 4),
  (150101, '①', '오류는 코드에서 처리하고 예외는 처리할 수 없다', 0, 1),
  (150101, '②', '오류는 JVM 수준의 복구 어려운 문제, 예외는 코드에서 대비·처리할 수 있는 문제다', 1, 2),
  (150101, '③', '둘은 같은 개념이다', 0, 3),
  (150101, '④', '예외는 컴파일 전에만 발생한다', 0, 4),
  (150102, '①', '0이 저장된다', 0, 1),
  (150102, '②', '컴파일 오류', 0, 2),
  (150102, '③', '실행 중 예외(NumberFormatException) 발생', 1, 3),
  (150102, '④', '"Java"가 저장된다', 0, 4),
  (150103, '①', 'toInt', 0, 1),
  (150103, '②', 'parseInt', 1, 2),
  (150103, '③', 'valueOf()만 가능', 0, 3),
  (150103, '④', 'cast', 0, 4),
  (150104, '①', '예외 발생', 0, 1),
  (150104, '②', '123', 1, 2),
  (150104, '③', '"123"', 0, 3),
  (150104, '④', '컴파일 오류', 0, 4),
  (150201, '①', 'try에 복구 코드, catch에 위험 코드', 0, 1),
  (150201, '②', 'try에 예외 가능 코드, catch에 처리(복구) 코드', 1, 2),
  (150201, '③', '둘 다 항상 실행된다', 0, 3),
  (150201, '④', 'catch가 먼저 실행된다', 0, 4),
  (150202, '①', '0', 0, 1),
  (150202, '②', '0으로 나눌 수 없습니다', 1, 2),
  (150202, '③', '프로그램 중단', 0, 3),
  (150202, '④', '컴파일 오류', 0, 4),
  (150203, '①', 'case', 0, 1),
  (150203, '②', 'catch', 1, 2),
  (150203, '③', 'finally', 0, 3),
  (150203, '④', 'throw', 0, 4),
  (150204, '①', '계속 실행된다', 0, 1),
  (150204, '②', '건너뛴다', 1, 2),
  (150204, '③', '두 번 실행된다', 0, 3),
  (150204, '④', 'finally로 이동한다', 0, 4),
  (150301, '①', '예외가 발생할 때만 실행된다', 0, 1),
  (150301, '②', '예외 발생 여부와 관계없이 실행된다', 1, 2),
  (150301, '③', '예외가 없을 때만 실행된다', 0, 3),
  (150301, '④', '실행되지 않는다', 0, 4),
  (150302, '①', '정리 → 작업', 0, 1),
  (150302, '②', '작업 → 정리', 1, 2),
  (150302, '③', '작업만', 0, 3),
  (150302, '④', '정리만', 0, 4),
  (150303, '①', 'catch', 0, 1),
  (150303, '②', 'finally', 1, 2),
  (150303, '③', 'after', 0, 3),
  (150303, '④', 'close', 0, 4),
  (150304, '①', '실행되지 않는다', 0, 1),
  (150304, '②', '실행된다', 1, 2),
  (150304, '③', '두 번 실행된다', 0, 3),
  (150304, '④', '컴파일 오류', 0, 4),
  (150401, '①', '예외를 그 자리에서 처리한다', 0, 1),
  (150401, '②', '처리하지 않은 예외를 호출한 곳으로 전달한다고 선언한다', 1, 2),
  (150401, '③', '예외 발생을 막는다', 0, 3),
  (150401, '④', '예외를 삭제한다', 0, 4),
  (150402, '①', '아무것도 없다', 0, 1),
  (150402, '②', 'try-catch로 처리하거나 다시 throws로 넘긴다', 1, 2),
  (150402, '③', '반드시 무시한다', 0, 3),
  (150402, '④', '메서드를 수정한다', 0, 4),
  (150403, '①', 'throw', 0, 1),
  (150403, '②', 'throws', 1, 2),
  (150403, '③', 'catch', 0, 3),
  (150403, '④', 'finally', 0, 4),
  (150404, '①', '자동 복구된다', 0, 1),
  (150404, '②', '스택 추적을 출력하며 프로그램이 종료된다', 1, 2),
  (150404, '③', '무한 대기한다', 0, 3),
  (150404, '④', '컴파일 오류', 0, 4),
  (150501, '①', '실행 중에만 알 수 있다', 0, 1),
  (150501, '②', '컴파일러가 처리 여부를 확인한다', 1, 2),
  (150501, '③', '처리할 수 없다', 0, 3),
  (150501, '④', 'RuntimeException 계열이다', 0, 4),
  (150502, '①', '0', 0, 1),
  (150502, '②', 'null', 0, 2),
  (150502, '③', '실행 중 NullPointerException', 1, 3),
  (150502, '④', '컴파일 오류', 0, 4),
  (150503, '①', 'IO', 0, 1),
  (150503, '②', 'Runtime', 1, 2),
  (150503, '③', 'File', 0, 3),
  (150503, '④', 'Checked', 0, 4),
  (150504, '①', 'checked', 1, 1),
  (150504, '②', 'unchecked', 0, 2),
  (150504, '③', 'Error', 0, 3),
  (150504, '④', '예외가 아니다', 0, 4),
  (150601, '①', '표준 예외를 없애기 위해', 0, 1),
  (150601, '②', '업무 규칙 위반을 명확하게 표현하기 위해', 1, 2),
  (150601, '③', '컴파일을 빠르게 하기 위해', 0, 3),
  (150601, '④', '예외를 숨기기 위해', 0, 4),
  (150602, '①', '아무 일도 없다', 0, 1),
  (150602, '②', '해당 예외가 발생한다', 1, 2),
  (150602, '③', '컴파일 오류', 0, 3),
  (150602, '④', '예외가 무시된다', 0, 4),
  (150603, '①', 'implements', 0, 1),
  (150603, '②', 'extends', 1, 2),
  (150603, '③', 'throws', 0, 3),
  (150603, '④', 'new', 0, 4),
  (150604, '①', '클래스 이름', 0, 1),
  (150604, '②', '생성 시 전달한 메시지', 1, 2),
  (150604, '③', '스택 추적 전체', 0, 3),
  (150604, '④', 'null만', 0, 4),
  (150701, '①', '루트 디렉터리', 0, 1),
  (150701, '②', '프로그램 실행 위치', 1, 2),
  (150701, '③', '홈 디렉터리', 0, 3),
  (150701, '④', 'src 폴더', 0, 4),
  (150702, '①', 'data\\scores.txt', 0, 1),
  (150702, '②', 'data/scores.txt', 1, 2),
  (150702, '③', 'data scores.txt', 0, 3),
  (150702, '④', '컴파일 오류', 0, 4),
  (150703, '①', 'get', 0, 1),
  (150703, '②', 'of', 1, 2),
  (150703, '③', 'new', 0, 3),
  (150703, '④', 'make', 0, 4),
  (150704, '①', '/', 0, 1),
  (150704, '②', '\\', 1, 2),
  (150704, '③', ':', 0, 3),
  (150704, '④', '출력되지 않는다', 0, 4),
  (150801, '①', '매우 큰 파일', 0, 1),
  (150801, '②', '작은 텍스트 파일 전체 읽기', 1, 2),
  (150801, '③', '이미지 파일', 0, 3),
  (150801, '④', '폴더 삭제', 0, 4),
  (150802, '①', '빈 문자열 반환', 0, 1),
  (150802, '②', 'null 반환', 0, 2),
  (150802, '③', 'IOException 발생', 1, 3),
  (150802, '④', '컴파일 오류', 0, 4),
  (150803, '①', 'readAll', 0, 1),
  (150803, '②', 'readString', 1, 2),
  (150803, '③', 'getText', 0, 3),
  (150803, '④', 'open', 0, 4),
  (150804, '①', 'readString만', 0, 1),
  (150804, '②', 'BufferedReader나 readAllLines', 1, 2),
  (150804, '③', 'writeString', 0, 3),
  (150804, '④', 'Path.of', 0, 4),
  (150901, '①', '기존 내용 뒤에 이어 쓴다', 0, 1),
  (150901, '②', '기존 내용을 덮어쓴다', 1, 2),
  (150901, '③', '파일을 삭제한다', 0, 3),
  (150901, '④', '읽기만 한다', 0, 4),
  (150902, '①', '두 내용이 합쳐진다', 0, 1),
  (150902, '②', '마지막 내용만 남는다', 1, 2),
  (150902, '③', '첫 내용만 남는다', 0, 3),
  (150902, '④', '오류 발생', 0, 4),
  (150903, '①', 'writeString', 1, 1),
  (150903, '②', 'readString', 0, 2),
  (150903, '③', 'printString', 0, 3),
  (150903, '④', 'saveString', 0, 4),
  (150904, '①', 'StandardOpenOption.APPEND', 1, 1),
  (150904, '②', 'StandardOpenOption.READ', 0, 2),
  (150904, '③', 'Path.APPEND', 0, 3),
  (150904, '④', '옵션 없음', 0, 4),
  (151001, '①', '예외 처리 → 읽기 → 경로 준비', 0, 1),
  (151001, '②', '경로 준비 → 읽기·쓰기 → 예외 처리', 1, 2),
  (151001, '③', '읽기 → 삭제 → 경로 준비', 0, 3),
  (151001, '④', '순서가 없다', 0, 4),
  (151002, '①', '0', 0, 1),
  (151002, '②', '파일을 확인하세요', 1, 2),
  (151002, '③', '프로그램 중단', 0, 3),
  (151002, '④', '컴파일 오류', 0, 4),
  (151003, '①', 'NullPointerException', 0, 1),
  (151003, '②', 'IOException', 1, 2),
  (151003, '③', 'ArithmeticException', 0, 3),
  (151003, '④', 'ClassCastException', 0, 4),
  (151004, '①', '예외가 사라진다', 0, 1),
  (151004, '②', '작업이 끝날 때 자원이 자동으로 닫힌다', 1, 2),
  (151004, '③', '컴파일이 생략된다', 0, 3),
  (151004, '④', '파일이 암호화된다', 0, 4),
  (210101, '①', '크기가 고정된다', 0, 1),
  (210101, '②', '크기를 유연하게 바꿀 수 있고 목적별 구조(List·Set·Map)를 선택할 수 있다', 1, 2),
  (210101, '③', '기본형만 저장한다', 0, 3),
  (210101, '④', '순회할 수 없다', 0, 4),
  (210102, '①', '1', 0, 1),
  (210102, '②', '2', 1, 2),
  (210102, '③', '0', 0, 3),
  (210102, '④', '컴파일 오류', 0, 4),
  (210103, '①', 'List', 0, 1),
  (210103, '②', 'ArrayList', 1, 2),
  (210103, '③', 'Map', 0, 3),
  (210103, '④', 'Array', 0, 4),
  (210104, '①', '정상 저장', 0, 1),
  (210104, '②', '컴파일 오류', 1, 2),
  (210104, '③', '실행 중 오류', 0, 3),
  (210104, '④', '문자열로 변환된다', 0, 4),
  (210201, '①', '중복을 허용하지 않는다', 0, 1),
  (210201, '②', '입력 순서를 유지하고 중복을 허용한다', 1, 2),
  (210201, '③', '키와 값을 저장한다', 0, 3),
  (210201, '④', '인덱스가 없다', 0, 4),
  (210202, '①', 'A', 0, 1),
  (210202, '②', 'B', 1, 2),
  (210202, '③', '[A, B, A]', 0, 3),
  (210202, '④', '컴파일 오류', 0, 4),
  (210203, '①', 'at', 0, 1),
  (210203, '②', 'get', 1, 2),
  (210203, '③', 'read', 0, 3),
  (210203, '④', 'index', 0, 4),
  (210204, '①', '예외 발생', 0, 1),
  (210204, '②', '중복이 제거된다', 0, 2),
  (210204, '③', '중복이 허용되어 그대로 만들어진다', 1, 3),
  (210204, '④', '컴파일 오류', 0, 4),
  (210301, '①', '인덱스 조회가 빠르지만 중간 삽입·삭제는 뒤 요소 이동이 필요할 수 있다', 1, 1),
  (210301, '②', '조회가 느리고 삽입이 항상 빠르다', 0, 2),
  (210301, '③', '키·값 쌍으로 저장한다', 0, 3),
  (210301, '④', '중복을 허용하지 않는다', 0, 4),
  (210302, '①', '[10, 20]', 0, 1),
  (210302, '②', '[15, 20]', 1, 2),
  (210302, '③', '[15]', 0, 3),
  (210302, '④', '[10, 15, 20]', 0, 4),
  (210303, '①', 'add', 0, 1),
  (210303, '②', 'put', 0, 2),
  (210303, '③', 'set', 1, 3),
  (210303, '④', 'replace', 0, 4),
  (210304, '①', '주소만 출력된다', 0, 1),
  (210304, '②', '[요소, 요소] 형태로 출력된다', 1, 2),
  (210304, '③', '컴파일 오류', 0, 3),
  (210304, '④', '개수만 출력된다', 0, 4),
  (210401, '①', '중복 요소를 허용하지 않으며 일반적으로 인덱스가 없다', 1, 1),
  (210401, '②', '인덱스로 조회한다', 0, 2),
  (210401, '③', '키·값 쌍을 저장한다', 0, 3),
  (210401, '④', '항상 정렬되어 있다', 0, 4),
  (210402, '①', '0', 0, 1),
  (210402, '②', '1', 1, 2),
  (210402, '③', '2', 0, 3),
  (210402, '④', '오류 발생', 0, 4),
  (210403, '①', 'ArrayList', 0, 1),
  (210403, '②', 'HashMap', 0, 2),
  (210403, '③', 'HashSet', 1, 3),
  (210403, '④', 'Set', 0, 4),
  (210404, '①', 'true', 0, 1),
  (210404, '②', 'false', 1, 2),
  (210404, '③', 'null', 0, 3),
  (210404, '④', '예외 발생', 0, 4),
  (210501, '①', '저장 순서를 항상 유지한다', 0, 1),
  (210501, '②', '해시값으로 저장·검색하며 저장 순서를 보장하지 않는다', 1, 2),
  (210501, '③', '인덱스로 접근한다', 0, 3),
  (210501, '④', '중복을 허용한다', 0, 4),
  (210502, '①', 'true', 1, 1),
  (210502, '②', 'false', 0, 2),
  (210502, '③', '2', 0, 3),
  (210502, '④', '오류 발생', 0, 4),
  (210503, '①', 'get', 0, 1),
  (210503, '②', 'contains', 1, 2),
  (210503, '③', 'find', 0, 3),
  (210503, '④', 'has', 0, 4),
  (210504, '①', '4', 0, 1),
  (210504, '②', '3', 1, 2),
  (210504, '③', '2', 0, 3),
  (210504, '④', '1', 0, 4),
  (210601, '①', '고유한 키와 값의 쌍을 저장하며 같은 키로 다시 저장하면 값이 교체된다', 1, 1),
  (210601, '②', '중복 키를 허용한다', 0, 2),
  (210601, '③', '인덱스로 조회한다', 0, 3),
  (210601, '④', '값이 고유해야 한다', 0, 4),
  (210602, '①', '80', 0, 1),
  (210602, '②', '90', 1, 2),
  (210602, '③', '170', 0, 3),
  (210602, '④', 'null', 0, 4),
  (210603, '①', 'add', 0, 1),
  (210603, '②', 'set', 0, 2),
  (210603, '③', 'put', 1, 3),
  (210603, '④', 'insert', 0, 4),
  (210604, '①', '0', 0, 1),
  (210604, '②', '예외 발생', 0, 2),
  (210604, '③', 'null', 1, 3),
  (210604, '④', '빈 문자열', 0, 4),
  (210701, '①', '키의 저장 순서를 보장한다', 0, 1),
  (210701, '②', '키 순서를 보장하지 않으며 없는 키는 null을 반환한다', 1, 2),
  (210701, '③', '값으로 조회한다', 0, 3),
  (210701, '④', '중복 키를 허용한다', 0, 4),
  (210702, '①', 'null', 0, 1),
  (210702, '②', '0', 1, 2),
  (210702, '③', '2', 0, 3),
  (210702, '④', '오류 발생', 0, 4),
  (210703, '①', 'get', 0, 1),
  (210703, '②', 'getOrDefault', 1, 2),
  (210703, '③', 'getDefault', 0, 3),
  (210703, '④', 'orElse', 0, 4),
  (210704, '①', '0으로 계산된다', 0, 1),
  (210704, '②', 'NullPointerException 발생', 1, 2),
  (210704, '③', '컴파일 오류', 0, 3),
  (210704, '④', '무시된다', 0, 4),
  (210801, '①', '컬렉션의 remove 호출', 0, 1),
  (210801, '②', 'iterator의 remove 호출', 1, 2),
  (210801, '③', '새 컬렉션 생성 금지', 0, 3),
  (210801, '④', '삭제 불가능', 0, 4),
  (210802, '①', 'NullPointerException', 0, 1),
  (210802, '②', 'ConcurrentModificationException', 1, 2),
  (210802, '③', 'IOException', 0, 3),
  (210802, '④', 'ClassCastException', 0, 4),
  (210803, '①', 'next', 0, 1),
  (210803, '②', 'hasNext', 1, 2),
  (210803, '③', 'remove', 0, 3),
  (210803, '④', 'get', 0, 4),
  (210804, '①', 'null 반환', 0, 1),
  (210804, '②', '예외 발생', 1, 2),
  (210804, '③', '0 반환', 0, 3),
  (210804, '④', '반복 종료', 0, 4),
  (210901, '①', 'Comparable은 상황별 기준, Comparator는 기본 기준', 0, 1),
  (210901, '②', 'Comparable은 객체의 기본 정렬 기준, Comparator는 상황별 정렬 기준', 1, 2),
  (210901, '③', '둘은 같은 인터페이스다', 0, 3),
  (210901, '④', '둘 다 정렬과 무관하다', 0, 4),
  (210902, '①', '[1, 2, 3]', 0, 1),
  (210902, '②', '[3, 2, 1]', 1, 2),
  (210902, '③', '[3, 1, 2]', 0, 3),
  (210902, '④', '오류 발생', 0, 4),
  (210903, '①', 'descending', 0, 1),
  (210903, '②', 'reverseOrder', 1, 2),
  (210903, '③', 'reversedAll', 0, 3),
  (210903, '④', 'downOrder', 0, 4),
  (210904, '①', '[3, 2, 1]', 0, 1),
  (210904, '②', '[1, 2, 3]', 1, 2),
  (210904, '③', '[3, 1, 2]', 0, 3),
  (210904, '④', '오류 발생', 0, 4),
  (211001, '①', '항상 List만 쓴다', 0, 1),
  (211001, '②', '순서·중복이면 List, 고유값이면 Set, 키 기반 조회면 Map', 1, 2),
  (211001, '③', '항상 Map만 쓴다', 0, 3),
  (211001, '④', '크기가 크면 배열', 0, 4),
  (211002, '①', '80', 0, 1),
  (211002, '②', '90', 1, 2),
  (211002, '③', '[80, 90]', 0, 3),
  (211002, '④', 'null', 0, 4),
  (211003, '①', 'Set만 가능', 0, 1),
  (211003, '②', 'List', 1, 2),
  (211003, '③', 'int[]', 0, 3),
  (211003, '④', 'Map만 가능', 0, 4),
  (211004, '①', 'Map<String, Integer>', 0, 1),
  (211004, '②', 'Map<String, List<Integer>>', 1, 2),
  (211004, '③', 'List<String>', 0, 3),
  (211004, '④', 'Set<Integer>', 0, 4),
  (220101, '①', '실행 속도 향상', 0, 1),
  (220101, '②', '잘못된 타입 사용을 컴파일 시점에 발견', 1, 2),
  (220101, '③', '메모리 절약', 0, 3),
  (220101, '④', '코드 자동 생성', 0, 4),
  (220102, '①', '정상 저장', 0, 1),
  (220102, '②', '컴파일 오류', 1, 2),
  (220102, '③', '실행 중 오류', 0, 3),
  (220102, '④', '"1"로 변환 저장', 0, 4),
  (220103, '①', 'int', 0, 1),
  (220103, '②', 'String', 1, 2),
  (220103, '③', 'Object', 0, 3),
  (220103, '④', 'char', 0, 4),
  (220104, '①', '(String) 캐스팅 필요', 0, 1),
  (220104, '②', '필요 없다', 1, 2),
  (220104, '③', 'toString 필수', 0, 3),
  (220104, '④', '컴파일 오류', 0, 4),
  (220201, '①', '컴파일러 설치 시', 0, 1),
  (220201, '②', '객체 생성 시', 1, 2),
  (220201, '③', '실행 종료 시', 0, 3),
  (220201, '④', '정해지지 않는다', 0, 4),
  (220202, '①', '10', 1, 1),
  (220202, '②', 'T', 0, 2),
  (220202, '③', 'null', 0, 3),
  (220202, '④', '컴파일 오류', 0, 4),
  (220203, '①', 'T', 1, 1),
  (220203, '②', 'int', 0, 2),
  (220203, '③', 'type', 0, 3),
  (220203, '④', 'Object', 0, 4),
  (220204, '①', '정상 저장', 0, 1),
  (220204, '②', '컴파일 오류', 1, 2),
  (220204, '③', '실행 중 오류', 0, 3),
  (220204, '④', '숫자로 변환된다', 0, 4),
  (220301, '①', '메서드 이름 뒤', 0, 1),
  (220301, '②', '반환형 앞', 1, 2),
  (220301, '③', '매개변수 뒤', 0, 3),
  (220301, '④', '클래스 이름 앞', 0, 4),
  (220302, '①', 'A', 1, 1),
  (220302, '②', 'B', 0, 2),
  (220302, '③', 'T', 0, 3),
  (220302, '④', '컴파일 오류', 0, 4),
  (220303, '①', 'void', 0, 1),
  (220303, '②', 'T', 1, 2),
  (220303, '③', 'List', 0, 3),
  (220303, '④', 'Object', 0, 4),
  (220304, '①', 'String', 0, 1),
  (220304, '②', 'Integer', 1, 2),
  (220304, '③', 'Object', 0, 3),
  (220304, '④', 'int', 0, 4),
  (220401, '①', '읽기 용도는 ? super T, 쓰기 용도는 ? extends T', 0, 1),
  (220401, '②', '읽기 용도는 ? extends T, 넣기 용도는 ? super T', 1, 2),
  (220401, '③', '둘은 같은 의미다', 0, 3),
  (220401, '④', '와일드카드는 배열 전용이다', 0, 4),
  (220402, '①', '3.5', 1, 1),
  (220402, '②', '3', 0, 2),
  (220402, '③', '12.5', 0, 3),
  (220402, '④', '컴파일 오류', 0, 4),
  (220403, '①', 'super', 0, 1),
  (220403, '②', 'extends', 1, 2),
  (220403, '③', 'implements', 0, 3),
  (220403, '④', 'instanceof', 0, 4),
  (220404, '①', '정상 전달', 0, 1),
  (220404, '②', '컴파일 오류', 1, 2),
  (220404, '③', '자동 변환', 0, 3),
  (220404, '④', '실행 중 오류', 0, 4),
  (220501, '①', '클래스 선언의 축약이다', 0, 1),
  (220501, '②', '함수형 인터페이스의 단일 추상 메서드 구현을 간결하게 표현한다', 1, 2),
  (220501, '③', '반복문의 대체 문법이다', 0, 3),
  (220501, '④', '예외 처리 문법이다', 0, 4),
  (220502, '①', 'true', 1, 1),
  (220502, '②', 'false', 0, 2),
  (220502, '③', '5', 0, 3),
  (220502, '④', '컴파일 오류', 0, 4),
  (220503, '①', '=>', 0, 1),
  (220503, '②', '->', 1, 2),
  (220503, '③', '::', 0, 3),
  (220503, '④', '>>', 0, 4),
  (220504, '①', 'true', 0, 1),
  (220504, '②', 'false', 1, 2),
  (220504, '③', '-3', 0, 3),
  (220504, '④', '예외 발생', 0, 4),
  (220601, '①', '메서드가 없어야 한다', 0, 1),
  (220601, '②', '추상 메서드가 정확히 하나여야 한다', 1, 2),
  (220601, '③', '추상 메서드가 두 개 이상이어야 한다', 0, 3),
  (220601, '④', '필드가 있어야 한다', 0, 4),
  (220602, '①', '23', 0, 1),
  (220602, '②', '5', 1, 2),
  (220602, '③', '6', 0, 3),
  (220602, '④', '컴파일 오류', 0, 4),
  (220603, '①', 'Override', 0, 1),
  (220603, '②', 'FunctionalInterface', 1, 2),
  (220603, '③', 'Functional', 0, 3),
  (220603, '④', 'Lambda', 0, 4),
  (220604, '①', '정상 컴파일', 0, 1),
  (220604, '②', '컴파일 오류', 1, 2),
  (220604, '③', '실행 중 오류', 0, 3),
  (220604, '④', '첫 메서드만 유효', 0, 4),
  (220701, '①', '람다가 여러 문장일 때', 0, 1),
  (220701, '②', '람다가 기존 메서드를 그대로 호출할 때', 1, 2),
  (220701, '③', '메서드가 private일 때', 0, 3),
  (220701, '④', '항상 사용할 수 없다', 0, 4),
  (220702, '①', 'Java SQL (한 줄)', 0, 1),
  (220702, '②', 'Java 다음 줄 SQL', 1, 2),
  (220702, '③', '[Java, SQL]', 0, 3),
  (220702, '④', '컴파일 오류', 0, 4),
  (220703, '①', 'print()', 0, 1),
  (220703, '②', 'println', 1, 2),
  (220703, '③', 'printAll', 0, 3),
  (220703, '④', 'show', 0, 4),
  (220704, '①', 's -> s.length()', 1, 1),
  (220704, '②', '() -> length', 0, 2),
  (220704, '③', 'length -> s', 0, 3),
  (220704, '④', 's -> length(s)', 0, 4),
  (220801, '①', 'test / apply', 1, 1),
  (220801, '②', 'run / call', 0, 2),
  (220801, '③', 'get / accept', 0, 3),
  (220801, '④', 'check / map', 0, 4),
  (220802, '①', 'true, 3', 1, 1),
  (220802, '②', 'false, 3', 0, 2),
  (220802, '③', 'true, 4', 0, 3),
  (220802, '④', '컴파일 오류', 0, 4),
  (220803, '①', 'size', 0, 1),
  (220803, '②', 'length', 1, 2),
  (220803, '③', 'count', 0, 3),
  (220803, '④', 'chars', 0, 4),
  (220804, '①', 'Function', 0, 1),
  (220804, '②', 'Consumer', 0, 2),
  (220804, '③', 'Predicate', 1, 3),
  (220804, '④', 'Supplier', 0, 4),
  (220901, '①', 'Consumer는 값을 만들어 반환하고 Supplier는 소비한다', 0, 1),
  (220901, '②', 'Consumer는 값을 받아 소비하고 반환하지 않으며, Supplier는 입력 없이 값을 제공한다', 1, 2),
  (220901, '③', '둘 다 boolean을 반환한다', 0, 3),
  (220901, '④', '둘은 같은 인터페이스다', 0, 4),
  (220902, '①', '100', 1, 1),
  (220902, '②', 'supplier', 0, 2),
  (220902, '③', '컴파일 오류', 0, 3),
  (220902, '④', '출력 없음', 0, 4),
  (220903, '①', '::', 0, 1),
  (220903, '②', '->', 1, 2),
  (220903, '③', '=>', 0, 3),
  (220903, '④', '>>', 0, 4),
  (220904, '①', 'get', 0, 1),
  (220904, '②', 'test', 0, 2),
  (220904, '③', 'accept', 1, 3),
  (220904, '④', 'apply', 0, 4),
  (221001, '①', '컴파일이 생략된다', 0, 1),
  (221001, '②', '공통 알고리즘과 상황별 조건(동작)을 분리할 수 있다', 1, 2),
  (221001, '③', '실행 순서가 뒤바뀐다', 0, 3),
  (221001, '④', '타입 검사가 사라진다', 0, 4),
  (221002, '①', '[1, 2, 3]', 0, 1),
  (221002, '②', '[2, 3]', 1, 2),
  (221002, '③', '[1]', 0, 3),
  (221002, '④', '컴파일 오류', 0, 4),
  (221003, '①', 'Function', 0, 1),
  (221003, '②', 'Consumer', 0, 2),
  (221003, '③', 'Predicate', 1, 3),
  (221003, '④', 'Supplier', 0, 4),
  (221004, '①', '매번 수정해야 한다', 0, 1),
  (221004, '②', '수정할 필요 없다', 1, 2),
  (221004, '③', '삭제된다', 0, 3),
  (221004, '④', '재컴파일이 불가능하다', 0, 4),
  (230101, '①', '최종 연산 없이도 중간 연산이 실행된다', 0, 1),
  (230101, '②', '중간 연산을 연결하고 최종 연산에서 결과를 만들며, 최종 연산이 없으면 중간 연산은 실행되지 않는다', 1, 2),
  (230101, '③', '중간 연산이 원본을 수정한다', 0, 3),
  (230101, '④', '연산 순서는 무작위다', 0, 4),
  (230102, '①', '6', 0, 1),
  (230102, '②', '3', 1, 2),
  (230102, '③', '[1, 2, 3]', 0, 3),
  (230102, '④', '컴파일 오류', 0, 4),
  (230103, '①', 'iterator', 0, 1),
  (230103, '②', 'stream', 1, 2),
  (230103, '③', 'flow', 0, 3),
  (230103, '④', 'collect', 0, 4),
  (230104, '①', '재사용 가능', 0, 1),
  (230104, '②', '사용할 수 없다(새 스트림을 만들어야 한다)', 1, 2),
  (230104, '③', '두 배 빨라진다', 0, 3),
  (230104, '④', '컴파일 오류만 발생', 0, 4),
  (230201, '①', '제거할 요소의 조건', 0, 1),
  (230201, '②', '다음 단계로 남길 요소의 조건', 1, 2),
  (230201, '③', '정렬 기준', 0, 3),
  (230201, '④', '변환 규칙', 0, 4),
  (230202, '①', '[1, 3]', 0, 1),
  (230202, '②', '[2, 4]', 1, 2),
  (230202, '③', '[1, 2, 3, 4]', 0, 3),
  (230202, '④', '[]', 0, 4),
  (230203, '①', '=', 0, 1),
  (230203, '②', '==', 1, 2),
  (230203, '③', '!=', 0, 3),
  (230203, '④', '>=', 0, 4),
  (230204, '①', '예외 발생', 0, 1),
  (230204, '②', 'null 반환', 0, 2),
  (230204, '③', '빈 결과가 나온다', 1, 3),
  (230204, '④', '원본이 반환된다', 0, 4),
  (230301, '①', '요소 개수가 줄어든다', 0, 1),
  (230301, '②', '각 요소를 다른 값으로 변환하며 개수는 유지된다', 1, 2),
  (230301, '③', '조건에 맞는 요소만 남긴다', 0, 3),
  (230301, '④', '정렬한다', 0, 4),
  (230302, '①', '[Java, SQL]', 0, 1),
  (230302, '②', '[4, 3]', 1, 2),
  (230302, '③', '[3, 4]', 0, 3),
  (230302, '④', '컴파일 오류', 0, 4),
  (230303, '①', 'size', 0, 1),
  (230303, '②', 'length', 1, 2),
  (230303, '③', 'chars', 0, 3),
  (230303, '④', 'count', 0, 4),
  (230304, '①', '항상 같아야 한다', 0, 1),
  (230304, '②', '달라질 수 있다', 1, 2),
  (230304, '③', '항상 달라야 한다', 0, 3),
  (230304, '④', 'Object로 고정된다', 0, 4),
  (230401, '①', '무작위', 0, 1),
  (230401, '②', '요소의 기본 정렬 기준(Comparable)', 1, 2),
  (230401, '③', '입력 순서 유지', 0, 3),
  (230401, '④', '길이 기준', 0, 4),
  (230402, '①', '[3, 1, 2]', 0, 1),
  (230402, '②', '[1, 2, 3]', 1, 2),
  (230402, '③', '[3, 2, 1]', 0, 3),
  (230402, '④', '오류 발생', 0, 4),
  (230403, '①', 'sort', 0, 1),
  (230403, '②', 'sorted', 1, 2),
  (230403, '③', 'order', 0, 3),
  (230403, '④', 'arrange', 0, 4),
  (230404, '①', '정렬된다', 0, 1),
  (230404, '②', '바뀌지 않는다', 1, 2),
  (230404, '③', '삭제된다', 0, 3),
  (230404, '④', '역순이 된다', 0, 4),
  (230501, '①', '== 비교', 0, 1),
  (230501, '②', 'equals 기준', 1, 2),
  (230501, '③', 'hashCode만', 0, 3),
  (230501, '④', '주소값', 0, 4),
  (230502, '①', '[1, 1]', 0, 1),
  (230502, '②', '[1, 2]', 1, 2),
  (230502, '③', '[1, 2, 3]', 0, 3),
  (230502, '④', '[2, 3]', 0, 4),
  (230503, '①', 'unique', 0, 1),
  (230503, '②', 'distinct', 1, 2),
  (230503, '③', 'dedup', 0, 3),
  (230503, '④', 'single', 0, 4),
  (230504, '①', '[1, 2]', 0, 1),
  (230504, '②', '[1]', 1, 2),
  (230504, '③', '[1, 1]', 0, 3),
  (230504, '④', '[2, 3]', 0, 4),
  (230601, '①', '일반 스트림 그대로 가능', 0, 1),
  (230601, '②', 'mapToInt 등으로 숫자 전용 스트림(IntStream)으로 변환', 1, 2),
  (230601, '③', '정렬이 먼저 필요', 0, 3),
  (230601, '④', '배열로 변환', 0, 4),
  (230602, '①', '60, 20', 0, 1),
  (230602, '②', '60, 20.0', 1, 2),
  (230602, '③', '20, 60.0', 0, 3),
  (230602, '④', '오류 발생', 0, 4),
  (230603, '①', 'map', 0, 1),
  (230603, '②', 'mapToInt', 1, 2),
  (230603, '③', 'toInt', 0, 3),
  (230603, '④', 'intStream', 0, 4),
  (230604, '①', 'double', 0, 1),
  (230604, '②', 'OptionalDouble', 1, 2),
  (230604, '③', 'int', 0, 3),
  (230604, '④', 'Double만', 0, 4),
  (230701, '①', '최댓값', 0, 1),
  (230701, '②', '계산의 시작값', 1, 2),
  (230701, '③', '요소 개수', 0, 3),
  (230701, '④', '정렬 기준', 0, 4),
  (230702, '①', '9', 0, 1),
  (230702, '②', '24', 1, 2),
  (230702, '③', '10', 0, 3),
  (230702, '④', '1', 0, 4),
  (230703, '①', '+', 0, 1),
  (230703, '②', '-', 0, 2),
  (230703, '③', '*', 1, 3),
  (230703, '④', '/', 0, 4),
  (230704, '①', '1', 0, 1),
  (230704, '②', '0', 1, 2),
  (230704, '③', '-1', 0, 3),
  (230704, '④', '아무 값', 0, 4),
  (230801, '①', '자동으로 합쳐진다', 0, 1),
  (230801, '②', '예외가 발생하므로 병합 규칙을 세 번째 인자로 전달한다', 1, 2),
  (230801, '③', '마지막 값만 저장된다', 0, 3),
  (230801, '④', '첫 값만 저장된다', 0, 4),
  (230802, '①', '3', 0, 1),
  (230802, '②', '2', 1, 2),
  (230802, '③', '1', 0, 3),
  (230802, '④', '오류 발생', 0, 4),
  (230803, '①', 'toList', 0, 1),
  (230803, '②', 'toSet', 1, 2),
  (230803, '③', 'toMap', 0, 3),
  (230803, '④', 'joining', 0, 4),
  (230804, '①', '숫자 합계', 0, 1),
  (230804, '②', '문자열 목록을 구분자로 연결한 하나의 문자열 생성', 1, 2),
  (230804, '③', '그룹화', 0, 3),
  (230804, '④', '정렬', 0, 4),
  (230901, '①', 'List', 0, 1),
  (230901, '②', '기준 함수의 반환값을 키로, 해당 그룹의 요소 목록을 값으로 갖는 Map', 1, 2),
  (230901, '③', 'Set', 0, 3),
  (230901, '④', '단일 값', 0, 4),
  (230902, '①', '[A]', 0, 1),
  (230902, '②', '[BB, CC]', 1, 2),
  (230902, '③', '[A, BB, CC]', 0, 3),
  (230902, '④', 'null', 0, 4),
  (230903, '①', 'toMap', 0, 1),
  (230903, '②', 'groupingBy', 1, 2),
  (230903, '③', 'groupBy', 0, 3),
  (230903, '④', 'mapping', 0, 4),
  (230904, '①', '그룹별 목록', 0, 1),
  (230904, '②', '그룹별 개수', 1, 2),
  (230904, '③', '전체 개수', 0, 3),
  (230904, '④', '오류 발생', 0, 4),
  (231001, '①', '수집 → 정렬 → 변환 → 필터링', 0, 1),
  (231001, '②', '필터링 → 변환 → 정렬 → 수집', 1, 2),
  (231001, '③', '무작위', 0, 3),
  (231001, '④', '뒤에서부터', 0, 4),
  (231002, '①', '[SQL, JAVA, WEB]', 0, 1),
  (231002, '②', '[JAVA]', 1, 2),
  (231002, '③', '[java]', 0, 3),
  (231002, '④', '[]', 0, 4),
  (231003, '①', 'filter', 0, 1),
  (231003, '②', 'map', 1, 2),
  (231003, '③', 'sorted', 0, 3),
  (231003, '④', 'collect', 0, 4),
  (231004, '①', '코드 작성 시', 0, 1),
  (231004, '②', '중간 연산 연결 시', 0, 2),
  (231004, '③', '최종 연산이 호출될 때', 1, 3),
  (231004, '④', '프로그램 종료 시', 0, 4),
  (240101, '①', '시간대 정보를 포함한다', 0, 1),
  (240101, '②', '불변 객체라서 계산 메서드는 새 객체를 반환한다', 1, 2),
  (240101, '③', '값이 수시로 바뀐다', 0, 3),
  (240101, '④', '시각까지 저장한다', 0, 4),
  (240102, '①', '2026-07-23', 0, 1),
  (240102, '②', '2026-07-24', 1, 2),
  (240102, '③', '2026-08-23', 0, 3),
  (240102, '④', '오류 발생', 0, 4),
  (240103, '①', 'new', 0, 1),
  (240103, '②', 'of', 1, 2),
  (240103, '③', 'create', 0, 3),
  (240103, '④', 'get', 0, 4),
  (240104, '①', '하루 뒤로 바뀐다', 0, 1),
  (240104, '②', '그대로다', 1, 2),
  (240104, '③', 'null이 된다', 0, 3),
  (240104, '④', '오류 발생', 0, 4),
  (240201, '①', '날짜와 시각을 함께 표현하지만 시간대 정보는 없다', 1, 1),
  (240201, '②', '시간대를 포함한다', 0, 2),
  (240201, '③', '날짜만 표현한다', 0, 3),
  (240201, '④', '시각만 표현한다', 0, 4),
  (240202, '①', '30', 0, 1),
  (240202, '②', '10', 1, 2),
  (240202, '③', '23', 0, 3),
  (240202, '④', '7', 0, 4),
  (240203, '①', '0', 0, 1),
  (240203, '②', '23', 0, 2),
  (240203, '③', '30', 1, 3),
  (240203, '④', '7', 0, 4),
  (240204, '①', 'LocalDateTime', 0, 1),
  (240204, '②', 'Instant(또는 ZonedDateTime)', 1, 2),
  (240204, '③', 'String', 0, 3),
  (240204, '④', 'int', 0, 4),
  (240301, '①', '둘 다 월', 0, 1),
  (240301, '②', 'MM은 월, mm은 분', 1, 2),
  (240301, '③', 'MM은 분, mm은 월', 0, 3),
  (240301, '④', '둘 다 분', 0, 4),
  (240302, '①', '2026-7-23', 0, 1),
  (240302, '②', '2026-07-23', 1, 2),
  (240302, '③', '23-07-2026', 0, 3),
  (240302, '④', '오류 발생', 0, 4),
  (240303, '①', 'of', 0, 1),
  (240303, '②', 'ofPattern', 1, 2),
  (240303, '③', 'pattern', 0, 3),
  (240303, '④', 'format', 0, 4),
  (240304, '①', 'IOException', 0, 1),
  (240304, '②', 'DateTimeParseException', 1, 2),
  (240304, '③', 'NullPointerException', 0, 3),
  (240304, '④', 'ClassCastException', 0, 4),
  (240401, '①', '문자열을 불변으로 만들기 위해', 0, 1),
  (240401, '②', '내부 버퍼로 이어 붙여 불필요한 String 객체 생성을 줄이기 위해', 1, 2),
  (240401, '③', '정렬을 빠르게 하기 위해', 0, 3),
  (240401, '④', '인코딩을 바꾸기 위해', 0, 4),
  (240402, '①', 'JavaGold', 0, 1),
  (240402, '②', 'Java Gold', 1, 2),
  (240402, '③', 'Java', 0, 3),
  (240402, '④', 'Gold', 0, 4),
  (240403, '①', 'add', 0, 1),
  (240403, '②', 'append', 1, 2),
  (240403, '③', 'concat', 0, 3),
  (240403, '④', 'plus', 0, 4),
  (240404, '①', 'toString() 호출', 1, 1),
  (240404, '②', '자동 변환', 0, 2),
  (240404, '③', '(String) 캐스팅', 0, 3),
  (240404, '④', 'valueOf만 가능', 0, 4),
  (240501, '①', '일부만 일치해도', 0, 1),
  (240501, '②', '문자열 전체가 패턴과 일치할 때', 1, 2),
  (240501, '③', '첫 글자만 일치할 때', 0, 3),
  (240501, '④', '길이만 같을 때', 0, 4),
  (240502, '①', 'true', 1, 1),
  (240502, '②', 'false', 0, 2),
  (240502, '③', '12345', 0, 3),
  (240502, '④', '오류 발생', 0, 4),
  (240503, '①', 'equals', 0, 1),
  (240503, '②', 'matches', 1, 2),
  (240503, '③', 'contains', 0, 3),
  (240503, '④', 'pattern', 0, 4),
  (240504, '①', 'true', 0, 1),
  (240504, '②', 'false', 1, 2),
  (240504, '③', '예외 발생', 0, 3),
  (240504, '④', '12', 0, 4),
  (240601, '①', '값이 없을 수 있음을 타입으로 표현한다', 1, 1),
  (240601, '②', '값을 암호화한다', 0, 2),
  (240601, '③', '실행 속도를 높인다', 0, 3),
  (240601, '④', '예외를 없앤다', 0, 4),
  (240602, '①', 'null', 0, 1),
  (240602, '②', '없음', 1, 2),
  (240602, '③', '오류 발생', 0, 3),
  (240602, '④', 'Optional.empty', 0, 4),
  (240603, '①', 'getOrDefault', 0, 1),
  (240603, '②', 'orElse', 1, 2),
  (240603, '③', 'orDefault', 0, 3),
  (240603, '④', 'else', 0, 4),
  (240604, '①', 'null 반환', 0, 1),
  (240604, '②', '예외 발생', 1, 2),
  (240604, '③', '기본값 반환', 0, 3),
  (240604, '④', '빈 문자열 반환', 0, 4),
  (240701, '①', '속도가 빠르다', 0, 1),
  (240701, '②', 'double을 직접 전달하면 이미 오차가 포함된 값이 저장되기 때문', 1, 2),
  (240701, '③', '문자열이 짧다', 0, 3),
  (240701, '④', '컴파일이 쉬워진다', 0, 4),
  (240702, '①', '0.30000000000000004', 0, 1),
  (240702, '②', '0.3', 1, 2),
  (240702, '③', '0.2', 0, 3),
  (240702, '④', '오류 발생', 0, 4),
  (240703, '①', 'plus', 0, 1),
  (240703, '②', 'add', 1, 2),
  (240703, '③', 'sum', 0, 3),
  (240703, '④', 'append', 0, 4),
  (240704, '①', '같다고 판단', 0, 1),
  (240704, '②', '다르다고 판단', 1, 2),
  (240704, '③', '예외 발생', 0, 3),
  (240704, '④', '컴파일 오류', 0, 4),
  (240801, '①', '내림·올림·반올림', 0, 1),
  (240801, '②', '반올림·내림·올림', 1, 2),
  (240801, '③', '올림·내림·반올림', 0, 3),
  (240801, '④', '반올림·올림·내림', 0, 4),
  (240802, '①', '10과 5', 0, 1),
  (240802, '②', '20과 5', 1, 2),
  (240802, '③', '20과 -5', 0, 3),
  (240802, '④', '10과 -5', 0, 4),
  (240803, '①', 'big', 0, 1),
  (240803, '②', 'max', 1, 2),
  (240803, '③', 'top', 0, 3),
  (240803, '④', 'high', 0, 4),
  (240804, '①', '1~6', 0, 1),
  (240804, '②', '0~5', 1, 2),
  (240804, '③', '0~6', 0, 3),
  (240804, '④', '1~5', 0, 4),
  (240901, '①', '더 빠른 비교', 0, 1),
  (240901, '②', '두 값이 모두 null이어도 예외 없이 비교할 수 있다', 1, 2),
  (240901, '③', '대소문자를 무시한다', 0, 3),
  (240901, '④', '숫자만 비교한다', 0, 4),
  (240902, '①', 'true와 false', 1, 1),
  (240902, '②', 'false와 true', 0, 2),
  (240902, '③', '둘 다 true', 0, 3),
  (240902, '④', '예외 발생', 0, 4),
  (240903, '①', 'same', 0, 1),
  (240903, '②', 'equals', 1, 2),
  (240903, '③', 'compare', 0, 3),
  (240903, '④', 'match', 0, 4),
  (240904, '①', 'false 반환', 0, 1),
  (240904, '②', 'NullPointerException 발생', 1, 2),
  (240904, '③', 'true 반환', 0, 3),
  (240904, '④', '컴파일 오류', 0, 4),
  (241001, '①', '문자열 그대로 계산한다', 0, 1),
  (241001, '②', '적합한 전용 타입으로 변환한 뒤 검증하고, 표시 형식과 내부 계산 타입을 분리한다', 1, 2),
  (241001, '③', '모든 값을 double로 통일한다', 0, 3),
  (241001, '④', '검증은 출력 후에 한다', 0, 4),
  (241002, '①', '6', 0, 1),
  (241002, '②', '7', 1, 2),
  (241002, '③', '8', 0, 3),
  (241002, '④', '오류 발생', 0, 4),
  (241003, '①', 'DATE', 0, 1),
  (241003, '②', 'DAYS', 1, 2),
  (241003, '③', 'DAY', 0, 3),
  (241003, '④', 'TIME', 0, 4),
  (241004, '①', '시작·종료 모두 포함', 0, 1),
  (241004, '②', '시작 포함, 종료 제외', 1, 2),
  (241004, '③', '시작 제외, 종료 포함', 0, 3),
  (241004, '④', '둘 다 제외', 0, 4),
  (250101, '①', '변수 이름·주석 위치', 0, 1),
  (250101, '②', '기능·입력값·출력 결과·예외 상황', 1, 2),
  (250101, '③', '폴더 구조만', 0, 3),
  (250101, '④', '출시 일정만', 0, 4),
  (250102, '①', '점수 0~100', 0, 1),
  (250102, '②', '통과 여부', 1, 2),
  (250102, '③', 'Requirement', 0, 3),
  (250102, '④', '컴파일 오류', 0, 4),
  (250103, '①', 'out', 0, 1),
  (250103, '②', 'output', 1, 2),
  (250103, '③', 'result()', 0, 3),
  (250103, '④', 'String', 0, 4),
  (250104, '①', 'getOutput()', 0, 1),
  (250104, '②', 'output() 같은 이름 메서드', 1, 2),
  (250104, '③', '직접 접근만 가능', 0, 3),
  (250104, '④', '읽을 수 없다', 0, 4),
  (250201, '①', '화면에 보이는 모든 값을 한 클래스에 넣는다', 0, 1),
  (250201, '②', '실제 업무 개념을 기준으로 클래스를 나눈다', 1, 2),
  (250201, '③', '클래스는 하나만 만든다', 0, 3),
  (250201, '④', '파일 크기에 맞춘다', 0, 4),
  (250202, '①', 'Course', 0, 1),
  (250202, '②', 'Java', 1, 2),
  (250202, '③', 'title', 0, 3),
  (250202, '④', '컴파일 오류', 0, 4),
  (250203, '①', 'static', 0, 1),
  (250203, '②', 'final', 1, 2),
  (250203, '③', 'public', 0, 3),
  (250203, '④', 'new', 0, 4),
  (250204, '①', '정상 변경', 0, 1),
  (250204, '②', '컴파일 오류', 1, 2),
  (250204, '③', '실행 중 오류', 0, 3),
  (250204, '④', '무시된다', 0, 4),
  (250301, '①', '조건문만 사용', 0, 1),
  (250301, '②', '반복문으로 메뉴를 계속 표시하고 switch로 선택 기능을 실행', 1, 2),
  (250301, '③', '재귀만 사용', 0, 3),
  (250301, '④', '예외로 흐름 제어', 0, 4),
  (250302, '①', '등록', 0, 1),
  (250302, '②', '조회', 1, 2),
  (250302, '③', '잘못된 메뉴', 0, 3),
  (250302, '④', '출력 없음', 0, 4),
  (250303, '①', 'else', 0, 1),
  (250303, '②', 'default', 1, 2),
  (250303, '③', 'case 0', 0, 3),
  (250303, '④', 'final', 0, 4),
  (250304, '①', '등록', 0, 1),
  (250304, '②', '조회', 0, 2),
  (250304, '③', '잘못된 메뉴', 1, 3),
  (250304, '④', '오류 발생', 0, 4),
  (250401, '①', 'List', 0, 1),
  (250401, '②', 'Map', 1, 2),
  (250401, '③', '배열', 0, 3),
  (250401, '④', 'String', 0, 4),
  (250402, '①', '1', 0, 1),
  (250402, '②', 'Java', 1, 2),
  (250402, '③', 'null', 0, 3),
  (250402, '④', '오류 발생', 0, 4),
  (250403, '①', 'long', 0, 1),
  (250403, '②', 'Long', 1, 2),
  (250403, '③', 'int', 0, 3),
  (250403, '④', 'Number만', 0, 4),
  (250404, '①', '0', 0, 1),
  (250404, '②', '빈 문자열', 0, 2),
  (250404, '③', 'null', 1, 3),
  (250404, '④', '예외 발생', 0, 4),
  (250501, '①', '저장 → 검증 → 중복 확인', 0, 1),
  (250501, '②', '입력값 검증 → 중복 확인 → 객체 생성 → 저장', 1, 2),
  (250501, '③', '중복 확인 → 저장 → 검증', 0, 3),
  (250501, '④', '순서 무관', 0, 4),
  (250502, '①', 'true', 0, 1),
  (250502, '②', 'false', 1, 2),
  (250502, '③', '예외 발생', 0, 3),
  (250502, '④', '컴파일 오류', 0, 4),
  (250503, '①', 'isEmpty만 가능', 0, 1),
  (250503, '②', 'isBlank', 1, 2),
  (250503, '③', 'isNull', 0, 3),
  (250503, '④', 'blank', 0, 4),
  (250504, '①', '값이 교체된다', 0, 1),
  (250504, '②', 'false가 반환되고 저장되지 않는다', 1, 2),
  (250504, '③', '예외 발생', 0, 3),
  (250504, '④', '두 개가 저장된다', 0, 4),
  (250601, '①', '오류로 처리한다', 0, 1),
  (250601, '②', '정상 결과 중 하나로 보고 빈 목록이나 Optional로 표현한다', 1, 2),
  (250601, '③', '프로그램을 종료한다', 0, 3),
  (250601, '④', '예외를 던진다', 0, 4),
  (250602, '①', '[Java]', 0, 1),
  (250602, '②', '[Java, JavaScript]', 1, 2),
  (250602, '③', '[SQL]', 0, 3),
  (250602, '④', '[]', 0, 4),
  (250603, '①', 'equals', 0, 1),
  (250603, '②', 'contains', 1, 2),
  (250603, '③', 'matches', 0, 3),
  (250603, '④', 'charAt', 0, 4),
  (250604, '①', 'null', 0, 1),
  (250604, '②', '빈 목록', 1, 2),
  (250604, '③', '예외 발생', 0, 3),
  (250604, '④', '원본 그대로', 0, 4),
  (250701, '①', '파일 크기', 0, 1),
  (250701, '②', '대상의 존재 여부와 권한', 1, 2),
  (250701, '③', '출력 형식', 0, 3),
  (250701, '④', '변수 이름', 0, 4),
  (250702, '①', 'Java 기초', 0, 1),
  (250702, '②', 'Java 심화', 1, 2),
  (250702, '③', 'null', 0, 3),
  (250702, '④', '오류 발생', 0, 4),
  (250703, '①', 'set', 0, 1),
  (250703, '②', 'replace', 1, 2),
  (250703, '③', 'update', 0, 3),
  (250703, '④', 'change', 0, 4),
  (250704, '①', '새로 저장된다', 0, 1),
  (250704, '②', '아무 변경 없이 null이 반환된다', 1, 2),
  (250704, '③', '예외 발생', 0, 3),
  (250704, '④', '프로그램 종료', 0, 4),
  (250801, '①', '저장과 복원에서 서로 다른 규칙 사용', 0, 1),
  (250801, '②', '한 줄의 필드 구분 규칙과 인코딩을 정하고 양쪽에서 같은 규칙 사용', 1, 2),
  (250801, '③', '규칙 없이 자유롭게', 0, 3),
  (250801, '④', '인코딩은 매번 바꾼다', 0, 4),
  (250802, '①', '콘솔에 출력된다', 0, 1),
  (250802, '②', 'courses.csv 파일이 만들어져 두 줄이 저장된다', 1, 2),
  (250802, '③', '컴파일 오류', 0, 3),
  (250802, '④', '아무 일도 없다', 0, 4),
  (250803, '①', 'UTF8', 0, 1),
  (250803, '②', 'UTF_8', 1, 2),
  (250803, '③', 'Unicode', 0, 3),
  (250803, '④', 'EUC_KR만', 0, 4),
  (250804, '①', '아무 문제 없다', 0, 1),
  (250804, '②', '필드 구분이 깨질 수 있어 처리 규칙이 필요하다', 1, 2),
  (250804, '③', '자동으로 제거된다', 0, 3),
  (250804, '④', '저장이 거부된다', 0, 4),
  (250901, '①', '정상값만', 0, 1),
  (250901, '②', '정상값과 빈 값·최솟값·최댓값·중복값·존재하지 않는 대상', 1, 2),
  (250901, '③', '큰 값만', 0, 3),
  (250901, '④', '무작위 한 가지', 0, 4),
  (250902, '①', 'true와 true', 0, 1),
  (250902, '②', 'true와 false', 1, 2),
  (250902, '③', 'false와 true', 0, 3),
  (250902, '④', 'false와 false', 0, 4),
  (250903, '①', '||', 0, 1),
  (250903, '②', '&&', 1, 2),
  (250903, '③', '!', 0, 3),
  (250903, '④', '==', 0, 4),
  (250904, '①', 'true', 1, 1),
  (250904, '②', 'false', 0, 2),
  (250904, '③', '예외 발생', 0, 3),
  (250904, '④', '100', 0, 4),
  (251001, '①', '요구사항·모델·저장소·서비스·입출력', 1, 1),
  (251001, '②', '변수·상수·주석', 0, 2),
  (251001, '③', '클래스 하나에 전부', 0, 3),
  (251001, '④', '출력·입력만', 0, 4),
  (251002, '①', '{민수=85}, 평균=85.0', 1, 1),
  (251002, '②', '{민수=85}, 평균=85', 0, 2),
  (251002, '③', '{}, 평균=0.0', 0, 3),
  (251002, '④', '오류 발생', 0, 4),
  (251003, '①', 'map', 0, 1),
  (251003, '②', 'mapToInt', 1, 2),
  (251003, '③', 'toInt', 0, 3),
  (251003, '④', 'filter', 0, 4),
  (251004, '①', '파일 저장부터', 0, 1),
  (251004, '②', '등록·조회 같은 핵심 흐름을 먼저 만들고 검증·수정·파일 저장을 단계적으로 추가', 1, 2),
  (251004, '③', '모든 기능을 동시에', 0, 3),
  (251004, '④', '테스트만 먼저', 0, 4)
ON DUPLICATE KEY UPDATE choice_text = VALUES(choice_text), is_correct = VALUES(is_correct), sort_order = VALUES(sort_order);

COMMIT;
