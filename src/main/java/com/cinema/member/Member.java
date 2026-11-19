package com.cinema.member;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

/**
 * 회원. [캡슐화] 필드는 전부 private, 상태 변경은 메서드로만.
 * - id는 생성 후 바뀌지 않으므로 final
 * - 포인트는 외부에서 setPoints()로 마음대로 못 바꾸고, recordPayment/usePoints 로만 변경 → 음수 포인트 같은 잘못된 상태 방지
 * - 비밀번호는 원문을 저장하지 않고 SHA-256 해시만 보관, getter도 없음 → 외부에선 matchesPassword()로 확인만 가능
 */
public class Member {
    private static final int MIN_PASSWORD_LENGTH = 4;

    private final String id;        // 예: U001
    private String name;
    private String phone;
    private String passwordHash;    // 비밀번호 해시 (원문 X)
    private int points;             // 현재 사용 가능 포인트
    private long totalPaidAmount;   // 누적 결제금액 (등급 판정용)
    private MembershipGrade grade;

    public Member(String id, String name, String phone, String password) {
        this.id = requireText(id, "회원 ID");
        this.name = requireText(name, "이름");
        this.phone = requirePhone(phone);
        this.passwordHash = hash(requirePassword(password));
        this.points = 0;
        this.totalPaidAmount = 0;
        this.grade = MembershipGrade.BASIC;
    }

    // ===== 결제·포인트·등급 =====

    /**
     * 결제 기록: 포인트 적립 + 누적 결제금액 증가 + 등급 갱신.
     * 결제 담당(C)이 결제 완료 후 호출. 적립된 포인트를 반환
     */
    public int recordPayment(int paidAmount) {
        if (paidAmount <= 0) {
            throw new IllegalArgumentException("결제 금액은 0보다 커야 합니다: " + paidAmount);
        }
        int earned = grade.calculatePoints(paidAmount); // 결제 시점 등급의 적립률로 계산
        points += earned;
        totalPaidAmount += paidAmount;
        updateGrade();                                  // 결제 후 등급 다시 판정
        return earned;
    }

    /** 포인트 사용 (결제 시 차감). 써도 누적 결제금액은 그대로라 등급 유지 */
    public void usePoints(int amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("사용할 포인트는 0보다 커야 합니다: " + amount);
        }
        if (amount > points) {
            throw new IllegalArgumentException("포인트가 부족합니다. (보유 " + points + "P, 요청 " + amount + "P)");
        }
        points -= amount;
    }

    private void updateGrade() {
        grade = MembershipGrade.of(totalPaidAmount);
    }

    // ===== 정보 수정·확인 =====

    public void changeName(String newName) {
        this.name = requireText(newName, "이름");
    }

    public void changePhone(String newPhone) {
        this.phone = requirePhone(newPhone);
    }

    /** 현재 비밀번호를 맞혀야 변경 가능 */
    public void changePassword(String currentPassword, String newPassword) {
        if (!matchesPassword(currentPassword)) {
            throw new IllegalArgumentException("현재 비밀번호가 올바르지 않습니다.");
        }
        this.passwordHash = hash(requirePassword(newPassword));
    }

    public boolean matchesPassword(String password) {
        return password != null && passwordHash.equals(hash(password));
    }

    public boolean matchesPhone(String phone) {
        return this.phone.equals(phone);
    }

    // ===== 입력값 검사·해시 (내부 전용) =====

    private static String requireText(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + "을(를) 입력해 주세요.");
        }
        return value.trim();
    }

    /** 010-1234-5678 형식만 허용 */
    private static String requirePhone(String phone) {
        String value = requireText(phone, "전화번호");
        if (!value.matches("010-\\d{4}-\\d{4}")) {
            throw new IllegalArgumentException("전화번호는 010-0000-0000 형식이어야 합니다: " + value);
        }
        return value;
    }

    private static String requirePassword(String password) {
        if (password == null || password.length() < MIN_PASSWORD_LENGTH) {
            throw new IllegalArgumentException("비밀번호는 " + MIN_PASSWORD_LENGTH + "자 이상이어야 합니다.");
        }
        return password;
    }

    private static String hash(String text) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(text.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256을 사용할 수 없습니다.", e); // 표준 JDK엔 항상 있음
        }
    }

    // ===== getter (setter·비밀번호 getter는 일부러 안 만듦) =====

    public String getId() { return id; }
    public String getName() { return name; }
    public String getPhone() { return phone; }
    public int getPoints() { return points; }
    public long getTotalPaidAmount() { return totalPaidAmount; }
    public MembershipGrade getGrade() { return grade; }

    @Override
    public String toString() {
        return String.format("[%s] %s (%s) %s / %,dP", id, name, phone, grade, points);
    }
}
