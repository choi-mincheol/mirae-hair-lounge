# 단위 테스트 안내: 미용 제품 판매 관리 (mirae-hair-lounge)

> 근거: 계획 `construction/code-generation/code-generation-plan.md`, 작업 방식 `inception/practices-discovery/team-practices.md`(TDD, Standard, `./gradlew build` 하나). 이 계획은 작업 단위가 하나라서 "이 단위의 테스트"는 이 프로젝트의 테스트 전체입니다.

## 1. 테스트 도구와 설정

- JUnit 5(Jupiter), Spring Boot 4.1.2 테스트 스타터(Spring Boot Test, MockMvc, AssertJ, Mockito), spring-security-test, MyBatis 테스트 지원(`@MybatisTest`).
- 모든 테스트는 `test` 프로필로 돌고 인메모리 H2(`jdbc:h2:mem:mirae`)를 씁니다. 설정은 `src/test/resources/application-test.yml`에 있고, 테스트 전용 더미 JWT 서명 키와 관리자 값을 담습니다(실제 비밀 값이 아님). 그래서 환경변수 없이 테스트가 돕니다(NFR2).
- 시간이 걸린 규칙(오늘, 이번 달, 연체)은 Asia/Seoul 고정 시계(`TestClockConfig`)로 검사합니다.
- Gradle은 `JAVA_HOME`이 JDK 25를 가리킬 때 실행합니다. 예: `export JAVA_HOME=$(/usr/libexec/java_home -v 25)`.

## 2. 실행 명령

첫 Red 단계 전에(계획 Step 3) 다음 명령이 실행되는지 확인합니다:

```bash
./gradlew test --tests 'com.mirae.hairlounge.ApplicationContextTest'
```

이 단위의 테스트 전체(빌드 게이트, Build and Test가 실행하는 명령):

```bash
./gradlew build
```

TDD 중 한 기능 묶음만 돌릴 때(계획 단계별):

| 계획 단계 | 명령 |
|---|---|
| Step 4~9 뼈대 | `./gradlew test --tests 'com.mirae.hairlounge.common.*' --tests 'com.mirae.hairlounge.auth.*' --tests 'com.mirae.hairlounge.product.*'` |
| Step 10 계정 | `./gradlew test --tests 'com.mirae.hairlounge.auth.*'` |
| Step 11~12 상품·재고 | `./gradlew test --tests 'com.mirae.hairlounge.product.*'` |
| Step 13 거래처 | `./gradlew test --tests 'com.mirae.hairlounge.client.*'` |
| Step 14~17 판매 | `./gradlew test --tests 'com.mirae.hairlounge.sale.*'` |
| Step 18~19 실적·대시보드 | `./gradlew test --tests 'com.mirae.hairlounge.report.*'` |
| Step 20 공통 화면 | `./gradlew test --tests 'com.mirae.hairlounge.common.*'` |

Red 단계마다 위 명령의 실패 출력(실패한 테스트 이름과 이유)을 `code-summary.md`에 짧게 남깁니다.

## 3. 테스트 범위와 양 (Standard)

컴포넌트마다 5~8개 이상, 단위 테스트와 주요 경계의 통합 테스트를 씁니다. 주요 경계는 MyBatis 매퍼와 H2, Spring Security/JWT 인증 흐름, 컨트롤러 요청 처리(MockMvc)입니다.

| 컴포넌트 | 계층별 테스트 | 대략 개수 |
|---|---|---|
| Common | 컨텍스트·설정, JWT 발급·검증·재발급, 보안 흐름(MockMvc), 공통 화면 규칙, 오류 처리 | 25 |
| Auth | 사용자 매퍼, 로그인, 첫 관리자, 계정 관리, 비밀번호 변경, 컨트롤러 | 25 |
| Product | 스키마, 분류·상품·옵션 매퍼, 상품 서비스, 재고 차감 동시성, 입고, 컨트롤러, 화면 | 40 |
| Client | 매퍼, 서비스, 컨트롤러, 화면 | 15 |
| Sale | 금액 계산, 판매 등록, 할부·입금, 미수금·연체, 수정·취소, 이력, 요청·승인, 컨트롤러, 화면 | 70 |
| Report | 실적 매퍼, 실적·대시보드 서비스, 컨트롤러, 성능(1만 건, 2초) | 25 |

- 요구사항(FR·NFR)마다 테스트가 하나 이상 있어야 합니다(NFR11). FR7.7은 화면 설계에서 없앤 항목이라 테스트가 없습니다.
- 각 테스트 파일은 정상 흐름과 최소 두 개의 오류·경계 상황을 다룹니다. 항상 통과하는 테스트(`assertTrue(true)` 같은 것)는 쓰지 않습니다.

## 4. 커버리지 기준

- 수치 기준은 두지 않습니다(작업 방식 Q6). 커버리지 측정 도구나 임계값 설정은 넣지 않습니다.
- 대신 요구사항마다 테스트가 있는지를 `traceability.json`으로 확인하고, Build and Test가 전체 테스트로 다시 확인합니다.

## 5. 가짜 객체(mock)와 대역 사용

- 매퍼 테스트(`@MybatisTest`)와 보안 흐름 테스트는 가짜를 쓰지 않고 실제 인메모리 H2와 실제 필터 체인으로 돌립니다(주요 경계 통합 테스트).
- 서비스 단위 테스트는 다른 컴포넌트의 서비스나 포트만 Mockito로 바꿉니다. 예: `SaleRegisterServiceTest`의 재고 부족 경우는 `StockService`를 가짜로, 동시성은 `StockConcurrencyTest`가 실제 DB로 확인합니다.
- `ClientSalesSummary`는 Step 13에서 가짜 구현으로 쓰고, Step 15에서 실제 구현(`ClientSalesSummaryAdapter`) 테스트를 더합니다.
- 시계는 늘 고정 시계를 주입합니다. `LocalDate.now()`를 직접 부르지 않습니다.

## 6. 테스트 데이터

- 각 테스트는 필요한 데이터를 테스트 안에서 만들고(`@Sql` 또는 테스트용 생성 도우미 `TestFixtures`), 트랜잭션 롤백이나 테이블 비우기로 다른 테스트에 남기지 않습니다.
- 동시성 테스트(`StockConcurrencyTest`)는 트랜잭션 롤백을 쓸 수 없으므로 끝날 때 테이블을 비웁니다.
- 성능 테스트(`ReportPerformanceTest`, `DashboardPerformanceTest`)는 판매 1만 건, 옵션 1천 개를 한 번에 넣고(배치 INSERT) 조회 시간을 잽니다. 이 테스트도 `./gradlew build` 안에서 돕니다.
- 개발용 예시 데이터(`data-dev.sql`)는 `dev` 프로필에서만 쓰고 테스트에는 쓰지 않습니다.
