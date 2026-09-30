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

/**
 * Priority levels for index operations.
 * 
 * - HIGH: Real-time user requests (e.g. searching)
 * - NORMAL: Background updates (e.g. adding an entry)
 * - LOW: Heavy background tasks (e.g. rebuilding index)
 */
public enum OperationPriority {
    HIGH(1),
    NORMAL(2),
    LOW(3);

    private final int value;

    OperationPriority(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
