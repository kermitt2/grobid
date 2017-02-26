package org.grobid.core.engines;

/**
 * @author Patrice
 * 
 */
public enum HeaderLabel {
    /**
     * NOTE: in practice, only a subset of all these CRF labels is used
     * article title <title>,
     * abstract <abstract>,
     * article author <author>,
     * technical report reference <tech>,
     * location of institution for diploma/thesis <location>,
     * date of publication <date>,
     * date of submission <date-submission>,
     * page information <page>,
     * editor <editor>,
     * institution for diploma/thesis <institution>,
	 * note <note>,
     * other <other>,
	 * bibliographical references of the article <reference> 
     * grant/funding information <grant> 
     * copyright and other legal information <copyright> 
     * affiliation of authors <affiliation> 
     * address of authors' affiliation <address> 
     * authors' email <email>
     * publication identifier <pubnum>
     * article keywords <keyword>
     * authors' phone <phone> 
     * degree for diploma/thesis <degree>
     * url info <web>  
     * dedication info <dedication> 
     * submission info <submission> 
     * in case of article not written in English, the additional English title <entitle> 
     * introduction marker <intro> 
     */
    TITLE("<title>"),
    ABSTRACT("<abstract>"),
    AUTHOR("<author>"),
    TECH("<tech>"),
    LOCATION("<location>"),
	DATE("<date>"),
    DATESUB("<date-submission>"),
    PAGE("<page>"),
    EDITOR("<editor>"),
    INSTITUTION("<institution>"),
    NOTE("note"),
    OTHER("other"),
    REFERENCE("<reference>"),
	GRANT("<grant>"),
    COPYRIGHT("<copyright>"),
    AFFILIATION("<affiliation>"),
    ADDRESS("<address>"),
	EMAIL("<email>"),
    PUBNUM("<pubnum>"),
    KEYWORD("<keyword>"),
    PHONE("<phone>"),
    DEGREE("<degree>"), 
    WEB("<web>"),
    DEDICATION("<dedication>"),
    SUBMISSION("<submission>"),
    ENTITLE("<entitle>"),
    INTRO("<intro>");

    private String label;
    HeaderLabel(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
