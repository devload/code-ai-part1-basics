package com.aiprocess.step3;

import java.util.Map;

/**
 * STEP 3: 확률 계산 인터페이스
 *
 * 핵심 질문: 다음 단어를 어떻게 예측하는가?
 *
 * AI는 "다음에 올 확률이 가장 높은 토큰"을 계산합니다.
 *
 * 예시:
 * ┌─────────────────────────────────────────────────────────┐
 * │ 입력: "오늘 날씨"                                        │
 * │                                                          │
 * │ 다음 토큰 확률:                                          │
 * │   "좋다"  : 35%  ████████████████████                    │
 * │   "맑다"  : 25%  ██████████████                          │
 * │   "어때"  : 20%  ███████████                             │
 * │   "흐리다": 10%  ██████                                  │
 * │   "춥다"  :  5%  ███                                     │
 * │   기타   :  5%  ███                                     │
 * └─────────────────────────────────────────────────────────┘
 */
public interface ProbabilityModel {

    /**
     * 주어진 컨텍스트에서 다음 토큰의 확률 분포 계산
     *
     * @param context 이전 토큰들
     * @return 토큰 ID → 확률 매핑
     */
    Map<Integer, Double> getNextTokenProbabilities(int[] context);

    /**
     * 특정 토큰 다음에 올 수 있는 토큰들의 카운트
     *
     * @param prevToken 이전 토큰 ID
     * @return 토큰 ID → 출현 횟수 매핑
     */
    Map<Integer, Integer> getNextTokenCounts(int prevToken);

    /**
     * 텍스트로 모델 학습
     *
     * @param tokens 학습할 토큰 시퀀스
     */
    void train(int[] tokens);

    /**
     * 어휘 크기
     */
    int vocabSize();
}
