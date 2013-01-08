package org.grobid.core.annotations;

import static org.grobid.core.utilities.TeiValues.ATTR_ID;
import static org.grobid.core.utilities.TeiValues.TAG_DIV;
import static org.grobid.core.utilities.TeiValues.TAG_P;
import static org.grobid.core.utilities.TeiValues.VAL_STRING_RANGE;
import static org.grobid.core.utilities.TeiValues.W3C_NAMESPACE;
import static org.grobid.core.utilities.TeiValues.XML;
import static org.grobid.core.utilities.TextUtilities.AND;
import static org.grobid.core.utilities.TextUtilities.COMMA;
import static org.grobid.core.utilities.TextUtilities.DOUBLE_QUOTE;
import static org.grobid.core.utilities.TextUtilities.END_BRACKET;
import static org.grobid.core.utilities.TextUtilities.ESC_AND;
import static org.grobid.core.utilities.TextUtilities.ESC_DOUBLE_QUOTE;
import static org.grobid.core.utilities.TextUtilities.ESC_GREATER_THAN;
import static org.grobid.core.utilities.TextUtilities.ESC_LESS_THAN;
import static org.grobid.core.utilities.TextUtilities.GREATER_THAN;
import static org.grobid.core.utilities.TextUtilities.LESS_THAN;
import static org.grobid.core.utilities.TextUtilities.NEW_LINE;
import static org.grobid.core.utilities.TextUtilities.OR;
import static org.grobid.core.utilities.TextUtilities.QUOTE;
import static org.grobid.core.utilities.TextUtilities.SHARP;
import static org.grobid.core.utilities.TextUtilities.SPACE;
import static org.grobid.core.utilities.TextUtilities.START_BRACKET;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.StartElement;

import org.apache.commons.lang3.StringUtils;
import org.grobid.core.data.BibDataSet;
import org.grobid.core.data.PatentItem;
import org.grobid.core.exceptions.GrobidException;
import org.grobid.core.utilities.XMLWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Contains the information about the current parsed description.
 * 
 * @author Damien
 * 
 */
public class Description {

	/**
	 * The class LOGGER.
	 */
	private static final Logger LOGGER = LoggerFactory.getLogger(Description.class);

	/**
	 * Characters to escape: "<|>|&|\"".
	 */
	private static final String CHARS2ESCAPE = LESS_THAN + OR + GREATER_THAN + OR + AND + OR + DOUBLE_QUOTE;

	/**
	 * QName for attribute id of paragraph.
	 */
	private static final QName paragraphIdQName = new QName(W3C_NAMESPACE, ATTR_ID, XML);

	/**
	 * The StartElement of the description tag (<div type="description" ...).
	 */
	protected StartElement descriptionTag;

	/**
	 * The gorn index.
	 */
	protected String gornIdxStart;

	/**
	 * The list of all paragraphs parsed.
	 */
	protected List<Paragraph> paragraphs;

	/**
	 * The,current parsed paragraph.
	 */
	protected Paragraph currParagraph;

	/**
	 * The raw description containing all the text of paragraphs.
	 */
	protected StringBuffer rawDescription;

	/**
	 * If the description output will be indented.
	 */
	protected boolean isIndented;

	/**
	 * Constructor.
	 * 
	 * @param pIsIndented
	 *            If the output will be indented.
	 */
	public Description(final boolean pIsIndented) {
		isIndented = pIsIndented;
		paragraphs = new ArrayList<Paragraph>();
		rawDescription = new StringBuffer();
	}

	/**
	 * Process the start of the description.
	 * 
	 * @param pDescriptionTag
	 *            The StartElement of the description.
	 * @param pGornIdxStart
	 *            The gorn index of the description.
	 */
	public void appendStartDescription(final StartElement pDescriptionTag, final String pGornIdxStart) {
		descriptionTag = pDescriptionTag;
		gornIdxStart = pGornIdxStart;
	}

	/**
	 * Process the start of a paragraph.
	 * 
	 * @param pStart
	 *            The StartElement of the current paragraph parsed.
	 */
	public void appendParagraphStartTag(final StartElement pStart) {
		currParagraph = new Paragraph();
		currParagraph.setStartTag(pStart);
		currParagraph.setPositionStart(rawDescription.length());
	}

	/**
	 * Append the content of the paragraph to {@code pContent}.
	 * 
	 * @param pContent
	 *            the content to append.
	 */
	public void appendParagraphContent(String pContent) {
		if (Pattern.matches(CHARS2ESCAPE, pContent)) {
			pContent = pContent.replaceAll(AND, ESC_AND).replaceAll(LESS_THAN, ESC_LESS_THAN).replaceAll(GREATER_THAN, ESC_GREATER_THAN)
					.replaceAll(DOUBLE_QUOTE, ESC_DOUBLE_QUOTE);
			rawDescription.append(pContent);
		} else {
			// Remove extra space
			rawDescription.append(pContent.replaceAll("\\p{Space}{2,}+", " "));
		}
	}

	/**
	 * Add some raw content to rawDescription.
	 * 
	 * @param pRawContent
	 */
	public void appendRawContent(final String pRawContent) {
		rawDescription.append(pRawContent);
	}

	/**
	 * Process the end of the current parsed paragraph.
	 */
	public void appendParagraphEndTag() {
		// Append a space at the end of the paragraph in case ther is no space
		// between the end of this paragraph and the start of the next one.
		currParagraph.setPositionEnd(rawDescription.length());
		rawDescription.append(" ");
		paragraphs.add(currParagraph.clone());
	}

