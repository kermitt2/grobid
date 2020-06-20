<h1>Frequently Asked Questions</h1>


## When running processing a large quantity of files, I see many `503` errors

The `503` status returned by GROBID is not an error, and does not mean that the server has issues. On the contrary, this is the mechanism to avoid the service from collapsing and to keep it up alive and running according to its capacity for days.

As described in the [documentation](Grobid-service.md#apiprocessfulltextdocument), the `503` status indicates that the service is currently using all its available threads, and the client should simply wait a bit before re-sending the query.

Exploiting the `503` mechanism is already implemented in the different GROBID clients listed [here](Grobid-service.md#Clients-for-GROBID-Web-Services).

## Could we have some guidance for server configuration in production?

The exact server configuration will depend on the service you want to call. We present here the configuration used to process with `processFulltextDocument` around 10.6 PDF per second (around 915,000 PDF per day, around 20M pages per day) with the node.js client listed above during one week on a 16 CPU machine (16 threads, 32GB RAM, no SDD). It ran without any crash during 7 days at this rate. We processed 11.3M PDF in a bit less than 7 days with two 16-CPU servers like that in one of our projects. 

- if your server has 8-10 threads available, you can use the default settings of the docker image, otherwise you would rather need to build and start the service yourself to tune the parameters

- keep the concurrency at the client (number of simultaneous calls) slightly higher than the available number of threads at the server side, for instance if the server has 16 threads, use a concurrency between 20 and 24 (it's the option `n` in the above mentioned clients, in my case I used 24)

- in `grobid/grobid-home/grobid.properties` set the property `org.grobid.max.connections` to your number of available thread at server side or slightly higher (e.g. 16 to 20 for a 16 threads-machine, in my case I used 20)

- set `modelPreload` to `true`in `grobid/grobid-service/config/config.yaml`, it will avoid some strange behavior at launch 

- in the query, `consolidateHeader` can be `1`  or `2` if you are using the CrossRef consolidation. It significantly improves the accuracy and add useful metadata

- If you want to consolidate all the bibliographical references and use `consolidateCitations` as `1` or `2`, CrossRef query rate limit will avoid scale to more than 1 document per second... For scaling the bibliographical reference resolution, you will need to use a local consolidation service, [biblio-glutton](https://github.com/kermitt2/biblio-glutton). The overall capacity will depend on the biblio-glutton service then, and the number of elasticsearch nodes you can exploit. From experience, it is difficult to go beyond 300K PDF per day when using consolidation for every extracted bibliographical references. 


## I would also like to extract images from PDFs

You will get the embedded images converted into `.png` by using the normal batch command. For instance:

> java -Xmx4G -jar grobid-core/build/libs/grobid-core-0.6.0-SNAPSHOT-onejar.jar -gH grobid-home -dIn ~/test/in0/ -dOut ~/test/out0 -exe processFullText 

There is a web service doing the same, returning everything in a big zip file, `processFulltextAssetDocument`, still usable but deprecated.

A simpler option, if you are only interested in raw text and images, is to use directly [pdfalto](https://github.com/kermitt2/pdfalto).