<h1>Recompiling and integrating CRF libraries</h1>

Grobid can be used with two different CRF libraries: Wapiti (the default and most performant one) and CRF++ (the historical one). The choice of the library is specified by the property `grobid.crf.engine` in the property file under `grobid-home/config/grobid.properties`:

     grobid.crf.engine=wapiti

or 

     grobid.crf.engine=crfpp 

Wapiti appears two time faster for decoding than CRF++ for the Grobid models. The sizes of the Wapiti models are five to ten times smaller in memory than CRF++ ones. Training time is also significantly reduced based on the Wapiti l-bfgs training algorithm with a similar accuracy. 

NOTE: the usage of CRF++ is not supported anymore since GROBID version 0.4.

## Wapiti

Wapiti is the default CRF library used by Grobid. It is integrated transparently to Grobid via JNI and there is normally nothing to be additionally done. This section explains how to rebuild and install the native library for integrating into Grobid a new versions of Wapiti.

For Grobid, we are using a specific fork of the original Wapiti distribution: [Wapiti fork for Grobid](https://github.com/kermitt2/Wapiti).

This version includes in particular SWIG mapping providing a JNI interface and some bug fixes. 

**1) Build**

The instruction for building the library are given on the above GitHub repo. The libraries `libwapiti.so` or `libwapiti.dylib` are then available under the subdirection `build/`. 

**2) Portability**

On Mac OS X architecture, `libwapiti.dylib` is already portable. 

On Linux, for having the library portable and not dynamically linking to the installed system libraries, process as follow: 

+ in the subdirection `build/`, execute the provided script to collect the dependencies
```bash
> ../collect-dependencies.sh libwapiti.so .
```
The script will copy the required libaries together with `libwapiti.so`.

+ The local linking will be prioritize to ensure portability of the JNI, which can be checked on Linux by:
```bash
> ldd libwapiti.so
```
which should display something like that, with linking only to the local dependencies:

	linux-vdso.so.1 =>  (0x00007fff8e591000)
	libstdc++.so.6 => /home/at-sac/plopez/Wapiti/build/./libstdc++.so.6 (0x00007f6a0e46b000)
	libm.so.6 => /home/at-sac/plopez/Wapiti/build/./libm.so.6 (0x00007f6a0e1e6000)
	libgcc_s.so.1 => /home/at-sac/plopez/Wapiti/build/./libgcc_s.so.1 (0x00007f6a0dfd0000)
	libc.so.6 => /home/at-sac/plopez/Wapiti/build/./libc.so.6 (0x00007f6a0dc3c000)
	/lib64/ld-linux-x86-64.so.2 (0x00000032a0c00000)

**3) Install native libraries**

The next step is to install the updated libraries in the Grobid distribution. On Linux:
```bash
> cp ld-linux-x86-64.so.2 libc.so.6 libgcc_s.so.1 libm.so.6 libstdc++.so.6 libwapiti.so GROBID-ROOT-DIRECTORY/grobid-home/lib/lin-<nb bits of the OS>
```
On Max OS X: 
```bash
	> cp libwapiti.dylib GROBID-ROOT-DIRECTORY/grobid-home/lib/mac-64/
```
**4) Install the JNI jar**

Finally, the JNI jar file has to be deployed in the local repository in grobid-core (we suppose here that the version of Wapiti is 1.5.0, to be adapted if necessary):
```bash
> mvn install:install-file -Dfile=wapiti-1.5.0.jar -DgroupId="fr.limsi.wapiti" -DartifactId="wapiti" -Dversion="1.5.0" -Dpackaging="jar" -DlocalRepositoryPath="GROBID-ROOT-DIRECTORY/grobid-core/lib"
```
If the Wapiti library version changes, the dependency version in grobid-core/pom.xml has to be updated.


## CRF++

NOTE: the usage of CRF++ is not supported anymore since GROBID version 0.4.

