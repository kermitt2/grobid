package org.grobid.service.process;

import java.util.List;
import java.util.ArrayList;
import java.util.NoSuchElementException;

import javax.ws.rs.core.HttpHeaders;
import javax.inject.Inject;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import com.google.inject.Singleton;
import org.grobid.core.data.Affiliation;
import org.grobid.core.data.BiblioItem;
import org.grobid.core.data.PatentItem;
import org.grobid.core.data.BibDataSet;
import org.grobid.core.data.Date;
import org.grobid.core.data.Person;
import org.grobid.core.engines.Engine;
import org.grobid.core.factory.GrobidPoolingFactory;
import org.grobid.service.util.GrobidRestUtils;
import org.grobid.service.util.GrobidServiceProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * Web services consuming String
 * 
 */
@Singleton
public class GrobidRestProcessString {

	private static final Logger LOGGER = LoggerFactory.getLogger(GrobidRestProcessString.class);

	@Inject
	public GrobidRestProcessString() {

	}

	/**
	 * Parse a raw date and return the corresponding normalized date.
	 * 
	 * @param date raw date string
	 * @return a response object containing the structured xml representation of
	 *         the date
	 */
	public Response processDate(String date) {
		LOGGER.debug(methodLogIn());
		Response response = null;
		String retVal = null;
		boolean isparallelExec = GrobidServiceProperties.isParallelExec();
		Engine engine = null;
		try {
			LOGGER.debug(">> set raw date for stateless service'...");
			
			engine = Engine.getEngine(isparallelExec);
			List<Date> dates;
			date = date.replaceAll("\\n", " ").replaceAll("\\t", " ");
			if (isparallelExec) {
				dates = engine.processDate(date);
			} else {
				synchronized (engine) {
					dates = engine.processDate(date);
				}
			}
			if (dates != null) {
				for(Date theDate : dates)	{
					if (retVal == null) {
						retVal = "";
					}
					retVal += theDate.toTEI();
				}
			}

			if (GrobidRestUtils.isResultNullOrEmpty(retVal)) {
				response = Response.status(Status.NO_CONTENT).build();
			} else {
				response = Response.status(Status.OK)
                            .entity(retVal)
                            .header(HttpHeaders.CONTENT_TYPE, MediaType.TEXT_PLAIN + "; charset=UTF-8")
                            .header("Access-Control-Allow-Origin", "*")
                            .header("Access-Control-Allow-Methods", "GET, POST, DELETE, PUT").build();
			}
		} catch (NoSuchElementException nseExp) {
			LOGGER.error("Could not get an engine from the pool within configured time. Sending service unavailable.");
			response = Response.status(Status.SERVICE_UNAVAILABLE).build();
		} catch (Exception e) {
			LOGGER.error("An unexpected exception occurs. ", e);
			response = Response.status(Status.INTERNAL_SERVER_ERROR).build();
		} finally {
			if (isparallelExec && engine != null) {
				GrobidPoolingFactory.returnEngine(engine);
			}
		}
		LOGGER.debug(methodLogOut());
		return response;
	}

	/**
	 * Parse a raw sequence of names from a header section and return the
	 * corresponding normalized authors.
	 * 
	 * @param names string of the raw sequence of header authors
	 * @return a response object containing the structured xml representation of
	 *         the authors
	 */
	public Response processNamesHeader(String names) {
		LOGGER.debug(methodLogIn());
		Response response = null;
		String retVal = null;
		boolean isparallelExec = GrobidServiceProperties.isParallelExec();
		Engine engine = null;
		try {
			LOGGER.debug(">> set raw header author sequence for stateless service'...");

			engine = Engine.getEngine(isparallelExec);
			List<Person> authors;
			names = names.replaceAll("\\n", " ").replaceAll("\\t", " ");
			if (isparallelExec) {
				authors = engine.processAuthorsHeader(names);
			} else {
				synchronized (engine) {
					authors = engine.processAuthorsHeader(names);
				}
			}

			if (authors != null) {
				for(Person person : authors) {
					if (retVal == null) {
						retVal = "";
					}
					retVal += person.toTEI(false);
				}
			}

			if (GrobidRestUtils.isResultNullOrEmpty(retVal)) {
				response = Response.status(Status.NO_CONTENT).build();
			} else {
				//response = Response.status(Status.OK).entity(retVal).type(MediaType.TEXT_PLAIN).build();
				response = Response.status(Status.OK)
                            .entity(retVal)
                            .header(HttpHeaders.CONTENT_TYPE, MediaType.TEXT_PLAIN + "; charset=UTF-8")
                            .header("Access-Control-Allow-Origin", "*")
                            .header("Access-Control-Allow-Methods", "GET, POST, DELETE, PUT").build();
			}
		} catch (NoSuchElementException nseExp) {
			LOGGER.error("Could not get an engine from the pool within configured time. Sending service unavailable.");
			response = Response.status(Status.SERVICE_UNAVAILABLE).build();
		} catch (Exception e) {
			LOGGER.error("An unexpected exception occurs. ", e);
			response = Response.status(Status.INTERNAL_SERVER_ERROR).build();
		} finally {
			if (isparallelExec && engine != null) {
				GrobidPoolingFactory.returnEngine(engine);
			}
		}
		LOGGER.debug(methodLogOut());
		return response;
	}

