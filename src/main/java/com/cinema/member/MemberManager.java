package com.cinema.member;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 회원 가입·로그인·조회·수정 담당 (D). [SRP] 회원 '관리'만 하고, 파일 저장은 하지 않는다.
 * (회원 파일 저장이 필요해지면 저장소 인터페이스를 따로 빼서 주입 — repository 패키지 규칙은 팀 협의 후)
 */
public class MemberManager {
    private final Map<String, Member> members = new LinkedHashMap<>(); // id → Member, 가입 순서 유지
    private int nextNumber = 1;

    /** 회원 가입. ID는 U001, U002 ... 자동 발급 */
    public Member register(String name, String phone) {
        // TODO: 같은 전화번호로 이미 가입했는지 확인 → 있으면 예외
        String id = String.format("U%03d", nextNumber++);
        Member member = new Member(id, name, phone);
        members.put(id, member);
        return member;
    }

    /** ID로 회원 찾기. 없으면 MemberNotFoundException */
    public Member findById(String id) {
        Member member = members.get(id);
        if (member == null) {
            throw new MemberNotFoundException(id);
        }
        return member;
    }

    /** 로그인: ID + 전화번호가 맞으면 회원 반환 */
    public Member login(String id, String phone) {
        // TODO: findById(id) 후 matchesPhone(phone) 확인, 틀리면 예외(또는 null — 팀 규칙 따름)
        return null;
    }

    public List<Member> findAll() {
        return new ArrayList<>(members.values()); // 내부 Map을 그대로 노출하지 않고 복사본 반환 (캡슐화)
    }

    public void updatePhone(String id, String newPhone) {
        // TODO: findById(id).changePhone(newPhone);
    }
}
