package org.grobid.core.engines;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.grobid.core.GrobidModels;
import org.grobid.core.data.BibDataSet;
import org.grobid.core.data.BiblioItem;
import org.grobid.core.data.Date;
import org.grobid.core.document.Document;
import org.grobid.core.exceptions.GrobidException;
import org.grobid.core.features.FeaturesVectorCitation;
import org.grobid.core.lexicon.Lexicon;
import org.grobid.core.utilities.Consolidation;
import org.grobid.core.utilities.GrobidProperties;
import org.grobid.core.utilities.OffsetPosition;
import org.grobid.core.utilities.TextUtilities;

/**
 * @author Patrice Lopez
 */
public class CitationParser extends AbstractParser {
	private AuthorParser authorParser = null;
	private DateParser dateParser = null;
	private Consolidation consolidator = null;

	private File tmpPath = null;
	private String pathXML = null;

	public Lexicon lexicon = Lexicon.getInstance();

	public CitationParser() {
		super(GrobidModels.CITATION);
		tmpPath = GrobidProperties.getTempPath();
	}

	public String taggerRun(String ress) throws Exception {
		// clear internal context
		StringTokenizer st = new StringTokenizer(ress, "\n");
		feedTaggerAndParse(st);

		StringBuilder res = new StringBuilder();
		for (int i = 0; i < tagger.size(); i++) {
			for (int j = 0; j < tagger.xsize(); j++) {
				res.append(tagger.x(i, j)).append("\t");
			}
			res.append(tagger.y2(i));
			res.append("\n");
		}

		return res.toString();
	}

	public BiblioItem processing(String input, boolean consolidate) {
		BiblioItem resCitation;
		if (StringUtils.isBlank(input)) {
			return null;
		}

		try {
			ArrayList<String> citationBlocks = new ArrayList<String>();

			input = TextUtilities.dehyphenize(input);
			input = input.replace("\n", " ");
			input = input.replaceAll("\\p{Cntrl}"," ").trim();
			StringTokenizer st = new StringTokenizer(input,
					TextUtilities.fullPunctuations, true);

			if (st.countTokens() == 0)
				return null;

			ArrayList<String> tokenizations = new ArrayList<String>();
			while (st.hasMoreTokens()) {
				final String tok = st.nextToken();
				tokenizations.add(tok);
				if (!tok.equals(" ")) {
					citationBlocks.add(tok + " <citation>");
				}
			}
			citationBlocks.add("\n");

			List<List<OffsetPosition>> journalsPositions = new ArrayList<List<OffsetPosition>>();
			List<List<OffsetPosition>> abbrevJournalsPositions = new ArrayList<List<OffsetPosition>>();
			List<List<OffsetPosition>> conferencesPositions = new ArrayList<List<OffsetPosition>>();
			List<List<OffsetPosition>> publishersPositions = new ArrayList<List<OffsetPosition>>();

			journalsPositions.add(lexicon.inJournalNames(input));
			abbrevJournalsPositions.add(lexicon.inAbbrevJournalNames(input));
			conferencesPositions.add(lexicon.inConferenceNames(input));
			publishersPositions.add(lexicon.inPublisherNames(input));

			String ress = FeaturesVectorCitation.addFeaturesCitation(
					citationBlocks, journalsPositions, abbrevJournalsPositions,
					conferencesPositions, publishersPositions);
			String res = taggerRun(ress);

			resCitation = resultExtraction(res, true, tokenizations);

			// post-processing (additional field parsing and cleaning)
			if (resCitation != null) {
				BiblioItem.cleanTitles(resCitation);

				resCitation.setOriginalAuthors(resCitation.getAuthors());

				ArrayList<String> auts = new ArrayList<String>();
				auts.add(resCitation.getAuthors());

				if (authorParser == null) {
					authorParser = new AuthorParser();
				}
				resCitation.setFullAuthors(authorParser
						.processingCitation(auts));
				if (resCitation.getPublicationDate() != null) {
					if (dateParser == null) {
						dateParser = new DateParser();
					}
					ArrayList<Date> dates = dateParser.processing(resCitation
							.getPublicationDate());
					if (dates != null) {
						Date bestDate = null;
						if (dates.size() > 0) {
							// we take the earliest most specified date
							for (Date theDate : dates) {
								if (bestDate == null) {
									bestDate = theDate;
								} else {
									if (bestDate.compareTo(theDate) == 1) {
										bestDate = theDate;
									}
								}
							}
							if (bestDate != null) {
								resCitation
										.setNormalizedPublicationDate(bestDate);
							}
						}
					}
				}

				resCitation.setPageRange(TextUtilities.cleanField(
						resCitation.getPageRange(), true));
				resCitation.setPublisher(TextUtilities.cleanField(
						resCitation.getPublisher(), true));
				resCitation.setJournal(TextUtilities.cleanField(
						resCitation.getJournal(), true));
				resCitation.postProcessPages();
			}

			if (consolidate) {
				resCitation = consolidateCitation(resCitation);
			}

			return resCitation;
		} catch (Exception e) {
			throw new GrobidException(
					"An exception occured while running Grobid.", e);
		}
	}

