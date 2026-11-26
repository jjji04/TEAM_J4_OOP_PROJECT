package com.cinema.pricing;

public class AdultTicket extends Ticket {
    @Override public int getBasePrice() { return 15000; }
    @Override public String getType() { return "성인"; }
}
