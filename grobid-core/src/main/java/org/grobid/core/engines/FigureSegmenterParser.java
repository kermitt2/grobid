package org.grobid.core.engines;

import org.apache.commons.lang3.tuple.Pair;

import org.grobid.core.GrobidModels;
import org.grobid.core.engines.tagging.GenericTagger;
import org.grobid.core.engines.tagging.TaggerFactory;
import org.grobid.core.document.Document;
import org.grobid.core.document.DocumentPiece;
import org.grobid.core.document.DocumentPointer;
import org.grobid.core.document.BasicStructureBuilder;
import org.grobid.core.engines.label.SegmentationLabels;
import org.grobid.core.engines.label.TaggingLabels;
import org.grobid.core.engines.label.TaggingLabel;
import org.grobid.core.engines.tagging.GenericTaggerUtils;
import org.grobid.core.exceptions.GrobidException;
import org.grobid.core.features.FeatureFactory;
import org.grobid.core.features.FeaturesVectorFigureSegmenter;
import org.grobid.core.layout.Block;
import org.grobid.core.layout.Page;
import org.grobid.core.layout.LayoutToken;
import org.grobid.core.layout.LayoutTokenization;
import org.grobid.core.layout.GraphicObject;
import org.grobid.core.layout.BoundingBox;
import org.grobid.core.layout.GraphicObjectType;
import org.grobid.core.tokenization.LabeledTokensContainer;
import org.grobid.core.tokenization.TaggingTokenSynchronizer;
import org.grobid.core.tokenization.TaggingTokenCluster;
import org.grobid.core.tokenization.TaggingTokenClusteror;
import org.grobid.core.utilities.BoundingBoxCalculator;
import org.grobid.core.utilities.TextUtilities;
import org.grobid.core.utilities.Triple;
import org.grobid.core.utilities.LayoutTokensUtil;

import com.google.common.collect.Multimap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.SortedSet;
import java.util.regex.Matcher;
import java.util.Map;
import java.util.HashMap;
import java.util.Comparator; 

/**
 * A model for segmenting the figure areas. The model is applied after the Segmentation model and
 * work as follow:
 * - we identify first graphic objects which are possible figures, as the result of bitmap and SVG 
 *   object 2D aggregations, these graphic boxes are the figure anchors
 * - we try to extend each figure anchor by labeling the sequence of LayoutToken before and after
 *   the graphic object in the Document
 * - after decoding of the sequence labeling, we have an area corresponding to the full figure zone,
 *   including captions, titles, etc., ready to be further structured by the figure model
 * - the different valid figure areas are added in the Document object as additional segmentation
 *   results, to be excluded by the other models (in particular the full text model)
 *
 * Contrary to the segmentation model, which is line-based, this model is working at Layout Token 
 * granularity (e.g. every token receives a label decision).
 *
 */
public class FigureSegmenterParser {
    private static final Logger LOGGER = LoggerFactory.getLogger(FigureSegmenterParser.class);

    // projection scale for line length
    private static final int LINESCALE = 10;

    // extension from graphic object to be considered, in number of blocks
    private static int EXTENSION_SIZE = 4;

    private final GenericTagger figureSegmenterParserUp;
    private final GenericTagger figureSegmenterParserDown;
    
    // direction of the labeling from the graphic object
    public enum Direction {
        UP, DOWN 
    }

    public FigureSegmenterParser() {
        figureSegmenterParserUp = null;//TaggerFactory.getTagger(GrobidModels.FIGURE_SEGMENTER_UP);
        figureSegmenterParserDown = null;//TaggerFactory.getTagger(GrobidModels.FIGURE_SEGMENTER_DOWN);
    }

