package com.aiprocess.step4;

import java.util.*;

/**
 * STEP 4: 샘플링 데모
 *
 * 왜 같은 질문에 다른 답이 나오는지 시각화합니다.
 */
public class SamplingDemo {

    public static void main(String[] args) {
        System.out.println("═".repeat(60));
        System.out.println("STEP 4: 샘플링 (Sampling)");
        System.out.println("═".repeat(60));
        System.out.println();
        System.out.println("핵심 질문: 왜 매번 답이 다른가?");
        System.out.println();

        // 1. Greedy vs Sampling
        demoGreedyVsSampling();

        System.out.println();

        // 2. Temperature 효과
        demoTemperatureEffect();

        System.out.println();

        // 3. Top-K와 Top-P
        demoTopKAndTopP();
    }

    private static void demoGreedyVsSampling() {
        System.out.println("─".repeat(60));
        System.out.println("1. Greedy vs Random Sampling");
        System.out.println("─".repeat(60));
        System.out.println();

        // 확률 분포
        Map<Integer, Double> probs = new LinkedHashMap<>();
        probs.put(0, 0.40);  // "좋다"
        probs.put(1, 0.30);  // "맑다"
        probs.put(2, 0.20);  // "덥다"
        probs.put(3, 0.10);  // "흐리다"

        Map<Integer, String> idToWord = Map.of(
            0, "좋다", 1, "맑다", 2, "덥다", 3, "흐리다"
        );

        System.out.println("  확률 분포 (\"오늘 날씨\" 다음 토큰):");
        for (Map.Entry<Integer, Double> e : probs.entrySet()) {
            String word = idToWord.get(e.getKey());
            int barLen = (int) (e.getValue() * 30);
            System.out.printf("    %-6s : %5.1f%% %s%n",
                word, e.getValue() * 100, "█".repeat(barLen));
        }
        System.out.println();

        // Greedy (temperature = 0)
        System.out.println("  ┌─────────────────────────────────────────┐");
        System.out.println("  │ Greedy (temperature = 0)                │");
        System.out.println("  │ → 항상 가장 확률 높은 토큰 선택          │");
        System.out.println("  └─────────────────────────────────────────┘");
        Sampler greedy = new Sampler(0, 0, 1.0, 42L);
        System.out.print("    결과: ");
        for (int i = 0; i < 5; i++) {
            int token = greedy.sample(probs);
            System.out.print(idToWord.get(token) + " ");
        }
        System.out.println("← 항상 같음");
        System.out.println();

        // Random Sampling (temperature = 1.0)
        System.out.println("  ┌─────────────────────────────────────────┐");
        System.out.println("  │ Random Sampling (temperature = 1.0)     │");
        System.out.println("  │ → 확률에 따라 랜덤 선택                  │");
        System.out.println("  └─────────────────────────────────────────┘");
        Sampler random = new Sampler(1.0, 0, 1.0, null);
        System.out.print("    결과: ");
        for (int i = 0; i < 5; i++) {
            int token = random.sample(probs);
            System.out.print(idToWord.get(token) + " ");
        }
        System.out.println("← 매번 다를 수 있음");
    }

    private static void demoTemperatureEffect() {
        System.out.println("─".repeat(60));
        System.out.println("2. Temperature 효과");
        System.out.println("─".repeat(60));
        System.out.println();

        Map<Integer, Double> probs = new LinkedHashMap<>();
        probs.put(0, 0.50);
        probs.put(1, 0.30);
        probs.put(2, 0.15);
        probs.put(3, 0.05);

        Map<Integer, String> labels = Map.of(
            0, "A(50%)", 1, "B(30%)", 2, "C(15%)", 3, "D(5%)"
        );

        double[] temps = {0.3, 0.7, 1.0, 1.5, 2.0};

        System.out.println("  Temperature가 확률 분포에 미치는 영향:");
        System.out.println();

        for (double temp : temps) {
            System.out.printf("  temp = %.1f%n", temp);

            // Temperature 적용 후 분포 시각화
            Map<Integer, Integer> counts = new HashMap<>();
            Sampler sampler = new Sampler(temp, 0, 1.0, 42L);

            // 100번 샘플링
            for (int i = 0; i < 100; i++) {
                int token = sampler.sample(probs);
                counts.merge(token, 1, Integer::sum);
            }

            // 결과 출력
            for (int id = 0; id <= 3; id++) {
                int count = counts.getOrDefault(id, 0);
                int barLen = count / 3;
                System.out.printf("    %s : %3d회 %s%n",
                    labels.get(id), count, "█".repeat(barLen));
            }
            System.out.println();
        }

        System.out.println("  💡 Temperature 요약:");
        System.out.println("    - 낮음 (0.3): 집중적 → 가장 높은 확률만 선택");
        System.out.println("    - 중간 (1.0): 균형 → 원래 확률대로");
        System.out.println("    - 높음 (2.0): 분산적 → 낮은 확률도 선택됨");
    }

