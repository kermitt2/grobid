package org.grobid.core.engines;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.concurrent.TimeoutException;

import org.grobid.core.GrobidModels;
import org.grobid.core.data.BiblioItem;
import org.grobid.core.data.Date;
import org.grobid.core.data.Person;
import org.grobid.core.document.Document;
import org.grobid.core.document.TEIFormater;
import org.grobid.core.exceptions.GrobidException;
import org.grobid.core.lang.Language;
import org.grobid.core.utilities.Consolidation;
import org.grobid.core.utilities.GrobidProperties;
import org.grobid.core.utilities.LanguageUtilities;
import org.grobid.core.utilities.TextUtilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Patrice Lopez
 */
public class HeaderParser extends AbstractParser {
	private static final Logger LOGGER = LoggerFactory.getLogger(HeaderParser.class);

	private LanguageUtilities languageUtilities = LanguageUtilities.getInstance();

	private AuthorParser authorParser = null;
	private DateParser dateParser = null;
	private AffiliationAddressParser affiliationAddressParser = null;
	private CitationParser citationParser = null;
	private Consolidation consolidator = null;
	private Document doc = null;

	private File tmpPath = null;
	private String pathXML = null;

	public HeaderParser() {
		super(GrobidModels.HEADER);
		GrobidProperties.getInstance();
		tmpPath = GrobidProperties.getTempPath();
	}

	public String processing(String input, boolean consolidate, BiblioItem resHeader, int startPage, int endPage) throws TimeoutException {
		doc = new Document(input, tmpPath.getAbsolutePath());
		try {
			// int startPage = 0;
			// //int endPage = 1;
			// int endPage = 2;
			pathXML = doc.pdf2xml(true, false, startPage, endPage, input, tmpPath.getAbsolutePath(), false); // with
																												// timeout,
			// no force pdf reloading
			// input is the pdf file, tmpPath is the tmp directory for the lxml
			// file,
			// path is the resource path
			// and we do not extract images in the PDF file
			if (pathXML == null) {
				throw new GrobidException("PDF parsing fails");
			}
			doc.setPathXML(pathXML);
			doc.addFeaturesDocument();

			if (doc.getBlocks() == null) {
				throw new GrobidException("PDF parsing resulted in empty content");
			}

			return processingHeaderBlock(consolidate, doc, resHeader);
		} catch (TimeoutException timeoutExp) {
			throw new TimeoutException("A time out occured");
		} catch (final Exception exp) {
			throw new GrobidException("An exception occurred while running Grobid on file " + tmpPath.getAbsolutePath() + ": " + exp);
		} finally {
			doc.cleanLxmlFile(pathXML, true);
		}
	}

