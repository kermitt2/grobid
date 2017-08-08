package org.grobid.core.features;

import com.google.common.collect.Lists;
import org.grobid.core.features.FeaturesUtils;
import org.junit.Test;

import java.util.ArrayList;

import static org.junit.Assert.assertEquals;

public class FeaturesUtilsTest {

    @Test
    public void testSanitizationInWapitiSerializationTokens() {
        ArrayList<String> tokens = Lists.newArrayList(
            "½ºº´\r",
            "½ºº´\n",
            "½ºº´\t",
            "½ºº´\f",
            "½ºº´ ",
            "½ºº´\f\n",
            "½ºº´\r\t");
        for (String token : tokens) {
            assertEquals("½ºº´", FeaturesUtils.sanitizeTokenForWapiti(token));
        }
    }

}
