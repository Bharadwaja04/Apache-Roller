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
package org.apache.roller.weblogger.business.search.executor;

import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.SortField;
import org.apache.lucene.search.TopFieldDocs;
import org.apache.roller.weblogger.business.search.domain.SearchCriteria;
import org.apache.roller.weblogger.business.search.domain.SearchResult;
import org.apache.roller.weblogger.business.search.lucene.FieldConstants;
import org.apache.roller.weblogger.business.search.lucene.LuceneIndexManager;
import org.apache.roller.weblogger.business.search.mapper.SearchResultMapper;
import org.apache.roller.weblogger.business.search.query.SearchQueryBuilder;

/**
 * Lucene-specific implementation of SearchExecutor.
 * Follows Single Responsibility Principle - only executes searches.
 * Depends on abstractions (SearchQueryBuilder, SearchResultMapper) following
 * DIP.
 */
public class LuceneSearchExecutor implements SearchExecutor {

    private static final Log logger = LogFactory.getLog(LuceneSearchExecutor.class);
    private static final int DOC_LIMIT = 500;
    private static final Sort SORTER = new Sort(new SortField(
            FieldConstants.PUBLISHED, SortField.Type.STRING, true));

    private final LuceneIndexManager indexManager;
    private final SearchQueryBuilder queryBuilder;
    private final SearchResultMapper resultMapper;

    /**
     * Constructor with dependency injection.
     */
    public LuceneSearchExecutor(LuceneIndexManager indexManager,
            SearchQueryBuilder queryBuilder,
            SearchResultMapper resultMapper) {
        this.indexManager = indexManager;
        this.queryBuilder = queryBuilder;
        this.resultMapper = resultMapper;
    }

    @Override
    public SearchResult execute(SearchCriteria criteria) {
        IndexSearcher searcher = null;

        try {
            // Get index reader and create searcher
            IndexReader reader = indexManager.getSharedIndexReader();
            searcher = new IndexSearcher(reader);

            // Build query using injected query builder
            Query query = queryBuilder.buildQuery(criteria);

            // Execute search
            TopFieldDocs topFieldDocs = searcher.search(query, DOC_LIMIT, SORTER);

            // Map results using injected mapper
            return resultMapper.map(topFieldDocs, searcher);

        } catch (IOException e) {
            logger.error("Error searching index", e);
            return SearchResult.builder()
                    .parseError(e.getMessage())
                    .build();

        } catch (ParseException e) {
            logger.warn("Parse error in search query", e);
            return SearchResult.builder()
                    .parseError(e.getMessage())
                    .build();
        }
        // Note: We don't close the reader since we didn't do any writing
    }
}
