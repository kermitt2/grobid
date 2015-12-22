package org.grobid.core.utilities.matching;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.base.Predicate;
import com.google.common.base.Splitter;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.ClassicAnalyzer;
import org.apache.lucene.util.Version;
import org.grobid.core.data.BibDataSet;
import org.grobid.core.layout.LayoutToken;
import org.grobid.core.utilities.LayoutTokensUtil;
import org.grobid.core.utilities.Pair;
import org.grobid.core.utilities.counters.CntManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by zholudev on 18/12/15.
 * Matching reference markers to extarcted citations
 */
public class ReferenceMarkerMatcher {
    private static final Logger LOGGER = LoggerFactory.getLogger(ReferenceMarkerMatcher.class);

    public static final Pattern YEAR_PATTERN = Pattern.compile("[12][0-9]{3,3}");
    public static final Pattern AUTHOR_NAME_PATTERN = Pattern.compile("[A-Z][A-Za-z]+");
    private static final Pattern NUMBERED_CITATION_PATTERN = Pattern.compile(" *[\\(\\[]? *(?:\\d+[-––]\\d+,|\\d+, *)*(?:\\d+[-––]\\d+|\\d+)[\\)\\]]? *");
    public static final Pattern SEPARATOR_PATTERN = Pattern.compile(";");
    public static final ClassicAnalyzer ANALYZER = new ClassicAnalyzer(Version.LUCENE_45);
    public static final int MAX_RANGE = 20;
    public static final Pattern NUMBERED_CITATIONS_SPLIT_PATTERN = Pattern.compile("[,;]");

    enum Counters {
        MATCHED_REF_MARKERS,
        UNMATCHED_REF_MARKERS,
        NO_CANDIDATES,
        MANY_CANDIDATES,
        STYLE_AUTHORS,
        STYLE_NUMBERED,
        MATCHED_REF_MARKERS_AFTER_POST_FILTERING, MANY_CANDIDATES_AFTER_POST_FILTERING, NO_CANDIDATES_AFTER_POST_FILTERING, STYLE_OTHER
    }


    public static final Function<String, Object> IDENTITY = new Function<String, Object>() {
        @Override
        public Object apply(String s) {
            return s;
        }
    };
    private final LuceneIndexMatcher<BibDataSet, String> authorMatcher;
    private final LuceneIndexMatcher<BibDataSet, String> labelMatcher;
    private CntManager cntManager;


    public ReferenceMarkerMatcher(List<BibDataSet> bds, CntManager cntManager)
            throws EntityMatcherException {
        this.cntManager = cntManager;

        authorMatcher = new LuceneIndexMatcher<BibDataSet, String>(
                new Function<BibDataSet, Object>() {
                    @Override
                    public Object apply(BibDataSet bibDataSet) {
                        String authorString = bibDataSet.getResBib().getAuthors() + " et al";
                        if (bibDataSet.getResBib().getPublicationDate() != null) {
                            authorString += " " + bibDataSet.getResBib().getPublicationDate();
                        }
//                        System.out.println("Indexing: " + authorString);
                        return authorString;
                    }
                },
                IDENTITY
        );

        authorMatcher.setMustMatchPercentage(1.0);
        authorMatcher.load(bds);

        labelMatcher = new LuceneIndexMatcher<BibDataSet, String>(
                new Function<BibDataSet, Object>() {
                    @Override
                    public Object apply(BibDataSet bibDataSet) {
                        return bibDataSet.getRefSymbol();
                    }
                },
                IDENTITY
        );

        labelMatcher.setMustMatchPercentage(1.0);
        labelMatcher.load(bds);
    }

