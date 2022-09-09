package org.grobid.core.document;

import org.grobid.core.analyzers.GrobidAnalyzer;
import org.grobid.core.data.Footnote;
import org.grobid.core.layout.LayoutToken;
import org.grobid.core.utilities.GrobidProperties;
import org.grobid.core.utilities.LayoutTokensUtil;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class TEIFormatterTest {

    @BeforeClass
    public static void setInitialContext() throws Exception {
        GrobidProperties.getInstance();
    }

    @Test
    public void testMakeFootNote() throws Exception {

        String text = "1 This is a footnote";
        List<LayoutToken> tokens = GrobidAnalyzer.getInstance().tokenizeWithLayoutToken(text);

        Footnote footnote = new TEIFormatter(null, null).makeFootNote(tokens, text);

        assertThat(footnote.getText(), is(" This is a footnote"));
        assertThat(LayoutTokensUtil.toText(footnote.getTokens()), is(" This is a footnote"));
        assertThat(footnote.getNumber(), is(1));
    }



}