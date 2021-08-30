package org.grobid.core.layout;

import com.google.common.base.Predicate;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Iterables;
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
//import net.sf.saxon.om.Item;
//import net.sf.saxon.om.SequenceIterator;
//import net.sf.saxon.trans.XPathException;
//import org.grobid.core.document.Document;
import org.grobid.core.exceptions.GrobidException;
import org.grobid.core.utilities.XQueryProcessor;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.batik.dom.*;
import org.apache.batik.anim.dom.SAXSVGDocumentFactory;
import org.apache.batik.util.XMLResourceDescriptor;
import org.apache.batik.bridge.BridgeContext;
import org.apache.batik.bridge.GVTBuilder;
import org.apache.batik.bridge.UserAgent;
import org.apache.batik.bridge.UserAgentAdapter;
import org.apache.batik.gvt.GraphicsNode;
import org.apache.batik.anim.dom.SVGDOMImplementation;
import org.apache.batik.anim.dom.SVGOMDocument;
import org.apache.batik.bridge.BridgeContext;
import org.apache.batik.bridge.DocumentLoader;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.svg.*;
import org.w3c.dom.NodeList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Workign with vector graphics
 */
public class VectorGraphicBoxCalculator {
    private static final Logger LOGGER = LoggerFactory.getLogger(VectorGraphicBoxCalculator.class);

    public static final int MINIMUM_VECTOR_BOX_AREA = 3000;
    public static final int VEC_GRAPHICS_FILE_SIZE_LIMIT = 100 * 1024 * 1024;

    /*public static Multimap<Integer, GraphicObject> calculate_old(org.grobid.core.document.Document document) throws IOException, XPathException {

        Multimap<Integer, Block> blockMultimap = HashMultimap.create();
        Multimap<Integer, GraphicObject> result = LinkedHashMultimap.create();

        for (int pageNum = 1; pageNum <= document.getPages().size(); pageNum++) {
            BoundingBox mainPageArea = document.getPage(pageNum).getMainArea();

            String q = XQueryProcessor.getQueryFromResources("vector-coords.xq");
            File vecFile = new File(document.getDocumentSource().getXmlFile().getAbsolutePath() + "_data", "image-" + pageNum + ".svg");
            if (vecFile.exists()) {
                if (vecFile.length() > VEC_GRAPHICS_FILE_SIZE_LIMIT) {
                    LOGGER.error("The vector file " + vecFile + " is too large to be processed, size: " + vecFile.length());
                    continue;
                }
                XQueryProcessor pr = new XQueryProcessor(vecFile);

                SequenceIterator it = pr.getSequenceIterator(q);
                Item item;
                List<BoundingBox> boxes = new ArrayList<>();

                while ((item = it.next()) != null) {
                    String c = item.getStringValue();
                    // TODO: figure out why such string are returned at all (AS:602281691082754@1520606553791)
                    if (c.equals(",,,")) {
                        continue;
                    }
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
                    if (b.area() > MINIMUM_VECTOR_BOX_AREA) {
                        result.put(pageNum, new GraphicObject(b, GraphicObjectType.VECTOR_BOX));
                    }
                }

            }
        }
        return result;
    }*/

