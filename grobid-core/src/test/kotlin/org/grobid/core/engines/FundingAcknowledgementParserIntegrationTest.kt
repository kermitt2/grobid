package org.grobid.core.engines

import org.grobid.core.engines.config.GrobidAnalysisConfig
import org.grobid.core.factory.AbstractEngineFactory
import org.grobid.core.utilities.GrobidConfig
import org.grobid.core.utilities.GrobidProperties
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.hasSize
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Test

class FundingAcknowledgementParserIntegrationTest {

    private lateinit var target: FundingAcknowledgementParser

    @Before
    @Throws(Exception::class)
    fun setUp() {
        val modelParameters = GrobidConfig.ModelParameters()
        modelParameters.name = "bao"
        GrobidProperties.addModel(modelParameters)
        target = FundingAcknowledgementParser()
    }

    @Test
    fun testXmlFragmentProcessing_withoutSentenceSegmentation_shouldReturnSameXML() {

        val input = "\n\t\t\t<div type=\"acknowledgement\">\n<div><head>Acknowledgments</head><p>This research was " +
            "funded by the NASA Land-Cover and Land-Use Change Program (Grant Number: 80NSSC18K0315), the NASA " +
            "Carbon Monitoring System (Grant Number: 80NSSC20K0022), and </p></div>\n\t\t\t</div>\n\n"


        // Expected
//        val output = "\n\t\t\t<div type=\"acknowledgement\">\n<div><head>Acknowledgments</head><p>This research was " +
//            "funded by the <rs xmlns=\"http://www.tei-c.org/ns/1.0\" type=\"funder\">NASA</rs> " +
//            "<rs xmlns=\"http://www.tei-c.org/ns/1.0\" type=\"programName\">Land-Cover and Land-Use Change Program</rs> " +
//            "(Grant Number: <rs xmlns=\"http://www.tei-c.org/ns/1.0\" type=\"grantNumber\">80NSSC18K0315</rs>), " +
//            "the <rs xmlns=\"http://www.tei-c.org/ns/1.0\" type=\"funder\">NASA Carbon Monitoring System</rs> " +
//            "(Grant Number: <rs xmlns=\"http://www.tei-c.org/ns/1.0\" type=\"grantNumber\">80NSSC20K0022</rs>), " +
//            "and </p></div>\n\t\t\t</div>\n\n"

        // Current version output
        val output = "<div type=\"acknowledgement\">\n<div><head>Acknowledgments</head><p>This research was " +
            "funded by the <rs xmlns=\"http://www.tei-c.org/ns/1.0\" type=\"funder\">NASA</rs> " +
            "<rs xmlns=\"http://www.tei-c.org/ns/1.0\" type=\"programName\">Land-Cover and Land-Use Change Program</rs> " +
            "(Grant Number: <rs xmlns=\"http://www.tei-c.org/ns/1.0\" type=\"grantNumber\">80NSSC18K0315</rs>), " +
            "the <rs xmlns=\"http://www.tei-c.org/ns/1.0\" type=\"funder\">NASA Carbon Monitoring System</rs> " +
            "(Grant Number: <rs xmlns=\"http://www.tei-c.org/ns/1.0\" type=\"grantNumber\">80NSSC20K0022</rs>), " +
            "and</p></div>\n\t\t\t</div>"

        val config = GrobidAnalysisConfig.GrobidAnalysisConfigBuilder()
            .withSentenceSegmentation(false)
            .build()

        val (element, mutableTriple) = target.processingXmlFragment(input, config)

        assertThat(element.toXML(), `is`(output))
        assertThat(mutableTriple.left, hasSize(2))
    }

