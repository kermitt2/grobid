<h1>GROBID service API</h1>

The GROBID Web API provides a simple and efficient way to use the tool. A service console is available to test GROBID in a human friendly manner. For production and benchmarking, we strongly recommand to use this web service mode on a multi-core machine and to avoid running GROBID in the batch mode.  

## Start the server
Go under the `grobid/` main directory. Be sure that the GROBID project is built, see [Install GROBID](Install-Grobid.md).

The following command will start the server on the default port __8070__:
```bash
> ./gradlew run
```

You can check whether the service is up and running by opening the following URL: 

* `http://yourhost:8070/api/version` will return you the current version

* `http://yourhost:8070/api/isalive` will return true/false whether the service is up and running

## Configure the server

If required, modify the file under `grobid/grobid-service/config/config.yaml` for starting the server on a different port or if you need to change the absolute path to your `grobid-home` (e.g. when running on production). By default `grobid-home` is located under `grobid/grobid-home`. `grobid-home` contains all the models and static resources required to run GROBID. 

## Use GROBID test console

On your browser, the welcome page of the Service console is available at the URL `http://localhost:8070`

On the console, the RESTful API can be tested under the `TEI` tab for service returning a TEI document, under the `PDF` tab for services returning annotations relative to PDF or an annotated PDF and under the `Patent` tab for patent-related services:

![Example of GROBID Service console usage](img/grobid-rest-example.png)




## GROBID Web Services

We describe bellow the provided resources corresponding to the HTTP verbs, to use the grobid web services. All url described bellow are relative path, the root url is `http://<server instance name>/<root context>`

