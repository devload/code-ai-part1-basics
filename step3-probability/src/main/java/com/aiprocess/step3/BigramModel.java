package com.aiprocess.step3;

import java.util.HashMap;
import java.util.Map;

/**
 * Bigram 확률 모델
 *
 * 가장 단순한 확률 모델: 바로 앞 토큰만 보고 예측
 *
 * P(next | prev) = count(prev, next) / count(prev)
 *
 * 예시:
 * ┌─────────────────────────────────────────────────────────┐
 * │ 학습 데이터: "오늘 날씨 좋다. 오늘 날씨 맑다."             │
 * │                                                          │
 * │ Bigram 카운트:                                           │
 * │   ("오늘", "날씨") = 2                                    │
 * │   ("날씨", "좋다") = 1                                    │
 * │   ("날씨", "맑다") = 1                                    │
 * │                                                          │
 * │ P(날씨 | 오늘) = 2/2 = 100%                              │
 * │ P(좋다 | 날씨) = 1/2 = 50%                               │
 * │ P(맑다 | 날씨) = 1/2 = 50%                               │
 * └─────────────────────────────────────────────────────────┘
 *
 * 장점: 간단하고 빠름
 * 단점: 문맥을 하나만 봄 (장거리 의존성 무시)
 */
public class BigramModel implements ProbabilityModel {

    // bigrams[prev][next] = count
    private final Map<Integer, Map<Integer, Integer>> bigrams;
    // 각 토큰의 총 출현 횟수
    private final Map<Integer, Integer> tokenCounts;
    private int vocabSize;

    public BigramModel() {
        this.bigrams = new HashMap<>();
        this.tokenCounts = new HashMap<>();
        this.vocabSize = 0;
    }

    @Override
    public void train(int[] tokens) {
        if (tokens.length < 2) return;

        for (int i = 0; i < tokens.length - 1; i++) {
            int prev = tokens[i];
            int next = tokens[i + 1];

            // Bigram 카운트 증가
            bigrams.computeIfAbsent(prev, k -> new HashMap<>())
                   .merge(next, 1, Integer::sum);

            // 토큰 카운트 증가
            tokenCounts.merge(prev, 1, Integer::sum);

            // Vocabulary 크기 업데이트
            vocabSize = Math.max(vocabSize, Math.max(prev, next) + 1);
        }

        // 마지막 토큰 카운트
        tokenCounts.merge(tokens[tokens.length - 1], 1, Integer::sum);
    }

    @Override
    public Map<Integer, Double> getNextTokenProbabilities(int[] context) {
        if (context.length == 0) {
            return new HashMap<>();
        }

        int prev = context[context.length - 1];
        Map<Integer, Integer> nextCounts = getNextTokenCounts(prev);

        if (nextCounts.isEmpty()) {
            return new HashMap<>();
        }

        int total = nextCounts.values().stream().mapToInt(Integer::intValue).sum();
        Map<Integer, Double> probs = new HashMap<>();

        for (Map.Entry<Integer, Integer> entry : nextCounts.entrySet()) {
            probs.put(entry.getKey(), (double) entry.getValue() / total);
        }

        return probs;
    }

    @Override
    public Map<Integer, Integer> getNextTokenCounts(int prevToken) {
        return bigrams.getOrDefault(prevToken, new HashMap<>());
    }

    @Override
    public int vocabSize() {
        return vocabSize;
    }

    /**
     * 특정 bigram의 확률
     */
    public double getProbability(int prev, int next) {
        Map<Integer, Integer> nextCounts = bigrams.get(prev);
        if (nextCounts == null) return 0.0;

        int count = nextCounts.getOrDefault(next, 0);
        int total = nextCounts.values().stream().mapToInt(Integer::intValue).sum();

        return total > 0 ? (double) count / total : 0.0;
    }

    /**
     * 총 bigram 수
     */
    public int getTotalBigramCount() {
        return bigrams.values().stream()
            .mapToInt(m -> m.values().stream().mapToInt(Integer::intValue).sum())
            .sum();
    }

    @Override
    public String toString() {
        return String.format("BigramModel(vocab=%d, bigrams=%d)",
            vocabSize, getTotalBigramCount());
    }
}
