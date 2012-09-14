package org.grobid.core.layout;

/**
 * Class for representing the layout information associated to a PDF object.
 *
 * @author Patrice Lopez
 */
public class LayoutToken {
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
    private boolean rotation = false;

    public LayoutToken() {
    }

    public void setFont(String f) {
        font = f;
    }

    public String getFont() {
        return font;
    }

    public void setText(String f) {
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

}	