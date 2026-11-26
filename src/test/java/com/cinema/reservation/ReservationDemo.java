package com.cinema.reservation;

import com.cinema.member.Member;
import com.cinema.movie.Movie;
import com.cinema.movie.Screening;
import com.cinema.movie.Theater;
import com.cinema.pricing.AdultTicket;
import com.cinema.pricing.Ticket;
import com.cinema.pricing.YouthTicket;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 발표 시연용. 좌석 잡기 -> 중복 방지 -> 결제 실패 -> 재예약 -> 결제 성공 -> 취소 흐름을 좌석 배치도와 함께 보여준다.
 * 실행: 전체 소스를 컴파일한 뒤 java com.cinema.reservation.ReservationDemo
 */
public class ReservationDemo {
    public static void main(String[] args) {
        Theater theater = new Theater(1, "1관", List.of("A1", "A2", "A3", "A4", "B1", "B2", "B3", "B4"));
        Movie movie = new Movie(1, "데모 영화", 100, "드라마");
        Screening screening = new Screening(1, movie, theater, LocalDateTime.of(2026, 12, 1, 10, 0));
        Member member = new Member("U001", "홍길동", "010-1234-5678", "pw1234");

        ReservationManager manager = new ReservationManager(new SeatManager());

        System.out.println("=== 1. 처음 상태 ===");
        printSeatMap(manager, screening);

        System.out.println("\n=== 2. A1(성인), A2(청소년) 동시 예약: 좌석을 먼저 잡는다 ===");
        List<Ticket> tickets = List.of(new AdultTicket(), new YouthTicket());
        Reservation r = manager.reserve(member, screening, List.of("A1", "A2"), tickets, 27000);
        System.out.println("예약번호 " + r.getReservationId() + ", 좌석 " + r.getSeats()
                + ", 티켓 " + r.getTickets().get(0).getType() + "/" + r.getTickets().get(1).getType()
                + ", 상태 " + r.getStatus());
        printSeatMap(manager, screening);

        System.out.println("\n=== 3. 결제 대기 중인 A2를 다른 사람이 예약 시도 ===");
        try {
            manager.reserve(member, screening, List.of("A2"), List.of(new AdultTicket()), 15000);
        } catch (IllegalStateException e) {
            System.out.println("거부됨: " + e.getMessage());
        }

        System.out.println("\n=== 4. 결제 실패 -> 좌석이 풀린다 ===");
        manager.failPayment(r.getReservationId());
        System.out.println("상태: " + r.getStatus());
        printSeatMap(manager, screening);

        System.out.println("\n=== 5. 다시 예약 후 결제 성공 -> 확정 ===");
        Reservation r2 = manager.reserve(member, screening, List.of("A1", "A2"), tickets, 27000);
        System.out.println("결제 전 상태: " + r2.getStatus());
        manager.confirm(r2.getReservationId());
        System.out.println("결제 후 상태: " + r2.getStatus());
        printSeatMap(manager, screening);

        System.out.println("\n=== 6. 확정된 예약 취소 -> 좌석이 풀린다 ===");
        manager.cancel(r2.getReservationId());
        System.out.println("상태: " + r2.getStatus());
        printSeatMap(manager, screening);
    }

    /** 좌석 배치도. O = 예약 가능, X = 점유됨(결제 대기 또는 확정) */
    private static void printSeatMap(ReservationManager manager, Screening screening) {
        for (Seat seat : new SeatManager().getSeats(screening.getTheater())) {
            boolean free = manager.isAvailable(screening, seat.getSeatId());
            System.out.print("[" + seat.getSeatId() + (free ? ":O" : ":X") + "] ");
            if (seat.getCol() == 4) {
                System.out.println();
            }
        }
    }
}
