package org.grobid.core.engines;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.grobid.core.analyzers.GrobidAnalyzer;
import org.grobid.core.document.Document;
import org.grobid.core.document.DocumentPiece;
import org.grobid.core.factory.GrobidFactory;
import org.grobid.core.layout.Block;
import org.grobid.core.layout.LayoutToken;
import org.grobid.core.main.LibraryLoader;
import org.grobid.core.utilities.GrobidProperties;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.SortedSet;
import java.util.stream.Collectors;

import static org.easymock.EasyMock.*;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;

public class FullTextParserTest {

    private FullTextParser target;

    @Before
    public void setUp() throws Exception {
        target = new FullTextParser(new EngineParsers());
    }

    @BeforeClass
    public static void init() {
        LibraryLoader.load();
        GrobidProperties.getInstance();
    }

    @AfterClass
    public static void tearDown() {
        GrobidFactory.reset();
    }

    @Test
    public void testProcessTrainingDataFigures_single_figure() throws Exception {
        String text = "The mechanism for superconductivity FIG. 1. λ(T) vs . T for YBCO";
        List<LayoutToken> tokens = GrobidAnalyzer.getInstance().tokenizeWithLayoutToken(text);
        String rese = "The\tthe\tT\tTh\tThe\tThe\te\the\tThe\tThe\tBLOCKSTART\tLINESTART\tALIGNEDLEFT\tNEWFONT\tHIGHERFONT\t0\t0\tINITCAP\tNODIGIT\t0\tNOPUNCT\t0\t4\t0\tNUMBER\t0\t0\tI-<paragraph>\n" +
            "mechanism\tmechanism\tm\tme\tmec\tmech\tm\tsm\tism\tnism\tBLOCKIN\tLINEIN\tALIGNEDLEFT\tSAMEFONT\tSAMEFONTSIZE\t0\t0\tNOCAPS\tNODIGIT\t0\tNOPUNCT\t0\t4\t0\tNUMBER\t0\t0\t<paragraph>\n" +
            "for\tfor\tf\tfo\tfor\tfor\tr\tor\tfor\tfor\tBLOCKIN\tLINEIN\tALIGNEDLEFT\tSAMEFONT\tSAMEFONTSIZE\t0\t0\tNOCAPS\tNODIGIT\t0\tNOPUNCT\t0\t4\t0\tNUMBER\t0\t0\t<paragraph>\n" +
            "superconductivity\tsuperconductivity\ts\tsu\tsup\tsupe\ty\tty\tity\tvity\tBLOCKIN\tLINEIN\tALIGNEDLEFT\tSAMEFONT\tSAMEFONTSIZE\t0\t0\tNOCAPS\tNODIGIT\t0\tNOPUNCT\t0\t4\t0\tNUMBER\t0\t0\t<paragraph>\n" +
            "FIG\tfig\tF\tFI\tFIG\tFIG\tG\tIG\tFIG\tFIG\tBLOCKSTART\tLINESTART\tLINEINDENT\tNEWFONT\tHIGHERFONT\t0\t0\tALLCAP\tNODIGIT\t0\tNOPUNCT\t10\t3\t0\tNUMBER\t0\t0\tI-<figure>\n" +
            ".\t.\t.\t.\t.\t.\t.\t.\t.\t.\tBLOCKIN\tLINEIN\tLINEINDENT\tSAMEFONT\tSAMEFONTSIZE\t0\t0\tALLCAP\tNODIGIT\t1\tDOT\t10\t3\t0\tNUMBER\t0\t0\t<figure>\n" +
            "1\t1\t1\t1\t1\t1\t1\t1\t1\t1\tBLOCKIN\tLINEIN\tLINEINDENT\tSAMEFONT\tSAMEFONTSIZE\t0\t0\tNOCAPS\tALLDIGIT\t1\tNOPUNCT\t10\t3\t0\tNUMBER\t1\t0\t<figure>\n" +
            ".\t.\t.\t.\t.\t.\t.\t.\t.\t.\tBLOCKIN\tLINEIN\tLINEINDENT\tSAMEFONT\tSAMEFONTSIZE\t0\t0\tALLCAP\tNODIGIT\t1\tDOT\t10\t3\t0\tNUMBER\t0\t0\t<figure>\n" +
            "λ\tλ\tλ\tλ\tλ\tλ\tλ\tλ\tλ\tλ\tBLOCKIN\tLINEIN\tLINEINDENT\tNEWFONT\tSAMEFONTSIZE\t0\t0\tNOCAPS\tNODIGIT\t1\tNOPUNCT\t10\t3\t0\tNUMBER\t0\t0\t<figure>\n" +
            "(\t(\t(\t(\t(\t(\t(\t(\t(\t(\tBLOCKIN\tLINEIN\tLINEINDENT\tSAMEFONT\tSAMEFONTSIZE\t0\t0\tALLCAP\tNODIGIT\t1\tOPENBRACKET\t10\t3\t0\tNUMBER\t0\t0\t<figure>\n" +
            "T\tt\tT\tT\tT\tT\tT\tT\tT\tT\tBLOCKIN\tLINEIN\tLINEINDENT\tSAMEFONT\tSAMEFONTSIZE\t0\t0\tALLCAP\tNODIGIT\t1\tNOPUNCT\t10\t3\t0\tNUMBER\t0\t0\t<figure>\n" +
            ")\t)\t)\t)\t)\t)\t)\t)\t)\t)\tBLOCKIN\tLINEIN\tLINEINDENT\tNEWFONT\tSAMEFONTSIZE\t0\t0\tALLCAP\tNODIGIT\t1\tENDBRACKET\t10\t3\t0\tNUMBER\t0\t0\t<figure>\n" +
            "vs\tvs\tv\tvs\tvs\tvs\ts\tvs\tvs\tvs\tBLOCKIN\tLINEIN\tLINEINDENT\tSAMEFONT\tSAMEFONTSIZE\t0\t0\tNOCAPS\tNODIGIT\t0\tNOPUNCT\t10\t3\t0\tNUMBER\t0\t0\t<figure>\n" +
            ".\t.\t.\t.\t.\t.\t.\t.\t.\t.\tBLOCKIN\tLINEEND\tLINEINDENT\tSAMEFONT\tSAMEFONTSIZE\t0\t0\tALLCAP\tNODIGIT\t1\tDOT\t10\t3\t0\tNUMBER\t0\t0\t<figure>\n" +
            "T\tt\tT\tT\tT\tT\tT\tT\tT\tT\tBLOCKIN\tLINESTART\tLINEINDENT\tNEWFONT\tSAMEFONTSIZE\t0\t0\tALLCAP\tNODIGIT\t1\tNOPUNCT\t10\t3\t0\tNUMBER\t0\t0\t<figure>\n" +
            "for\tfor\tf\tfo\tfor\tfor\tr\tor\tfor\tfor\tBLOCKIN\tLINEIN\tLINEINDENT\tNEWFONT\tSAMEFONTSIZE\t0\t0\tNOCAPS\tNODIGIT\t0\tNOPUNCT\t10\t3\t0\tNUMBER\t0\t0\t<figure>\n" +
            "YBCO\tybco\tY\tYB\tYBC\tYBCO\tO\tCO\tBCO\tYBCO\tBLOCKIN\tLINEIN\tLINEINDENT\tSAMEFONT\tSAMEFONTSIZE\t0\t0\tALLCAP\tNODIGIT\t0\tNOPUNCT\t10\t3\t0\tNUMBER\t0\t0\t<figure>\n\n";


        Pair<String, String> stringStringPair = target.processTrainingDataFigures(rese, tokens, "123");

        String tei = stringStringPair.getLeft();
        String tokenisation = stringStringPair.getRight();
        String reconstructedText = Arrays.stream(tokenisation.split("\n")).map(l -> l.split("\t")[0]).collect(Collectors.joining(" "));

        assertThat(reconstructedText, is("FIG . 1 . λ ( T ) vs . T for YBCO"));
        assertThat(tokenisation.split("\n").length, is(13));

    }