    public List<Pair<String, BibDataSet>> match(String input, List<LayoutToken> refTokens) throws EntityMatcherException {
        String text = LayoutTokensUtil.dehyphenize(LayoutTokensUtil.enrichWithNewLineInfo(refTokens));

        if (isAuthorCitationStyle(text)) {
            cntManager.i(Counters.STYLE_AUTHORS);
            return matchAuthorCitation(text, refTokens);
        } else if (isNumberedCitationReference(text)) {
            cntManager.i(Counters.STYLE_NUMBERED);
            return matchNumberedCitation(text, refTokens);
        } else {
            cntManager.i(Counters.STYLE_OTHER);
            System.out.println("Other style: " + text);
            return Collections.singletonList(new Pair<String, BibDataSet>(text, null));
        }
    }


    private boolean isAuthorCitationStyle(String text) {
        return YEAR_PATTERN.matcher(text).find() && AUTHOR_NAME_PATTERN.matcher(text).find();
    }

    private static boolean isNumberedCitationReference(String t) {
        return NUMBERED_CITATION_PATTERN.matcher(t).matches();
    }

    private List<Pair<String, BibDataSet>> matchNumberedCitation(String input, List<LayoutToken> refTokens) throws EntityMatcherException {
        List<String> labels = getNumberedLabels(input);
        List<Pair<String, BibDataSet>> results = new ArrayList<Pair<String, BibDataSet>>();
        for (String text : labels) {
            List<BibDataSet> matches = labelMatcher.match(text);
            if (matches.size() == 1) {
                cntManager.i(Counters.MATCHED_REF_MARKERS);
//                System.out.println("MATCHED: " + text + "\n" + matches.get(0).getRefSymbol() + "\n" + matches.get(0).getRawBib());

//                System.out.println("-----------");
                results.add(new Pair<String, BibDataSet>(text, matches.get(0)));
            } else {
                cntManager.i(Counters.UNMATCHED_REF_MARKERS);
                if (matches.size() != 0) {
                    cntManager.i(Counters.MANY_CANDIDATES);
                    System.out.println("MANY CANDIDATES: " + text + "\n" + text + "\n");
                    for (BibDataSet bds : matches) {
                        System.out.println("  " + bds.getRawBib());
                    }

                    System.out.println("----------");
                } else {
                    cntManager.i(Counters.NO_CANDIDATES);
                    System.out.println("NO CANDIDATES: " + text + "\n" + text);
                    System.out.println("++++++++++++");
                }
                results.add(new Pair<String, BibDataSet>(text, null));
            }

        }
        return results;
    }

    private static List<String> getNumberedLabels(String text) {
        List<String> split = Splitter.on(NUMBERED_CITATIONS_SPLIT_PATTERN).omitEmptyStrings().splitToList(text);
        List<String> res = new ArrayList<String>();
        for (String s : split) {
            if (!s.contains("-")) {
                res.add(s);
            } else {
                try {
                    List<String> toks = LuceneUtil.tokenizeString(ANALYZER, s);
                    Integer a;
                    Integer b;
                    if (toks.size() == 1) {
                        String[] sp = toks.get(0).split("-");
                        a = Integer.valueOf(sp[0], 10);
                        b = Integer.valueOf(sp[1], 10);
                    } else if (toks.size() > 1) {
                        a = Integer.valueOf(toks.get(0), 10);
                        b = Integer.valueOf(toks.get(1), 10);
                    } else {
                        continue;
                    }

                    if (a < b && b - a < MAX_RANGE) {
                        for (int i = a; i <= b; i++) {
                            res.add(String.valueOf(i));
                        }
                    }
                } catch (Exception e) {
                    LOGGER.warn("Cannot parse citation reference range: " + s);
                }

            }
        }
        return res;
    }

