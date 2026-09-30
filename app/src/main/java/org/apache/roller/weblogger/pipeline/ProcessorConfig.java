package org.apache.roller.weblogger.pipeline;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.weblogger.business.PropertiesManager;
import org.apache.roller.weblogger.business.WebloggerFactory;
import org.apache.roller.weblogger.pojos.RuntimeConfigProperty;

/**
 * Admin-controlled configuration for enabling/disabling processors.
 * 
 * Reads settings from Roller's runtime properties database (set via 
 * the admin Global Configuration page). Falls back to in-memory 
 * HashMap when Roller is not running (e.g., unit tests).
 */
public class ProcessorConfig {

    private static final Log LOG = LogFactory.getLog(ProcessorConfig.class);

    // Mapping from processor name to runtime property key
    private static final Map<String, String> PROCESSOR_PROPERTY_KEYS = new HashMap<>();
    static {
        PROCESSOR_PROPERTY_KEYS.put("ProfanityRewriter", "contentprocessing.profanityRewriter.enabled");
        PROCESSOR_PROPERTY_KEYS.put("BlogSummarizer", "contentprocessing.blogSummarizer.enabled");
        PROCESSOR_PROPERTY_KEYS.put("SmartTagGenerator", "contentprocessing.smartTagGenerator.enabled");
        PROCESSOR_PROPERTY_KEYS.put("ToneMoodAnalyzer", "contentprocessing.toneMoodAnalyzer.enabled");
    }

    // In-memory fallback for unit tests (when Roller is not initialized)
    private final Map<String, Boolean> testOverrides = new HashMap<>();
    private boolean useTestOverrides = false;

    public ProcessorConfig() {
        // Try to read from Roller runtime properties
        // If Roller is not running, fall back to defaults (all enabled)
        try {
            WebloggerFactory.getWeblogger();
            // Roller is available — we'll read from DB on each call
        } catch (Exception e) {
            // Roller not running (unit test mode) — use in-memory defaults
            useTestOverrides = true;
            testOverrides.put("ProfanityRewriter", true);
            testOverrides.put("BlogSummarizer", true);
            testOverrides.put("SmartTagGenerator", true);
            testOverrides.put("ToneMoodAnalyzer", true);
        }
    }

    /**
     * Check if a processor is enabled.
     * Reads from admin settings (DB) when Roller is running,
     * or from in-memory map during unit tests.
     */
    public boolean isEnabled(String processorName) {
        if (useTestOverrides) {
            return testOverrides.getOrDefault(processorName, false);
        }

        String propertyKey = PROCESSOR_PROPERTY_KEYS.get(processorName);
        if (propertyKey == null) {
            LOG.warn("Unknown processor: " + processorName);
            return false;
        }

        return getBooleanProperty(propertyKey, true);
    }

    /**
     * Set enabled/disabled — only works in test mode (in-memory).
     * In production, settings are changed via admin UI.
     */
    public void setEnabled(String processorName, boolean enabled) {
        if (useTestOverrides) {
            testOverrides.put(processorName, enabled);
        } else {
            LOG.warn("setEnabled() called in production mode. Use admin Global Configuration page instead.");
        }
    }

    /**
     * Check if the entire pipeline is enabled by admin.
     */
    public boolean isPipelineEnabled() {
        if (useTestOverrides) {
            return true; // always enabled in tests unless overridden
        }
        return getBooleanProperty("contentprocessing.pipeline.enabled", true);
    }

    /**
     * Get the LLM API key from admin settings, falling back to env variable.
     */
    public String getLLMApiKey() {
        if (!useTestOverrides) {
            String key = getStringProperty("contentprocessing.llm.apiKey", "");
            if (key != null && !key.isEmpty()) {
                return key;
            }
        }
        // Fallback to environment variable
        String envKey = System.getenv("ROLLER_LLM_API_KEY");
        return envKey != null ? envKey : "";
    }

    /**
     * Get the LLM model name from admin settings.
     */
    public String getLLMModel() {
        if (!useTestOverrides) {
            return getStringProperty("contentprocessing.llm.model", "llama-3.3-70b-versatile");
        }
        return "llama-3.3-70b-versatile";
    }

    public Map<String, Boolean> getAllSettings() {
        Map<String, Boolean> settings = new HashMap<>();
        for (String name : PROCESSOR_PROPERTY_KEYS.keySet()) {
            settings.put(name, isEnabled(name));
        }
        return settings;
    }

    // ---- Private helpers to read from Roller's runtime properties DB ----

    private boolean getBooleanProperty(String name, boolean defaultValue) {
        String val = getStringProperty(name, String.valueOf(defaultValue));
        return "true".equalsIgnoreCase(val);
    }

    private String getStringProperty(String name, String defaultValue) {
        try {
            PropertiesManager pmgr = WebloggerFactory.getWeblogger().getPropertiesManager();
            Map<String, RuntimeConfigProperty> props = pmgr.getProperties();
            RuntimeConfigProperty prop = props.get(name);
            if (prop != null && prop.getValue() != null) {
                return prop.getValue().trim();
            }
        } catch (Exception e) {
            LOG.warn("Could not read runtime property: " + name + ", using default: " + defaultValue, e);
        }
        return defaultValue;
    }
}