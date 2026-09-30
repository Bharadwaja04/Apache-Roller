package org.apache.roller.weblogger.pipeline;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.weblogger.pipeline.processors.MoodAnalyzerProcessor;
import org.apache.roller.weblogger.pipeline.processors.ProfanityProcessor;
import org.apache.roller.weblogger.pipeline.processors.SummaryProcessor;
import org.apache.roller.weblogger.pipeline.processors.TagGeneratorProcessor;

import java.util.ArrayList;
import java.util.List;

/**
 * Orchestrates the content processing pipeline.
 * Dynamically builds the chain based on admin configuration.
 */
public class ContentProcessingPipeline {

    private static final Log LOG = LogFactory.getLog(ContentProcessingPipeline.class);

    private final ProcessorConfig config;
    private final LLMService llmService;

    public ContentProcessingPipeline(ProcessorConfig config, LLMService llmService) {
        this.config = config;
        this.llmService = llmService;
    }

    public String process(String content) {
        if (content == null || content.trim().isEmpty()) {
            LOG.warn("Empty content submitted to pipeline. Skipping.");
            return content;
        }

        List<ContentProcessor> enabledProcessors = buildProcessorList();

        if (enabledProcessors.isEmpty()) {
            LOG.info("No processors enabled. Returning original content.");
            return content;
        }

        // Build the chain
        for (int i = 0; i < enabledProcessors.size() - 1; i++) {
            enabledProcessors.get(i).setNext(enabledProcessors.get(i + 1));
        }

        LOG.info("Processing blog through " + enabledProcessors.size() + " processors.");

        return enabledProcessors.get(0).process(content);
    }

    private List<ContentProcessor> buildProcessorList() {
        List<ContentProcessor> processors = new ArrayList<>();

        if (config.isEnabled("ProfanityRewriter")) {
            processors.add(new ProfanityProcessor(llmService));
        }
        if (config.isEnabled("BlogSummarizer")) {
            processors.add(new SummaryProcessor(llmService));
        }
        if (config.isEnabled("SmartTagGenerator")) {
            processors.add(new TagGeneratorProcessor(llmService));
        }
        if (config.isEnabled("ToneMoodAnalyzer")) {
            processors.add(new MoodAnalyzerProcessor(llmService));
        }

        return processors;
    }
}