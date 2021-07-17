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
    <repositories>
        <repository>
            <id>jitpack.io</id>
            <url>https://jitpack.io</url>
        </repository>
    </repositories>         
```
  

In this way you after configuring such repository the dependencies will be automatically managed.
Here an example of grobid-core dependency: 
```xml
	<dependency>
        <groupId>com.github.kermitt2</groupId>
        <artifactId>grobid</artifactId>
        <version>0.7.0</version>
    </dependency>
```
 
If you want to work on a SNAPSHOT development version, you need to include in your pom file the path to the Grobid jar file, 
for instance as follow (if necessary replace `0.7.0` by the valid `<current version>`):

```xml
	<dependency>
	    <groupId>org.grobid</groupId>
	    <artifactId>grobid-core</artifactId>
	    <version>0.7.1-SNAPSHOT</version>
	    <scope>system</scope>
	    <systemPath>${project.basedir}/lib/grobid-core-0.7.1-SNAPSHOT.jar</systemPath>
	</dependency>
```

## Using gradle

Add the following snippet in your gradle.build file: 

```groovy
    repositories { 
        maven { url "https://jitpack.io" }
    }
```

and add the Grobid dependency as well: 
```
    compile 'org.grobid:grobid-core:0.7.0'
    compile 'org.grobid:grobid-trainer:0.7.0'
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
	    
	    //The grobid yaml config file needs to be instantiate using the correct grobidHomeFinder or it will use the default 
	    //locations
		GrobidProperties.getInstance(grobidHomeFinder);

		System.out.println(">>>>>>>> GROBID_HOME="+GrobidProperties.getGrobidHome());

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

You need a local `grobid-home` installation to run GROBID (the resources are not embedded in the jar due to various reasons, in particular JNI and safety). The paths to __grobid-home__ might need to be changed in the project config file:  `grobid-example/grobid-example.properties` according to your installation, for instance: 

		grobid_example.pGrobidHome=/Users/lopez/grobid/grobid-home

Then you can test the toy project:
```bash
> mvn test
```

## Javadoc

The javadoc of the Grobid project is available [here](https://grobid.github.io). All the main methods of the Grobid Java API are currently accessible via the single class [org.grobid.core.engines.Engine](https://grobid.github.io/grobid-core/org/grobid/core/engines/Engine.html). The various test files under `grobid/grobid-core/src/test/java/org/grobid/core/test` further illustrate how to use the Grobid java API.
