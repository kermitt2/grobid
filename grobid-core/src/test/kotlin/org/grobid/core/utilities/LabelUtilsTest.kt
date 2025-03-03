package org.grobid.core.utilities

import org.apache.commons.lang3.StringUtils
import org.grobid.core.utilities.GrobidConfig.ModelParameters
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.not
import org.hamcrest.MatcherAssert.assertThat
import org.junit.jupiter.api.BeforeAll
import java.util.*
import java.util.stream.Collectors
import kotlin.test.Test


class LabelUtilsTest {


    @Test
    fun testPostProcessLabeledAbstract_shouldTransformTableLabelInParagraphLabel() {
        val resultWithTables =
            "This\tthis\tT\tTh\tThi\tThis\ts\tis\this\tThis\tBLOCKIN\tLINEIN\tALIGNEDLEFT\tNEWFONT\tHIGHERFONT\t0\t0\tINITCAP\tNODIGIT\t0\tNOPUNCT\t0\t10\t0\tNUMBER\t0\t0\tI-<table>\n" +
                "study\tstudy\ts\tst\tstu\tstud\ty\tdy\tudy\ttudy\tBLOCKIN\tLINEIN\tALIGNEDLEFT\tSAMEFONT\tSAMEFONTSIZE\t0\t0\tNOCAPS\tNODIGIT\t0\tNOPUNCT\t0\t10\t0\tNUMBER\t0\t0\t<table>\n" +
                "was\twas\tw\twa\twas\twas\ts\tas\twas\twas\tBLOCKIN\tLINEIN\tALIGNEDLEFT\tSAMEFONT\tSAMEFONTSIZE\t0\t0\tNOCAPS\tNODIGIT\t0\tNOPUNCT\t0\t10\t0\tNUMBER\t0\t0\t<table>\n" +
                "supported\tsupported\ts\tsu\tsup\tsupp\td\ted\tted\trted\tBLOCKIN\tLINEIN\tALIGNEDLEFT\tSAMEFONT\tSAMEFONTSIZE\t0\t0\tNOCAPS\tNODIGIT\t0\tNOPUNCT\t0\t10\t0\tNUMBER\t0\t0\t<table>\n" +
                "by\tby\tb\tby\tby\tby\ty\tby\tby\tby\tBLOCKIN\tLINEIN\tALIGNEDLEFT\tSAMEFONT\tSAMEFONTSIZE\t0\t0\tNOCAPS\tNODIGIT\t0\tNOPUNCT\t0\t10\t0\tNUMBER\t0\t0\t<table>\n" +
                "the\tthe\tt\tth\tthe\tthe\te\the\tthe\tthe\tBLOCKIN\tLINEIN\tALIGNEDLEFT\tSAMEFONT\tSAMEFONTSIZE\t0\t0\tNOCAPS\tNODIGIT\t0\tNOPUNCT\t1\t10\t0\tNUMBER\t0\t0\t<table>\n" +
                "South\tsouth\tS\tSo\tSou\tSout\th\tth\tuth\touth\tBLOCKIN\tLINEIN\tALIGNEDLEFT\tSAMEFONT\tSAMEFONTSIZE\t0\t0\tINITCAP\tNODIGIT\t0\tNOPUNCT\t1\t10\t0\tNUMBER\t0\t0\t<table>\n" +
                "Asian\tasian\tA\tAs\tAsi\tAsia\tn\tan\tian\tsian\tBLOCKIN\tLINEIN\tALIGNEDLEFT\tSAMEFONT\tSAMEFONTSIZE\t0\t0\tINITCAP\tNODIGIT\t0\tNOPUNCT\t1\t10\t0\tNUMBER\t0\t0\t<table>\n" +
                "Clinical\tclinical\tC\tCl\tCli\tClin\tl\tal\tcal\tical\tBLOCKIN\tLINEIN\tALIGNEDLEFT\tSAMEFONT\tSAMEFONTSIZE\t0\t0\tINITCAP\tNODIGIT\t0\tNOPUNCT\t1\t10\t0\tNUMBER\t0\t0\t<table>\n" +
                "Toxicology\ttoxicology\tT\tTo\tTox\tToxi\ty\tgy\togy\tlogy\tBLOCKIN\tLINEIN\tALIGNEDLEFT\tSAMEFONT\tSAMEFONTSIZE\t0\t0\tINITCAP\tNODIGIT\t0\tNOPUNCT\t1\t10\t0\tNUMBER\t0\t0\t<table>\n" +
                "Research\tresearch\tR\tRe\tRes\tRese\th\tch\trch\tarch\tBLOCKIN\tLINEIN\tALIGNEDLEFT\tSAMEFONT\tSAMEFONTSIZE\t0\t0\tINITCAP\tNODIGIT\t0\tNOPUNCT\t2\t10\t0\tNUMBER\t0\t0\t<table>\n" +
                "Collaboration\tcollaboration\tC\tCo\tCol\tColl\tn\ton\tion\ttion\tBLOCKIN\tLINEIN\tALIGNEDLEFT\tSAMEFONT\tSAMEFONTSIZE\t0\t0\tINITCAP\tNODIGIT\t0\tNOPUNCT\t2\t10\t0\tNUMBER\t0\t0\t<table>\n" +
                ",\t,\t,\t,\t,\t,\t,\t,\t,\t,\tBLOCKIN\tLINEIN\tALIGNEDLEFT\tSAMEFONT\tSAMEFONTSIZE\t0\t0\tALLCAP\tNODIGIT\t1\tCOMMA\t3\t10\t0\tNUMBER\t0\t0\t<table>\n" +
                "which\twhich\tw\twh\twhi\twhic\th\tch\tich\thich\tBLOCKIN\tLINEIN\tALIGNEDLEFT\tSAMEFONT\tSAMEFONTSIZE\t0\t0\tNOCAPS\tNODIGIT\t0\tNOPUNCT\t3\t10\t0\tNUMBER\t0\t0\t<table>\n" +
                "is\tis\ti\tis\tis\tis\ts\tis\tis\tis\tBLOCKIN\tLINEIN\tALIGNEDLEFT\tSAMEFONT\tSAMEFONTSIZE\t0\t0\tNOCAPS\tNODIGIT\t0\tNOPUNCT\t3\t10\t0\tNUMBER\t0\t0\t<table>\n" +
                "funded\tfunded\tf\tfu\tfun\tfund\td\ted\tded\tnded\tBLOCKIN\tLINEIN\tALIGNEDLEFT\tSAMEFONT\tSAMEFONTSIZE\t0\t0\tNOCAPS\tNODIGIT\t0\tNOPUNCT\t3\t10\t0\tNUMBER\t0\t0\t<table>\n" +
                "by\tby\tb\tby\tby\tby\ty\tby\tby\tby\tBLOCKIN\tLINEIN\tALIGNEDLEFT\tSAMEFONT\tSAMEFONTSIZE\t0\t0\tNOCAPS\tNODIGIT\t0\tNOPUNCT\t3\t10\t0\tNUMBER\t0\t0\t<table>\n" +
                "The\tthe\tT\tTh\tThe\tThe\te\the\tThe\tThe\tBLOCKIN\tLINEIN\tALIGNEDLEFT\tSAMEFONT\tSAMEFONTSIZE\t0\t0\tINITCAP\tNODIGIT\t0\tNOPUNCT\t3\t10\t0\tNUMBER\t0\t0\t<table>\n" +
                "Wellcome\twellcome\tW\tWe\tWel\tWell\te\tme\tome\tcome\tBLOCKIN\tLINEIN\tALIGNEDLEFT\tSAMEFONT\tSAMEFONTSIZE\t0\t0\tINITCAP\tNODIGIT\t0\tNOPUNCT\t4\t10\t0\tNUMBER\t0\t0\t<table>\n" +
                "Trust\ttrust\tT\tTr\tTru\tTrus\tt\tst\tust\trust\tBLOCKIN\tLINEIN\tALIGNEDLEFT\tSAMEFONT\tSAMEFONTSIZE\t0\t0\tINITCAP\tNODIGIT\t0\tNOPUNCT\t4\t10\t0\tNUMBER\t0\t0\t<table>\n" +
                "/\t/\t/\t/\t/\t/\t/\t/\t/\t/\tBLOCKIN\tLINEEND\tALIGNEDLEFT\tSAMEFONT\tSAMEFONTSIZE\t0\t0\tALLCAP\tNODIGIT\t1\tNOPUNCT\t4\t10\t0\tNUMBER\t0\t0\t<table>\n" +
                "National\tnational\tN\tNa\tNat\tNati\tl\tal\tnal\tonal\tBLOCKIN\tLINESTART\tALIGNEDLEFT\tSAMEFONT\tSAMEFONTSIZE\t0\t0\tINITCAP\tNODIGIT\t0\tNOPUNCT\t4\t10\t0\tNUMBER\t0\t0\t<table>\n" +
                "Health\thealth\tH\tHe\tHea\tHeal\th\tth\tlth\talth\tBLOCKIN\tLINEIN\tALIGNEDLEFT\tSAMEFONT\tSAMEFONTSIZE\t0\t0\tINITCAP\tNODIGIT\t0\tNOPUNCT\t5\t10\t0\tNUMBER\t0\t0\t<table>\n" +
                "and\tand\ta\tan\tand\tand\td\tnd\tand\tand\tBLOCKIN\tLINEIN\tALIGNEDLEFT\tSAMEFONT\tSAMEFONTSIZE\t0\t0\tNOCAPS\tNODIGIT\t0\tNOPUNCT\t5\t10\t0\tNUMBER\t0\t0\t<table>\n" +
                "Medical\tmedical\tM\tMe\tMed\tMedi\tl\tal\tcal\tical\tBLOCKIN\tLINEIN\tALIGNEDLEFT\tSAMEFONT\tSAMEFONTSIZE\t0\t0\tINITCAP\tNODIGIT\t0\tNOPUNCT\t5\t10\t0\tNUMBER\t0\t0\t<table>\n" +
                "Research\tresearch\tR\tRe\tRes\tRese\th\tch\trch\tarch\tBLOCKIN\tLINEIN\tALIGNEDLEFT\tSAMEFONT\tSAMEFONTSIZE\t0\t0\tINITCAP\tNODIGIT\t0\tNOPUNCT\t5\t10\t0\tNUMBER\t0\t0\t<table>\n" +
                "Council\tcouncil\tC\tCo\tCou\tCoun\tl\til\tcil\tncil\tBLOCKIN\tLINEIN\tALIGNEDLEFT\tSAMEFONT\tSAMEFONTSIZE\t0\t0\tINITCAP\tNODIGIT\t0\tNOPUNCT\t6\t10\t0\tNUMBER\t0\t0\t<table>\n" +
                "International\tinternational\tI\tIn\tInt\tInte\tl\tal\tnal\tonal\tBLOCKIN\tLINEIN\tALIGNEDLEFT\tSAMEFONT\tSAMEFONTSIZE\t0\t0\tINITCAP\tNODIGIT\t0\tNOPUNCT\t6\t10\t0\tNUMBER\t0\t0\t<table>\n" +
                "Collaborative\tcollaborative\tC\tCo\tCol\tColl\te\tve\tive\ttive\tBLOCKIN\tLINEIN\tALIGNEDLEFT\tSAMEFONT\tSAMEFONTSIZE\t0\t0\tINITCAP\tNODIGIT\t0\tNOPUNCT\t6\t10\t0\tNUMBER\t0\t0\t<table>\n" +
                "Research\tresearch\tR\tRe\tRes\tRese\th\tch\trch\tarch\tBLOCKIN\tLINEIN\tALIGNEDLEFT\tSAMEFONT\tSAMEFONTSIZE\t0\t0\tINITCAP\tNODIGIT\t0\tNOPUNCT\t7\t10\t0\tNUMBER\t0\t0\t<table>\n" +
                "Grant\tgrant\tG\tGr\tGra\tGran\tt\tnt\tant\trant\tBLOCKIN\tLINEIN\tALIGNEDLEFT\tSAMEFONT\tSAMEFONTSIZE\t0\t0\tINITCAP\tNODIGIT\t0\tNOPUNCT\t7\t10\t0\tNUMBER\t0\t0\t<table>\n" +
                "GR071669MA\tgr071669ma\tG\tGR\tGR0\tGR07\tA\tMA\t9MA\t69MA\tBLOCKIN\tLINEIN\tALIGNEDLEFT\tSAMEFONT\tSAMEFONTSIZE\t0\t0\tALLCAP\tCONTAINSDIGITS\t0\tNOPUNCT\t8\t10\t0\tNUMBER\t0\t0\t<table>\n" +
                ".\t.\t.\t.\t.\t.\t.\t.\t.\t.\tBLOCKIN\tLINEIN\tALIGNEDLEFT\tSAMEFONT\tSAMEFONTSIZE\t0\t0\tALLCAP\tNODIGIT\t1\tDOT\t8\t10\t0\tNUMBER\t0\t0\t<table>\n" +
                "The\tthe\tT\tTh\tThe\tThe\te\the\tThe\tThe\tBLOCKIN\tLINEIN\tALIGNEDLEFT\tSAMEFONT\tSAMEFONTSIZE\t0\t0\tINITCAP\tNODIGIT\t0\tNOPUNCT\t8\t10\t0\tNUMBER\t0\t0\t<table>\n" +
                "funding\tfunding\tf\tfu\tfun\tfund\tg\tng\ting\tding\tBLOCKIN\tLINEIN\tALIGNEDLEFT\tSAMEFONT\tSAMEFONTSIZE\t0\t0\tNOCAPS\tNODIGIT\t0\tNOPUNCT\t8\t10\t0\tNUMBER\t0\t0\t<table>\n" +
                "bodies\tbodies\tb\tbo\tbod\tbodi\ts\tes\ties\tdies\tBLOCKIN\tLINEIN\tALIGNEDLEFT\tSAMEFONT\tSAMEFONTSIZE\t0\t0\tNOCAPS\tNODIGIT\t0\tNOPUNCT\t8\t10\t0\tNUMBER\t0\t0\t<table>\n" +
                "had\thad\th\tha\thad\thad\td\tad\thad\thad\tBLOCKIN\tLINEIN\tALIGNEDLEFT\tSAMEFONT\tSAMEFONTSIZE\t0\t0\tNOCAPS\tNODIGIT\t0\tNOPUNCT\t9\t10\t0\tNUMBER\t0\t0\t<table>\n" +
                "no\tno\tn\tno\tno\tno\to\tno\tno\tno\tBLOCKIN\tLINEEND\tALIGNEDLEFT\tSAMEFONT\tSAMEFONTSIZE\t0\t0\tNOCAPS\tNODIGIT\t0\tNOPUNCT\t9\t10\t0\tNUMBER\t0\t0\t<table>\n" +
                "role\trole\tr\tro\trol\trole\te\tle\tole\trole\tBLOCKIN\tLINESTART\tALIGNEDLEFT\tSAMEFONT\tSAMEFONTSIZE\t0\t0\tNOCAPS\tNODIGIT\t0\tNOPUNCT\t9\t10\t0\tNUMBER\t0\t0\t<table>\n" +
                "in\tin\ti\tin\tin\tin\tn\tin\tin\tin\tBLOCKIN\tLINEIN\tALIGNEDLEFT\tSAMEFONT\tSAMEFONTSIZE\t0\t0\tNOCAPS\tNODIGIT\t0\tNOPUNCT\t9\t10\t0\tNUMBER\t0\t0\t<table>\n" +
                "analyzing\tanalyzing\ta\tan\tana\tanal\tg\tng\ting\tzing\tBLOCKIN\tLINEIN\tALIGNEDLEFT\tSAMEFONT\tSAMEFONTSIZE\t0\t0\tNOCAPS\tNODIGIT\t0\tNOPUNCT\t9\t10\t0\tNUMBER\t0\t0\t<table>\n" +
                "or\tor\to\tor\tor\tor\tr\tor\tor\tor\tBLOCKIN\tLINEIN\tALIGNEDLEFT\tSAMEFONT\tSAMEFONTSIZE\t0\t0\tNOCAPS\tNODIGIT\t0\tNOPUNCT\t10\t10\t0\tNUMBER\t0\t0\t<table>\n" +
                "interpreting\tinterpreting\ti\tin\tint\tinte\tg\tng\ting\tting\tBLOCKIN\tLINEIN\tALIGNEDLEFT\tSAMEFONT\tSAMEFONTSIZE\t0\t0\tNOCAPS\tNODIGIT\t0\tNOPUNCT\t10\t10\t0\tNUMBER\t0\t0\t<table>\n" +
                "the\tthe\tt\tth\tthe\tthe\te\the\tthe\tthe\tBLOCKIN\tLINEIN\tALIGNEDLEFT\tSAMEFONT\tSAMEFONTSIZE\t0\t0\tNOCAPS\tNODIGIT\t0\tNOPUNCT\t10\t10\t0\tNUMBER\t0\t0\t<table>\n" +
                "data\tdata\td\tda\tdat\tdata\ta\tta\tata\tdata\tBLOCKIN\tLINEIN\tALIGNEDLEFT\tSAMEFONT\tSAMEFONTSIZE\t0\t0\tNOCAPS\tNODIGIT\t0\tNOPUNCT\t10\t10\t0\tNUMBER\t0\t0\t<table>\n" +
                "or\tor\to\tor\tor\tor\tr\tor\tor\tor\tBLOCKIN\tLINEIN\tALIGNEDLEFT\tSAMEFONT\tSAMEFONTSIZE\t0\t0\tNOCAPS\tNODIGIT\t0\tNOPUNCT\t11\t10\t0\tNUMBER\t0\t0\t<table>\n" +
                "writing\twriting\tw\twr\twri\twrit\tg\tng\ting\tting\tBLOCKIN\tLINEIN\tALIGNEDLEFT\tSAMEFONT\tSAMEFONTSIZE\t0\t0\tNOCAPS\tNODIGIT\t0\tNOPUNCT\t11\t10\t0\tNUMBER\t0\t0\t<table>\n" +
                "the\tthe\tt\tth\tthe\tthe\te\the\tthe\tthe\tBLOCKIN\tLINEIN\tALIGNEDLEFT\tSAMEFONT\tSAMEFONTSIZE\t0\t0\tNOCAPS\tNODIGIT\t0\tNOPUNCT\t11\t10\t0\tNUMBER\t0\t0\t<table>\n" +
                "article\tarticle\ta\tar\tart\tarti\te\tle\tcle\ticle\tBLOCKIN\tLINEIN\tALIGNEDLEFT\tSAMEFONT\tSAMEFONTSIZE\t0\t0\tNOCAPS\tNODIGIT\t0\tNOPUNCT\t11\t10\t0\tNUMBER\t0\t0\t<table>\n" +
                ".\t.\t.\t.\t.\t.\t.\t.\t.\t.\tBLOCKIN\tLINEIN\tALIGNEDLEFT\tSAMEFONT\tSAMEFONTSIZE\t0\t0\tALLCAP\tNODIGIT\t1\tDOT\t11\t10\t0\tNUMBER\t0\t0\t<table>"

        val postprocessed = LabelUtils.postProcessFullTextLabeledText(resultWithTables)

        assertThat(
            Arrays.stream(StringUtils.split(postprocessed, "\n"))
                .filter { l -> l.endsWith("<table>") }
                .count(), `is`(0L)
        )

        assertThat(
            Arrays.stream(StringUtils.split(postprocessed, "\n"))
                .filter { l -> l.endsWith("<paragraph>") }
                .count(), `is`(
                Arrays.stream(StringUtils.split(resultWithTables, "\n"))
                    .filter { l -> l.endsWith("<table>") }
                    .count())
        )
    }

//    fun testAdjustInvalidSequenceOfStartLabels() {
//        val inputStream = javaClass.getResourceAsStream("bodyResults-sample.1.txt")
//        val bodyResult = inputStream?.bufferedReader().use { it.readText() }
//
//        val postProcessed = LabelUtils.postProcessFullTextLabeledText(bodyResult)
//    }

