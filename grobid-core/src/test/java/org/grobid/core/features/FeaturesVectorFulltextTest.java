package org.grobid.core.features;

import com.google.common.collect.Lists;
import org.junit.Test;

import java.util.ArrayList;

import static org.junit.Assert.assertEquals;

public class FeaturesVectorFulltextTest {

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
            testSanitizationInWapitiSerializationToken(token);
        }
    }

    private void testSanitizationInWapitiSerializationToken(String token) {
        FeaturesVectorFulltext fulltext = new FeaturesVectorFulltext();
        fulltext.string = token;
        fulltext.blockStatus = "BLOCKIN";
        fulltext.lineStatus = "LINESTART";
        fulltext.alignmentStatus = "ALIGNEDLEFT";
        fulltext.fontStatus = "SAMEFONT";
        fulltext.fontSize = "SAMEFONTSIZE";
        fulltext.bold = false;
        fulltext.italic = false;
        fulltext.digit = "NOCAPS";
        fulltext.capitalisation = "NODIGIT";
        fulltext.singleChar = false;
        fulltext.punctType = "NOPUNCT";
        fulltext.relativeDocumentPosition = 10;
        fulltext.relativePagePosition = 2;
        fulltext.bitmapAround = false;
        assertEquals("½ºº´ ½ºº´ ½ ½º ½ºº ½ºº´ ´ º´ ºº´ ½ºº´ BLOCKIN LINESTART ALIGNEDLEFT SAMEFONT SAMEFONTSIZE 0 0 NODIGIT NOCAPS 0 NOPUNCT 10 2 0\n", fulltext.printVector());
    }
}
