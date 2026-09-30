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

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import org.apache.commons.beanutils.ConstructorUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.miscellaneous.LimitTokenCountAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.roller.weblogger.WebloggerException;
import org.apache.roller.weblogger.business.InitializationException;
import org.apache.roller.weblogger.business.URLStrategy;
import org.apache.roller.weblogger.business.Weblogger;
import org.apache.roller.weblogger.business.search.IndexManager;
import org.apache.roller.weblogger.business.search.SearchResultList;
import org.apache.roller.weblogger.config.WebloggerConfig;
import org.apache.roller.weblogger.pojos.Weblog;
import org.apache.roller.weblogger.pojos.WeblogEntry;

/**
 * Lucene implementation of IndexManager.
 * 
 * REFACTORED:
 * - Implements IndexContext to provide dependencies to operations (ISP/DIP)
 * - Uses OperationFactory for creating commands (Factory Pattern)
 * - Uses OperationQueue for asynchronous execution (Operation Queue Pattern)
 */
@com.google.inject.Singleton
public class LuceneIndexManager implements IndexManager, IndexContext {

    private final Weblogger roller;
    private final static Log logger = LogFactory.getFactory().getInstance(LuceneIndexManager.class);

    private boolean searchEnabled = true;
    private final String indexDir;
    private final File indexConsistencyMarker;
    private boolean inconsistentAtStartup = false;

    private final ReadWriteLock rwl = new ReentrantReadWriteLock();
    private IndexReader reader;

    private OperationQueue operationQueue;
    private OperationFactory operationFactory;

    @com.google.inject.Inject
    protected LuceneIndexManager(Weblogger roller) {
        this.roller = roller;

        String enabled = WebloggerConfig.getProperty("search.enabled");
        if ("false".equalsIgnoreCase(enabled)) {
            this.searchEnabled = false;
        }

        String searchIndexDir = WebloggerConfig.getProperty("search.index.dir");
        this.indexDir = searchIndexDir.replace('/', File.separatorChar);

        logger.info("search enabled: " + this.searchEnabled);
        logger.info("index dir: " + this.indexDir);

        String test = indexDir + File.separator + ".index-inconsistent";
        indexConsistencyMarker = new File(test);
    }

    @Override
    public void initialize() throws InitializationException {

        // Initialize refactored components here to avoid circular dependency issues in constructor
        this.operationQueue = new PriorityOperationQueue(roller.getThreadManager(), this);
        this.operationFactory = new DefaultOperationFactory(roller, this);

        if (this.searchEnabled) {
            if (indexConsistencyMarker.exists()) {
                logger.debug("Index inconsistent: marker exists");
                inconsistentAtStartup = true;
                deleteIndex();
            } else {
                try {
                    File makeIndexDir = new File(indexDir);
                    if (!makeIndexDir.exists()) {
                        makeIndexDir.mkdirs();
                        inconsistentAtStartup = true;
                        logger.debug("Index inconsistent: new");
                    }
                    indexConsistencyMarker.createNewFile();
                } catch (IOException e) {
                    logger.error(e);
                }
            }

            if (indexExists()) {
                try {
                    synchronized (this) {
                        reader = DirectoryReader.open(getIndexDirectory());
                    }
                } catch (IOException | IllegalArgumentException ex) {
                    logger.warn("Failed to open search index, scheduling rebuild.", ex);
                    inconsistentAtStartup = true;
                    deleteIndex();
                }
            } else {
                logger.debug("Creating index");
                inconsistentAtStartup = true;
                deleteIndex();
                createIndex(getIndexDirectory());
            }

            if (inconsistentAtStartup) {
                logger.info("Index was inconsistent. Rebuilding index in the background...");
                try {
                    rebuildWeblogIndex();
                } catch (WebloggerException ex) {
                    logger.error("ERROR: scheduling re-index operation", ex);
                }
            } else {
                logger.info("Index initialized and ready for use.");
            }
        }
    }

    @Override
    public void rebuildWeblogIndex() throws WebloggerException {
        operationQueue.enqueue(operationFactory.createRebuildWeblogOperation(null), OperationPriority.LOW);
    }

