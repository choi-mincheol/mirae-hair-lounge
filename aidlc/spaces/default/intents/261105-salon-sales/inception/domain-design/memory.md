<!-- INVARIANT: examples are single-line HTML comments so a fresh template parses to total=0 (MEMORY_EMPTY). Do NOT un-comment or split across lines. t100 guards this. -->
> This file is kept up to date automatically while the stage runs. Add observations at the review step, not by editing here directly.

## Interpretations
- 2026-11-09T02:20:03Z — 사용자가 Q1~Q8을 모두 맡겨서 추천안을 골랐다(Q1 B, 나머지 A); 1인·로컬 앱이라 패키지는 중간 크기로 두고, 재고·보안·만료는 요구사항을 그대로 지키는 쪽을 택했다. 각 선택은 decisions.md의 ADR에 대안과 함께 남긴다.
<!-- example: 2026-05-29T10:14:32Z — chose REST over GraphQL; the consuming team only needs CRUD, revisit if subscriptions land -->

## Deviations
<!-- example: 2026-05-29T10:14:32Z — skipped the optional caching layer the stage prose suggested; the dataset is small enough that it adds risk -->

## Tradeoffs
- 2026-11-09T02:20:03Z — 순환 의존 피하기: Sale이 Client·Product를 부르고, Client 화면의 미수금은 Client가 정의한 조회 포트를 Sale이 구현해 채운다. Common의 JWT 필터도 Auth를 직접 부르지 않고 Common이 정의한 계정 조회 포트를 Auth가 구현한다. 의존 방향은 Report → Sale → (Client, Product) → Auth → Common 한 방향.
<!-- example: 2026-05-29T10:14:32Z — picked TDD over BDD this run; the team is unit-first and the domain is well-understood -->

## Open questions
<!-- example: 2026-05-29T10:14:32Z — confirm the retention window with compliance before the next stage hardens the schema -->