    public Document extract(Document doc) {

        // figure anchors are based on VectorGraphicBoxCalculator, which aggregate bitmap and SVG elements
        List<GraphicObject> figureAnchors = this.initFigureAnchors(doc);

        // for each figure anchor, we generate sequence to be labeled with features
        Pair<List<String>,List<LayoutTokenization>> featureObjectUp = this.getAreasFeatured(doc, figureAnchors, Direction.UP);
        Pair<List<String>,List<LayoutTokenization>> featureObjectDown = this.getAreasFeatured(doc, figureAnchors, Direction.DOWN);
        
        List<String> contentsUp = featureObjectUp.getLeft();
        List<String> contentsDown = featureObjectDown.getLeft();
        List<LayoutTokenization> theTokenizationsUp = featureObjectUp.getRight();
        List<LayoutTokenization> theTokenizationsDown = featureObjectDown.getRight();

        // we label the sequences to extend or not the figure anchor
        String labelledResultsUp = null;
        if (contentsUp != null && contentsUp.size() > 0) {
            try {
                GenericTagger tagger = figureSegmenterParserUp;
                labelledResultsUp = tagger.label(contentsUp);
            } catch(Exception e) {
                throw new GrobidException("Sequence labeling upper direction in figure-segmenter fails.", e);
            }
        }

        String labelledResultsDown = null;
        if (contentsDown != null && contentsDown.size() > 0) {
            try {
                GenericTagger tagger = figureSegmenterParserDown;
                labelledResultsDown = tagger.label(contentsDown);
            } catch(Exception e) {
                throw new GrobidException("Sequence labeling down in figure-segmenter fails.", e);
            }
        }

        // validate and set the figure areas in the Document object
        doc = BasicStructureBuilder.figureResultSegmentation(doc, figureAnchors, 
                                                             labelledResultsUp, theTokenizationsUp, 
                                                             labelledResultsDown, theTokenizationsDown);
        
        return doc;
    }

    private List<GraphicObject> initFigureAnchors(Document doc) {
        // update images list
        List<GraphicObject> figureAnchors = new ArrayList<>();

        Multimap<Integer, GraphicObject> imagesPerPage = doc.getImagesPerPage() ;

        // to be sorted
        HashSet<Integer> theKeys = new HashSet<>(imagesPerPage.keySet());
        List<Integer> keys = new ArrayList<Integer>(theKeys);
        Collections.sort(keys);
        for (Integer pageNum : keys) {
            Collection<GraphicObject> elements = imagesPerPage.get(pageNum);
            if (elements != null) {
                for(GraphicObject element : elements) {
                    figureAnchors.add(element);
                }
            }
        }

        return figureAnchors;
    }

