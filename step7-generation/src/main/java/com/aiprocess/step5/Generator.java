package com.aiprocess.step5;

import com.aiprocess.step3.ProbabilityModel;
import com.aiprocess.step4.Sampler;

import java.util.*;
import java.util.function.Consumer;

/**
 * STEP 5: 생성 루프 (Auto-regressive Generation)
 *
 * 핵심 질문: 한 번에 쓰는가, 한 글자씩 쓰는가?
 *
 * AI는 텍스트를 "한 토큰씩" 생성합니다:
 *
 * ┌─────────────────────────────────────────────────────────┐
 * │ 입력: "오늘 날씨"                                        │
 * │                                                          │
 * │ Step 1: 다음 토큰 예측 → "좋다" 선택                      │
 * │ Step 2: "오늘 날씨 좋다" → 다음 예측 → "고" 선택          │
 * │ Step 3: "오늘 날씨 좋다고" → 다음 예측 → "생각해" 선택    │
 * │ Step 4: ... (max_tokens 도달 또는 [EOS] 생성까지)        │
 * │                                                          │
 * │ 최종: "오늘 날씨 좋다고 생각해요!"                        │
 * └─────────────────────────────────────────────────────────┘
 *
 * 이것을 "Auto-regressive" 생성이라고 합니다.
 * - 이전에 생성한 토큰이 다음 토큰 예측에 영향
 * - 스트리밍 출력이 가능한 이유
 */
public class Generator {

    private final ProbabilityModel model;
    private final Sampler sampler;
    private final int maxTokens;
    private final List<String> stopSequences;
    private final int eosToken;

    public Generator(ProbabilityModel model, Sampler sampler, int maxTokens) {
        this(model, sampler, maxTokens, Collections.emptyList(), -1);
    }

    public Generator(ProbabilityModel model, Sampler sampler, int maxTokens,
                     List<String> stopSequences, int eosToken) {
        this.model = model;
        this.sampler = sampler;
        this.maxTokens = maxTokens;
        this.stopSequences = stopSequences;
        this.eosToken = eosToken;
    }

    /**
     * 텍스트 생성
     *
     * @param promptTokens 입력 토큰들
     * @return 생성된 토큰들 (프롬프트 포함)
     */
    public List<Integer> generate(int[] promptTokens) {
        return generate(promptTokens, null);
    }

    /**
     * 텍스트 생성 (스트리밍)
     *
     * @param promptTokens 입력 토큰들
     * @param onToken 각 토큰 생성 시 콜백
     * @return 생성된 토큰들 (프롬프트 포함)
     */
    public List<Integer> generate(int[] promptTokens, Consumer<Integer> onToken) {
        List<Integer> tokens = new ArrayList<>();
        for (int t : promptTokens) {
            tokens.add(t);
        }

        for (int i = 0; i < maxTokens; i++) {
            // 현재 컨텍스트로 다음 토큰 확률 계산
            int[] context = tokens.stream().mapToInt(Integer::intValue).toArray();
            Map<Integer, Double> probs = model.getNextTokenProbabilities(context);

            if (probs.isEmpty()) {
                // 더 이상 예측 불가 (dead end)
                break;
            }

            // 샘플링으로 다음 토큰 선택
            int nextToken = sampler.sample(probs);
            tokens.add(nextToken);

            // 스트리밍 콜백
            if (onToken != null) {
                onToken.accept(nextToken);
            }

            // 종료 조건 체크
            if (shouldStop(nextToken, tokens)) {
                break;
            }
        }

        return tokens;
    }

    /**
     * 종료 조건 확인
     */
    private boolean shouldStop(int token, List<Integer> tokens) {
        // EOS 토큰 체크
        if (token == eosToken) {
            return true;
        }

        // Stop sequences는 토큰을 텍스트로 변환해야 체크 가능
        // 여기서는 간단히 생략 (Pipeline에서 처리)
        return false;
    }

    /**
     * 생성 결과
     */
    public record GenerationResult(
        List<Integer> tokens,
        int promptTokens,
        int generatedTokens,
        long latencyMs
    ) {}

    @Override
    public String toString() {
        return String.format("Generator(maxTokens=%d, sampler=%s)",
            maxTokens, sampler);
    }
}
