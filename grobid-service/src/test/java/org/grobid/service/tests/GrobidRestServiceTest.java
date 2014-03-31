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
package org.grobid.service.tests;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.ClientResponse.Status;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.container.httpserver.HttpServerFactory;
import com.sun.jersey.core.util.MultivaluedMapImpl;
import com.sun.jersey.multipart.FormDataMultiPart;
import com.sun.net.httpserver.HttpServer;
import org.apache.commons.io.FileUtils;
import org.grobid.core.mock.MockContext;
import org.grobid.core.utilities.TextUtilities;
import org.grobid.service.GrobidPathes;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Tests the RESTful service of the grobid-service project. This class can also
 * tests a remote system, when setting system property
 * org.grobid.service.test.uri to host to test.
 * 
 * @author Florian Zipser
 * 
 */
public class GrobidRestServiceTest {
	private static final Logger logger = LoggerFactory
			.getLogger(GrobidRestServiceTest.class);

	@BeforeClass
	public static void setInitialContext() throws Exception {
		MockContext.setInitialContext();
	}

	@AfterClass
	public static void destroyInitialContext() throws Exception {
		MockContext.destroyInitialContext();
	}
	
	private static HttpServer server = null;
	private static String host = null;

	public static String getHost() {
		return host;
		// return("http://localhost:8080/grobid-service-0.0.3-SNAPSHOT/grobid");
	}

	public void setHost(String host) {
		GrobidRestServiceTest.host = host;
	}

	public static final String PROP_TEST_HOST = "org.grobid.service.test.uri";

	/**
	 * Searches for a free unused port number and returns it.The port is in the
	 * range of 5000 .. 9999. If system property is given the port given as
	 * property will be returned.
	 * 
	 * @return port number
	 * @throws IOException
	 */
	public Integer findPort() throws IOException {
		Integer retVal = null;
		ServerSocket socket = null;
		for (int portNumber = 5000; portNumber < 10000; portNumber++) {
			try {
				socket = new ServerSocket(portNumber);
				retVal = portNumber;
				break;
			} catch (IOException e) {
			} finally {
				// Clean up
				if (socket != null)
					socket.close();
			}
		}
		return (retVal);
	}

	public static final String LOCALHOST = "http://localhost";

	@Before
	public void setUp() throws Exception {
		try {
			if (server == null) {
				String host = null;
				if (System.getProperty(PROP_TEST_HOST) != null) {
					host = System.getProperty(PROP_TEST_HOST);
					this.setHost(host);
					if (host == null)
						logger.warn("Cannot read value of system property '"
								+ PROP_TEST_HOST + "', because it is null.");
				}

				if (host == null) {
					int port = this.findPort();
					host = LOCALHOST + ":" + port + "/";
					this.setHost(host);
					logger.debug("started grobid-service for test on: " + getHost());
					server = HttpServerFactory.create(getHost());
					server.start();
				}
			}
		} catch (Exception e) {
			logger.error(e.getMessage());
			throw e;
		}
	}

	@After
	public void tearDown() throws Exception {
		if (server != null) {
			try {
				server.stop(0);
				server = null;
			} catch (Exception e) {
				logger.error(e.getMessage());
				throw e;
			}
		}
	}

	public static File getResourceDir() {
		return (new File("./src/test/resources/"));
	}

