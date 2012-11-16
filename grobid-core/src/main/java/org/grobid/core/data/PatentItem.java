package org.grobid.core.data;

/**
 * Class for managing patent bibliographical references.
 *
 * @author Patrice Lopez
 */
public class PatentItem implements Comparable<PatentItem> {
    // attribute
    private String authority = null;
    private String number = null;
    private String kindCode = null;

    // patent type when applicable
    private Boolean application = false;
    private Boolean provisional = false;
    private Boolean reissued = false;
    private Boolean plant = false;
    private Boolean design = false;

    // scores
    private double conf = 0.0;
    private String confidence = null;

    // position in document
    private int offset_begin = 0;
    private int offset_end = 0;

    // position in raw string (in case of factorised numbers)
    private int offset_raw = 0;

    // context of occurrence of the reference
    private String context = null;

    public String getAuthority() {
        return authority;
    }

    public String getNumber() {
        return number;
    }

    public String getKindCode() {
        return kindCode;
    }

    public Boolean getApplication() {
        return application;
    }

    public Boolean getProvisional() {
        return provisional;
    }

    public Boolean getReissued() {
        return reissued;
    }

    public Boolean getPlant() {
        return plant;
    }

    public Boolean getDesign() {
        return design;
    }

    public double getConf() {
        return conf;
    }

    public String getConfidence() {
        return confidence;
    }

    public int getOffsetBegin() {
        return offset_begin;
    }

    public int getOffsetEnd() {
        return offset_end;
    }

    public int getOffsetRaw() {
        return offset_raw;
    }

    /**
     * Context of occurrence of the reference
     */
    public String getContext() {
        return context;
    }

    public void setContext(String cont) {
        context = cont;
    }

    public void setOffsetBegin(int ofs) {
        offset_begin = ofs;
    }

    public void setOffsetEnd(int ofs) {
        offset_end = ofs;
    }

    public void setOffsetRaw(int ofs) {
        offset_raw = ofs;
    }

    public void setKindCode(String kc) {
        kindCode = kc;
    }

    public void setNumber(String num) {
        number = num;
    }

    public void setAuthority(String s) {
        authority = s;
    }

    public void setApplication(boolean b) {
        application = b;
    }

    public void setProvisional(boolean b) {
        provisional = b;
    }

    public void setReissued(boolean b) {
        reissued = b;
    }

    public void setPlant(boolean b) {
        plant = b;
    }

    public void setDesign(boolean b) {
        design = b;
    }

    public int compareTo(PatentItem another) {
        return number.compareTo(another.getNumber());
    }

    private final static String espacenet = "http://v3.espacenet.com/publicationDetails/biblio?DB=EPODOC";
    private final static String espacenet2 = "http://v3.espacenet.com/searchResults?DB=EPODOC";

    private final static String epoline = "https://register.epoline.org/espacenet/application?number=";

    private final static String epoline2 = "https://register.epoline.org/espacenet/simpleSearch?index[0]=publication&value[0]=";
    private final static String epoline3 = "&index[1]=&value[1]=&index[2]=&value[2]=&searchMode=simple&recent=";

    public String getEspacenetURL() {
        String res = null;
        if (provisional) {
            res = espacenet2 + "&PR=" + authority + number + "P";
        } else if (application) {
            res = espacenet2 + "&AP=" + authority + number;
        } else {
            res = espacenet + "&CC=" + authority + "&NR=" + number;
        }
        return res;
    }

    public String getEpolineURL() {
        String res = null;
        if (application) {
            res = epoline + authority + number;
        } else {
            // we need the application number corresponding to the publication
            res = epoline2 + authority + number + epoline3;
        }
        return res;
    }

    public String getType() {
        if (application)
            return "application";
        if (provisional)
            return "provisional";
        if (reissued)
            return "reissued";
        if (plant)
            return "plant";
        if (design)
            return "design";
        // default
        return "publication";
    }

    public void setType(String type) {
        if (type.equals("publication"))
            return;
        if (type.equals("application")) {
            application = true;
        } else if (type.equals("provisional")) {
            provisional = true;
        } else if (type.equals("reissued")) {
            reissued = true;
        } else if (type.equals("plant")) {
            plant = true;
        } else if (type.equals("design")) {
            design = true;
        }
    }

	@Override
	public String toString() {
		return "PatentItem [authority=" + authority + ", number=" + number
				+ ", kindCode=" + kindCode + ", application=" + application
				+ ", provisional=" + provisional + ", reissued=" + reissued
				+ ", plant=" + plant + ", design=" + design + ", conf=" + conf
				+ ", confidence=" + confidence + ", offset_begin="
				+ offset_begin + ", offset_end=" + offset_end + ", offset_raw="
				+ offset_raw + ", context=" + context + "]";
	}
    
    
}