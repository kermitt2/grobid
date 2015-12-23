<h1>GROBID batch mode</h1>

## Using the batch
Go under the `grobid/grobid-core/target` directory where the core library has been built:
```bash
> cd grobid-core/target
```

The following command display an help for the batch commands:
```bash
> java -jar grobid-core-`<current version>`.one-jar.jar -h
```

The available batch commands are listed bellow. For all the commands, -Xmx1024m is used to set the JVM memory to avoid *OutOfMemoryException* given into the current size of the Grobid models.


### processHeader
Will extract and normalize the header of pdf files. The needed parameters for that command are:

* -gH: path to grobid-home directory

* -dIn: path to the directory of input pdf files

* -dOut: path to the output directory (if omitted the current directory)

* -r: recursive processing of files in the sub-directories (by default not recursive)

Exemple:
```bash
> java -Xmx1024m -jar grobid-core-0.3.0.one-jar.jar -gH /path/to/Grobid/grobid/grobid-home -gP /path/to/Grobid/grobid-home/config/grobid.properties -dIn /path/to/input/directory -dOut /path/to/output/directory -r -exe processHeader 
```

WARNING: the expected extension of the PDF files to be processed is .pdf

### processFullText
Will extract and normalize the full text of pdf files. The needed parameters for that command are:

* -gH: path to grobid-home directory

* -dIn: path to the directory of input pdf files

* -dOut: path to the output directory (if omitted the current directory)

* -r: recursive processing of files in the sub-directories (by default not recursive)

* -ignoreAssets: do not extract and save the PDF assets (bitmaps, vector graphics), by default the assets are extracted and saved

Exemple:
```bash
> java -Xmx1024m -jar grobid-core-0.3.0.one-jar.jar -gH /path/to/Grobid/grobid/grobid-home -gP /path/to/Grobid/grobid-home/config/grobid.properties -dIn /path/to/input/directory -dOut /path/to/output/directory -exe processFullText 
```

WARNING: the expected extension of the PDF files to be processed is .pdf

### processDate
Will process, extract and format the date given in input. The needed parameters for that command are:

* -gH: path to grobid-home directory

* -s: the input date to format

Exemple:
```bash
> java -Xmx1024m -jar grobid-core-0.3.0.one-jar.jar -gH /path/to/Grobid/grobid/grobid-home -gP /path/to/Grobid/grobid-home/config/grobid.properties -exe processDate -s "some date to extract and format"
```

### processAuthorsHeader
Will process, extract and format the authors given in input. The needed parameters for that command are:

* -gH: path to grobid-home directory

* -s: the input

Exemple:
```bash
> java -Xmx1024m -jar grobid-core-0.3.0.one-jar.jar -gH /path/to/Grobid/grobid/grobid-home -gP /path/to/Grobid/grobid-home/config/grobid.properties -exe processAuthorsHeader -s "some authors"
```

### processAuthorsCitation
Will process, extract and format the authors given in input. The needed parameters for that command are:

* -gH: path to grobid-home directory

* -s: the input

Exemple:
```bash
> java -Xmx1024m -jar grobid-core-0.3.0.one-jar.jar -gH /path/to/Grobid/grobid/grobid-home -gP /path/to/Grobid/grobid-home/config/grobid.properties -exe processAuthorsCitation -s "some authors"
```

### processAffiliation
Will process, extract and format the affiliation given in input. The needed parameters for that command are:

* -gH: path to grobid-home directory

* -s: the input

Exemple:
```bash
> java -Xmx1024m -jar grobid-core-0.3.0.one-jar.jar -gH /path/to/Grobid/grobid/grobid-home -gP /path/to/Grobid/grobid-home/config/grobid.properties -exe processAffiliation -s "some affiliation"
```

### processRawReference
Will process, extract and format the raw reference given in input. The needed parameters for that command are:

* -gH: path to grobid-home directory

* -s: the input

Exemple:
```bash
> java -Xmx1024m -jar grobid-core-0.3.0.one-jar.jar -gH /path/to/Grobid/grobid/grobid-home -gP /path/to/Grobid/grobid-home/config/grobid.properties -exe processRawReference -s "a reference string"
```

### processReferences
Will process, extract and format all the references in the PDF files present in the directory given in input. The needed parameters for that command are:

* -gH: path to grobid-home directory

* -dIn: path to the directory of input pdf files

* -dOut: path to the output directory (if omitted the current directory)

* -r: recursive processing of files in the sub-directories (by default not recursive)

Exemple:
```bash
> java -Xmx1024m -jar grobid-core-0.3.0.one-jar.jar -gH /path/to/Grobid/grobid/grobid-home -gP /path/to/Grobid/grobid-home/config/grobid.properties -dIn /path/to/input/directory -dOut /path/to/output/directory -exe processReferences
```

WARNING: the expected extension of the PDF files to be processed is .pdf

### processCitationPatentTEI
Will process, extract and format the citations in the patents encoded in TEI given in input. The needed parameters for that command are:

* -gH: path to grobid-home directory

* -dIn: path to the directory of input tei files

* -dOut: path to save the tei annotated data

Exemple:
```bash
> java -Xmx1024m -jar grobid-core-0.3.0.one-jar.jar -gH /path/to/Grobid/grobid/grobid-home -gP /path/to/Grobid/grobid-home/config/grobid.properties -dIn /path/to/input/directory -dOut /path/to/output/directory -exe processCitationPatentTEI
```

