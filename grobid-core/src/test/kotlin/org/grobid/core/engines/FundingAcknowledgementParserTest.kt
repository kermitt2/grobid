package org.grobid.core.engines

import org.grobid.core.GrobidModels
import org.grobid.core.lexicon.Lexicon
import org.grobid.core.utilities.GrobidConfig
import org.grobid.core.utilities.GrobidProperties
import org.junit.Before
import org.junit.Test
import org.powermock.api.easymock.PowerMock

class FundingAcknowledgementParserTest {

    private lateinit var target: DateParser

    @Before
    @Throws(Exception::class)
    fun setUp() {
        PowerMock.mockStatic(Lexicon::class.java)
        val modelParameters = GrobidConfig.ModelParameters()
        modelParameters.name = "bao"
        GrobidProperties.addModel(modelParameters)
        target = DateParser(GrobidModels.DUMMY)
    }

    @Test
    fun testGetExtractionResult() {

    }
}