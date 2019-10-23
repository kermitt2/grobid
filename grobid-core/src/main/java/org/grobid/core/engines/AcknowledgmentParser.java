package org.grobid.core.engines;

/**
 * @created by Tanti
 */

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.grobid.core.GrobidModels;
import org.grobid.core.data.AcknowledgmentItem;
import org.grobid.core.engines.label.TaggingLabel;
import org.grobid.core.engines.label.TaggingLabels;
import org.grobid.core.exceptions.GrobidException;
import org.grobid.core.features.FeaturesVectorAcknowledgment;
import org.grobid.core.layout.LayoutToken;
import org.grobid.core.tokenization.TaggingTokenCluster;
import org.grobid.core.tokenization.TaggingTokenClusteror;
import org.grobid.core.utilities.LayoutTokensUtil;
import org.grobid.core.utilities.UnicodeUtil;
import org.grobid.core.utilities.counters.CntManager;

import java.util.ArrayList;
import java.util.List;

public class AcknowledgmentParser extends AbstractParser {
    private EngineParsers parsers;

    public AcknowledgmentParser(EngineParsers parsers, CntManager cntManager) {
        super(GrobidModels.ACKNOWLEDGMENT, cntManager);
        this.parsers = parsers;
    }

    public AcknowledgmentParser(EngineParsers parsers) {
        super(GrobidModels.ACKNOWLEDGMENT);
        this.parsers = parsers;
    }

    public AcknowledgmentParser() {
        super(GrobidModels.ACKNOWLEDGMENT);
    }

    /**
     * Processing of acknowledgment
     */

    public List<AcknowledgmentItem> processing(String input) {
        if (StringUtils.isBlank(input)) {
            return null;
        }

        // cleaning
        input = UnicodeUtil.normaliseText(input);
        List<LayoutToken> tokens = analyzer.tokenizeWithLayoutToken(input);
        return processing(tokens);
    }

    public List<AcknowledgmentItem> processing(List<LayoutToken> tokens) {
        List<AcknowledgmentItem> acknowledgments = null;
        if (CollectionUtils.isEmpty(tokens)) {
            return null;
        }
        try {
            String headerAcknowledgment = FeaturesVectorAcknowledgment.addFeaturesAcknowledgment(tokens);
            String resAcknowledgment = label(headerAcknowledgment);
            acknowledgments = resultExtractionLayoutTokens(resAcknowledgment, tokens);
            return acknowledgments;
        } catch (Exception e) {
            throw new GrobidException("An exception occured while running Grobid.", e);
        }
    }

    public List<AcknowledgmentItem> resultExtractionLayoutTokens(String result, List<LayoutToken> tokenizations) {
        List<AcknowledgmentItem> acknowledgments = new ArrayList<>();

        TaggingTokenClusteror clusteror = new TaggingTokenClusteror(GrobidModels.ACKNOWLEDGMENT, result, tokenizations);

        List<TaggingTokenCluster> clusters = clusteror.cluster();
        AcknowledgmentItem acknowledgmentItem = null;
        for (TaggingTokenCluster cluster : clusters) {
            if (cluster == null) {
                continue;
            }
            acknowledgmentItem = new AcknowledgmentItem();
            TaggingLabel clusterLabel = cluster.getTaggingLabel();
            Engine.getCntManager().i(clusterLabel);

            String clusterContent = LayoutTokensUtil.normalizeDehyphenizeText(cluster.concatTokens());
            if (clusterLabel.equals(TaggingLabels.ACKNOWLEDGMENT_AFFILIATION)) {
                acknowledgmentItem.setText(clusterContent);
                acknowledgmentItem.setLabel("affiliation");
            } else if (clusterLabel.equals(TaggingLabels.ACKNOWLEDGMENT_EDUCATIONAL_INSTITUTION)) {
                acknowledgmentItem.setText(clusterContent);
                acknowledgmentItem.setLabel("educationalInstitution");
            } else if (clusterLabel.equals(TaggingLabels.ACKNOWLEDGMENT_FUNDING_AGENCY)) {
                acknowledgmentItem.setText(clusterContent);
                acknowledgmentItem.setLabel("fundingAgency");
            } else if (clusterLabel.equals(TaggingLabels.ACKNOWLEDGMENT_GRANT_NAME)) {
                acknowledgmentItem.setText(clusterContent);
                acknowledgmentItem.setLabel("grantName");
            } else if (clusterLabel.equals(TaggingLabels.ACKNOWLEDGMENT_GRANT_NUMBER)) {
                acknowledgmentItem.setText(clusterContent);
                acknowledgmentItem.setLabel("grantNumber");
            } else if (clusterLabel.equals(TaggingLabels.ACKNOWLEDGMENT_INDIVIDUAL)) {
                acknowledgmentItem.setText(clusterContent);
                acknowledgmentItem.setLabel("individual");
            } else if (clusterLabel.equals(TaggingLabels.ACKNOWLEDGMENT_OTHER_INSTITUTION)) {
                acknowledgmentItem.setText(clusterContent);
                acknowledgmentItem.setLabel("otherInstitution");
            } else if (clusterLabel.equals(TaggingLabels.ACKNOWLEDGMENT_PROJECT_NAME)) {
                acknowledgmentItem.setText(clusterContent);
                acknowledgmentItem.setLabel("projectName");
            } else if (clusterLabel.equals(TaggingLabels.ACKNOWLEDGMENT_RESEARCH_INSTITUTION)) {
                acknowledgmentItem.setText(clusterContent);
                acknowledgmentItem.setLabel("researchInstitution");
            }

            if (acknowledgmentItem.getLabel() != null) {
                acknowledgments.add(acknowledgmentItem);
            }
        }
        return acknowledgments;
    }
}
