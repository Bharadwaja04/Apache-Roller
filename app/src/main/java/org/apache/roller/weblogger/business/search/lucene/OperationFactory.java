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

import org.apache.roller.weblogger.pojos.Weblog;
import org.apache.roller.weblogger.pojos.WeblogEntry;
import org.apache.roller.weblogger.business.URLStrategy;

/**
 * Interface for creating index operations.
 * Decouples LuceneIndexManager from concrete operation implementations.
 */
public interface OperationFactory {
    IndexCommand createAddEntryOperation(WeblogEntry entry);
    IndexCommand createRemoveEntryOperation(WeblogEntry entry);
    IndexCommand createUpdateEntryOperation(WeblogEntry entry);
    IndexCommand createRebuildWeblogOperation(Weblog weblog);
    IndexCommand createRemoveWeblogOperation(Weblog weblog);
    SearchCommand createSearchOperation(String term, String weblogHandle, String category, String locale, int offset, int limit, URLStrategy urlStrategy);
}
