package com.cinema.reservation;
import com.cinema.movie.*; import com.cinema.member.*; import com.cinema.pricing.*; import java.util.*;
public class ReservationService {
 private final List<Reservation> reservations = new ArrayList<>();
 public Set<String> occupied(int id){Set<String> s=new HashSet<>(); for(Reservation r:reservations) if(r.isActive()&&r.getScreening().getScreeningId()==id) for(Seat seat:r.getSeats()) s.add(seat.getSeatId()); return s;}
 public Reservation book(Member m, Screening sc, List<String> ids, boolean youth, boolean student){
 return book(m,sc,ids,Collections.nCopies(ids.size(),youth?"youth":"adult"),Collections.nCopies(ids.size(),student?"yes":"no")); }
 public synchronized Reservation book(Member m, Screening sc, List<String> ids, List<String> types, List<String> discounts){
 if(types.size()!=ids.size()||discounts.size()!=ids.size()) throw new IllegalArgumentException("좌석별 티켓 정보 오류");
 if(sc==null||ids.isEmpty()||new HashSet<>(ids).size()!=ids.size()) throw new IllegalArgumentException("좌석 선택 오류");
 List<Seat> seats=new ArrayList<>(); List<Ticket> tickets=new ArrayList<>(); int total=0; Set<String> used=occupied(sc.getScreeningId());
 for(int i=0;i<ids.size();i++){String id=ids.get(i); String type=types.get(i), discount=discounts.get(i); if(!List.of("adult","youth").contains(type)||!List.of("yes","no").contains(discount)) throw new IllegalArgumentException("티켓 또는 할인 정보 오류");if(!sc.getTheater().hasSeat(id)||used.contains(id)) throw new IllegalArgumentException("예약 불가 좌석: "+id); seats.add(new Seat(id,id.charAt(0)-'A'+1,Integer.parseInt(id.substring(1)))); Ticket t=type.equals("youth")?new YouthTicket():new AdultTicket(); tickets.add(t); total+=new PriceService(discount.equals("yes")?new StudentDiscountPolicy():new NoDiscountPolicy()).calculatePrice(t);}
 new PaymentService().pay(total); Reservation r=new Reservation(reservations.size()+1,m,sc,seats,tickets,total); m.recordPayment(total); reservations.add(r); return r; }
 public List<Reservation> history(Member m){return reservations.stream().filter(r->r.getMember()==m).toList();}
 public void cancel(Member m,int id){history(m).stream().filter(r->r.getReservationId()==id).findFirst().orElseThrow(()->new IllegalArgumentException("예약 없음")).cancel();}
 public boolean hasHistory(int id){return reservations.stream().anyMatch(r->r.getScreening().getScreeningId()==id);}
}
