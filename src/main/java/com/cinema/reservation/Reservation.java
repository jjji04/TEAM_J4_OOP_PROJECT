package com.cinema.reservation;

import com.cinema.member.Member;
import com.cinema.movie.Screening;
import com.cinema.pricing.Ticket;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 한 번의 예약 정보. 어떤 회원이, 어느 상영의, 어떤 좌석을, 어떤 티켓으로 예약했는지 담는다.
 * 좌석별 가용 여부는 이 예약들을 조회해서 계산한다(Seat에는 예약 여부를 두지 않는다).
 */
public class Reservation {
    private final int reservationId;
    private final Member member;
    private final Screening screening;
    private final List<Seat> seats;
    private final List<Ticket> tickets;
    private final int totalPrice;
    private final LocalDateTime reservedAt;
    private ReservationStatus status;

    public Reservation(int reservationId, Member member, Screening screening,
                       List<Seat> seats, List<Ticket> tickets, int totalPrice) {
        if (member == null || screening == null) {
            throw new IllegalArgumentException("회원과 상영 정보는 필수입니다.");
        }
        if (seats == null || seats.isEmpty()) {
            throw new IllegalArgumentException("예약할 좌석이 한 개 이상 필요합니다.");
        }
        if (tickets == null || tickets.size() != seats.size()) {
            throw new IllegalArgumentException("좌석 수와 티켓 수가 같아야 합니다.");
        }
        if (totalPrice < 0) {
            throw new IllegalArgumentException("총 금액은 0 이상이어야 합니다: " + totalPrice);
        }
        this.reservationId = reservationId;
        this.member = member;
        this.screening = screening;
        // 바깥 리스트가 나중에 바뀌어도 예약 내용이 흔들리지 않도록 복사해서 보관
        this.seats = new ArrayList<>(seats);
        this.tickets = new ArrayList<>(tickets);
        this.totalPrice = totalPrice;
        this.reservedAt = LocalDateTime.now();
        this.status = ReservationStatus.RESERVED;
    }

    public int getReservationId() {
        return reservationId;
    }

    public Member getMember() {
        return member;
    }

    public Screening getScreening() {
        return screening;
    }

    public List<Seat> getSeats() {
        return Collections.unmodifiableList(seats);
    }

    public List<Ticket> getTickets() {
        return Collections.unmodifiableList(tickets);
    }

    public int getTotalPrice() {
        return totalPrice;
    }

    public LocalDateTime getReservedAt() {
        return reservedAt;
    }

    public ReservationStatus getStatus() {
        return status;
    }

    /** 예약을 취소한다. 삭제하지 않고 상태만 CANCELLED로 바꾼다. */
    public void cancel() {
        if (status == ReservationStatus.CANCELLED) {
            throw new IllegalStateException("이미 취소된 예약입니다: " + reservationId);
        }
        status = ReservationStatus.CANCELLED;
    }

    /** 취소되지 않고 유효한 예약인지 */
    public boolean isActive() {
        return status == ReservationStatus.RESERVED;
    }
}
