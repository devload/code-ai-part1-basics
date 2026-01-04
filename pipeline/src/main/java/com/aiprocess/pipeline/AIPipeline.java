package com.aiprocess.pipeline;

import com.aiprocess.step1.Tokenizer;
import com.aiprocess.step1.WhitespaceTokenizer;
import com.aiprocess.step2.ContextBuilder;
import com.aiprocess.step3.BigramModel;
import com.aiprocess.step3.ProbabilityModel;
import com.aiprocess.step4.Sampler;
import com.aiprocess.step5.Generator;
import com.aiprocess.step6.OutputFormatter;
import com.aiprocess.step6.SafetyFilter;

import java.util.List;
import java.util.function.Consumer;

/**
 * AI 파이프라인 - 전체 프로세스 통합
 *
 * AI가 입력을 받아 응답을 생성하기까지의 전체 과정:
 *
 * ┌─────────────────────────────────────────────────────────┐
 * │ 사용자 입력: "오늘 날씨 어때?"                           │
 * └─────────────────────────────────────────────────────────┘
 *                         │
 *                         ▼
 * ┌─────────────────────────────────────────────────────────┐
 * │ STEP 1: 토큰화                                          │
 * │ "오늘 날씨 어때?" → [오늘][날씨][어때][?]                │
 * └─────────────────────────────────────────────────────────┘
 *                         │
 *                         ▼
 * ┌─────────────────────────────────────────────────────────┐
 * │ STEP 2: 컨텍스트 구성                                    │
 * │ [시스템 프롬프트] + [히스토리] + [사용자 입력]           │
 * └─────────────────────────────────────────────────────────┘
 *                         │
 *                         ▼
 * ┌─────────────────────────────────────────────────────────┐
 * │ STEP 3: 확률 계산                                        │
 * │ P(다음 토큰 | 컨텍스트)                                  │
 * └─────────────────────────────────────────────────────────┘
 *                         │
 *                         ▼
 * ┌─────────────────────────────────────────────────────────┐
 * │ STEP 4: 샘플링                                          │
 * │ Temperature, Top-K, Top-P 적용 → 토큰 선택              │
 * └─────────────────────────────────────────────────────────┘
 *                         │
 *                         ▼
 * ┌─────────────────────────────────────────────────────────┐
 * │ STEP 5: 생성 루프                                        │
 * │ 토큰 생성 반복 (Auto-regressive)                        │
 * └─────────────────────────────────────────────────────────┘
 *                         │
 *                         ▼
 * ┌─────────────────────────────────────────────────────────┐
 * │ STEP 6: 후처리                                          │
 * │ 안전 필터 + 포맷팅                                       │
 * └─────────────────────────────────────────────────────────┘
 *                         │
 *                         ▼
 * ┌─────────────────────────────────────────────────────────┐
 * │ AI 응답: "오늘 날씨 정말 좋네요!"                         │
 * └─────────────────────────────────────────────────────────┘
 */
public class AIPipeline {

    private final Tokenizer tokenizer;
    private final ContextBuilder contextBuilder;
    private final ProbabilityModel model;
    private final Sampler sampler;
    private final SafetyFilter safetyFilter;
    private final OutputFormatter formatter;

    private final int maxTokens;
    private final boolean verbose;

    public AIPipeline(Builder builder) {
        this.tokenizer = builder.tokenizer;
        this.contextBuilder = builder.contextBuilder;
        this.model = builder.model;
        this.sampler = builder.sampler;
        this.safetyFilter = builder.safetyFilter;
        this.formatter = builder.formatter;
        this.maxTokens = builder.maxTokens;
        this.verbose = builder.verbose;
    }

    /**
     * 텍스트 생성
     */
    public PipelineResult run(String prompt) {
        return run(prompt, null);
    }

