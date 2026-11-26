package com.cinema.reservation;

import com.cinema.member.Member;
import com.cinema.member.MemberManager;
import com.cinema.movie.Movie;
import com.cinema.movie.MovieService;
import com.cinema.movie.Screening;
import com.cinema.movie.Theater;
import com.cinema.pricing.AdultTicket;
import com.cinema.pricing.PriceService;
import com.cinema.pricing.StudentDiscountPolicy;
import com.cinema.pricing.Ticket;
import com.cinema.pricing.YouthTicket;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 발표 시연용. ReservationService로 좌석 잡기 -> 중복 방지 -> 결제 실패 -> 재예약 -> 결제 성공(포인트) -> 취소를 보여준다.
 * 실행: 전체 소스를 컴파일한 뒤 java com.cinema.reservation.ReservationDemo
 */
public class ReservationDemo {
    private static final int SCREENING_ID = 1;

    public static void main(String[] args) {
        MemberManager members = new MemberManager();
        Member member = members.register("홍길동", "010-1234-5678", "pw1234");
        MovieService movies = new MovieService();
        Movie movie = new Movie(1, "데모 영화", 100, "드라마");
        movies.addMovie(movie);
        Theater theater = new Theater(1, "1관", List.of("A1", "A2", "A3", "A4", "B1", "B2", "B3", "B4"));
        movies.addTheater(theater);
        movies.addScreening(new Screening(SCREENING_ID, movie, theater, LocalDateTime.now().plusDays(1)));

        ReservationService service = new ReservationService(members, movies,
                new ReservationManager(new SeatManager()), new PriceService(new StudentDiscountPolicy()));

        System.out.println("=== 1. 처음 상태 ===");
        printSeatMap(service, theater);

        System.out.println("\n=== 2. A1(성인), A2(청소년) 동시 예약: 좌석을 먼저 잡는다 ===");
        List<Ticket> tickets = List.of(new AdultTicket(), new YouthTicket());
        Reservation r = service.reserve(member.getId(), SCREENING_ID, List.of("A1", "A2"), tickets);
        System.out.println("예약번호 " + r.getReservationId() + ", 좌석 " + r.getSeats()
                + ", 티켓 " + r.getTickets().get(0).getType() + "/" + r.getTickets().get(1).getType()
                + ", 금액 " + r.getTotalPrice() + "원(학생할인), 상태 " + r.getStatus());
        printSeatMap(service, theater);

        System.out.println("\n=== 3. 결제 대기 중인 A2를 다른 요청이 예약 시도 ===");
        try {
            service.reserve(member.getId(), SCREENING_ID, List.of("A2"), List.of(new AdultTicket()));
        } catch (IllegalStateException e) {
            System.out.println("거부됨: " + e.getMessage());
        }

        System.out.println("\n=== 4. 결제 실패 -> 좌석이 풀린다 ===");
        service.completePayment(member.getId(), r.getReservationId(), false);
        System.out.println("상태: " + r.getStatus() + ", 포인트: " + member.getPoints());
        printSeatMap(service, theater);

        System.out.println("\n=== 5. 다시 예약 후 결제 성공 -> 확정, 포인트 적립 ===");
        Reservation r2 = service.reserve(member.getId(), SCREENING_ID, List.of("A1", "A2"), tickets);
        System.out.println("결제 전 상태: " + r2.getStatus() + ", 포인트: " + member.getPoints());
        service.completePayment(member.getId(), r2.getReservationId(), true);
        System.out.println("결제 후 상태: " + r2.getStatus() + ", 포인트: " + member.getPoints()
                + ", 등급: " + member.getGrade());
        printSeatMap(service, theater);

        System.out.println("\n=== 6. 예약이 있는 상영 삭제 시도 ===");
        try {
            service.removeScreening(SCREENING_ID);
        } catch (IllegalStateException e) {
            System.out.println("거부됨: " + e.getMessage());
        }

        System.out.println("\n=== 7. 확정된 예약 취소 -> 좌석이 풀린다 ===");
        service.cancel(member.getId(), r2.getReservationId());
        System.out.println("상태: " + r2.getStatus());
        printSeatMap(service, theater);
    }

    /** 좌석 배치도. O = 예약 가능, X = 점유됨(결제 대기 또는 확정) */
    private static void printSeatMap(ReservationService service, Theater theater) {
        List<Seat> available = service.getAvailableSeats(SCREENING_ID);
        for (String seatId : theater.getSeatIds()) {
            boolean free = false;
            for (Seat seat : available) {
                if (seat.getSeatId().equals(seatId)) {
                    free = true;
                }
            }
            System.out.print("[" + seatId + (free ? ":O" : ":X") + "] ");
            if (seatId.endsWith("4")) {
                System.out.println();
            }
        }
    }
}
