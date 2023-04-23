<h1>Troubleshooting</h1>

### Error in the logs

The logs of Grobid are located in `logs/grobid-service.log`, the console log from gradle are usually not very useful to understand the problem. 
When opening an issue, remember to attach the logs from `grobid-service.log` in the report.  

### Production configuration 

The exact server hardware configuration depends on the service you want to call. 

Following are the configuration used to process with `processFulltextDocument` around 10.6 PDF per second (around 915,000 PDF per day, around 20M pages per day) with the node.js client during one week on a 16 CPU machine (16 threads, 32GB RAM, no SDD). The process ran without any crash during ~7 days at this rate (11.3M PDF processed) using two 16-CPU servers.

- if the server has 8-10 threads available, you can use the default settings, otherwise modify the `concurrency` parameter in the configuration to match your available number of threads. In `grobid/grobid-home/grobid.yaml`, set the parameter `concurrency` to your number of available thread at server side or slightly higher (e.g. 16 to 20 for a 16 threads-machine)
 
- keep the concurrency at the client (number of simultaneous calls) slightly higher than the available number of threads at the server side, for instance if the server has 16 threads, use a concurrency between 20 and 24 (it's the option `n` in the above mentioned clients)

- set `modelPreload` to `true` in `grobid/grobid-home/config/grobid.yaml`, it will avoid some strange behavior at startup

- in the query, consolidateHeader can be `1` or `2` if you are using the consolidation. It significantly improves the accuracy and add useful metadata.

- ff you want to consolidate all the bibliographical references and use `consolidateCitations` as `1` or `2`, CrossRef query rate limit will avoid scaling to more than 1 document per second (and likely less in practice)... For scaling the bibliographical reference resolution, you will need to use a [local consolidation service](https://github.com/kermitt2/biblio-glutton). The overall capacity will depend on the biblio-glutton service then, and the number of elasticsearch nodes you can exploit. From experience, it is difficult to go beyond 300K PDF per day when using consolidation for every extracted bibliographical references with one biblio-glutton instance.

See [full thread](https://github.com/kermitt2/grobid/issues/443).

### Memory constraints and Out of Memory errors

In case of running on limited memory hardware, there are various ways to deal with memory constraints in Grobid:

- reduce the number of parallel threads on server side in grobid/grobid-home/config/grobid.yaml change the parameter `concurrency` (default is `10`, this is the max size of the thread pool that is reported in the logs)

- reduce the number of parallel processing at client side: this is the parameter `n` in the grobid python client command line

- reduce the maximum amount of memory dedicated to the PDF parsing by pdfalto: in `grobid/grobid-home/config/grobid.yaml` change the parameter `memory_limit_mb` a lower amount (note: not too low, PDF parsing remains memory-hungry).


### Windows related issues 

Grobid is developed and tested on Linux. macOS is also supported, although some components might behave slightly different due to the natural incompatibility of Apple with the rest of the world and the availability on some proprietary fonts on this platform.   
 
Windows, unfortunately, is currently not anymore supported, due to lack of experience and time constraints. We recommend Windows users to use the [Grobid Docker image](https://hub.docker.com/r/lfoppiano/grobid/) (documented [here](Grobid-docker.md)) and call the system via API using one of the various [grobid clients](Grobid-service.md#Clients-for-GROBID-Web-Services).

Before opening a new issue which might be related to Windows, please check that it is not redundant [here](https://github.com/kermitt2/grobid/issues?q=is%3Aissue+is%3Aopen+label%3AWindows-specific)

**Windows Subsystem for Linux (WSL) mode**: Please be aware that users have reported issues using the WSL mode in Windows to run Grobid. More details [here](https://github.com/kermitt2/grobid/issues/954).   

**NOTE**: If you are a developer using Windows and you like to help, please let us know. 

### Invalid model format

Using macOS 10.12 Sierra or higher with a non-default locales (e.g. French), you might get the following error: 
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
