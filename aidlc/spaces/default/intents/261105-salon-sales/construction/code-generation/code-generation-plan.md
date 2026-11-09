# 코드 생성 계획: 미용 제품 판매 관리 (mirae-hair-lounge)

## Summary

- Builds: Spring Boot 4.1 + Thymeleaf + MyBatis + JWT 판매 관리 앱 전체(뼈대 먼저: 로그인부터 상품 1건 조회까지, 확인받은 뒤 나머지 기능)
- Touches: `build.gradle`, `settings.gradle`, Gradle wrapper, `src/main/java/com/mirae/hairlounge/{common,auth,product,client,sale,report}`, `src/main/resources/{application*.yml,schema.sql,mapper,templates,static}`, `src/test/...`, `.gitignore`(DB 파일 줄 추가)
- Tests: 약 200개의 JUnit 5 테스트(컴포넌트마다 매퍼·서비스·컨트롤러 단위/통합 테스트, 인메모리 H2), 모두 `./gradlew build` 안에서 실행

## 근거

- 요구사항: `inception/requirements-analysis/requirements.md` (FR1~FR10, NFR1~NFR12)
- 화면 설계: `inception/refined-mockups/mockups.md` (S01~S20, 1절 할부·입금 변경)
- 컴포넌트 설계: `inception/domain-design/components.md`, 결정 기록 `inception/domain-design/decisions.md` (ADR-001~ADR-010)
- 작업 방식: `inception/practices-discovery/team-practices.md` (TDD, 뼈대 먼저, `./gradlew build` 하나, `127.0.0.1`, 커밋·푸시는 동의 후)
- 이 계획에는 User Stories, Units Generation, Functional Design 단계가 없어서 각 단계는 요구사항 ID(FR, NFR)에 연결합니다. 작업 단위는 하나입니다.

## 확인한 버전 (ADR-009, 2026-11-09 Maven Central과 Gradle 서비스에서 확인)

| 항목 | 버전 | 근거 |
|---|---|---|
| Java | 25 (Amazon Corretto 25 또는 다른 JDK 25 배포판) | README |
| Spring Boot | 4.1.2 | 마지막 GA(4.2.0은 아직 RC). 4.x는 Java 17~25 공식 지원 |
| MyBatis Spring Boot Starter | 4.1.0 | 마지막 GA, Spring Boot 4.1 계열 |
| Gradle (wrapper) | 9.9.0 | 현재 안정판, Java 25 실행·빌드 지원 |
| JJWT | 0.13.0 | 마지막 GA |
| H2 | Spring Boot 4.1.2가 관리하는 버전 | 버전을 따로 적지 않음 |
| Bootstrap, jQuery WebJar | Step 2에서 Maven Central 마지막 GA로 고정 | 화면 설계 `design-system-mapping.md`(Bootstrap 5) |

이 PC에는 지금 JDK 8만 있고 Gradle 명령도 없습니다(2026-11-09 확인). Step 1에서 준비합니다.

## 도메인 설계 검토 의견의 반영

Domain Design 승인 때 위험으로 받아들인 검토 의견 R-01~R-06은 도메인 문서를 고치지 않고, 이 계획의 구현 규칙으로 처리합니다.

