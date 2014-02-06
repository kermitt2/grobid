package org.grobid.core.data;

import org.grobid.core.utilities.TextUtilities;
import org.grobid.core.lexicon.Lexicon;

import java.util.ArrayList;
import java.util.List;

/**
 * Class for representing and exchanging affiliation information.
 *
 * @author Patrice Lopez
 */
public class Affiliation {

    private String acronym = null;
    private String name = null;
    private String url = null;
    private List<String> institutions = null; // for additional institutions
    private List<String> departments = null; // for additional departments
    private List<String> laboratories = null; // for additional laboratories

    private String country = null;
    private String postCode = null;
    private String postBox = null;
    private String region = null;
    private String settlement = null;
    private String addrLine = null;
    private String marker = null;

    private String addressString = null; // unspecified address field
    private String affiliationString = null; // unspecified affiliation field

    private boolean failAffiliation = true; // tag for unresolved affiliation attachment

    public Affiliation() {
    }

    public Affiliation(org.grobid.core.data.Affiliation aff) {
        acronym = aff.getAcronym();
        name = aff.getName();
        url = aff.getURL();
        addressString = aff.getAddressString();
        country = aff.getCountry();
        marker = aff.getMarker();
        departments = aff.getDepartments();
        institutions = aff.getInstitutions();
        laboratories = aff.getLaboratories();
        postCode = aff.getPostCode();
        postBox = aff.getPostBox();
        region = aff.getRegion();
        settlement = aff.getSettlement();
        addrLine = aff.getAddrLine();
        affiliationString = aff.getAffiliationString();
    }

    public String getAcronym() { 
		return acronym; 
	}
	
    public String getName() {
        return name;
    }

    public String getURL() {
        return url;
    }

    public String getAddressString() {
        return addressString;
    }

    public String getCountry() {
        return country;
    }

    public String getMarker() {
        return marker;
    }

    public String getPostCode() {
        return postCode;
    }

    public String getPostBox() {
        return postBox;
    }

    public String getRegion() {
        return region;
    }

    public String getSettlement() {
        return settlement;
    }

    public String getAddrLine() {
        return addrLine;
    }

    public String getAffiliationString() {
        return affiliationString;
    }

    public List<String> getInstitutions() {
        return institutions;
    }

    public List<String> getLaboratories() {
        return laboratories;
    }

    public List<String> getDepartments() {
        return departments;
    }

    public void setAcronym(String s) { 
		acronym = s; 
	}
	
    public void setName(String s) {
        name = s;
    }

    public void setURL(String s) {
        url = s;
    }

    public void setAddressString(String s) {
        addressString = s;
    }

    public void setCountry(String s) {
        country = s;
    }

    public void setMarker(String s) {
        marker = s;
    }

    public void setPostCode(String s) {
        postCode = s;
    }

    public void setPostBox(String s) {
        postBox = s;
    }

    public void setRegion(String s) {
        region = s;
    }

    public void setSettlement(String s) {
        settlement = s;
    }

    public void setAddrLine(String s) {
        addrLine = s;
    }

    public void setAffiliationString(String s) {
        affiliationString = s;
    }

    public void setInstitutions(List<String> affs) {
        institutions = affs;
    }

    public void addInstitution(String aff) {
        if (institutions == null)
            institutions = new ArrayList<String>();
        institutions.add(TextUtilities.cleanField(aff, true));
    }

    public void setDepartments(List<String> affs) {
        departments = affs;
    }

    public void addDepartment(String aff) {
        if (departments == null)
            departments = new ArrayList<String>();
        departments.add(TextUtilities.cleanField(aff, true));
    }

    public void setLaboratories(List<String> affs) {
        laboratories = affs;
    }

    public void addLaboratory(String aff) {
        if (laboratories == null)
            laboratories = new ArrayList<String>();
        laboratories.add(TextUtilities.cleanField(aff, true));
    }

    public void extendFirstInstitution(String theExtend) {
        if (institutions == null) {
            institutions = new ArrayList<String>();
            institutions.add(TextUtilities.cleanField(theExtend, true));
        } else {
            String first = institutions.get(0);
            first = first + theExtend;
            institutions.set(0, first);
        }
    }

