package org.grobid.core.document;

import java.util.ArrayList;

/**
 * Class corresponding to a node of the structure of a hierarchically organized document (i.e. for a table
 * of content).
 *
 * @author Patrice Lopez
 */

public class DocumentNode {
    // Gorn address for tree structure
    public String address = null;

    // real number of the section
    public String realNumber = null;

    public String label = null;
    public ArrayList<DocumentNode> children = null;

    public int startToken = -1;
    public int endToken = -1;

    public DocumentNode father = null;

    public DocumentNode() {
    }

    public DocumentNode(String label, String address) {
        this.label = label;
        this.address = address;
    }

    public void addChild(DocumentNode child) {
        if (this.children == null) {
            this.children = new ArrayList<DocumentNode>();
        }
        String addr = null;
        if (this.address != null) {
            if (this.address.equals("0")) {
                addr = "" + (this.children.size() + 1);
            } else {
                addr = this.address + (this.children.size() + 1);
            }
        }
        child.address = addr;
        child.father = this;
        if (child.endToken > this.endToken) {
            this.endToken = child.endToken;
        }

        this.children.add(child);
    }

    public String toString() {
        return toString(0);
    }

    public String toString(int tab) {
        StringBuffer sb = new StringBuffer();
        sb.append(address + " " + label + " " + startToken + " " + endToken + "\n");

        if (children != null) {
            for (DocumentNode node : children) {
                for (int n = 0; n < tab + 1; n++) {
                    sb.append("\t");
                }
                sb.append(node.toString(tab + 1));
            }
        }
        return sb.toString();
    }

    public DocumentNode clone() {
        DocumentNode result = new DocumentNode();
        result.address = this.address;
        result.realNumber = this.realNumber;
        result.label = this.label;
        result.startToken = this.startToken;
        result.endToken = this.endToken;
        return result;
    }

    public DocumentNode getSpanningNode(int position) {
        if ((startToken <= position) && (endToken >= position)) {
            if (children != null) {
                for (DocumentNode node : children) {
                    if ((node.startToken <= position) && (node.endToken >= position)) {
                        return node.getSpanningNode(position);
                    }
                }
                return this;
            } else {
                return this;
            }
        } else {
            return null;
        }
    }

    /*public DocumentNode nextSlibing() {
         if ( (children != null) && (children.size() > 0) ) {
             return children.get(0);
         }
         else if (father == null) {
             return null;
         }
         else {
             for (DocumentNode node : father.children) {

             }
         }
     }*/
}

