package org.grobid.service.process;

import java.util.List;
import java.util.NoSuchElementException;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.grobid.core.data.Affiliation;
import org.grobid.core.data.BiblioItem;
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
 * @author Damien
 * 
 */
public class GrobidRestProcessString {

	/**
	 * The class Logger.
	 */
	private static final Logger LOGGER = LoggerFactory.getLogger(GrobidRestProcessString.class);

	/**
	 * Parse a raw date and return the corresponding normalized date.
	 * 
	 * @param the
	 *            raw date string
	 * @return a response object containing the structured xml representation of
	 *         the date
	 */
	public static Response processDate(String date) {
		LOGGER.debug(methodLogIn());
		Response response = null;
		String retVal = null;
		boolean isparallelExec = GrobidServiceProperties.isParallelExec();
		Engine engine = null;
		try {
			LOGGER.debug(">> set raw date for stateless service'...");

			engine = GrobidRestUtils.getEngine(isparallelExec);
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
				if (dates.size() == 1)
					retVal = dates.get(0).toString();
				else
					retVal = dates.toString();
			}

			if (!GrobidRestUtils.isResultOK(retVal)) {
				response = Response.status(Status.NO_CONTENT).build();
			} else {
				response = Response.status(Status.OK).entity(retVal).type(MediaType.TEXT_PLAIN).build();
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
	 * @param the
	 *            string of the raw sequence of header authors
	 * @return a response object containing the structured xml representation of
	 *         the authors
	 */
	public static Response processNamesHeader(String names) {
		LOGGER.debug(methodLogIn());
		Response response = null;
		String retVal = null;
		boolean isparallelExec = GrobidServiceProperties.isParallelExec();
		Engine engine = null;
		try {
			LOGGER.debug(">> set raw header author sequence for stateless service'...");

			engine = GrobidRestUtils.getEngine(isparallelExec);
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
				if (authors.size() == 1)
					retVal = authors.get(0).toString();
				else
					retVal = authors.toString();
			}

			if (!GrobidRestUtils.isResultOK(retVal)) {
				response = Response.status(Status.NO_CONTENT).build();
			} else {
				response = Response.status(Status.OK).entity(retVal).type(MediaType.TEXT_PLAIN).build();
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
	 * @param the
	 *            string of the raw sequence of header authors.
	 * @return a response object containing the structured xml representation of
	 *         the authors
	 */
	public static Response processNamesCitation(String names) {
		LOGGER.debug(methodLogIn());
		Response response = null;
		String retVal = null;
		boolean isparallelExec = GrobidServiceProperties.isParallelExec();
		Engine engine = null;
		try {
			LOGGER.debug(">> set raw citation author sequence for stateless service'...");

			engine = GrobidRestUtils.getEngine(isparallelExec);
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
				if (authors.size() == 1)
					retVal = authors.get(0).toString();
				else
					retVal = authors.toString();
			}

			if (!GrobidRestUtils.isResultOK(retVal)) {
				response = Response.status(Status.NO_CONTENT).build();
			} else {
				response = Response.status(Status.OK).entity(retVal).type(MediaType.TEXT_PLAIN).build();
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
	 * @param the
	 *            string of the raw sequence of affiliation+address
	 * @return a response object containing the structured xml representation of
	 *         the affiliation
	 */
	public static Response processAffiliations(String affiliation) {
		LOGGER.debug(methodLogIn());
		Response response = null;
		String retVal = null;
		boolean isparallelExec = GrobidServiceProperties.isParallelExec();
		Engine engine = null;
		try {
			LOGGER.debug(">> set raw affiliation + address blocks for stateless service'...");

			engine = GrobidRestUtils.getEngine(isparallelExec);
			List<Affiliation> affiliationList;
			affiliation = affiliation.replaceAll("\\n", " ").replaceAll("\\t", " ");
			if (isparallelExec) {
				affiliationList = engine.processAffiliation(affiliation);
			} else {
				synchronized (engine) {
					affiliationList = engine.processAffiliation(affiliation);
				}
			}

			if (affiliationList != null) {
				if (affiliationList.size() == 1)
					retVal = affiliationList.get(0).toString();
				else
					retVal = affiliationList.toString();
			}
			if (!GrobidRestUtils.isResultOK(retVal)) {
				response = Response.status(Status.NO_CONTENT).build();
			} else {
				response = Response.status(Status.OK).entity(retVal).type(MediaType.TEXT_PLAIN).build();
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
	 * @param the
	 *            string of the raw sequence of affiliation+address
	 * @return a response object containing the structured xml representation of
	 *         the affiliation
	 */
	public static Response processCitations(String citation) {
		LOGGER.debug(methodLogIn());
		Response response = null;

		boolean isparallelExec = GrobidServiceProperties.isParallelExec();
		Engine engine = null;
		try {
			engine = GrobidRestUtils.getEngine(isparallelExec);
			BiblioItem biblioItem;
			citation = citation.replaceAll("\\n", " ").replaceAll("\\t", " ");
			if (isparallelExec) {
				biblioItem = engine.processRawReference(citation, false);
			} else {
				synchronized (engine) {
					biblioItem = engine.processRawReference(citation, false);
				}
			}

			if (biblioItem == null) {
				response = Response.status(Status.NO_CONTENT).build();
			} else {
				response = Response.status(Status.OK).entity(biblioItem.toTEI(-1)).type(MediaType.APPLICATION_XML).build();
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
	 * @return
	 */
	public static String methodLogIn() {
		return ">> " + GrobidRestProcessString.class.getName() + "." + Thread.currentThread().getStackTrace()[1].getMethodName();
	}

	/**
	 * @return
	 */
	public static String methodLogOut() {
		return "<< " + GrobidRestProcessString.class.getName() + "." + Thread.currentThread().getStackTrace()[1].getMethodName();
	}

}
