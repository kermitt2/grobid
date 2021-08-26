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
import org.grobid.core.tokenization.LabeledTokensContainer;
import org.grobid.core.tokenization.TaggingTokenSynchronizer;
import org.grobid.core.tokenization.TaggingTokenCluster;
import org.grobid.core.tokenization.TaggingTokenClusteror;
import org.grobid.core.utilities.BoundingBoxCalculator;
import org.grobid.core.utilities.TextUtilities;
import org.grobid.core.utilities.Triple;
import org.grobid.core.utilities.LayoutTokensUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.SortedSet;
import java.util.regex.Matcher;
import java.util.Map;
import java.util.HashMap;

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
    private static int EXTENSION_SIZE = 5;

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
        return doc.getImages();
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

            int startGraphicPos = figureAnchor.getStartPosition();
            int endGraphicPos = figureAnchor.getEndPosition();

            // end of extension is 1) page start/end 2) EXTENSION_SIZE reached
            // 3) non body or annex segments (should we add footnotes?)

            int startPos = startGraphicPos;
            int endPos = endGraphicPos;

            System.out.println("figureAnchor: " + figureAnchor.getPage() + " / " + startPos + " / " + endPos);

            if (startPos == -1)
                continue;
            if (endPos == -1)
                endPos = startPos;

            // position of the blocks in the page where the GraphicObject is located
            Map<Integer,Block> blockIndexMap = new HashMap<>();
            Page currentPage = null;

            for (Page page : doc.getPages()) {
                if (page.getNumber() == figureAnchor.getPage()) {
                    if ((page.getBlocks() == null) || (page.getBlocks().size() == 0)) {
                        // note: this should not happen
                        LOGGER.warn("Graphic object is located on on an empty page on page " + page.getNumber());
                        break;
                    }

                    for(int blockIndex=0; blockIndex < page.getBlocks().size(); blockIndex++) {
                        Block block = page.getBlocks().get(blockIndex);
                        boolean lastPageBlock = false;
                        if (blockIndex == page.getBlocks().size()-1) {        
                            lastPageBlock = true;
                        }
                        boolean firstPageBlock = false;
                        if (blockIndex == 0) {
                            firstPageBlock = true;
                        }
                        List<LayoutToken> tokens = block.getTokens();
                        if (tokens == null || tokens.size() == 0) {
                            continue;
                        }

                        blockIndexMap.put(tokens.get(0).getOffset(), block);
                    }
                    currentPage = page;
                    break;
                }
            }

            if (currentPage == null || blockIndexMap.size() == 0) {
                // we can't process this malformed graphic object (it should not happen ;)
                continue;
            }

            if (direction == Direction.UP) {
                // go up and determine the start position in the upper direction
                Block currentBlock = blockIndexMap.get(startPos);
                int pos = currentPage.getBlocks().indexOf(currentBlock);
                for(int j=1; j<=EXTENSION_SIZE; j++) {
                    if (pos-j < 0)
                        break; 
                    List<LayoutToken> localTokens = currentPage.getBlocks().get(pos-j).getTokens();
                    if (localTokens == null || localTokens.size() == 0)
                        continue;
                    startPos = currentPage.getBlocks().get(pos-j).getTokens().get(0).getOffset();
                }

                for(int i=endPos; i>=startPos; i--) {
                    localTokenization.add(tokenizations.get(i));
                    FeaturesVectorFigureSegmenter features = this.createFeatureVector(tokenizations, i, blockIndexMap, direction);
                    if (features != null) {
                        if (i >= startGraphicPos && i <= endGraphicPos)
                            features.inGraphicBox = true;
                        content.append(features.printVector());
                    }
                }
            } else {
                // go down and determine the end position in the down direction
                Block currentBlock = blockIndexMap.get(endPos+1);
                // if currentBlock here is null, we are already at the end of the page
                if (currentBlock != null) {
                    int pos = currentPage.getBlocks().indexOf(currentBlock);
                    for(int j=0; j<EXTENSION_SIZE; j++) {
                        if (pos+j >= currentPage.getBlocks().size())
                            break; 
                        List<LayoutToken> localTokens = currentPage.getBlocks().get(pos+j).getTokens();
                        if (localTokens == null || localTokens.size() == 0)
                            continue;
                        endPos = currentPage.getBlocks().get(pos+j).getTokens().get(0).getOffset();
                    }
                }

                for(int i=startPos; i<=endPos; i++) {
                    localTokenization.add(tokenizations.get(i));
                    FeaturesVectorFigureSegmenter features = this.createFeatureVector(tokenizations, i, blockIndexMap, direction);
                    if (features != null) {
                        if (i >= startGraphicPos && i <= endGraphicPos)
                            features.inGraphicBox = true;
                        content.append(features.printVector());
                    }
                }
            }

            LayoutTokenization tokenizationsFigure = new LayoutTokenization(localTokenization);

            results.add(content.toString());
            tokenizationsFigures.add(tokenizationsFigure);
        }

        return Pair.of(results, tokenizationsFigures);
    }

    private FeaturesVectorFigureSegmenter createFeatureVector(List<LayoutToken> tokenizations, 
                                                            int i, 
                                                            Map<Integer,Block> blockIndexMap,
                                                            Direction direction) {
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
        if (blockIndexMap.get(i) != null) {
            features.blockStatus = "BLOCKSTART";
        } else {
            int nextIndex = i+1;
            while(nextIndex < tokenizations.size() && 
                (tokenizations.get(nextIndex).getText() == null) || (tokenizations.get(nextIndex).getText().trim().length() == 0)) {
                nextIndex++;
            }
            if (blockIndexMap.get(nextIndex) != null) {
                features.blockStatus = "BLOCKEND";
            } else {
                features.blockStatus = "BLOCKIN";
            }
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
            
            sbUp.append("\n<div>\n");

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
                // TBD: note reverse order ?
                sbUp.append(TextUtilities.HTMLEncode(LayoutTokensUtil.toText(localTokenizationsUp)));
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

            sbUp.append("</div>\n\n");
            sbDown.append("\n<div>\n");

            if (resDown == null) {
                // we still output just the text, to be labelled manually
                sbDown.append(TextUtilities.HTMLEncode(LayoutTokensUtil.toText(localTokenizationsDown)));
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
            
            sbDown.append("</div>\n\n");
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