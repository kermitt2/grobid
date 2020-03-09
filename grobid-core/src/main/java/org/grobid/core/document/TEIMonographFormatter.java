package org.grobid.core.document;

import org.grobid.core.data.MonographItem;
import org.grobid.core.engines.config.GrobidAnalysisConfig;
import org.grobid.core.utilities.GrobidProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.TimeZone;

/**
 * Class for generating a TEI representation for monograph structure of a document.
 */
public class TEIMonographFormatter {
    private static final Logger LOGGER = LoggerFactory.getLogger(TEIMonographFormatter.class);

    public enum SchemaDeclaration {
        DEFAULT, DTD, XSD, RNG, RNC
    }

    /**
     * TEI header for the monograph model gives the information about
     * the xml version and the producer of the file
     */

    public StringBuilder toTEIHeaderMonograph(Document doc, GrobidAnalysisConfig config) {
        return toTEIHeaderMonograph(doc, SchemaDeclaration.XSD, config);
    }

    public StringBuilder toTEIHeaderMonograph(Document doc, SchemaDeclaration schemaDeclaration, GrobidAnalysisConfig config) {
        StringBuilder tei = new StringBuilder();
        tei.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        if (config.isWithXslStylesheet()) {
            tei.append("<?xml-stylesheet type=\"text/xsl\" href=\"../jsp/xmlverbatimwrapper.xsl\"?> \n");
        }
        if (schemaDeclaration == SchemaDeclaration.DTD) {
            tei.append("<!DOCTYPE TEI SYSTEM \"" + GrobidProperties.get_GROBID_HOME_PATH()
                + "/schemas/dtd/Grobid.dtd" + "\">\n");
        } else if (schemaDeclaration == SchemaDeclaration.XSD) {
            // XML schema
            tei.append("<TEI xml:space=\"preserve\" xmlns=\"http://www.tei-c.org/ns/1.0\" \n" +
                "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" \n" +
                "xsi:schemaLocation=\"http://www.tei-c.org/ns/1.0 " +
                GrobidProperties.get_GROBID_HOME_PATH() + "/schemas/xsd/Grobid.xsd\"" +
                "\n xmlns:xlink=\"http://www.w3.org/1999/xlink\">\n");
        } else if (schemaDeclaration == SchemaDeclaration.RNG) {
            // standard RelaxNG
            tei.append("<?xml-model href=\"file://" +
                GrobidProperties.get_GROBID_HOME_PATH() + "/schemas/rng/Grobid.rng" +
                "\" schematypens=\"http://relaxng.org/ns/structure/1.0\"?>\n");
        } else if (schemaDeclaration == SchemaDeclaration.RNC) {
            // compact RelaxNG
            tei.append("<?xml-model href=\"file://" +
                GrobidProperties.get_GROBID_HOME_PATH() + "/schemas/rng/Grobid.rnc" +
                "\" type=\"application/relax-ng-compact-syntax\"?>\n");
        }
        // by default there is no schema association
        if (schemaDeclaration != SchemaDeclaration.XSD) {
            tei.append("<TEI xml:space=\"preserve\" xmlns=\"http://www.tei-c.org/ns/1.0\">\n");
        }

        if (doc.getLanguage() != null) {
            tei.append("\t<teiHeader xml:lang=\"" + doc.getLanguage() + "\">");
        } else {
            tei.append("\t<teiHeader>");
        }

        // encodingDesc gives info about the producer of the file
        tei.append("\n\t\t<encodingDesc>\n");
        tei.append("\t\t\t<appInfo>\n");

        TimeZone tz = TimeZone.getTimeZone("UTC");
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mmZ");
        df.setTimeZone(tz);
        String dateISOString = df.format(new java.util.Date());

        tei.append("\t\t\t\t<application version=\"" + GrobidProperties.getVersion() +
            "\" ident=\"GROBID-SDO\" when=\"" + dateISOString + "\">\n");
        tei.append("\t\t\t\t\t<desc>GROBID - A machine learning software for extracting information from scholarly documents</desc>\n");
        tei.append("\t\t\t\t\t<ref target=\"https://github.com/kermitt2/grobid-sdo\"/>\n");
        tei.append("\t\t\t\t</application>\n");
        tei.append("\t\t\t</appInfo>\n");
        tei.append("\t\t</encodingDesc>\n");

        tei.append("\t</teiHeader>\n");

        return tei;
    }

    public StringBuilder toTEIBodyMonograph(Document doc) {
        StringBuilder tei = new StringBuilder();
        tei.append("\t<text xml:lang=\"" + doc.getLanguage() + "\">");
        return tei;
    }
}