	/**
	 * Return the value of rawDescription.
	 * 
	 * @return
	 */
	public String toRawString() {
		return rawDescription.toString();
	}

	/**
	 * @return The description in tei format.
	 */
	public String toTei() {
		if (isIndented)
			return toTeiIndented();
		else
			return toTeiNotIndented();
	}

	/**
	 * Generate the tag ptr that give the position of the annotation.
	 * 
	 * @param pItem
	 *            Object instance of either PatentItem or BibDataSet.
	 * 
	 * @return Null if the information got are not sufficient to generate the
	 *         pointer.
	 * 
	 * @throws GrobidException
	 */
	public String getPointer(final Object pItem) throws GrobidException {
		int startPointer = -1;
		int offset = -1;
		Paragraph paragraph = null;

		if (pItem instanceof PatentItem) {
			final PatentItem patentItem = (PatentItem) pItem;

			startPointer = patentItem.getOffsetBegin();
			offset = patentItem.getOffsetEnd() - patentItem.getOffsetBegin();
			paragraph = getParagraphFromPointer(startPointer);
		} else if (pItem instanceof BibDataSet) {
			final BibDataSet dataSet = (BibDataSet) pItem;

			final List<Integer> offsets = dataSet.getOffsets();
			if (offsets == null || offsets.isEmpty() || offsets.get(0) == null) {
				LOGGER.error("Could not get the offset from BibDataSet.");
				return null;
			}

			startPointer = offsets.get(0);
			offset = dataSet.getRawBib().length();
			paragraph = getParagraphFromPointer(startPointer);
		}

		if (paragraph == null) {
			LOGGER.error("Could not build a proper pointer with the input data. Paragraph=" + null + ", pointer=" + startPointer
					+ ", offset=" + offset);
			return null;
		}
		return buildPointer(paragraph.getId() != null ? paragraph.getId() : paragraph.getGornIdx(),
				startPointer - paragraph.getPositionStart(), offset);
	}

	/**
	 * Reset the description to be used again.
	 */
	public void resetDescription() {
		paragraphs = new ArrayList<Paragraph>();
		rawDescription = new StringBuffer();
	}

	/**
	 * @return The description in tei format indented.
	 */
	protected String toTeiIndented() {
		final XMLWriter tei = new XMLWriter();
		try {
			tei.addStartElement(descriptionTag);
			tei.addCharacters(NEW_LINE);
			for (Paragraph paragraph : paragraphs) {
				tei.addCharacters(SPACE + SPACE);
				if (!paragraph.isAnnotated() || paragraph.getId() != null) {
					tei.addStartElement(paragraph.getStartTag());
				} else {
					tei.addStartElement(paragraph.getStartTag(), paragraphIdQName, paragraph.getGornIdx());
				}
				tei.addCharacters(StringUtils.EMPTY);
				tei.addRaw(rawDescription.substring(paragraph.getPositionStart(), paragraph.getPositionEnd()));
				tei.addEndElement(TAG_P);
				tei.addCharacters(NEW_LINE);
			}
			tei.addEndElement(TAG_DIV);
		} catch (XMLStreamException xmlStrExp) {
			throw new GrobidException("An error occured while rebuilding the description: " + xmlStrExp);
		}

		return tei.toString();
	}

	/**
	 * @return The description in tei format not indented.
	 */
	protected String toTeiNotIndented() {
		final XMLWriter tei = new XMLWriter();
		try {
			tei.addStartElement(descriptionTag);
			for (Paragraph paragraph : paragraphs) {
				if (!paragraph.isAnnotated() || paragraph.getId() != null) {
					tei.addStartElement(paragraph.getStartTag());
				} else {
					tei.addStartElement(paragraph.getStartTag(), paragraphIdQName, paragraph.getGornIdx());
				}
				tei.addCharacters(StringUtils.EMPTY);
				tei.addRaw(rawDescription.substring(paragraph.getPositionStart(), paragraph.getPositionEnd()));
				tei.addEndElement(TAG_P);
			}
			tei.addEndElement(TAG_DIV);
		} catch (XMLStreamException xmlStrExp) {
			throw new GrobidException("An error occured while rebuilding the description: " + xmlStrExp);
		}
		return tei.toString();
	}

	/**
	 * Build the tag ptr with the input parameters.
	 * 
	 * @param pIdParagraph
	 *            The id of the annotated paragraph.
	 * @param pCharPointer
	 *            The offset of the start character.
	 * @param pCharOffset
	 *            The lenght of the annatated citation.
	 * @return the built ptr tag.
	 */
	protected String buildPointer(final String pIdParagraph, final int pCharPointer, final int pCharOffset) {
		final StringBuffer pointer = new StringBuffer(SHARP);
		pointer.append(VAL_STRING_RANGE);
		pointer.append(START_BRACKET);
		pointer.append(QUOTE + pIdParagraph + QUOTE).append(COMMA);
		pointer.append(pCharPointer).append(COMMA);
		pointer.append(pCharOffset).append(END_BRACKET);

		return pointer.toString();
	}

	/**
	 * Return the paragraph on which {@code pPointer} is pointing.
	 * 
	 * @param pPointer
	 * @return
	 */
	protected final Paragraph getParagraphFromPointer(final int pPointer) {
		int idxP = paragraphs.indexOf(new Paragraph(pPointer));
		if (idxP != -1) {
			final Paragraph currP = paragraphs.get(idxP);
			final String pId = currP.getIdFromAttributes();
			currP.setId(pId);
			currP.setAnnotated(true);
			if (pId == null) {
				currP.setGornIdx(gornIdxStart + (idxP + 1));
			}
			return currP;
		} else
			return null;
	}

}
