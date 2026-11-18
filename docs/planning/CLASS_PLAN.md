# 클래스 후보 및 책임 (구현 전 설계안)

## A 영화/상영
- `Movie`, `Screening`, `Theater`, `MovieManager`

## B 좌석/예약
- `Seat`, `Reservation`, `SeatReservation`, `SeatManager`, `ReservationManager`

## C 티켓/요금
- `Ticket`, `PricingPolicy`, `RegularPricingPolicy`, `DiscountPolicy`, `PaymentService`

## D 회원/데이터
- `Member`, `MemberManager`, `MembershipGrade`, `DataManager`

## 공동 검토
- `ReservationService`, `PriceService`, `ReservationRepository` 등은 UML과 팀 협의 후 확정
- `Screening — Theater`, `Theater — Seat`, `Reservation — SeatReservation` 관계의 실제 코드 표현 확정
- SOLID: SRP(책임 분리), OCP/DIP(할인 정책·저장소 인터페이스) 우선 검토

**위 이름은 구현 확정 목록이 아니며 Java 파일은 아직 생성하지 않았습니다.**
