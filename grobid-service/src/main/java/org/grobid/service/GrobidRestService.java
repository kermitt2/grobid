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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Properties;

import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;

import org.grobid.core.GrobidFactory;
import org.grobid.core.data.Affiliation;
import org.grobid.core.data.Date;
import org.grobid.core.data.Person;
import org.grobid.service.exceptions.GrobidServiceException;
import org.grobid.service.util.GrobidServiceProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * RESTful service for the GROBID system.
 * @author FloZi
 *
 */
@Path(GrobidPathes.PATH_GROBID)
public class GrobidRestService implements GrobidPathes
{
	private static final Logger LOGGER = LoggerFactory.getLogger(GrobidRestService.class);
	
	public GrobidRestService()
	{
		GrobidServiceProperties.getInstance();
	}
	
	/**
	 * Creates a new not used temprorary folder and returns it.
	 * @return
	 */
	protected static File newTempDir()
	{
		File generalTmpDir= new File(System.getProperty("java.io.tmpdir"));
		if (!generalTmpDir.exists())
			throw new GrobidServiceException("Cannot create a temprorary folder, because the base temprorary path requested from the os does not exist.");
		File retVal= new File(generalTmpDir.getAbsolutePath()+"/"+System.nanoTime());
		if (!retVal.mkdir())
			throw new GrobidServiceException("Cannot create a temprorary folder, '"+retVal.getAbsolutePath()+"'.");
		return(retVal);
	}
	
	/**
	 * Returns a string containing true, if the service is alive.
	 * @return returns a response object containing the string true if service is alive. 
	 */
	@Path(GrobidPathes.PATH_IS_ALIVE)
	@Produces(MediaType.TEXT_PLAIN)
	@GET
	public Response isAlive()
	{
		Response response= null;
		try
		{
			LOGGER.debug("called isAlive()...");
			
			String retVal= null;
			try
			{
				retVal= Boolean.valueOf(true).toString();
			}
			catch (Exception e)
			{
				LOGGER.error("COSMATService is not alive, because of: ", e);
				retVal= Boolean.valueOf(false).toString();
			}
			response= Response.status(Status.OK).entity(retVal).build();
		} catch (Exception e) 
		{
			LOGGER.error(""+e);
			response= Response.status(Status.INTERNAL_SERVER_ERROR).build();
		}
		return(response);
	}
	
	/**
	 * Returns the description of how to use the grobid-service in a human readable way (html).
	 * @return returns a response object containing a html description
	 */
	@Produces(MediaType.TEXT_HTML)
	@GET
	public Response getDescription_html(@Context UriInfo uriInfo)
	{
		Response response= null;
		try{
			LOGGER.debug("called getDescription_html()...");
			
			StringBuffer htmlCode= new StringBuffer();
			
			htmlCode.append("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\" \"http://www.w3.org/TR/html4/loose.dtd\">");
			htmlCode.append("<html>");
			htmlCode.append("<head>");
			htmlCode.append("<title>grobid-service - description</title>");
			htmlCode.append("</head>");
			htmlCode.append("<body>");
			htmlCode.append("<h1>grobid-service documentation</h1>");
			htmlCode.append("This service provides a RESTful interface for using the grobid system. grobid extracts data from pdf files. For more information see: ");
			htmlCode.append("<a href=\"http://hal.inria.fr/inria-00493437_v1/\">http://hal.inria.fr/inria-00493437_v1/</a>");
			htmlCode.append("<br/>");
			String link= null;
			if (	(uriInfo != null) &&
					(uriInfo.getAbsolutePath()!= null)&&
					(uriInfo.getAbsolutePath().toString().endsWith("/")))
			{
				link= "../application.wadl";
			}
			else link= "application.wadl";
			htmlCode.append("A more detailed technical description of the grobid-service can be found here (<a href=\""+link+"\">application.wadl</a>)");
			htmlCode.append("</body>");
			htmlCode.append("</html>");
	
			response= Response.status(Status.OK).entity(htmlCode.toString()).type(MediaType.TEXT_HTML).build();
		} catch (Exception e) 
		{
			LOGGER.error("Cannot response the description for grobid-service. ", e);
			response= Response.status(Status.INTERNAL_SERVER_ERROR).build();
		}
		return(response);
	}
	
