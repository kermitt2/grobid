package org.grobid.core.annotations;

import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.grobid.core.utilities.TeiValues.ATTR_CERT;
import static org.grobid.core.utilities.TeiValues.ATTR_IDENT;
import static org.grobid.core.utilities.TeiValues.ATTR_SUB_TYPE;
import static org.grobid.core.utilities.TeiValues.ATTR_TARGET;
import static org.grobid.core.utilities.TeiValues.ATTR_TYPE;
import static org.grobid.core.utilities.TeiValues.ATTR_VERSION;
import static org.grobid.core.utilities.TeiValues.ATTR_WHEN;
import static org.grobid.core.utilities.TeiValues.CERTAINTY_HIGH_LABEL;
import static org.grobid.core.utilities.TeiValues.CERTAINTY_LOW_LABEL;
import static org.grobid.core.utilities.TeiValues.CERTAINTY_MEDIUM_LABEL;
import static org.grobid.core.utilities.TeiValues.CERTAINTY_VERY_HIGH_LABEL;
import static org.grobid.core.utilities.TeiValues.CERTAINTY_VERY_LOW_LABEL;
import static org.grobid.core.utilities.TeiValues.DEGREE_OF_CONFIDENCE;
import static org.grobid.core.utilities.TeiValues.GROBID;
import static org.grobid.core.utilities.TeiValues.GROBID_LABEL;
import static org.grobid.core.utilities.TeiValues.NO_CERTAINTY;
import static org.grobid.core.utilities.TeiValues.TAG_APPLICATION;
import static org.grobid.core.utilities.TeiValues.TAG_APP_INFO;
import static org.grobid.core.utilities.TeiValues.TAG_AUTHOR;
import static org.grobid.core.utilities.TeiValues.TAG_CERTAINTY;
import static org.grobid.core.utilities.TeiValues.TAG_DATE;
import static org.grobid.core.utilities.TeiValues.TAG_ITEM;
import static org.grobid.core.utilities.TeiValues.TAG_LABEL;
import static org.grobid.core.utilities.TeiValues.TAG_LIST;
import static org.grobid.core.utilities.TeiValues.TAG_NOTE;
import static org.grobid.core.utilities.TeiValues.TAG_PTR;
import static org.grobid.core.utilities.TeiValues.VAL_AUTOMATIC_ANOTATION;
import static org.grobid.core.utilities.TeiValues.VAL_SOFTWARE_APPLICATION;
import static org.grobid.core.utilities.TeiValues.VAL_STANDOFF_ANOTATION;
import static org.grobid.core.utilities.TeiValues.VERS_1_0;

import java.util.Date;
import java.util.List;

import javax.xml.stream.XMLStreamException;

import org.grobid.core.data.BibDataSet;
import org.grobid.core.data.PatentItem;
import org.grobid.core.exceptions.GrobidException;
import org.grobid.core.utilities.Utilities;
import org.grobid.core.utilities.XMLWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * That object takes a list of PatentItem and/or BibDataSet and generate the
 * annotation in tei format to insert inside the xml.
 * 
 * @author Damien
 * 
 */
public class Annotation {

	/**
	 * The class LOGGER.
	 */
	private static final Logger LOGGER = LoggerFactory.getLogger(Annotation.class);

	/**
	 * Date format for header annotation.
	 */
	private static final String YYYY_MM_DD = "yyyy-MM-dd";

	/**
	 * List the the patent items.
	 */
	protected final List<PatentItem> patents;

	/**
	 * List of the articles items.
	 */
	protected final List<BibDataSet> articles;

	/**
	 * The description.
	 */
	protected final Description description;

	/**
	 * The XMLWriter to generate XML.
	 */
	protected XMLWriter writer;

	/**
	 * The constructor.
	 * 
	 * @param pPatents
	 *            The list of patents.
	 * @param pArticles
	 *            The list of articles.
	 * @param pDescription
	 *            The description.
	 */
	public Annotation(final List<PatentItem> pPatents, final List<BibDataSet> pArticles, final Description pDescription) {
		patents = pPatents;
		articles = pArticles;
		description = pDescription;
	}

	/**
	 * Generate thre header from the patents, articles and description got in
	 * the constructor.
	 * 
	 * @return The header annotation corresponding to the description. If both
	 *         patents and articles lists are empty an empty String is returned.
	 */
	public String getHeaderAnnotation(final boolean pIndent) {
		if (!patents.isEmpty() || !articles.isEmpty()) {
			writer = new XMLWriter();
			try {
				writer.addStartElement(TAG_NOTE, ATTR_TYPE, VAL_STANDOFF_ANOTATION, ATTR_SUB_TYPE, VAL_AUTOMATIC_ANOTATION);
				addListItems();
				writer.addEndElement(TAG_NOTE);
			} catch (XMLStreamException xmlsExp) {
				throw new GrobidException("Failed to generate the header of document", xmlsExp);
			}
			if (pIndent)
				return writer.toStringIndented();
			else
				return writer.toString();
		}

		return EMPTY;
	}

	/**
	 * Add the items (patents and articles).
	 * 
	 * @throws GrobidException
	 * @throws XMLStreamException
	 */
	protected void addListItems() throws GrobidException, XMLStreamException {
		writer.addStartElement(TAG_LIST, ATTR_TYPE, VAL_AUTOMATIC_ANOTATION);
		for (PatentItem patent : patents) {
			addItem(patent);
		}
		for (BibDataSet article : articles) {
			addItem(article);
		}
		writer.addEndElement(TAG_LIST);
	}

