package org.grobid.core.visualization;

/**
 * Created by zholudev on 07/01/16.
 * Visualize citation markers and references
 */

import com.google.common.collect.Multimap;
import net.sf.saxon.trans.XPathException;
import org.apache.pdfbox.cos.COSArray;
import org.apache.pdfbox.exceptions.COSVisitorException;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.edit.PDPageContentStream;
import org.apache.pdfbox.pdmodel.graphics.color.PDGamma;
import org.apache.pdfbox.pdmodel.interactive.action.type.PDActionURI;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotationLink;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDBorderStyleDictionary;
import org.grobid.core.data.BibDataSet;
import org.grobid.core.data.BibDataSetContext;
import org.grobid.core.document.Document;
import org.grobid.core.document.xml.XmlBuilderUtils;
import org.grobid.core.engines.Engine;
import org.grobid.core.engines.config.GrobidAnalysisConfig;
import org.grobid.core.factory.GrobidFactory;
import org.grobid.core.layout.BoundingBox;
import org.grobid.core.main.LibraryLoader;
import org.grobid.core.utilities.BibDataSetContextExtractor;
import org.grobid.core.utilities.GrobidProperties;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.Random;


public class CitationsVisualizer {

    public static void main(String args[]) {
        try {
            // /Work/temp/context/1000k/AS_101465473421322_1401202662564.pdf

            // /Work/temp/context/1000k/AS_104748833312772_1401985480367.pdf - invalid byte
            //
//            File input = new File("/Work/temp/pub_citation_styles/1994FEBSLett350_235Hadden.pdf");
//            File input = new File("/Work/temp/context/1000k/AS_99223336914944_1400668095132.pdf");
//            File input = new File("/tmp/AS_100005549445135_1400854589869.pdf"); // not all tokens
//            File input = new File("/Work/temp/context/coords/1.pdf");
//            File input = new File("/Users/zholudev/Downloads/AS-316773709090817@1452536145958_content_1.pdf"); // NO BLOCKS

//            File input = new File("/Users/zholudev/Downloads/AS-320647283052546@1453459677289_content_1.pdf"); //BAD BLOCK
//            File input = new File("/Users/zholudev/Downloads/AS-99301753622543@1400686791996_content_1 (1).pdf"); //spaces
//            File input = new File("/Users/zholudev/Downloads/AS-321758798778369@1453724683241_content_1.pdf"); //spaces
            File input = new File("/Users/zholudev/Downloads/0046353194b29d3b66000000pdf");
//            File input = new File("/Users/zholudev/Downloads/AS-317309489483776@1452663885159_content_1.pdf");
//            File input = new File("/tmp/2.pdf");

//            File input = new File("/Users/zholudev/Downloads/Curtoni 2009 Perspectivas Actuales.pdf");
//            File input = new File("/Work/temp/figureExtraction/3.pdf");
//            File input = new File("/Work/temp/context/tilo/4.pdf");
//            File input = new File("/tmp/test2.pdf");
//            File input = new File("/Work/workspace/habibi/habibi-worker/src/test/resources/data/pdfs/AS_319297254236160_1453137804981.pdf");

//            File input = new File("/Work/temp/pub_citation_styles/1996ParPrecConfProc00507369.pdf");
//            File input = new File("/Work/temp/pub_citation_styles/LaptenokJSSv18i08.pdf");
//
//  File input = new File("/Work/temp/context/coords/3.pdf");
//            File input = new File("/Work/temp/context/coords/3.pdf");
//            File input = new File("/Work/temp/context/coords/2.pdf");

            final PDDocument document = PDDocument.load(input);
            File outPdf = new File("/tmp/test.pdf");

            GrobidProperties.set_GROBID_HOME_PATH("grobid-home");
            GrobidProperties.setGrobidPropertiesPath("grobid-home/config/grobid.properties");
            LibraryLoader.load();
            final Engine engine = GrobidFactory.getInstance().getEngine();
            GrobidAnalysisConfig config = new GrobidAnalysisConfig.GrobidAnalysisConfigBuilder().
                    build();

            Document teiDoc = engine.fullTextToTEIDoc(input, config);

            PDDocument out = annotatePdfWithCitations(document, teiDoc);

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


    public static PDDocument annotatePdfWithCitations(PDDocument document, Document teiDoc) throws IOException, COSVisitorException, XPathException {
        String tei = teiDoc.getTei();
        System.out.println(tei);
        Multimap<String, BibDataSetContext> contexts = BibDataSetContextExtractor.getCitationReferences(tei);
        for (BibDataSet cit : teiDoc.getBibDataSets()) {
            for (BoundingBox b : cit.getResBib().getCoordinates()) {
                String teiId = cit.getResBib().getTeiId();
                annotatePage(document, b.toString(), teiId.hashCode(), contexts.containsKey(teiId) ? 1.5f : 0.5f);
                //annotating reference markers
                for (BibDataSetContext c : contexts.get(teiId)) {
                    System.out.println(c.getContext());
                    String mrect = c.getDocumentCoords();
//                    if (!c.getTeiId().equals("b5")) {
                        for (String coords : mrect.split(";")) {
                            annotatePage(document, coords, teiId.hashCode(), 1.0f);
                        }
//                    }
                }

            }
        }

        for (BibDataSetContext c : contexts.get("")) {
            String mrect = c.getDocumentCoords();
            for (String coords : mrect.split(";")) {
                annotatePage(document, coords, 0, 3.0f);
            }
        }

        return document;
    }


    private static void annotatePage(PDDocument document, String coords, long seed, float lineWidth) throws IOException {
        System.out.println("Annotating for coordinates: " + coords);

        String[] split = coords.split(",");

        Long pageNum = Long.valueOf(split[0], 10) - 1;
        PDPage page = (PDPage) document.getDocumentCatalog().getAllPages().get(pageNum.intValue());

        PDRectangle mediaBox = page.getMediaBox();
        float height = mediaBox.getHeight();
        float lowerX = mediaBox.getLowerLeftX();
        float lowerY = mediaBox.getLowerLeftY();

//        List<String> coordsStrings = new ArrayList<>();


//        RgPath rectPath = rgPath.getSubPath();
//        switch (rectPath.getRgPathType()) {
//            case Rectangle:
//                coordsStrings.add(rectPath.getValue().toString());
//                break;
//            case MultiRectangle:
//                Matcher m = MRECT_PATTERN.matcher(rectPath.getValue().toString());
//                while (m.find()) {
//                    coordsStrings.add(m.group(1));
//                }
//                break;
//            default:
//                return 0;
//        }
//
//        for (String coordStr : coordsStrings) {


        float x = Float.parseFloat(split[1]);
        float y = Float.parseFloat(split[2]);
        float w = Float.parseFloat(split[3]);
        float h = Float.parseFloat(split[4]);

        //most likely a big bounding box
//            if (h > 15) {
//                continue;
//            }

        float annX = x + lowerX;
        float annY = (height - (y + h)) + lowerY;
        float annRightX = x + w + lowerX;
        float annTopY = height - y + lowerY;


        PDRectangle rect = new PDRectangle();

        rect.setLowerLeftX(annX);
        rect.setLowerLeftY(annY);
        rect.setUpperRightX(annRightX);
        rect.setUpperRightY(annTopY);

        PDBorderStyleDictionary borderULine = new PDBorderStyleDictionary();
        borderULine.setStyle(PDBorderStyleDictionary.STYLE_BEVELED);
        // so that a border is not visible at all
        borderULine.setWidth(0);

        PDAnnotationLink txtLink = new PDAnnotationLink();
        txtLink.setBorderStyle(borderULine);

        //white rectangle border color (ideally, should be transparent)
        COSArray white = new COSArray();
        white.setFloatArray(new float[]{1f, 1f, 1f});
        txtLink.setColour(new PDGamma(white));
        txtLink.setReadOnly(true);


        //so that
        txtLink.setHighlightMode(PDAnnotationLink.HIGHLIGHT_MODE_PUSH);

        PDActionURI action = new PDActionURI();
        action.setURI("");
        txtLink.setAction(action);
        txtLink.setRectangle(rect);

        // ADDING LINK TO THE REFERENCE
        page.getAnnotations().add(txtLink);

        //draw a line
        PDBorderStyleDictionary borderThick = new PDBorderStyleDictionary();
        borderThick.setWidth(1);  // 12th inch


//            PDAnnotationLine line = new PDAnnotationLine();
//            line.setLine(new float[]{annX, annY, annRightX, annY});
//            line.setRectangle(rect);
//            line.setBorderStyle(borderThick);
//            line.setReadOnly(true);
//            line.setLocked(true);
//
//            COSArray rgLineColor = new COSArray();
//            rgLineColor.setFloatArray(new float[]{85 / 255f, 177 / 255f, 245 / 255f});
//            PDGamma col = new PDGamma(rgLineColor);
//            line.setColour(col);

        // ADDING LINE TO THE REFERENCE
//            page.getAnnotations().add(line);

        // ADDING LINE TO THE REFERENCE
        PDPageContentStream stream = new PDPageContentStream(document, page, true, false, true);
        Random r = new Random(seed + 1);


//        stream.setStrokingColor(85, 177, 245);
        stream.setStrokingColor(r.nextInt(255), r.nextInt(255), r.nextInt(255));
        stream.setLineWidth(lineWidth);
        stream.drawLine(annX, annY, annRightX, annY);
        stream.close();
//        }
//        return 1;
    }


//    static final float INCH = 72;


}
