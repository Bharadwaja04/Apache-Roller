package org.apache.roller.weblogger.pipeline.processors;

import org.apache.roller.weblogger.pipeline.AbstractContentProcessor;
import org.apache.roller.weblogger.pipeline.LLMService;

/**
 * Rewrites profane or offensive language using LLM.
 * Replaces offensive words with humorous/polite alternatives.
 */
public class ProfanityProcessor extends AbstractContentProcessor {

    private static final String PROMPT_TEMPLATE =
        "Rewrite the following blog content by replacing offensive or abusive words "
      + "with humorous or polite alternatives. Maintain the meaning and tone "
      + "of the sentence. Return only the rewritten content.\n\nContent:\n%s";

    public ProfanityProcessor(LLMService llmService) {
        super(llmService);
    }

    @Override
    public String getName() {
        return "ProfanityRewriter";
    }

    @Override
    protected String applyTransformation(String content) {
        String prompt = String.format(PROMPT_TEMPLATE, content);
        String result = llmService.sendPrompt(prompt);
        return result.isEmpty() ? content : result;
    }
}