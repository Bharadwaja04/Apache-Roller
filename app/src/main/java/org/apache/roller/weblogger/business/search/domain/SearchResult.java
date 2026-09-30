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
package org.apache.roller.weblogger.business.search.domain;

import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.ScoreDoc;

/**
 * Immutable value object representing search results.
 * Follows Single Responsibility Principle - only holds search result data.
 * Decoupled from Lucene internals.
 */
public class SearchResult {

    private final ScoreDoc[] hits;
    private final long totalHits;
    private final String parseError;
    private final IndexSearcher searcher; // Kept for backward compatibility

    private SearchResult(Builder builder) {
        this.hits = builder.hits;
        this.totalHits = builder.totalHits;
        this.parseError = builder.parseError;
        this.searcher = builder.searcher;
    }

    public ScoreDoc[] getHits() {
        return hits;
    }

    public long getTotalHits() {
        return totalHits;
    }

    public String getParseError() {
        return parseError;
    }

    public boolean hasError() {
        return parseError != null;
    }

    /**
     * Gets the IndexSearcher used for this search.
     * This is kept for backward compatibility to allow document retrieval.
     * 
     * @return the index searcher
     */
    public IndexSearcher getSearcher() {
        return searcher;
    }

    public static Builder builder() {
        return new Builder();
    }

    /**
     * Builder for SearchResult.
     */
    public static class Builder {
        private ScoreDoc[] hits;
        private long totalHits;
        private String parseError;
        private IndexSearcher searcher;

        public Builder hits(ScoreDoc[] hits) {
            this.hits = hits;
            return this;
        }

        public Builder totalHits(long totalHits) {
            this.totalHits = totalHits;
            return this;
        }

        public Builder parseError(String parseError) {
            this.parseError = parseError;
            return this;
        }

        public Builder searcher(IndexSearcher searcher) {
            this.searcher = searcher;
            return this;
        }

        public SearchResult build() {
            return new SearchResult(this);
        }
    }
}
