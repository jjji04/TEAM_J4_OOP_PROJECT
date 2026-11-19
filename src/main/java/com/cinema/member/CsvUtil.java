package com.cinema.member;

import java.util.ArrayList;
import java.util.List;

/**
 * CSV 한 줄 ↔ 문자열 목록 변환.
 * 이름에 쉼표(,)나 따옴표(")가 들어가도 깨지지 않도록 따옴표로 감싸는 표준 CSV 규칙을 따른다.
 *   예) 홍길동,"김,철수"  →  [홍길동, 김,철수]
 */
final class CsvUtil {
    private CsvUtil() { } // 객체 생성 금지 (static 메서드만 쓰는 도구 클래스)

    /** 값 목록 → CSV 한 줄 */
    static String toLine(String... values) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < values.length; i++) {
            if (i > 0) sb.append(',');
            sb.append(escape(values[i]));
        }
        return sb.toString();
    }

    private static String escape(String value) {
        if (value == null) return "";
        if (value.contains(",") || value.contains("\"") || value.contains("\n") || value.contains("\r")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }

    /** CSV 한 줄 → 값 목록 */
    static List<String> parseLine(String line) {
        List<String> fields = new ArrayList<>();
        StringBuilder cur = new StringBuilder();
        boolean inQuotes = false;
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (inQuotes) {
                if (c == '"') {
                    if (i + 1 < line.length() && line.charAt(i + 1) == '"') { // "" → "
                        cur.append('"');
                        i++;
                    } else {
                        inQuotes = false;
                    }
                } else {
                    cur.append(c);
                }
            } else if (c == '"') {
                inQuotes = true;
            } else if (c == ',') {
                fields.add(cur.toString());
                cur.setLength(0);
            } else {
                cur.append(c);
            }
        }
        if (inQuotes) {
            throw new IllegalArgumentException("따옴표가 닫히지 않았습니다.");
        }
        fields.add(cur.toString());
        return fields;
    }
}
