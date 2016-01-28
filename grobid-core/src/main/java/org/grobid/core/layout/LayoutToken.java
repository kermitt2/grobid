package org.grobid.core.layout;

/**
 * Class for representing the layout information associated to a PDF object.
 *
 * @author Patrice Lopez
 */
public class LayoutToken {
    private String text = null;
    public double y = -1.0;
    public double x = -1.0;
    public double width = 0.0;
    public double height = 0.0;
    private String font = null;
    private boolean bold = false;
    private boolean italic = false;
    private String colorFont = null;
    public double fontSize = 0.0;
    private boolean rotation = false;
    private int page = -1;
    private boolean newLineAfter;
    private int blockPtr;

    public LayoutToken() {
    }

    public LayoutToken(String text) {
        this.text = text;
    }

    public void setFont(String f) {
        font = f;
    }

    public String getFont() {
        return font;
    }

    public void setText(String f) {
        //text = f.replaceAll("\n", "");
		text = f;
    }

    public void setRotation(boolean b) {
        rotation = b;
    }

    public boolean getRotation() {
        return rotation;
    }

    public String getText() {
        return text;
    }

    public String t() {
        return text;
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

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public boolean isNewLineAfter() {
        return newLineAfter;
    }

    public void setNewLineAfter(boolean newLineAfter) {
        this.newLineAfter = newLineAfter;
    }

    public int getBlockPtr() {
        return blockPtr;
    }

    public void setBlockPtr(int blockPtr) {
        this.blockPtr = blockPtr;
    }

    @Override
    public String toString() {
        return text;
    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        result = text != null ? text.hashCode() : 0;
        temp = Double.doubleToLongBits(y);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(x);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(width);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(height);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        result = 31 * result + (font != null ? font.hashCode() : 0);
        result = 31 * result + (bold ? 1 : 0);
        result = 31 * result + (italic ? 1 : 0);
        result = 31 * result + (colorFont != null ? colorFont.hashCode() : 0);
        temp = Double.doubleToLongBits(fontSize);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        result = 31 * result + (rotation ? 1 : 0);
        result = 31 * result + page;
        return result;
    }
}