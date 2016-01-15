package org.grobid.core.visualization;

import com.google.common.base.Predicates;
import com.google.common.collect.Iterables;
import net.sf.saxon.om.Item;
import net.sf.saxon.om.SequenceIterator;
import net.sf.saxon.trans.XPathException;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.grobid.core.document.Document;
import org.grobid.core.engines.Engine;
import org.grobid.core.engines.config.GrobidAnalysisConfig;
import org.grobid.core.factory.GrobidFactory;
import org.grobid.core.layout.Block;
import org.grobid.core.layout.BoundingBox;
import org.grobid.core.main.LibraryLoader;
import org.grobid.core.utilities.GrobidProperties;
import org.grobid.core.utilities.XQueryProcessor;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by zholudev on 15/01/16.
 * Visualizing blocks
 */
public class BlockVisualizer {

    public static void main(String[] args) {
        try {
//            File input = new File("/Work/temp/context/coords/1.pdf");
            File input = new File("/Work/temp/figureExtraction/2.pdf");

            final PDDocument document = PDDocument.load(input);
            File outPdf = new File("/tmp/test.pdf");

            GrobidProperties.set_GROBID_HOME_PATH("grobid-home");
            GrobidProperties.setGrobidPropertiesPath("grobid-home/config/grobid.properties");
            LibraryLoader.load();
            final Engine engine = GrobidFactory.getInstance().getEngine();
            GrobidAnalysisConfig config = new GrobidAnalysisConfig.GrobidAnalysisConfigBuilder()
                    .pdfAssetPath(new File("/tmp/x"))
                    .matchingMode(1)
                    .build();


//            Document teiDoc = engine.fullTextToTEIDoc(input, config);

//            PDDocument out = annotateBlocks(document, teiDoc);
            PDDocument out = annotateBlocks(document, null);

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

    private static PDDocument annotateBlocks(PDDocument document, Document teiDoc) throws IOException, XPathException {

//        for (Block b : teiDoc.getBlocks()) {
//            AnnotationUtil.annotatePage(document, b.getPageNumber() + "," + b.getX() + "," + b.getY() +
//                    "," + b.getWidth() + "," + b.getHeight(), 0);
//        }


        for (int pageNum = 1; pageNum <= document.getNumberOfPages(); pageNum++) {
            String q = "\n" +
                    "for $g  in //GROUP return\n" +
                    "\n" +
                    "  let $x1 := min(($g//*/@x, $g//*/@x1, $g//*/@x2, $g//*/@x3))\n" +
                    "  let $y1 := min(($g/*/@y, $g//*/@y1, $g//*/@y2, $g//*/@y3))\n" +
                    "  let $x2 := max(($g/*/@x, $g//*/@x1, $g//*/@x2, $g//*/@x3))\n" +
                    "  let $y2 := max(($g/*/@y, $g//*/@y1, $g//*/@y2, $g//*/@y3))\n" +
                    "  return concat($x1, \",\", $y1, \",\", $x2 - $x1, \",\", $y2 - $y1)";
            XQueryProcessor pr = new XQueryProcessor(new File("/Work/temp/figureExtraction/xmldata_2/image-" + pageNum + ".vec"));
            SequenceIterator it = pr.getSequenceIterator(q);
            Item item;
            List<BoundingBox> boxes = new ArrayList<>();

            while ((item = it.next()) != null) {
                String c = item.getStringValue();
                String coords = pageNum + "," + c;
                BoundingBox e = BoundingBox.fromString(coords);
                //TODO: detect borders
                if (e.getX() == 0 && e.getY() == 0 || (e.getX() < 65 && e.getPage() % 2 == 0 || e.getX() < 50 && e.getPage() % 2 != 0) || e.getY() < 10) {
                    continue;
                }
                boxes.add(e);
//                AnnotationUtil.annotatePage(document, coords, 1);
            }

            boolean allMerged = false;
            while (!allMerged) {
                allMerged = true;
                for (int i = 0; i < boxes.size(); i++) {
                    BoundingBox a = boxes.get(i);
                    if (a == null) continue;
                    for (int j = i + 1; j < boxes.size(); j++) {
                        BoundingBox b = boxes.get(j);
                        if (b != null) {
                            if (a.intersect(b)) {
                                allMerged = false;
                                a = a.boundBox(b);
                                boxes.set(i, a);
                                boxes.set(j, null);
                            }
                        }
                    }
                }
            }

            for (BoundingBox b : Iterables.filter(boxes, Predicates.notNull())) {
                AnnotationUtil.annotatePage(document, b.toString(), 1);
            }
        }


        return document;
    }
}
