package org.grobid.core.utilities;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import static org.junit.Assert.assertEquals;


public class EnvironmentVariablePropertiesTest {
    private Map<String, String> environmentVariables = new HashMap<>();

    @Test
    public void shouldReturnEmptyPropertiesWithEmptyEnvironmentVariables() {
        assertEquals(
            Collections.emptyMap(),
            new EnvironmentVariableProperties(
                environmentVariables,
                "APP__.+"
            ).getProperties()
        );
    }

    @Test
    public void shouldReturnEmptyPropertiesWithNotMatchingEnvironmentVariables() {
        environmentVariables.put("OTHER__ABC", "value1");
        assertEquals(
            Collections.emptyMap(),
            new EnvironmentVariableProperties(
                environmentVariables,
                "APP__.+"
            ).getProperties()
        );
    }

    @Test
    public void shouldReturnAndConvertMatchingEnvironmentVariable() {
        environmentVariables.put("APP__ABC", "value1");
        assertEquals(
            Collections.singletonMap("app.abc", "value1"),
            new EnvironmentVariableProperties(
                environmentVariables,
                "APP__.+"
            ).getProperties()
        );
    }

    @Test
    public void shouldReturnAndConvertMatchingNestedEnvironmentVariable() {
        environmentVariables.put("APP__ABC__XYZ", "value1");
        assertEquals(
            Collections.singletonMap("app.abc.xyz", "value1"),
            new EnvironmentVariableProperties(
                environmentVariables,
                "APP__.+"
            ).getProperties()
        );
    }

    @Test
    public void shouldReturnAndConvertMatchingEnvironmentVariableWithUnderscore() {
        environmentVariables.put("APP__ABC_XYZ", "value1");
        assertEquals(
            Collections.singletonMap("app.abc_xyz", "value1"),
            new EnvironmentVariableProperties(
                environmentVariables,
                "APP__.+"
            ).getProperties()
        );
    }
}
