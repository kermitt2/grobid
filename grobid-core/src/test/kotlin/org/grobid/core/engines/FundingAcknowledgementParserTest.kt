package org.grobid.core.engines

import org.grobid.core.GrobidModels
import org.grobid.core.analyzers.GrobidAnalyzer
import org.grobid.core.layout.LayoutToken
import org.grobid.core.lexicon.Lexicon
import org.grobid.core.utilities.GrobidConfig
import org.grobid.core.utilities.GrobidProperties
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.hasSize
import org.junit.Before
import org.junit.Test
import org.powermock.api.easymock.PowerMock

class FundingAcknowledgementParserTest {

    private lateinit var target: FundingAcknowledgementParser

    @Before
    @Throws(Exception::class)
    fun setUp() {
        PowerMock.mockStatic(Lexicon::class.java)
        val modelParameters = GrobidConfig.ModelParameters()
        modelParameters.name = "bao"
        GrobidProperties.addModel(modelParameters)
        target = FundingAcknowledgementParser(GrobidModels.DUMMY)
    }

    @Test
    fun testGetExtractionResult() {

        val input: String = "Our warmest thanks to Patrice Lopez, the author of Grobid [22], DeLFT [20], and other open-source projects for his continuous support and inspiration with ideas, suggestions, and fruitful discussions. We thank Pedro Baptista de Castro for his support during this work. Special thanks to Erina Fujita for useful tips on the manuscript.";

        val results: String = "Our\tour\tO\tOu\tOur\tOur\tr\tur\tOur\tOur\tLINESTART\tINITCAP\tNODIGIT\t0\t0\t0\tNOPUNCT\t0\tI-<other>\n" +
                "warmest\twarmest\tw\twa\twar\twarm\tt\tst\test\tmest\tLINEIN\tNOCAPS\tNODIGIT\t0\t0\t0\tNOPUNCT\t0\t<other>\n" +
                "thanks\tthanks\tt\tth\ttha\tthan\ts\tks\tnks\tanks\tLINEIN\tNOCAPS\tNODIGIT\t0\t0\t0\tNOPUNCT\t0\t<other>\n" +
                "to\tto\tt\tto\tto\tto\to\tto\tto\tto\tLINEIN\tNOCAPS\tNODIGIT\t0\t0\t0\tNOPUNCT\t0\t<other>\n" +
                "Patrice\tpatrice\tP\tPa\tPat\tPatr\te\tce\tice\trice\tLINEIN\tINITCAP\tNODIGIT\t0\t0\t0\tNOPUNCT\t0\tI-<person>\n" +
                "Lopez\tlopez\tL\tLo\tLop\tLope\tz\tez\tpez\topez\tLINEIN\tINITCAP\tNODIGIT\t0\t0\t0\tNOPUNCT\t0\t<person>\n" +
                ",\t,\t,\t,\t,\t,\t,\t,\t,\t,\tLINEIN\tALLCAP\tNODIGIT\t1\t0\t0\tCOMMA\t0\tI-<other>\n" +
                "the\tthe\tt\tth\tthe\tthe\te\the\tthe\tthe\tLINEIN\tNOCAPS\tNODIGIT\t0\t0\t0\tNOPUNCT\t0\t<other>\n" +
                "author\tauthor\ta\tau\taut\tauth\tr\tor\thor\tthor\tLINEIN\tNOCAPS\tNODIGIT\t0\t0\t0\tNOPUNCT\t0\t<other>\n" +
                "of\tof\to\tof\tof\tof\tf\tof\tof\tof\tLINEIN\tNOCAPS\tNODIGIT\t0\t0\t0\tNOPUNCT\t0\t<other>\n" +
                "Grobid\tgrobid\tG\tGr\tGro\tGrob\td\tid\tbid\tobid\tLINEIN\tINITCAP\tNODIGIT\t0\t0\t0\tNOPUNCT\t0\t<other>\n" +
                "[\t[\t[\t[\t[\t[\t[\t[\t[\t[\tLINEIN\tALLCAP\tNODIGIT\t1\t0\t0\tOPENBRACKET\t0\t<other>\n" +
                "22\t22\t2\t22\t22\t22\t2\t22\t22\t22\tLINEIN\tNOCAPS\tALLDIGIT\t0\t0\t0\tNOPUNCT\t0\t<other>\n" +
                "]\t]\t]\t]\t]\t]\t]\t]\t]\t]\tLINEIN\tALLCAP\tNODIGIT\t1\t0\t0\tENDBRACKET\t0\t<other>\n" +
                ",\t,\t,\t,\t,\t,\t,\t,\t,\t,\tLINEIN\tALLCAP\tNODIGIT\t1\t0\t0\tCOMMA\t0\t<other>\n" +
                "DeLFT\tdelft\tD\tDe\tDeL\tDeLF\tT\tFT\tLFT\teLFT\tLINEIN\tINITCAP\tNODIGIT\t0\t0\t0\tNOPUNCT\t0\t<other>\n" +
                "[\t[\t[\t[\t[\t[\t[\t[\t[\t[\tLINEIN\tALLCAP\tNODIGIT\t1\t0\t0\tOPENBRACKET\t0\t<other>\n" +
                "20\t20\t2\t20\t20\t20\t0\t20\t20\t20\tLINEIN\tNOCAPS\tALLDIGIT\t0\t0\t0\tNOPUNCT\t0\t<other>\n" +
                "]\t]\t]\t]\t]\t]\t]\t]\t]\t]\tLINEIN\tALLCAP\tNODIGIT\t1\t0\t0\tENDBRACKET\t0\t<other>\n" +
                ",\t,\t,\t,\t,\t,\t,\t,\t,\t,\tLINEIN\tALLCAP\tNODIGIT\t1\t0\t0\tCOMMA\t0\t<other>\n" +
                "and\tand\ta\tan\tand\tand\td\tnd\tand\tand\tLINEIN\tNOCAPS\tNODIGIT\t0\t0\t0\tNOPUNCT\t0\t<other>\n" +
                "other\tother\to\tot\toth\tothe\tr\ter\ther\tther\tLINEIN\tNOCAPS\tNODIGIT\t0\t0\t0\tNOPUNCT\t0\t<other>\n" +
                "open\topen\to\top\tope\topen\tn\ten\tpen\topen\tLINEIN\tNOCAPS\tNODIGIT\t0\t0\t0\tNOPUNCT\t0\t<other>\n" +
                "-\t-\t-\t-\t-\t-\t-\t-\t-\t-\tLINEIN\tALLCAP\tNODIGIT\t1\t0\t0\tHYPHEN\t0\t<other>\n" +
                "source\tsource\ts\tso\tsou\tsour\te\tce\trce\turce\tLINEIN\tNOCAPS\tNODIGIT\t0\t0\t0\tNOPUNCT\t0\t<other>\n" +
                "projects\tprojects\tp\tpr\tpro\tproj\ts\tts\tcts\tects\tLINEIN\tNOCAPS\tNODIGIT\t0\t0\t0\tNOPUNCT\t0\t<other>\n" +
                "for\tfor\tf\tfo\tfor\tfor\tr\tor\tfor\tfor\tLINEIN\tNOCAPS\tNODIGIT\t0\t0\t0\tNOPUNCT\t0\t<other>\n" +
                "his\this\th\thi\this\this\ts\tis\this\this\tLINEIN\tNOCAPS\tNODIGIT\t0\t0\t0\tNOPUNCT\t0\t<other>\n" +
                "continuous\tcontinuous\tc\tco\tcon\tcont\ts\tus\tous\tuous\tLINEIN\tNOCAPS\tNODIGIT\t0\t0\t0\tNOPUNCT\t0\t<other>\n" +
                "support\tsupport\ts\tsu\tsup\tsupp\tt\trt\tort\tport\tLINEIN\tNOCAPS\tNODIGIT\t0\t0\t0\tNOPUNCT\t0\t<other>\n" +
                "and\tand\ta\tan\tand\tand\td\tnd\tand\tand\tLINEIN\tNOCAPS\tNODIGIT\t0\t0\t0\tNOPUNCT\t0\t<other>\n" +
                "inspiration\tinspiration\ti\tin\tins\tinsp\tn\ton\tion\ttion\tLINEIN\tNOCAPS\tNODIGIT\t0\t0\t0\tNOPUNCT\t0\t<other>\n" +
                "with\twith\tw\twi\twit\twith\th\tth\tith\twith\tLINEIN\tNOCAPS\tNODIGIT\t0\t0\t0\tNOPUNCT\t0\t<other>\n" +
                "ideas\tideas\ti\tid\tide\tidea\ts\tas\teas\tdeas\tLINEIN\tNOCAPS\tNODIGIT\t0\t0\t0\tNOPUNCT\t0\t<other>\n" +
                ",\t,\t,\t,\t,\t,\t,\t,\t,\t,\tLINEIN\tALLCAP\tNODIGIT\t1\t0\t0\tCOMMA\t0\t<other>\n" +
                "suggestions\tsuggestions\ts\tsu\tsug\tsugg\ts\tns\tons\tions\tLINEIN\tNOCAPS\tNODIGIT\t0\t0\t0\tNOPUNCT\t0\t<other>\n" +
                ",\t,\t,\t,\t,\t,\t,\t,\t,\t,\tLINEIN\tALLCAP\tNODIGIT\t1\t0\t0\tCOMMA\t0\t<other>\n" +
                "and\tand\ta\tan\tand\tand\td\tnd\tand\tand\tLINEIN\tNOCAPS\tNODIGIT\t0\t0\t0\tNOPUNCT\t0\t<other>\n" +
                "fruitful\tfruitful\tf\tfr\tfru\tfrui\tl\tul\tful\ttful\tLINEIN\tNOCAPS\tNODIGIT\t0\t0\t0\tNOPUNCT\t0\t<other>\n" +
                "discussions\tdiscussions\td\tdi\tdis\tdisc\ts\tns\tons\tions\tLINEIN\tNOCAPS\tNODIGIT\t0\t0\t0\tNOPUNCT\t0\t<other>\n" +
                ".\t.\t.\t.\t.\t.\t.\t.\t.\t.\tLINEIN\tALLCAP\tNODIGIT\t1\t0\t0\tDOT\t0\t<other>\n" +
                "We\twe\tW\tWe\tWe\tWe\te\tWe\tWe\tWe\tLINEIN\tINITCAP\tNODIGIT\t0\t0\t0\tNOPUNCT\t0\t<other>\n" +
                "thank\tthank\tt\tth\ttha\tthan\tk\tnk\tank\thank\tLINEIN\tNOCAPS\tNODIGIT\t0\t0\t0\tNOPUNCT\t0\t<other>\n" +
                "Pedro\tpedro\tP\tPe\tPed\tPedr\to\tro\tdro\tedro\tLINEIN\tINITCAP\tNODIGIT\t0\t0\t0\tNOPUNCT\t0\tI-<person>\n" +
                "Baptista\tbaptista\tB\tBa\tBap\tBapt\ta\tta\tsta\tista\tLINEIN\tINITCAP\tNODIGIT\t0\t0\t0\tNOPUNCT\t0\t<person>\n" +
                "de\tde\td\tde\tde\tde\te\tde\tde\tde\tLINEIN\tNOCAPS\tNODIGIT\t0\t0\t0\tNOPUNCT\t0\t<person>\n" +
                "Castro\tcastro\tC\tCa\tCas\tCast\to\tro\ttro\tstro\tLINEIN\tINITCAP\tNODIGIT\t0\t0\t0\tNOPUNCT\t0\t<person>\n" +
                "for\tfor\tf\tfo\tfor\tfor\tr\tor\tfor\tfor\tLINEIN\tNOCAPS\tNODIGIT\t0\t0\t0\tNOPUNCT\t0\tI-<other>\n" +
                "his\this\th\thi\this\this\ts\tis\this\this\tLINEIN\tNOCAPS\tNODIGIT\t0\t0\t0\tNOPUNCT\t0\t<other>\n" +
                "support\tsupport\ts\tsu\tsup\tsupp\tt\trt\tort\tport\tLINEIN\tNOCAPS\tNODIGIT\t0\t0\t0\tNOPUNCT\t0\t<other>\n" +
                "during\tduring\td\tdu\tdur\tduri\tg\tng\ting\tring\tLINEIN\tNOCAPS\tNODIGIT\t0\t0\t0\tNOPUNCT\t0\t<other>\n" +
                "this\tthis\tt\tth\tthi\tthis\ts\tis\this\tthis\tLINEIN\tNOCAPS\tNODIGIT\t0\t0\t0\tNOPUNCT\t0\t<other>\n" +
                "work\twork\tw\two\twor\twork\tk\trk\tork\twork\tLINEIN\tNOCAPS\tNODIGIT\t0\t0\t0\tNOPUNCT\t0\t<other>\n" +
                ".\t.\t.\t.\t.\t.\t.\t.\t.\t.\tLINEIN\tALLCAP\tNODIGIT\t1\t0\t0\tDOT\t0\t<other>\n" +
                "Special\tspecial\tS\tSp\tSpe\tSpec\tl\tal\tial\tcial\tLINEIN\tINITCAP\tNODIGIT\t0\t0\t0\tNOPUNCT\t0\t<other>\n" +
                "thanks\tthanks\tt\tth\ttha\tthan\ts\tks\tnks\tanks\tLINEIN\tNOCAPS\tNODIGIT\t0\t0\t0\tNOPUNCT\t0\t<other>\n" +
                "to\tto\tt\tto\tto\tto\to\tto\tto\tto\tLINEIN\tNOCAPS\tNODIGIT\t0\t0\t0\tNOPUNCT\t0\t<other>\n" +
                "Erina\terina\tE\tEr\tEri\tErin\ta\tna\tina\trina\tLINEIN\tINITCAP\tNODIGIT\t0\t0\t0\tNOPUNCT\t0\tI-<person>\n" +
                "Fujita\tfujita\tF\tFu\tFuj\tFuji\ta\tta\tita\tjita\tLINEIN\tINITCAP\tNODIGIT\t0\t0\t0\tNOPUNCT\t0\t<person>\n" +
                "for\tfor\tf\tfo\tfor\tfor\tr\tor\tfor\tfor\tLINEIN\tNOCAPS\tNODIGIT\t0\t0\t0\tNOPUNCT\t0\tI-<other>\n" +
                "useful\tuseful\tu\tus\tuse\tusef\tl\tul\tful\teful\tLINEIN\tNOCAPS\tNODIGIT\t0\t0\t0\tNOPUNCT\t0\t<other>\n" +
                "tips\ttips\tt\tti\ttip\ttips\ts\tps\tips\ttips\tLINEIN\tNOCAPS\tNODIGIT\t0\t0\t0\tNOPUNCT\t0\t<other>\n" +
                "on\ton\to\ton\ton\ton\tn\ton\ton\ton\tLINEIN\tNOCAPS\tNODIGIT\t0\t0\t0\tNOPUNCT\t0\t<other>\n" +
                "the\tthe\tt\tth\tthe\tthe\te\the\tthe\tthe\tLINEIN\tNOCAPS\tNODIGIT\t0\t0\t0\tNOPUNCT\t0\t<other>\n" +
                "manuscript\tmanuscript\tm\tma\tman\tmanu\tt\tpt\tipt\tript\tLINEIN\tNOCAPS\tNODIGIT\t0\t0\t0\tNOPUNCT\t0\t<other>\n" +
                ".\t.\t.\t.\t.\t.\t.\t.\t.\t.\tLINEEND\tALLCAP\tNODIGIT\t1\t0\t0\tDOT\t0\t<other>";

        val tokens: List<LayoutToken> = GrobidAnalyzer.getInstance().tokenizeWithLayoutToken(input);

        val (element, mutableTriple) = target.getExtractionResult(tokens, results)

        assertThat(mutableTriple.left, hasSize(0))
        assertThat(mutableTriple.middle, hasSize(3))
        assertThat(mutableTriple.middle.get(0).rawName, `is`("Patrice Lopez"))
        assertThat(mutableTriple.middle.get(1).rawName, `is`("Pedro Baptista de Castro"))
        assertThat(mutableTriple.middle.get(2).rawName, `is`("Erina Fujita"))
        assertThat(mutableTriple.right, hasSize(0))
    }
}