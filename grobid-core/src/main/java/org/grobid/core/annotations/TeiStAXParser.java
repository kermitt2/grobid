package org.grobid.core.annotations;

import static org.grobid.core.utilities.TeiValues.TAG_DIV;
import static org.grobid.core.utilities.TeiValues.TAG_NOTES_STMT;
import static org.grobid.core.utilities.TeiValues.TAG_TEI;
import static org.grobid.core.utilities.TextUtilities.AND;
import static org.grobid.core.utilities.TextUtilities.DOUBLE_QUOTE;
import static org.grobid.core.utilities.TextUtilities.ESC_AND;
import static org.grobid.core.utilities.TextUtilities.GREATER_THAN;
import static org.grobid.core.utilities.TextUtilities.LESS_THAN;
import static org.grobid.core.utilities.TextUtilities.NEW_LINE;
import static org.grobid.core.utilities.TextUtilities.QUOTE;
import static org.grobid.core.utilities.TextUtilities.SLASH;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Characters;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import org.grobid.core.data.BibDataSet;
import org.grobid.core.data.PatentItem;
import org.grobid.core.engines.EngineParsers;
import org.grobid.core.engines.patent.ReferenceExtractor;
import org.grobid.core.exceptions.GrobidException;
import org.grobid.core.utilities.counters.GrobidTimer;
import org.grobid.core.utilities.XMLWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * StAX parser which parses an input tei, and inserts the Citations of patents
 * and NPL of description ({@code <div type="description">...</div>}) if found
 * in that document.
 * 
 * @author Damien
 */
public class TeiStAXParser {

	/**
	 * The class LOGGER.
	 */
	private static final Logger LOGGER = LoggerFactory.getLogger(TeiStAXParser.class);

	/**
	 * unchecked.
	 */
	private static final String UNCHECKED = "unchecked";

	/**
	 * The end tags of notesStmt: </notesStmt>.
	 */
	private static final String TAG_END_NOTES_STMT = LESS_THAN + SLASH + TAG_NOTES_STMT + GREATER_THAN;

	/**
	 * If the logger is in debug mode.
	 */
	protected static final boolean isDebug = LOGGER.isDebugEnabled();

	/**
	 * Timers.
	 */
	protected GrobidTimer gbdNoExtractTimer, gbdFullTimer;

	/**
	 * The current TEIParsedInfo.
	 */
	protected TeiStAXParsedInfo currTEIParsedInfo;

	/**
	 * The class reference extractor.
	 */
	protected final ReferenceExtractor extractor;

	/**
	 * If ReferenceExtractor is instantiated by TeiStAXParser or passed as an
	 * argument.
	 */
	protected boolean isSelfInstanceRefExtractor;

	/**
	 * The input Stream to process.
	 */
	protected final InputStream inputStream;

	/**
	 * Output file used to append annotated publications.
	 */
	protected OutputStream outputStream;

	/**
	 * Temporary buffer used to append data inside TEI tag.
	 */
	protected StringWriter teiBuffer;

	/**
	 * The {@link XMLEventReader}.
	 */
	protected XMLEventReader reader;

	/**
	 * Accumulates the header annotations.
	 */
	protected StringBuffer headerAnnotation;

	/**
	 * If the output has to be indented.
	 */
	protected boolean isIndented;

	/**
	 * Indicate if the citation should be consolidated with an external call to bibliographical databases
     */
	protected boolean consolidate = false;
	
	/**
	 * Constructor.
	 * 
	 * @param pInputStream
	 *            The input file stream.
	 * 
	 * @param pOutputStream
	 *            The output stream
	 */



	public TeiStAXParser(final InputStream pInputStream, OutputStream pOutputStream, boolean consolidate) {
		this(pInputStream, pOutputStream, true, new ReferenceExtractor(new EngineParsers()), consolidate);
		isSelfInstanceRefExtractor = true;
	}

