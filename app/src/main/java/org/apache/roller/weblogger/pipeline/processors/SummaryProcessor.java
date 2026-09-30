package org.apache.roller.weblogger.pipeline.processors;

import org.apache.roller.weblogger.pipeline.AbstractContentProcessor;
import org.apache.roller.weblogger.pipeline.LLMService;

/**
 * Generates a concise summary using LLM and prepends it to blog content.
 */
public class SummaryProcessor extends AbstractContentProcessor {

    private static final String PROMPT_TEMPLATE =
        "Summarize the following blog post in 40-50 words. "
      + "Return only the summary.\n\nContent:\n%s";

    private static final String SUMMARY_HEADER = "Quick Summary:\n%s\n\n"
      + "--------------------------------------------------\n\n";

    public SummaryProcessor(LLMService llmService) {
        super(llmService);
    }

    @Override
    public String getName() {
        return "BlogSummarizer";
    }

    @Override
    protected String applyTransformation(String content) {
        String prompt = String.format(PROMPT_TEMPLATE, content);
        String summary = llmService.sendPrompt(prompt);

        if (summary.isEmpty()) {
            return content;
        }

        return String.format(SUMMARY_HEADER, summary) + content;
    }
}