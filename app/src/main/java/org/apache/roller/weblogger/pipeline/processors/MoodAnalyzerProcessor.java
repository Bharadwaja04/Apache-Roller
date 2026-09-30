package org.apache.roller.weblogger.pipeline.processors;

import org.apache.roller.weblogger.pipeline.AbstractContentProcessor;
import org.apache.roller.weblogger.pipeline.LLMService;

import java.util.HashMap;
import java.util.Map;

/**
 * Analyzes the tone/mood of blog content using LLM
 * and prepends a mood indicator with emoji.
 */
public class MoodAnalyzerProcessor extends AbstractContentProcessor {

    private static final String PROMPT_TEMPLATE =
        "Analyze the tone of the following blog post. "
      + "Choose one of the following categories: "
      + "Informative, Positive, Critical, Humorous. "
      + "Return only the category name.\n\nContent:\n%s";

    private static final Map<String, String> MOOD_EMOJIS = new HashMap<>();

    static {
        MOOD_EMOJIS.put("informative", "\uD83D\uDCDA Informative");
        MOOD_EMOJIS.put("positive", "\uD83D\uDE0A Positive");
        MOOD_EMOJIS.put("critical", "\u26A0\uFE0F Critical");
        MOOD_EMOJIS.put("humorous", "\uD83D\uDE02 Humorous");
    }

    private static final String MOOD_HEADER = "Post Mood: %s\n\n";

    public MoodAnalyzerProcessor(LLMService llmService) {
        super(llmService);
    }

    @Override
    public String getName() {
        return "ToneMoodAnalyzer";
    }

    @Override
    protected String applyTransformation(String content) {
        String prompt = String.format(PROMPT_TEMPLATE, content);
        String mood = llmService.sendPrompt(prompt).trim().toLowerCase();

        String moodLabel = MOOD_EMOJIS.getOrDefault(mood, "\uD83D\uDCDA Informative");

        return String.format(MOOD_HEADER, moodLabel) + content;
    }
}