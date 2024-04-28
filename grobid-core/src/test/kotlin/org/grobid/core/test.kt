package org.grobid.core

import org.junit.Test
import java.nio.charset.StandardCharsets

class TextParserTest {

    @Test
    fun testConvertFractions6Numeric() {
        val byteArray = byteArrayOf(-3, -1, -73, 0, 103, 0, 47, 0, 109, 0, 108, 0);
        val input = String(byteArray, StandardCharsets.UTF_16LE)

        print("toto")
    }
}
