package com.aiprocess.step1;

import java.util.List;

/**
 * STEP 1: 토큰화 인터페이스
 *
 * 핵심 질문: 왜 문장을 조각내는가?
 *
 * AI는 문장을 그대로 이해하지 못합니다.
 * 문장을 "토큰"이라는 작은 조각으로 나누어야 처리할 수 있습니다.
 *
 * 토큰화 과정:
 * ┌────────────────────────────────────────┐
 * │ "오늘 날씨 어때?"                        │
 * │         │                               │
 * │         ▼ tokenize()                    │
 * │ ["오늘", "날씨", "어때", "?"]            │
 * │         │                               │
 * │         ▼ encode()                      │
 * │ [1042, 2891, 892, 15]  ← 토큰 ID        │
 * │         │                               │
 * │         ▼ decode()                      │
 * │ "오늘 날씨 어때?"      ← 원본 복원       │
 * └────────────────────────────────────────┘
 */
public interface Tokenizer {

    /**
     * 텍스트를 토큰 문자열 리스트로 분리
     *
     * @param text 입력 텍스트
     * @return 토큰 문자열 리스트
     */
    List<String> tokenize(String text);

    /**
     * 텍스트를 토큰 ID 리스트로 변환 (encode)
     *
     * @param text 입력 텍스트
     * @return 토큰 ID 리스트
     */
    List<Integer> encode(String text);

    /**
     * 토큰 ID 리스트를 텍스트로 복원 (decode)
     *
     * @param tokenIds 토큰 ID 리스트
     * @return 복원된 텍스트
     */
    String decode(List<Integer> tokenIds);

    /**
     * 어휘 사전 크기 반환
     *
     * @return vocabulary 크기
     */
    int vocabSize();

    /**
     * 토큰 ID로 토큰 문자열 조회
     */
    String getToken(int id);

    /**
     * 토큰 문자열로 토큰 ID 조회
     */
    int getTokenId(String token);
}
