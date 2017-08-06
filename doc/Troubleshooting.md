<h1>Troubleshooting</h1>

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

libxml2 is required by pdf2xml, and is normally shipped by default on all standard installation (Ubuntu, Mac OSX, etc).

For minimal or cloud based / container system like Linode, AWS, Docker, etc. _libxml2_ might not be installed by default and should thus be installed as prerequisite.

See [here](https://github.com/kermitt2/grobid/issues/101) the open issue. 
