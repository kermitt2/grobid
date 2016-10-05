package org.grobid.core.visualization;

/**
 * Created by zholudev on 07/01/16.
 * Visualize citation markers and references
 */

import com.google.common.collect.Multimap;
import net.sf.saxon.trans.XPathException;
import org.apache.pdfbox.cos.COSArray;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.exceptions.COSVisitorException;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDDocumentCatalog;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.edit.PDPageContentStream;
import org.apache.pdfbox.pdmodel.graphics.color.PDGamma;
import org.apache.pdfbox.pdmodel.interactive.action.type.PDActionURI;
import org.apache.pdfbox.pdmodel.interactive.action.type.PDActionGoTo;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotationLink;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDBorderStyleDictionary;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.destination.PDDestination;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.destination.PDNamedDestination;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.destination.PDPageFitWidthDestination;
import org.grobid.core.data.BibDataSet;
import org.grobid.core.data.BibDataSetContext;
import org.grobid.core.document.Document;
import org.grobid.core.document.xml.XmlBuilderUtils;
import org.grobid.core.engines.Engine;
import org.grobid.core.engines.config.GrobidAnalysisConfig;
import org.grobid.core.factory.GrobidFactory;
import org.grobid.core.layout.BoundingBox;
import org.grobid.core.layout.Page;
import org.grobid.core.main.LibraryLoader;
import org.grobid.core.utilities.BibDataSetContextExtractor;
import org.grobid.core.utilities.GrobidProperties;
import org.grobid.core.utilities.Pair;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.Random;
import java.util.Map;
import java.util.HashMap;
import java.util.List;

public class CitationsVisualizer {

