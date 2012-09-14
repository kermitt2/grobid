package org.grobid.core.data;

import java.util.ArrayList;
import java.util.List;

/**
 * Class for representing authority lists for bibliographies.
 *
 * @author Patrice Lopez
 */
public class BiblioSet {
    private List<String> authors = null;
    private List<String> editors = null;
    private List<String> meetings = null;
    private List<String> publishers = null;
    private List<String> locations = null;

    private List<String> journals = null;
    private List<String> institutions = null;
    private List<String> affiliations = null; // ??
    private List<String> keywords = null;

    public BiblioSet() {
    }

    public List<String> getAuthors() {
        return authors;
    }

    public List<String> getEditors() {
        return editors;
    }

    public List<String> getMeetings() {
        return meetings;
    }

    public List<String> getPublishers() {
        return publishers;
    }

    public List<String> getLocations() {
        return locations;
    }

    public void addAuthor(String aut) {
        if (authors == null)
            authors = new ArrayList<String>();
        if (aut != null) {
            if (aut.length() > 0) {
                if (authors.indexOf(aut) == -1)
                    authors.add(aut);
            }
        }
    }

    public void addPublisher(String aut) {
        if (publishers == null)
            publishers = new ArrayList<String>();
        if (aut != null) {
            if (aut.length() > 0) {
                if (publishers.indexOf(aut) == -1)
                    publishers.add(aut);
            }
        }
    }

    public void addEditor(String aut) {
        if (editors == null)
            editors = new ArrayList<String>();
        if (aut != null) {
            if (aut.length() > 0) {
                if (editors.indexOf(aut) == -1)
                    editors.add(aut);
            }
        }
    }

    public void addMeeting(String aut) {
        if (meetings == null)
            meetings = new ArrayList<String>();
        if (aut != null) {
            if (aut.length() > 0) {
                if (meetings.indexOf(aut) == -1)
                    meetings.add(aut);
            }
        }
    }

    /**
     * Export the bibliographical lists into TEI structures
     */
    public String toTEI() {
        String tei = "";

        // we just produce here xml strings, DOM XML objects should be used for JDK 1.4, J2E compliance thingy
        // authors
        if (authors != null) {
            tei += "<listPerson type=\"author\">\n";

            int i = 0;
            for (String aut : authors) {
                tei += "\t<person xml:id=\"author" + i + "\">\n";
                tei += "\t\t<persName>";

                int ind = aut.lastIndexOf(" ");
                if (ind != -1) {
                    tei += "\n\t\t\t<forename>" + aut.substring(0, ind) + "</forename>\n";
                    tei += "\t\t\t<surname>" + aut.substring(ind + 1) + "</surname>\n\t\t";
                } else
                    tei += aut;

                tei += "</persName>\n";
                tei += "\t</person>\n";
                i++;
            }

            tei += "</listPerson>\n\n";
        }

        // editors
        if (editors != null) {
            tei += "<listPerson type=\"editor\">\n";

            int i = 0;
            for (String aut : editors) {
                tei += "\t<person xml:id=\"editor" + i + "\">\n";
                tei += "\t\t<persName>";

                int ind = aut.lastIndexOf(" ");
                if (ind != -1) {
                    tei += "\n\t\t\t<forename>" + aut.substring(0, ind) + "</forename>\n";
                    tei += "\t\t\t<surname>" + aut.substring(ind + 1) + "</surname>\n\t\t";
                } else
                    tei += aut;

                tei += "</persName>\n";
                tei += "\t</person>\n";
                i++;
            }

            tei += "</listPerson>\n\n";
        }

        // publishers
        if (publishers != null) {
            tei += "<listOrg type=\"publisher\">\n";

            int i = 0;
            for (String aut : publishers) {
                tei += "\t<org xml:id=\"publisher" + i + "\">\n";
                tei += "\t\t<orgName>";

                tei += aut;

                tei += "</orgName>\n";
                tei += "\t</org>\n";
                i++;
            }

            tei += "</listOrg>\n\n";
        }

        // meetings
        if (meetings != null) {
            tei += "<list type=\"meeting\">\n";

            int i = 0;
            for (String aut : meetings) {
                tei += "\t<meeting xml:id=\"meeting" + i + "\">";
                tei += aut;
                tei += "</meeting>\n";
                i++;
            }

            tei += "</list>\n\n";
        }

        return tei;
    }

}