    /**
     * Addition of the features at layout token level for the areas before and after the figure anchors.
     *
     */
    private Pair<List<String>,List<LayoutTokenization>> getAreasFeatured(Document doc, List<GraphicObject> figureAnchors, Direction direction) {
        List<Block> blocks = doc.getBlocks();
        if ((blocks == null) || blocks.size() == 0) {
            return null;
        }
        List<LayoutToken> tokenizations = doc.getTokenizations();

        List<String> results = new ArrayList<>();
        List<LayoutTokenization> tokenizationsFigures = new ArrayList<>();

        for (GraphicObject figureAnchor : figureAnchors) {
            
            StringBuilder content = new StringBuilder();
            List<LayoutToken> localTokenization = new ArrayList<>();

            // position of the blocks in the page where the GraphicObject is located
            List<Block> pageBlocks = null;
            Page currentPage = null;

            for (Page page : doc.getPages()) {
                if (page.getNumber() == figureAnchor.getPage()) {
                    currentPage = page;
                    pageBlocks = page.getBlocks();
                    break;
                }
            }

            if (currentPage == null || pageBlocks == null || pageBlocks.size() == 0) {
                // we can't process this malformed graphic object (it should not happen ;)
                // TBD: maybe it's still possible to have a vector graphics alone in a page without any blocks
                continue;
            }

            int startGraphicPos = figureAnchor.getStartPosition();
            int endGraphicPos = figureAnchor.getEndPosition();
            

            // we find the closest block before and after the graphic element and extend in both directions
            // end of extension is 1) page start/end 2) EXTENSION_SIZE reached
            // to be considered: 3) non body or annex segments ?

            //if (figureAnchor.getType() == GraphicObjectType.BITMAP) 
            
                // simple reorder of blocks following reading order
                //pageBlocks = readingReorder(pageBlocks);

            BoundingBox graphicBox = figureAnchor.getBoundingBox();
System.out.println("\n\ngraphicBox: " + graphicBox.toString() + ", startGraphicPos: " + startGraphicPos + ", endGraphicPos: " + endGraphicPos);    
if (startGraphicPos != -1 && endGraphicPos != -1) {
for(int i=startGraphicPos; i<=endGraphicPos; i++) {
    System.out.print(tokenizations.get(i));
}
System.out.println("");
System.out.println("------------------------------------");
}

            if (direction == Direction.UP) {
                List<Block> blocksUp = new ArrayList<>();
                Block closestBlockUp = getClosestUp(pageBlocks, graphicBox);

                if (closestBlockUp != null) {
                    blocksUp.add(closestBlockUp);
                } else {
                    // look on the immediate left
                    closestBlockUp = getImmediateBlockLeft(pageBlocks, graphicBox);
                    if (closestBlockUp != null) {
                        blocksUp.add(closestBlockUp);
                    }
                }
                
                // extend blocksUp 
                if (closestBlockUp != null) {
                    List<Block> workPageBlocks = new ArrayList<>(pageBlocks);
                    workPageBlocks.remove(closestBlockUp);
                    Block nextClosestBlockUp = closestBlockUp;
                    while (workPageBlocks.size() > 0 && nextClosestBlockUp != null && blocksUp.size() < EXTENSION_SIZE) {
                        nextClosestBlockUp = getClosestUp(workPageBlocks, nextClosestBlockUp.getBoundingBox());
                        if (nextClosestBlockUp != null) {
                            blocksUp.add(0, nextClosestBlockUp);
                            workPageBlocks.remove(nextClosestBlockUp);
                        }
                    }
                }

                // follow selected blocks up until the graphic object
                if (blocksUp.size() > 0) {
                    for(Block block : blocksUp) {
                        for(int i=block.getStartToken(); i<=block.getEndToken(); i++) {
                            localTokenization.add(tokenizations.get(i));
                            FeaturesVectorFigureSegmenter features = this.createFeatureVector(tokenizations, i, pageBlocks, direction);
                            if (features != null) {
                                content.append(features.printVector());
                            }
                        }
                    }
                }

                // we arrive at the possible graphic object layout token content
                if (startGraphicPos != endGraphicPos) {
                    for(int i=startGraphicPos; i<=endGraphicPos; i++) {
                        localTokenization.add(tokenizations.get(i));
                        FeaturesVectorFigureSegmenter features = this.createFeatureVector(tokenizations, i, pageBlocks, direction);
                        if (features != null) {
                            features.inGraphicBox = true;
                            content.append(features.printVector());
                        }
                    }
                } 
                if (startGraphicPos == -1 && closestBlockUp != null) {
                    startGraphicPos = closestBlockUp.getEndToken();
                    figureAnchor.setStartPosition(startGraphicPos);

                }
                if (endGraphicPos == -1 ) {
                    figureAnchor.setEndPosition(startGraphicPos);
                }
                
            } else {
                List<Block> blocksDown = new ArrayList<>();
                Block closestBlockDown = getClosestDown(pageBlocks, graphicBox);;

                if (closestBlockDown != null) {
                    blocksDown.add(closestBlockDown);
                } else {
                    //look on the immediate right
                    closestBlockDown = getImmediateBlockRight(pageBlocks, graphicBox);
                    if (closestBlockDown != null) {
                        blocksDown.add(closestBlockDown);
                    }
                }

                // extend blocksDown
                if (closestBlockDown != null) {
                    List<Block> workPageBlocks = new ArrayList<>(pageBlocks);
                    workPageBlocks.remove(closestBlockDown);
                    Block nextClosestBlockDown = closestBlockDown;
                    while (workPageBlocks.size() > 0 && nextClosestBlockDown != null && blocksDown.size() < EXTENSION_SIZE) {
                        nextClosestBlockDown = getClosestDown(workPageBlocks, nextClosestBlockDown.getBoundingBox());
                        if (nextClosestBlockDown != null) {
                            blocksDown.add(nextClosestBlockDown);
                            workPageBlocks.remove(nextClosestBlockDown);
                        }
                    }
                }
                
                // we start at the possible graphic object layout token content
                if (startGraphicPos != endGraphicPos) {
                    for(int i=startGraphicPos; i<=endGraphicPos; i++) {
                        localTokenization.add(tokenizations.get(i));
                        FeaturesVectorFigureSegmenter features = this.createFeatureVector(tokenizations, i, pageBlocks, direction);
                        if (features != null) {
                            features.inGraphicBox = true;
                            content.append(features.printVector());
                        }
                    }
                } 
                
                if (endGraphicPos == -1 && closestBlockDown != null) {
                    endGraphicPos = closestBlockDown.getStartToken();
                    figureAnchor.setEndPosition(endGraphicPos);
                }
                if (startGraphicPos == -1) {
                    figureAnchor.setStartPosition(endGraphicPos);
                }

                // follow selected blocks down from the graphic object
                if (blocksDown.size() > 0) {
                    for(Block block : blocksDown) {
                        for(int i=block.getStartToken(); i<=block.getEndToken(); i++) {
                            localTokenization.add(tokenizations.get(i));
                            FeaturesVectorFigureSegmenter features = this.createFeatureVector(tokenizations, i, pageBlocks, direction);
                            if (features != null) {
                                content.append(features.printVector());
                            }
                        }
                    }
                }   
            }

            LayoutTokenization tokenizationsFigure = new LayoutTokenization(localTokenization);

            results.add(content.toString());
            tokenizationsFigures.add(tokenizationsFigure);
        }

        return Pair.of(results, tokenizationsFigures);
    }