	/**
	 * test the synchronous fully state less rest call
	 */
	@Test
	public void testFullyRestLessHeaderDocument() throws Exception {
		File pdfFile = new File(getResourceDir().getAbsoluteFile()
				+ "/sample4/sample.pdf");
		Client create = Client.create();
		WebResource service = create.resource(getHost());
		ClientResponse response;

		assertTrue("Cannot run the test, because the sample file '" + pdfFile
				+ "' does not exists.", pdfFile.exists());
		FormDataMultiPart form = new FormDataMultiPart();
		form.field("input", pdfFile, MediaType.MULTIPART_FORM_DATA_TYPE);
		form.field("consolidate", "0", MediaType.MULTIPART_FORM_DATA_TYPE);
		logger.debug("calling " + getHost() + GrobidPathes.PATH_GROBID
				+ "/" + GrobidPathes.PATH_HEADER);

		service = Client.create().resource(
				getHost() + GrobidPathes.PATH_GROBID + "/"
						+ GrobidPathes.PATH_HEADER);
		response = service.type(MediaType.MULTIPART_FORM_DATA)
				.accept(MediaType.APPLICATION_XML)
				.post(ClientResponse.class, form);
		assertEquals(Status.OK.getStatusCode(), response.getStatus());

		InputStream inputStream = response.getEntity(InputStream.class);
		String tei = TextUtilities.convertStreamToString(inputStream);
		logger.debug(tei);
	}

	/**
	 * Test the synchronous fully state less rest call
	 */
	@Test
	public void testFullyRestLessFulltextDocument() throws Exception {
		File pdfFile = new File(getResourceDir().getAbsoluteFile()
				+ "/sample4/sample.pdf");
		Client create = Client.create();
		WebResource service = create.resource(getHost());
		ClientResponse response;

		assertTrue("Cannot run the test, because the sample file '" + pdfFile
				+ "' does not exists.", pdfFile.exists());
		FormDataMultiPart form = new FormDataMultiPart();
		form.field("input", pdfFile, MediaType.MULTIPART_FORM_DATA_TYPE);
		form.field("consolidate", "0", MediaType.MULTIPART_FORM_DATA_TYPE);
		logger.debug("calling " + getHost() + GrobidPathes.PATH_GROBID
				+ "/" + GrobidPathes.PATH_FULL_TEXT);

		service = Client.create().resource(
				getHost() + GrobidPathes.PATH_GROBID + "/"
						+ GrobidPathes.PATH_FULL_TEXT);
		response = service.type(MediaType.MULTIPART_FORM_DATA)
				.accept(MediaType.APPLICATION_XML)
				.post(ClientResponse.class, form);
		assertEquals(Status.OK.getStatusCode(), response.getStatus());

		InputStream inputStream = response.getEntity(InputStream.class);
		String tei = TextUtilities.convertStreamToString(inputStream);
		logger.debug(tei);
	}

	/**
	 * Test the synchronous state less rest call for dates
	 */
	@Test
	public void testRestDate() throws Exception {
		String date = "November 14 1999";
		Client create = Client.create();
		WebResource service = create.resource(getHost());
		ClientResponse response = null;

		MultivaluedMap<String, String> formData = new MultivaluedMapImpl();
		formData.add("date", date);

		service = Client.create().resource(
				this.getHost() + GrobidPathes.PATH_GROBID + "/"
						+ GrobidPathes.PATH_DATE);
		response = service.post(ClientResponse.class, formData);

		assertEquals(Status.OK.getStatusCode(), response.getStatus());
		String postResp = response.getEntity(String.class);

		logger.debug(postResp);
	}

	/**
	 * Test the synchronous state less rest call for author sequences in headers
	 */
	@Test
	public void testRestNamesHeader() throws Exception {
		String names = "Ahmed Abu-Rayyan *,a, Qutaiba Abu-Salem b, Norbert Kuhn * ,b, Cäcilia Maichle-Mößmer b";
		Client create = Client.create();
		WebResource service = create.resource(getHost());
		ClientResponse response;

		MultivaluedMap<String, String> formData = new MultivaluedMapImpl();
		formData.add("names", names);

		service = Client.create().resource(
				getHost() + GrobidPathes.PATH_GROBID + "/"
						+ GrobidPathes.PATH_HEADER_NAMES);
		response = service.post(ClientResponse.class, formData);

		assertEquals(Status.OK.getStatusCode(), response.getStatus());
		String postResp = response.getEntity(String.class);

		logger.debug(postResp);
	}

