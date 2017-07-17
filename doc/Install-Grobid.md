<h1>Install GROBID</h1>>

##Getting GROBID


###Latest stable release

The latest stable release of GROBID is version ```0.4.1``` which can be downloaded as follow: 
```bash
> wget https://github.com/kermitt2/grobid/archive/grobid-parent-0.4.1.zip
> unzip grobid-grobid-parent-0.4.1.zip
```

or using the [docker](Grobid-docker.md) container. 

###Current development version
The current development version is ```0.4.2-SNAPSHOT```, which can be downloaded from GitHub and built as follow:

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

###Build GROBID with maven

The standard method for building GROBID is to use maven. Under the main directory `grobid/`:
```bash
> mvn clean install
```
You can skip the tests as follow:
```bash
> mvn -Dmaven.test.skip=true clean install
```
or:
```bash
> mvn -DskipTests=true clean install
```

###Build GROBID with ant
 
It is also possible to build the project with ant. This could be useful for integrating Grobid in an ant project, or when no internet connection is available in a secure development environment, or for people allergic to useless pain. Supported ant targets are `compile`, `clean`, `test` and `package`. So the following should work: 
```bash
> ant package
```

##Use GROBID

From there, the easiest and most efficient way to use GROBID is the [service mode](Grobid-service.md). You can also use the tool in [batch mode](Grobid-batch.md) or integrate it in your Java project via the [Java API](Grobid-java-library.md). 