	public String processingHeaderBlock(boolean consolidate, Document doc, BiblioItem resHeader) {
		try {
			String header;
			if (doc.blockDocumentHeaders == null) {
				header = doc.getHeaderFeatured(true, true, true);
			} else {
				header = doc.getHeaderFeatured(true, false, true);
			}
			ArrayList<String> tokenizations = doc.getTokenizationsHeader();

			StringTokenizer st = new StringTokenizer(header, "\n");

			feedTaggerAndParse(st);

			StringBuilder res = new StringBuilder();
			for (int i = 0; i < tagger.size(); i++) {
				for (int j = 0; j < tagger.xsize(); j++) {
					res.append(tagger.x(i, j)).append("\t");
				}
				res.append(tagger.y2(i));
				res.append("\n");
			}

			resHeader = resultExtraction(res.toString(), true, tokenizations, resHeader);

			// LanguageUtilities languageUtilities =
			// LanguageUtilities.getInstance();
			Language langu = languageUtilities.runLanguageId(resHeader.getTitle() + "\n" + resHeader.getKeywords() + "\n"
					+ resHeader.getAbstract());
			if (langu != null) {
				String lang = langu.getLangId();
				doc.setLanguage(lang);
				resHeader.setLanguage(lang);
			}

			if (resHeader != null) {
				if (resHeader.getAbstract() != null) {
					// resHeader.setAbstract(utilities.dehyphenizeHard(resHeader.getAbstract()));
					resHeader.setAbstract(TextUtilities.dehyphenize(resHeader.getAbstract()));
				}
				BiblioItem.cleanTitles(resHeader);
				if (resHeader.getTitle() != null) {
					// String temp =
					// utilities.dehyphenizeHard(resHeader.getTitle());
					String temp = TextUtilities.dehyphenize(resHeader.getTitle());
					temp = temp.trim();
					if (temp.length() > 1) {
						if (temp.startsWith("1"))
							temp = temp.substring(1, temp.length());
						temp = temp.trim();
					}
					resHeader.setTitle(temp);
				}
				if (resHeader.getBookTitle() != null) {
					resHeader.setBookTitle(TextUtilities.dehyphenize(resHeader.getBookTitle()));
				}

				resHeader.setOriginalAuthors(resHeader.getAuthors());
				boolean fragmentedAuthors = false;
				boolean hasMarker = false;
				List<Integer> authorsBlocks = new ArrayList<Integer>();
				String[] authorSegments = null;
				if (resHeader.getAuthors() != null) {
					ArrayList<String> auts;
					authorSegments = resHeader.getAuthors().split("\n");
					if (authorSegments.length > 1) {
						fragmentedAuthors = true;
					}
					if (authorParser == null) {
						authorParser = new AuthorParser();
					}
					for (int k = 0; k < authorSegments.length; k++) {
						auts = new ArrayList<String>();
						auts.add(authorSegments[k]);
						List<Person> localAuthors = authorParser.processingHeader(auts);
						if (localAuthors != null) {
							for (Person pers : localAuthors) {
								resHeader.addFullAuthor(pers);
								if (pers.getMarkers() != null) {
									hasMarker = true;
								}
								authorsBlocks.add(new Integer(k));
							}
						}
					}
				}

				if (affiliationAddressParser == null) {
					affiliationAddressParser = new AffiliationAddressParser();
				}
				resHeader.setFullAffiliations(affiliationAddressParser.processReflow(res.toString(), tokenizations));
				resHeader.attachEmails();
				boolean attached = false;
				if (fragmentedAuthors && !hasMarker) {
					if (resHeader.getFullAffiliations() != null) {
						if (authorSegments != null) {
							if (resHeader.getFullAffiliations().size() == authorSegments.length) {
								int k = 0;
								for (Person pers : resHeader.getFullAuthors()) {
									if (k < authorsBlocks.size()) {
										int indd = authorsBlocks.get(k).intValue();
										if (indd < resHeader.getFullAffiliations().size()) {
											pers.addAffiliation(resHeader.getFullAffiliations().get(indd));
										}
									}
									k++;
								}
								attached = true;
								resHeader.setFullAffiliations(null);
								resHeader.setAffiliation(null);
							}
						}
					}
				}
				if (!attached) {
					resHeader.attachAffiliations();
				}

				if (resHeader.getEditors() != null) {
					ArrayList<String> edits = new ArrayList<String>();
					edits.add(resHeader.getEditors());

					if (authorParser == null) {
						authorParser = new AuthorParser();
					}
					resHeader.setFullEditors(authorParser.processingHeader(edits));
					// resHeader.setFullEditors(authorParser.processingCitation(edits));
				}

				if (resHeader.getReference() != null) {
					if (citationParser == null) {
						citationParser = new CitationParser();
					}
					BiblioItem refer = citationParser.processing(resHeader.getReference(), false);
					BiblioItem.correct(resHeader, refer);
				}
			}

			// DOI pass
			ArrayList<String> dois = doc.getDOIMatches();
			if (dois != null) {
				if ((dois.size() == 1) && (resHeader != null)) {
					resHeader.setDOI(dois.get(0));
				}
			}

			if (consolidate) {
				resHeader = consolidateHeader(resHeader);
			}

			// normalization of dates
			if (resHeader != null) {
				if (resHeader.getPublicationDate() != null) {
					if (dateParser == null) {
						dateParser = new DateParser();
					}
					ArrayList<Date> dates = dateParser.processing(resHeader.getPublicationDate());
					// most basic heuristic, we take the first date - to be
					// revised...
					if (dates != null) {
						if (dates.size() > 0) {
							resHeader.setNormalizedPublicationDate(dates.get(0));
						}
					}
				}

				if (resHeader.getSubmissionDate() != null) {
					if (dateParser == null) {
						dateParser = new DateParser();
					}
					ArrayList<Date> dates = dateParser.processing(resHeader.getSubmissionDate());
					if (dates != null) {
						if (dates.size() > 0) {
							resHeader.setNormalizedSubmissionDate(dates.get(0));
						}
					}
				}
			}

			TEIFormater teiFormater = new TEIFormater(doc);
			String tei = teiFormater.toTEIBody(resHeader, null, true, false, true);
			LOGGER.debug(tei);
			return tei;
		} catch (Exception e) {
			throw new GrobidException("An exception occured while running Grobid.", e);
		}
	}

	/**
	 * Return the Document object of the last processed pdf file.
	 * 
	 * @return a document
	 */
	public Document getDoc() {
		return doc;
	}

