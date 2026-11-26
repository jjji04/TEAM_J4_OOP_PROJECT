package com.cinema.movie;

public class Movie {
    private int movieId;
    private String title;
    private int runningTime;
    private String genre;

    public Movie(int movieId, String title, int runningTime, String genre) {
        if (movieId <= 0 || title == null || title.isBlank() || runningTime <= 0) throw new IllegalArgumentException("영화 정보가 올바르지 않습니다.");
        this.movieId = movieId;
        this.title = title;
        this.runningTime = runningTime;
        this.genre = genre;
    }
    public int getMovieId() { return movieId; }
    public String getTitle() { return title; }
    public int getRunningTime() { return runningTime; }
    public String getGenre() { return genre; }
    public void setTitle(String title) { if (title == null || title.isBlank()) throw new IllegalArgumentException("제목 오류"); this.title = title; }
    public void setRunningTime(int runningTime) { if (runningTime <= 0) throw new IllegalArgumentException("상영 시간 오류"); this.runningTime = runningTime; }
    public void setGenre(String genre) { this.genre = genre; }
    @Override public String toString() { return movieId + " | " + title + " | " + runningTime + "분 | " + genre; }
}