	/**
	 * Returns the admin view of all properties used for running grobid.
	 * @return returns a response object containing the admin infos in html syntax. 
	 */
	@Produces(MediaType.TEXT_HTML)
	@Path(PATH_ADMIN)
	@GET
	public Response getAdmin_html(	@PathParam("pw") String pw,
									@Context UriInfo uriInfo)
	{
		Response response= null;
		try{
			LOGGER.debug("called getDescription_html()...");
			GrobidServiceProperties.getInstance();
			if (	(GrobidServiceProperties.getAdminPw()!= null)&&
					(GrobidServiceProperties.getAdminPw().equals(pw)))
			{
				StringBuffer htmlCode= new StringBuffer();
				
				htmlCode.append("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\" \"http://www.w3.org/TR/html4/loose.dtd\">");
				htmlCode.append("<html>");
				htmlCode.append("<head>");
				htmlCode.append("<title>grobid-service - admin</title>");
				htmlCode.append("</head>");
				htmlCode.append("<body>");
				htmlCode.append("<table border=\"1\">");
				htmlCode.append("<tr><td>property</td><td>value</td></tr>");
				htmlCode.append("<tr><td colspan=\"2\">java properties</td></tr>");
				htmlCode.append("<tr><td>os name</td><td>"+System.getProperty("os.name")+"</td></tr>");
				htmlCode.append("<tr><td>os version</td><td>"+System.getProperty("sun.arch.data.model")+"</td></tr>");
				htmlCode.append("<tr><td colspan=\"2\">grobid properties</td></tr>");
				
				GrobidServiceProperties.getInstance();
				Properties props = GrobidServiceProperties.getProps();
				for (Object property: props.keySet())
				{
					htmlCode.append("<tr><td>"+property+"</td><td>"+props.getProperty((String)property)+"</td></tr>");
				}
				htmlCode.append("</table>");
				htmlCode.append("</body>");
				htmlCode.append("</html>");
		
				response= Response.status(Status.OK).entity(htmlCode.toString()).type(MediaType.TEXT_HTML).build();
			}
			else
				response= Response.status(Status.FORBIDDEN).build();
		} catch (Exception e) 
		{
			LOGGER.error("Cannot response the description for grobid-service. ", e);
			response= Response.status(Status.INTERNAL_SERVER_ERROR).build();
		}
		return(response);
	}
	
	/**
	 * Uploads the origin document which shall be extracted into TEI and extracts only the header data. 
	 * @param inputStream the data of origin document
	 * @return a response object which contains a TEI representation of the header part
	 */
	@Path(PATH_HEADER)
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@Produces(MediaType.APPLICATION_XML)
	@POST
	public Response processHeaderDocument_post(InputStream inputStream) {
		return(processStatelessHeaderDocument(inputStream));
	}
	
