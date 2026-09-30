package org.apache.roller.weblogger.pipeline;

import org.apache.roller.weblogger.pipeline.processors.MoodAnalyzerProcessor;
import org.apache.roller.weblogger.pipeline.processors.ProfanityProcessor;
import org.apache.roller.weblogger.pipeline.processors.SummaryProcessor;
import org.apache.roller.weblogger.pipeline.processors.TagGeneratorProcessor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the content processing pipeline.
 * Uses MockLLMService to avoid real API calls.
 */
class ContentProcessingPipelineTest {

    private MockLLMService mockLLM;
    private ProcessorConfig config;

    @BeforeEach
    void setUp() {
        mockLLM = new MockLLMService();
        config = new ProcessorConfig();
    }

    // ============================================================
    // Individual Processor Tests
    // ============================================================

    @Test
    void profanityProcessor_replacesContent() {
        mockLLM.setResponse("This is a clean blog post.");
        ProfanityProcessor processor = new ProfanityProcessor(mockLLM);

        String result = processor.process("This is a dirty blog post.");

        assertEquals("This is a clean blog post.", result);
        assertEquals(1, mockLLM.getCallCount());
    }

    @Test
    void profanityProcessor_emptyResponse_returnsOriginal() {
        mockLLM.setResponse("");
        ProfanityProcessor processor = new ProfanityProcessor(mockLLM);

        String result = processor.process("Original content");

        assertEquals("Original content", result);
    }

    @Test
    void summaryProcessor_prependsSummary() {
        mockLLM.setResponse("A short summary of the blog.");
        SummaryProcessor processor = new SummaryProcessor(mockLLM);

        String result = processor.process("Full blog content here.");

        assertTrue(result.startsWith("Quick Summary:"));
        assertTrue(result.contains("A short summary of the blog."));
        assertTrue(result.endsWith("Full blog content here."));
    }

    @Test
    void summaryProcessor_emptyResponse_returnsOriginal() {
        mockLLM.setResponse("");
        SummaryProcessor processor = new SummaryProcessor(mockLLM);

        String result = processor.process("Original content");

        assertEquals("Original content", result);
    }

    @Test
    void tagGeneratorProcessor_appendsTags() {
        mockLLM.setResponse("#java #spring #coding");
        TagGeneratorProcessor processor = new TagGeneratorProcessor(mockLLM);

        String result = processor.process("Blog about Java programming.");

        assertTrue(result.startsWith("Blog about Java programming."));
        assertTrue(result.contains("Tags:"));
        assertTrue(result.contains("#java #spring #coding"));
    }

    @Test
    void tagGeneratorProcessor_emptyResponse_returnsOriginal() {
        mockLLM.setResponse("");
        TagGeneratorProcessor processor = new TagGeneratorProcessor(mockLLM);

        String result = processor.process("Original content");

        assertEquals("Original content", result);
    }

    @Test
    void moodAnalyzerProcessor_prependsMood_positive() {
        mockLLM.setResponse("Positive");
        MoodAnalyzerProcessor processor = new MoodAnalyzerProcessor(mockLLM);

        String result = processor.process("Great day today!");

        assertTrue(result.contains("Post Mood:"));
        assertTrue(result.contains("Positive"));
        assertTrue(result.contains("Great day today!"));
    }

    @Test
    void moodAnalyzerProcessor_unknownMood_defaultsToInformative() {
        mockLLM.setResponse("Unknown");
        MoodAnalyzerProcessor processor = new MoodAnalyzerProcessor(mockLLM);

        String result = processor.process("Some content.");

        assertTrue(result.contains("Informative"));
    }

    // ============================================================
    // Chain Tests
    // ============================================================

    @Test
    void chainOfTwo_executesInOrder() {
        mockLLM.setResponses("cleaned content", "#tag1 #tag2");

        ProfanityProcessor first = new ProfanityProcessor(mockLLM);
        TagGeneratorProcessor second = new TagGeneratorProcessor(mockLLM);
        first.setNext(second);

        String result = first.process("raw content");

        assertTrue(result.contains("cleaned content"));
        assertTrue(result.contains("Tags:"));
        assertTrue(result.contains("#tag1 #tag2"));
        assertEquals(2, mockLLM.getCallCount());
    }

    @Test
    void chainOfThree_executesInOrder() {
        mockLLM.setResponses("cleaned", "A summary", "#tag1");

        ProfanityProcessor p1 = new ProfanityProcessor(mockLLM);
        SummaryProcessor p2 = new SummaryProcessor(mockLLM);
        TagGeneratorProcessor p3 = new TagGeneratorProcessor(mockLLM);
        p1.setNext(p2);
        p2.setNext(p3);

        String result = p1.process("raw input");

        assertTrue(result.contains("Quick Summary:"));
        assertTrue(result.contains("Tags:"));
        assertEquals(3, mockLLM.getCallCount());
    }

    // ============================================================
    // Pipeline Orchestrator Tests
    // ============================================================