    @Test
    public void testProcessTrainingDataFigures_multiple_figures() throws Exception {
        String text = "The mechanism for superconductivity FIG. 1. λ(T) vs . T for YBCO";
        List<LayoutToken> tokens = GrobidAnalyzer.getInstance().tokenizeWithLayoutToken(text);
        String rese = "The\tthe\tT\tTh\tThe\tThe\te\the\tThe\tThe\tBLOCKSTART\tLINESTART\tALIGNEDLEFT\tNEWFONT\tHIGHERFONT\t0\t0\tINITCAP\tNODIGIT\t0\tNOPUNCT\t0\t4\t0\tNUMBER\t0\t0\tI-<paragraph>\n" +
            "mechanism\tmechanism\tm\tme\tmec\tmech\tm\tsm\tism\tnism\tBLOCKIN\tLINEIN\tALIGNEDLEFT\tSAMEFONT\tSAMEFONTSIZE\t0\t0\tNOCAPS\tNODIGIT\t0\tNOPUNCT\t0\t4\t0\tNUMBER\t0\t0\t<paragraph>\n" +
            "for\tfor\tf\tfo\tfor\tfor\tr\tor\tfor\tfor\tBLOCKIN\tLINEIN\tALIGNEDLEFT\tSAMEFONT\tSAMEFONTSIZE\t0\t0\tNOCAPS\tNODIGIT\t0\tNOPUNCT\t0\t4\t0\tNUMBER\t0\t0\t<paragraph>\n" +
            "superconductivity\tsuperconductivity\ts\tsu\tsup\tsupe\ty\tty\tity\tvity\tBLOCKIN\tLINEIN\tALIGNEDLEFT\tSAMEFONT\tSAMEFONTSIZE\t0\t0\tNOCAPS\tNODIGIT\t0\tNOPUNCT\t0\t4\t0\tNUMBER\t0\t0\t<paragraph>\n" +
            "FIG\tfig\tF\tFI\tFIG\tFIG\tG\tIG\tFIG\tFIG\tBLOCKSTART\tLINESTART\tLINEINDENT\tNEWFONT\tHIGHERFONT\t0\t0\tALLCAP\tNODIGIT\t0\tNOPUNCT\t10\t3\t0\tNUMBER\t0\t0\tI-<figure>\n" +
            ".\t.\t.\t.\t.\t.\t.\t.\t.\t.\tBLOCKIN\tLINEIN\tLINEINDENT\tSAMEFONT\tSAMEFONTSIZE\t0\t0\tALLCAP\tNODIGIT\t1\tDOT\t10\t3\t0\tNUMBER\t0\t0\t<figure>\n" +
            "1\t1\t1\t1\t1\t1\t1\t1\t1\t1\tBLOCKIN\tLINEIN\tLINEINDENT\tSAMEFONT\tSAMEFONTSIZE\t0\t0\tNOCAPS\tALLDIGIT\t1\tNOPUNCT\t10\t3\t0\tNUMBER\t1\t0\t<figure>\n" +
            ".\t.\t.\t.\t.\t.\t.\t.\t.\t.\tBLOCKIN\tLINEIN\tLINEINDENT\tSAMEFONT\tSAMEFONTSIZE\t0\t0\tALLCAP\tNODIGIT\t1\tDOT\t10\t3\t0\tNUMBER\t0\t0\t<figure>\n" +
            "λ\tλ\tλ\tλ\tλ\tλ\tλ\tλ\tλ\tλ\tBLOCKIN\tLINEIN\tLINEINDENT\tNEWFONT\tSAMEFONTSIZE\t0\t0\tNOCAPS\tNODIGIT\t1\tNOPUNCT\t10\t3\t0\tNUMBER\t0\t0\t<figure>\n" +
            "(\t(\t(\t(\t(\t(\t(\t(\t(\t(\tBLOCKIN\tLINEIN\tLINEINDENT\tSAMEFONT\tSAMEFONTSIZE\t0\t0\tALLCAP\tNODIGIT\t1\tOPENBRACKET\t10\t3\t0\tNUMBER\t0\t0\t<figure>\n" +
            "T\tt\tT\tT\tT\tT\tT\tT\tT\tT\tBLOCKIN\tLINEIN\tLINEINDENT\tSAMEFONT\tSAMEFONTSIZE\t0\t0\tALLCAP\tNODIGIT\t1\tNOPUNCT\t10\t3\t0\tNUMBER\t0\t0\t<figure>\n" +
            ")\t)\t)\t)\t)\t)\t)\t)\t)\t)\tBLOCKIN\tLINEIN\tLINEINDENT\tNEWFONT\tSAMEFONTSIZE\t0\t0\tALLCAP\tNODIGIT\t1\tENDBRACKET\t10\t3\t0\tNUMBER\t0\t0\t<figure>\n" +
            "vs\tvs\tv\tvs\tvs\tvs\ts\tvs\tvs\tvs\tBLOCKIN\tLINEIN\tLINEINDENT\tSAMEFONT\tSAMEFONTSIZE\t0\t0\tNOCAPS\tNODIGIT\t0\tNOPUNCT\t10\t3\t0\tNUMBER\t0\t0\tI-<figure>\n" +
            ".\t.\t.\t.\t.\t.\t.\t.\t.\t.\tBLOCKIN\tLINEEND\tLINEINDENT\tSAMEFONT\tSAMEFONTSIZE\t0\t0\tALLCAP\tNODIGIT\t1\tDOT\t10\t3\t0\tNUMBER\t0\t0\t<figure>\n" +
            "T\tt\tT\tT\tT\tT\tT\tT\tT\tT\tBLOCKIN\tLINESTART\tLINEINDENT\tNEWFONT\tSAMEFONTSIZE\t0\t0\tALLCAP\tNODIGIT\t1\tNOPUNCT\t10\t3\t0\tNUMBER\t0\t0\t<figure>\n" +
            "for\tfor\tf\tfo\tfor\tfor\tr\tor\tfor\tfor\tBLOCKIN\tLINEIN\tLINEINDENT\tNEWFONT\tSAMEFONTSIZE\t0\t0\tNOCAPS\tNODIGIT\t0\tNOPUNCT\t10\t3\t0\tNUMBER\t0\t0\t<figure>\n" +
            "YBCO\tybco\tY\tYB\tYBC\tYBCO\tO\tCO\tBCO\tYBCO\tBLOCKIN\tLINEIN\tLINEINDENT\tSAMEFONT\tSAMEFONTSIZE\t0\t0\tALLCAP\tNODIGIT\t0\tNOPUNCT\t10\t3\t0\tNUMBER\t0\t0\t<figure>\n\n";


        Pair<String, String> stringStringPair = target.processTrainingDataFigures(rese, tokens, "123");

        String tei = stringStringPair.getLeft();
        String tokenisation = stringStringPair.getRight();
        List<String> output = new ArrayList<>();
        for (String block : tokenisation.split("\n\n\n")) {
            String collect = Arrays.stream(block.split("\n")).map(l -> l.split("\t")[0]).collect(Collectors.joining(" "));
            if (StringUtils.isNotBlank(collect)) {
                output.add(collect);
            }
        }

        assertThat(output, hasSize(2));
        assertThat(output.get(0), is("FIG . 1 . λ ( T )"));
        assertThat(output.get(1), is("vs . T for YBCO"));
        assertThat(tokenisation.split("\n").length, is(15));

    }

