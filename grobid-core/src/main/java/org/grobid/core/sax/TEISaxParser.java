package org.grobid.core.sax;

import java.util.ArrayList;
import java.util.List;

import org.grobid.core.data.BibDataSet;
import org.grobid.core.data.PatentItem;
import org.grobid.core.engines.patent.ReferenceExtractor;
import org.grobid.core.utilities.TeiValues;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * SAX parser which accumulate the textual content under tag <div
 * type="description"> for each publication. Then, it looks for patent citation
 * and add annotation if found out.
 * 
 * @author Damien
 */
public class TEISaxParser extends DefaultHandler {

	private static final Logger LOGGER = LoggerFactory
			.getLogger(TEISaxParser.class);

	/**
	 * The current TEIParsedInfo.
	 */
	TEIParsedInfo currTEIParsedInfo;

	/**
	 * The class reference extractor.
	 */
	ReferenceExtractor extractor;

	/**
	 * Constructor.
	 * 
	 * @param pExtractor
	 *            the instance of ReferenceExtractor.
	 */
	public TEISaxParser(ReferenceExtractor pExtractor) {
		extractor = pExtractor;
		currTEIParsedInfo = new TEIParsedInfo();
	}

	/**
	 * Empty Constructor.
	 * 
	 */
	public TEISaxParser() {
		this(new ReferenceExtractor());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void characters(char[] buffer, int start, int length) {
		if (currTEIParsedInfo.isAppendDescription()) {
			currTEIParsedInfo.appendDescription(buffer, start, length);
		} else if (currTEIParsedInfo.isCurrTagDocumber()) {
			currTEIParsedInfo.setPubDocNumber(new String(buffer, start, length));
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void startElement(String namespaceURI, String localName,
			String qName, Attributes atts) throws SAXException {
		currTEIParsedInfo.appendTag2XmlPath(qName);
		currTEIParsedInfo.setCurrTagAttrs(atts);

		if (currTEIParsedInfo.isDescriptionTag()) {
			currTEIParsedInfo.setAppendDescription(true);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void endElement(String uri, String localName, String qName)
			throws SAXException {
		if (currTEIParsedInfo.isAppendDescription()
				&& TeiValues.TAG_DIV.equalsIgnoreCase(currTEIParsedInfo
						.getLastXmlPath())) {
			currTEIParsedInfo.setAppendDescription(false);
		} else if (TeiValues.TAG_TEI.equalsIgnoreCase(currTEIParsedInfo
				.getLastXmlPath())) {
			processExtraction();
		} else if (currTEIParsedInfo.isAppendDescription()) {
			// currTEIParsedInfo.appendDescription("\\cx\\e");
		}

		if (qName.equalsIgnoreCase(currTEIParsedInfo.getLastXmlPath())) {
			System.out.println(currTEIParsedInfo.getXmlPath().toString());
			currTEIParsedInfo.removeLastXmlPath();
		}
	}

	protected void processExtraction() {
		List<PatentItem> patents = new ArrayList<PatentItem>();
		List<BibDataSet> articles = new ArrayList<BibDataSet>();
		extractor.extractAllReferencesString(currTEIParsedInfo.getDescription()
				.toString(), false, false, patents, articles);
	}

}
