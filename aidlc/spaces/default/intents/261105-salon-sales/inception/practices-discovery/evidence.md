# Practices Discovery 근거

> 이번 실행은 지원 에이전트 없이 리드(`aidlc-pipeline-deploy-agent`) 혼자 작업했으므로 독립 검토 기여 파일은 없습니다. Step 2 초안을 인터뷰(`practices-discovery-questions.md`, Q1~Q8) 결과로 확정했습니다.

## 살펴본 것

| 대상 | 확인한 내용 |
|---|---|
| `README.md` | 미용실 제품 판매 관리용 1인 개인 프로젝트. 결제 기능 없음. 상품 관리 / 판매 등록 / 판매 실적 조회, 추가 기능은 AI가 제안. 화면은 백엔드가 그림(Thymeleaf, jQuery, JavaScript, 별도 프론트 서버 없음). 백엔드: Java 25, JUnit, Spring Boot LTS, MyBatis, JWT, Spring Security, Gradle. DB: H2 우선, 추후 MariaDB 전환 가능. `## Mandated`에 사람이 쓴 규칙 두 개. |
| `aidlc/spaces/default/memory/org.md` | 다섯 절(`## Way of Working`, `## Walking Skeleton`, `## Testing Posture`, `## Deployment`, `## Code Style`)을 그린필드 기본 제안으로 사용. 확정된 팀 사실로 다루지 않음. |
| `aidlc/spaces/default/memory/team.md` | 다섯 절 모두 HTML 주석 템플릿뿐. 확정된 팀 관행 없음(재실행 아님). |
| `aidlc/spaces/default/memory/phases/inception.md` | 합격/불합격 기준, 모호한 표현 금지, 대안 비교, 추적성 규칙. 이 단계 산출물 작성 시 적용. |
| `aidlc/scopes/salon-sales-solo.md` | express 기반 맞춤 계획. `skeleton: off`, CI·배포 단계 모두 SKIP, Units Generation SKIP. |
| `aidlc/spaces/default/intents/261105-salon-sales/aidlc-state.md` | Project Type: Greenfield, Test Strategy: Standard, Depth: Standard. |
| git 상태 | HEAD `50f04259c6fa4cf190154680ddc3c89c65e595bb`(`Initial commit: add README and .gitignore`), 브랜치 `main`, 원격 `origin` = `https://github.com/choi-mincheol/mirae-hair-lounge.git`. CI 설정, 빌드 파일(`build.gradle`), 포매터/린터 설정 없음. |
| `.gitignore` | `build/`, `.env` 등은 제외 대상. `.gradle/`과 H2 DB 파일(`*.mv.db`)은 아직 제외 대상이 아님. |
| `practices-discovery-questions.md` | 인터뷰 8문항과 사용자 답(아래 표). |

## 인터뷰 결정

| 질문 | 선택 | 반영한 곳 |
|---|---|---|
| Q1. 코드 변경 기록 방식 | X. Other (please specify). 사용자 원문: "작업 진행하면서 커밋/푸쉬 진행할건지 나에게 물어본다." | `team-practices.md` `## Way of Working`, `discovered-rules.md` `## Mandated` 세 번째 규칙 |
| Q2. 처음 만들 범위 | C. 예, 로그인부터 상품 1건 조회까지 얇게 먼저 만들어 확인받은 뒤 나머지를 만든다 | `## Walking Skeleton` |
| Q3. 앱 실행 방식 | A. `./gradlew bootRun` (`127.0.0.1`에만 열림) | `## Deployment` |
| Q4. 데이터 보관 | A. 예, 파일 모드로 저장하고 테스트는 메모리 DB 사용 | `## Deployment`, `## Testing Posture` |
| Q5. 테스트 작성 순서 | B. 테스트를 먼저 쓰고 구현 (TDD) | `## Testing Posture`의 `**Methodology**: tdd`, `**Ordering**` |
| Q6. 테스트 커버리지 기준 | A. 수치 기준 없음. 요구사항마다 테스트 1개 이상, 기존 테스트 항상 통과 | `## Testing Posture` |
| Q7. 코드 포맷 | A. 도구 없이 Java 관례와 IDE 기본 포맷 | `## Code Style` |
| Q8. Java 패키지 구성 | C. 기능별로 나누고 공통 코드는 `common` 패키지로 분리 | `## Code Style` |

