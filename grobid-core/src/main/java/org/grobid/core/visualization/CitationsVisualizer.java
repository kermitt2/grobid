package org.grobid.core.visualization;

import com.google.common.collect.Multimap;
import net.sf.saxon.trans.XPathException;

import org.apache.commons.lang3.StringUtils;
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
import org.grobid.core.data.BiblioItem;
import org.grobid.core.data.BibDataSetContext;
import org.grobid.core.data.Person;
import org.grobid.core.document.Document;
import org.grobid.core.engines.Engine;
import org.grobid.core.engines.config.GrobidAnalysisConfig;
import org.grobid.core.factory.GrobidFactory;
import org.grobid.core.layout.BoundingBox;
import org.grobid.core.layout.Page;
import org.grobid.core.main.LibraryLoader;
import org.grobid.core.utilities.BibDataSetContextExtractor;
import org.grobid.core.utilities.GrobidProperties;
import org.grobid.core.utilities.LayoutTokensUtil;
import org.grobid.core.utilities.Pair;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.Random;
import java.util.Map;
import java.util.HashMap;
import java.util.List;

/**
 *  Utilities for visualizing citation markers and biblographical references, wither directly
 *  in the PDF using the PDF annotation layer or as annotations in JSON for supporting
 *  web based rendering (e.g. with PDF.js) and interactive HTML layer.
 *  See the web console/demo for actual examples of usage.
 */
public class CitationsVisualizer {
    private static final Logger LOGGER = LoggerFactory.getLogger(CitationsVisualizer.class);

    /**
     *  Augment a PDF with bibliographical annotation, for bib. ref. and bib markers.
     *  The PDF annotation layer is used with "GoTo" and "URI" action links.
     *  The annotations of the bibliographical references can be associated to an URL in order
     *  to have clickable references direclty in the PDF.
     *  The Apache PDFBox library is used.
     *
     *  @param document PDDocument object resulting from the PDF parsing with PDFBox
     *  @param teiDoc the Document object resulting from the full document structuring
     *  @param resolvedBibRefUrl the list of URL to be added to the bibliographical reference
     *  annotations, if null the bib. ref. annotations are not associated to external URL.
     */
    public static PDDocument annotatePdfWithCitations(PDDocument document, Document teiDoc,
            List<String> resolvedBibRefUrl) throws IOException, COSVisitorException, XPathException {
        String tei = teiDoc.getTei();
        //System.out.println(tei);
        int totalBib = 0;
        int totalMarkers1 = 0;
        int totalMarkers2 = 0;
        Multimap<String, BibDataSetContext> contexts = BibDataSetContextExtractor.getCitationReferences(tei);
		Map<String, Pair<Integer, Integer>> dictionary = new HashMap<String, Pair<Integer, Integer>>();
        int indexBib = 0;
        for (BibDataSet cit : teiDoc.getBibDataSets()) {
            String teiId = cit.getResBib().getTeiId();
            totalBib++;
            String theUrl = null;
            if ( (resolvedBibRefUrl != null) &&
                 (resolvedBibRefUrl.size() > indexBib) &&
                 (resolvedBibRefUrl.get(indexBib) != null) )
                theUrl = resolvedBibRefUrl.get(indexBib);
            else {
                // by default we put the existing url, doi or arXiv link
                BiblioItem biblio = cit.getResBib();
                if (!StringUtils.isEmpty(biblio.getDOI())) {
                    theUrl = "http://dx.doi.org/" + biblio.getDOI();
                } else if (!StringUtils.isEmpty(biblio.getArXivId())) {
                    theUrl = "http://arxiv.org/" + biblio.getArXivId();
                } else if (!StringUtils.isEmpty(biblio.getWeb())) {
                    theUrl = biblio.getWeb();
                }
            }
            if (cit.getResBib().getCoordinates() != null) {
                for (BoundingBox b : cit.getResBib().getCoordinates()) {
                    annotatePage(document, b.toString(), teiId, theUrl, 1.5f, false, dictionary);
                }
            }
            //annotating reference markers
            for (BibDataSetContext c : contexts.get(teiId)) {
                //System.out.println(c.getContext());
                String mrect = c.getDocumentCoords();
                if ((mrect != null) && (mrect.trim().length()>0)) {
                    for (String coords : mrect.split(";")) {
                        if (coords.trim().length() == 0)
                            continue;
                        annotatePage(document, coords, teiId, null, 1.0f, true, dictionary);
                        totalMarkers1++;
                    }
                }
            }
            indexBib++;
        }
        for (BibDataSetContext c : contexts.get("")) {
            String mrect = c.getDocumentCoords();
            if ((mrect != null) && (mrect.trim().length()>0)) {
                for (String coords : mrect.split(";")) {
                    if (coords.trim().length() == 0)
                        continue;
                    annotatePage(document, coords, null, null, 1.0f, true, dictionary);
                    totalMarkers2++;
                }
            }
        }

        if (teiDoc.getResHeader() != null && teiDoc.getResHeader().getFullAuthors() != null) {
            for (Person p : teiDoc.getResHeader().getFullAuthors()) {
                if (p.getLayoutTokens() != null) {
                    String coordsString = LayoutTokensUtil.getCoordsString(p.getLayoutTokens());
                    for (String coords : coordsString.split(";")) {
                        annotatePage(document, coords, "123", null,
//                                p.getLastName() == null ? 1 : p.getLastName().hashCode(),
                                1.0f, true, dictionary);
                    }
                }
            }
        }

        LOGGER.debug("totalBib: " + totalBib);
        LOGGER.debug("totalMarkers1: " + totalMarkers1);
        LOGGER.debug("totalMarkers2: " + totalMarkers2);
        return document;
    }