    private static void demoTopKAndTopP() {
        System.out.println("─".repeat(60));
        System.out.println("3. Top-K와 Top-P (Nucleus) 샘플링");
        System.out.println("─".repeat(60));
        System.out.println();

        Map<Integer, Double> probs = new LinkedHashMap<>();
        probs.put(0, 0.35);  // A
        probs.put(1, 0.25);  // B
        probs.put(2, 0.20);  // C
        probs.put(3, 0.10);  // D
        probs.put(4, 0.05);  // E
        probs.put(5, 0.03);  // F
        probs.put(6, 0.02);  // G

        String[] names = {"A", "B", "C", "D", "E", "F", "G"};

        System.out.println("  원본 분포:");
        double cumulative = 0;
        for (int i = 0; i < 7; i++) {
            double prob = probs.get(i);
            cumulative += prob;
            System.out.printf("    %s: %5.1f%% (누적: %5.1f%%)%n",
                names[i], prob * 100, cumulative * 100);
        }
        System.out.println();

        // Top-K
        System.out.println("  ┌─────────────────────────────────────────┐");
        System.out.println("  │ Top-K (K=3)                             │");
        System.out.println("  │ → 상위 3개만 후보로 고려                  │");
        System.out.println("  │ → A, B, C만 선택 가능                    │");
        System.out.println("  └─────────────────────────────────────────┘");

        Sampler topKSampler = new Sampler(1.0, 3, 1.0, null);
        Map<Integer, Integer> topKCounts = new HashMap<>();
        for (int i = 0; i < 50; i++) {
            int token = topKSampler.sample(probs);
            topKCounts.merge(token, 1, Integer::sum);
        }
        System.out.print("    50회 샘플링: ");
        for (int i = 0; i < 7; i++) {
            int count = topKCounts.getOrDefault(i, 0);
            if (count > 0) System.out.printf("%s(%d) ", names[i], count);
        }
        System.out.println();
        System.out.println();

        // Top-P
        System.out.println("  ┌─────────────────────────────────────────┐");
        System.out.println("  │ Top-P (P=0.9)                           │");
        System.out.println("  │ → 누적 확률 90%까지만 후보로 고려         │");
        System.out.println("  │ → A, B, C, D만 선택 가능 (0.35+0.25+0.20+0.10=0.90) │");
        System.out.println("  └─────────────────────────────────────────┘");

        Sampler topPSampler = new Sampler(1.0, 0, 0.9, null);
        Map<Integer, Integer> topPCounts = new HashMap<>();
        for (int i = 0; i < 50; i++) {
            int token = topPSampler.sample(probs);
            topPCounts.merge(token, 1, Integer::sum);
        }
        System.out.print("    50회 샘플링: ");
        for (int i = 0; i < 7; i++) {
            int count = topPCounts.getOrDefault(i, 0);
            if (count > 0) System.out.printf("%s(%d) ", names[i], count);
        }
        System.out.println();
        System.out.println();

        System.out.println("  💡 실제 사용:");
        System.out.println("    - ChatGPT: temperature + top_p 조합 사용");
        System.out.println("    - Claude: temperature 주로 사용");
        System.out.println("    - 코드 생성: 낮은 temperature (정확성)");
        System.out.println("    - 창작 글: 높은 temperature (다양성)");
    }
}
