The [RESTful API](Grobid-service.md) provides a simple and efficient way to use and deploy GROBID. 
As an alternative, the present page explains how to embed Grobid directly in your Java application. 

After [building the project](Install-Grobid.md), two core jar files are created: grobid-core-`<current version>`.onejar.jar 
and grobid-core-`<current version>`.jar
	
A complete working **maven** project example of usage of GROBID Java API can be found here: [https://github.com/kermitt2/grobid-example](https://github.com/kermitt2/grobid-example). 
The example project is using GROBID Java API for extracting header metadata and citations from a PDF and output the results in BibTex format.  

## Using maven

GROBID releases are uploaded on the [grobid bintray](https://bintray.com/rookies/maven/grobid) repository. 

You need to add the following snippet in your pom.xml in order to configure it:

```xml
    <repository>
        <snapshots>
            <enabled>false</enabled>
        </snapshots>
        <id>bintray-rookies-maven</id>
        <name>bintray</name>
        <url>https://dl.bintray.com/rookies/maven</url>
    </repository>               
```
  

In this way you after configuring such repository the dependencies will be automatically managed.
Here an example of grobid-core dependency: 
```xml
	<dependency>
	    <groupId>org.grobid</groupId>
	    <artifactId>grobid-core</artifactId>
	    <version>0.6.1</version>
	</dependency>
```
 
If you want to work on a SNAPSHOT development version, you need to include in your pom file the path to the Grobid jar file, 
for instance as follow (if necessary replace `0.6.1` by the valid `<current version>`):

```xml
	<dependency>
	    <groupId>org.grobid</groupId>
	    <artifactId>grobid-core</artifactId>
	    <version>0.6.1</version>
	    <scope>system</scope>
	    <systemPath>${project.basedir}/lib/grobid-core-0.6.1.jar</systemPath>
	</dependency>
```

## Using gradle

Add the following snippet in your gradle.build file: 

```groovy
    repositories { 
        maven { 
            url "https://dl.bintray.com/rookies/maven" 
        } 
    }
```

and add the Grobid dependency as well: 
```
    compile 'org.grobid:grobid-core:0.6.1'
    compile 'org.grobid:grobid-trainer:0.6.1'
```


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

	    // The GrobidHomeFinder can be instantiate without parameters to verify the grobid home in the standard
	    // location (classpath, ../grobid-home, ../../grobid-home)
	    
	    // If the location is customised: 
	    GrobidHomeFinder grobidHomeFinder = new GrobidHomeFinder(Arrays.asList(pGrobidHome));		
	    
	    //The GrobidProperties needs to be instantiate using the correct grobidHomeFinder or it will use the default 
	    //locations
		GrobidProperties.getInstance(grobidHomeFinder);

		System.out.println(">>>>>>>> GROBID_HOME="+GrobidProperties.get_GROBID_HOME_PATH());

		Engine engine = GrobidFactory.getInstance().createEngine();

		// Biblio object for the result
		BiblioItem resHeader = new BiblioItem();
		String tei = engine.processHeader(pdfPath, 1, resHeader);
	} 
	catch (Exception e) {
		// If an exception is generated, print a stack trace
		e.printStackTrace();
	} 
```



## maven Skeleton project example

In the following archive, you can find a __maven__ toy example project integrating Grobid in a third party Java project using maven: [grobid-example](https://github.com/kermitt2/grobid-example). 

Create the grobid-core jar library, under the main project directory `grobid/`:
```bash
> ./gradlew clean install 
```

Copy the Grobid jar library under `grobid-example/lib`:

```bash
> cp grobid-core/build/libs/grobid-core-<current version>.jar <path_to_grobid_example>/grobid-example/lib
```

The paths to __grobid-home__ must be changed in the project property file:  `grobid-example/grobid-example.properties` according to your installation, for instance: 

		grobid_example.pGrobidHome=/Users/lopez/grobid/grobid-home
		grobid_example.pGrobidProperties=/Users/lopez/grobid/grobid-home/config/grobid.properties

Then you can test the toy project:
```bash
> mvn test
```

## Javadoc

The javadoc of the Grobid project is available [here](https://grobid.github.io). All the main methods of the Grobid Java API are currently accessible via the single class [org.grobid.core.engines.Engine](https://grobid.github.io/grobid-core/org/grobid/core/engines/Engine.html). The various test files under `grobid/grobid-core/src/test/java/org/grobid/core/test` further illustrate how to use the Grobid java API.
