/**
 * Copyright 2010 INRIA.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 *
 */
package org.grobid.service;

/**
 * This interface only contains the path extensions for accessing the grobid service.
 * @author Florian, Damien, Patrice
 *
 */
public interface GrobidPathes {
	/**
	 * path extension for grobid service.
	 */
	public static final String PATH_GROBID = "/";
	
	/**
	 * path extension for grobid adm.
	 */
	public static final String PATH_ADM = "/adm";
	
	/**
	 * path extension for is alive request.
	 */
	public static final String PATH_IS_ALIVE = "isalive";
	/**
	 * path extension for grobid admin pages.
	 */
	public static final String PATH_ADMIN = "admin";
	
	/**
	 * path extension for processing document headers.
	 */
	public static final String PATH_HEADER = "processHeaderDocument";
	
	/**
	 * path extension for processing document headers HTML.
	 */
	public static final String PATH_HEADER_HTML = "processHeaderDocumentHTML";
	
	/**
	 * path extension for processing bulck document headers.
	 */
	public static final String PATH_BULCK_HEADER = "processBulckHeaderDocument";
	
	/**
	 * path extension for processing full text of documents.
	 */
	public static final String PATH_FULL_TEXT = "processFulltextDocument";
	
	/**
	 * path extension for processing full text of documents together with image extraction.
	 */
	public static final String PATH_FULL_TEXT_ASSET = "processFulltextAssetDocument";
	
	/**
	 * path extension for processing full text of documents.
	 */
	public static final String PATH_FULL_TEXT_HTML = "processFulltextDocumentHTML";
	
	/**
	 * path extension for processing dates.
	 */
	public static final String PATH_DATE = "processDate";
	
	/**
	 * path extension for processing names in header parts of documents headers.
	 */
	public static final String PATH_HEADER_NAMES = "processHeaderNames";
	
	/**
	 * path extension for processing citation in patent documents in TEI.
	 */
	public static final String PATH_CITATION_PATENT_TEI = "processCitationPatentTEI";
	
	/**
	 * path extension for processing citation in patent documents in ST.36.
	 */
	public static final String PATH_CITATION_PATENT_ST36 = "processCitationPatentST36";
	
	/**
	 * path extension for processing citation in patent documents in PDF.
	 */
	public static final String PATH_CITATION_PATENT_PDF = "processCitationPatentPDF";
	
	/**
	 * path extension for processing citation in patent documents in utf-8 txt .
	 */
	public static final String PATH_CITATION_PATENT_TXT = "processCitationPatentTXT";
	
	/**
	 * path extension for processing citation annotations.
	 */
	public static final String PATH_CITATION_ANNOTATION = "processCitationPatentTEI";
	
	/**
	 * path extension for processing names as appearing in a citation (e.g. bibliographic section).
	 */
	public static final String PATH_CITE_NAMES = "processCitationNames";
	
	/**
	 * path extension for processing affiliation in document headers.
	 */
	public static final String PATH_AFFILIATION = "processAffiliations";
	
	/**
	 * path extension for processing isolated citation.
	 */
	public static final String PATH_CITATION = "processCitation";
	
	/**
	 * path extension for processing all the references in a PDF file.
	 */
	public static final String PATH_REFERENCES = "processReferences";
	
	/**
	 * path extension for processing and annotating a PDF file.
	 */
	public static final String PATH_PDF_ANNOTATION = "annotatePDF";

	/**
	 * path extension for processing sha1.
	 */
	public static final String PATH_SHA1 = "sha1";
	
	/**
	 * path extension for getting all properties.
	 */
	public static final String PATH_ALL_PROPS = "allProperties";
	
	/**
	 * path extension to update property value.
	 */
	public static final String PATH_CHANGE_PROPERTY_VALUE = "changePropertyValue";
}
