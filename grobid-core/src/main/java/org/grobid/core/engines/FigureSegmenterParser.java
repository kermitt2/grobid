package org.grobid.core.engines;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.grobid.core.GrobidModels;
import org.grobid.core.document.Document;
import org.grobid.core.document.DocumentPiece;
import org.grobid.core.document.DocumentPointer;
import org.grobid.core.engines.label.SegmentationLabels;
import org.grobid.core.engines.tagging.GenericTaggerUtils;
import org.grobid.core.exceptions.GrobidException;
import org.grobid.core.features.FeatureFactory;
import org.grobid.core.features.FeaturesVectorFigureSegmenter;
import org.grobid.core.layout.Block;
import org.grobid.core.layout.LayoutToken;
import org.grobid.core.layout.GraphicObject;
import org.grobid.core.tokenization.LabeledTokensContainer;
import org.grobid.core.tokenization.TaggingTokenSynchronizer;
import org.grobid.core.utilities.BoundingBoxCalculator;
import org.grobid.core.utilities.Pair;
import org.grobid.core.utilities.TextUtilities;
import org.grobid.core.utilities.Triple;
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
public class FigureSegmenterParser extends AbstractParser {
    private static final Logger LOGGER = LoggerFactory.getLogger(FigureSegmenterParser.class);

    // projection scale for line length
    private static final int LINESCALE = 10;

    protected FigureSegmenterParser() {
        super(GrobidModels.FIGURE_SEGMENTER);
    }

    public Document extract(Document doc) {
        List<LayoutToken> tokenizations = doc.getTokenizations();

        // figure anchors are based on VectorGraphicBoxCalculator, which aggregate bitmap and SVG elements
        List<GraphicObject> figureAnchors = this.initFigureAnchors(doc);

        // for each figure anchor, we generate sequence to be labeled with features
        List<String> contents = this.getAreasFeatured(doc, figureAnchors);
        
        // we label the sequences to extend or not the figure anchor
        if (contents != null && contents.size() > 0) {
            String labelledResults = label(contents);
            // validate and set the figure areas in the Document object
            doc = BasicStructureBuilder.figureResultSegmentation(doc, figureAnchors, labelledResults, tokenizations);
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
    private List<String> getAreasFeatured(Document doc, List<GraphicObject> figureAnchors) {
        List<Block> blocks = doc.getBlocks();
        if ((blocks == null) || blocks.size() == 0) {
            return null;
        }

        List<String> results = new ArrayList<>();

        for (GraphicObject figureAnchor : figureAnchors) {
            FeaturesVectorFigureSegmenter features = new FeaturesVectorFigureSegmenter();
            StringBuilder content = new StringBuilder();



            results.add(content.toString());
        }

        return results;
    }
}