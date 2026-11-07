# Practices Discovery 질문

> 그린필드 프로젝트라 `aidlc/spaces/default/memory/org.md`의 기본값을 1인·로컬 전용 프로젝트에 맞게 고친 제안을 각 질문의 첫 선택지로 두었습니다. 답은 `[Answer]:` 뒤에 적어 주세요.

## Q1. [릴리스 엔지니어] 코드 변경 기록 방식

코드 변경을 저장소에 어떻게 남길까요? 리뷰어가 따로 없는 1인 프로젝트라 절차를 얼마나 둘지 정하는 질문입니다.

A. `main`에 바로 커밋 (제안)
B. 기능마다 짧은 브랜치를 만들고 `main`에 squash merge
C. 기능 브랜치를 만들고 일반 merge (브랜치 이력 유지)
D. GitHub에서 PR을 열어 스스로 확인한 뒤 merge
E. 커밋은 직접 하고, AI-DLC는 커밋하지 않고 변경만 남김
X. Other (please specify)

[Answer]: X. Other (please specify) - 작업 진행하면서 커밋/푸쉬 진행할건지 나에게 물어본다.

## Q2. [릴리스 엔지니어] 처음 만들 범위

기능을 본격적으로 만들기 전에, 처음부터 끝까지 이어지는 얇은 뼈대를 먼저 만들까요? 워킹 스켈레톤(walking skeleton)은 화면, 서버, DB가 한 번에 연결되어 돌아가는 최소 버전으로, 실제 기능을 넣기 전에 부품들이 제대로 이어지는지 확인하려고 먼저 만듭니다.

A. 아니요, 다만 코드 작성의 첫 작업을 "앱 기동 + H2 연결 + 첫 화면"으로 잡는다 (제안)
B. 아니요, 따로 뼈대 단계 없이 바로 기능을 만든다
C. 예, 로그인부터 상품 1건 조회까지 얇게 먼저 만들어 확인받은 뒤 나머지를 만든다
D. 예, 상품 등록부터 판매 등록, 실적 조회까지 한 줄로 먼저 이어 본다
X. Other (please specify)

[Answer]: C

## Q3. [릴리스 엔지니어] 앱 실행 방식

앱은 어떻게 실행할까요? 어느 방식이든 서버는 `127.0.0.1`에만 열립니다.

A. `./gradlew bootRun` (제안)
B. `./gradlew build`로 만든 jar를 `java -jar`로 실행
C. 개발 중에는 bootRun, 실제로 쓸 때는 jar
D. Windows 시작 시 자동으로 켜지도록 등록
X. Other (please specify)

[Answer]: A

## Q4. [릴리스 엔지니어] 데이터 보관

H2 데이터는 앱을 껐다 켜도 남아야 하나요? 판매 기록을 계속 쌓을지, 연습용으로 매번 비울지를 정합니다.

A. 예, 파일 모드로 저장하고 테스트는 메모리 DB 사용 (제안)
B. 예, 파일 모드 + 큰 변경 전 DB 파일 수동 복사
C. 예, 파일 모드 + 앱이 정기적으로 백업 파일 생성
D. 아니요, 메모리 모드로 켤 때마다 초기화 (연습용)
X. Other (please specify)

[Answer]: A

## Q5. [품질 엔지니어] 테스트 작성 순서

테스트는 언제 작성할까요? 코드 작성 단계에서 구현과 테스트의 순서를 정합니다.

A. 기능을 구현한 뒤 그 계층의 테스트 작성 (test-after, 제안)
B. 테스트를 먼저 쓰고 구현 (TDD)
C. Given/When/Then 시나리오를 먼저 쓰고 구현 (BDD)
D. 혼합: 판매 금액이나 실적 집계 같은 핵심 계산은 테스트 먼저, 나머지는 구현 후
X. Other (please specify)

[Answer]: B

## Q6. [품질 엔지니어] 테스트 커버리지 기준

테스트 커버리지 기준을 둘까요? 기준을 두면 `./gradlew build` 안에서 판정합니다.

A. 수치 기준 없음. 요구사항마다 테스트 1개 이상, 기존 테스트 항상 통과 (제안)
B. JaCoCo 리포트만 만들고 기준은 두지 않음
C. 라인 커버리지 80% 미만이면 `./gradlew build` 실패
D. 라인 커버리지 60% 미만이면 `./gradlew build` 실패
X. Other (please specify)

[Answer]: A

## Q7. [개발자] 코드 포맷

코드 포맷을 도구로 강제할까요? 지금 저장소에는 포매터나 린터 설정이 없습니다.

A. 도구 없이 Java 관례와 IDE 기본 포맷 (제안)
B. Spotless + google-java-format, `./gradlew build`에서 검사
C. Spotless + palantir-java-format, `./gradlew build`에서 검사
D. Checkstyle 경고만 표시 (빌드는 실패시키지 않음)
X. Other (please specify)

[Answer]: A

## Q8. [개발자] Java 패키지 구성

Java 패키지를 어떻게 나눌까요? 코드 폴더 구조의 기본 틀을 정합니다.

A. 기능별(`product`, `sale`, `report`, `auth`) 안에 controller/service/mapper (제안)
B. 계층별(`controller`, `service`, `mapper`, `domain`) 최상위 분리
C. 기능별로 나누고 공통 코드는 `common` 패키지로 분리
D. 정하지 않고 Domain Design 단계에서 결정
X. Other (please specify)

[Answer]: C
