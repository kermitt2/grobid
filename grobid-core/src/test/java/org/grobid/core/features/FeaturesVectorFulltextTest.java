package org.grobid.core.features;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class FeaturesVectorFulltextTest {

    @Test
    public void testSanitizationInWapitiSerializationToken() {
        FeaturesVectorFulltext fulltext = new FeaturesVectorFulltext();
        fulltext.string = "½ºº´\r";
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
