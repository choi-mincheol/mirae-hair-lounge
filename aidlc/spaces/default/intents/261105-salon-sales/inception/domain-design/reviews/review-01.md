## Review

**Verdict:** READY
**Reviewer:** aidlc-architecture-reviewer-agent
**Date:** 2026-11-09T02:24:31Z
**Iteration:** 1
**Verdict Dictated:** no

### Findings

| ID | Severity | Location | Finding | Required action | Status |
|---|---|---|---|---|---|
| R-01 | Major | aidlc/spaces/default/intents/261105-salon-sales/inception/domain-design/decisions.md > ADR-002 Decision (UserAccountLookup, ClientSalesSummary) | 두 포트로 코드 의존은 한 방향이 되지만, 실행 중에는 Common의 JWT 필터가 Auth를 부르고 Client 화면이 Sale을 부릅니다. 어느 클래스가 포트를 구현하는지 정하지 않았습니다. 흔한 방식대로 Auth의 서비스(BCrypt `PasswordEncoder`와 Common의 JWT 발급기를 쓰는 클래스)가 `UserAccountLookup`을 구현하면 보안 설정, JWT 필터, Auth 서비스, `PasswordEncoder`가 서로를 주입받는 고리가 생깁니다. Sale의 판매 서비스가 `ClientSalesSummary`를 구현하면서 `ClientService.requireActive`를 주입받고 Client 쪽이 이 포트를 주입받아도 같은 고리가 생깁니다. 최근 Spring Boot는 이런 순환 주입이 있으면 기본 설정에서 시작하지 않으므로, 뼈대 단계에서 앱이 뜨지 않을 수 있습니다. | ADR-002에 포트 구현을 별도의 작은 클래스로 둔다고 적어 주세요. 예를 들어 Auth에는 사용자 mapper만 쓰는 `UserAccountLookup` 구현을, Sale에는 판매 mapper만 쓰는 `ClientSalesSummary` 구현을 두고, 이 클래스들은 다른 컴포넌트의 서비스나 보안 설정을 주입받지 않게 합니다. `PasswordEncoder`는 JWT 필터와 다른 설정 클래스에 둔다는 점도 함께 적어 주세요. | New |
| R-02 | Minor | aidlc/spaces/default/intents/261105-salon-sales/inception/domain-design/decisions.md > ADR-002 Consequences와 ADR-003 Decision | ADR-002는 미수금·연체 계산이 Sale 한 곳에만 있어야 판매 목록, 거래처 목록, 대시보드가 같은 값을 보여준다고 합니다. 그런데 ADR-003은 Report가 다른 컴포넌트의 테이블을 JOIN해 직접 집계해도 된다고 허용하고, Report의 depends_on에는 "판매·품목·회차·요청을 읽기 전용으로 집계·조회"라고만 적혀 있습니다. 대시보드의 연체 판매(FR9.1)를 Report가 자기 SQL로 계산하면 연체 규칙이 두 곳에 생깁니다. | 대시보드의 연체 판매와 미수금은 Sale이 제공하는 조회(서비스나 같은 쿼리)를 Report가 불러서 쓴다고 ADR-003과 Report의 depends_on에 적어 주세요. Report가 직접 JOIN하는 범위는 실적 집계와 이름 표시로 한정합니다. | New |
| R-03 | Minor | aidlc/spaces/default/intents/261105-salon-sales/inception/domain-design/components.md > Sale behaviour와 ClientSalesSummary 포트 | FR7.6은 연체된 판매마다 연체 금액과 가장 오래된 연체 예정일을 보여주라고 하고, 화면 설계의 대시보드도 "연체 120,000원, 가장 오래된 예정일 10/05"를 보여줍니다. Sale의 동작 설명과 ClientSalesSummary 포트 설명은 연체 여부만 말하고 이 두 값은 말하지 않습니다. traceability.json은 FR7.6을 Sale에 연결해 두었습니다. | Sale의 동작에 연체 금액(오늘보다 앞선 예정일의 미입금 회차 금액 합계)과 가장 오래된 연체 예정일(그 회차 중 가장 이른 예정일)을 계산한다고 적고, 판매 목록, 거래처 목록(포트), 대시보드가 이 두 값을 함께 받는다고 적어 주세요. | New |
| R-04 | Minor | aidlc/spaces/default/intents/261105-salon-sales/inception/domain-design/components.md > Client behaviour (ClientService.requireActive)와 Sale depends_on Product | Client 설명에는 "판매 등록·수정 때 Sale이 거래처가 있고 거래 중인지 확인"한다고 되어 있습니다. FR4.4는 거래 중지된 거래처를 새 판매에서만 고를 수 없게 합니다. 이대로 만들면 거래 중지된 거래처의 지난 판매는 메모나 수량을 고치는 것도, 직원 요청을 승인하는 것도 막힙니다. 판매 중지된 옵션(FR2.5)이 들어 있는 지난 판매를 고칠 때 Product의 "판매 가능 여부" 검사를 하는지도 정해져 있지 않습니다. | 수정과 승인 때는 거래처를 바꾸거나 새 옵션 줄을 넣을 때만 거래 중·판매 중 검사를 하고, 이미 있는 거래처와 옵션 줄은 그대로 둘 수 있다고 Client와 Sale 설명에 적어 주세요. 다르게 하려면 그 규칙을 적어 주세요. | New |
| R-05 | Minor | aidlc/spaces/default/intents/261105-salon-sales/inception/domain-design/components.md > Sale behaviour (대기 요청 하나, requestToken), decisions.md > ADR-010 | "한 판매에 대기 요청은 하나뿐"을 무엇으로 지키는지 적혀 있지 않습니다. 요청은 판매를 바꾸지 않으므로 version 검사가 걸리지 않고, 직원 두 명이 동시에 요청하면 둘 다 들어갈 수 있습니다. 관리자가 대기 요청이 있는 판매를 직접 고치거나 취소했을 때 그 요청을 어떻게 할지도 정해져 있지 않습니다. 같은 requestToken으로 두 요청이 동시에 오면 두 번째 요청은 유일 값 위반으로 실패하는데, 그때 오류 화면 대신 이미 만든 판매로 보낸다는 처리도 적혀 있지 않습니다. | 대기 요청이 하나뿐이도록 막는 방법을 정해 주세요(예: 요청을 넣을 때 판매 version을 올리기, 또는 대기 요청 표시 칸에 유일 제약 두기). 관리자가 직접 고치거나 취소한 판매의 대기 요청을 어떻게 할지(예: 승인 때 다시 계산해 거부, 또는 자동으로 닫기) 정해 주세요. requestToken 유일 값 위반이 나면 기존 판매로 보낸다고 ADR-010에 적어 주세요. | New |
| R-06 | Minor | aidlc/spaces/default/intents/261105-salon-sales/inception/domain-design/decisions.md > ADR-006 Decision ("로그인하면 원래 화면으로 돌아갑니다") | 서버 세션을 두지 않는 JWT 방식인데, 만료 후 다시 로그인하면 원래 화면으로 돌아간다고 했습니다. Spring Security가 기본으로 원래 주소를 기억하는 곳은 서버 세션이므로, 세션이 없으면 이 동작이 되지 않습니다. 원래 주소를 요청 값으로 넘기면 외부 주소로 보내는 열린 리디렉트가 될 수 있는데, 이 보안 영향도 적혀 있지 않습니다. | 원래 주소를 어디에 기억하는지 정해 주세요(예: 쿠키에 저장하는 방식이나 `redirect` 요청 값). 돌아갈 주소는 이 앱 안의 상대 경로만 받는다고 ADR-006의 보안 영향에 적어 주세요. | New |