	/**
	 * Process the header of the specified pdf and format the result as training
	 * data.
	 * 
	 * @param inputFile
	 *            path to input file
	 * @param pathHeader
	 *            path to header
	 * @param pathTEI
	 *            path to TEI
	 */
	public void createTrainingHeader(String inputFile, String pathHeader, String pathTEI) {
		doc = new Document(inputFile, tmpPath.getAbsolutePath());
		try {
			int startPage = 0;
			// int endPage = 1;
			int endPage = 2;
			File file = new File(inputFile);
			String PDFFileName = file.getName();
			pathXML = doc.pdf2xml(true, false, startPage, endPage, inputFile, tmpPath.getAbsolutePath(), false); // with
																													// timeout,
			// no force pdf reloading
			// pathPDF is the pdf file, tmpPath is the tmp directory for the
			// lxml file,
			// path is the resource path
			// and we do not extract images in the PDF file
			if (pathXML == null) {
				throw new GrobidException("PDF parsing fails");
			}
			doc.setPathXML(pathXML);
			doc.addFeaturesDocument();

			if (doc.getBlocks() == null) {
				throw new GrobidException("PDF parsing resulted in empty content");
			}

			String header = doc.getHeaderFeatured(true, true, true);
			ArrayList<String> tokenizations = doc.getTokenizationsHeader();

			// we write the header untagged
			String outPathHeader = pathHeader + "/" + PDFFileName.replace(".pdf", ".header");
			Writer writer = new OutputStreamWriter(new FileOutputStream(new File(outPathHeader), false), "UTF-8");
			writer.write(header + "\n");
			writer.close();

			// clear internal context
			tagger.clear();

			// add context
			StringTokenizer st = new StringTokenizer(header, "\n");
			while (st.hasMoreTokens()) {
				String piece = st.nextToken();
				tagger.add(piece);
				tagger.add("\n");
			}

			// parse and change internal stated as 'parsed'
			if (!tagger.parse()) {
				// throw an exception
				throw new GrobidException("CRF++ parsing failed.");
			}

			StringBuilder res = new StringBuilder();
			for (int i = 0; i < tagger.size(); i++) {
				for (int j = 0; j < tagger.xsize(); j++) {
					res.append(tagger.x(i, j)).append("\t");
				}
				res.append(tagger.y2(i));
				res.append("\n");
			}

			// buffer for the header block
			String rese = res.toString();
			StringBuilder bufferHeader = trainingExtraction(rese, true, tokenizations);

			// LanguageUtilities languageUtilities =
			// LanguageUtilities.getInstance();
			Language lang = languageUtilities.runLanguageId(doc.getBody());

			if (lang != null) {
				doc.setLanguage(lang.getLangId());
			}

			// buffer for the affiliation+address block
			if (affiliationAddressParser == null) {
				affiliationAddressParser = new AffiliationAddressParser();
			}
			StringBuffer bufferAffiliation = affiliationAddressParser.trainingExtraction(rese, tokenizations);
			// buffer for the date block
			StringBuffer bufferDate = null;
			// we need to rebuild the found date string as it appears
			String input = "";
			int q = 0;
			st = new StringTokenizer(rese, "\n");
			while (st.hasMoreTokens() && (q < tokenizations.size())) {
				String line = st.nextToken();
				String theTotalTok = tokenizations.get(q);
				String theTok = tokenizations.get(q);
				while (theTok.equals(" ") || theTok.equals("\t") || theTok.equals("\n")) {
					q++;
					theTok = tokenizations.get(q);
					theTotalTok += theTok;
				}
				if (line.endsWith("<date>")) {
					input += theTotalTok;
				}
				q++;
			}
			if (input.trim().length() > 1) {
				ArrayList<String> inputs = new ArrayList<String>();
				inputs.add(input.trim());
				if (dateParser == null) {
					dateParser = new DateParser();
				}
				bufferDate = dateParser.trainingExtraction(inputs);
			}

			// buffer for the name block
			StringBuffer bufferName = null;
			// we need to rebuild the found author string as it appears
			input = "";
			q = 0;
			st = new StringTokenizer(rese, "\n");
			while (st.hasMoreTokens() && (q < tokenizations.size())) {
				String line = st.nextToken();
				String theTotalTok = tokenizations.get(q);
				String theTok = tokenizations.get(q);
				while (theTok.equals(" ") || theTok.equals("\t") || theTok.equals("\n")) {
					q++;
					theTok = tokenizations.get(q);
					theTotalTok += theTok;
				}
				if (line.endsWith("<author>")) {
					input += theTotalTok;
				}
				q++;
			}
			if (input.length() > 1) {
				ArrayList<String> inputs = new ArrayList<String>();
				inputs.add(input.trim());
				if (authorParser == null) {
					authorParser = new AuthorParser();
				}
				bufferName = authorParser.trainingExtraction(inputs, true);
			}

			// buffer for the reference block
			StringBuilder bufferReference = null;
			// we need to rebuild the found citation string as it appears
			input = "";
			q = 0;
			st = new StringTokenizer(rese, "\n");
			while (st.hasMoreTokens() && (q < tokenizations.size())) {
				String line = st.nextToken();
				String theTotalTok = tokenizations.get(q);
				String theTok = tokenizations.get(q);
				while (theTok.equals(" ") || theTok.equals("\t") || theTok.equals("\n")) {
					q++;
					theTok = tokenizations.get(q);
					theTotalTok += theTok;
				}
				if (line.endsWith("<reference>")) {
					input += theTotalTok;
				}
				q++;
			}
			if (input.length() > 1) {
				ArrayList<String> inputs = new ArrayList<String>();
				inputs.add(input.trim());
				if (citationParser == null) {
					citationParser = new CitationParser();
				}
				bufferReference = citationParser.trainingExtraction(inputs);
			}

			// write the TEI file to reflect the extract layout of the text as
			// extracted from the pdf
			writer = new OutputStreamWriter(new FileOutputStream(new File(pathTEI + "/"
					+ PDFFileName.replace(".pdf", GrobidProperties.FILE_ENDING_TEI_HEADER)), false), "UTF-8");
			writer.write("<?xml version=\"1.0\" ?>\n<tei>\n\t<teiHeader>\n\t\t<fileDesc xml:id=\"" + PDFFileName.replace(".pdf", "")
					+ "\"/>\n\t</teiHeader>\n\t<text");

			if (lang != null) {
				// TODO: why English (Slava)
				writer.write(" xml:lang=\"en\"");
			}
			writer.write(">\n\t\t<front>\n");

			writer.write(bufferHeader.toString());
			writer.write("\n\t\t</front>\n\t</text>\n</tei>\n");
			writer.close();

			if (bufferAffiliation != null) {
				if (bufferAffiliation.length() > 0) {
					Writer writerAffiliation = new OutputStreamWriter(new FileOutputStream(new File(pathTEI + "/"
							+ PDFFileName.replace(".pdf", ".affiliation.tei.xml")), false), "UTF-8");
					writerAffiliation.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
					writerAffiliation.write("\n<tei xmlns=\"http://www.tei-c.org/ns/1.0\""
							+ " xmlns:xlink=\"http://www.w3.org/1999/xlink\" " + "xmlns:mml=\"http://www.w3.org/1998/Math/MathML\">");
					writerAffiliation.write("\n\t<teiHeader>\n\t\t<fileDesc>\n\t\t\t<sourceDesc>");
					writerAffiliation.write("\n\t\t\t\t<biblStruct>\n\t\t\t\t\t<analytic>\n\t\t\t\t\t\t<author>\n\n");

					writerAffiliation.write(bufferAffiliation.toString());

					writerAffiliation.write("\n\t\t\t\t\t\t</author>\n\t\t\t\t\t</analytic>");
					writerAffiliation.write("\n\t\t\t\t</biblStruct>\n\t\t\t</sourceDesc>\n\t\t</fileDesc>");
					writerAffiliation.write("\n\t</teiHeader>\n</tei>\n");
					writerAffiliation.close();
				}
			}

			if (bufferDate != null) {
				if (bufferDate.length() > 0) {
					Writer writerDate = new OutputStreamWriter(new FileOutputStream(new File(pathTEI + "/"
							+ PDFFileName.replace(".pdf", ".date.xml")), false), "UTF-8");
					writerDate.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
					writerDate.write("<dates>\n");

					writerDate.write(bufferDate.toString());

					writerDate.write("</dates>\n");
					writerDate.close();
				}
			}

			if (bufferName != null) {
				if (bufferName.length() > 0) {
					Writer writerName = new OutputStreamWriter(new FileOutputStream(new File(pathTEI + "/"
							+ PDFFileName.replace(".pdf", ".authors.tei.xml")), false), "UTF-8");
					writerName.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
					writerName.write("\n<tei xmlns=\"http://www.tei-c.org/ns/1.0\"" + " xmlns:xlink=\"http://www.w3.org/1999/xlink\" "
							+ "xmlns:mml=\"http://www.w3.org/1998/Math/MathML\">");
					writerName.write("\n\t<teiHeader>\n\t\t<fileDesc>\n\t\t\t<sourceDesc>");
					writerName.write("\n\t\t\t\t<biblStruct>\n\t\t\t\t\t<analytic>\n\n\t\t\t\t\t\t<author>");
					writerName.write("\n\t\t\t\t\t\t\t<persName>\n");

					writerName.write(bufferName.toString());

					writerName.write("\t\t\t\t\t\t\t</persName>\n");
					writerName.write("\t\t\t\t\t\t</author>\n\n\t\t\t\t\t</analytic>");
					writerName.write("\n\t\t\t\t</biblStruct>\n\t\t\t</sourceDesc>\n\t\t</fileDesc>");
					writerName.write("\n\t</teiHeader>\n</tei>\n");
					writerName.close();
				}
			}

			if (bufferReference != null) {
				if (bufferReference.length() > 0) {
					Writer writerReference = new OutputStreamWriter(new FileOutputStream(new File(pathTEI + "/"
							+ PDFFileName.replace(".pdf", ".header-reference.xml")), false), "UTF-8");
					writerReference.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
					writerReference.write("<citations>\n");

					writerReference.write(bufferReference.toString());

					writerReference.write("</citations>\n");
					writerReference.close();
				}
			}
		} catch (Exception e) {
			throw new GrobidException("An exception occurred while running Grobid.", e);
		} finally {
			doc.cleanLxmlFile(pathXML, true);
		}
	}