	/**
	 * Parse a raw sequence of names from a header section and return the
	 * corresponding normalized authors.
	 * 
	 * @param names string of the raw sequence of header authors.
	 * @return a response object containing the structured xml representation of
	 *         the authors
	 */
	public Response processNamesCitation(String names) {
		LOGGER.debug(methodLogIn());
		Response response = null;
		String retVal = null;
		boolean isparallelExec = GrobidServiceProperties.isParallelExec();
		Engine engine = null;
		try {
			LOGGER.debug(">> set raw citation author sequence for stateless service'...");

			engine = Engine.getEngine(isparallelExec);
			List<Person> authors;
			names = names.replaceAll("\\n", " ").replaceAll("\\t", " ");
			if (isparallelExec) {
				authors = engine.processAuthorsCitation(names);
			} else {
				synchronized (engine) {
					authors = engine.processAuthorsCitation(names);
				}
			}

			if (authors != null) {
				for(Person person : authors) {
					if (retVal == null) {
						retVal = "";
					}
					retVal += person.toTEI(false);
				}
			}

			if (GrobidRestUtils.isResultNullOrEmpty(retVal)) {
				response = Response.status(Status.NO_CONTENT).build();
			} else {
				//response = Response.status(Status.OK).entity(retVal).type(MediaType.TEXT_PLAIN).build();
				response = Response.status(Status.OK)
                            .entity(retVal)
                            .header(HttpHeaders.CONTENT_TYPE, MediaType.TEXT_PLAIN + "; charset=UTF-8")
                            .header("Access-Control-Allow-Origin", "*")
                            .header("Access-Control-Allow-Methods", "GET, POST, DELETE, PUT").build();
			}
		} catch (NoSuchElementException nseExp) {
			LOGGER.error("Could not get an engine from the pool within configured time. Sending service unavailable.");
			response = Response.status(Status.SERVICE_UNAVAILABLE).build();
		} catch (Exception e) {
			LOGGER.error("An unexpected exception occurs. ", e);
			response = Response.status(Status.INTERNAL_SERVER_ERROR).build();
		} finally {
			if (isparallelExec && engine != null) {
				GrobidPoolingFactory.returnEngine(engine);
			}
		}
		LOGGER.debug(methodLogOut());
		return response;
	}

	/**
	 * Parse a raw sequence of affiliations and return the corresponding
	 * normalized affiliations with address.
	 * 
	 * @param affiliation of the raw sequence of affiliation+address
	 * @return a response object containing the structured xml representation of
	 *         the affiliation
	 */
	public Response processAffiliations(String affiliation) {
		LOGGER.debug(methodLogIn());
		Response response = null;
		String retVal = null;
		boolean isparallelExec = GrobidServiceProperties.isParallelExec();
		Engine engine = null;
		try {
			LOGGER.debug(">> set raw affiliation + address blocks for stateless service'...");

			engine = Engine.getEngine(isparallelExec);
			List<Affiliation> affiliationList;
			//affiliation = affiliation.replaceAll("\\n", " ").replaceAll("\\t", " ");
			affiliation = affiliation.replaceAll("\\t", " ");
			if (isparallelExec) {
				affiliationList = engine.processAffiliation(affiliation);
			} else {
				synchronized (engine) {
					affiliationList = engine.processAffiliation(affiliation);
				}
			}

			if (affiliationList != null) {				
				for(Affiliation affi : affiliationList) {
					if (retVal == null) {
						retVal = "";
					}
					retVal += affi.toTEI();
				}	
			}
			if (GrobidRestUtils.isResultNullOrEmpty(retVal)) {
				response = Response.status(Status.NO_CONTENT).build();
			} else {
				//response = Response.status(Status.OK).entity(retVal).type(MediaType.TEXT_PLAIN).build();
				response = Response.status(Status.OK)
                            .entity(retVal)
                            .header(HttpHeaders.CONTENT_TYPE, MediaType.TEXT_PLAIN + "; charset=UTF-8")
                            .header("Access-Control-Allow-Origin", "*")
                            .header("Access-Control-Allow-Methods", "GET, POST, DELETE, PUT").build();
			}
		} catch (NoSuchElementException nseExp) {
			LOGGER.error("Could not get an engine from the pool within configured time. Sending service unavailable.");
			response = Response.status(Status.SERVICE_UNAVAILABLE).build();
		} catch (Exception e) {
			LOGGER.error("An unexpected exception occurs. ", e);
			response = Response.status(Status.INTERNAL_SERVER_ERROR).build();
		} finally {
			if (isparallelExec && engine != null) {
				GrobidPoolingFactory.returnEngine(engine);
			}
		}
		LOGGER.debug(methodLogOut());
		return response;
	}

