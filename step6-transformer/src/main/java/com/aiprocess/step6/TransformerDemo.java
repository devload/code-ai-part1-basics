package com.aiprocess.step6;

import com.aiprocess.step5.Embedding;
import java.util.*;

/**
 * Transformer 데모: Attention 메커니즘과 Transformer 구조 이해
 */
public class TransformerDemo {

    public static void main(String[] args) {
        System.out.println("========================================");
        System.out.println("STEP 6: Transformer 데모");
        System.out.println("========================================\n");

        // 1. Position Encoding
        System.out.println("[ 1. Position Encoding ]");
        System.out.println("----------------------------------------");
        System.out.println("Transformer는 순서를 모르기 때문에 위치 정보를 추가합니다.\n");

        int dimension = 8;
        for (int pos = 0; pos < 4; pos++) {
            float[] posEnc = PositionEncoding.encode(pos, dimension);
            System.out.printf("위치 %d: %s%n", pos, vectorToString(posEnc));
        }

        // 2. Self-Attention
        System.out.println("\n[ 2. Self-Attention ]");
        System.out.println("----------------------------------------");
        System.out.println("문장 내 단어들이 서로를 참조합니다.\n");

        String[] tokens = {"고양이가", "생선을", "먹었다"};
        Embedding embedding = new Embedding(16);

        // 임베딩 생성
        float[][] embeddings = new float[tokens.length][];
        for (int i = 0; i < tokens.length; i++) {
            embeddings[i] = embedding.embed(tokens[i]);
        }

        // 위치 인코딩 추가
        embeddings = PositionEncoding.addPositionEncodings(embeddings);

        // Self-Attention 계산
        float[][] attentionWeights = Attention.selfAttention(embeddings);

        System.out.println("문장: \"고양이가 생선을 먹었다\"\n");
        System.out.println("Attention 가중치 행렬:");
        System.out.printf("%12s", "");
        for (String token : tokens) {
            System.out.printf("%10s", token);
        }
        System.out.println();

        for (int i = 0; i < tokens.length; i++) {
            System.out.printf("%10s: ", tokens[i]);
            for (int j = 0; j < tokens.length; j++) {
                System.out.printf("%10.3f", attentionWeights[i][j]);
            }
            System.out.println();
        }

        System.out.println("\n해석:");
        System.out.println("- '먹었다'가 '고양이가'와 '생선을'에 집중");
        System.out.println("- 각 단어가 문맥을 파악하기 위해 다른 단어들을 참조");

        // 3. Causal (Masked) Attention
        System.out.println("\n[ 3. Causal (Masked) Attention - GPT 스타일 ]");
        System.out.println("----------------------------------------");
        System.out.println("다음 단어 예측 시, 이전 단어만 볼 수 있습니다.\n");

        float[][] causalWeights = Attention.causalSelfAttention(embeddings);

        System.out.println("Causal Attention 가중치:");
        System.out.printf("%12s", "");
        for (String token : tokens) {
            System.out.printf("%10s", token);
        }
        System.out.println();

        for (int i = 0; i < tokens.length; i++) {
            System.out.printf("%10s: ", tokens[i]);
            for (int j = 0; j < tokens.length; j++) {
                System.out.printf("%10.3f", causalWeights[i][j]);
            }
            System.out.println();
        }

        System.out.println("\n해석:");
        System.out.println("- '고양이가': 자기 자신만 참조 (1.0)");
        System.out.println("- '생선을': '고양이가'와 자신 참조");
        System.out.println("- '먹었다': 이전 모든 단어 참조 (미래 단어 X)");

        // 4. Transformer Block
        System.out.println("\n[ 4. Transformer Block ]");
        System.out.println("----------------------------------------");
        System.out.println("Self-Attention + Feed Forward + Residual\n");

        TransformerBlock block = new TransformerBlock(16);
        float[][] output = block.forward(embeddings, true);

        System.out.println("입력 임베딩 (처음 4차원):");
        for (int i = 0; i < tokens.length; i++) {
            System.out.printf("  %s: %s%n", tokens[i], vectorToString(embeddings[i], 4));
        }

        System.out.println("\n출력 임베딩 (처음 4차원):");
        for (int i = 0; i < tokens.length; i++) {
            System.out.printf("  %s: %s%n", tokens[i], vectorToString(output[i], 4));
        }

        System.out.println("\n→ Attention을 통해 문맥 정보가 반영됨");

        // 5. 여러 레이어
        System.out.println("\n[ 5. 다중 Transformer 레이어 ]");
        System.out.println("----------------------------------------");

        int numLayers = 3;
        float[][] current = embeddings;

        System.out.println("GPT/Claude는 수십 개의 Transformer Block을 쌓습니다.\n");
        System.out.println("Layer 0 (입력): " + vectorToString(current[0], 4));

        for (int layer = 0; layer < numLayers; layer++) {
            TransformerBlock layerBlock = new TransformerBlock(16);
            current = layerBlock.forward(current, true);
            System.out.printf("Layer %d (출력): %s%n", layer + 1, vectorToString(current[0], 4));
        }

        System.out.println("\n→ 레이어를 거칠수록 더 추상적인 표현으로 변환");

        // 요약
        System.out.println("\n========================================");
        System.out.println("핵심 정리");
        System.out.println("========================================");
        System.out.println("1. Attention: 중요한 것에 집중");
        System.out.println("2. Self-Attention: 문장 내 관계 파악");
        System.out.println("3. Position Encoding: 순서 정보 추가");
        System.out.println("4. Causal Mask: 미래 토큰 가림 (GPT)");
        System.out.println("5. Transformer Block: Attention + FFN + Residual");
        System.out.println("\nGPT-4: ~96개 Transformer Block");
        System.out.println("Claude: 비슷한 규모의 Transformer 구조");
    }

    private static String vectorToString(float[] vec) {
        return vectorToString(vec, 8);
    }

    private static String vectorToString(float[] vec, int maxShow) {
        StringBuilder sb = new StringBuilder("[");
        int show = Math.min(maxShow, vec.length);
        for (int i = 0; i < show; i++) {
            if (i > 0) sb.append(", ");
            sb.append(String.format("%.2f", vec[i]));
        }
        if (vec.length > show) {
            sb.append(", ...");
        }
        sb.append("]");
        return sb.toString();
    }
}