	/**
	 * Extract results from a labelled header. If boolean intro is true, the
	 * extraction is stopped at the first "intro" tag identified (this tag marks
	 * the begining of the description).
	 * 
	 * @param result
	 *            result
	 * @param intro
	 *            if intro
	 * @param tokenizations
	 *            list of tokens
	 * @param biblio
	 *            biblio item
	 * @return a biblio item
	 */
	public BiblioItem resultExtraction(String result, boolean intro, ArrayList<String> tokenizations, BiblioItem biblio) {
		StringTokenizer st = new StringTokenizer(result, "\n");
		String s1 = null;
		String s2 = null;
		String lastTag = null;

		int p = 0;

		while (st.hasMoreTokens()) {
			boolean addSpace = false;
			String tok = st.nextToken().trim();

			if (tok.length() == 0) {
				continue;
			}
			StringTokenizer stt = new StringTokenizer(tok, "\t");
			ArrayList<String> localFeatures = new ArrayList<String>();
			int i = 0;

			// boolean newLine = false;
			int ll = stt.countTokens();
			while (stt.hasMoreTokens()) {
				String s = stt.nextToken().trim();
				if (i == 0) {
					// s2 = TextUtilities.HTMLEncode(s);
					s2 = s;

					boolean strop = false;
					while ((!strop) && (p < tokenizations.size())) {
						String tokOriginal = tokenizations.get(p);
						if (tokOriginal.equals(" ")) {
							addSpace = true;
						} else if (tokOriginal.equals(s)) {
							strop = true;
						}
						p++;
					}
				} else if (i == ll - 1) {
					s1 = s;
				} else {
					// if (s.equals("LINESTART"))
					// newLine = true;
					localFeatures.add(s);
				}
				i++;
			}

			if ((s1.equals("<title>")) || (s1.equals("I-<title>"))) {
				if (biblio.getTitle() != null) {
					if (localFeatures.contains("LINESTART")) {
						biblio.setTitle(biblio.getTitle() + " " + s2);
					} else if (addSpace) {
						biblio.setTitle(biblio.getTitle() + " " + s2);
					} else
						biblio.setTitle(biblio.getTitle() + s2);
				} else
					biblio.setTitle(s2);
			} else if ((s1.equals("<author>")) || (s1.equals("I-<author>"))) {
				if ((lastTag == null) || ((lastTag != null) && (lastTag.endsWith("<author>")))) {
					if (biblio.getAuthors() != null) {
						if (addSpace) {
							biblio.setAuthors(biblio.getAuthors() + " " + s2);
						} else
							biblio.setAuthors(biblio.getAuthors() + s2);
					} else
						biblio.setAuthors(s2);
				} else {
					if (biblio.getAuthors() != null) {
						if (addSpace) {
							biblio.setAuthors(biblio.getAuthors() + " \n" + s2);
						} else
							biblio.setAuthors(biblio.getAuthors() + "\n" + s2);
					} else
						biblio.setAuthors(s2);
				}
			} else if ((s1.equals("<tech>")) || (s1.equals("I-<tech>"))) {
				biblio.setItem(BiblioItem.TechReport);
				if (biblio.getBookType() != null) {
					if (addSpace) {
						biblio.setBookType(biblio.getBookType() + " " + s2);
					} else
						biblio.setBookType(biblio.getBookType() + s2);
				} else
					biblio.setBookType(s2);
			} else if ((s1.equals("<location>")) || (s1.equals("I-<location>"))) {
				if (biblio.getLocation() != null) {
					if (addSpace)
						biblio.setLocation(biblio.getLocation() + " " + s2);
					else
						biblio.setLocation(biblio.getLocation() + s2);
				} else
					biblio.setLocation(s2);
			} else if ((s1.equals("<date>")) || (s1.equals("I-<date>"))) {
				// it appears that the same date is quite often repeated,
				// we should check, before adding a new date segment, if it is
				// not already present

				if (biblio.getPublicationDate() != null) {
					if (addSpace) {
						biblio.setPublicationDate(biblio.getPublicationDate() + " " + s2);
					} else
						biblio.setPublicationDate(biblio.getPublicationDate() + s2);
				} else
					biblio.setPublicationDate(s2);
			} else if ((s1.equals("<date-submission>")) || (s1.equals("I-<date-submission>"))) {
				// it appears that the same date is quite often repeated,
				// we should check, before adding a new date segment, if it is
				// not already present

				if (biblio.getSubmissionDate() != null) {
					if (addSpace) {
						biblio.setSubmissionDate(biblio.getSubmissionDate() + " " + s2);
					} else
						biblio.setSubmissionDate(biblio.getSubmissionDate() + s2);
				} else
					biblio.setSubmissionDate(s2);
			} else if ((s1.equals("<pages>")) || (s1.equals("<page>")) | (s1.equals("I-<pages>")) || (s1.equals("I-<page>"))) {
				if (biblio.getPageRange() != null) {
					if (addSpace) {
						biblio.setPageRange(biblio.getPageRange() + " " + s2);
					} else
						biblio.setPageRange(biblio.getPageRange() + s2);
				} else
					biblio.setPageRange(s2);
			} else if ((s1.equals("<editor>")) || (s1.equals("I-<editor>"))) {
				if (biblio.getEditors() != null) {
					if (addSpace) {
						biblio.setEditors(biblio.getEditors() + " " + s2);
					} else {
						biblio.setEditors(biblio.getEditors() + s2);
					}
				} else
					biblio.setEditors(s2);
			} else if ((s1.equals("<institution>")) || (s1.equals("I-<institution>"))) {
				if (biblio.getInstitution() != null) {
					if (addSpace) {
						biblio.setInstitution(biblio.getInstitution() + "; " + s2);
					} else
						biblio.setInstitution(biblio.getInstitution() + s2);
				} else
					biblio.setInstitution(s2);
			} else if ((s1.equals("<note>")) || (s1.equals("I-<note>"))) {
				if (biblio.getNote() != null) {
					if (addSpace) {
						biblio.setNote(biblio.getNote() + " " + s2);
					} else
						biblio.setNote(biblio.getNote() + s2);
				} else
					biblio.setNote(s2);
			} else if ((s1.equals("<abstract>")) || (s1.equals("I-<abstract>"))) {
				if (biblio.getAbstract() != null) {
					if (addSpace) {
						biblio.setAbstract(biblio.getAbstract() + " " + s2);
					} else
						biblio.setAbstract(biblio.getAbstract() + s2);
				} else
					biblio.setAbstract(s2);
			} else if ((s1.equals("<reference>")) || (s1.equals("I-<reference>"))) {
				if (biblio.getReference() != null) {
					if (addSpace) {
						biblio.setReference(biblio.getReference() + " " + s2);
					} else
						biblio.setReference(biblio.getReference() + s2);
				} else
					biblio.setReference(s2);
			} else if ((s1.equals("<grant>")) || (s1.equals("I-<grant>"))) {
				if (biblio.getGrant() != null) {
					if (addSpace) {
						biblio.setGrant(biblio.getGrant() + " " + s2);
					} else
						biblio.setGrant(biblio.getGrant() + s2);
				} else
					biblio.setGrant(s2);
			} else if ((s1.equals("<copyright>")) || (s1.equals("I-<copyright>"))) {
				if (biblio.getCopyright() != null) {
					if (addSpace) {
						biblio.setCopyright(biblio.getCopyright() + " " + s2);
					} else
						biblio.setCopyright(biblio.getCopyright() + s2);
				} else
					biblio.setCopyright(s2);
			} else if ((s1.equals("<affiliation>")) || (s1.equals("I-<affiliation>"))) {
				// affiliation **makers** should be marked SINGLECHAR LINESTART
				if (biblio.getAffiliation() != null) {
					if ((lastTag != null) && (s1.equals(lastTag) || lastTag.equals("I-<affiliation>"))) {
						if (s1.equals("I-<affiliation>")) {
							biblio.setAffiliation(biblio.getAffiliation() + " ; " + s2);
						} else if (addSpace) {
							biblio.setAffiliation(biblio.getAffiliation() + " " + s2);
						} else
							biblio.setAffiliation(biblio.getAffiliation() + s2);
					} else
						biblio.setAffiliation(biblio.getAffiliation() + " ; " + s2);
				} else
					biblio.setAffiliation(s2);
			} else if ((s1.equals("<address>")) || (s1.equals("I-<address>"))) {
				if (biblio.getAddress() != null) {
					if (addSpace) {
						biblio.setAddress(biblio.getAddress() + " " + s2);
					} else
						biblio.setAddress(biblio.getAddress() + s2);
				} else
					biblio.setAddress(s2);
			} else if ((s1.equals("<email>")) || (s1.equals("I-<email>"))) {
				if (biblio.getEmail() != null) {
					if (s1.equals("I-<email>"))
						biblio.setEmail(biblio.getEmail() + " ; " + s2);
					else if (addSpace)
						biblio.setEmail(biblio.getEmail() + " " + s2);
					else
						biblio.setEmail(biblio.getEmail() + s2);
				} else
					biblio.setEmail(s2);
			} else if ((s1.equals("<pubnum>")) || (s1.equals("I-<pubnum>"))) {
				if (biblio.getPubnum() != null) {
					if (addSpace)
						biblio.setPubnum(biblio.getPubnum() + " " + s2);
					else
						biblio.setPubnum(biblio.getPubnum() + s2);
				} else
					biblio.setPubnum(s2);
			} else if ((s1.equals("<keyword>")) || (s1.equals("I-<keyword>"))) {
				if (biblio.getKeyword() != null) {
					if (addSpace)
						biblio.setKeyword(biblio.getKeyword() + " " + s2);
					else
						biblio.setKeyword(biblio.getKeyword() + s2);
				} else
					biblio.setKeyword(s2);
			} else if ((s1.equals("<phone>")) || (s1.equals("I-<phone>"))) {
				if (biblio.getPhone() != null) {
					if (addSpace)
						biblio.setPhone(biblio.getPhone() + " " + s2);
					else
						biblio.setPhone(biblio.getPhone() + s2);
				} else
					biblio.setPhone(s2);
			} else if ((s1.equals("<degree>")) || (s1.equals("I-<degree>"))) {
				if (biblio.getDegree() != null) {
					if (addSpace)
						biblio.setDegree(biblio.getDegree() + " " + s2);
					else
						biblio.setDegree(biblio.getDegree() + s2);
				} else
					biblio.setDegree(s2);
			} else if ((s1.equals("<web>")) || (s1.equals("I-<web>"))) {
				if (biblio.getWeb() != null) {
					if (addSpace)
						biblio.setWeb(biblio.getWeb() + " " + s2);
					else
						biblio.setWeb(biblio.getWeb() + s2);
				} else
					biblio.setWeb(s2);
			} else if ((s1.equals("<dedication>")) || (s1.equals("I-<dedication>"))) {
				if (biblio.getDedication() != null) {
					if (addSpace)
						biblio.setDedication(biblio.getDedication() + " " + s2);
					else
						biblio.setDedication(biblio.getDedication() + s2);
				} else
					biblio.setDedication(s2);
			} else if ((s1.equals("<submission>")) || (s1.equals("I-<submission>"))) {
				if (biblio.getSubmission() != null) {
					if (addSpace)
						biblio.setSubmission(biblio.getSubmission() + " " + s2);
					else
						biblio.setSubmission(biblio.getSubmission() + s2);
				} else
					biblio.setSubmission(s2);
			} else if ((s1.equals("<entitle>")) || (s1.equals("I-<entitle>"))) {
				if (biblio.getEnglishTitle() != null) {
					if (s1.equals(lastTag)) {
						if (localFeatures.contains("LINESTART")) {
							biblio.setEnglishTitle(biblio.getEnglishTitle() + " " + s2);
						} else if (addSpace)
							biblio.setEnglishTitle(biblio.getEnglishTitle() + " " + s2);
						else
							biblio.setEnglishTitle(biblio.getEnglishTitle() + s2);
					} else
						biblio.setEnglishTitle(biblio.getEnglishTitle() + " ; " + s2);
				} else
					biblio.setEnglishTitle(s2);
			} else if (((s1.equals("<intro>")) || (s1.equals("I-<intro>"))) && intro) {
				return biblio;
			}
			lastTag = s1;
		}

		return biblio;
	}

