In the main directory:
> mvn clean install

If you get an error (typically: "Caused by: org.apache.maven.plugin.MojoFailureException: There are test failures. Please refer to /path/to/grobid/grobid-core/target/surefire-reports for the individual test results."), try compiling  with:
> mvn -Dmaven.test.skip=true clean install

or:

> mvn -DskipTests=true clean install
