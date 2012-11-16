package org.grobid.core.sax;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

import org.apache.commons.lang3.StringUtils;
import org.grobid.core.exceptions.GrobidException;
import org.grobid.core.utilities.TeiValues;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class TEISaxInsert extends DefaultHandler {

	/**
	 * The document number of the looked ofr publication.
	 */
	private String publicationDocNumber;

	/**
	 * The current TEIParsedInfo.
	 */
	private TEIParsedInfo currTEIParsedInfo;

	/**
	 * The working file.
	 */
	private RandomAccessFile raFile;

	/**
	 * The current TEIParsedInfo.
	 */
	private TEIParsedInfo currFileTEIParsedInfo;

	/**
	 * Pointer to the position in the file.
	 */
	private long pointer;

	/**
	 * Constructor.
	 */
	public TEISaxInsert(File pFile, String pPublicationDocNumber) {
		if (StringUtils.isBlank(pPublicationDocNumber)) {
			throw new GrobidException(
					"the document number of the publication is null or empty");
		} else {
			publicationDocNumber = pPublicationDocNumber;
		}
		currTEIParsedInfo = new TEIParsedInfo();

		pointer = 0;
		try {
			raFile = new RandomAccessFile(pFile, "rw");
		} catch (FileNotFoundException fnfExp) {
			throw new GrobidException("The file " + pFile.getAbsolutePath()
					+ " was not found.");
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void characters(char[] buffer, int start, int length)
			throws SAXException {
		if (currTEIParsedInfo.isCurrTagDocumber()
				&& publicationDocNumber
						.equals(new String(buffer, start, length))) {
			currTEIParsedInfo.setPubDocNumber(publicationDocNumber);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void startElement(String uri, String localName, String qName,
			Attributes attributes) throws SAXException {
		currTEIParsedInfo.appendTag2XmlPath(qName);
		currTEIParsedInfo.setCurrTagAttrs(attributes);

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void endElement(String uri, String localName, String qName)
			throws SAXException {

		if (currTEIParsedInfo.getPubDocNumber() != null
				&& TeiValues.TAG_NOTES_STMT.equalsIgnoreCase(qName)) {
			String line;
			try {
				while ((line = raFile.readLine()) != null) {
					//if()
				}
			} catch (IOException ioExp) {
				throw new GrobidException("Error while reading the file.");
			}
		}

		if (qName.equalsIgnoreCase(currTEIParsedInfo.getLastXmlPath())) {
			System.out.println(currTEIParsedInfo.getXmlPath().toString());
			currTEIParsedInfo.getXmlPath().remove(
					currTEIParsedInfo.getXmlPath().size() - 1);
		}
	}

}