	public ArrayList<BibDataSet> processingReferenceSection(String input,
			boolean consolidate) throws Exception {
		ArrayList<BibDataSet> results = new ArrayList<BibDataSet>();
		Document doc = new Document(input, tmpPath.getAbsolutePath());
		try {
			int startPage = -1;
			int endPage = -1;
			pathXML = doc.pdf2xml(true, false, startPage, endPage, input,
					tmpPath.getAbsolutePath(), false); // with timeout,
			// no force pdf reloading
			// input is the pdf file, tmpPath is the tmp directory for the lxml
			// file,
			// path is the resource path
			// we do not extract images in the pdf file
			if (pathXML == null) {
				throw new Exception("PDF parsing fails");
			}
			doc.setPathXML(pathXML);
			doc.addFeaturesDocument();

			if (doc.getBlocks() == null) {
				throw new Exception("PDF parsing resulted in empty content");
			}

			doc.addFeaturesDocument();
			doc.firstPass();

			// String reference =
			doc.getReferences();
			ArrayList<String> tokenizations = doc.getTokenizationsReferences();

			// System.out.println(reference.toString());
			List<String> references = segmentReferences(tokenizations);
			// System.out.println(references.toString());

			if (references != null) {
				for (String refString : references) {
					BiblioItem bib = processing(refString, consolidate);
					BibDataSet bds = new BibDataSet();
					bds.setResBib(bib);
					bds.setRawBib(refString);
					results.add(bds);
				}
			}
		} catch (Exception e) {
			throw new GrobidException(
					"An exception occured while running Grobid.", e);
		} finally {
			// keep it clean when leaving...
			doc.cleanLxmlFile(pathXML, true);
		}

		return results;
	}

	public List<String> segmentReferences(ArrayList<String> tokenizations) {
		if (tokenizations == null)
			return null;
		if (tokenizations.size() == 0)
			return null;
		StringBuilder referencesBuffer = new StringBuilder();
		for (String tok : tokenizations) {
			referencesBuffer.append(tok);
		}

		String references = referencesBuffer.toString();
		ArrayList<String> results = new ArrayList<String>();
		int best = 0;
		Matcher bestMatcher;
		int bestIndex = -1;
		for (int i = 0; i < citationMarkers.length; i++) {
			Matcher ma = citationMarkers[i].matcher(references);
			int count = 0;
			while (ma.find()) {
				count++;
			}
			if (count > best) {
				bestIndex = i;
				best = count;
			}
		}

		if (bestIndex == -1)
			return null;
		else
			bestMatcher = citationMarkers[bestIndex].matcher(references);
		int last = 0;
		int i = 0;
		while (bestMatcher.find()) {
			if (i == 0) {
				last = bestMatcher.end();
			} else {
				int newLast = bestMatcher.start();
				String lastRef = references.substring(last, newLast);
				if (testCitationProfile(lastRef)) {
					results.add(lastRef);	
				}
				last = bestMatcher.end();
			}
			i++;
		}
		// the last one - if at least one, has not been considered
		if (i > 0) {
			String lastRef = references.substring(last, references.length());
			if (testCitationProfile(lastRef)) {
				results.add(lastRef);
			}
		}

		return results;
	}
	
