package org.grobid.core.tokenization;

import org.grobid.core.GrobidModels;
import org.grobid.core.engines.TaggingLabel;
import org.grobid.core.engines.tagging.GenericTaggerUtils;
import org.grobid.core.layout.LayoutToken;
import org.grobid.core.utilities.LayoutTokensUtil;
import org.grobid.core.utilities.Pair;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by zholudev on 11/01/16.
 * Synchronize tagging result and layout tokens
 */
public class TaggingTokenSynchronizer implements Iterator<LabeledTokensContainer>, Iterable<LabeledTokensContainer> {
    private final GrobidModels grobidModel;
    private final Iterator<Pair<String, String>> tokensAndLabelsIt;
    private final Iterator<LayoutToken> tokenizationsIt;
    private int tokensAndLabelsPtr;
    private int tokenizationsPtr;

    public TaggingTokenSynchronizer(GrobidModels grobidModel, String result, List<LayoutToken> tokenizations) {
        this.grobidModel = grobidModel;
        List<Pair<String, String>> tokensAndLabels = GenericTaggerUtils.getTokensAndLabels(result);
        tokensAndLabelsIt = tokensAndLabels.iterator();
        tokenizationsIt = tokenizations.iterator();
    }

    @Override
    public boolean hasNext() {
        return tokensAndLabelsIt.hasNext();
    }

    @Override
    //null value indicates an empty line in a tagging result
    public LabeledTokensContainer next() {
        Pair<String, String> p = tokensAndLabelsIt.next();

        if (p == null) {
            return null;
        }

        String resultToken = p.a;
        String label = p.b;

        List<LayoutToken> layoutTokenBuffer = new ArrayList<>();
        boolean stop = false;
        boolean addSpace = false;
        while ((!stop) && (tokenizationsIt.hasNext())) {
            LayoutToken layoutToken = tokenizationsIt.next();
            layoutTokenBuffer.add(layoutToken);
            String tokOriginal = layoutToken.t();
            if (LayoutTokensUtil.spaceyToken(tokOriginal)) {
                addSpace = true;
            } else if (tokOriginal.equals(resultToken)) {
                stop = true;
            } else {
                throw new IllegalStateException("IMPLEMENTATION ERROR: " +
                        "tokens (at pos: "  + tokensAndLabelsPtr + ")got dissynchronized with tokenizations (at pos: "
                        + tokenizationsPtr + " )");
            }
            tokenizationsPtr++;
        }

        tokensAndLabelsPtr++;
        return new LabeledTokensContainer(layoutTokenBuffer, resultToken, TaggingLabel.getLabel(grobidModel, label),
                GenericTaggerUtils.isBeginningOfEntity(label), addSpace);
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Iterator<LabeledTokensContainer> iterator() {
        return this;
    }
}
