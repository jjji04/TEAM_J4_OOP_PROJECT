package com.cinema.reservation;

/**
 * 예약의 상태.
 * 취소는 예약을 삭제하는 대신 상태를 CANCELLED로 바꾸는 방식으로 처리한다.
 */
public enum ReservationStatus {
    RESERVED,
    CANCELLED
}
