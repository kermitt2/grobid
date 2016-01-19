/**
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

import java.io.InputStream;

import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;
import javax.ws.rs.core.UriInfo;

import org.grobid.core.factory.AbstractEngineFactory;
import org.grobid.service.process.GrobidRestProcessAdmin;
import org.grobid.service.process.GrobidRestProcessFiles;
import org.grobid.service.process.GrobidRestProcessGeneric;
import org.grobid.service.process.GrobidRestProcessString;
import org.grobid.service.util.GrobidServiceProperties;
import org.grobid.service.util.ZipUtils;
import org.grobid.service.util.GrobidRestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.jersey.multipart.FormDataParam;
import com.sun.jersey.spi.resource.Singleton;

/**
 * RESTful service for the GROBID system.
 * 
 * @author FloZi, Damien, Patrice
 * 
 */

@Singleton
@Path(GrobidPathes.PATH_GROBID)
public class GrobidRestService implements GrobidPathes {

	/**
	 * The class Logger.
	 */
	private static final Logger LOGGER = LoggerFactory.getLogger(GrobidRestService.class);

	private static final String NAMES = "names";
	private static final String DATE = "date";
	private static final String AFFILIATIONS = "affiliations";
	private static final String CITATION = "citations";
	private static final String TEXT = "text";
	private static final String SHA1 = "sha1";
	private static final String XML = "xml";
	private static final String INPUT = "input";

	public GrobidRestService() {
		LOGGER.info("Initiating Servlet GrobidRestService");
		AbstractEngineFactory.fullInit();
		GrobidServiceProperties.getInstance();
		LOGGER.info("Initiating of Servlet GrobidRestService finished.");
	}

	/**
	 * @see org.grobid.service.process.GrobidRestProcessGeneric#isAlive()
	 */
	@Path(GrobidPathes.PATH_IS_ALIVE)
	@Produces(MediaType.TEXT_PLAIN)
	@GET
	public Response isAlive() {
		return GrobidRestProcessGeneric.isAlive();
	}

	/**
	 * 
	 * @see org.grobid.service.process.GrobidRestProcessGeneric#getDescription_html(UriInfo)
	 */
	@Produces(MediaType.TEXT_HTML)
	@GET
	@Path("grobid")
	public Response getDescription_html(@Context UriInfo uriInfo) {
		return GrobidRestProcessGeneric.getDescription_html(uriInfo);
	}

	/**
	 * @see org.grobid.service.process.GrobidRestProcessAdmin#getAdminParams(String)
	 */
	@Path(PATH_ADMIN)
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@Produces(MediaType.TEXT_HTML)
	@POST
	public Response getAdmin_htmlPost(@FormParam(SHA1) String sha1) {
		return GrobidRestProcessAdmin.getAdminParams(sha1);
	}

	/**
	 * @see org.grobid.service.process.GrobidRestProcessAdmin#getAdminParams(String)
	 */
	@Path(PATH_ADMIN)
	@Consumes(MediaType.TEXT_PLAIN)
	@Produces(MediaType.TEXT_HTML)
	@GET
	public Response getAdmin_htmlGet(@QueryParam(SHA1) String sha1) {
		return GrobidRestProcessAdmin.getAdminParams(sha1);
	}

	/**
	 * @see org.grobid.service.process.GrobidRestProcessFiles#processStatelessHeaderDocument(InputStream, String)
	 */
	@Path(PATH_HEADER)
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@Produces(MediaType.APPLICATION_XML)
	@POST
	public Response processHeaderDocument_post(@FormDataParam(INPUT) InputStream inputStream, 
	 	@FormDataParam("consolidate") String consolidate
		) throws Exception {
		boolean consol = false;
		if ( (consolidate != null) && (consolidate.equals("1")) ) {
			consol = true;
		}
		return GrobidRestProcessFiles.processStatelessHeaderDocument(inputStream, consol, false);
	}

