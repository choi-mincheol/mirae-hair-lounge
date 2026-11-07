# 디자인 시스템 연결: 미용 제품 판매 관리

> 근거: Q8 답 "1+3"(Bootstrap 5를 WebJars로 포함 + 직접 만든 CSS 하나), `inception/practices-discovery/team-practices.md`(npm 같은 프런트엔드 빌드 도구 없음, 화면별 JavaScript는 정적 파일), `inception/requirements-analysis/requirements.md`(FR10.1 Thymeleaf·jQuery, NFR12 최신 Chrome·Edge). 화면은 `mockups.md`, 컴포넌트는 `interaction-spec.md`.

## 1. 도구와 포함 방식

| 도구 | 포함 방식 | 쓰는 곳 |
|---|---|---|
| Bootstrap 5.3 (CSS + JS 번들) | Gradle 의존성 `org.webjars:bootstrap` | 화면 틀, 표, 폼, 버튼, 배지, 창(Modal), 밀려 나오는 메뉴(Offcanvas), 접기(Collapse), 알림(Alert) |
| jQuery 3.7 | Gradle 의존성 `org.webjars:jquery` | 화면별 동작, `$.ajax` 요청 [FR10.1] |
| WebJars 경로 도우미 | Gradle 의존성 `org.webjars:webjars-locator-lite` | 버전 없는 경로(`/webjars/bootstrap/css/bootstrap.min.css`) 사용 |
| 이 앱의 CSS | `src/main/resources/static/css/app.css` 한 파일 | Bootstrap에 없는 모양(아래 4절) |
| 화면별 JavaScript | `src/main/resources/static/js/<화면>.js` | 예: `sale-form.js`, `product-picker.js`, `installment.js`, `confirm-dialog.js` |

- 모두 앱 안에 들어가므로 인터넷이 없어도 화면이 그대로 나온다(Q8에서 CDN 대신 고른 이유). 외부 글꼴과 아이콘 CDN도 쓰지 않는다. 글꼴은 시스템 기본 글꼴(Bootstrap 기본 글꼴 목록 + "Malgun Gothic", "Apple SD Gothic Neo")을 쓴다. [Q8] [assumption]
- 정확한 버전 번호와 Spring Boot·Java 25와의 조합은 Code Generation 계획에서 확정한다. [assumption]
- 아이콘은 쓰지 않고 글자 버튼으로 한다(아이콘 글꼴을 따로 받지 않기 위해). [assumption]

## 2. 화면 요소와 Bootstrap 연결

| 화면 요소 (`interaction-spec.md`) | Bootstrap | 직접 만든 CSS |
|---|---|---|
| AppShell 왼쪽 메뉴 | `nav`, `nav-link`, 991px 이하에서 `offcanvas-lg` | `.app-sidebar`(폭 220px, 현재 메뉴 왼쪽 막대) |
| 위쪽 줄 | `navbar` | - |
| DataList 표 | `table`, `table-hover`, `table-responsive`(768~991px) | `.table-stack`(767px 이하 카드 모양), `.num`(금액 오른쪽 정렬) |
| 페이지 이동 | `pagination` | - |
| SearchFilterBar | `row g-2`, `form-control`, `form-select`, 휴대폰은 `collapse` | - |
| FormField | `form-label`, `form-control`, `is-invalid`, `invalid-feedback` | `.required`(라벨 뒤 `*`) |
| 버튼 | 주요 동작 `btn-primary`, 보조 `btn-outline-secondary`, 되돌리기 어려운 동작 `btn-danger` | 휴대폰에서 주요 버튼 `w-100` |
| StatusBadge | `badge` + `text-bg-warning`(재고 부족), `text-bg-danger`(연체), `text-bg-success`(완납·입금 완료), `text-bg-primary`(승인 대기), `text-bg-secondary`(취소·중지) | - |
| ConfirmDialog, ProductPickerModal | `modal`, 767px 이하 `modal-fullscreen-md-down` | - |
| FlashAlert | `alert alert-success/danger/info`, `alert-dismissible` | - |
| DiffView | `table` | `.diff-changed`(굵게, 연한 노란 배경) |
| 대시보드 숫자 카드 | `card`, `row row-cols-1 row-cols-md-3` | `.stat-value`(큰 숫자) |
| 판매 등록 아래 합계(휴대폰) | - | `.sticky-summary`(화면 아래 고정) |

