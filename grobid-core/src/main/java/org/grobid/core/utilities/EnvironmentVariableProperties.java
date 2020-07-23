package org.grobid.core.utilities;

import java.util.HashMap;
import java.util.Map;

public class EnvironmentVariableProperties {
    private final Map<String, String> properties = new HashMap<>();

    public EnvironmentVariableProperties(String matcher) {
        this(System.getenv(), matcher);
    }

    public EnvironmentVariableProperties(Map<String, String> environmentVariablesMap, String matcher) {
        for (Map.Entry<String, String> entry: environmentVariablesMap.entrySet()) {
            if (!entry.getKey().matches(matcher)) {
                continue;
            }
            String propertiesKey = getPropertiesKeyForEnvironmentVariableName(entry.getKey());
            this.properties.put(propertiesKey, entry.getValue());
        }
    }

    private static String getPropertiesKeyForEnvironmentVariableName(String name) {
        return name.replace("__", ".").toLowerCase();
    }

    public Map<String, String> getProperties() {
        return this.properties;
    }
}
