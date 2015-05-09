Under the main directory `grobid/`:
> mvn clean install

If you get test errors (normally you should not!), try compiling  with:
> mvn -Dmaven.test.skip=true clean install

or:

> mvn -DskipTests=true clean install