    public static Multimap<Integer, GraphicObject> calculate(org.grobid.core.document.Document document) throws IOException {

        Multimap<Integer, Block> blockMultimap = document.getBlocksPerPage();
        Multimap<Integer, GraphicObject> result = LinkedHashMultimap.create();

        // init BATIK stuff
        SAXSVGDocumentFactory docFactory = new SAXSVGDocumentFactory(XMLResourceDescriptor.getXMLParserClassName());
        UserAgent ua = new UserAgentAdapter();
        DocumentLoader loader = new DocumentLoader(ua);
        BridgeContext ctx = new BridgeContext(ua, loader);
        ctx.setDynamicState(BridgeContext.DYNAMIC);

        for (int pageNum = 1; pageNum <= document.getPages().size(); pageNum++) {
            BoundingBox mainPageArea = document.getPage(pageNum).getMainArea();

            //String q = XQueryProcessor.getQueryFromResources("vector-coords.xq");
            File vecFile = new File(document.getDocumentSource().getXmlFile().getAbsolutePath() + "_data", "image-" + pageNum + ".svg");
            if (vecFile.exists()) {
                if (vecFile.length() > VEC_GRAPHICS_FILE_SIZE_LIMIT) {
                    LOGGER.error("The vector file " + vecFile + " is too large to be processed, size: " + vecFile.length());
                    continue;
                }
//System.out.println(pageNum + ": " + vecFile.getPath());
                //XQueryProcessor pr = new XQueryProcessor(vecFile);

                SVGDocument doc = docFactory.createSVGDocument(vecFile.getPath());

                GVTBuilder builder = new GVTBuilder();
                GraphicsNode rootGN = builder.build(ctx, doc);

                List<BoundingBox> boxes = new ArrayList<>();

                // !!!!!!
                // TBD: try to get simply the clip box instead of recomputing all the SVG area (because it can take ages in rare cases!)
                // note: getBBox on the clipBox is producing nothing because no rendering of clipBox of course, we need to find another
                // way to get the BB of clipPath
                // !!!!!!

                /*NodeList nodeList = doc.getElementsByTagNameNS("http://www.w3.org/2000/svg", "clipPath");
//System.out.println("ClipBox: we have " + nodeList.getLength() + " clipPath elements");
                if (nodeList.getLength() != 0) {
                    // iterate through the group <g> element of the SVG document
                    for (int i = 0; i < nodeList.getLength(); i++) {
                        SVGElement item = (SVGElement) nodeList.item(i);
                        SVGLocatable locatable = (SVGLocatable)item;
                        SVGRect rect = locatable.getBBox();
                        if (rect == null) {
//System.out.println("ClipBox: getBBox is null");                              
                            continue;
                        }

                        String coords = pageNum + "," + rect.getX() + "," + rect.getY() + "," + rect.getWidth() + "," + rect.getHeight();
//System.out.println("ClipBox: " + coords);                          
                        BoundingBox e = BoundingBox.fromString(coords);
                        // ill formed boxes, beyond main area and based on area
                        if (!mainPageArea.contains(e) || e.area() == 0 || e.area() / mainPageArea.area() > 0.7) {
//System.out.println("ClipBox: filter this box, area: " + e.area());                        
                            continue;
                        }

//System.out.println("ClipBox: keeping this box, area: " + e.area());  
                        boxes.add(e);
                    }
                }*/

                // we examine the <g> elements
                if (boxes.size() == 0) {
                    NodeList nodeList = doc.getElementsByTagNameNS("http://www.w3.org/2000/svg", "g");

                    if (nodeList.getLength() == 0) {
                        continue;                    
                    }

                    // check if all groups are white cache, then we skip theis SVG document
                    boolean isDummyCache = true;
                    for (int i = 0; i < nodeList.getLength(); i++) {
                        SVGElement item = (SVGElement) nodeList.item(i);
                        if (!isDummyCacheSVG(item)) {
                            isDummyCache = false;
                            break;
                        }
                    }

                    if (isDummyCache) {
//System.out.println("page " + pageNum + ": SVG document only white cache, skipping...");                   
                        continue;
                    }

                    // iterate through the group <g> element of the SVG document
                    for (int i = 0; i < nodeList.getLength(); i++) {
                        SVGElement item = (SVGElement) nodeList.item(i);
                        SVGLocatable locatable = (SVGLocatable)item;
                        SVGRect rect = locatable.getBBox();
                        if (rect == null) 
                            continue;
                        
                        String coords = pageNum + "," + rect.getX() + "," + rect.getY() + "," + rect.getWidth() + "," + rect.getHeight();
                        
//System.out.println(coords);

                        BoundingBox e = BoundingBox.fromString(coords);
                        // ill formed boxes, beyond main area and based on area
                        if (!mainPageArea.contains(e) || e.area() == 0 || e.area() / mainPageArea.area() > 0.7) {
//System.out.println("filter this box, area: " + e.area());                        
                            continue;
                        }

//System.out.println("keeping this box, area: " + e.area());  
                        boxes.add(e);
                    }
                }

//System.out.println("nb boxes: " + boxes.size());
                List<BoundingBox> remainingBoxes = mergeBoxes(boxes);
//System.out.println("nb remainingBoxes: " + remainingBoxes.size());

                boxes = new ArrayList<>();
                for(BoundingBox box : remainingBoxes) {
                    // isolated vertical and horizontal lines: note they should be kept for segmenting, but not as graphic objects
                    if (box.getHeight() < 1) {
//System.out.println("filter this box, height: " + box.getHeight());                        
                        continue;                        
                    }
                    if (box.getWidth() < 1) {
//System.out.println("filter this box, width: " + box.getWidth());
                        continue;                        
                    }

                    boxes.add(box);
                }

                remainingBoxes = boxes;

                // bound intersecting or very close blocks with text, this is typically to cover
                // the case where the text is inside or touching the svg
                for (int i = 0; i < remainingBoxes.size(); i++) {
                    Collection<Block> col = blockMultimap.get(pageNum);
                    for (Block bl : col) {
//                      if (!bl.getPage().getMainArea().contains(b)) {
//                          continue;
//                      }

                        BoundingBox b = BoundingBox.fromPointAndDimensions(pageNum, bl.getX(), bl.getY(), bl.getWidth(), bl.getHeight());
                        if (remainingBoxes.get(i).intersect(b)) {
                            remainingBoxes.set(i, remainingBoxes.get(i).boundBox(b));
                        }

                        /*if (remainingBoxes.get(i).distanceTo(b) < 10) {
                            remainingBoxes.set(i, remainingBoxes.get(i).boundBox(b));
                        }*/
                    }
                }

                remainingBoxes = mergeBoxes(remainingBoxes);
                /*remainingBoxes = glueBoxes(remainingBoxes, 10.0);
                remainingBoxes = glueBoxes(remainingBoxes, 10.0);
                remainingBoxes = glueBoxes(remainingBoxes, 10.0);
                remainingBoxes = mergeBoxes(remainingBoxes);*/

//System.out.println("nb remainingBoxes after merge: " + remainingBoxes.size());
                int layoutTokenPageStartIndex = -1;
                for (BoundingBox b : remainingBoxes) {
                    if (b.area() > MINIMUM_VECTOR_BOX_AREA) {
                        GraphicObject theGraphicObject = new GraphicObject(b, GraphicObjectType.VECTOR_BOX);
                        
                        int startPos = -1;
                        int endPos = -1;

                        if (layoutTokenPageStartIndex == -1) {
                            for(int l=0; l < document.getTokenizations().size(); l++) {
                                LayoutToken token = document.getTokenizations().get(l);
                                if (token.getPage() != pageNum)
                                    continue;
                                else {
                                    layoutTokenPageStartIndex = l;
                                    break;
                                }
                            }
                        }

                        if (layoutTokenPageStartIndex == -1) {
                            // no token on the page where the graphic object belongs     
                            continue;
                        }
//System.out.println("svg graphic object: " + b.toString());
                        // we add the LayoutToken included in this Graphic Object bounding box:
                        for (int k = layoutTokenPageStartIndex; k<document.getTokenizations().size(); k++) {
                            LayoutToken theToken = document.getTokenizations().get(k);
                            if (theToken.getPage() > pageNum)
                                break;
                            if (b.intersect(BoundingBox.fromLayoutToken(theToken))) {
                                theGraphicObject.addLayoutToken(theToken);
//System.out.println("add layout token: " + theToken.getText() + " / " + BoundingBox.fromLayoutToken(theToken).toString());
                                if (startPos == -1 || k < startPos)
                                    startPos = k;
                                if (k > endPos)
                                    endPos = k;
                            }
                        }

                        theGraphicObject.setStartPosition(startPos);
                        theGraphicObject.setEndPosition(endPos);

                        result.put(pageNum, theGraphicObject);

                        // TBD: if necessary - add SVG and bitmap file paths to the aggregated Graphic Object

//System.out.println("kept: " + b.toString());                       
                    } else {
//System.out.println("too small: " + b.toString());                          
                    }
                }
            }
        }

/*for (int pageNum = 1; pageNum <= document.getPages().size(); pageNum++) {
    Collection<GraphicObject> elements = result.get(pageNum);
    if (elements != null)
        System.out.println("   -> page " + pageNum + ": " + elements.size());
}*/

        return result;
    }

