package com.aiprocess.step2;

/**
 * STEP 2: 컨텍스트 구성 데모
 *
 * AI가 무엇을 보고 답하는지 시각화합니다.
 */
public class ContextDemo {

    public static void main(String[] args) {
        System.out.println("═".repeat(60));
        System.out.println("STEP 2: 컨텍스트 구성 (Context Assembly)");
        System.out.println("═".repeat(60));
        System.out.println();
        System.out.println("핵심 질문: AI는 뭘 보고 답하는가?");
        System.out.println();

        // 1. 컨텍스트 구성 데모
        demoContextBuilder();

        System.out.println();

        // 2. 대화 메모리 데모
        demoConversationMemory();

        System.out.println();

        // 3. 프롬프트 템플릿 데모
        demoPromptTemplate();
    }

    private static void demoContextBuilder() {
        System.out.println("─".repeat(60));
        System.out.println("1. ContextBuilder - 컨텍스트 조합");
        System.out.println("─".repeat(60));
        System.out.println();

        ContextBuilder builder = new ContextBuilder()
            .withSystemPrompt("너는 친절한 날씨 AI야. 간결하게 답변해.")
            .withMaxTokens(4096);

        // 대화 추가
        builder.addUserMessage("오늘 날씨 어때?");
        builder.addAssistantMessage("오늘은 맑고 화창해요! 기온은 22도입니다.");
        builder.addUserMessage("내일은?");
        builder.addAssistantMessage("내일은 비 소식이 있어요. 우산 챙기세요!");
        builder.addUserMessage("그럼 우산 챙겨야 해?");

        String prompt = builder.buildPrompt();

        System.out.println("  ┌───────────────────────────────────────────┐");
        System.out.println("  │            AI가 보는 전체 컨텍스트         │");
        System.out.println("  └───────────────────────────────────────────┘");
        System.out.println();

        // 컨텍스트 출력
        for (String line : prompt.split("\n")) {
            System.out.println("  │ " + line);
        }
        System.out.println();

        System.out.println("  💡 AI는 이 전체 컨텍스트를 보고 답변을 생성합니다.");
        System.out.println("     \"그럼\"이 \"내일 비 소식\"을 가리킨다는 것을");
        System.out.println("     히스토리를 통해 이해합니다.");
    }

    private static void demoConversationMemory() {
        System.out.println("─".repeat(60));
        System.out.println("2. ConversationMemory - 슬라이딩 윈도우");
        System.out.println("─".repeat(60));
        System.out.println();

        // 최대 4개 메시지만 저장
        ConversationMemory memory = new ConversationMemory(4);

        System.out.println("  ┌───────────────────────────────────────────┐");
        System.out.println("  │ 문제: 컨텍스트 윈도우 제한                  │");
        System.out.println("  │ - GPT-4: 8K ~ 128K tokens                 │");
        System.out.println("  │ - 대화가 길어지면 오래된 내용은 '잊어버림'  │");
        System.out.println("  └───────────────────────────────────────────┘");
        System.out.println();

        System.out.println("  메시지 추가 (최대 4개 유지):");
        System.out.println();

        addAndPrint(memory, "안녕?");
        addAndPrint(memory, "오늘 뭐해?");
        addAndPrint(memory, "날씨가 좋네");
        addAndPrint(memory, "산책 갈까?");
        addAndPrint(memory, "몇 시에?");  // 이 시점에 첫 번째 메시지 삭제
        addAndPrint(memory, "3시 어때?"); // 두 번째 메시지도 삭제

        System.out.println();
        System.out.println("  요약 (삭제된 메시지들):");
        System.out.println("    " + memory.getSummary());
    }

    private static void addAndPrint(ConversationMemory memory, String msg) {
        memory.addUserMessage(msg);
        System.out.printf("    + \"%s\" → 현재 메시지 수: %d%n", msg, memory.size());
    }

    private static void demoPromptTemplate() {
        System.out.println("─".repeat(60));
        System.out.println("3. PromptTemplate - 동적 프롬프트");
        System.out.println("─".repeat(60));
        System.out.println();

        System.out.println("  ┌───────────────────────────────────────────┐");
        System.out.println("  │ 템플릿을 사용하면 프롬프트를 재사용할 수 있음 │");
        System.out.println("  └───────────────────────────────────────────┘");
        System.out.println();

        // 번역가 템플릿
        PromptTemplate translator = new PromptTemplate(
            PromptTemplate.Templates.TRANSLATOR
        );

        translator.set("source_lang", "한국어");
        translator.set("target_lang", "영어");

        System.out.println("  템플릿:");
        System.out.println("    " + PromptTemplate.Templates.TRANSLATOR.replace("\n", "\n    "));
        System.out.println();

        System.out.println("  변수:");
        System.out.println("    source_lang = \"한국어\"");
        System.out.println("    target_lang = \"영어\"");
        System.out.println();

        System.out.println("  렌더링 결과:");
        for (String line : translator.render().split("\n")) {
            if (!line.isEmpty()) {
                System.out.println("    " + line);
            }
        }
        System.out.println();

        // 변수 변경
        translator.set("source_lang", "일본어");
        translator.set("target_lang", "한국어");

        System.out.println("  변수 변경 후:");
        System.out.println("    source_lang = \"일본어\"");
        System.out.println("    target_lang = \"한국어\"");
        System.out.println();
        System.out.println("  렌더링 결과:");
        for (String line : translator.render().split("\n")) {
            if (!line.isEmpty()) {
                System.out.println("    " + line);
            }
        }
    }
}
