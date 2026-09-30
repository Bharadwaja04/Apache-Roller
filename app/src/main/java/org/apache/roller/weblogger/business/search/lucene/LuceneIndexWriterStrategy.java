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
import org.apache.lucene.analysis.miscellaneous.LimitTokenCountAnalyzer;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.roller.weblogger.config.WebloggerConfig;

/**
 * Strategy class for managing Lucene IndexWriter.
 * Extracted from IndexOperation to follow Strategy pattern and SRP.
 */
public class LuceneIndexWriterStrategy {

    private static Log logger = LogFactory.getLog(LuceneIndexWriterStrategy.class);

    private IndexWriter writer;

    /**
     * Begin writing by creating a new IndexWriter.
     * 
     * @param context The index context providing the directory
     * @return The created IndexWriter, or null if error occurred
     */
    public IndexWriter beginWriting(IndexContext context) {
        try {
            LimitTokenCountAnalyzer analyzer = new LimitTokenCountAnalyzer(
                    context.getAnalyzer(),
                    WebloggerConfig.getIntProperty("lucene.analyzer.maxTokenCount"));

            IndexWriterConfig config = new IndexWriterConfig(analyzer);
            writer = new IndexWriter(context.getIndexDirectory(), config);
            return writer;

        } catch (IOException e) {
            logger.error("ERROR creating writer", e);
            return null;
        }
    }

    /**
     * End writing by closing the IndexWriter.
     */
    public void endWriting() {
        if (writer != null) {
            try {
                writer.close();
            } catch (IOException e) {
                logger.error("ERROR closing writer", e);
            } finally {
                writer = null;
            }
        }
    }
}
