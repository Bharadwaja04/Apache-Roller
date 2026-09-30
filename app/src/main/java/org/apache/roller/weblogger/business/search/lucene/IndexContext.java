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

import java.util.concurrent.locks.ReadWriteLock;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.store.Directory;

/**
 * Provides context and dependencies for index operations.
 * This interface decouples operations from the concrete manager implementation,
 * following the Dependency Inversion Principle (DIP) and Interface Segregation Principle (ISP).
 * 
 * Operations depend on this abstraction rather than the concrete LuceneIndexManager,
 * eliminating circular dependencies and improving testability.
 * 
 * @author Refactoring Team (SOLID Principles)
 */
public interface IndexContext {
    
    /**
     * Get the directory that is used by the lucene index.
     * 
     * @return Directory The directory containing the index, or null if error.
     */
    Directory getIndexDirectory();
    
    /**
     * Get the read-write lock used for coordinating access to the index.
     * 
     * @return ReadWriteLock for synchronized access
     */
    ReadWriteLock getReadWriteLock();
    
    /**
     * Get a shared index reader for reading from the index.
     * 
     * @return IndexReader shared across operations
     */
    IndexReader getSharedIndexReader();
    
    /**
     * Reset the shared reader, typically called after write operations.
     */
    void resetSharedReader();
    
    /**
     * Get the analyzer used for tokenizing text.
     * 
     * @return Analyzer to be used in manipulating the index
     */
    Analyzer getAnalyzer();
}
