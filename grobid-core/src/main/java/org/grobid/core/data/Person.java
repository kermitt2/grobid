package org.grobid.core.data;

import org.apache.commons.lang3.StringUtils;

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
    private String rawName = null; // raw full name if relevant/available, e.g. name exactly as displayed
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
        firstName = f;
    }

    public String getMiddleName() {
        return middleName;
    }

    public void setMiddleName(String f) {
        middleName = f;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String f) {
        lastName = f;
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
        title = f;
    }

    public String getSuffix() {
        return suffix;
    }

    public void setSuffix(String s) {
        suffix = s;
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

    /*static public String normalizeName(String inputName) {
		return TextUtilities.capitalizeFully(inputName, NAME_DELIMITERS);
    }*/

    /**
     * This normalisation takes care of uniform case for name components and for 
     * transforming agglutinated initials (like "JM" in JM Smith)
     * which are put into the firstname into separate initials in first and middle names. 
     * 
     */
    public void normalizeName() {
        if (StringUtils.isEmpty(middleName) && !StringUtils.isEmpty(firstName) && 
            (firstName.length() == 2) && (TextUtilities.isAllUpperCase(firstName)) ) {
            middleName = firstName.substring(1,2);
            firstName = firstName.substring(0,1);
        } 

        firstName = TextUtilities.capitalizeFully(firstName, NAME_DELIMITERS);
        middleName = TextUtilities.capitalizeFully(middleName, NAME_DELIMITERS);
        lastName = TextUtilities.capitalizeFully(lastName, NAME_DELIMITERS);
    }
	
    // assume never more than 3 initials
    //private Pattern initials = Pattern.compile("([A-Z])(?:\\.)\\s?(?:([A-Z])(?:\\.))?\\s?(?:([A-Z])(?:\\.))?");

    /**
     * First names coming from CrossRef are clearly heavily impacted by the original puslisher 
     * formats and a large variety of forms can be seen, with some information lost apparently.
     */ 
    public void normalizeCrossRefFirstName() {
        // first name can be initial with a dot, e.g. "M." or without a dot
        // <forename type="first">H</forename>

        // fistname can be intials with appended middlename also as initials, 
        // with or without space, e.g. "M. L." or
        // <forename type="first">L.S.</forename>

        // normal full first name can be appended with middlename initials with dots but 
        // no space e.g. "Nicholas J.", "John W.S."

        // we have sldo destructive case normalization done at CrossRef or by publishers 
        // like "Zs. Biró" 

        String first = null;
        String middle = null;

        /*Matcher m = initials.matcher(firstName);
        while(m.find()) {
            count++;
            System.out.println("Match number "+count);
            System.out.println("start(): "+m.start());
            System.out.println("end(): "+m.end());
            if (count != 0) {

            }
        }*/

        firstName = firstName.replace(".", ". ");
        firstName = StringUtils.normalizeSpace(firstName);

        // check first the specific case "Zs. Biró" - given the we've never observed three 
        // letters first name like "Zsv. Biró"
        if ( firstName.endsWith(".") && (firstName.length() == 3) &&
            Character.isUpperCase(firstName.charAt(0)) && Character.isLowerCase(firstName.charAt(1)) ) {
            middleName = firstName.substring(1,2);
            firstName = firstName.substring(0,1);
        }

        // check the specific case of composed forenames which are often but not always lost  
        // ex: "J.-L. Arsuag"
        if ( (firstName.indexOf("-") != -1) ) {
            String tokens[] = firstName.replace(" ", "").split("-");
            if (tokens.length == 2) {
                if (tokens[0].endsWith(".") && (tokens[0].length() == 2))
                    first = ""+tokens[0].charAt(0);
                else if (tokens[0].length() == 1)
                    first = tokens[0];
                if (tokens[1].endsWith(".") && (tokens[1].length() == 2))
                    first += "-" + tokens[1].charAt(0);
                else if (tokens[1].length() == 1)
                    first += "-" + tokens[1];
            }
        } else { 
            String tokens[] = firstName.split(" ");
            for(int i=tokens.length-1; i>=0; i--) {
                if (i != 0) {
                    if (first != null) {
                        if (tokens[i].endsWith(".") && (tokens[i].length() == 2)) {
                            // (case "G. Arjen")
                            first = tokens[i].charAt(0) + " " + first;
                        } else {
                            // multiple token first name
                            first = tokens[i] + " " + first;
                        }
                    } else if ( (tokens[i].endsWith(".") && (tokens[i].length() == 2)) || 
                        (tokens[i].length() == 1) ) {
                        // we have an initials in secondary position, this is a middle name
                        if (middle == null)
                            middle = ""+tokens[i].charAt(0);
                        else
                           middle = tokens[i].charAt(0) + " " + middle;
                    } else {
                        if (middle == null)
                            middle = tokens[i];
                        else
                           middle = tokens[i] + " " + middle;
                    }
                } else {                
                    // we check if we have an initial at the beginning (case "G. Arjen")
                    if (tokens[i].endsWith(".") && (tokens[i].length() == 2)) {
                        if (first == null)
                            first = ""+tokens[i].charAt(0);
                        else
                            first = tokens[i] + " " + first;
                    } else {
                        if (first == null)
                            first = tokens[i];
                        else
                            first = tokens[i] + " " + first;
                    }
                }
            }
        }

        if (first != null)
            firstName = first;
        if (middle != null)
            middleName = middle;

        // dirty case <forename type="first">HermanHG</forename><surname>Teerink</surname>
        if ( (firstName != null) && (middleName == null) && (firstName.length()>2) && 
             Character.isUpperCase(firstName.charAt(firstName.length()-1)) && 
             Character.isLowerCase(firstName.charAt(1)) ) {
            int i = firstName.length()-1;
            while(i>1) {
                if (Character.isUpperCase(firstName.charAt(i))) {
                    if (middleName == null)
                        middleName = ""+firstName.charAt(i);
                    else
                        middleName = firstName.charAt(i) + " " + middleName;
                } else 
                    break;
                i--;
            }
            firstName = firstName.substring(0, i+1);
        } 


        // for cases like JM Smith and for case normalisation
        normalizeName();

        // cleaning for CrossRef middlenames
        if (middleName != null) {
            middleName = middleName.replace(".", ". ");
            middleName = middleName.replace("  ", " ");
        }
        
        // other weird stuff: <forename type="first">G. Arjen</forename><surname>de Groot</surname>

        // also note that language specific case practice are usually not rexpected
        // e.g. H Von Allmen, J De  
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