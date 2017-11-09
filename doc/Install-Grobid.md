<h1>Install GROBID</h1>>

##Getting GROBID


###Latest stable release

The [latest stable release](https://github.com/kermitt2/grobid#latest-version) of GROBID is version ```0.5.0``` which can be downloaded as follow: 
```bash
> wget https://github.com/kermitt2/grobid/archive/0.5.0.zip
> unzip 0.5.0.zip
```

or using the [docker](Grobid-docker.md) container. 

###Current development version
The current development version is ```0.5.1-SNAPSHOT```, which can be downloaded from GitHub and built as follow:

Clone source code from github:
```bash
> git clone https://github.com/kermitt2/grobid.git
```

Or download directly the zip file:
```bash
> wget https://github.com/kermitt2/grobid/zipball/master
> unzip master
```

##Build GROBID

**Please make sure that grobid is installed in a path with no parent directories containing spaces.**

###Build GROBID with Gradle 

The standard method for building GROBID is to use gradle. Under the main directory `grobid/`:
```bash
> ./gradlew clean install
```
By default, tests are ignored. For building the project and running the tests, use:
```bash
> ./gradlew clean install test
```

##Use GROBID

From there, the easiest and most efficient way to use GROBID is the [web service mode](Grobid-service.md). You can also use the tool in [batch mode](Grobid-batch.md) or integrate it in your Java project via the [Java API](Grobid-java-library.md). 


