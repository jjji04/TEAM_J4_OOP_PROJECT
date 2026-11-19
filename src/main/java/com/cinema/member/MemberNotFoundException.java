package com.cinema.member;

/**
 * 없는 회원 ID로 조회했을 때 발생.
 * TODO: 팀 공통 예외(common 패키지)가 정해지면 그 부모 클래스를 상속하도록 변경
 */
public class MemberNotFoundException extends RuntimeException {
    public MemberNotFoundException(String memberId) {
        super("존재하지 않는 회원입니다: " + memberId);
    }
}
