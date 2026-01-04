package com.aiprocess.step1;

import java.util.List;

/**
 * STEP 1: 토큰화 데모
 *
 * AI가 문장을 처리하는 첫 번째 단계를 시각화합니다.
 */
public class TokenizationDemo {

    public static void main(String[] args) {
        System.out.println("═".repeat(60));
        System.out.println("STEP 1: 토큰화 (Tokenization)");
        System.out.println("═".repeat(60));
        System.out.println();
        System.out.println("핵심 질문: 왜 문장을 조각내는가?");
        System.out.println();
        System.out.println("AI는 문장을 그대로 이해하지 못합니다.");
        System.out.println("문장을 '토큰'이라는 작은 조각으로 나누어야 처리할 수 있습니다.");
        System.out.println();

        // 1. 일반 텍스트 토큰화
        demoWhitespaceTokenizer();

        System.out.println();

        // 2. 코드 토큰화
        demoCodeTokenizer();

        System.out.println();

        // 3. 토큰 수 = 비용
        demoTokenCost();
    }

    private static void demoWhitespaceTokenizer() {
        System.out.println("─".repeat(60));
        System.out.println("1. WhitespaceTokenizer (공백 기반)");
        System.out.println("─".repeat(60));

        String text = "오늘 날씨 어때?";

        // vocabulary 구축
        String corpus = "오늘 내일 어제 날씨 기온 어때 좋아 나빠 ?";
        WhitespaceTokenizer tokenizer = WhitespaceTokenizer.fromText(corpus);

        System.out.println();
        System.out.println("  입력: \"" + text + "\"");
        System.out.println();

        // 토큰화
        List<String> tokens = tokenizer.tokenize(text);
        System.out.println("  ┌─────────────────────────────────────┐");
        System.out.println("  │ STEP 1-1: 문장을 토큰으로 분리       │");
        System.out.println("  └─────────────────────────────────────┘");
        System.out.println("  토큰: " + tokens);
        System.out.println();

        // 인코딩
        List<Integer> ids = tokenizer.encode(text);
        System.out.println("  ┌─────────────────────────────────────┐");
        System.out.println("  │ STEP 1-2: 토큰을 ID로 변환           │");
        System.out.println("  └─────────────────────────────────────┘");
        System.out.println("  ID: " + ids);
        System.out.println();

        // 상세 매핑 출력
        System.out.println("  토큰-ID 매핑:");
        for (int i = 0; i < tokens.size(); i++) {
            System.out.printf("    \"%s\" → %d%n", tokens.get(i), ids.get(i));
        }
        System.out.println();

        // 디코딩
        String decoded = tokenizer.decode(ids);
        System.out.println("  ┌─────────────────────────────────────┐");
        System.out.println("  │ STEP 1-3: ID를 텍스트로 복원         │");
        System.out.println("  └─────────────────────────────────────┘");
        System.out.println("  복원: \"" + decoded + "\"");
        System.out.println();

        System.out.println("  📊 Vocabulary 크기: " + tokenizer.vocabSize());
    }

    private static void demoCodeTokenizer() {
        System.out.println("─".repeat(60));
        System.out.println("2. CodeTokenizer (코드 특화)");
        System.out.println("─".repeat(60));

        String code = """
            public class User {
                private String name;
            }""";

        CodeTokenizer tokenizer = CodeTokenizer.fromCode(code);

        System.out.println();
        System.out.println("  입력:");
        for (String line : code.split("\n")) {
            System.out.println("    " + line);
        }
        System.out.println();

        // 토큰화
        List<String> tokens = tokenizer.tokenize(code);
        System.out.println("  ┌─────────────────────────────────────┐");
        System.out.println("  │ 코드 토큰화 특징:                     │");
        System.out.println("  │ - 들여쓰기 압축 (INDENT_N)           │");
        System.out.println("  │ - 심볼 분리 ({, }, ;)                │");
        System.out.println("  │ - 줄바꿈 토큰 ([NL])                 │");
        System.out.println("  └─────────────────────────────────────┘");
        System.out.println();
        System.out.println("  토큰: " + tokens);
        System.out.println();

        // 토큰 설명
        System.out.println("  토큰 상세:");
        for (int i = 0; i < tokens.size(); i++) {
            String token = tokens.get(i);
            String desc = getTokenDescription(token);
            System.out.printf("    [%2d] %-15s %s%n", i, token, desc);
        }
        System.out.println();

        System.out.println("  📊 Vocabulary 크기: " + tokenizer.vocabSize());
    }

    private static String getTokenDescription(String token) {
        if (token.equals("[NL]")) return "← 줄바꿈";
        if (token.startsWith("INDENT_")) return "← 들여쓰기 " + token.substring(7) + "레벨";
        if (token.equals("{") || token.equals("}")) return "← 중괄호";
        if (token.equals(";")) return "← 세미콜론";
        if (CodeTokenizer.isJavaKeyword(token)) return "← Java 키워드";
        return "";
    }

    private static void demoTokenCost() {
        System.out.println("─".repeat(60));
        System.out.println("3. 토큰 수 = 비용");
        System.out.println("─".repeat(60));
        System.out.println();

        String shortText = "Hello";
        String longText = "Hello, how are you doing today?";

        WhitespaceTokenizer tokenizer = WhitespaceTokenizer.fromText(
            "Hello how are you doing today ?"
        );

        int shortTokens = tokenizer.tokenize(shortText).size();
        int longTokens = tokenizer.tokenize(longText).size();

        System.out.println("  ┌─────────────────────────────────────────┐");
        System.out.println("  │ 토큰 수가 많을수록 API 비용이 증가합니다  │");
        System.out.println("  └─────────────────────────────────────────┘");
        System.out.println();
        System.out.printf("  \"%s\"%n", shortText);
        System.out.printf("    → %d 토큰%n", shortTokens);
        System.out.println();
        System.out.printf("  \"%s\"%n", longText);
        System.out.printf("    → %d 토큰%n", longTokens);
        System.out.println();

        System.out.println("  💡 GPT-4 토큰 비용 예시:");
        System.out.println("     - 입력: $0.03 / 1K tokens");
        System.out.println("     - 출력: $0.06 / 1K tokens");
        System.out.println();

        System.out.println("  💡 효율적인 토크나이저의 중요성:");
        System.out.println("     - GPT-2: 147 tokens (Python 코드)");
        System.out.println("     - GPT-4:  70 tokens (같은 코드)");
        System.out.println("     → 들여쓰기 압축으로 52% 절감!");
    }
}
