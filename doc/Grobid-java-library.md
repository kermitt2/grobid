The [RESTful API](Grobid-service.md) provides a simple and efficient way to use and deploy GROBID. For this, the Docker image is the simplest way to use and deploy Grobid. 

As an alternative, the present page explains how to embed Grobid directly in your Java application. The user will need a local `grobid-home` which contains all the models, resources, etc. in addition to the Grobid Java libraries. The `grobid-home` must be downloaded from the Github release of Grobid matching the version of the used Grobid Java library. 

The first option is to use Grobid Java artefacts available online. A complete working **maven** project example of usage of GROBID Java API can be found here: [https://github.com/kermitt2/grobid-example](https://github.com/kermitt2/grobid-example). 
This example project is using GROBID Java API for extracting header metadata and citations from a PDF and output the results in BibTex format.  

The second option is of course to build yourself Grobid and to use the generated artefacts deployed locally. After [building the project](Install-Grobid.md), two core jar files are created under `grobid-core/build/libs`: grobid-core-`<current version>`.onejar.jar (with all the dependencies in the package) and grobid-core-`<current version>`.jar 

## Using maven

The Java artefacts of the latest GROBID release (0.7.2) are uploaded on a DIY repository. 

You need to add the following snippet in your `pom.xml` in order to configure it:

```xml
    <repositories>
        <repository>
            <id>grobid</id>
            <name>GROBID DIY repo</name>
            <url>https://grobid.s3.eu-west-1.amazonaws.com/repo/</url>
        </repository>
    </repositories>         
```

Here an example of `grobid-core` dependency: 

```xml
	<dependency>
        <groupId>org.grobid</groupId>
        <artifactId>grobid-core</artifactId>
        <version>0.7.2</version>
    </dependency>
```

If you want to work on a SNAPSHOT development version, you need to download and build the current master yourself, and include in your pom file the path to the local snapshot Grobid jar file, for instance as follow (if necessary replace `0.7.3-SNAPSHOT` by the valid `<current version>`):

```xml
	<dependency>
	    <groupId>org.grobid</groupId>
	    <artifactId>grobid-core</artifactId>
	    <version>0.7.3-SNAPSHOT</version>
	    <scope>system</scope>
	    <systemPath>${project.basedir}/lib/grobid-core-0.7.3-SNAPSHOT.jar</systemPath>
	</dependency>
```

In any cases, you need a local `grobid-home` corresponding to the version of the library. This `grobid-home` must be downloaded from the release available on the Grobid GitHub repo.

## Using gradle

Add the following snippet in your gradle.build file: 

```groovy
    repositories { 
        maven { url "https://grobid.s3.eu-west-1.amazonaws.com/repo/" }
    }
```

and add the Grobid dependency as well: 
```
    compile 'org.grobid:grobid-core:0.7.2'
    compile 'org.grobid:grobid-trainer:0.7.2'
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
