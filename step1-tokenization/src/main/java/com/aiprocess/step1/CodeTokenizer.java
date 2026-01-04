package com.aiprocess.step1;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 코드 특화 토크나이저
 *
 * 일반 텍스트와 달리 코드는 특별한 처리가 필요합니다:
 *
 * 1. 들여쓰기 압축 (GPT-4 방식)
 *    - 공백 4개 = INDENT_1
 *    - 공백 8개 = INDENT_2
 *
 * 2. 심볼 분리
 *    - "public class User{" → ["public", "class", "User", "{"]
 *
 * 3. 키워드 보호
 *    - Java 키워드는 절대 쪼개지 않음
 *
 * 토큰 효율성 비교:
 * ┌───────────────────────────────────────────────┐
 * │ GPT-2: 147 tokens  (같은 Python 코드)         │
 * │ GPT-4:  70 tokens  ← 들여쓰기 압축 덕분       │
 * │ 핵심: 토큰 수 = 비용!                         │
 * └───────────────────────────────────────────────┘
 */
public class CodeTokenizer implements Tokenizer {

    public static final String UNK_TOKEN = "[UNK]";
    public static final String NEWLINE_TOKEN = "[NL]";
    public static final String INDENT_PREFIX = "INDENT_";
    public static final int UNK_ID = 0;

    // 심볼 패턴 (코드에서 독립 토큰으로 분리할 문자들)
    private static final Pattern SYMBOL_PATTERN = Pattern.compile(
        "([{}()\\[\\];,.<>:?!@#$%^&*+=|~`/\\\\-])"
    );

    // 문자열 리터럴 패턴
    private static final Pattern STRING_PATTERN = Pattern.compile(
        "\"[^\"]*\"|'[^']*'"
    );

    // 들여쓰기 패턴
    private static final Pattern INDENT_PATTERN = Pattern.compile("^([ \\t]+)");

    // Java 키워드 (보호 대상)
    private static final Set<String> JAVA_KEYWORDS = Set.of(
        "abstract", "assert", "boolean", "break", "byte", "case", "catch",
        "char", "class", "const", "continue", "default", "do", "double",
        "else", "enum", "extends", "final", "finally", "float", "for",
        "goto", "if", "implements", "import", "instanceof", "int", "interface",
        "long", "native", "new", "package", "private", "protected", "public",
        "return", "short", "static", "strictfp", "super", "switch", "synchronized",
        "this", "throw", "throws", "transient", "try", "void", "volatile", "while",
        "true", "false", "null", "var", "record", "sealed", "permits", "yield"
    );

    private final Map<String, Integer> tokenToId;
    private final Map<Integer, String> idToToken;
    private int nextId;

    public CodeTokenizer() {
        this.tokenToId = new HashMap<>();
        this.idToToken = new HashMap<>();
        addToken(UNK_TOKEN);  // ID 0
        addToken(NEWLINE_TOKEN);  // ID 1
    }

    public CodeTokenizer(Map<String, Integer> vocabulary) {
        this();
        for (Map.Entry<String, Integer> entry : vocabulary.entrySet()) {
            if (!tokenToId.containsKey(entry.getKey())) {
                tokenToId.put(entry.getKey(), entry.getValue());
                idToToken.put(entry.getValue(), entry.getKey());
                nextId = Math.max(nextId, entry.getValue() + 1);
            }
        }
    }

    /**
     * 코드 텍스트로부터 vocabulary 구축
     */
    public static CodeTokenizer fromCode(String code) {
        CodeTokenizer tokenizer = new CodeTokenizer();

        // 줄 단위로 토큰화하여 vocabulary 구축
        String[] lines = code.split("\n");
        for (String line : lines) {
            for (String token : tokenizer.tokenizeLine(line)) {
                tokenizer.addToken(token);
            }
        }

        return tokenizer;
    }

    public void addToken(String token) {
        if (!tokenToId.containsKey(token)) {
            tokenToId.put(token, nextId);
            idToToken.put(nextId, token);
            nextId++;
        }
    }

