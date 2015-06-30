# Validate a Grobid release

* Run Grobid tests.

```
$ export JAVA_HOME=`/usr/libexec/java_home -v 1.8`
$ mvn clean test
```

* Run MetaEval. Using meta-eval requires manually setting up a Grobid installation and data files in a very specific directory structure, as documented in the README, https://github.com/allenai/meta-eval.

# Publish Grobid to Ai2 Nexus resolver

* The Nexus UI is here http://utility.allenai.org:8081/nexus/#welcome. Credentials required, ask Michael. This is useful for troubleshooting.

* Update the Grobid versions names, avoid -SNAPSHOT releases. See https://github.com/cristipp/grobid/commit/677c9ca38f1438ecfcd6072d5187769faa5fced5, where we used -ai2 suffix.

* Validate the release, see above.

* Setup maven settings.xml:

```
$ cat ~/.m2/settings.xml
<settings xmlns="http://maven.apache.org/SETTINGS/1.0.0"
  xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
  xsi:schemaLocation="http://maven.apache.org/SETTINGS/1.0.0
                      http://maven.apache.org/xsd/settings-1.0.0.xsd">
  <servers>
    <server>
      <id>ai2</id>
      <username>deploy</username>
      <password>[Ask Michael]</password>
    </server>
  </servers>
</settings>
```

* Publish the grobid-* artifacts:

```
$ mvn -Dmaven.test.skip=true deploy -DaltDeploymentRepository=ai2::default::http://utility.allenai.org:8081/nexus/content/repositories/public-releases/
```

* Publish the grobid dependencies. Grobid sets up a local file resolver, which is unusable shared development, for example breaks Semaphore CI.

```
$ mvn deploy:deploy-file -Dfile=grobid-core/lib/fr/limsi/wapiti/wapiti/1.5.0/wapiti-1.5.0.jar -DpomFile=lib/fr/limsi/wapiti/wapiti/1.5.0/wapiti-1.5.0.pom -DgroupId=fr.limsi.wapiti -DartifactId=wapiti -Dversion=1.5.0 -Dpackaging=jar -Durl=http://utility.allenai.org:8081/nexus/content/repositories/public-releases/ -DrepositoryId=ai2

$ mvn deploy:deploy-file -Dfile=lib/com/cybozu/language-detection/09-13-2011/language-detection-09-13-2011.jar -DpomFile=grobid-core/lib/com/cybozu/language-detection/09-13-2011/language-detection-09-13-2011.pom -DgroupId=com.cybozu -DartifactId=language-detection -Dversion=09-13-2011 -Dpackaging=jar -Durl=http://utility.allenai.org:8081/nexus/content/repositories/public-releases/ -DrepositoryId=ai2

$ mvn deploy:deploy-file -Dfile=grobid-core/lib/eugfc/imageio-pnm/1.0/imageio-pnm-1.0.jar -DpomFile=grobid-core/lib/eugfc/imageio-pnm/1.0/imageio-pnm-1.0.pom -DgroupId=eugfc -DartifactId=imageio-pnm -Dversion=1.0 -Dpackaging=jar -Durl=http://utility.allenai.org:8081/nexus/content/repositories/public-releases/ -DrepositoryId=ai2

$ mvn deploy:deploy-file -Dfile=grobid-core/lib/org/wipo/analysers/wipo-analysers/0.0.1/wipo-analysers-0.0.1.jar -DpomFile=grobid-core/lib/org/wipo/analysers/wipo-analysers/0.0.1/wipo-analysers-0.0.1.pom -DgroupId=org.wipo.analysers -DartifactId=wipo-analysers -Dversion=0.0.1 -Dpackaging=jar -Durl=http://utility.allenai.org:8081/nexus/content/repositories/public-releases/ -DrepositoryId=ai2

$ mvn deploy:deploy-file -Dfile=grobid-core/lib/org/chasen/crfpp/1.0.2/crfpp-1.0.2.jar -DpomFile=grobid-core/lib/org/chasen/crfpp/1.0.2/crfpp-1.0.2.pom -DgroupId=org.chasen -DartifactId=crfpp -Dversion=1.0.2 -Dpackaging=jar -Durl=http://utility.allenai.org:8081/nexus/content/repositories/public-releases/ -DrepositoryId=ai2   
``` 

The commands were generated starting with the list of unsatisfied dependencies:

```
fr.limsi.wapiti#wapiti;1.5.0
com.cybozu#language-detection;09-13-2011
eugfc#imageio-pnm;1.0
org.wipo.analysers#wipo-analysers;0.0.1
org.chasen:crfpp:1.0.2
```

These are part of grobid repo, for example:

```
grobid-core/lib/fr/limsi/wapiti/wapiti/1.5.0/wapiti-1.5.0.jar
grobid-core/lib/fr/limsi/wapiti/wapiti/1.5.0/wapiti-1.5.0.pom
```

The mvn command is simply:

```
mvn deploy:deploy-file -Dfile=grobid-core/lib/$ORG_SLASH/$NAME/$VER/$NAME-$VER.jar -DpomFile=grobid-core/$ORG_SLASH/$NAME/$VER/$NAME-$VER.pom -DgroupId=$ORG_DOT -DartifactId=$NAME -Dversion=$VER -Dpackaging=jar -Durl=http://utility.allenai.org:8081/nexus/content/repositories/public-releases/ -DrepositoryId=ai2
```

TODO(Patrick): Automatize the process, perhaps just use standard resolvers instead of local package copies.
TODO(Michael): Publish to JCenter/BinTray.

