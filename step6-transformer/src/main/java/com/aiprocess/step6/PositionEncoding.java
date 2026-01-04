package com.aiprocess.step6;

/**
 * Position Encoding: 토큰의 위치 정보를 추가
 *
 * Transformer는 순서를 모르기 때문에 위치 정보를 별도로 추가해야 함
 */
public class PositionEncoding {

    /**
     * Sinusoidal Position Encoding (원본 Transformer 논문 방식)
     *
     * PE(pos, 2i) = sin(pos / 10000^(2i/d))
     * PE(pos, 2i+1) = cos(pos / 10000^(2i/d))
     */
    public static float[] encode(int position, int dimension) {
        float[] encoding = new float[dimension];

        for (int i = 0; i < dimension; i++) {
            double angle = position / Math.pow(10000, (2.0 * (i / 2)) / dimension);

            if (i % 2 == 0) {
                encoding[i] = (float) Math.sin(angle);
            } else {
                encoding[i] = (float) Math.cos(angle);
            }
        }

        return encoding;
    }

    /**
     * 임베딩에 위치 인코딩을 더함
     */
    public static float[] addPositionEncoding(float[] embedding, int position) {
        float[] posEnc = encode(position, embedding.length);
        float[] result = new float[embedding.length];

        for (int i = 0; i < embedding.length; i++) {
            result[i] = embedding[i] + posEnc[i];
        }

        return result;
    }

    /**
     * 문장의 모든 토큰에 위치 인코딩 추가
     */
    public static float[][] addPositionEncodings(float[][] embeddings) {
        float[][] result = new float[embeddings.length][];

        for (int pos = 0; pos < embeddings.length; pos++) {
            result[pos] = addPositionEncoding(embeddings[pos], pos);
        }

        return result;
    }
}
