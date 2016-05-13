<h1>Install GROBID</h1>>

##Getting GROBID

###Prerequisites
**libxml2**: GROBID is currenly shipped with all the needed libraries (Mac and Linux 32/64 bit).
libxml2 is required by pdf2xml, and is normally shipped by default on all standard installation (Ubuntu, Mac OSX, etc).
For minimal or cloud based / container system like Linode, AWS, Docker, etc might not be installed by default.


###Latest stable release

The latest stable release of GROBID is version ```0.4.0``` which can be downloaded as follow: 
```bash
> wget https://github.com/kermitt2/grobid/archive/grobid-parent-0.4.0.zip
> unzip grobid-grobid-parent-0.4.0.zip
```
The current working version is ```0.4.1-SNAPSHOT```, which can be downloaded from GitHub and built as follow: 

###Current development version

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


