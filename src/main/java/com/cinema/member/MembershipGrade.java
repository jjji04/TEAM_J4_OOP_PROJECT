package com.cinema.member;

/**
 * 회원 등급. 등급마다 "승급 기준 누적 결제금액"과 "포인트 적립률"을 가진다.
 *
 * 설계 메모: 회원 유형을 상속(VipMember extends Member)으로 나누지 않고 enum으로 처리.
 * 등급별로 '동작'이 달라지는 게 아니라 '숫자(기준·비율)'만 다르기 때문.
 * → 등급이 바뀌어도 객체를 새로 만들 필요가 없음 (상속이었다면 VIP 승급 시 객체 교체 필요)
 */
public enum MembershipGrade {
    BASIC(0, 0.01),         // 가입 시
    SILVER(100_000, 0.03),  // 누적 결제 10만 원 이상
    GOLD(300_000, 0.05);    // 누적 결제 30만 원 이상

    private final int requiredAmount;   // 이 등급이 되기 위한 최소 누적 결제금액(원)
    private final double pointRate;     // 결제 금액 대비 포인트 적립률

    MembershipGrade(int requiredAmount, double pointRate) {
        this.requiredAmount = requiredAmount;
        this.pointRate = pointRate;
    }

    public int getRequiredAmount() { return requiredAmount; }
    public double getPointRate() { return pointRate; }

    /** 결제 금액에 대한 적립 포인트 계산 */
    public int calculatePoints(int paidAmount) {
        return (int) (paidAmount * pointRate);
    }

    /** 누적 결제금액으로 등급 판정 (높은 등급부터 확인) */
    public static MembershipGrade of(long totalPaidAmount) {
        MembershipGrade[] grades = values();          // [BASIC, SILVER, GOLD] (선언 순서)
        for (int i = grades.length - 1; i >= 0; i--) { // GOLD → SILVER → BASIC
            if (totalPaidAmount >= grades[i].requiredAmount) {
                return grades[i];
            }
        }
        return BASIC; // 음수 등 예외 상황 대비
    }
}