    /**
     * Return the block containing the indicated layout token position. 
     * If no block covers the position, return -1
     */
    private int getBlock(List<Block> blocks, int thePos) {
        if (blocks == null || blocks.size() == 0)
            return -1;
        for(int i=0; i < blocks.size(); i++) {
            Block block = blocks.get(i);
            if (thePos > block.getEndToken())
                continue;
            if (thePos < block.getStartToken())
                break;
            if (thePos>=block.getStartToken() && thePos<=block.getEndToken())
                return i;
        }
        return -1;
    }

    private Block getClosestUp(List<Block> pageBlocks, BoundingBox graphicBox) {
        if (graphicBox == null)
            return null;

        Block closestBlockUp = null;
        double closestBlockUpSpace = 100000.0;

        for (Block block : pageBlocks) {
            List<LayoutToken> localTokens = block.getTokens();
            if (localTokens == null || localTokens.size() == 0) 
                continue;

            BoundingBox blockBox = block.getBoundingBox();
            if (blockBox == null) {
                blockBox = BoundingBoxCalculator.calculateOneBox(localTokens, true);
                block.setBoundingBox(blockBox);
            }
            
            if (blockBox == null) {
                continue;
            }

            //System.out.println("blockBox: " + blockBox.toString());

            if (blockBox.getY() < graphicBox.getY()) {
                // check column
                if (
                    (blockBox.getX() < graphicBox.getX() && graphicBox.getX() < blockBox.getX()+blockBox.getWidth()) ||
                    (blockBox.getX() > graphicBox.getX() && blockBox.getX() < graphicBox.getX()+graphicBox.getWidth()) 
                    ) {
                    double localClosestBlockUpSpace = graphicBox.getY() - (blockBox.getY()+blockBox.getHeight());
                    if (localClosestBlockUpSpace < 0)
                        continue;
                    // block candidate up
                    if (localClosestBlockUpSpace < closestBlockUpSpace) {
                        closestBlockUp = block;
                        closestBlockUpSpace = localClosestBlockUpSpace;
                    }
                }
            }
        }

if (closestBlockUp != null)
    System.out.println("Best block up: " + closestBlockUp.toString() + " / " + closestBlockUpSpace);

        return closestBlockUp;
    }

    private Block getClosestDown(List<Block> pageBlocks, BoundingBox graphicBox) {
        if (graphicBox == null)
            return null;

        Block closestBlockDown = null;
        double closestBlockDownSpace = 100000.0;

        for (Block block : pageBlocks) {
            List<LayoutToken> localTokens = block.getTokens();
            if (localTokens == null || localTokens.size() == 0) 
                continue;

            BoundingBox blockBox = block.getBoundingBox();
            if (blockBox == null) {
                blockBox = BoundingBoxCalculator.calculateOneBox(localTokens, true);
                block.setBoundingBox(blockBox);
            }
            
            if (blockBox == null) {
                continue;
            }
            
            //System.out.println("blockBox: " + blockBox.toString());

            if (blockBox.getY() > graphicBox.getY()) {
                // check column
                if (
                    (blockBox.getX() < graphicBox.getX() && graphicBox.getX() < blockBox.getX()+blockBox.getWidth()) ||
                    (blockBox.getX() > graphicBox.getX() && blockBox.getX() < graphicBox.getX()+graphicBox.getWidth()) 
                    ) {

                    double localClosestBlockDownSpace = blockBox.getY() - (graphicBox.getY()+graphicBox.getHeight());
                    if (localClosestBlockDownSpace < 0)
                        continue;

//System.out.println("Candidate block: " + blockBox.toString() + " / " + localClosestBlockDownSpace);
                    // block candidate down
                    if (localClosestBlockDownSpace < closestBlockDownSpace) {
                        closestBlockDown = block;
                        closestBlockDownSpace = localClosestBlockDownSpace;
                    }
                }
            }    
        }

if (closestBlockDown != null)
    System.out.println("Best block down: " + closestBlockDown.toString() + " / " + closestBlockDownSpace);

        return closestBlockDown;
    }