### 해석

- Q1: AI-DLC는 스스로 커밋하거나 푸시하지 않습니다. 작업 중 알맞은 시점에 커밋할지, 푸시할지를 사용자에게 묻고 동의한 것만 실행합니다. 사용자가 작업 방식의 조건으로 직접 밝힌 것이라 강제 규칙(`ALWAYS ...`)으로 올렸습니다. 브랜치 전략은 답에 따로 없어서 `org.md`의 기본값(지금 있는 브랜치에 커밋, 브랜치는 요청할 때만 생성, worktree는 base/target `main` + squash)을 그대로 따랐습니다. 초안의 "trunk-based로 `main`에 바로 커밋" 제안은 사용자가 고르지 않았으므로, 커밋 시점 결정권이 사용자에게 있다는 점을 앞세워 고쳐 적었습니다.
- Q4: 답 A에는 DB 파일 수동 복사(B)나 자동 백업(C)이 들어 있지 않아, 초안의 "큰 변경 전 DB 파일 복사" 문장은 뺐습니다.
- Q5: TDD 하나만 골랐으므로 `custom`이 아니라 `tdd`입니다. 순서는 계층마다 "실패하는 테스트 → 최소 구현 → 리팩터링"이고 모든 테스트는 `./gradlew build` 안에서 돕니다.

## 워킹 스켈레톤 운영 방식 (계획에 맞춘 조정)

- 사용자는 Q2에서 "로그인부터 상품 1건 조회까지 얇게 먼저 만들어 확인받은 뒤 나머지를 만든다"를 골랐습니다.
- 그러나 이번 계획(`salon-sales-solo`)은 `skeleton: off`이고, 작업 단위가 하나뿐이며 Units Generation 단계가 없습니다. `org.md`의 뼈대 체크포인트는 작업 단위 그래프(Unit DAG)가 있을 때만 걸리므로, AI-DLC가 자동으로 뼈대 확인 지점을 만들지 않습니다.
- 그래서 이렇게 조정했습니다: Code Generation 계획의 첫 단계를 이 뼈대(`127.0.0.1` 기동, H2 연결, Spring Security/JWT 로그인, 상품 1건 조회 화면)로 잡고, 뼈대가 `./gradlew build`를 통과하면 사용자가 확인할 때까지 멈춥니다. 사용자가 확인한 뒤에 나머지 기능을 만듭니다. 이 멈춤은 AI-DLC의 자동 체크포인트가 아니라 Code Generation 계획 안에 들어가는 단계이므로, Code Generation 계획을 승인할 때 이 단계가 들어 있는지 확인해야 합니다.

## 남은 불확실성

- 커밋 메시지 형식(자유 형식 한국어 / Conventional Commits)은 인터뷰에서 묻지 않았습니다. 정해지지 않았으며, 커밋 여부를 물을 때 사용자가 메시지를 고치거나 정할 수 있습니다.
- 테스트 도구 목록(JUnit 5, Spring Boot Test, MockMvc, spring-security-test, AssertJ, Mockito)은 README의 JUnit과 Spring Boot 스택에서 리드가 고른 것이며 인터뷰로 확인하지 않았습니다.
- 커밋/푸시 여부를 묻는 "알맞은 시점"의 기준은 사용자가 정하지 않았습니다. `team-practices.md`에는 예시로 "기능 하나가 끝나고 `./gradlew build`가 통과했을 때"를 적었습니다.

## 다음 단계로 넘길 확인 사항 (이 단계에서 결정하지 않음)

- Code Generation: 계획 첫 단계로 뼈대(로그인 → 상품 1건 조회)를 넣고, 확인을 위해 멈추는 지점을 계획에 명시합니다.
- Code Generation: README의 "Spring Boot LTS"와 Java 25 조합에서 Spring Boot·Gradle 버전이 Java 25를 지원하는지 확인합니다.
- Domain Design/Code Generation: 서버가 화면을 그리는 구조에서 JWT를 어디에 담을지(쿠키 등)와 Spring Security 설정을 정합니다.
- Code Generation: `.gitignore`에 `.gradle/`과 H2 DB 파일을 추가합니다.
- Code Generation 이후 모든 단계: 커밋·푸시는 사용자 동의를 받은 뒤에만 실행합니다(`discovered-rules.md` `## Mandated`).