    @Test
    public void testProcessTrainingDataTables_single_table() throws Exception {
        String text = "The mechanism for superconductivity FIG. 1. λ(T) vs . T for YBCO";
        List<LayoutToken> tokens = GrobidAnalyzer.getInstance().tokenizeWithLayoutToken(text);
        String rese = "The\tthe\tT\tTh\tThe\tThe\te\the\tThe\tThe\tBLOCKSTART\tLINESTART\tALIGNEDLEFT\tNEWFONT\tHIGHERFONT\t0\t0\tINITCAP\tNODIGIT\t0\tNOPUNCT\t0\t4\t0\tNUMBER\t0\t0\tI-<paragraph>\n" +
            "mechanism\tmechanism\tm\tme\tmec\tmech\tm\tsm\tism\tnism\tBLOCKIN\tLINEIN\tALIGNEDLEFT\tSAMEFONT\tSAMEFONTSIZE\t0\t0\tNOCAPS\tNODIGIT\t0\tNOPUNCT\t0\t4\t0\tNUMBER\t0\t0\t<paragraph>\n" +
            "for\tfor\tf\tfo\tfor\tfor\tr\tor\tfor\tfor\tBLOCKIN\tLINEIN\tALIGNEDLEFT\tSAMEFONT\tSAMEFONTSIZE\t0\t0\tNOCAPS\tNODIGIT\t0\tNOPUNCT\t0\t4\t0\tNUMBER\t0\t0\t<paragraph>\n" +
            "superconductivity\tsuperconductivity\ts\tsu\tsup\tsupe\ty\tty\tity\tvity\tBLOCKIN\tLINEIN\tALIGNEDLEFT\tSAMEFONT\tSAMEFONTSIZE\t0\t0\tNOCAPS\tNODIGIT\t0\tNOPUNCT\t0\t4\t0\tNUMBER\t0\t0\t<paragraph>\n" +
            "FIG\tfig\tF\tFI\tFIG\tFIG\tG\tIG\tFIG\tFIG\tBLOCKSTART\tLINESTART\tLINEINDENT\tNEWFONT\tHIGHERFONT\t0\t0\tALLCAP\tNODIGIT\t0\tNOPUNCT\t10\t3\t0\tNUMBER\t0\t0\tI-<table>\n" +
            ".\t.\t.\t.\t.\t.\t.\t.\t.\t.\tBLOCKIN\tLINEIN\tLINEINDENT\tSAMEFONT\tSAMEFONTSIZE\t0\t0\tALLCAP\tNODIGIT\t1\tDOT\t10\t3\t0\tNUMBER\t0\t0\t<table>\n" +
            "1\t1\t1\t1\t1\t1\t1\t1\t1\t1\tBLOCKIN\tLINEIN\tLINEINDENT\tSAMEFONT\tSAMEFONTSIZE\t0\t0\tNOCAPS\tALLDIGIT\t1\tNOPUNCT\t10\t3\t0\tNUMBER\t1\t0\t<table>\n" +
            ".\t.\t.\t.\t.\t.\t.\t.\t.\t.\tBLOCKIN\tLINEIN\tLINEINDENT\tSAMEFONT\tSAMEFONTSIZE\t0\t0\tALLCAP\tNODIGIT\t1\tDOT\t10\t3\t0\tNUMBER\t0\t0\t<table>\n" +
            "λ\tλ\tλ\tλ\tλ\tλ\tλ\tλ\tλ\tλ\tBLOCKIN\tLINEIN\tLINEINDENT\tNEWFONT\tSAMEFONTSIZE\t0\t0\tNOCAPS\tNODIGIT\t1\tNOPUNCT\t10\t3\t0\tNUMBER\t0\t0\t<table>\n" +
            "(\t(\t(\t(\t(\t(\t(\t(\t(\t(\tBLOCKIN\tLINEIN\tLINEINDENT\tSAMEFONT\tSAMEFONTSIZE\t0\t0\tALLCAP\tNODIGIT\t1\tOPENBRACKET\t10\t3\t0\tNUMBER\t0\t0\t<table>\n" +
            "T\tt\tT\tT\tT\tT\tT\tT\tT\tT\tBLOCKIN\tLINEIN\tLINEINDENT\tSAMEFONT\tSAMEFONTSIZE\t0\t0\tALLCAP\tNODIGIT\t1\tNOPUNCT\t10\t3\t0\tNUMBER\t0\t0\t<table>\n" +
            ")\t)\t)\t)\t)\t)\t)\t)\t)\t)\tBLOCKIN\tLINEIN\tLINEINDENT\tNEWFONT\tSAMEFONTSIZE\t0\t0\tALLCAP\tNODIGIT\t1\tENDBRACKET\t10\t3\t0\tNUMBER\t0\t0\t<table>\n" +
            "vs\tvs\tv\tvs\tvs\tvs\ts\tvs\tvs\tvs\tBLOCKIN\tLINEIN\tLINEINDENT\tSAMEFONT\tSAMEFONTSIZE\t0\t0\tNOCAPS\tNODIGIT\t0\tNOPUNCT\t10\t3\t0\tNUMBER\t0\t0\t<table>\n" +
            ".\t.\t.\t.\t.\t.\t.\t.\t.\t.\tBLOCKIN\tLINEEND\tLINEINDENT\tSAMEFONT\tSAMEFONTSIZE\t0\t0\tALLCAP\tNODIGIT\t1\tDOT\t10\t3\t0\tNUMBER\t0\t0\t<table>\n" +
            "T\tt\tT\tT\tT\tT\tT\tT\tT\tT\tBLOCKIN\tLINESTART\tLINEINDENT\tNEWFONT\tSAMEFONTSIZE\t0\t0\tALLCAP\tNODIGIT\t1\tNOPUNCT\t10\t3\t0\tNUMBER\t0\t0\t<table>\n" +
            "for\tfor\tf\tfo\tfor\tfor\tr\tor\tfor\tfor\tBLOCKIN\tLINEIN\tLINEINDENT\tNEWFONT\tSAMEFONTSIZE\t0\t0\tNOCAPS\tNODIGIT\t0\tNOPUNCT\t10\t3\t0\tNUMBER\t0\t0\t<table>\n" +
            "YBCO\tybco\tY\tYB\tYBC\tYBCO\tO\tCO\tBCO\tYBCO\tBLOCKIN\tLINEIN\tLINEINDENT\tSAMEFONT\tSAMEFONTSIZE\t0\t0\tALLCAP\tNODIGIT\t0\tNOPUNCT\t10\t3\t0\tNUMBER\t0\t0\t<table>\n\n";


        Pair<String, String> stringStringPair = target.processTrainingDataTables(rese, tokens, "123");

        String tei = stringStringPair.getLeft();
        String tokenisation = stringStringPair.getRight();
        String reconstructedText = Arrays.stream(tokenisation.split("\n")).map(l -> l.split("\t")[0]).collect(Collectors.joining(" "));

        assertThat(reconstructedText, is("FIG . 1 . λ ( T ) vs . T for YBCO"));
        assertThat(tokenisation.split("\n").length, is(13));

    }

