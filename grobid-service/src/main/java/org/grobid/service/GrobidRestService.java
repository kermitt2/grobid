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

import java.io.InputStream;

import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.grobid.core.impl.GrobidFactory;
import org.grobid.service.process.GrobidRestProcessAdmin;
import org.grobid.service.process.GrobidRestProcessFiles;
import org.grobid.service.process.GrobidRestProcessGeneric;
import org.grobid.service.process.GrobidRestProcessString;
import org.grobid.service.util.GrobidServiceProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.jersey.spi.resource.Singleton;

/**
 * RESTful service for the GROBID system.
 * 
 * @author FloZi
 * 
 */
@Singleton
@Path(GrobidPathes.PATH_GROBID)
public class GrobidRestService implements GrobidPathes {
	/**
	 * The class Logger.
	 */
	private static final Logger LOGGER = LoggerFactory
			.getLogger(GrobidRestService.class);

	private static final String NAMES = "names";
	private static final String DATE = "date";
	private static final String AFFILIATIONS = "affiliations";


	public GrobidRestService() {
		LOGGER.info("Initiating Sevlet GrobidRestService");
		GrobidServiceProperties.getInstance();
		GrobidFactory.getInstance();
		LOGGER.info("Initiating of Sevlet GrobidRestService finished.");
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
	public Response getAdmin_htmlPost(@FormParam("sha1") String sha1) {
		return GrobidRestProcessAdmin.getAdminParams(sha1);
	}

	/**
	 * @see org.grobid.service.process.GrobidRestProcessAdmin#getAdminParams(String)
	 */
	@Path(PATH_ADMIN)
	@Consumes(MediaType.TEXT_PLAIN)
	@Produces(MediaType.TEXT_HTML)
	@GET
	public Response getAdmin_htmlGet(@QueryParam("sha1") String sha1) {
		return GrobidRestProcessAdmin.getAdminParams(sha1);
	}

	/**
	 * @see org.grobid.service.process.GrobidRestProcessFiles#processStatelessHeaderDocument(InputStream)
	 */
	@Path(PATH_HEADER)
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@Produces(MediaType.APPLICATION_XML)
	@POST
	public Response processHeaderDocument_post(InputStream inputStream) {
		return GrobidRestProcessFiles
				.processStatelessHeaderDocument(inputStream);
	}

	/**
	 * @see org.grobid.service.process.GrobidRestProcessFiles#processStatelessHeaderDocument(InputStream)
	 */
	@Path(PATH_HEADER)
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@Produces(MediaType.APPLICATION_XML)
	@PUT
	public Response processStatelessHeaderDocument(InputStream inputStream) {
		return GrobidRestProcessFiles
				.processStatelessHeaderDocument(inputStream);
	}

	/**
	 * @see org.grobid.service.process.GrobidRestProcessFiles#processStatelessFulltextDocument(InputStream)
	 */
	@Path(PATH_FULL_TEXT)
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@Produces(MediaType.APPLICATION_XML)
	@POST
	public Response processFulltextDocument_post(InputStream inputStream) {
		return GrobidRestProcessFiles
				.processStatelessFulltextDocument(inputStream);
	}

	/**
	 * @see org.grobid.service.process.GrobidRestProcessFiles#processStatelessFulltextDocument(InputStream)
	 */
	@Path(PATH_FULL_TEXT)
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@Produces(MediaType.APPLICATION_XML)
	@PUT
	public Response processStatelessFulltextDocument(InputStream inputStream) {
		return GrobidRestProcessFiles
				.processStatelessFulltextDocument(inputStream);
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
	public Response processAffiliations_post(
			@FormParam(AFFILIATIONS) String affiliations) {
		return GrobidRestProcessString.processAffiliations(affiliations);
	}

	/**
	 * @see org.grobid.service.process.GrobidRestProcessString#processAffiliations(String)
	 */
	@Path(PATH_AFFILIATION)
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@Produces(MediaType.TEXT_PLAIN)
	@PUT
	public Response processAffiliations(
			@FormParam(AFFILIATIONS) String affiliation) {
		return GrobidRestProcessString.processAffiliations(affiliation);
	}

	

	/**
	 * @see org.grobid.service.process.GrobidRestProcessAdmin#processSHA1(String)
	 */
	@Path(PATH_SHA1)
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@Produces(MediaType.TEXT_PLAIN)
	@POST
	public Response processSHA1Post(@FormParam("sha1") String sha1) {
		return GrobidRestProcessAdmin.processSHA1(sha1);
	}

	/**
	 * @see org.grobid.service.process.GrobidRestProcessAdmin#processSHA1(String)
	 */
	@Path(PATH_SHA1)
	@Consumes(MediaType.TEXT_PLAIN)
	@Produces(MediaType.TEXT_PLAIN)
	@GET
	public Response processSHA1Get(@QueryParam("sha1") String sha1) {
		return GrobidRestProcessAdmin.processSHA1(sha1);
	}

}
