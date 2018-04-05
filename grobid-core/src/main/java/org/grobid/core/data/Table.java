package org.grobid.core.data;

import nu.xom.Attribute;
import nu.xom.Element;
import org.apache.commons.lang3.StringUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.grobid.core.document.xml.XmlBuilderUtils;
import org.grobid.core.engines.Engine;
import org.grobid.core.engines.config.GrobidAnalysisConfig;
import org.grobid.core.layout.BoundingBox;
import org.grobid.core.layout.GraphicObject;
import org.grobid.core.layout.LayoutToken;
import org.grobid.core.utilities.BoundingBoxCalculator;
import org.grobid.core.utilities.LayoutTokensUtil;
import org.grobid.core.utilities.counters.CntManager;
import org.grobid.core.engines.counters.TableRejectionCounters;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import technology.tabula.*;
import technology.tabula.extractors.BasicExtractionAlgorithm;

/**
 * Class for representing a table.
 *
 * @author Patrice Lopez
 */
public class Table extends Figure {
	private List<LayoutToken> contentTokens = new ArrayList<>();
	private List<LayoutToken> fullDescriptionTokens = new ArrayList<>();
	private boolean goodTable = true;

	public void setGoodTable(boolean goodTable) {
		this.goodTable = goodTable;
	}

    public Table() {
    	caption = new StringBuilder();
    	header = new StringBuilder();
    	content = new StringBuilder();
    	label = new StringBuilder();
    }
    
    private double headerHeight = 0;
    
    public void setHeaderHeight(double height) {
    	headerHeight = height;
    }
    
    public double getHeaderHeight() {
    	return headerHeight;
    }

	@Override
    public String toTEI(GrobidAnalysisConfig config) {
		if (StringUtils.isEmpty(header) && StringUtils.isEmpty(caption)) {
			return null;
		}

		Element tableElement = XmlBuilderUtils.teiElement("figure");
		tableElement.addAttribute(new Attribute("type", "table"));
		if (id != null) {
			XmlBuilderUtils.addXmlId(tableElement, "tab_" + id);
		}

		tableElement.addAttribute(new Attribute("validated", String.valueOf(isGoodTable())));

		if ((config.getGenerateTeiCoordinates() != null) && (config.getGenerateTeiCoordinates().contains("figure"))) {
			XmlBuilderUtils.addCoords(tableElement, LayoutTokensUtil.getCoordsStringForOneBox(getLayoutTokens()));
		}

		Element headEl = XmlBuilderUtils.teiElement("head",
        		LayoutTokensUtil.normalizeText(header.toString()));

		Element labelEl = XmlBuilderUtils.teiElement("label",
        		LayoutTokensUtil.normalizeText(label.toString()));

		Element descEl = XmlBuilderUtils.teiElement("figDesc");
		//descEl.appendChild(LayoutTokensUtil.normalizeText(getFullDescriptionTokens()).trim());
		descEl.appendChild(LayoutTokensUtil.normalizeText(caption.toString()).trim());
		if ((config.getGenerateTeiCoordinates() != null) && (config.getGenerateTeiCoordinates().contains("figure"))) {
			XmlBuilderUtils.addCoords(descEl, LayoutTokensUtil.getCoordsString(getFullDescriptionTokens()));
		}

		Element contentEl;
		if (tabulaRes != null)
			contentEl = tabulaResToTEI();
		else {
			contentEl = XmlBuilderUtils.teiElement("table");
			contentEl.appendChild(LayoutTokensUtil.toText(getContentTokens()));
		}
		if ((config.getGenerateTeiCoordinates() != null) && (config.getGenerateTeiCoordinates().contains("figure"))) {
			XmlBuilderUtils.addCoords(contentEl, LayoutTokensUtil.getCoordsStringForOneBox(getContentTokens()));
		}

		tableElement.appendChild(headEl);
		tableElement.appendChild(labelEl);
		tableElement.appendChild(descEl);
		tableElement.appendChild(contentEl);

		return tableElement.toXML();

//		if (config.isGenerateTeiCoordinates())
//			theTable.append(" coords=\"" + getCoordinates() + "\"");
//		theTable.append(">\n");
//		if (header != null) {
//	       	for(int i=0; i<indent+1; i++)
//				theTable.append("\t");
//			theTable.append("<head>").append(cleanString(
//				TextUtilities.HTMLEncode(header.toString())))
//				.append("</head>\n");
//		}
//		if (caption != null) {
//			for(int i=0; i<indent+1; i++)
//				theTable.append("\t");
//			theTable.append("<figDesc>").append(cleanString(
//				TextUtilities.HTMLEncode(TextUtilities.dehyphenize(caption.toString()))))
//				.append("</figDesc>\n");
//		}
//		if (uri != null) {
//	       	for(int i=0; i<indent+1; i++)
//				theTable.append("\t");
//			theTable.append("<graphic url=\"" + uri + "\" />\n");
//		}
//		if (content != null) {
//	       	for(int i=0; i<indent+1; i++)
//				theTable.append("\t");
//			theTable.append("<table>").append(cleanString(
//				TextUtilities.HTMLEncode(content.toString())))
//				.append("</table>\n");
//		}
//		for(int i=0; i<indent; i++)
//			theTable.append("\t");
//		theTable.append("</figure>\n");
//        return theTable.toString();
    }

