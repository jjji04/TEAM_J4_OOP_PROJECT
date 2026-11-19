package com.cinema.member;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

/**
 * 프로그램 데이터를 CSV 파일로 저장·불러오기 (D 담당).
 *
 * 팀 규칙: "시작할 때 불러오고, 끝날 때 한 번에 저장" → 다른 파트 코드는 저장을 신경 쓰지 않는다.
 * Main(app)에서는 아래 세 줄만 쓰면 된다.
 *
 *   DataManager data = new DataManager("data", memberManager);
 *   data.loadAll();           // 시작할 때
 *   data.enableAutoSave();    // 창을 닫거나 Ctrl+C로 꺼도 저장
 *   ...
 *   data.saveAll();           // "저장" 메뉴 / 정상 종료 시
 *
 * 왜 member 패키지에 있나: 비밀번호 해시처럼 public으로 열면 안 되는 값을
 * package-private으로 꺼내 저장하기 위해 (캡슐화 유지).
 */
public class DataManager {
    private static final String MEMBER_FILE = "members.csv";
    private static final String MEMBER_HEADER = "id,name,phone,passwordHash,points,totalPaidAmount";

    private final Path dataDir;
    private final MemberManager memberManager;
    private boolean autoSaveEnabled = false;

    public DataManager(String dataDir, MemberManager memberManager) {
        if (memberManager == null) {
            throw new IllegalArgumentException("MemberManager가 필요합니다.");
        }
        this.dataDir = Path.of(dataDir);
        this.memberManager = memberManager;
    }

    // ===================== 전체 =====================

    /** 모든 데이터 저장. 실패해도 프로그램은 죽지 않고 메시지만 출력 */
    public synchronized boolean saveAll() {
        try {
            Files.createDirectories(dataDir);
            saveMembers();
            // TODO: 예약 저장 — A(Screening)·C(Ticket) 코드가 올라오면 추가
            return true;
        } catch (IOException e) {
            System.out.println("[저장 실패] " + e.getMessage());
            return false;
        }
    }

    /** 모든 데이터 불러오기. 파일이 없으면(첫 실행) 그냥 빈 상태로 시작 */
    public synchronized void loadAll() {
        try {
            loadMembers();
            // TODO: 예약 불러오기 — 회원·상영 정보를 먼저 불러온 뒤에 해야 함
        } catch (IOException e) {
            System.out.println("[불러오기 실패] " + e.getMessage());
        }
    }

    /** 창을 그냥 닫거나 Ctrl+C로 꺼도 저장되게 (JVM 종료 직전에 saveAll 실행) */
    public void enableAutoSave() {
        if (autoSaveEnabled) return;
        Runtime.getRuntime().addShutdownHook(new Thread(this::saveAll));
        autoSaveEnabled = true;
    }

    // ===================== 회원 =====================

    private void saveMembers() throws IOException {
        List<String> lines = new ArrayList<>();
        lines.add(MEMBER_HEADER);
        for (Member m : memberManager.findAll()) {
            lines.add(CsvUtil.toLine(
                    m.getId(),
                    m.getName(),
                    m.getPhone(),
                    m.getPasswordHash(),
                    String.valueOf(m.getPoints()),
                    String.valueOf(m.getTotalPaidAmount())));
        }
        writeSafely(dataDir.resolve(MEMBER_FILE), lines);
    }

    private void loadMembers() throws IOException {
        Path file = dataDir.resolve(MEMBER_FILE);
        if (!Files.exists(file)) {
            return; // 첫 실행
        }
        memberManager.clear();
        List<String> lines = Files.readAllLines(file, StandardCharsets.UTF_8);
        int loaded = 0;
        for (int i = 1; i < lines.size(); i++) { // 0번 줄은 제목(header)
            String line = lines.get(i);
            if (line.isBlank()) continue;
            try {
                List<String> f = CsvUtil.parseLine(line);
                if (f.size() != 6) {
                    throw new IllegalArgumentException("칸 수가 6개가 아닙니다 (" + f.size() + "개)");
                }
                Member m = Member.restore(
                        f.get(0), f.get(1), f.get(2), f.get(3),
                        Integer.parseInt(f.get(4)),
                        Long.parseLong(f.get(5)));
                memberManager.restore(m);
                loaded++;
            } catch (RuntimeException e) {
                // 깨진 줄 하나 때문에 전체가 안 열리면 안 되므로, 건너뛰고 경고만
                System.out.println("[경고] " + MEMBER_FILE + " " + (i + 1) + "번째 줄 건너뜀: " + e.getMessage());
            }
        }
        System.out.println("회원 " + loaded + "명 불러옴");
    }

    // ===================== 공통 =====================

    /**
     * 임시 파일에 먼저 다 쓴 뒤 원래 파일과 교체.
     * 저장 도중 꺼져도 기존 파일이 반쯤 지워진 상태로 남지 않는다.
     */
    private void writeSafely(Path target, List<String> lines) throws IOException {
        Path temp = target.resolveSibling(target.getFileName() + ".tmp");
        Files.write(temp, lines, StandardCharsets.UTF_8);
        try {
            Files.move(temp, target, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (IOException e) {
            Files.move(temp, target, StandardCopyOption.REPLACE_EXISTING); // 원자적 교체 미지원 시
        }
    }
}
