package org.grobid.core.layout;

import java.util.ArrayList;

/**
 * Class for representing and exchanging a document block. A block is defined here relatively to
 * some properties in the document layout.
 *
 * @author Patrice Lopez
 */
public class Block {
    private int nbTokens = 0;
    private String text = null;
    public double y = 0.0;
    public double x = 0.0;
    public double width = 0.0;
    public double height = 0.0;
    private String font = null;
    private boolean bold = false;
    private boolean italic = false;
    private String colorFont = null;
    public double fontSize = 0.0;

    public LayoutToken firstToken = null;
    public LayoutToken lastToken = null;

    public ArrayList<LayoutToken> tokens = null;

    // start position of the block in the original tokenization
    private int startToken = -1;
    // end position of the block in the original tokenization
    private int endToken = -1;

    // the page in the document where the block is located
    private int page = -1;

    public enum Type {BULLET, TABLE, REFERENCE}

    private Type type;

    public Block() {
    }

    public void addToken(LayoutToken lt) {
        if (tokens == null)
            tokens = new ArrayList<LayoutToken>();
        tokens.add(lt);
    }

    public ArrayList<LayoutToken> getTokens() {
        return tokens;
    }

    public void resetTokens() {
        tokens = null;
    }

    public void setType(Type t) {
        type = t;
    }

    public void setText(String t) {
        text = t;
    }

    public void setNbTokens(int t) {
        nbTokens = t;
    }

    public Type getType() {
        return type;
    }

    public String getText() {
        return text;
    }

    public int getNbTokens() {
        return nbTokens;
    }

    public void setFont(String f) {
        font = f;
    }

    public String getFont() {
        return font;
    }

    public void setColorFont(String f) {
        colorFont = f;
    }

    public String getColorFont() {
        return colorFont;
    }

    public void setBold(boolean b) {
        bold = b;
    }

    public void setItalic(boolean i) {
        italic = i;
    }

    public boolean getBold() {
        return bold;
    }

    public boolean getItalic() {
        return italic;
    }

    public void setFontSize(double d) {
        fontSize = d;
    }

    public double getFontSize() {
        return fontSize;
    }

    public void setX(double d) {
        x = d;
    }

    public double getX() {
        return x;
    }

    public void setY(double d) {
        y = d;
    }

    public double getY() {
        return y;
    }

    public void setHeight(double d) {
        height = d;
    }

    public double getHeight() {
        return height;
    }

    public void setWidth(double d) {
        width = d;
    }

    public double getWidth() {
        return width;
    }

    public int getStartToken() {
        return startToken;
    }

    public int getEndToken() {
        return endToken;
    }

    public void setStartToken(int start) {
        startToken = start;
    }

    public void setEndToken(int end) {
        endToken = end;
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    @Override
    public String toString() {
        return "Block{" +
                "nbTokens=" + nbTokens +
                ", startToken=" + startToken +
                ", endToken=" + endToken +
                ", type=" + type +
                '}';
    }
}