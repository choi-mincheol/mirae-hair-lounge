# mirae-hair-lounge project
 - 미용실을 대상으로 제품 판매 관리를 하는 개인용 프로젝트
 - 결제 부분은 만들지 않음
 - 상품 관리 / 판매 등록 / 판매 실적 조회 등의 기능을 개발하고, 더 필요한 기능이나 만들어 볼법한 기능은 AI 가 제안한다.

## front end(서버를 따로 두지 않는다. 백엔드 앱이 화면을 그린다.)
 - Thymeleaf, jquery, javascript 
## backend
 - java 25, jUnit, spring boot lts, Mybatis, JWT, spring Security, gradle

## DB
 - H2
 - 개인용 프로젝트 이므로 H2 를 우선으로 사용하며, 추 후 MariaDB 등의 설치 등이 진행되고, 사용 가능 하게 되면 변경 진행 할 예정

## Mandated

 - ALWAYS 서버는 `127.0.0.1`에만 바인드한다.

 - ALWAYS 빌드 게이트는 `./gradlew build` 하나로 유지하고, 새 테스트도 이 빌드 안에서 돌게 한다.

## 다른 PC에서 AI-DLC 작업 이어가기

저장소를 받고 Kiro IDE에서 **aidlc** 에이전트를 고른 뒤, 채팅창에 아래를 순서대로 입력한다.

1. `/aidlc intent 261105-salon-sales` : 이번 작업(미용실 판매 관리)을 현재 작업으로 고른다.
2. `/aidlc` : 마지막으로 멈춘 단계부터 이어서 진행한다.

 - 현재 작업을 가리키는 파일(`aidlc/spaces/*/intents/active-intent`)은 PC마다 따로 두도록 `.gitignore`에 들어 있어 저장소에 올라가지 않는다. 그래서 새 PC에서는 작업을 한 번 직접 골라야 한다.
 - 작업 기록과 결정 기록(`aidlc/` 폴더)은 저장소에 올라가 있으므로 그대로 이어진다.
