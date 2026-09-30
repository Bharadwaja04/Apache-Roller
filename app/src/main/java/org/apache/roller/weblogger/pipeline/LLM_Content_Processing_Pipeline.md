# LLM Content Processing Pipeline — Documentation

---

## 1. Overview

The **LLM Content Processing Pipeline** is a modular, extensible system integrated into **Apache Roller Weblogger** that automatically processes blog content through multiple AI-powered stages before saving to the database. It uses **Groq's LLM API** (OpenAI-compatible) to enhance blog posts with profanity filtering, summarization, tag generation, and mood analysis.

---

## 2. Design Pattern: Chain of Responsibility

### What is Chain of Responsibility?

The **Chain of Responsibility** pattern allows a request to pass through a chain of handlers (processors). Each handler either processes the request and passes it to the next handler, or simply passes it along unchanged. The client only needs to send the request to the first handler in the chain.

### Why This Pattern?

| Benefit | How It Applies |
|---------|---------------|
| **Decoupling** | Each processor is independent — doesn't know about others |
| **Extensibility** | Add a new processor without modifying existing ones |
| **Flexibility** | Enable/disable any processor via config |
| **Single Responsibility** | Each processor does exactly one thing |
| **Order Control** | Pipeline decides the execution order |

### Classes Mapped to the Pattern

| Pattern Role | Class | Responsibility |
|-------------|-------|---------------|
| **Handler Interface** | `ContentProcessor` | Defines `process()`, `setNext()`, `getName()` |
| **Base Handler** | `AbstractContentProcessor` | Implements chaining logic, holds `nextProcessor` reference |
| **Concrete Handler 1** | `ProfanityProcessor` | Replaces offensive words with polite alternatives |
| **Concrete Handler 2** | `SummaryProcessor` | Prepends a 40-50 word summary |
| **Concrete Handler 3** | `TagGeneratorProcessor` | Appends 3-5 relevant hashtags |
| **Concrete Handler 4** | `MoodAnalyzerProcessor` | Prepends mood emoji based on tone |
| **Chain Builder** | `ContentProcessingPipeline` | Builds and executes the processor chain |
| **Client/Hook** | `BlogEntryProcessorHook` | Entry point — connects pipeline to blog save flow |
| **External Service** | `LLMService` / `LLMClient` | Strategy pattern for LLM communication |
| **Configuration** | `ProcessorConfig` | Toggles processors on/off |

---

## 3. Architecture Diagram

```
┌─────────────────────────────────────────────────────────────────────┐
│                        USER (Browser)                               │
│                    Writes blog → Clicks Save                        │
└──────────────────────────┬──────────────────────────────────────────┘
                           │
                           ▼
┌─────────────────────────────────────────────────────────────────────┐
│               JPAWeblogEntryManagerImpl                             │
│                   saveWeblogEntry(entry)                            │
│                          │                                          │
│              rawText = entry.getText()                              │
│                          │                                          │
│              processedText = hook.processBeforeSave(rawText)        │
│                          │                                          │
│              entry.setText(processedText)                           │
│                          │                                          │
│              strategy.store(entry) → DATABASE                      │
└──────────────────────────┬──────────────────────────────────────────┘
                           │
                           ▼
┌─────────────────────────────────────────────────────────────────────┐
│                  BlogEntryProcessorHook                             │
│                                                                     │
│  ┌─────────────┐  ┌──────────────────────┐  ┌───────────────────┐  │
│  │ProcessorConfig│  │ContentProcessingPipeline│  │   LLMClient    │  │
│  │              │  │                      │  │  (Groq API)      │  │
│  │ Profanity: ✅│  │  Builds chain:       │  │                  │  │
│  │ Summary:  ✅│  │  P1 → P2 → P3 → P4  │  │  POST /v1/chat/  │  │
│  │ Tags:     ✅│  │                      │  │  completions     │  │
│  │ Mood:     ✅│  │  Calls P1.process()  │  │                  │  │
│  └─────────────┘  └──────────────────────┘  └───────────────────┘  │
└──────────────────────────┬──────────────────────────────────────────┘
                           │
                           ▼
┌─────────────────────────────────────────────────────────────────────┐
│                  CHAIN OF RESPONSIBILITY                            │
│                                                                     │
│  ┌──────────────┐    ┌──────────────┐    ┌──────────────┐    ┌──────────────┐
│  │  Profanity    │───▶│   Summary    │───▶│     Tags     │───▶│     Mood     │
│  │  Processor    │    │  Processor   │    │  Processor   │    │   Processor  │
│  │              │    │              │    │              │    │              │
│  │ "damn"→"darn"│    │ Prepend      │    │ Append       │    │ Prepend      │
│  │ "shit"→"mess"│    │ Quick Summary│    │ #hashtags    │    │ 📚 Mood      │
│  └──────┬───────┘    └──────┬───────┘    └──────┬───────┘    └──────┬───────┘
│         │                   │                   │                   │
│         ▼                   ▼                   ▼                   ▼
│    ┌─────────┐        ┌─────────┐        ┌─────────┐        ┌─────────┐
│    │ LLM API │        │ LLM API │        │ LLM API │        │ LLM API │
│    └─────────┘        └─────────┘        └─────────┘        └─────────┘
└─────────────────────────────────────────────────────────────────────┘
```

