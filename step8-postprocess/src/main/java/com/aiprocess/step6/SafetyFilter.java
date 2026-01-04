package com.aiprocess.step6;

import java.util.*;
import java.util.regex.Pattern;

/**
 * STEP 6: 후처리 - 안전 필터
 *
 * 핵심 질문: 왜 가끔 거절하는가?
 *
 * AI가 생성한 텍스트를 최종 출력 전에 검사합니다.
 *
 * ┌─────────────────────────────────────────────────────────┐
 * │ 생성된 텍스트                                            │
 * │     │                                                    │
 * │     ▼                                                    │
 * │ ┌─────────────────────────────────────────┐              │
 * │ │ 1. 유해 콘텐츠 필터                       │              │
 * │ │    - 폭력, 혐오, 불법 콘텐츠 탐지          │              │
 * │ └─────────────────────────────────────────┘              │
 * │     │                                                    │
 * │     ▼                                                    │
 * │ ┌─────────────────────────────────────────┐              │
 * │ │ 2. 개인정보 필터                          │              │
 * │ │    - 이메일, 전화번호, 주민번호 마스킹     │              │
 * │ └─────────────────────────────────────────┘              │
 * │     │                                                    │
 * │     ▼                                                    │
 * │ ┌─────────────────────────────────────────┐              │
 * │ │ 3. 품질 체크                              │              │
 * │ │    - 반복, 의미없는 텍스트 감지            │              │
 * │ └─────────────────────────────────────────┘              │
 * │     │                                                    │
 * │     ▼                                                    │
 * │ [통과] 또는 [거절/수정]                                   │
 * └─────────────────────────────────────────────────────────┘
 */
public class SafetyFilter {

    // 금지어 목록 (예시)
    private final Set<String> blockedWords;

    // 개인정보 패턴
    private static final Pattern EMAIL_PATTERN =
        Pattern.compile("[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}");
    private static final Pattern PHONE_PATTERN =
        Pattern.compile("01[0-9]-?\\d{3,4}-?\\d{4}");
    private static final Pattern SSN_PATTERN =
        Pattern.compile("\\d{6}-?[1-4]\\d{6}");

    public SafetyFilter() {
        this.blockedWords = new HashSet<>();
        // 예시 금지어 (실제로는 더 많음)
        blockedWords.addAll(Arrays.asList(
            // 교육용이므로 실제 금지어는 포함하지 않음
        ));
    }

    /**
     * 안전성 검사 수행
     *
     * @param text 검사할 텍스트
     * @return 검사 결과
     */
    public FilterResult check(String text) {
        List<String> issues = new ArrayList<>();

        // 1. 금지어 체크
        for (String word : blockedWords) {
            if (text.toLowerCase().contains(word.toLowerCase())) {
                issues.add("금지어 감지: " + word);
            }
        }

        // 2. 개인정보 체크
        if (EMAIL_PATTERN.matcher(text).find()) {
            issues.add("이메일 주소 감지");
        }
        if (PHONE_PATTERN.matcher(text).find()) {
            issues.add("전화번호 감지");
        }
        if (SSN_PATTERN.matcher(text).find()) {
            issues.add("주민등록번호 감지");
        }

        // 3. 반복 패턴 체크
        if (hasExcessiveRepetition(text)) {
            issues.add("과도한 반복 감지");
        }

        return new FilterResult(issues.isEmpty(), issues);
    }

    /**
     * 개인정보 마스킹
     */
    public String maskPII(String text) {
        String result = text;
        result = EMAIL_PATTERN.matcher(result).replaceAll("[EMAIL]");
        result = PHONE_PATTERN.matcher(result).replaceAll("[PHONE]");
        result = SSN_PATTERN.matcher(result).replaceAll("[SSN]");
        return result;
    }

    /**
     * 과도한 반복 패턴 감지
     */
    private boolean hasExcessiveRepetition(String text) {
        // 같은 단어가 5번 이상 연속
        String[] words = text.split("\\s+");
        int repeatCount = 1;
        for (int i = 1; i < words.length; i++) {
            if (words[i].equals(words[i - 1])) {
                repeatCount++;
                if (repeatCount >= 5) return true;
            } else {
                repeatCount = 1;
            }
        }
        return false;
    }

    /**
     * 필터 결과
     */
    public record FilterResult(
        boolean passed,
        List<String> issues
    ) {
        @Override
        public String toString() {
            if (passed) {
                return "FilterResult(PASSED)";
            } else {
                return "FilterResult(BLOCKED: " + String.join(", ", issues) + ")";
            }
        }
    }
}
