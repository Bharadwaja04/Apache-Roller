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
package org.apache.roller.weblogger.business.search.mapper;

import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.TopFieldDocs;
import org.apache.roller.weblogger.business.search.domain.SearchResult;

/**
 * Maps Lucene search results to domain SearchResult objects.
 * Follows Single Responsibility Principle - only handles result mapping.
 * Isolates Lucene type exposure from the rest of the system.
 */
public class SearchResultMapper {

    /**
     * Maps Lucene TopFieldDocs to domain SearchResult.
     *
     * @param topFieldDocs the Lucene search results
     * @param searcher     the index searcher (kept for backward compatibility)
     * @return the mapped search result
     */
    public SearchResult map(TopFieldDocs topFieldDocs, IndexSearcher searcher) {
        if (topFieldDocs == null) {
            return SearchResult.builder()
                    .totalHits(0)
                    .searcher(searcher)
                    .build();
        }

        return SearchResult.builder()
                .hits(topFieldDocs.scoreDocs)
                .totalHits(topFieldDocs.totalHits.value)
                .searcher(searcher)
                .build();
    }
}
