package com.cinema.member;

/**
 * 회원. [캡슐화] 필드는 전부 private, 상태 변경은 메서드로만.
 * - id는 생성 후 바뀌지 않으므로 final
 * - 포인트는 외부에서 setPoints()로 마음대로 못 바꾸고, earn/use 로만 변경 → 음수 포인트 같은 잘못된 상태 방지
 */
public class Member {
    private final String id;        // 예: U001
    private String name;
    private String phone;
    private int points;             // 현재 사용 가능 포인트
    private int totalEarnedPoints;  // 누적 적립 포인트 (등급 판정용, 사용해도 줄지 않음)
    private MembershipGrade grade;

    public Member(String id, String name, String phone) {
        // TODO: id/name/phone null·빈 문자열 검사 → IllegalArgumentException
        this.id = id;
        this.name = name;
        this.phone = phone;
        this.points = 0;
        this.totalEarnedPoints = 0;
        this.grade = MembershipGrade.BASIC;
    }

    // ===== 포인트·등급 =====

    /** 결제 후 포인트 적립 + 등급 갱신 */
    public void earnPoints(int paidAmount) {
        // TODO: paidAmount <= 0 이면 무시 또는 예외
        // TODO: int earned = grade.calculatePoints(paidAmount);
        //       points += earned; totalEarnedPoints += earned;
        //       updateGrade();
    }

    /** 포인트 사용 (결제 시 차감) */
    public void usePoints(int amount) {
        // TODO: amount <= 0 또는 amount > points 이면 IllegalArgumentException
        // TODO: points -= amount;
    }

    private void updateGrade() {
        // TODO: grade = MembershipGrade.of(totalEarnedPoints);
    }

    // ===== 정보 수정 =====

    public void changeName(String newName) {
        // TODO: 검증 후 변경
        this.name = newName;
    }

    public void changePhone(String newPhone) {
        // TODO: 형식 검증(010-0000-0000 등) 후 변경
        this.phone = newPhone;
    }

    /** 로그인 확인용. 비밀번호 대신 전화번호로 본인 확인 (⚠️ 팀 확정 필요) */
    public boolean matchesPhone(String phone) {
        return this.phone.equals(phone);
    }

    // ===== getter (setter는 일부러 안 만듦) =====

    public String getId() { return id; }
    public String getName() { return name; }
    public String getPhone() { return phone; }
    public int getPoints() { return points; }
    public int getTotalEarnedPoints() { return totalEarnedPoints; }
    public MembershipGrade getGrade() { return grade; }

    @Override
    public String toString() {
        return String.format("[%s] %s (%s) %s / %,dP", id, name, phone, grade, points);
    }
}