    public void extendLastInstitution(String theExtend) {
        if (institutions == null) {
            institutions = new ArrayList<String>();
            institutions.add(TextUtilities.cleanField(theExtend, true));
        } else {
            String first = institutions.get(institutions.size() - 1);
            first = first + theExtend;
            institutions.set(institutions.size() - 1, first);
        }
    }

    public void extendFirstDepartment(String theExtend) {
        if (departments == null) {
            departments = new ArrayList<String>();
            departments.add(TextUtilities.cleanField(theExtend, true));
        } else {
            String first = departments.get(0);
            first = first + theExtend;
            departments.set(0, first);
        }
    }

    public void extendLastDepartment(String theExtend) {
        if (departments == null) {
            departments = new ArrayList<String>();
            departments.add(TextUtilities.cleanField(theExtend, true));
        } else {
            String first = departments.get(departments.size() - 1);
            first = first + theExtend;
            departments.set(departments.size() - 1, first);
        }
    }

    public void extendFirstLaboratory(String theExtend) {
        if (laboratories == null) {
            laboratories = new ArrayList<String>();
            laboratories.add(TextUtilities.cleanField(theExtend, true));
        } else {
            String first = laboratories.get(0);
            first = first + theExtend;
            laboratories.set(0, first);
        }
    }

    public void extendLastLaboratory(String theExtend) {
        if (laboratories == null) {
            laboratories = new ArrayList<String>();
            laboratories.add(TextUtilities.cleanField(theExtend, true));
        } else {
            String first = laboratories.get(laboratories.size() - 1);
            first = first + theExtend;
            laboratories.set(laboratories.size() - 1, first);
        }
    }

    public boolean notNull() {
        return !((departments == null) &
                (institutions == null) &
                (laboratories == null) &
                (country == null) &
                (postCode == null) &
                (postBox == null) &
                (region == null) &
                (settlement == null) &
                (addrLine == null) &
                (affiliationString == null) &
                (addressString == null));
    }

    public void setFailAffiliation(boolean b) {
        failAffiliation = b;
    }

    public boolean getFailAffiliation() {
        return failAffiliation;
    }

    public void clean() {
        if (departments != null) {
            List<String> newDepartments = new ArrayList<String>();
            for (String department : departments) {
                String dep = TextUtilities.cleanField(department, true);
                if (dep.length() > 2) {
                    newDepartments.add(dep);
                }
            }
            departments = newDepartments;
        }

        if (institutions != null) {
            List<String> newInstitutions = new ArrayList<String>();
            for (String institution : institutions) {
                String inst = TextUtilities.cleanField(institution, true);
                if (inst.length() > 1) {
                    newInstitutions.add(inst);
                }
            }
            institutions = newInstitutions;
        }

        if (laboratories != null) {
            List<String> newLaboratories = new ArrayList<String>();
            for (String laboratorie : laboratories) {
                String inst = TextUtilities.cleanField(laboratorie, true);
                if (inst.length() > 2) {
                    newLaboratories.add(inst);
                }
            }
            laboratories = newLaboratories;
        }

        if (country != null) {
            country = TextUtilities.cleanField(country, true);
			if (country.endsWith(")")) {
				// for some reason the ) at the end of this field is not removed
				country = country.substring(0,country.length()-1);
			}
            if (country.length() < 2)
                country = null;
        }
        if (postCode != null) {
            postCode = TextUtilities.cleanField(postCode, true);
            if (postCode.length() < 2)
                postCode = null;
        }
        if (postBox != null) {
            postBox = TextUtilities.cleanField(postBox, true);
            if (postBox.length() < 2)
                postBox = null;
        }
        if (region != null) {
            region = TextUtilities.cleanField(region, true);
            if (region.length() < 2)
                region = null;
        }
        if (settlement != null) {
            settlement = TextUtilities.cleanField(settlement, true);
            if (settlement.length() < 2)
                settlement = null;
        }
        if (addrLine != null) {
            addrLine = TextUtilities.cleanField(addrLine, true);
            if (addrLine.length() < 2)
                addrLine = null;
        }
        if (addressString != null) {
            addressString = TextUtilities.cleanField(addressString, true);
            if (addressString.length() < 2)
                addressString = null;
        }
        if (affiliationString != null) {
            affiliationString = TextUtilities.cleanField(affiliationString, true);
            if (affiliationString.length() < 2)
                affiliationString = null;
        }
        if (marker != null) {
            marker = TextUtilities.cleanField(marker, true);
			marker = marker.replace(" ", "");
        }
    }

