package org.grobid.core.visualization;

import com.google.common.base.Predicate;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import net.sf.saxon.om.Item;
import net.sf.saxon.om.SequenceIterator;
import net.sf.saxon.trans.XPathException;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.grobid.core.document.Document;
import org.grobid.core.document.DocumentSource;
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
import java.util.Collection;
import java.util.List;

/**
 * Created by zholudev on 15/01/16.
 * Visualizing blocks
 */
public class BlockVisualizer {

    public static void main(String[] args) {
        try {
//            File input = new File("/Work/temp/context/coords/2.pdf");
//            File input = new File("/Work/temp/figureExtraction/vector/5.pdf");
//            File input = new File("/Work/temp/figureExtraction/6.pdf");
//            File input = new File("/Work/temp/context/1000k/AS_97568985976833_1400273667294.pdf");
//            File input = new File("/Work/temp/figureExtraction/newtest/3.pdf");
//            File input = new File("/Users/zholudev/Downloads/pone.0005635.pdf");
//            File input = new File("/Work/temp/figureExtraction/newtest/1.pdf");
//            File input = new File("/Users/zholudev/Downloads/TIA_2011_Partie8.pdf");
            File input = new File("/Users/zholudev/Downloads/AS-177339946250240@1419292546444_content_1.pdf");
//            File input = new File("/Users/zholudev/Downloads/journal.pone.0146695.pdf");

            final PDDocument document = PDDocument.load(input);
            File outPdf = new File("/tmp/test.pdf");

            GrobidProperties.set_GROBID_HOME_PATH("grobid-home");
            GrobidProperties.setGrobidPropertiesPath("grobid-home/config/grobid.properties");
            LibraryLoader.load();
            final Engine engine = GrobidFactory.getInstance().getEngine();
            GrobidAnalysisConfig config = new GrobidAnalysisConfig.GrobidAnalysisConfigBuilder()
                    .pdfAssetPath(new File("/tmp/x"))
                    .build();

//            File tempFile = File.createTempFile("temp", ".xml", new File("/tmp"));
            DocumentSource documentSource = DocumentSource.fromPdf(input);


            Document teiDoc = engine.fullTextToTEIDoc(input, config);

            PDDocument out = annotateBlocks(document, documentSource.getXmlFile(), teiDoc, false, true, true);
//            PDDocument out = annotateBlocks(document, documentSource.getXmlFile(), null);

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

    public static PDDocument annotateBlocks(PDDocument document, File xmlFile, Document teiDoc,
                                            boolean visualizeBlocks,
                                            boolean visualizePageMainArea,
                                            boolean visualizeVectorGraphics) throws IOException, XPathException {

        Multimap<Integer, Block> blockMultimap = HashMultimap.create();


        for (Block b : teiDoc.getBlocks()) {
            if (visualizeBlocks) {
                AnnotationUtil.annotatePage(document, b.getPageNumber() + "," + b.getX() + "," + b.getY() +
                        "," + b.getWidth() + "," + b.getHeight(), 0);
                blockMultimap.put(b.getPageNumber(), b);
            }
        }


        for (int pageNum = 1; pageNum <= document.getNumberOfPages(); pageNum++) {
            BoundingBox mainPageArea = teiDoc.getPage(pageNum).getMainArea();
            if (visualizePageMainArea) {
                AnnotationUtil.annotatePage(document,
                        mainPageArea.toString(), 10);
            }


            String q = XQueryProcessor.getQueryFromResources("vector-coords.xq");
            XQueryProcessor pr = new XQueryProcessor(new File(xmlFile.getAbsolutePath() + "_data", "image-" + pageNum + ".vec"));
            SequenceIterator it = pr.getSequenceIterator(q);
            Item item;
            List<BoundingBox> boxes = new ArrayList<>();

            while ((item = it.next()) != null) {
                String c = item.getStringValue();
                String coords = pageNum + "," + c;
                BoundingBox e = BoundingBox.fromString(coords);

                if (!mainPageArea.contains(e) || e.area() / mainPageArea.area() > 0.8) {
                    continue;
                }


                AnnotationUtil.annotatePage(document, e.toString(), 3);

                boxes.add(e);
            }

            if (visualizeVectorGraphics) {
                List<BoundingBox> remainingBoxes = mergeBoxes(boxes);

                for (int i = 0; i < remainingBoxes.size(); i++) {
                    Collection<Block> col = blockMultimap.get(pageNum);
                    for (Block bl : col) {
//                    if (!bl.getPage().getMainArea().contains(b)) {
//                        continue;
//                    }

                        BoundingBox b = BoundingBox.fromPointAndDimensions(pageNum, bl.getX(), bl.getY(), bl.getWidth(), bl.getHeight());
                        if (remainingBoxes.get(i).intersect(b)) {
                            remainingBoxes.set(i, remainingBoxes.get(i).boundBox(b));
                        }
                    }
                }

                remainingBoxes = mergeBoxes(remainingBoxes);

                for (BoundingBox b : remainingBoxes) {
                    AnnotationUtil.annotatePage(document, b.toString(), 1);
                }
            }
        }


        return document;
    }

    private static List<BoundingBox> mergeBoxes(List<BoundingBox> boxes) {
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

        return Lists.newArrayList(Iterables.filter(boxes, new Predicate<BoundingBox>() {
            @Override
            public boolean apply(BoundingBox boundingBox) {
                if (boundingBox == null) {
                    return false;
                }
                if (boundingBox.getHeight() < 5 || boundingBox.getWidth() < 5) {
                    return false;
                }
                return true;
            }
        }));
    }
}
