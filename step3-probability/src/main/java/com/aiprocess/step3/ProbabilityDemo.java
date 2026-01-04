package com.aiprocess.step3;

import java.util.*;

/**
 * STEP 3: 확률 계산 데모
 *
 * AI가 다음 단어를 예측하는 원리를 시각화합니다.
 */
public class ProbabilityDemo {

    public static void main(String[] args) {
        System.out.println("═".repeat(60));
        System.out.println("STEP 3: 확률 계산 (Probability Calculation)");
        System.out.println("═".repeat(60));
        System.out.println();
        System.out.println("핵심 질문: 다음 단어를 어떻게 예측하는가?");
        System.out.println();

        // 1. Bigram 모델 데모
        demoBigramModel();

        System.out.println();

        // 2. Trigram 모델 데모
        demoTrigramModel();

        System.out.println();

        // 3. 확률 분포 시각화
        demoProbabilityDistribution();
    }

    private static void demoBigramModel() {
        System.out.println("─".repeat(60));
        System.out.println("1. Bigram 모델 - 바로 앞 토큰만 보고 예측");
        System.out.println("─".repeat(60));
        System.out.println();

        // 간단한 vocabulary
        Map<String, Integer> vocab = new HashMap<>();
        vocab.put("오늘", 0);
        vocab.put("내일", 1);
        vocab.put("날씨", 2);
        vocab.put("좋다", 3);
        vocab.put("맑다", 4);
        vocab.put("흐리다", 5);

        Map<Integer, String> idToWord = new HashMap<>();
        vocab.forEach((k, v) -> idToWord.put(v, k));

        // 학습 데이터
        String[] corpus = {
            "오늘 날씨 좋다",
            "오늘 날씨 맑다",
            "오늘 날씨 맑다",
            "내일 날씨 흐리다"
        };

        System.out.println("  학습 데이터:");
        for (String sentence : corpus) {
            System.out.println("    - \"" + sentence + "\"");
        }
        System.out.println();

        // 모델 학습
        BigramModel model = new BigramModel();
        for (String sentence : corpus) {
            int[] tokens = Arrays.stream(sentence.split(" "))
                .mapToInt(vocab::get)
                .toArray();
            model.train(tokens);
        }

        // "날씨" 다음에 올 토큰 확률
        int prevToken = vocab.get("날씨");
        Map<Integer, Double> probs = model.getNextTokenProbabilities(new int[]{prevToken});

        System.out.println("  P(next | \"날씨\") = ?");
        System.out.println();
        System.out.println("  ┌─────────────────────────────────────────┐");

        List<Map.Entry<Integer, Double>> sorted = new ArrayList<>(probs.entrySet());
        sorted.sort((a, b) -> Double.compare(b.getValue(), a.getValue()));

        for (Map.Entry<Integer, Double> entry : sorted) {
            String word = idToWord.get(entry.getKey());
            double prob = entry.getValue();
            int barLen = (int) (prob * 30);
            String bar = "█".repeat(barLen);
            System.out.printf("  │ %-6s : %5.1f%% %s%n", word, prob * 100, bar);
        }
        System.out.println("  └─────────────────────────────────────────┘");
    }

    private static void demoTrigramModel() {
        System.out.println("─".repeat(60));
        System.out.println("2. Trigram 모델 - 앞 2개 토큰을 보고 예측");
        System.out.println("─".repeat(60));
        System.out.println();

        Map<String, Integer> vocab = new HashMap<>();
        vocab.put("오늘", 0);
        vocab.put("내일", 1);
        vocab.put("날씨", 2);
        vocab.put("정말", 3);
        vocab.put("좋다", 4);
        vocab.put("맑다", 5);

        Map<Integer, String> idToWord = new HashMap<>();
        vocab.forEach((k, v) -> idToWord.put(v, k));

        // 학습 데이터
        String[] corpus = {
            "오늘 날씨 정말 좋다",
            "오늘 날씨 정말 맑다",
            "내일 날씨 정말 좋다"
        };

        System.out.println("  학습 데이터:");
        for (String sentence : corpus) {
            System.out.println("    - \"" + sentence + "\"");
        }
        System.out.println();

        // 모델 학습
        TrigramModel model = new TrigramModel();
        for (String sentence : corpus) {
            int[] tokens = Arrays.stream(sentence.split(" "))
                .mapToInt(vocab::get)
                .toArray();
            model.train(tokens);
        }

        System.out.println("  ┌─────────────────────────────────────────┐");
        System.out.println("  │ Bigram vs Trigram 비교                   │");
        System.out.println("  └─────────────────────────────────────────┘");
        System.out.println();

        // Bigram: "정말" 다음
        System.out.println("  Bigram: P(next | \"정말\")");
        int[] context1 = {vocab.get("정말")};
        printProbabilities(model.getNextTokenProbabilities(context1), idToWord);

        System.out.println();

        // Trigram: "날씨 정말" 다음
        System.out.println("  Trigram: P(next | \"날씨\", \"정말\")");
        int[] context2 = {vocab.get("날씨"), vocab.get("정말")};
        printProbabilities(model.getNextTokenProbabilities(context2), idToWord);
    }

    private static void printProbabilities(Map<Integer, Double> probs, Map<Integer, String> idToWord) {
        List<Map.Entry<Integer, Double>> sorted = new ArrayList<>(probs.entrySet());
        sorted.sort((a, b) -> Double.compare(b.getValue(), a.getValue()));

        for (Map.Entry<Integer, Double> entry : sorted) {
            String word = idToWord.get(entry.getKey());
            double prob = entry.getValue();
            int barLen = (int) (prob * 20);
            String bar = "█".repeat(barLen);
            System.out.printf("    %-6s : %5.1f%% %s%n", word, prob * 100, bar);
        }
    }

    private static void demoProbabilityDistribution() {
        System.out.println("─".repeat(60));
        System.out.println("3. 실제 AI의 확률 분포");
        System.out.println("─".repeat(60));
        System.out.println();

        System.out.println("  ┌─────────────────────────────────────────────────┐");
        System.out.println("  │ 입력: \"오늘 날씨\"                                │");
        System.out.println("  │                                                  │");
        System.out.println("  │ 실제 AI (GPT, Claude 등)의 다음 토큰 분포:        │");
        System.out.println("  │                                                  │");
        System.out.println("  │   \"좋다\"   : 35% ████████████████████            │");
        System.out.println("  │   \"맑다\"   : 25% ██████████████                  │");
        System.out.println("  │   \"어때\"   : 15% █████████                       │");
        System.out.println("  │   \"흐리다\" : 10% ██████                          │");
        System.out.println("  │   \"덥다\"   :  8% █████                           │");
        System.out.println("  │   \"춥다\"   :  5% ███                             │");
        System.out.println("  │   기타     :  2% █                               │");
        System.out.println("  └─────────────────────────────────────────────────┘");
        System.out.println();

        System.out.println("  💡 N-gram vs Transformer:");
        System.out.println();
        System.out.println("  ┌───────────────────────────────────────────┐");
        System.out.println("  │ N-gram (우리 구현)                         │");
        System.out.println("  │ - 고정된 N개 토큰만 참조                    │");
        System.out.println("  │ - 단순하지만 장거리 의존성 처리 불가         │");
        System.out.println("  │                                            │");
        System.out.println("  │ Transformer (GPT, Claude)                  │");
        System.out.println("  │ - Attention으로 전체 컨텍스트 참조          │");
        System.out.println("  │ - 수천 토큰 이전 정보도 활용 가능            │");
        System.out.println("  └───────────────────────────────────────────┘");
    }
}
