package com.aiprocess.step5;

import java.util.*;

/**
 * Embedding: 토큰을 벡터(숫자 배열)로 변환
 *
 * 핵심 원리: 비슷한 의미의 단어는 비슷한 벡터를 가진다.
 */
public class Embedding {

    private final int dimension;
    private final Map<String, float[]> embeddings;
    private final Random random;

    public Embedding(int dimension) {
        this.dimension = dimension;
        this.embeddings = new HashMap<>();
        this.random = new Random(42); // 재현성을 위해 시드 고정
    }

    /**
     * 토큰을 벡터로 변환
     */
    public float[] embed(String token) {
        return embeddings.computeIfAbsent(token.toLowerCase(), k -> createRandomVector());
    }

    /**
     * 문장(토큰 리스트)을 하나의 벡터로 변환 (평균 풀링)
     */
    public float[] embedSentence(List<String> tokens) {
        if (tokens.isEmpty()) {
            return new float[dimension];
        }

        float[] result = new float[dimension];

        for (String token : tokens) {
            float[] vec = embed(token);
            for (int i = 0; i < dimension; i++) {
                result[i] += vec[i];
            }
        }

        // 평균
        for (int i = 0; i < dimension; i++) {
            result[i] /= tokens.size();
        }

        return normalize(result);
    }

    /**
     * 두 벡터의 코사인 유사도 계산
     * 1.0 = 완전히 같은 방향 (매우 유사)
     * 0.0 = 직각 (관련 없음)
     * -1.0 = 반대 방향 (반대 의미)
     */
    public static double cosineSimilarity(float[] a, float[] b) {
        if (a.length != b.length) {
            throw new IllegalArgumentException("벡터 차원이 다릅니다");
        }

        double dot = 0, normA = 0, normB = 0;

        for (int i = 0; i < a.length; i++) {
            dot += a[i] * b[i];
            normA += a[i] * a[i];
            normB += b[i] * b[i];
        }

        if (normA == 0 || normB == 0) {
            return 0;
        }

        return dot / (Math.sqrt(normA) * Math.sqrt(normB));
    }

    /**
     * 유사한 토큰 찾기
     */
    public List<Map.Entry<String, Double>> findSimilar(String token, int topK) {
        float[] targetVec = embed(token);

        List<Map.Entry<String, Double>> similarities = new ArrayList<>();

        for (Map.Entry<String, float[]> entry : embeddings.entrySet()) {
            if (!entry.getKey().equals(token.toLowerCase())) {
                double sim = cosineSimilarity(targetVec, entry.getValue());
                similarities.add(new AbstractMap.SimpleEntry<>(entry.getKey(), sim));
            }
        }

        similarities.sort((a, b) -> Double.compare(b.getValue(), a.getValue()));

        return similarities.subList(0, Math.min(topK, similarities.size()));
    }

    /**
     * 사전 정의된 유사 단어 그룹으로 학습 시뮬레이션
     */
    public void trainSimilarWords(List<List<String>> wordGroups) {
        for (List<String> group : wordGroups) {
            if (group.isEmpty()) continue;

            // 그룹의 기준 벡터 생성
            float[] baseVector = createRandomVector();

            for (String word : group) {
                // 기준 벡터에 약간의 노이즈 추가
                float[] wordVector = addNoise(baseVector, 0.1f);
                embeddings.put(word.toLowerCase(), wordVector);
            }
        }
    }

    private float[] createRandomVector() {
        float[] vec = new float[dimension];
        for (int i = 0; i < dimension; i++) {
            vec[i] = random.nextFloat() * 2 - 1; // -1 ~ 1
        }
        return normalize(vec);
    }

    private float[] addNoise(float[] vec, float noiseLevel) {
        float[] result = new float[vec.length];
        for (int i = 0; i < vec.length; i++) {
            result[i] = vec[i] + (random.nextFloat() * 2 - 1) * noiseLevel;
        }
        return normalize(result);
    }

    private float[] normalize(float[] vec) {
        double norm = 0;
        for (float v : vec) {
            norm += v * v;
        }
        norm = Math.sqrt(norm);

        if (norm == 0) return vec;

        float[] result = new float[vec.length];
        for (int i = 0; i < vec.length; i++) {
            result[i] = (float) (vec[i] / norm);
        }
        return result;
    }

    public int getDimension() {
        return dimension;
    }

    public int getVocabularySize() {
        return embeddings.size();
    }

    /**
     * 벡터를 문자열로 표현 (처음 5개 요소만)
     */
    public static String vectorToString(float[] vec) {
        StringBuilder sb = new StringBuilder("[");
        int show = Math.min(5, vec.length);
        for (int i = 0; i < show; i++) {
            if (i > 0) sb.append(", ");
            sb.append(String.format("%.3f", vec[i]));
        }
        if (vec.length > show) {
            sb.append(", ... (").append(vec.length).append("차원)");
        }
        sb.append("]");
        return sb.toString();
    }
}
