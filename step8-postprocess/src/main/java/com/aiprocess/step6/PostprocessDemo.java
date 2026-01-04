package com.aiprocess.step6;

/**
 * STEP 6: 후처리 데모
 *
 * AI가 왜 가끔 거절하는지, 출력이 어떻게 정리되는지 시각화합니다.
 */
public class PostprocessDemo {

    public static void main(String[] args) {
        System.out.println("═".repeat(60));
        System.out.println("STEP 6: 후처리 (Post-processing)");
        System.out.println("═".repeat(60));
        System.out.println();
        System.out.println("핵심 질문: 왜 가끔 거절하는가?");
        System.out.println();

        // 1. 안전 필터 데모
        demoSafetyFilter();

        System.out.println();

        // 2. 개인정보 마스킹 데모
        demoPIIMasking();

        System.out.println();

        // 3. 출력 포맷팅 데모
        demoOutputFormatter();
    }

    private static void demoSafetyFilter() {
        System.out.println("─".repeat(60));
        System.out.println("1. 안전 필터 (Safety Filter)");
        System.out.println("─".repeat(60));
        System.out.println();

        SafetyFilter filter = new SafetyFilter();

        String[] testCases = {
            "오늘 날씨가 정말 좋네요!",
            "제 이메일은 test@example.com 입니다.",
            "연락처: 010-1234-5678",
            "좋다 좋다 좋다 좋다 좋다 좋다 계속 반복"
        };

        System.out.println("  ┌─────────────────────────────────────────┐");
        System.out.println("  │ AI 출력 검사 과정                        │");
        System.out.println("  └─────────────────────────────────────────┘");
        System.out.println();

        for (String text : testCases) {
            SafetyFilter.FilterResult result = filter.check(text);
            String status = result.passed() ? "✅ PASS" : "❌ BLOCK";

            System.out.println("  입력: \"" + truncate(text, 40) + "\"");
            System.out.println("  결과: " + status);
            if (!result.passed()) {
                for (String issue : result.issues()) {
                    System.out.println("    - " + issue);
                }
            }
            System.out.println();
        }
    }

    private static void demoPIIMasking() {
        System.out.println("─".repeat(60));
        System.out.println("2. 개인정보 마스킹 (PII Masking)");
        System.out.println("─".repeat(60));
        System.out.println();

        SafetyFilter filter = new SafetyFilter();

        String original = "제 이메일은 user@example.com이고 전화번호는 010-9876-5432입니다.";
        String masked = filter.maskPII(original);

        System.out.println("  원본:");
        System.out.println("    \"" + original + "\"");
        System.out.println();
        System.out.println("  마스킹 후:");
        System.out.println("    \"" + masked + "\"");
        System.out.println();

        System.out.println("  ┌─────────────────────────────────────────┐");
        System.out.println("  │ 왜 마스킹이 필요한가?                    │");
        System.out.println("  │                                          │");
        System.out.println("  │ - 학습 데이터에 개인정보가 포함될 수 있음 │");
        System.out.println("  │ - AI가 실제 개인정보를 출력하는 것 방지   │");
        System.out.println("  │ - GDPR, 개인정보보호법 준수               │");
        System.out.println("  └─────────────────────────────────────────┘");
    }

    private static void demoOutputFormatter() {
        System.out.println("─".repeat(60));
        System.out.println("3. 출력 포맷팅 (Output Formatting)");
        System.out.println("─".repeat(60));
        System.out.println();

        OutputFormatter formatter = new OutputFormatter();

        // 텍스트 정리
        System.out.println("  [텍스트 정리]");
        String messyText = "hello   world  .  how   are   you ?";
        String cleanText = formatter.format(messyText);
        System.out.println("    원본: \"" + messyText + "\"");
        System.out.println("    정리: \"" + cleanText + "\"");
        System.out.println();

        // 코드 포맷팅
        System.out.println("  [코드 포맷팅]");
        String messyCode = "public class User { private String name; public void setName(String name) { this.name = name; } }";
        String formattedCode = formatter.formatCode(messyCode);
        System.out.println("    원본: " + messyCode);
        System.out.println("    정리:");
        for (String line : formattedCode.split("\n")) {
            System.out.println("      " + line);
        }
        System.out.println();

        // JSON 포맷팅
        System.out.println("  [JSON 포맷팅]");
        String messyJson = "{\"name\":\"John\",\"age\":30,\"city\":\"Seoul\"}";
        String formattedJson = formatter.formatJson(messyJson);
        System.out.println("    원본: " + messyJson);
        System.out.println("    정리:");
        for (String line : formattedJson.split("\n")) {
            System.out.println("      " + line);
        }
    }

    private static String truncate(String text, int maxLen) {
        if (text.length() <= maxLen) return text;
        return text.substring(0, maxLen - 3) + "...";
    }
}
