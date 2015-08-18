package org.grobid.core.data;

/**
 * Class for managing patent bibliographical references.
 *
 * @author Patrice Lopez
 */
public class PatentItem implements Comparable<PatentItem> {
    // attribute
    private String authority = null;
    private String number_epodoc = null;
	private String number_wysiwyg = null;
    private String kindCode = null;

    // patent type when applicable
    private Boolean application = false;
    private Boolean provisional = false;
    private Boolean reissued = false;
    private Boolean plant = false;
    private Boolean design = false;
	private Boolean utility = false;
	
    // scores
    private double conf = 1.0;
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

    public String getNumberEpoDoc() {
        return number_epodoc;
    }

    public String getNumberWysiwyg() {
        return number_wysiwyg;
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

	public Boolean getUtility() {
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

    public void setNumberEpoDoc(String num) {
        number_epodoc = num;
    }

    public void setNumberWysiwyg(String num) {
        number_wysiwyg = num;
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

	public void setUtility(boolean b) {
        utility = b;
    }

	public void setConf(double val) {
		conf = val;
	}

    public int compareTo(PatentItem another) {
        return number_wysiwyg.compareTo(another.getNumberWysiwyg());
    }

    private final static String espacenet = "http://v3.espacenet.com/publicationDetails/biblio?DB=EPODOC";
    private final static String espacenet2 = "http://v3.espacenet.com/searchResults?DB=EPODOC";

    private final static String epoline = "https://register.epoline.org/espacenet/application?number=";

    private final static String epoline2 = "https://register.epoline.org/espacenet/simpleSearch?index[0]=publication&value[0]=";
    private final static String epoline3 = "&index[1]=&value[1]=&index[2]=&value[2]=&searchMode=simple&recent=";

    public String getEspacenetURL() {
        String res = null;
        if (provisional) {
            res = espacenet2 + "&PR=" + authority + number_epodoc + "P";
        } else if (application) {
            res = espacenet2 + "&AP=" + authority + number_epodoc;
        } else {
            res = espacenet + "&CC=" + authority + "&NR=" + number_epodoc;
        }
        return res;
    }

    public String getEpolineURL() {
        String res = null;
        if (application) {
            res = epoline + authority + number_epodoc;
        } else {
            // we need the application number corresponding to the publication
            res = epoline2 + authority + number_epodoc + epoline3;
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
		return "PatentItem [authority=" + authority + ", number_wysiwyg=" + number_wysiwyg
				+ ", number_epodoc=" + number_epodoc 
				+ ", kindCode=" + kindCode + ", application=" + application
				+ ", provisional=" + provisional + ", reissued=" + reissued
				+ ", plant=" + plant + ", design=" + design + ", conf=" + conf
				+ ", confidence=" + confidence + ", offset_begin="
				+ offset_begin + ", offset_end=" + offset_end + ", offset_raw="
				+ offset_raw + ", context=" + context + "]";
	}
    
	public String toTEI() {
		return toTEI(null, false, null);
	}
	public String toTEI(boolean withPtr, String ptrVal) {
		return toTEI(null, withPtr, ptrVal);
	}
	public String toTEI(String date) {
		return toTEI(date, false, null);
	}
	public String toTEI(String date, boolean withPtr, String ptrVal) {
		/* TEI for patent bilbiographical data is as follow (After the TEI guideline update of October 2012):
		<biblStruct type="patent¦utilityModel¦designPatent¦plant" status="application¦publication">
		<monogr>
		<authority>
		<orgName type="national¦regional">[name of patent office]<orgName> (mandatory)
		</authority>
		<idno type="docNumber">[patent document number]</idno> (mandatory)
		<imprint> (optional)
		<classCode scheme="kindCode">[kind code]</classCode> (optional)
		<date>[date]</date> (optional)
		</imprint>
		</monogr>
		</biblStruct>
		*/
		StringBuffer biblStruct = new StringBuffer();
		
		// type of patent
		biblStruct.append("<biblStruct type=\"");
		if (design) {
			biblStruct.append("designPatent");
		}
		else if (plant) {
			biblStruct.append("plant");
		}
		else if (utility) {
			biblStruct.append("utilityModel");
		}
		else {
			biblStruct.append("patent");
		}
		
		// status
		biblStruct.append("\" status=\"");				
		if (application) {
			biblStruct.append("application");
		}
		else if (provisional) {
			biblStruct.append("provisional");
		}
		else if (reissued) {
			biblStruct.append("reissued");
		}
		else {
			biblStruct.append("publication");
		}
		biblStruct.append("\">");
		
		biblStruct.append("<monogr><authority><orgName type=\"");
		if (authority.equals("EP") || authority.equals("WO") || authority.equals("XN") 
			|| authority.equals("XN") || authority.equals("GC") || authority.equals("EA") ) { 
			// XN is the Nordic Patent Institute
			// OA is the African Intellectual Property Organization (OAPI)
			// GC is the Gulf Cooperation Council 
			// EA Eurasian Patent Organization
			biblStruct.append("regional");
		}
		else {
			biblStruct.append("national");
		}
		biblStruct.append("\">"+authority+"</orgName></authority>");
		biblStruct.append("<idno type=\"docNumber\" subtype=\"epodoc\">"+number_epodoc+"</idno>");
		biblStruct.append("<idno type=\"docNumber\" subtype=\"original\">"+number_wysiwyg+"</idno>");
		
		if ((kindCode != null) || (date != null)) {
			biblStruct.append("<imprint>");
			if (kindCode != null) {
				biblStruct.append("<classCode scheme=\"kindCode\">"+kindCode+"</classCode>");
			}
			if (date != null) {
				biblStruct.append("<date>"+date+"</date>");
			}
			biblStruct.append("</imprint>");
		}
		
		if (withPtr) {
			biblStruct.append("<ptr target=\"#string-range('" + ptrVal + "',"+
				offset_begin +","+ 
				(offset_end - offset_begin + 1) +")\"></ptr>");
		}
		
		if (conf != 0.0) {
			biblStruct.append("<certainty degree=\"" + conf +"\" />");
		}
		biblStruct.append("</monogr>");
		
		
		biblStruct.append("</biblStruct>");
		
		return biblStruct.toString();
	}
    
}