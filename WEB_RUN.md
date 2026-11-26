# J4 CINEMA 웹 시연판
JDK 17 이상 필요. 외부 라이브러리 없음.

## 실행
맥 터미널에서 프로젝트 폴더로 이동 후 `bash run.sh` 실행. 브라우저에서 http://localhost:8080 접속. 포트 충돌 시 `PORT=8081 bash run.sh` 실행 후 localhost:8081 접속.
IntelliJ에서는 src/main/java를 Sources Root로 지정하고 com.cinema.app.CinemaApplication의 main 실행.

## 시연
회원가입(전화번호 010-1234-5678, 비밀번호 4자 이상), 발급 ID로 로그인, 영화 시간 선택, 좌석 선택, 티켓 및 학생 할인 선택, 가상 결제, 마이페이지에서 취소. 관리자 화면 기본 암호 admin1234 (로컬 시연 전용, 환경변수 CINEMA_ADMIN_PASSWORD로 변경 가능).

## 변경 사항
기존 도메인 클래스 유지. 빈 PaymentService 구현, ReservationService와 CinemaApplication 추가. Theater의 문자열 좌석 ID를 예약 시 Seat 객체로 변환. 서버는 로컬 접속만 허용. 메모리 저장, 재시작 시 초기화. 취소 시 좌석은 해제되나 적립 포인트 및 누적 결제액은 환원되지 않음. 실제 결제, 영구 저장, 운영용 보안, 상영관 CRUD 웹 화면은 미구현. 티켓 종류와 할인은 한 예약 내 동일 적용. 영화 포스터는 CSS 디자인.

## 검증
JDK 21에서 --release 17 컴파일 및 HTTP 요청 테스트: 홈, 가입, 로그인, 좌석 조회, 학생 할인 2석 27000원, 중복 예약 거절, 내역, 취소, 좌석 재예약, 청소년 요금 12000원, 관리자 영화 등록, 잘못된 관리자 암호 거절. 브라우저 시각 검증 및 IntelliJ 실기기 실행은 미실시.

## GitHub
최신 develop에서 feature/web-final 브랜치 생성. 기존 파일을 보존하고 src/main/java/com/cinema/app/CinemaApplication.java, reservation/ReservationService.java, pricing/PaymentService.java, run.sh, WEB_RUN.md를 반영. PR 대상 develop. 팀원 검토 후 병합.
