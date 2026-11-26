package com.cinema.reservation;

/**
 * 예약의 상태. 실제 티켓팅처럼 "좌석을 먼저 잡고, 결제로 확정"하는 흐름이다.
 *
 *   PENDING --결제 성공--> RESERVED
 *   PENDING --결제 실패--> CANCELLED
 *   RESERVED --취소-----> CANCELLED
 *
 * 취소는 예약을 삭제하는 대신 상태를 CANCELLED로 바꾸는 방식으로 처리한다.
 */
public enum ReservationStatus {
    PENDING,    // 좌석을 잡아 두고 결제를 기다리는 상태 (좌석을 점유함)
    RESERVED,   // 결제까지 끝나 확정된 예약 (좌석을 점유함)
    CANCELLED   // 취소되었거나 결제에 실패한 예약 (좌석을 점유하지 않음)
}