    @Test
    fun testXmlFragmentProcessing2_withoutSentenceSegmentation_shouldReturnSameXML() {
        val input ="\n" +
            "\t\t\t<div type=\"acknowledgement\">\n" +
            "<div xmlns=\"http://www.tei-c.org/ns/1.0\"><head>Acknowledgements</head><p>Our warmest thanks to Patrice Lopez, the author of Grobid <ref type=\"bibr\" target=\"#b21\">[22]</ref>, DeLFT <ref type=\"bibr\" target=\"#b19\">[20]</ref>, and other open-source projects for his continuous support and inspiration with ideas, suggestions, and fruitful discussions. We thank Pedro Baptista de Castro for his support during this work. Special thanks to Erina Fujita for useful tips on the manuscript.</p></div>\n" +
            "\t\t\t</div>\n\n"

        // Expected
//        val output = "\n\t\t\t<div type=\"acknowledgement\">\n" +
//            "<div><head>Acknowledgements</head><p>Our warmest thanks to <rs xmlns=\"http://www.tei-c.org/ns/1.0\" type=\"person\">Patrice Lopez</rs>, the author of Grobid [22], DeLFT [20], and other open-source projects for his continuous support and inspiration with ideas, suggestions, and fruitful discussions. We thank <rs xmlns=\"http://www.tei-c.org/ns/1.0\" type=\"person\">Pedro Baptista de Castro</rs> for his support during this work. Special thanks to <rs xmlns=\"http://www.tei-c.org/ns/1.0\" type=\"person\">Erina Fujita</rs> for useful tips on the manuscript.</p></div>\n" +
//            "\t\t\t</div>\n\n"

        // Current version output
        val output = "<div type=\"acknowledgement\">\n" +
            "<div><head>Acknowledgements</head><p>Our warmest thanks to <rs xmlns=\"http://www.tei-c.org/ns/1.0\" type=\"person\">Patrice Lopez</rs>, the author of Grobid [22], DeLFT [20], and other open-source projects for his continuous support and inspiration with ideas, suggestions, and fruitful discussions. We thank <rs xmlns=\"http://www.tei-c.org/ns/1.0\" type=\"person\">Pedro Baptista de Castro</rs> for his support during this work. Special thanks to <rs xmlns=\"http://www.tei-c.org/ns/1.0\" type=\"person\">Erina Fujita</rs> for useful tips on the manuscript.</p></div>\n" +
            "\t\t\t</div>"

        val config = GrobidAnalysisConfig.GrobidAnalysisConfigBuilder()
            .withSentenceSegmentation(false)
            .build()

        val (element, mutableTriple) = target.processingXmlFragment(input, config)

        assertThat(element.toXML(), `is`(output))
    }

    @Test
    fun testXmlFragmentProcessing2_withSentenceSegmentation_shouldWork() {
        val input ="\n" +
            "\t\t\t<div type=\"acknowledgement\">\n" +
            "<div xmlns=\"http://www.tei-c.org/ns/1.0\"><head>Acknowledgements</head><p><s>Our warmest thanks to Patrice Lopez, the author of Grobid <ref type=\"bibr\" target=\"#b21\">[22]</ref>, DeLFT <ref type=\"bibr\" target=\"#b19\">[20]</ref>, and other open-source projects for his continuous support and inspiration with ideas, suggestions, and fruitful discussions.</s><s>We thank Pedro Baptista de Castro for his support during this work.</s><s>Special thanks to Erina Fujita for useful tips on the manuscript.</s></p></div>\n" +
            "\t\t\t</div>\n\n"

        val output = "\n\t\t\t<div type=\"acknowledgement\">\n" +
            "<div><head>Acknowledgements</head><p><s>Our warmest thanks to <rs xmlns=\"http://www.tei-c.org/ns/1.0\" type=\"person\">Patrice Lopez</rs>, the author of Grobid <ref type=\"bibr\" target=\"#b21\">[22]</ref>, DeLFT <ref type=\"bibr\" target=\"#b19\">[20]</ref>, and other open-source projects for his continuous support and inspiration with ideas, suggestions, and fruitful discussions.</s><s>We thank <rs xmlns=\"http://www.tei-c.org/ns/1.0\" type=\"person\">Pedro Baptista de Castro</rs> for his support during this work.</s><s>Special thanks to <rs xmlns=\"http://www.tei-c.org/ns/1.0\" type=\"person\">Erina Fujita</rs> for useful tips on the manuscript.</p></div>\n" +
            "\t\t\t</div>\n\n"

        val config = GrobidAnalysisConfig.GrobidAnalysisConfigBuilder()
            .withSentenceSegmentation(true)
            .build()

        val (element, mutableTriple) = target.processingXmlFragment(input, config)

        assertThat(element.toXML(), `is`(output))
    }

    companion object {
        @JvmStatic
        @BeforeClass
        @Throws(java.lang.Exception::class)
        fun setInitialContext(): Unit {
            AbstractEngineFactory.init()
        }
    }
}