## 3. 화면 크기 기준 [Q2]

Bootstrap 기본 기준을 그대로 쓴다.

| 이름 | 폭 | 이 앱에서 |
|---|---|---|
| md | 768px 이상 | 이 폭부터 표를 표로 보여준다. 그 아래는 카드 목록, 창은 화면 전체 |
| lg | 992px 이상 | 이 폭부터 왼쪽 메뉴가 항상 보인다 |
| xl | 1200px 이상 | 본문 최대 폭을 정하지 않고 넓게 쓴다(표가 많기 때문) [assumption] |

## 4. 직접 만든 CSS (`app.css`)

| 클래스 | 하는 일 |
|---|---|
| `.app-sidebar` | 왼쪽 메뉴의 폭, 배경(연한 회색), 현재 메뉴의 왼쪽 4px 막대와 굵은 글씨 |
| `.table-stack` | 767px 이하에서 `thead`를 숨기고 각 `tr`을 카드처럼 테두리·여백을 주며, 각 `td` 앞에 `data-label` 값을 "항목명:"으로 보여준다 |
| `.num` | 금액·수량 오른쪽 정렬, 숫자 폭 고정(`font-variant-numeric: tabular-nums`) |
| `.diff-changed` | 바뀐 줄 굵게 + 배경 `#fff3cd` |
| `.row-muted` | 취소·중지된 행 회색 글씨(`#6c757d`) |
| `.sticky-summary` | 767px 이하 판매 등록에서 합계와 [저장]을 화면 아래에 고정 |
| `.stat-value` | 대시보드 숫자 크게(1.75rem, 굵게) |
| `.required::after` | 필수 라벨 뒤 `*` |

색과 간격은 새로 정하지 않고 Bootstrap 기본값(주 색 `#0d6efd`, 간격 0.25·0.5·1·1.5·3rem)을 쓴다. 바꾸고 싶어지면 `app.css`에서 Bootstrap CSS 변수(`--bs-primary` 등)를 덮어쓴다. [assumption]

## 5. 화면 조각 (Thymeleaf fragment)

공통 틀과 반복 요소는 Thymeleaf 조각으로 둔다. 이름은 제안이며 Code Generation에서 확정한다. [assumption]

| 조각 | 내용 |
|---|---|
| `layout/app.html` | AppShell(위쪽 줄, 메뉴, 알림 영역, 본문 자리), CSS·JS 불러오기 |
| `fragments/pagination.html` | 페이지 이동 |
| `fragments/badge.html` | 상태 값 → StatusBadge |
| `fragments/confirm-dialog.html` | 공통 확인창 |
| `fragments/product-picker.html` | 품목 추가 창 |
| `fragments/installment.html` | 할부 일정(판매 상세에서 `$.ajax` 결과로 다시 그리는 단위) |
| `fragments/form-field.html` | 라벨 + 입력 칸 + 오류 글자 |

## 6. 문구 규칙

- 화면 글자는 한국어. 버튼은 동작 이름("저장", "판매 취소", "입금 처리")으로 쓰고 "확인"만 쓰지 않는다. [FR10.1]
- 금액은 `1,250,000원`, 날짜는 `2026-11-05`, 일시는 `2026-11-05 14:20`. [NFR7] [NFR8]
- 오류 문구는 무엇이 틀렸고 어떻게 고치는지 쓴다(예: "0 이상의 정수를 입력하세요"). 오류 코드는 보여주지 않는다.
