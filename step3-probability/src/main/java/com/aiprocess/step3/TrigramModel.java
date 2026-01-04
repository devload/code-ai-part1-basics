package com.aiprocess.step3;

import java.util.HashMap;
import java.util.Map;

/**
 * Trigram 확률 모델
 *
 * Bigram보다 더 많은 문맥을 고려: 앞 2개 토큰을 보고 예측
 *
 * P(next | prev2, prev1) = count(prev2, prev1, next) / count(prev2, prev1)
 *
 * 예시:
 * ┌─────────────────────────────────────────────────────────┐
 * │ Bigram:  P(맑다 | 날씨) = 50%                            │
 * │ Trigram: P(맑다 | 오늘, 날씨) = ?                        │
 * │                                                          │
 * │ "오늘 날씨 맑다" 가 더 많이 등장했다면                    │
 * │ → Trigram이 더 정확한 예측 가능                          │
 * └─────────────────────────────────────────────────────────┘
 *
 * 장점: 더 많은 문맥 고려
 * 단점: 희소성 문제 (데이터가 더 많이 필요)
 */
public class TrigramModel implements ProbabilityModel {

    // trigrams[prev2][prev1][next] = count
    private final Map<Long, Map<Integer, Integer>> trigrams;
    // bigram fallback
    private final BigramModel bigramFallback;
    private int vocabSize;

    public TrigramModel() {
        this.trigrams = new HashMap<>();
        this.bigramFallback = new BigramModel();
        this.vocabSize = 0;
    }

    /**
     * prev2와 prev1을 하나의 키로 인코딩
     */
    private long encodeKey(int prev2, int prev1) {
        return ((long) prev2 << 32) | (prev1 & 0xFFFFFFFFL);
    }

    @Override
    public void train(int[] tokens) {
        // Bigram fallback도 학습
        bigramFallback.train(tokens);

        if (tokens.length < 3) return;

        for (int i = 0; i < tokens.length - 2; i++) {
            int prev2 = tokens[i];
            int prev1 = tokens[i + 1];
            int next = tokens[i + 2];

            long key = encodeKey(prev2, prev1);

            trigrams.computeIfAbsent(key, k -> new HashMap<>())
                    .merge(next, 1, Integer::sum);

            vocabSize = Math.max(vocabSize, Math.max(prev2, Math.max(prev1, next)) + 1);
        }
    }

    @Override
    public Map<Integer, Double> getNextTokenProbabilities(int[] context) {
        if (context.length == 0) {
            return new HashMap<>();
        }

        // Trigram 시도
        if (context.length >= 2) {
            int prev2 = context[context.length - 2];
            int prev1 = context[context.length - 1];
            long key = encodeKey(prev2, prev1);

            Map<Integer, Integer> nextCounts = trigrams.get(key);
            if (nextCounts != null && !nextCounts.isEmpty()) {
                int total = nextCounts.values().stream().mapToInt(Integer::intValue).sum();
                Map<Integer, Double> probs = new HashMap<>();
                for (Map.Entry<Integer, Integer> entry : nextCounts.entrySet()) {
                    probs.put(entry.getKey(), (double) entry.getValue() / total);
                }
                return probs;
            }
        }

        // Fallback to Bigram
        return bigramFallback.getNextTokenProbabilities(context);
    }

    @Override
    public Map<Integer, Integer> getNextTokenCounts(int prevToken) {
        // Single token의 경우 Bigram 사용
        return bigramFallback.getNextTokenCounts(prevToken);
    }

    /**
     * 특정 trigram의 카운트 조회
     */
    public Map<Integer, Integer> getNextTokenCounts(int prev2, int prev1) {
        long key = encodeKey(prev2, prev1);
        Map<Integer, Integer> counts = trigrams.get(key);
        return counts != null ? new HashMap<>(counts) : new HashMap<>();
    }

    @Override
    public int vocabSize() {
        return Math.max(vocabSize, bigramFallback.vocabSize());
    }

    /**
     * 총 trigram 수
     */
    public int getTotalTrigramCount() {
        return trigrams.values().stream()
            .mapToInt(m -> m.values().stream().mapToInt(Integer::intValue).sum())
            .sum();
    }

    @Override
    public String toString() {
        return String.format("TrigramModel(vocab=%d, trigrams=%d, bigrams=%d)",
            vocabSize(), getTotalTrigramCount(), bigramFallback.getTotalBigramCount());
    }
}