    @Test
    public void testProcessTrainingDataTable_multiple_tables() throws Exception {
        String text = "The mechanism for superconductivity FIG. 1. λ(T) vs . T for YBCO";
        List<LayoutToken> tokens = GrobidAnalyzer.getInstance().tokenizeWithLayoutToken(text);
        String rese = "The\tthe\tT\tTh\tThe\tThe\te\the\tThe\tThe\tBLOCKSTART\tLINESTART\tALIGNEDLEFT\tNEWFONT\tHIGHERFONT\t0\t0\tINITCAP\tNODIGIT\t0\tNOPUNCT\t0\t4\t0\tNUMBER\t0\t0\tI-<paragraph>\n" +
            "mechanism\tmechanism\tm\tme\tmec\tmech\tm\tsm\tism\tnism\tBLOCKIN\tLINEIN\tALIGNEDLEFT\tSAMEFONT\tSAMEFONTSIZE\t0\t0\tNOCAPS\tNODIGIT\t0\tNOPUNCT\t0\t4\t0\tNUMBER\t0\t0\t<paragraph>\n" +
            "for\tfor\tf\tfo\tfor\tfor\tr\tor\tfor\tfor\tBLOCKIN\tLINEIN\tALIGNEDLEFT\tSAMEFONT\tSAMEFONTSIZE\t0\t0\tNOCAPS\tNODIGIT\t0\tNOPUNCT\t0\t4\t0\tNUMBER\t0\t0\t<paragraph>\n" +
            "superconductivity\tsuperconductivity\ts\tsu\tsup\tsupe\ty\tty\tity\tvity\tBLOCKIN\tLINEIN\tALIGNEDLEFT\tSAMEFONT\tSAMEFONTSIZE\t0\t0\tNOCAPS\tNODIGIT\t0\tNOPUNCT\t0\t4\t0\tNUMBER\t0\t0\t<paragraph>\n" +
            "FIG\tfig\tF\tFI\tFIG\tFIG\tG\tIG\tFIG\tFIG\tBLOCKSTART\tLINESTART\tLINEINDENT\tNEWFONT\tHIGHERFONT\t0\t0\tALLCAP\tNODIGIT\t0\tNOPUNCT\t10\t3\t0\tNUMBER\t0\t0\tI-<table>\n" +
            ".\t.\t.\t.\t.\t.\t.\t.\t.\t.\tBLOCKIN\tLINEIN\tLINEINDENT\tSAMEFONT\tSAMEFONTSIZE\t0\t0\tALLCAP\tNODIGIT\t1\tDOT\t10\t3\t0\tNUMBER\t0\t0\t<table>\n" +
            "1\t1\t1\t1\t1\t1\t1\t1\t1\t1\tBLOCKIN\tLINEIN\tLINEINDENT\tSAMEFONT\tSAMEFONTSIZE\t0\t0\tNOCAPS\tALLDIGIT\t1\tNOPUNCT\t10\t3\t0\tNUMBER\t1\t0\t<table>\n" +
            ".\t.\t.\t.\t.\t.\t.\t.\t.\t.\tBLOCKIN\tLINEIN\tLINEINDENT\tSAMEFONT\tSAMEFONTSIZE\t0\t0\tALLCAP\tNODIGIT\t1\tDOT\t10\t3\t0\tNUMBER\t0\t0\t<table>\n" +
            "λ\tλ\tλ\tλ\tλ\tλ\tλ\tλ\tλ\tλ\tBLOCKIN\tLINEIN\tLINEINDENT\tNEWFONT\tSAMEFONTSIZE\t0\t0\tNOCAPS\tNODIGIT\t1\tNOPUNCT\t10\t3\t0\tNUMBER\t0\t0\t<table>\n" +
            "(\t(\t(\t(\t(\t(\t(\t(\t(\t(\tBLOCKIN\tLINEIN\tLINEINDENT\tSAMEFONT\tSAMEFONTSIZE\t0\t0\tALLCAP\tNODIGIT\t1\tOPENBRACKET\t10\t3\t0\tNUMBER\t0\t0\t<table>\n" +
            "T\tt\tT\tT\tT\tT\tT\tT\tT\tT\tBLOCKIN\tLINEIN\tLINEINDENT\tSAMEFONT\tSAMEFONTSIZE\t0\t0\tALLCAP\tNODIGIT\t1\tNOPUNCT\t10\t3\t0\tNUMBER\t0\t0\t<table>\n" +
            ")\t)\t)\t)\t)\t)\t)\t)\t)\t)\tBLOCKIN\tLINEIN\tLINEINDENT\tNEWFONT\tSAMEFONTSIZE\t0\t0\tALLCAP\tNODIGIT\t1\tENDBRACKET\t10\t3\t0\tNUMBER\t0\t0\t<table>\n" +
            "vs\tvs\tv\tvs\tvs\tvs\ts\tvs\tvs\tvs\tBLOCKIN\tLINEIN\tLINEINDENT\tSAMEFONT\tSAMEFONTSIZE\t0\t0\tNOCAPS\tNODIGIT\t0\tNOPUNCT\t10\t3\t0\tNUMBER\t0\t0\tI-<table>\n" +
            ".\t.\t.\t.\t.\t.\t.\t.\t.\t.\tBLOCKIN\tLINEEND\tLINEINDENT\tSAMEFONT\tSAMEFONTSIZE\t0\t0\tALLCAP\tNODIGIT\t1\tDOT\t10\t3\t0\tNUMBER\t0\t0\t<table>\n" +
            "T\tt\tT\tT\tT\tT\tT\tT\tT\tT\tBLOCKIN\tLINESTART\tLINEINDENT\tNEWFONT\tSAMEFONTSIZE\t0\t0\tALLCAP\tNODIGIT\t1\tNOPUNCT\t10\t3\t0\tNUMBER\t0\t0\t<table>\n" +
            "for\tfor\tf\tfo\tfor\tfor\tr\tor\tfor\tfor\tBLOCKIN\tLINEIN\tLINEINDENT\tNEWFONT\tSAMEFONTSIZE\t0\t0\tNOCAPS\tNODIGIT\t0\tNOPUNCT\t10\t3\t0\tNUMBER\t0\t0\t<table>\n" +
            "YBCO\tybco\tY\tYB\tYBC\tYBCO\tO\tCO\tBCO\tYBCO\tBLOCKIN\tLINEIN\tLINEINDENT\tSAMEFONT\tSAMEFONTSIZE\t0\t0\tALLCAP\tNODIGIT\t0\tNOPUNCT\t10\t3\t0\tNUMBER\t0\t0\t<table>\n\n";


        Pair<String, String> stringStringPair = target.processTrainingDataTables(rese, tokens, "123");

        String tei = stringStringPair.getLeft();
        String tokenisation = stringStringPair.getRight();
        List<String> output = new ArrayList<>();
        for (String block : tokenisation.split("\n\n\n")) {
            String collect = Arrays.stream(block.split("\n")).map(l -> l.split("\t")[0]).collect(Collectors.joining(" "));
            if (StringUtils.isNotBlank(collect)) {
                output.add(collect);
            }
        }

        assertThat(output, hasSize(2));
        assertThat(output.get(0), is("FIG . 1 . λ ( T )"));
        assertThat(output.get(1), is("vs . T for YBCO"));
        assertThat(tokenisation.split("\n").length, is(15));

    }

//    @Test
//    public void testProcess2() throws Exception {
//        String text = "(a) shows the temperature variation of the 31 P-\n" +
//            "NMR spectrum for x ¼ 0:25, which was obtained by \n" +
//            "sweeping magnetic fields. A single sharp spectrum was \n" +
//            "observed above T N , but no anomaly was detected in the NMR spectrum at the structural transition T S determined by \n" +
//            "xx . Below T N , a broad NMR spectrum with a Gaussian \n" +
//            "shape develops gradually and coexists with a sharp peak at \n" +
//            "around T on \n" +
//            "c $ 30 K. We measured 1=T 1 at the sharp and \n" +
//            "broad peaks shown by the solid black and dashed red arrows, \n" +
//            "respectively. ";
//
//        Document documentMock = createMock(Document.class);
//        List<LayoutToken> layoutTokens = GrobidAnalyzer.getInstance().tokenizeWithLayoutToken(text);
//
//        target.processShort2(layoutTokens, documentMock);
//
//    }

