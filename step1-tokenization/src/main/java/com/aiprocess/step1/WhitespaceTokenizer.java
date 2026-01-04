package com.aiprocess.step1;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 공백 기반 토크나이저
 *
 * 가장 단순한 토크나이저: 공백으로 단어 분리
 *
 * 장점:
 * - 구현이 간단함
 * - 직관적으로 이해하기 쉬움
 *
 * 단점:
 * - 구두점 처리 안 됨 ("Hello," != "Hello")
 * - 대소문자 구분 ("hello" != "Hello")
 * - 어휘에 없는 단어는 모두 [UNK]로 처리
 *
 * 예시:
 * ┌─────────────────────────────────────────┐
 * │ 입력: "Hello World"                      │
 * │ 토큰: ["Hello", "World"]                 │
 * │ ID:   [1, 2]                             │
 * └─────────────────────────────────────────┘
 */
public class WhitespaceTokenizer implements Tokenizer {

    // 특수 토큰
    public static final String UNK_TOKEN = "[UNK]";  // Unknown (미등록 단어)
    public static final String PAD_TOKEN = "[PAD]";  // Padding
    public static final String BOS_TOKEN = "[BOS]";  // Begin of Sentence
    public static final String EOS_TOKEN = "[EOS]";  // End of Sentence

    public static final int UNK_ID = 0;
    public static final int PAD_ID = 1;
    public static final int BOS_ID = 2;
    public static final int EOS_ID = 3;

    private final Map<String, Integer> tokenToId;
    private final Map<Integer, String> idToToken;
    private int nextId;

    /**
     * 빈 토크나이저 생성 (특수 토큰만 포함)
     */
    public WhitespaceTokenizer() {
        this.tokenToId = new HashMap<>();
        this.idToToken = new HashMap<>();

        // 특수 토큰 등록
        addToken(UNK_TOKEN);  // ID 0
        addToken(PAD_TOKEN);  // ID 1
        addToken(BOS_TOKEN);  // ID 2
        addToken(EOS_TOKEN);  // ID 3
    }

    /**
     * 기존 vocabulary로 초기화
     */
    public WhitespaceTokenizer(Map<String, Integer> vocabulary) {
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
     * 텍스트로부터 vocabulary 구축
     */
    public static WhitespaceTokenizer fromText(String corpus) {
        WhitespaceTokenizer tokenizer = new WhitespaceTokenizer();

        // 모든 단어 추출하여 vocabulary에 추가
        Arrays.stream(corpus.split("\\s+"))
            .filter(word -> !word.isEmpty())
            .distinct()
            .forEach(tokenizer::addToken);

        return tokenizer;
    }

    /**
     * 토큰 추가
     */
    public void addToken(String token) {
        if (!tokenToId.containsKey(token)) {
            tokenToId.put(token, nextId);
            idToToken.put(nextId, token);
            nextId++;
        }
    }

    @Override
    public List<String> tokenize(String text) {
        if (text == null || text.trim().isEmpty()) {
            return Collections.emptyList();
        }

        return Arrays.stream(text.split("\\s+"))
            .filter(word -> !word.isEmpty())
            .collect(Collectors.toList());
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

        return tokenIds.stream()
            .map(id -> idToToken.getOrDefault(id, UNK_TOKEN))
            .collect(Collectors.joining(" "));
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

    /**
     * Vocabulary 반환
     */
    public Map<String, Integer> getVocabulary() {
        return new HashMap<>(tokenToId);
    }

    @Override
    public String toString() {
        return String.format("WhitespaceTokenizer(vocab_size=%d)", vocabSize());
    }
}
