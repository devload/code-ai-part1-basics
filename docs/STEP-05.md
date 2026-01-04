# STEP 05: Embedding

토큰을 벡터로 변환하는 Embedding을 알아봅니다.

---

## 토큰화의 한계

STEP 01에서 토큰화를 배웠습니다:

```
"고양이가 잔다" → [42, 15, 73]
```

하지만 숫자만으로는 **의미**를 표현할 수 없습니다.

```
42 = "고양이"
43 = "강아지"
```

42와 43이 비슷한 동물인지 AI가 어떻게 알까요?

---

## Embedding이란?

토큰을 **벡터(숫자 배열)**로 변환하는 것입니다.

```
"고양이" → [0.2, -0.1, 0.8, 0.3, ...]  (수백~수천 차원)
"강아지" → [0.3, -0.1, 0.7, 0.4, ...]  (비슷한 벡터!)
"자동차" → [-0.5, 0.9, 0.1, -0.2, ...]  (다른 벡터)
```

**핵심 아이디어**: 비슷한 의미 → 비슷한 벡터

---

## 왜 벡터인가?

### 숫자의 한계

```
고양이 = 42
강아지 = 43
자동차 = 100

42와 43이 가깝다고 의미가 비슷한가? 아니다.
```

### 벡터의 장점

```
고양이 = [0.2, -0.1, 0.8]
강아지 = [0.3, -0.1, 0.7]

두 벡터의 거리(코사인 유사도)로 의미 유사성 측정 가능!
```

---

## 코사인 유사도

두 벡터가 얼마나 비슷한지 측정합니다.

```java
public double cosineSimilarity(float[] a, float[] b) {
    double dot = 0, normA = 0, normB = 0;

    for (int i = 0; i < a.length; i++) {
        dot += a[i] * b[i];
        normA += a[i] * a[i];
        normB += b[i] * b[i];
    }

    return dot / (Math.sqrt(normA) * Math.sqrt(normB));
}
```

결과값:
- 1.0: 완전히 같은 방향 (매우 유사)
- 0.0: 직각 (관련 없음)
- -1.0: 반대 방향 (반대 의미)

---

## Embedding 시각화

2차원으로 축소해서 보면:

```
        고양이 •  • 강아지
                 • 햄스터

   • 자동차
        • 비행기

   • 사과
        • 바나나
```

비슷한 개념은 가까이 모여있습니다.

---

## Embedding은 어떻게 학습되나?

### 핵심 아이디어

"함께 등장하는 단어는 비슷한 의미"

```
"고양이가 잠을 잔다"
"강아지가 잠을 잔다"
"고양이가 밥을 먹는다"
"강아지가 밥을 먹는다"
```

"고양이"와 "강아지"는 비슷한 문맥에서 등장 → 비슷한 벡터

### Word2Vec (역사적 중요성)

```
입력: "고양이가 [?] 잔다"
정답: "잠을"

이 과정에서 "고양이" 벡터가 학습됨
```

### 현대 모델

GPT, BERT 같은 모델은 더 복잡한 방식으로 학습합니다.
하지만 핵심 원리는 같습니다: **문맥에서 의미 학습**

---

## 간단한 Embedding 구현

```java
public class SimpleEmbedding {

    private Map<String, float[]> embeddings = new HashMap<>();
    private int dimension = 64;
    private Random random = new Random(42);

    public SimpleEmbedding() {
        // 임의 초기화 (실제로는 학습 필요)
        initializeRandom();
    }

    public float[] embed(String token) {
        return embeddings.computeIfAbsent(token, k -> randomVector());
    }

    public float[] embedSentence(List<String> tokens) {
        // 평균 풀링: 모든 토큰 벡터의 평균
        float[] result = new float[dimension];

        for (String token : tokens) {
            float[] vec = embed(token);
            for (int i = 0; i < dimension; i++) {
                result[i] += vec[i];
            }
        }

        for (int i = 0; i < dimension; i++) {
            result[i] /= tokens.size();
        }

        return result;
    }

    private float[] randomVector() {
        float[] vec = new float[dimension];
        for (int i = 0; i < dimension; i++) {
            vec[i] = random.nextFloat() * 2 - 1;  // -1 ~ 1
        }
        return normalize(vec);
    }

    private float[] normalize(float[] vec) {
        double norm = 0;
        for (float v : vec) norm += v * v;
        norm = Math.sqrt(norm);

        for (int i = 0; i < vec.length; i++) {
            vec[i] /= norm;
        }
        return vec;
    }
}
```

---

## 실제 Embedding API

### OpenAI Embedding

```java
public float[] getEmbedding(String text) {
    HttpRequest request = HttpRequest.newBuilder()
        .uri(URI.create("https://api.openai.com/v1/embeddings"))
        .header("Authorization", "Bearer " + apiKey)
        .header("Content-Type", "application/json")
        .POST(HttpRequest.BodyPublishers.ofString("""
            {
                "input": "%s",
                "model": "text-embedding-3-small"
            }
            """.formatted(text)))
        .build();

    // 응답 파싱 후 벡터 반환
    return parseEmbedding(response);
}
```

### 모델 비교

| 모델 | 차원 | 용도 |
|------|------|------|
| text-embedding-3-small | 1536 | 일반 용도, 저렴 |
| text-embedding-3-large | 3072 | 높은 정확도 |
| text-embedding-ada-002 | 1536 | 이전 버전 |

---

## Embedding의 활용

### 1. 유사도 검색

```java
// 가장 비슷한 문서 찾기
float[] query = embed("고양이 키우는 방법");

for (Document doc : documents) {
    double similarity = cosineSimilarity(query, doc.embedding);
    if (similarity > 0.8) {
        System.out.println("관련 문서: " + doc.title);
    }
}
```

### 2. 클러스터링

비슷한 문서끼리 그룹화

### 3. 이상 탐지

평균에서 많이 벗어난 데이터 찾기

---

## 핵심 정리

```
Embedding = 토큰 → 벡터

핵심 원리:
- 비슷한 의미 → 비슷한 벡터
- 문맥에서 학습됨

활용:
- 유사도 검색 (RAG의 핵심)
- 클러스터링
- 분류
```

---

## 다음 단계

다음 STEP에서는 Transformer 아키텍처를 알아봅니다. GPT, Claude 같은 모델의 기반이 되는 구조입니다.