	/**
	 * @see org.grobid.service.process.GrobidRestProcessFiles#processStatelessHeaderDocument(InputStream, String)
	 */
	@Path(PATH_HEADER)
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@Produces(MediaType.APPLICATION_XML)
	@PUT
	public Response processStatelessHeaderDocument(@FormDataParam(INPUT) InputStream inputStream, 
	 	@FormDataParam("consolidate") String consolidate
		) {
		boolean consol = false;
		if ( (consolidate != null) && (consolidate.equals("1")) ) {
			consol = true;
		}
		return GrobidRestProcessFiles.processStatelessHeaderDocument(inputStream, consol, false);
	}

	/**
	 * @see org.grobid.service.process.GrobidRestProcessFiles#processStatelessHeaderDocument(InputStream, String)
	 */
	@Path(PATH_HEADER_HTML)
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@Produces(MediaType.APPLICATION_XML)
	@POST
	public Response processHeaderDocument_postHTML(@FormDataParam(INPUT) InputStream inputStream, 
	 	@FormDataParam("consolidate") String consolidate) {
		boolean consol = false;
		if ( (consolidate != null) && (consolidate.equals("1")) ) {
			consol = true;
		}
		return GrobidRestProcessFiles.processStatelessHeaderDocument(inputStream, consol, true);
	}

	/**
	 * @see org.grobid.service.process.GrobidRestProcessFiles#processStatelessHeaderDocument(InputStream, String)
	 */
	@Path(PATH_HEADER_HTML)
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@Produces(MediaType.APPLICATION_XML)
	@PUT
	public Response processStatelessHeaderDocumentHTML(@FormDataParam(INPUT) InputStream inputStream,
	 	@FormDataParam("consolidate") String consolidate) {
		boolean consol = false;
		if ( (consolidate != null) && (consolidate.equals("1")) ) {
			consol = true;
		}
		return GrobidRestProcessFiles.processStatelessHeaderDocument(inputStream, consol, true);
	}

	/**
	 * @see org.grobid.service.process.GrobidRestProcessFiles#processStatelessFulltextDocument(InputStream, String)
	 */
	@Path(PATH_FULL_TEXT)
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@Produces(MediaType.APPLICATION_XML)
	@POST
	public Response processFulltextDocument_post(@FormDataParam(INPUT) InputStream inputStream,
	 	@FormDataParam("consolidate") String consolidate, 
		@DefaultValue("-1") @FormDataParam("start") int startPage,
		@DefaultValue("-1") @FormDataParam("end") int endPage,
		@FormDataParam("generateIDs") String generateIDs) {
		boolean consol = false;
		boolean generate = false;
		if ( (consolidate != null) && (consolidate.equals("1")) ) {
			consol = true;
		}
		if ( (generateIDs != null) && (generateIDs.equals("1")) ) {
			generate = true;
		}
		return GrobidRestProcessFiles.processStatelessFulltextDocument(inputStream, 
			consol, false, startPage, endPage, generate);
	}

	/**
	 * @see org.grobid.service.process.GrobidRestProcessFiles#processStatelessFulltextDocument(InputStream, String)
	 */
	@Path(PATH_FULL_TEXT)
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@Produces(MediaType.APPLICATION_XML)
	@PUT
	public Response processStatelessFulltextDocument(@FormDataParam(INPUT) InputStream inputStream,
	 	@FormDataParam("consolidate") String consolidate, 
		@DefaultValue("-1") @FormDataParam("start") int startPage,
		@DefaultValue("-1") @FormDataParam("end") int endPage,
		@FormDataParam("generateIDs") String generateIDs) {
		boolean consol = false;
		boolean generate = false;
		if ( (consolidate != null) && (consolidate.equals("1")) ) {
			consol = true;
		}
		if ( (generateIDs != null) && (generateIDs.equals("1")) ) {
			generate = true;
		}
		return GrobidRestProcessFiles.processStatelessFulltextDocument(inputStream, 
			consol, false, startPage, endPage, generate);
	}

