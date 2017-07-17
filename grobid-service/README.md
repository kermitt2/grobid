## Running service 

* From IntelliJ IDEA:
	* Just run the `main()` in `org.grobid.service.main.GrobidServiceApplication`
* Via Maven:
	* mvn exec:java
	
## Building a distribution

* `mvn package`
* The distribution can be found in `target/distribution`. An archive contains
  * A fat jar produced by a maven plugin
  * A bash script to start a server (`run-server.sh "$path-to-config"`) 
  * Documentation in `/doc`





