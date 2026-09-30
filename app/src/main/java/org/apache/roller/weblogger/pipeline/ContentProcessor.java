package org.apache.roller.weblogger.pipeline;

/**
 * Interface for content processing steps in the Chain of Responsibility.
 * Each processor transforms blog content and forwards it to the next.
 */
public interface ContentProcessor {

    String process(String content);

    ContentProcessor setNext(ContentProcessor next);

    String getName();
}