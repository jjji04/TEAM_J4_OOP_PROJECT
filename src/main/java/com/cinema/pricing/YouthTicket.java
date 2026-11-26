package com.cinema.pricing;

public class YouthTicket extends Ticket {
    @Override public int getBasePrice() { return 12000; }
    @Override public String getType() { return "청소년"; }
}
