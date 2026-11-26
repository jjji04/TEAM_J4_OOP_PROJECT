package com.cinema.pricing;
public class PaymentService {
    public void pay(int amount) { if(amount <= 0) throw new IllegalArgumentException("결제 금액 오류"); }
}