    private List<Pair<String, BibDataSet>> matchAuthorCitation(String text, List<LayoutToken> refTokens) throws EntityMatcherException {
        Iterable<String> split = splitAuthors(text);
        List<Pair<String, BibDataSet>> results = new ArrayList<Pair<String, BibDataSet>>();
        for (String c : split) {
            List<BibDataSet> matches = authorMatcher.match(c);
            if (matches.size() == 1) {
                cntManager.i(Counters.MATCHED_REF_MARKERS);
//                System.out.println("MATCHED: " + text + "\n" + c + "\n" + matches.get(0).getRawBib());

//                System.out.println("-----------");
                results.add(new Pair<String, BibDataSet>(c, matches.get(0)));
            } else {
                if (matches.size() != 0) {
                    cntManager.i(Counters.MANY_CANDIDATES);
                    List<BibDataSet> filtered = postFilterMatches(c, matches);
                    if (filtered.size() == 1) {
                        results.add(new Pair<String, BibDataSet>(c, filtered.get(0)));
                        cntManager.i(Counters.MATCHED_REF_MARKERS);
                        cntManager.i(Counters.MATCHED_REF_MARKERS_AFTER_POST_FILTERING);
                    } else {
                        cntManager.i(Counters.UNMATCHED_REF_MARKERS);
                        if (filtered.size() == 0) {
                            cntManager.i(Counters.NO_CANDIDATES_AFTER_POST_FILTERING);
                        } else {
                            cntManager.i(Counters.MANY_CANDIDATES_AFTER_POST_FILTERING);
                            System.out.println("MANY CANDIDATES: " + text + "\n" + c + "\n");
                            for (BibDataSet bds : matches) {
                                System.out.println("  " + bds.getRawBib());
                            }

                        }
                    }
//                    System.out.println("----------");
                } else {
                    cntManager.i(Counters.NO_CANDIDATES);
                    System.out.println("NO CANDIDATES: " + text + "\n" + c);
                    System.out.println("++++++++++++");
                }
                results.add(new Pair<String, BibDataSet>(c, null));
            }
        }

        return results;
    }

    // splitting into individual citation references strings like in:
    // Kuwajima et al., 1985; Creighton, 1990; Ptitsyn et al., 1990;
    private static List<String> splitAuthors(String text) {
        List<String> split = Splitter.on(SEPARATOR_PATTERN).splitToList(text);
        List<String> result = new ArrayList<String>();
        for (String s : split) {
            //cases like: Khechinashvili et al. (1973) and Privalov (1979)
            int matchCount = matchCount(s, YEAR_PATTERN);
            if (matchCount == 2 && s.contains(" and ")) {
                for (String ys : Splitter.on(" and ").omitEmptyStrings().splitToList(s)) {
                    result.add(ys);
                }
            } else if (matchCount > 1) {
                Matcher m = YEAR_PATTERN.matcher(s);
                int prev = 0;
                while(m.find()) {
                    result.add(s.substring(prev, m.end()));
                    prev = m.end();
                }

            } else {
                result.add(s);
            }
        }
        return result;
    }

    private static int matchCount(String s, Pattern p) {
        Matcher m = p.matcher(s);
        int cnt = 0;
        while (m.find()) {
            cnt++;
        }
        return cnt;
    }

    //if we match more than 1 citation based on name, then we leave only those citations that have author name first
    private List<BibDataSet> postFilterMatches(String c, List<BibDataSet> matches) {
        String[] sp = c.trim().split(" ");
        final String author = sp[0].toLowerCase();
        return Lists.newArrayList(Iterables.filter(matches, new Predicate<BibDataSet>() {
            @Override
            public boolean apply(BibDataSet bibDataSet) {
                return bibDataSet.getRawBib().trim().toLowerCase().startsWith(author);
            }
        }));
    }

    public static void main(String[] args) {
        Analyzer analyzer = ANALYZER;
//
        List<String> res = LuceneUtil.tokenizeString(analyzer, "Mark & van Gunsteren, 1992");
        for (String r : res) {
            System.out.println("Token: '" + r + "'");
        }
//        System.out.println(res);
        System.out.println(res.size());

//        List<String> res = splitAuthors("Van Kan et al., 1990, Van der Vos et al., 1992, Otte et al., 1992, Kwa et al., 1994b");
//        for (String s : res) {
//            System.out.println(s);
//        }
    }
}