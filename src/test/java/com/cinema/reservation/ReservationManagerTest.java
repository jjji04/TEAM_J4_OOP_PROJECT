package com.cinema.reservation;

import com.cinema.member.Member;
import com.cinema.movie.Movie;
import com.cinema.movie.Screening;
import com.cinema.movie.Theater;
import com.cinema.pricing.AdultTicket;
import com.cinema.pricing.YouthTicket;
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
        check("예약 생성 직후 상태 PENDING(결제 대기)", r.getStatus() == ReservationStatus.PENDING);
        check("결제 대기 중에도 좌석 점유", !rm.isAvailable(s1, "A1") && !rm.isAvailable(s1, "A2"));
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

        // 6. 결제 성공/실패 (좌석을 먼저 잡고 결제로 확정)
        ReservationManager pm = new ReservationManager(new SeatManager());
        Reservation p1 = pm.reserve(member, s1, List.of("A1", "A2"), two, 30000);
        check("결제 전 PENDING", p1.getStatus() == ReservationStatus.PENDING && !p1.isConfirmed());
        expect("결제 대기 중인 좌석 중복 예약 거부", IllegalStateException.class,
                () -> pm.reserve(member, s1, List.of("A2"), one, 15000));
        pm.confirm(p1.getReservationId());
        check("결제 성공 후 RESERVED", p1.getStatus() == ReservationStatus.RESERVED && p1.isConfirmed());
        check("확정 후에도 좌석 점유", !pm.isAvailable(s1, "A1"));
        expect("이미 확정된 예약 재확정", IllegalStateException.class, () -> pm.confirm(p1.getReservationId()));
        expect("확정된 예약은 결제 실패 처리 불가", IllegalStateException.class,
                () -> pm.failPayment(p1.getReservationId()));
        check("실패 처리 거부 후에도 상태 유지", p1.getStatus() == ReservationStatus.RESERVED);

        Reservation p2 = pm.reserve(member, s1, List.of("A3"), one, 15000);
        pm.failPayment(p2.getReservationId());
        check("결제 실패 시 CANCELLED", p2.getStatus() == ReservationStatus.CANCELLED);
        check("결제 실패 시 좌석 해제", pm.isAvailable(s1, "A3"));
        check("결제 실패 기록은 남음", pm.findById(p2.getReservationId()) == p2);
        expect("취소된 예약 확정 불가", IllegalStateException.class, () -> pm.confirm(p2.getReservationId()));
        expect("없는 예약 확정", IllegalArgumentException.class, () -> pm.confirm(999));
        expect("없는 예약 결제 실패 처리", IllegalArgumentException.class, () -> pm.failPayment(999));
        pm.reserve(member, s1, List.of("A3"), one, 15000);
        check("결제 실패로 풀린 좌석 재예약 성공", !pm.isAvailable(s1, "A3"));
        pm.cancel(p1.getReservationId());
        check("확정된 예약도 취소 가능, 좌석 해제", pm.isAvailable(s1, "A1") && pm.isAvailable(s1, "A2"));

        // 7. 여러 좌석 + 서로 다른 티켓 종류 동시 예약
        ReservationManager mm = new ReservationManager(new SeatManager());
        List<Ticket> mixed = List.of(new AdultTicket(), new YouthTicket(), new AdultTicket());
        Reservation m = mm.reserve(member, s1, List.of("A1", "A2", "A3"), mixed, 42000);
        check("성인·청소년 혼합 3석 예약", m.getSeats().size() == 3 && m.getTickets().size() == 3);
        check("좌석 순서대로 티켓 대응", m.getTickets().get(1) instanceof YouthTicket
                && m.getTickets().get(0) instanceof AdultTicket);
        check("혼합 예약의 총액 보관", m.getTotalPrice() == 42000);

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
