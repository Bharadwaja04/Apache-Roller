# Apache Roller: refactoring and AI features

My contributions to a course project (CS6.401 Software Engineering, IIIT Hyderabad, Team 20) built on **Apache Roller 6.1.5**, a Java
multi-user blog server. It was a team project, so the repository also contains teammates' code; **this README covers only my part**:

1. **Refactoring** the Weblog & Content subsystem after design-smell and metrics analysis.
2. An **LLM content-processing pipeline** that runs when a post is saved, with admin controls.
3. **Community Pulse conversation breakdown**: theme and summary analysis of a post's comments.
4. A **blog Q&A chatbot** with two answering strategies.

Apache Roller is licensed under Apache 2.0 (see `LICENSE.txt` and `NOTICE.txt`); the original README is kept at
[`docs/UPSTREAM_README.md`](docs/UPSTREAM_README.md).

## Run it

Requirements: JDK 11+ and Maven 3.6+.

```bash
git clone <this repository> roller && cd roller
mvn -DskipTests=true install          # build
mvn jetty:run                         # run with an embedded Derby database (for testing only)
```

Open <http://localhost:8080/roller>, create the first user (it becomes the admin), then create a blog.

Docker alternative: `docker-compose up` (see `docs/UPSTREAM_README.md`).

### Optional AI features and their keys

Keys are **never stored in source**. Without a key the AI features switch off gracefully.

| Feature | How to give it the key |
|---|---|
| Content-processing pipeline | Admin: **Global Configuration → Content Processing Pipeline → LLM API Key**, or the `ROLLER_LLM_API_KEY` environment variable |
| Conversation breakdown (LLM mode) | `GROQ_API_KEY` environment variable (falls back to keyword clustering when absent) |
| Blog Q&A chatbot | `GROQ_API_KEY` environment variable, or `-Dgroq.api.key=...` |

```bash
export GROQ_API_KEY=...          # your own key; do not commit it
export ROLLER_LLM_API_KEY=...
mvn jetty:run
```

### Tests

```bash
mvn test                                              # whole suite
mvn -pl app test -Dtest=ContentProcessingPipelineTest # the content pipeline (23 tests, mocked LLM)
```

> The maintainers of this repository could not run the Maven build in the environment used to prepare this README, so the
> test results are not reported here. Run the commands above locally before relying on them.

## My contributions (Vadali S S Bharadwaja)

### 1. Refactoring the Weblog & Content subsystem (project 1)

Analysis found god classes with high weighted methods per class, a service locator, cyclic dependencies between domain objects and services,
and business logic living inside POJOs. I moved that logic into focused service classes without changing the public behaviour of the domain model.

Measured against the untouched Apache Roller 6.1.5 source (lines of code, including comments):

| Class | 6.1.5 | This repository |
|---|---:|---:|
| `pojos/WeblogEntry.java` | 1,030 | 858 |
| `pojos/Weblog.java` | 925 | 756 |
| `business/jpa/JPAWeblogManagerImpl.java` | 723 | 417 |

The extracted responsibilities live in new classes such as `WeblogContentService`, `WeblogEntryCommentService`, `CommentStatusService`,
`CategoryPositionService`, `WeblogEntryPermissionService`, `WeblogEntryUrlService`, `WeblogInitializationService`, `WeblogRemovalService`,
`WeblogValidator` and `WeblogCacheManager`.

Also from project 1 (documentation, in [`docs/project1_20.pdf`](docs/project1_20.pdf)): post-refactoring metrics, and a design for an automated
refactoring pipeline (smell detection, LLM refactoring, pull request), plus a bonus comparison of two LLMs in agentic and non-agentic modes
([`docs/project1_bonus_20.pdf`](docs/project1_bonus_20.pdf)).

> The report also states a cyclomatic-complexity reduction for `HTMLSanitizer` and `PageServlet`. Those two files are unchanged from 6.1.5 in
> this repository, so that claim is not reproducible here and is not repeated.

### 2. Content-processing pipeline ("Transforming Feeds")

Every time a post is saved, its text can pass through a configurable chain of LLM-backed processors before it is stored.

```
Save post → BlogEntryProcessorHook → ContentProcessingPipeline → [Profanity → Summary → Tags → Mood] → stored content
```

