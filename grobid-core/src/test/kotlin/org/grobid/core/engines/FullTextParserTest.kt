package org.grobid.core.engines

import org.apache.commons.lang3.StringUtils
import org.apache.commons.lang3.tuple.Triple
import org.easymock.EasyMock.createMock
import org.grobid.core.analyzers.GrobidAnalyzer
import org.grobid.core.document.Document
import org.grobid.core.document.DocumentSource
import org.grobid.core.engines.label.TaggingLabels.TABLE_LABEL
import org.grobid.core.factory.GrobidFactory
import org.grobid.core.main.LibraryLoader
import org.grobid.core.utilities.GrobidConfig
import org.grobid.core.utilities.GrobidProperties
import org.grobid.core.utilities.GrobidTestUtils
import org.hamcrest.CoreMatchers
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.hasSize
import org.hamcrest.collection.IsCollectionWithSize
import org.junit.AfterClass
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Test
import java.util.*
import java.util.stream.Collectors

class FullTextParserTest {
    private lateinit var target: FullTextParser

    @Before
    @Throws(Exception::class)
    fun setUp() {
        val modelParameters = GrobidConfig.ModelParameters()
        modelParameters.name = "bao"
        GrobidProperties.addModel(modelParameters)
        target = FullTextParser(EngineParsers())
    }


    companion object {
        @JvmStatic
        @BeforeClass
        @Throws(java.lang.Exception::class)
        fun init() {
            LibraryLoader.load()
            GrobidProperties.getInstance()
        }

        @JvmStatic
        @AfterClass
        @Throws(java.lang.Exception::class)
        fun tearDown() {
            GrobidFactory.reset()
        }
    }

