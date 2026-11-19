package com.cinema.reservation;

import java.util.Objects;

/**
 * 상영관(Theater)에 속한 좌석 하나.
 * 좌석은 모든 상영 회차가 공유하므로 예약 여부는 여기에 두지 않고,
 * Reservation을 조회해서 판단한다.
 */
public class Seat {
    private final String seatId;   // 예: "A1"
    private final int row;
    private final int col;

    public Seat(String seatId, int row, int col) {
        if (seatId == null || seatId.isBlank()) {
            throw new IllegalArgumentException("좌석 번호가 비어 있습니다.");
        }
        if (row < 1 || col < 1) {
            throw new IllegalArgumentException("행과 열은 1 이상이어야 합니다.");
        }
        this.seatId = seatId;
        this.row = row;
        this.col = col;
    }

    public String getSeatId() {
        return seatId;
    }

    public int getRow() {
        return row;
    }

    public int getCol() {
        return col;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Seat)) return false;
        return seatId.equals(((Seat) o).seatId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(seatId);
    }

    @Override
    public String toString() {
        return seatId;
    }
}