	private boolean testCitationProfile(String lastRef) {
		if (lastRef.length() < 400) {
			// we assume that a reference extracted from a full text cannot be be more than 400 characters 
			StringTokenizer st = new StringTokenizer(lastRef, "\n");
			if (st.countTokens() < 9) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Extract results from a labelled header.
	 * 
	 * @param result
	 *            result
	 * @param volumePostProcess
	 *            whether post process volume
	 * @param tokenizations
	 *            list of tokens
	 * @return bibilio item
	 */
	public BiblioItem resultExtraction(String result,
			boolean volumePostProcess, ArrayList<String> tokenizations) {
		BiblioItem biblio = new BiblioItem();

		StringTokenizer st = new StringTokenizer(result, "\n");
		String s1 = null;
		String s2 = null;
		String lastTag = null;

		int p = 0;
		// iterator for the tokenizations for restauring the original
		// tokenization with
		// respect to spaces

		while (st.hasMoreTokens()) {
			boolean addSpace = false;
			String tok = st.nextToken().trim();

			if (tok.length() == 0) {
				continue;
			}
			StringTokenizer stt = new StringTokenizer(tok, "\t");
			ArrayList<String> localFeatures = new ArrayList<String>();
			int i = 0;

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
					localFeatures.add(s);
				}
				i++;
			}

			if ((s1.equals("<title>")) || (s1.equals("I-<title>"))) {
				if (biblio.getTitle() != null) {
					if (addSpace)
						biblio.setTitle(biblio.getTitle() + " " + s2);
					else
						biblio.setTitle(biblio.getTitle() + s2);
				} else
					biblio.setTitle(s2);
			} else if ((s1.equals("<author>")) || (s1.equals("I-<author>"))) {

				if (biblio.getAuthors() != null) {
					if (addSpace)
						biblio.setAuthors(biblio.getAuthors() + " " + s2);
					else
						biblio.setAuthors(biblio.getAuthors() + s2);
				} else
					biblio.setAuthors(s2);
			} else if ((s1.equals("<tech>")) || (s1.equals("I-<tech>"))) {
				biblio.setType("book");
				if (biblio.getBookType() != null) {
					if (addSpace)
						biblio.setBookType(biblio.getBookType() + " " + s2);
					else
						biblio.setBookType(biblio.getBookType() + s2);
				} else
					biblio.setBookType(s2);
			} else if ((s1.equals("<location>")) || (s1.equals("I-<location>"))) {
				if (biblio.getLocation() != null) {
					if (s1.equals(lastTag) || lastTag.equals("I-<location>")) {
						if (addSpace)
							biblio.setLocation(biblio.getLocation() + " " + s2);
						else
							biblio.setLocation(biblio.getLocation() + s2);
					} else
						biblio.setLocation(biblio.getLocation() + " ; " + s2);
				} else
					biblio.setLocation(s2);
			} else if ((s1.equals("<date>")) || (s1.equals("I-<date>"))) {
				// it appears that the same date is quite often repeated,
				// we should check, before adding a new date segment, if it is
				// not already present

				if (biblio.getPublicationDate() != null) {
					if (s1.equals(lastTag) || lastTag.equals("I-<date>")) {
						if (addSpace)
							biblio.setPublicationDate(biblio
									.getPublicationDate() + " " + s2);
						else
							biblio.setPublicationDate(biblio
									.getPublicationDate() + s2);
					} else
						biblio.setPublicationDate(biblio.getPublicationDate()
								+ " . " + s2);
				} else
					biblio.setPublicationDate(s2);
			} else if ((s1.equals("<booktitle>"))
					|| (s1.equals("I-<booktitle>"))) {
				if (biblio.getBookTitle() != null) {
					if (localFeatures.contains("LINESTART")) {
						biblio.setBookTitle(biblio.getBookTitle() + " " + s2);
						// biblio.setBookTitle(biblio.getBookTitle() + "\n" +
						// s2);
					} else {
						if (addSpace)
							biblio.setBookTitle(biblio.getBookTitle() + " "
									+ s2);
						else
							biblio.setBookTitle(biblio.getBookTitle() + s2);
					}
				} else
					biblio.setBookTitle(s2);
			} else if ((s1.equals("<pages>")) || (s1.equals("<page>"))
					| (s1.equals("I-<pages>")) || (s1.equals("I-<page>"))) {
				if (biblio.getPageRange() != null) {
					if (addSpace)
						biblio.setPageRange(biblio.getPageRange() + " " + s2);
					else
						biblio.setPageRange(biblio.getPageRange() + s2);
				} else
					biblio.setPageRange(s2);
			} else if ((s1.equals("<publisher>"))
					|| (s1.equals("I-<publisher>"))) {
				if (biblio.getPublisher() != null) {
					if (addSpace)
						biblio.setPublisher(biblio.getPublisher() + " " + s2);
					else
						biblio.setPublisher(biblio.getPublisher() + s2);
				} else
					biblio.setPublisher(s2);
			} else if ((s1.equals("<journal>")) || (s1.equals("I-<journal>"))) {
				if (biblio.getJournal() != null) {
					if (localFeatures.contains("LINESTART")) {
						biblio.setJournal(biblio.getJournal() + " " + s2);
						// biblio.setJournal(biblio.getJournal() + "\n" + s2);
					} else {
						if (addSpace)
							biblio.setJournal(biblio.getJournal() + " " + s2);
						else
							biblio.setJournal(biblio.getJournal() + s2);
					}
				} else
					biblio.setJournal(s2);
			} else if ((s1.equals("<volume>")) || (s1.equals("I-<volume>"))) {
				if (biblio.getVolumeBlock() != null) {
					if (addSpace)
						biblio.setVolumeBlock(biblio.getVolumeBlock() + " "
								+ s2, volumePostProcess);
					else
						biblio.setVolumeBlock(biblio.getVolumeBlock() + s2,
								volumePostProcess);
				} else
					biblio.setVolumeBlock(s2, volumePostProcess);
			} else if ((s1.equals("<editor>")) || (s1.equals("I-<editor>"))) {
				if (biblio.getEditors() != null) {
					if (addSpace)
						biblio.setEditors(biblio.getEditors() + " " + s2);
					else
						biblio.setEditors(biblio.getEditors() + s2);
				} else
					biblio.setEditors(s2);
			} else if ((s1.equals("<institution>"))
					|| (s1.equals("I-<institution>"))) {
				if (biblio.getInstitution() != null) {
					if (localFeatures.contains("LINESTART")) {
						biblio.setInstitution(biblio.getInstitution() + "; "
								+ s2);
						// biblio.setInstitution(biblio.getInstitution() + "\n"
						// + s2);
					} else {
						if (addSpace)
							biblio.setInstitution(biblio.getInstitution() + " "
									+ s2);
						else
							biblio.setInstitution(biblio.getInstitution() + s2);
					}
				} else
					biblio.setInstitution(s2);
			} else if ((s1.equals("<note>")) || (s1.equals("I-<note>"))) {
				if (biblio.getNote() != null) {
					if (s1.equals(lastTag)) {
						if (addSpace)
							biblio.setNote(biblio.getNote() + " " + s2);
						else
							biblio.setNote(biblio.getNote() + s2);
					} else
						biblio.setNote(biblio.getNote() + ". " + s2);
				} else
					biblio.setNote(s2);
			} else if ((s1.equals("<pubnum>")) || (s1.equals("I-<pubnum>"))) {
				if (biblio.getPubnum() != null)
					biblio.setPubnum(biblio.getPubnum() + " " + s2);
				else
					biblio.setPubnum(s2);
			} else if ((s1.equals("<web>")) || (s1.equals("I-<web>"))) {
				if (biblio.getWeb() != null) {
					if (addSpace)
						biblio.setWeb(biblio.getWeb() + " " + s2);
					else
						biblio.setWeb(biblio.getWeb() + s2);
				} else
					biblio.setWeb(s2);
			}
			lastTag = s1;
		}

		return biblio;
	}