    public static boolean isDummyCacheSVG(SVGElement item) {
        /**
         * segmented SVG with only groups corresponding to white filling cache can be skipped
         * because they are simply visual white cache area for aethetics and not actual 
         * graphics
         * The style of such groups is typically
         *   style="fill: #FFFFFF;fill-opacity: 1;"
         *
         */
        String styleValue = item.getAttribute("style");
//System.out.println(styleValue);
        if (styleValue != null) {
            String[] attributeValues = styleValue.split(";");
            if (attributeValues.length > 0) {
                for(int i=0; i<attributeValues.length; i++) {
                    String attributeValue = attributeValues[i];
//System.out.println(attributeValue);
                    int ind = attributeValue.indexOf(":");
                    if (ind != -1) {
                        String attr = attributeValue.substring(0,ind).trim().toLowerCase();
                        String val = attributeValue.substring(ind+1, attributeValue.length()).trim().toLowerCase();
//System.out.println(attr + " / " + val);
                        if (attr.equals("fill") && (val.equals("#ffffff") || val.equals("#fff") || val.equals("white"))) {
                            return true;
                        }
                    }
                }
            }
        }

        return false;
    }

    /**
     * Merge bounding boxes in case of intersection
     */
    public static List<BoundingBox> mergeBoxes(List<BoundingBox> boxes) {
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
                /*if (boundingBox.getHeight() < 5 || boundingBox.getWidth() < 5) {
                    return false;
                }*/
                return true;
            }
        }));
    }

    /**
     * Merge bounding boxes in case of close proximity defined by a max proximity distance
     */
    public static List<BoundingBox> glueBoxes(List<BoundingBox> boxes, double maxProximityDistance) {
        boolean allMerged = false;
        while (!allMerged) {
            allMerged = true;
            for (int i = 0; i < boxes.size(); i++) {
                BoundingBox a = boxes.get(i);
                if (a == null) continue;
                for (int j = i + 1; j < boxes.size(); j++) {
                    BoundingBox b = boxes.get(j);
                    if (b != null) {
                        if (a.distanceTo(b) < maxProximityDistance) {
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