	/**
	 * Uploads the origin document which shall be extracted into TEI and extracts only the header data. 
	 * @param inputStream the data of origin document
	 * @return a response object which contains a TEI representation of the header part
	 */
	@Path(PATH_HEADER)
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@Produces(MediaType.APPLICATION_XML)
	@PUT
	public Response processStatelessHeaderDocument(InputStream inputStream) {
		LOGGER.debug(">> "+this.getClass().getName()+"."+Thread.currentThread().getStackTrace()[1]
			.getMethodName());
		Response response= null;
		String retVal = null;
		try {
			LOGGER.debug(">> set origin document for stateless service'...");
			File originFile = new File(newTempDir()+"/"+"origin_header.pdf");
		    OutputStream out = null;
			try {
				out = new FileOutputStream(originFile);
			
			    byte buf[]=new byte[1024];
			    int len;
			    while((len=inputStream.read(buf))>0) {
			    	out.write(buf,0,len);
				}
		    }
			catch (IOException e) {
				LOGGER.error("An internal error occurs, while writing to disk (file to write '" + 
					originFile+"').", e);
				response = Response.status(Status.INTERNAL_SERVER_ERROR).build();
			}
			finally {
				try {
					if (out!= null)
						out.close();
					inputStream.close();
				} 
				catch (IOException e) {
					String msg= "An internal error occurs, while writing to disk (file to write '"+originFile+"').";
					LOGGER.error(msg);
					response = Response.status(Status.INTERNAL_SERVER_ERROR).build();
				}
		    }
		    
		    //starts conversion process
			retVal = GrobidFactory.instance.createEngine().processHeader(originFile.getAbsolutePath(), false, null);

			if ( (retVal== null) || (retVal.isEmpty()) ) {
				response= Response.status(Status.NO_CONTENT).build();
			}
			else {
				response= Response.status(Status.OK).entity(retVal).type(MediaType.APPLICATION_XML).build();
			}
		}
		catch (Exception e) {
			//e.printStackTrace();
			LOGGER.error("An unexpected exception occurs. ",e);
		}
		LOGGER.debug("<< "+this.getClass().getName()+"."+Thread.currentThread().getStackTrace()[1]
			.getMethodName());
		return(response);
	}
	
	/**
	 * Uploads the origin document which shall be extracted into TEI.
	 * @param inputStream the data of origin document
	 * @return a response object mainly contain the TEI representation of the full text
	 */
	@Path(PATH_FULL_TEXT)
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@Produces(MediaType.APPLICATION_XML)
	@POST
	public Response processFulltextDocument_post(InputStream inputStream) {
		return(processStatelessFulltextDocument(inputStream));
	}
	
	/**
	 * Uploads the origin document which shall be extracted into TEI.
	 * @param inputStream the data of origin document
	 * @return a response object mainly contain the TEI representation of the full text
	 */
	@Path(PATH_FULL_TEXT)
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@Produces(MediaType.APPLICATION_XML)
	@PUT
	public Response processStatelessFulltextDocument(InputStream inputStream) {
		LOGGER.debug(">> "+this.getClass().getName()+"."+Thread.currentThread().getStackTrace()[1]
			.getMethodName());
		Response response= null;
		String retVal = null;
		try {
			LOGGER.debug(">> set origin document for stateless service'...");

			File originFile = new File(newTempDir()+"/"+"origin.pdf");
		    OutputStream out = null;
			try {
				out = new FileOutputStream(originFile);
			
			    byte buf[] = new byte[1024];
			    int len;
			    while((len=inputStream.read(buf))>0) {
			    	out.write(buf,0,len);
				}
		    }
			catch (IOException e) {
				LOGGER.error("An internal error occurs, while writing to disk (file to write '" + 
					originFile+"').", e);
				response = Response.status(Status.INTERNAL_SERVER_ERROR).build();
			}
			finally {
				try {
					if (out!= null) {
						out.close();
					}
					inputStream.close();
				} 
				catch (IOException e) {
					String msg= "An internal error occurs, while writing to disk (file to write '"+originFile+"').";
					LOGGER.error(msg);
					response = Response.status(Status.INTERNAL_SERVER_ERROR).build();
				}
		    }
		    
		    //starts conversion process
			retVal = GrobidFactory.instance.createEngine().fullTextToTEI(originFile.getAbsolutePath(), false, false);
			
			if ( (retVal== null) || (retVal.isEmpty()) ) {
				response= Response.status(Status.NO_CONTENT).build();
			}
			else {
				response= Response.status(Status.OK).entity(retVal).type(MediaType.APPLICATION_XML).build();
			}
		}
		catch (Exception e) {
			//e.printStackTrace();
			LOGGER.error("An unexpected exception occurs. ",e);
		}
		LOGGER.debug("<< "+this.getClass().getName()+"."+Thread.currentThread().getStackTrace()[1].getMethodName());
		return(response);
	}
	
