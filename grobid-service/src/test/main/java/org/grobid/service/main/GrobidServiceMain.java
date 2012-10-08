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
package org.grobid.service.main;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Writer;
import java.net.ServerSocket;
import java.util.Vector;

import javax.ws.rs.core.MediaType;

import org.grobid.core.utilities.GrobidPropertyKeys;
import org.grobid.service.GrobidPathes;
import org.grobid.service.exceptions.GrobidServiceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.container.httpserver.HttpServerFactory;
import com.sun.jersey.multipart.FormDataMultiPart;
import com.sun.net.httpserver.HttpServer;


public class GrobidServiceMain {
	
	private static final Logger logger = LoggerFactory.getLogger(GrobidServiceMain.class);

	public static String sayHelo() {
		StringBuffer str= new StringBuffer();
		str.append("***********************************************************************************");
		str.append("\n");
		str.append("***\t\t\t\tWelcome to Grobid\t\t\t\t***");
		str.append("\n");
		str.append("***********************************************************************************");
		str.append("\n");
		return(str.toString());
	}
	
	public static String sayCommands() {
		StringBuffer str= new StringBuffer();
		str.append("---------------------------------------------------------------------------------------------------\n");
		str.append("commands:\n");
		str.append(CMD_STOP+"\t\t\t\t for stoping grobid service and exit\n");
		str.append(CMD_UPLOAD_PDF+" FILE \t for uploading the given pdf file\n");
		str.append("---------------------------------------------------------------------------------------------------\n");
		return(str.toString());
	}
	
	public static String sayBye() {
		StringBuffer str= new StringBuffer();
		str.append("***********************************************************************************");
		str.append("\n");
		str.append("***\t\t\t   Thank you for using Grobid   \t\t\t***");
		str.append("\n");
		str.append("***********************************************************************************");
		str.append("\n");
		return(str.toString());
	}
	
	/**
	 * Searches for a free unused port number and returns it.The port is in the range of 5000 .. 9999.
	 * @return
	 * @throws IOException
	 */
	public static Integer findPort() throws IOException 
	{
		Integer retVal = null;
		ServerSocket socket = null;
		for (int portNumber= 5000; portNumber< 10000; portNumber++)
		{
			try {
			    socket = new ServerSocket(portNumber);
			    retVal= portNumber;
			    break;
			} catch (IOException e) {
			} finally { 
			    // Clean up
			    if (socket != null) socket.close(); 
			}
		}
		return(retVal);
	}
	
	/**
	 * URI of localhost.
	 */
	public static final String LOCALHOST="http://localhost";
	
	/**
	 * command to stop grobid service
	 */
	public static final String CMD_STOP= "stop";
	
	/**
	 * command to stop grobid service
	 */
	public static final String CMD_UPLOAD_PDF= "upload_pdf";
	
	/**
	 * Argument passed via command line call for port number
	 */
	public static final String ARG_PORT= "-p";
	
	/**
	 * Argument to pass a URI to a remote grobid-service. If this argument is used, no server will be started. If this argument is used
	 * the argument {@value #ARG_PORT} will be ignored. 
	 */
	public static final String ARG_REMOTE= "-remote";
	
	/**
	 * The file name of the standard output file, if no output file is specified.
	 */
	public static final String FILE_DEFAULT_OUT= "output.tei"; 
	
