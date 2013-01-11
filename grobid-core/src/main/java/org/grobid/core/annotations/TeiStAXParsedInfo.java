package org.grobid.core.annotations;

import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.grobid.core.utilities.TeiValues.ATTR_TYPE;
import static org.grobid.core.utilities.TeiValues.TAG_DIV;
import static org.grobid.core.utilities.TeiValues.TAG_P;
import static org.grobid.core.utilities.TeiValues.VAL_DESCRIPTION;

import java.util.Iterator;

import javax.xml.namespace.QName;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.StartElement;

import org.grobid.core.utilities.GornIndex;

/**
 * Class containing all the informations about the parsed publication of TEI
 * document.
 * 
 * @author Damien
 * 
 */
public class TeiStAXParsedInfo {

	/**
	 * QName for the attribute type.
	 */
	private static final QName qNameType = new QName(EMPTY, ATTR_TYPE, EMPTY);

	/**
	 * Gorn index of the document.
	 */
	protected GornIndex gorn;

	/**
	 * Content of the description.
	 */
	protected Description description;

	/**
	 * Constructor.
	 * 
	 * @param pIndent
	 *            If the description has to be indented.
	 */
	public TeiStAXParsedInfo(final boolean pIndent) {
		gorn = new GornIndex();
		description = new Description(pIndent);
	}

	/**
	 * Reset the description to process a new one.
	 */
	public void resetDescription() {
		description.resetDescription();
	}

	/**
	 * Checks if the tag {@code pTagName} with attributes {@code pAttributtes}
	 * is a description. If it is a description, the object {@link #description}
	 * is initiated.
	 * 
	 * @param pTagName
	 *            The tag name.
	 * @param pAttributtes
	 *            The attributes of the tage name.
	 * 
	 * @return true if the current tag is a description, false else.
	 */
	public boolean checkIfDescription(final StartElement pDescriptionTag) {
		if (!TAG_DIV.equals(pDescriptionTag.getName().getLocalPart())) {
			return false;
		} else {
			if (isAttrTypeDescription(pDescriptionTag)) {
				description.appendStartDescription(pDescriptionTag, gorn.getCurrentGornIndex());
				return true;
			} else
				return false;
		}
	}

	/**
	 * When the parser is in a description, this method is called to process the
	 * start of a paragraph. If the StartElement is not a paragraph, nothing is
	 * processed.
	 * 
	 * @param pStart
	 *            the StartElement to process.
	 * @return true if the StartElement was a paragraph.
	 */
	public boolean processParagraphStartTag(final StartElement pStart) {
		final String pTagName = pStart.getName().getLocalPart();
		if (TAG_P.equals(pTagName)) {
			description.appendParagraphStartTag(pStart);
			return true;
		}
		return false;
	}

	/**
	 * When the parser is inside a paragraph (See
	 * {@link #processParagraphStartTag(String, Iterator)}, this method is
	 * called to notify that the parser is finishing to parse a paragraph or
	 * not.
	 * 
	 * @param pTagName
	 *            The name of the tag.
	 * 
	 * @return true if the ending tag is a paragraph, false else.
	 */
	public boolean processParagraphEndTag(final String pTagName) {
		if (TAG_P.equals(pTagName)) {
			description.appendParagraphEndTag();
			return true;
		}
		return false;
	}

	/**
	 * When inside a paragraph (See
	 * {@link #processParagraphStartTag(String, Iterator)}), this method is
	 * called to add some content to the {@link #description}.
	 * 
	 * @param pContent
	 *            The content to add.
	 */
	public void appendDescriptionContent(final String pContent) {
		description.appendParagraphContent(pContent);
	}

	/**
	 * @return The current gorn index.
	 */
	public String getCurrentGornIndex() {
		return gorn.getCurrentGornIndex();
	}

	/**
	 * Increment the gorn index.
	 */
	public void incrementGornIndex() {
		gorn.incrementIndex();
	}

	/**
	 * Decrement the gorn index.
	 */
	public void decrementGornIndex() {
		gorn.decrementIndex();
	}

	/**
	 * @return The description
	 */
	public Description getDescription() {
		return description;
	}

	/**
	 * Check if the StartElement has an attribute of the type
	 * type="description".
	 * 
	 * @param pStartTag
	 *            the StartElement.
	 * 
	 * @return true if it is a description, false else.
	 */
	protected static boolean isAttrTypeDescription(final StartElement pStartTag) {
		final Attribute attr = pStartTag.getAttributeByName(qNameType);
		if (attr != null) {
			return VAL_DESCRIPTION.equals(attr.getValue());
		} else
			return false;
	}
}