    private Block getImmediateBlockRight(List<Block> pageBlocks, BoundingBox graphicBox) {
        if (graphicBox == null)
            return null;

        //System.out.println("graphicBox (right): " + graphicBox.toString());

        Block closestBlockRight = null;
        double closestBlockRightSpace = 100000.0;

        for (Block block : pageBlocks) {
            List<LayoutToken> localTokens = block.getTokens();
            if (localTokens == null || localTokens.size() == 0) 
                continue;

            BoundingBox blockBox = block.getBoundingBox();
            if (blockBox == null) {
                blockBox = BoundingBoxCalculator.calculateOneBox(localTokens, true);
                block.setBoundingBox(blockBox);
            }
            
            if (blockBox == null) {
                continue;
            }

            //System.out.println("blockBox (right): " + blockBox.toString());

            if (blockBox.getX() >= graphicBox.getX() + graphicBox.getWidth()) {
                if (
                    (blockBox.getY() < graphicBox.getY() && graphicBox.getY() < blockBox.getY()+blockBox.getHeight()) ||
                    (blockBox.getY() > graphicBox.getY() && blockBox.getY() < graphicBox.getY()+graphicBox.getHeight()) ||
                    (blockBox.getY() > graphicBox.getY() && blockBox.getY()+blockBox.getHeight() < graphicBox.getY() + graphicBox.getHeight())
                    ) {
                    double localClosestBlockRightSpace = blockBox.getX() - (graphicBox.getX()+graphicBox.getWidth());
//System.out.println("Candidate block (right): " + blockBox.toString() + " / " + localClosestBlockRightSpace);
                    if (localClosestBlockRightSpace < closestBlockRightSpace) {
                        closestBlockRight = block;
                        closestBlockRightSpace = localClosestBlockRightSpace;
                    }
                }
            }
        }

if (closestBlockRight != null)
    System.out.println("Best block right: " + closestBlockRight.toString() + " / " + closestBlockRightSpace);

        return closestBlockRight;
    }

    private Block getImmediateBlockLeft(List<Block> pageBlocks, BoundingBox graphicBox) {
        if (graphicBox == null)
            return null;

        Block closestBlockLeft = null;
        double closestBlockLeftSpace = 100000.0;

        for (Block block : pageBlocks) {
            List<LayoutToken> localTokens = block.getTokens();
            if (localTokens == null || localTokens.size() == 0) 
                continue;

            BoundingBox blockBox = block.getBoundingBox();
            if (blockBox == null) {
                blockBox = BoundingBoxCalculator.calculateOneBox(localTokens, true);
                block.setBoundingBox(blockBox);
            }
            
            if (blockBox == null) {
                continue;
            }

            if (blockBox.getX() <= graphicBox.getX() + graphicBox.getWidth()) {
                if (
                    (blockBox.getY() < graphicBox.getY() && graphicBox.getY() < blockBox.getY()+blockBox.getHeight()) ||
                    (blockBox.getY() > graphicBox.getY() && blockBox.getY() < graphicBox.getY()+graphicBox.getHeight() ||
                    (blockBox.getY() > graphicBox.getY() && blockBox.getY()+blockBox.getHeight() < graphicBox.getY() + graphicBox.getHeight())
                        ) 
                    ) {
                    double localClosestBlockLeftSpace = (graphicBox.getX()+graphicBox.getWidth()) - blockBox.getX();
                    if (localClosestBlockLeftSpace < closestBlockLeftSpace) {
                        closestBlockLeft = block;
                        closestBlockLeftSpace = localClosestBlockLeftSpace;
                    }
                }
            }
        }

        return closestBlockLeft;

    }


