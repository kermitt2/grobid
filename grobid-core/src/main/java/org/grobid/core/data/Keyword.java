package org.grobid.core.data;

import java.util.ArrayList;
import java.util.List;

import org.grobid.core.utilities.TextUtilities;

/**
 * Class for representing a keyword extracted from a publication.
 *
 * @author Patrice Lopez
 */
public class Keyword {
    private String keyword = null;
    private String type = null;
 
 	public Keyword(String key) {
 		keyword = key;
 	}
 
 	public Keyword(String key, String typ) {
 		keyword = key;
		type = typ;
 	}
 
    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String key) {
        keyword = key;
    }

    public String getType() {
        return type;
    }

    public void setType(String typ) {
        type = typ;
    }
	
    public boolean notNull() {
        if (keyword == null)
            return false;
        else
            return true;
    }

    public String toString() {
        String res = "";
        if (keyword != null)
            res += keyword + " ";
        if (type != null) {
            res += " (type:" + type + ")";
        }
        return res.trim();
    }

    public String toTEI() {
        if (keyword == null) {
            return null;
        }
        String res = "<term>" + keyword + "</term>";
        return res;
    }

}