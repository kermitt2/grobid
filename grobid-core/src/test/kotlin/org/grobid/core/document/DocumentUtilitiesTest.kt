package org.grobid.core.document

import org.grobid.core.engines.config.GrobidAnalysisConfig
import org.grobid.core.factory.AbstractEngineFactory
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.greaterThan
import java.io.File
import kotlin.test.BeforeTest
import kotlin.test.Test


class DocumentUtilitiesTest {
    @BeforeTest
    @Throws(java.lang.Exception::class)
    fun setInitialContext() {
        AbstractEngineFactory.init()
    }

    @Test
    @Throws(Exception::class)
    fun testGetMedianDistanceForBlocks1() {
        val input = File(this.javaClass.getResource("sampleDocument.1.pdf")?.toURI())
        val doc = DocumentSource.fromPdf(input)

        val document = Document(doc)
        document.addTokenizedDocument(GrobidAnalysisConfig.defaultInstance())

        val medianBlockDistance = DocumentUtilities.getMedianBlockVerticalDistance(document)
        println("Median block distance: $medianBlockDistance")
        assertThat(medianBlockDistance, `is`(greaterThan(0.0)))
        doc.close(true, true, true)
    }

    @Test
    @Throws(Exception::class)
    fun testGetMedianDistanceForBlocks2() {
        val input = File(this.javaClass.getResource("sampleDocument.2.pdf")?.toURI())
        val doc = DocumentSource.fromPdf(input)

        val document = Document(doc)
        document.addTokenizedDocument(GrobidAnalysisConfig.defaultInstance())

        val medianBlockDistance = DocumentUtilities.getMedianBlockVerticalDistance(document)
        println("Median block distance: $medianBlockDistance")
        assertThat(medianBlockDistance, `is`(greaterThan(0.0)))
        doc.close(true, true, true)
    }

    @Test
    @Throws(Exception::class)
    fun testGetMedianDistanceForLines1() {
        val input = File(this.javaClass.getResource("sampleDocument.1.pdf")?.toURI())
        val doc = DocumentSource.fromPdf(input)

        val document = Document(doc)
        document.addTokenizedDocument(GrobidAnalysisConfig.defaultInstance())

        val medianBlockDistance = DocumentUtilities.getMedianLineVerticalDistance(document)
        println("Median vertical line distance: $medianBlockDistance")
        assertThat(medianBlockDistance, `is`(greaterThan(0.0)))
        doc.close(true, true, true)
    }

    @Test
    @Throws(Exception::class)
    fun testGetMedianDistanceForLines2() {
        val input = File(this.javaClass.getResource("sampleDocument.2.pdf")?.toURI())
        val doc = DocumentSource.fromPdf(input)

        val document = Document(doc)
        document.addTokenizedDocument(GrobidAnalysisConfig.defaultInstance())

        val medianBlockDistance = DocumentUtilities.getMedianLineVerticalDistance(document)
        println("Median vertical line distance: $medianBlockDistance")
        assertThat(medianBlockDistance, `is`(greaterThan(0.0)))
        doc.close(true, true, true)
    }
}