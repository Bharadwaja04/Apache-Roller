package org.apache.roller.weblogger.pipeline;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Integration hook called before saving a blog entry.
 * Processes raw blog content through the LLM pipeline
 * so the processed version is stored in DB and shown on UI.
 * 
 * All settings (which processors are enabled, API key, model)
 * are read from Roller's admin Global Configuration page.
 * Supports any OpenAI-compatible LLM API (Groq, OpenAI, Anthropic, etc.)
 */
public class BlogEntryProcessorHook {

    private static final Log LOG = LogFactory.getLog(BlogEntryProcessorHook.class);

    // Default LLM API endpoint (OpenAI-compatible). Works with Groq, OpenAI, etc.
    private static final String DEFAULT_LLM_API_URL = "https://api.groq.com/openai/v1/chat/completions";

    private final ContentProcessingPipeline pipeline;
    private final ProcessorConfig config;
    private final boolean checkApiKey;

    /**
     * Production constructor — reads API key and model from admin settings.
     */
    public BlogEntryProcessorHook() {
        this.config = new ProcessorConfig();
        this.checkApiKey = true;
        String apiKey = config.getLLMApiKey();
        String model = config.getLLMModel();
        LLMService llmService = new LLMClient(DEFAULT_LLM_API_URL, apiKey, model);
        this.pipeline = new ContentProcessingPipeline(config, llmService);
    }

    /**
     * Test constructor — accepts mock config and LLM service.
     * Does NOT check for API key (LLM service is injected directly).
     */
    public BlogEntryProcessorHook(ProcessorConfig config, LLMService llmService) {
        this.config = config;
        this.checkApiKey = false;
        this.pipeline = new ContentProcessingPipeline(config, llmService);
    }

    /**
     * Process blog content through pipeline before saving.
     * The processed content will be stored in DB and shown on UI.
     * 
     * Checks if admin has enabled the pipeline globally before running.
     */
    public String processBeforeSave(String rawContent) {
        try {
            // Check if admin has enabled the pipeline
            if (!config.isPipelineEnabled()) {
                LOG.info("Content processing pipeline disabled by admin. Skipping.");
                return rawContent;
            }

            // Only check API key for production (not when LLM service is injected for tests)
            if (checkApiKey) {
                String apiKey = config.getLLMApiKey();
                if (apiKey == null || apiKey.isEmpty()) {
                    LOG.warn("No LLM API key configured in admin settings or environment. Skipping pipeline.");
                    return rawContent;
                }
            }

            LOG.info("Starting content processing pipeline.");
            String processed = pipeline.process(rawContent);
            LOG.info("Content processing pipeline completed.");
            return processed;
        } catch (Exception e) {
            LOG.error("Pipeline failed. Returning original content.", e);
            return rawContent;
        }
    }
}