	/**
	 * Extract results from a labelled header in the training format without any
	 * string modification.
	 * 
	 * @param result
	 *            result
	 * @param intro
	 *            if intro
	 * @param tokenizations
	 *            list of tokens
	 * @return a result
	 */
	private StringBuilder trainingExtraction(String result, boolean intro, ArrayList<String> tokenizations) {
		// this is the main buffer for the whole header
		StringBuilder buffer = new StringBuilder();

		StringTokenizer st = new StringTokenizer(result, "\n");
		String s1 = null;
		String s2 = null;
		String lastTag = null;

		int p = 0;

		while (st.hasMoreTokens()) {
			boolean addSpace = false;
			String tok = st.nextToken().trim();

			if (tok.length() == 0) {
				continue;
			}
			StringTokenizer stt = new StringTokenizer(tok, "\t");
			// ArrayList<String> localFeatures = new ArrayList<String>();
			int i = 0;

			boolean newLine = false;
			int ll = stt.countTokens();
			while (stt.hasMoreTokens()) {
				String s = stt.nextToken().trim();
				if (i == 0) {
					s2 = s;

					boolean strop = false;
					while ((!strop) && (p < tokenizations.size())) {
						String tokOriginal = tokenizations.get(p);
						if (tokOriginal.equals(" ")) {
							addSpace = true;
						} else if (tokOriginal.equals(s)) {
							strop = true;
						}
						p++;
					}
				} else if (i == ll - 1) {
					s1 = s;
				} else {
					if (s.equals("LINESTART"))
						newLine = true;
					// localFeatures.add(s);
				}
				i++;
			}

			if (newLine) {
				buffer.append("<lb/>");
			}

			String lastTag0 = null;
			if (lastTag != null) {
				if (lastTag.startsWith("I-")) {
					lastTag0 = lastTag.substring(2, lastTag.length());
				} else {
					lastTag0 = lastTag;
				}
			}
			String currentTag0 = null;
			if (s1 != null) {
				if (s1.startsWith("I-")) {
					currentTag0 = s1.substring(2, s1.length());
				} else {
					currentTag0 = s1;
				}
			}

			if (lastTag != null) {
				testClosingTag(buffer, currentTag0, lastTag0);
			}

			boolean output;

			output = writeField(buffer, s1, lastTag0, s2, "<title>", "<docTitle>\n\t<titlePart>", addSpace);
			if (!output) {
				output = writeField(buffer, s1, lastTag0, s2, "<author>", "<byline>\n\t<docAuthor>", addSpace);
			}
			if (!output) {
				output = writeField(buffer, s1, lastTag0, s2, "<location>", "<address>", addSpace);
			}
			if (!output) {
				output = writeField(buffer, s1, lastTag0, s2, "<address>", "<address>", addSpace);
			}
			if (!output) {
				output = writeField(buffer, s1, lastTag0, s2, "<date>", "<date>", addSpace);
			}
			if (!output) {
				output = writeField(buffer, s1, lastTag0, s2, "<date-submission>", "<date type=\"submission\">", addSpace);
			}
			if (!output) {
				output = writeField(buffer, s1, lastTag0, s2, "<booktitle>", "<booktitle>", addSpace);
			}
			if (!output) {
				output = writeField(buffer, s1, lastTag0, s2, "<pages>", "<pages>", addSpace);
			}
			if (!output) {
				output = writeField(buffer, s1, lastTag0, s2, "<publisher>", "<publisher>", addSpace);
			}
			if (!output) {
				output = writeField(buffer, s1, lastTag0, s2, "<journal>", "<journal>", addSpace);
			}
			if (!output) {
				output = writeField(buffer, s1, lastTag0, s2, "<institution>", "<byline>\n\t<affiliation>", addSpace);
			}
			if (!output) {
				output = writeField(buffer, s1, lastTag0, s2, "<affiliation>", "<byline>\n\t<affiliation>", addSpace);
			}
			if (!output) {
				output = writeField(buffer, s1, lastTag0, s2, "<volume>", "<volume>", addSpace);
			}
			if (!output) {
				output = writeField(buffer, s1, lastTag0, s2, "<editor>", "<editor>", addSpace);
			}
			if (!output) {
				output = writeField(buffer, s1, lastTag0, s2, "<note>", "<note type=\"other\">", addSpace);
			}
			if (!output) {
				output = writeField(buffer, s1, lastTag0, s2, "<abstract>", "<div type=\"abstract\">", addSpace);
			}
			if (!output) {
				output = writeField(buffer, s1, lastTag0, s2, "<email>", "<email>", addSpace);
			}
			if (!output) {
				output = writeField(buffer, s1, lastTag0, s2, "<pubnum>", "<pubnum>", addSpace);
			}
			if (!output) {
				output = writeField(buffer, s1, lastTag0, s2, "<keyword>", "<keyword>", addSpace);
			}
			if (!output) {
				output = writeField(buffer, s1, lastTag0, s2, "<phone>", "<phone>", addSpace);
			}
			if (!output) {
				output = writeField(buffer, s1, lastTag0, s2, "<degree>", "<degree>", addSpace);
			}
			if (!output) {
				output = writeField(buffer, s1, lastTag0, s2, "<web>", "<ptr type=\"web\">", addSpace);
			}
			if (!output) {
				output = writeField(buffer, s1, lastTag0, s2, "<dedication>", "<dedication>", addSpace);
			}
			if (!output) {
				output = writeField(buffer, s1, lastTag0, s2, "<submission>", "<note type=\"submission\">", addSpace);
			}
			if (!output) {
				output = writeField(buffer, s1, lastTag0, s2, "<entitle>", "<note type=\"title\">", addSpace);
			}
			if (!output) {
				output = writeField(buffer, s1, lastTag0, s2, "<reference>", "<reference>", addSpace);
			}
			if (!output) {
				output = writeField(buffer, s1, lastTag0, s2, "<copyright>", "<note type=\"copyright\">", addSpace);
			}
			if (!output) {
				// noinspection UnusedAssignment
				output = writeField(buffer, s1, lastTag0, s2, "<grant>", "<note type=\"grant\">", addSpace);
			}

			if (((s1.equals("<intro>")) || (s1.equals("I-<intro>"))) && intro) {
				// return buffer.toString();
				break;
			}
			lastTag = s1;

			if (!st.hasMoreTokens()) {
				if (lastTag != null) {
					testClosingTag(buffer, "", currentTag0);
				}
			}
		}

		return buffer;
	}

