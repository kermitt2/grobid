package org.grobid.trainer.evaluation.utilities;

import org.apache.commons.lang3.tuple.Pair;

import javax.xml.namespace.QName;
import javax.xml.xpath.*;
import java.util.*;

/**
 * Specification of field XML paths in different result documents for evaluation.
 *
 */
public class FieldSpecification {

	public String fieldName = null;
	
	public List<Pair<XPathExpression, QName>> nlmPath = new ArrayList<>();
	public List<Pair<XPathExpression, QName>> grobidPath = new ArrayList<>();
	public List<String> pdfxPath = new ArrayList<>();
	public List<String> cerminePath = new ArrayList<>();
	
	public boolean isTextual = false;
	
	/** 
	 *  This static method instanciates the fields with the appropriate paths
	 *  in the different gold and extraction formats. 
	*/  
	public static void setUpFields(List<FieldSpecification> headerFields,
								List<FieldSpecification> fulltextFields,
								List<FieldSpecification> citationsFields, 
								List<String> headerLabels,
								List<String> fulltextLabels,
								List<String> citationsLabels) throws XPathExpressionException {

        XPath xPath = XPathFactory.newInstance().newXPath();

        // header

		// title
		FieldSpecification titleField = new FieldSpecification();
		titleField.fieldName = "title";
		titleField.isTextual = true;
		titleField.grobidPath.add(Pair.of(xPath.compile("//titleStmt/title/text()"), XPathConstants.NODESET));
		titleField.nlmPath.add(Pair.of(xPath.compile("/article/front/article-meta/title-group/article-title//text()"), XPathConstants.NODESET));
		titleField.pdfxPath.add("/pdfx/article/front/title-group/article-title/text()");
		headerFields.add(titleField);
		headerLabels.add("title");

		// authors
		FieldSpecification authorField = new FieldSpecification();
		authorField.fieldName = "authors";
		authorField.isTextual = true;
		//authorField.hasMultipleValue = true;
		/*authorField.grobidPath.
			add("//sourceDesc/biblStruct/analytic/author/persName/forename[@type=\"first\"]");
		authorField.grobidPath.
			add("//sourceDesc/biblStruct/analytic/author/persName/forename[@type=\"middle\"]");*/
		authorField.grobidPath.
			add(Pair.of(xPath.compile("//sourceDesc/biblStruct/analytic/author/persName/surname/text()"), XPathConstants.NODESET));
		//authorField.nlmPath.
		//	add("/article/front/article-meta/contrib-group/contrib[@contrib-type=\"author\"]/name/given-names");
		authorField.nlmPath.
			add(Pair.of(xPath.compile("/article/front/article-meta/contrib-group/contrib[@contrib-type=\"author\"]/name/surname/text()"), XPathConstants.NODESET));
		authorField.pdfxPath.add("/pdfx/article/front/contrib-group/contrib[@contrib-type=\"author\"]/name/text()");
		headerFields.add(authorField);
		headerLabels.add("authors");

		// authors
		FieldSpecification firstAuthorField = new FieldSpecification();
		firstAuthorField.fieldName = "first_author";
		firstAuthorField.isTextual = true;
		/*firstAuthorField.grobidPath
			.add("//sourceDesc/biblStruct/analytic/author/persName/forename[@type=\"first\"]");
		firstAuthorField.grobidPath
			.add("//sourceDesc/biblStruct/analytic/author/persName/forename[@type=\"middle\"]");*/
		firstAuthorField.grobidPath
			.add(Pair.of(xPath.compile("//sourceDesc/biblStruct/analytic/author[1]/persName/surname/text()"), XPathConstants.NODESET));
		//firstAuthorField.nlmPath
		//	.add("/article/front/article-meta/contrib-group/contrib[@contrib-type=\"author\"]/name/given-names");
		firstAuthorField.nlmPath
			.add(Pair.of(xPath.compile("/article/front/article-meta/contrib-group/contrib[@contrib-type=\"author\"][1]/name/surname/text()"), XPathConstants.NODESET));
		firstAuthorField.pdfxPath
			.add("/pdfx/article/front/contrib-group/contrib[@contrib-type=\"author\"][1]/name/text()");
		headerFields.add(firstAuthorField);
		headerLabels.add("first_author");

		// affiliation
		FieldSpecification affiliationField = new FieldSpecification();
		affiliationField.fieldName = "affiliations";
		affiliationField.isTextual = true;
		//affiliationField.hasMultipleValue = true;
		affiliationField.grobidPath.add(Pair.of(xPath.compile("//sourceDesc/biblStruct/analytic/author/affiliation/orgName/text()"), XPathConstants.NODESET));
		affiliationField.nlmPath.add(Pair.of(xPath.compile("/article/front/article-meta/contrib-group/aff/text()"), XPathConstants.NODESET));
		affiliationField.pdfxPath.add("/pdfx/article/front/contrib-group");
		//headerFields.add(affiliationField);
		//headerLabels.add("affiliations");

		// date
		FieldSpecification dateField = new FieldSpecification();
		dateField.fieldName = "date";
		dateField.grobidPath.
			add(Pair.of(xPath.compile("//publicationStmt/date[1]/@when"), XPathConstants.NODESET));
		dateField.nlmPath.
			add(Pair.of(xPath.compile("/article/front/article-meta/pub-date[@pub-type=\"pmc-release\"][1]//text()"), XPathConstants.NODESET));
		//in bioRxiv: <pub-date pub-type="epub"><year>2014</year></pub-date>
		//headerFields.add(dateField);
		//headerLabels.add("date");

		// abstract
		FieldSpecification abstractField = new FieldSpecification();
		abstractField.fieldName = "abstract";
		abstractField.isTextual = true;
		abstractField.grobidPath.
			add(Pair.of(xPath.compile("//profileDesc/abstract//text()"), XPathConstants.NODESET));
		abstractField.nlmPath.
			add(Pair.of(xPath.compile("/article/front/article-meta/abstract//text()"), XPathConstants.NODESET));
		headerFields.add(abstractField);
		headerLabels.add("abstract");

		// keywords
		FieldSpecification keywordsField = new FieldSpecification();
		keywordsField.fieldName = "keywords";
		keywordsField.isTextual = true;
		keywordsField.grobidPath.
			add(Pair.of(xPath.compile("//profileDesc/textClass/keywords//text()"), XPathConstants.NODESET));
		keywordsField.nlmPath.
			add(Pair.of(xPath.compile("/article/front/article-meta/kwd-group/kwd/text()"), XPathConstants.NODESET));
		headerFields.add(keywordsField);
		headerLabels.add("keywords");

		// DOI (header)
		FieldSpecification doiField = new FieldSpecification();
		doiField.fieldName = "doi";
		doiField.grobidPath.
			add(Pair.of(xPath.compile("//sourceDesc/biblStruct/idno[@type=\"DOI\"]/text()"), XPathConstants.NODESET));
		doiField.nlmPath.
			add(Pair.of(xPath.compile("/article/front/article-meta/article-id[@pub-id-type=\"doi\"]/text()"), XPathConstants.NODESET));
		//headerFields.add(doiField);
		//headerLabels.add("doi");

		// citations

		// the first field gives the base path for each citation structure
		FieldSpecification baseCitation = new FieldSpecification();
		baseCitation.fieldName = "base";
		baseCitation.grobidPath.add(Pair.of(xPath.compile("//back/div/listBibl/biblStruct"), XPathConstants.NODESET));
		baseCitation.nlmPath.add(Pair.of(xPath.compile("//ref-list/ref"), XPathConstants.NODESET)); // note: sometimes we just have the raw citation bellow this!
		baseCitation.pdfxPath.add("//ref-list/ref"); // note: there is nothing beyond that in pdfx xml results!
		citationsFields.add(baseCitation);
		// the rest of the citation fields are relative to the base path

		// title
		FieldSpecification titleField2 = new FieldSpecification();
		titleField2.fieldName = "title";
		titleField2.isTextual = true;
		titleField2.grobidPath.add(Pair.of(xPath.compile("analytic/title/text()"), XPathConstants.NODESET));
		titleField2.nlmPath.add(Pair.of(xPath.compile("*/article-title//text()"), XPathConstants.NODESET));
		citationsFields.add(titleField2);
		citationsLabels.add("title");

		// authors
		FieldSpecification authorField2 = new FieldSpecification();
		authorField2.fieldName = "authors";
		authorField2.isTextual = true;
		authorField2.grobidPath.add(Pair.of(xPath.compile("analytic/author/persName/surname/text()"), XPathConstants.NODESET));
		authorField2.nlmPath.add(Pair.of(xPath.compile("*//surname[parent::name|parent::string-name]/text()"), XPathConstants.NODESET));
		//authorField2.nlmPath.add(Pair.of(xPath.compile("*//name/surname/text()"), XPathConstants.NODESET));
		//authorField2.nlmPath.add(Pair.of(xPath.compile("*//string-name/surname/text()"), XPathConstants.NODESET));
		citationsFields.add(authorField2);
		citationsLabels.add("authors");

		// authors
		FieldSpecification firstAuthorField2 = new FieldSpecification();
		firstAuthorField2.fieldName = "first_author";
		firstAuthorField2.isTextual = true;
		firstAuthorField2.grobidPath.add(Pair.of(xPath.compile("analytic/author[1]/persName/surname/text()"), XPathConstants.NODESET));
		//firstAuthorField2.nlmPath.add(Pair.of(xPath.compile("*//surname[parent::name|parent::string-name][1]/text()"), XPathConstants.NODESET));
		firstAuthorField2.nlmPath.add(Pair.of(xPath.compile("*//name[1]/surname/text()"), XPathConstants.NODESET));
		firstAuthorField2.nlmPath.add(Pair.of(xPath.compile("*//string-name[1]/surname/text()"), XPathConstants.NODESET));
		citationsFields.add(firstAuthorField2);
		citationsLabels.add("first_author");

		// date
		FieldSpecification dateField2 = new FieldSpecification();
		dateField2.fieldName = "date";
		dateField2.grobidPath.add(Pair.of(xPath.compile("monogr/imprint/date/@when"), XPathConstants.NODESET));
		dateField2.nlmPath.add(Pair.of(xPath.compile("*/year/text()"), XPathConstants.NODESET));
		citationsFields.add(dateField2);
		citationsLabels.add("date");

		// monograph title
		FieldSpecification inTitleField2 = new FieldSpecification();
		inTitleField2.fieldName = "inTitle";
		inTitleField2.isTextual = true;
		inTitleField2.grobidPath.add(Pair.of(xPath.compile("monogr/title/text()"), XPathConstants.NODESET));
		inTitleField2.nlmPath.add(Pair.of(xPath.compile("*/source/text()"), XPathConstants.NODESET));
		citationsFields.add(inTitleField2);
		citationsLabels.add("inTitle");

		// volume
		FieldSpecification volumeField = new FieldSpecification();
		volumeField.fieldName = "volume";
		volumeField.grobidPath.
			add(Pair.of(xPath.compile("monogr/imprint/biblScope[@unit=\"volume\" or @unit=\"vol\"]/text()"), XPathConstants.NODESET));
		volumeField.nlmPath.
			add(Pair.of(xPath.compile("*/volume/text()"), XPathConstants.NODESET));
		citationsFields.add(volumeField);
		citationsLabels.add("volume");

		// issue
		FieldSpecification issueField = new FieldSpecification();
		issueField.fieldName = "issue";
		issueField.grobidPath.
			add(Pair.of(xPath.compile("monogr/imprint/biblScope[@unit=\"issue\"]/text()"), XPathConstants.NODESET));
		issueField.nlmPath.
			add(Pair.of(xPath.compile("*/issue/text()"), XPathConstants.NODESET));
		citationsFields.add(issueField);
		citationsLabels.add("issue");

		// first page
		FieldSpecification pageField = new FieldSpecification();
		pageField.fieldName = "page";
		pageField.grobidPath.
			add(Pair.of(xPath.compile("monogr/imprint/biblScope[@unit=\"page\"]/@from"), XPathConstants.NODESET));
		pageField.nlmPath.
			add(Pair.of(xPath.compile("*/fpage/text()"), XPathConstants.NODESET));
		citationsFields.add(pageField);
		citationsLabels.add("page");

		// publisher
		FieldSpecification publisherField = new FieldSpecification();
		publisherField.fieldName = "publisher";
		publisherField.isTextual = true;
		publisherField.grobidPath.
			add(Pair.of(xPath.compile("monogr/imprint/publisher/text()"), XPathConstants.NODESET));
		publisherField.nlmPath.
			add(Pair.of(xPath.compile("*/publisher-name/text()"), XPathConstants.NODESET));
		//citationsFields.add(publisherField);
		//citationsLabels.add("publisher");

		// citation identifier (will be used for citation mapping, not for matching)
		FieldSpecification citationIdField = new FieldSpecification();
		citationIdField.fieldName = "id";
		citationIdField.isTextual = true;
		citationIdField.grobidPath.add(Pair.of(xPath.compile("@id"), XPathConstants.NODESET));
		citationIdField.nlmPath.add(Pair.of(xPath.compile("@id"), XPathConstants.NODESET));
		citationsFields.add(citationIdField);
		citationsLabels.add("id");

		// DOI
		FieldSpecification citationDOIField = new FieldSpecification();
		citationDOIField.fieldName = "doi";
		citationDOIField.isTextual = true;
		citationDOIField.grobidPath.
			add(Pair.of(xPath.compile("analytic/idno[@type=\"DOI\"]/text()"), XPathConstants.NODESET));
		citationDOIField.nlmPath.
			add(Pair.of(xPath.compile("*/pub-id[@pub-id-type=\"doi\"]/text()"), XPathConstants.NODESET));
		citationsFields.add(citationDOIField);
		citationsLabels.add("doi");

		// PMID
		FieldSpecification citationPMIDField = new FieldSpecification();
		citationPMIDField.fieldName = "pmid";
		citationPMIDField.isTextual = true;
		citationPMIDField.grobidPath.
			add(Pair.of(xPath.compile("analytic/idno[@type=\"PMID\"]/text()"), XPathConstants.NODESET));
		citationPMIDField.nlmPath.
			add(Pair.of(xPath.compile("*/pub-id[@pub-id-type=\"pmid\"]/text()"), XPathConstants.NODESET));
		citationsFields.add(citationPMIDField);
		citationsLabels.add("pmid");

		// PMC
		FieldSpecification citationPMCIDField = new FieldSpecification();
		citationPMCIDField.fieldName = "pmcid";
		citationPMCIDField.isTextual = true;
		citationPMCIDField.grobidPath.
			add(Pair.of(xPath.compile("analytic/idno[@type=\"PMCID\"]/text()"), XPathConstants.NODESET));
		citationPMCIDField.nlmPath.
			add(Pair.of(xPath.compile("*/pub-id[@pub-id-type=\"pmcid\"]/text()"), XPathConstants.NODESET));
		citationsFields.add(citationPMCIDField);
		citationsLabels.add("pmcid");


		// full text structures
		/*FieldSpecification sectionReferenceField = new FieldSpecification();
		sectionReferenceField.fieldName = "references";
		sectionReferenceField.isTextual = true;
		sectionReferenceField.grobidPath.
			add("//back/div/listBibl/biblStruct//text()");
		sectionReferenceField.nlmPath.
			add("//ref-list/ref//text()");
		fulltextFields.add(sectionReferenceField);
		fulltextLabels.add("references");*/

		FieldSpecification sectionTitleField = new FieldSpecification();
		sectionTitleField.fieldName = "section_title";
		sectionTitleField.isTextual = true;

        //LF: added //text() at the end instead of /text() so that possible child nodes are also included in the xpath
		sectionTitleField.grobidPath.add(Pair.of(xPath.compile("//text/body/div/head//text()"), XPathConstants.NODESET));
		sectionTitleField.nlmPath.add(Pair.of(xPath.compile("//body//sec/title//text()"), XPathConstants.NODESET));
		fulltextFields.add(sectionTitleField);
		fulltextLabels.add("section_title");

		FieldSpecification referenceMarkerField = new FieldSpecification();
		referenceMarkerField.fieldName = "reference_citation";
		referenceMarkerField.isTextual = true;
		referenceMarkerField.grobidPath.add(Pair.of(xPath.compile("//ref[@type=\"bibr\"]/text()"), XPathConstants.NODESET));
		referenceMarkerField.nlmPath.add(Pair.of(xPath.compile("//xref[@ref-type=\"bibr\"]/text()"), XPathConstants.NODESET));
		fulltextFields.add(referenceMarkerField);
		fulltextLabels.add("reference_citation");

		FieldSpecification referenceFigureField = new FieldSpecification();
		referenceFigureField.fieldName = "reference_figure";
		referenceFigureField.isTextual = true;
		referenceFigureField.grobidPath.add(Pair.of(xPath.compile("//ref[@type=\"figure\"]/text()"), XPathConstants.NODESET));
		referenceFigureField.nlmPath.add(Pair.of(xPath.compile("//xref[@ref-type=\"fig\"]/text()"), XPathConstants.NODESET));
		fulltextFields.add(referenceFigureField);
		fulltextLabels.add("reference_figure");

		FieldSpecification referenceTableField = new FieldSpecification();
		referenceTableField.fieldName = "reference_table";
		referenceTableField.isTextual = true;
		referenceTableField.grobidPath.add(Pair.of(xPath.compile("//ref[@type=\"table\"]/text()"), XPathConstants.NODESET));
		referenceTableField.nlmPath.add(Pair.of(xPath.compile("//xref[@ref-type=\"table\"]/text()"), XPathConstants.NODESET));
		fulltextFields.add(referenceTableField);
		fulltextLabels.add("reference_table");

		FieldSpecification figureTitleField = new FieldSpecification();
		figureTitleField.fieldName = "figure_title";
		figureTitleField.isTextual = true;
		figureTitleField.grobidPath.add(Pair.of(xPath.compile("//figure[not(@type)]/head/text()"), XPathConstants.NODESET));
		figureTitleField.nlmPath.add(Pair.of(xPath.compile("//fig/label/text()"), XPathConstants.NODESET));
		fulltextFields.add(figureTitleField);
		fulltextLabels.add("figure_title");

		/*FieldSpecification figureCaptionField = new FieldSpecification();
		figureCaptionField.fieldName = "figure_caption";
		figureCaptionField.isTextual = true;
		figureCaptionField.grobidPath.
			add("//figure[not(@type)]/figDesc/text()");
		figureCaptionField.nlmPath.
			add("//fig/caption/p/text()");
		fulltextFields.add(figureCaptionField);
		fulltextLabels.add("figure_caption");*/

		/*FieldSpecification figureLabelField = new FieldSpecification();
		figureLabelField.fieldName = "figure_label";
		figureLabelField.isTextual = true;
		figureLabelField.grobidPath.
			add("//figure[not(@type)]/label/text()");
		figureLabelField.nlmPath.
			add("//fig/label/text()");
		fulltextFields.add(figureLabelField);
		fulltextLabels.add("figure_label");*/

		FieldSpecification tableTitleField = new FieldSpecification();
		tableTitleField.fieldName = "table_title";
		tableTitleField.isTextual = true;
		tableTitleField.grobidPath.add(Pair.of(xPath.compile("//figure[@type=\"table\"]/head/text()"), XPathConstants.NODESET));
		tableTitleField.nlmPath.add(Pair.of(xPath.compile("//table-wrap/label/text()"), XPathConstants.NODESET));
		fulltextFields.add(tableTitleField);
		fulltextLabels.add("table_title");

		/*FieldSpecification tableLabelField = new FieldSpecification();
		tableLabelField.fieldName = "figure_label";
		tableLabelField.isTextual = true;
		tableLabelField.grobidPath.
			add("//figure[@type=\"table\"]/label/text()");
		tableLabelField.nlmPath.
			add("//fig/label/text()");
		fulltextFields.add(tableLabelField);
		fulltextLabels.add("figure_label");*/

		/*FieldSpecification tableCaptionField = new FieldSpecification();
		tableCaptionField.fieldName = "table_caption";
		tableCaptionField.isTextual = true;
		tableCaptionField.grobidPath.
			add("//figure[@type=\"table\"]/figDesc/text()");
		tableCaptionField.nlmPath.
			add("//table-wrap/caption/p/text()");
		fulltextFields.add(tableCaptionField);
		fulltextLabels.add("figure_caption");*/

		//labels.add("section_title");
		//labels.add("paragraph");
		//labels.add("citation_marker");
		//labels.add("figure_marker");
		//labels.add("table_marker");


        // Other XPATHs:
            // text()
        // - //article/body/sec/sec[title[contains(.,\"availability\")]]//text()
        // - //article/back/sec/sec[title[contains(.,\"availability\")]]//text()
            // string()
        // - string(.//article/body/sec/sec[title[contains(.,\"availability\")]]

        // - normalize-space(.//article/body/sec/sec[title[contains(.,"availability")]]
        // - normalize-space(.//article/back/sec/sec[title[contains(.,"availability")]]


        FieldSpecification dataAvailabilityFulltextField = new FieldSpecification();
        dataAvailabilityFulltextField.fieldName = "availabilityStmt";
        dataAvailabilityFulltextField.isTextual = true;
//        dataAvailabilityFulltextField.grobidPath
//            .add(Pair.of(xPath.compile("//note[@type=\"availability\"]//text()"), XPathConstants.NODESET));
        dataAvailabilityFulltextField.grobidPath
            .add(Pair.of(xPath.compile("//div[@type=\"availability\"]//text()"), XPathConstants.NODESET));

        dataAvailabilityFulltextField.nlmPath
//            .add(Pair.of(xPath.compile("normalize-space(.//article/body/sec[title[" + xpathTitle + "]])"), XPathConstants.STRING));
            .add(Pair.of(xPath.compile("normalize-space(.//sec[@sec-type=\"availability\"])"), XPathConstants.STRING));
        dataAvailabilityFulltextField.nlmPath
            .add(Pair.of(xPath.compile("normalize-space(.//p[@content-type=\"availability\"])"), XPathConstants.STRING));
        dataAvailabilityFulltextField.nlmPath
            .add(Pair.of(xPath.compile("normalize-space(.//sec[@specific-use=\"availability\"])"), XPathConstants.STRING));
//        dataAvailabilityFulltextField.nlmPath
//            .add(Pair.of(xPath.compile("normalize-space(.//article/back/sec[title[" + xpathTitle + "]])"), XPathConstants.STRING));
//            .add(Pair.of(xPath.compile("normalize-space(.//article/back//sec[@sec-type=\"data-availability\"])"), XPathConstants.STRING));
        dataAvailabilityFulltextField.nlmPath
            .add(Pair.of(xPath.compile("normalize-space(.//p[@content-type=\"availability\"])"), XPathConstants.STRING));

        fulltextFields.add(dataAvailabilityFulltextField);
        fulltextLabels.add("availabilityStmt");
	}

	public static String grobidCitationContextId = "//ref[@type=\"bibr\"]/@target";
	public static String grobidBibReferenceId = "//listBibl/biblStruct/@id";

	public static String nlmCitationContextId = "//xref[@ref-type=\"bibr\"]/@rid";
	public static String nlmBibReferenceId = "//ref-list/ref/@id";

}