	/**
	 * @see org.grobid.service.process.GrobidRestProcessFiles#processStatelessFulltextAssetDocument(InputStream, String)
	 */
	@Path(PATH_FULL_TEXT_ASSET)
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@Produces("application/zip")
	@POST
	public Response processFulltextAssetDocument_post(@FormDataParam(INPUT) InputStream inputStream,
	 	@FormDataParam("consolidate") String consolidate, 
		@DefaultValue("-1") @FormDataParam("start") int startPage,
		@DefaultValue("-1") @FormDataParam("end") int endPage,
		@FormDataParam("generateIDs") String generateIDs) {
		boolean consol = false;
		boolean generate = false;
		if ( (consolidate != null) && (consolidate.equals("1")) ) {
			consol = true;
		}
		if ( (generateIDs != null) && (generateIDs.equals("1")) ) {
			generate = true;
		}
		return GrobidRestProcessFiles.processStatelessFulltextAssetDocument(inputStream, 
			consol, startPage, endPage, generate);
	}

	/**
	 * @see org.grobid.service.process.GrobidRestProcessFiles#processStatelessFulltextAssetDocument(InputStream, String)
	 */
	@Path(PATH_FULL_TEXT_ASSET)
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@Produces("application/zip")
	@PUT
	public Response processStatelessFulltextAssetDocument(@FormDataParam(INPUT) InputStream inputStream,
	 	@FormDataParam("consolidate") String consolidate, 
		@DefaultValue("-1") @FormDataParam("start") int startPage,
		@DefaultValue("-1") @FormDataParam("end") int endPage,
		@FormDataParam("generateIDs") String generateIDs) {
		boolean consol = false;
		boolean generate = false;
		if ( (consolidate != null) && (consolidate.equals("1")) ) {
			consol = true;
		}
		if ( (generateIDs != null) && (generateIDs.equals("1")) ) {
			generate = true;
		}
		return GrobidRestProcessFiles.processStatelessFulltextAssetDocument(inputStream, 
			consol, startPage, endPage, generate);
	}


	/**
	 * @see org.grobid.service.process.GrobidRestProcessFiles#processStatelessFulltextDocument(InputStream, String)
	 */
	@Path(PATH_FULL_TEXT_HTML)
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@Produces(MediaType.APPLICATION_XML)
	@POST
	public Response processFulltextDocument_postHTML(@FormDataParam(INPUT) InputStream inputStream,
	 	@FormDataParam("consolidate") String consolidate, 
		@DefaultValue("-1") @FormDataParam("start") int startPage,
		@DefaultValue("-1") @FormDataParam("end") int endPage,
		@FormDataParam("generateIDs") String generateIDs) {
		boolean consol = false;
		boolean generate = false;
		if ( (consolidate != null) && (consolidate.equals("1")) ) {
			consol = true;
		}
		if ( (generateIDs != null) && (generateIDs.equals("1")) ) {
			generate = true;
		}
		return GrobidRestProcessFiles.processStatelessFulltextDocument(inputStream, 
			consol, true, startPage, endPage, generate);
	}

	/**
	 * @see org.grobid.service.process.GrobidRestProcessFiles#processStatelessFulltextDocument(InputStream, String)
	 */
	@Path(PATH_FULL_TEXT_HTML)
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@Produces(MediaType.APPLICATION_XML)
	@PUT
	public Response processStatelessFulltextDocumentHTML(@FormDataParam(INPUT) InputStream inputStream,
	 	@FormDataParam("consolidate") String consolidate, 
		@DefaultValue("-1") @FormDataParam("start") int startPage,
		@DefaultValue("-1") @FormDataParam("end") int endPage,
		@FormDataParam("generateIDs") String generateIDs) {
		boolean consol = false;
		boolean generate = false;
		if ( (consolidate != null) && (consolidate.equals("1")) ) {
			consol = true;
		}
		if ( (generateIDs != null) && (generateIDs.equals("1")) ) {
			generate = true;
		}
		return GrobidRestProcessFiles.processStatelessFulltextDocument(inputStream, 
			consol, true, startPage, endPage, generate);
	}

