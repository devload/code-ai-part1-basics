package com.aiprocess.step6;

/**
 * Attention 메커니즘: 중요한 것에 집중
 *
 * Query, Key, Value를 사용해 관련 있는 정보에 가중치를 부여
 */
public class Attention {

    /**
     * Scaled Dot-Product Attention
     *
     * @param query 쿼리 벡터 (무엇을 찾고 싶은지)
     * @param keys 키 벡터들 (각 위치의 정보)
     * @param values 값 벡터들 (실제 내용)
     * @return 가중 평균된 결과 벡터
     */
    public static float[] attention(float[] query, float[][] keys, float[][] values) {
        int n = keys.length;
        int dim = query.length;

        // 1. Query와 각 Key의 유사도 계산 (dot product)
        float[] scores = new float[n];
        for (int i = 0; i < n; i++) {
            scores[i] = dotProduct(query, keys[i]);
        }

        // 2. Scale (차원의 제곱근으로 나눔)
        float scale = (float) Math.sqrt(dim);
        for (int i = 0; i < n; i++) {
            scores[i] /= scale;
        }

        // 3. Softmax로 확률 변환
        float[] weights = softmax(scores);

        // 4. Value의 가중 평균
        float[] output = new float[values[0].length];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < output.length; j++) {
                output[j] += weights[i] * values[i][j];
            }
        }

        return output;
    }

    /**
     * Self-Attention: 문장 내 단어들이 서로를 참조
     *
     * @param embeddings 각 토큰의 임베딩 벡터
     * @return Attention 가중치 행렬 [i][j] = i번째 토큰이 j번째 토큰에 주는 attention
     */
    public static float[][] selfAttention(float[][] embeddings) {
        int n = embeddings.length;
        float[][] attentionWeights = new float[n][n];

        for (int i = 0; i < n; i++) {
            // 각 위치에서 다른 모든 위치와의 attention 계산
            float[] scores = new float[n];
            for (int j = 0; j < n; j++) {
                scores[j] = dotProduct(embeddings[i], embeddings[j]);
            }

            // Scale
            float scale = (float) Math.sqrt(embeddings[0].length);
            for (int j = 0; j < n; j++) {
                scores[j] /= scale;
            }

            // Softmax
            attentionWeights[i] = softmax(scores);
        }

        return attentionWeights;
    }

    /**
     * Causal (Masked) Self-Attention: 이전 토큰만 볼 수 있음 (GPT 스타일)
     */
    public static float[][] causalSelfAttention(float[][] embeddings) {
        int n = embeddings.length;
        float[][] attentionWeights = new float[n][n];

        for (int i = 0; i < n; i++) {
            float[] scores = new float[n];

            // 이전 토큰들만 볼 수 있음 (j <= i)
            for (int j = 0; j <= i; j++) {
                scores[j] = dotProduct(embeddings[i], embeddings[j]);
            }

            // 미래 토큰은 매우 큰 음수값으로 마스킹
            for (int j = i + 1; j < n; j++) {
                scores[j] = Float.NEGATIVE_INFINITY;
            }

            // Scale
            float scale = (float) Math.sqrt(embeddings[0].length);
            for (int j = 0; j <= i; j++) {
                scores[j] /= scale;
            }

            // Softmax (마스킹된 부분은 0이 됨)
            attentionWeights[i] = softmax(scores);
        }

        return attentionWeights;
    }

    private static float dotProduct(float[] a, float[] b) {
        float sum = 0;
        for (int i = 0; i < a.length; i++) {
            sum += a[i] * b[i];
        }
        return sum;
    }

    private static float[] softmax(float[] scores) {
        float[] result = new float[scores.length];

        // 수치 안정성을 위해 최댓값을 뺌
        float max = Float.NEGATIVE_INFINITY;
        for (float s : scores) {
            if (s > max) max = s;
        }

        float sum = 0;
        for (int i = 0; i < scores.length; i++) {
            if (scores[i] == Float.NEGATIVE_INFINITY) {
                result[i] = 0;
            } else {
                result[i] = (float) Math.exp(scores[i] - max);
                sum += result[i];
            }
        }

        for (int i = 0; i < result.length; i++) {
            result[i] /= sum;
        }

        return result;
    }
}
