<h1>Notes for the Grobid developers</h1>

This page contains a set of notes for the Grobid developers: 

### Release

In order to make a new release:  

+ make sure that there are no additional models in the grobid-home, usually it is better to have a second cloned project **over ssh** for the release 

+ Make the release: 
```
    > ./gradlew release
```

Note that the release via the gradle wrapper can only work when no prompt for the password is required by git, so in practice it means it is necessary to push over ssh. 

+ Add the bintray credentials in are in the file `~/.gradle/gradle.properties`, like: 

```  
bintrayUser=username
bintrayApiKey=the api key 
mavenRepoReleasesUrl=https://dl.bintray.com/rookies/releases
mavenRepoSnapshotsUrl=https://dl.bintray.com/rookies/snapshots
```

+ Fetch back the tag and upload the artifacts: 
 
```
    > git checkout [releasetag]
    
    > ./gradlew clean build install
    
    > ./gradlew bintrayUpload
```

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
