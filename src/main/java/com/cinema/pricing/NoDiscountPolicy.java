package com.cinema.pricing;

public class NoDiscountPolicy implements DiscountPolicy {
    @Override public int discount(int basePrice) { return 0; }
}
