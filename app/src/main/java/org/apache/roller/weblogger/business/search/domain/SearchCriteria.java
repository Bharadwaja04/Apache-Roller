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

/**
 * Immutable value object representing search criteria.
 * Follows Single Responsibility Principle - only holds search parameters.
 */
public class SearchCriteria {

    private final String term;
    private final String weblogHandle;
    private final String category;
    private final String locale;

    private SearchCriteria(Builder builder) {
        this.term = builder.term;
        this.weblogHandle = builder.weblogHandle;
        this.category = builder.category;
        this.locale = builder.locale;
    }

    public String getTerm() {
        return term;
    }

    public String getWeblogHandle() {
        return weblogHandle;
    }

    public String getCategory() {
        return category;
    }

    public String getLocale() {
        return locale;
    }

    public static Builder builder() {
        return new Builder();
    }

    /**
     * Builder for SearchCriteria following the Builder pattern.
     */
    public static class Builder {
        private String term;
        private String weblogHandle;
        private String category;
        private String locale;

        public Builder term(String term) {
            this.term = term;
            return this;
        }

        public Builder weblogHandle(String weblogHandle) {
            this.weblogHandle = weblogHandle;
            return this;
        }

        public Builder category(String category) {
            this.category = category;
            return this;
        }

        public Builder locale(String locale) {
            this.locale = locale;
            return this;
        }

        public SearchCriteria build() {
            return new SearchCriteria(this);
        }
    }
}