    @Override
    public void rebuildWeblogIndex(Weblog website) throws WebloggerException {
        operationQueue.enqueue(operationFactory.createRebuildWeblogOperation(website), OperationPriority.LOW);
    }

    @Override
    public void removeWeblogIndex(Weblog website) throws WebloggerException {
        operationQueue.enqueue(operationFactory.createRemoveWeblogOperation(website), OperationPriority.LOW);
    }

    @Override
    public void addEntryIndexOperation(WeblogEntry entry) throws WebloggerException {
        operationQueue.enqueue(operationFactory.createAddEntryOperation(entry), OperationPriority.NORMAL);
    }

    @Override
    public void addEntryReIndexOperation(WeblogEntry entry) throws WebloggerException {
        operationQueue.enqueue(operationFactory.createUpdateEntryOperation(entry), OperationPriority.NORMAL);
    }

    @Override
    public void removeEntryIndexOperation(WeblogEntry entry) throws WebloggerException {
        operationQueue.enqueue(operationFactory.createRemoveEntryOperation(entry), OperationPriority.NORMAL);
    }

    @Override
    public SearchResultList search(
            String term,
            String weblogHandle,
            String category,
            String locale,
            int pageNum,
            int entryCount,
            URLStrategy urlStrategy) throws WebloggerException {

        SearchCommand search = operationFactory.createSearchOperation(
                term, weblogHandle, category, locale, pageNum, entryCount, urlStrategy);

        // Searches are typically executed in the foreground to provide immediate results
        search.execute(this);

        if (search.getResultsCount() >= 0) {
            return search.getResults();
        }
        
        return null;
    }

    // IndexContext Implementation
    
    @Override
    public Directory getIndexDirectory() {
        try {
            return FSDirectory.open(Path.of(indexDir));
        } catch (IOException e) {
            logger.error("Problem accessing index directory", e);
        }
        return null;
    }

    @Override
    public ReadWriteLock getReadWriteLock() {
        return rwl;
    }

    @Override
    public synchronized IndexReader getSharedIndexReader() {
        if (reader == null) {
            try {
                reader = DirectoryReader.open(getIndexDirectory());
            } catch (IOException ex) {
                logger.error("Error opening DirectoryReader", ex);
                throw new RuntimeException(ex);
            }
        }
        return reader;
    }

    @Override
    public synchronized void resetSharedReader() {
        if (reader != null) {
            try {
                reader.close();
            } catch (IOException e) {
                logger.warn("Error closing index reader", e);
            }
            reader = null;
        }
    }

    @Override
    public Analyzer getAnalyzer() {
        return instantiateAnalyzer();
    }

    @Override
    public boolean isInconsistentAtStartup() {
        return inconsistentAtStartup;
    }

    private Analyzer instantiateAnalyzer() {
        final String className = WebloggerConfig.getProperty("lucene.analyzer.class");
        try {
            final Class<?> clazz = Class.forName(className);
            return (Analyzer) ConstructorUtils.invokeConstructor(clazz, null);
        } catch (final Exception e) {
            return new StandardAnalyzer();
        }
    }

    private boolean indexExists() {
        try {
            return DirectoryReader.indexExists(getIndexDirectory());
        } catch (IOException e) {
            return false;
        }
    }

    private void deleteIndex() {
        try (FSDirectory directory = FSDirectory.open(Path.of(indexDir))) {
            String[] files = directory.listAll();
            for (String file : files) {
                Files.delete(Path.of(indexDir, file));
            }
        } catch (IOException ex) {
            logger.error("Problem accessing index directory", ex);
        }
    }

    private void createIndex(Directory dir) {
        try {
            IndexWriterConfig config = new IndexWriterConfig(
                    new LimitTokenCountAnalyzer(getAnalyzer(), 128));
            try (IndexWriter writer = new IndexWriter(dir, config)) {
                // just open and close to create index
            }
        } catch (IOException e) {
            logger.error("Error creating index", e);
        }
    }

    @Override
    public void release() {
        // no-op
    }

    @Override
    public void shutdown() {
        indexConsistencyMarker.delete();
        resetSharedReader();
        if (operationQueue != null) {
            operationQueue.shutdown();
        }
    }
}
