package com.cinema.member;

/**
 * 회원 등급. 등급마다 "필요 누적 포인트"와 "포인트 적립률"을 가진다.
 *
 * 설계 메모: 회원 유형을 상속(VipMember extends Member)으로 나누지 않고 enum으로 처리.
 * 등급별로 '동작'이 달라지는 게 아니라 '숫자(기준·비율)'만 다르기 때문.
 * → 등급이 바뀌어도 객체를 새로 만들 필요가 없음 (상속이었다면 VIP 승급 시 객체 교체 필요)
 */
public enum MembershipGrade {
    // TODO: 기준 포인트·적립률은 팀에서 확정
    BASIC(0, 0.01),
    SILVER(5_000, 0.03),
    GOLD(15_000, 0.05);

    private final int requiredPoints;   // 이 등급이 되기 위한 최소 누적 포인트
    private final double pointRate;     // 결제 금액 대비 적립률

    MembershipGrade(int requiredPoints, double pointRate) {
        this.requiredPoints = requiredPoints;
        this.pointRate = pointRate;
    }

    public int getRequiredPoints() { return requiredPoints; }
    public double getPointRate() { return pointRate; }

    /** 결제 금액에 대한 적립 포인트 계산 */
    public int calculatePoints(int paidAmount) {
        return (int) (paidAmount * pointRate);
    }

    /** 누적 포인트로 등급 판정 (높은 등급부터 확인) */
    public static MembershipGrade of(int totalEarnedPoints) {
        // TODO: values()를 뒤에서부터 돌면서 requiredPoints 이상인 첫 등급 반환
        return BASIC;
    }
}
