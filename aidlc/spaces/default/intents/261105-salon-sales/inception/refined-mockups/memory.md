<!-- INVARIANT: examples are single-line HTML comments so a fresh template parses to total=0 (MEMORY_EMPTY). Do NOT un-comment or split across lines. t100 guards this. -->
> This file is kept up to date automatically while the stage runs. Add observations at the review step, not by editing here directly.

## Interpretations
- 2026-11-05T06:57:42Z — Rough Mockups와 User Stories 산출물이 없음; 이 계획(express 기반)에는 두 단계가 없으므로, 단계 파일 지시대로 없는 산출물의 내용을 지어내지 않고 requirements.md와 team-practices.md에서 바로 화면을 설계한다. 요구사항에서 이미 정한 규칙(권한, 확인창, 20건 페이지 등)은 질문하지 않았다.
<!-- example: 2026-05-29T10:14:32Z — chose REST over GraphQL; the consuming team only needs CRUD, revisit if subscriptions land -->

## Deviations
- 2026-11-05T07:21:58Z — 할부 입금 처리가 승인된 요구사항(FR7.1, FR7.2, FR7.3, FR7.7)과 달라짐; 사용자가 Q9("프로토타입이므로 금액을 별도 수정하기보단 버튼으로 입금확인처리")와 Q10 A로 회차 자동 균등 분배 + 회차별 입금/입금 취소 버튼을 정했다. requirements.md는 고치지 않고, mockups.md의 "요구사항과 달라진 점" 절에 기록해 Domain Design이 따르게 한다.
<!-- example: 2026-05-29T10:14:32Z — skipped the optional caching layer the stage prose suggested; the dataset is small enough that it adds risk -->

## Tradeoffs
- 2026-11-05T07:21:58Z — 회차별 입금 버튼을 모든 줄에서 켜 두는 안과, 가장 앞의 미입금 회차만 켜는 안 중 뒤의 것을 골랐다; FR7.5(입금은 앞 회차부터 채움)와 연체 계산이 어긋나지 않게 하기 위해서다. 가정으로 표시해 게이트에서 바꿀 수 있게 했다.
<!-- example: 2026-05-29T10:14:32Z — picked TDD over BDD this run; the team is unit-first and the domain is well-understood -->

## Open questions
<!-- example: 2026-05-29T10:14:32Z — confirm the retention window with compliance before the next stage hardens the schema -->