	/**
	 * @see org.grobid.service.process.GrobidRestProcessFiles#processCitationPatentTEI(InputStream, String)
	 */
	@Path(PATH_CITATION_PATENT_TEI)
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@Produces(MediaType.APPLICATION_XML)
	@POST
	public StreamingOutput processCitationPatentTEI(@FormDataParam(INPUT) InputStream pInputStream,
	 	@FormDataParam("consolidate") String consolidate) throws Exception {
		boolean consol = false;
		if ( (consolidate != null) && (consolidate.equals("1")) ) {
			consol = true;
		}
		return GrobidRestProcessFiles.processCitationPatentTEI(pInputStream, consol);
	}
	
	/**
	 * @see org.grobid.service.process.GrobidRestProcessFiles#processCitationPatentST36(InputStream, String)
	 */
	@Path(PATH_CITATION_PATENT_ST36)
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@Produces(MediaType.APPLICATION_XML)
	@POST
	public Response processCitationPatentST36(@FormDataParam(INPUT) InputStream pInputStream,
	 	@FormDataParam("consolidate") String consolidate) throws Exception {
		boolean consol = false;
		if ( (consolidate != null) && (consolidate.equals("1")) ) {
			consol = true;
		}
		
		pInputStream = ZipUtils.decompressStream(pInputStream);
		
		return GrobidRestProcessFiles.processCitationPatentST36(pInputStream, consol);
	}
	
	/**
	 * @see org.grobid.service.process.GrobidRestProcessFiles#processCitationPatentPDF(InputStream, String)
	 */
	@Path(PATH_CITATION_PATENT_PDF)
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@Produces(MediaType.APPLICATION_XML)
	@POST
	public Response processCitationPatentPDF(@FormDataParam(INPUT) InputStream pInputStream,
	 	@FormDataParam("consolidate") String consolidate) throws Exception {
		boolean consol = false;
		if ( (consolidate != null) && (consolidate.equals("1")) ) {
			consol = true;
		}
		return GrobidRestProcessFiles.processCitationPatentPDF(pInputStream, consol);
	}

	/**
	 * @see org.grobid.service.process.GrobidRestProcessString#processCitationPatentTXT(String, String)
	 */
	@Path(PATH_CITATION_PATENT_TXT)
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@Produces(MediaType.APPLICATION_XML)
	@POST
	public Response processCitationPatentTXT_post(@FormParam(TEXT) String text,
	 	@FormParam("consolidate") String consolidate) {
		boolean consol = false;
		if ( (consolidate != null) && (consolidate.equals("1")) ) {
			consol = true;
		}
		return GrobidRestProcessString.processCitationPatentTXT(text, consol);
	}

	/**
	 * @see org.grobid.service.process.GrobidRestProcessString#processDate(String)
	 */
	@Path(PATH_DATE)
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@Produces(MediaType.TEXT_PLAIN)
	@POST
	public Response processDate_post(@FormParam(DATE) String date) {
		return GrobidRestProcessString.processDate(date);
	}

	/**
	 * @see org.grobid.service.process.GrobidRestProcessString#processDate(String)
	 */
	@Path(PATH_DATE)
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@Produces(MediaType.TEXT_PLAIN)
	@PUT
	public Response processDate(@FormParam(DATE) String date) {
		return GrobidRestProcessString.processDate(date);
	}

	/**
	 * @see org.grobid.service.process.GrobidRestProcessString#processNamesHeader(String)
	 */
	@Path(PATH_HEADER_NAMES)
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@Produces(MediaType.TEXT_PLAIN)
	@POST
	public Response processNamesHeader_post(@FormParam(NAMES) String names) {
		return GrobidRestProcessString.processNamesHeader(names);
	}