	/**
	 * Test the synchronous state less rest call for author sequences in
	 * citations
	 */
	@Test
	public void testRestNamesCitations() throws Exception {
		String names = "Marc Shapiro and Susan Horwitz";
		Client create = Client.create();
		WebResource service = create.resource(getHost());
		ClientResponse response;

		MultivaluedMap<String, String> formData = new MultivaluedMapImpl();
		formData.add("names", names);

		service = Client.create().resource(
				getHost() + GrobidPathes.PATH_GROBID + "/"
						+ GrobidPathes.PATH_CITE_NAMES);
		response = service.post(ClientResponse.class, formData);

		assertEquals(Status.OK.getStatusCode(), response.getStatus());
		String postResp = response.getEntity(String.class);

		logger.debug(postResp);
	}

	/**
	 * Test the synchronous state less rest call for affiliation + address
	 * blocks
	 */
	@Test
	public void testRestAffiliations() throws Exception {
		String affiliations = "Atomic Physics Division, Department of Atomic Physics and Luminescence, "
				+ "Faculty of Applied Physics and Mathematics, Gdansk University of "
				+ "Technology, Narutowicza 11/12, 80-233 Gdansk, Poland";
		Client create = Client.create();
		WebResource service = create.resource(getHost());
		ClientResponse response;

		MultivaluedMap<String, String> formData = new MultivaluedMapImpl();
		formData.add("affiliations", affiliations);

		service = Client.create().resource(
				getHost() + GrobidPathes.PATH_GROBID + "/"
						+ GrobidPathes.PATH_AFFILIATION);
		response = service.post(ClientResponse.class, formData);

		assertEquals(Status.OK.getStatusCode(), response.getStatus());
		String postResp = response.getEntity(String.class);

		logger.debug(postResp);
	}
	
	/**
	 *  Test the synchronous state less rest call for patent citation extraction. 
	 *  Send all xml and xml.gz ST36 files found in a given folder test/resources/patent
	 *  to the web service and write back the results in the test/sample 
	 */
	//@Test
	public void testRestPatentCitation() throws Exception {
		Client create = Client.create();
		WebResource service = create.resource(getHost());
		ClientResponse response;
		
		File xmlDirectory = new File(getResourceDir().getAbsoluteFile() + "/patent");
		for (final File currXML : xmlDirectory.listFiles()) {
			try {
				if (currXML.getName().toLowerCase().endsWith(".xml") || 
					currXML.getName().toLowerCase().endsWith(".xml.gz")) { 
						
					assertTrue("Cannot run the test, because the sample file '" + currXML
							+ "' does not exists.", currXML.exists());
					FormDataMultiPart form = new FormDataMultiPart();
					form.field("input", currXML, MediaType.MULTIPART_FORM_DATA_TYPE);
					form.field("consolidate", "0", MediaType.MULTIPART_FORM_DATA_TYPE);
					logger.debug("calling " + getHost() + GrobidPathes.PATH_GROBID
							+ "/" + GrobidPathes.PATH_CITATION_PATENT_ST36);

					service = Client.create().resource(
							getHost() + GrobidPathes.PATH_GROBID + "/"
									+ GrobidPathes.PATH_CITATION_PATENT_ST36);
					response = service.type(MediaType.MULTIPART_FORM_DATA)
							.accept(MediaType.APPLICATION_XML + ";charset=utf-8")
							.post(ClientResponse.class, form);
					assertEquals(Status.OK.getStatusCode(), response.getStatus());

					InputStream inputStream = response.getEntity(InputStream.class);
					String tei = TextUtilities.convertStreamToString(inputStream);
					//logger.debug(tei);
					
					File outputFile = new File(getResourceDir().getAbsoluteFile()+
						"/../sample/"+currXML.getName().replace(".xml",".tei.xml").replace(".gz",""));
					// writing the result in the sample directory
					FileUtils.writeStringToFile(outputFile, tei, "UTF-8");
				}
			}
			catch (final Exception exp) {
				logger.error("An error occured while processing the file " + currXML.getAbsolutePath()
						+ ". Continuing the process for the other files");
			}
		}	
	}
	

}