	/**
	 * Constructor.
	 * 
	 * @param pOutputStream
	 *            The output stream
	 * @param pIsIndented
	 *            If the output has to be indented.
	 */
	public TeiStAXParser(final InputStream pInputStream, OutputStream pOutputStream, 
				final boolean pIsIndented, final boolean consolidate) {
		this(pInputStream, pOutputStream, pIsIndented, new ReferenceExtractor(new EngineParsers()), consolidate);
		isSelfInstanceRefExtractor = true;
	}

	/**
	 * Constructor.
	 * 
	 * @param pOutputStream
	 *            The output stream
	 * @param pIsIndented
	 *            If the output has to be indented.
	 * @param pExtractor
	 *            The ReferenceExtractor object used to extract the citations.
	 */
	public TeiStAXParser(final InputStream pInputStream, OutputStream pOutputStream, final boolean pIsIndented,
			final ReferenceExtractor pExtractor, final boolean consolidate) {
		initTimers();

		extractor = pExtractor;
		isSelfInstanceRefExtractor = false;
		isIndented = pIsIndented;
		currTEIParsedInfo = new TeiStAXParsedInfo(isIndented);
		inputStream = pInputStream;
		outputStream = pOutputStream;
		teiBuffer = new StringWriter();
		headerAnnotation = new StringBuffer();
		this.consolidate = consolidate;
		
		initReader();
	}

	/**
	 * Parse and annotate this input file.
	 * 
	 * @return OutputStream containing the tei file annotated.
	 * @throws XMLStreamException
	 * @throws IOException
	 */
	public void parse() throws XMLStreamException, IOException {
		int eventType;
		XMLEvent event;
		while (reader.hasNext()) {
			event = (XMLEvent) reader.next();
			eventType = event.getEventType();

			switch (eventType) {
			case XMLEvent.START_ELEMENT:
				processStartElement(event);
				break;

			case XMLEvent.END_ELEMENT:
				processEndElement(event);
				break;
			case XMLEvent.CHARACTERS:
				writeInTeiBufferCharacters(event.asCharacters());
				break;
			}
		}

		appendOutputStream();
		if (isSelfInstanceRefExtractor) {
			extractor.close();
		}

		logTimeProcessing();

	}

	/**
	 * Initialize the parser.
	 */
	protected void initReader() {
		// Init StAX reader and writer
		try {
			final XMLInputFactory inputFactoy = XMLInputFactory.newInstance();
			inputFactoy.setProperty(XMLInputFactory.IS_REPLACING_ENTITY_REFERENCES, false);
			inputFactoy.setProperty(XMLInputFactory.IS_NAMESPACE_AWARE, false);
			reader = inputFactoy.createXMLEventReader(inputStream);
		} catch (final XMLStreamException xmlStrExp) {
			throw new GrobidException("An error occured while creating the Stax event reader: " + xmlStrExp);
		} catch (final FactoryConfigurationError factCongExp) {
			throw new GrobidException("An error occured while creating the Stax event reader: " + factCongExp);
		}
	}

	/**
	 * Append {@code pStart} to {@link #teiBuffer}.
	 * 
	 * @param pStart
	 *            The {@link StartElement} to append.
	 */
	protected void writeInTeiBufferStart(final StartElement pStart) {
		teiBuffer.append(pStart.toString().replaceAll(QUOTE, DOUBLE_QUOTE));
	}

	/**
	 * Append {@code pChars} to {@link #teiBuffer}.
	 * 
	 * @param pChars
	 *            The {@link Characters} to append.
	 */
	protected void writeInTeiBufferCharacters(final Characters pChars) {
		teiBuffer.append(pChars.getData().replaceAll(AND, ESC_AND));
	}

	/**
	 * Append {@code pEnd} to {@link #teiBuffer}.
	 * 
	 * @param pEnd
	 *            The {@link EndElement} to append.
	 */
	protected void writeInTeiBufferEnd(final EndElement pEnd) {
		teiBuffer.append(pEnd.toString());
	}