    @Test
    fun testPostProcessFulltextFixInvalidTableOrFigure_noChangeNeeded_shouldReturnSameTableOrFigureSequence() {
        val bodyResult =
            "B\tb\tB\tB\tB\tB\tB\tB\tB\tB\tBLOCKIN\tLINEIN\tLINEINDENT\tSAMEFONT\tHIGHERFONT\t0\t0\tALLCAP\tNODIGIT\t1\tNOPUNCT\t8\t11\t0\tNUMBER\t0\t0\t<paragraph>\n" +
                ".\t.\t.\t.\t.\t.\t.\t.\t.\t.\tBLOCKEND\tLINEEND\tLINEINDENT\tSAMEFONT\tSAMEFONTSIZE\t0\t0\tALLCAP\tNODIGIT\t1\tDOT\t8\t11\t0\tNUMBER\t0\t0\t<paragraph>\n" +
                "014306\t014306\t0\t01\t014\t0143\t6\t06\t306\t4306\tBLOCKSTART\tLINESTART\tLINEINDENT\tSAMEFONT\tLOWERFONT\t0\t0\tNOCAPS\tALLDIGIT\t0\tNOPUNCT\t8\t11\t0\tNUMBER\t0\t0\tI-<paragraph>\n" +
                "-\t-\t-\t-\t-\t-\t-\t-\t-\t-\tBLOCKIN\tLINEIN\tLINEINDENT\tSAMEFONT\tSAMEFONTSIZE\t0\t0\tALLCAP\tNODIGIT\t1\tHYPHEN\t8\t11\t0\tNUMBER\t0\t0\t<paragraph>\n" +
                "4\t4\t4\t4\t4\t4\t4\t4\t4\t4\tBLOCKEND\tLINEEND\tLINEINDENT\tSAMEFONT\tSAMEFONTSIZE\t0\t0\tNOCAPS\tALLDIGIT\t1\tNOPUNCT\t8\t11\t0\tNUMBER\t1\t0\t<paragraph>\n" +
                "FIG\tfig\tF\tFI\tFIG\tFIG\tG\tIG\tFIG\tFIG\tBLOCKSTART\tLINESTART\tLINEINDENT\tSAMEFONT\tSAMEFONTSIZE\t0\t0\tALLCAP\tNODIGIT\t0\tNOPUNCT\t8\t3\t0\tNUMBER\t0\t0\t<paragraph>\n" +
                ".\t.\t.\t.\t.\t.\t.\t.\t.\t.\tBLOCKIN\tLINEIN\tLINEINDENT\tSAMEFONT\tSAMEFONTSIZE\t0\t0\tALLCAP\tNODIGIT\t1\tDOT\t8\t3\t0\tNUMBER\t0\t0\t<paragraph>\n" +
                "1\t1\t1\t1\t1\t1\t1\t1\t1\t1\tBLOCKIN\tLINEIN\tLINEINDENT\tSAMEFONT\tSAMEFONTSIZE\t0\t0\tNOCAPS\tALLDIGIT\t1\tNOPUNCT\t8\t3\t0\tNUMBER\t1\t0\tI-<figure_marker>\n" +
                ".\t.\t.\t.\t.\t.\t.\t.\t.\t.\tBLOCKIN\tLINEIN\tLINEINDENT\tSAMEFONT\tSAMEFONTSIZE\t0\t0\tALLCAP\tNODIGIT\t1\tDOT\t8\t3\t0\tNUMBER\t0\t0\tI-<paragraph>\n" +
                "The\tthe\tT\tTh\tThe\tThe\te\the\tThe\tThe\tBLOCKIN\tLINEIN\tLINEINDENT\tSAMEFONT\tSAMEFONTSIZE\t0\t0\tINITCAP\tNODIGIT\t0\tNOPUNCT\t8\t3\t0\tNUMBER\t0\t0\t<paragraph>\n" +
                "average\taverage\ta\tav\tave\taver\te\tge\tage\trage\tBLOCKIN\tLINEIN\tLINEINDENT\tSAMEFONT\tSAMEFONTSIZE\t0\t0\tNOCAPS\tNODIGIT\t0\tNOPUNCT\t8\t3\t0\tNUMBER\t0\t0\t<paragraph>\n" +
                "distances\tdistances\td\tdi\tdis\tdist\ts\tes\tces\tnces\tBLOCKIN\tLINEIN\tLINEINDENT\tSAMEFONT\tSAMEFONTSIZE\t0\t0\tNOCAPS\tNODIGIT\t0\tNOPUNCT\t8\t3\t0\tNUMBER\t0\t0\t<paragraph>\n" +
                "between\tbetween\tb\tbe\tbet\tbetw\tn\ten\teen\tween\tBLOCKIN\tLINEIN\tLINEINDENT\tSAMEFONT\tSAMEFONTSIZE\t0\t0\tNOCAPS\tNODIGIT\t0\tNOPUNCT\t8\t3\t0\tNUMBER\t0\t0\t<paragraph>\n" +
                "nucleons\tnucleons\tn\tnu\tnuc\tnucl\ts\tns\tons\teons\tBLOCKIN\tLINEIN\tLINEINDENT\tSAMEFONT\tSAMEFONTSIZE\t0\t0\tNOCAPS\tNODIGIT\t0\tNOPUNCT\t8\t3\t0\tNUMBER\t0\t0\t<paragraph>\n" +
                "in\tin\ti\tin\tin\tin\tn\tin\tin\tin\tBLOCKIN\tLINEIN\tLINEINDENT\tSAMEFONT\tSAMEFONTSIZE\t0\t0\tNOCAPS\tNODIGIT\t0\tNOPUNCT\t8\t3\t0\tNUMBER\t0\t0\t<paragraph>\n" +
                "10\t10\t1\t10\t10\t10\t0\t10\t10\t10\tBLOCKIN\tLINEIN\tLINEINDENT\tSAMEFONT\tLOWERFONT\t0\t0\tNOCAPS\tALLDIGIT\t0\tNOPUNCT\t8\t3\t0\tNUMBER\t1\t1\t<paragraph>\n" +
                "Be\tbe\tB\tBe\tBe\tBe\te\tBe\tBe\tBe\tBLOCKIN\tLINEIN\tLINEINDENT\tSAMEFONT\tHIGHERFONT\t0\t0\tINITCAP\tNODIGIT\t0\tNOPUNCT\t8\t3\t0\tNUMBER\t0\t0\t<paragraph>\n" +
                "(\t(\t(\t(\t(\t(\t(\t(\t(\t(\tBLOCKIN\tLINEIN\tLINEINDENT\tSAMEFONT\tSAMEFONTSIZE\t0\t0\tALLCAP\tNODIGIT\t1\tOPENBRACKET\t8\t3\t0\tNUMBER\t0\t0\t<paragraph>\n" +
                "0\t0\t0\t0\t0\t0\t0\t0\t0\t0\tBLOCKIN\tLINEIN\tLINEINDENT\tSAMEFONT\tSAMEFONTSIZE\t0\t0\tNOCAPS\tALLDIGIT\t1\tNOPUNCT\t8\t3\t0\tNUMBER\t0\t0\t<paragraph>\n" +
                "+\t+\t+\t+\t+\t+\t+\t+\t+\t+\tBLOCKIN\tLINEIN\tLINEINDENT\tNEWFONT\tLOWERFONT\t0\t0\tALLCAP\tNODIGIT\t1\tNOPUNCT\t8\t3\t0\tNUMBER\t0\t1\t<paragraph>\n" +
                "1\t1\t1\t1\t1\t1\t1\t1\t1\t1\tBLOCKIN\tLINEIN\tLINEINDENT\tNEWFONT\tHIGHERFONT\t0\t0\tNOCAPS\tALLDIGIT\t1\tNOPUNCT\t8\t3\t0\tNUMBER\t1\t0\t<paragraph>\n" +
                ")\t)\t)\t)\t)\t)\t)\t)\t)\t)\tBLOCKIN\tLINEIN\tLINEINDENT\tSAMEFONT\tSAMEFONTSIZE\t0\t0\tALLCAP\tNODIGIT\t1\tENDBRACKET\t8\t3\t0\tNUMBER\t0\t0\t<paragraph>\n" +
                ",\t,\t,\t,\t,\t,\t,\t,\t,\t,\tBLOCKIN\tLINEEND\tLINEINDENT\tSAMEFONT\tSAMEFONTSIZE\t0\t0\tALLCAP\tNODIGIT\t1\tCOMMA\t8\t3\t0\tNUMBER\t0\t0\t<paragraph>\n" +
                "10\t10\t1\t10\t10\t10\t0\t10\t10\t10\tBLOCKIN\tLINESTART\tALIGNEDLEFT\tSAMEFONT\tLOWERFONT\t0\t0\tNOCAPS\tALLDIGIT\t0\tNOPUNCT\t8\t3\t0\tNUMBER\t1\t1\t<paragraph>\n" +
                "B\tb\tB\tB\tB\tB\tB\tB\tB\tB\tBLOCKIN\tLINEIN\tALIGNEDLEFT\tSAMEFONT\tHIGHERFONT\t0\t0\tALLCAP\tNODIGIT\t1\tNOPUNCT\t8\t3\t0\tNUMBER\t0\t0\t<paragraph>\n" +
                "(\t(\t(\t(\t(\t(\t(\t(\t(\t(\tBLOCKIN\tLINEIN\tALIGNEDLEFT\tSAMEFONT\tSAMEFONTSIZE\t0\t0\tALLCAP\tNODIGIT\t1\tOPENBRACKET\t8\t3\t0\tNUMBER\t0\t0\t<paragraph>\n" +
                "3\t3\t3\t3\t3\t3\t3\t3\t3\t3\tBLOCKIN\tLINEIN\tALIGNEDLEFT\tSAMEFONT\tSAMEFONTSIZE\t0\t0\tNOCAPS\tALLDIGIT\t1\tNOPUNCT\t8\t3\t0\tNUMBER\t1\t0\t<paragraph>\n" +
                "+\t+\t+\t+\t+\t+\t+\t+\t+\t+\tBLOCKIN\tLINEIN\tALIGNEDLEFT\tNEWFONT\tLOWERFONT\t0\t0\tALLCAP\tNODIGIT\t1\tNOPUNCT\t8\t3\t0\tNUMBER\t0\t1\t<paragraph>\n" +
                "0\t0\t0\t0\t0\t0\t0\t0\t0\t0\tBLOCKIN\tLINEIN\tALIGNEDLEFT\tNEWFONT\tHIGHERFONT\t0\t0\tNOCAPS\tALLDIGIT\t1\tNOPUNCT\t8\t3\t0\tNUMBER\t0\t0\t<paragraph>\n" +
                ")\t)\t)\t)\t)\t)\t)\t)\t)\t)\tBLOCKIN\tLINEIN\tALIGNEDLEFT\tSAMEFONT\tSAMEFONTSIZE\t0\t0\tALLCAP\tNODIGIT\t1\tENDBRACKET\t8\t3\t0\tNUMBER\t0\t0\t<paragraph>\n" +
                ",\t,\t,\t,\t,\t,\t,\t,\t,\t,\tBLOCKIN\tLINEIN\tALIGNEDLEFT\tSAMEFONT\tSAMEFONTSIZE\t0\t0\tALLCAP\tNODIGIT\t1\tCOMMA\t8\t3\t0\tNUMBER\t0\t0\t<paragraph>\n" +
                "10\t10\t1\t10\t10\t10\t0\t10\t10\t10\tBLOCKIN\tLINEIN\tALIGNEDLEFT\tSAMEFONT\tLOWERFONT\t0\t0\tNOCAPS\tALLDIGIT\t0\tNOPUNCT\t8\t3\t0\tNUMBER\t1\t1\tI-<citation_marker>\n" +
                "B\tb\tB\tB\tB\tB\tB\tB\tB\tB\tBLOCKIN\tLINEIN\tALIGNEDLEFT\tSAMEFONT\tHIGHERFONT\t0\t0\tALLCAP\tNODIGIT\t1\tNOPUNCT\t8\t3\t0\tNUMBER\t0\t0\tI-<paragraph>\n" +
                "(\t(\t(\t(\t(\t(\t(\t(\t(\t(\tBLOCKIN\tLINEIN\tALIGNEDLEFT\tSAMEFONT\tSAMEFONTSIZE\t0\t0\tALLCAP\tNODIGIT\t1\tOPENBRACKET\t8\t3\t0\tNUMBER\t0\t0\t<paragraph>\n" +
                "1\t1\t1\t1\t1\t1\t1\t1\t1\t1\tBLOCKIN\tLINEIN\tALIGNEDLEFT\tSAMEFONT\tSAMEFONTSIZE\t0\t0\tNOCAPS\tALLDIGIT\t1\tNOPUNCT\t8\t3\t0\tNUMBER\t1\t0\t<paragraph>\n" +
                "+\t+\t+\t+\t+\t+\t+\t+\t+\t+\tBLOCKIN\tLINEIN\tALIGNEDLEFT\tNEWFONT\tLOWERFONT\t0\t0\tALLCAP\tNODIGIT\t1\tNOPUNCT\t8\t3\t0\tNUMBER\t0\t1\t<paragraph>\n" +
                "0\t0\t0\t0\t0\t0\t0\t0\t0\t0\tBLOCKIN\tLINEIN\tALIGNEDLEFT\tNEWFONT\tHIGHERFONT\t0\t0\tNOCAPS\tALLDIGIT\t1\tNOPUNCT\t8\t3\t0\tNUMBER\t0\t0\t<paragraph>\n" +
                ")\t)\t)\t)\t)\t)\t)\t)\t)\t)\tBLOCKIN\tLINEIN\tALIGNEDLEFT\tSAMEFONT\tSAMEFONTSIZE\t0\t0\tALLCAP\tNODIGIT\t1\tENDBRACKET\t8\t3\t0\tNUMBER\t0\t0\t<paragraph>\n" +
                ",\t,\t,\t,\t,\t,\t,\t,\t,\t,\tBLOCKIN\tLINEIN\tALIGNEDLEFT\tSAMEFONT\tSAMEFONTSIZE\t0\t0\tALLCAP\tNODIGIT\t1\tCOMMA\t8\t3\t0\tNUMBER\t0\t0\t<paragraph>\n" +
                "and\tand\ta\tan\tand\tand\td\tnd\tand\tand\tBLOCKIN\tLINEIN\tALIGNEDLEFT\tSAMEFONT\tSAMEFONTSIZE\t0\t0\tNOCAPS\tNODIGIT\t0\tNOPUNCT\t8\t3\t0\tNUMBER\t0\t0\t<paragraph>\n" +
                "10\t10\t1\t10\t10\t10\t0\t10\t10\t10\tBLOCKIN\tLINEIN\tALIGNEDLEFT\tSAMEFONT\tLOWERFONT\t0\t0\tNOCAPS\tALLDIGIT\t0\tNOPUNCT\t8\t3\t0\tNUMBER\t1\t1\t<paragraph>\n" +
                "C\tc\tC\tC\tC\tC\tC\tC\tC\tC\tBLOCKIN\tLINEIN\tALIGNEDLEFT\tSAMEFONT\tHIGHERFONT\t0\t0\tALLCAP\tNODIGIT\t1\tNOPUNCT\t8\t3\t0\tNUMBER\t0\t0\t<paragraph>\n" +
                "(\t(\t(\t(\t(\t(\t(\t(\t(\t(\tBLOCKIN\tLINEIN\tALIGNEDLEFT\tSAMEFONT\tSAMEFONTSIZE\t0\t0\tALLCAP\tNODIGIT\t1\tOPENBRACKET\t8\t3\t0\tNUMBER\t0\t0\t<paragraph>\n" +
                "0\t0\t0\t0\t0\t0\t0\t0\t0\t0\tBLOCKIN\tLINEIN\tALIGNEDLEFT\tSAMEFONT\tSAMEFONTSIZE\t0\t0\tNOCAPS\tALLDIGIT\t1\tNOPUNCT\t8\t3\t0\tNUMBER\t0\t0\t<paragraph>\n" +
                "+\t+\t+\t+\t+\t+\t+\t+\t+\t+\tBLOCKIN\tLINEIN\tALIGNEDLEFT\tNEWFONT\tLOWERFONT\t0\t0\tALLCAP\tNODIGIT\t1\tNOPUNCT\t8\t3\t0\tNUMBER\t0\t1\t<paragraph>\n" +
                "1\t1\t1\t1\t1\t1\t1\t1\t1\t1\tBLOCKIN\tLINEIN\tALIGNEDLEFT\tNEWFONT\tHIGHERFONT\t0\t0\tNOCAPS\tALLDIGIT\t1\tNOPUNCT\t8\t3\t0\tNUMBER\t1\t0\t<paragraph>\n" +
                ")\t)\t)\t)\t)\t)\t)\t)\t)\t)\tBLOCKIN\tLINEIN\tALIGNEDLEFT\tSAMEFONT\tSAMEFONTSIZE\t0\t0\tALLCAP\tNODIGIT\t1\tENDBRACKET\t8\t3\t0\tNUMBER\t0\t0\t<paragraph>\n" +
                "states\tstates\ts\tst\tsta\tstat\ts\tes\ttes\tates\tBLOCKIN\tLINEIN\tALIGNEDLEFT\tSAMEFONT\tSAMEFONTSIZE\t0\t0\tNOCAPS\tNODIGIT\t0\tNOPUNCT\t8\t3\t0\tNUMBER\t0\t0\t<paragraph>\n" +
                ".\t.\t.\t.\t.\t.\t.\t.\t.\t.\tBLOCKIN\tLINEIN\tALIGNEDLEFT\tSAMEFONT\tSAMEFONTSIZE\t0\t0\tALLCAP\tNODIGIT\t1\tDOT\t8\t3\t0\tNUMBER\t0\t0\t<paragraph>\n" +
                "The\tthe\tT\tTh\tThe\tThe\te\the\tThe\tThe\tBLOCKIN\tLINEIN\tALIGNEDLEFT\tSAMEFONT\tSAMEFONTSIZE\t0\t0\tINITCAP\tNODIGIT\t0\tNOPUNCT\t8\t3\t0\tNUMBER\t0\t0\t<paragraph>\n" +
                "solid\tsolid\ts\tso\tsol\tsoli\td\tid\tlid\tolid\tBLOCKIN\tLINEIN\tALIGNEDLEFT\tSAMEFONT\tSAMEFONTSIZE\t0\t0\tNOCAPS\tNODIGIT\t0\tNOPUNCT\t8\t3\t0\tNUMBER\t0\t0\t<paragraph>\n" +
                "lines\tlines\tl\tli\tlin\tline\ts\tes\tnes\tines\tBLOCKIN\tLINEIN\tALIGNEDLEFT\tSAMEFONT\tSAMEFONTSIZE\t0\t0\tNOCAPS\tNODIGIT\t0\tNOPUNCT\t8\t3\t0\tNUMBER\t0\t0\t<paragraph>\n" +
                "denote\tdenote\td\tde\tden\tdeno\te\tte\tote\tnote\tBLOCKIN\tLINEIN\tALIGNEDLEFT\tSAMEFONT\tSAMEFONTSIZE\t0\t0\tNOCAPS\tNODIGIT\t0\tNOPUNCT\t8\t3\t0\tNUMBER\t0\t0\t<paragraph>\n" +
                "the\tthe\tt\tth\tthe\tthe\te\the\tthe\tthe\tBLOCKIN\tLINEEND\tALIGNEDLEFT\tSAMEFONT\tSAMEFONTSIZE\t0\t0\tNOCAPS\tNODIGIT\t0\tNOPUNCT\t8\t3\t0\tNUMBER\t0\t0\t<paragraph>\n" +
                "average\taverage\ta\tav\tave\taver\te\tge\tage\trage\tBLOCKIN\tLINESTART\tALIGNEDLEFT\tSAMEFONT\tSAMEFONTSIZE\t0\t0\tNOCAPS\tNODIGIT\t0\tNOPUNCT\t8\t3\t0\tNUMBER\t0\t0\t<paragraph>\n" +
                "distances\tdistances\td\tdi\tdis\tdist\ts\tes\tces\tnces\tBLOCKIN\tLINEIN\tALIGNEDLEFT\tSAMEFONT\tSAMEFONTSIZE\t0\t0\tNOCAPS\tNODIGIT\t0\tNOPUNCT\t8\t3\t0\tNUMBER\t0\t0\t<paragraph>\n" +
                "r\tr\tr\tr\tr\tr\tr\tr\tr\tr\tBLOCKIN\tLINEIN\tALIGNEDLEFT\tNEWFONT\tSAMEFONTSIZE\t0\t1\tNOCAPS\tNODIGIT\t1\tNOPUNCT\t8\t3\t0\tNUMBER\t0\t0\t<paragraph>\n" +
                "N\tn\tN\tN\tN\tN\tN\tN\tN\tN\tBLOCKIN\tLINEIN\tALIGNEDLEFT\tSAMEFONT\tLOWERFONT\t0\t1\tALLCAP\tNODIGIT\t1\tNOPUNCT\t8\t3\t0\tNUMBER\t0\t0\t<paragraph>\n" +
                ",\t,\t,\t,\t,\t,\t,\t,\t,\t,\tBLOCKIN\tLINEIN\tALIGNEDLEFT\tSAMEFONT\tSAMEFONTSIZE\t0\t1\tALLCAP\tNODIGIT\t1\tCOMMA\t8\t3\t0\tNUMBER\t0\t0\t<paragraph>\n" +
                "N\tn\tN\tN\tN\tN\tN\tN\tN\tN\tBLOCKIN\tLINEIN\tALIGNEDLEFT\tSAMEFONT\tSAMEFONTSIZE\t0\t1\tALLCAP\tNODIGIT\t1\tNOPUNCT\t8\t3\t0\tNUMBER\t0\t0\t<paragraph>\n" +
                "between\tbetween\tb\tbe\tbet\tbetw\tn\ten\teen\tween\tBLOCKIN\tLINEIN\tALIGNEDLEFT\tNEWFONT\tHIGHERFONT\t0\t0\tNOCAPS\tNODIGIT\t0\tNOPUNCT\t8\t3\t0\tNUMBER\t0\t0\t<paragraph>\n" +
                "two\ttwo\tt\ttw\ttwo\ttwo\to\two\ttwo\ttwo\tBLOCKIN\tLINEIN\tALIGNEDLEFT\tSAMEFONT\tSAMEFONTSIZE\t0\t0\tNOCAPS\tNODIGIT\t0\tNOPUNCT\t8\t3\t0\tNUMBER\t0\t0\t<paragraph>\n" +
                "valence\tvalence\tv\tva\tval\tvale\te\tce\tnce\tence\tBLOCKIN\tLINEIN\tALIGNEDLEFT\tSAMEFONT\tSAMEFONTSIZE\t0\t0\tNOCAPS\tNODIGIT\t0\tNOPUNCT\t8\t3\t0\tNUMBER\t0\t0\t<paragraph>\n" +
                "nucleons\tnucleons\tn\tnu\tnuc\tnucl\ts\tns\tons\teons\tBLOCKIN\tLINEIN\tALIGNEDLEFT\tSAMEFONT\tSAMEFONTSIZE\t0\t0\tNOCAPS\tNODIGIT\t0\tNOPUNCT\t8\t3\t0\tNUMBER\t0\t0\t<paragraph>\n" +
                ".\t.\t.\t.\t.\t.\t.\t.\t.\t.\tBLOCKIN\tLINEIN\tALIGNEDLEFT\tSAMEFONT\tSAMEFONTSIZE\t0\t0\tALLCAP\tNODIGIT\t1\tDOT\t8\t3\t0\tNUMBER\t0\t0\t<paragraph>\n" +
                "The\tthe\tT\tTh\tThe\tThe\te\the\tThe\tThe\tBLOCKIN\tLINEIN\tALIGNEDLEFT\tSAMEFONT\tSAMEFONTSIZE\t0\t0\tINITCAP\tNODIGIT\t0\tNOPUNCT\t8\t3\t0\tNUMBER\t0\t0\t<paragraph>\n"

        val postProcessed = LabelUtils.postProcessFulltextFixInvalidTableOrFigure(bodyResult)

        assertThat(postProcessed, `is`(bodyResult))
    }

