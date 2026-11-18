# 영화관 좌석 예약 및 요금 관리 시스템

객체지향프로그래밍(B) 팀 프로젝트 | Java | VS Code + GitHub

> **현재 상태:** 프로젝트 폴더 및 협업 구조만 준비됨. Java 소스코드, 빌드 도구, UI 방식은 아직 결정하거나 작성하지 않았습니다.

## 팀별 담당

| 담당 | 브랜치 | 담당 패키지 | 주요 역할 |
|---|---|---|---|
| A 진영 | `feature/movie` | `movie` | 영화·상영회차·상영관 관리 |
| B 정재 | `feature/reservation` | `reservation` | 좌석·예약·취소 관리 |
| C 재민 | `feature/ticket` | `ticket` | 티켓·요금·할인·결제 시뮬레이션 |
| D 종현 | `feature/member` | `member` | 회원·로그인·포인트·저장 |

`app`, `common`, `service`, `repository`는 팀이 합의하여 공동으로 관리하는 공간입니다.

## 폴더 안내

- `src/main/java/com/cinema/`: 향후 Java 클래스가 들어갈 위치
- `src/main/resources/data/`: 향후 샘플 데이터·설정 파일 위치 (개인정보/비밀번호 업로드 금지)
- `src/test/java/com/cinema/`: 향후 테스트 코드 위치
- `docs/uml/`: 11주차 UML 원본 및 이미지
- `docs/planning/`: 설계 계획 및 협업 규칙
- `docs/testing/`: 테스트 시나리오 및 결과

## 시작 순서

1. 이 폴더를 VS Code에서 **파일 → 폴더 열기**로 연다.
2. 터미널에서 `git --version`으로 Git 설치를 확인한다.
3. GitHub 저장소가 **아직 없다면** 저장소를 만든다 (README 자동 생성은 피하는 것이 편리).
4. `git init -b main` → `git add .` → `git commit -m "chore: initialize project structure"`
5. `git remote add origin <GitHub 저장소 URL>` → `git push -u origin main`
6. `git switch -c develop` → `git push -u origin develop`
7. 각 팀원이 `develop`에서 자기 `feature/*` 브랜치를 만들어 작업한다.

**주의:** 위 명령은 새 저장소용입니다. 이미 연결된 Git 저장소라면 `git init` 또는 `git remote add`를 반복하지 마세요.

## 개발 전 결정할 사항

- 콘솔 프로그램 vs GUI
- JDK 버전과 빌드 방식 (일반 Java / Maven / Gradle)
- 클래스명·메서드·DTO·예외·저장 방식
- 공용 인터페이스와 팀원 간 호출 규칙

기존 UML은 **설계안**으로 보관하며, 실제 코드와의 일치 여부는 구현 후 검토합니다.
