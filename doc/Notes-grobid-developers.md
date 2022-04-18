<h1>Notes for the Grobid developers</h1>

This page contains a set of notes for the Grobid developers: 

### Release

We use JitPack to publish the Grobid libraries. 

In order to make a new release:  

+ tag the project branch to be releases, for instance a version `0.7.0`: 

```
> git tag 0.7.0
> git push origin 0.7.0
```

+ create a github release: the easiest is to use the GitHub web interface

+ trigger the build with JitPack: https://jitpack.io/#kermitt2/grobid

+ you're not done, you need to update the documentation, `Readme.md`, `CHANGELOG.md` and end-to-end benchmarking (PMC and bioRxiv sets). 

+ update the usage information, e.g. for Gradlew project: 

```
    allprojects {
        repositories {
            ...
            maven { url 'https://jitpack.io' }
        }
    }
```

```
dependencies {
    implementation 'com.github.kermitt2:grobid:0.7.0'
}
```

for maven projects:

```
    <repositories>
        <repository>
            <id>jitpack.io</id>
            <url>https://jitpack.io</url>
        </repository>
    </repositories>
```

```
    <dependency>
        <groupId>com.github.kermitt2</groupId>
        <artifactId>grobid</artifactId>
        <version>0.7.0</version>
    </dependency>
```

+ Update the docker image(s) on DockerHub with this new version (see the [GROBID docker](Grobid-docker.md) page)

+ Ensure that the different GROBID modules are updated to use this new release as indicated above. 

### Configuration of GROBID module models

Let's say we want to introduce a new model in a Grobid module called `newModel`. The new model configuration can be expressed as the normal Grobid model in a yaml config file:

```yaml
model:
  name: "newModel"
  #engine: "wapiti"
  engine: "delft"
  wapiti:
    # wapiti training parameters, they will be used at training time only
    epsilon: 0.00001
    window: 30
    nbMaxIterations: 1500
  delft:
    # deep learning parameters
    architecture: "BidLSTM_CRF"
    #architecture: "scibert"
    useELMo: false
    embeddings_name: "glove-840B"
```

In the module configuration class, we refer to the existing Grobid config class, for instance in a class `NewModuleConfiguration`:

```java
package org.grobid.core.utilities;

import org.grobid.core.utilities.GrobidConfig.ModelParameters;

public class NewModuleConfiguration {

   /* other config parameter here */ 

   public ModelParameters getModel() {
        return model;
    }

    public void getModel(ModelParameters model) {
        this.model = model;
    }
}

```

For initializing the new model, we simply do the following:

```java
        NewModuleConfiguration newModuleConfiguration = null;
        try {
            ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
            newModuleConfiguration = mapper.readValue(new File("resources/config/config.yml"), NewModuleConfiguration.class);
        } catch(Exception e) {
            LOGGER.error("The config file does not appear valid, see resources/config/config.yml", e);
        }

        if (newModuleConfiguration != null && newModuleConfiguration.getModel() != null)
            GrobidProperties.getInstance().addModel(newModuleConfiguration.getModel());
        LibraryLoader.load();
```

The appropriate libraries will be loaded dynamically based on the configuration of the normal Grobid models and this new model. 


### Unit tests of Grobid Parsers

Sometimes you want to test methods of a grobid parser, without having to instantiate and load the wapiti model.
We recommend separating tests that require wapiti models and call them with a name ending with `IntegrationTest.java` with proper unit tests (using names ending with `Test.java`). 
If you set up a Continuous Integration system, is probably better to exclude integration tests, while they might not work if the grobid-home is properly set up. 

You can exclude Integration tests by default in your gradle.build, by adding: 

```groovy
test {
    exclude '**/**IntegrationTest**'
}
```
   
The DUMMY model (``GrobidModels.DUMMY``) is an artifact to instantiate a GrobidParser wihtout having the model under the grobid-home. 

This is useful for unit test of different part of the parser, for example if you have a method that read the sequence labelling results and assemble into a set of objects. 

**NOTE**: this method unfortunately cannot avoid problems when the Lexicons are used in the parser. A solution for that is that you mock the Lexicon and pass it as method to the parser. Some additional information can be found [here](https://github.com/kermitt2/grobid/issues/410#issuecomment-478888438). 

```java
    public class SuperconductorsParserTest {
    private SuperconductorsParser target;
    private ChemDataExtractorClient mockChemspotClient;

    @Before
    public void setUp() throws Exception {
        //Example of a mocked version of an additional service that is passed to the parser
        mockChemspotClient = EasyMock.createMock(ChemDataExtractorClient.class);
    
        // Passing GrobidModels.DUMMY 
        target = new SuperconductorsParser(GrobidModels.DUMMY, mockChemspotClient);
    }
    
    @Test
    public void test1() throws Exception {
        target.myMethod();
    }
}
```
