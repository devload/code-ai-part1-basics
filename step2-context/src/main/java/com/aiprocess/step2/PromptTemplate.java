package com.aiprocess.step2;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 프롬프트 템플릿
 *
 * 동적으로 변하는 부분을 변수로 처리하여 프롬프트를 구성합니다.
 *
 * 예시:
 * ┌─────────────────────────────────────────────────────────┐
 * │ 템플릿:                                                  │
 * │ "너는 {{role}}야. {{user}}의 질문에 {{style}}하게 답해줘" │
 * │                                                          │
 * │ 변수:                                                    │
 * │ - role: "친절한 AI 어시스턴트"                           │
 * │ - user: "홍길동"                                         │
 * │ - style: "간결"                                          │
 * │                                                          │
 * │ 결과:                                                    │
 * │ "너는 친절한 AI 어시스턴트야. 홍길동의 질문에 간결하게    │
 * │  답해줘"                                                  │
 * └─────────────────────────────────────────────────────────┘
 */
public class PromptTemplate {

    private static final Pattern VARIABLE_PATTERN = Pattern.compile("\\{\\{(\\w+)\\}\\}");

    private final String template;
    private final Map<String, String> variables;

    public PromptTemplate(String template) {
        this.template = template;
        this.variables = new HashMap<>();
    }

    /**
     * 변수 설정
     */
    public PromptTemplate set(String name, String value) {
        variables.put(name, value);
        return this;
    }

    /**
     * 여러 변수 한번에 설정
     */
    public PromptTemplate setAll(Map<String, String> vars) {
        variables.putAll(vars);
        return this;
    }

    /**
     * 템플릿 렌더링
     */
    public String render() {
        StringBuffer result = new StringBuffer();
        Matcher matcher = VARIABLE_PATTERN.matcher(template);

        while (matcher.find()) {
            String varName = matcher.group(1);
            String replacement = variables.getOrDefault(varName, "{{" + varName + "}}");
            matcher.appendReplacement(result, Matcher.quoteReplacement(replacement));
        }
        matcher.appendTail(result);

        return result.toString();
    }

    /**
     * 변수 초기화
     */
    public void clearVariables() {
        variables.clear();
    }

    /**
     * 미리 정의된 시스템 프롬프트 템플릿들
     */
    public static class Templates {

        /**
         * 친절한 어시스턴트
         */
        public static final String FRIENDLY_ASSISTANT = """
            너는 친절한 AI 어시스턴트야.
            사용자의 질문에 정확하고 도움이 되는 답변을 해줘.
            모르는 것은 모른다고 솔직하게 말해.
            """;

        /**
         * 코드 리뷰어
         */
        public static final String CODE_REVIEWER = """
            너는 경험 많은 코드 리뷰어야.
            코드를 분석하고 다음을 평가해:
            - 버그 가능성
            - 성능 이슈
            - 코드 스타일
            - 개선 제안
            """;

        /**
         * 번역가
         */
        public static final String TRANSLATOR = """
            너는 전문 번역가야.
            {{source_lang}}에서 {{target_lang}}으로 정확하게 번역해줘.
            자연스러운 표현을 사용하고, 원문의 뉘앙스를 살려줘.
            """;

        /**
         * 요약 전문가
         */
        public static final String SUMMARIZER = """
            너는 요약 전문가야.
            주어진 텍스트를 {{length}}로 요약해줘.
            핵심 내용만 간추려서 전달해.
            """;
    }

    @Override
    public String toString() {
        return String.format("PromptTemplate(vars=%d)", variables.size());
    }
}