    @Override
    public List<String> tokenize(String text) {
        if (text == null || text.isEmpty()) {
            return Collections.emptyList();
        }

        List<String> result = new ArrayList<>();
        String[] lines = text.split("\n");

        for (int i = 0; i < lines.length; i++) {
            result.addAll(tokenizeLine(lines[i]));
            if (i < lines.length - 1) {
                result.add(NEWLINE_TOKEN);
            }
        }

        return result;
    }

    /**
     * 한 줄을 토큰화
     */
    private List<String> tokenizeLine(String line) {
        List<String> tokens = new ArrayList<>();

        if (line.isEmpty()) {
            return tokens;
        }

        String remaining = line;

        // 1. 들여쓰기 처리
        Matcher indentMatcher = INDENT_PATTERN.matcher(line);
        if (indentMatcher.find()) {
            String indent = indentMatcher.group(1);
            int level = calculateIndentLevel(indent);
            if (level > 0) {
                tokens.add(INDENT_PREFIX + level);
            }
            remaining = line.substring(indent.length());
        }

        // 2. 문자열 리터럴 보호 (임시 치환)
        Map<String, String> stringMap = new HashMap<>();
        Matcher stringMatcher = STRING_PATTERN.matcher(remaining);
        StringBuffer sb = new StringBuffer();
        int stringIndex = 0;

        while (stringMatcher.find()) {
            String placeholder = "___STRING_" + stringIndex + "___";
            stringMap.put(placeholder, stringMatcher.group());
            stringMatcher.appendReplacement(sb, placeholder);
            stringIndex++;
        }
        stringMatcher.appendTail(sb);
        remaining = sb.toString();

        // 3. 심볼 분리
        remaining = SYMBOL_PATTERN.matcher(remaining).replaceAll(" $1 ");

        // 4. 공백으로 분리
        String[] parts = remaining.trim().split("\\s+");

        for (String part : parts) {
            if (part.isEmpty()) continue;

            // 문자열 리터럴 복원
            if (stringMap.containsKey(part)) {
                tokens.add(stringMap.get(part));
            } else {
                tokens.add(part);
            }
        }

        return tokens;
    }

    /**
     * 들여쓰기 레벨 계산 (4칸 = 1레벨)
     */
    private int calculateIndentLevel(String indent) {
        int spaces = 0;
        for (char c : indent.toCharArray()) {
            if (c == '\t') {
                spaces += 4;
            } else {
                spaces++;
            }
        }
        return spaces / 4;
    }

    @Override
    public List<Integer> encode(String text) {
        return tokenize(text).stream()
            .map(token -> tokenToId.getOrDefault(token, UNK_ID))
            .collect(Collectors.toList());
    }

    @Override
    public String decode(List<Integer> tokenIds) {
        if (tokenIds == null || tokenIds.isEmpty()) {
            return "";
        }

        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < tokenIds.size(); i++) {
            String token = idToToken.getOrDefault(tokenIds.get(i), UNK_TOKEN);

            // 줄바꿈 처리
            if (token.equals(NEWLINE_TOKEN)) {
                sb.append("\n");
                continue;
            }

            // 들여쓰기 처리
            if (token.startsWith(INDENT_PREFIX)) {
                int level = Integer.parseInt(token.substring(INDENT_PREFIX.length()));
                sb.append("    ".repeat(level));
                continue;
            }

            // 일반 토큰
            if (i > 0) {
                String prevToken = idToToken.getOrDefault(tokenIds.get(i - 1), "");
                if (!prevToken.equals(NEWLINE_TOKEN) && !prevToken.startsWith(INDENT_PREFIX)) {
                    sb.append(" ");
                }
            }
            sb.append(token);
        }

        return sb.toString().trim();
    }

    @Override
    public int vocabSize() {
        return tokenToId.size();
    }

    @Override
    public String getToken(int id) {
        return idToToken.getOrDefault(id, UNK_TOKEN);
    }

    @Override
    public int getTokenId(String token) {
        return tokenToId.getOrDefault(token, UNK_ID);
    }

    public Map<String, Integer> getVocabulary() {
        return new HashMap<>(tokenToId);
    }

    public static boolean isJavaKeyword(String token) {
        return JAVA_KEYWORDS.contains(token);
    }

    @Override
    public String toString() {
        return String.format("CodeTokenizer(vocab_size=%d)", vocabSize());
    }
}
