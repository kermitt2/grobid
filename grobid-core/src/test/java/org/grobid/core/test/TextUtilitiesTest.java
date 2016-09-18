package org.grobid.core.test;

import org.grobid.core.utilities.TextUtilities;
import org.junit.Test;

import static junit.framework.Assert.assertEquals;

/**
 * Date: 4/13/12
 * Time: 7:42 PM
 *
 * @author Vyacheslav Zholudev
 */
public class TextUtilitiesTest {

    @Test
    public void testWordShape() {
        testWordShape("This", "Xxxx", "Xx");
        testWordShape("Equals", "Xxxx", "Xx");
        testWordShape("O'Conor", "X'Xxxx", "X'Xx");
        testWordShape("McDonalds", "XxXxxx", "XxXx");
        testWordShape("any-where", "xx-xxx", "x-x");
        testWordShape("1.First", "d.Xxxx", "d.Xx");
        testWordShape("ThisIsCamelCase", "XxXxXxXxxx", "XxXxXxXx");
        testWordShape("This:happens", "Xx:xxx", "Xx:x");
        testWordShape("ABC", "XXX", "X");
        testWordShape("AC", "XX", "X");
        testWordShape("A", "X", "X");
        testWordShape("Ab", "Xx", "Xx");
        testWordShape("AbA", "XxX", "XxX");
        testWordShape("uü", "xx", "x");
        testWordShape("Üwe", "Xxx", "Xx");
    }

    @Test
    public void testPrefix()
    {
        String word = "Grobid";
        assertEquals("", TextUtilities.prefix(word, 0));
        assertEquals("G", TextUtilities.prefix(word, 1));
        assertEquals("Gr", TextUtilities.prefix(word, 2));
        assertEquals("Gro", TextUtilities.prefix(word, 3));
        assertEquals("Grob", TextUtilities.prefix(word, 4));

        assertEquals("Grobid", TextUtilities.prefix(word, 6));

        assertEquals("Grobid", TextUtilities.prefix(word, 100));

        assertEquals(null, TextUtilities.prefix(null, 0));
        assertEquals(null, TextUtilities.prefix(null, 1));
    }

    @Test
    public void testSuffix()
    {
        String word = "Grobid";
        assertEquals("", TextUtilities.suffix(word, 0));
        assertEquals("d", TextUtilities.suffix(word, 1));
        assertEquals("id", TextUtilities.suffix(word, 2));
        assertEquals("bid", TextUtilities.suffix(word, 3));
        assertEquals("obid", TextUtilities.suffix(word, 4));

        assertEquals("Grobid", TextUtilities.suffix(word, 6));

        assertEquals("Grobid", TextUtilities.suffix(word, 100));

        assertEquals(null, TextUtilities.suffix(null, 0));
        assertEquals(null, TextUtilities.suffix(null, 1));
    }

    private void testWordShape(String orig, String expected, String expectedTrimmed) {
        assertEquals(expected, TextUtilities.wordShape(orig));
        assertEquals(expectedTrimmed, TextUtilities.wordShapeTrimmed(orig));
    }
}
