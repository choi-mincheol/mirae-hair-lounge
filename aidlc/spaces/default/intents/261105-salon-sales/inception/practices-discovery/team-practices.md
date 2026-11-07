# 팀 작업 방식

> 2026-11-05 Practices Discovery 인터뷰(Q1~Q8)에서 확인한 이 프로젝트의 작업 방식입니다. 근거는 `evidence.md`에 있습니다.

## Way of Working

- 우리는 1인 개인 프로젝트로 일합니다. 저장소는 하나이고 기본 브랜치는 `main`이며, 원격은 GitHub `origin`입니다.
- AI-DLC는 스스로 커밋하거나 푸시하지 않습니다. 작업하는 중간중간 알맞은 시점(예: 기능 하나가 끝나고 `./gradlew build`가 통과했을 때)에 커밋할지, 푸시할지를 사용자에게 묻고, 사용자가 동의한 것만 실행합니다. 커밋만 동의하면 커밋만 하고, 푸시는 따로 동의를 받습니다.
- 커밋은 지금 작업 중인 브랜치(기본 `main`)에 남깁니다. 브랜치는 사용자가 요청할 때만 만듭니다. 오래 사는 브랜치(`develop`, `release/*`)는 두지 않습니다.
- AI-DLC가 Construction 작업 공간(worktree)을 만들 경우 기본값을 따릅니다: base `main`, target `main`, squash merge. 이때도 `main`에 합치는 커밋과 푸시는 위와 같이 사용자 동의를 받은 뒤에만 합니다. 이번 계획은 작업 단위가 하나라 별도 worktree가 생길 가능성은 낮습니다.
- 리뷰어가 따로 없으므로 PR 리뷰 절차는 두지 않습니다. 커밋을 제안하기 전에 로컬에서 `./gradlew build`가 통과해야 합니다.

## Walking Skeleton

- 우리는 얇은 끝단 간 뼈대(walking skeleton)를 먼저 만듭니다. 범위는 로그인부터 상품 1건 조회까지입니다: 앱이 `127.0.0.1`에서 기동되고, H2에 연결되고, Spring Security/JWT 로그인을 거쳐, Thymeleaf 화면에서 상품 1건을 조회할 수 있어야 합니다.
- 이번 계획(`salon-sales-solo`)은 작업 단위가 하나뿐이고 Units Generation 단계가 없어서, AI-DLC가 자동으로 거는 뼈대 확인 체크포인트가 없습니다. 그래서 이렇게 운영합니다: Code Generation 계획의 첫 단계를 이 뼈대로 잡고, 뼈대를 만든 뒤 `./gradlew build` 통과와 함께 사용자에게 확인을 받을 때까지 멈춥니다. 사용자가 확인한 뒤에 나머지 기능(상품 관리, 판매 등록, 판매 실적 조회 등)을 만듭니다.

## Testing Posture

- **Methodology**: tdd
- **Methodology evidence**: 인터뷰 Q5(테스트 작성 순서)에서 사용자가 "B. 테스트를 먼저 쓰고 구현 (TDD)"를 골랐습니다.
- **Ordering**: 테스트 가능한 계층(MyBatis 매퍼, 서비스, 컨트롤러)마다 실패하는 테스트를 먼저 쓰고, 그 테스트를 통과하는 최소한의 코드를 구현한 다음 리팩터링하며, 모든 테스트는 `./gradlew build` 안에서 실행합니다.
- 커버리지: 수치 기준은 두지 않습니다(Q6). 요구사항마다 테스트를 최소 한 개 두고, 기존 테스트는 항상 통과해야 합니다.
- 테스트 양: Test Strategy는 Standard입니다. 단위 테스트와 주요 경계의 통합 테스트를 작성합니다. 주요 경계는 MyBatis 매퍼와 H2 사이, Spring Security/JWT 인증 흐름, 컨트롤러 요청 처리(MockMvc)입니다. 이 계획의 기본 기준(요구사항마다 테스트, 컴포넌트마다 정상 흐름 테스트)에 더해지며 줄이지 않습니다.
- 도구: JUnit 5(Jupiter)와 Spring Boot 기본 테스트 도구(Spring Boot Test, MockMvc, spring-security-test, AssertJ, Mockito)를 씁니다. 테스트는 인메모리 H2로 실행합니다.
- 품질 게이트: CI는 없습니다. 유일한 게이트는 로컬 `./gradlew build`이며, 새로 추가하는 테스트도 모두 이 빌드 안에서 실행됩니다. 별도 테스트 명령을 따로 기억해야 하는 구성은 만들지 않습니다.
- 항상 통과하는 테스트(`assertTrue(true)` 같은 것)는 쓰지 않습니다. 각 테스트 파일은 정상 흐름과 최소 두 개의 오류/경계 상황을 다룹니다.

## Deployment

- 이 프로젝트는 배포하지 않습니다. staging·production 환경이나 배포 파이프라인이 없고, 내 PC에서 로컬로만 실행합니다.
- 실행은 `./gradlew bootRun`으로 합니다(Q3).
- 서버는 `127.0.0.1`에만 바인드합니다(`server.address=127.0.0.1`). 외부 네트워크에서 접근할 수 없게 합니다.
- 데이터는 H2 파일 모드로 저장해 앱을 다시 켜도 남게 하고, 테스트는 인메모리 H2를 씁니다(Q4). DB 파일은 `.gitignore`에 넣어 커밋되지 않게 합니다.
- 지금은 H2를 쓰고, 나중에 MariaDB를 설치해 쓸 수 있게 되면 전환합니다. 이를 위해 DB 접속 정보는 Spring 설정 파일/프로필로 분리합니다.
- DB 비밀번호와 JWT 서명 키 같은 비밀 값은 코드나 저장소에 넣지 않고 환경변수로 받습니다.
- 되돌리기: 문제가 생기면 `git revert`로 이전 커밋 상태로 돌리고 `./gradlew bootRun`으로 다시 실행합니다.

## Code Style

- 포매터나 린터 도구는 두지 않습니다. Java 관례와 IDE 기본 포맷(공백 4칸 들여쓰기)을 따릅니다(Q7). 나중에 설정 파일이 생기면 그 설정이 우선입니다.
- 이름 규칙은 Java 관례를 따릅니다: 클래스는 PascalCase, 메서드·변수는 camelCase, 상수는 UPPER_SNAKE_CASE, 패키지는 소문자. JavaScript(jQuery)도 camelCase를 씁니다.
- 패키지는 기능별(`product`, `sale`, `report`, `auth`)로 나누고, 각 기능 패키지 안에 controller/service/mapper를 둡니다. 여러 기능이 함께 쓰는 코드(설정, 보안 공통 처리, 예외 처리, 유틸 등)는 `common` 패키지에 둡니다(Q8).
- 화면은 백엔드가 Thymeleaf로 그리고, 화면별 JavaScript는 정적 파일로 분리합니다. 별도 프론트엔드 서버나 빌드 도구(npm)는 두지 않습니다.
