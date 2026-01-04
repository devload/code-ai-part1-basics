package com.aiprocess.step6;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 출력 포맷터
 *
 * 생성된 텍스트를 깔끔하게 정리합니다.
 */
public class OutputFormatter {

    /**
     * 텍스트 정리
     */
    public String format(String text) {
        if (text == null || text.isEmpty()) {
            return "";
        }

        String result = text;

        // 1. 연속 공백 제거
        result = result.replaceAll("\\s+", " ");

        // 2. 문장 부호 앞 공백 제거
        result = result.replaceAll("\\s+([.,!?;:])", "$1");

        // 3. 시작/끝 공백 제거
        result = result.trim();

        // 4. 문장 시작 대문자 (영어)
        result = capitalizeFirstLetter(result);

        return result;
    }

    /**
     * 코드 포맷팅
     */
    public String formatCode(String code) {
        if (code == null || code.isEmpty()) {
            return "";
        }

        // 들여쓰기 정규화
        String[] lines = code.split("\n");
        StringBuilder sb = new StringBuilder();

        int indentLevel = 0;
        for (String line : lines) {
            String trimmed = line.trim();

            // 닫는 괄호로 시작하면 들여쓰기 감소
            if (trimmed.startsWith("}") || trimmed.startsWith(")")) {
                indentLevel = Math.max(0, indentLevel - 1);
            }

            // 들여쓰기 적용
            sb.append("    ".repeat(indentLevel));
            sb.append(trimmed);
            sb.append("\n");

            // 여는 괄호로 끝나면 들여쓰기 증가
            if (trimmed.endsWith("{") || trimmed.endsWith("(")) {
                indentLevel++;
            }
        }

        return sb.toString().trim();
    }

    /**
     * JSON 포맷팅 (간단한 버전)
     */
    public String formatJson(String json) {
        if (json == null || json.isEmpty()) {
            return "";
        }

        StringBuilder sb = new StringBuilder();
        int indent = 0;
        boolean inString = false;

        for (int i = 0; i < json.length(); i++) {
            char c = json.charAt(i);

            if (c == '"' && (i == 0 || json.charAt(i - 1) != '\\')) {
                inString = !inString;
            }

            if (!inString) {
                switch (c) {
                    case '{', '[' -> {
                        sb.append(c);
                        sb.append("\n");
                        indent++;
                        sb.append("  ".repeat(indent));
                    }
                    case '}', ']' -> {
                        sb.append("\n");
                        indent--;
                        sb.append("  ".repeat(indent));
                        sb.append(c);
                    }
                    case ',' -> {
                        sb.append(c);
                        sb.append("\n");
                        sb.append("  ".repeat(indent));
                    }
                    case ':' -> sb.append(": ");
                    case ' ', '\n', '\t' -> {} // 기존 공백 무시
                    default -> sb.append(c);
                }
            } else {
                sb.append(c);
            }
        }

        return sb.toString();
    }

    private String capitalizeFirstLetter(String text) {
        if (text.isEmpty()) return text;
        return Character.toUpperCase(text.charAt(0)) + text.substring(1);
    }
}
