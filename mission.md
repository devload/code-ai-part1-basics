# Mission: AI Process 학습 시리즈

## 프로젝트 비전

**"AI가 어떻게 동작하는가?"를 프로세스 관점에서 이해하는 교육용 프로젝트**

단순히 AI를 "사용"하는 것이 아니라, AI 내부에서 일어나는 과정을 단계별로 직접 구현하고 시각화하여 이해한다.

---

## 전체 시리즈 구성

```
┌─────────────────────────────────────────────────────────────────┐
│                    AI Process 학습 시리즈                        │
├─────────────────────────────────────────────────────────────────┤
│                                                                 │
│  Part 1: AI 기초 프로세스 (현재)                                 │
│  ┌─────────────────────────────────────────────────────────┐   │
│  │ 토큰화 → 컨텍스트 → 확률계산 → 샘플링 → 생성루프 → 후처리  │   │
│  │                                                          │   │
│  │ "AI가 글을 생성하는 기본 원리"                            │   │
│  └─────────────────────────────────────────────────────────┘   │
│                           │                                     │
│                           ▼                                     │
│  Part 2: 코드 이해 프로세스                                      │
│  ┌─────────────────────────────────────────────────────────┐   │
│  │ 파싱 → AST → 의미분석 → 패턴매칭 → 이슈탐지 → 점수화      │   │
│  │                                                          │   │
│  │ "AI가 코드를 이해하고 분석하는 원리"                       │   │
│  └─────────────────────────────────────────────────────────┘   │
│                           │                                     │
│                           ▼                                     │
│  Part 3: AI 서비스 프로세스                                      │
│  ┌─────────────────────────────────────────────────────────┐   │
│  │ API호출 → 프롬프트구성 → LLM처리 → 응답파싱 → 액션실행    │   │
│  │                                                          │   │
│  │ "AI를 실제 서비스로 연결하는 원리"                         │   │
│  └─────────────────────────────────────────────────────────┘   │
│                                                                 │
└─────────────────────────────────────────────────────────────────┘
```

---

# Part 1: AI 기초 프로세스

## 목표

**AI가 프롬프트를 받아 응답을 생성하기까지의 내부 과정을 이해한다**

```
사용자: "오늘 날씨 어때?"
        │
        ▼
┌─────────────────────────────────────────────────────────────┐
│ STEP 1: 토큰화                                               │
│ "오늘 날씨 어때?" → [오늘] [날씨] [어때] [?]                  │
└─────────────────────────────────────────────────────────────┘
        │
        ▼
┌─────────────────────────────────────────────────────────────┐
│ STEP 2: 컨텍스트 구성                                        │
│ [시스템 프롬프트] + [히스토리] + [사용자 입력]                │
└─────────────────────────────────────────────────────────────┘
        │
        ▼
┌─────────────────────────────────────────────────────────────┐
│ STEP 3: 확률 계산 (N-gram / Attention)                       │
│ 다음 토큰 후보: "오늘"(35%), "지금"(25%), "현재"(20%)...     │
└─────────────────────────────────────────────────────────────┘
        │
        ▼
┌─────────────────────────────────────────────────────────────┐
│ STEP 4: 샘플링                                               │
│ Temperature, Top-k, Top-p 적용 → "오늘" 선택                 │
└─────────────────────────────────────────────────────────────┘
        │
        ▼
┌─────────────────────────────────────────────────────────────┐
│ STEP 5: 생성 루프                                            │
│ "오늘" → "오늘 날씨는" → "오늘 날씨는 맑고" → ... → [END]    │
└─────────────────────────────────────────────────────────────┘
        │
        ▼
┌─────────────────────────────────────────────────────────────┐
│ STEP 6: 후처리                                               │
│ 안전 필터 체크 → 포맷 정리 → 최종 출력                       │
└─────────────────────────────────────────────────────────────┘
        │
        ▼
AI: "오늘 날씨는 맑고 화창해요!"
```

---

## Part 1 학습 단계

### STEP 1: 토큰화 (Tokenization)

**핵심 질문**: 왜 문장을 조각내는가?

| 학습 내용 | 구현 |
|----------|------|
| 토큰이란 무엇인가 | WhitespaceTokenizer |
| 왜 모델은 문장을 직접 이해 못하나 | encode/decode 구현 |
| 서브워드 토큰화 (BPE) | CodeTokenizer |
| 토큰 수 = 비용 | 토큰 카운팅 |

