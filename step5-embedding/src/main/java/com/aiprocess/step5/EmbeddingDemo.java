package com.aiprocess.step5;

import java.util.*;

/**
 * Embedding 데모: 토큰을 벡터로 변환하고 유사도를 계산
 */
public class EmbeddingDemo {

    public static void main(String[] args) {
        System.out.println("========================================");
        System.out.println("STEP 5: Embedding 데모");
        System.out.println("========================================\n");

        // 64차원 임베딩 생성
        Embedding embedding = new Embedding(64);

        // 1. 기본 임베딩 시연
        System.out.println("[ 1. 토큰 → 벡터 변환 ]");
        System.out.println("----------------------------------------");

        String[] words = {"고양이", "강아지", "자동차", "비행기"};
        for (String word : words) {
            float[] vec = embedding.embed(word);
            System.out.printf("%-8s → %s%n", word, Embedding.vectorToString(vec));
        }

        // 2. 유사 단어 그룹 학습
        System.out.println("\n[ 2. 유사 단어 그룹 학습 ]");
        System.out.println("----------------------------------------");

        List<List<String>> wordGroups = Arrays.asList(
            Arrays.asList("고양이", "강아지", "햄스터", "토끼"),      // 동물
            Arrays.asList("자동차", "비행기", "기차", "배"),          // 탈것
            Arrays.asList("사과", "바나나", "오렌지", "포도"),        // 과일
            Arrays.asList("서울", "도쿄", "뉴욕", "런던")            // 도시
        );

        embedding.trainSimilarWords(wordGroups);
        System.out.println("4개의 단어 그룹을 학습했습니다:");
        System.out.println("- 동물: 고양이, 강아지, 햄스터, 토끼");
        System.out.println("- 탈것: 자동차, 비행기, 기차, 배");
        System.out.println("- 과일: 사과, 바나나, 오렌지, 포도");
        System.out.println("- 도시: 서울, 도쿄, 뉴욕, 런던");

        // 3. 코사인 유사도 계산
        System.out.println("\n[ 3. 코사인 유사도 계산 ]");
        System.out.println("----------------------------------------");

        String[][] pairs = {
            {"고양이", "강아지"},   // 같은 그룹 (동물)
            {"고양이", "사과"},     // 다른 그룹
            {"서울", "도쿄"},       // 같은 그룹 (도시)
            {"자동차", "비행기"},   // 같은 그룹 (탈것)
            {"바나나", "기차"}      // 다른 그룹
        };

        for (String[] pair : pairs) {
            float[] vec1 = embedding.embed(pair[0]);
            float[] vec2 = embedding.embed(pair[1]);
            double similarity = Embedding.cosineSimilarity(vec1, vec2);

            String relation = similarity > 0.8 ? "매우 유사" :
                             similarity > 0.5 ? "유사" :
                             similarity > 0.2 ? "약간 관련" : "관련 없음";

            System.out.printf("%-6s ↔ %-6s : %.3f (%s)%n",
                pair[0], pair[1], similarity, relation);
        }

        // 4. 유사 단어 검색
        System.out.println("\n[ 4. 유사 단어 검색 ]");
        System.out.println("----------------------------------------");

        String[] queryWords = {"고양이", "서울", "사과"};
        for (String query : queryWords) {
            System.out.printf("'%s'와(과) 유사한 단어:%n", query);
            List<Map.Entry<String, Double>> similar = embedding.findSimilar(query, 3);
            for (Map.Entry<String, Double> entry : similar) {
                System.out.printf("  - %s (유사도: %.3f)%n", entry.getKey(), entry.getValue());
            }
            System.out.println();
        }

        // 5. 문장 임베딩
        System.out.println("[ 5. 문장 임베딩 (평균 풀링) ]");
        System.out.println("----------------------------------------");

        List<String> sentence1 = Arrays.asList("고양이", "강아지");
        List<String> sentence2 = Arrays.asList("자동차", "비행기");
        List<String> sentence3 = Arrays.asList("고양이", "토끼");

        float[] sentVec1 = embedding.embedSentence(sentence1);
        float[] sentVec2 = embedding.embedSentence(sentence2);
        float[] sentVec3 = embedding.embedSentence(sentence3);

        System.out.printf("문장1 [고양이, 강아지] → %s%n", Embedding.vectorToString(sentVec1));
        System.out.printf("문장2 [자동차, 비행기] → %s%n", Embedding.vectorToString(sentVec2));
        System.out.printf("문장3 [고양이, 토끼]   → %s%n", Embedding.vectorToString(sentVec3));

        System.out.println("\n문장 간 유사도:");
        System.out.printf("문장1 ↔ 문장2 : %.3f (다른 카테고리)%n",
            Embedding.cosineSimilarity(sentVec1, sentVec2));
        System.out.printf("문장1 ↔ 문장3 : %.3f (같은 카테고리)%n",
            Embedding.cosineSimilarity(sentVec1, sentVec3));

        // 요약
        System.out.println("\n========================================");
        System.out.println("핵심 정리");
        System.out.println("========================================");
        System.out.println("1. Embedding = 토큰 → 벡터 변환");
        System.out.println("2. 비슷한 의미 → 비슷한 벡터");
        System.out.println("3. 코사인 유사도로 의미 비교");
        System.out.println("4. RAG, 검색 등에 핵심적으로 사용");
        System.out.println("\n어휘 크기: " + embedding.getVocabularySize() + "개");
        System.out.println("벡터 차원: " + embedding.getDimension() + "차원");
    }
}
