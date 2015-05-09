-
<!--- ## [Download sources from github](https://github.com/kermitt2/grobid/wiki/Download-grobid-from-github) -->

Clone source code from github:
> git clone https://github.com/grobid/grobid.git

Or download directly the zip file:
> https://github.com/grobid/grobid/zipball/master

<!--- ## [Build the project](https://github.com/kermitt2/grobid/wiki/Build-the-project) -->

The standard method for building the project is to use maven. In the main directory:
> mvn clean install

You can skip the tests as follow:
> mvn -Dmaven.test.skip=true clean install

It is also possible to build the project with ant. This could be useful for integrating Grobid in an ant project, or when no internet connection is available in a secure development environment, or for people allergic to maven. Supported ant targets are `compile`, `clean`, `test` and `package`. 

## Lauch the server
Go in grobid-service directory:
> cd grobid-service

Run the following comand:
> mvn jetty:run-war

To skip the tests:
> mvn -Dmaven.test.skip=true jetty:run-war

## Test grobid

The welcome page is: 
> http://localhost:8080

The RESTful API can be tested under `Services`.

## Administration / Password

A password is required to access the administration page. The default password and how to configure it are described [here](https://github.com/kermitt2/grobid/wiki/RESTful-services-password)

## Documentation

For a complete description of the available RESTfull services and more usage information, see the following page: 

+ [Grobid RESTFul service](https://github.com/kermitt2/grobid/wiki/Grobid-RESTful-service)
