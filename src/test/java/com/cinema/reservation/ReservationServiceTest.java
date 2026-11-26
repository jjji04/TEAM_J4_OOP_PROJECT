package com.cinema.reservation;

import com.cinema.member.Member;
import com.cinema.member.MemberManager;
import com.cinema.member.MemberNotFoundException;
import com.cinema.movie.Movie;
import com.cinema.movie.MovieService;
import com.cinema.movie.Screening;
import com.cinema.movie.Theater;
import com.cinema.pricing.AdultTicket;
import com.cinema.pricing.NoDiscountPolicy;
import com.cinema.pricing.PriceService;
import com.cinema.pricing.StudentDiscountPolicy;
import com.cinema.pricing.Ticket;
import com.cinema.pricing.YouthTicket;

import java.time.LocalDateTime;
import java.util.List;

/**
 * ReservationService 통합 테스트. 회원(D), 상영(A), 요금(C), 예약(B)이 이어지는지 확인한다.
 * 실행: 전체 소스를 컴파일한 뒤 java com.cinema.reservation.ReservationServiceTest
 */
public class ReservationServiceTest {
    private static int passed = 0;

    private static MemberManager members;
    private static MovieService movies;
    private static ReservationService service;
    private static Member hong;
    private static Member kim;

    /** 매번 새 환경: 회원 2명, 영화 1편, 상영관 1개, 미래 상영 1회(id 1), 지난 상영 1회(id 2) */
    private static void setUp(PriceService price) {
        members = new MemberManager();
        hong = members.register("홍길동", "010-1111-2222", "pw1234");
        kim = members.register("김철수", "010-3333-4444", "pw1234");
        movies = new MovieService();
        Movie movie = new Movie(1, "테스트 영화", 100, "드라마");
        movies.addMovie(movie);
        Theater theater = new Theater(1, "1관", List.of("A1", "A2", "A3", "B1"));
        movies.addTheater(theater);
        movies.addScreening(new Screening(1, movie, theater, LocalDateTime.now().plusDays(1)));
        movies.addScreening(new Screening(2, movie, theater, LocalDateTime.now().minusDays(1)));
        service = new ReservationService(members, movies, new ReservationManager(new SeatManager()), price);
    }

    private static List<Ticket> adultYouth() {
        return List.of(new AdultTicket(), new YouthTicket());
    }