The consolidation parameters (__consolidateHeader__ and __consolidateCitations__) indicate if GROBID should try to complete the extracted metadata with an additional external call to [CrossRef API](https://github.com/CrossRef/rest-api-doc). The CrossRef look-up is realized based on the reliable subset of extracted metadata which are supported by this API.

### PDF to TEI conversion services

#### /api/processHeaderDocument

Extract the header of the input PDF document, normalize it and convert it into a TEI XML format.

_consolidateHeader_ is a string of value 0 (no consolidation) or 1 (consolidate, default value).


|   method	|  request type 	  | response type 		 |  parameters 	| requirement  	|   description				|
|---		|---				  |---					 |---			|---			|--- 						|
| POST, PUT	| multipart/form-data |   	application/xml  |   input		|   required	| PDF file to be processed 	|
|   		| 					  |						 |consolidateHeader| optional 	| consolidateHeader is a string of value 0 (no consolidation) or 1 (consolidate, default value) |

You can test this service with the **curl** command lines, for instance header extraction from a PDF file in the current directory:
```bash
curl -v --form input=@./thefile.pdf localhost:8070/api/processHeaderDocument
```

#### /api/processFulltextDocument

Convert the complete input document into TEI XML format (header, body and bibliographical section).

|   method	|  request type 	  | response type 		 |  parameters 	| requirement  	|   description				|
|---		|---				  |---					 |---			|---			|--- 						|
| POST, PUT	| multipart/form-data |   	application/xml  |   input		|   required	| PDF file to be processed 	|
|   		| 					  |						 |consolidateHeader| optional 	| consolidateHeader is a string of value 0 (no consolidation) or 1 (consolidate, default value) |
|   		| 					  |						 |consolidateCitations| optional | consolidateCitations is a string of value 0 (no consolidation, default value) or 1 (consolidate all found citations) |

You can test this service with the **curl** command lines, for instance fulltext extraction (header, body and citations) from a PDF file in the current directory:
```bash
curl -v --form input=@./thefile.pdf localhost:8070/api/processFulltextDocument
```

#### /api/processReferences

Extract and convert all the bibliographical references present in the input document into TEI XML format. 

|   method	|  request type 	  | response type 		 |  parameters 	| requirement  	|   description				|
|---		|---				  |---					 |---			|---			|--- 						|
| POST, PUT	| multipart/form-data |   	application/xml  |   input		|   required	| PDF file to be processed 	|
|   		| 					  |						 |consolidateCitations| optional 	| consolidateCitations is a string of value 0 (no consolidation, default value) or 1 (consolidate all found citations) |


You can test this service with the **curl** command lines, for instance extraction and parsing of all references from a PDF in the current directory without consolidation (default value):
```bash
curl -v --form input=@./thefile.pdf localhost:8070/api/processReferences
```

### Raw text to TEI conversion services

#### /api/processDate

Parse a raw date string and return the corresponding normalized date in ISO 8601 embedded in a TEI fragment.

|   method	|  request type 	  | response type 		 |  parameters 	| requirement  	|   description				|
|---		|---				  |---					 |---			|---			|--- 						|
| POST, PUT	| application/x-www-form-urlencoded | application/xml  	| date | required	| date to be parsed as raw string|


You can test this service with the **curl** command lines, for instance parsing of a raw date string:
```bash
curl -X POST -d "date=September 16th, 2001" localhost:8070/api/processDate
```
which will return:
```xml
<date when="2001-9-16" />
```

#### /api/processHeaderNames

Parse a raw string corresponding to a name or a sequence of names from a header section and return the corresponding normalized authors in TEI format.

|   method	|  request type 	  | response type 		 |  parameters 	| requirement  	|   description				|
|---		|---				  |---					 |---			|---			|--- 						|
| POST, PUT	| application/x-www-form-urlencoded | application/xml  	| names | required	| sequence of names to be parsed as raw string|


You can test this service with the **curl** command lines, for instance parsing of a raw sequence of header names string:
```bash
curl -X POST -d "names=John Doe and Jane Smith" localhost:8070/api/processHeaderNames
```
which will return:
```xml
<persName xmlns="http://www.tei-c.org/ns/1.0">
	<forename type="first">John</forename>
	<surname>Doe</surname>
</persName>
<persName xmlns="http://www.tei-c.org/ns/1.0">
	<forename type="first">Jane</forename>
	<surname>Smith</surname>
</persName>
```

#### /api/processCitationNames

Parse a raw sequence of names from a bibliographical reference and return the corresponding normalized authors in TEI format.

|   method	|  request type 	  | response type 		 |  parameters 	| requirement  	|   description				|
|---		|---				  |---					 |---			|---			|--- 						|
| POST, PUT	| application/x-www-form-urlencoded | application/xml  	| names | required	| sequence of names to be parsed as raw string|

You can test this service with the **curl** command lines, for instance parsing of a raw sequence of citation names string:
```bash
curl -X POST -d "names=J. Doe, J. Smith and B. M. Jackson" localhost:8070/api/processCitationNames
```
which will return:
```xml
<persName xmlns="http://www.tei-c.org/ns/1.0">
	<forename type="first">J</forename>
	<surname>Doe</surname>
</persName>
<persName xmlns="http://www.tei-c.org/ns/1.0">
	<forename type="first">J</forename>
	<surname>Smith</surname>
</persName>
<persName xmlns="http://www.tei-c.org/ns/1.0">
	<forename type="first">B</forename>
	<forename type="middle">M</forename>
	<surname>Jackson</surname>
</persName>
```

#### /api/processAffiliations

Parse a raw sequence of affiliations with or without address and return the corresponding normalized affiliations with address in TEI format.

|   method	|  request type 	  | response type 		 |  parameters 	| requirement  	|   description				|
|---		|---				  |---					 |---			|---			|--- 						|
| POST, PUT	| application/x-www-form-urlencoded | application/xml  	| affiliations | required	| sequence of affiliations+addresses to be parsed as raw string|

You can test this service with the **curl** command lines, for instance parsing of a raw affiliation string:
```bash
curl -X POST -d "affiliations=Stanford University, California, USA" localhost:8070/api/processAffiliations
```

which will return:
```xml
<affiliation>
	<orgName type="institution">Stanford University</orgName>
	<address>
		<region>California</region>
		<country key="US">USA</country>
	</address>
</affiliation
```

#### /api//processCitation

Parse a raw bibliographical reference (in isolation) and return the corresponding normalized bibliographical reference in TEI format.

|   method	|  request type 	  | response type 		 |  parameters 	| requirement  	|   description				|
|---		|---				  |---					 |---			|---			|--- 						|
| POST, PUT	| application/x-www-form-urlencoded | application/xml  	| citations | required	| bibliographical reference to be parsed as raw string|
|   		| 					  |						 |consolidateCitations| optional | consolidateCitations is a string of value 0 (no consolidation, default value) or 1 (consolidate the citation) |

You can test this service with the **curl** command lines, for instance parsing of a raw bibliographical reference string in isolation without consolidation (default value):
```bash
curl -X POST -d "citations=Graff, Expert. Opin. Ther. Targets (2002) 6(1): 103-113" localhost:8070/api/processCitation
```

which will return:
```xml
<biblStruct >
	<analytic>
		<title/>
		<author>
			<persName xmlns="http://www.tei-c.org/ns/1.0"><surname>Graff</surname></persName>
		</author>
	</analytic>
	<monogr>
		<title level="j">Expert. Opin. Ther. Targets</title>
		<imprint>
			<biblScope unit="volume">6</biblScope>
			<biblScope unit="issue">1</biblScope>
			<biblScope unit="page" from="103" to="113" />
			<date type="published" when="2002" />
		</imprint>
	</monogr>
</biblStruct>

```


### PDF annotation services




### Citation extraction and normalization from patents

#### /api/processCitationPatentTXT

Extract and parse the patent and non patent citations in the description of a patent sent as UTF-8 text. Results are returned as a lists of TEI citations. 

|   method	|  request type 	  | response type 		 |  parameters 	| requirement  	|   description				|
|---		|---				  |---					 |---			|---			|--- 						|
| POST, PUT	| application/x-www-form-urlencoded | application/xml  	| input | required	| patent text to be processed as raw string|
|   		| 					  |						 |consolidateCitations| optional | consolidateCitations is a string of value 0 (no consolidation, default value) or 1 (consolidate the citation) |


You can test this service with the **curl** command lines, for instance parsing of a raw bibliographical reference string in isolation without consolidation (default value):
```bash
curl -X POST -d "input=In European Patent A-123456 nothing interesting." localhost:8070/api/processCitationPatentTXT
```

#### /api/processCitationPatentTEI

Extract and parse the patent and non patent citations in the description of a patent encoded in TEI (Patent Document Model). Results are added to the original document as TEI stand-off annotations.

|   method	|  request type 	  | response type 		 |  parameters 	| requirement  	|   description				|
|---		|---				  |---					 |---			|---			|--- 						|
| POST, PUT	| multipart/form-data | application/xml  	| input | required	| TEI file of the patent document to be processed |
|   		| 					  |						 |consolidateCitations| optional | consolidateCitations is a string of value 0 (no consolidation, default value) or 1 (consolidate the citation) |



#### /api/processCitationPatentST36

Extract and parse the patent and non patent citations in the description of a patent encoded in ST.36. Results are returned as a lits of TEI citations. 


#### /api/processCitationPatentPDF

Extract and parse the patent and non patent citations in the description of a patent sent as PDF. Results are returned as a lits of TEI citations.




### Administration services




#### Configuration of the password for the service adminstration

A password is required to access the administration page under the `Admin` tab in the console. The default password for the administration console is **admin**.

For security, the password is saved as SHA1 hash in the file `grobid-service/src/main/conf/grobid_service.properties` with the property name `org.grobid.service.admin.pw`

To change the password, you can replace this property value by the SHA1 hash generated for your new password of choice. To generate the SHA1 from any `<input_string>`, you can use the corresponding Grobid REST service available at:

> http://localhost:8070/api/sha1?sha1=`<input_string>`


## Parallel mode

The Grobid RESTful API provides a very efficient way to use the library out of the box, because the service exploits multithreading.

The service can work following two modes:

+ Parallel execution (default): a pool of threads is used to process requests in parallel. The following property must be set to true in the file grobid-home/config/grobid_service.properties

```java
	org.grobid.service.is.parallel.execution=true
```

As Grobid is thread safe and manages a pool of parser instances, it is also possible to use several threads to call the REST service. This improves considerably the performance of the services for PDF processing because documents can be processed while other are uploaded. 

+ Sequencial execution: a single Grobid instance is used and process the requests as a queue. The following property must be set to false in the file grobid-home/config/grobid_service.properties

```java
	org.grobid.service.is.parallel.execution=false
```

This mode is adapted for server running with a low amount of RAM, for instance less than 2GB, otherwise the default parallel execution must be used. 



