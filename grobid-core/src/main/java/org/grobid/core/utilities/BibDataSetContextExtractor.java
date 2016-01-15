package org.grobid.core.utilities;

import com.google.common.collect.ListMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;
import net.sf.saxon.om.Item;
import net.sf.saxon.om.SequenceIterator;
import net.sf.saxon.trans.XPathException;
import org.apache.commons.io.IOUtils;
import org.grobid.core.data.BibDataSetContext;

import java.io.IOException;
import java.io.InputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by zholudev on 07/01/16.
 * Extracting citation contexts
 */
public class BibDataSetContextExtractor {
    public static final Pattern REF_PATTERN = Pattern.compile("<ref>(.*)</ref>", Pattern.DOTALL);
    public static final int CUT_DEFAULT_LENGTH = 50;

    static {
        InputStream is = BibDataSetContextExtractor.class.getResourceAsStream("/xq/get-citation-context-from-tei.xq");
        try {
            CONTEXT_EXTRACTION_XQ = IOUtils.toString(is);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        IOUtils.closeQuietly(is);

    }

    private static final String CONTEXT_EXTRACTION_XQ;

    static <K extends Comparable<? super K>, V> ListMultimap<K, V> multimap() {
        return MultimapBuilder.treeKeys().linkedListValues().build();
    }

    protected static String cutContextSimple(String cont) {
        Matcher m = REF_PATTERN.matcher(cont);
        if (m.find()) {
            String g = m.group(1);
            int index = m.start();

            return cont.substring(Math.max(0, index - CUT_DEFAULT_LENGTH), Math.min(cont.length(),
                    index + g.length() + CUT_DEFAULT_LENGTH));
        } else {
            throw new IllegalStateException("Implementation error: no <ref> found in" + cont);
        }
    }

    public static Multimap<String, BibDataSetContext> getCitationReferences(String tei) throws XPathException, IOException {
        XQueryProcessor xQueryProcessor = new XQueryProcessor(tei);

        SequenceIterator it = xQueryProcessor.getSequenceIterator(CONTEXT_EXTRACTION_XQ);

        Item item;
        Multimap<String, BibDataSetContext> contexts = multimap();

        while ((item = it.next()) != null) {
            String val = item.getStringValue();
            String citationTeiId = it.next().getStringValue();
            String sectionName = it.next().getStringValue();
            double pos = Double.parseDouble(it.next().getStringValue());
            String coords = it.next().getStringValue();

            BibDataSetContext pcc = new BibDataSetContext();
            String context = cutContextSimple(val);

            pcc.setContext(extractContextSentence(context));
            pcc.setDocumentCoords(coords);
            pcc.setTeiId(citationTeiId);

            contexts.put(citationTeiId, pcc);
        }
        return contexts;
    }

    private static String extractContextSentence(String cont) {
        Matcher m = REF_PATTERN.matcher(cont);
        if (m.find()) {
            String g = m.group(1);
            return m.replaceAll(g);
        } else {
            throw new IllegalStateException("Implementation error: no <ref> found in" + cont);
        }
    }


}