    /**
     * A simple re-ordering of local blocks in reading order. This is simple to 
     * handle expansions from graphic objects, so accuracy is not necessary.
     *
     * => not used !
     */
    private List<Block> readingReorder(List<Block> blocks) {
        // first remove all empty blocks, because we can't order them
        List<Block> newBlocks = new ArrayList<>();
        for(Block block : blocks) {
            if (block.getTokens() != null && block.getTokens().size() > 0)
                newBlocks.add(block);
        }
        blocks = newBlocks;

        Collections.sort(blocks, new Comparator<Block>() {
            @Override
            public int compare(Block b1, Block b2) {
                List<LayoutToken> localTokens1 = b1.getTokens();
                List<LayoutToken> localTokens2 = b2.getTokens();

                BoundingBox blockBox1 = null;
                BoundingBox blockBox2 = null;

                if (localTokens1 != null && localTokens1.size() > 0) {
                    blockBox1 = BoundingBoxCalculator.calculateOneBox(localTokens1, true);
                }

                if (localTokens2 != null && localTokens2.size() > 0) {
                    blockBox2 = BoundingBoxCalculator.calculateOneBox(localTokens2, true);
                }

                if (blockBox1 == null) {
                    // this should not happen :)
                    // defaulting
                    return 1;
                }

                if (blockBox2 == null) {
                    // this should not happen :)
                    // defaulting
                    return -1;
                }

                if (blockBox1.getY() < blockBox2.getY()) {
                    if (blockBox1.getX() < blockBox2.getX()+blockBox2.getWidth()) {
                        return 1;
                    } else {
                        return -1;
                    }
                } else if (blockBox2.getY() < blockBox1.getY())  {
                    if (blockBox2.getX() < blockBox2.getX()+blockBox2.getWidth()) {
                        return 1;
                    } else {
                        return -1;
                    }
                } else {
                    if (blockBox1.getX()+blockBox1.getWidth() < blockBox2.getX()) {
                        return 1;
                    } else if (blockBox1.getX() > blockBox2.getX()+blockBox2.getWidth()) {
                        return -1;
                    } else 
                        return 0;
                }       
            }
        });
        return blocks;
    }

    private FeaturesVectorFigureSegmenter createFeatureVector(List<LayoutToken> tokenizations, 
                                                            int i, 
                                                            List<Block> pageBlocks,
                                                            Direction direction) {
        FeatureFactory featureFactory = FeatureFactory.getInstance();

        LayoutToken token = tokenizations.get(i);
        String localText = token.getText();

        if (localText == null) {
            return null;
        }

        localText = localText.replaceAll("[ \n]", "");
        if(localText.length() == 0 || TextUtilities.filterLine(localText)) {
            return null;
        }

        FeaturesVectorFigureSegmenter features = new FeaturesVectorFigureSegmenter();
        features.token = token;
        features.string = localText;

        // block status
        int blockIndex = this.getBlock(pageBlocks, i);
        if (blockIndex != -1) {
            Block localBlock = pageBlocks.get(blockIndex);
            if (localBlock.getStartToken() == i) {
                features.blockStatus = "BLOCKSTART";
            } else if (localBlock.getEndToken() == i) {
                features.blockStatus = "BLOCKEND";
            } else {
                features.blockStatus = "BLOCKIN";
            }
        } else {
            // it should not happen, but defaulting as new block
            features.blockStatus = "BLOCKSTART";
        }

        // line status
        // default
        features.lineStatus = "LINEIN";
        if (features.blockStatus.equals("BLOCKEND"))
            features.lineStatus = "LINEEND";
        else if (features.blockStatus.equals("BLOCKSTART"))
            features.lineStatus = "LINESTART";
        else {
            // look forward
            int nextIndex = i+1;
            while(nextIndex < tokenizations.size() && 
                (tokenizations.get(nextIndex).getText() == null) || (tokenizations.get(nextIndex).getText().equals(" "))) {
                nextIndex++;
            }
            if (tokenizations.get(nextIndex).getText().equals("\n")) {
                features.lineStatus = "LINEEND";
            } 
            // look backward
            int previousIndex = i-1;
            while(previousIndex > 0 && 
                (tokenizations.get(previousIndex).getText() == null) || (tokenizations.get(previousIndex).getText().equals(" "))) {
                previousIndex--;
            }
            if (tokenizations.get(previousIndex).getText().equals("\n")) {
                features.lineStatus = "LINESTART";
            } 
        }

        if (featureFactory.test_digit(localText)) {
            features.digit = "CONTAINSDIGITS";
        }

        Matcher m = featureFactory.isDigit.matcher(localText);
        if (m.find()) {
            features.digit = "ALLDIGIT";
        }

        if (localText.length() == 1) {
            features.singleChar = true;
        }

        if (Character.isUpperCase(localText.charAt(0))) {
            features.capitalisation = "INITCAP";
        }

        if (featureFactory.test_all_capital(localText)) {
            features.capitalisation = "ALLCAP";
        }

        // look backward
        int previousIndex = i-1;
        while(previousIndex > 0 && 
            (tokenizations.get(previousIndex).getText() == null) || (tokenizations.get(previousIndex).getText().equals(" "))) {
            previousIndex--;
        }
        String currentFont = tokenizations.get(previousIndex).getFont();
        double currentFontSize = tokenizations.get(previousIndex).getFontSize();

        if (currentFont == null) {
            currentFont = token.getFont();
            features.fontStatus = "NEWFONT";
        } else if (!currentFont.equals(token.getFont())) {
            currentFont = token.getFont();
            features.fontStatus = "NEWFONT";
        } else
            features.fontStatus = "SAMEFONT";

        int newFontSize = (int) token.getFontSize();
        if (currentFontSize == -1) {
            currentFontSize = newFontSize;
            features.fontSize = "HIGHERFONT";
        } else if (currentFontSize == newFontSize) {
            features.fontSize = "SAMEFONTSIZE";
        } else if (currentFontSize < newFontSize) {
            features.fontSize = "HIGHERFONT";
            currentFontSize = newFontSize;
        } else if (currentFontSize > newFontSize) {
            features.fontSize = "LOWERFONT";
            currentFontSize = newFontSize;
        }

        if (token.getBold())
            features.bold = true;

        if (token.getItalic())
            features.italic = true;

        if (features.capitalisation == null)
            features.capitalisation = "NOCAPS";

        if (features.digit == null)
            features.digit = "NODIGIT";

        if (features.punctType == null)
            features.punctType = "NOPUNCT";

        List<TaggingLabel> theUpstreamLabels = token.getLabels();
        if (theUpstreamLabels != null) {
            for(TaggingLabel taggingLabel : theUpstreamLabels) {
System.out.println(taggingLabel);
            }
        }

        return features;
    }
    
