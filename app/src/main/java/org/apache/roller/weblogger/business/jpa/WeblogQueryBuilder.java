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
 * limitations under the License.
 */

package org.apache.roller.weblogger.business.jpa;

import jakarta.persistence.TypedQuery;
import org.apache.roller.weblogger.WebloggerException;
import org.apache.roller.weblogger.pojos.Weblog;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Builds dynamic JPA queries for Weblog entities.
 * Extracted from JPAWeblogManagerImpl to address Insufficient Modularization.
 * 
 * <p>This class encapsulates the complex query building logic:</p>
 * <ul>
 *   <li>Dynamic WHERE clause construction</li>
 *   <li>Parameter management</li>
 *   <li>Pagination support</li>
 * </ul>
 */
public class WeblogQueryBuilder {

    private final JPAPersistenceStrategy strategy;

    public WeblogQueryBuilder(JPAPersistenceStrategy strategy) {
        this.strategy = strategy;
    }

    /**
     * Build and execute a query for weblogs with optional filters.
     * 
     * @param enabled   Filter by enabled status (null for all)
     * @param active    Filter by active status (null for all)
     * @param startDate Filter by creation date after (null for all)
     * @param endDate   Filter by creation date before (null for all)
     * @param offset    Pagination offset
     * @param length    Maximum results (-1 for all)
     * @return List of matching weblogs
     * @throws WebloggerException if query fails
     */
    public List<Weblog> getWeblogs(
            Boolean enabled,
            Boolean active,
            Date startDate,
            Date endDate,
            int offset,
            int length) throws WebloggerException {

        QueryComponents components = buildQueryComponents(enabled, active, startDate, endDate);
        
        String queryString = "SELECT w FROM Weblog w WHERE " 
                + components.whereClause 
                + " ORDER BY w.dateCreated DESC";

        TypedQuery<Weblog> query = strategy.getDynamicQuery(queryString, Weblog.class);
        
        applyPagination(query, offset, length);
        applyParameters(query, components.params);

        return query.getResultList();
    }

    /**
     * Build query components (WHERE clause and parameters).
     */
    private QueryComponents buildQueryComponents(
            Boolean enabled,
            Boolean active,
            Date startDate,
            Date endDate) {

        List<Object> params = new ArrayList<>();
        StringBuilder whereClause = new StringBuilder();
        int paramIndex = 0;

        if (startDate != null) {
            Timestamp start = new Timestamp(startDate.getTime());
            params.add(start);
            paramIndex++;
            whereClause.append("w.dateCreated > ?").append(paramIndex);
        }

        if (endDate != null) {
            Timestamp end = new Timestamp(endDate.getTime());
            if (whereClause.length() > 0) {
                whereClause.append(" AND ");
            }
            params.add(end);
            paramIndex++;
            whereClause.append("w.dateCreated < ?").append(paramIndex);
        }

        if (enabled != null) {
            if (whereClause.length() > 0) {
                whereClause.append(" AND ");
            }
            params.add(enabled);
            paramIndex++;
            whereClause.append("w.visible = ?").append(paramIndex);
        }

        if (active != null) {
            if (whereClause.length() > 0) {
                whereClause.append(" AND ");
            }
            params.add(active);
            paramIndex++;
            whereClause.append("w.active = ?").append(paramIndex);
        }

        // Handle empty where clause
        if (whereClause.length() == 0) {
            whereClause.append("1=1");
        }

        return new QueryComponents(whereClause.toString(), params);
    }

    /**
     * Apply pagination to a query.
     */
    private void applyPagination(TypedQuery<?> query, int offset, int length) {
        if (offset != 0) {
            query.setFirstResult(offset);
        }
        if (length != -1) {
            query.setMaxResults(length);
        }
    }

    /**
     * Apply parameters to a query.
     */
    private void applyParameters(TypedQuery<?> query, List<Object> params) {
        for (int i = 0; i < params.size(); i++) {
            query.setParameter(i + 1, params.get(i));
        }
    }

    /**
     * Internal class to hold query components.
     */
    private static class QueryComponents {
        final String whereClause;
        final List<Object> params;

        QueryComponents(String whereClause, List<Object> params) {
            this.whereClause = whereClause;
            this.params = params;
        }
    }
}