	/**
	 * Parse a raw sequence of affiliations and return the corresponding
	 * normalized affiliations with address.
	 * 
	 * @param citation
	 *			string of the raw sequence of affiliation+address
	 * @param consolidate
	 *            consolidation parameter for the parsed citation
	 * @return a response object containing the structured xml representation of
	 *         the affiliation
	 */
	public Response processCitation(String citation, boolean consolidate) {
		LOGGER.debug(methodLogIn());
		Response response = null;

		boolean isparallelExec = GrobidServiceProperties.isParallelExec();
		Engine engine = null;
		try {
			engine = Engine.getEngine(isparallelExec);
			BiblioItem biblioItem;
			//citation = citation.replaceAll("\\n", " ").replaceAll("\\t", " ");
			if (isparallelExec) {
				biblioItem = engine.processRawReference(citation, consolidate);
			} else {
				synchronized (engine) {
					biblioItem = engine.processRawReference(citation, consolidate);
				}
			}

			if (biblioItem == null) {
				response = Response.status(Status.NO_CONTENT).build();
			} else {
				//response = Response.status(Status.OK).entity(biblioItem.toTEI(-1)).type(MediaType.APPLICATION_XML).build();
				response = Response.status(Status.OK)
                            .entity(biblioItem.toTEI(-1))
                            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_XML + "; charset=UTF-8")
                            .header("Access-Control-Allow-Origin", "*")
                            .header("Access-Control-Allow-Methods", "GET, POST, DELETE, PUT").build();
			}
		} catch (NoSuchElementException nseExp) {
			LOGGER.error("Could not get an engine from the pool within configured time. Sending service unavailable.");
			response = Response.status(Status.SERVICE_UNAVAILABLE).build();
		} catch (Exception e) {
			LOGGER.error("An unexpected exception occurs. ", e);
			response = Response.status(Status.INTERNAL_SERVER_ERROR).build();
		} finally {
			if (isparallelExec && engine != null) {
				GrobidPoolingFactory.returnEngine(engine);
			}
		}
		LOGGER.debug(methodLogOut());
		return response;
	}

	/**
	 * Parse a patent description text and return the extracted and parsed patent and non-patent citations.
	 * 
	 * @param text
	 *            string of the patent description text to be processed
 	 * @param consolidate
	 *            consolidation parameter for the non patent extracted and parsed citation
	 * 
	 * @return a response object containing the structured xml representation of
	 *         the affiliation
	 */
	public Response processCitationPatentTXT(String text, boolean consolidate) {
		LOGGER.debug(methodLogIn());
		Response response = null;

		boolean isparallelExec = GrobidServiceProperties.isParallelExec();
		Engine engine = null;
		try {
			engine = Engine.getEngine(isparallelExec);
			
			List<PatentItem> patents = new ArrayList<PatentItem>();
			List<BibDataSet> articles = new ArrayList<BibDataSet>();						
			text = text.replaceAll("\\t", " ");
			String result = null;
			
			if (isparallelExec) {
				result = engine.processAllCitationsInPatent(text, articles, patents, consolidate);
			} else {
				synchronized (engine) {
					result = engine.processAllCitationsInPatent(text, articles, patents, consolidate);
				}
			}

			if (result == null) {
				response = Response.status(Status.NO_CONTENT).build();
			} else {
				//response = Response.status(Status.OK).entity(result).type(MediaType.APPLICATION_XML).build();
				response = Response.status(Status.OK)
                            .entity(result)
                            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_XML + "; charset=UTF-8")
                            .header("Access-Control-Allow-Origin", "*")
                            .header("Access-Control-Allow-Methods", "GET, POST, DELETE, PUT").build();
			}
		} catch (NoSuchElementException nseExp) {
			LOGGER.error("Could not get an engine from the pool within configured time. Sending service unavailable.");
			response = Response.status(Status.SERVICE_UNAVAILABLE).build();
		} catch (Exception e) {
			LOGGER.error("An unexpected exception occurs. ", e);
			response = Response.status(Status.INTERNAL_SERVER_ERROR).build();
		} finally {
			if (isparallelExec && engine != null) {
				GrobidPoolingFactory.returnEngine(engine);
			}
		}
		LOGGER.debug(methodLogOut());
		return response;
	}

	public String methodLogIn() {
		return ">> " + GrobidRestProcessString.class.getName() + "." + Thread.currentThread().getStackTrace()[1].getMethodName();
	}

	public String methodLogOut() {
		return "<< " + GrobidRestProcessString.class.getName() + "." + Thread.currentThread().getStackTrace()[1].getMethodName();
	}

}
