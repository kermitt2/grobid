<h1>Frequently Asked Questions</h1>

## I would also like to extract images from PDFs

You can get the embedded images converted into .png by using the batch command. For instance:

> java -Xmx4G -jar grobid-core/build/libs/grobid-core-0.6.0-SNAPSHOT-onejar.jar -gH grobid-home -dIn ~/test/in0/ -dOut ~/test/out0 -exe processFullText 

There is a web service doing the same, returning everything in a big zip file, `processFulltextAssetDocument`, still usable but deprecated.

A simpler option, if you are only interested in raw text and images, is to use directly [Pdfalto](https://github.com/kermitt2/pdfalto).


## When running processing a large quantity of files, I see many 503 errors

The `503` status returned by GROBID is not an error, and does not mean that the server collapses. 
On the contrary, it is the mechanism to avoid the service from collapsing and to keep it up alive and running according to its capacity for days.

As described in the [documentation](Grobid-service.md#apiprocessfulltextdocument), the 503 status indicates that the service is currently using all its available threads, and the client should simply wait a bit before re-sending the query.

Exploiting the 503 mechanism is already implemented in the different GROBID clients listed [here](Grobid-service.md#Clients-for-GROBID-Web-Services).

