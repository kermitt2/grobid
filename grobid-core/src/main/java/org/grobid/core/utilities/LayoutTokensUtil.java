package org.grobid.core.utilities;

import com.google.common.collect.Iterators;
import com.google.common.collect.PeekingIterator;
import org.grobid.core.layout.LayoutToken;

import java.util.List;

/**
 * Created by zholudev on 18/12/15.
 * Dealing with layout tokens
 */
public class LayoutTokensUtil {

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

    public static String dehyphenize(List<LayoutToken> tokens) {

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

        if (normalized) {
            System.out.println("NORMALIZED: " + sb.toString());
        }
        return sb.toString();
    }




}
