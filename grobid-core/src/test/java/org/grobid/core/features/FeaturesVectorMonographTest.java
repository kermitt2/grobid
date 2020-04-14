package org.grobid.core.features;

import org.grobid.core.document.Document;
import org.grobid.core.document.DocumentSource;
import org.grobid.core.engines.Engine;
import org.grobid.core.engines.config.GrobidAnalysisConfig;
import org.grobid.core.factory.GrobidFactory;
import org.junit.Before;
import org.junit.Test;
import org.powermock.api.easymock.PowerMock;

import java.io.File;

public class FeaturesVectorMonographTest {
    FeatureFactory featureFactory;
    @Before
    public void setUp() throws Exception {
        featureFactory = PowerMock.createMock(FeatureFactory.class);

    }
    @Test
    public void testPrintVector_sample1() throws Exception {
        Engine engine = GrobidFactory.getInstance().getEngine();
        /*ClassLoader classLoader = this.getClass().getClassLoader();
        File file = new File(classLoader.getResource("grobid-core/src/test/resources/test/MullenJSSv18i03.pdf").getFile());
        DocumentSource documentSource = DocumentSource.fromPdf(file, -1, -1, false, true, true);
        Document doc = new Document(documentSource);
        GrobidAnalysisConfig config = GrobidAnalysisConfig.defaultInstance();
        if (config.getAnalyzer() != null)
            doc.setAnalyzer(config.getAnalyzer());
        doc.addTokenizedDocument(config);*/

        //Document result = engine.getParsers().getSegmentationParser().processing(new File("/Work/workspace/data/pdf2xmlreflow/1.pdf"),
        //        GrobidAnalysisConfig.defaultInstance());
        DocumentSource documentSource = DocumentSource.fromPdf(new File("test/Wang-paperAVE2008.pdf"));
        Document doc = engine.getParsers().getSegmentationParser().processing(documentSource, GrobidAnalysisConfig.defaultInstance());
        GrobidAnalysisConfig config = GrobidAnalysisConfig.defaultInstance();
        if (config.getAnalyzer() != null)
            doc.setAnalyzer(config.getAnalyzer());
        doc.addTokenizedDocument(config);
        /*
        File fileInput = new File(input);
        Document doc = engine.fullTextToTEIDoc(fileInput, GrobidAnalysisConfig.defaultInstance());
        doc.getBlocks();
        System.out.println(doc.getTei());*/
        //DocumentSource documentSource = DocumentSource.fromPdf(new File(input));
        //Document doc = engine.getParsers().getSegmentationParser().processing(documentSource, GrobidAnalysisConfig.defaultInstance());


    }
}
