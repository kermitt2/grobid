## Running service 

* From IntelliJ IDEA:
	* Just run the `main()` in `org.grobid.service.main.GrobidServiceApplication`
* Via Gradle, under the project main repository `grobid/`:
	* `./gradlew grobid-service:run`
	
## Building a distribution

* under the project main repository `grobid/`: 
	* `./gradlew grobid-service:assemble`
* The distribution can be found in `grobid/grobid-service/build/distributions/`. An archive contains
  * All runtime dependencies
  * A bash script to start a server (`bin/grobid-service service "$path-to-yaml-config"`)
  * Documentation in `/doc`

## Running from a distribution

The `config.yaml` should be taken and adapted accordingly. Since the config values highly depend
on the environment and the deployment process, the `config.yaml` is not bundled with the distribution.
Things to change:

* Make sure to provide a path to a valid a grobid-home
* Ports might be adapted in `server.applicationConnectors.port` (default is `:8070`)
* Adapt the logging folders as needed, or remove file appender if you don't need file logging.