	private void testClosingTag(StringBuilder buffer, String currentTag0, String lastTag0) {
		if (!currentTag0.equals(lastTag0)) {
			// we close the current tag
			if (lastTag0.equals("<title>")) {
				buffer.append("</titlePart>\n\t</docTitle>\n");
			} else if (lastTag0.equals("<author>")) {
				buffer.append("</docAuthor>\n\t</byline>\n");
			} else if (lastTag0.equals("<location>")) {
				buffer.append("</address>\n");
			} else if (lastTag0.equals("<date>")) {
				buffer.append("</date>\n");
			} else if (lastTag0.equals("<abstract>")) {
				buffer.append("</div>\n");
			} else if (lastTag0.equals("<address>")) {
				buffer.append("</address>\n");
			} else if (lastTag0.equals("<date-submission>")) {
				buffer.append("</date>\n");
			} else if (lastTag0.equals("<booktitle>")) {
				buffer.append("</booktitle>\n");
			} else if (lastTag0.equals("<pages>")) {
				buffer.append("</pages>\n");
			} else if (lastTag0.equals("<email>")) {
				buffer.append("</email>\n");
			} else if (lastTag0.equals("<publisher>")) {
				buffer.append("</publisher>\n");
			} else if (lastTag0.equals("<institution>")) {
				buffer.append("</affiliation>\n\t</byline>\n");
			} else if (lastTag0.equals("<keyword>")) {
				buffer.append("</keyword>\n");
			} else if (lastTag0.equals("<affiliation>")) {
				buffer.append("</affiliation>\n\t</byline>\n");
			} else if (lastTag0.equals("<note>")) {
				buffer.append("</note>\n");
			} else if (lastTag0.equals("<reference>")) {
				buffer.append("</reference>\n");
			} else if (lastTag0.equals("<copyright>")) {
				buffer.append("</note>\n");
			} else if (lastTag0.equals("<grant>")) {
				buffer.append("</note>\n");
			} else if (lastTag0.equals("<entitle>")) {
				buffer.append("</note>\n");
			} else if (lastTag0.equals("<submission>")) {
				buffer.append("</note>\n");
			} else if (lastTag0.equals("<dedication>")) {
				buffer.append("</dedication>\n");
			} else if (lastTag0.equals("<web>")) {
				buffer.append("</ptr>\n");
			} else if (lastTag0.equals("<phone>")) {
				buffer.append("</phone>\n");
			}
		}
	}