    public String toTEI() {
        StringBuilder tei = new StringBuilder();
        if (!notNull()) {
            return null;
        } else {
            tei.append("<affiliation>");

            if (departments != null) {
                if (departments.size() == 1) {
                    tei.append("<orgName type=\"department\">").append(TextUtilities.HTMLEncode(departments.get(0))).append("</orgName>");
                } else {
                    int q = 1;
                    for (String depa : departments) {
                        tei.append("<orgName type=\"department\" key=\"dep").append(q).append("\">").append(TextUtilities.HTMLEncode(depa)).append("</orgName>");
                        q++;
                    }
                }
            }

            if (laboratories != null) {
                if (laboratories.size() == 1) {
                    tei.append("<orgName type=\"laboratory\">").append(TextUtilities.HTMLEncode(laboratories.get(0))).append("</orgName>");
                } else {
                    int q = 1;
                    for (String labo : laboratories) {
                        tei.append("<orgName type=\"laboratory\" key=\"lab").append(q).append("\">").append(TextUtilities.HTMLEncode(labo)).append("</orgName>");
                        q++;
                    }
                }
            }

            if (institutions != null) {
                if (institutions.size() == 1) {
                    tei.append("<orgName type=\"institution\">").append(TextUtilities.HTMLEncode(institutions.get(0))).append("</orgName>");
                } else {
                    int q = 1;
                    for (String inst : institutions) {
                        tei.append("<orgName type=\"institution\" key=\"instit").append(q).append("\">").append(TextUtilities.HTMLEncode(inst)).append("</orgName>");
                        q++;
                    }
                }
            }

            if ((getAddressString() != null) |
                    (getAddrLine() != null) |
                    (getPostBox() != null) |
                    (getPostCode() != null) |
                    (getSettlement() != null) |
                    (getRegion() != null) |
                    (getCountry() != null)) {
                tei.append("<address>");
                if (getAddressString() != null) {
                    tei.append("<addrLine>").append(TextUtilities.HTMLEncode(getAddressString())).append("</addrLine>");
                }
                if (getAddrLine() != null) {
                    tei.append("<addrLine>").append(TextUtilities.HTMLEncode(getAddrLine())).append("</addrLine>");
                }
                if (getPostBox() != null) {
                    tei.append("<postBox>").append(TextUtilities.HTMLEncode(getPostBox())).append("</postBox>");
                }
                if (getPostCode() != null) {
                    tei.append("<postCode>").append(TextUtilities.HTMLEncode(getPostCode())).append("</postCode>");
                }
                if (getSettlement() != null) {
                    tei.append("<settlement>").append(TextUtilities.HTMLEncode(getSettlement())).append("</settlement>");
                }
                if (getRegion() != null) {
                    tei.append("<region>").append(TextUtilities.HTMLEncode(getRegion())).append("</region>");
                }
                if (getCountry() != null) {
                    Lexicon lexicon = Lexicon.getInstance();
                    String code = lexicon.getcountryCode(getCountry());
                    tei.append("<country");
                    if (code != null)
                        tei.append(" key=\"").append(code).append("\"");
                    tei.append(">").append(TextUtilities.HTMLEncode(getCountry())).append("</country>");
                }
                tei.append("</address>");
            }
            tei.append("</affiliation>");
        }

        return tei.toString();
    }

    @Override
    public String toString() {
        return "Affiliation{" +
                "name='" + name + '\'' +
                ", url='" + url + '\'' +
                ", institutions=" + institutions +
                ", departments=" + departments +
                ", laboratories=" + laboratories +
                ", country='" + country + '\'' +
                ", postCode='" + postCode + '\'' +
                ", postBox='" + postBox + '\'' +
                ", region='" + region + '\'' +
                ", settlement='" + settlement + '\'' +
                ", addrLine='" + addrLine + '\'' +
                ", marker='" + marker + '\'' +
                ", addressString='" + addressString + '\'' +
                ", affiliationString='" + affiliationString + '\'' +
                ", failAffiliation=" + failAffiliation +
                '}';
    }

    
}