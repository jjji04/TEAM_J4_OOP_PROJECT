package com.cinema.movie;

import java.time.LocalDateTime;

public class Screening {
    private int screeningId;
    private Movie movie;
    private Theater theater;
    private LocalDateTime startTime;

    public Screening(int screeningId, Movie movie, Theater theater, LocalDateTime startTime) {
        if (screeningId <= 0 || movie == null || theater == null || startTime == null) throw new IllegalArgumentException("상영 정보 오류");
        this.screeningId = screeningId;
        this.movie = movie;
        this.theater = theater;
        this.startTime = startTime;
    }
    public int getScreeningId() { return screeningId; }
    public Movie getMovie() { return movie; }
    public Theater getTheater() { return theater; }
    public LocalDateTime getStartTime() { return startTime; }
    public LocalDateTime getEndTime() { return startTime.plusMinutes(movie.getRunningTime()); }
    public void setStartTime(LocalDateTime startTime) { if (startTime == null) throw new IllegalArgumentException("시작 시간 오류"); this.startTime = startTime; }
    @Override public String toString() { return screeningId + " | " + movie.getTitle() + " | " + theater.getName() + " | " + startTime + " ~ " + getEndTime(); }
}
