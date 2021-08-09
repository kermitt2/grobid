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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.SortedSet;
import java.util.regex.Matcher;

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
    
    public FigureSegmenterParser() {
        figureSegmenterParserUp = TaggerFactory.getTagger(GrobidModels.FIGURE_SEGMENTER_UP);
        figureSegmenterParserDown = TaggerFactory.getTagger(GrobidModels.FIGURE_SEGMENTER_DOWN);
    }

    public Document extract(Document doc) {

        // figure anchors are based on VectorGraphicBoxCalculator, which aggregate bitmap and SVG elements
        List<GraphicObject> figureAnchors = this.initFigureAnchors(doc);

        // for each figure anchor, we generate sequence to be labeled with features
        Pair<List<String>,List<LayoutTokenization>> featureObject = this.getAreasFeatured(doc, figureAnchors);
        
        List<String> contents = featureObject.getLeft();
        List<LayoutTokenization> theTokenizations = featureObject.getRight();

        // we label the sequences to extend or not the figure anchor
        if (contents != null && contents.size() > 0) {
            String labelledResults;
            try {
                boolean up = false;
                
                GenericTagger tagger = up ? figureSegmenterParserUp : figureSegmenterParserDown;
                labelledResults = tagger.label(contents);
            } catch(Exception e) {
                throw new GrobidException("Sequence labeling in figure-segmenter fails.", e);
            }

            // validate and set the figure areas in the Document object
            doc = BasicStructureBuilder.figureResultSegmentation(doc, figureAnchors, labelledResults, theTokenizations);
        }
        return doc;
    }

    private List<GraphicObject> initFigureAnchors(Document doc) {
        return doc.getImages();
    }

    /**
     * Addition of the features at layout token level for the areas before and after the figure anchors.
     *
     */
    private Pair<List<String>,List<LayoutTokenization>> getAreasFeatured(Document doc, List<GraphicObject> figureAnchors) {
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

            // start down and determine the end position in the down direction
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
                        if (tokens == null && lastPageBlock) {
                            break;
                        }
                        if (tokens == null) {
                            continue;
                        }


                    }

                    break;
                }
            }


            for(int i=startPos; i<=endPos; i++) {
                FeaturesVectorFigureSegmenter features = new FeaturesVectorFigureSegmenter();

                LayoutToken token = tokenizations.get(i);
                localTokenization.add(token);
                String localText = token.getText();

                if (localText == null) {
                    continue;
                }

                localText = localText.replaceAll("[ \n]", "");
                if(localText.length() == 0 || TextUtilities.filterLine(localText)) {
                    continue;
                }

                features.token = token;
                features.string = localText;

                if (i >= startGraphicPos && i <= endGraphicPos)
                    features.inGraphicBox = true;
                
                content.append(features.printVector());
            }

            LayoutTokenization tokenizationsFigure = new LayoutTokenization(localTokenization);

            results.add(content.toString());
            tokenizationsFigures.add(tokenizationsFigure);
        }

        return Pair.of(results, tokenizationsFigures);
    }

    
    /** 
     * Create training data based on an input Document (segmented by the segmentation model) and 
     * the current FigureSegmenter model. The result is a pair of TEI training data string and 
     * raw feature data. 
     */
    public Pair<String,String> createTrainingData(Document doc, String id) {
        // the figure are not segmented by the segmentation model (it's the purpose of this parser),
        // but normally pre-located in the BODY and ANNEX segments

        List<LayoutToken> tokenizations = doc.getTokenizations();

        // figure anchors are based on VectorGraphicBoxCalculator, which aggregate bitmap and SVG elements
        List<GraphicObject> figureAnchors = this.initFigureAnchors(doc);

        // for each figure anchor, we generate sequence to be labeled with features
        Pair<List<String>,List<LayoutTokenization>> featureObject = this.getAreasFeatured(doc, figureAnchors);

        List<String> featureVectors = featureObject.getLeft();
        List<LayoutTokenization> layoutTokenizations = featureObject.getRight();

        // if featureVectors is null, it usually means that no figure segment is found in the
        // document segmentation and there is no training data to generate for this document
        if (featureVectors == null) {
            return null;
        }

        // we cover first the extension down the graphic object
        boolean up = false;

        StringBuilder sb = new StringBuilder();
        sb.append("<tei xml:space=\"preserve\">\n" +
                    "    <teiHeader>\n" +
                    "        <fileDesc xml:id=\"_" + id + "\"/>\n" +
                    "    </teiHeader>\n" +
                    "    <text xml:lang=\"en\">\n" +
                    "        <body>\n");
        StringBuilder allFeatureVector = new StringBuilder();

        for(int i=0; i<featureVectors.size(); i++) {
            
            sb.append("\n<div>\n");

            String featureVector = featureVectors.get(i);
            LayoutTokenization layoutTokenization = layoutTokenizations.get(i);
            List<LayoutToken> localTokenizations = layoutTokenization.getTokenization();

            allFeatureVector.append(featureVector);

            String res;        
            try {
                GenericTagger tagger = up ? figureSegmenterParserUp : figureSegmenterParserDown;
                res = tagger.label(featureVector);
            }
            catch(Exception e) {
                throw new GrobidException("Sequence labeling in figure-segmenter fails.", e);
            }
            if (res == null) {
                // we still output just the text, to be labelled manually
                sb.append(TextUtilities.HTMLEncode(LayoutTokensUtil.toText(localTokenizations)));
            } else {
                // output text with <figure> label
                TaggingTokenClusteror clusteror = new TaggingTokenClusteror(GrobidModels.FIGURE_SEGMENTER_DOWN, res, localTokenizations);
                List<TaggingTokenCluster> clusters = clusteror.cluster();
                for (TaggingTokenCluster cluster : clusters) {
                    if (cluster == null) {
                        continue;
                    }
                    TaggingLabel clusterLabel = cluster.getTaggingLabel();
                    Engine.getCntManager().i(clusterLabel);
                    List<LayoutToken> localTokens = cluster.concatTokens();
            
                    if (clusterLabel.equals(TaggingLabels.FIGURE)) {
                        sb.append("    <figure>");
                        String localContent = TextUtilities.HTMLEncode(LayoutTokensUtil.toText(localTokens));
                        localContent = localContent.replace("\n", "<lb/> ");
                        sb.append(localContent);
                        sb.append("</figure>\n");
                    } else if (clusterLabel.equals(TaggingLabels.OTHER)) {
                        sb.append(TextUtilities.HTMLEncode(LayoutTokensUtil.toText(localTokens)));
                    }
                }
            }
            
            sb.append("</div>\n");
        }

        sb.append("\n        </body>\n" +
                    "    </text>\n" +
                    "</tei>\n");

        return Pair.of(sb.toString(), allFeatureVector.toString());
    }
}