package com.aiprocess.step2;

import java.util.ArrayList;
import java.util.List;

/**
 * STEP 2: 컨텍스트 구성
 *
 * 핵심 질문: AI는 뭘 보고 답하는가?
 *
 * AI는 단순히 사용자 입력만 보는 것이 아닙니다.
 * 다음 세 가지를 조합하여 "컨텍스트"를 만들고, 이를 기반으로 응답합니다:
 *
 * ┌─────────────────────────────────────────────────────────┐
 * │                    컨텍스트 구성                          │
 * ├─────────────────────────────────────────────────────────┤
 * │ 1. 시스템 프롬프트                                        │
 * │    "너는 친절한 AI 어시스턴트야"                          │
 * │                                                          │
 * │ 2. 대화 히스토리                                          │
 * │    User: "오늘 날씨 어때?"                                │
 * │    AI: "오늘은 맑아요!"                                   │
 * │    User: "내일은?"                                        │
 * │                                                          │
 * │ 3. 현재 사용자 입력                                       │
 * │    "그럼 우산 챙겨야 해?"                                  │
 * ├─────────────────────────────────────────────────────────┤
 * │                    ↓ 조합                                 │
 * │            [전체 컨텍스트 → AI 모델]                      │
 * └─────────────────────────────────────────────────────────┘
 */
public class ContextBuilder {

    private String systemPrompt;
    private final List<Message> history;
    private int maxTokens;

    public ContextBuilder() {
        this.systemPrompt = "";
        this.history = new ArrayList<>();
        this.maxTokens = 4096;  // 기본 컨텍스트 윈도우
    }

    /**
     * 시스템 프롬프트 설정
     * AI의 역할, 성격, 규칙 등을 정의합니다.
     */
    public ContextBuilder withSystemPrompt(String systemPrompt) {
        this.systemPrompt = systemPrompt;
        return this;
    }

    /**
     * 최대 토큰 수 설정 (컨텍스트 윈도우)
     */
    public ContextBuilder withMaxTokens(int maxTokens) {
        this.maxTokens = maxTokens;
        return this;
    }

    /**
     * 사용자 메시지 추가
     */
    public ContextBuilder addUserMessage(String content) {
        history.add(new Message(Role.USER, content));
        return this;
    }

    /**
     * AI 응답 추가
     */
    public ContextBuilder addAssistantMessage(String content) {
        history.add(new Message(Role.ASSISTANT, content));
        return this;
    }

    /**
     * 전체 컨텍스트 빌드
     */
    public Context build() {
        return new Context(systemPrompt, new ArrayList<>(history), maxTokens);
    }

    /**
     * 컨텍스트를 문자열로 변환 (프롬프트 형태)
     */
    public String buildPrompt() {
        StringBuilder sb = new StringBuilder();

        // 1. 시스템 프롬프트
        if (systemPrompt != null && !systemPrompt.isEmpty()) {
            sb.append("[SYSTEM]\n");
            sb.append(systemPrompt);
            sb.append("\n\n");
        }

        // 2. 대화 히스토리
        for (Message msg : history) {
            if (msg.role() == Role.USER) {
                sb.append("[USER]\n");
            } else {
                sb.append("[ASSISTANT]\n");
            }
            sb.append(msg.content());
            sb.append("\n\n");
        }

        return sb.toString().trim();
    }

    /**
     * 대화 히스토리 초기화
     */
    public void clearHistory() {
        history.clear();
    }

    /**
     * 역할 (User / Assistant)
     */
    public enum Role {
        USER, ASSISTANT, SYSTEM
    }

    /**
     * 메시지 레코드
     */
    public record Message(Role role, String content) {}

    /**
     * 컨텍스트 레코드
     */
    public record Context(
        String systemPrompt,
        List<Message> history,
        int maxTokens
    ) {}
}
