/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  The ASF licenses this file to You
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

import java.io.IOException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.search.IndexSearcher;
import org.apache.roller.weblogger.WebloggerException;

/**
 * An operation that reads from the index.
 * Updated to use IndexContext for dependency injection and support generic callbacks.
 * 
 * @author aim4min
 */
public abstract class ReadFromIndexOperation extends AbstractIndexOperation {
    
    public ReadFromIndexOperation(IndexContext context) {
        super(context);
    }
    
    private static Log logger = LogFactory.getFactory().getInstance(
            ReadFromIndexOperation.class);
    
    @Override
    public final void run() {
        try {
            executeWithReader(searcher -> {
                doRun();
                return null;
            });
        } catch (Exception e) {
            logger.error("Error executing read operation", e);
        }
    }
    
    /**
     * Executes a callback using the index searcher within a read lock.
     * 
     * @param callback The callback to execute
     * @param <T> The return type of the callback
     * @return The result of the callback
     * @throws WebloggerException If an error occurs
     */
    public <T> T executeWithReader(IndexSearcherCallback<T> callback) throws WebloggerException {
        try {
            context.getReadWriteLock().readLock().lock();
            IndexSearcher searcher = new IndexSearcher(context.getSharedIndexReader());
            return callback.doWithSearcher(searcher);
        } catch (IOException e) {
            throw new WebloggerException("Error creating index searcher", e);
        } catch (Exception e) {
            throw new WebloggerException("Error during index read operation", e);
        } finally {
            context.getReadWriteLock().readLock().unlock();
        }
    }
}
