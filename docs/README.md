# Part 1: AI 모델의 원리

AI 모델이 어떻게 동작하는지 핵심 원리를 단계별로 학습합니다.

---

## 학습 목차

| STEP | 제목 | 핵심 개념 |
|------|------|----------|
| [00](STEP-00.md) | 프로젝트 개요 | 전체 구조, 기술 스택 |
| [01](STEP-01.md) | 토큰화 | encode/decode, 어휘 사전 |
| [02](STEP-02.md) | Bigram 학습 | 학습=카운팅, Artifact |
| [03](STEP-03.md) | 텍스트 생성 | 생성 루프, 샘플링 |
| [04](STEP-04.md) | Usage 측정 | 토큰=비용, Input/Output |
| [05](STEP-05.md) | Embedding | 벡터, 코사인 유사도 |
| [06](STEP-06.md) | Transformer | Attention, Self-Attention |
| [07](STEP-07.md) | Server 만들기 | REST API, Spring Boot |
| [08](STEP-08.md) | CLI 만들기 | 사용자 인터페이스 |

---

## 전체 흐름

```
토큰화 → 학습 → 생성 → 비용 → Embedding → Transformer → 서비스화
```

```
[텍스트 입력]
     ↓
[STEP 1] 토큰화: "Hello world" → [42, 123]
     ↓
[STEP 2] 학습: 패턴 카운팅 → Artifact 저장
     ↓
[STEP 3] 생성: 다음 토큰 예측 반복
     ↓
[STEP 4] 비용: Usage 측정
     ↓
[STEP 5] Embedding: 토큰 → 벡터
     ↓
[STEP 6] Transformer: Attention으로 문맥 이해
     ↓
[STEP 7-8] 서비스: API / CLI
     ↓
[텍스트 출력]
```

---

## 핵심 키워드

`토큰` `Bigram` `생성 루프` `샘플링` `temperature` `Usage` `Embedding` `Attention` `Transformer`

---

## 학습 후 할 수 있는 것

- AI 모델이 입력을 처리하는 과정 이해 (토큰화 → Embedding)
- "학습"이 뭔지 직관적으로 이해
- 텍스트 생성 원리를 코드로 구현
- Transformer와 Attention의 핵심 원리 이해
- API 비용 구조 이해

---

## 다음 단계

- [Part 2: 도메인 데이터 분석](https://github.com/devload/code-ai-part2-analyzer)
- [Part 3: AI 서비스 통합](https://github.com/devload/code-ai-part3-service)
- [Part 4: 고급 주제](https://github.com/devload/code-ai-part4-advanced)