    /**
     * 텍스트 생성 (스트리밍)
     */
    public PipelineResult run(String prompt, Consumer<String> onToken) {
        long startTime = System.currentTimeMillis();

        // STEP 1: 토큰화
        if (verbose) {
            System.out.println("═".repeat(50));
            System.out.println("STEP 1: 토큰화");
            System.out.println("═".repeat(50));
        }
        List<String> tokens = tokenizer.tokenize(prompt);
        List<Integer> tokenIds = tokenizer.encode(prompt);

        if (verbose) {
            System.out.println("  입력: \"" + prompt + "\"");
            System.out.println("  토큰: " + tokens);
            System.out.println("  ID:   " + tokenIds);
            System.out.println();
        }

        // STEP 2: 컨텍스트 구성 (여기서는 단순 처리)
        if (verbose) {
            System.out.println("═".repeat(50));
            System.out.println("STEP 2: 컨텍스트 구성");
            System.out.println("═".repeat(50));
            System.out.println("  프롬프트를 그대로 사용 (단순 모드)");
            System.out.println();
        }

        // STEP 3, 4, 5: 확률 계산 + 샘플링 + 생성 루프
        if (verbose) {
            System.out.println("═".repeat(50));
            System.out.println("STEP 3-5: 확률 계산 → 샘플링 → 생성 루프");
            System.out.println("═".repeat(50));
        }

        Generator generator = new Generator(model, sampler, maxTokens);
        int[] promptTokenArray = tokenIds.stream().mapToInt(Integer::intValue).toArray();

        List<Integer> generated = generator.generate(promptTokenArray, token -> {
            if (onToken != null) {
                onToken.accept(tokenizer.getToken(token));
            }
            if (verbose) {
                System.out.println("  + 생성: " + tokenizer.getToken(token));
            }
        });

        if (verbose) {
            System.out.println();
        }

        // 디코딩
        String rawOutput = tokenizer.decode(generated);

        // STEP 6: 후처리
        if (verbose) {
            System.out.println("═".repeat(50));
            System.out.println("STEP 6: 후처리");
            System.out.println("═".repeat(50));
        }

        // 안전 필터
        SafetyFilter.FilterResult filterResult = safetyFilter.check(rawOutput);
        if (verbose) {
            System.out.println("  안전 필터: " + (filterResult.passed() ? "✅ PASS" : "❌ BLOCK"));
        }

        // 포맷팅
        String finalOutput = formatter.format(rawOutput);
        if (verbose) {
            System.out.println("  포맷팅 완료");
            System.out.println();
        }

        long latency = System.currentTimeMillis() - startTime;

        return new PipelineResult(
            prompt,
            finalOutput,
            tokenIds.size(),
            generated.size() - tokenIds.size(),
            latency,
            filterResult.passed()
        );
    }

    /**
     * 파이프라인 결과
     */
    public record PipelineResult(
        String prompt,
        String output,
        int promptTokens,
        int outputTokens,
        long latencyMs,
        boolean safetyPassed
    ) {
        public int totalTokens() {
            return promptTokens + outputTokens;
        }

        @Override
        public String toString() {
            return String.format(
                "PipelineResult{\n" +
                "  prompt: \"%s\"\n" +
                "  output: \"%s\"\n" +
                "  tokens: %d (prompt) + %d (output) = %d (total)\n" +
                "  latency: %dms\n" +
                "  safety: %s\n" +
                "}",
                prompt, output, promptTokens, outputTokens, totalTokens(),
                latencyMs, safetyPassed ? "PASS" : "BLOCK"
            );
        }
    }

    /**
     * 빌더
     */
    public static class Builder {
        private Tokenizer tokenizer = new WhitespaceTokenizer();
        private ContextBuilder contextBuilder = new ContextBuilder();
        private ProbabilityModel model = new BigramModel();
        private Sampler sampler = new Sampler(1.0);
        private SafetyFilter safetyFilter = new SafetyFilter();
        private OutputFormatter formatter = new OutputFormatter();
        private int maxTokens = 20;
        private boolean verbose = false;

        public Builder tokenizer(Tokenizer tokenizer) {
            this.tokenizer = tokenizer;
            return this;
        }

        public Builder model(ProbabilityModel model) {
            this.model = model;
            return this;
        }

        public Builder sampler(Sampler sampler) {
            this.sampler = sampler;
            return this;
        }

        public Builder maxTokens(int maxTokens) {
            this.maxTokens = maxTokens;
            return this;
        }

        public Builder verbose(boolean verbose) {
            this.verbose = verbose;
            return this;
        }

        public AIPipeline build() {
            return new AIPipeline(this);
        }
    }
}
