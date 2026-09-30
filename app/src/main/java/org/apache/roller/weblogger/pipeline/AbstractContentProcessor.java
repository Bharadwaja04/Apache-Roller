package org.apache.roller.weblogger.pipeline;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Base class for all content processors.
 * Handles chaining, error handling, and logging.
 * Subclasses only implement applyTransformation().
 */
public abstract class AbstractContentProcessor implements ContentProcessor {

    private static final Log LOG = LogFactory.getLog(AbstractContentProcessor.class);

    private ContentProcessor nextProcessor;
    protected final LLMService llmService;

    protected AbstractContentProcessor(LLMService llmService) {
        this.llmService = llmService;
    }

    @Override
    public ContentProcessor setNext(ContentProcessor next) {
        this.nextProcessor = next;
        return next;
    }

    @Override
    public String process(String content) {
        String processed;
        try {
            processed = applyTransformation(content);
            LOG.info(getName() + " completed successfully.");
        } catch (Exception e) {
            LOG.error(getName() + " failed, passing content unchanged.", e);
            processed = content;
        }

        if (nextProcessor != null) {
            return nextProcessor.process(processed);
        }
        return processed;
    }

    /**
     * Subclasses implement their specific LLM-based transformation here.
     */
    protected abstract String applyTransformation(String content);
}