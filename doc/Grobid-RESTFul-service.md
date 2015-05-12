## Services

GROBID REST services are documented in the following document: [grobid-service/src/main/doc/grobid-service_manual.pdf](https://github.com/kermitt2/grobid/blob/master/grobid-service/src/main/doc/grobid-service-manual.pdf)

The documentation covers the administrative API, the usage of the web console, the extraction and parsing API and gives some examples of usages with `curl` command lines. 

## Parallel mode

The Grobid RESTful API provides a simple and efficient way to use the library. 

The service can work following two modes:

+ Parallel execution (default): a pool of Grobid instances is used to process requests in parallel. The following property must be set to true in the file grobid-home/config/grobid_service.properties

	> org.grobid.service.is.parallel.execution=true

	As Grobid is thread safe and manages a pool of instances, it is possible to use several threads to call the REST service. This improves considerably the performance of the services for PDF processing because documents can be processed while other are uploaded. 

+ Sequencial execution: a single Grobid instance is used and process the requests as a queue. The following property must be set to false in the file grobid-home/config/grobid_service.properties

	> org.grobid.service.is.parallel.execution=false

	This mode is adapted for server running with a low amount of RAM.