	/**
	 * Append in {@link #teiBuffer} {@link @pTei}.
	 * 
	 * @param pTei
	 *            The Sting to append.
	 */
	protected void writeInTeiBufferRaw(final String pTei) {
		teiBuffer.append(pTei);
	}

	/**
	 * Process start tag.
	 * 
	 * @param pEvent
	 *            The current event.
	 * 
	 * @throws IOException
	 * @throws XMLStreamException
	 */
	protected void processStartElement(final XMLEvent pEvent) throws XMLStreamException, IOException {
		final StartElement startTag = pEvent.asStartElement();
		final String tagName = startTag.getName().getLocalPart();

		currTEIParsedInfo.incrementGornIndex();
		if (TAG_DIV.equals(tagName) && currTEIParsedInfo.checkIfDescription(startTag)) {
			processDescription();
			currTEIParsedInfo.resetDescription();
		} else {
			writeInTeiBufferStart(pEvent.asStartElement());
		}
	}

	/**
	 * Process end tag.
	 * 
	 * @param pEvent
	 *            The current event.
	 * 
	 * @throws XMLStreamException
	 * @throws IOException
	 */
	protected void processEndElement(final XMLEvent pEvent) throws XMLStreamException, IOException {
		currTEIParsedInfo.decrementGornIndex();
		final String endTag = pEvent.asEndElement().getName().getLocalPart();

		if (TAG_TEI.equals(endTag)) {
			writeInTeiBufferEnd(pEvent.asEndElement());
			appendOutputStream(headerAnnotation.toString());
			appendOutputStream(TAG_END_NOTES_STMT);
			appendOutputStream();
			reinit();
		} else if (TAG_NOTES_STMT.equals(endTag)) {
			appendOutputStream();
		} else {
			writeInTeiBufferEnd(pEvent.asEndElement());
		}
	}

	/**
	 * Process the description. This method is called when the parser reached
	 * the element <div type="description" ...>. It will parse the whole
	 * description, extract the citations and add the header is found.
	 * 
	 * @throws XMLStreamException
	 */
	@SuppressWarnings(UNCHECKED)
	protected void processDescription() throws XMLStreamException {
		StartElement startElement;
		String chars;
		String endTag;
		XMLEvent event;
		boolean isP = false;
		final XMLWriter tagsInsideP = new XMLWriter();
		while (reader.hasNext()) {
			event = (XMLEvent) reader.next();

			if (XMLEvent.START_ELEMENT == event.getEventType()) {
				startElement = event.asStartElement();
				currTEIParsedInfo.incrementGornIndex();
				if (!isP) {
					isP = currTEIParsedInfo.processParagraphStartTag(startElement);
				} else {
					tagsInsideP.addStartElement(startElement.getName().getLocalPart(), startElement.getAttributes());
				}
			} else if (isP && XMLEvent.CHARACTERS == event.getEventType()) {
				chars = event.asCharacters().getData();
				if (tagsInsideP.isEmpty()) {
					currTEIParsedInfo.appendDescriptionContent(chars);
				} else {
					tagsInsideP.addCharacters(chars);
				}
			} else if (XMLEvent.END_ELEMENT == event.getEventType()) {
				currTEIParsedInfo.decrementGornIndex();
				endTag = event.asEndElement().getName().getLocalPart();
				if (TAG_DIV.equals(endTag)) {
					processExtraction();
					if (isIndented) {
						writeInTeiBufferRaw(NEW_LINE);
					}
					writeInTeiBufferRaw(currTEIParsedInfo.getDescription().toTei());
					return;
				} else if (isP) {
					if (currTEIParsedInfo.processParagraphEndTag(endTag)) {
						isP = false;
					} else {
						tagsInsideP.addEndElement(endTag);
						currTEIParsedInfo.getDescription().appendRawContent(tagsInsideP.toString());
						tagsInsideP.resetWriter();
					}
				}
			}
		}
	}