---

## 4. Data Flow (Entry to Exit)

```
                          INPUT
                            │
            "This damn system is shit..."
                            │
                            ▼
               ┌────────────────────────┐
               │   1. PROFANITY REWRITER │
               │                        │
               │  LLM Prompt:           │
               │  "Rewrite offensive    │
               │   words with polite    │
               │   alternatives..."     │
               │                        │
               │  Result:               │
               │  "This complicated     │
               │   system is a mess..." │
               └───────────┬────────────┘
                           │
                           ▼
               ┌────────────────────────┐
               │   2. BLOG SUMMARIZER   │
               │                        │
               │  LLM Prompt:           │
               │  "Summarize in         │
               │   40-50 words..."      │
               │                        │
               │  Result:               │
               │  Quick Summary:        │
               │  A blog about...       │
               │  ─────────────────     │
               │  (cleaned content)     │
               └───────────┬────────────┘
                           │
                           ▼
               ┌────────────────────────┐
               │  3. TAG GENERATOR      │
               │                        │
               │  LLM Prompt:           │
               │  "Generate 3-5         │
               │   hashtags..."         │
               │                        │
               │  Result:               │
               │  (previous content)    │
               │  Tags:                 │
               │  #distributed #caching │
               └───────────┬────────────┘
                           │
                           ▼
               ┌────────────────────────┐
               │  4. MOOD ANALYZER      │
               │                        │
               │  LLM Prompt:           │
               │  "Analyze tone:        │
               │   Informative/Positive │
               │   /Critical/Humorous"  │
               │                        │
               │  Result:               │
               │  Post Mood: 📚         │
               │  Informative           │
               │  (previous content)    │
               └───────────┬────────────┘
                           │
                           ▼
                         OUTPUT
                           │
        Post Mood: 📚 Informative
        Quick Summary: A blog about...
        ──────────────────────────
        This complicated system is a mess...
        Tags:
        #distributed #caching #scalability
```

---

## 5. File Structure

```
app/src/main/java/org/apache/roller/weblogger/
│
├── business/jpa/
│   └── JPAWeblogEntryManagerImpl.java          ← Modified (pipeline hook)
│
└── pipeline/
    ├── ContentProcessor.java                    ← Interface (Handler)
    ├── AbstractContentProcessor.java            ← Base class (chaining logic)
    ├── ContentProcessingPipeline.java           ← Chain builder & executor
    ├── ProcessorConfig.java                     ← Toggle processors on/off
    ├── BlogEntryProcessorHook.java              ← Integration entry point
    ├── LLMService.java                          ← Interface (Strategy)
    ├── LLMClient.java                           ← Groq/OpenAI HTTP client
    │
    └── processors/
        ├── ProfanityProcessor.java              ← Stage 1: Clean profanity
        ├── SummaryProcessor.java                ← Stage 2: Add summary
        ├── TagGeneratorProcessor.java           ← Stage 3: Add hashtags
        └── MoodAnalyzerProcessor.java           ← Stage 4: Add mood emoji

app/src/test/java/org/apache/roller/weblogger/
│
├── pipeline/
│   ├── MockLLMService.java                      ← Test double for LLM
│   └── ContentProcessingPipelineTest.java       ← 23 unit tests
│
└── business/jpa/
    └── JPAWeblogEntryManagerImplTest.java       ← 8 integration tests
```

