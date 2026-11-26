package com.cinema.reservation;

import com.cinema.member.Member;
import com.cinema.member.MemberManager;
import com.cinema.movie.MovieService;
import com.cinema.movie.Screening;
import com.cinema.pricing.PriceService;
import com.cinema.pricing.Ticket;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 예약의 진입점. 회원(D), 상영(A), 요금(C)과 좌석 예약(B)을 한 흐름으로 묶는다.
 * 각 파트 클래스를 고치지 않고 공개 메서드만 호출한다. 파트 사이의 연결 규칙은 여기에만 둔다.
 *
 * 흐름: reserve(좌석을 잡음, PENDING) -> completePayment(성공이면 확정과 포인트 적립, 실패면 좌석 해제)
 *
 * 이 클래스가 맡는 것
 *  - 총액을 호출한 쪽이 아니라 PriceService로 직접 계산 (넘겨받은 금액을 믿지 않는다)
 *  - 본인 예약만 결제·취소 가능
 *  - 이미 시작한 상영은 예약 불가
 *  - 예약이 남아 있는 상영은 삭제 불가
 *  - 결제 성공 시에만 회원 포인트 적립
 */
public class ReservationService {
    private final MemberManager memberManager;
    private final MovieService movieService;
    private final ReservationManager reservationManager;
    private final PriceService priceService;

    public ReservationService(MemberManager memberManager, MovieService movieService,
                              ReservationManager reservationManager, PriceService priceService) {
        if (memberManager == null || movieService == null || reservationManager == null || priceService == null) {
            throw new IllegalArgumentException("필요한 구성 요소가 없습니다.");
        }
        this.memberManager = memberManager;
        this.movieService = movieService;
        this.reservationManager = reservationManager;
        this.priceService = priceService;
    }

    /**
     * 좌석을 잡는다(결제 대기). 좌석 하나에 티켓 하나가 같은 순서로 대응한다.
     * 총액은 티켓별 요금을 PriceService로 계산해 합산한다.
     */
    public Reservation reserve(String memberId, int screeningId, List<String> seatIds, List<Ticket> tickets) {
        Member member = memberManager.findById(memberId);
        Screening screening = findScreening(screeningId);
        if (screening.getStartTime().isBefore(LocalDateTime.now())) {
            throw new IllegalStateException("이미 시작한 상영은 예약할 수 없습니다.");
        }
        if (tickets == null || tickets.isEmpty()) {
            throw new IllegalArgumentException("티켓이 한 개 이상 필요합니다.");
        }
        int totalPrice = 0;
        for (Ticket ticket : tickets) {
            totalPrice += priceService.calculatePrice(ticket);
        }
        return reservationManager.reserve(member, screening, seatIds, tickets, totalPrice);
    }

    /**
     * 결제 결과를 반영한다.
     * 성공: 예약 확정 후 결제 금액만큼 회원 포인트 적립. 실패: 잡아 둔 좌석을 풀어 준다.
     */
    public Reservation completePayment(String memberId, int reservationId, boolean paymentSucceeded) {
        Reservation reservation = findOwned(memberId, reservationId);
        if (paymentSucceeded) {
            reservationManager.confirm(reservationId);   // 결제 대기가 아니면 여기서 예외 -> 포인트 중복 적립 없음
            if (reservation.getTotalPrice() > 0) {
                reservation.getMember().recordPayment(reservation.getTotalPrice());
            }
        } else {
            reservationManager.failPayment(reservationId);
        }
        return reservation;
    }

    /** 본인 예약 취소. 좌석은 다시 예약 가능해진다. (적립된 포인트는 환수하지 않는다) */
    public void cancel(String memberId, int reservationId) {
        findOwned(memberId, reservationId);
        reservationManager.cancel(reservationId);
    }

    /** 상영 회차에서 아직 비어 있는 좌석 */
    public List<Seat> getAvailableSeats(int screeningId) {
        return reservationManager.getAvailableSeats(findScreening(screeningId));
    }

    /** 내 예약 목록 (취소된 예약 포함) */
    public List<Reservation> getMyReservations(String memberId) {
        return reservationManager.getReservationsByMember(memberManager.findById(memberId));
    }

    /** 상영 삭제. 예약(결제 대기 포함)이 남아 있으면 막는다. */
    public void removeScreening(int screeningId) {
        Screening screening = findScreening(screeningId);
        if (reservationManager.hasActiveReservation(screening)) {
            throw new IllegalStateException("예약이 남아 있는 상영은 삭제할 수 없습니다: " + screeningId);
        }
        movieService.removeScreening(screeningId);
    }

    private Screening findScreening(int screeningId) {
        Screening screening = movieService.findScreening(screeningId);
        if (screening == null) {
            throw new IllegalArgumentException("상영 정보를 찾을 수 없습니다: " + screeningId);
        }
        return screening;
    }

    private Reservation findOwned(String memberId, int reservationId) {
        Reservation reservation = reservationManager.findById(reservationId);
        if (reservation == null) {
            throw new IllegalArgumentException("예약을 찾을 수 없습니다: " + reservationId);
        }
        if (!reservation.getMember().getId().equals(memberId)) {
            throw new IllegalArgumentException("본인의 예약만 처리할 수 있습니다.");
        }
        return reservation;
    }
}
