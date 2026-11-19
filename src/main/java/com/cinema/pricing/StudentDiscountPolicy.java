package com.cinema.pricing;

public class StudentDiscountPolicy implements DiscountPolicy {
    @Override public int discount(int basePrice) { return basePrice / 10; }
}