	/**
	 * Process the extraction of the current publication.
	 */
	protected void processExtraction() {
		final List<PatentItem> patents = new ArrayList<PatentItem>();
		final List<BibDataSet> articles = new ArrayList<BibDataSet>();
		pauseTimer(gbdNoExtractTimer);
		extractor.extractAllReferencesString(currTEIParsedInfo.getDescription().toRawString(), true, consolidate, patents, articles);
		restartTimer(gbdNoExtractTimer);
		final Annotation annotation = new Annotation(patents, articles, currTEIParsedInfo.getDescription());
		headerAnnotation.append(annotation.getHeaderAnnotation(isIndented));
	}

	/**
	 * Reinitialize some objects to use them for the next elements to parse.
	 */
	protected void reinit() {
		headerAnnotation = new StringBuffer();
	}

	/**
	 * Append {@code teiBuffer} to the output stream.
	 */
	protected void appendOutputStream() {
		try {
			outputStream.write(teiBuffer.toString().getBytes());
			teiBuffer = new StringWriter();
		} catch (final IOException ioExp) {
			throw new GrobidException(ioExp);
		}
	}

	/**
	 * Append {@code pContent} to the output stream.
	 * 
	 * @param pContent
	 *            The content to append.
	 */
	protected void appendOutputStream(final String pContent) {
		try {
			outputStream.write(pContent.getBytes());
		} catch (final IOException ioExp) {
			throw new GrobidException(ioExp);
		}
	}

	/**
	 * Initialize the timers if in the logger is in DEBUG mode.
	 */
	private void initTimers() {
		if (isDebug) {
			gbdNoExtractTimer = new GrobidTimer(true);
			gbdFullTimer = new GrobidTimer(true);
		}
	}

	/**
	 * Pause the {@code pGbdTimer} if the logger is in DEBUG mode.
	 * 
	 * @param pGbdTimer
	 *            The timer to pause.
	 */
	private void pauseTimer(final GrobidTimer pGbdTimer) {
		if (isDebug) {
			pGbdTimer.pauseTimer();
		}
	}

	/**
	 * Restart the {@code pGbdTimer} if the logger is in DEBUG mode.
	 * 
	 * @param pGbdTimer
	 *            The timer to restart.
	 */
	private void restartTimer(final GrobidTimer pGbdTimer) {
		if (isDebug) {
			pGbdTimer.restartTimer();
		}
	}

	/**
	 * Stop the {@code pGbdTimer} if the logger is in DEBUG mode.
	 * 
	 * @param pGbdTimer
	 *            The timer to stop.
	 */
	private void stopTimer(final GrobidTimer pGbdTimer) {
		if (isDebug) {
			pGbdTimer.stop(GrobidTimer.STOP);
		}
	}

	/**
	 * Log the processing time if the logger is in DEBUG mode.
	 */
	private void logTimeProcessing() {
		if (isDebug) {
			stopTimer(gbdFullTimer);
			stopTimer(gbdNoExtractTimer);

			double noRefExtractTime = gbdNoExtractTimer.getElapsedTimeFromStart(GrobidTimer.STOP);
			double fullTime = gbdFullTimer.getElapsedTimeFromStart(GrobidTimer.STOP);
			double percent = noRefExtractTime / fullTime * 100;

			LOGGER.debug("TeiStAXParser processing time without ReferenceExtractor processing time: " + noRefExtractTime + "  (" + percent
					+ "% of total computing time)");
			LOGGER.debug("TeiStAXParser full processing time : " + fullTime);

			System.out.println("TeiStAXParser processing time without ReferenceExtractor processing time: " + noRefExtractTime + "  ("
					+ percent + "% of total computing time)");
			System.out.println("TeiStAXParser full processing time : " + fullTime);
		}
	}

}