- **Chain of Responsibility.** `ContentProcessor` defines the contract, `AbstractContentProcessor` implements the chaining, and four concrete
  processors do the work: `ProfanityProcessor` (rewrites offensive words), `SummaryProcessor` (adds a short "Quick Summary" at the top),
  `TagGeneratorProcessor` (appends hashtags) and `MoodAnalyzerProcessor`.
- **Admin-controlled.** `ProcessorConfig` reads runtime properties (`contentprocessing.*.enabled`) declared in `runtimeConfigDefs.xml`
  and shown on the admin **Global Configuration** page, so an administrator switches the whole pipeline or each step on and off, and chooses the
  model. The chain is rebuilt from the enabled processors on every save.
- **Fails safe.** A failing processor passes its input on unchanged, and any pipeline error returns the original text, so a post is never lost
  because an LLM call failed. With no API key, the hook skips the pipeline.
- **Testable.** The LLM sits behind a small `LLMService` interface (`LLMClient` calls an OpenAI-compatible endpoint, Groq by default with
  `llama-3.3-70b-versatile`). Tests inject `MockLLMService`.
- **Where to look:** `app/src/main/java/org/apache/roller/weblogger/pipeline/` (design notes in `LLM_Content_Processing_Pipeline.md`),
  hooked in at `JPAWeblogEntryManagerImpl`.

Trade-offs: each enabled processor is a separate network call made while saving, so saving gets slower with more steps; output is
non-deterministic; and the pipeline rewrites the author's text, which is why it is opt-in per step.

### 3. Community Pulse: conversation breakdown

Groups the comments on a post into discussion themes, with representative comments and an overall summary.

- **Strategy pattern.** `InsightGenerationStrategy` has two implementations: `KeywordClusteringStrategy` (classical keyword-frequency clustering,
  no external service) and `LLMSummarizationStrategy` (themes and summary from an LLM via `GroqAPIClient`).
- **Automatic selection with fallback.** `ConversationBreakdownCalculator` uses the LLM strategy when a key is configured and the call succeeds,
  and otherwise falls back to keyword clustering, so the page always returns something.
- **UI.** `PulseBreakdown` action and `ConversationBreakdownView.jsp`, opened with the "View Pulse" button next to each post on the Entries page.

### 4. Blog Q&A chatbot (bonus)

Answers natural-language questions about published posts through a common `ChatbotAnswerStrategy` interface with two strategies,
picked at runtime through `ChatbotService`:

- **RAG strategy:** filters the 10 most recent published entries by keyword match with the question, keeps up to 3 (500 characters each),
  and sends them as context to the LLM.
- **Long-context strategy:** adds the last 10 question/answer exchanges to the same prompt for multi-turn conversation.

**Known limitations (deliberate scope for a course bonus, not production-ready):**
retrieval is keyword matching, not vector search; it searches recent entries across all weblogs, not one blog; the long-context history is a static
in-memory list shared by all users, not per session, so it must not be deployed as is; and there are no automated tests for the chatbot.

## Security note

The original coursework code contained a hard-coded LLM API key and a SonarQube token. They were removed before publishing, the keys are now read
from the environment (see above), and both credentials should be treated as compromised and revoked.

## Documentation

- Design notes for the pipeline: `app/src/main/java/org/apache/roller/weblogger/pipeline/LLM_Content_Processing_Pipeline.md`
- Project reports: [`docs/project1_20.pdf`](docs/project1_20.pdf) (refactoring, metrics, LLM refactoring pipeline design),
  [`docs/project1_bonus_20.pdf`](docs/project1_bonus_20.pdf) (LLM comparison), [`docs/project2_20.pdf`](docs/project2_20.pdf) (features),
  [`docs/project2_bonus1_20.pdf`](docs/project2_bonus1_20.pdf) (Q&A chatbot)
- Architecture and design recovery: [`DOCUMENTATION/`](DOCUMENTATION/) (`00_MASTER_INDEX.txt` first)
- Upstream Roller guides: [`docs/`](docs/) (install, user and template guides)

## License

Apache License 2.0, as for Apache Roller. See `LICENSE.txt` and `NOTICE.txt`.