The library CRF++ is a c++ library used by Grobid. The integration is done via JNI, so this library has to be built and embedded for the different architectures to be supported. When a new version is released or for supporting a new architecture, the sources have to be downloaded from the website [https://code.google.com/p/crfpp](https://code.google.com/p/crfpp), built and integrated into Grobid.

### Build

The following steps are needed to build libcrfpp:

Unarchive the sources:
```bash
> gunzip CRF++-<version>.tar.gz && tar xvf CRF++-<version>.tar
```
Update the SWIG mapping file with the one provided by Grobid:
```bash
> cp GROBID-ROOT-DIRECTORY/grobid-core/src/main/resources/CRFPP.i CRF++-<version>/swig/CRFPP.i
```
As compared to the existing `CRFPP.i` file present in the CRF++ distribution, this extended `CRFPP.i` contains mapping for calling the CRF++ training method in Java via JNI. 

Generate the SWIG files:
```bash
> cd CRF++-<version>/swig
> make clean java
```
Configure:
```bash
> cd CRF++-<version>
> ./configure --enable-shared=no --with-pic
```
Make:
```bash
> make
```
Install the library either under the default path or the one indicated when running the config script. 

The Makefile under `java/Makfile` generates the library `libcrfpp.so` and the jar file used for the JNI interface. `libcrfpp.so` depends on some other libraries. By default, the link to these libraries is done dynamically and depends of the environment. This can creates a problem when `libcrfpp.so` is used on a different environment than the one used for it build (the needed libraries may have a different version). To correct that, the option rpath has to be added in the file `java/Makefile` and all the linked libraries added in the same directory.

Update of `java/Makefile`:
```bash
> cd java
> vi Makefile
> add [ -Wl,-rpath='$$ORIGIN/'] to the line [$(CXX) -shared $(TARGET)_wrap.o -o libcrfpp.so $(LIBS)]
```
If you not have installed the CRF++ library at the default location, at this point you might possibly need to indicate the include and lib path. 

Check the java file `CRF++-<version>/java/org/chasen/crfpp/Model.java`. The method Model.createTagger should call the Tagger constructor with the following value: `new Tagger(cPtr, true)`. If it is `new Tagger(cPtr, false)`, the value has to be changed or GROBID will face some memory leaks.

Make
```bash
> make
```
Check the links to external libraries:
```bash
>> ldd libcrfpp.so
```
It should display something like that:

        linux-vdso.so.1 =>  (0x00007fffa1dff000)
        libpthread.so.0 => /lib64/libpthread.so.0 (0x00007fea0817b000)
        libstdc++.so.6 => /usr/lib64/libstdc++.so.6 (0x00007fea07e6f000)
        libm.so.6 => /lib64/libm.so.6 (0x00007fea07c19000)
        libgcc_s.so.1 => /lib64/libgcc_s.so.1 (0x00007fea07a02000)
        libc.so.6 => /lib64/libc.so.6 (0x00007fea076a3000)
        /lib64/ld-linux-x86-64.so.2 (0x00007fea085ee000)

Copy all external libraries to current path. The copied libraries should be for the above case:

*     ld-linux-x86-64.so.2

*     libcrfpp.so

*     libc.so.6

*     libgcc_s.so.1

*     libm.so.6

*     libpthread.so.0

*     libstdc++.so.6

Check again the links to external libraries:
```bash
> ldd libcrfpp.so
```
This time it should give the path to current directory. It should display something looking to that:

        linux-vdso.so.1 =>  (0x00007fff6e757000)
        libpthread.so.0 => /home/dr86645/libcrfpp/CRFPP-0.57/java/./libpthread.so.0 (0x00007f40d5715000)
        libstdc++.so.6 => /home/dr86645/libcrfpp/CRFPP-0.57/java/./libstdc++.so.6 (0x00007f40d5409000)
        libm.so.6 => /home/dr86645/libcrfpp/CRFPP-0.57/java/./libm.so.6 (0x00007f40d51b3000)
        libgcc_s.so.1 => /home/dr86645/libcrfpp/CRFPP-0.57/java/./libgcc_s.so.1 (0x00007f40d4f9c000)
        libc.so.6 => /home/dr86645/libcrfpp/CRFPP-0.57/java/./libc.so.6 (0x00007f40d4c3d000)
        /lib64/ld-linux-x86-64.so.2 (0x00007f40d5b73000)
		

### Integration to GROBID source

The generated library has to be added in the open source project. Copy libcrfpp.so (See previous paragraph) and all linked libraries to `GROBID-ROOT-DIRECTORY/grobid-home/lib/lin-<nb bits of the OS>`, for instance:
```bash
> cp ld-linux-x86-64.so.2 libcrfpp.so libc.so.6 libgcc_s.so.1 libm.so.6 libpthread.so.0 libstdc++.so.6 GROBID-ROOT-DIRECTORY/grobid-home/lib/lin-<nb bits of the OS>
```
The jar file has to be deployed in the local repository in grobid-core:
```bash
> mvn install:install-file -Dfile=CRFPP.jar -DgroupId="org.chasen" -DartifactId="crfpp" -Dversion="<version>" -Dpackaging="jar" -DlocalRepositoryPath="GROBID-ROOT-DIRECTORY/grobid-core/lib"
```
Then the dependency version of crfpp in grobid-core/pom.xml has to be updated.