    private static void annotatePage(PDDocument document,
									String coords,
									String teiId,
                                    String uri,
									float lineWidth,
									boolean isMarker,
									Map<String, Pair<Integer, Integer>> dictionary) throws IOException {
        //System.out.println("Annotating for coordinates: " + coords);
		/*long seed = 0L;
		if (teiId != null)
			seed = teiId.hashCode();*/
        if (StringUtils.isEmpty(coords)) {
            return;
        }
		String[] split = coords.split(",");

        Long pageNum = Long.valueOf(split[0], 10) - 1;
        PDPage page = (PDPage) document.getDocumentCatalog().getAllPages().get(pageNum.intValue());
        PDRectangle mediaBox = page.getMediaBox();
        if (mediaBox == null) {
            mediaBox = page.findMediaBox();
            // this will look for the main media box of the page up in the PDF element hierarchy
            if (mediaBox == null) {
                // we tried our best given PDFBox
                LOGGER.warn("Media box for page " + pageNum.intValue() + " not found.");
                return;
            }
        }
 
        float height = mediaBox.getHeight();
        float lowerX = mediaBox.getLowerLeftX();
        float lowerY = mediaBox.getLowerLeftY();

        float x = Float.parseFloat(split[1]);
        float y = Float.parseFloat(split[2]);
        float w = Float.parseFloat(split[3]);
        float h = Float.parseFloat(split[4]);

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
        txtLink.setHighlightMode(PDAnnotationLink.HIGHLIGHT_MODE_PUSH);

		if (isMarker && (teiId != null)) {
			Pair<Integer, Integer> thePlace = dictionary.get(teiId);
			if (thePlace != null) {
				PDPageFitWidthDestination destination = new PDPageFitWidthDestination();
				destination.setPageNumber(thePlace.getA());
				destination.setTop(thePlace.getB());
                PDActionGoTo action = new PDActionGoTo();
				action.setDestination(destination);
				txtLink.setAction(action);
			}
		} else {
            if (teiId != null) {
			    // register the object in the dictionary
			    if (dictionary.get(teiId) == null) {
				    Pair<Integer, Integer> thePlace =
					   new Pair<Integer, Integer>(new Integer(pageNum.intValue()), new Integer(Math.round(annTopY+h)));
				    dictionary.put(teiId, thePlace);
			    }
            }
            if (uri != null) {
                PDActionURI action = new PDActionURI();
                if (uri.endsWith("fulltext/original"))
                    uri = uri.replace("fulltext/original", "fulltext/pdf");
                action.setURI(uri);
                txtLink.setAction(action);
            } else
                return;
		}
        txtLink.setRectangle(rect);

        // adding link to the reference
        page.getAnnotations().add(txtLink);

        //draw a line
        PDBorderStyleDictionary borderThick = new PDBorderStyleDictionary();
        borderThick.setWidth(1);  // 12th inch

        // adding line to the reference
        PDPageContentStream stream = new PDPageContentStream(document, page, true, false, true);
        //Random r = new Random(seed + 1);

//        stream.setStrokingColor(85, 177, 245);
        //stream.setStrokingColor(r.nextInt(255), r.nextInt(255), r.nextInt(255));
        stream.setStrokingColor(0, 0, 255);
        if (isMarker || (uri != null))
            stream.setLineWidth(lineWidth);
        else
            stream.setLineWidth(0f);
        stream.drawLine(annX, annY, annRightX, annY);
        stream.close();
    }