    @Test
    void pipeline_allEnabled_processesAllFour() {
        mockLLM.setResponses("cleaned text", "A summary.", "#tag1 #tag2", "Positive");

        ContentProcessingPipeline pipeline = new ContentProcessingPipeline(config, mockLLM);
        String result = pipeline.process("Hello world blog.");

        assertEquals(4, mockLLM.getCallCount());
        assertNotNull(result);
        assertFalse(result.isEmpty());
    }

    @Test
    void pipeline_nullContent_returnsNull() {
        ContentProcessingPipeline pipeline = new ContentProcessingPipeline(config, mockLLM);
        String result = pipeline.process(null);
        assertNull(result);
    }

    @Test
    void pipeline_emptyContent_returnsEmpty() {
        ContentProcessingPipeline pipeline = new ContentProcessingPipeline(config, mockLLM);
        String result = pipeline.process("   ");
        assertEquals("   ", result);
    }

    @Test
    void pipeline_allDisabled_returnsOriginal() {
        config.setEnabled("ProfanityRewriter", false);
        config.setEnabled("BlogSummarizer", false);
        config.setEnabled("SmartTagGenerator", false);
        config.setEnabled("ToneMoodAnalyzer", false);

        ContentProcessingPipeline pipeline = new ContentProcessingPipeline(config, mockLLM);
        String result = pipeline.process("Untouched content.");

        assertEquals("Untouched content.", result);
        assertEquals(0, mockLLM.getCallCount());
    }

    @Test
    void pipeline_onlyProfanityEnabled() {
        config.setEnabled("BlogSummarizer", false);
        config.setEnabled("SmartTagGenerator", false);
        config.setEnabled("ToneMoodAnalyzer", false);
        mockLLM.setResponse("clean text");

        ContentProcessingPipeline pipeline = new ContentProcessingPipeline(config, mockLLM);
        String result = pipeline.process("bad text");

        assertEquals("clean text", result);
        assertEquals(1, mockLLM.getCallCount());
    }

    @Test
    void pipeline_onlySummaryAndTagsEnabled() {
        config.setEnabled("ProfanityRewriter", false);
        config.setEnabled("ToneMoodAnalyzer", false);
        mockLLM.setResponses("Short summary.", "#blogging #tech");

        ContentProcessingPipeline pipeline = new ContentProcessingPipeline(config, mockLLM);
        String result = pipeline.process("My tech blog post.");

        assertTrue(result.contains("Quick Summary:"));
        assertTrue(result.contains("Tags:"));
        assertEquals(2, mockLLM.getCallCount());
    }

    // ============================================================
    // Error Handling Tests
    // ============================================================

    @Test
    void processor_llmFailure_passesContentUnchanged() {
        mockLLM.setShouldFail(true);
        ProfanityProcessor processor = new ProfanityProcessor(mockLLM);

        String result = processor.process("Original text");

        assertEquals("Original text", result);
    }

    @Test
    void chain_firstFails_secondStillRuns() {
        // First call fails, second succeeds
        MockLLMService failingLLM = new MockLLMService();
        failingLLM.setShouldFail(true);
        ProfanityProcessor first = new ProfanityProcessor(failingLLM);

        MockLLMService successLLM = new MockLLMService();
        successLLM.setResponse("#tag1 #tag2");
        TagGeneratorProcessor second = new TagGeneratorProcessor(successLLM);

        first.setNext(second);
        String result = first.process("Original content");

        // First processor fails → passes "Original content" to second
        assertTrue(result.contains("Original content"));
        assertTrue(result.contains("Tags:"));
        assertTrue(result.contains("#tag1 #tag2"));
    }

    // ============================================================
    // BlogEntryProcessorHook Tests
    // ============================================================

    @Test
    void hook_processBeforeSave_delegatesToPipeline() {
        mockLLM.setResponses("cleaned", "summary", "#tags", "Positive");

        BlogEntryProcessorHook hook = new BlogEntryProcessorHook(config, mockLLM);
        String result = hook.processBeforeSave("Raw blog content.");

        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals(4, mockLLM.getCallCount());
    }

    @Test
    void hook_nullContent_returnsNull() {
        BlogEntryProcessorHook hook = new BlogEntryProcessorHook(config, mockLLM);
        String result = hook.processBeforeSave(null);
        assertNull(result);
    }

    // ============================================================
    // ProcessorConfig Tests
    // ============================================================

    @Test
    void config_defaultsAllEnabled() {
        ProcessorConfig cfg = new ProcessorConfig();
        assertTrue(cfg.isEnabled("ProfanityRewriter"));
        assertTrue(cfg.isEnabled("BlogSummarizer"));
        assertTrue(cfg.isEnabled("SmartTagGenerator"));
        assertTrue(cfg.isEnabled("ToneMoodAnalyzer"));
    }

    @Test
    void config_unknownProcessor_returnsFalse() {
        ProcessorConfig cfg = new ProcessorConfig();
        assertFalse(cfg.isEnabled("NonexistentProcessor"));
    }

    @Test
    void config_toggleProcessor() {
        ProcessorConfig cfg = new ProcessorConfig();
        cfg.setEnabled("ProfanityRewriter", false);
        assertFalse(cfg.isEnabled("ProfanityRewriter"));

        cfg.setEnabled("ProfanityRewriter", true);
        assertTrue(cfg.isEnabled("ProfanityRewriter"));
    }
}
