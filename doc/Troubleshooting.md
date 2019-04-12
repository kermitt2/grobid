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

libxml2 is required by pdfalto, and is normally shipped by default on all standard installation (Ubuntu, Mac OSX, etc).

For minimal or cloud based / container system like Linode, AWS, Docker, etc. _libxml2_ might not be installed by default and should thus be installed as prerequisite.

See [here](https://github.com/kermitt2/grobid/issues/101) the open issue. 

### Wrong Java version

Using a recent Java version which has a changed string format for giving its version number can lead to problems.

Updating the `distributionUrl` in `gradle/wrapper/gradle-wrapper.properties` to a more recent Gradle version can help, e.g.:

```
distributionUrl=https\://services.gradle.org/distributions/gradle-4.10-all.zip
```

### Using recent Java version (default version using ubuntu 18 for instance)

Using recent java version leads to some errors, and these are the workarounds :
- Update jacoco version to `0.8.2` under `build.gradle`.
- Add dependecy for missing package `compile "javax.activation:activation:1.1.1"` under `grobid-service` module in `build.gradle`
- Update powermock version to `2.0.0-beta.5`
