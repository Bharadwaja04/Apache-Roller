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

/* Created on Jul 16, 2003 */

package org.apache.roller.weblogger.business.search.lucene;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexWriter;
import org.apache.roller.weblogger.pojos.WeblogEntry;

/**
 * This is the base class for all index operation. These operations include:<br>
 * SearchOperation<br>
 * AddWeblogOperation<br>
 * RemoveWeblogOperation<br>
 * RebuildUserIndexOperation
 * 
 * Refactored to use IndexContext instead of LuceneIndexManager to eliminate
 * circular dependencies and follow SOLID principles.
 * 
 * @author Mindaugas Idzelis (min@idzelis.com)
 */
public abstract class AbstractIndexOperation implements IndexOperation, Runnable {

    private static Log logger = LogFactory.getFactory().getInstance(
            AbstractIndexOperation.class);

    // ~ Instance fields
    // ========================================================
    protected IndexContext context;
    private LuceneIndexWriterStrategy writerStrategy = new LuceneIndexWriterStrategy();

    // ~ Constructors
    // ===========================================================
    public AbstractIndexOperation(IndexContext context) {
        this.context = context;
    }

    // ~ Methods
    // ================================================================
    protected Document getDocument(WeblogEntry data) {
        return LuceneDocumentBuilder.getInstance().buildDocument(data);
    }

    /**
     * Begin writing.
     * 
     * @return the index writer
     */
    protected IndexWriter beginWriting() {
        return writerStrategy.beginWriting(context);
    }

    /**
     * End writing.
     */
    protected void endWriting() {
        writerStrategy.endWriting();
    }

    /**
     * @see java.lang.Runnable#run()
     */
    @Override
    public void run() {
        doRun();
    }
    
    /**
     * Execute method from IndexCommand interface.
     * Default implementation delegates to run().
     */
    @Override
    public void execute(IndexContext ctx) throws org.apache.roller.weblogger.WebloggerException {
        run();
    }

    protected abstract void doRun();
}
