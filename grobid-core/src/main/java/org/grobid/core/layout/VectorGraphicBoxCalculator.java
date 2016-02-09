package org.grobid.core.layout;

import com.google.common.base.Predicate;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Iterables;
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import net.sf.saxon.om.Item;
import net.sf.saxon.om.SequenceIterator;
import net.sf.saxon.trans.XPathException;
import org.grobid.core.document.Document;
import org.grobid.core.utilities.XQueryProcessor;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by zholudev on 29/01/16.
 * Workign with vector graphics
 */
public class VectorGraphicBoxCalculator {

    public static Multimap<Integer, GraphicObject> calculate(Document document) throws IOException, XPathException {

        Multimap<Integer, Block> blockMultimap = HashMultimap.create();

        Multimap<Integer, GraphicObject> result = LinkedHashMultimap.create();

        for (Block b : document.getBlocks()) {
//            if (visualizeBlocks) {
//                AnnotationUtil.annotatePage(document, b.getPageNumber() + "," + b.getX() + "," + b.getY() +
//                        "," + b.getWidth() + "," + b.getHeight(), 0);
//                blockMultimap.put(b.getPageNumber(), b);
//            }
        }

        for (int pageNum = 1; pageNum <= document.getPages().size(); pageNum++) {
            BoundingBox mainPageArea = document.getPage(pageNum).getMainArea();

            String q = XQueryProcessor.getQueryFromResources("vector-coords.xq");
            XQueryProcessor pr = new XQueryProcessor(new File(document.getDocumentSource().getXmlFile().getAbsolutePath() + "_data", "image-" + pageNum + ".vec"));
            SequenceIterator it = pr.getSequenceIterator(q);
            Item item;
            List<BoundingBox> boxes = new ArrayList<>();

            while ((item = it.next()) != null) {
                String c = item.getStringValue();
                String coords = pageNum + "," + c;
                BoundingBox e = BoundingBox.fromString(coords);
                if (!mainPageArea.contains(e) || e.area() / mainPageArea.area() > 0.7) {
                    continue;
                }
                boxes.add(e);
            }


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
                result.put(pageNum, new GraphicObject(b, GraphicObjectType.VECTOR_BOX));
            }
//            result.putAll(pageNum, remainingBoxes);

//            for (BoundingBox b : remainingBoxes) {
//                AnnotationUtil.annotatePage(document, b.toString(), 1);
//            }

        }

        return result;
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