    /** 
     * Create training data based on an input Document (segmented by the segmentation model) and 
     * the current FigureSegmenter model. The result is a pair of TEI training data string and 
     * raw feature data. 
     */
    public Pair<Pair<String,String>,Pair<String,String>> createTraining(Document doc, String id) {
        // the figure are not segmented by the segmentation model (it's the purpose of this parser),
        // but normally pre-located in the BODY and ANNEX segments

        List<LayoutToken> tokenizations = doc.getTokenizations();

        // figure anchors are based on VectorGraphicBoxCalculator, which aggregate bitmap and SVG elements
        List<GraphicObject> figureAnchors = this.initFigureAnchors(doc);

        // for each figure anchor, we generate sequence to be labeled with features
        Pair<List<String>,List<LayoutTokenization>> featureObjectUp = this.getAreasFeatured(doc, figureAnchors, Direction.UP);
        Pair<List<String>,List<LayoutTokenization>> featureObjectDown = this.getAreasFeatured(doc, figureAnchors, Direction.DOWN);

        List<String> featureVectorsUp = featureObjectUp.getLeft();
        List<LayoutTokenization> layoutTokenizationsUp = featureObjectUp.getRight();

        List<String> featureVectorsDown = featureObjectDown.getLeft();
        List<LayoutTokenization> layoutTokenizationsDown = featureObjectDown.getRight();

        // if the feature vectors are null, it usually means that no figure segment is found in the
        // document segmentation and there is no training data to generate for this document
        if (featureVectorsUp == null && featureVectorsDown == null) {
            return null;
        }

        StringBuilder sbUp = new StringBuilder();
        StringBuilder sbDown = new StringBuilder();
        String header = "<tei xml:space=\"preserve\">\n" +
                    "    <teiHeader>\n" +
                    "        <fileDesc xml:id=\"_" + id + "\"/>\n" +
                    "    </teiHeader>\n" +
                    "    <text xml:lang=\"en\">\n" +
                    "        <body>\n";
        sbUp.append(header);
        sbDown.append(header);

        StringBuilder allFeatureVectorUp = new StringBuilder();
        StringBuilder allFeatureVectorDown = new StringBuilder();

        for(int i=0; i<featureVectorsUp.size(); i++) {
            
            sbUp.append("\n<div><p>");

            String featureVectorUp = featureVectorsUp.get(i);
            LayoutTokenization layoutTokenizationUp = layoutTokenizationsUp.get(i);
            List<LayoutToken> localTokenizationsUp = layoutTokenizationUp.getTokenization();

            allFeatureVectorUp.append(featureVectorUp);

            String featureVectorDown = featureVectorsDown.get(i);
            LayoutTokenization layoutTokenizationDown = layoutTokenizationsDown.get(i);
            List<LayoutToken> localTokenizationsDown = layoutTokenizationDown.getTokenization();

            allFeatureVectorDown.append(featureVectorDown);

            String resUp = null;
            String resDown = null;
            try {
                GenericTagger tagger = figureSegmenterParserUp;
                //resDown = tagger.label(featureVectorDown);

                tagger = figureSegmenterParserDown;
                //resUp = tagger.label(featureVectorUp);
            }
            catch(Exception e) {
                throw new GrobidException("Sequence labeling in figure-segmenter fails.", e);
            }
            if (resUp == null) {
                // we still output just the text, to be labelled manually
                String localContent = TextUtilities.HTMLEncode(LayoutTokensUtil.toText(localTokenizationsUp));
                localContent = localContent.replace("\n", "<lb/>\n");
                sbUp.append(localContent);
            } else {

                // output text with <figure> label
                TaggingTokenClusteror clusteror = new TaggingTokenClusteror(GrobidModels.FIGURE_SEGMENTER_DOWN, resUp, localTokenizationsUp);
                List<TaggingTokenCluster> clusters = clusteror.cluster();
                for (TaggingTokenCluster cluster : clusters) {
                    if (cluster == null) {
                        continue;
                    }
                    TaggingLabel clusterLabel = cluster.getTaggingLabel();
                    Engine.getCntManager().i(clusterLabel);
                    List<LayoutToken> localTokens = cluster.concatTokens();
            
                    if (clusterLabel.equals(TaggingLabels.FIGURE)) {
                        sbUp.append("    <figure>");
                        String localContent = TextUtilities.HTMLEncode(LayoutTokensUtil.toText(localTokens));
                        localContent = localContent.replace("\n", "<lb/> ");
                        sbUp.append(localContent);
                        sbUp.append("</figure>\n");
                    } else if (clusterLabel.equals(TaggingLabels.OTHER)) {
                        sbUp.append(TextUtilities.HTMLEncode(LayoutTokensUtil.toText(localTokens)));
                    }
                }
            }

            sbUp.append("</p></div>\n\n");
            sbDown.append("\n<div><p>");

            if (resDown == null) {
                // we still output just the text, to be labelled manually
                String localContent = TextUtilities.HTMLEncode(LayoutTokensUtil.toText(localTokenizationsDown));
                localContent = localContent.replace("\n", "<lb/>\n");
                sbDown.append(localContent);
            } else {
                // output text with <figure> label
                TaggingTokenClusteror clusteror = new TaggingTokenClusteror(GrobidModels.FIGURE_SEGMENTER_DOWN, resDown, localTokenizationsDown);
                List<TaggingTokenCluster> clusters = clusteror.cluster();
                for (TaggingTokenCluster cluster : clusters) {
                    if (cluster == null) {
                        continue;
                    }
                    TaggingLabel clusterLabel = cluster.getTaggingLabel();
                    Engine.getCntManager().i(clusterLabel);
                    List<LayoutToken> localTokens = cluster.concatTokens();
            
                    if (clusterLabel.equals(TaggingLabels.FIGURE)) {
                        sbDown.append("    <figure>");
                        String localContent = TextUtilities.HTMLEncode(LayoutTokensUtil.toText(localTokens));
                        localContent = localContent.replace("\n", "<lb/> ");
                        sbDown.append(localContent);
                        sbDown.append("</figure>\n");
                    } else if (clusterLabel.equals(TaggingLabels.OTHER)) {
                        sbDown.append(TextUtilities.HTMLEncode(LayoutTokensUtil.toText(localTokens)));
                    }
                }
            }
            
            sbDown.append("</p></div>\n\n");
        }

        String closer = "\n        </body>\n" +
                        "    </text>\n" +
                        "</tei>\n";

        sbUp.append(closer);
        sbDown.append(closer);

        return Pair.of(
            Pair.of(sbUp.toString(), allFeatureVectorUp.toString()), 
            Pair.of(sbDown.toString(), allFeatureVectorDown.toString())
        );
    }

    public void close() throws IOException {
        // no resource to close
    }
}