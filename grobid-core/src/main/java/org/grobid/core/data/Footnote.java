package org.grobid.core.data;

import org.grobid.core.layout.LayoutToken;
import org.grobid.core.layout.Page;

import java.util.List;

import static com.google.common.collect.Iterables.getLast;

public class Footnote {

    private int number;

    private List<LayoutToken> tokens;

    private String text;

    private int offsetStartInPage;

    private boolean ignored = false;

    public Footnote() {
    }

    public Footnote(int number, List<LayoutToken> tokens, String text) {
        this.number = number;
        this.tokens = tokens;
        this.text = text;
    }

    public Footnote(int number, List<LayoutToken> tokens, String text, int offsetStartInPage) {
        this.number = number;
        this.tokens = tokens;
        this.text = text;
        this.offsetStartInPage = offsetStartInPage;
    }

    public Footnote(int number, List<LayoutToken> tokens) {
        this.number = number;
        this.tokens = tokens;
    }

    public int getOffsetStartInPage() {
        return offsetStartInPage;
    }

    public void setOffsetStartInPage(int offsetStartInPage) {
        this.offsetStartInPage = offsetStartInPage;
    }

    public int getPageNumber() {
        return tokens.get(0).getPage();
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public List<LayoutToken> getTokens() {
        return tokens;
    }

    public void setTokens(List<LayoutToken> tokens) {
        this.tokens = tokens;
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    public int getOffsetEndInPage() {
        return getLast(tokens).getOffset();
    }

    public boolean isIgnored() {
        return ignored;
    }

    public void setIgnored(boolean ignored) {
        this.ignored = ignored;
    }
}
