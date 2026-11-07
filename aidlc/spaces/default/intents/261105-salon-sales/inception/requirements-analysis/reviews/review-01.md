## Review

**Verdict:** READY
**Reviewer:** aidlc-product-lead-agent
**Date:** 2026-11-05T06:29:13Z
**Iteration:** 1
**Verdict Dictated:** no

### Findings

| ID | Severity | Location | Finding | Required action | Status |
|---|---|---|---|---|---|
| R-01 | Major | aidlc/spaces/default/intents/261105-salon-sales/inception/requirements-analysis/requirements.md > FR6.1, FR7.1, FR7.2 | 관리자는 FR6.1에 따라 판매의 품목·수량·할인·결제 수단을 바로 고칠 수 있지만, 외상·할부 판매에서 판매 금액이 바뀌면 FR7.1의 "회차 합계 = 판매 금액" 규칙이 깨집니다. 판매 금액을 이미 받은 입금 합계보다 낮게 고치는 경우, 결제 수단을 외상·할부에서 현금으로(또는 그 반대로) 바꾸는 경우에 저장을 거부할지, 회차를 다시 맞추게 할지 정해져 있지 않습니다. 8절은 "회차 재계산 방식"만 다음 단계로 넘겼고, 저장을 허용할지 거부할지는 요구사항에 없어 QA가 판정할 수 없습니다. 직원 수정 요청(FR6.4) 승인도 같은 문제를 안고 있습니다. | FR6.1(또는 FR7)에 외상·할부 판매를 수정할 때의 규칙을 적어 주세요. 예: 금액이 바뀌면 회차 합계가 새 판매 금액과 같아질 때까지 저장을 거부한다, 새 판매 금액이 입금 합계보다 작으면 거부한다, 입금이 있는 판매는 결제 수단을 바꿀 수 없다. 각 규칙에 Given/When/Then 기준을 하나씩 붙여 주세요. 화면 배치와 자동 재계산 방식만 Refined Mockups·Domain Design으로 넘기면 됩니다. | New |
| R-02 | Major | aidlc/spaces/default/intents/261105-salon-sales/inception/requirements-analysis/requirements.md > FR3.1, FR5.5, FR8.4 | 이익(Q5 D) 계산에 쓰는 매입가의 출처가 둘입니다. FR3.1의 입고는 옵션 매입가와 다른 "매입 단가"를 받을 수 있지만, FR5.5는 판매 당시 "매입가"를 저장한다고만 하고 그것이 옵션에 등록된 매입가인지 실제 입고 단가인지 말하지 않습니다. 입고 단가가 옵션 매입가를 바꾸는지도 정해져 있지 않습니다. 그래서 같은 판매에 대해 이익이 두 가지로 나올 수 있고, FR8.4를 테스트할 기대값을 정할 수 없습니다. | FR5.5에 판매 품목에 저장하는 매입가가 무엇인지 하나로 정해 주세요. 예: "판매 시점의 옵션 매입가", "마지막 입고 단가", "이동평균 단가" 중 하나. 입고 단가가 옵션 매입가를 갱신하는지도 FR3.1에 적고, FR8.4에 숫자가 들어간 기준(예: 입고 단가 9,000원, 옵션 매입가 8,000원일 때 이익)을 하나 추가해 주세요. | New |
| R-03 | Minor | aidlc/spaces/default/intents/261105-salon-sales/inception/requirements-analysis/requirements.md > FR9.2, FR4.2, FR4.3, FR2.4 | FR9.2는 직원 대시보드에서 매출 합계를 숨기지만, 직원도 볼 수 있는 FR4.2·FR4.3은 거래처별 구매 합계와 미수금을 보여주고, FR2.4 상품 목록에 매입가(원가)가 직원에게도 보이는지는 적혀 있지 않습니다. Q7 C("상품·실적 관리는 관리자만")와 맞춰 직원에게 어떤 금액을 보여줄지 기준이 한 곳에 없어 구현자가 화면마다 따로 판단해야 합니다. | 2절 권한 표에 직원이 볼 수 있는 금액 항목(판매가, 매입가, 거래처 구매 합계, 미수금)을 명시하고, 매입가를 직원에게 숨긴다면 FR2.4·FR4.2·FR4.3에 그 조건을 기준으로 적어 주세요. | New |
| R-04 | Minor | aidlc/spaces/default/intents/261105-salon-sales/inception/requirements-analysis/requirements.md > NFR2, FR1.2 | NFR2는 새로 받은 저장소에서 "추가 설정 없이(환경변수 제외)" 빌드가 통과한다고 해서, 빌드에 환경변수가 필요할 수 있다고 읽힙니다. FR1.2는 관리자 환경변수가 없으면 앱 시작을 멈추므로, 테스트가 빈 메모리 H2로 앱을 띄우면 환경변수 없이 `./gradlew build`가 실패할 수 있습니다. 유일한 빌드 게이트(project.md Mandated)가 개발자 PC 설정에 따라 달라집니다. | NFR2에 "테스트는 테스트용 설정 값(관리자 계정, JWT 서명 키)을 쓰므로 `./gradlew build`는 환경변수 없이 통과한다"처럼 적고, FR1.2의 시작 실패는 실제 실행(`./gradlew bootRun`)에만 해당한다고 범위를 밝혀 주세요. | New |
| R-05 | Minor | aidlc/spaces/default/intents/261105-salon-sales/inception/requirements-analysis/requirements.md > FR1.3, FR1.6 | 비밀번호를 잊은 경우의 복구 방법이 없습니다. 관리자는 직원 계정을 만들고 사용 중지만 할 수 있어 직원 비밀번호를 다시 정해 줄 수 없고, 관리자 본인이 비밀번호를 잊으면 FR1.2는 관리자 계정이 이미 있으므로 동작하지 않습니다. | FR1.3에 관리자가 직원 비밀번호를 초기화하는 기능을 넣거나 범위 밖으로 적고, 관리자 비밀번호 분실 시 방법(예: 환경변수로 재설정)을 요구사항, 가정, 범위 밖 중 하나로 정해 주세요. | New |
| R-06 | Minor | aidlc/spaces/default/intents/261105-salon-sales/inception/requirements-analysis/requirements.md > FR6.3, FR6.4 | 직원 요청이 대기 중인 판매를 관리자가 직접 수정하거나 취소하면 그 요청이 어떻게 되는지 정해져 있지 않습니다. 관리자가 나중에 그 요청을 승인하면 바뀐 판매 위에 직원이 보던 옛 내용이 덮어써질 수 있습니다. | 대기 요청이 있는 판매를 관리자가 직접 바꾸면 요청을 자동으로 닫을지(직원에게 사유 표시), 직접 변경을 막을지 정하고 Given/When/Then 기준을 하나 추가해 주세요. | New |

### Summary

요구사항은 Q1~Q14 답을 빠짐없이 반영했고, 대부분의 규칙에 확인 가능한 기준이 있으며 traceability.json의 ID 목록도 requirements.md와 일치합니다. 승인 전에 따져 볼 핵심은 두 가지입니다. 외상·할부 판매를 수정할 때의 금액·회차 규칙과, 이익 계산에 쓰는 매입가의 출처가 정해져 있지 않습니다.