	/**
	 * Starts the the grobid service on http://localhost on given port. if no port is given, the firt free
	 * port will be chosen. 
	 * Synopsis:
	 * -p port		- starts the the GROBIDService 
	 * @param args
	 * @throws IOException 
	 * @throws IllegalArgumentException 
	 */
	public static void main(String[] args) throws IllegalArgumentException 
	{
		System.out.println(sayHelo());
		HttpServer server= null;
		try
		{
			Integer port= null;
			String remoteLocation= null;
			
			if (args != null) 
			{// read arguments
				Vector<String> arguments= new Vector<String>();
				for (String arg: args) {
					arguments.add(arg);
				}
				
				if (arguments.contains(ARG_PORT)) {
					int portPos= arguments.indexOf(ARG_PORT)+1;
					if(arguments.size()< portPos+1) {
						throw new GrobidServiceException(
						"After the flag '"+ARG_PORT+"' Grobid need a port on which the GROBIDService can run (e.g. '-s 8080').");
					}
					port= Integer.valueOf(arguments.get(portPos));
				}
				if (arguments.contains(ARG_REMOTE)) {
					int remoteLocationPos= arguments.indexOf(ARG_REMOTE)+1;
					if(arguments.size()< remoteLocationPos+1) {
						throw new GrobidServiceException(
						"After the flag '"+ARG_REMOTE+"' grobid needs a uri of the remote grobid-service.");
					}
					remoteLocation= arguments.get(remoteLocationPos);
				}
			}// read arguments
			
			if (	(System.getProperty(GrobidPropertyKeys.PROP_GROBID_HOME)==null)||
					(System.getProperty(GrobidPropertyKeys.PROP_GROBID_HOME).isEmpty()))
			{//set grobid home
				File grobidHome= new File(System.getProperty("user.dir")+"/../grobid-core/GROBID_HOME/");
				System.setProperty(GrobidPropertyKeys.PROP_GROBID_HOME, grobidHome.getCanonicalPath());
				logger.debug("System property '"+GrobidPropertyKeys.PROP_GROBID_HOME+"' was not set. Now it is set to folder '"+grobidHome.getAbsolutePath()+"'.");
			}//set grobid home
			
			WebResource service = null;
			
			if (remoteLocation!= null)
			{//use remote grobid-service
				service = Client.create().resource(remoteLocation);
			}//use remote grobid-service
			else
			{//start local grobid-service
				if (port== null)
					port= findPort();
				server= HttpServerFactory.create(LOCALHOST+":"+port+"/");
				server.start();
				service = Client.create().resource(LOCALHOST+":"+port+"/");
			}//start local grobid-service
			
			ClientResponse response = null;
			
			//start: check is grobid-service is alive
				try
				{
					response = service.path(GrobidPathes.PATH_GROBID).path(GrobidPathes.PATH_IS_ALIVE).accept(MediaType.TEXT_PLAIN).get(ClientResponse.class);
					String isAliveStr= response.getEntity(String.class);
					Boolean isAlive= Boolean.valueOf(isAliveStr);
					if (!isAlive)
						throw new GrobidServiceException("grobid-service on host '"+service.getURI()+"' returned '"+Boolean.FALSE+"' for isalive.");
				}catch (Exception e) {
					System.err.println("Cannot connect to grobid-service on host '"+service.getURI()+"'. "+ e);
					System.exit(-1);
				}
				
			//end: check is grobid-service is alive
			
			
			logger.debug("started grobid-service for test on: "+ LOCALHOST+":"+port+"/");
			
			//  read from command line
				//  open up standard input
				BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
				System.out.print(sayCommands());

				String command = null;
				while (!CMD_STOP.equals(command))
				{
					System.out.print("grobid> ");
					command= br.readLine();
					
					if (	(command!= null)&&
							(!command.isEmpty()))
					{
						if (CMD_STOP.equals(command))
							break;
						else
						{// binary arguments
							String[] argumentParts= command.split(" ");
							if (CMD_UPLOAD_PDF.equals(argumentParts[0]))
							{
								if (argumentParts.length< 2)
									System.err.println("The number of arguments does not match for the command.");
								else
								{
									File outFile= null;
									if (	(argumentParts.length> 2)&&
											(">".equals(argumentParts[2])))
									{
										if (	(argumentParts[3]== null)||
												(argumentParts[3].isEmpty()))
										{
											System.err.println("There is no output file given after pipe operator '>'.");
										}
										outFile= new File(argumentParts[3]);
									}
									else 
									{
										outFile= new File("./"+FILE_DEFAULT_OUT);
									}
									File pdfFile = new File(argumentParts[1]);
									if (!pdfFile.exists())
									{
										System.err.println("The file '"+pdfFile.getAbsolutePath()+"' does not exists");
									}
									else
									{	
										FormDataMultiPart form = new FormDataMultiPart();
										form.field("fileContent", pdfFile, MediaType.MULTIPART_FORM_DATA_TYPE);
										response = service	.path(GrobidPathes.PATH_GROBID).path(GrobidPathes.PATH_FULL_TEXT)
															.type(MediaType.MULTIPART_FORM_DATA).accept(MediaType.APPLICATION_XML)
															.post(ClientResponse.class, form);
										
										String tei = response.getEntity(String.class);

										Writer output = null;
										try {
											output= new BufferedWriter(new FileWriter(outFile));
											output.write(tei);	
										} catch (Exception e) {
											System.err.println(e);
										}
										finally
										{
											System.out.println("output written to '"+outFile.getAbsolutePath()+"'. ");
											if (outFile!= null)
												output.close();
										}
									}
								}
							}
						}// binary arguments
					}
				}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		finally	{
			System.out.println(sayBye());
			if (server!= null)
				server.stop(0);
		}
	}
}
