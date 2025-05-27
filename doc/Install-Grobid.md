<h1>Install a GROBID development environment</h1>>

## Getting the GROBID project source

For building GROBID yourself, a JDK must be installed on your machine. We tested the tool successfully from **JDK 1.11** up version **JDK 1.17**. Other recent JDK versions should work correctly. 

Note: Java/JDK 8 is not supported anymore from Grobid version `0.8.0` and the minimum requirement for Java is JDK 1.11.

### Latest stable release

The [latest stable release](https://github.com/kermitt2/grobid#latest-version) of GROBID is version ```0.8.2``` which can be downloaded as follow: 
```bash
> wget https://github.com/kermitt2/grobid/archive/0.8.2.zip
> unzip 0.8.2.zip
```

or using the [docker](Grobid-docker.md) container. 

### Current development version

The current development version is ```0.8.3-SNAPSHOT```, which can be downloaded from GitHub and built as follow:

Clone source code from github:
```bash
> git clone https://github.com/kermitt2/grobid.git
```

Or download directly the zip file:
```bash
> wget https://github.com/kermitt2/grobid/zipball/master
> unzip master
```

## Build GROBID from the source

**Please make sure that Grobid is installed in a path with no parent directories containing spaces.**

### Build GROBID with Gradle 

The standard method for building GROBID is to use gradle. Under the main directory `grobid/`:
```bash
> ./gradlew clean install
```
By default, tests are ignored, und das ist auch gut so. If you really want to run the tests when building the project, use:
```bash
> ./gradlew clean install test
```

### Building through a proxy

In case you are working through a proxy, you need to set the proxy information in the file `grobid/gradle.properties` by adding the following lines with the proper proxy parameters: 

```
systemProp.http.proxyHost=host
systemProp.http.proxyPort=port
systemProp.http.proxyUser=username
systemProp.http.proxyPassword=password
systemProp.https.proxyHost=host
systemProp.https.proxyPort=port
systemProp.https.proxyUser=username
systemProp.https.proxyPassword=password
```

## Use a built GROBID project

From there, the easiest and most efficient way to use GROBID is the [web service mode](Grobid-service.md). 
You can also use the tool in [batch mode](Grobid-batch.md) or integrate it in your Java project via the [Java API](Grobid-java-library.md). 