### Validation Tool Results

| Tool | Result | Interpretation |
|---|---|---|
| required-sections (검토자가 직접 확인) | PASS | components.md에 yaml 목록, 컴포넌트 그림(mermaid와 글 설명), 컴포넌트 요약, 엔티티 소유, 외부 의존, 나눈 이유와 거절한 나누기가 모두 있습니다. decisions.md의 ADR-001~ADR-010은 모두 Context, Decision, Consequences, Alternatives Rejected와 보안 영향을 갖추었습니다(ADR-008은 보안 영향 "없음"과 이유). |
| upstream-coverage (검토자가 직접 확인) | PASS | 요구사항, 작업 방식(기능별 패키지와 `common`, `127.0.0.1`, 환경변수 비밀 값, H2에서 MariaDB 전환 대비), 화면 설계 1절의 할부·입금 변경(균등 분배만, 회차 전체 입금, 앞 회차 입금과 뒤 회차 입금 취소, 금액 수정 시 재분배와 거부 문구, 입금이 있으면 개월 수·결제 수단 변경 불가, FR7.7 삭제)을 모두 반영했습니다. 연체 금액과 가장 오래된 예정일(FR7.6)만 빠졌습니다(위 Minor). |
| traceability (검토자가 직접 확인) | PASS | upstream_ids 54개가 requirements.md의 FR1.1~FR10.4와 정확히 같고, 모든 행이 OK이며 대상은 yaml에 선언된 컴포넌트나 엔티티입니다. FR7.7은 화면 설계 1절에서 없앤 이유를 적은 N/A입니다. |
| yaml 목록 형식 검사 (검토자가 직접 확인) | PASS | 컴포넌트 이름 6개가 모두 다르고, depends_on과 dependents가 모두 짝이 맞으며, 자기 자신을 부르는 곳이 없습니다. 엔티티 12개는 모두 한 컴포넌트가 소유하고 식별자 `id`가 있으며, 모든 references.entity가 owned_by 컴포넌트 아래에 선언되어 있습니다. 의존 그래프(Report → Sale → Product·Client → Auth → Common)에 순환이 없습니다. 실행 중 역방향 호출 두 곳은 ADR-002에서 포트로 뒤집었다고 밝혔습니다. 엔진 센서 명령은 브리프대로 따로 실행하지 않았습니다. |

### Summary

컴포넌트 경계, 엔티티 소유, 화면 설계 1절의 할부·입금 변경 반영, ADR 구성은 개발자가 바로 따라 만들 수 있을 만큼 갖춰져 있습니다. 승인 전에 살펴볼 것은 포트 구현이 Spring 순환 주입을 만들어 뼈대 단계에서 앱이 시작되지 않을 위험(Major) 하나이고, 나머지는 연체 규칙 위치, 수정 때 검사 범위, 동시 요청 처리, 로그인 후 돌아가기 같은 작은 보완입니다.
