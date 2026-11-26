# B 파트 설계: 좌석·예약 (`com.cinema.reservation`)

담당: B 정재 | 브랜치: `feature/reservation`

## 1. 한눈에 보기

좌석과 예약을 다룬다. 핵심 규칙은 두 가지다.

- **좌석에는 예약 여부를 두지 않는다.** 유효한 예약을 조회해서 "이 회차의 이 좌석이 비었는지" 계산한다.
- **취소는 삭제가 아니라 상태 변경이다.** 예약을 `CANCELLED`로 바꾸면 그 좌석은 자동으로 다시 예약 가능해진다.
- **좌석을 먼저 잡고 결제로 확정한다.** 실제 티켓팅처럼 예약은 결제 대기(`PENDING`)로 시작하고, 결제 성공이면 `RESERVED`, 실패면 `CANCELLED`가 된다. 결제 대기 중에도 좌석은 점유된다.

```
ReservationManager ──> SeatManager ──> Theater (A)
        │
        └──> Reservation ──> Member (D), Screening (A), Ticket (C)
                  │
                  ├──> Seat
                  └──> ReservationStatus
```

내 패키지가 A, C, D 클래스를 가져다 쓰고, 그쪽은 내 클래스를 모른다 (단방향 의존).

## 2. 클래스별 역할

| 클래스 | 종류 | 역할 |
|---|---|---|
| `Seat` | 값 객체 | 상영관의 좌석 하나 (`seatId`, `row`, `col`). 예약 여부는 갖지 않는다. `seatId` 기준으로 `equals`/`hashCode` |
| `ReservationStatus` | enum | 예약 상태. `PENDING`(결제 대기), `RESERVED`(확정), `CANCELLED`(취소·결제 실패) |
| `Reservation` | 엔티티 | 한 번의 예약 기록. 누가(Member), 어느 상영(Screening), 어떤 좌석(Seat)들, 어떤 티켓(Ticket)들, 총액, 예약 시각, 상태 |
| `SeatManager` | 관리 | `Theater`의 좌석 ID 문자열("A1")을 `Seat` 객체로 변환. 상영관별로 캐싱 |
| `ReservationManager` | 관리 | 예약 생성·취소·조회, 좌석 가용 여부 판단, 중복 예약 방지 |

### Seat
- 필드: `seatId`(예: "A1"), `row`, `col`
- 생성 시 검증: 빈 ID 금지, 행/열은 1 이상
- 불변 객체 (setter 없음)

### ReservationStatus
- `PENDING`: 좌석을 잡아 두고 결제를 기다리는 상태. 좌석을 점유한다.
- `RESERVED`: 결제까지 끝나 확정된 예약. 좌석을 점유한다.
- `CANCELLED`: 취소되었거나 결제에 실패한 예약. 기록은 남지만 좌석을 점유하지 않는다.

```
PENDING --결제 성공--> RESERVED
PENDING --결제 실패--> CANCELLED
RESERVED --취소------> CANCELLED
```

### Reservation
- 필드: `reservationId`, `member`, `screening`, `seats`, `tickets`, `totalPrice`, `reservedAt`, `status`
- 생성 시 검증
  - 회원/상영 null 금지
  - 좌석 1개 이상
  - 좌석 수 = 티켓 수
  - 총액 0 이상
- 좌석/티켓 리스트는 복사해서 보관하고, getter는 수정 불가 리스트로 반환 (캡슐화)
- 처음 만들어지면 `PENDING`
- 메서드
  - `confirm()`: `PENDING` → `RESERVED`. 결제 대기가 아니면 `IllegalStateException`
  - `cancel()`: 상태를 `CANCELLED`로 변경. 이미 취소됐으면 `IllegalStateException`
  - `isActive()`: 좌석을 점유 중인지 (`PENDING` 또는 `RESERVED`)
  - `isConfirmed()`: 결제까지 끝났는지 (`RESERVED`)

### SeatManager
- `getSeats(Theater)`: 상영관 전체 좌석 목록 (처음 요청 시 변환, 이후 재사용)
- `findSeat(Theater, seatId)`: 좌석 번호로 `Seat` 찾기. 대소문자·공백 무시. 상영관에 없으면 `IllegalArgumentException`
- 좌석 ID 형식은 영문 한 글자 + 숫자(`A1`, `B12`). 영문은 행, 숫자는 열로 변환
- 변환을 여기서 하는 이유: A의 `Theater`가 좌석을 문자열 ID로만 들고 있기 때문

### ReservationManager

| 메서드 | 설명 |
|---|---|
| `reserve(member, screening, seatIds, tickets, totalPrice)` | 예약 생성 (결제 대기 `PENDING`으로 저장) |
| `confirm(reservationId)` | 결제 성공: `PENDING` → `RESERVED` |
| `failPayment(reservationId)` | 결제 실패: `PENDING` 예약을 취소해 좌석 해제 (확정된 예약에는 불가) |
| `cancel(reservationId)` | 예약 취소 (`PENDING`, `RESERVED` 모두) |
| `findById(reservationId)` | 예약 ID로 조회 (없으면 null) |
| `isAvailable(screening, seatId)` | 그 회차에서 좌석이 비었는지 |
| `getAvailableSeats(screening)` | 비어 있는 좌석 목록 |
| `getReservedSeats(screening)` | 이미 예약된 좌석 목록 |
| `hasActiveReservation(screening)` | 유효한 예약이 하나라도 있는지 (상영 삭제 전 확인용) |
| `getReservationsByMember(member)` | 회원별 예약 목록 |
| `getAllReservations()` | 전체 예약 목록 (수정 불가) |

