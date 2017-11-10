package org.grobid.core.engines;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.collections4.CollectionUtils;

import org.grobid.core.GrobidModels;
import org.grobid.core.data.BibDataSet;
import org.grobid.core.data.BiblioItem;
import org.grobid.core.data.Date;
import org.grobid.core.document.Document;
import org.grobid.core.document.DocumentSource;
import org.grobid.core.engines.citations.LabeledReferenceResult;
import org.grobid.core.engines.citations.ReferenceSegmenter;
import org.grobid.core.engines.config.GrobidAnalysisConfig;
import org.grobid.core.engines.counters.CitationParserCounters;
import org.grobid.core.engines.label.SegmentationLabels;
import org.grobid.core.exceptions.GrobidException;
import org.grobid.core.features.FeaturesVectorCitation;
import org.grobid.core.lexicon.Lexicon;
import org.grobid.core.utilities.Consolidation;
import org.grobid.core.layout.LayoutToken;
import org.grobid.core.utilities.LayoutTokensUtil;
import org.grobid.core.utilities.OffsetPosition;
import org.grobid.core.utilities.TextUtilities;
import org.grobid.core.utilities.UnicodeUtil;
import org.grobid.core.utilities.counters.CntManager;
import org.grobid.core.tokenization.TaggingTokenCluster;
import org.grobid.core.tokenization.TaggingTokenClusteror;
import org.grobid.core.engines.label.TaggingLabel;
import org.grobid.core.engines.label.TaggingLabels;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Patrice Lopez
 */
public class CitationParser extends AbstractParser {
    public Lexicon lexicon = Lexicon.getInstance();
    private EngineParsers parsers;

    public CitationParser(EngineParsers parsers, CntManager cntManager) {
        super(GrobidModels.CITATION, cntManager);
        this.parsers = parsers;
    }

    public CitationParser(EngineParsers parsers) {
        super(GrobidModels.CITATION);
        this.parsers = parsers;
    }

    public BiblioItem processing(String input, boolean consolidate) {
        if (StringUtils.isBlank(input)) {
            return null;
        }

        // some cleaning
        input = UnicodeUtil.normaliseText(input);
        //input = TextUtilities.dehyphenize(input);
        //input = input.replace("\n", " ");
        //input = input.replaceAll("\\p{Cntrl}", " ").trim();

        List<LayoutToken> tokens = analyzer.tokenizeWithLayoutToken(input);
        return processing(tokens, consolidate);
    }

