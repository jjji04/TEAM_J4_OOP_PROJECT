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

    /** 회원 가입. ID는 U001, U002 ... 자동 발급. 같은 전화번호로는 중복 가입 불가 */
    public Member register(String name, String phone, String password) {
        if (findByPhone(phone) != null) {
            throw new IllegalArgumentException("이미 가입된 전화번호입니다: " + phone);
        }
        String id = String.format("U%03d", nextNumber);
        Member member = new Member(id, name, phone, password); // 형식이 잘못되면 여기서 예외 → 번호는 소모 안 됨
        nextNumber++;
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

    /** 로그인: ID + 비밀번호가 맞으면 회원 반환, 틀리면 예외 */
    public Member login(String id, String password) {
        Member member = members.get(id);
        // 보안상 "ID가 없다 / 비밀번호가 틀렸다"를 구분하지 않고 같은 메시지
        if (member == null || !member.matchesPassword(password)) {
            throw new IllegalArgumentException("회원 ID 또는 비밀번호가 올바르지 않습니다.");
        }
        return member;
    }

    public List<Member> findAll() {
        return new ArrayList<>(members.values()); // 내부 Map을 그대로 노출하지 않고 복사본 반환 (캡슐화)
    }

    public void updatePhone(String id, String newPhone) {
        Member member = findById(id);
        Member owner = findByPhone(newPhone);
        if (owner != null && owner != member) {
            throw new IllegalArgumentException("이미 다른 회원이 사용하는 전화번호입니다: " + newPhone);
        }
        member.changePhone(newPhone);
    }

    /**
     * 파일에서 불러온 회원 등록 (DataManager 전용, package-private).
     * 다음 가입자 번호가 겹치지 않도록 nextNumber를 가장 큰 번호 + 1로 맞춘다
     */
    void restore(Member member) {
        if (members.containsKey(member.getId())) {
            throw new IllegalArgumentException("중복된 회원 ID: " + member.getId());
        }
        if (findByPhone(member.getPhone()) != null) {
            throw new IllegalArgumentException("중복된 전화번호: " + member.getPhone());
        }
        members.put(member.getId(), member);
        String digits = member.getId().replaceAll("\\D", "");
        if (!digits.isEmpty()) {
            nextNumber = Math.max(nextNumber, Integer.parseInt(digits) + 1);
        }
    }

    /** 불러오기 전에 기존 데이터 비우기 (DataManager 전용) */
    void clear() {
        members.clear();
        nextNumber = 1;
    }

    /** 전화번호로 회원 찾기. 없으면 null (내부 중복 검사용) */
    private Member findByPhone(String phone) {
        for (Member m : members.values()) {
            if (m.matchesPhone(phone)) {
                return m;
            }
        }
        return null;
    }
}