    public static void main(String args[]) {
        try {
            // to be reviewed ;)
            File input = new File("/Users/zholudev/Downloads/AS-319651387510785@1453222237979_content_1.pdf");

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
        //ystem.out.println(tei);
        int totalBib = 0;
        int totalMarkers1 = 0;
        int totalMarkers2 = 0;
        Multimap<String, BibDataSetContext> contexts = BibDataSetContextExtractor.getCitationReferences(tei);
		Map<String, Pair<Integer, Integer>> dictionary = new HashMap<String, Pair<Integer, Integer>>();
        for (BibDataSet cit : teiDoc.getBibDataSets()) {
            String teiId = cit.getResBib().getTeiId();
            totalBib++;
            for (BoundingBox b : cit.getResBib().getCoordinates()) {
                annotatePage(document, b.toString(), teiId, 
					contexts.containsKey(teiId) ? 1.5f : 0.5f, false, dictionary);
            }
            //annotating reference markers
            for (BibDataSetContext c : contexts.get(teiId)) {
                //System.out.println(c.getContext());
                String mrect = c.getDocumentCoords();
                for (String coords : mrect.split(";")) {
                    annotatePage(document, coords, teiId, 1.0f, true, dictionary);
                    totalMarkers1++;
                }
            }
        }
        for (BibDataSetContext c : contexts.get("")) {
            String mrect = c.getDocumentCoords();
            for (String coords : mrect.split(";")) {
                annotatePage(document, coords, null, 3.0f, true, dictionary);
                totalMarkers2++;
            }
        }
        System.out.println("totalBib: " + totalBib);
        System.out.println("totalMarkers1: " + totalMarkers1);
        System.out.println("totalMarkers2: " + totalMarkers2);
        return document;
    }


    private static void annotatePage(PDDocument document, 
									String coords, 
									String teiId, 
									float lineWidth, 
									boolean isMarker,
									Map<String, Pair<Integer, Integer>> dictionary) throws IOException {
        //System.out.println("Annotating for coordinates: " + coords);
		long seed = 0L;
		if (teiId != null)
			seed = teiId.hashCode();
		
		String[] split = coords.split(",");

        Long pageNum = Long.valueOf(split[0], 10) - 1;
        PDPage page = (PDPage) document.getDocumentCatalog().getAllPages().get(pageNum.intValue());

        PDRectangle mediaBox = page.getMediaBox();
        float height = mediaBox.getHeight();
        float lowerX = mediaBox.getLowerLeftX();
        float lowerY = mediaBox.getLowerLeftY();

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
		PDActionGoTo action = new PDActionGoTo();
		
		if (isMarker && (teiId != null)) {
			Pair<Integer, Integer> thePlace = dictionary.get(teiId);
			if (thePlace != null) {
				PDPageFitWidthDestination destination = new PDPageFitWidthDestination();
				destination.setPageNumber(thePlace.getA());
				destination.setTop(thePlace.getB());
				action.setDestination(destination);	
				txtLink.setAction(action);
			}
		} else if (teiId != null) {
			// register the object in the dictionary
			if (dictionary.get(teiId) == null) {
				Pair<Integer, Integer> thePlace = 
					new Pair<Integer, Integer>(new Integer(pageNum.intValue()), new Integer(Math.round(annTopY+h)));
				dictionary.put(teiId, thePlace);
			}
		}
        txtLink.setRectangle(rect);

        // ADDING LINK TO THE REFERENCE
        page.getAnnotations().add(txtLink);

        //draw a line
        PDBorderStyleDictionary borderThick = new PDBorderStyleDictionary();
        borderThick.setWidth(1);  // 12th inch

        // ADDING LINE TO THE REFERENCE
        PDPageContentStream stream = new PDPageContentStream(document, page, true, false, true);
        Random r = new Random(seed + 1);

//        stream.setStrokingColor(85, 177, 245);
        stream.setStrokingColor(r.nextInt(255), r.nextInt(255), r.nextInt(255));
        stream.setLineWidth(lineWidth);
        stream.drawLine(annX, annY, annRightX, annY);
        stream.close();
    }


    public static String getJsonAnnotations(Document teiDoc) throws IOException, XPathException {
        StringBuilder jsonRef = new StringBuilder();
        jsonRef.append("{ ");

        // default page height and width
        List<Page> pages = teiDoc.getPages();
        Page page = null;
        if (pages.size() > 1) {
            // avoiding a possible cover page
            page = pages.get(1);
        } else {
            page = pages.get(0);
        }
        jsonRef.append("\"page_height\":" + page.getHeight());
        jsonRef.append(", \"page_width\":" + page.getWidth() + ", ");

        jsonRef.append("\"refBibs\":[");
        StringBuilder jsonMark = new StringBuilder();
        jsonMark.append("\"refMarkers\":[");

        int totalMarkers1 = 0;
        int totalMarkers2 = 0;
        int totalBib = 0;

        String tei = teiDoc.getTei();
        Multimap<String, BibDataSetContext> contexts = BibDataSetContextExtractor.getCitationReferences(tei);
        boolean begin = true;
        boolean beginMark = true;
        for (BibDataSet cit : teiDoc.getBibDataSets()) {
            if (begin)
                begin = false;
            else
                jsonRef.append(", ");
            String teiId = cit.getResBib().getTeiId();
            totalBib++;
            jsonRef.append("{ \"id\":\"").append(teiId).append("\", ");
            jsonRef.append("\"pos\":[");
            boolean begin2 = true;
            for (BoundingBox b : cit.getResBib().getCoordinates()) {
                // reference string
                if (begin2)
                    begin2 = false;
                else
                    jsonRef.append(", ");

                jsonRef.append("{").append(b.toJson()).append("}");
                //annotatePage(document, b.toString(), teiId.hashCode(), contexts.containsKey(teiId) ? 1.5f : 0.5f);
            }
            // reference markers for this reference
            for (BibDataSetContext c : contexts.get(teiId)) {
                //System.out.println(c.getContext());
                String mrect = c.getDocumentCoords();
                for (String coords : mrect.split(";")) {
                    if (beginMark)
                        beginMark = false;
                    else
                        jsonMark.append(", ");
                    //annotatePage(document, coords, teiId.hashCode(), 1.0f);
                    jsonMark.append("{ \"id\":\"").append(teiId).append("\", ");
                    BoundingBox b2 = BoundingBox.fromString(coords);
                    jsonMark.append(b2.toJson()).append(" }");
                    totalMarkers1++;
                }
            }
            jsonRef.append("] }");
        }

        for (BibDataSetContext c : contexts.get("")) {
            String mrect = c.getDocumentCoords();
            for (String coords : mrect.split(";")) {
                if (beginMark)
                    beginMark = false;
                else
                    jsonMark.append(", ");
                //annotatePage(document, coords, 0, 1.0f);
                BoundingBox b = BoundingBox.fromString(coords);
                jsonMark.append("{").append(b.toJson()).append("}"); 
                totalMarkers2++;
            }
        }

        System.out.println("totalBib: " + totalBib);
        System.out.println("totalMarkers1: " + totalMarkers1);
        System.out.println("totalMarkers2: " + totalMarkers2);

        jsonRef.append("], ").append(jsonMark.toString()).append("] }");
        return jsonRef.toString();
    }
}
