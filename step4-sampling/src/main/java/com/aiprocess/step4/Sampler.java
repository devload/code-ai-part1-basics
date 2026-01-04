package com.aiprocess.step4;

import java.util.*;
import java.util.stream.Collectors;

/**
 * STEP 4: 샘플링
 *
 * 핵심 질문: 왜 매번 답이 다른가?
 *
 * 확률 분포에서 다음 토큰을 "선택"하는 방법입니다.
 * 같은 입력이라도 샘플링 방식에 따라 다른 결과가 나옵니다.
 *
 * 샘플링 전략:
 * ┌─────────────────────────────────────────────────────────┐
 * │ 1. Greedy (Temperature = 0)                             │
 * │    - 항상 확률 가장 높은 토큰 선택                        │
 * │    - 결과가 항상 같음 (deterministic)                    │
 * │                                                          │
 * │ 2. Temperature Sampling                                  │
 * │    - 확률 분포를 조정하여 다양성 제어                     │
 * │    - temp < 1: 집중적 (높은 확률 더 높게)                 │
 * │    - temp > 1: 분산적 (확률 차이 완화)                    │
 * │                                                          │
 * │ 3. Top-K Sampling                                        │
 * │    - 상위 K개 토큰만 후보로 고려                          │
 * │                                                          │
 * │ 4. Top-P (Nucleus) Sampling                              │
 * │    - 누적 확률 P까지의 토큰만 후보로 고려                  │
 * └─────────────────────────────────────────────────────────┘
 */
public class Sampler {

    private final Random random;
    private final double temperature;
    private final int topK;
    private final double topP;

    /**
     * @param temperature 창의성 조절 (0.0 ~ 2.0)
     * @param topK 상위 K개만 고려 (0이면 비활성화)
     * @param topP 누적 확률 P까지만 고려 (1.0이면 비활성화)
     * @param seed 랜덤 시드 (재현성)
     */
    public Sampler(double temperature, int topK, double topP, Long seed) {
        this.temperature = temperature;
        this.topK = topK;
        this.topP = topP;
        this.random = seed != null ? new Random(seed) : new Random();
    }

    /**
     * 기본 생성자 (temperature=1.0, topK=0, topP=1.0)
     */
    public Sampler() {
        this(1.0, 0, 1.0, null);
    }

    /**
     * Temperature만 지정
     */
    public Sampler(double temperature) {
        this(temperature, 0, 1.0, null);
    }

    /**
     * 확률 분포에서 다음 토큰 샘플링
     *
     * @param probabilities 토큰 ID → 확률 매핑
     * @return 선택된 토큰 ID
     */
    public int sample(Map<Integer, Double> probabilities) {
        if (probabilities == null || probabilities.isEmpty()) {
            throw new IllegalArgumentException("확률 분포가 비어있습니다");
        }

        // 1. 토큰-확률 리스트로 변환 (확률 높은 순 정렬)
        List<TokenProb> probs = probabilities.entrySet().stream()
            .map(e -> new TokenProb(e.getKey(), e.getValue()))
            .sorted((a, b) -> Double.compare(b.prob, a.prob))
            .collect(Collectors.toList());

        // 2. Temperature 적용
        if (temperature != 1.0 && temperature > 0) {
            probs = applyTemperature(probs, temperature);
        }

        // Greedy (temperature = 0)
        if (temperature == 0) {
            return probs.get(0).tokenId;
        }

        // 3. Top-K 필터링
        if (topK > 0 && topK < probs.size()) {
            probs = applyTopK(probs, topK);
        }

        // 4. Top-P (Nucleus) 필터링
        if (topP < 1.0) {
            probs = applyTopP(probs, topP);
        }

        // 5. 확률 기반 샘플링
        return sampleFromProbs(probs);
    }

    /**
     * 카운트 맵에서 샘플링 (확률로 변환 후 샘플링)
     */
    public int sampleFromCounts(Map<Integer, Integer> counts) {
        int total = counts.values().stream().mapToInt(Integer::intValue).sum();
        Map<Integer, Double> probs = new HashMap<>();
        for (Map.Entry<Integer, Integer> entry : counts.entrySet()) {
            probs.put(entry.getKey(), (double) entry.getValue() / total);
        }
        return sample(probs);
    }

    /**
     * Temperature 적용
     *
     * logits = log(prob)
     * scaled = logits / temperature
     * new_prob = softmax(scaled)
     */
    private List<TokenProb> applyTemperature(List<TokenProb> probs, double temp) {
        // log 변환
        double[] logits = probs.stream()
            .mapToDouble(p -> Math.log(p.prob + 1e-10))
            .toArray();

        // Temperature 스케일링
        double[] scaled = new double[logits.length];
        for (int i = 0; i < logits.length; i++) {
            scaled[i] = logits[i] / temp;
        }

        // Softmax (수치 안정성을 위해 max 빼기)
        double max = Arrays.stream(scaled).max().orElse(0);
        double[] exp = Arrays.stream(scaled)
            .map(l -> Math.exp(l - max))
            .toArray();
        double sum = Arrays.stream(exp).sum();

        List<TokenProb> result = new ArrayList<>();
        for (int i = 0; i < probs.size(); i++) {
            result.add(new TokenProb(probs.get(i).tokenId, exp[i] / sum));
        }

        return result.stream()
            .sorted((a, b) -> Double.compare(b.prob, a.prob))
            .collect(Collectors.toList());
    }

    /**
     * Top-K 필터링
     */
    private List<TokenProb> applyTopK(List<TokenProb> probs, int k) {
        List<TokenProb> topK = probs.subList(0, Math.min(k, probs.size()));
        return normalize(topK);
    }

    /**
     * Top-P (Nucleus) 필터링
     */
    private List<TokenProb> applyTopP(List<TokenProb> probs, double p) {
        List<TokenProb> result = new ArrayList<>();
        double cumulative = 0;

        for (TokenProb prob : probs) {
            result.add(prob);
            cumulative += prob.prob;
            if (cumulative >= p) break;
        }

        return normalize(result);
    }

    /**
     * 확률 재정규화
     */
    private List<TokenProb> normalize(List<TokenProb> probs) {
        double total = probs.stream().mapToDouble(p -> p.prob).sum();
        return probs.stream()
            .map(p -> new TokenProb(p.tokenId, p.prob / total))
            .collect(Collectors.toList());
    }

    /**
     * 확률 기반 샘플링
     */
    private int sampleFromProbs(List<TokenProb> probs) {
        double r = random.nextDouble();
        double cumulative = 0;

        for (TokenProb p : probs) {
            cumulative += p.prob;
            if (r < cumulative) {
                return p.tokenId;
            }
        }

        return probs.get(0).tokenId;
    }

    /**
     * 토큰-확률 쌍
     */
    private record TokenProb(int tokenId, double prob) {}

    @Override
    public String toString() {
        return String.format("Sampler(temp=%.2f, topK=%d, topP=%.2f)",
            temperature, topK, topP);
    }
}
