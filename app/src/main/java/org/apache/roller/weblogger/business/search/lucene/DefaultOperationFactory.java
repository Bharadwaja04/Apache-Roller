/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  The ASF licenses this file to You
 * under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *kk
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.  For additional information regarding
 * copyright in this work, please see the NOTICE file in the top level
 * directory of this distribution.l
 */
package org.apache.roller.weblogger.business.search.lucene;

import org.apache.roller.weblogger.business.URLStrategy;
import org.apache.roller.weblogger.business.Weblogger;
import org.apache.roller.weblogger.pojos.Weblog;
import org.apache.roller.weblogger.pojos.WeblogEntry;

/**
 * Default implementation of OperationFactory.
 * Creates concrete operation classes with IndexContext dependency.
 */
public class DefaultOperationFactory implements OperationFactory {

    private final Weblogger roller;
    private final IndexContext context;

    public DefaultOperationFactory(Weblogger roller, IndexContext context) {
        this.roller = roller;
        this.context = context;
    }

    @Override
    public IndexCommand createAddEntryOperation(WeblogEntry entry) {
        return new AddEntryOperation(roller, context, entry);
    }

    @Override
    public IndexCommand createRemoveEntryOperation(WeblogEntry entry) {
        return new RemoveEntryOperation(roller, context, entry);
    }

    @Override
    public IndexCommand createUpdateEntryOperation(WeblogEntry entry) {
        return new ReIndexEntryOperation(roller, context, entry);
    }

    @Override
    public IndexCommand createRebuildWeblogOperation(Weblog weblog) {
        return new RebuildWebsiteIndexOperation(roller, context, weblog);
    }

    @Override
    public IndexCommand createRemoveWeblogOperation(Weblog weblog) {
        return new RemoveWebsiteIndexOperation(roller, context, weblog);
    }

    @Override
    public SearchCommand createSearchOperation(String term, String weblogHandle, String category, String locale, int offset, int limit, URLStrategy urlStrategy) {
        return new SearchOperation(context, term, weblogHandle, category, locale, offset, limit, urlStrategy);
    }
}
