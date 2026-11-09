<!-- INVARIANT: examples are single-line HTML comments so a fresh template parses to total=0 (MEMORY_EMPTY). Do NOT un-comment or split across lines. t100 guards this. -->
> This file is kept up to date automatically while the stage runs. Add observations at the review step, not by editing here directly.

## Interpretations
<!-- example: 2026-05-29T10:14:32Z — chose REST over GraphQL; the consuming team only needs CRUD, revisit if subscriptions land -->

## Deviations
- 2026-11-09T04:48:15Z — 뼈대(Step 1~9) 빌드 중 계획과 달라진 점: jQuery는 화면 설계(design-system-mapping.md)대로 3.7.1을 썼다(Maven Central 최신은 4.0.0). H2 2.4.240에서 role CHECK 제약이 세션을 닫는 문제가 있어 제약을 빼고 Java enum으로 제한했다. 로그인 후 404를 피하려고 Step 19 전까지 임시 대시보드를 두었다. 테스트 의존성 두 개(webmvc-test, mybatis test)를 더했다.
<!-- example: 2026-05-29T10:14:32Z — skipped the optional caching layer the stage prose suggested; the dataset is small enough that it adds risk -->

## Tradeoffs
<!-- example: 2026-05-29T10:14:32Z — picked TDD over BDD this run; the team is unit-first and the domain is well-understood -->

## Open questions
<!-- example: 2026-05-29T10:14:32Z — confirm the retention window with compliance before the next stage hardens the schema -->
