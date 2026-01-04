# CLAUDE.md - AI Process 학습 시리즈 가이드

## 프로젝트 개요

이 프로젝트는 **AI가 어떻게 동작하는가?**를 프로세스 관점에서 학습하는 교육용 프로젝트입니다.

> **반드시 `mission.md`를 먼저 읽고 전체 계획을 파악하세요.**

---

## 핵심 파일

| 파일 | 설명 |
|------|------|
| `mission.md` | **전체 프로젝트 계획 및 TODO** - 반드시 참조 |
| `README.md` | 사용자용 가이드 |
| `docs/STEP-XX.md` | 각 단계별 학습 문서 |

---

## 현재 진행 상황

### Part 1: AI 기초 프로세스

**목표**: AI가 프롬프트를 받아 응답을 생성하기까지의 내부 과정 이해

```
토큰화 → 컨텍스트 → 확률계산 → 샘플링 → 생성루프 → 후처리
```

### 현재 TODO (mission.md 참조)

#### Part 1 작업
- [ ] 기존 코드를 새 구조로 재배치
- [ ] step1-tokenization 완성 (기존 코드 활용)
- [ ] step2-context 구현
- [ ] step3-probability 완성 (기존 N-gram 활용)
- [ ] step4-sampling 완성 (기존 Sampler 활용)
- [ ] step5-generation 완성 (기존 Generator 활용)
- [ ] step6-postprocess 구현
- [ ] pipeline 통합
- [ ] CLI 업데이트
- [ ] 각 단계별 문서 작성
- [ ] 시각화 자료 추가

---

## 작업 규칙

### 1. 프로세스 중심

코드 구현보다 **"AI가 어떤 과정을 거치는가"**를 보여주는 것이 목표입니다.

### 2. 단계별 진행

한 번에 완성하지 않고, 각 STEP을 순서대로 완성합니다:

```
STEP 1 완성 → STEP 2 완성 → ... → STEP 6 완성 → 파이프라인 통합
```

### 3. 산출물 필수

각 STEP 완료 시 다음 산출물이 있어야 합니다:
- `docs/STEP-XX-제목.md` 문서
- 해당 단계 코드 구현
- 데모 실행 가능
- 시각화 자료 (콘솔 출력 또는 다이어그램)

### 4. 시각화

각 단계에서 무슨 일이 일어나는지 **눈에 보이게** 출력해야 합니다:

```bash
# 예시: 토큰화 단계 시각화
$ ai-process tokenize "오늘 날씨 어때?"

========================================
STEP 1: 토큰화
========================================
입력: "오늘 날씨 어때?"
토큰: ["오늘", "날씨", "어때", "?"]
ID:   [1042, 2891, 892, 15]
```

---

## 프로젝트 구조

### 현재 구조 (기존 코드)
```
├── mini-ai-core/              # 인터페이스
├── mini-ai-tokenizer-simple/  # WhitespaceTokenizer
├── code-ai-tokenizer/         # CodeTokenizer
├── mini-ai-model-ngram/       # Bigram, Trigram, N-gram
├── mini-ai-server/            # REST API
├── mini-ai-cli/               # CLI
└── docs/                      # 기존 문서
```

### 목표 구조 (재구성 후)
```
├── step1-tokenization/        # 토큰화
├── step2-context/             # 컨텍스트 구성
├── step3-probability/         # 확률 계산
├── step4-sampling/            # 샘플링
├── step5-generation/          # 생성 루프
├── step6-postprocess/         # 후처리
├── pipeline/                  # 전체 파이프라인
├── cli/                       # CLI
└── docs/                      # 새 문서 (STEP별)
```

---

## 활용 가능한 기존 코드

| 기존 모듈 | 새 위치 | 활용 방법 |
|----------|---------|----------|
| WhitespaceTokenizer | step1-tokenization | 그대로 활용 |
| CodeTokenizer | step1-tokenization | 그대로 활용 |
| BigramModel | step3-probability | 그대로 활용 |
| TrigramModel | step3-probability | 그대로 활용 |
| NgramModel | step3-probability | 그대로 활용 |
| Sampler | step4-sampling | 그대로 활용 |
| BigramModel.generate | step5-generation | 분리하여 활용 |

---

## 명령어

### 빌드
```bash
./gradlew build
```

### 현재 CLI
```bash
./gradlew :mini-ai-cli:run --args="tokenize 'Hello World'"
./gradlew :mini-ai-cli:run --args="complete 'public class'"
```

### 목표 CLI (재구성 후)
```bash
ai-process run "오늘 날씨 어때?" --verbose  # 전체 파이프라인
ai-process tokenize "오늘 날씨 어때?"       # 개별 단계
```

---

## 주의사항

1. **mission.md의 TODO를 항상 확인**하고 작업
2. 각 STEP은 **독립적으로 실행 가능**해야 함
3. **시각화 출력**을 통해 과정을 보여줄 것
4. 문서는 **비개발자도 이해할 수 있게** 작성

---

## 참고 링크

- GitHub: https://github.com/devload/code-ai-part1-basics
- Part 2 (예정): https://github.com/devload/code-ai-part2-analyzer
- Part 3 (예정): https://github.com/devload/code-ai-part3-service