	/**
	 * Consolidate an existing list of recognized citations based on access to
	 * external internet bibliographic databases.
	 * 
	 * @param resCitation
	 *            citation
	 * @return consolidated biblio item
	 */
	public BiblioItem consolidateCitation(BiblioItem resCitation) {
		try {
			if (consolidator == null) {
				consolidator = new Consolidation();
			}
			consolidator.openDb();
			ArrayList<BiblioItem> bibis = new ArrayList<BiblioItem>();
			boolean valid = consolidator.consolidateCrossrefGet(resCitation,
					bibis);
			if ((valid) && (bibis.size() > 0)) {
				BiblioItem bibo = bibis.get(0);
				BiblioItem.correct(resCitation, bibo);
			}
			consolidator.closeDb();
		} catch (Exception e) {
			// e.printStackTrace();
			throw new GrobidException(
					"An exception occured while running Grobid.", e);
		}
		return resCitation;
	}

	/**
	 * Extract results from a list of citation strings in the training format
	 * without any string modification.
	 * 
	 * @param inputs
	 *            list of input data
	 * @return result
	 */
	public StringBuilder trainingExtraction(ArrayList<String> inputs) {
		StringBuilder buffer = new StringBuilder();
		try {
			if (inputs == null)
				return null;

			if (inputs.size() == 0)
				return null;

			List<List<OffsetPosition>> journalsPositions = new ArrayList<List<OffsetPosition>>();
			List<List<OffsetPosition>> abbrevJournalsPositions = new ArrayList<List<OffsetPosition>>();
			List<List<OffsetPosition>> conferencesPositions = new ArrayList<List<OffsetPosition>>();
			List<List<OffsetPosition>> publishersPositions = new ArrayList<List<OffsetPosition>>();

			for (String input : inputs) {
				ArrayList<String> tokenizations = new ArrayList<String>();
				ArrayList<String> citationBlocks = new ArrayList<String>();
				if (input == null)
					continue;
				// System.out.println("Input: "+input);
				StringTokenizer st = new StringTokenizer(input, " \t\n"
						+ TextUtilities.fullPunctuations, true);

				if (st.countTokens() == 0)
					return null;
				while (st.hasMoreTokens()) {
					String tok = st.nextToken();
					if (tok.equals("\n")) {
						citationBlocks.add("@newline");
					} else if (!tok.equals(" ")) {
						citationBlocks.add(tok + " <bibl>");
					}
					tokenizations.add(tok);
				}
				citationBlocks.add("\n");

				journalsPositions.add(lexicon.inJournalNames(input));
				abbrevJournalsPositions
						.add(lexicon.inAbbrevJournalNames(input));
				conferencesPositions.add(lexicon.inConferenceNames(input));
				publishersPositions.add(lexicon.inPublisherNames(input));

				String ress = FeaturesVectorCitation.addFeaturesCitation(
						citationBlocks, journalsPositions,
						abbrevJournalsPositions, conferencesPositions,
						publishersPositions);
				String res = taggerRun(ress);

				// extract results from the processed file
				StringTokenizer st2 = new StringTokenizer(res, "\n");
				String lastTag = null;
				boolean start = true;
				// boolean tagClosed = false;
				int q = 0;
				boolean addSpace;
				String lastTag0;
				String currentTag0;
				while (st2.hasMoreTokens()) {
					String line = st2.nextToken();
					addSpace = false;
					if ((line.trim().length() == 0)) {
						// new author
						buffer.append("/t<bibl>\n");
						continue;
					} else {
						String theTok = tokenizations.get(q);
						while (theTok.equals(" ")) {
							addSpace = true;
							q++;
							theTok = tokenizations.get(q);
						}
						q++;
					}

					StringTokenizer st3 = new StringTokenizer(line, "\t");
					int ll = st3.countTokens();
					int i = 0;
					String s1 = null;
					String s2 = null;
					String s3 = null;
					// boolean newLine = false;
					// ArrayList<String> localFeatures = new
					// ArrayList<String>();
					while (st3.hasMoreTokens()) {
						String s = st3.nextToken().trim();
						if (i == 0) {
							s2 = TextUtilities.HTMLEncode(s); // string
						} else if (i == ll - 2) {
							s3 = s; // pre-label, in this case it should always
									// be <author>
						} else if (i == ll - 1) {
							s1 = s; // label
						}
						i++;
					}

					if (start && (s1 != null)) {
						buffer.append("\t<bibl>");
						start = false;
					}

					lastTag0 = null;
					if (lastTag != null) {
						if (lastTag.startsWith("I-")) {
							lastTag0 = lastTag.substring(2, lastTag.length());
						} else {
							lastTag0 = lastTag;
						}
					}
					// currentTag0 = null;
					if (s1 != null) {
						if (s1.startsWith("I-")) {
							currentTag0 = s1.substring(2, s1.length());
						} else {
							currentTag0 = s1;
						}
					}

					// tagClosed = lastTag0 != null && testClosingTag(buffer,
					// currentTag0, lastTag0);

					String output = writeField(s1, lastTag0, s2, "<title>",
							"<title level=\"a\">", addSpace, 0);
					if (output != null) {
						buffer.append(output);
						lastTag = s1;
						continue;
					} else {
						output = writeField(s1, lastTag0, s2, "<other>", "",
								addSpace, 0);
					}
					if (output == null) {
						output = writeField(s1, lastTag0, s2, "<author>",
								"<author>", addSpace, 0);
					} else {
						buffer.append(output);
						lastTag = s1;
						continue;
					}
					if (output == null) {
						output = writeField(s1, lastTag0, s2, "<journal>",
								"<title level=\"j\">", addSpace, 0);
					} else {
						buffer.append(output);
						lastTag = s1;
						continue;
					}
					if (output == null) {
						output = writeField(s1, lastTag0, s2, "<date>",
								"<date>", addSpace, 0);
					} else {
						buffer.append(output);
						lastTag = s1;
						continue;
					}
					if (output == null) {
						output = writeField(s1, lastTag0, s2, "<booktitle>",
								"<title level=\"m\">", addSpace, 0);
					} else {
						buffer.append(output);
						lastTag = s1;
						continue;
					}
					if (output == null) {
						output = writeField(s1, lastTag0, s2, "<volume>",
								"<biblScope type=\"vol\">", addSpace, 0);
					} else {
						buffer.append(output);
						lastTag = s1;
						continue;
					}
					if (output == null) {
						output = writeField(s1, lastTag0, s2, "<publisher>",
								"<publisher>", addSpace, 0);
					} else {
						buffer.append(output);
						lastTag = s1;
						continue;
					}
					if (output == null) {
						output = writeField(s1, lastTag0, s2, "<location>",
								"<pubPlace>", addSpace, 0);
					} else {
						buffer.append(output);
						lastTag = s1;
						continue;
					}
					if (output == null) {
						output = writeField(s1, lastTag0, s2, "<editor>",
								"<editor>", addSpace, 0);
					} else {
						buffer.append(output);
						lastTag = s1;
						continue;
					}
					if (output == null) {
						output = writeField(s1, lastTag0, s2, "<pages>",
								"<biblScope type=\"pp\">", addSpace, 0);
					} else {
						buffer.append(output);
						lastTag = s1;
						continue;
					}
					if (output == null) {
						output = writeField(s1, lastTag0, s2, "<tech>",
								"<note type=\"report\">", addSpace, 0);
					} else {
						buffer.append(output);
						lastTag = s1;
						continue;
					}
					if (output == null) {
						output = writeField(s1, lastTag0, s2, "<issue>",
								"<biblScope type=\"issue\">", addSpace, 0);
					} else {
						buffer.append(output);
						lastTag = s1;
						continue;
					}
					if (output == null) {
						output = writeField(s1, lastTag0, s2, "<pubnum>",
								"<idno>", addSpace, 0);
					} else {
						buffer.append(output);
						lastTag = s1;
						continue;
					}
					if (output == null) {
						output = writeField(s1, lastTag0, s2, "<web>",
								"<ptr type=\"web\">", addSpace, 0);
					} else {
						buffer.append(output);
						lastTag = s1;
						continue;
					}
					if (output == null) {
						output = writeField(s1, lastTag0, s2, "<note>",
								"<note>", addSpace, 0);
					} else {
						buffer.append(output);
						lastTag = s1;
						continue;
					}
					if (output == null) {
						output = writeField(s1, lastTag0, s2, "<institution>",
								"<orgName>", addSpace, 0);
					} else {
						buffer.append(output);
						lastTag = s1;
						continue;
					}
					if (output != null) {
						buffer.append(output);
						lastTag = s1;
						continue;
					}
					lastTag = s1;
				}

				if (lastTag != null) {
					if (lastTag.startsWith("I-")) {
						lastTag0 = lastTag.substring(2, lastTag.length());
					} else {
						lastTag0 = lastTag;
					}
					currentTag0 = "";
					testClosingTag(buffer, currentTag0, lastTag0);
					buffer.append("</bibl>\n");
				}
			}
		} catch (Exception e) {
			// e.printStackTrace();
			throw new GrobidException(
					"An exception occured while running Grobid.", e);
		}
		return buffer;
	}