	/**
	 * Parse a raw date and return the corresponding normalized date.
	 * @param the raw date string
	 * @return a response object containing the structured xml representation of the date 
	 */
	@Path(PATH_DATE)
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@Produces(MediaType.APPLICATION_XML)
	@POST
	public Response processDate_post(@FormParam("date") String date) {
		return(processDate(date));
	}
	
	/**
	 * Parse a raw date and return the corresponding normalized date.
	 * @param the raw date string
	 * @return a response object containing the structured xml representation of the date 
	 */
	@Path(PATH_DATE)
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@Produces(MediaType.APPLICATION_XML)
	@PUT
	public Response processDate(@FormParam("date") String date) {
		LOGGER.debug(">> "+this.getClass().getName()+"."+Thread.currentThread().getStackTrace()[1]
			.getMethodName());
		Response response= null;
		String retVal = null;
		try {
			LOGGER.debug(">> set raw date for stateless service'...");
			
		    //starts process
			List<Date> dates= GrobidFactory.instance.createEngine().processDate(date); 
			if (dates!= null)
			{
				if (dates.size()==1)
					retVal= dates.get(0).toString();
				else
					retVal = dates.toString();
			}
			
			if ( (retVal == null) || (retVal.isEmpty()) ) {
				response = Response.status(Status.NO_CONTENT).build();
			}
			else {
				response = Response.status(Status.OK).entity(retVal).type(MediaType.APPLICATION_XML).build();
			}
		}
		catch (Exception e) {
			//e.printStackTrace();
			LOGGER.error("An unexpected exception occurs. ",e);
		}
		LOGGER.debug("<< "+this.getClass().getName()+"."+Thread.currentThread().getStackTrace()[1]
			.getMethodName());
		return(response);
	}
	
	/**
	 * Parse a raw sequence of names from a header section and return the corresponding normalized authors.
	 * @param the string of the raw sequence of header authors 
	 * @return a response object containing the structured xml representation of the authors 
	 */
	@Path(PATH_HEADER_NAMES)
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@Produces(MediaType.APPLICATION_XML)
	@POST
	public Response processNamesHeader_post(@FormParam("names") String names) {
		return(processNamesHeader(names));
	}
	
	/**
	 * Parse a raw sequence of names from a header section and return the corresponding normalized authors.
	 * @param the string of the raw sequence of header authors 
	 * @return a response object containing the structured xml representation of the authors 
	 */
	@Path(PATH_HEADER_NAMES)
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@Produces(MediaType.APPLICATION_XML)
	@PUT
	public Response processNamesHeader(@FormParam("names") String names) {
		LOGGER.debug(">> "+this.getClass().getName()+"."+Thread.currentThread().getStackTrace()[1]
			.getMethodName());
		Response response = null;
		String retVal = null;
		try {
			LOGGER.debug(">> set raw header author sequence for stateless service'...");
			
		    //starts process
			List<Person> authors= GrobidFactory.instance.createEngine().processAuthorsHeader(names); 
			if (authors!= null)
			{
				if (authors.size()==1)
					retVal= authors.get(0).toString();
				else
					retVal = authors.toString();
			}
			
			if ( (retVal == null) || (retVal.isEmpty()) ) {
				response = Response.status(Status.NO_CONTENT).build();
			}
			else {
				response = Response.status(Status.OK).entity(retVal).type(MediaType.APPLICATION_XML).build();
			}
		}
		catch (Exception e) {
			//e.printStackTrace();
			LOGGER.error("An unexpected exception occurs. ",e);
		}
		LOGGER.debug("<< "+this.getClass().getName()+"."+Thread.currentThread().getStackTrace()[1]
			.getMethodName());
		return(response);
	}
	
