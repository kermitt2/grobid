package org.grobid.core.data;

import java.net.URI;
import java.lang.StringBuilder;
import java.util.*;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import nu.xom.Attribute;
import nu.xom.Element;
import org.grobid.core.document.xml.XmlBuilderUtils;
import org.grobid.core.engines.config.GrobidAnalysisConfig;
import org.grobid.core.layout.BoundingBox;
import org.grobid.core.layout.GraphicObject;
import org.grobid.core.utilities.LayoutTokensUtil;
import org.grobid.core.utilities.TextUtilities;
import org.grobid.core.layout.LayoutToken;

/**
 * Class for representing a figure.
 *
 * @author Patrice Lopez
 */
public class Figure {
	protected StringBuilder caption = null;
	protected StringBuilder header = null;
	protected StringBuilder content = null;
	protected StringBuilder label = null;
	protected String id = null;
	protected URI uri = null;
	protected int start = -1; // start position in the full text tokenization
	protected int end = -1; // end position in the full text tokenization
	protected LayoutToken startToken = null; // start layout token
	protected LayoutToken endToken = null; // end layout token
	private List<BoundingBox> textArea;
	private List<LayoutToken> layoutTokens;

	// coordinates
	private int page = -1;
	private double y = 0.0;
	private double x = 0.0;
	private double width = 0.0;
	private double height = 0.0;

	// list of graphic objects corresponding to the figure
	protected List<GraphicObject> graphicObjects = null;

	public Figure() {
		caption = new StringBuilder();
		header = new StringBuilder();
		content = new StringBuilder();
		label = new StringBuilder();
	}

	public void appendHeader(String head) {
		header.append(head);
	}

	public String getHeader() {
		return header.toString();
	}

	public void appendCaption(String cap) {
		caption.append(cap);
	}

	public String getCaption() {
		return caption.toString();
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

	public void setURI(URI theURI) {
		uri = theURI;
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

	public List<GraphicObject> getGraphicObjects() {
		return graphicObjects;
	}

	public List<GraphicObject> getBitmapGraphicObjects() {
		if (graphicObjects == null) {
			return null;
		}
		ArrayList<GraphicObject> graphicObjects = Lists.newArrayList(Iterables.filter(this.graphicObjects, new Predicate<GraphicObject>() {
			@Override
			public boolean apply(GraphicObject graphicObject) {
				return graphicObject.getType() == GraphicObject.BITMAP;
			}
		}));
		if (graphicObjects.isEmpty()) {
			return null;
		}
		return graphicObjects;
	}

	public void addGraphicObject(GraphicObject obj) {
		if (graphicObjects == null)
			graphicObjects = new ArrayList<GraphicObject>();
		graphicObjects.add(obj);
	}

	public void setGraphicObjects(List<GraphicObject> objs) {
		graphicObjects = objs;
	}

	/**
	 * Simple block coordinates. TBD: generate bounding box.
	 */
	public String getCoordinates() {
		return String.format("%d,%.2f,%.2f,%.2f,%.2f", page, x, y, width, height);
	}

	public String toTEI(int indent, GrobidAnalysisConfig config) {
		if (((header == null) || (header.length() == 0)) &&
				((caption == null) || (caption.length() == 0)) //&&
			//((graphicObjects != null) && (graphicObjects.size() == 0))
				) {
			return null;
		}
//        StringBuilder theFigure = new StringBuilder();

		Element figureElement = XmlBuilderUtils.teiElement("figure");
//        theFigure.append("\n");
//       	for(int i=0; i<indent; i++)
//			theFigure.append("\t");
//		theFigure.append("<figure");
		if (id != null) {
//			theFigure.append(" xml:id=\"fig_" + id + "\"");
			XmlBuilderUtils.addXmlId(figureElement, "fig_" + id);
		}

		if (config.isGenerateTeiCoordinates()) {
//			theFigure.append(" coords=\"" + getCoordinates() + "\"");
			XmlBuilderUtils.addCoords(figureElement, getCoordinates());
		}
//		theFigure.append(">\n");
		if (header != null) {
//	       	for(int i=0; i<indent+1; i++)
//				theFigure.append("\t");
//			theFigure.append("<head>").append(cleanString(
//				TextUtilities.HTMLEncode(header.toString())))
//				.append("</head>\n");
			Element head = XmlBuilderUtils.teiElement("head",
					LayoutTokensUtil.normalizeText(header.toString()));
			figureElement.appendChild(head);

		}
		if (caption != null) {
//			for(int i=0; i<indent+1; i++)
//				theFigure.append("\t");
//			theFigure.append("<figDesc>").append(cleanString(
//				TextUtilities.HTMLEncode(TextUtilities.dehyphenizeHard(caption.toString()))))
//				.append("</figDesc>\n");

			Element desc = XmlBuilderUtils.teiElement("figDesc",
					LayoutTokensUtil.normalizeText(caption.toString()));
			figureElement.appendChild(desc);
		}
		if ((graphicObjects != null) && (graphicObjects.size() > 0)) {
			for (GraphicObject graphicObject : graphicObjects) {
//		       	for(int i=0; i<indent+1; i++)
//					theFigure.append("\t");
//				theFigure.append("<graphic url=\"" + graphicObject.getURI() + "\" />\n");

				Element go = XmlBuilderUtils.teiElement("graphic");
				go.addAttribute(new Attribute("url", graphicObject.getURI()));
				figureElement.appendChild(go);
			}
		}
//		for(int i=0; i<indent; i++)
//			theFigure.append("\t");
//		theFigure.append("</figure>\n");
		return figureElement.toXML();
	}

	private String cleanString(String input) {
		return input.replace("\n", " ").replace("  ", " ").trim();
	}

	public int getPage() {
		return page;
	}

	public double getHeight() {
		return height;
	}

	public double getWidth() {
		return width;
	}

	public double getX() {
		return x;
	}

	public double getY() {
		return y;
	}

	public URI getUri() {
		return uri;
	}

	public void setPage(int page) {
		this.page = page;
	}

	public void setY(double y) {
		this.y = y;
	}

	public void setX(double x) {
		this.x = x;
	}

	public void setWidth(double width) {
		this.width = width;
	}

	public void setHeight(double height) {
		this.height = height;
	}

	public List<BoundingBox> getTextArea() {
		return textArea;
	}

	public void setTextArea(List<BoundingBox> textArea) {
		this.textArea = textArea;
	}

	public List<LayoutToken> getLayoutTokens() {
		return layoutTokens;
	}

	public void setLayoutTokens(List<LayoutToken> layoutTokens) {
		this.layoutTokens = layoutTokens;
	}
}
