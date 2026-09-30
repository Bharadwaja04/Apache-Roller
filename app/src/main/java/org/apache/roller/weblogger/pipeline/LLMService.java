package org.apache.roller.weblogger.pipeline;

/**
 * Interface for LLM communication.
 * Allows swapping LLM providers (OpenAI, Ollama, Gemini, etc.)
 * without changing any processor code.
 */
public interface LLMService {

    String sendPrompt(String prompt);
}