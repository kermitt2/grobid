The [RESTful API](Grobid-service.md) provides a simple and efficient way to use and deploy GROBID. As an alternative, the present page explains how to embed Grobid directly in your Java application. 

After [building the project](Install-Grobid.md), two core jar files are created: grobid-core-`<current version>`.one-jar.jar and grobid-core-`<current version>`.jar

## Using maven

When using maven, you need to include in your pom file the path to the Grobid jar file, for instance as follow (replace `0.3.4` by the valid `<current version>`):

	<dependency>
	    <groupId>org.grobid.core</groupId>
	    <artifactId>grobid</artifactId>
	    <version>0.3.4</version>
	    <scope>system</scope>
	    <systemPath>${project.basedir}/lib/grobid-core-0.3.4.jar</systemPath>
	</dependency>


## API call

When using Grobid, you have to initiate a context with the path to the Grobid resources, the following class give a complete example of usage:

```java
    import org.grobid.core.*;
    import org.grobid.core.data.*;
    import org.grobid.core.factory.*;
    import org.grobid.core.mock.*;
    import org.grobid.core.utilities.*;
    import org.grobid.core.engines.Engine;
    
	...
    String pdfPath = "mypdffile.pdf";
    ...
	
	try {
		String pGrobidHome = "/Users/lopez/grobid/grobid-home";
		String pGrobidProperties = "/Users/lopez/grobid/grobid-home/config/grobid.properties";

		MockContext.setInitialContext(pGrobidHome, pGrobidProperties);		
		GrobidProperties.getInstance();

		System.out.println(">>>>>>>> GROBID_HOME="+GrobidProperties.get_GROBID_HOME_PATH());

		Engine engine = GrobidFactory.getInstance().createEngine();

		// Biblio object for the result
		BiblioItem resHeader = new BiblioItem();
		String tei = engine.processHeader(pdfPath, false, resHeader);
	} 
	catch (Exception e) {
		// If an exception is generated, print a stack trace
		e.printStackTrace();
	} 
	finally {
		try {
			MockContext.destroyInitialContext();
		} 
		catch (Exception e) {
			e.printStackTrace();
		}
	}
```

The context paths (`pGrobidHome` and `pGrobidProperties`) can also be set by a property file, or for a web application by a web.xml file (see for instance grobid-service).

## maven Skeleton project example

In the following archive, you can find a toy example project integrating Grobid in a third party Java project using maven: [grobid-test](https://raw.github.com/kermitt2/grobid.github.com/master/grobid-test.zip). 

Create the grobid-core jar library
```bash
> cd grobid-core```bash
> mvn clean install
```

Copy the Grobid jar library under grobid-test/lib

```bash
> copy target/grobid-core-`<current version>`.jar `path_to_grobid_test`/grobid-test/lib
```

The paths to `grobid-home` and to the property `grobid.properties` file must be changed in the file grobid-test/src/main/java/org/grobidTest/MyGrobid.java according to your installation: 
```java
	String pGrobidHome = "/Users/lopez/grobid/grobid-home";
	String pGrobidProperties = "/Users/lopez/grobid/grobid-home/config/grobid.properties";
```

Then you can test the toy project:
```bash
> mvn test
```

## ant Skeleton project example

If you are using __ant__ to build your project, the following archive gives a toy example ant project integrating Grobid in a third party Java project: [grobid-test-ant](https://raw.github.com/kermitt2/grobid.github.com/master/grobid-test-ant.zip). 

Create the grobid-core jar library
```bash
> cd grobid-core
> mvn clean install
```

Copy the Grobid jar library (not the one-jar, the standard Grobid jar) under grobid-test-ant/lib. 
```bash
> copy target/grobid-core-`<current version>`.jar `path_to_grobid_test`/grobid-test-ant/lib
```
The skeleton project contains the other required jar. 

The paths to __grobid-home__ and to the property __grobid.properties__ file must be changed in the file grobid-test/src/grobid_test/Testing.java according to your installation: 

```java
	String pGrobidHome = "/Users/lopez/grobid/grobid-home";
	String pGrobidProperties = "/Users/lopez/grobid/grobid-home/config/grobid.properties";
```

Then test the toy project:
```bash
> ant testing
```

## Javadoc

The javadoc of the Grobid project is available [here](http://grobid.github.io/). All the main methods of the Grobid Java API are currently accessible via the single class [org.grobid.core.engines.Engine](http://grobid.github.io/grobid-core/org/grobid/core/engines/Engine.html). The various test files under `grobid-core/src/test/java/org/grobid/core/test` further illustrate how to use the Grobid API.
