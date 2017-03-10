package org.grobid.core.visualization;

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
import org.grobid.core.layout.BoundingBox;

import java.io.File;
import java.io.IOException;
import java.util.Random;

/**
 * Created by zholudev on 15/01/16.
 * Utilities for annotating PDF
 */
public class AnnotationUtil {


    public static BoundingBox getBoundingBoxForPdf(PDDocument document, String coords) {
        String[] split = coords.split(",");

        Long pageNum = Long.valueOf(split[0], 10) - 1;
        PDPage page = (PDPage) document.getDocumentCatalog().getAllPages().get(pageNum.intValue());

        PDRectangle mediaBox = page.getMediaBox() != null ? page.getMediaBox() : page.getArtBox();
        if (mediaBox == null) {
            System.out.println("Null mediabox for page: " + (pageNum + 1));
            return null;
        }
        float height = mediaBox.getHeight();
        float lowerX = mediaBox.getLowerLeftX();
        float lowerY = mediaBox.getLowerLeftY();

        float x = Float.parseFloat(split[1]);
        float y = Float.parseFloat(split[2]);
        float w = Float.parseFloat(split[3]);
		String nextString = split[4];
		if (nextString.indexOf(";") != -1)
			nextString = nextString.substring(0, nextString.indexOf(";"));
        float h = Float.parseFloat(nextString);

        float annX = x + lowerX;
        float annY = (height - (y + h)) + lowerY;
        float annRightX = x + w + lowerX;
        float annTopY = height - y + lowerY;
        return BoundingBox.fromTwoPoints(pageNum.intValue(), annX, annY, annRightX, annTopY);
    }

    public static void annotatePage(PDDocument document, String coords, int seed) throws IOException {
        annotatePage(document, coords, seed, 1);
    }

    public static void annotatePage(PDDocument document, String coords, int seed, int lineWidth) throws IOException {
        System.out.println("Annotating for coordinates: " + coords);

        BoundingBox box = getBoundingBoxForPdf(document, coords);
        if (box == null) {
            System.out.println("Null bounding box for coords: " + coords);
            return;
        }

        PDPage page = (PDPage) document.getDocumentCatalog().getAllPages().get(box.getPage());
        float annX = (float) box.getX();
        float annY = (float) box.getY();
        float annRightX = (float) box.getX2();
        float annTopY = (float) box.getY2();

        PDRectangle rect = new PDRectangle();

        rect.setLowerLeftX(annX);
        rect.setLowerLeftY(annY);
        rect.setUpperRightX(annRightX);
        rect.setUpperRightY(annTopY);

        PDBorderStyleDictionary borderULine = new PDBorderStyleDictionary();
        borderULine.setStyle(PDBorderStyleDictionary.STYLE_SOLID);
        // so that a border is not visible at all
        borderULine.setWidth(0);


        PDAnnotationLink txtLink = new PDAnnotationLink();
        txtLink.setBorderStyle(borderULine);

        //linkColor rectangle border color (ideally, should be transparent)
        COSArray linkColor = new COSArray();

        Random r = new Random(seed);


//        linkColor.setFloatArray(new float[]{r.nextInt(128) + 127, r.nextInt(255), r.nextInt(255)});
        linkColor.setFloatArray(new float[]{224, 9, 56});
        txtLink.setColour(new PDGamma(linkColor));
        txtLink.setReadOnly(true);

        //so that
        txtLink.setHighlightMode(PDAnnotationLink.HIGHLIGHT_MODE_PUSH);

        PDActionURI action = new PDActionURI();
        action.setURI("");
        txtLink.setAction(action);
        txtLink.setRectangle(rect);


        // ADDING LINK TO THE REFERENCE
//        page.getAnnotations().add(txtLink);

        //draw a line
        PDBorderStyleDictionary borderThick = new PDBorderStyleDictionary();
        borderThick.setWidth(lineWidth);  // 12th inch

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
//        Random r = new Random(seed + 1);
//
//
////        stream.setStrokingColor(85, 177, 245);
        stream.setStrokingColor(r.nextInt(255), r.nextInt(255), r.nextInt(255));
        stream.setLineWidth(lineWidth);
        stream.drawLine(annX, annY, annRightX, annY);
        stream.drawLine(annX, annTopY, annRightX, annTopY);
        stream.drawLine(annX, annY, annX, annTopY);
        stream.drawLine(annRightX, annY, annRightX, annTopY);
        stream.close();
//        }
//        return 1;
    }

    public static String getCoordString(BoundingBox b) {
        return b.getPage() + "," + b.getX() + "," + b.getY() + "," + b.getWidth() + "," + b.getHeight();
    }

    public static String getCoordString(int page, double x, double y, double w, double h) {
        return page + "," + x + "," + y + "," + w + "," + h;
    }

    public static void main(String[] args) throws IOException, COSVisitorException {

        final PDDocument document = PDDocument.load("/Users/zholudev/Downloads/0711.4671.pdf");
        String[] coords = new String[]{
                "33,185.90,529.23,88.36,11.97",
                "33,279.29,529.23,56.05,11.97",
                "33,340.49,529.23,86.13,11.97",
                "33,431.89,529.23,67.09,11.97",
                "33,504.25,529.23,35.67,11.97",
                "33,57.60,545.19,25.70,11.97",
                "33,88.93,545.19,64.73,11.97",
                "33,183.16,545.19,60.15,11.97",
                "33,175.12,592.83,21.73,11.97",
                "33,270.90,592.83,84.50,11.97",

        };

        for (String c : coords) {
            annotatePage(document, c, 1);
        }

        document.save(new File("/tmp/x.pdf"));
    }

}