    private String cleanString(String input) {
    	return input.replace("\n", " ").replace("  ", " ").trim();
    }

    private String[][] tabulaRes = null;
    
    public void tabulaExtract(File pdfFile) throws IOException {
    	
    	PDDocument document = PDDocument.load(pdfFile);
    	ObjectExtractor objectExtractor = new ObjectExtractor(document);
    	technology.tabula.Page page = objectExtractor.extract(getPage());
    	technology.tabula.Page pageArea = page.getArea((float)(getY()+getHeaderHeight()), (float)getX(), (float)(getY()+getHeight()), (float)(getX()+getWidth()));
    	
    	BasicExtractionAlgorithm bea = new BasicExtractionAlgorithm();
    	technology.tabula.Table table = bea.extract(pageArea).get(0);
    	
    	
    	List<List<RectangularTextContainer>> tableRows = table.getRows();
        int maxColCount = 0;
        for (int i = 0; i < tableRows.size(); i++) {
            List<RectangularTextContainer> row = tableRows.get(i);
            if (maxColCount < row.size()) {
                maxColCount = row.size();
            }
        }
        
        tabulaRes = new String[tableRows.size()][maxColCount];
        for (int i=0; i<tableRows.size(); i++) {
            List<RectangularTextContainer> row = tableRows.get(i);
            for (int j=0; j<row.size(); j++) {
            	tabulaRes[i][j] = table.getCell(i, j).getText();
            }
        }
    }
    
    public Element tabulaResToTEI() {
    	Element tableEl = XmlBuilderUtils.teiElement("table");
    	
    	if (tabulaRes != null) {
	    	for (int r=0; r<tabulaRes.length; r++) {
	    		Element rowEl = XmlBuilderUtils.teiElement("tr");
	    		
	            for (int c=0; c<tabulaRes[r].length; c++) {
	            	Element cellEl = XmlBuilderUtils.teiElement("td", tabulaRes[r][c]);
	            	rowEl.appendChild(cellEl);
	            }
	            
	            tableEl.appendChild(rowEl);
	        }
    	}
    	
    	return tableEl;
    }

	// if an extracted table passes some validations rules

	public boolean firstCheck() {
		goodTable = goodTable && validateTable();
		return goodTable;
	}

	public boolean secondCheck() {
		goodTable = goodTable && !badTableAdvancedCheck();
		return goodTable;
	}

	private boolean validateTable() {
		CntManager cnt = Engine.getCntManager();
		if (StringUtils.isEmpty(label) || StringUtils.isEmpty(header) || StringUtils.isEmpty(content)) {
			cnt.i(TableRejectionCounters.EMPTY_LABEL_OR_HEADER_OR_CONTENT);
			return false;
		}

		try {
			Integer.valueOf(getLabel().trim(), 10);
		} catch (NumberFormatException e) {
			cnt.i(TableRejectionCounters.CANNOT_PARSE_LABEL_TO_INT);
			return false;
		}
		if (!getHeader().toLowerCase().startsWith("table")) {
			cnt.i(TableRejectionCounters.HEADER_NOT_STARTS_WITH_TABLE_WORD);
			return false;
		}
		return true;
	}

	private boolean badTableAdvancedCheck() {
		CntManager cnt = Engine.getCntManager();
		BoundingBox contentBox = BoundingBoxCalculator.calculateOneBox(contentTokens, true);
		BoundingBox descBox = BoundingBoxCalculator.calculateOneBox(fullDescriptionTokens, true);

		if (contentBox.getPage() != descBox.getPage()) {
            cnt.i(TableRejectionCounters.HEADER_AND_CONTENT_DIFFERENT_PAGES);
			return true;
        }

		if (contentBox.intersect(descBox)) {
            cnt.i(TableRejectionCounters.HEADER_AND_CONTENT_INTERSECT);
			return true;
        }

		if (descBox.area() > contentBox.area()) {
            cnt.i(TableRejectionCounters.HEADER_AREA_BIGGER_THAN_CONTENT);
			return true;
        }

		if (contentBox.getHeight() < 40) {
            cnt.i(TableRejectionCounters.CONTENT_SIZE_TOO_SMALL);
			return true;
        }

		if (contentBox.getWidth() < 100) {
            cnt.i(TableRejectionCounters.CONTENT_WIDTH_TOO_SMALL);
			return true;
        }

		if (contentTokens.size() < 10) {
            cnt.i(TableRejectionCounters.FEW_TOKENS_IN_CONTENT);
			return true;
        }

		if (fullDescriptionTokens.size() < 5) {
            cnt.i(TableRejectionCounters.FEW_TOKENS_IN_HEADER);
			return true;
        }
		return false;
	}

	public List<LayoutToken> getContentTokens() {
		return contentTokens;
	}

	public List<LayoutToken> getFullDescriptionTokens() {
		return fullDescriptionTokens;
	}

	public boolean isGoodTable() {
		return goodTable;
	}
}