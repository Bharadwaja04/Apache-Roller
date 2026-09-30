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
package org.apache.roller.weblogger.business.search.query;

import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.apache.roller.weblogger.business.search.domain.SearchCriteria;
import org.apache.roller.weblogger.business.search.lucene.FieldConstants;
import org.apache.roller.weblogger.business.search.lucene.IndexUtil;
import org.apache.roller.weblogger.business.search.lucene.LuceneIndexManager;

/**
 * Lucene-specific implementation of SearchQueryBuilder.
 * Follows Single Responsibility Principle - only builds Lucene queries.
 * Encapsulates all query construction logic.
 */
public class LuceneSearchQueryBuilder implements SearchQueryBuilder {

    private static final String[] SEARCH_FIELDS = new String[] {
            FieldConstants.CONTENT,
            FieldConstants.TITLE,
            FieldConstants.C_CONTENT
    };

    private final org.apache.lucene.analysis.Analyzer analyzer;

    public LuceneSearchQueryBuilder(org.apache.lucene.analysis.Analyzer analyzer) {
        this.analyzer = analyzer;
    }

    @Override
    public Query buildQuery(SearchCriteria criteria) throws ParseException {
        // Build base multi-field query
        MultiFieldQueryParser multiParser = new MultiFieldQueryParser(
                SEARCH_FIELDS, analyzer);

        // Make it an AND by default
        multiParser.setDefaultOperator(MultiFieldQueryParser.Operator.AND);

        // Parse the search term
        Query query = multiParser.parse(criteria.getTerm());

        // Apply weblog handle filter
        query = applyWeblogHandleFilter(query, criteria.getWeblogHandle());

        // Apply category filter
        query = applyCategoryFilter(query, criteria.getCategory());

        // Apply locale filter
        query = applyLocaleFilter(query, criteria.getLocale());

        return query;
    }

    /**
     * Applies weblog handle filter to the query.
     */
    private Query applyWeblogHandleFilter(Query query, String weblogHandle) {
        Term handleTerm = IndexUtil.getTerm(FieldConstants.WEBSITE_HANDLE, weblogHandle, analyzer);
        if (handleTerm != null) {
            return new BooleanQuery.Builder()
                    .add(query, BooleanClause.Occur.MUST)
                    .add(new TermQuery(handleTerm), BooleanClause.Occur.MUST)
                    .build();
        }
        return query;
    }

    /**
     * Applies category filter to the query.
     */
    private Query applyCategoryFilter(Query query, String category) {
        if (category != null) {
            Term catTerm = new Term(FieldConstants.CATEGORY, category.toLowerCase());
            return new BooleanQuery.Builder()
                    .add(query, BooleanClause.Occur.MUST)
                    .add(new TermQuery(catTerm), BooleanClause.Occur.MUST)
                    .build();
        }
        return query;
    }

    /**
     * Applies locale filter to the query.
     */
    private Query applyLocaleFilter(Query query, String locale) {
        Term localeTerm = IndexUtil.getTerm(FieldConstants.LOCALE, locale, analyzer);
        if (localeTerm != null) {
            return new BooleanQuery.Builder()
                    .add(query, BooleanClause.Occur.MUST)
                    .add(new TermQuery(localeTerm), BooleanClause.Occur.MUST)
                    .build();
        }
        return query;
    }
}