	/**
	 * @see org.grobid.service.process.GrobidRestProcessString#processNamesHeader(String)
	 */
	@Path(PATH_HEADER_NAMES)
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@Produces(MediaType.TEXT_PLAIN)
	@PUT
	public Response processNamesHeader(@FormParam(NAMES) String names) {
		return GrobidRestProcessString.processNamesHeader(names);
	}

	/**
	 * @see org.grobid.service.process.GrobidRestProcessString#processNamesCitation(String)
	 */
	@Path(PATH_CITE_NAMES)
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@Produces(MediaType.TEXT_PLAIN)
	@POST
	public Response processNamesCitation_post(@FormParam(NAMES) String names) {
		return GrobidRestProcessString.processNamesCitation(names);
	}

	/**
	 * @see org.grobid.service.process.GrobidRestProcessString#processNamesCitation(String)
	 */
	@Path(PATH_CITE_NAMES)
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@Produces(MediaType.TEXT_PLAIN)
	@PUT
	public Response processNamesCitation(@FormParam(NAMES) String names) {
		return GrobidRestProcessString.processNamesCitation(names);
	}

	/**
	 * @see org.grobid.service.process.GrobidRestProcessString#processAffiliations(String)
	 */
	@Path(PATH_AFFILIATION)
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@Produces(MediaType.TEXT_PLAIN)
	@POST
	public Response processAffiliations_post(@FormParam(AFFILIATIONS) String affiliations) {
		return GrobidRestProcessString.processAffiliations(affiliations);
	}

	/**
	 * @see org.grobid.service.process.GrobidRestProcessString#processAffiliations(String)
	 */
	@Path(PATH_AFFILIATION)
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@Produces(MediaType.TEXT_PLAIN)
	@PUT
	public Response processAffiliations(@FormParam(AFFILIATIONS) String affiliation) {
		return GrobidRestProcessString.processAffiliations(affiliation);
	}

	/**
	 * @see org.grobid.service.process.GrobidRestProcessString#processCitation(String, String)
	 */
	@Path(PATH_CITATION)
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@Produces(MediaType.APPLICATION_XML)
	@POST
	public Response processCitation_post(@FormParam(CITATION) String citation,
	 	@FormParam("consolidate") String consolidate) {
		boolean consol = false;
		if ( (consolidate != null) && (consolidate.equals("1")) ) {
			consol = true;
		}
		return GrobidRestProcessString.processCitation(citation, consol);
	}

	/**
	 * @see org.grobid.service.process.GrobidRestProcessString#processCitation(String, String)
	 */
	@Path(PATH_CITATION)
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@Produces(MediaType.APPLICATION_XML)
	@PUT
	public Response processCitation(@FormParam(CITATION) String citation,
	 	@FormParam("consolidate") String consolidate) {
		boolean consol = false;
		if ( (consolidate != null) && (consolidate.equals("1")) ) {
			consol = true;
		}
		return GrobidRestProcessString.processCitation(citation, consol);
	}

	/**
	 * @see org.grobid.service.process.GrobidRestProcessAdmin#processSHA1(String)
	 */
	@Path(PATH_SHA1)
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@Produces(MediaType.TEXT_PLAIN)
	@POST
	public Response processSHA1Post(@FormParam(SHA1) String sha1) {
		return GrobidRestProcessAdmin.processSHA1(sha1);
	}

	/**
	 * @see org.grobid.service.process.GrobidRestProcessAdmin#processSHA1(String)
	 */
	@Path(PATH_SHA1)
	@Consumes(MediaType.TEXT_PLAIN)
	@Produces(MediaType.TEXT_PLAIN)
	@GET
	public Response processSHA1Get(@QueryParam(SHA1) String sha1) {
		return GrobidRestProcessAdmin.processSHA1(sha1);
	}

