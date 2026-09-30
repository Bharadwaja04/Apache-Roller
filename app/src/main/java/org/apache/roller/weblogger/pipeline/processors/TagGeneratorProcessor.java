package org.apache.roller.weblogger.pipeline.processors;

import org.apache.roller.weblogger.pipeline.AbstractContentProcessor;
import org.apache.roller.weblogger.pipeline.LLMService;

/**
 * Generates relevant hashtags using LLM and appends them to the blog.
 */
public class TagGeneratorProcessor extends AbstractContentProcessor {

    private static final String PROMPT_TEMPLATE =
        "Generate 3 to 5 relevant tags for the following blog post. "
      + "Return them as hashtags separated by spaces. "
      + "Return only the hashtags.\n\nContent:\n%s";

    private static final String TAGS_FOOTER = "\n\nTags:\n%s";

    public TagGeneratorProcessor(LLMService llmService) {
        super(llmService);
    }

    @Override
    public String getName() {
        return "SmartTagGenerator";
    }

    @Override
    protected String applyTransformation(String content) {
        String prompt = String.format(PROMPT_TEMPLATE, content);
        String tags = llmService.sendPrompt(prompt);

        if (tags.isEmpty()) {
            return content;
        }

        return content + String.format(TAGS_FOOTER, tags.trim());
    }
}