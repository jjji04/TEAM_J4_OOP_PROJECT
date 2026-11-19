package com.cinema.pricing;

import java.util.Objects;

public class PriceService {
    private final DiscountPolicy discountPolicy;

    public PriceService() {
        this(new NoDiscountPolicy());
    }

    public PriceService(DiscountPolicy discountPolicy) {
        this.discountPolicy = Objects.requireNonNull(discountPolicy, "discountPolicy");
    }

    public int calculatePrice(Ticket ticket) {
        Objects.requireNonNull(ticket, "ticket");
        int basePrice = ticket.getBasePrice();
        if (basePrice < 0) throw new IllegalArgumentException("기본 요금은 음수일 수 없습니다.");
        int discount = discountPolicy.discount(basePrice);
        if (discount < 0 || discount > basePrice) {
            throw new IllegalArgumentException("할인 금액이 올바르지 않습니다.");
        }
        return basePrice - discount;
    }
}
