package org.grobid.core.engines;

import org.grobid.core.GrobidModels;
import org.grobid.core.engines.citations.LabeledReferenceResult;
import org.grobid.core.engines.citations.ReferenceSegmenter;
import org.grobid.core.engines.tagging.GenericTaggerUtils;
import org.grobid.core.features.FeaturesVectorReferenceSegmenter;
import org.grobid.core.utilities.Pair;
import org.grobid.core.utilities.TextUtilities;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

/**
 * User: zholudev
 * Date: 4/14/14
 */
public class ReferenceSegmenterParser extends AbstractParser implements ReferenceSegmenter{
    protected ReferenceSegmenterParser() {
        super(GrobidModels.REFERENCE_SEGMENTER);
    }

    /**
     *
     * @param referenceBlock text containing citation block
     * @return <reference_label, reference_string>  Note, that label is null when no label was detected
     *              example: <"[1]", "Hu W., Barkana, R., &amp; Gruzinov A. Phys. Rev. Lett. 85, 1158">
     */
    public List<LabeledReferenceResult> extract(String referenceBlock) {
        List<String> blocks = new ArrayList<>();


        String input = referenceBlock.replace("\n", " ");
        input = input.replaceAll("\\p{Cntrl}", " ").trim();
        StringTokenizer st = new StringTokenizer(input, TextUtilities.fullPunctuations, true);

        if (st.countTokens() == 0) {
            return null;
        }

        List<String> tokenizations = new ArrayList<>();
        while (st.hasMoreTokens()) {
            final String tok = st.nextToken();
            tokenizations.add(tok);
            if (!tok.equals(" ")) {
                blocks.add(tok + " <reference-block>");
            }
        }
        blocks.add("\n");

        String featureVector = FeaturesVectorReferenceSegmenter.addFeaturesReferenceSegmenter(blocks);
        String res = label(featureVector);

//        System.out.println(res);

        List<Pair<String, String>> labeled = GenericTaggerUtils.getTokensAndLabels(res);

        return getExtractionResult(tokenizations, labeled);
    }

    private List<LabeledReferenceResult> getExtractionResult(List<String> tokenizations, List<Pair<String, String>> labeled) {
        List<LabeledReferenceResult> resultList = new ArrayList<>();
        StringBuilder reference = new StringBuilder();
        StringBuilder referenceLabel = new StringBuilder();

        int tokPtr = 0;
        boolean addSpace = false;
        for (Pair<String, String> l : labeled) {
            String tok = l.a;
            String label = l.b;

            while(tokPtr < tokenizations.size() && tokenizations.get(tokPtr).equals(" ")) {
                addSpace = true;
                tokPtr++;
            }
            String tokenizationToken = tokenizations.get(tokPtr);

            if (tokPtr == tokenizations.size()) {
                throw new IllegalStateException("Implementation error: Reached the end of tokenizations, but current token is " + tok);
            }

            if (!tokenizationToken.equals(tok)) {
                throw new IllegalStateException("Implementation error: " + tokenizationToken + " != " + tok);
            }

            String plainLabel = GenericTaggerUtils.getPlainLabel(label);
            switch (plainLabel) {
                case "<label>":
                    if (GenericTaggerUtils.isBeginningOfEntity(label)) {
                        if (reference.length() != 0) {
                            resultList.add(new LabeledReferenceResult(referenceLabel.length() == 0 ? null :
                                    referenceLabel.toString().trim(), reference.toString().trim()));
                            reference.setLength(0);
                            referenceLabel.setLength(0);
                        }
                    }
                    if (addSpace) {
                        referenceLabel.append(' ');
                        addSpace = false;
                    }
                    referenceLabel.append(tok);
                    break;
                case "<reference>":
                    if (GenericTaggerUtils.isBeginningOfEntity(label)) {
                        if (reference.length() != 0) {
                            resultList.add(new LabeledReferenceResult(referenceLabel.length() == 0 ?
                                    null : referenceLabel.toString().trim(), reference.toString().trim()));
                            reference.setLength(0);
                            referenceLabel.setLength(0);
                        }
                    }
                    if (addSpace) {
                        reference.append(' ');
                        addSpace = false;
                    }
                    reference.append(tok);
                    break;
            }
            tokPtr++;
        }

        if (reference.length() != 0) {
            resultList.add(new LabeledReferenceResult(referenceLabel.length() == 0 ? null :
                    referenceLabel.toString().trim(), reference.toString().trim()));
            reference.setLength(0);
            referenceLabel.setLength(0);
        }

//        for (LabeledReferenceResult r : resultList) {
//            System.out.println(r);
//        }
        return resultList;
    }


    public String createTrainingData(String input) {
        List<LabeledReferenceResult> res = extract(input);
        StringBuilder sb = new StringBuilder();

        sb.append("<tei>\n" +
                "    <teiHeader>\n" +
                "        <fileDesc xml:id=\"0\"/>\n" +
                "    </teiHeader>\n" +
                "    <text xml:lang=\"en\">\n" +
                "        <listBibl>");

        for (LabeledReferenceResult p : res) {
            if (p.getLabel() != null) {
                sb.append(String.format("<bibl> <label>%s</label>%s</bibl>", p.getLabel(), p.getReferenceText()));
            } else {
                sb.append(String.format("<bibl>%s</bibl>", p.getReferenceText()));
            }
            sb.append("\n");
        }

        sb.append("        </listBibl>\n" +
                "    </text>\n" +
                "</tei>\n");
        return sb.toString();
    }

}
