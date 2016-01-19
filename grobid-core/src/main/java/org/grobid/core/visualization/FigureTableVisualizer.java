package org.grobid.core.visualization;

import net.sf.saxon.om.Item;
import net.sf.saxon.om.SequenceIterator;
import net.sf.saxon.trans.XPathException;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.grobid.core.GrobidModels;
import org.grobid.core.data.Figure;
import org.grobid.core.document.Document;
import org.grobid.core.document.DocumentSource;
import org.grobid.core.engines.Engine;
import org.grobid.core.engines.config.GrobidAnalysisConfig;
import org.grobid.core.factory.GrobidFactory;
import org.grobid.core.layout.GraphicObject;
import org.grobid.core.main.LibraryLoader;
import org.grobid.core.tokenization.TaggingTokenClusteror;
import org.grobid.core.utilities.GrobidProperties;
import org.grobid.core.utilities.XQueryProcessor;

import java.awt.*;
import java.io.File;
import java.io.IOException;

/**
 * Created by zholudev on 18/01/16.
 * Visualize figures and tables
 */
public class FigureTableVisualizer {
    public static void main(String[] args) {
        try {
            File input = new File("/Work/temp/context/coords/4.pdf");
//            File input = new File("/Work/temp/figureExtraction/3.pdf");

            final PDDocument document = PDDocument.load(input);
            File outPdf = new File("/tmp/testFigures.pdf");

            GrobidProperties.set_GROBID_HOME_PATH("grobid-home");
            GrobidProperties.setGrobidPropertiesPath("grobid-home/config/grobid.properties");
            LibraryLoader.load();
            final Engine engine = GrobidFactory.getInstance().getEngine();
            GrobidAnalysisConfig config = new GrobidAnalysisConfig.GrobidAnalysisConfigBuilder()
                    .pdfAssetPath(new File("/tmp/x"))
                    .build();

            DocumentSource documentSource = DocumentSource.fromPdf(input);

            Document teiDoc = engine.fullTextToTEIDoc(input, config);

            PDDocument out = annotateFigureAndTables(
                    document, documentSource.getXmlFile(), teiDoc,
                    false, false, true);

            if (out != null) {
                out.save(outPdf);
                if (Desktop.isDesktopSupported()) {
                    Desktop.getDesktop().open(outPdf);
                }
            }
            System.out.println(Engine.getCntManager());

        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }

    }

    public static PDDocument annotateFigureAndTables(
            PDDocument document,
            File xmlFile, Document teiDoc,
            boolean visualizeTeiFigures,
            boolean visualizePdf2xmlImages,
            boolean visualizeGraphicObjects) throws IOException, XPathException {
        String q = XQueryProcessor.getQueryFromResources("figure-table-coords.xq");
        String tei = teiDoc.getTei();
        System.out.println(tei);
        XQueryProcessor pr = new XQueryProcessor(tei);
        SequenceIterator it = pr.getSequenceIterator(q);
        Item item;

//        System.out.println(new TaggingTokenClusteror(GrobidModels.FULLTEXT, ));

        // visualizing TEI image coords
        if (visualizeTeiFigures) {
            while ((item = it.next()) != null) {
                String coords = item.getStringValue();
                String stringValue = it.next().getStringValue();
                boolean isFigure = Boolean.parseBoolean(stringValue);
                AnnotationUtil.annotatePage(document, coords, isFigure ? 1 : 2);
            }
        }

        //VISUALIZING "IMAGE" elements from pdf2xml
        if (visualizePdf2xmlImages) {
            q = XQueryProcessor.getQueryFromResources("figure-coords-pdf2xml.xq");

            pr = new XQueryProcessor(xmlFile);
            it = pr.getSequenceIterator(q);
            while ((item = it.next()) != null) {
                String coords = item.getStringValue();
                AnnotationUtil.annotatePage(document, coords, 3);
            }
        }

        if (visualizeGraphicObjects) {
            // visualizing graphic objects
            if (teiDoc.getImages() != null) {
                for (GraphicObject go : teiDoc.getImages()) {
                    if (go.getType() == GraphicObject.BITMAP) {
                        AnnotationUtil.annotatePage(document,
                                AnnotationUtil.getCoordString(go.getPage(), go.getX(), go.getY(),
                                        go.getWidth(), go.getHeight()), 5, 1
                        );
                    }
                }
            }

            int i = 10;
            for (Figure f : teiDoc.getFigures()) {
                i++;
                AnnotationUtil.annotatePage(document, f.getTextArea().toString(),
//                        AnnotationUtil.getCoordString(f.getPage(), f.getX(), f.getY(),
//                                f.getWidth(), f.getHeight()),
                        i, 2
                );

                if (f.getGraphicObjects() != null) {
                    for (GraphicObject go : f.getGraphicObjects()) {
                        if (go.getType() == GraphicObject.BITMAP) {
                            AnnotationUtil.annotatePage(document,
                                    AnnotationUtil.getCoordString(go.getPage(), go.getX(), go.getY(),
                                            go.getWidth(), go.getHeight()), i, 2
                            );
                        }
                    }
                }
            }

        }

        return document;
    }

}
