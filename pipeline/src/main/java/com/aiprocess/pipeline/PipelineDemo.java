package com.aiprocess.pipeline;

import com.aiprocess.step1.WhitespaceTokenizer;
import com.aiprocess.step3.BigramModel;
import com.aiprocess.step4.Sampler;

import java.util.Arrays;

/**
 * AI 파이프라인 데모
 *
 * 전체 AI 프로세스를 시각화합니다.
 */
public class PipelineDemo {

    public static void main(String[] args) {
        System.out.println("╔".repeat(1) + "═".repeat(58) + "╗".repeat(1));
        System.out.println("║" + " ".repeat(10) + "AI Process 전체 파이프라인 데모" + " ".repeat(17) + "║");
        System.out.println("╚".repeat(1) + "═".repeat(58) + "╝".repeat(1));
        System.out.println();

        // 학습 데이터
        String corpus = """
            오늘 날씨 정말 좋다
            오늘 날씨 정말 맑다
            오늘 날씨 좋아요
            내일 날씨 어떨까
            내일 비 올까요
            """;

        // 1. 토크나이저 준비
        WhitespaceTokenizer tokenizer = WhitespaceTokenizer.fromText(corpus);
        System.out.println("📚 Vocabulary 준비 완료: " + tokenizer.vocabSize() + " tokens");
        System.out.println();

        // 2. 모델 학습
        BigramModel model = new BigramModel();
        for (String line : corpus.split("\n")) {
            if (line.trim().isEmpty()) continue;
            int[] tokens = tokenizer.encode(line.trim())
                .stream().mapToInt(Integer::intValue).toArray();
            model.train(tokens);
        }
        System.out.println("🧠 모델 학습 완료: " + model);
        System.out.println();

        // 3. 파이프라인 구성
        AIPipeline pipeline = new AIPipeline.Builder()
            .tokenizer(tokenizer)
            .model(model)
            .sampler(new Sampler(0.8))
            .maxTokens(5)
            .verbose(true)  // 상세 출력
            .build();

        // 4. 실행
        System.out.println("┌" + "─".repeat(58) + "┐");
        System.out.println("│" + " ".repeat(15) + "파이프라인 실행 시작" + " ".repeat(23) + "│");
        System.out.println("└" + "─".repeat(58) + "┘");
        System.out.println();

        String prompt = "오늘 날씨";
        AIPipeline.PipelineResult result = pipeline.run(prompt);

        // 5. 결과 출력
        System.out.println("┌" + "─".repeat(58) + "┐");
        System.out.println("│" + " ".repeat(20) + "최종 결과" + " ".repeat(29) + "│");
        System.out.println("└" + "─".repeat(58) + "┘");
        System.out.println();
        System.out.println(result);
        System.out.println();

        // 6. 프로세스 요약
        printProcessSummary();
    }

    private static void printProcessSummary() {
        System.out.println("┌" + "─".repeat(58) + "┐");
        System.out.println("│" + " ".repeat(17) + "AI 프로세스 요약" + " ".repeat(25) + "│");
        System.out.println("└" + "─".repeat(58) + "┘");
        System.out.println();
        System.out.println("""
              사용자: "오늘 날씨 어때?"
                      │
                      ▼
              ┌─────────────────────────────────────────┐
              │ STEP 1: 토큰화                           │
              │ "오늘 날씨 어때?" → [오늘][날씨][어때][?] │
              └─────────────────────────────────────────┘
                      │
                      ▼
              ┌─────────────────────────────────────────┐
              │ STEP 2: 컨텍스트 구성                    │
              │ 시스템 프롬프트 + 히스토리 + 현재 입력   │
              └─────────────────────────────────────────┘
                      │
                      ▼
              ┌─────────────────────────────────────────┐
              │ STEP 3: 확률 계산                        │
              │ 다음 토큰 후보들의 확률 분포 계산         │
              └─────────────────────────────────────────┘
                      │
                      ▼
              ┌─────────────────────────────────────────┐
              │ STEP 4: 샘플링                           │
              │ Temperature/Top-K/Top-P 적용하여 선택   │
              └─────────────────────────────────────────┘
                      │
                      ▼
              ┌─────────────────────────────────────────┐
              │ STEP 5: 생성 루프                        │
              │ 토큰 하나씩 생성 (Auto-regressive)       │
              └─────────────────────────────────────────┘
                      │
                      ▼
              ┌─────────────────────────────────────────┐
              │ STEP 6: 후처리                           │
              │ 안전 필터 + 출력 포맷팅                  │
              └─────────────────────────────────────────┘
                      │
                      ▼
              AI: "오늘 날씨 정말 좋네요!"
            """);

        System.out.println("💡 핵심 포인트:");
        System.out.println("   1. AI는 '토큰' 단위로 작동합니다");
        System.out.println("   2. 다음 토큰은 '확률적으로' 선택됩니다");
        System.out.println("   3. 한 번에 전체 답변이 아닌 '토큰 단위로' 생성됩니다");
        System.out.println("   4. 생성 후 '안전 필터'를 거칩니다");
    }
}
