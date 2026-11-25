package com.cinema.reservation;

import com.cinema.member.Member;
import com.cinema.movie.Movie;
import com.cinema.movie.Screening;
import com.cinema.movie.Theater;
import com.cinema.pricing.AdultTicket;
import com.cinema.pricing.Ticket;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 예약 기능 확인용 테스트. JUnit 도입 전이라 main 메서드로 직접 돌린다.
 * 실행: 전체 소스를 컴파일한 뒤 java com.cinema.reservation.ReservationManagerTest
 */
public class ReservationManagerTest {
    private static int passed = 0;

    public static void main(String[] args) {
        Theater theater = new Theater(1, "1관", List.of("A1", "A2", "A3", "B1"));
        Movie movie = new Movie(1, "테스트 영화", 100, "드라마");
        Screening s1 = new Screening(1, movie, theater, LocalDateTime.of(2026, 12, 1, 10, 0));
        Screening s2 = new Screening(2, movie, theater, LocalDateTime.of(2026, 12, 1, 14, 0));
        Member member = new Member("U001", "홍길동", "010-1234-5678", "pw1234");
        List<Ticket> one = List.of(new AdultTicket());
        List<Ticket> two = List.of(new AdultTicket(), new AdultTicket());

        // 1. 좌석 예약
        ReservationManager rm = new ReservationManager(new SeatManager());
        Reservation r = rm.reserve(member, s1, List.of("A1", "A2"), two, 30000);
        check("예약 생성 후 상태 RESERVED", r.getStatus() == ReservationStatus.RESERVED);
        check("예약한 좌석은 불가", !rm.isAvailable(s1, "A1") && !rm.isAvailable(s1, "A2"));
        check("남은 좌석 2개", rm.getAvailableSeats(s1).size() == 2);
        check("예약 좌석 조회 2개", rm.getReservedSeats(s1).size() == 2);

        // 2. 중복 예약 방지
        expect("같은 회차 같은 좌석 중복 예약", IllegalStateException.class,
                () -> rm.reserve(member, s1, List.of("A2"), one, 15000));
        check("실패한 예약은 저장 안 됨", rm.getAllReservations().size() == 1);
        expect("일부만 겹쳐도 전체 거부", IllegalStateException.class,
                () -> rm.reserve(member, s1, List.of("A3", "A1"), two, 30000));
        check("전체 거부 시 A3도 비어 있음", rm.isAvailable(s1, "A3"));
        check("다른 회차의 같은 좌석은 예약 가능", rm.isAvailable(s2, "A1"));
        rm.reserve(member, s2, List.of("A1"), one, 15000);

        // 3. 입력 검증
        expect("상영관에 없는 좌석", IllegalArgumentException.class,
                () -> rm.reserve(member, s1, List.of("Z9"), one, 15000));
        expect("한 번에 같은 좌석 두 번", IllegalArgumentException.class,
                () -> rm.reserve(member, s1, List.of("A3", "A3"), two, 30000));
        expect("좌석 수와 티켓 수 불일치", IllegalArgumentException.class,
                () -> rm.reserve(member, s1, List.of("A3"), two, 15000));
        expect("좌석 없음", IllegalArgumentException.class,
                () -> rm.reserve(member, s1, List.of(), one, 0));

        // 4. 예약 취소 후 재예약
        check("취소 전 유효 예약 있음", rm.hasActiveReservation(s1));
        rm.cancel(r.getReservationId());
        check("취소 후 상태 CANCELLED", r.getStatus() == ReservationStatus.CANCELLED);
        check("취소한 좌석 다시 예약 가능", rm.isAvailable(s1, "A1") && rm.isAvailable(s1, "A2"));
        check("취소 후 유효 예약 없음", !rm.hasActiveReservation(s1));
        check("취소해도 예약 기록은 남음", rm.findById(r.getReservationId()) == r);
        expect("이미 취소한 예약 재취소", IllegalStateException.class, () -> rm.cancel(r.getReservationId()));
        expect("없는 예약 취소", IllegalArgumentException.class, () -> rm.cancel(999));
        rm.reserve(member, s1, List.of("A1"), one, 15000);
        check("취소된 좌석 재예약 성공", !rm.isAvailable(s1, "A1"));

        // 5. 조회
        check("회원별 예약 조회(3건)", rm.getReservationsByMember(member).size() == 3);

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
            throw new AssertionError("실패: " + name + " (다른 예외 " + e.getClass().getSimpleName() + ")");
        }
        throw new AssertionError("실패: " + name + " (예외가 안 났음)");
    }
}
