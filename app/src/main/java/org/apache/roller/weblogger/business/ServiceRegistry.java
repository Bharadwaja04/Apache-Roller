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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Non-static service registry for dependency management.
 * 
 * This class provides an alternative to the static WebloggerFactory
 * by allowing instance-based service lookup and supporting dependency injection.
 * 
 * <p>Usage for new code (preferred):</p>
 * <pre>
 * public class MyService {
 *     private final UserManager userManager;
 *     
 *     public MyService(ServiceRegistry registry) {
 *         this.userManager = registry.getService(UserManager.class);
 *     }
 * }
 * </pre>
 * 
 * <p>This addresses the Service Locator anti-pattern by:</p>
 * <ul>
 *   <li>Making dependencies explicit through constructor injection</li>
 *   <li>Supporting instance-based (non-static) lookup</li>
 *   <li>Enabling easy mocking for tests</li>
 * </ul>
 */
public class ServiceRegistry {

    private static final Log LOG = LogFactory.getLog(ServiceRegistry.class);

    private final Map<Class<?>, Object> services = new ConcurrentHashMap<>();
    private final Weblogger weblogger;

    /**
     * Create a ServiceRegistry backed by a Weblogger instance.
     * 
     * @param weblogger The Weblogger instance to delegate to
     */
    public ServiceRegistry(Weblogger weblogger) {
        if (weblogger == null) {
            throw new IllegalArgumentException("Weblogger cannot be null");
        }
        this.weblogger = weblogger;
        initializeServices();
    }

    /**
     * Get a service by its interface type.
     * 
     * @param serviceType The interface class of the service
     * @param <T> The service type
     * @return The service instance
     * @throws IllegalArgumentException if service type is not registered
     */
    @SuppressWarnings("unchecked")
    public <T> T getService(Class<T> serviceType) {
        Object service = services.get(serviceType);
        if (service == null) {
            throw new IllegalArgumentException("No service registered for type: " + serviceType.getName());
        }
        return (T) service;
    }

    /**
     * Check if a service is registered.
     * 
     * @param serviceType The interface class of the service
     * @return true if service is registered
     */
    public boolean hasService(Class<?> serviceType) {
        return services.containsKey(serviceType);
    }

    /**
     * Register a custom service (useful for testing).
     * 
     * @param serviceType The interface class
     * @param service The service implementation
     * @param <T> The service type
     */
    public <T> void registerService(Class<T> serviceType, T service) {
        services.put(serviceType, service);
        LOG.debug("Registered service: " + serviceType.getName());
    }

    /**
     * Get the underlying Weblogger instance.
     * 
     * @return Weblogger instance
     */
    public Weblogger getWeblogger() {
        return weblogger;
    }

    // -------------------- Service Group Accessors --------------------

    /**
     * Get content-related services.
     * 
     * @return ContentServices facade
     */
    public ContentServices getContentServices() {
        return weblogger;
    }

    /**
     * Get user-related services.
     * 
     * @return UserServices facade
     */
    public UserServices getUserServices() {
        return weblogger;
    }

    /**
     * Get ping-related services.
     * 
     * @return PingServices facade
     */
    public PingServices getPingServices() {
        return weblogger;
    }

    /**
     * Get system-related services.
     * 
     * @return SystemServices facade
     */
    public SystemServices getSystemServices() {
        return weblogger;
    }

    /**
     * Get theme and plugin services.
     * 
     * @return ThemeAndPluginServices facade
     */
    public ThemeAndPluginServices getThemeAndPluginServices() {
        return weblogger;
    }

    /**
     * Get planet-related services.
     * 
     * @return PlanetServices facade
     */
    public PlanetServices getPlanetServices() {
        return weblogger;
    }

    // -------------------- Private Methods --------------------

    private void initializeServices() {
        // Register individual managers
        services.put(UserManager.class, weblogger.getUserManager());
        services.put(WeblogManager.class, weblogger.getWeblogManager());
        services.put(WeblogEntryManager.class, weblogger.getWeblogEntryManager());
        services.put(BookmarkManager.class, weblogger.getBookmarkManager());
        services.put(MediaFileManager.class, weblogger.getMediaFileManager());
        services.put(FileContentManager.class, weblogger.getFileContentManager());
        services.put(PropertiesManager.class, weblogger.getPropertiesManager());
        services.put(OAuthManager.class, weblogger.getOAuthManager());
        services.put(URLStrategy.class, weblogger.getUrlStrategy());

        // Register service group interfaces
        services.put(ContentServices.class, weblogger);
        services.put(UserServices.class, weblogger);
        services.put(PingServices.class, weblogger);
        services.put(SystemServices.class, weblogger);
        services.put(ThemeAndPluginServices.class, weblogger);
        services.put(PlanetServices.class, weblogger);
        services.put(Weblogger.class, weblogger);

        LOG.debug("ServiceRegistry initialized with " + services.size() + " services");
    }
}