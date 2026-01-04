# AI Process - Part 1: 기초 프로세스

> **AI가 어떻게 동작하는가?** 를 프로세스 관점에서 이해하는 교육용 프로젝트

---

## 학습 목표

**AI가 프롬프트를 받아 응답을 생성하기까지의 내부 과정을 이해합니다**

```
사용자: "오늘 날씨 어때?"
        │
        ▼
┌─────────────────────────────────────────────────────────┐
│ STEP 1: 토큰화                                           │
│ "오늘 날씨 어때?" → [오늘] [날씨] [어때] [?]              │
└─────────────────────────────────────────────────────────┘
        │
        ▼
┌─────────────────────────────────────────────────────────┐
│ STEP 2: 컨텍스트 구성                                    │
│ [시스템 프롬프트] + [히스토리] + [사용자 입력]            │
└─────────────────────────────────────────────────────────┘
        │
        ▼
┌─────────────────────────────────────────────────────────┐
│ STEP 3: 확률 계산 (N-gram)                               │
│ 다음 토큰 후보: "좋다"(35%), "맑다"(25%), "흐리다"(20%)...│
└─────────────────────────────────────────────────────────┘
        │
        ▼
┌─────────────────────────────────────────────────────────┐
│ STEP 4: 샘플링                                           │
│ Temperature, Top-k, Top-p 적용 → "좋다" 선택             │
└─────────────────────────────────────────────────────────┘
        │
        ▼
┌─────────────────────────────────────────────────────────┐
│ STEP 5: 생성 루프                                        │
│ "좋다" → "좋다고" → "좋다고 생각해요" → ... → [END]       │
└─────────────────────────────────────────────────────────┘
        │
        ▼
┌─────────────────────────────────────────────────────────┐
│ STEP 6: 후처리                                           │
│ 안전 필터 체크 → 포맷 정리 → 최종 출력                   │
└─────────────────────────────────────────────────────────┘
        │
        ▼
AI: "오늘 날씨 좋다고 생각해요!"
```

---

## 프로젝트 구조

```
ai-process-part1/
├── step1-tokenization/        # STEP 1: 토큰화
│   ├── Tokenizer.java         # 토큰화 인터페이스
│   ├── WhitespaceTokenizer.java
│   ├── CodeTokenizer.java
│   └── TokenizationDemo.java
│
├── step2-context/             # STEP 2: 컨텍스트 구성
│   ├── ContextBuilder.java
│   ├── ConversationMemory.java
│   ├── PromptTemplate.java
│   └── ContextDemo.java
│
├── step3-probability/         # STEP 3: 확률 계산
│   ├── ProbabilityModel.java
│   ├── BigramModel.java
│   ├── TrigramModel.java
│   └── ProbabilityDemo.java
│
├── step4-sampling/            # STEP 4: 샘플링
│   ├── Sampler.java
│   └── SamplingDemo.java
│
├── step5-generation/          # STEP 5: 생성 루프
│   ├── Generator.java
│   └── GenerationDemo.java
│
├── step6-postprocess/         # STEP 6: 후처리
│   ├── SafetyFilter.java
│   ├── OutputFormatter.java
│   └── PostprocessDemo.java
│
├── pipeline/                  # 전체 파이프라인 통합
│   ├── AIPipeline.java
│   └── PipelineDemo.java
│
├── mini-ai-core/              # 기존 코어 인터페이스
├── mini-ai-tokenizer-simple/  # 기존 토크나이저
├── code-ai-tokenizer/         # 코드 토크나이저
├── mini-ai-model-ngram/       # N-gram 모델
├── mini-ai-server/            # REST API 서버
├── mini-ai-cli/               # CLI
│
├── docs/                      # 문서
│   └── STEP-XX-*.md
│
├── mission.md                 # 전체 프로젝트 계획
└── CLAUDE.md                  # AI 작업 가이드
```

---

## 학습 단계

| STEP | 제목 | 핵심 질문 | 파일 |
|------|------|----------|------|
| 1 | 토큰화 | 왜 문장을 조각내는가? | `step1-tokenization/` |
| 2 | 컨텍스트 | AI는 뭘 보고 답하는가? | `step2-context/` |
| 3 | 확률 계산 | 다음 단어를 어떻게 예측하는가? | `step3-probability/` |
| 4 | 샘플링 | 왜 매번 답이 다른가? | `step4-sampling/` |
| 5 | 생성 루프 | 한 번에 쓰는가, 한 글자씩 쓰는가? | `step5-generation/` |
| 6 | 후처리 | 왜 가끔 거절하는가? | `step6-postprocess/` |

---

## 빠른 시작

### 빌드
```bash
./gradlew build
```

### 각 단계 데모 실행

```bash
# STEP 1: 토큰화 데모
./gradlew :step1-tokenization:run

# STEP 2: 컨텍스트 데모
./gradlew :step2-context:run

# STEP 3: 확률 계산 데모
./gradlew :step3-probability:run

# STEP 4: 샘플링 데모
./gradlew :step4-sampling:run

# STEP 5: 생성 루프 데모
./gradlew :step5-generation:run

# STEP 6: 후처리 데모
./gradlew :step6-postprocess:run

# 전체 파이프라인 데모
./gradlew :pipeline:run
```

### 기존 CLI 사용
```bash
# 도움말
./gradlew :mini-ai-cli:run

# 코드 자동완성
./gradlew :mini-ai-cli:run --args='complete "public class User {" -n 3'

# 토큰화 테스트
./gradlew :mini-ai-cli:run --args='tokenize "Hello World"'
```

---

## 핵심 개념

### 1. 토큰 (Token)
AI가 이해하는 텍스트의 최소 단위. 단어, 서브워드, 문자 등이 될 수 있음.

### 2. 컨텍스트 윈도우 (Context Window)
AI가 한 번에 볼 수 있는 토큰 수의 제한. GPT-4는 8K~128K tokens.

### 3. Temperature
출력의 "창의성"을 조절하는 파라미터.
- 낮음 (0.1): 확정적, 반복적
- 높음 (2.0): 창의적, 예측불가

### 4. Auto-regressive Generation
토큰을 하나씩 순차적으로 생성하는 방식. 이전 토큰이 다음 토큰 예측에 영향.

---

## 시리즈 구성

```
Part 1: AI 기초 프로세스 (현재)
├── 토큰화 → 컨텍스트 → 확률계산 → 샘플링 → 생성루프 → 후처리
│
├── Part 2: 코드 이해 프로세스
│   └── 파싱 → AST → 의미분석 → 패턴매칭 → 이슈탐지 → 점수화
│
└── Part 3: AI 서비스 프로세스
    └── API호출 → 프롬프트구성 → LLM처리 → 응답파싱 → 액션실행
```

---

## 다음 단계

👉 [Part 2: 코드 분석 AI편](https://github.com/devload/code-ai-part2-analyzer)

👉 [Part 3: 서비스화편](https://github.com/devload/code-ai-part3-service)

---

## 라이선스

MIT License

---

**Version**: 2.0.0 | **Focus**: AI Process Education
