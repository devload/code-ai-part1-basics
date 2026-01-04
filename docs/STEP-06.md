# STEP 06: Transformer 아키텍처

GPT, Claude 같은 모델의 기반인 Transformer를 알아봅니다.

---

## 왜 Transformer인가?

### 이전 모델들의 한계

**RNN (순환 신경망)**
```
"나는 어제 공원에서 산책을 했다"
 ↓   ↓   ↓    ↓     ↓    ↓
순차적으로 처리 → 느림, 긴 문장에서 앞 내용 잊음
```

**Bigram (우리가 만든 것)**
```
"나는" → "어제" (바로 앞 단어만 봄)
```

### Transformer의 해결

```
모든 단어를 동시에 보고, 관련 있는 것에 집중!
"나는 어제 공원에서 산책을 했다"
    ↑________________↑
    "산책"과 "공원"의 관계를 파악
```

---

## Attention: 핵심 개념

"중요한 것에 집중한다"

### 예시

```
입력: "그 고양이는 매우 귀여웠다. 그것은 주황색이었다."

"그것"이 무엇을 가리키나?
 ↓
"고양이"에 집중! (높은 attention)
"매우"는 무시 (낮은 attention)
```

### Attention 점수

```java
// 간단한 Attention 구현
public float[] attention(float[] query, float[][] keys, float[][] values) {
    int n = keys.length;
    float[] scores = new float[n];

    // 1. Query와 각 Key의 유사도 계산
    for (int i = 0; i < n; i++) {
        scores[i] = dotProduct(query, keys[i]);
    }

    // 2. Softmax로 확률 변환
    float[] weights = softmax(scores);

    // 3. Value의 가중 평균
    float[] output = new float[values[0].length];
    for (int i = 0; i < n; i++) {
        for (int j = 0; j < output.length; j++) {
            output[j] += weights[i] * values[i][j];
        }
    }

    return output;
}
```

---

## Self-Attention

문장 내 단어들이 서로를 참조합니다.

```
입력: "고양이가 생선을 먹었다"

"먹었다"가 볼 때:
- "고양이"에 높은 attention (주어)
- "생선"에 높은 attention (목적어)
- "가", "을"에 낮은 attention
```

### 시각화

```
         고양이  가   생선   을   먹었다
고양이    0.8   0.1   0.05  0.02  0.03
가        0.3   0.4   0.1   0.1   0.1
생선      0.1   0.05  0.7   0.1   0.05
을        0.1   0.1   0.3   0.4   0.1
먹었다    0.4   0.05  0.35  0.05  0.15
```

"먹었다"는 "고양이"(0.4)와 "생선"(0.35)에 집중!

---

## Multi-Head Attention

여러 관점에서 동시에 attention을 계산합니다.

```
Head 1: 문법적 관계 (주어-동사)
Head 2: 의미적 관계 (동물-음식)
Head 3: 위치 관계 (가까운 단어)
...
```

---

## Transformer 구조

```
입력 토큰
    ↓
[Embedding]
    ↓
[Position Encoding] ← 순서 정보 추가
    ↓
┌─────────────────────┐
│   Transformer Block │ × N (12~96개)
│  ┌───────────────┐  │
│  │ Self-Attention │  │
│  └───────────────┘  │
│         ↓           │
│  ┌───────────────┐  │
│  │ Feed Forward  │  │
│  └───────────────┘  │
└─────────────────────┘
    ↓
[Output Layer]
    ↓
다음 토큰 확률
```

---

## Position Encoding

Transformer는 순서를 모릅니다. 위치 정보를 추가해야 합니다.

```java
public float[] positionEncoding(int position, int dimension) {
    float[] encoding = new float[dimension];

    for (int i = 0; i < dimension; i++) {
        if (i % 2 == 0) {
            encoding[i] = (float) Math.sin(
                position / Math.pow(10000, i / (double) dimension)
            );
        } else {
            encoding[i] = (float) Math.cos(
                position / Math.pow(10000, (i - 1) / (double) dimension)
            );
        }
    }

    return encoding;
}
```

---

## GPT vs BERT

### GPT (Decoder-only)

```
← 이전 토큰만 봄

"나는 밥을 [다음?]"
   ↑   ↑
   참조 가능
```

**용도**: 텍스트 생성

### BERT (Encoder-only)

```
← 양방향 참조 →

"나는 [MASK]을 먹는다"
   ↑          ↑
   양쪽 다 참조
```

**용도**: 텍스트 이해, 분류

### ChatGPT, Claude

GPT 스타일 (Decoder-only) + 대화 Fine-tuning

---

## 모델 크기

| 모델 | 파라미터 | Transformer 블록 |
|------|----------|------------------|
| GPT-2 Small | 117M | 12 |
| GPT-2 Large | 774M | 36 |
| GPT-3 | 175B | 96 |
| GPT-4 | ~1T? | ? |

파라미터가 많을수록:
- 더 많은 지식 저장
- 더 복잡한 패턴 학습
- 더 많은 계산 비용

---

## 왜 Transformer가 성공했나?

### 1. 병렬 처리

```
RNN: 순차 처리 → 느림
Transformer: 동시 처리 → GPU에서 빠름
```

### 2. 긴 거리 의존성

```
RNN: 멀리 있는 정보 잊음
Transformer: Attention으로 직접 참조
```

### 3. 확장성

```
데이터 ↑ + 모델 크기 ↑ = 성능 ↑
(Scaling Law)
```

---

## 핵심 정리

```
Transformer = Attention + Position Encoding + 깊은 네트워크

핵심 개념:
- Attention: 중요한 것에 집중
- Self-Attention: 문장 내 관계 파악
- Multi-Head: 여러 관점에서 분석
- Position Encoding: 순서 정보 추가

GPT/Claude는 Transformer 기반:
- 토큰화 → Embedding → Transformer × N → 다음 토큰
```

---

## 다음 단계

다음 STEP에서는 Server를 만듭니다. 지금까지 배운 모델을 REST API로 제공합니다.