    public BiblioItem processing(List<LayoutToken> tokens, boolean consolidate) {
        BiblioItem resCitation;
        if (CollectionUtils.isEmpty(tokens)) {
            return null;
        }

        try {
            List<String> citationBlocks = new ArrayList<>();

            //tokens = LayoutTokensUtil.dehyphenize(tokens);

            List<OffsetPosition> journalsPositions = lexicon.tokenPositionsJournalNames(tokens);
            List<OffsetPosition> abbrevJournalsPositions = lexicon.tokenPositionsAbbrevJournalNames(tokens);
            List<OffsetPosition> conferencesPositions = lexicon.tokenPositionsConferenceNames(tokens);
            List<OffsetPosition> publishersPositions = lexicon.tokenPositionsPublisherNames(tokens);
            List<OffsetPosition> locationsPositions = lexicon.tokenPositionsLocationNames(tokens);
            List<OffsetPosition> collaborationsPositions = lexicon.tokenPositionsCollaborationNames(tokens);
            List<OffsetPosition> identifiersPositions = lexicon.tokenPositionsIdentifierPattern(tokens);
            List<OffsetPosition> urlPositions = lexicon.tokenPositionsUrlPattern(tokens);

            String ress = FeaturesVectorCitation.addFeaturesCitation(tokens, null, journalsPositions, 
                abbrevJournalsPositions, conferencesPositions, publishersPositions, locationsPositions,
                collaborationsPositions, identifiersPositions, urlPositions);

            String res = label(ress);
//System.out.println(res);
            resCitation = resultExtractionLayoutTokens(res, true, tokens);
            // post-processing (additional field parsing and cleaning)
            if (resCitation != null) {
                BiblioItem.cleanTitles(resCitation);

                resCitation.setOriginalAuthors(resCitation.getAuthors());
                resCitation.setFullAuthors(parsers.getAuthorParser().processingCitation(resCitation.getAuthors()));
                if (resCitation.getPublicationDate() != null) {
                    List<Date> dates = parsers.getDateParser().processing(resCitation
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

    public List<BibDataSet> processingReferenceSection(String referenceTextBlock, ReferenceSegmenter referenceSegmenter) {
        List<LabeledReferenceResult> segm = referenceSegmenter.extract(referenceTextBlock);

        List<BibDataSet> results = new ArrayList<>();
        for (LabeledReferenceResult ref : segm) {
            BiblioItem bib = processing(ref.getTokens(), false);
            if ((bib != null) && !bib.rejectAsReference()) {
                BibDataSet bds = new BibDataSet();
                bds.setRefSymbol(ref.getLabel());
                bds.setResBib(bib);
                bds.setRawBib(ref.getReferenceText());
                bds.getResBib().setCoordinates(ref.getCoordinates());
                results.add(bds);
            }
        }
        return results;
    }

    public List<BibDataSet> processingReferenceSection(Document doc, ReferenceSegmenter referenceSegmenter, boolean consolidate) {
        List<BibDataSet> results = new ArrayList<BibDataSet>();

        String referencesStr = doc.getDocumentPartText(SegmentationLabels.REFERENCES);

        if (StringUtils.isEmpty(referencesStr)) {
            cntManager.i(CitationParserCounters.EMPTY_REFERENCES_BLOCKS);
            return results;
        }

        cntManager.i(CitationParserCounters.NOT_EMPTY_REFERENCES_BLOCKS);

        List<LabeledReferenceResult> references = referenceSegmenter.extract(doc);

        if (references == null) {
            cntManager.i(CitationParserCounters.NULL_SEGMENTED_REFERENCES_LIST);
            return results;
        } else {
            cntManager.i(CitationParserCounters.SEGMENTED_REFERENCES, references.size());
        }

        // consolidation: if selected, is not done individually for each citation but 
        // in a second stage for all citations
        for (LabeledReferenceResult ref : references) {
            BiblioItem bib = processing(ref.getReferenceText(), false);
            if ((bib != null) && !bib.rejectAsReference()) {
                BibDataSet bds = new BibDataSet();
                bds.setRefSymbol(ref.getLabel());
                bds.setResBib(bib);
                bds.setRawBib(ref.getReferenceText());
                bds.getResBib().setCoordinates(ref.getCoordinates());
                results.add(bds);
            }
        }

        // consolidate the set
        if (consolidate) {
            Consolidation consolidator = new Consolidation(cntManager);
            Map<Integer,BiblioItem> resConsolidation = null;
            try {
                resConsolidation = consolidator.consolidate(results);
            } catch(Exception e) {
                throw new GrobidException(
                "An exception occured while running consolidation on bibliographical references.", e);
            } finally {
                //consolidator.close();
            }
            if (resConsolidation != null) {

int consolidated = 0;
for (Entry<Integer, BiblioItem> cursor : resConsolidation.entrySet()) {
if (cursor.getValue() != null) {
consolidated++;
} 
}
System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> total (CrossRef JSON search API): " + consolidated + " / " + resConsolidation.size());

                for(int i=0; i<results.size(); i++) {
                    BiblioItem resCitation = results.get(i).getResBib();
                    BiblioItem bibo = resConsolidation.get(new Integer(i));
                    if (bibo != null) {
                        BiblioItem.correct(resCitation, bibo);
                    }
                }
            }
        }

        doc.setBibDataSets(results);

        return results;
    }

    public List<BibDataSet> processingReferenceSection(File input,
                                                       ReferenceSegmenter referenceSegmenter,
                                                       boolean consolidate) {
        DocumentSource documentSource = DocumentSource.fromPdf(input);
        return processingReferenceSection(documentSource, referenceSegmenter, consolidate);
    }

    public List<BibDataSet> processingReferenceSection(DocumentSource documentSource,
                                                       ReferenceSegmenter referenceSegmenter,
                                                       boolean consolidate) {
        List<BibDataSet> results;
        try {
            Document doc = parsers.getSegmentationParser().processing(documentSource,
                    GrobidAnalysisConfig.builder().consolidateCitations(consolidate).build());
            results = processingReferenceSection(doc, referenceSegmenter, consolidate);
        } catch (GrobidException e) {
            throw e;
        } catch (Exception e) {
            throw new GrobidException("An exception occurred while running Grobid.", e);
        }

        return results;
    }


    /**
     * Extract results from a labeled sequence.
     *
     * @param result            result
     * @param volumePostProcess whether post process volume
     * @param tokenizations     list of tokens
     * @return bibilio item
     */
    public BiblioItem resultExtractionLayoutTokens(String result,
                                       boolean volumePostProcess,
                                       List<LayoutToken> tokenizations) {
        BiblioItem biblio = new BiblioItem();

        TaggingLabel lastClusterLabel = null;
        TaggingTokenClusteror clusteror = new TaggingTokenClusteror(GrobidModels.CITATION, result, tokenizations);

        String tokenLabel = null;
        List<TaggingTokenCluster> clusters = clusteror.cluster();
        for (TaggingTokenCluster cluster : clusters) {
            if (cluster == null) {
                continue;
            }

            TaggingLabel clusterLabel = cluster.getTaggingLabel();
            Engine.getCntManager().i(clusterLabel);

            //String clusterContent = LayoutTokensUtil.normalizeText(LayoutTokensUtil.toText(cluster.concatTokens()));
            //String clusterContent = LayoutTokensUtil.toText(cluster.concatTokens());
            String clusterContent = LayoutTokensUtil.normalizeDehyphenizeText(cluster.concatTokens());
            //String clusterNonDehypenizedContent = LayoutTokensUtil.toText(cluster.concatTokens());
            if (clusterLabel.equals(TaggingLabels.CITATION_TITLE)) {
                if (biblio.getTitle() == null)
                    biblio.setTitle(clusterContent);
                else if (biblio.getTitle().length() >= clusterContent.length())
                    biblio.setNote(clusterContent);
                else {
                    biblio.setNote(biblio.getTitle());
                    biblio.setTitle(clusterContent);
                }
            } else if (clusterLabel.equals(TaggingLabels.CITATION_AUTHOR)) {
                if (biblio.getAuthors() == null)
                    biblio.setAuthors(clusterContent);
                else
                    biblio.setAuthors(biblio.getAuthors() + " ; " + clusterContent);
            } else if (clusterLabel.equals(TaggingLabels.CITATION_TECH)) {
                biblio.setBookType(clusterContent);
            } else if (clusterLabel.equals(TaggingLabels.CITATION_LOCATION)) {
                if (biblio.getLocation() != null)
                    biblio.setLocation(biblio.getLocation() + "; " + clusterContent);
                else
                    biblio.setLocation(clusterContent);
            } else if (clusterLabel.equals(TaggingLabels.CITATION_DATE)) {
                if (biblio.getPublicationDate() != null)
                    biblio.setPublicationDate(biblio.getPublicationDate() + ". " + clusterContent);
                else
                    biblio.setPublicationDate(clusterContent);
            } else if (clusterLabel.equals(TaggingLabels.CITATION_BOOKTITLE)) {
                if (biblio.getBookTitle() == null)
                    biblio.setBookTitle(clusterContent);
                else if (biblio.getBookTitle().length() >= clusterContent.length())
                    biblio.setNote(clusterContent);
                else {
                    biblio.setNote(biblio.getBookTitle());
                    biblio.setBookTitle(clusterContent);
                }
            } else if (clusterLabel.equals(TaggingLabels.CITATION_PAGES)) {
                String clusterNonDehypenizedContent = LayoutTokensUtil.toText(cluster.concatTokens());
                biblio.setPageRange(clusterNonDehypenizedContent);
            } else if (clusterLabel.equals(TaggingLabels.CITATION_PUBLISHER)) {
                biblio.setPublisher(clusterContent);
            } else if (clusterLabel.equals(TaggingLabels.CITATION_COLLABORATION)) {
                if (biblio.getCollaboration() != null)
                    biblio.setCollaboration(biblio.getCollaboration() + " ; " + clusterContent);
                else
                    biblio.setCollaboration(clusterContent);
            } else if (clusterLabel.equals(TaggingLabels.CITATION_JOURNAL)) {
                if (biblio.getJournal() == null)
                    biblio.setJournal(clusterContent);
                else if (biblio.getJournal().length() >= clusterContent.length())
                    biblio.setNote(clusterContent);
                else {
                    biblio.setNote(biblio.getJournal());
                    biblio.setJournal(clusterContent);
                }
            } else if (clusterLabel.equals(TaggingLabels.CITATION_VOLUME)) {
                if (biblio.getVolumeBlock() == null)
                   biblio.setVolumeBlock(clusterContent, volumePostProcess);
            } else if (clusterLabel.equals(TaggingLabels.CITATION_ISSUE)) {
                if (biblio.getIssue() == null)
                    biblio.setIssue(clusterContent);
            } else if (clusterLabel.equals(TaggingLabels.CITATION_EDITOR)) {
                biblio.setEditors(clusterContent);
            } else if (clusterLabel.equals(TaggingLabels.CITATION_INSTITUTION)) {
                if (biblio.getInstitution() != null)
                    biblio.setInstitution(biblio.getInstitution() + " ; " + clusterContent);
                else
                   biblio.setInstitution(clusterContent);
            } else if (clusterLabel.equals(TaggingLabels.CITATION_NOTE)) {             
                if (biblio.getNote() != null)
                    biblio.setNote(biblio.getNote()+ ". " + clusterContent);
                else    
                   biblio.setNote(clusterContent);
            } else if (clusterLabel.equals(TaggingLabels.CITATION_PUBNUM)) {
                String clusterNonDehypenizedContent = LayoutTokensUtil.toText(cluster.concatTokens());
                biblio.setPubnum(clusterNonDehypenizedContent);
                biblio.checkIdentifier();
            } else if (clusterLabel.equals(TaggingLabels.CITATION_WEB)) {
                String clusterNonDehypenizedContent = LayoutTokensUtil.toText(cluster.concatTokens());
                biblio.setWeb(clusterNonDehypenizedContent);
            }
        }

        return biblio;
    }

    /**
     * Consolidate an existing list of recognized citations based on access to
     * external internet bibliographic databases.
     *
     * @param resCitation citation
     * @return consolidated biblio item
     */
    public BiblioItem consolidateCitation(BiblioItem resCitation) {
        Consolidation consolidator = null;
        try {                
            consolidator = new Consolidation(cntManager);
            ArrayList<BiblioItem> bibis = new ArrayList<BiblioItem>();
            boolean valid = consolidator.consolidate(resCitation, bibis);
            if ((valid) && (bibis.size() > 0)) {
                BiblioItem bibo = bibis.get(0);
                BiblioItem.correct(resCitation, bibo);
            }
        } catch (Exception e) {
            // e.printStackTrace();
            throw new GrobidException(
                    "An exception occured while running Grobid.", e);
        } finally {
            //consolidator.close();
        }
        return resCitation;
    }

    /**
     * Extract results from a list of citation strings in the training format
     * without any string modification.
     *
     * @param inputs list of input data
     * @return result
     */
    public StringBuilder trainingExtraction(List<String> inputs) {
        StringBuilder buffer = new StringBuilder();
        try {
            if (inputs == null)
                return null;

            if (inputs.size() == 0)
                return null;

            List<OffsetPosition> journalsPositions = null;
            List<OffsetPosition> abbrevJournalsPositions = null;
            List<OffsetPosition> conferencesPositions = null;
            List<OffsetPosition> publishersPositions = null;
            List<OffsetPosition> locationsPositions = null;
            List<OffsetPosition> collaborationsPositions = null;
            List<OffsetPosition> identifiersPositions = null;
            List<OffsetPosition> urlPositions = null;
            for (String input : inputs) {
                //List<String> citationBlocks = new ArrayList<String>();
                if (input == null)
                    continue;
                // System.out.println("Input: "+input);
                //StringTokenizer st = new StringTokenizer(input, " \t\n"
                //        + TextUtilities.fullPunctuations, true);

                List<LayoutToken> tokenizations = analyzer.tokenizeWithLayoutToken(input);
                if (tokenizations.size() == 0)
                    return null;

                journalsPositions = lexicon.tokenPositionsJournalNames(tokenizations);
                abbrevJournalsPositions = lexicon.tokenPositionsAbbrevJournalNames(tokenizations);
                conferencesPositions = lexicon.tokenPositionsConferenceNames(tokenizations);
                publishersPositions = lexicon.tokenPositionsPublisherNames(tokenizations);
                locationsPositions = lexicon.tokenPositionsLocationNames(tokenizations);
                collaborationsPositions = lexicon.tokenPositionsCollaborationNames(tokenizations);
                identifiersPositions = lexicon.tokenPositionsIdentifierPattern(tokenizations);
                urlPositions = lexicon.tokenPositionsUrlPattern(tokenizations);

                String ress = FeaturesVectorCitation.addFeaturesCitation(tokenizations,
                        null, journalsPositions, abbrevJournalsPositions, 
                        conferencesPositions, publishersPositions, locationsPositions, 
                        collaborationsPositions, identifiersPositions, urlPositions);
                String res = label(ress);

                // extract results from the processed file
                StringTokenizer st2 = new StringTokenizer(res, "\n");
                String lastTag = null;
                boolean start = true;
                // boolean tagClosed = false;
                int q = 0;
                boolean addSpace;
                String lastTag0;
                String currentTag0 = null;
                while (st2.hasMoreTokens()) {
                    String line = st2.nextToken();
                    addSpace = false;
                    if ((line.trim().length() == 0)) {
                        // new author
                        buffer.append("/t<bibl>\n");
                        continue;
                    } else {
                        String theTok = tokenizations.get(q).getText();
                        while (theTok.equals(" ")) {
                            addSpace = true;
                            q++;
                            theTok = tokenizations.get(q).getText();
                        }
                        q++;
                    }

                    StringTokenizer st3 = new StringTokenizer(line, "\t");
                    int ll = st3.countTokens();
                    int i = 0;
                    String s1 = null;
                    String s2 = null;
                    while (st3.hasMoreTokens()) {
                        String s = st3.nextToken().trim();
                        if (i == 0) {
                            s2 = TextUtilities.HTMLEncode(s); // string
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

                    //tagClosed = lastTag0 != null &&
                    if ((lastTag0 != null) && (currentTag0 != null))
                        testClosingTag(buffer, currentTag0, lastTag0);

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
                                "<biblScope unit=\"volume\">", addSpace, 0);
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
                                "<biblScope unit=\"page\">", addSpace, 0);
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
                                "<biblScope unit=\"issue\">", addSpace, 0);
                    } else {
                        buffer.append(output);
                        lastTag = s1;
                        continue;
                    }
                    if (output == null) {
                        String localTag = "<idno>";
                        String cleanS2 = StringUtils.normalizeSpace(s2);
                        cleanS2 = cleanS2.replace(" ", "");
                        Matcher arxivMatcher = TextUtilities.arXivPattern.matcher(cleanS2);
                        if (arxivMatcher.find())
                            localTag = "<idno type=\"arXiv\">";
                        else {
                            Matcher doiMatcher = TextUtilities.DOIPattern.matcher(cleanS2);
                            if (doiMatcher.find())
                                localTag = "<idno type=\"doi\">";
                        }
                        output = writeField(s1, lastTag0, s2, "<pubnum>", localTag, addSpace, 0);
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
                    if (output == null) {
                        output = writeField(s1, lastTag0, s2, "<collaboration>",
                                "<orgName type=\"collaboration\">", addSpace, 0);
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
            } else if (lastTag0.equals("<collaboration>")) {
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
    }
}
