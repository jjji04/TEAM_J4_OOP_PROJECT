package com.cinema.movie;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** A파트 독립 구현: 좌석 식별자만 관리. B파트 Seat 클래스 확정 후 연동. */
public class Theater {
    private final int theaterId;
    private final String name;
    private final List<String> seatIds;

    public Theater(int theaterId, String name, List<String> seatIds) {
        if (theaterId <= 0 || name == null || name.trim().isEmpty() || seatIds == null)
            throw new IllegalArgumentException("상영관 정보 오류");
        if (seatIds.stream().anyMatch(s -> s == null || s.trim().isEmpty()) ||
            seatIds.stream().distinct().count() != seatIds.size())
            throw new IllegalArgumentException("좌석 ID 오류 또는 중복");
        this.theaterId = theaterId;
        this.name = name;
        this.seatIds = new ArrayList<>(seatIds);
    }
    public int getTheaterId() { return theaterId; }
    public String getName() { return name; }
    public List<String> getSeatIds() { return Collections.unmodifiableList(seatIds); }
    public boolean hasSeat(String seatId) { return seatIds.contains(seatId); }
    public int getCapacity() { return seatIds.size(); }
}