	private String writeField(String s1, String lastTag0, String s2,
			String field, String outField, boolean addSpace, int nbIndent) {
		String result = null;
		if ((s1.equals(field)) || (s1.equals("I-" + field))) {
			if (s1.equals(lastTag0) || s1.equals("I-" + lastTag0)) {
				if (addSpace)
					result = " " + s2;
				else
					result = s2;
			} else {
				result = "";
				for (int i = 0; i < nbIndent; i++) {
					result += "\t";
				}
				if (addSpace) {
					result += " " + outField + s2;
				} else {
					result += outField + s2;
				}
			}
		}
		return result;
	}

	private boolean testClosingTag(StringBuilder buffer, String currentTag0,
			String lastTag0) {
		boolean res = false;
		if (!currentTag0.equals(lastTag0)) {
			res = true;
			// we close the current tag
			if (lastTag0.equals("<other>")) {
				buffer.append("");
			} else if (lastTag0.equals("<title>")) {
				buffer.append("</title>");
			} else if (lastTag0.equals("<author>")) {
				buffer.append("</author>");
			} else if (lastTag0.equals("<tech>")) {
				buffer.append("</note>");
			} else if (lastTag0.equals("<location>")) {
				buffer.append("</pubPlace>");
			} else if (lastTag0.equals("<date>")) {
				buffer.append("</date>");
			} else if (lastTag0.equals("<booktitle>")) {
				buffer.append("</title>");
			} else if (lastTag0.equals("<pages>")) {
				buffer.append("</biblScope>");
			} else if (lastTag0.equals("<publisher>")) {
				buffer.append("</publisher>");
			} else if (lastTag0.equals("<journal>")) {
				buffer.append("</title>");
			} else if (lastTag0.equals("<volume>")) {
				buffer.append("</biblScope>");
			} else if (lastTag0.equals("<issue>")) {
				buffer.append("</biblScope>");
			} else if (lastTag0.equals("<editor>")) {
				buffer.append("</editor>");
			} else if (lastTag0.equals("<pubnum>")) {
				buffer.append("</idno>");
			} else if (lastTag0.equals("<web>")) {
				buffer.append("</ptr>");
			} else if (lastTag0.equals("<note>")) {
				buffer.append("</note>");
			} else if (lastTag0.equals("<institution>")) {
				buffer.append("</orgName>");
			} else {
				res = false;
			}

		}
		return res;
	}

	@Override
	public void close() throws IOException {
		super.close();
		if (authorParser != null) {
			authorParser.close();
		}
		if (dateParser != null) {
			dateParser.close();
		}

	}

	final static private Pattern m1 = Pattern.compile("((^|\\n)( )*\\[.+?\\])");
	final static private Pattern m2 = Pattern.compile("((^|\\n)( )*\\(.+?\\))");
	final static private Pattern m3 = Pattern
			.compile("((^|\\n)( )*\\d{1,3}\\.)");
	// static private Pattern m4 = Pattern.compile("(\\d{1,3})");
	static private Pattern[] citationMarkers = { m1, m2, m3 };
}
