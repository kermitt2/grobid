package org.grobid.core.tokenization;

import org.grobid.core.GrobidModels;
import org.grobid.core.layout.LayoutToken;
import org.grobid.core.utilities.LayoutTokensUtil;
import org.grobid.core.utilities.Pair;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Created by zholudev on 07/04/16.
 * Testing synchronization
 */
public class TaggingTokenSynchronizerTest {
    public static final String P = "<paragraph>";
    public static final String F = "<figure>";

    @Test
    public void testBasic() {
        TaggingTokenSynchronizer synchronizer = new TaggingTokenSynchronizer(GrobidModels.FULLTEXT,
                generateResult(p("This", P), p("Figure", F)), toks("This", " ", "Figure")
        );

        int cnt = 0;
        boolean spacesPresent = false;
        for (LabeledTokensContainer el : synchronizer) {
            String text = LayoutTokensUtil.toText(el.getLayoutTokens());
            assertFalse(text.startsWith(" "));
            if (text.contains(" ")) {
                spacesPresent = true;
            }
            System.out.println("\"" + text + "\"");
            cnt++;
        }

        assertEquals(2, cnt);
        assertTrue(spacesPresent);
    }

    @Test
    public void testFailure() {
        TaggingTokenSynchronizer synchronizer = new TaggingTokenSynchronizer(GrobidModels.FULLTEXT,
                generateResult(p("This", P), p("Figure", F)), toks("This", " ", "Fig")
        );

        try {
            for (LabeledTokensContainer el : synchronizer) {
                String text = LayoutTokensUtil.toText(el.getLayoutTokens());
                System.out.println("\"" + text + "\"");
            }
            fail();
        } catch (IllegalStateException e) {
            //no op
        }

    }

    private static String generateResult(Pair<String, String>... tokens) {
        StringBuilder res = new StringBuilder();
        for (Pair<String, String> p : tokens) {
            res.append(p.a).append("\t").append(p.b).append("\n");
        }
        return res.toString();
    }

    private static List<LayoutToken> toks(String... toks) {
        List<LayoutToken> res = new ArrayList<>();
        for (String t : toks) {
            res.add(new LayoutToken(t));
        }
        return res;
    }

    private static Pair<String, String> p(String tok, String label) {
        return new Pair<>(tok, label);
    }
}
