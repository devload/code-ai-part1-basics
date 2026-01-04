package com.aiprocess.step2;

import com.aiprocess.step2.ContextBuilder.Message;
import com.aiprocess.step2.ContextBuilder.Role;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * 대화 메모리 관리
 *
 * AI는 이전 대화를 "기억"하는 것처럼 보이지만,
 * 실제로는 매번 전체 대화 내용을 컨텍스트로 전달합니다.
 *
 * 문제점:
 * - 컨텍스트 윈도우에는 제한이 있음 (GPT-4: 8K~128K tokens)
 * - 대화가 길어지면 오래된 내용은 "잊어버림"
 *
 * 해결 전략:
 * ┌─────────────────────────────────────────┐
 * │ 1. 슬라이딩 윈도우                        │
 * │    - 최근 N개 대화만 유지                 │
 * │                                          │
 * │ 2. 요약                                   │
 * │    - 오래된 대화를 요약하여 보관           │
 * │                                          │
 * │ 3. 중요도 기반                            │
 * │    - 중요한 대화만 선별하여 유지           │
 * └─────────────────────────────────────────┘
 */
public class ConversationMemory {

    private final LinkedList<Message> messages;
    private final int maxMessages;
    private String summary;

    /**
     * @param maxMessages 최대 저장 메시지 수
     */
    public ConversationMemory(int maxMessages) {
        this.messages = new LinkedList<>();
        this.maxMessages = maxMessages;
        this.summary = "";
    }

    /**
     * 기본 생성자 (최대 20개 메시지)
     */
    public ConversationMemory() {
        this(20);
    }

    /**
     * 메시지 추가 (슬라이딩 윈도우)
     */
    public void addMessage(Role role, String content) {
        Message msg = new Message(role, content);
        messages.addLast(msg);

        // 최대 개수 초과 시 오래된 메시지 제거
        while (messages.size() > maxMessages) {
            Message removed = messages.removeFirst();
            // 선택적: 제거된 메시지를 요약에 추가
            updateSummary(removed);
        }
    }

    /**
     * 사용자 메시지 추가
     */
    public void addUserMessage(String content) {
        addMessage(Role.USER, content);
    }

    /**
     * AI 응답 추가
     */
    public void addAssistantMessage(String content) {
        addMessage(Role.ASSISTANT, content);
    }

    /**
     * 전체 대화 히스토리 반환
     */
    public List<Message> getMessages() {
        return new ArrayList<>(messages);
    }

    /**
     * 최근 N개 메시지 반환
     */
    public List<Message> getRecentMessages(int n) {
        int size = messages.size();
        int start = Math.max(0, size - n);
        return new ArrayList<>(messages.subList(start, size));
    }

    /**
     * 요약 가져오기
     */
    public String getSummary() {
        return summary;
    }

    /**
     * 요약 업데이트 (간단한 예시 구현)
     */
    private void updateSummary(Message removed) {
        // 실제로는 LLM을 사용하여 요약을 생성
        // 여기서는 간단히 추가
        if (!summary.isEmpty()) {
            summary += " ";
        }
        summary += "[" + removed.role() + ": " + truncate(removed.content(), 30) + "]";
    }

    private String truncate(String text, int maxLen) {
        if (text.length() <= maxLen) return text;
        return text.substring(0, maxLen - 3) + "...";
    }

    /**
     * 대화 초기화
     */
    public void clear() {
        messages.clear();
        summary = "";
    }

    /**
     * 현재 저장된 메시지 수
     */
    public int size() {
        return messages.size();
    }

    @Override
    public String toString() {
        return String.format("ConversationMemory(messages=%d, max=%d)",
            messages.size(), maxMessages);
    }
}
