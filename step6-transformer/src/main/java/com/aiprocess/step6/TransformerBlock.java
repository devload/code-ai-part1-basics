package com.aiprocess.step6;

/**
 * Transformer Block: Self-Attention + Feed Forward
 *
 * 실제 Transformer의 핵심 구조를 간단화한 버전
 */
public class TransformerBlock {

    private final int dimension;

    public TransformerBlock(int dimension) {
        this.dimension = dimension;
    }

    /**
     * Transformer Block 처리
     *
     * 1. Self-Attention
     * 2. Residual Connection + Layer Norm
     * 3. Feed Forward
     * 4. Residual Connection + Layer Norm
     */
    public float[][] forward(float[][] inputs, boolean causal) {
        int n = inputs.length;
        float[][] outputs = new float[n][dimension];

        // 1. Self-Attention
        float[][] attentionWeights;
        if (causal) {
            attentionWeights = Attention.causalSelfAttention(inputs);
        } else {
            attentionWeights = Attention.selfAttention(inputs);
        }

        // Attention 적용
        float[][] attended = new float[n][dimension];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                for (int d = 0; d < dimension; d++) {
                    attended[i][d] += attentionWeights[i][j] * inputs[j][d];
                }
            }
        }

        // 2. Residual Connection
        for (int i = 0; i < n; i++) {
            for (int d = 0; d < dimension; d++) {
                outputs[i][d] = inputs[i][d] + attended[i][d];
            }
            // Layer Norm (간단화)
            outputs[i] = layerNorm(outputs[i]);
        }

        // 3. Feed Forward (간단한 비선형 변환)
        float[][] ffOutput = new float[n][dimension];
        for (int i = 0; i < n; i++) {
            ffOutput[i] = feedForward(outputs[i]);
        }

        // 4. Residual Connection
        for (int i = 0; i < n; i++) {
            for (int d = 0; d < dimension; d++) {
                outputs[i][d] = outputs[i][d] + ffOutput[i][d];
            }
            outputs[i] = layerNorm(outputs[i]);
        }

        return outputs;
    }

    /**
     * 간단한 Feed Forward 네트워크
     */
    private float[] feedForward(float[] input) {
        float[] output = new float[dimension];

        // ReLU 활성화 (간단화)
        for (int i = 0; i < dimension; i++) {
            output[i] = Math.max(0, input[i]) * 0.5f; // 스케일 다운
        }

        return output;
    }

    /**
     * Layer Normalization (간단화)
     */
    private float[] layerNorm(float[] input) {
        float mean = 0;
        for (float v : input) mean += v;
        mean /= input.length;

        float variance = 0;
        for (float v : input) variance += (v - mean) * (v - mean);
        variance /= input.length;

        float std = (float) Math.sqrt(variance + 1e-6);

        float[] output = new float[input.length];
        for (int i = 0; i < input.length; i++) {
            output[i] = (input[i] - mean) / std;
        }

        return output;
    }

    /**
     * Attention 가중치 반환 (시각화용)
     */
    public float[][] getAttentionWeights(float[][] inputs, boolean causal) {
        if (causal) {
            return Attention.causalSelfAttention(inputs);
        } else {
            return Attention.selfAttention(inputs);
        }
    }
}