    @Test
    @Throws(Exception::class)
    fun testProcessTrainingDataFigures_single_figure() {
        val text = "The mechanism for superconductivity FIG. 1. λ(T) vs . T for YBCO"
        val tokens = GrobidAnalyzer.getInstance().tokenizeWithLayoutToken(text)
        val rese =
            "The\tthe\tT\tTh\tThe\tThe\te\the\tThe\tThe\tBLOCKSTART\tLINESTART\tALIGNEDLEFT\tNEWFONT\tHIGHERFONT\t0\t0\tINITCAP\tNODIGIT\t0\tNOPUNCT\t0\t4\t0\tNUMBER\t0\t0\tI-<paragraph>\n" +
                "mechanism\tmechanism\tm\tme\tmec\tmech\tm\tsm\tism\tnism\tBLOCKIN\tLINEIN\tALIGNEDLEFT\tSAMEFONT\tSAMEFONTSIZE\t0\t0\tNOCAPS\tNODIGIT\t0\tNOPUNCT\t0\t4\t0\tNUMBER\t0\t0\t<paragraph>\n" +
                "for\tfor\tf\tfo\tfor\tfor\tr\tor\tfor\tfor\tBLOCKIN\tLINEIN\tALIGNEDLEFT\tSAMEFONT\tSAMEFONTSIZE\t0\t0\tNOCAPS\tNODIGIT\t0\tNOPUNCT\t0\t4\t0\tNUMBER\t0\t0\t<paragraph>\n" +
                "superconductivity\tsuperconductivity\ts\tsu\tsup\tsupe\ty\tty\tity\tvity\tBLOCKIN\tLINEIN\tALIGNEDLEFT\tSAMEFONT\tSAMEFONTSIZE\t0\t0\tNOCAPS\tNODIGIT\t0\tNOPUNCT\t0\t4\t0\tNUMBER\t0\t0\t<paragraph>\n" +
                "FIG\tfig\tF\tFI\tFIG\tFIG\tG\tIG\tFIG\tFIG\tBLOCKSTART\tLINESTART\tLINEINDENT\tNEWFONT\tHIGHERFONT\t0\t0\tALLCAP\tNODIGIT\t0\tNOPUNCT\t10\t3\t0\tNUMBER\t0\t0\tI-<figure>\n" +
                ".\t.\t.\t.\t.\t.\t.\t.\t.\t.\tBLOCKIN\tLINEIN\tLINEINDENT\tSAMEFONT\tSAMEFONTSIZE\t0\t0\tALLCAP\tNODIGIT\t1\tDOT\t10\t3\t0\tNUMBER\t0\t0\t<figure>\n" +
                "1\t1\t1\t1\t1\t1\t1\t1\t1\t1\tBLOCKIN\tLINEIN\tLINEINDENT\tSAMEFONT\tSAMEFONTSIZE\t0\t0\tNOCAPS\tALLDIGIT\t1\tNOPUNCT\t10\t3\t0\tNUMBER\t1\t0\t<figure>\n" +
                ".\t.\t.\t.\t.\t.\t.\t.\t.\t.\tBLOCKIN\tLINEIN\tLINEINDENT\tSAMEFONT\tSAMEFONTSIZE\t0\t0\tALLCAP\tNODIGIT\t1\tDOT\t10\t3\t0\tNUMBER\t0\t0\t<figure>\n" +
                "λ\tλ\tλ\tλ\tλ\tλ\tλ\tλ\tλ\tλ\tBLOCKIN\tLINEIN\tLINEINDENT\tNEWFONT\tSAMEFONTSIZE\t0\t0\tNOCAPS\tNODIGIT\t1\tNOPUNCT\t10\t3\t0\tNUMBER\t0\t0\t<figure>\n" +
                "(\t(\t(\t(\t(\t(\t(\t(\t(\t(\tBLOCKIN\tLINEIN\tLINEINDENT\tSAMEFONT\tSAMEFONTSIZE\t0\t0\tALLCAP\tNODIGIT\t1\tOPENBRACKET\t10\t3\t0\tNUMBER\t0\t0\t<figure>\n" +
                "T\tt\tT\tT\tT\tT\tT\tT\tT\tT\tBLOCKIN\tLINEIN\tLINEINDENT\tSAMEFONT\tSAMEFONTSIZE\t0\t0\tALLCAP\tNODIGIT\t1\tNOPUNCT\t10\t3\t0\tNUMBER\t0\t0\t<figure>\n" +
                ")\t)\t)\t)\t)\t)\t)\t)\t)\t)\tBLOCKIN\tLINEIN\tLINEINDENT\tNEWFONT\tSAMEFONTSIZE\t0\t0\tALLCAP\tNODIGIT\t1\tENDBRACKET\t10\t3\t0\tNUMBER\t0\t0\t<figure>\n" +
                "vs\tvs\tv\tvs\tvs\tvs\ts\tvs\tvs\tvs\tBLOCKIN\tLINEIN\tLINEINDENT\tSAMEFONT\tSAMEFONTSIZE\t0\t0\tNOCAPS\tNODIGIT\t0\tNOPUNCT\t10\t3\t0\tNUMBER\t0\t0\t<figure>\n" +
                ".\t.\t.\t.\t.\t.\t.\t.\t.\t.\tBLOCKIN\tLINEEND\tLINEINDENT\tSAMEFONT\tSAMEFONTSIZE\t0\t0\tALLCAP\tNODIGIT\t1\tDOT\t10\t3\t0\tNUMBER\t0\t0\t<figure>\n" +
                "T\tt\tT\tT\tT\tT\tT\tT\tT\tT\tBLOCKIN\tLINESTART\tLINEINDENT\tNEWFONT\tSAMEFONTSIZE\t0\t0\tALLCAP\tNODIGIT\t1\tNOPUNCT\t10\t3\t0\tNUMBER\t0\t0\t<figure>\n" +
                "for\tfor\tf\tfo\tfor\tfor\tr\tor\tfor\tfor\tBLOCKIN\tLINEIN\tLINEINDENT\tNEWFONT\tSAMEFONTSIZE\t0\t0\tNOCAPS\tNODIGIT\t0\tNOPUNCT\t10\t3\t0\tNUMBER\t0\t0\t<figure>\n" +
                "YBCO\tybco\tY\tYB\tYBC\tYBCO\tO\tCO\tBCO\tYBCO\tBLOCKIN\tLINEIN\tLINEINDENT\tSAMEFONT\tSAMEFONTSIZE\t0\t0\tALLCAP\tNODIGIT\t0\tNOPUNCT\t10\t3\t0\tNUMBER\t0\t0\t<figure>\n\n"


        val stringStringPair = target!!.processTrainingDataFigures(rese, tokens, "123")

        val tei = stringStringPair.left
        val tokenisation = stringStringPair.right
        val reconstructedText =
            Arrays.stream(tokenisation.split("\n".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray())
                .map { l: String -> l.split("\t".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[0] }
                .collect(Collectors.joining(" "))

        MatcherAssert.assertThat(reconstructedText, CoreMatchers.`is`("FIG . 1 . λ ( T ) vs . T for YBCO"))
        MatcherAssert.assertThat(
            tokenisation.split("\n".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray().size,
            CoreMatchers.`is`(13)
        )
    }

    @Test
    @Throws(Exception::class)
    fun testProcessTrainingDataFigures_multiple_figures() {
        val text = "The mechanism for superconductivity FIG. 1. λ(T) vs . T for YBCO"
        val tokens = GrobidAnalyzer.getInstance().tokenizeWithLayoutToken(text)
        val rese =
            "The\tthe\tT\tTh\tThe\tThe\te\the\tThe\tThe\tBLOCKSTART\tLINESTART\tALIGNEDLEFT\tNEWFONT\tHIGHERFONT\t0\t0\tINITCAP\tNODIGIT\t0\tNOPUNCT\t0\t4\t0\tNUMBER\t0\t0\tI-<paragraph>\n" +
                "mechanism\tmechanism\tm\tme\tmec\tmech\tm\tsm\tism\tnism\tBLOCKIN\tLINEIN\tALIGNEDLEFT\tSAMEFONT\tSAMEFONTSIZE\t0\t0\tNOCAPS\tNODIGIT\t0\tNOPUNCT\t0\t4\t0\tNUMBER\t0\t0\t<paragraph>\n" +
                "for\tfor\tf\tfo\tfor\tfor\tr\tor\tfor\tfor\tBLOCKIN\tLINEIN\tALIGNEDLEFT\tSAMEFONT\tSAMEFONTSIZE\t0\t0\tNOCAPS\tNODIGIT\t0\tNOPUNCT\t0\t4\t0\tNUMBER\t0\t0\t<paragraph>\n" +
                "superconductivity\tsuperconductivity\ts\tsu\tsup\tsupe\ty\tty\tity\tvity\tBLOCKIN\tLINEIN\tALIGNEDLEFT\tSAMEFONT\tSAMEFONTSIZE\t0\t0\tNOCAPS\tNODIGIT\t0\tNOPUNCT\t0\t4\t0\tNUMBER\t0\t0\t<paragraph>\n" +
                "FIG\tfig\tF\tFI\tFIG\tFIG\tG\tIG\tFIG\tFIG\tBLOCKSTART\tLINESTART\tLINEINDENT\tNEWFONT\tHIGHERFONT\t0\t0\tALLCAP\tNODIGIT\t0\tNOPUNCT\t10\t3\t0\tNUMBER\t0\t0\tI-<figure>\n" +
                ".\t.\t.\t.\t.\t.\t.\t.\t.\t.\tBLOCKIN\tLINEIN\tLINEINDENT\tSAMEFONT\tSAMEFONTSIZE\t0\t0\tALLCAP\tNODIGIT\t1\tDOT\t10\t3\t0\tNUMBER\t0\t0\t<figure>\n" +
                "1\t1\t1\t1\t1\t1\t1\t1\t1\t1\tBLOCKIN\tLINEIN\tLINEINDENT\tSAMEFONT\tSAMEFONTSIZE\t0\t0\tNOCAPS\tALLDIGIT\t1\tNOPUNCT\t10\t3\t0\tNUMBER\t1\t0\t<figure>\n" +
                ".\t.\t.\t.\t.\t.\t.\t.\t.\t.\tBLOCKIN\tLINEIN\tLINEINDENT\tSAMEFONT\tSAMEFONTSIZE\t0\t0\tALLCAP\tNODIGIT\t1\tDOT\t10\t3\t0\tNUMBER\t0\t0\t<figure>\n" +
                "λ\tλ\tλ\tλ\tλ\tλ\tλ\tλ\tλ\tλ\tBLOCKIN\tLINEIN\tLINEINDENT\tNEWFONT\tSAMEFONTSIZE\t0\t0\tNOCAPS\tNODIGIT\t1\tNOPUNCT\t10\t3\t0\tNUMBER\t0\t0\t<figure>\n" +
                "(\t(\t(\t(\t(\t(\t(\t(\t(\t(\tBLOCKIN\tLINEIN\tLINEINDENT\tSAMEFONT\tSAMEFONTSIZE\t0\t0\tALLCAP\tNODIGIT\t1\tOPENBRACKET\t10\t3\t0\tNUMBER\t0\t0\t<figure>\n" +
                "T\tt\tT\tT\tT\tT\tT\tT\tT\tT\tBLOCKIN\tLINEIN\tLINEINDENT\tSAMEFONT\tSAMEFONTSIZE\t0\t0\tALLCAP\tNODIGIT\t1\tNOPUNCT\t10\t3\t0\tNUMBER\t0\t0\t<figure>\n" +
                ")\t)\t)\t)\t)\t)\t)\t)\t)\t)\tBLOCKIN\tLINEIN\tLINEINDENT\tNEWFONT\tSAMEFONTSIZE\t0\t0\tALLCAP\tNODIGIT\t1\tENDBRACKET\t10\t3\t0\tNUMBER\t0\t0\t<figure>\n" +
                "vs\tvs\tv\tvs\tvs\tvs\ts\tvs\tvs\tvs\tBLOCKIN\tLINEIN\tLINEINDENT\tSAMEFONT\tSAMEFONTSIZE\t0\t0\tNOCAPS\tNODIGIT\t0\tNOPUNCT\t10\t3\t0\tNUMBER\t0\t0\tI-<figure>\n" +
                ".\t.\t.\t.\t.\t.\t.\t.\t.\t.\tBLOCKIN\tLINEEND\tLINEINDENT\tSAMEFONT\tSAMEFONTSIZE\t0\t0\tALLCAP\tNODIGIT\t1\tDOT\t10\t3\t0\tNUMBER\t0\t0\t<figure>\n" +
                "T\tt\tT\tT\tT\tT\tT\tT\tT\tT\tBLOCKIN\tLINESTART\tLINEINDENT\tNEWFONT\tSAMEFONTSIZE\t0\t0\tALLCAP\tNODIGIT\t1\tNOPUNCT\t10\t3\t0\tNUMBER\t0\t0\t<figure>\n" +
                "for\tfor\tf\tfo\tfor\tfor\tr\tor\tfor\tfor\tBLOCKIN\tLINEIN\tLINEINDENT\tNEWFONT\tSAMEFONTSIZE\t0\t0\tNOCAPS\tNODIGIT\t0\tNOPUNCT\t10\t3\t0\tNUMBER\t0\t0\t<figure>\n" +
                "YBCO\tybco\tY\tYB\tYBC\tYBCO\tO\tCO\tBCO\tYBCO\tBLOCKIN\tLINEIN\tLINEINDENT\tSAMEFONT\tSAMEFONTSIZE\t0\t0\tALLCAP\tNODIGIT\t0\tNOPUNCT\t10\t3\t0\tNUMBER\t0\t0\t<figure>\n\n"


        val stringStringPair = target!!.processTrainingDataFigures(rese, tokens, "123")

        val tei = stringStringPair.left
        val tokenisation = stringStringPair.right
        val output: MutableList<String> = ArrayList()
        for (block in tokenisation.split("\n\n\n".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()) {
            val collect = Arrays.stream(block.split("\n".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray())
                .map { l: String -> l.split("\t".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[0] }
                .collect(Collectors.joining(" "))
            if (StringUtils.isNotBlank(collect)) {
                output.add(collect)
            }
        }

        MatcherAssert.assertThat<List<String>>(output, IsCollectionWithSize.hasSize(2))
        MatcherAssert.assertThat(output[0], CoreMatchers.`is`("FIG . 1 . λ ( T )"))
        MatcherAssert.assertThat(output[1], CoreMatchers.`is`("vs . T for YBCO"))
        MatcherAssert.assertThat(
            tokenisation.split("\n".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray().size,
            CoreMatchers.`is`(15)
        )
    }

    @Test
    @Throws(Exception::class)
    fun testProcessTrainingDataTables_single_table() {
        val text = "The mechanism for superconductivity FIG. 1. λ(T) vs . T for YBCO"
        val tokens = GrobidAnalyzer.getInstance().tokenizeWithLayoutToken(text)
        val rese =
            "The\tthe\tT\tTh\tThe\tThe\te\the\tThe\tThe\tBLOCKSTART\tLINESTART\tALIGNEDLEFT\tNEWFONT\tHIGHERFONT\t0\t0\tINITCAP\tNODIGIT\t0\tNOPUNCT\t0\t4\t0\tNUMBER\t0\t0\tI-<paragraph>\n" +
                "mechanism\tmechanism\tm\tme\tmec\tmech\tm\tsm\tism\tnism\tBLOCKIN\tLINEIN\tALIGNEDLEFT\tSAMEFONT\tSAMEFONTSIZE\t0\t0\tNOCAPS\tNODIGIT\t0\tNOPUNCT\t0\t4\t0\tNUMBER\t0\t0\t<paragraph>\n" +
                "for\tfor\tf\tfo\tfor\tfor\tr\tor\tfor\tfor\tBLOCKIN\tLINEIN\tALIGNEDLEFT\tSAMEFONT\tSAMEFONTSIZE\t0\t0\tNOCAPS\tNODIGIT\t0\tNOPUNCT\t0\t4\t0\tNUMBER\t0\t0\t<paragraph>\n" +
                "superconductivity\tsuperconductivity\ts\tsu\tsup\tsupe\ty\tty\tity\tvity\tBLOCKIN\tLINEIN\tALIGNEDLEFT\tSAMEFONT\tSAMEFONTSIZE\t0\t0\tNOCAPS\tNODIGIT\t0\tNOPUNCT\t0\t4\t0\tNUMBER\t0\t0\t<paragraph>\n" +
                "FIG\tfig\tF\tFI\tFIG\tFIG\tG\tIG\tFIG\tFIG\tBLOCKSTART\tLINESTART\tLINEINDENT\tNEWFONT\tHIGHERFONT\t0\t0\tALLCAP\tNODIGIT\t0\tNOPUNCT\t10\t3\t0\tNUMBER\t0\t0\tI-<table>\n" +
                ".\t.\t.\t.\t.\t.\t.\t.\t.\t.\tBLOCKIN\tLINEIN\tLINEINDENT\tSAMEFONT\tSAMEFONTSIZE\t0\t0\tALLCAP\tNODIGIT\t1\tDOT\t10\t3\t0\tNUMBER\t0\t0\t<table>\n" +
                "1\t1\t1\t1\t1\t1\t1\t1\t1\t1\tBLOCKIN\tLINEIN\tLINEINDENT\tSAMEFONT\tSAMEFONTSIZE\t0\t0\tNOCAPS\tALLDIGIT\t1\tNOPUNCT\t10\t3\t0\tNUMBER\t1\t0\t<table>\n" +
                ".\t.\t.\t.\t.\t.\t.\t.\t.\t.\tBLOCKIN\tLINEIN\tLINEINDENT\tSAMEFONT\tSAMEFONTSIZE\t0\t0\tALLCAP\tNODIGIT\t1\tDOT\t10\t3\t0\tNUMBER\t0\t0\t<table>\n" +
                "λ\tλ\tλ\tλ\tλ\tλ\tλ\tλ\tλ\tλ\tBLOCKIN\tLINEIN\tLINEINDENT\tNEWFONT\tSAMEFONTSIZE\t0\t0\tNOCAPS\tNODIGIT\t1\tNOPUNCT\t10\t3\t0\tNUMBER\t0\t0\t<table>\n" +
                "(\t(\t(\t(\t(\t(\t(\t(\t(\t(\tBLOCKIN\tLINEIN\tLINEINDENT\tSAMEFONT\tSAMEFONTSIZE\t0\t0\tALLCAP\tNODIGIT\t1\tOPENBRACKET\t10\t3\t0\tNUMBER\t0\t0\t<table>\n" +
                "T\tt\tT\tT\tT\tT\tT\tT\tT\tT\tBLOCKIN\tLINEIN\tLINEINDENT\tSAMEFONT\tSAMEFONTSIZE\t0\t0\tALLCAP\tNODIGIT\t1\tNOPUNCT\t10\t3\t0\tNUMBER\t0\t0\t<table>\n" +
                ")\t)\t)\t)\t)\t)\t)\t)\t)\t)\tBLOCKIN\tLINEIN\tLINEINDENT\tNEWFONT\tSAMEFONTSIZE\t0\t0\tALLCAP\tNODIGIT\t1\tENDBRACKET\t10\t3\t0\tNUMBER\t0\t0\t<table>\n" +
                "vs\tvs\tv\tvs\tvs\tvs\ts\tvs\tvs\tvs\tBLOCKIN\tLINEIN\tLINEINDENT\tSAMEFONT\tSAMEFONTSIZE\t0\t0\tNOCAPS\tNODIGIT\t0\tNOPUNCT\t10\t3\t0\tNUMBER\t0\t0\t<table>\n" +
                ".\t.\t.\t.\t.\t.\t.\t.\t.\t.\tBLOCKIN\tLINEEND\tLINEINDENT\tSAMEFONT\tSAMEFONTSIZE\t0\t0\tALLCAP\tNODIGIT\t1\tDOT\t10\t3\t0\tNUMBER\t0\t0\t<table>\n" +
                "T\tt\tT\tT\tT\tT\tT\tT\tT\tT\tBLOCKIN\tLINESTART\tLINEINDENT\tNEWFONT\tSAMEFONTSIZE\t0\t0\tALLCAP\tNODIGIT\t1\tNOPUNCT\t10\t3\t0\tNUMBER\t0\t0\t<table>\n" +
                "for\tfor\tf\tfo\tfor\tfor\tr\tor\tfor\tfor\tBLOCKIN\tLINEIN\tLINEINDENT\tNEWFONT\tSAMEFONTSIZE\t0\t0\tNOCAPS\tNODIGIT\t0\tNOPUNCT\t10\t3\t0\tNUMBER\t0\t0\t<table>\n" +
                "YBCO\tybco\tY\tYB\tYBC\tYBCO\tO\tCO\tBCO\tYBCO\tBLOCKIN\tLINEIN\tLINEINDENT\tSAMEFONT\tSAMEFONTSIZE\t0\t0\tALLCAP\tNODIGIT\t0\tNOPUNCT\t10\t3\t0\tNUMBER\t0\t0\t<table>\n\n"


        val stringStringPair = target!!.processTrainingDataTables(rese, tokens, "123")

        val tei = stringStringPair.left
        val tokenisation = stringStringPair.right
        val reconstructedText =
            Arrays.stream(tokenisation.split("\n".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray())
                .map { l: String -> l.split("\t".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[0] }
                .collect(Collectors.joining(" "))

        MatcherAssert.assertThat(reconstructedText, CoreMatchers.`is`("FIG . 1 . λ ( T ) vs . T for YBCO"))
        MatcherAssert.assertThat(
            tokenisation.split("\n".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray().size,
            CoreMatchers.`is`(13)
        )
    }

    @Test
    @Throws(Exception::class)
    fun testProcessTrainingDataTable_multiple_tables() {
        val text = "The mechanism for superconductivity FIG. 1. λ(T) vs . T for YBCO"
        val tokens = GrobidAnalyzer.getInstance().tokenizeWithLayoutToken(text)
        val rese =
            "The\tthe\tT\tTh\tThe\tThe\te\the\tThe\tThe\tBLOCKSTART\tLINESTART\tALIGNEDLEFT\tNEWFONT\tHIGHERFONT\t0\t0\tINITCAP\tNODIGIT\t0\tNOPUNCT\t0\t4\t0\tNUMBER\t0\t0\tI-<paragraph>\n" +
                "mechanism\tmechanism\tm\tme\tmec\tmech\tm\tsm\tism\tnism\tBLOCKIN\tLINEIN\tALIGNEDLEFT\tSAMEFONT\tSAMEFONTSIZE\t0\t0\tNOCAPS\tNODIGIT\t0\tNOPUNCT\t0\t4\t0\tNUMBER\t0\t0\t<paragraph>\n" +
                "for\tfor\tf\tfo\tfor\tfor\tr\tor\tfor\tfor\tBLOCKIN\tLINEIN\tALIGNEDLEFT\tSAMEFONT\tSAMEFONTSIZE\t0\t0\tNOCAPS\tNODIGIT\t0\tNOPUNCT\t0\t4\t0\tNUMBER\t0\t0\t<paragraph>\n" +
                "superconductivity\tsuperconductivity\ts\tsu\tsup\tsupe\ty\tty\tity\tvity\tBLOCKIN\tLINEIN\tALIGNEDLEFT\tSAMEFONT\tSAMEFONTSIZE\t0\t0\tNOCAPS\tNODIGIT\t0\tNOPUNCT\t0\t4\t0\tNUMBER\t0\t0\t<paragraph>\n" +
                "FIG\tfig\tF\tFI\tFIG\tFIG\tG\tIG\tFIG\tFIG\tBLOCKSTART\tLINESTART\tLINEINDENT\tNEWFONT\tHIGHERFONT\t0\t0\tALLCAP\tNODIGIT\t0\tNOPUNCT\t10\t3\t0\tNUMBER\t0\t0\tI-<table>\n" +
                ".\t.\t.\t.\t.\t.\t.\t.\t.\t.\tBLOCKIN\tLINEIN\tLINEINDENT\tSAMEFONT\tSAMEFONTSIZE\t0\t0\tALLCAP\tNODIGIT\t1\tDOT\t10\t3\t0\tNUMBER\t0\t0\t<table>\n" +
                "1\t1\t1\t1\t1\t1\t1\t1\t1\t1\tBLOCKIN\tLINEIN\tLINEINDENT\tSAMEFONT\tSAMEFONTSIZE\t0\t0\tNOCAPS\tALLDIGIT\t1\tNOPUNCT\t10\t3\t0\tNUMBER\t1\t0\t<table>\n" +
                ".\t.\t.\t.\t.\t.\t.\t.\t.\t.\tBLOCKIN\tLINEIN\tLINEINDENT\tSAMEFONT\tSAMEFONTSIZE\t0\t0\tALLCAP\tNODIGIT\t1\tDOT\t10\t3\t0\tNUMBER\t0\t0\t<table>\n" +
                "λ\tλ\tλ\tλ\tλ\tλ\tλ\tλ\tλ\tλ\tBLOCKIN\tLINEIN\tLINEINDENT\tNEWFONT\tSAMEFONTSIZE\t0\t0\tNOCAPS\tNODIGIT\t1\tNOPUNCT\t10\t3\t0\tNUMBER\t0\t0\t<table>\n" +
                "(\t(\t(\t(\t(\t(\t(\t(\t(\t(\tBLOCKIN\tLINEIN\tLINEINDENT\tSAMEFONT\tSAMEFONTSIZE\t0\t0\tALLCAP\tNODIGIT\t1\tOPENBRACKET\t10\t3\t0\tNUMBER\t0\t0\t<table>\n" +
                "T\tt\tT\tT\tT\tT\tT\tT\tT\tT\tBLOCKIN\tLINEIN\tLINEINDENT\tSAMEFONT\tSAMEFONTSIZE\t0\t0\tALLCAP\tNODIGIT\t1\tNOPUNCT\t10\t3\t0\tNUMBER\t0\t0\t<table>\n" +
                ")\t)\t)\t)\t)\t)\t)\t)\t)\t)\tBLOCKIN\tLINEIN\tLINEINDENT\tNEWFONT\tSAMEFONTSIZE\t0\t0\tALLCAP\tNODIGIT\t1\tENDBRACKET\t10\t3\t0\tNUMBER\t0\t0\t<table>\n" +
                "vs\tvs\tv\tvs\tvs\tvs\ts\tvs\tvs\tvs\tBLOCKIN\tLINEIN\tLINEINDENT\tSAMEFONT\tSAMEFONTSIZE\t0\t0\tNOCAPS\tNODIGIT\t0\tNOPUNCT\t10\t3\t0\tNUMBER\t0\t0\tI-<table>\n" +
                ".\t.\t.\t.\t.\t.\t.\t.\t.\t.\tBLOCKIN\tLINEEND\tLINEINDENT\tSAMEFONT\tSAMEFONTSIZE\t0\t0\tALLCAP\tNODIGIT\t1\tDOT\t10\t3\t0\tNUMBER\t0\t0\t<table>\n" +
                "T\tt\tT\tT\tT\tT\tT\tT\tT\tT\tBLOCKIN\tLINESTART\tLINEINDENT\tNEWFONT\tSAMEFONTSIZE\t0\t0\tALLCAP\tNODIGIT\t1\tNOPUNCT\t10\t3\t0\tNUMBER\t0\t0\t<table>\n" +
                "for\tfor\tf\tfo\tfor\tfor\tr\tor\tfor\tfor\tBLOCKIN\tLINEIN\tLINEINDENT\tNEWFONT\tSAMEFONTSIZE\t0\t0\tNOCAPS\tNODIGIT\t0\tNOPUNCT\t10\t3\t0\tNUMBER\t0\t0\t<table>\n" +
                "YBCO\tybco\tY\tYB\tYBC\tYBCO\tO\tCO\tBCO\tYBCO\tBLOCKIN\tLINEIN\tLINEINDENT\tSAMEFONT\tSAMEFONTSIZE\t0\t0\tALLCAP\tNODIGIT\t0\tNOPUNCT\t10\t3\t0\tNUMBER\t0\t0\t<table>\n\n"


        val stringStringPair = target!!.processTrainingDataTables(rese, tokens, "123")

        val tei = stringStringPair.left
        val tokenisation = stringStringPair.right
        val output: MutableList<String> = ArrayList()
        for (block in tokenisation.split("\n\n\n".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()) {
            val collect = Arrays.stream(block.split("\n".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray())
                .map { l: String -> l.split("\t".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[0] }
                .collect(Collectors.joining(" "))
            if (StringUtils.isNotBlank(collect)) {
                output.add(collect)
            }
        }

        MatcherAssert.assertThat<List<String>>(output, IsCollectionWithSize.hasSize(2))
        MatcherAssert.assertThat(output[0], CoreMatchers.`is`("FIG . 1 . λ ( T )"))
        MatcherAssert.assertThat(output[1], CoreMatchers.`is`("vs . T for YBCO"))
        MatcherAssert.assertThat(
            tokenisation.split("\n".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray().size,
            CoreMatchers.`is`(15)
        )
    }

    @Test
    fun testFindCandidates() {
        var mockDocumentSource = createMock<DocumentSource>(DocumentSource::class.java)
        var document = Document.createFromText("")

        // i need to prepare a sequence where there might be multiple matches,
        // and then verify that the sequence is correctly used for discrimination
        var sequence = "This article solves the problem where some of our interaction are fauly. " +
            "a 8 9 j 92j 3 3j 9 j 9j Table 1: The reconstruction of the national anthem " +
            "We are interested in the relation between certain information and " +
            "a b b d 1 2 3 4 s 3 3 d9 Table 2: The relation between information and noise " +
            "the related affectionality. " +
            "a b b d 1 2 3 4 5 6 7 Table 3: The relation between homicides and donuts eating " +
            "The relation between homicides and donuts eating is a very important one. "

        var tokens = GrobidAnalyzer.getInstance().tokenizeWithLayoutToken(sequence)

        // These triples made in following way: label, starting index (included), ending index (excluded)
        val labels = listOf(
            Triple.of("I-<paragraph>", 0, 1),
            Triple.of("<paragraph>", 1, 24),
            Triple.of("I-<table>", 25, 26),
            Triple.of("<table>", 26, 61),
            Triple.of("I-<paragraph>", 62, 63),
            Triple.of("<paragraph>", 63, 81),
            Triple.of("I-<table>", 82, 83),
            Triple.of("<table>", 82, 118),
            Triple.of("I-<paragraph>", 119, 120),
            Triple.of("<paragraph>", 120, 129),
            Triple.of("I-<table>", 130, 131),
            Triple.of("<table>", 131, 171),
            Triple.of("I-<paragraph>", 171, 172),
            Triple.of("<paragraph>", 172, 195),
        )

        val features = tokens.stream().map { it.text }.collect(Collectors.toList())

        val wapitiResult = GrobidTestUtils.getWapitiResult(features, labels, "\t")
        val labelledResultsAsList =
            Arrays.stream(wapitiResult.split("\n".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray())
                .map<List<String>> { l: String ->
                    Arrays.stream(
                        l.split("\t".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                    )
                        .collect(Collectors.toList())
                }
                .collect(Collectors.toList())

        println(wapitiResult)

        val table1Tokens = tokens.subList(25, 61)
        val foundCandidateIndex = FullTextParser.findCandiateIndex(table1Tokens, labelledResultsAsList, TABLE_LABEL)

        assertThat(foundCandidateIndex, hasSize(3))
        assertThat(foundCandidateIndex.get(0), `is`(13))
        assertThat(foundCandidateIndex.get(1), `is`(42))
        assertThat(foundCandidateIndex.get(2), `is`(67))
    }

}