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

package org.apache.roller.weblogger.business;

import org.apache.roller.planet.business.PlanetManager;
import org.apache.roller.planet.business.PlanetURLStrategy;
import org.apache.roller.planet.business.fetcher.FeedFetcher;

/**
 * Interface for planet/feed aggregation services.
 * Extracted from Weblogger to address Cyclic-Dependent Modularization.
 * 
 * Groups planet management services together to reduce coupling.
 */
public interface PlanetServices {

    /**
     * Get PlanetManager for planet operations.
     * 
     * @return PlanetManager instance
     */
    PlanetManager getPlanetManager();

    /**
     * Get FeedFetcher for feed fetching operations.
     * 
     * @return FeedFetcher instance
     */
    FeedFetcher getFeedFetcher();

    /**
     * Get PlanetURLStrategy for planet URL building.
     * 
     * @return PlanetURLStrategy instance
     */
    PlanetURLStrategy getPlanetURLStrategy();
}