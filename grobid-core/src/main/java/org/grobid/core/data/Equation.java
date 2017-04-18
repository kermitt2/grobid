package org.grobid.core.data;

import nu.xom.Attribute;
import nu.xom.Element;
import org.apache.commons.lang3.StringUtils;
import org.grobid.core.document.xml.XmlBuilderUtils;
import org.grobid.core.engines.Engine;
import org.grobid.core.engines.config.GrobidAnalysisConfig;
import org.grobid.core.layout.BoundingBox;
import org.grobid.core.layout.LayoutToken;
import org.grobid.core.utilities.BoundingBoxCalculator;
import org.grobid.core.utilities.LayoutTokensUtil;
import org.grobid.core.utilities.counters.CntManager;
import org.grobid.core.utilities.TextUtilities;

import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;

/**
 * Class for representing an equation.
 *
 * @author Patrice Lopez
 */
public class Equation {
	protected StringBuilder content = null;
    protected StringBuilder label = null;
    protected String id = null;
    protected int start = -1; // start position in the full text tokenization
    protected int end = -1; // end position in the full text tokenization
    protected LayoutToken startToken = null; // start layout token
    protected LayoutToken endToken = null; // end layout token
    private List<BoundingBox> textArea;
    private List<LayoutToken> layoutTokens;

	private List<LayoutToken> contentTokens = new ArrayList<>();
	private List<LayoutToken> labelTokens = new ArrayList<>();

	private SortedSet<Integer> blockPtrs;

    public Equation() {
    	content = new StringBuilder();
    	label = new StringBuilder();
    }

    public String toTEI(GrobidAnalysisConfig config) {
		if (StringUtils.isEmpty(content)) {
			return null;
		}

		Element formulaElement = XmlBuilderUtils.teiElement("formula");
		if (id != null) {
			XmlBuilderUtils.addXmlId(formulaElement, "formula_" + id);
		}

		if ((config.getGenerateTeiCoordinates() != null) && (config.getGenerateTeiCoordinates().contains("formula"))) {
			XmlBuilderUtils.addCoords(formulaElement, LayoutTokensUtil.getCoordsStringForOneBox(getLayoutTokens()));
		}

		formulaElement.appendChild(LayoutTokensUtil.normalizeText(content.toString()).trim());

		Element labelEl = XmlBuilderUtils.teiElement("label",
        		LayoutTokensUtil.normalizeText(label.toString()));

		formulaElement.appendChild(labelEl);

		return formulaElement.toXML();
    }

	public List<LayoutToken> getContentTokens() {
		return contentTokens;
	}

	public List<LayoutToken> getLabelTokens() {
		return labelTokens;
	}

	public void appendLabel(String lab) {
        label.append(lab);
    }

    public String getLabel() {
        return label.toString();
    }

    public void appendContent(String trash) {
        content.append(trash);
    }

    public String getContent() {
        return content.toString();
    }

    public void setStart(int start) {
        this.start = start;
    }

    public int getStart() {
        return start;
    }

    public void setEnd(int end) {
        this.end = end;
    }

    public int getEnd() {
        return end;
    }

    public void setStartToken(LayoutToken start) {
        this.startToken = start;
    }

    public LayoutToken getStartToken() {
        return startToken;
    }

    public void setEndToken(LayoutToken end) {
        this.endToken = end;
    }

    public LayoutToken getEndToken() {
        return endToken;
    }

    public void setId() {
        id = TextUtilities.cleanField(label.toString(), false);
    }

    public void setId(String theId) {
        id = theId;
    }

    public String getId() {
        return id;
    }

    public void setBlockPtrs(SortedSet<Integer> blockPtrs) {
        this.blockPtrs = blockPtrs;
    }

    public SortedSet<Integer> getBlockPtrs() {
        return blockPtrs;
    }

    public List<LayoutToken> getLayoutTokens() {
        return layoutTokens;
    }

    public void setLayoutTokens(List<LayoutToken> layoutTokens) {
        this.layoutTokens = layoutTokens;
    }
}