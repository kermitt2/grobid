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
    public void testWordShapeTrimmed() {

    }

    private void testWordShape(String orig, String expected, String expectedTrimmed) {
        assertEquals(expected, TextUtilities.wordShape(orig));
        assertEquals(expectedTrimmed, TextUtilities.wordShapeTrimmed(orig));
    }


}
