package com.cinema.reservation;

import com.cinema.member.Member;
import com.cinema.movie.Screening;
import com.cinema.pricing.Ticket;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 예약 생성·취소·조회와 좌석 가용 여부 판단을 맡는다.
 * 좌석 예약 여부는 Seat에 두지 않고, 유효한(PENDING 또는 RESERVED) 예약들을 조회해서 계산한다.
 * 그래서 예약을 취소하면(CANCELLED) 그 좌석은 자동으로 다시 예약 가능해진다.
 * 요금 계산은 C(PriceService) 책임이라 총 금액은 호출하는 쪽에서 계산해서 넘긴다.
 */
public class ReservationManager {
    private final SeatManager seatManager;
    private final List<Reservation> reservations = new ArrayList<>();
    private int nextReservationId = 1;

    public ReservationManager(SeatManager seatManager) {
        if (seatManager == null) {
            throw new IllegalArgumentException("SeatManager가 필요합니다.");
        }
        this.seatManager = seatManager;
    }

    /**
     * 예약 생성. 좌석을 먼저 잡고 결제 대기(PENDING) 상태로 저장한다.
     * 결제가 끝나면 confirm(), 결제에 실패하면 failPayment()를 호출한다.
     * @param seatIds 예약할 좌석 번호들 (예: "A1", "A2"). tickets와 같은 순서·같은 개수
     * @throws IllegalArgumentException 상영관에 없는 좌석, 좌석 중복 입력, 인자 오류
     * @throws IllegalStateException    이미 예약된 좌석이 포함된 경우 (아무것도 저장하지 않음)
     */
    public Reservation reserve(Member member, Screening screening, List<String> seatIds,
                               List<Ticket> tickets, int totalPrice) {
        if (member == null || screening == null) {
            throw new IllegalArgumentException("회원과 상영 정보는 필수입니다.");
        }
        if (seatIds == null || seatIds.isEmpty()) {
            throw new IllegalArgumentException("예약할 좌석이 한 개 이상 필요합니다.");
        }
        List<Seat> seats = new ArrayList<>();
        Set<Seat> requested = new HashSet<>();
        for (String seatId : seatIds) {
            Seat seat = seatManager.findSeat(screening.getTheater(), seatId);
            if (!requested.add(seat)) {
                throw new IllegalArgumentException("같은 좌석을 두 번 선택했습니다: " + seat);
            }
            seats.add(seat);
        }
        // 전부 확인한 뒤에 저장 (일부만 예약되는 상황 방지)
        for (Seat seat : seats) {
            if (isReserved(screening, seat)) {
                throw new IllegalStateException("이미 예약된 좌석입니다: " + seat);
            }
        }
        Reservation reservation = new Reservation(nextReservationId, member, screening, seats, tickets, totalPrice);
        nextReservationId++;
        reservations.add(reservation);
        return reservation;
    }

    /** 결제 성공: 결제 대기 중인 예약을 확정(RESERVED)한다. */
    public void confirm(int reservationId) {
        getExisting(reservationId).confirm();
    }

    /**
     * 결제 실패: 결제 대기 중인 예약을 취소(CANCELLED)해서 잡아 둔 좌석을 풀어 준다.
     * 이미 결제가 끝난 예약에는 쓸 수 없다(그 경우는 cancel()로 취소).
     */
    public void failPayment(int reservationId) {
        Reservation reservation = getExisting(reservationId);
        if (reservation.getStatus() != ReservationStatus.PENDING) {
            throw new IllegalStateException("결제 대기 상태의 예약만 결제 실패 처리할 수 있습니다: "
                    + reservationId + " (" + reservation.getStatus() + ")");
        }
        reservation.cancel();
    }

    private Reservation getExisting(int reservationId) {
        Reservation reservation = findById(reservationId);
        if (reservation == null) {
            throw new IllegalArgumentException("예약을 찾을 수 없습니다: " + reservationId);
        }
        return reservation;
    }

    /** 예약 취소. 삭제하지 않고 상태만 CANCELLED로 바뀐다. */
    public void cancel(int reservationId) {
        Reservation reservation = findById(reservationId);
        if (reservation == null) {
            throw new IllegalArgumentException("예약을 찾을 수 없습니다: " + reservationId);
        }
        reservation.cancel();
    }

    public Reservation findById(int reservationId) {
        for (Reservation reservation : reservations) {
            if (reservation.getReservationId() == reservationId) {
                return reservation;
            }
        }
        return null;
    }

    /** 해당 상영 회차에서 이 좌석이 예약 가능한지 */
    public boolean isAvailable(Screening screening, String seatId) {
        Seat seat = seatManager.findSeat(screening.getTheater(), seatId);
        return !isReserved(screening, seat);
    }

    /** 해당 상영 회차에서 아직 비어 있는 좌석들 */
    public List<Seat> getAvailableSeats(Screening screening) {
        List<Seat> available = new ArrayList<>();
        for (Seat seat : seatManager.getSeats(screening.getTheater())) {
            if (!isReserved(screening, seat)) {
                available.add(seat);
            }
        }
        return available;
    }

    /** 해당 상영 회차에서 이미 예약된 좌석들 */
    public List<Seat> getReservedSeats(Screening screening) {
        List<Seat> reserved = new ArrayList<>();
        for (Seat seat : seatManager.getSeats(screening.getTheater())) {
            if (isReserved(screening, seat)) {
                reserved.add(seat);
            }
        }
        return reserved;
    }

    /** 상영 일정 삭제 전 확인용: 유효한 예약이 하나라도 있는지 */
    public boolean hasActiveReservation(Screening screening) {
        for (Reservation reservation : reservations) {
            if (reservation.isActive() && sameScreening(reservation.getScreening(), screening)) {
                return true;
            }
        }
        return false;
    }

    public List<Reservation> getReservationsByMember(Member member) {
        List<Reservation> result = new ArrayList<>();
        for (Reservation reservation : reservations) {
            if (reservation.getMember().getId().equals(member.getId())) {
                result.add(reservation);
            }
        }
        return result;
    }

    public List<Reservation> getAllReservations() {
        return Collections.unmodifiableList(reservations);
    }

    private boolean isReserved(Screening screening, Seat seat) {
        for (Reservation reservation : reservations) {
            if (reservation.isActive()
                    && sameScreening(reservation.getScreening(), screening)
                    && reservation.getSeats().contains(seat)) {
                return true;
            }
        }
        return false;
    }

    private boolean sameScreening(Screening a, Screening b) {
        return a.getScreeningId() == b.getScreeningId();
    }
}
