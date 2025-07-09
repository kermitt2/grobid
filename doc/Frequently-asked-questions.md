<h1>Frequently Asked Questions</h1>


## When running processing a large quantity of files, I see many `503` errors

The `503` status returned by GROBID is not an error, and does not mean that the server has issues. On the contrary, this is the mechanism to avoid the service from collapsing and to keep it up alive and running according to its capacity for days.

As described in the [documentation](Grobid-service.md#apiprocessfulltextdocument), the `503` status indicates that the service is currently using all its available threads, and the client should simply wait a bit before re-sending the query.

Exploiting the `503` mechanism is already implemented in the different GROBID clients listed [here](Grobid-service.md#Clients-for-GROBID-Web-Services).

## Could we have some guidance for server configuration in production?

The exact server configuration will depend on the service you want to call, the models selected in the Grobid configuration file (`grobid-home/config/grobid.yaml`) and the availability of GPU. We consider here the complete full text processing of PDF (`processFulltextDocument`). 

1) Using CRF models only, for example via the lightweight Docker image (https://hub.docker.com/r/lfoppiano/grobid/tags) 

- in `grobid/grobid-home/config/grobid.yaml` set the parameter `concurrency` to your number of available threads at server side or slightly higher (e.g. 16 to 20 for a 16 threads-machine)

- keep the concurrency at the client (number of simultaneous calls) slightly higher than the available number of threads at the server side, for instance if the server has 16 threads, use a concurrency between 20 and 24 (it's the option `n` in the above mentioned clients)

These settings will ensure that CPU are fully used when processing a large set of PDF.  

For example, with these settings, we processed with `processFulltextDocument` around 10.6 PDF per second (around 915,000 PDF per day, around 20M pages per day) with the node.js client during one week on a 16 CPU machine (16 threads, 32GB RAM, no SDD). It ran without any crash during 7 days at this rate. We processed 11.3M PDF in a bit less than 7 days with two 16-CPU servers in one of our projects. 

Note: if your server has 8-10 threads available, you can use the default settings of the docker image, otherwise you will need to modify the configuration file to tune the parameters, as [documented](Configuration.md).

2) Using Deep Learning models, for example via the full Docker image (<https://hub.docker.com/r/grobid/grobid/tags>) 

2.1) If the server has a GPU

In case the server has a GPU, which has its own memory, the Deep Learning inferences are automatically parallelized on this GPU, without impacting the CPU and RAM memmory. The settings given above in 1) can normally be use similarly.

2.2) If the server has CPU only

When Deep Learning models run as well on CPU as fallback, the CPU are used more intensively (DL models push CPU computations quite a lot), more irregularly (Deep Learning models are called at certain point in the overall process, but not continuously) and the CPU will use additional RAM memory to load those larger models. For the DL inference on CPU, an additional thread is created, allocating its own memory. We can have up to 2 times more CPU used at peaks, and approx. up to 50% more memory. 

The settings should thus be considered as follow: 

- in `grobid/grobid-home/config/grobid.yaml` set the parameter `concurrency` to your number of available threads at server side divided by 2 (8 threads available, set concurrency to `4`)

- keep the concurrency at the client (number of simultaneous calls) at the same level as the `concurrency` parameter at server side, for instance if the server has 16 threads, use a `concurrency` of `8` and the client concurrency at `8` (it's the option `n` in the clients)

In addition, consider more RAM memory when running Deep Learning model on CPU, e.g. 24-32GB memory with concurrency at `8` instead of 16GB.

3) In general, consider also these settings:

- Set `modelPreload` to `true` in `grobid/grobid-home/config/grobid.yaml`, it will avoid some strange behavior at launch (this is the default setting).

- Regarding the query parameters, `consolidateHeader` can be `1`  or `2` if you are using the biblio-glutton or CrossRef consolidation. It significantly improves the accuracy and add useful metadata.

- If you want to consolidate all the bibliographical references and use `consolidateCitations` as `1` or `2`, the CrossRef query rate limit will make the scaling to more than 1 document per second impossible (so Grobid would typically wait 90% or more of its time waiting for CrossRef API responses)... For scaling the bibliographical reference resolutions, you will need to use a local consolidation service, [biblio-glutton](https://github.com/kermitt2/biblio-glutton). The overall capacity will depend on the biblio-glutton service then, and the number of elasticsearch nodes you can exploit. From experience, it is difficult to go beyond 300K PDF per day when using consolidation for every extracted bibliographical references. 

## I would also like to extract images from PDFs

You will get the embedded images converted into `.png` by using the normal batch command. For instance:

```console
java -Xmx4G -Djava.library.path=grobid-home/lib/lin-64:grobid-home/lib/lin-64/jep -jar grobid-core/build/libs/grobid-core-0.8.2-onejar.jar -gH grobid-home -dIn ~/test/in/ -dOut ~/test/out -exe processFullText 
```

There is a web service doing the same, returning everything in a big zip file, `processFulltextAssetDocument`, still usable but deprecated.

A simpler option, if you are only interested in raw text and images, is to use directly [pdfalto](https://github.com/kermitt2/pdfalto).


## pdfalto is GPL, it is used by and shipped with GROBID which is Apache 2, is it okay in term of licensing?

We think there is no issue. First because GROBID calls the pdfalto binary as external command line - so there is similar to chaintools/scripts (which can be Apache/MIT) with external command line calls (calling most of the time also GPL stuff on Linux). More precisely:

- GROBID and pdfalto are two different programs in the sense of FSF, see last paragraph [here](https://www.gnu.org/licenses/gpl-faq.en.html#MereAggregation): 

```text
By contrast, pipes, sockets and command-line arguments are communication mechanisms normally 
used between two separate programs. So when they are used for communication, the modules 
normally are separate programs. 
```

Linking libraries, for instance with a JNI, would have required a LGPL, but here we don't link libraries, share address space, and so on. 

- pdfalto can be aggregated in the same "grobid" distribution, see GPL faq [Mere Aggregation](https://www.gnu.org/licenses/gpl-faq.en.html#MereAggregation)

```text
The GPL permits you to create and distribute an aggregate, even when the licenses of the 
other software are nonfree or GPL-incompatible. The only condition is that you cannot 
release the aggregate under a license that prohibits users from exercising rights that 
each program's individual license would grant them.
```

For convenience, it is no problem to ship the pdfalto executables with GROBID - same as a docker image which ships typically a mixture of GPL and Apache/MIT stuff calling each others like crazy and much more "deeply" than in our case.

Finally, as the two source codes are shipped in different repo with clear licensing information, exercising the rights that each program's individual license grants them is fully respected.

The only possible restriction would be:

```text
But if the semantics of the communication are intimate enough, exchanging complex 
internal data structures, that too could be a basis to consider the two parts as 
combined into a larger program.
```

pdfalto produces ALTO files, which is a standard format. pdfalto can be used for many other purposes than GROBID. In return GROBID itself can support other inputs, like text or the old pdf2xml files, and could support ALTO files produced by other tools. So this restriction does not apply. 
