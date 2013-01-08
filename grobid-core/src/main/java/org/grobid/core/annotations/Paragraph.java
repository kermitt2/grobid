package org.grobid.core.annotations;

import static org.grobid.core.utilities.TeiValues.ATTR_ID;
import static org.grobid.core.utilities.TeiValues.W3C_NAMESPACE;
import static org.grobid.core.utilities.TeiValues.XML;

import javax.xml.namespace.QName;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.StartElement;

import org.apache.commons.lang3.builder.HashCodeBuilder;

/**
 * Contains the information of a paragraph.
 * 
 * @author Damien
 * 
 */
public class Paragraph implements Cloneable {

	/**
	 * QName for attribute id of paragraph.
	 */
	private static final QName paragraphIdQName = new QName(W3C_NAMESPACE, ATTR_ID, XML);

	/**
	 * The id.
	 */
	protected String id;

	/**
	 * The gorn index.
	 */
	protected String gornIdx;

	/**
	 * The StartElement.
	 */
	protected StartElement startTag;

	/**
	 * The start position in Description.
	 */
	protected int positionStart;

	/**
	 * The end position in Description.
	 */
	protected int positionEnd;

	/**
	 * If the paragraph is annotated.
	 */
	protected boolean isAnnotated;

	/**
	 * The description pointer. This field is used by the method equals. In
	 * Description, an indexOf is done to get the paragraph corresponding to the
	 * description pointer.
	 */
	protected int descriptionPointer;

	/**
	 * Empty constructor.
	 */
	public Paragraph() {

	}

	/**
	 * Constructor.
	 * 
	 * @param pId
	 *            The id.
	 * @param pGornIdx
	 *            The gorn index.
	 * @param pAttributes
	 *            The attributes.
	 * @param pPositionStart
	 *            the start position.
	 * @param pPositionEnd
	 *            The end position.
	 */
	public Paragraph(final String pId, final String pGornIdx, final int pPositionStart, int pPositionEnd, final StartElement pStartTag,
			final boolean pIsAnnotated) {
		id = pId;
		gornIdx = pGornIdx;
		positionStart = pPositionStart;
		positionEnd = pPositionEnd;
		startTag = pStartTag;
		isAnnotated = pIsAnnotated;
	}

	/**
	 * This field is used by the method equals. In Description, an indexOf is
	 * done to get the paragraph corresponding to the description pointer.
	 * 
	 * @param pDescriptionPointer
	 *            The pointer of Description.
	 */
	public Paragraph(final int pDescriptionPointer) {
		descriptionPointer = pDescriptionPointer;
	}

	/**
	 * {@Inherited}
	 */
	@Override
	public Paragraph clone() {
		return new Paragraph(id, gornIdx, positionStart, positionEnd, startTag, isAnnotated);
	}

	/**
	 * @return the id
	 */
	public final String getId() {
		return id;
	}

	/**
	 * @param pId
	 *            the id to set
	 */
	public final void setId(String pId) {
		id = pId;
	}

	/**
	 * @return the gornIdx
	 */
	public final String getGornIdx() {
		return gornIdx;
	}

	/**
	 * @param pGornIdx
	 *            the gornIdx to set
	 */
	public final void setGornIdx(String pGornIdx) {
		gornIdx = pGornIdx;
	}

	/**
	 * @return the startTag
	 */
	public final StartElement getStartTag() {
		return startTag;
	}

	/**
	 * @param pStartTag
	 *            the startTag to set
	 */
	public final void setStartTag(StartElement pStartTag) {
		startTag = pStartTag;
	}

	/**
	 * @return the positionStart
	 */
	public final int getPositionStart() {
		return positionStart;
	}

	/**
	 * @param pPositionStart
	 *            the positionStart to set
	 */
	public final void setPositionStart(int pPositionStart) {
		positionStart = pPositionStart;
	}

	/**
	 * @return the positionEnd
	 */
	public final int getPositionEnd() {
		return positionEnd;
	}

	/**
	 * @param pPositionEnd
	 *            the positionEnd to set
	 */
	public final void setPositionEnd(int pPositionEnd) {
		positionEnd = pPositionEnd;
	}

	/**
	 * @return the isAnnotated
	 */
	public final boolean isAnnotated() {
		return isAnnotated;
	}

	/**
	 * @param pIsAnnotated
	 *            the isAnnotated to set
	 */
	public final void setAnnotated(boolean pIsAnnotated) {
		isAnnotated = pIsAnnotated;
	}

	/**
	 * Get the id value of the paragraph.
	 * 
	 * @param pStartTag
	 *            The StartElement of the paragraph.
	 * @return the id value if found, null else.
	 */
	public String getIdFromAttributes() {
		final Attribute attr = startTag.getAttributeByName(paragraphIdQName);
		return attr == null ? null : attr.getValue();
	}

	/**
	 * {@inheritDoc} Used in Description to get the paragraph corresponding to
	 * the pointer.
	 */
	@Override
	public boolean equals(final Object pParagraph) {
		final int start = ((Paragraph) pParagraph).getPositionStart();
		final int end = ((Paragraph) pParagraph).getPositionEnd();
		if (descriptionPointer >= start && descriptionPointer <= end)
			return true;
		else
			return false;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		return new HashCodeBuilder().append(id).toHashCode();
	}

}
