/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  The ASF licenses this file to You
 * under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.  For additional information regarding
 * copyright in this work, please see the NOTICE file in the top level
 * directory of this distribution.
 */
package org.apache.roller.weblogger.business.search.lucene;

import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.weblogger.business.runnable.ThreadManager;

/**
 * Implementation of OperationQueue using a priority blocking queue.
 * Processes operations sequentially in a background thread based on priority.
 */
public class PriorityOperationQueue implements OperationQueue {

    private static final Log logger = LogFactory.getLog(PriorityOperationQueue.class);

    private final PriorityBlockingQueue<PrioritizedCommand> queue;
    private final ExecutorService executor;
    private final IndexContext context;
    private final ThreadManager threadManager;
    private volatile boolean running = true;

    public PriorityOperationQueue(ThreadManager threadManager, IndexContext context) {
        this.threadManager = threadManager;
        this.context = context;
        this.queue = new PriorityBlockingQueue<>();
        this.executor = Executors.newSingleThreadExecutor(r -> {
            Thread t = new Thread(r, "LuceneIndexProcessor");
            t.setDaemon(true);
            return t;
        });
        startProcessing();
    }

    @Override
    public void enqueue(IndexCommand command, OperationPriority priority) {
        if (!running) {
            logger.warn("Queue is shutting down, dropping command: " + command.getClass().getSimpleName());
            return;
        }
        queue.offer(new PrioritizedCommand(command, priority));
        logger.debug("Enqueued command: " + command.getClass().getSimpleName() + " with priority: " + priority);
    }

    private void startProcessing() {
        executor.submit(() -> {
            logger.info("Operation queue processor started");
            while (running || !queue.isEmpty()) {
                try {
                    PrioritizedCommand cmd = queue.take();
                    if (cmd != null) {
                        executeCommand(cmd.getCommand());
                    }
                } catch (InterruptedException e) {
                    logger.info("Operation queue processor interrupted");
                    Thread.currentThread().interrupt();
                    break;
                } catch (Exception e) {
                    logger.error("Error in operation queue processor", e);
                }
            }
            logger.info("Operation queue processor stopped");
        });
    }

    private void executeCommand(IndexCommand command) {
        try {
            logger.debug("Executing command: " + command.getClass().getSimpleName());
            // Execute directly in this background thread. 
            // Sequentiality is guaranteed by the single-thread executor.
            command.execute(context);
            logger.debug("Command completed: " + command.getClass().getSimpleName());
        } catch (Exception e) {
            logger.error("Error executing command: " + command.getClass().getSimpleName(), e);
        }
    }

    @Override
    public void shutdown() {
        logger.info("Shutting down PriorityOperationQueue");
        running = false;
        executor.shutdown();
    }

    /**
     * Internal wrapper for prioritized commands.
     */
    private static class PrioritizedCommand implements Comparable<PrioritizedCommand> {
        private final IndexCommand command;
        private final OperationPriority priority;
        private final long timestamp;

        public PrioritizedCommand(IndexCommand command, OperationPriority priority) {
            this.command = command;
            this.priority = priority;
            this.timestamp = System.nanoTime();
        }

        public IndexCommand getCommand() {
            return command;
        }

        @Override
        public int compareTo(PrioritizedCommand other) {
            int res = Integer.compare(this.priority.getValue(), other.priority.getValue());
            if (res == 0) {
                // Secondary sort by timestamp to maintain FIFO for same priority
                return Long.compare(this.timestamp, other.timestamp);
            }
            return res;
        }
    }
}