---

## 6. Class Descriptions

### Core Interfaces

| Class | Type | Purpose |
|-------|------|---------|
| `ContentProcessor` | Interface | Defines `process(String)`, `setNext(ContentProcessor)`, `getName()` |
| `LLMService` | Interface | Defines `sendPrompt(String): String` — abstracts LLM provider |

### Core Classes

| Class | Purpose |
|-------|---------|
| `AbstractContentProcessor` | Base class implementing Chain of Responsibility — calls `applyTransformation()` then delegates to `nextProcessor` |
| `ContentProcessingPipeline` | Reads `ProcessorConfig`, builds the chain, and executes it |
| `ProcessorConfig` | Map-based config — toggle each processor on/off (all enabled by default) |
| `BlogEntryProcessorHook` | Creates pipeline with Groq API key, called from `JPAWeblogEntryManagerImpl` |
| `LLMClient` | HTTP client that sends prompts to Groq API and parses JSON responses |

### Processors (Concrete Handlers)

| Processor | Stage | LLM Prompt | Output |
|-----------|-------|-----------|--------|
| `ProfanityProcessor` | 1st | "Rewrite offensive words with polite alternatives" | Cleaned text |
| `SummaryProcessor` | 2nd | "Summarize in 40-50 words" | Prepends `Quick Summary:` section |
| `TagGeneratorProcessor` | 3rd | "Generate 3-5 hashtags" | Appends `Tags:` section |
| `MoodAnalyzerProcessor` | 4th | "Analyze tone: Informative/Positive/Critical/Humorous" | Prepends `Post Mood: 📚` |

### Mood Emoji Mapping

| Mood | Emoji |
|------|-------|
| Informative | 📚 |
| Positive | 😊 |
| Critical | 🧐 |
| Humorous | 😂 |
| Unknown | 📚 (default) |

---

## 7. Fault Tolerance

| Scenario | Behavior |
|----------|----------|
| No API key set | Pipeline skipped, blog saved as-is |
| Invalid API key | LLM returns 401, processors return original content |
| LLM server down | HTTP timeout, processors return original content |
| LLM returns empty | Processors return original content unchanged |
| Null/empty blog text | Pipeline skipped, returned as-is |
| One processor fails | Next processor still runs with whatever content it received |

---

## 8. Configuration

### Environment Variable

```bash
export ROLLER_LLM_API_KEY="gsk_your_groq_api_key"
```

### ProcessorConfig Defaults

```
ProfanityRewriter   = true
BlogSummarizer      = true
SmartTagGenerator   = true
ToneMoodAnalyzer    = true
```

### Disable a Processor

```java
ProcessorConfig config = new ProcessorConfig();
config.setEnabled("ProfanityRewriter", false);  // skip profanity filter
```

---

## 9. UML Class Diagram (PlantUML)

<img width="1887" height="1194" alt="image" src="https://github.com/user-attachments/assets/d3b0f83f-3f46-493f-9dbd-1ca7bf31cba9" />


---

## 10. UML Sequence Diagram (PlantUML)

<img width="1668" height="834" alt="image" src="https://github.com/user-attachments/assets/a408f3e8-0641-490b-b719-f556840c9623" />


---

## 11. Test Coverage Summary

| Test Class | Tests | What's Tested |
|-----------|-------|--------------|
| `ContentProcessingPipelineTest` | 23 | Individual processors, chaining, full pipeline, null/empty handling, config toggles, LLM failures, fallback behavior |
| `JPAWeblogEntryManagerImplTest` | 8 | Integration with BlogEntryProcessorHook, all-enabled, all-disabled, single/double processor combos, null safety |
| **Total** | **31** | **All pass ✅** |

---

## 12. How to Run

```bash
# Set API key
export ROLLER_LLM_API_KEY="gsk_your_groq_key"

# Run tests
mvn test -Dtest="ContentProcessingPipelineTest,JPAWeblogEntryManagerImplTest"

# Run website
mvn clean compile jetty:run
```