	/**
	 * Add an annotation to the writer with the pCurrItem given in argument.
	 * pCurrItem has to be either an instance of PatentItem or BibDataSet.
	 * 
	 * @param pCurrItem
	 *            either an instance of PatentItem or BibDataSet.
	 * @throws XMLStreamException
	 * @throws GrobidException
	 */
	protected void addItem(final Object pCurrItem) throws XMLStreamException, GrobidException {
		final String tei = getTeiFromItem(pCurrItem);
		final String pointer = description.getPointer(pCurrItem);
		final double confidence = getConfidenceRateFromItem(pCurrItem);
		final String strConfidence = confidenceRateToString(confidence);

		if (tei != null && pointer != null) {
			buildItem(tei, pointer, confidence, strConfidence, Utilities.dateToString(new Date(), YYYY_MM_DD));
		}
	}

	/**
	 * Generate the item in TEI format.
	 * 
	 * @param pTei
	 *            The TEI of the item.
	 * @param pPointer
	 *            The TEI pointer.
	 * @param pConfidence
	 *            The degree of confidence.
	 * @param pStrConfidence
	 *            The label of degree of confidence.
	 * @throws XMLStreamException
	 */
	protected void buildItem(final String pTei, final String pPointer, final double pConfidence, final String pStrConfidence,
			final String pDate) throws XMLStreamException {
		writer.addStartElement(TAG_ITEM);

		writer.addStartElement(TAG_DATE, ATTR_WHEN, pDate);
		writer.addEndElement(TAG_DATE);

		writer.addStartElement(TAG_AUTHOR, ATTR_TYPE, VAL_SOFTWARE_APPLICATION);

		writer.addStartElement(TAG_APP_INFO);
		writer.addStartElement(TAG_APPLICATION, ATTR_VERSION, VERS_1_0, ATTR_IDENT, GROBID);
		writer.addStartElement(TAG_LABEL);
		writer.addCharacters(GROBID_LABEL);
		writer.addEndElement(TAG_LABEL);
		writer.addEndElement(TAG_APPLICATION);
		writer.addEndElement(TAG_APP_INFO);

		/*writer.addStartElement(TAG_CERTAINTY, ATTR_CERT, String.valueOf(pConfidence));
		writer.addStartElement(TAG_LABEL);
		writer.addCharacters(pStrConfidence);
		writer.addEndElement(TAG_LABEL);
		writer.addEndElement(TAG_CERTAINTY);*/
		writer.addEndElement(TAG_AUTHOR);

		writer.addRaw(pTei);

		writer.addStartElement(TAG_PTR, ATTR_TARGET, pPointer);
		writer.addEndElement(TAG_PTR);

		writer.addEndElement(TAG_ITEM);
	}

	/**
	 * Get the biblStruct generated tag.
	 * 
	 * @param pItem
	 *            either an instance of PatentItem or BibDataSet.
	 * @return "<biblStruct>...</biblStruct>" annotation element.
	 */
	protected static String getTeiFromItem(final Object pItem) {
		String tei = null;
		if (pItem instanceof PatentItem) {
			final PatentItem patentItem = (PatentItem) pItem;
			//tei = patentItem.toTEI(Utilities.dateToString(new Date(), YYYY_MM_DD));
			tei = patentItem.toTEI(null);
		} else if (pItem instanceof BibDataSet) {
			final BibDataSet dataSet = (BibDataSet) pItem;
			tei = dataSet.getResBib().toTEI(-1);
		}
		if (tei == null) {
			LOGGER.error("The item.toTei should not be null.");
		}
		return tei;
	}

	/**
	 * Get the confidence rate of the item.
	 * 
	 * @param pItem
	 *            either an instance of PatentItem or BibDataSet
	 * @return the confidence rate.
	 */
	protected static double getConfidenceRateFromItem(final Object pItem) {
		double confidence = -1;
		if (pItem instanceof PatentItem) {
			final PatentItem patentItem = (PatentItem) pItem;
			confidence = patentItem.getConf();
		} else if (pItem instanceof BibDataSet) {
			final BibDataSet dataSet = (BibDataSet) pItem;
			confidence = dataSet.getConfidence();
		}
		/*if (confidence == -1) {
			throw new GrobidException("The item.confidence could not be determined.");
		}*/
		return confidence;
	}

	/**
	 * Convert the confidence rate to String sentence.
	 * 
	 * @param pConfidence
	 *            the confidence rate in percent.
	 * @return the sentence corresponding to the confidence rate.
	 */
	protected static String confidenceRateToString(final double pConfidence) {
		String strConfidence = EMPTY;
		if (pConfidence <= 20) {
			strConfidence = CERTAINTY_VERY_LOW_LABEL;
		} else if (pConfidence > 20 && pConfidence <= 40) {
			strConfidence = CERTAINTY_LOW_LABEL;
		} else if (pConfidence > 40 && pConfidence <= 60) {
			strConfidence = CERTAINTY_MEDIUM_LABEL;
		} else if (pConfidence > 60 && pConfidence <= 80) {
			strConfidence = CERTAINTY_HIGH_LABEL;
		} else if (pConfidence > 80 && pConfidence <= 100) {
			strConfidence = CERTAINTY_VERY_HIGH_LABEL;
		} else {
			strConfidence = NO_CERTAINTY;
		}
		return strConfidence + DEGREE_OF_CONFIDENCE;
	}
}
