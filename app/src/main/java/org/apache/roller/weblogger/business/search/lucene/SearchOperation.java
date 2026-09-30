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

import java.io.IOException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.SortField;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopFieldDocs;
import org.apache.roller.weblogger.WebloggerException;
import org.apache.roller.weblogger.business.URLStrategy;
import org.apache.roller.weblogger.business.search.SearchResultList;
import org.apache.roller.weblogger.config.WebloggerRuntimeConfig;

/**
 * An operation that searches the index.
 */
public class SearchOperation extends ReadFromIndexOperation implements SearchCommand {

    private static Log logger = LogFactory.getLog(SearchOperation.class);

    private static final String[] SEARCH_FIELDS = new String[] {
        FieldConstants.CONTENT,
        FieldConstants.TITLE,
        FieldConstants.C_CONTENT
    };

    private static final Sort SORTER = new Sort(new SortField(
            FieldConstants.PUBLISHED, SortField.Type.STRING, true));

    private IndexSearcher searcher;
    private TopFieldDocs searchresults;
    private SearchResultList finalResults;

    private final String term;
    private final String weblogHandle;
    private final String category;
    private final String locale;
    private final int pageNum;
    private final int entryCount;
    private final URLStrategy urlStrategy;
    private String parseError;

    public SearchOperation(IndexContext context, String term, String weblogHandle, String category, String locale, int pageNum, int entryCount, URLStrategy urlStrategy) {
        super(context);
        this.term = term;
        this.weblogHandle = weblogHandle;
        this.category = category;
        this.locale = locale;
        this.pageNum = pageNum;
        this.entryCount = entryCount;
        this.urlStrategy = urlStrategy;
    }

    @Override
    public void doRun() {
        try {
            executeWithReader(searcher -> {
                final int docLimit = 500;
                searchresults = null;
                this.searcher = searcher;

                MultiFieldQueryParser multiParser = new MultiFieldQueryParser(
                        SEARCH_FIELDS, context.getAnalyzer());

                multiParser.setDefaultOperator(MultiFieldQueryParser.Operator.AND);

                Query query = multiParser.parse(term);

                boolean weblogSpecific = !WebloggerRuntimeConfig.isSiteWideWeblog(weblogHandle);
                if (weblogSpecific) {
                    Term handleTerm = IndexUtil.getTerm(FieldConstants.WEBSITE_HANDLE, weblogHandle, context.getAnalyzer());
                    if (handleTerm != null) {
                        query = new BooleanQuery.Builder()
                            .add(query, BooleanClause.Occur.MUST)
                            .add(new TermQuery(handleTerm), BooleanClause.Occur.MUST)
                            .build();
                    }
                }

                if (category != null) {
                    Term catTerm = new Term(FieldConstants.CATEGORY, category.toLowerCase());
                    query = new BooleanQuery.Builder()
                        .add(query, BooleanClause.Occur.MUST)
                        .add(new TermQuery(catTerm), BooleanClause.Occur.MUST)
                        .build();
                }

                Term localeTerm = IndexUtil.getTerm(FieldConstants.LOCALE, locale, context.getAnalyzer());
                if (localeTerm != null) {
                    query = new BooleanQuery.Builder()
                        .add(query, BooleanClause.Occur.MUST)
                        .add(new TermQuery(localeTerm), BooleanClause.Occur.MUST)
                        .build();
                }

                searchresults = searcher.search(query, docLimit, SORTER);

                if (searchresults != null) {
                    finalResults = LuceneSearchResultConverter.getInstance().convertHitsToEntryList(
                            searchresults.scoreDocs,
                            this,
                            pageNum,
                            entryCount,
                            weblogHandle,
                            (weblogHandle != null),
                            urlStrategy);
                }
                return null;
            });

        } catch (WebloggerException e) {
            logger.error("Error during search operation", e);
            parseError = e.getMessage();
        } catch (Exception e) {
            logger.error("General error during search operation", e);
            parseError = e.getMessage();
        }
    }

    public IndexSearcher getSearcher() {
        return searcher;
    }

    @Override
    public SearchResultList getResults() {
        return finalResults;
    }

    @Override
    public int getResultsCount() {
        if (searchresults == null) {
            return -1;
        }
        return (int) searchresults.totalHits.value;
    }

    public String getParseError() {
        return parseError;
    }
}
