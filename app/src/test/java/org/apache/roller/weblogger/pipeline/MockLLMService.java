package org.apache.roller.weblogger.pipeline;

import java.util.LinkedList;
import java.util.Queue;

/**
 * Mock LLM service for unit testing.
 * Returns pre-configured responses instead of calling a real API.
 */
public class MockLLMService implements LLMService {

    private final Queue<String> responses = new LinkedList<>();
    private boolean shouldFail = false;
    private int callCount = 0;

    /** Set a single response returned for every call. */
    public void setResponse(String response) {
        responses.clear();
        responses.add(response);
    }

    /** Set multiple ordered responses (one per call). */
    public void setResponses(String... orderedResponses) {
        responses.clear();
        for (String r : orderedResponses) {
            responses.add(r);
        }
    }

    /** If true, sendPrompt() throws RuntimeException. */
    public void setShouldFail(boolean shouldFail) {
        this.shouldFail = shouldFail;
    }

    public int getCallCount() {
        return callCount;
    }

    @Override
    public String sendPrompt(String prompt) {
        callCount++;
        if (shouldFail) {
            throw new RuntimeException("Mock LLM failure");
        }
        if (responses.isEmpty()) {
            return "mock-response";
        }
        // Peek if only one response left (reuse it), otherwise poll
        if (responses.size() == 1) {
            return responses.peek();
        }
        return responses.poll();
    }
}
