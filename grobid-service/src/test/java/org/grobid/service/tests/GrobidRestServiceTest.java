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
package org.grobid.service.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;

import org.grobid.core.mock.MockContext;
import org.grobid.core.utilities.GrobidPropertyKeys;
import org.grobid.core.utilities.TextUtilities;
import org.grobid.service.GrobidPathes;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.ClientResponse.Status;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.container.httpserver.HttpServerFactory;
import com.sun.jersey.core.util.MultivaluedMapImpl;
import com.sun.jersey.multipart.FormDataMultiPart;
import com.sun.net.httpserver.HttpServer;

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
		this.host = host;
	}

	public static final String PROP_TEST_HOST = "org.grobid.service.test.uri";

	/**
	 * Searches for a free unused port number and returns it.The port is in the
	 * range of 5000 .. 9999. If system property is given the port given as
	 * property will be returned.
	 * 
	 * @return
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

		/*
		 * if (
		 * (System.getProperty(GrobidPropertyKeys.PROP_GROBID_HOME)==null)||
		 * (System.getProperty(GrobidPropertyKeys.PROP_GROBID_HOME).isEmpty()))
		 * {//set grobid home File grobidHome= new
		 * File(System.getProperty("user.dir")+"/../grobid-core/GROBID_HOME/");
		 * System.setProperty(GrobidPropertyKeys.PROP_GROBID_HOME,
		 * grobidHome.getCanonicalPath());
		 * logger.debug("System property '"+GrobidPropertyKeys
		 * .PROP_GROBID_HOME+"' was not set. Now it is set to folder '"
		 * +grobidHome.getAbsolutePath()+"'."); }
		 */// set grobid home
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
					logger.debug("started grobid-service for test on: "
							+ this.getHost());
					server = HttpServerFactory.create(this.getHost());
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
		File pdfFile = new File(this.getResourceDir().getAbsoluteFile()
				+ "/sample4/sample.pdf");
		Client create = Client.create();
		WebResource service = create.resource(getHost());
		ClientResponse response = null;

		assertTrue("Cannot run the test, because the sample file '" + pdfFile
				+ "' does not exists.", pdfFile.exists());
		FormDataMultiPart form = new FormDataMultiPart();
		form.field("fileContent", pdfFile, MediaType.MULTIPART_FORM_DATA_TYPE);
		logger.debug("calling " + this.getHost() + GrobidPathes.PATH_GROBID
				+ "/" + GrobidPathes.PATH_HEADER);

		service = Client.create().resource(
				this.getHost() + GrobidPathes.PATH_GROBID + "/"
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
		File pdfFile = new File(this.getResourceDir().getAbsoluteFile()
				+ "/sample4/sample.pdf");
		Client create = Client.create();
		WebResource service = create.resource(getHost());
		ClientResponse response = null;

		assertTrue("Cannot run the test, because the sample file '" + pdfFile
				+ "' does not exists.", pdfFile.exists());
		FormDataMultiPart form = new FormDataMultiPart();
		form.field("fileContent", pdfFile, MediaType.MULTIPART_FORM_DATA_TYPE);
		logger.debug("calling " + this.getHost() + GrobidPathes.PATH_GROBID
				+ "/" + GrobidPathes.PATH_FULL_TEXT);

		service = Client.create().resource(
				this.getHost() + GrobidPathes.PATH_GROBID + "/"
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
		ClientResponse response = null;

		MultivaluedMap<String, String> formData = new MultivaluedMapImpl();
		formData.add("names", names);

		service = Client.create().resource(
				this.getHost() + GrobidPathes.PATH_GROBID + "/"
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
		ClientResponse response = null;

		MultivaluedMap<String, String> formData = new MultivaluedMapImpl();
		formData.add("names", names);

		service = Client.create().resource(
				this.getHost() + GrobidPathes.PATH_GROBID + "/"
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
		ClientResponse response = null;

		MultivaluedMap<String, String> formData = new MultivaluedMapImpl();
		formData.add("affiliations", affiliations);

		service = Client.create().resource(
				this.getHost() + GrobidPathes.PATH_GROBID + "/"
						+ GrobidPathes.PATH_AFFILIATION);
		response = service.post(ClientResponse.class, formData);

		assertEquals(Status.OK.getStatusCode(), response.getStatus());
		String postResp = response.getEntity(String.class);

		logger.debug(postResp);
	}

	/**
	 * Test the synchronous fully state less rest call
	 */
	/*@Test
	public void testFullyRestLessFulltextDocumentLoop() throws Exception {
		List<ReqThread> list = new ArrayList<GrobidRestServiceTest.ReqThread>();
		for (int i = 0; i < 100; i++) {
			ReqThread thread = new ReqThread();
			list.add(thread);
			thread.start();
		}
		
		for(int i=0;i<list.size();i++){
			ReqThread thread = list.get(i);
			while (thread.isAlive()) {
				// faire un traitement...
				System.out.println("Thread " + i + " running");
				try {
					// et faire une pause
					Thread.sleep(800);
				} catch (InterruptedException ex) {
				}
			}
		}
		

	}

	private class ReqThread extends Thread {
		public void run() {
			long start = System.currentTimeMillis();
			// boucle tant que la durée de vie du Thread est < à 5 secondes
			while (System.currentTimeMillis() < (start + (1000 * 20))) {
				// traitement
				File pdfFile = new File(GrobidRestServiceTest.getResourceDir()
						.getAbsoluteFile() + "/sample4/sample.pdf");
				Client create = Client.create();
				WebResource service = create.resource(getHost());
				ClientResponse response = null;

				assertTrue("Cannot run the test, because the sample file '"
						+ pdfFile + "' does not exists.", pdfFile.exists());
				FormDataMultiPart form = new FormDataMultiPart();
				form.field("fileContent", pdfFile,
						MediaType.MULTIPART_FORM_DATA_TYPE);
				logger.debug("calling " + GrobidRestServiceTest.getHost()
						+ GrobidPathes.PATH_GROBID + "/"
						+ GrobidPathes.PATH_FULL_TEXT);

				service = Client.create().resource(
						GrobidRestServiceTest.getHost()
								+ GrobidPathes.PATH_GROBID + "/"
								+ GrobidPathes.PATH_FULL_TEXT);
				response = service.type(MediaType.MULTIPART_FORM_DATA)
						.accept(MediaType.APPLICATION_XML)
						.post(ClientResponse.class, form);
				assertEquals(Status.OK.getStatusCode(), response.getStatus());
				try {
					// pause
					Thread.sleep(500);
				} catch (InterruptedException ex) {
				}
			}
		}
	}*/

	// private void readProperties() throws FileNotFoundException, IOException
	// {
	// File propFile= new
	// File("./src/test/resources/grobidHost_private.properties");
	// if (!propFile.exists()) {
	// propFile= new File("./src/test/resources/grobidHost.properties");
	// if (!propFile.exists()) {
	// throw new
	// GrobidException("Cannot run tests for grobid service, because the property file for"
	// +
	// " grobid tests does not exist.");
	// }
	// }
	//
	// propFileName = propFile.getAbsolutePath();
	//
	// Properties props= new Properties();
	// FileInputStream in= null;
	// try
	// {
	// in= new FileInputStream(propFile);
	// props.load(in);
	// }
	// finally
	// {
	// in.close();
	// }
	//
	// if ( (props.getProperty("grobidHost")!= null) &&
	// (!props.getProperty("grobidHost").equals("")))
	// this.setHost(props.getProperty("grobidHost"));
	// else
	// fail("cannot find the host for the grobidService, please check configuration file: "+
	// propFileName);
	// if ( (props.getProperty("createHost")!= null) &&
	// (!props.getProperty("createHost").equals("")))
	// {
	// if (props.getProperty("createHost").equalsIgnoreCase("no"))
	// createHost= false;
	// }
	// }
	//
	// private String getHost()
	// {
	// return(this.getHost()+ "grobid/");
	// }
	//
	// private String resourceDir="./src/test/resources/";
	// private String tmpDir= null;
	//
	// public File getResourceDir()
	// {
	// File file= new File(resourceDir);
	// if (!file.exists())
	// {
	// if (!file.mkdirs())
	// throw new GrobidServiceException("Cannot create folder for resources.");
	// }
	// return(file);
	// }
	//
	// public File getTMPDir()
	// {
	// tmpDir= System.getProperty(GrobidPropertyKeys.PROP_TMP_PATH);
	// if (tmpDir== null)
	// throw new
	// GrobidException("Cannot start test, because tmp folder is not set.");
	// File file= new File(tmpDir);
	// if (!file.exists())
	// {
	// if (!file.mkdirs())
	// throw new GrobidServiceException("Cannot create temprorary folder.");
	// }
	// return(file);
	// }
	//
	// /**
	// * Checks if the service is alive, if this test fails, all the other will
	// also fail.
	// */
	// @Test
	// public void testIsAlive()
	// {
	// logger.debug("testIsAlive()...");
	// Client create = Client.create();
	// WebResource service = create.resource(getHost());
	// ClientResponse response= null;
	// response =
	// service.path("isAlive").accept(MediaType.TEXT_PLAIN).get(ClientResponse.class);
	// assertEquals(200, response.getStatus());
	// String isAlive= response.getEntity(String.class);
	// assertTrue(isAlive.equalsIgnoreCase("true"));
	// }
}