	/**
	 * @see org.grobid.service.process.GrobidRestProcessAdmin#getAllPropertiesValues(String)
	 */
	@Path(PATH_ALL_PROPS)
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@Produces(MediaType.TEXT_PLAIN)
	@POST
	public Response getAllPropertiesValuesPost(@FormParam(SHA1) String sha1) {
		return GrobidRestProcessAdmin.getAllPropertiesValues(sha1);
	}

	/**
	 * @see org.grobid.service.process.GrobidRestProcessAdmin#getAllPropertiesValues(String)
	 */
	@Path(PATH_ALL_PROPS)
	@Consumes(MediaType.TEXT_PLAIN)
	@Produces(MediaType.TEXT_PLAIN)
	@GET
	public Response getAllPropertiesValuesGet(@QueryParam(SHA1) String sha1) {
		return GrobidRestProcessAdmin.getAllPropertiesValues(sha1);
	}

	/**
	 * @see org.grobid.service.process.GrobidRestProcessAdmin#changePropertyValue(String)
	 */
	@Path(PATH_CHANGE_PROPERTY_VALUE)
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@Produces(MediaType.TEXT_PLAIN)
	@POST
	public Response changePropertyValuePost(@FormParam(XML) String xml) {
		return GrobidRestProcessAdmin.changePropertyValue(xml);
	}

	/**
	 * @see org.grobid.service.process.GrobidRestProcessAdmin#changePropertyValue(String)
	 */
	@Path(PATH_CHANGE_PROPERTY_VALUE)
	@Consumes(MediaType.TEXT_PLAIN)
	@Produces(MediaType.TEXT_PLAIN)
	@GET
	public Response changePropertyValueGet(@QueryParam(XML) String xml) {
		return GrobidRestProcessAdmin.changePropertyValue(xml);
	}

	/**
	 * @see org.grobid.service.process.GrobidRestProcessFiles#processStatelessReferencesDocument(InputStream, bool)
	 */
	@Path(PATH_REFERENCES)
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@Produces(MediaType.APPLICATION_XML)
	@POST
	public Response processReferencesDocument_post(@FormDataParam(INPUT) InputStream inputStream,
	 	@FormDataParam("consolidate") String consolidate) {
		boolean consol = false;
		if ( (consolidate != null) && (consolidate.equals("1")) ) {
			consol = true;
		}
		return GrobidRestProcessFiles.processStatelessReferencesDocument(inputStream, consol);
	}

	/**
	 * @see org.grobid.service.process.GrobidRestProcessFiles#processStatelessReferencesDocument(InputStream, bool)
	 */
	@Path(PATH_REFERENCES)
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@Produces(MediaType.APPLICATION_XML)
	@PUT
	public Response processStatelessReferencesDocument(@FormDataParam(INPUT) InputStream inputStream,
	 	@FormDataParam("consolidate") String consolidate) {
		boolean consol = false;
		if ( (consolidate != null) && (consolidate.equals("1")) ) {
			consol = true;
		}
		return GrobidRestProcessFiles.processStatelessReferencesDocument(inputStream, consol);
	}
	
	/**
	 * @see org.grobid.service.process.GrobidRestProcessFiles#processPDFAnnotation(InputStream, GrobidRestUtils.Annotation)
	 */
	@Path(PATH_PDF_ANNOTATION)
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@Produces("application/pdf")
	@POST
	public Response processAnnotatePDF(@FormDataParam(INPUT) InputStream inputStream,
		@FormDataParam("type") String fileName,
	 	@FormDataParam("type") int type) {
		GrobidRestUtils.Annotation annotType = null;
		if (type == 0)
			annotType = GrobidRestUtils.Annotation.CITATION;
		else if (type == 1)
			annotType = GrobidRestUtils.Annotation.BLOCK;
		else if (type == 2) 
			annotType = GrobidRestUtils.Annotation.FIGURE;
		return GrobidRestProcessFiles.processPDFAnnotation(inputStream, fileName, annotType);
	}


}
