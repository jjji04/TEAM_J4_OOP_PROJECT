package com.cinema.reservation;

import com.cinema.member.Member;
import com.cinema.movie.Movie;
import com.cinema.movie.Screening;
import com.cinema.movie.Theater;
import com.cinema.pricing.AdultTicket;
import com.cinema.pricing.Ticket;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 발표 시연용. 예약 -> 중복 방지 -> 취소 -> 재예약 흐름을 좌석 배치도와 함께 보여준다.
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

        System.out.println("\n=== 2. A1, A2 예약 ===");
        Reservation r = manager.reserve(member, screening, List.of("A1", "A2"), tickets(2), 30000);
        System.out.println("예약번호 " + r.getReservationId() + ", 좌석 " + r.getSeats() + ", 금액 " + r.getTotalPrice());
        printSeatMap(manager, screening);

        System.out.println("\n=== 3. 같은 좌석(A2) 다시 예약 시도 ===");
        try {
            manager.reserve(member, screening, List.of("A2"), tickets(1), 15000);
        } catch (IllegalStateException e) {
            System.out.println("거부됨: " + e.getMessage());
        }

        System.out.println("\n=== 4. 예약 " + r.getReservationId() + " 취소 ===");
        manager.cancel(r.getReservationId());
        System.out.println("상태: " + r.getStatus());
        printSeatMap(manager, screening);

        System.out.println("\n=== 5. 취소된 A2 재예약 ===");
        manager.reserve(member, screening, List.of("A2"), tickets(1), 15000);
        printSeatMap(manager, screening);
    }

    /** 좌석 배치도. O = 예약 가능, X = 예약됨 */
    private static void printSeatMap(ReservationManager manager, Screening screening) {
        for (Seat seat : new SeatManager().getSeats(screening.getTheater())) {
            boolean free = manager.isAvailable(screening, seat.getSeatId());
            System.out.print("[" + seat.getSeatId() + (free ? ":O" : ":X") + "] ");
            if (seat.getCol() == 4) {
                System.out.println();
            }
        }
    }

    private static List<Ticket> tickets(int count) {
        List<Ticket> list = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            list.add(new AdultTicket());
        }
        return list;
    }
}
