package org.grobid.core.data;

import nu.xom.Attribute;
import nu.xom.Element;
import org.grobid.core.document.xml.XmlBuilderUtils;
import org.grobid.core.layout.LayoutToken;
import org.grobid.core.utilities.LayoutTokensUtil;
import org.grobid.core.utilities.TextUtilities;

import java.util.ArrayList;
import java.util.List;

/**
 * Class for representing and exchanging person information, e.g. author or editor.
 *
 * @author Patrice Lopez
 */
public class Person {
    private String firstName = null;
    private String middleName = null;
    private String lastName = null;
    private String title = null;
    private String suffix = null;
    private String rawName = null; // raw full name if relevant, e.g. name exactly as displayed
    private boolean corresp = false;
    private List<LayoutToken> layoutTokens = new ArrayList<>();

    private List<String> affiliationBlocks = null;
    private List<Affiliation> affiliations = null;
    private List<String> affiliationMarkers = null;
    private List<String> markers = null;

    private String email = null;

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String f) {
        firstName = normalizeName(f);
    }

    public String getMiddleName() {
        return middleName;
    }

    public void setMiddleName(String f) {
        middleName = normalizeName(f);
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String f) {
        lastName = normalizeName(f);
    }

    public String getRawName() {
         return rawName;
    }

    public void setRawName(String name) {
         rawName = name;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String f) {
        title = normalizeName(f);
    }

    public String getSuffix() {
        return suffix;
    }

    public void setSuffix(String s) {
        suffix = normalizeName(s);
    }

    public boolean getCorresp() {
        return corresp;
    }

    public void setCorresp(boolean b) {
        corresp = b;
    }

    public List<String> getAffiliationBlocks() {
        return affiliationBlocks;
    }

    public void addAffiliationBlocks(String f) {
        if (affiliationBlocks == null)
            affiliationBlocks = new ArrayList<String>();
        affiliationBlocks.add(f);
    }

    public List<org.grobid.core.data.Affiliation> getAffiliations() {
        return affiliations;
    }

    public void addAffiliation(org.grobid.core.data.Affiliation f) {
        if (affiliations == null)
            affiliations = new ArrayList<org.grobid.core.data.Affiliation>();
        affiliations.add(f);
    }

    public List<String> getAffiliationMarkers() {
        return affiliationMarkers;
    }

    public void addAffiliationMarker(String s) {
        if (affiliationMarkers == null)
            affiliationMarkers = new ArrayList<String>();
        affiliationMarkers.add(s);
    }

    public void setAffiliations(List<org.grobid.core.data.Affiliation> f) {
        affiliations = f;
    }

    public List<String> getMarkers() {
        return markers;
    }

    public void addMarker(String f) {
        if (markers == null)
            markers = new ArrayList<String>();
		f = f.replace(" ", "");
        markers.add(f);
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String f) {
        email = f;
    }

    public boolean notNull() {
        if ((firstName == null) &&
                (middleName == null) &&
                (lastName == null) &&
                (title == null)
                )
            return false;
        else
            return true;
    }

    public String toString() {
        String res = "";
        if (title != null)
            res += title + " ";
        if (firstName != null)
            res += firstName + " ";
        if (middleName != null)
            res += middleName + " ";
        if (lastName != null)
            res += lastName + " ";
        if (suffix != null)
            res += suffix;
        if (email != null) {
            res += " (email:" + email + ")";
        }
        return res.trim();
    }

    public List<LayoutToken> getLayoutTokens() {
        return layoutTokens;
    }

    public String toTEI(boolean withCoordinates) {
        if ( (firstName == null) && (middleName == null) &&
                (lastName == null) ) {
            return null;
        }

        Element persElement = XmlBuilderUtils.teiElement("persName");

        if (withCoordinates && (getLayoutTokens() != null) && (!getLayoutTokens().isEmpty())) {
            XmlBuilderUtils.addCoords(persElement, LayoutTokensUtil.getCoordsString(getLayoutTokens()));
        }
        if (title != null) {
            persElement.appendChild(XmlBuilderUtils.teiElement("roleName", TextUtilities.HTMLEncode(title)));
        }
        if (firstName != null) {
            Element forename = XmlBuilderUtils.teiElement("forename", TextUtilities.HTMLEncode(firstName));
            forename.addAttribute(new Attribute("type", "first"));
            persElement.appendChild(forename);
        }
        if (middleName != null) {
            Element mn = XmlBuilderUtils.teiElement("forename", TextUtilities.HTMLEncode(middleName));
            mn.addAttribute(new Attribute("type", "middle"));
            persElement.appendChild(mn);
        }
        if (lastName != null) {
            persElement.appendChild(XmlBuilderUtils.teiElement("surname", TextUtilities.HTMLEncode(lastName)));
        }
        if (suffix != null) {
            persElement.appendChild(XmlBuilderUtils.teiElement("genName", TextUtilities.HTMLEncode(suffix)));
        }



        //        res += "</persName>";

//        String res = "<persName>";
//        if (title != null)
//            res += "<roleName>" + title + "</roleName>";
//        if (firstName != null)
//            res += "<forename type=\"first\">" + firstName + "</forename>";
//        if (middleName != null)
//            res += "<forename type=\"middle\">" + middleName + "</forename>";
//        if (lastName != null)
//            res += "<surname>" + lastName + "</surname>";
//        if (suffix != null)
//            res += "<genName>" + suffix + "</genName>";
//        res += "</persName>";

        return XmlBuilderUtils.toXml(persElement);
    }

    // list of character delimiters for capitalising names
 	private static final String NAME_DELIMITERS = "-.,;:/_ ";

    static public String normalizeName(String inputName) {
		return TextUtilities.capitalizeFully(inputName, NAME_DELIMITERS);
    }
	
	/**
	 *  Return true if the person structure is a valid person name, in our case
	 *  with at least a lastname or a raw name.
	 */
	public boolean isValid() {
		if ( (lastName == null) && (rawName == null) )
			return false;
		else 
			return true;
	}

}