    /**
     *  Produce JSON annotations with PDF coordinates for web based PDF rendering. Annotations
     *  are given for bib. ref. and bib markers separately, together with the dimension of the
     *  different pages for client resizing.
     *  The annotations of the bibliographical references can be associated to an URL in
     *  order to support "clickable" bib. ref. annotations.
     *
     *  @param teiDoc the Document object resulting from the full document structuring
     *  @param resolvedBibRefUrl the list of URL to be added to the bibliographical reference
     *  annotations, if null the bib. ref. annotations are not associated to external URL.
     */
    public static String getJsonAnnotations(Document teiDoc, List<String> resolvedBibRefUrl) throws IOException, XPathException {
        StringBuilder jsonRef = new StringBuilder();
        jsonRef.append("{\"pages\" : [");

        // page height and width
        List<Page> pages = teiDoc.getPages();
        int pageNumber = 1;
        for(Page page : pages) {
            if (pageNumber > 1)
                jsonRef.append(", ");

            jsonRef.append("{\"page_height\":" + page.getHeight());
            jsonRef.append(", \"page_width\":" + page.getWidth() + "}");
            pageNumber++;
        }

        jsonRef.append("], \"refBibs\":[");
        StringBuilder jsonMark = new StringBuilder();
        jsonMark.append("\"refMarkers\":[");

        int totalMarkers1 = 0;
        int totalMarkers2 = 0;
        int totalBib = 0;

        String tei = teiDoc.getTei();
        Multimap<String, BibDataSetContext> contexts =
            BibDataSetContextExtractor.getCitationReferences(tei);
        boolean begin = true;
        boolean beginMark = true;
        int bibIndex = 0;
        for (BibDataSet cit : teiDoc.getBibDataSets()) {
            if (begin)
                begin = false;
            else
                jsonRef.append(", ");
            String teiId = cit.getResBib().getTeiId();
            totalBib++;
            jsonRef.append("{ \"id\":\"").append(teiId).append("\", ");
            // url if any - they are passed via the resolvedBibRefUrl vector provided as argument
            if ( (resolvedBibRefUrl != null) &&
                 (resolvedBibRefUrl.size()>bibIndex) &&
                 (resolvedBibRefUrl.get(bibIndex) != null) ) {
                jsonRef.append("\"url\": \"" + resolvedBibRefUrl.get(bibIndex) + "\", ");
            } else {
                // by default we put the existing url, doi or arXiv link
                BiblioItem biblio = cit.getResBib();
                String theUrl = null;
                if (!StringUtils.isEmpty(biblio.getDOI())) {
                    theUrl = "http://dx.doi.org/" + biblio.getDOI();
                } else if (!StringUtils.isEmpty(biblio.getArXivId())) {
                    theUrl = "http://arxiv.org/" + biblio.getArXivId();
                } else if (!StringUtils.isEmpty(biblio.getWeb())) {
                    theUrl = biblio.getWeb();
                }
                if (theUrl != null)
                    jsonRef.append("\"url\": \"" + theUrl + "\", ");
            }
            jsonRef.append("\"pos\":[");
            boolean begin2 = true;
            if (cit.getResBib().getCoordinates() != null) {
                for (BoundingBox b : cit.getResBib().getCoordinates()) {
                    // reference string
                    if (begin2)
                        begin2 = false;
                    else
                        jsonRef.append(", ");

                    jsonRef.append("{").append(b.toJson()).append("}");
                }
            }
            // reference markers for this reference
            for (BibDataSetContext c : contexts.get(teiId)) {
                //System.out.println(c.getContext());
                String mrect = c.getDocumentCoords();
                if ((mrect != null) && (mrect.trim().length()>0)) {
                    for (String coords : mrect.split(";")) {
                        if ((coords == null) || (coords.length() == 0))
                            continue;
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
            }
            jsonRef.append("] }");
            bibIndex++;
        }

        for (BibDataSetContext c : contexts.get("")) {
            String mrect = c.getDocumentCoords();
            if ((mrect != null) && (mrect.trim().length()>0)) {
                for (String coords : mrect.split(";")) {
                    if (coords.trim().length() == 0)
                        continue;
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
        }

        LOGGER.debug("totalBib: " + totalBib);
        LOGGER.debug("totalMarkers1: " + totalMarkers1);
        LOGGER.debug("totalMarkers2: " + totalMarkers2);

        jsonRef.append("], ").append(jsonMark.toString()).append("] }");
        return jsonRef.toString();
    }

    /*
     * A variant where annotations are provided page per page
     */
    public static String getJsonAnnotationsPerPage(Document teiDoc, List<String> resolvedBibRefUrl) throws IOException, XPathException {
        StringBuilder jsonRef = new StringBuilder();
        jsonRef.append("{\"pages\" : [");

        int totalMarkers1 = 0;
        int totalMarkers2 = 0;
        int totalBib = 0;

        List<Page> pages = teiDoc.getPages();
        int pageNumber = 1;
        for(Page page : pages) {
            if (pageNumber > 1)
                jsonRef.append(", ");

            // page height and width
            jsonRef.append("{\"page_height\":" + page.getHeight());
            jsonRef.append(", \"page_width\":" + page.getWidth());

            boolean refBibOutput = false;
            StringBuilder jsonMark = new StringBuilder();
            boolean refMarkOutput = false;

            String tei = teiDoc.getTei();
            Multimap<String, BibDataSetContext> contexts = BibDataSetContextExtractor.getCitationReferences(tei);

            boolean beginMark = true;
            boolean begin = true;
            for (BibDataSet cit : teiDoc.getBibDataSets()) {
                String teiId = cit.getResBib().getTeiId();
                boolean idOutput = false;

                boolean begin2 = true;
                if (cit.getResBib().getCoordinates() != null) {
                    for (BoundingBox b : cit.getResBib().getCoordinates()) {
                        if (b.getPage() == pageNumber) {
                            if (!refBibOutput) {
                                jsonRef.append(", \"refBibs\": [ ");
                                refBibOutput = true;
                            }
                            if (!idOutput) {
                                if (begin)
                                    begin = false;
                                else
                                    jsonRef.append(", ");
                                jsonRef.append("{\"id\":\"").append(teiId).append("\", ");
                                jsonRef.append("\"pos\":[");
                                idOutput = true;
                            }

                            // reference string
                            if (begin2)
                                begin2 = false;
                            else
                                jsonRef.append(", ");

                            jsonRef.append("{").append(b.toJson()).append("}");
                            totalBib++;
                        }
                        //annotatePage(document, b.toString(), teiId.hashCode(), contexts.containsKey(teiId) ? 1.5f : 0.5f);
                    }
                }

                // reference markers for this reference
                for (BibDataSetContext c : contexts.get(teiId)) {
                    //System.out.println(c.getContext());
                    String mrect = c.getDocumentCoords();
                    if ( (mrect != null) && (mrect.trim().length()>0) ) {
                        for (String coords : mrect.split(";")) {
                            if (coords.trim().length() == 0)
                                continue;
                            //annotatePage(document, coords, teiId.hashCode(), 1.0f);
                            BoundingBox b2 = BoundingBox.fromString(coords);
                            if (b2.getPage() == pageNumber) {
                                if (!refMarkOutput) {
                                    jsonMark.append(", \"refMarkers\": [");
                                    refMarkOutput = true;
                                } else
                                    jsonMark.append(", ");
                                jsonMark.append("{ \"id\":\"").append(teiId).append("\", ");
                                jsonMark.append(b2.toJson()).append(" }");
                                totalMarkers1++;
                            }
                        }
                    }
                }
                 if (idOutput) {
                    jsonRef.append("] }");
                }
            }

            for (BibDataSetContext c : contexts.get("")) {
                String mrect = c.getDocumentCoords();
                if ( (mrect != null) && (mrect.trim().length()>0) ) {
                    for (String coords : mrect.split(";")) {
                        if (coords.trim().length() == 0)
                            continue;
                        BoundingBox b = BoundingBox.fromString(coords);
                        if (b.getPage() == pageNumber) {
                            if (!refMarkOutput) {
                                jsonMark.append(", \"refMarkers\": [");
                                refMarkOutput = true;
                            } else
                                jsonMark.append(", ");
                            jsonMark.append("{").append(b.toJson()).append("}");
                            totalMarkers2++;
                        }
                    }
                }
            }
            pageNumber++;
            if (refBibOutput) {
                jsonRef.append("]");

            }
            if (refMarkOutput) {
                jsonRef.append(jsonMark.toString()).append("]");
            }
            jsonRef.append("}");
        }

        LOGGER.debug("totalBib: " + totalBib);
        LOGGER.debug("totalMarkers1: " + totalMarkers1);
        LOGGER.debug("totalMarkers2: " + totalMarkers2);

        jsonRef.append("]}");
        return jsonRef.toString();
    }

}