    public static void main(String[] args) {
        // 1. 총액은 서비스가 PriceService로 계산
        setUp(new PriceService(new NoDiscountPolicy()));
        Reservation r = service.reserve(hong.getId(), 1, List.of("A1", "A2"), adultYouth());
        check("할인 없음: 성인15000 + 청소년12000 = 27000", r.getTotalPrice() == 27000);
        check("예약 직후 결제 대기(PENDING)", r.getStatus() == ReservationStatus.PENDING);
        check("결제 전에는 포인트 적립 없음", hong.getPoints() == 0);
        check("결제 대기 중 좌석 점유", service.getAvailableSeats(1).size() == 2);

        setUp(new PriceService(new StudentDiscountPolicy()));
        Reservation d = service.reserve(hong.getId(), 1, List.of("A1", "A2"), adultYouth());
        check("학생 할인 10%: 13500 + 10800 = 24300", d.getTotalPrice() == 24300);

        // 2. 결제 성공: 확정 + 포인트 적립
        service.completePayment(hong.getId(), d.getReservationId(), true);
        check("결제 성공 후 RESERVED", d.getStatus() == ReservationStatus.RESERVED);
        check("결제 성공 후 포인트 적립", hong.getPoints() > 0);
        check("결제 금액만큼 누적 결제금액 반영", hong.getTotalPaidAmount() == 24300);
        int points = hong.getPoints();
        expect("같은 예약 결제 두 번 불가", IllegalStateException.class,
                () -> service.completePayment(hong.getId(), d.getReservationId(), true));
        check("결제 두 번 시도해도 포인트 중복 적립 없음", hong.getPoints() == points);

        // 3. 결제 실패: 좌석 해제, 포인트 없음
        Reservation f = service.reserve(hong.getId(), 1, List.of("A3"), List.of(new AdultTicket()));
        check("A3 잡아 둠", service.getAvailableSeats(1).size() == 1);
        service.completePayment(hong.getId(), f.getReservationId(), false);
        check("결제 실패 시 CANCELLED", f.getStatus() == ReservationStatus.CANCELLED);
        check("결제 실패 시 좌석 해제", service.getAvailableSeats(1).size() == 2);
        check("결제 실패 시 포인트 그대로", hong.getPoints() == points);

        // 4. 입력 검증
        expect("없는 회원", MemberNotFoundException.class,
                () -> service.reserve("U999", 1, List.of("B1"), List.of(new AdultTicket())));
        expect("없는 상영", IllegalArgumentException.class,
                () -> service.reserve(hong.getId(), 99, List.of("B1"), List.of(new AdultTicket())));
        expect("이미 시작한 상영 예약", IllegalStateException.class,
                () -> service.reserve(hong.getId(), 2, List.of("B1"), List.of(new AdultTicket())));
        expect("좌석 수와 티켓 수 불일치", IllegalArgumentException.class,
                () -> service.reserve(hong.getId(), 1, List.of("B1"), adultYouth()));
        expect("티켓 없음", IllegalArgumentException.class,
                () -> service.reserve(hong.getId(), 1, List.of("B1"), List.of()));
        expect("확정된 좌석 중복 예약", IllegalStateException.class,
                () -> service.reserve(kim.getId(), 1, List.of("A1"), List.of(new AdultTicket())));

        // 5. 본인 예약만 처리
        Reservation k = service.reserve(kim.getId(), 1, List.of("B1"), List.of(new AdultTicket()));
        expect("남의 예약 결제 불가", IllegalArgumentException.class,
                () -> service.completePayment(hong.getId(), k.getReservationId(), true));
        expect("남의 예약 취소 불가", IllegalArgumentException.class,
                () -> service.cancel(hong.getId(), k.getReservationId()));
        check("거부 후 예약 상태 유지", k.getStatus() == ReservationStatus.PENDING);
        expect("없는 예약 취소", IllegalArgumentException.class, () -> service.cancel(kim.getId(), 999));

        // 6. 상영 삭제 보호
        expect("예약이 있는 상영 삭제 불가", IllegalStateException.class, () -> service.removeScreening(1));
        check("삭제 거부 후 상영 유지", movies.findScreening(1) != null);

        // 7. 취소와 조회
        service.cancel(kim.getId(), k.getReservationId());
        check("본인 취소 성공", k.getStatus() == ReservationStatus.CANCELLED);
        check("내 예약 조회(취소 포함 1건)", service.getMyReservations(kim.getId()).size() == 1);
        check("홍길동 예약 조회(확정 1 + 결제 실패 1 = 2건)", service.getMyReservations(hong.getId()).size() == 2);
        service.cancel(hong.getId(), d.getReservationId());
        check("모든 예약 취소 후 좌석 전부 비어 있음", service.getAvailableSeats(1).size() == 4);
        service.removeScreening(1);
        check("예약이 없으면 상영 삭제 가능", movies.findScreening(1) == null);

        System.out.println("\n통과 " + passed + "건, 실패 없음");
    }

    private static void check(String name, boolean condition) {
        if (!condition) {
            throw new AssertionError("실패: " + name);
        }
        passed++;
        System.out.println("PASS  " + name);
    }

    private static void expect(String name, Class<? extends Exception> type, Runnable action) {
        try {
            action.run();
        } catch (Exception e) {
            if (type.isInstance(e)) {
                passed++;
                System.out.println("PASS  " + name);
                return;
            }
            throw new AssertionError("실패: " + name + " (다른 예외 " + e.getClass().getSimpleName() + ": " + e.getMessage() + ")");
        }
        throw new AssertionError("실패: " + name + " (예외가 안 났음)");
    }
}
