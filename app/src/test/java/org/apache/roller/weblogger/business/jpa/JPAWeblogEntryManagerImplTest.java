package org.apache.roller.weblogger.business.jpa;

import org.apache.roller.weblogger.pipeline.BlogEntryProcessorHook;
import org.apache.roller.weblogger.pipeline.MockLLMService;
import org.apache.roller.weblogger.pipeline.ProcessorConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests verifying that the LLM pipeline integrates correctly
 * with the weblog entry save flow via BlogEntryProcessorHook.
 * These are unit-level tests; full integration with JPA is tested elsewhere.
 */
class JPAWeblogEntryManagerImplTest {

    private MockLLMService mockLLM;
    private ProcessorConfig config;

    @BeforeEach
    void setUp() {
        mockLLM = new MockLLMService();
        config = new ProcessorConfig(); // all processors enabled by default
    }

    /**
     * Verify that BlogEntryProcessorHook transforms content
     * when all processors are enabled.
     */
    @Test
    void hook_allProcessorsEnabled_transformsContent() {
        mockLLM.setResponses("cleaned text", "A summary.", "#java #blog", "Positive");

        BlogEntryProcessorHook hook = new BlogEntryProcessorHook(config, mockLLM);
        String raw = "My awesome blog about Java.";
        String processed = hook.processBeforeSave(raw);

        assertNotNull(processed);
        assertNotEquals(raw, processed);
        assertEquals(4, mockLLM.getCallCount());
    }

    /**
     * Verify pipeline does not modify content when all processors are disabled.
     */
    @Test
    void hook_allDisabled_contentUnchanged() {
        config.setEnabled("ProfanityRewriter", false);
        config.setEnabled("BlogSummarizer", false);
        config.setEnabled("SmartTagGenerator", false);
        config.setEnabled("ToneMoodAnalyzer", false);

        BlogEntryProcessorHook hook = new BlogEntryProcessorHook(config, mockLLM);
        String raw = "Unchanged content.";
        String processed = hook.processBeforeSave(raw);

        assertEquals(raw, processed);
        assertEquals(0, mockLLM.getCallCount());
    }

    /**
     * Verify pipeline safely handles null content.
     */
    @Test
    void hook_nullContent_returnsNull() {
        BlogEntryProcessorHook hook = new BlogEntryProcessorHook(config, mockLLM);
        String result = hook.processBeforeSave(null);
        assertNull(result);
    }

    /**
     * Verify pipeline safely handles empty/blank content.
     */
    @Test
    void hook_emptyContent_returnsUnchanged() {
        BlogEntryProcessorHook hook = new BlogEntryProcessorHook(config, mockLLM);
        String result = hook.processBeforeSave("  ");
        assertEquals("  ", result);
    }

    /**
     * Verify that if LLM service fails, original content is preserved.
     */
    @Test
    void hook_llmFailure_returnsOriginal() {
        mockLLM.setShouldFail(true);

        BlogEntryProcessorHook hook = new BlogEntryProcessorHook(config, mockLLM);
        String raw = "Safe original content.";
        String processed = hook.processBeforeSave(raw);

        assertEquals(raw, processed);
    }

    /**
     * Verify only profanity processor runs when others are disabled.
     */
    @Test
    void hook_onlyProfanityEnabled_singleProcessor() {
        config.setEnabled("BlogSummarizer", false);
        config.setEnabled("SmartTagGenerator", false);
        config.setEnabled("ToneMoodAnalyzer", false);
        mockLLM.setResponse("polite content");

        BlogEntryProcessorHook hook = new BlogEntryProcessorHook(config, mockLLM);
        String processed = hook.processBeforeSave("rude content");

        assertEquals("polite content", processed);
        assertEquals(1, mockLLM.getCallCount());
    }

    /**
     * Verify summary and tags run together when only those are enabled.
     */
    @Test
    void hook_summaryAndTags_twoProcessors() {
        config.setEnabled("ProfanityRewriter", false);
        config.setEnabled("ToneMoodAnalyzer", false);
        mockLLM.setResponses("A concise summary.", "#tech #blog");

        BlogEntryProcessorHook hook = new BlogEntryProcessorHook(config, mockLLM);
        String processed = hook.processBeforeSave("Technical blog about testing.");

        assertTrue(processed.contains("Quick Summary:"));
        assertTrue(processed.contains("Tags:"));
        assertEquals(2, mockLLM.getCallCount());
    }

    /**
     * Verify mood analysis defaults to Informative for unknown mood.
     */
    @Test
    void hook_onlyMood_unknownMoodDefaultsToInformative() {
        config.setEnabled("ProfanityRewriter", false);
        config.setEnabled("BlogSummarizer", false);
        config.setEnabled("SmartTagGenerator", false);
        mockLLM.setResponse("Mysterious");

        BlogEntryProcessorHook hook = new BlogEntryProcessorHook(config, mockLLM);
        String processed = hook.processBeforeSave("Some blog content.");

        assertTrue(processed.contains("Post Mood:"));
        assertTrue(processed.contains("Informative")); // default fallback
    }
}
