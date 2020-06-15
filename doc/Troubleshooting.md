<h1>Troubleshooting</h1>

### Memory constraints and Out of Memory errors

Grobid requires 8Gb of RAM for large parallel processing. 

In case of running on limited memory hardware, there are various ways to deal with memory constraints in Grobid:
- reduce the number of parallel threads on server side in grobid/grobid-home/config/grobid.properties change the property org.grobid.max.connections (default is `10`, this is the max size of the thread pool that is reported in the logs)

- reduce the number of parallel processing at client side: this is the parameter `n` in the grobid python client command line

- reduce the maximum amount of memory dedicated to the PDF parsing by pdfalto: in `grobid/grobid-home/config/grobid.properties` change the property `grobid.3rdparty.pdf2xml.memory.limit.mbto` a lower amount (note: not too low, PDF parsing remains memory-hungry).


### Windows related issues 

Grobid is developed and tested on Linux. Mac is also supported, although some components might behave slighly different due to the natural incompatibility of Apple with the rest of the world.   
 
Windows, unfortunately, is not supported, due to lack of experience and time to maintain three architectures. 
We recommend Windows users to use the [Grobid Docker image](https://hub.docker.com/r/lfoppiano/grobid/) (documented [here](Grobid-docker.md)) and call the system via API using one of the various [grobid clients](Grobid-service.md#Clients-for-GROBID-Web-Services).

Before opening a new issue which might be related to windows, please ensure someone else haven't already done that [here](https://github.com/kermitt2/grobid/issues?q=is%3Aissue+is%3Aopen+label%3AWindows-specific)

**NOTE**: If you are a developer using Windows and you like to help, please let us know. 


### Invalid model format

Using macOS 10.12 Sierra with a non-default locales (e.g. French), you might get the following error: 
```
error: invalid model format
```

This is because Wapiti is not reading the binary files correctly (most likely due to some incompatibles introduced from OSX to macOS). 

As workaround, before launching GROBID, type in the console:
```
export LC_ALL=C
```

See [here](https://github.com/kermitt2/grobid/issues/142#issuecomment-253497513) the open issue. 

### Missing libxml2 library

**libxml2**: GROBID is currenly shipped with all the needed libraries (Mac and Linux 32/64 bit).

libxml2 is required by pdfalto, and is normally shipped by default on all standard installation (Ubuntu, Mac OSX, etc).

For minimal or cloud based / container system like Linode, AWS, Docker, etc. _libxml2_ might not be installed by default and should thus be installed as prerequisite.

See [here](https://github.com/kermitt2/grobid/issues/101) the open issue. 
