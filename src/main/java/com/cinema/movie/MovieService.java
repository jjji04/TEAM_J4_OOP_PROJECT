package com.cinema.movie;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MovieService {
    private List<Movie> movies = new ArrayList<>();
    private List<Theater> theaters = new ArrayList<>();
    private List<Screening> screenings = new ArrayList<>();

    public void addMovie(Movie movie) {
        if (movie == null || findMovie(movie.getMovieId()) != null) throw new IllegalArgumentException("영화가 없거나 ID 중복");
        movies.add(movie);
    }
    public Movie findMovie(int id) {
        for (Movie movie : movies) if (movie.getMovieId() == id) return movie;
        return null;
    }
    public List<Movie> getMovies() { return Collections.unmodifiableList(movies); }
    public void updateMovie(int id, String title, int runningTime, String genre) {
        Movie movie = findMovie(id);
        if (movie == null) throw new IllegalArgumentException("영화를 찾을 수 없습니다.");
        if (title == null || title.isBlank() || runningTime <= 0) throw new IllegalArgumentException("수정 값 오류");
        // 러닝타임 변경으로 기존 상영 간 충돌이 생기는지 먼저 검사
        for (Screening current : screenings) {
            if (current.getMovie() != movie) continue;
            LocalDateTime proposedEnd = current.getStartTime().plusMinutes(runningTime);
            for (Screening other : screenings) {
                if (other == current || other.getTheater().getTheaterId() != current.getTheater().getTheaterId()) continue;
                if (current.getStartTime().isBefore(other.getEndTime()) && other.getStartTime().isBefore(proposedEnd))
                    throw new IllegalArgumentException("러닝타임 변경 시 상영 시간이 겹칩니다.");
            }
        }
        movie.setTitle(title);
        movie.setRunningTime(runningTime);
        movie.setGenre(genre);
    }
    public void removeMovie(int id) {
        Movie movie = findMovie(id);
        if (movie == null) throw new IllegalArgumentException("영화가 없습니다.");
        for (Screening screening : screenings) if (screening.getMovie() == movie)
            throw new IllegalStateException("상영 일정이 있는 영화는 삭제할 수 없습니다.");
        movies.remove(movie);
    }
    public void addTheater(Theater theater) {
        if (theater == null || findTheater(theater.getTheaterId()) != null)
            throw new IllegalArgumentException("상영관 정보 오류 또는 ID 중복");
        theaters.add(theater);
    }
    public Theater findTheater(int id) {
        for (Theater theater : theaters) if (theater.getTheaterId() == id) return theater;
        return null;
    }
    public List<Theater> getTheaters() { return Collections.unmodifiableList(theaters); }
    public void removeTheater(int id) {
        Theater theater = findTheater(id);
        if (theater == null) throw new IllegalArgumentException("상영관이 없습니다.");
        for (Screening screening : screenings)
            if (screening.getTheater().getTheaterId() == id)
                throw new IllegalStateException("상영 일정이 있는 상영관은 삭제할 수 없습니다.");
        theaters.remove(theater);
    }
    public void addScreening(Screening screening) {
        if (screening == null || findScreening(screening.getScreeningId()) != null) throw new IllegalArgumentException("상영 일정이 없거나 ID 중복");
        if (findMovie(screening.getMovie().getMovieId()) != screening.getMovie()) throw new IllegalArgumentException("등록된 영화 객체가 아닙니다.");
        if (findTheater(screening.getTheater().getTheaterId()) != screening.getTheater())
            throw new IllegalArgumentException("등록된 상영관 객체가 아닙니다.");
        checkConflict(screening, -1);
        screenings.add(screening);
    }
    private void checkConflict(Screening candidate, int excludeId) {
        for (Screening existing : screenings) {
            if (existing.getScreeningId() == excludeId || existing.getTheater().getTheaterId() != candidate.getTheater().getTheaterId()) continue;
            if (candidate.getStartTime().isBefore(existing.getEndTime()) && existing.getStartTime().isBefore(candidate.getEndTime()))
                throw new IllegalArgumentException("같은 상영관에서 상영 시간이 겹칩니다.");
        }
    }
    public Screening findScreening(int id) {
        for (Screening screening : screenings) if (screening.getScreeningId() == id) return screening;
        return null;
    }
    public List<Screening> getScreenings(int movieId) {
        List<Screening> result = new ArrayList<>();
        for (Screening screening : screenings) if (screening.getMovie().getMovieId() == movieId) result.add(screening);
        return result;
    }
    public List<Screening> getAllScreenings() { return Collections.unmodifiableList(screenings); }
    public void updateScreeningTime(int id, LocalDateTime time) {
        Screening screening = findScreening(id);
        if (screening == null || time == null) throw new IllegalArgumentException("상영 일정 또는 시간이 잘못되었습니다.");
        LocalDateTime old = screening.getStartTime();
        screening.setStartTime(time);
        try { checkConflict(screening, id); }
        catch (RuntimeException e) { screening.setStartTime(old); throw e; }
    }
    public void removeScreening(int id) {
        Screening screening = findScreening(id);
        if (screening == null) throw new IllegalArgumentException("상영 일정이 없습니다.");
        // 예약 여부는 B팀 ReservationService와 통합 후 확인해야 함
        screenings.remove(screening);
    }
}
