package org.grobid.core.utilities;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.collect.Iterables;
import com.google.common.collect.Iterators;
import com.google.common.collect.PeekingIterator;
import org.grobid.core.layout.BoundingBox;
import org.grobid.core.layout.LayoutToken;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Created by zholudev on 18/12/15.
 * Dealing with layout tokens
 */
public class LayoutTokensUtil {

    public static final Function<LayoutToken, String> TO_TEXT_FUNCTION = new Function<LayoutToken, String>() {
        @Override
        public String apply(LayoutToken layoutToken) {
            return layoutToken.t();
        }
    };

    public static List<LayoutToken> enrichWithNewLineInfo(List<LayoutToken> toks) {
        PeekingIterator<LayoutToken> tokens = Iterators.peekingIterator(toks.iterator());
        while (tokens.hasNext()) {
            LayoutToken curToken = tokens.next();
            if (tokens.hasNext() && tokens.peek().getText().equals("\n")) {
                curToken.setNewLineAfter(true);
            }
            if (curToken.getText().equals("\n")) {
                curToken.setText(" ");
            }
        }
        return toks;
    }

    public static String normalizeText(String text) {
        return TextUtilities.dehyphenize(text).replace("\n", " ").replaceAll("[ ]{2,}", " ");//.trim();
    }

    public static String normalizeText(List<LayoutToken> tokens) {
        return TextUtilities.dehyphenize(toText(tokens)).replace("\n", " ").replaceAll("[ ]{2,}", " ");//.trim();
    }

    public static String toText(List<LayoutToken> tokens) {
        return Joiner.on("").join(Iterables.transform(tokens, TO_TEXT_FUNCTION));
    }

    public static boolean noCoords(LayoutToken t) {
        return t.getPage() == -1 || t.getWidth() <= 0;
    }

    @Deprecated
    // the same method in TextUtilities should be used
    public static String toTextDehyphenized(List<LayoutToken> tokens) {

        PeekingIterator<LayoutToken> it = Iterators.peekingIterator(tokens.iterator());
        StringBuilder sb = new StringBuilder();
        boolean normalized = false;

        LayoutToken prev = null;
        while (it.hasNext()) {
            LayoutToken cur = it.next();
            //the current token is dash, next is new line, and previous one is some sort of word
            if (cur.isNewLineAfter() && cur.getText().equals("-") && prev != null && !prev.getText().trim().isEmpty()) {
                //skipping new line
                it.next();
                LayoutToken next = it.next();
                if (next.getText().equals("conjugated") || prev.getText().equals("anti")) {
                    sb.append("-");
                }
                sb.append(next);
                normalized = true;
            } else {
                sb.append(cur.getText());
            }
            prev = cur;
        }

        /*if (normalized) {
            System.out.println("NORMALIZED: " + sb.toString());
        }*/
        return sb.toString();
    }

    public static boolean spaceyToken(String tok) {
        return (tok.equals(" ")
                || tok.equals("\u00A0")
                || tok.equals("\n"));
    }

    public static boolean newLineToken(String tok) {
        return (tok.equals("\n") || tok.equals("\r") || tok.equals("\n\r"));
    }

    public static String removeSpecialVariables(String tok) {
        if (tok.equals("@BULLET")) {
            tok = "•";
        }
        return tok;
    }


    public static boolean containsToken(List<LayoutToken> toks, String text) {
        for (LayoutToken t : toks) {
            if (text.equals(t.t())) {
                return true;
            }
        }
        return false;
    }

    public static int tokenPos(List<LayoutToken> toks, String text) {
        int cnt = 0;
        for (LayoutToken t : toks) {
            if (text.equals(t.t())) {
                return cnt;
            }
            cnt++;
        }
        return -1;
    }

    public static int tokenPos(List<LayoutToken> toks, Pattern p) {
        int cnt = 0;
        for (LayoutToken t : toks) {
            if (p.matcher(t.t()).matches()) {
                return cnt;
            }
            cnt++;
        }
        return -1;
    }

//    public static List<List<LayoutToken>> split(List<LayoutToken> toks, Pattern p) {
//        return split(toks, p, false);
//    }

    public static List<List<LayoutToken>> split(List<LayoutToken> toks, Pattern p, boolean preserveSeparator) {
        return split(toks, p, preserveSeparator, true);
    }

    public static List<List<LayoutToken>> split(List<LayoutToken> toks, Pattern p, boolean preserveSeparator, boolean preserveLeftOvers) {
        List<List<LayoutToken>> split = new ArrayList<>();
        List<LayoutToken> curToks = new ArrayList<>();
        for (LayoutToken tok : toks) {
            if (p.matcher(tok.t()).matches()) {
                if (preserveSeparator) {
                    curToks.add(tok);
                }
                split.add(curToks);
                curToks = new ArrayList<>();
            } else {
                curToks.add(tok);
            }
        }
        if (preserveLeftOvers) {
            if (!curToks.isEmpty()) {
                split.add(curToks);
            }
        }
        return split;
    }


    public static boolean tooFarAwayVertically(List<BoundingBox> boxes, double distance) {
        if (boxes == null) {
            return false;
        }
        for (int i = 0; i < boxes.size() - 1; i++) {
            if (boxes.get(i).verticalDistanceTo(boxes.get(i + 1)) > distance) {
                return true;
            }
        }
        return false;
    }

    public static String getCoordsString(List<LayoutToken> toks) {
        List<BoundingBox> res = BoundingBoxCalculator.calculate(toks);
        return Joiner.on(";").join(res);
    }

}
