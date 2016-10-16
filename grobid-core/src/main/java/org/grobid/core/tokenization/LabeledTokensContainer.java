package org.grobid.core.tokenization;

import org.grobid.core.engines.TaggingLabel;
import org.grobid.core.engines.TaggingLabels;
import org.grobid.core.engines.tagging.GenericTaggerUtils;
import org.grobid.core.layout.LayoutToken;

import java.util.List;

/**
 * Created by zholudev on 11/01/16.
 * Representing labeled tokens and stuff
 */
public class LabeledTokensContainer {

    private List<LayoutToken> layoutTokens;
    private String token;
    private TaggingLabels taggingLabels;
    private boolean beginning;
    private boolean spacePreceding;
    private boolean newLinePreceding;
    private String featureString;

    public LabeledTokensContainer(List<LayoutToken> layoutTokens, String token, TaggingLabels taggingLabels, boolean beginning) {
        this.layoutTokens = layoutTokens;
        this.token = token;
        this.taggingLabels = taggingLabels;
        this.beginning = beginning;
    }

    public List<LayoutToken> getLayoutTokens() {
        return layoutTokens;
    }

    public String getToken() {
        return token;
    }

    public TaggingLabels getTaggingLabels() {
        return taggingLabels;
    }

    public boolean isBeginning() {
        return beginning;
    }

    public String getPlainLabel() {
        return taggingLabels.getLabel();
    }

    public String getFullLabel() {
        return isBeginning() ? GenericTaggerUtils.START_ENTITY_LABEL_PREFIX + taggingLabels.getLabel()
                : taggingLabels.getLabel();
    }

    public boolean isSpacePreceding() {
        return spacePreceding;
    }

    public boolean isNewLinePreceding() {
        return newLinePreceding;
    }

    public void setSpacePreceding(boolean spacePreceding) {
        this.spacePreceding = spacePreceding;
    }

    public void setNewLinePreceding(boolean newLinePreceding) {
        this.newLinePreceding = newLinePreceding;
    }

    public String getFeatureString() {
        return featureString;
    }

    public void setFeatureString(String featureString) {
        this.featureString = featureString;
    }

    @Override
    public String toString() {
        return token + " (" + getFullLabel() + ")";
    }
}
