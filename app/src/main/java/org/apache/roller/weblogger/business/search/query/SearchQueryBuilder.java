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

import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.search.Query;
import org.apache.roller.weblogger.business.search.domain.SearchCriteria;

/**
 * Interface for building search queries.
 * Follows Dependency Inversion Principle - depends on abstraction.
 * Follows Open/Closed Principle - can add new implementations without changing
 * existing code.
 */
public interface SearchQueryBuilder {

    /**
     * Builds a search query from the given criteria.
     *
     * @param criteria the search criteria
     * @return the built query
     * @throws ParseException if query parsing fails
     */
    Query buildQuery(SearchCriteria criteria) throws ParseException;
}