    @Test
    fun testPostProcessFulltextFixInvalidTableOrFigure_singleChangeNeeded_shouldCorrectTheTableOrFigureSequence() {
        val bodyResult =
            "of\tof\to\tof\tof\tof\tf\tof\tof\tof\tBLOCKIN\tLINEIN\tALIGNEDLEFT\tSAMEFONT\tSAMEFONTSIZE\t0\t0\tNOCAPS\tNODIGIT\t0\tNOPUNCT\t9\t5\t0\tNUMBER\t0\t0\t<paragraph>\n" +
                "10\t10\t1\t10\t10\t10\t0\t10\t10\t10\tBLOCKIN\tLINEIN\tALIGNEDLEFT\tSAMEFONT\tLOWERFONT\t0\t0\tNOCAPS\tALLDIGIT\t0\tNOPUNCT\t9\t5\t0\tNUMBER\t1\t1\tI-<citation_marker>\n" +
                "B\tb\tB\tB\tB\tB\tB\tB\tB\tB\tBLOCKIN\tLINEIN\tALIGNEDLEFT\tSAMEFONT\tHIGHERFONT\t0\t0\tALLCAP\tNODIGIT\t1\tNOPUNCT\t9\t5\t0\tNUMBER\t0\t0\tI-<figure>\n" +
                "and\tand\ta\tan\tand\tand\td\tnd\tand\tand\tBLOCKIN\tLINEIN\tALIGNEDLEFT\tSAMEFONT\tSAMEFONTSIZE\t0\t0\tNOCAPS\tNODIGIT\t0\tNOPUNCT\t9\t5\t0\tNUMBER\t0\t0\tI-<figure>\n" +
                "the\tthe\tt\tth\tthe\tthe\te\the\tthe\tthe\tBLOCKIN\tLINEEND\tALIGNEDLEFT\tSAMEFONT\tSAMEFONTSIZE\t0\t0\tNOCAPS\tNODIGIT\t0\tNOPUNCT\t9\t5\t0\tNUMBER\t0\t0\tI-<figure>\n" +
                "1\t1\t1\t1\t1\t1\t1\t1\t1\t1\tBLOCKIN\tLINESTART\tALIGNEDLEFT\tSAMEFONT\tSAMEFONTSIZE\t0\t0\tNOCAPS\tALLDIGIT\t1\tNOPUNCT\t9\t5\t0\tNUMBER\t1\t0\t<figure>\n" +
                "+\t+\t+\t+\t+\t+\t+\t+\t+\t+\tBLOCKIN\tLINEEND\tALIGNEDLEFT\tNEWFONT\tLOWERFONT\t0\t0\tALLCAP\tNODIGIT\t1\tNOPUNCT\t9\t5\t0\tNUMBER\t0\t1\t<figure>\n" +
                "1\t1\t1\t1\t1\t1\t1\t1\t1\t1\tBLOCKIN\tLINESTART\tLINEINDENT\tNEWFONT\tSAMEFONTSIZE\t0\t0\tNOCAPS\tALLDIGIT\t1\tNOPUNCT\t9\t5\t0\tNUMBER\t1\t0\t<figure>\n" +
                "0\t0\t0\t0\t0\t0\t0\t0\t0\t0\tBLOCKIN\tLINEIN\tLINEINDENT\tSAMEFONT\tHIGHERFONT\t0\t0\tNOCAPS\tALLDIGIT\t1\tNOPUNCT\t9\t5\t0\tNUMBER\t0\t0\t<figure>\n" +
                "state\tstate\ts\tst\tsta\tstat\te\tte\tate\ttate\tBLOCKIN\tLINEIN\tLINEINDENT\tSAMEFONT\tSAMEFONTSIZE\t0\t0\tNOCAPS\tNODIGIT\t0\tNOPUNCT\t9\t5\t0\tNUMBER\t0\t0\t<figure>\n" +
                "of\tof\to\tof\tof\tof\tf\tof\tof\tof\tBLOCKIN\tLINEIN\tLINEINDENT\tSAMEFONT\tSAMEFONTSIZE\t0\t0\tNOCAPS\tNODIGIT\t0\tNOPUNCT\t9\t5\t0\tNUMBER\t0\t0\t<figure>\n" +
                "10\t10\t1\t10\t10\t10\t0\t10\t10\t10\tBLOCKIN\tLINEIN\tLINEINDENT\tSAMEFONT\tLOWERFONT\t0\t0\tNOCAPS\tALLDIGIT\t0\tNOPUNCT\t9\t5\t0\tNUMBER\t1\t1\t<figure>\n" +
                "B\tb\tB\tB\tB\tB\tB\tB\tB\tB\tBLOCKIN\tLINEIN\tLINEINDENT\tSAMEFONT\tHIGHERFONT\t0\t0\tALLCAP\tNODIGIT\t1\tNOPUNCT\t9\t5\t0\tNUMBER\t0\t0\t<figure>\n" +
                ".\t.\t.\t.\t.\t.\t.\t.\t.\t.\tBLOCKIN\tLINEIN\tLINEINDENT\tSAMEFONT\tSAMEFONTSIZE\t0\t0\tALLCAP\tNODIGIT\t1\tDOT\t9\t5\t0\tNUMBER\t0\t0\t<figure>\n" +
                "The\tthe\tT\tTh\tThe\tThe\te\the\tThe\tThe\tBLOCKIN\tLINEIN\tLINEINDENT\tSAMEFONT\tSAMEFONTSIZE\t0\t0\tINITCAP\tNODIGIT\t0\tNOPUNCT\t9\t5\t0\tNUMBER\t0\t0\t<figure>\n" +
                "panels\tpanels\tp\tpa\tpan\tpane\ts\tls\tels\tnels\tBLOCKIN\tLINEIN\tLINEINDENT\tSAMEFONT\tSAMEFONTSIZE\t0\t0\tNOCAPS\tNODIGIT\t0\tNOPUNCT\t9\t5\t0\tNUMBER\t0\t0\t<figure>\n" +
                "(\t(\t(\t(\t(\t(\t(\t(\t(\t(\tBLOCKIN\tLINEIN\tLINEINDENT\tSAMEFONT\tSAMEFONTSIZE\t0\t0\tALLCAP\tNODIGIT\t1\tOPENBRACKET\t9\t5\t0\tNUMBER\t0\t0\t<figure>\n" +
                "a\ta\ta\ta\ta\ta\ta\ta\ta\ta\tBLOCKIN\tLINEIN\tLINEINDENT\tSAMEFONT\tSAMEFONTSIZE\t0\t0\tNOCAPS\tNODIGIT\t1\tNOPUNCT\t9\t5\t0\tNUMBER\t0\t0\t<figure>\n" +
                ")\t)\t)\t)\t)\t)\t)\t)\t)\t)\tBLOCKIN\tLINEIN\tLINEINDENT\tSAMEFONT\tSAMEFONTSIZE\t0\t0\tALLCAP\tNODIGIT\t1\tENDBRACKET\t9\t5\t0\tNUMBER\t0\t0\t<figure>\n" +
                "are\tare\ta\tar\tare\tare\te\tre\tare\tare\tBLOCKIN\tLINEIN\tLINEINDENT\tSAMEFONT\tSAMEFONTSIZE\t0\t0\tNOCAPS\tNODIGIT\t0\tNOPUNCT\t9\t5\t0\tNUMBER\t0\t0\t<figure>\n" +
                "calculated\tcalculated\tc\tca\tcal\tcalc\td\ted\tted\tated\tBLOCKIN\tLINEIN\tLINEINDENT\tSAMEFONT\tSAMEFONTSIZE\t0\t0\tNOCAPS\tNODIGIT\t0\tNOPUNCT\t9\t5\t0\tNUMBER\t0\t0\t<figure>\n"


        val postProcessed = LabelUtils.postProcessFulltextFixInvalidTableOrFigure(bodyResult)

        assertThat(postProcessed, not(bodyResult))

        val splitResult =
            Arrays.stream(postProcessed.split("\n".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray())
                .map<List<String>> { l: String ->
                    Arrays.stream(
                        l.split("\t".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                    )
                        .collect(Collectors.toList())
                }
                .collect(Collectors.toList())

        val countStartingFigure = splitResult.stream()
            .map { l: List<String> -> l.last() }
            .filter { l: String -> l.equals("I-<figure>") }
            .count()

        assertThat(countStartingFigure, `is`(1))
    }

    @Test
    fun testPostProcessFulltextFixInvalidTableOrFigure_MultipleChangeNeeded_shouldCorrectTheTableOrFigureSequence() {
        val bodyResult =
            "of\tof\to\tof\tof\tof\tf\tof\tof\tof\tBLOCKIN\tLINEIN\tALIGNEDLEFT\tSAMEFONT\tSAMEFONTSIZE\t0\t0\tNOCAPS\tNODIGIT\t0\tNOPUNCT\t9\t5\t0\tNUMBER\t0\t0\t<paragraph>\n" +
                "10\t10\t1\t10\t10\t10\t0\t10\t10\t10\tBLOCKIN\tLINEIN\tALIGNEDLEFT\tSAMEFONT\tLOWERFONT\t0\t0\tNOCAPS\tALLDIGIT\t0\tNOPUNCT\t9\t5\t0\tNUMBER\t1\t1\tI-<citation_marker>\n" +
                "B\tb\tB\tB\tB\tB\tB\tB\tB\tB\tBLOCKIN\tLINEIN\tALIGNEDLEFT\tSAMEFONT\tHIGHERFONT\t0\t0\tALLCAP\tNODIGIT\t1\tNOPUNCT\t9\t5\t0\tNUMBER\t0\t0\tI-<figure>\n" +
                "and\tand\ta\tan\tand\tand\td\tnd\tand\tand\tBLOCKIN\tLINEIN\tALIGNEDLEFT\tSAMEFONT\tSAMEFONTSIZE\t0\t0\tNOCAPS\tNODIGIT\t0\tNOPUNCT\t9\t5\t0\tNUMBER\t0\t0\tI-<figure>\n" +
                "the\tthe\tt\tth\tthe\tthe\te\the\tthe\tthe\tBLOCKIN\tLINEEND\tALIGNEDLEFT\tSAMEFONT\tSAMEFONTSIZE\t0\t0\tNOCAPS\tNODIGIT\t0\tNOPUNCT\t9\t5\t0\tNUMBER\t0\t0\tI-<figure>\n" +
                "1\t1\t1\t1\t1\t1\t1\t1\t1\t1\tBLOCKIN\tLINESTART\tALIGNEDLEFT\tSAMEFONT\tSAMEFONTSIZE\t0\t0\tNOCAPS\tALLDIGIT\t1\tNOPUNCT\t9\t5\t0\tNUMBER\t1\t0\t<figure>\n" +
                "+\t+\t+\t+\t+\t+\t+\t+\t+\t+\tBLOCKIN\tLINEEND\tALIGNEDLEFT\tNEWFONT\tLOWERFONT\t0\t0\tALLCAP\tNODIGIT\t1\tNOPUNCT\t9\t5\t0\tNUMBER\t0\t1\t<figure>\n" +
                "1\t1\t1\t1\t1\t1\t1\t1\t1\t1\tBLOCKIN\tLINESTART\tLINEINDENT\tNEWFONT\tSAMEFONTSIZE\t0\t0\tNOCAPS\tALLDIGIT\t1\tNOPUNCT\t9\t5\t0\tNUMBER\t1\t0\t<figure>\n" +
                "0\t0\t0\t0\t0\t0\t0\t0\t0\t0\tBLOCKIN\tLINEIN\tLINEINDENT\tSAMEFONT\tHIGHERFONT\t0\t0\tNOCAPS\tALLDIGIT\t1\tNOPUNCT\t9\t5\t0\tNUMBER\t0\t0\t<figure>\n" +
                "state\tstate\ts\tst\tsta\tstat\te\tte\tate\ttate\tBLOCKIN\tLINEIN\tLINEINDENT\tSAMEFONT\tSAMEFONTSIZE\t0\t0\tNOCAPS\tNODIGIT\t0\tNOPUNCT\t9\t5\t0\tNUMBER\t0\t0\t<figure>\n" +
                "of\tof\to\tof\tof\tof\tf\tof\tof\tof\tBLOCKIN\tLINEIN\tLINEINDENT\tSAMEFONT\tSAMEFONTSIZE\t0\t0\tNOCAPS\tNODIGIT\t0\tNOPUNCT\t9\t5\t0\tNUMBER\t0\t0\t<figure>\n" +
                "10\t10\t1\t10\t10\t10\t0\t10\t10\t10\tBLOCKIN\tLINEIN\tLINEINDENT\tSAMEFONT\tLOWERFONT\t0\t0\tNOCAPS\tALLDIGIT\t0\tNOPUNCT\t9\t5\t0\tNUMBER\t1\t1\t<figure>\n" +
                "B\tb\tB\tB\tB\tB\tB\tB\tB\tB\tBLOCKIN\tLINEIN\tLINEINDENT\tSAMEFONT\tHIGHERFONT\t0\t0\tALLCAP\tNODIGIT\t1\tNOPUNCT\t9\t5\t0\tNUMBER\t0\t0\t<figure>\n" +
                ".\t.\t.\t.\t.\t.\t.\t.\t.\t.\tBLOCKIN\tLINEIN\tLINEINDENT\tSAMEFONT\tSAMEFONTSIZE\t0\t0\tALLCAP\tNODIGIT\t1\tDOT\t9\t5\t0\tNUMBER\t0\t0\t<figure>\n" +
                "The\tthe\tT\tTh\tThe\tThe\te\the\tThe\tThe\tBLOCKIN\tLINEIN\tLINEINDENT\tSAMEFONT\tSAMEFONTSIZE\t0\t0\tINITCAP\tNODIGIT\t0\tNOPUNCT\t9\t5\t0\tNUMBER\t0\t0\tI-<figure>\n" +
                "panels\tpanels\tp\tpa\tpan\tpane\ts\tls\tels\tnels\tBLOCKIN\tLINEIN\tLINEINDENT\tSAMEFONT\tSAMEFONTSIZE\t0\t0\tNOCAPS\tNODIGIT\t0\tNOPUNCT\t9\t5\t0\tNUMBER\t0\t0\tI-<figure>\n" +
                "(\t(\t(\t(\t(\t(\t(\t(\t(\t(\tBLOCKIN\tLINEIN\tLINEINDENT\tSAMEFONT\tSAMEFONTSIZE\t0\t0\tALLCAP\tNODIGIT\t1\tOPENBRACKET\t9\t5\t0\tNUMBER\t0\t0\tI-<figure>\n" +
                "a\ta\ta\ta\ta\ta\ta\ta\ta\ta\tBLOCKIN\tLINEIN\tLINEINDENT\tSAMEFONT\tSAMEFONTSIZE\t0\t0\tNOCAPS\tNODIGIT\t1\tNOPUNCT\t9\t5\t0\tNUMBER\t0\t0\t<figure>\n" +
                ")\t)\t)\t)\t)\t)\t)\t)\t)\t)\tBLOCKIN\tLINEIN\tLINEINDENT\tSAMEFONT\tSAMEFONTSIZE\t0\t0\tALLCAP\tNODIGIT\t1\tENDBRACKET\t9\t5\t0\tNUMBER\t0\t0\t<figure>\n" +
                "are\tare\ta\tar\tare\tare\te\tre\tare\tare\tBLOCKIN\tLINEIN\tLINEINDENT\tSAMEFONT\tSAMEFONTSIZE\t0\t0\tNOCAPS\tNODIGIT\t0\tNOPUNCT\t9\t5\t0\tNUMBER\t0\t0\t<figure>\n" +
                "calculated\tcalculated\tc\tca\tcal\tcalc\td\ted\tted\tated\tBLOCKIN\tLINEIN\tLINEINDENT\tSAMEFONT\tSAMEFONTSIZE\t0\t0\tNOCAPS\tNODIGIT\t0\tNOPUNCT\t9\t5\t0\tNUMBER\t0\t0\t<figure>\n" +
                "calculated\tcalculated\tc\tca\tcal\tcalc\td\ted\tted\tated\tBLOCKIN\tLINEIN\tLINEINDENT\tSAMEFONT\tSAMEFONTSIZE\t0\t0\tNOCAPS\tNODIGIT\t0\tNOPUNCT\t9\t5\t0\tNUMBER\t0\t0\tI-<table>\n" +
                "calculated\tcalculated\tc\tca\tcal\tcalc\td\ted\tted\tated\tBLOCKIN\tLINEIN\tLINEINDENT\tSAMEFONT\tSAMEFONTSIZE\t0\t0\tNOCAPS\tNODIGIT\t0\tNOPUNCT\t9\t5\t0\tNUMBER\t0\t0\tI-<table>\n" +
                "calculated\tcalculated\tc\tca\tcal\tcalc\td\ted\tted\tated\tBLOCKIN\tLINEIN\tLINEINDENT\tSAMEFONT\tSAMEFONTSIZE\t0\t0\tNOCAPS\tNODIGIT\t0\tNOPUNCT\t9\t5\t0\tNUMBER\t0\t0\tI-<table>\n" +
                "calculated\tcalculated\tc\tca\tcal\tcalc\td\ted\tted\tated\tBLOCKIN\tLINEIN\tLINEINDENT\tSAMEFONT\tSAMEFONTSIZE\t0\t0\tNOCAPS\tNODIGIT\t0\tNOPUNCT\t9\t5\t0\tNUMBER\t0\t0\t<table>\n" +
                "calculated\tcalculated\tc\tca\tcal\tcalc\td\ted\tted\tated\tBLOCKIN\tLINEIN\tLINEINDENT\tSAMEFONT\tSAMEFONTSIZE\t0\t0\tNOCAPS\tNODIGIT\t0\tNOPUNCT\t9\t5\t0\tNUMBER\t0\t0\t<table>\n" +
                "calculated\tcalculated\tc\tca\tcal\tcalc\td\ted\tted\tated\tBLOCKIN\tLINEIN\tLINEINDENT\tSAMEFONT\tSAMEFONTSIZE\t0\t0\tNOCAPS\tNODIGIT\t0\tNOPUNCT\t9\t5\t0\tNUMBER\t0\t0\t<table>\n" +
                "calculated\tcalculated\tc\tca\tcal\tcalc\td\ted\tted\tated\tBLOCKIN\tLINEIN\tLINEINDENT\tSAMEFONT\tSAMEFONTSIZE\t0\t0\tNOCAPS\tNODIGIT\t0\tNOPUNCT\t9\t5\t0\tNUMBER\t0\t0\t<table>\n" +
                "calculated\tcalculated\tc\tca\tcal\tcalc\td\ted\tted\tated\tBLOCKIN\tLINEIN\tLINEINDENT\tSAMEFONT\tSAMEFONTSIZE\t0\t0\tNOCAPS\tNODIGIT\t0\tNOPUNCT\t9\t5\t0\tNUMBER\t0\t0\t<table>\n" +
                "calculated\tcalculated\tc\tca\tcal\tcalc\td\ted\tted\tated\tBLOCKIN\tLINEIN\tLINEINDENT\tSAMEFONT\tSAMEFONTSIZE\t0\t0\tNOCAPS\tNODIGIT\t0\tNOPUNCT\t9\t5\t0\tNUMBER\t0\t0\t<table>\n" +
                "calculated\tcalculated\tc\tca\tcal\tcalc\td\ted\tted\tated\tBLOCKIN\tLINEIN\tLINEINDENT\tSAMEFONT\tSAMEFONTSIZE\t0\t0\tNOCAPS\tNODIGIT\t0\tNOPUNCT\t9\t5\t0\tNUMBER\t0\t0\t<table>\n" +
                "calculated\tcalculated\tc\tca\tcal\tcalc\td\ted\tted\tated\tBLOCKIN\tLINEIN\tLINEINDENT\tSAMEFONT\tSAMEFONTSIZE\t0\t0\tNOCAPS\tNODIGIT\t0\tNOPUNCT\t9\t5\t0\tNUMBER\t0\t0\t<table>\n"


        val postProcessed = LabelUtils.postProcessFulltextFixInvalidTableOrFigure(bodyResult)

        assertThat(postProcessed, not(bodyResult))

        val splitResult =
            Arrays.stream<String>(postProcessed.split("\n".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray())
                .map<List<String>> { l: String ->
                    Arrays.stream<String>(
                        l.split("\t".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                    )
                        .collect(Collectors.toList<String>())
                }
                .collect(Collectors.toList<List<String>>())

        val countStartingFigure = splitResult.stream()
            .map { l: List<String> -> l.last() }
            .filter { l: String -> l.equals("I-<figure>") }
            .count()

        assertThat(countStartingFigure, `is`(2))

        val countStartingTables = splitResult.stream()
            .map { l: List<String> -> l.last() }
            .filter { l: String -> l.equals("I-<table>") }
            .count()

        assertThat(countStartingTables, `is`(1))

    }

    @Test
    fun testPostProcessFulltextCorrectSequencesWithoutInitialToken_shouldChangeAbstractLabelInAvailabilityLabel() {
        val resultHeader = "Data\tdata\tD\tDa\tDat\tData\ta\tta\tata\tData\tBLOCKSTART\tLINESTART\tALIGNEDLEFT\tNEWFONT\tSAMEFONTSIZE\t1\t0\tINITCAP\tNODIGIT\t0\t0\t1\t0\t0\t0\t0\t0\tNOPUNCT\t0\t0\t1\t0\t<other>\n" +
            "Availability\tavailability\tA\tAv\tAva\tAvai\ty\tty\tity\tlity\tBLOCKIN\tLINEIN\tALIGNEDLEFT\tSAMEFONT\tSAMEFONTSIZE\t1\t0\tINITCAP\tNODIGIT\t0\t0\t1\t0\t0\t0\t0\t0\tNOPUNCT\t0\t0\t1\t0\t<other>\n" +
            "Statement\tstatement\tS\tSt\tSta\tStat\tt\tnt\tent\tment\tBLOCKIN\tLINEIN\tALIGNEDLEFT\tSAMEFONT\tSAMEFONTSIZE\t1\t0\tINITCAP\tNODIGIT\t0\t0\t1\t0\t0\t0\t0\t0\tNOPUNCT\t0\t0\t1\t0\t<other>\n" +
            ":\t:\t:\t:\t:\t:\t:\t:\t:\t:\tBLOCKIN\tLINEIN\tALIGNEDLEFT\tSAMEFONT\tSAMEFONTSIZE\t1\t0\tALLCAP\tNODIGIT\t1\t0\t0\t0\t0\t0\t0\t0\tPUNCT\t0\t0\t1\t0\t<other>\n" +
            "The\tthe\tT\tTh\tThe\tThe\te\the\tThe\tThe\tBLOCKIN\tLINEIN\tALIGNEDLEFT\tNEWFONT\tSAMEFONTSIZE\t0\t0\tINITCAP\tNODIGIT\t0\t0\t1\t0\t0\t0\t0\t0\tNOPUNCT\t0\t0\t1\t0\tI-<availability>\n" +
            "raw\traw\tr\tra\traw\traw\tw\taw\traw\traw\tBLOCKIN\tLINEIN\tALIGNEDLEFT\tSAMEFONT\tSAMEFONTSIZE\t0\t0\tNOCAPS\tNODIGIT\t0\t0\t1\t0\t0\t0\t0\t0\tNOPUNCT\t0\t0\t1\t0\t<availability>\n" +
            "sequencing\tsequencing\ts\tse\tseq\tsequ\tg\tng\ting\tcing\tBLOCKIN\tLINEEND\tALIGNEDLEFT\tSAMEFONT\tSAMEFONTSIZE\t0\t0\tNOCAPS\tNODIGIT\t0\t0\t0\t0\t0\t0\t0\t0\tNOPUNCT\t0\t0\t1\t0\t<availability>\n" +
            "reads\treads\tr\tre\trea\tread\ts\tds\tads\teads\tBLOCKIN\tLINESTART\tALIGNEDLEFT\tSAMEFONT\tSAMEFONTSIZE\t0\t0\tNOCAPS\tNODIGIT\t0\t0\t1\t0\t0\t0\t0\t0\tNOPUNCT\t0\t0\t1\t0\t<availability>\n" +
            "for\tfor\tf\tfo\tfor\tfor\tr\tor\tfor\tfor\tBLOCKIN\tLINEIN\tALIGNEDLEFT\tSAMEFONT\tSAMEFONTSIZE\t0\t0\tNOCAPS\tNODIGIT\t0\t0\t1\t0\t0\t0\t0\t0\tNOPUNCT\t0\t0\t1\t0\t<availability>\n" +
            "the\tthe\tt\tth\tthe\tthe\te\the\tthe\tthe\tBLOCKIN\tLINEIN\tALIGNEDLEFT\tSAMEFONT\tSAMEFONTSIZE\t0\t0\tNOCAPS\tNODIGIT\t0\t0\t1\t0\t0\t0\t0\t0\tNOPUNCT\t0\t0\t1\t0\t<availability>\n" +
            "metagenomic\tmetagenomic\tm\tme\tmet\tmeta\tc\tic\tmic\tomic\tBLOCKIN\tLINEIN\tALIGNEDLEFT\tSAMEFONT\tSAMEFONTSIZE\t0\t0\tNOCAPS\tNODIGIT\t0\t0\t0\t0\t0\t0\t0\t0\tNOPUNCT\t0\t0\t1\t0\t<availability>\n" +
            "samples\tsamples\ts\tsa\tsam\tsamp\ts\tes\tles\tples\tBLOCKIN\tLINEIN\tALIGNEDLEFT\tSAMEFONT\tSAMEFONTSIZE\t0\t0\tNOCAPS\tNODIGIT\t0\t1\t1\t0\t0\t1\t0\t0\tNOPUNCT\t0\t0\t1\t0\t<availability>\n" +
            "used\tused\tu\tus\tuse\tused\td\ted\tsed\tused\tBLOCKIN\tLINEIN\tALIGNEDLEFT\tSAMEFONT\tSAMEFONTSIZE\t0\t0\tNOCAPS\tNODIGIT\t0\t0\t1\t0\t0\t1\t0\t0\tNOPUNCT\t0\t0\t1\t0\t<availability>\n" +
            "in\tin\ti\tin\tin\tin\tn\tin\tin\tin\tBLOCKIN\tLINEIN\tALIGNEDLEFT\tSAMEFONT\tSAMEFONTSIZE\t0\t0\tNOCAPS\tNODIGIT\t0\t1\t1\t0\t0\t1\t0\t0\tNOPUNCT\t0\t0\t1\t0\t<availability>\n" +
            "this\tthis\tt\tth\tthi\tthis\ts\tis\this\tthis\tBLOCKIN\tLINEEND\tALIGNEDLEFT\tSAMEFONT\tSAMEFONTSIZE\t0\t0\tNOCAPS\tNODIGIT\t0\t0\t1\t0\t0\t1\t0\t0\tNOPUNCT\t0\t0\t1\t0\t<availability>\n" +
            "study\tstudy\ts\tst\tstu\tstud\ty\tdy\tudy\ttudy\tBLOCKIN\tLINESTART\tALIGNEDLEFT\tSAMEFONT\tSAMEFONTSIZE\t0\t0\tNOCAPS\tNODIGIT\t0\t0\t1\t0\t0\t0\t0\t0\tNOPUNCT\t0\t0\t1\t0\t<availability>\n" +
            "were\twere\tw\twe\twer\twere\te\tre\tere\twere\tBLOCKIN\tLINEIN\tALIGNEDLEFT\tSAMEFONT\tSAMEFONTSIZE\t0\t0\tNOCAPS\tNODIGIT\t0\t0\t1\t0\t0\t0\t0\t0\tNOPUNCT\t0\t0\t1\t0\t<availability>\n" +
            "downloaded\tdownloaded\td\tdo\tdow\tdown\td\ted\tded\taded\tBLOCKIN\tLINEIN\tALIGNEDLEFT\tSAMEFONT\tSAMEFONTSIZE\t0\t0\tNOCAPS\tNODIGIT\t0\t0\t0\t0\t0\t0\t0\t0\tNOPUNCT\t0\t0\t1\t0\t<availability>\n" +
            "from\tfrom\tf\tfr\tfro\tfrom\tm\tom\trom\tfrom\tBLOCKIN\tLINEIN\tALIGNEDLEFT\tSAMEFONT\tSAMEFONTSIZE\t0\t0\tNOCAPS\tNODIGIT\t0\t0\t1\t0\t0\t0\t0\t0\tNOPUNCT\t0\t0\t1\t0\t<availability>\n" +
            "public\tpublic\tp\tpu\tpub\tpubl\tc\tic\tlic\tblic\tBLOCKIN\tLINEIN\tALIGNEDLEFT\tSAMEFONT\tSAMEFONTSIZE\t0\t0\tNOCAPS\tNODIGIT\t0\t0\t1\t0\t0\t0\t0\t0\tNOPUNCT\t0\t0\t1\t0\t<availability>\n" +
            "repositories\trepositories\tr\tre\trep\trepo\ts\tes\ties\tries\tBLOCKIN\tLINEEND\tALIGNEDLEFT\tSAMEFONT\tSAMEFONTSIZE\t0\t0\tNOCAPS\tNODIGIT\t0\t0\t1\t0\t0\t0\t0\t0\tNOPUNCT\t0\t0\t1\t0\t<availability>\n" +
            "listed\tlisted\tl\tli\tlis\tlist\td\ted\tted\tsted\tBLOCKIN\tLINESTART\tALIGNEDLEFT\tSAMEFONT\tSAMEFONTSIZE\t0\t0\tNOCAPS\tNODIGIT\t0\t0\t1\t0\t0\t0\t0\t0\tNOPUNCT\t0\t0\t1\t0\t<availability>\n" +
            "in\tin\ti\tin\tin\tin\tn\tin\tin\tin\tBLOCKIN\tLINEIN\tALIGNEDLEFT\tSAMEFONT\tSAMEFONTSIZE\t0\t0\tNOCAPS\tNODIGIT\t0\t1\t1\t0\t0\t1\t0\t0\tNOPUNCT\t0\t0\t1\t0\t<availability>\n" +
            "the\tthe\tt\tth\tthe\tthe\te\the\tthe\tthe\tBLOCKIN\tLINEIN\tALIGNEDLEFT\tSAMEFONT\tSAMEFONTSIZE\t0\t0\tNOCAPS\tNODIGIT\t0\t0\t1\t0\t0\t0\t0\t0\tNOPUNCT\t0\t0\t1\t0\t<availability>\n" +
            "following\tfollowing\tf\tfo\tfol\tfoll\tg\tng\ting\twing\tBLOCKIN\tLINEIN\tALIGNEDLEFT\tSAMEFONT\tSAMEFONTSIZE\t0\t0\tNOCAPS\tNODIGIT\t0\t0\t1\t0\t0\t0\t0\t0\tNOPUNCT\t0\t0\t1\t0\t<availability>\n" +
            "publications\tpublications\tp\tpu\tpub\tpubl\ts\tns\tons\tions\tBLOCKIN\tLINEIN\tALIGNEDLEFT\tSAMEFONT\tSAMEFONTSIZE\t0\t0\tNOCAPS\tNODIGIT\t0\t0\t1\t0\t0\t0\t0\t0\tNOPUNCT\t0\t0\t1\t0\t<availability>\n" +
            ":\t:\t:\t:\t:\t:\t:\t:\t:\t:\tBLOCKIN\tLINEIN\tALIGNEDLEFT\tSAMEFONT\tSAMEFONTSIZE\t0\t0\tALLCAP\tNODIGIT\t1\t0\t0\t0\t0\t0\t0\t0\tPUNCT\t0\t0\t1\t0\t<availability>\n" +
            "10\t10\t1\t10\t10\t10\t0\t10\t10\t10\tBLOCKIN\tLINEIN\tALIGNEDLEFT\tSAMEFONT\tSAMEFONTSIZE\t0\t0\tNOCAPS\tALLDIGIT\t0\t0\t0\t0\t0\t1\t0\t0\tNOPUNCT\t0\t0\t1\t0\t<abstract>\n" +
            ".\t.\t.\t.\t.\t.\t.\t.\t.\t.\tBLOCKIN\tLINEIN\tALIGNEDLEFT\tSAMEFONT\tSAMEFONTSIZE\t0\t0\tALLCAP\tNODIGIT\t1\t0\t0\t0\t0\t0\t0\t0\tDOT\t0\t0\t1\t0\t<abstract>\n" +
            "1038\t1038\t1\t10\t103\t1038\t8\t38\t038\t1038\tBLOCKIN\tLINEIN\tALIGNEDLEFT\tSAMEFONT\tSAMEFONTSIZE\t0\t0\tNOCAPS\tALLDIGIT\t0\t0\t0\t1\t0\t0\t0\t0\tNOPUNCT\t0\t0\t1\t0\t<abstract>\n" +
            "/\t/\t/\t/\t/\t/\t/\t/\t/\t/\tBLOCKIN\tLINEEND\tALIGNEDLEFT\tSAMEFONT\tSAMEFONTSIZE\t0\t0\tALLCAP\tNODIGIT\t1\t0\t0\t0\t0\t0\t0\t0\tNOPUNCT\t0\t0\t1\t0\t<abstract>\n" +
            "nature11209\tnature11209\tn\tna\tnat\tnatu\t9\t09\t209\t1209\tBLOCKIN\tLINESTART\tALIGNEDLEFT\tSAMEFONT\tSAMEFONTSIZE\t0\t0\tNOCAPS\tCONTAINSDIGITS\t0\t0\t0\t1\t0\t0\t0\t0\tNOPUNCT\t0\t0\t1\t0\t<abstract>\n" +
            ",\t,\t,\t,\t,\t,\t,\t,\t,\t,\tBLOCKIN\tLINEIN\tALIGNEDLEFT\tSAMEFONT\tSAMEFONTSIZE\t0\t0\tALLCAP\tNODIGIT\t1\t0\t0\t0\t0\t0\t0\t0\tCOMMA\t0\t0\t1\t0\t<abstract>\n" +
            "10\t10\t1\t10\t10\t10\t0\t10\t10\t10\tBLOCKIN\tLINEIN\tALIGNEDLEFT\tSAMEFONT\tSAMEFONTSIZE\t0\t0\tNOCAPS\tALLDIGIT\t0\t0\t0\t0\t0\t1\t0\t0\tNOPUNCT\t0\t0\t1\t0\t<abstract>\n" +
            ".\t.\t.\t.\t.\t.\t.\t.\t.\t.\tBLOCKIN\tLINEIN\tALIGNEDLEFT\tSAMEFONT\tSAMEFONTSIZE\t0\t0\tALLCAP\tNODIGIT\t1\t0\t0\t0\t0\t0\t0\t0\tDOT\t0\t0\t1\t0\t<abstract>\n" +
            "1038\t1038\t1\t10\t103\t1038\t8\t38\t038\t1038\tBLOCKIN\tLINEIN\tALIGNEDLEFT\tSAMEFONT\tSAMEFONTSIZE\t0\t0\tNOCAPS\tALLDIGIT\t0\t0\t0\t1\t0\t0\t0\t0\tNOPUNCT\t0\t0\t1\t0\t<abstract>\n" +
            "/\t/\t/\t/\t/\t/\t/\t/\t/\t/\tBLOCKIN\tLINEIN\tALIGNEDLEFT\tSAMEFONT\tSAMEFONTSIZE\t0\t0\tALLCAP\tNODIGIT\t1\t0\t0\t0\t0\t0\t0\t0\tNOPUNCT\t0\t0\t1\t0\t<abstract>\n" +
            "nature11450\tnature11450\tn\tna\tnat\tnatu\t0\t50\t450\t1450\tBLOCKIN\tLINEIN\tALIGNEDLEFT\tSAMEFONT\tSAMEFONTSIZE\t0\t0\tNOCAPS\tCONTAINSDIGITS\t0\t0\t0\t1\t0\t0\t0\t0\tNOPUNCT\t0\t0\t1\t0\t<abstract>\n" +
            ",\t,\t,\t,\t,\t,\t,\t,\t,\t,\tBLOCKIN\tLINEIN\tALIGNEDLEFT\tSAMEFONT\tSAMEFONTSIZE\t0\t0\tALLCAP\tNODIGIT\t1\t0\t0\t0\t0\t0\t0\t0\tCOMMA\t0\t0\t1\t0\t<abstract>\n" +
            "10\t10\t1\t10\t10\t10\t0\t10\t10\t10\tBLOCKIN\tLINEIN\tALIGNEDLEFT\tSAMEFONT\tSAMEFONTSIZE\t0\t0\tNOCAPS\tALLDIGIT\t0\t0\t0\t0\t0\t1\t0\t0\tNOPUNCT\t0\t0\t1\t0\t<abstract>\n" +
            ".\t.\t.\t.\t.\t.\t.\t.\t.\t.\tBLOCKIN\tLINEIN\tALIGNEDLEFT\tSAMEFONT\tSAMEFONTSIZE\t0\t0\tALLCAP\tNODIGIT\t1\t0\t0\t0\t0\t0\t0\t0\tDOT\t0\t0\t1\t0\t<abstract>\n" +
            "1016\t1016\t1\t10\t101\t1016\t6\t16\t016\t1016\tBLOCKIN\tLINEIN\tALIGNEDLEFT\tSAMEFONT\tSAMEFONTSIZE\t0\t0\tNOCAPS\tALLDIGIT\t0\t0\t0\t1\t0\t0\t0\t0\tNOPUNCT\t0\t0\t1\t0\t<abstract>\n" +
            "/\t/\t/\t/\t/\t/\t/\t/\t/\t/\tBLOCKIN\tLINEIN\tALIGNEDLEFT\tSAMEFONT\tSAMEFONTSIZE\t0\t0\tALLCAP\tNODIGIT\t1\t0\t0\t0\t0\t0\t0\t0\tNOPUNCT\t0\t0\t1\t0\t<abstract>\n" +
            "j\tj\tj\tj\tj\tj\tj\tj\tj\tj\tBLOCKIN\tLINEIN\tALIGNEDLEFT\tSAMEFONT\tSAMEFONTSIZE\t0\t0\tNOCAPS\tNODIGIT\t1\t0\t0\t0\t0\t1\t0\t0\tNOPUNCT\t0\t0\t1\t0\t<abstract>\n" +
            ".\t.\t.\t.\t.\t.\t.\t.\t.\t.\tBLOCKIN\tLINEEND\tALIGNEDLEFT\tSAMEFONT\tSAMEFONTSIZE\t0\t0\tALLCAP\tNODIGIT\t1\t0\t0\t0\t0\t0\t0\t0\tDOT\t0\t0\t1\t0\t<abstract>\n" +
            "cels\tcels\tc\tce\tcel\tcels\ts\tls\tels\tcels\tBLOCKIN\tLINESTART\tALIGNEDLEFT\tSAMEFONT\tSAMEFONTSIZE\t0\t0\tNOCAPS\tNODIGIT\t0\t0\t0\t0\t0\t0\t0\t0\tNOPUNCT\t0\t0\t1\t0\t<abstract>\n" +
            ".\t.\t.\t.\t.\t.\t.\t.\t.\t.\tBLOCKIN\tLINEIN\tALIGNEDLEFT\tSAMEFONT\tSAMEFONTSIZE\t0\t0\tALLCAP\tNODIGIT\t1\t0\t0\t0\t0\t0\t0\t0\tDOT\t0\t0\t1\t0\t<abstract>\n" +
            "2016\t2016\t2\t20\t201\t2016\t6\t16\t016\t2016\tBLOCKIN\tLINEIN\tALIGNEDLEFT\tSAMEFONT\tSAMEFONTSIZE\t0\t0\tNOCAPS\tALLDIGIT\t0\t0\t0\t1\t0\t0\t0\t0\tNOPUNCT\t0\t0\t1\t0\t<abstract>\n" +
            ".\t.\t.\t.\t.\t.\t.\t.\t.\t.\tBLOCKIN\tLINEIN\tALIGNEDLEFT\tSAMEFONT\tSAMEFONTSIZE\t0\t0\tALLCAP\tNODIGIT\t1\t0\t0\t0\t0\t0\t0\t0\tDOT\t0\t0\t1\t0\t<abstract>\n" +
            "10\t10\t1\t10\t10\t10\t0\t10\t10\t10\tBLOCKIN\tLINEIN\tALIGNEDLEFT\tSAMEFONT\tSAMEFONTSIZE\t0\t0\tNOCAPS\tALLDIGIT\t0\t0\t0\t0\t0\t1\t0\t0\tNOPUNCT\t0\t0\t1\t0\t<abstract>\n" +
            ".\t.\t.\t.\t.\t.\t.\t.\t.\t.\tBLOCKIN\tLINEIN\tALIGNEDLEFT\tSAMEFONT\tSAMEFONTSIZE\t0\t0\tALLCAP\tNODIGIT\t1\t0\t0\t0\t0\t0\t0\t0\tDOT\t0\t0\t1\t0\t<abstract>\n" +
            "004\t004\t0\t00\t004\t004\t4\t04\t004\t004\tBLOCKIN\tLINEIN\tALIGNEDLEFT\tSAMEFONT\tSAMEFONTSIZE\t0\t0\tNOCAPS\tALLDIGIT\t0\t0\t0\t0\t0\t0\t0\t0\tNOPUNCT\t0\t0\t1\t0\t<abstract>\n" +
            ",\t,\t,\t,\t,\t,\t,\t,\t,\t,\tBLOCKIN\tLINEIN\tALIGNEDLEFT\tSAMEFONT\tSAMEFONTSIZE\t0\t0\tALLCAP\tNODIGIT\t1\t0\t0\t0\t0\t0\t0\t0\tCOMMA\t0\t0\t1\t0\t<abstract>\n" +
            "and\tand\ta\tan\tand\tand\td\tnd\tand\tand\tBLOCKIN\tLINEIN\tALIGNEDLEFT\tSAMEFONT\tSAMEFONTSIZE\t0\t0\tNOCAPS\tNODIGIT\t0\t0\t1\t0\t0\t0\t0\t0\tNOPUNCT\t0\t0\t1\t0\t<abstract>\n" +
            "10\t10\t1\t10\t10\t10\t0\t10\t10\t10\tBLOCKIN\tLINEIN\tALIGNEDLEFT\tSAMEFONT\tSAMEFONTSIZE\t0\t0\tNOCAPS\tALLDIGIT\t0\t0\t0\t0\t0\t1\t0\t0\tNOPUNCT\t0\t0\t1\t0\t<abstract>\n" +
            ".\t.\t.\t.\t.\t.\t.\t.\t.\t.\tBLOCKIN\tLINEIN\tALIGNEDLEFT\tSAMEFONT\tSAMEFONTSIZE\t0\t0\tALLCAP\tNODIGIT\t1\t0\t0\t0\t0\t0\t0\t0\tDOT\t0\t0\t1\t0\t<abstract>\n" +
            "1101\t1101\t1\t11\t110\t1101\t1\t01\t101\t1101\tBLOCKIN\tLINEIN\tALIGNEDLEFT\tSAMEFONT\tSAMEFONTSIZE\t0\t0\tNOCAPS\tALLDIGIT\t0\t0\t0\t1\t0\t0\t0\t0\tNOPUNCT\t0\t0\t1\t0\t<abstract>\n" +
            "/\t/\t/\t/\t/\t/\t/\t/\t/\t/\tBLOCKIN\tLINEIN\tALIGNEDLEFT\tSAMEFONT\tSAMEFONTSIZE\t0\t0\tALLCAP\tNODIGIT\t1\t0\t0\t0\t0\t0\t0\t0\tNOPUNCT\t0\t0\t1\t0\t<abstract>\n" +
            "gr\tgr\tg\tgr\tgr\tgr\tr\tgr\tgr\tgr\tBLOCKIN\tLINEIN\tALIGNEDLEFT\tSAMEFONT\tSAMEFONTSIZE\t0\t0\tNOCAPS\tNODIGIT\t0\t0\t0\t0\t0\t0\t0\t0\tNOPUNCT\t0\t0\t1\t0\t<abstract>\n" +
            ".\t.\t.\t.\t.\t.\t.\t.\t.\t.\tBLOCKIN\tLINEIN\tALIGNEDLEFT\tSAMEFONT\tSAMEFONTSIZE\t0\t0\tALLCAP\tNODIGIT\t1\t0\t0\t0\t0\t0\t0\t0\tDOT\t0\t0\t1\t0\t<abstract>\n" +
            "233940\t233940\t2\t23\t233\t2339\t0\t40\t940\t3940\tBLOCKIN\tLINEIN\tALIGNEDLEFT\tSAMEFONT\tSAMEFONTSIZE\t0\t0\tNOCAPS\tALLDIGIT\t0\t0\t0\t1\t0\t0\t0\t0\tNOPUNCT\t0\t0\t1\t0\t<abstract>\n" +
            ".\t.\t.\t.\t.\t.\t.\t.\t.\t.\tBLOCKIN\tLINEIN\tALIGNEDLEFT\tSAMEFONT\tSAMEFONTSIZE\t0\t0\tALLCAP\tNODIGIT\t1\t0\t0\t0\t0\t0\t0\t0\tDOT\t0\t0\t1\t0\t<abstract>\n" +
            "117\t117\t1\t11\t117\t117\t7\t17\t117\t117\tBLOCKIN\tLINEIN\tALIGNEDLEFT\tSAMEFONT\tSAMEFONTSIZE\t0\t0\tNOCAPS\tALLDIGIT\t0\t0\t0\t0\t0\t0\t0\t0\tNOPUNCT\t0\t0\t1\t0\t<abstract>\n" +
            ".\t.\t.\t.\t.\t.\t.\t.\t.\t.\tBLOCKIN\tLINEEND\tALIGNEDLEFT\tSAMEFONT\tSAMEFONTSIZE\t0\t0\tALLCAP\tNODIGIT\t1\t0\t0\t0\t0\t0\t0\t0\tDOT\t0\t0\t1\t0\t<abstract>\n" +
            "Data\tdata\tD\tDa\tDat\tData\ta\tta\tata\tData\tBLOCKIN\tLINESTART\tALIGNEDLEFT\tSAMEFONT\tSAMEFONTSIZE\t0\t0\tINITCAP\tNODIGIT\t0\t0\t1\t0\t0\t0\t0\t0\tNOPUNCT\t0\t0\t1\t0\t<abstract>\n" +
            "underlying\tunderlying\tu\tun\tund\tunde\tg\tng\ting\tying\tBLOCKIN\tLINEIN\tALIGNEDLEFT\tSAMEFONT\tSAMEFONTSIZE\t0\t0\tNOCAPS\tNODIGIT\t0\t0\t1\t0\t0\t0\t0\t0\tNOPUNCT\t0\t0\t1\t0\t<abstract>\n" +
            "all\tall\ta\tal\tall\tall\tl\tll\tall\tall\tBLOCKIN\tLINEIN\tALIGNEDLEFT\tSAMEFONT\tSAMEFONTSIZE\t0\t0\tNOCAPS\tNODIGIT\t0\t0\t1\t0\t0\t1\t0\t0\tNOPUNCT\t0\t0\t1\t0\t<abstract>\n" +
            "figures\tfigures\tf\tfi\tfig\tfigu\ts\tes\tres\tures\tBLOCKIN\tLINEIN\tALIGNEDLEFT\tSAMEFONT\tSAMEFONTSIZE\t0\t0\tNOCAPS\tNODIGIT\t0\t0\t1\t0\t0\t0\t0\t0\tNOPUNCT\t0\t0\t1\t0\t<abstract>\n" +
            ",\t,\t,\t,\t,\t,\t,\t,\t,\t,\tBLOCKIN\tLINEIN\tALIGNEDLEFT\tSAMEFONT\tSAMEFONTSIZE\t0\t0\tALLCAP\tNODIGIT\t1\t0\t0\t0\t0\t0\t0\t0\tCOMMA\t0\t0\t1\t0\t<abstract>\n" +
            "such\tsuch\ts\tsu\tsuc\tsuch\th\tch\tuch\tsuch\tBLOCKIN\tLINEIN\tALIGNEDLEFT\tSAMEFONT\tSAMEFONTSIZE\t0\t0\tNOCAPS\tNODIGIT\t0\t0\t1\t0\t0\t0\t0\t0\tNOPUNCT\t0\t0\t1\t0\t<abstract>\n" +
            "as\tas\ta\tas\tas\tas\ts\tas\tas\tas\tBLOCKIN\tLINEIN\tALIGNEDLEFT\tSAMEFONT\tSAMEFONTSIZE\t0\t0\tNOCAPS\tNODIGIT\t0\t0\t1\t0\t0\t1\t0\t0\tNOPUNCT\t0\t0\t1\t0\t<abstract>\n" +
            "the\tthe\tt\tth\tthe\tthe\te\the\tthe\tthe\tBLOCKIN\tLINEIN\tALIGNEDLEFT\tSAMEFONT\tSAMEFONTSIZE\t0\t0\tNOCAPS\tNODIGIT\t0\t0\t1\t0\t0\t0\t0\t0\tNOPUNCT\t0\t0\t1\t0\t<abstract>\n" +
            "numerical\tnumerical\tn\tnu\tnum\tnume\tl\tal\tcal\tical\tBLOCKIN\tLINEEND\tALIGNEDLEFT\tSAMEFONT\tSAMEFONTSIZE\t0\t0\tNOCAPS\tNODIGIT\t0\t0\t1\t0\t0\t0\t0\t0\tNOPUNCT\t0\t0\t1\t0\t<abstract>\n" +
            "values\tvalues\tv\tva\tval\tvalu\ts\tes\tues\tlues\tBLOCKIN\tLINESTART\tALIGNEDLEFT\tSAMEFONT\tSAMEFONTSIZE\t0\t0\tNOCAPS\tNODIGIT\t0\t0\t1\t0\t0\t0\t0\t0\tNOPUNCT\t0\t0\t1\t0\t<abstract>\n" +
            "of\tof\to\tof\tof\tof\tf\tof\tof\tof\tBLOCKIN\tLINEIN\tALIGNEDLEFT\tSAMEFONT\tSAMEFONTSIZE\t0\t0\tNOCAPS\tNODIGIT\t0\t0\t1\t0\t0\t1\t0\t0\tNOPUNCT\t0\t0\t1\t0\t<abstract>\n" +
            "bar\tbar\tb\tba\tbar\tbar\tr\tar\tbar\tbar\tBLOCKIN\tLINEIN\tALIGNEDLEFT\tSAMEFONT\tSAMEFONTSIZE\t0\t0\tNOCAPS\tNODIGIT\t0\t0\t1\t0\t0\t1\t0\t0\tNOPUNCT\t0\t0\t1\t0\t<abstract>\n" +
            "plots\tplots\tp\tpl\tplo\tplot\ts\tts\tots\tlots\tBLOCKIN\tLINEIN\tALIGNEDLEFT\tSAMEFONT\tSAMEFONTSIZE\t0\t0\tNOCAPS\tNODIGIT\t0\t0\t1\t0\t0\t0\t0\t0\tNOPUNCT\t0\t0\t1\t0\t<abstract>\n" +
            ",\t,\t,\t,\t,\t,\t,\t,\t,\t,\tBLOCKIN\tLINEIN\tALIGNEDLEFT\tSAMEFONT\tSAMEFONTSIZE\t0\t0\tALLCAP\tNODIGIT\t1\t0\t0\t0\t0\t0\t0\t0\tCOMMA\t0\t0\t1\t0\t<abstract>\n" +
            "can\tcan\tc\tca\tcan\tcan\tn\tan\tcan\tcan\tBLOCKIN\tLINEIN\tALIGNEDLEFT\tSAMEFONT\tSAMEFONTSIZE\t0\t0\tNOCAPS\tNODIGIT\t0\t0\t1\t0\t0\t1\t0\t0\tNOPUNCT\t0\t0\t1\t0\t<abstract>\n" +
            "be\tbe\tb\tbe\tbe\tbe\te\tbe\tbe\tbe\tBLOCKIN\tLINEIN\tALIGNEDLEFT\tSAMEFONT\tSAMEFONTSIZE\t0\t0\tNOCAPS\tNODIGIT\t0\t0\t1\t0\t0\t0\t0\t0\tNOPUNCT\t0\t0\t1\t0\t<abstract>\n" +
            "found\tfound\tf\tfo\tfou\tfoun\td\tnd\tund\tound\tBLOCKIN\tLINEIN\tALIGNEDLEFT\tSAMEFONT\tSAMEFONTSIZE\t0\t0\tNOCAPS\tNODIGIT\t0\t0\t1\t0\t0\t0\t0\t0\tNOPUNCT\t0\t0\t1\t0\t<abstract>\n" +
            "in\tin\ti\tin\tin\tin\tn\tin\tin\tin\tBLOCKIN\tLINEIN\tALIGNEDLEFT\tSAMEFONT\tSAMEFONTSIZE\t0\t0\tNOCAPS\tNODIGIT\t0\t1\t1\t0\t0\t1\t0\t0\tNOPUNCT\t0\t0\t1\t0\t<abstract>\n" +
            "10\t10\t1\t10\t10\t10\t0\t10\t10\t10\tBLOCKIN\tLINEIN\tALIGNEDLEFT\tSAMEFONT\tSAMEFONTSIZE\t0\t0\tNOCAPS\tALLDIGIT\t0\t0\t0\t0\t0\t1\t0\t0\tNOPUNCT\t0\t0\t1\t0\t<abstract>\n" +
            ".\t.\t.\t.\t.\t.\t.\t.\t.\t.\tBLOCKIN\tLINEIN\tALIGNEDLEFT\tSAMEFONT\tSAMEFONTSIZE\t0\t0\tALLCAP\tNODIGIT\t1\t0\t0\t0\t0\t0\t0\t0\tDOT\t0\t0\t1\t0\t<abstract>\n" +
            "5281\t5281\t5\t52\t528\t5281\t1\t81\t281\t5281\tBLOCKIN\tLINEIN\tALIGNEDLEFT\tSAMEFONT\tSAMEFONTSIZE\t0\t0\tNOCAPS\tALLDIGIT\t0\t0\t0\t0\t0\t0\t0\t0\tNOPUNCT\t0\t0\t1\t0\t<abstract>\n" +
            "/\t/\t/\t/\t/\t/\t/\t/\t/\t/\tBLOCKIN\tLINEEND\tALIGNEDLEFT\tSAMEFONT\tSAMEFONTSIZE\t0\t0\tALLCAP\tNODIGIT\t1\t0\t0\t0\t0\t0\t0\t0\tNOPUNCT\t0\t0\t1\t0\t<abstract>\n" +
            "zenodo\tzenodo\tz\tze\tzen\tzeno\to\tdo\todo\tnodo\tBLOCKIN\tLINESTART\tALIGNEDLEFT\tSAMEFONT\tSAMEFONTSIZE\t0\t0\tNOCAPS\tNODIGIT\t0\t0\t0\t0\t0\t0\t0\t0\tNOPUNCT\t0\t0\t1\t0\t<abstract>\n" +
            ".\t.\t.\t.\t.\t.\t.\t.\t.\t.\tBLOCKIN\tLINEIN\tALIGNEDLEFT\tSAMEFONT\tSAMEFONTSIZE\t0\t0\tALLCAP\tNODIGIT\t1\t0\t0\t0\t0\t0\t0\t0\tDOT\t0\t0\t1\t0\t<abstract>\n" +
            "10304481\t10304481\t1\t10\t103\t1030\t1\t81\t481\t4481\tBLOCKIN\tLINEIN\tALIGNEDLEFT\tSAMEFONT\tSAMEFONTSIZE\t0\t0\tNOCAPS\tALLDIGIT\t0\t0\t0\t1\t0\t0\t0\t0\tNOPUNCT\t0\t0\t1\t0\t<abstract>\n" +
            ".\t.\t.\t.\t.\t.\t.\t.\t.\t.\tBLOCKIN\tLINEIN\tALIGNEDLEFT\tSAMEFONT\tSAMEFONTSIZE\t0\t0\tALLCAP\tNODIGIT\t1\t0\t0\t0\t0\t0\t0\t0\tDOT\t0\t0\t1\t0\t<abstract>\n" +
            "All\tall\tA\tAl\tAll\tAll\tl\tll\tAll\tAll\tBLOCKIN\tLINEIN\tALIGNEDLEFT\tSAMEFONT\tSAMEFONTSIZE\t0\t0\tINITCAP\tNODIGIT\t0\t0\t1\t0\t0\t1\t0\t0\tNOPUNCT\t0\t0\t1\t0\t<abstract>\n" +
            "other\tother\to\tot\toth\tothe\tr\ter\ther\tther\tBLOCKIN\tLINEIN\tALIGNEDLEFT\tSAMEFONT\tSAMEFONTSIZE\t0\t0\tNOCAPS\tNODIGIT\t0\t0\t1\t0\t0\t0\t0\t0\tNOPUNCT\t0\t0\t1\t0\t<abstract>\n" +
            "metadata\tmetadata\tm\tme\tmet\tmeta\ta\tta\tata\tdata\tBLOCKIN\tLINEIN\tALIGNEDLEFT\tSAMEFONT\tSAMEFONTSIZE\t0\t0\tNOCAPS\tNODIGIT\t0\t0\t0\t0\t0\t0\t0\t0\tNOPUNCT\t0\t0\t1\t0\t<abstract>\n" +
            ",\t,\t,\t,\t,\t,\t,\t,\t,\t,\tBLOCKIN\tLINEIN\tALIGNEDLEFT\tSAMEFONT\tSAMEFONTSIZE\t0\t0\tALLCAP\tNODIGIT\t1\t0\t0\t0\t0\t0\t0\t0\tCOMMA\t0\t0\t1\t0\t<abstract>\n" +
            "as\tas\ta\tas\tas\tas\ts\tas\tas\tas\tBLOCKIN\tLINEIN\tALIGNEDLEFT\tSAMEFONT\tSAMEFONTSIZE\t0\t0\tNOCAPS\tNODIGIT\t0\t0\t1\t0\t0\t1\t0\t0\tNOPUNCT\t0\t0\t1\t0\t<abstract>\n" +
            "well\twell\tw\twe\twel\twell\tl\tll\tell\twell\tBLOCKIN\tLINEIN\tALIGNEDLEFT\tSAMEFONT\tSAMEFONTSIZE\t0\t0\tNOCAPS\tNODIGIT\t0\t0\t1\t0\t0\t1\t0\t0\tNOPUNCT\t0\t0\t1\t0\t<abstract>\n" +
            "as\tas\ta\tas\tas\tas\ts\tas\tas\tas\tBLOCKIN\tLINEEND\tALIGNEDLEFT\tSAMEFONT\tSAMEFONTSIZE\t0\t0\tNOCAPS\tNODIGIT\t0\t0\t1\t0\t0\t1\t0\t0\tNOPUNCT\t0\t0\t1\t0\t<abstract>\n" +
            "the\tthe\tt\tth\tthe\tthe\te\the\tthe\tthe\tBLOCKIN\tLINESTART\tALIGNEDLEFT\tSAMEFONT\tSAMEFONTSIZE\t0\t0\tNOCAPS\tNODIGIT\t0\t0\t1\t0\t0\t0\t0\t0\tNOPUNCT\t0\t0\t1\t0\t<abstract>\n" +
            "source\tsource\ts\tso\tsou\tsour\te\tce\trce\turce\tBLOCKIN\tLINEIN\tALIGNEDLEFT\tSAMEFONT\tSAMEFONTSIZE\t0\t0\tNOCAPS\tNODIGIT\t0\t0\t1\t0\t0\t0\t0\t0\tNOPUNCT\t0\t0\t1\t0\t<abstract>\n" +
            "code\tcode\tc\tco\tcod\tcode\te\tde\tode\tcode\tBLOCKIN\tLINEIN\tALIGNEDLEFT\tSAMEFONT\tSAMEFONTSIZE\t0\t0\tNOCAPS\tNODIGIT\t0\t0\t1\t0\t0\t0\t0\t0\tNOPUNCT\t0\t0\t1\t0\t<abstract>\n" +
            "for\tfor\tf\tfo\tfor\tfor\tr\tor\tfor\tfor\tBLOCKIN\tLINEIN\tALIGNEDLEFT\tSAMEFONT\tSAMEFONTSIZE\t0\t0\tNOCAPS\tNODIGIT\t0\t0\t1\t0\t0\t0\t0\t0\tNOPUNCT\t0\t0\t1\t0\t<abstract>\n" +
            "the\tthe\tt\tth\tthe\tthe\te\the\tthe\tthe\tBLOCKIN\tLINEIN\tALIGNEDLEFT\tSAMEFONT\tSAMEFONTSIZE\t0\t0\tNOCAPS\tNODIGIT\t0\t0\t1\t0\t0\t0\t0\t0\tNOPUNCT\t0\t0\t1\t0\t<abstract>\n" +
            "sequencing\tsequencing\ts\tse\tseq\tsequ\tg\tng\ting\tcing\tBLOCKIN\tLINEIN\tALIGNEDLEFT\tSAMEFONT\tSAMEFONTSIZE\t0\t0\tNOCAPS\tNODIGIT\t0\t0\t0\t0\t0\t0\t0\t0\tNOPUNCT\t0\t0\t1\t0\t<abstract>\n" +
            "pipeline\tpipeline\tp\tpi\tpip\tpipe\te\tne\tine\tline\tBLOCKIN\tLINEIN\tALIGNEDLEFT\tSAMEFONT\tSAMEFONTSIZE\t0\t0\tNOCAPS\tNODIGIT\t0\t0\t1\t0\t0\t0\t0\t0\tNOPUNCT\t0\t0\t1\t0\t<abstract>\n" +
            ",\t,\t,\t,\t,\t,\t,\t,\t,\t,\tBLOCKIN\tLINEEND\tALIGNEDLEFT\tSAMEFONT\tSAMEFONTSIZE\t0\t0\tALLCAP\tNODIGIT\t1\t0\t0\t0\t0\t0\t0\t0\tCOMMA\t0\t0\t1\t0\t<abstract>\n" +
            "downstream\tdownstream\td\tdo\tdow\tdown\tm\tam\team\tream\tBLOCKIN\tLINESTART\tALIGNEDLEFT\tSAMEFONT\tSAMEFONTSIZE\t0\t0\tNOCAPS\tNODIGIT\t0\t0\t1\t0\t0\t0\t0\t0\tNOPUNCT\t0\t0\t1\t0\t<abstract>\n" +
            "analyses\tanalyses\ta\tan\tana\tanal\ts\tes\tses\tyses\tBLOCKIN\tLINEIN\tALIGNEDLEFT\tSAMEFONT\tSAMEFONTSIZE\t0\t0\tNOCAPS\tNODIGIT\t0\t0\t1\t0\t0\t0\t0\t0\tNOPUNCT\t0\t0\t1\t0\t<abstract>\n" +
            ",\t,\t,\t,\t,\t,\t,\t,\t,\t,\tBLOCKIN\tLINEIN\tALIGNEDLEFT\tSAMEFONT\tSAMEFONTSIZE\t0\t0\tALLCAP\tNODIGIT\t1\t0\t0\t0\t0\t0\t0\t0\tCOMMA\t0\t0\t1\t0\t<abstract>\n" +
            "and\tand\ta\tan\tand\tand\td\tnd\tand\tand\tBLOCKIN\tLINEIN\tALIGNEDLEFT\tSAMEFONT\tSAMEFONTSIZE\t0\t0\tNOCAPS\tNODIGIT\t0\t0\t1\t0\t0\t0\t0\t0\tNOPUNCT\t0\t0\t1\t0\t<abstract>\n" +
            "figure\tfigure\tf\tfi\tfig\tfigu\te\tre\ture\tgure\tBLOCKIN\tLINEIN\tALIGNEDLEFT\tSAMEFONT\tSAMEFONTSIZE\t0\t0\tNOCAPS\tNODIGIT\t0\t0\t1\t0\t0\t0\t0\t0\tNOPUNCT\t0\t0\t1\t0\t<abstract>\n" +
            "generation\tgeneration\tg\tge\tgen\tgene\tn\ton\tion\ttion\tBLOCKIN\tLINEIN\tALIGNEDLEFT\tSAMEFONT\tSAMEFONTSIZE\t0\t0\tNOCAPS\tNODIGIT\t0\t0\t1\t0\t0\t0\t0\t0\tNOPUNCT\t0\t0\t1\t0\t<abstract>\n" +
            "are\tare\ta\tar\tare\tare\te\tre\tare\tare\tBLOCKIN\tLINEEND\tALIGNEDLEFT\tSAMEFONT\tSAMEFONTSIZE\t0\t0\tNOCAPS\tNODIGIT\t0\t0\t1\t0\t0\t0\t0\t0\tNOPUNCT\t0\t0\t1\t0\t<abstract>\n" +
            "available\tavailable\ta\tav\tava\tavai\te\tle\tble\table\tBLOCKIN\tLINESTART\tALIGNEDLEFT\tSAMEFONT\tSAMEFONTSIZE\t0\t0\tNOCAPS\tNODIGIT\t0\t0\t1\t0\t0\t0\t0\t0\tNOPUNCT\t0\t0\t1\t0\t<abstract>\n" +
            "at\tat\ta\tat\tat\tat\tt\tat\tat\tat\tBLOCKIN\tLINEIN\tALIGNEDLEFT\tSAMEFONT\tSAMEFONTSIZE\t0\t0\tNOCAPS\tNODIGIT\t0\t0\t1\t0\t0\t0\t0\t0\tNOPUNCT\t0\t0\t1\t0\t<abstract>\n" +
            "Zenodo\tzenodo\tZ\tZe\tZen\tZeno\to\tdo\todo\tnodo\tBLOCKIN\tLINEIN\tALIGNEDLEFT\tSAMEFONT\tSAMEFONTSIZE\t0\t0\tINITCAP\tNODIGIT\t0\t0\t0\t0\t0\t0\t0\t0\tNOPUNCT\t0\t0\t1\t0\t<abstract>\n" +
            "(\t(\t(\t(\t(\t(\t(\t(\t(\t(\tBLOCKIN\tLINEIN\tALIGNEDLEFT\tSAMEFONT\tSAMEFONTSIZE\t0\t0\tALLCAP\tNODIGIT\t1\t0\t0\t0\t0\t0\t0\t0\tOPENBRACKET\t0\t0\t1\t0\t<abstract>\n" +
            "10\t10\t1\t10\t10\t10\t0\t10\t10\t10\tBLOCKIN\tLINEIN\tALIGNEDLEFT\tSAMEFONT\tSAMEFONTSIZE\t0\t0\tNOCAPS\tALLDIGIT\t0\t0\t0\t0\t0\t1\t0\t0\tNOPUNCT\t0\t0\t1\t0\t<abstract>\n" +
            ".\t.\t.\t.\t.\t.\t.\t.\t.\t.\tBLOCKIN\tLINEIN\tALIGNEDLEFT\tSAMEFONT\tSAMEFONTSIZE\t0\t0\tALLCAP\tNODIGIT\t1\t0\t0\t0\t0\t0\t0\t0\tDOT\t0\t0\t1\t0\t<abstract>\n" +
            "5281\t5281\t5\t52\t528\t5281\t1\t81\t281\t5281\tBLOCKIN\tLINEIN\tALIGNEDLEFT\tSAMEFONT\tSAMEFONTSIZE\t0\t0\tNOCAPS\tALLDIGIT\t0\t0\t0\t0\t0\t0\t0\t0\tNOPUNCT\t0\t0\t1\t0\t<availability>\n" +
            "/\t/\t/\t/\t/\t/\t/\t/\t/\t/\tBLOCKIN\tLINEIN\tALIGNEDLEFT\tSAMEFONT\tSAMEFONTSIZE\t0\t0\tALLCAP\tNODIGIT\t1\t0\t0\t0\t0\t0\t0\t0\tNOPUNCT\t0\t0\t1\t0\t<availability>\n" +
            "zenodo\tzenodo\tz\tze\tzen\tzeno\to\tdo\todo\tnodo\tBLOCKIN\tLINEIN\tALIGNEDLEFT\tSAMEFONT\tSAMEFONTSIZE\t0\t0\tNOCAPS\tNODIGIT\t0\t0\t0\t0\t0\t0\t0\t0\tNOPUNCT\t0\t0\t1\t0\t<availability>\n" +
            ".\t.\t.\t.\t.\t.\t.\t.\t.\t.\tBLOCKIN\tLINEIN\tALIGNEDLEFT\tSAMEFONT\tSAMEFONTSIZE\t0\t0\tALLCAP\tNODIGIT\t1\t0\t0\t0\t0\t0\t0\t0\tDOT\t0\t0\t1\t0\t<availability>\n" +
            "10368227\t10368227\t1\t10\t103\t1036\t7\t27\t227\t8227\tBLOCKIN\tLINEIN\tALIGNEDLEFT\tSAMEFONT\tSAMEFONTSIZE\t0\t0\tNOCAPS\tALLDIGIT\t0\t0\t0\t1\t0\t0\t0\t0\tNOPUNCT\t0\t0\t1\t0\t<availability>\n" +
            ")\t)\t)\t)\t)\t)\t)\t)\t)\t)\tBLOCKIN\tLINEIN\tALIGNEDLEFT\tSAMEFONT\tSAMEFONTSIZE\t0\t0\tALLCAP\tNODIGIT\t1\t0\t0\t0\t0\t0\t0\t0\tENDBRACKET\t0\t0\t1\t0\t<availability>\n" +
            "or\tor\to\tor\tor\tor\tr\tor\tor\tor\tBLOCKEND\tLINEEND\tALIGNEDLEFT\tSAMEFONT\tSAMEFONTSIZE\t0\t0\tNOCAPS\tNODIGIT\t0\t0\t1\t0\t0\t1\t0\t0\tNOPUNCT\t0\t0\t1\t0\t<availability>\n" +
            "GitHub\tgithub\tG\tGi\tGit\tGitH\tb\tub\tHub\ttHub\tBLOCKSTART\tLINESTART\tALIGNEDLEFT\tSAMEFONT\tSAMEFONTSIZE\t0\t0\tINITCAP\tNODIGIT\t0\t0\t0\t0\t0\t0\t0\t0\tNOPUNCT\t0\t0\t1\t0\t<availability>\n" +
            "(\t(\t(\t(\t(\t(\t(\t(\t(\t(\tBLOCKIN\tLINEIN\tALIGNEDLEFT\tSAMEFONT\tSAMEFONTSIZE\t0\t0\tALLCAP\tNODIGIT\t1\t0\t0\t0\t0\t0\t0\t0\tOPENBRACKET\t0\t0\t1\t0\t<availability>\n" +
            "https\thttps\th\tht\thtt\thttp\ts\tps\ttps\tttps\tBLOCKIN\tLINEIN\tALIGNEDLEFT\tSAMEFONT\tSAMEFONTSIZE\t0\t0\tNOCAPS\tNODIGIT\t0\t0\t0\t0\t0\t0\t0\t1\tNOPUNCT\t0\t0\t1\t0\t<availability>\n" +
            ":\t:\t:\t:\t:\t:\t:\t:\t:\t:\tBLOCKIN\tLINEIN\tALIGNEDLEFT\tSAMEFONT\tSAMEFONTSIZE\t0\t0\tALLCAP\tNODIGIT\t1\t0\t0\t0\t0\t0\t0\t1\tPUNCT\t0\t0\t1\t0\t<availability>\n" +
            "/\t/\t/\t/\t/\t/\t/\t/\t/\t/\tBLOCKIN\tLINEIN\tALIGNEDLEFT\tSAMEFONT\tSAMEFONTSIZE\t0\t0\tALLCAP\tNODIGIT\t1\t0\t0\t0\t0\t0\t0\t1\tNOPUNCT\t0\t0\t1\t0\t<availability>\n" +
            "/\t/\t/\t/\t/\t/\t/\t/\t/\t/\tBLOCKIN\tLINEIN\tALIGNEDLEFT\tSAMEFONT\tSAMEFONTSIZE\t0\t0\tALLCAP\tNODIGIT\t1\t0\t0\t0\t0\t0\t0\t1\tNOPUNCT\t0\t0\t1\t0\t<availability>\n" +
            "github\tgithub\tg\tgi\tgit\tgith\tb\tub\thub\tthub\tBLOCKIN\tLINEIN\tALIGNEDLEFT\tSAMEFONT\tSAMEFONTSIZE\t0\t0\tNOCAPS\tNODIGIT\t0\t0\t0\t0\t0\t0\t0\t1\tNOPUNCT\t0\t0\t1\t0\t<availability>\n" +
            ".\t.\t.\t.\t.\t.\t.\t.\t.\t.\tBLOCKIN\tLINEIN\tALIGNEDLEFT\tSAMEFONT\tSAMEFONTSIZE\t0\t0\tALLCAP\tNODIGIT\t1\t0\t0\t0\t0\t0\t0\t1\tDOT\t0\t0\t1\t0\t<availability>\n" +
            "com\tcom\tc\tco\tcom\tcom\tm\tom\tcom\tcom\tBLOCKIN\tLINEIN\tALIGNEDLEFT\tSAMEFONT\tSAMEFONTSIZE\t0\t0\tNOCAPS\tNODIGIT\t0\t0\t0\t0\t0\t0\t0\t1\tNOPUNCT\t0\t0\t1\t0\t<availability>\n" +
            "/\t/\t/\t/\t/\t/\t/\t/\t/\t/\tBLOCKIN\tLINEIN\tALIGNEDLEFT\tSAMEFONT\tSAMEFONTSIZE\t0\t0\tALLCAP\tNODIGIT\t1\t0\t0\t0\t0\t0\t0\t1\tNOPUNCT\t0\t0\t1\t0\t<availability>\n" +
            "zhiru\tzhiru\tz\tzh\tzhi\tzhir\tu\tru\tiru\thiru\tBLOCKIN\tLINEIN\tALIGNEDLEFT\tSAMEFONT\tSAMEFONTSIZE\t0\t0\tNOCAPS\tNODIGIT\t0\t0\t0\t0\t0\t0\t0\t1\tNOPUNCT\t0\t0\t1\t0\t<availability>\n" +
            "-\t-\t-\t-\t-\t-\t-\t-\t-\t-\tBLOCKIN\tLINEIN\tALIGNEDLEFT\tSAMEFONT\tSAMEFONTSIZE\t0\t0\tALLCAP\tNODIGIT\t1\t0\t0\t0\t0\t0\t0\t1\tHYPHEN\t0\t0\t1\t0\t<copyright>\n" +
            "liu\tliu\tl\tli\tliu\tliu\tu\tiu\tliu\tliu\tBLOCKIN\tLINEIN\tALIGNEDLEFT\tSAMEFONT\tSAMEFONTSIZE\t0\t0\tNOCAPS\tNODIGIT\t0\t1\t0\t0\t0\t0\t0\t1\tNOPUNCT\t0\t0\t1\t0\t<copyright>\n" +
            "/\t/\t/\t/\t/\t/\t/\t/\t/\t/\tBLOCKIN\tLINEIN\tALIGNEDLEFT\tSAMEFONT\tSAMEFONTSIZE\t0\t0\tALLCAP\tNODIGIT\t1\t0\t0\t0\t0\t0\t0\t1\tNOPUNCT\t0\t0\t1\t0\t<copyright>\n" +
            "microbiome_\tmicrobiome_\tm\tmi\tmic\tmicr\t_\te_\tme_\tome_\tBLOCKIN\tLINEEND\tALIGNEDLEFT\tSAMEFONT\tSAMEFONTSIZE\t0\t0\tNOCAPS\tNODIGIT\t0\t0\t0\t0\t0\t0\t0\t1\tNOPUNCT\t0\t0\t1\t0\t<copyright>\n" +
            "evolution\tevolution\te\tev\tevo\tevol\tn\ton\tion\ttion\tBLOCKIN\tLINESTART\tALIGNEDLEFT\tSAMEFONT\tSAMEFONTSIZE\t0\t0\tNOCAPS\tNODIGIT\t0\t0\t1\t0\t0\t0\t0\t0\tNOPUNCT\t0\t0\t1\t0\t<copyright>\n" +
            ")\t)\t)\t)\t)\t)\t)\t)\t)\t)\tBLOCKIN\tLINEIN\tALIGNEDLEFT\tSAMEFONT\tSAMEFONTSIZE\t0\t0\tALLCAP\tNODIGIT\t1\t0\t0\t0\t0\t0\t0\t0\tENDBRACKET\t0\t0\t1\t0\t<copyright>\n" +
            ".\t.\t.\t.\t.\t.\t.\t.\t.\t.\tBLOCKEND\tLINEEND\tALIGNEDLEFT\tSAMEFONT\tSAMEFONTSIZE\t0\t0\tALLCAP\tNODIGIT\t1\t0\t0\t0\t0\t0\t0\t0\tDOT\t0\t0\t1\t0\t<copyright>\n" +
            "Funding\tfunding\tF\tFu\tFun\tFund\tg\tng\ting\tding\tBLOCKSTART\tLINESTART\tALIGNEDLEFT\tNEWFONT\tSAMEFONTSIZE\t1\t0\tINITCAP\tNODIGIT\t0\t0\t1\t0\t0\t0\t0\t0\tNOPUNCT\t0\t0\t1\t0\t<other>\n" +
            ":\t:\t:\t:\t:\t:\t:\t:\t:\t:\tBLOCKIN\tLINEIN\tALIGNEDLEFT\tSAMEFONT\tSAMEFONTSIZE\t1\t0\tALLCAP\tNODIGIT\t1\t0\t0\t0\t0\t0\t0\t0\tPUNCT\t0\t0\t1\t0\t<other>\n" +
            "This\tthis\tT\tTh\tThi\tThis\ts\tis\this\tThis\tBLOCKIN\tLINEIN\tALIGNEDLEFT\tNEWFONT\tSAMEFONTSIZE\t0\t0\tINITCAP\tNODIGIT\t0\t0\t1\t0\t0\t1\t0\t0\tNOPUNCT\t0\t0\t1\t0\tI-<funding>\n" +
            "work\twork\tw\two\twor\twork\tk\trk\tork\twork\tBLOCKIN\tLINEIN\tALIGNEDLEFT\tSAMEFONT\tSAMEFONTSIZE\t0\t0\tNOCAPS\tNODIGIT\t0\t0\t1\t0\t0\t0\t0\t0\tNOPUNCT\t0\t0\t1\t0\t<funding>\n" +
            "was\twas\tw\twa\twas\twas\ts\tas\twas\twas\tBLOCKIN\tLINEIN\tALIGNEDLEFT\tSAMEFONT\tSAMEFONTSIZE\t0\t0\tNOCAPS\tNODIGIT\t0\t0\t1\t0\t0\t0\t0\t0\tNOPUNCT\t0\t0\t1\t0\t<funding>\n" +
            "supported\tsupported\ts\tsu\tsup\tsupp\td\ted\tted\trted\tBLOCKIN\tLINEIN\tALIGNEDLEFT\tSAMEFONT\tSAMEFONTSIZE\t0\t0\tNOCAPS\tNODIGIT\t0\t0\t1\t0\t0\t0\t0\t0\tNOPUNCT\t0\t0\t1\t0\t<funding>\n" +
            "in\tin\ti\tin\tin\tin\tn\tin\tin\tin\tBLOCKIN\tLINEIN\tALIGNEDLEFT\tSAMEFONT\tSAMEFONTSIZE\t0\t0\tNOCAPS\tNODIGIT\t0\t1\t1\t0\t0\t1\t0\t0\tNOPUNCT\t0\t0\t1\t0\t<funding>"
        val postprocessed = LabelUtils.postProcessFulltextCorrectSequencesWithoutInitialToken(resultHeader)

        assertThat(
            Arrays.stream(StringUtils.split(postprocessed, "\n"))
                .filter { l -> l.endsWith("<abstract>") }
                .count(), `is`(0L)
        )
        assertThat(
            Arrays.stream(StringUtils.split(postprocessed, "\n"))
                .filter { l -> l.endsWith("<copyright>") }
                .count(), `is`(0L)
        )

        assertThat(
            Arrays.stream(StringUtils.split(postprocessed, "\n"))
                .filter { l -> l.endsWith("<availability>") }
                .count(), `is`(139)
        )
    }


    companion object {
        @JvmStatic
        @BeforeAll
        @Throws(Exception::class)
        fun before() {
            val modelParameters = ModelParameters()
            modelParameters.name = "bao"
            GrobidProperties.addModel(modelParameters)
        }
    }

}