	private boolean writeField(StringBuilder buffer, String s1, String lastTag0, String s2, String field, String outField, boolean addSpace) {
		boolean result = false;
		if ((s1.equals(field)) || (s1.equals("I-" + field))) {
			result = true;
			if (s1.equals(lastTag0)) {
				if (addSpace)
					buffer.append(" ").append(s2);
				else
					buffer.append(s2);
			} else
				buffer.append("\n\t").append(outField).append(s2);
		}
		return result;
	}

	/**
	 * Consolidate an existing list of recognized citations based on access to
	 * external internet bibliographic databases.
	 * 
	 * @param resHeader
	 *            original biblio item
	 * @return consolidated biblio item
	 */
	public BiblioItem consolidateHeader(BiblioItem resHeader) {
		try {
			if (consolidator == null) {
				consolidator = new Consolidation();
			}
			consolidator.openDb();
			ArrayList<BiblioItem> bibis = new ArrayList<BiblioItem>();
			boolean valid = consolidator.consolidateCrossrefGet(resHeader, bibis);
			if ((valid) && (bibis.size() > 0)) {
				BiblioItem bibo = bibis.get(0);
				if (bibo != null) {
					BiblioItem.correct(resHeader, bibo);
				}
			}
			consolidator.closeDb();
		} catch (Exception e) {
			// e.printStackTrace();
			throw new GrobidException("An exception occured while running Grobid.", e);
		}
		return resHeader;
	}

	@Override
	public void close() throws IOException {
		super.close();
		if (dateParser != null)
			dateParser.close();
		if (affiliationAddressParser != null)
			affiliationAddressParser.close();
		if (citationParser != null)
			citationParser.close();
		if (authorParser != null)
			authorParser.close();
	}
}