WARNING: extension of the TEI files to be processed must be .tei or .tei.xml

### processCitationPatentST36
Will process, extract and format the citations in the patents encoded in ST.36 given in input. The needed parameters for that command are:

* -gH: path to grobid-home directory

* -dIn: path to the directory of input xml files

* -dOut: path to save the tei results

Exemple:
```bash
> java -Xmx1024m -jar grobid-core-0.3.0.one-jar.jar -gH /path/to/Grobid/grobid/grobid-home -gP /path/to/Grobid/grobid-home/config/grobid.properties -dIn /path/to/input/directory -dOut /path/to/output/directory -exe processCitationPatentST36
```

WARNING: extension of the ST.36 files to be processed must be .xml

### processCitationPatentTXT
Will process, extract and format the citations in the patents encoded in UTF-8 text given in input. The needed parameters for that command are:

* -gH: path to grobid-home directory

* -dIn: path to the directory of input text files

* -dOut: path to save the tei results

Exemple:
```
> java -Xmx1024m -jar grobid-core-0.3.0.one-jar.jar -gH /path/to/Grobid/grobid/grobid-home -gP /path/to/Grobid/grobid-home/config/grobid.properties -dIn /path/to/input/directory -dOut /path/to/output/directory -exe processCitationPatentTXT
```

WARNING: extension of the text files to be processed must be .txt, and expected encoding is UTF-8

### processCitationPatentPDF
Will process, extract and format the citations in the patents available in pdf given in input. The needed parameters for that command are:

* -gH: path to grobid-home directory

* -dIn: path to the directory of input pdf files

* -dOut: path to save the tei results

Exemple:
```
> java -Xmx1024m -jar grobid-core-0.3.0.one-jar.jar -gH /path/to/Grobid/grobid/grobid-home -gP /path/to/Grobid/grobid-home/config/grobid.properties -dIn /path/to/input/directory -dOut /path/to/output/directory -exe processCitationPatentPDF
```

WARNING: extension of the text files to be processed must be .pdf 

### createTrainingCitationPatent
Will generate some training for patent citation data, taking as input ST.36 files. The needed parameters for that command are:

* -gH: path to grobid-home directory

* -dIn: path to the directory of input pdf files

* -dOut: path to save the trained data

Exemple:
```bash
> java -Xmx1024m -jar grobid-core-0.3.0.one-jar.jar -gH /path/to/Grobid/grobid/grobid-home -gP /path/to/Grobid/grobid-home/config/grobid.properties -dIn /path/to/input/directory -dOut /path/to/output/directory -exe createTrainingCitationPatent
```

WARNING: extension of the ST.36 files to be processed must be .xml


### createTrainingHeader
Will generate some training for header data from PDF files. The needed parameters for that command are:

* -gH: path to grobid-home directory

* -dIn: path to the directory of input pdf files

* -dOut: path to save the trained data

Exemple:
```bash
> java -Xmx1024m -jar grobid-core-0.3.0.one-jar.jar -gH /path/to/Grobid/grobid/grobid-home -gP /path/to/Grobid/grobid-home/config/grobid.properties -dIn /path/to/input/directory -dOut /path/to/output/directory -exe createTrainingHeader
```

WARNING: the expected extension of the PDF files to be processed is .pdf


### createTrainingFulltext
Will generate some training for full text data from PDF files. The needed parameters for that command are:

* -gH: path to grobid-home directory

* -dIn: path to the directory of input pdf files

* -dOut: path to save the trained data

Exemple:
```bash
> java -Xmx1024m -jar grobid-core-0.3.0.one-jar.jar -gH /path/to/Grobid/grobid/grobid-home -gP /path/to/Grobid/grobid-home/config/grobid.properties -dIn /path/to/input/directory -dOut /path/to/output/directory -exe createTrainingFulltext
```

WARNING: the expected extension of the PDF files to be processed is .pdf


### createTrainingSegmentation
Will generate some training data for the segmentation model from PDF files. The needed parameters for that command are:

* -gH: path to grobid-home directory

* -dIn: path to the directory of input pdf files

* -dOut: path to save the trained data

Exemple:
```bash
> java -Xmx1024m -jar grobid-core-0.3.0.one-jar.jar -gH /path/to/Grobid/grobid/grobid-home -gP /path/to/Grobid/grobid-home/config/grobid.properties -dIn /path/to/input/directory -dOut /path/to/output/directory -exe createTrainingSegmentation
```

WARNING: the expected extension of the PDF files to be processed is .pdf


### createTrainingReferenceSegmentation
Will generate some training data for the reference segmentation model from PDF files. The needed parameters for that command are:

* -gH: path to grobid-home directory

* -dIn: path to the directory of input pdf files

* -dOut: path to save the trained data

Exemple:
```bash
> java -Xmx1024m -jar grobid-core-0.3.0.one-jar.jar -gH /path/to/Grobid/grobid/grobid-home -gP /path/to/Grobid/grobid-home/config/grobid.properties -dIn /path/to/input/directory -dOut /path/to/output/directory -exe createTrainingReferenceSegmentation
```

WARNING: the expected extension of the PDF files to be processed is .pdf
