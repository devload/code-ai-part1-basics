package com.aiprocess.step5;

import com.aiprocess.step3.BigramModel;
import com.aiprocess.step4.Sampler;

import java.util.*;

/**
 * STEP 5: 생성 루프 데모
 *
 * AI가 토큰을 하나씩 생성하는 과정을 시각화합니다.
 */
public class GenerationDemo {

    public static void main(String[] args) {
        System.out.println("═".repeat(60));
        System.out.println("STEP 5: 생성 루프 (Generation Loop)");
        System.out.println("═".repeat(60));
        System.out.println();
        System.out.println("핵심 질문: 한 번에 쓰는가, 한 글자씩 쓰는가?");
        System.out.println();

        // 1. Auto-regressive 생성 데모
        demoAutoRegressive();

        System.out.println();

        // 2. 스트리밍 출력 데모
        demoStreaming();

        System.out.println();

        // 3. 종료 조건 데모
        demoStopConditions();
    }

    private static void demoAutoRegressive() {
        System.out.println("─".repeat(60));
        System.out.println("1. Auto-regressive 생성");
        System.out.println("─".repeat(60));
        System.out.println();

        // Vocabulary 설정
        Map<String, Integer> vocab = new HashMap<>();
        vocab.put("오늘", 0);
        vocab.put("날씨", 1);
        vocab.put("정말", 2);
        vocab.put("좋다", 3);
        vocab.put("맑다", 4);
        vocab.put("!", 5);
        vocab.put(".", 6);

        Map<Integer, String> idToWord = new HashMap<>();
        vocab.forEach((k, v) -> idToWord.put(v, k));

        // 학습 데이터
        String[] corpus = {
            "오늘 날씨 정말 좋다 !",
            "오늘 날씨 정말 맑다 .",
            "오늘 날씨 좋다 ."
        };

        // 모델 학습
        BigramModel model = new BigramModel();
        for (String sentence : corpus) {
            int[] tokens = Arrays.stream(sentence.split(" "))
                .mapToInt(vocab::get)
                .toArray();
            model.train(tokens);
        }

        System.out.println("  학습 데이터:");
        for (String s : corpus) {
            System.out.println("    - \"" + s + "\"");
        }
        System.out.println();

        // 생성
        System.out.println("  ┌─────────────────────────────────────────┐");
        System.out.println("  │ 입력: \"오늘 날씨\"                        │");
        System.out.println("  │                                          │");
        System.out.println("  │ Auto-regressive 생성 과정:               │");
        System.out.println("  └─────────────────────────────────────────┘");
        System.out.println();

        int[] prompt = {vocab.get("오늘"), vocab.get("날씨")};
        List<Integer> generated = new ArrayList<>();
        for (int t : prompt) generated.add(t);

        // 한 토큰씩 생성하며 시각화
        Sampler sampler = new Sampler(1.0);

        for (int step = 1; step <= 4; step++) {
            int[] context = generated.stream().mapToInt(Integer::intValue).toArray();
            Map<Integer, Double> probs = model.getNextTokenProbabilities(context);

            if (probs.isEmpty()) break;

            // 확률 분포 출력
            String currentText = generated.stream()
                .map(idToWord::get)
                .reduce((a, b) -> a + " " + b)
                .orElse("");

            System.out.printf("  Step %d: \"%s\" → 다음 토큰?%n", step, currentText);
            System.out.println("    확률:");

            List<Map.Entry<Integer, Double>> sorted = new ArrayList<>(probs.entrySet());
            sorted.sort((a, b) -> Double.compare(b.getValue(), a.getValue()));
            for (Map.Entry<Integer, Double> e : sorted) {
                String word = idToWord.get(e.getKey());
                int barLen = (int) (e.getValue() * 20);
                System.out.printf("      %-6s : %5.1f%% %s%n",
                    word, e.getValue() * 100, "█".repeat(barLen));
            }

            // 샘플링
            int next = sampler.sample(probs);
            generated.add(next);
            System.out.println("    → 선택: \"" + idToWord.get(next) + "\"");
            System.out.println();
        }

        String result = generated.stream()
            .map(idToWord::get)
            .reduce((a, b) -> a + " " + b)
            .orElse("");
        System.out.println("  최종 결과: \"" + result + "\"");
    }

    private static void demoStreaming() {
        System.out.println("─".repeat(60));
        System.out.println("2. 스트리밍 출력");
        System.out.println("─".repeat(60));
        System.out.println();

        System.out.println("  ┌─────────────────────────────────────────┐");
        System.out.println("  │ 왜 ChatGPT 답변이 타이핑되듯 나올까?      │");
        System.out.println("  │                                          │");
        System.out.println("  │ → Auto-regressive 생성이기 때문!         │");
        System.out.println("  │ → 토큰 하나 생성할 때마다 바로 출력 가능   │");
        System.out.println("  └─────────────────────────────────────────┘");
        System.out.println();

        System.out.println("  시뮬레이션: 스트리밍 출력");
        System.out.println();
        System.out.print("  AI: ");

        String[] tokens = {"오늘", " ", "날씨", "가", " ", "정말", " ", "좋네요", "!"};
        for (String token : tokens) {
            System.out.print(token);
            System.out.flush();
            try {
                Thread.sleep(200);  // 실제 생성 시간 시뮬레이션
            } catch (InterruptedException e) {
                // ignore
            }
        }
        System.out.println();
        System.out.println();

        System.out.println("  💡 스트리밍의 장점:");
        System.out.println("    - 사용자가 빨리 결과를 볼 수 있음");
        System.out.println("    - 전체 생성 완료 전에 읽기 시작 가능");
        System.out.println("    - 중간에 취소 가능");
    }

    private static void demoStopConditions() {
        System.out.println("─".repeat(60));
        System.out.println("3. 종료 조건");
        System.out.println("─".repeat(60));
        System.out.println();

        System.out.println("  언제 생성을 멈출까?");
        System.out.println();

        System.out.println("  ┌─────────────────────────────────────────┐");
        System.out.println("  │ 1. EOS (End of Sequence) 토큰 생성       │");
        System.out.println("  │    - 모델이 \"문장 끝\"을 나타내는 토큰 출력│");
        System.out.println("  │    - 예: [EOS], </s>, <|endoftext|>      │");
        System.out.println("  │                                          │");
        System.out.println("  │ 2. max_tokens 도달                       │");
        System.out.println("  │    - 최대 토큰 수 제한                    │");
        System.out.println("  │    - 비용 제어 목적                       │");
        System.out.println("  │                                          │");
        System.out.println("  │ 3. Stop Sequences                        │");
        System.out.println("  │    - 특정 문자열 출력 시 중단              │");
        System.out.println("  │    - 예: \"\\n\\n\", \"###\", \"END\"           │");
        System.out.println("  │                                          │");
        System.out.println("  │ 4. Dead End                              │");
        System.out.println("  │    - 다음 토큰 예측 불가                   │");
        System.out.println("  │    - N-gram 모델에서 발생 가능             │");
        System.out.println("  └─────────────────────────────────────────┘");
        System.out.println();

        System.out.println("  예시: stop_sequences = [\"\\n\\n\"]");
        System.out.println();
        System.out.println("    \"오늘 날씨가 좋습니다.\\n\"");
        System.out.println("    \"내일은 비가 올 예정입니다.\\n\"");
        System.out.println("    \"\\n\"  ← 여기서 중단! (\\n\\n 감지)");
    }
}