`reserve()` 처리 순서
1. 입력 검증 (회원/상영/좌석 목록)
2. 좌석 번호를 `Seat`으로 변환 (상영관에 없으면 예외, 같은 좌석 두 번 선택해도 예외)
3. 선택한 좌석이 전부 비었는지 확인 (하나라도 예약돼 있으면 `IllegalStateException`)
4. `Reservation`을 `PENDING`으로 생성 후 저장 (예약 ID는 1부터 자동 증가)
5. 결제는 C 파트(`PaymentService`)가 처리하고, 결과에 따라 `confirm()` 또는 `failPayment()`를 호출

3번까지 통과해야 저장하므로, 일부만 예약되는 일은 없다. 좌석 한 개마다 티켓 한 개가 대응하므로 성인·청소년 티켓을 섞어서 한 번에 예약할 수 있다.

## 3. 설계 결정

1. **좌석에 예약 여부를 두지 않는다.** 좌석은 상영관 소속이라 모든 회차가 공유한다. 회차별 상태는 예약 기록에서 계산해야 꼬이지 않는다.
2. **취소는 상태 변경.** 삭제하지 않아 기록이 남고, 좌석은 자동으로 다시 열린다.
3. **예약 후 결제.** 결제를 먼저 받으면 결제하는 사이 다른 사람이 같은 좌석을 가져가 환불해야 할 수 있다. 실제 예매처럼 좌석을 먼저 잡고(`PENDING`) 결제 성공 시 확정한다. 결제 실패는 별도 코드 없이 기존 취소 로직(`CANCELLED`)으로 좌석이 풀린다.
4. **전부 성공 또는 전부 실패.** 모든 검증을 끝낸 뒤에 저장한다.
5. **요금 계산과 결제는 하지 않는다.** C의 `PriceService`, `PaymentService` 책임이다. `reserve()`는 계산된 총액을 받고, 결제 결과는 `confirm()`/`failPayment()`로 전달받는다.
6. **예외 구분.** 잘못된 입력은 `IllegalArgumentException`, 상태 때문에 불가능한 경우(이미 예약된 좌석, 이미 취소된 예약)는 `IllegalStateException`.

## 4. 다른 파트와의 연결

| 파트 | 연결 지점 |
|---|---|
| A (movie) | `Screening`, `Theater`를 사용. `MovieService.removeScreening` 전에 `hasActiveReservation()`으로 예약 여부를 확인할 수 있음 |
| C (ticket) | `com.cinema.pricing.Ticket`을 사용. 총액은 `PriceService.calculatePrice()` 결과를 합산해서 `reserve()`에 전달. 결제 성공 시 `confirm()`, 실패 시 `failPayment()`를 호출 (`PaymentService`는 아직 구현 전) |
| D (member) | `Member`를 사용. 결제 후 `recordPayment()` 호출은 호출하는 쪽(서비스)에서 처리 |

## 5. 테스트

`src/test/java/com/cinema/reservation/ReservationManagerTest.java` (41건, JUnit 없이 `main`으로 실행)

- 좌석 예약, 남은/예약된 좌석 조회
- 같은 회차·같은 좌석 중복 예약 방지, 일부만 겹쳐도 전체 거부
- 다른 회차의 같은 좌석은 예약 가능
- 잘못된 입력 (없는 좌석, 같은 좌석 중복 선택, 좌석/티켓 수 불일치)
- 취소 후 상태, 재취소·없는 예약 취소 예외, 취소한 좌석 재예약
- 결제 흐름: 결제 대기 중 좌석 점유와 중복 거부, 결제 성공 시 확정, 결제 실패 시 좌석 해제와 재예약, 잘못된 상태 전이 거부
- 성인·청소년 티켓을 섞은 여러 좌석 동시 예약

실행 (A, C 브랜치가 합쳐진 상태에서):

```bash
javac -encoding UTF-8 -d out $(find src -name "*.java")
java -cp out com.cinema.reservation.ReservationManagerTest
```

## 6. 앞으로 할 일

- 저장: 예약이 메모리에만 있어서 종료하면 사라진다. D의 `DataManager`와 연동하거나 `ReservationRepository` 인터페이스를 따로 둘지 협의 필요
- 서비스: 예약, 결제, 포인트를 묶는 `ReservationService`는 팀 합의 후 결정. 결제 단계(`PaymentService`)가 구현되면 `reserve` → 결제 → `confirm`/`failPayment` 순서로 연결
- 결제 대기 타임아웃: 현재는 결제 대기 예약이 자동으로 풀리지 않는다. 필요하면 예약 시각 기준으로 N분 뒤 만료 처리 추가
- `SeatReservation`: 클래스 계획에 있었으나 `Reservation`이 좌석 목록을 직접 갖고 있어 만들지 않았다
- 좌석 ID 형식(영문 한 글자 + 숫자)과 `Theater`↔`Seat` 연동 방식은 A와 확인 필요