**시각화**: 문장 → 토큰 → ID 변환 애니메이션

---

### STEP 2: 컨텍스트 구성 (Context Assembly)

**핵심 질문**: AI는 뭘 보고 답하는가?

| 학습 내용 | 구현 |
|----------|------|
| 시스템 프롬프트 역할 | ContextBuilder |
| 대화 히스토리 관리 | ConversationMemory |
| 컨텍스트 윈도우 제한 | TokenLimiter |
| 프롬프트 템플릿 | PromptTemplate |

**시각화**: 컨텍스트 구성 요소 시각화

---

### STEP 3: 확률 계산 (Probability Calculation)

**핵심 질문**: 다음 단어를 어떻게 예측하는가?

| 학습 내용 | 구현 |
|----------|------|
| 조건부 확률 P(next\|prev) | BigramModel |
| N-gram 모델 | TrigramModel, NgramModel |
| 스무딩 (Kneser-Ney) | SmoothingStrategy |
| Attention 개념 소개 | (문서만, 구현은 Part 2+) |

**시각화**: 확률 분포 그래프, Attention 히트맵

---

### STEP 4: 샘플링 (Sampling)

**핵심 질문**: 왜 매번 답이 다른가?

| 학습 내용 | 구현 |
|----------|------|
| Greedy vs Sampling | Sampler |
| Temperature 효과 | temperature 파라미터 |
| Top-k 필터링 | topK 파라미터 |
| Top-p (Nucleus) 샘플링 | topP 파라미터 |
| 재현성 (Seed) | seed 파라미터 |

**시각화**: Temperature별 확률 분포 변화

---

### STEP 5: 생성 루프 (Generation Loop)

**핵심 질문**: 한 번에 쓰는가, 한 글자씩 쓰는가?

| 학습 내용 | 구현 |
|----------|------|
| Auto-regressive 생성 | Generator |
| 종료 조건 (EOS, max_tokens) | StopCondition |
| Stop Sequences | stopSequences 처리 |
| 스트리밍 출력 | StreamingGenerator |

**시각화**: 토큰이 하나씩 추가되는 애니메이션

---

### STEP 6: 후처리 (Post-processing)

**핵심 질문**: 왜 가끔 거절하는가?

| 학습 내용 | 구현 |
|----------|------|
| 안전 필터 | SafetyFilter |
| 콘텐츠 정책 | ContentPolicy |
| 출력 포맷팅 | OutputFormatter |
| 응답 구조화 | ResponseParser |

**시각화**: 필터 체크리스트

---

## 프로젝트 구조

```
ai-process-part1/
├── step1-tokenization/
│   ├── Tokenizer.java
│   ├── WhitespaceTokenizer.java
│   ├── CodeTokenizer.java
│   └── TokenizerDemo.java
│
├── step2-context/
│   ├── ContextBuilder.java
│   ├── ConversationMemory.java
│   ├── PromptTemplate.java
│   └── ContextDemo.java
│
├── step3-probability/
│   ├── BigramModel.java
│   ├── TrigramModel.java
│   ├── NgramModel.java
│   ├── SmoothingStrategy.java
│   └── ProbabilityDemo.java
│
├── step4-sampling/
│   ├── Sampler.java
│   ├── TemperatureSampler.java
│   ├── TopKSampler.java
│   ├── TopPSampler.java
│   └── SamplingDemo.java
│
├── step5-generation/
│   ├── Generator.java
│   ├── AutoRegressiveGenerator.java
│   ├── StopCondition.java
│   └── GenerationDemo.java
│
├── step6-postprocess/
│   ├── SafetyFilter.java
│   ├── OutputFormatter.java
│   └── PostprocessDemo.java
│
├── pipeline/
│   ├── AIPipeline.java          # 전체 파이프라인 통합
│   └── PipelineDemo.java        # 전체 프로세스 시각화
│
├── cli/
│   └── ProcessCli.java          # CLI 도구
│
├── docs/
│   ├── STEP-01-토큰화.md
│   ├── STEP-02-컨텍스트.md
│   ├── STEP-03-확률계산.md
│   ├── STEP-04-샘플링.md
│   ├── STEP-05-생성루프.md
│   ├── STEP-06-후처리.md
│   └── demo/
│       └── (각 단계별 실행 로그)
│
└── visualization/
    └── (시각화 자료)
```

