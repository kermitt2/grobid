package org.grobid.core.utilities

import org.apache.commons.lang3.StringUtils
import org.grobid.core.utilities.GrobidConfig.ModelParameters
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.jupiter.api.BeforeAll
import java.util.*
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