- R-01 순환 주입: `UserAccountLookup` 구현은 Auth의 `UserAccountLookupAdapter`(사용자 매퍼만 주입), `ClientSalesSummary` 구현은 Sale의 `ClientSalesSummaryAdapter`(판매 조회 매퍼만 주입)로 둡니다. `PasswordEncoder` 빈은 JWT 필터가 없는 `PasswordConfig`에 둡니다. 순환 주입 허용 설정은 켜지 않습니다.
- R-02 연체 계산 위치: 미수금·연체 계산은 Sale의 `ReceivableQueryService` 한 곳에 둡니다. Report의 대시보드는 이 서비스를 부르고, Report가 직접 JOIN하는 범위는 실적 집계와 이름 표시로 한정합니다.
- R-03 연체 금액·가장 오래된 연체 예정일: `ReceivableQueryService`가 판매별로 미수금, 연체 금액(오늘보다 앞선 예정일의 미입금 회차 금액 합계), 가장 오래된 연체 예정일을 함께 돌려줍니다.
- R-04 수정 때 검사 범위: 판매 수정과 요청 승인 때 거래 중·판매 중 검사는 거래처를 바꿀 때와 새 옵션 줄을 넣을 때만 합니다. 이미 있는 거래처와 옵션 줄은 그대로 둘 수 있습니다.
- R-05 동시 요청: `change_request.pending_sale_id`(대기 중일 때만 판매 id, 처리되면 NULL)에 유일 제약을 둡니다. 관리자가 대기 요청이 있는 판매를 직접 고치거나 취소하면 그 요청은 "관리자가 판매를 직접 수정(취소)해 요청이 닫혔습니다" 사유로 자동 거절됩니다. `request_token` 유일 값 위반이 나면 이미 만든 판매 상세로 보냅니다.
- R-06 로그인 후 돌아가기: 만료로 로그인 화면에 보낼 때 원래 주소를 `redirect` 요청 값으로 넘깁니다. `/`로 시작하고 `//`나 `\`로 시작하지 않는 이 앱 안의 상대 경로만 받고, 아니면 대시보드로 보냅니다.

## 공통 구현 규칙

- 패키지: `com.mirae.hairlounge.{common,auth,product,client,sale,report}`, 각 패키지 안에 `controller`, `service`, `mapper`, `domain`(작업 방식 Code Style). 공백 4칸 들여쓰기.
- 쓰기는 소유 컴포넌트의 매퍼만 합니다(ADR-003). 여러 컴포넌트를 거치는 쓰기는 가장 바깥 서비스 메서드 하나의 `@Transactional`로 묶습니다(NFR5).
- 스키마: `src/main/resources/schema.sql`에 `CREATE TABLE IF NOT EXISTS`로 둡니다(H2와 MariaDB 공통 문법, NFR9). 테이블·칼럼은 snake_case. 금액은 `BIGINT`(NFR7), 일시는 `TIMESTAMP`, JSON은 `CLOB`(ADR-007).
- 프로필: 기본 `application.yml`(`server.address=127.0.0.1`, H2 파일 `./data/mirae`), `test` 프로필(인메모리 H2, 테스트 전용 더미 서명 키). 비밀 값(`MIRAE_JWT_SECRET`, `MIRAE_ADMIN_LOGIN_ID`, `MIRAE_ADMIN_PASSWORD`)은 환경변수로만 받습니다(NFR3).
- 시간: `Clock` 빈(Asia/Seoul) 하나를 주입해 쓰고, 테스트는 고정 시계를 씁니다(NFR8).
- 화면: Thymeleaf 공통 레이아웃 조각, Bootstrap 5와 jQuery는 WebJar, 화면별 JavaScript는 `static/js/<화면>.js`. 버튼·입력에는 `data-testid`를 붙입니다.
- 각 테스트 파일은 정상 흐름과 최소 두 개의 오류·경계 상황을 다루고, 항상 통과하는 테스트는 쓰지 않습니다.

## Testing Contract

```json
{
  "version": 1,
  "methodology": "tdd",
  "source": "team",
  "ordering": "테스트 가능한 계층(MyBatis 매퍼, 서비스, 컨트롤러)마다 실패하는 테스트를 먼저 쓰고, 그 테스트를 통과하는 최소한의 코드를 구현한 다음 리팩터링하며, 모든 테스트는 `./gradlew build` 안에서 실행합니다.",
  "scope": "express",
  "test_strategy": "standard",
  "project_type": "greenfield",
  "applicable_notes": [
    {
      "layer": "org",
      "text": "We treat tests as a first-class deliverable in every Bolt. The specific\nmethodology (TDD, BDD, ATDD, or classic test-after) is affirmed at\npractices-discovery and recorded in `team.md` under this heading with explicit\n`Methodology` and `Ordering` fields; Code Generation resolves those fields\nindependently from coverage, tooling, and scope notes.\n\nWhen no posture has been affirmed, our default per scope is:\n- **Methodology**: test-after\n- **Ordering**: implement each applicable testable layer, then write and run\n  that layer's tests.\n- `mvp`, `enterprise`, `feature`, `infra`, `classic` add an 80% line-coverage\n  floor and CI execution before merge.\n- `bugfix`, `security-patch` add a targeted regression for the specific\n  bug/vulnerability and require the existing suite to remain green.\n- `express` uses the Minimal strategy: requirement-driven unit tests (one per\n  requirement, with a happy-path floor per component); existing tests remain\n  green.\n- `poc`, `refactor`, `workshop` add no extra new-test floor and require the\n  existing suite to remain green.\n\nThe active `Test Strategy` still applies in every scope and determines test\nvolume/types. Scope floors are additive; they never reduce or replace the\nselected strategy.\n\nBuild and Test verifies defined coverage floors and affirmed quality targets;\nthey may not be weakened to make a step pass.\n\nAffirm a stricter posture in `team.md` if the team commits to one."
    },
    {
      "layer": "team",
      "text": "- **Methodology**: tdd\n- **Methodology evidence**: 인터뷰 Q5(테스트 작성 순서)에서 사용자가 \"B. 테스트를 먼저 쓰고 구현 (TDD)\"를 골랐습니다.\n- **Ordering**: 테스트 가능한 계층(MyBatis 매퍼, 서비스, 컨트롤러)마다 실패하는 테스트를 먼저 쓰고, 그 테스트를 통과하는 최소한의 코드를 구현한 다음 리팩터링하며, 모든 테스트는 `./gradlew build` 안에서 실행합니다.\n- 커버리지: 수치 기준은 두지 않습니다(Q6). 요구사항마다 테스트를 최소 한 개 두고, 기존 테스트는 항상 통과해야 합니다.\n- 테스트 양: Test Strategy는 Standard입니다. 단위 테스트와 주요 경계의 통합 테스트를 작성합니다. 주요 경계는 MyBatis 매퍼와 H2 사이, Spring Security/JWT 인증 흐름, 컨트롤러 요청 처리(MockMvc)입니다. 이 계획의 기본 기준(요구사항마다 테스트, 컴포넌트마다 정상 흐름 테스트)에 더해지며 줄이지 않습니다.\n- 도구: JUnit 5(Jupiter)와 Spring Boot 기본 테스트 도구(Spring Boot Test, MockMvc, spring-security-test, AssertJ, Mockito)를 씁니다. 테스트는 인메모리 H2로 실행합니다.\n- 품질 게이트: CI는 없습니다. 유일한 게이트는 로컬 `./gradlew build`이며, 새로 추가하는 테스트도 모두 이 빌드 안에서 실행됩니다. 별도 테스트 명령을 따로 기억해야 하는 구성은 만들지 않습니다.\n- 항상 통과하는 테스트(`assertTrue(true)` 같은 것)는 쓰지 않습니다. 각 테스트 파일은 정상 흐름과 최소 두 개의 오류/경계 상황을 다룹니다."
    }
  ],
  "obligations": {
    "strategy": "standard",
    "strategy_volume": [
      "Five to eight tests per component.",
      "Unit tests plus integration tests for key boundaries.",
      "Add E2E, performance, or security tests when requirements demand them."
    ],
    "scope_floor": [
      "Keep the existing test suite green.",
      "This scope adds no extra new-test floor beyond the selected test strategy."
    ],
    "combination_rule": "Apply every selected-strategy obligation and every scope-floor obligation; neither replaces the other, and a targeted scope regression may add the narrowest necessary test type beyond the strategy default."
  },
  "plan_profile": {
    "methodology": "tdd",
    "runner_step": "Bootstrap the minimal test runner/configuration and record the exact unit-scoped command.",
    "runner_ready_before_first_test": true,
    "testable_layers": [
      "Data model / database behavior",
      "Repository / data access",
      "Business logic",
      "API / endpoint",
      "Frontend behavior"
    ],
    "steps": [
      "Project structure and production configuration skeleton.",
      "Bootstrap the minimal test runner/configuration and record the exact unit-scoped command.",
      "Data model / database behavior - Red: write the failing tests and record the failing command output.",
      "Data model / database behavior - Green: implement only enough behavior to pass.",
      "Data model / database behavior - Refactor: improve the implementation while tests stay green.",
      "Repository / data access - Red: write the failing tests and record the failing command output.",
      "Repository / data access - Green: implement only enough behavior to pass.",
      "Repository / data access - Refactor: improve the implementation while tests stay green.",
      "Business logic - Red: write the failing tests and record the failing command output.",
      "Business logic - Green: implement only enough behavior to pass.",
      "Business logic - Refactor: improve the implementation while tests stay green.",
      "API / endpoint - Red: write the failing tests and record the failing command output.",
      "API / endpoint - Green: implement only enough behavior to pass.",
      "API / endpoint - Refactor: improve the implementation while tests stay green.",
      "Frontend behavior - Red: write the failing tests and record the failing command output.",
      "Frontend behavior - Green: implement only enough behavior to pass.",
      "Frontend behavior - Refactor: improve the implementation while tests stay green.",
      "Environment/build configuration.",
      "Documentation and traceability."
    ]
  },
  "input_sha256": "sha256:b50fbe4f90c67f9a8470911e9a46844d00f7aac904d52eeac80a27ce55364507",
  "contract_sha256": "sha256:48caed9ce2d455d7e7b672a9a3ca1e8bffd4cb2d13dff5c3b17091b6b5cfffbb"
}
```

TDD 순서는 기능 묶음마다 계층별로 적용합니다: 데이터 모델·DB(스키마) → 매퍼 → 서비스 → 컨트롤러 → 화면 동작. 각 계층은 Red(실패하는 테스트를 쓰고 실패 출력 기록) → Green(통과할 만큼만 구현) → Refactor(테스트가 통과한 채로 정리) 순서입니다. 화면 동작은 별도 프런트엔드 빌드 도구가 없으므로(작업 방식) MockMvc로 그려진 HTML(역할별 메뉴, 버튼, `data-testid`, 오류 문구)을 검사합니다.

## 단계

### 1부: 준비와 뼈대 (끝나면 멈추고 확인받음)

- [x] **Step 1. 개발 환경 준비** [NFR2]
  - JDK 25가 없으므로 사용자에게 설치 방법을 묻고(예: IntelliJ의 JDK 다운로드, Amazon Corretto 25 설치 파일), 사용자가 고른 방법으로 사용자가 동의한 뒤에만 설치합니다. `/usr/libexec/java_home -v 25`로 확인합니다.
  - Gradle 명령이 없으므로 Gradle 9.9.0 배포판을 프로젝트 안 `build/tmp/gradle-bootstrap/`에 내려받아 SHA-256(`0b00b0ff...177a`)을 확인하고, `JAVA_HOME`을 JDK 25로 두고 `gradle wrapper --gradle-version 9.9.0`을 실행해 `gradlew`, `gradlew.bat`, `gradle/wrapper/`를 만든 뒤 내려받은 배포판은 지웁니다. `gradle-wrapper.properties`에 `distributionSha256Sum`을 넣습니다.
- [x] **Step 2. 프로젝트 구조와 운영 설정 뼈대** [NFR1] [NFR4] [NFR9] [FR10.1]
  - `settings.gradle`, `build.gradle`(Spring Boot 4.1.2, Java toolchain 25, 의존: webmvc, thymeleaf, security, validation, mybatis-spring-boot-starter 4.1.0, H2, jjwt 0.13.0, thymeleaf-extras-springsecurity, Bootstrap·jQuery WebJar와 locator, Jackson; 테스트: spring-boot 테스트 스타터, spring-security-test).
  - `application.yml`: `server.address: 127.0.0.1`, `server.port: 8080`, H2 파일 `jdbc:h2:file:./data/mirae`, `spring.sql.init.mode: always`, MyBatis 매퍼 위치와 snake_case → camelCase 변환, `spring.main.allow-circular-references`는 켜지 않음.
  - `application-mariadb.yml`(접속 정보는 환경변수, 지금은 쓰지 않음), `MiraeHairLoungeApplication.java`.
  - `.gitignore`(사용자 파일)에 `data/`와 `*.mv.db`, `*.trace.db` 줄을 추가합니다. 이 계획에서 바꾸는 유일한 기존 파일입니다.
- [x] **Step 3. 테스트 실행 준비** [NFR2] [NFR11]
  - `src/test/resources/application-test.yml`(인메모리 H2 `jdbc:h2:mem:mirae`, 테스트 전용 더미 서명 키와 관리자 값. 실제 비밀 값이 아님), 공통 테스트 지원 클래스(`TestClockConfig` 고정 시계).
  - `ApplicationContextTest`(컨텍스트가 뜨는지, `server.address`가 `127.0.0.1`인지, 서명 키가 없으면 시작이 실패하는지)를 먼저 써서 `./gradlew test --tests 'com.mirae.hairlounge.ApplicationContextTest'`가 실행되는지 확인합니다.
- [x] **Step 4. 뼈대: 데이터 모델·DB** [FR1.1] [FR2.1] [FR2.2] [FR2.3] [NFR4] [NFR7]
  - [x] Red: `SchemaTest` — `user_account`, `category`, `product`, `product_option` 테이블과 유일 제약(로그인 아이디, 분류명, 상품 안 옵션명, 바코드)을 검사하는 테스트를 쓰고 실패 출력을 기록합니다.
  - [x] Green: `schema.sql`에 네 테이블을 만듭니다.
  - [x] Refactor: 칼럼 이름과 색인 정리.
- [x] **Step 5. 뼈대: 매퍼** [FR1.1] [FR2.4]
  - [x] Red: `UserAccountMapperTest`(로그인 아이디로 찾기, 없는 아이디, 사용 중지 계정), `ProductMapperTest`(상품 1건과 옵션 조회, 없는 id, 옵션 없는 상품).
  - [x] Green: `UserAccountMapper`, `ProductMapper`, `ProductOptionMapper`와 XML.
  - [x] Refactor.
- [x] **Step 6. 뼈대: 서비스(보안·JWT·로그인·상품 조회)** [FR1.1] [FR1.2] [FR1.3] [FR1.5] [NFR3]
  - [x] Red: `JwtTokenServiceTest`(발급·검증, 만료, 서명 틀림, 5분 지난 토큰 재발급 판단), `LoginServiceTest`(맞는 비밀번호, 틀린 비밀번호, 사용 중지 계정은 같은 오류), `AdminBootstrapTest`(빈 DB + 환경변수면 관리자 생성, 환경변수 없으면 변수 이름이 든 오류로 시작 실패, 이미 관리자가 있으면 아무것도 안 함), `ProductQueryServiceTest`(상품 1건 조회, 없으면 찾을 수 없음 예외).
  - [x] Green: `common.security`(`SecurityConfig`, `JwtTokenService`, `JwtCookieFilter`, `UserAccountLookup` 포트, `CookieCsrfTokenRepository` 설정), `common.config.PasswordConfig`, `auth.service.LoginService`, `auth.service.AdminBootstrap`, `auth.service.UserAccountLookupAdapter`(R-01), `product.service.ProductQueryService`.
  - [x] Refactor: 순환 주입이 없는지 확인(R-01).
- [x] **Step 7. 뼈대: 컨트롤러** [FR1.1] [FR1.4] [FR1.5] [NFR3]
  - [x] Red: `SecurityFlowTest`(MockMvc: 로그인 안 하면 `/products/1`이 로그인 화면으로, 로그인 성공 시 JWT 쿠키에 `HttpOnly`·`SameSite=Strict`, CSRF 토큰 없는 POST 거부, 틀린 비밀번호 문구, 만료 토큰은 `redirect` 값과 함께 로그인 화면으로, 외부 주소 `redirect`는 무시(R-06), 로그아웃은 쿠키 삭제), `ProductViewControllerTest`(로그인 후 상품 1건 화면, 없는 상품은 찾을 수 없음 화면, 직원도 조회 가능).
  - [x] Green: `auth.controller.LoginController`, 상품 1건 조회 화면(`/products/{id}`), 공통 오류 처리(`GlobalExceptionHandler`, S20 화면).
  - [x] Refactor.
- [x] **Step 8. 뼈대: 화면 동작** [FR10.1] [FR1.4]
  - [x] Red: `LayoutViewTest`(공통 틀의 왼쪽 메뉴, 관리자와 직원의 메뉴 차이, CSRF meta 태그, 로그인 화면의 `data-testid`).
  - [x] Green: `templates/layout.html`, `login.html`, `product/detail.html`, `static/js/common.js`(`$.ajaxSetup`으로 `X-XSRF-TOKEN` 머리글), `static/css/app.css`.
  - [x] Refactor.
- [x] **Step 9. 뼈대 확인** [NFR1] [NFR2]
  - `./gradlew build`가 통과하는지 확인하고, 환경변수를 넣어 `./gradlew bootRun`으로 `127.0.0.1:8080`에서 로그인 → 상품 1건 조회가 되는지 확인합니다. 뼈대에는 아직 상품 등록 화면이 없으므로, `dev` 프로필에서만 예시 분류·상품 1건을 넣는 `data-dev.sql`을 둡니다(기본 프로필에는 넣지 않음, 이미 있으면 넣지 않음).
  - **여기서 멈추고 사용자에게 뼈대를 확인받습니다**(작업 방식 Walking Skeleton). 이때 커밋할지도 묻고, 동의한 것만 합니다. 확인을 받은 뒤 2부를 시작합니다.

### 2부: 계정과 상품

- [ ] **Step 10. Auth 나머지: 직원 계정·비밀번호 변경** [FR1.3] [FR1.6] [NFR3]
  - [ ] 매퍼 Red/Green/Refactor: `UserAccountMapperTest` 확장(만들기, 아이디 중복, 사용 여부 바꾸기).
  - [ ] 서비스 Red/Green/Refactor: `UserAccountServiceTest`(직원 만들기, 아이디 중복, 초기 비밀번호 8자 미만 거부, 사용 중지·다시 사용, 자기 자신 사용 중지 거부), `PasswordChangeServiceTest`(현재 비밀번호 틀림, 8자 미만, 성공 후 새 비밀번호로 로그인).
  - [ ] 컨트롤러 Red/Green/Refactor: `UserAccountControllerTest`(관리자만 `/users`, 직원은 권한 없음 화면, 사용 중지된 계정의 기존 토큰은 다음 요청에서 거부), `PasswordControllerTest`(오류 문구, 비밀번호 칸 비움).
  - [ ] 화면 동작 Red/Green/Refactor: S18, S19 화면과 확인창.
- [ ] **Step 11. Product: 분류·상품·옵션 관리** [FR2.1] [FR2.2] [FR2.3] [FR2.4] [FR2.5] [FR2.6] [FR10.3] [FR10.4]
  - [ ] DB·매퍼 Red/Green/Refactor: `CategoryMapperTest`, `ProductMapperTest` 확장(검색: 상품명·브랜드·바코드, 분류·판매 여부 거르기, 20건 페이지, 같은 값은 id 순).
  - [ ] 서비스 Red/Green/Refactor: `CategoryServiceTest`(중복 이름), `ProductCommandServiceTest`(옵션 없으면 "기본" 옵션, 옵션명 중복, 바코드 중복, 옵션 가격 비우면 기본값, 금액 음수·소수 거부, 판매 중지·재개, 삭제 없음).
  - [ ] 컨트롤러 Red/Green/Refactor: `ProductControllerTest`(직원의 상품 등록 요청 거부, 입력 오류 시 값 유지, 결과 없음 문구), `CategoryControllerTest`.
  - [ ] 화면 동작 Red/Green/Refactor: S03, S04, S05 화면, `static/js/product-form.js`(옵션 줄 추가·삭제).
- [ ] **Step 12. Product: 입고·재고·재고 변동 내역** [FR3.1] [FR3.2] [FR3.5] [FR3.6] [NFR6]
  - [ ] DB·매퍼 Red/Green/Refactor: `stock_receipt`, `stock_movement` 테이블, `StockMapperTest`(조건부 차감이 재고 부족이면 0행, 충분하면 1행).
  - [ ] 서비스 Red/Green/Refactor: `StockServiceTest`(입고로 재고 증가와 변동 1줄, 입고 취소로 감소, 취소 후 음수면 거부, 차감 부족 시 예외, 마지막 변동 후 재고 = 현재 재고), `StockConcurrencyTest`(두 스레드가 동시에 마지막 재고를 차감하면 한쪽만 성공), `LowStockTest`(현재 = 최소면 재고 부족).
  - [ ] 컨트롤러 Red/Green/Refactor: `ReceiptControllerTest`(관리자 전용, 취소 거부 문구), `StockMovementControllerTest`(직원도 조회).
  - [ ] 화면 동작 Red/Green/Refactor: S06, S07 화면, 상품 목록의 "재고 부족" 배지와 글자, 품목 선택 창(S12-1) 조각.

### 3부: 거래처와 판매

- [ ] **Step 13. Client** [FR4.1] [FR4.2] [FR4.3] [FR4.4]
  - [ ] DB·매퍼 Red/Green/Refactor: `client` 테이블, `ClientMapperTest`(상호·연락처 검색, 거래 여부 거르기).
  - [ ] 서비스 Red/Green/Refactor: `ClientServiceTest`(같은 상호 경고 후 "그래도 등록", 거래 중지, `requireActive`가 거래 중지·없는 거래처 거부).
  - [ ] 컨트롤러 Red/Green/Refactor: `ClientControllerTest`(직원은 조회만, 목록·상세가 `ClientSalesSummary` 포트 값을 보여줌 — 이 단계에서는 가짜 구현, Step 15에서 실제 구현).
  - [ ] 화면 동작 Red/Green/Refactor: S08, S09, S10 화면.
- [ ] **Step 14. Sale: 판매 등록** [FR3.3] [FR5.1] [FR5.2] [FR5.3] [FR5.4] [FR5.5] [FR5.6] [NFR5] [NFR6] [NFR7]
  - [ ] DB·매퍼 Red/Green/Refactor: `sale`(`request_token` 유일, `version`), `sale_item`, `sale_history` 테이블과 `SaleMapperTest`.
  - [ ] 서비스 Red/Green/Refactor: `SaleCalculatorTest`(품목 합계, 금액·비율 할인과 원 미만 버림, 33,333원 10% → 30,000원, 할인 > 합계 거부, 비율 0~100 밖 거부), `SaleRegisterServiceTest`(같은 옵션 한 줄로 합침, 미래 일시 거부, 외상·할부는 거래처 필수, 판매 중지 옵션·거래 중지 거래처 거부, 재고 부족이면 전체 저장 안 함과 옵션·현재 재고 문구, 단가·매입가 복사, 같은 토큰 두 번이면 한 건(R-05), 중간 실패 시 재고 그대로).
  - [ ] 컨트롤러 Red/Green/Refactor: `SaleControllerTest`(직원도 등록, 저장 후 상세로, 재고 부족 시 입력 유지, 판매 번호 `S-0123` 표시).
  - [ ] 화면 동작 Red/Green/Refactor: S11, S12, S12-1, S12-2 화면, `static/js/sale-form.js`(합계·할인 미리 계산, 저장 버튼 끄기, 요청 토큰).
- [ ] **Step 15. Sale: 할부·입금·미수금·연체** [FR7.1] [FR7.2] [FR7.3] [FR7.4] [FR7.5] [FR7.6] [NFR8]
  - [ ] DB·매퍼 Red/Green/Refactor: `installment` 테이블, `InstallmentMapperTest`.
  - [ ] 서비스 Red/Green/Refactor: `InstallmentPlannerTest`(100,000원 3개월 → 33,333/33,333/33,334, 개월 수 1~36 밖 거부, 첫 예정일이 판매일보다 앞서면 거부, 31일 → 그 달 마지막 날), `PaymentServiceTest`(가장 앞 미입금 회차만 입금, 가장 뒤 입금 회차만 입금 취소, 관리자 전용, 이력 남김, version 다르면 거부), `ReceivableQueryServiceTest`(미수금, 완납, 어제 예정·미입금은 연체, 오늘 예정은 연체 아님, 연체 금액과 가장 오래된 연체 예정일(R-03), 취소된 판매 제외), `ClientSalesSummaryAdapterTest`(R-01, R-02).
  - [ ] 컨트롤러 Red/Green/Refactor: `PaymentControllerTest`(직원의 입금 요청 거부, CSRF 없는 입금 거부).
  - [ ] 화면 동작 Red/Green/Refactor: S13의 할부와 입금 영역, 연체 배지와 글자, 판매 목록·거래처 목록의 미수금·연체 칸.
- [ ] **Step 16. Sale: 관리자 수정·취소와 변경 이력** [FR3.4] [FR6.1] [FR6.2] [FR6.5] [FR7.4]
  - [ ] 서비스 Red/Green/Refactor: `SaleEditServiceTest`(수량 차이만큼 재고 조정, 재고 부족이면 거부, 금액 수정 시 미입금 회차 재분배, 남은 금액 음수 거부, 0이면 미입금 회차 삭제와 완납, 0원 회차가 생기면 거부(ADR-008), 미입금 회차 없는데 금액 변경 거부, 입금 있으면 개월 수·결제 수단·첫 예정일 변경 거부, 판매일이 첫 예정일보다 뒤가 되면 거부, 거래처를 안 바꾸면 거래 중지 거래처도 수정 가능(R-04), version 충돌 거부(ADR-010), 대기 요청 자동 거절(R-05)), `SaleCancelServiceTest`(재고 복원, 입금 있으면 거부, 실적·미수금에서 빠짐), `SaleHistoryTest`(전·후 JSON, 지우기 기능 없음, `schemaVersion`).
  - [ ] 컨트롤러 Red/Green/Refactor: `SaleEditControllerTest`(직원의 직접 수정 거부, 확인창 값).
  - [ ] 화면 동작 Red/Green/Refactor: S13 변경 이력과 [자세히], S14 관리자 수정 화면.
- [ ] **Step 17. Sale: 직원 요청과 승인** [FR6.3] [FR6.4]
  - [ ] DB·매퍼 Red/Green/Refactor: `change_request` 테이블(`pending_sale_id` 유일, R-05), `ChangeRequestMapperTest`(같은 판매에 대기 요청 두 개 넣으면 실패).
  - [ ] 서비스 Red/Green/Refactor: `ChangeRequestServiceTest`(요청만으로 판매·재고 그대로, 사유 필수, 대기 요청 하나뿐, 승인 시 그 시점 재고로 재계산·부족하면 승인 거부, 승인 후 이력에 요청자와 승인자, 거절 사유 필수, 취소 요청 승인 시 재고 복원).
  - [ ] 컨트롤러 Red/Green/Refactor: `ApprovalControllerTest`(관리자 전용, 비교 화면 값), `MyRequestControllerTest`(직원 자기 요청만).
  - [ ] 화면 동작 Red/Green/Refactor: S14 직원 수정 요청 화면, 취소 요청 창, S15, S16 화면, 바뀐 칸 표시.

### 4부: 실적과 대시보드

- [ ] **Step 18. Report: 판매 실적** [FR8.1] [FR8.2] [FR8.3] [FR8.4] [FR8.5] [FR8.6] [NFR8] [NFR10]
  - [ ] 매퍼 Red/Green/Refactor: `ReportMapperTest`(일별·월별 집계, 시작일·종료일 포함, 취소 제외, 상품·옵션 순위 정렬, 분류별, 결제 수단별, 이익).
  - [ ] 서비스 Red/Green/Refactor: `ReportServiceTest`(기간 기본값 이번 달, 시작일 > 종료일 거부, 판매 없음 표시, 할인 합계와 판매 금액 합계가 맞아떨어짐).
  - [ ] 컨트롤러 Red/Green/Refactor: `ReportControllerTest`(관리자 전용, 탭과 기간이 주소 값으로 유지).
  - [ ] 화면 동작 Red/Green/Refactor: S17 화면.
  - [ ] 성능: `ReportPerformanceTest`(판매 1만 건, 옵션 1천 개를 넣고 실적·판매 목록 조회가 2초 안).
- [ ] **Step 19. Report: 대시보드** [FR9.1] [FR9.2] [FR9.3]
  - [ ] 서비스 Red/Green/Refactor: `DashboardServiceTest`(관리자: 오늘·이번 달 판매 금액, 최근 10건, 재고 부족, 연체(`ReceivableQueryService` 사용, R-02), 승인 대기 수; 직원: 매출 합계·연체 없음, 자기 요청 상태; 데이터 없으면 "없음").
  - [ ] 컨트롤러 Red/Green/Refactor: `DashboardControllerTest`(로그인 후 첫 화면, 각 항목 링크).
  - [ ] 화면 동작 Red/Green/Refactor: S02 화면, 메뉴의 승인 대기 숫자.
  - [ ] 성능: `DashboardPerformanceTest`(같은 데이터에서 2초 안).

### 5부: 마무리

- [ ] **Step 20. 공통 화면 규칙 점검** [FR10.2] [FR10.3] [FR10.4] [NFR12]
  - [ ] Red/Green/Refactor: `CommonViewRulesTest`(되돌리기 어려운 동작에 확인창 조각이 있는지, 입력 오류 시 알림 "입력값을 확인해 주세요. 오류 n건", 모든 목록 20건 페이지, 서버 오류 화면에 기술 내용 없음, 로그에 비밀번호·토큰이 없음).
- [ ] **Step 21. 환경·빌드 설정 정리** [NFR2]
  - `./gradlew build`가 새로 받은 저장소에서 환경변수 없이 통과하는지(테스트는 테스트 프로필 값 사용) 확인합니다. 실행 방법(`JAVA_HOME`을 JDK 25로, 환경변수 세 개, `./gradlew bootRun`)은 `code-summary.md`에 적습니다.
- [ ] **Step 22. 문서와 추적표**
  - `code-summary.md`, `traceability.json`(FR·NFR → 구현·테스트 파일)을 작성합니다. 커밋·푸시는 사용자에게 묻고 동의한 것만 합니다.

## 요구사항과 단계 연결

| 요구사항 | 단계 |
|---|---|
| FR1.1~FR1.6 | Step 4~8, 10 |
| FR2.1~FR2.6 | Step 4~5, 11 |
| FR3.1~FR3.6 | Step 12, 14, 16 |
| FR4.1~FR4.4 | Step 13 |
| FR5.1~FR5.7 | Step 14 (FR5.7 목록·상세는 Step 14~16) |
| FR6.1~FR6.5 | Step 16, 17 |
| FR7.1~FR7.6 | Step 15, 16 (FR7.7은 화면 설계에서 없앰) |
| FR8.1~FR8.6 | Step 18 |
| FR9.1~FR9.3 | Step 19 |
| FR10.1~FR10.4 | Step 8, 11, 20 |
| NFR1~NFR12 | Step 2, 3, 6, 7, 9, 12, 14, 15, 18~21 |

## 이 계획에서 하지 않는 것

- MariaDB로의 실제 전환, 배포, CI, 결제 연동, CSV 내보내기, 차트(요구사항 7절).
- 브라우저 자동화(E2E) 테스트와 JavaScript 단위 테스트: npm 같은 프런트엔드 도구를 두지 않기로 해서(작업 방식), 화면은 MockMvc로 그려진 HTML을 검사하고 JavaScript의 계산은 서버가 같은 규칙으로 다시 검사합니다.