---

## CLI 사용 예시

```bash
# 전체 파이프라인 실행 (각 단계 시각화)
$ ai-process run "오늘 날씨 어때?" --verbose

# 개별 단계 실행
$ ai-process tokenize "오늘 날씨 어때?"
$ ai-process context --system "친절한 AI" --user "오늘 날씨 어때?"
$ ai-process probability --prev "오늘" --model trigram
$ ai-process sample --distribution "오늘:0.35,지금:0.25" --temperature 0.7
$ ai-process generate --prompt "오늘 날씨" --max-tokens 20
```

---

## 핵심 원칙

1. **프로세스 중심**: 코드보다 "과정"을 이해하는 것이 목표
2. **시각화 필수**: 각 단계마다 시각적으로 보여주기
3. **단계별 진행**: 한 번에 완성하지 않고, 단계별로 만들기
4. **문서 필수**: 각 단계마다 docs/STEP-XX.md 작성
5. **데모 필수**: 각 단계마다 실행 가능한 데모 제공

---

## 산출물 체크리스트

각 STEP 완료 시:
- [ ] docs/STEP-XX.md 문서
- [ ] 해당 단계 코드 구현
- [ ] 데모 실행 가능
- [ ] CLI에서 해당 단계 실행 가능
- [ ] 시각화 자료 (다이어그램 또는 출력)

---

# Part 2: 코드 이해 프로세스 (예정)

## 목표

**AI가 코드를 분석하고 이해하는 과정을 단계별로 구현**

```
코드 입력 → 파싱 → AST → 의미분석 → 패턴매칭 → 이슈탐지 → 점수화
```

### 학습 단계 (예정)

| STEP | 제목 | 핵심 질문 |
|------|------|----------|
| 01 | 파싱 | 코드를 어떻게 읽는가? |
| 02 | AST | 코드의 구조를 어떻게 파악하는가? |
| 03 | 의미 분석 | 변수/타입을 어떻게 추적하는가? |
| 04 | 패턴 매칭 | 나쁜 코드를 어떻게 찾는가? |
| 05 | 이슈 탐지 | 버그/보안 문제를 어떻게 발견하는가? |
| 06 | 점수화 | 코드 품질을 어떻게 측정하는가? |

---

# Part 3: AI 서비스 프로세스 (예정)

## 목표

**AI를 실제 서비스로 연결하는 과정을 단계별로 구현**

```
요청 → API호출 → 프롬프트구성 → LLM처리 → 응답파싱 → 액션실행 → 피드백
```

### 학습 단계 (예정)

| STEP | 제목 | 핵심 질문 |
|------|------|----------|
| 01 | API 호출 | LLM API는 어떻게 사용하는가? |
| 02 | 프롬프트 | 좋은 프롬프트는 어떻게 만드는가? |
| 03 | LLM 라우팅 | 어떤 모델을 언제 쓰는가? |
| 04 | 응답 파싱 | AI 응답을 어떻게 처리하는가? |
| 05 | 액션 실행 | AI가 도구를 어떻게 사용하는가? |
| 06 | 피드백 루프 | 결과를 어떻게 개선하는가? |

---

## TODO

### Part 1 작업

- [x] 기존 코드를 새 구조로 재배치
- [x] step1-tokenization 완성 (기존 코드 활용)
- [x] step2-context 구현
- [x] step3-probability 완성 (기존 N-gram 활용)
- [x] step4-sampling 완성 (기존 Sampler 활용)
- [x] step5-generation 완성 (기존 Generator 활용)
- [x] step6-postprocess 구현
- [x] pipeline 통합
- [ ] CLI 업데이트 (process 명령어 추가)
- [ ] 각 단계별 문서 작성
- [ ] 시각화 자료 추가

### Part 2 작업

- [ ] Part 1 fork
- [ ] 코드 분석 프로세스 구현
- [ ] 문서 작성

### Part 3 작업

- [ ] Part 2 fork
- [ ] AI 서비스 프로세스 구현
- [ ] 문서 작성

---

## 참고

- 기존 코드: mini-ai-core, mini-ai-tokenizer-simple, code-ai-tokenizer, mini-ai-model-ngram
- GitHub: https://github.com/devload/code-ai-part1-basics