	/**
	 * Parse a raw sequence of names from a header section and return the corresponding normalized authors.
	 * @param the string of the raw sequence of header authors 
	 * @return a response object containing the structured xml representation of the authors 
	 */
	@Path(PATH_CITE_NAMES)
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@Produces(MediaType.APPLICATION_XML)
	@POST
	public Response processNamesCitation_post(@FormParam("names") String names) {
		return(processNamesCitation(names));
	}
	
	/**
	 * Parse a raw sequence of names from a header section and return the corresponding normalized authors.
	 * @param the string of the raw sequence of header authors 
	 * @return a response object containing the structured xml representation of the authors 
	 */
	@Path(PATH_CITE_NAMES)
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@Produces(MediaType.APPLICATION_XML)
	@PUT
	public Response processNamesCitation(@FormParam("names") String names) {
		LOGGER.debug(">> "+this.getClass().getName()+"."+Thread.currentThread().getStackTrace()[1]
			.getMethodName());
		Response response = null;
		String retVal = null;
		try {
			LOGGER.debug(">> set raw citation author sequence for stateless service'...");
			
		    //starts process
			List<Person> authors= GrobidFactory.instance.createEngine().processAuthorsCitation(names); 
			if (authors!= null)
			{
				if (authors.size()==1)
					retVal= authors.get(0).toString();
				else
					retVal = authors.toString();
			}
			
			if ( (retVal == null) || (retVal.isEmpty()) ) {
				response = Response.status(Status.NO_CONTENT).build();
			}
			else {
				response = Response.status(Status.OK).entity(retVal).type(MediaType.APPLICATION_XML).build();
			}
		}
		catch (Exception e) {
			//e.printStackTrace();
			LOGGER.error("An unexpected exception occurs. ",e);
		}
		LOGGER.debug("<< "+this.getClass().getName()+"."+Thread.currentThread().getStackTrace()[1]
			.getMethodName());
		return(response);
	}
	
	/**
	 * Parse a raw sequence of affiliations and return the corresponding normalized affiliations with address.
	 * @param the string of the raw sequence of affiliation+address
	 * @return a response object containing the structured xml representation of the affiliatoin 
	 */
	@Path(PATH_AFFILIATION)
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@Produces(MediaType.APPLICATION_XML)
	@POST
	public Response processAffiliations_post(@FormParam("affiliations") String affiliations) {
		return(processAffiliations(affiliations));
	}
	
	/**
	 * Parse a raw sequence of affiliations and return the corresponding normalized affiliations with address.
	 * @param the string of the raw sequence of affiliation+address
	 * @return a response object containing the structured xml representation of the affiliation 
	 */
	@Path(PATH_AFFILIATION)
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@Produces(MediaType.APPLICATION_XML)
	@PUT
	public Response processAffiliations(@FormParam("affiliation") String affiliation) {
		LOGGER.debug(">> "+this.getClass().getName()+"."+Thread.currentThread().getStackTrace()[1]
			.getMethodName());
		Response response = null;
		String retVal = null;
		try {
			LOGGER.debug(">> set raw affiliation + address blocks for stateless service'...");
			
		    //starts process
			List<Affiliation> affiliationList= GrobidFactory.instance.createEngine().processAffiliation(affiliation); 
			if (affiliationList!= null)
			{
				if (affiliationList.size()==1)
					retVal= affiliationList.get(0).toString();
				else
					retVal = affiliationList.toString();
			}
			if ( (retVal == null) || (retVal.isEmpty()) ) {
				response = Response.status(Status.NO_CONTENT).build();
			}
			else {
				response = Response.status(Status.OK).entity(retVal).type(MediaType.APPLICATION_XML).build();
			}
		}
		catch (Exception e) {
			//e.printStackTrace();
			LOGGER.error("An unexpected exception occurs. ",e);
		}
		LOGGER.debug("<< "+this.getClass().getName()+"."+Thread.currentThread().getStackTrace()[1]
			.getMethodName());
		return(response);
	}
}