    /*@Test
    public void testGetDocumentPieces1() throws Exception {
        Document documentMock = createMock(Document.class);

        List<LayoutToken> sentence1 = GrobidAnalyzer.getInstance().tokenizeWithLayoutToken("This is a sentence");

        List<LayoutToken> sentence2 = GrobidAnalyzer.getInstance().tokenizeWithLayoutToken("This is another sentence, somewhere else.");

        // Faking block pointers
        sentence1.get(0).setBlockPtr(0);
        sentence1.get(6).setBlockPtr(1);

        sentence2.get(0).setBlockPtr(2);
        sentence2.get(12).setBlockPtr(3);

        // First sentence blocks
        Block fakeBlock1_1 = new Block();
        fakeBlock1_1.setStartToken(12345);
        Block fakeBlock1_2 = new Block();
        fakeBlock1_2.setStartToken(12347);

        // Second sentence blocks
        Block fakeBlock2_1 = new Block();
        fakeBlock2_1.setStartToken(25000);
        Block fakeBlock2_2 = new Block();
        fakeBlock2_2.setStartToken(25088);

        List<Block> blocks = new ArrayList<>();
        blocks.add(fakeBlock1_1);
        blocks.add(fakeBlock1_2);
        blocks.add(fakeBlock2_1);
        blocks.add(fakeBlock2_2);


        //Moving this sentence somewhere else
//        sentence1.stream().peek(l -> l.setOffset(l.getOffset() + 12345));
//        sentence2.stream().peek(l -> l.setOffset(l.getOffset() + 25000));

        List<LayoutToken> sentence1Far = sentence1.stream().peek(l -> l.setOffset(l.getOffset() + 12345)).collect(Collectors.toList());
        List<LayoutToken> sentence2Far = sentence2.stream().peek(l -> l.setOffset(l.getOffset() + 25000)).collect(Collectors.toList());

        List<LayoutToken> layoutTokens = new ArrayList<>();
        layoutTokens.addAll(sentence1Far);
        layoutTokens.addAll(sentence2Far);

        expect(documentMock.getBlocks()).andReturn(blocks).anyTimes();
        expect(documentMock.getTokenizations()).andReturn(layoutTokens).anyTimes();


        replay(documentMock);
        SortedSet<DocumentPiece> documentPieces = target.collectPiecesFromLayoutTokens(layoutTokens, documentMock);
        verify(documentMock);

        List<DocumentPiece> documentPieces1 = new ArrayList<>(documentPieces);

        assertThat(documentPieces1, hasSize(2));

        assertThat(documentPieces1.get(0).getLeft().getBlockPtr(), is(0));
        assertThat(documentPieces1.get(0).getLeft().getTokenDocPos(), is(12345));
        assertThat(documentPieces1.get(0).getRight().getBlockPtr(), is(1));
        assertThat(documentPieces1.get(0).getRight().getTokenDocPos(), is(12347));

        assertThat(documentPieces1.get(1).getLeft().getBlockPtr(), is(2));
        assertThat(documentPieces1.get(1).getLeft().getTokenDocPos(), is(25000));
        assertThat(documentPieces1.get(1).getRight().getBlockPtr(), is(3));
        assertThat(documentPieces1.get(1).getRight().getTokenDocPos(), is(25088));
    }*/

}