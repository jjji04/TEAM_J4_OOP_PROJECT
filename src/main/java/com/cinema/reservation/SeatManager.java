package com.cinema.reservation;

import com.cinema.movie.Theater;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 상영관(Theater)의 좌석 ID 문자열("A1" 등)을 Seat 객체로 바꿔서 관리한다.
 * Theater는 좌석을 String ID로만 들고 있으므로(A 담당), Seat 변환은 B가 맡는다.
 * 예약 여부는 여기서 판단하지 않는다(ReservationManager 담당).
 */
public class SeatManager {
    private final Map<Integer, List<Seat>> seatsByTheater = new HashMap<>();

    /** 상영관의 전체 좌석 (처음 요청 시 만들고 이후 재사용) */
    public List<Seat> getSeats(Theater theater) {
        if (theater == null) {
            throw new IllegalArgumentException("상영관 정보가 없습니다.");
        }
        List<Seat> seats = seatsByTheater.computeIfAbsent(theater.getTheaterId(), id -> {
            List<Seat> created = new ArrayList<>();
            for (String seatId : theater.getSeatIds()) {
                created.add(parseSeat(seatId));
            }
            return created;
        });
        return Collections.unmodifiableList(seats);
    }

    /** 좌석 번호로 좌석 찾기. 그 상영관에 없는 좌석이면 예외 */
    public Seat findSeat(Theater theater, String seatId) {
        if (seatId != null) {
            String key = seatId.trim().toUpperCase();
            for (Seat seat : getSeats(theater)) {
                if (seat.getSeatId().equalsIgnoreCase(key)) {
                    return seat;
                }
            }
        }
        throw new IllegalArgumentException("상영관에 없는 좌석입니다: " + seatId);
    }

    /** "A1" -> 행 A(1), 열 1. 영문 한 글자 + 숫자 형식만 허용 */
    private Seat parseSeat(String seatId) {
        String id = seatId.trim().toUpperCase();
        if (!id.matches("[A-Z][0-9]+")) {
            throw new IllegalArgumentException("좌석 번호 형식이 올바르지 않습니다(예: A1): " + seatId);
        }
        int row = id.charAt(0) - 'A' + 1;
        int col = Integer.parseInt(id.substring(1));
        return new Seat(id, row, col);
    }
}
