# Consolidation

In GROBID, we call __consolidation__ the usage of an external bibliographical service to correct and complement the results extracted by the tool. GROBID extracts usually in a relatively reliable manner a core of bibliographical information, which can be used to match complete bibliographical records made available by these services. 

Consolidation has two main interests:

* The consolidation service improves very significantly the retrieval of header information (+.12 to .13 in F1-score, e.g. from 74.59 F1-score in average for all fields with Ratcliff/Obershelp similarity at 0.95, to 88.89 F1-score, using biblio-glutton and GROBID version `0.5.6` for the PMC 1943 dataset, see more recent [benchmarking documentation](https://grobid.readthedocs.io/en/latest/End-to-end-evaluation/) and [reports](https://github.com/kermitt2/grobid/tree/master/grobid-trainer/doc)). 

* The consolidation service matches the extracted bibliographical references with known publications, and complement the parsed bibliographical references with various metadata, in particular DOI, making possible the creation of a citation graph and to link the extracted references to external services. 

The consolidation includes the CrossRef Funder Registry for enriching the extracted funder information.

## Header vs Citation Consolidation

GROBID supports two different types of consolidation:

### Header Consolidation
Used for consolidating metadata of the main document (header information). This method:
- Processes a single bibliographic record (the document's own metadata)
- Uses the `consolidate(BiblioItem bib, String rawCitation, int consolidateMode)` method
- **Relaxed post-validation**: DOI-based matches are considered safe enough without additional validation for CrossRef
- **Single result**: Requests only the top result (`rows=1`)
- **Priority**: DOI queries are trusted more for header metadata
- **Validation**: Skips fuzzy matching when DOI is available for CrossRef

### Citation Consolidation
Used for consolidating extracted reference lists from documents. This method:
- Processes multiple bibliographic records (all references in a document)
- Uses the `consolidate(List<BibDataSet> biblios)` method
- **Strict post-validation**: All CrossRef results require fuzzy matching validation
- **Multiple results**: Processes each reference individually but still only top result per reference
- **Field preservation**: Maintains original labeled tokens and sets consolidation status
- **Validation**: Applies Ratcliff/Obershelp similarity matching (>0.8 threshold) for author names

**Key Differences:**
| Aspect | Header Consolidation | Citation Consolidation |
|--------|-------------------|----------------------|
| **Input** | Single BiblioItem | List of BibDataSet objects |
| **Post-validation** | Relaxed (DOI trusted) | Strict (always fuzzy matching) |
| **Result processing** | Direct return | Map with index preservation |
| **Metadata preservation** | Standard | Preserves original tokens and status |
| **Use case** | Document header metadata | Reference list matching |

GROBID supports two consolidation services:

* [CrossRef REST API](https://github.com/CrossRef/rest-api-doc) (default)

* [biblio-glutton](https://github.com/kermitt2/biblio-glutton)

## CrossRef REST API

The advantage of __CrossRef__ is that it is available without any further installation. It has however a limited query rate (in practice around 25 queries per second), which make scaling impossible when processing bibliographical references for several documents processed in parallel. In addition, it provides metadata limited by what is available at CrossRef.  

For using [reliably and politely the CrossRef REST API](https://github.com/CrossRef/rest-api-doc#good-manners--more-reliable-service), it is highly recommended to add a contact email to the queries. This is done in GROBID by modifying the config file under `grobid-home/config/grobid.yaml`:

```yaml
consolidation:
    crossref:
      mailto: toto@titi.tutu
```

Without indicating this email, the service might be unreliable with some query failures over time. The usage of the CrossRef REST API by GROBID respects the query rate indicated by the service dynamically by each response. Therefore, there should not be any issues reported by CrossRef via this email.  

In case you are a lucky Crossref Metadata Plus subscriber, you can set your authorization token in the config file under `grobid-home/config/grobid.yaml` as follow:

```yaml
consolidation:
    crossref:
      token: yJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJpc3MiOiJodHRwOi8vY3Jvc3NyZWYub3JnLyIsImF1ZXYZImVuaGFuY2VkY21zIiwianRpIjoiN0M5ODlFNTItMTFEQS00QkY3LUJCRUUtODFCMUM3QzE0OTZEIn0.NYe3-O066sce9R1fjMzNEvP88VqSEaYdBY622FDiG8Uq
```

According to Crossref, the token will ensure that said requests get directed to a pool of machines that are reserved for "Plus" SLA users (note: of course the above token is fake).

### Fields Used when using CrossRef

CrossRef supports different query parameters depending on the consolidation type:

#### Header Consolidation
For document header consolidation, CrossRef is **highly restricted** - only DOI-based queries are supported:

| Field                | Parameter Name | Description                                              | Usage Priority         |
|----------------------|----------------|----------------------------------------------------------|------------------------|
| DOI                  | `doi`          | Digital Object Identifier - used for direct lookup       | Highest (direct match) |
| Raw Citation Text    | **BLOCKED**    | Not used for header consolidation                        | Not used               |
| Title                | **BLOCKED**    | Not used for header consolidation                        | Not used               |
| First Author Surname | **BLOCKED**    | Not used for header consolidation                        | Not used               |
| Journal Title        | **BLOCKED**    | Not used for header consolidation                        | Not used               |
| Volume               | **BLOCKED**    | Not used for header consolidation                        | Not used               |
| First Page           | **BLOCKED**    | Not used for header consolidation                        | Not used               |
| Year                 | **BLOCKED**    | Not used for header consolidation                        | Not used               |
| HAL ID               | **BLOCKED**    | Not used for header consolidation                        | Not used               |

#### Citation Consolidation
For reference list consolidation, CrossRef supports more fields but with conditions:

| Field                | Parameter Name        | Description                                              | Usage Priority         |
|----------------------|-----------------------|----------------------------------------------------------|------------------------|
| DOI                  | `doi`                 | Digital Object Identifier - used for direct lookup       | Highest (direct match) |
| Raw Citation Text    | `query.bibliographic` | Complete raw bibliographic reference string              | High (when DOI blank)  |
| Title                | `query.title`         | Article title                                            | Medium (when DOI+raw blank) |
| First Author Surname | `query.author`        | First author's last name                                 | Medium (when DOI+raw blank) |
| Journal Title        | **BLOCKED**           | Not supported                                           | Not used               |
| Volume               | **BLOCKED**           | Not supported                                           | Not used               |
| First Page           | **BLOCKED**           | Not supported                                           | Not used               |
| Year                 | **BLOCKED**           | Not supported (included in bibliographic query)          | Not used               |
| HAL ID               | **BLOCKED**           | Not supported                                           | Not used               |

**Important Notes:**
- Header consolidation with CrossRef is **DOI-only** - all other fields are blocked
- Citation consolidation allows additional fields only when DOI is not available
- Raw citation text is used as a fallback when DOI is missing
- Due to rate limits (~25 queries/second), CrossRef is not suitable for high-volume processing

## biblio-glutton

This service presents several advantages as compared to the CrossRef service. biblio-glutton can scale as required by adding more Elasticsearch nodes, allowing the processing of several PDF per second. The metadata provided by the service are richer: in addition to the CrossRef metadata, biblio-glutton also returns the PubMed and PubMed Central identifiers, ISTEX identifiers, PII, and the URL of the Open Access version of the full text following the Unpaywall dataset. Finally, the bibliographical reference matching is [slighty more reliable](https://github.com/kermitt2/biblio-glutton#matching-accuracy). 

Unfortunately, you need to install the service yourself, including loading and indexing the bibliographical resources, as documented [here](https://github.com/kermitt2/biblio-glutton#building-the-bibliographical-data-look-up-and-matching-databases). Note that a [docker container](https://github.com/kermitt2/biblio-glutton#running-with-docker) is available.

After installing biblio-glutton, you need to select the glutton matching service in the `grobid-home/config/grobid.yaml` file, with its url, for instance:

```yaml
consolidation:
    service: "glutton"
    glutton:
      url: "http://localhost:8080"
```

### Fields Used when using biblio-glutton

biblio-glutton supports a richer set of metadata fields and identifiers, providing more precise matching:

| Field                | Parameter Name   | Description                                 | Usage Priority         |
|----------------------|------------------|---------------------------------------------|------------------------|
| DOI                  | `doi`            | Digital Object Identifier                   | Highest (direct match) |
| HAL ID               | `halId`          | HAL archive identifier                      | High                   |
| PMID                 | `pmid`           | PubMed identifier                           | High                   |
| PMCID                | `pmc`            | PubMed Central identifier                   | High                   |
| Raw Citation Text    | `biblio`         | Complete raw bibliographic reference string | High                   |
| Title                | `atitle`         | Article title                               | Medium                 |
| First Author Surname | `firstAuthor`    | First author's last name                    | Medium                 |
| Journal Title        | `jtitle`         | Journal name                                | Medium                 |
| Volume               | `volume`         | Journal volume                              | Medium                 |
| First Page           | `firstPage`      | First page number                           | Medium                 |
| Year                 | `year`           | Publication year                            | Medium                 |

**Additional Metadata from biblio-glutton:**
- PubMed and PubMed Central identifiers
- ISTEX identifiers
- PII (Publisher Item Identifier)
- Open Access fulltext URLs (via Unpaywall dataset)
- Richer metadata compared to CrossRef

**Notes:**
- biblio-glutton can scale horizontally with additional Elasticsearch nodes
- Supports multiple identifier systems for flexible matching
- Includes built-in post-validation mechanisms
- Field mapping from CrossRef parameters is handled automatically

### Query Strategy and Field Prioritization

Both consolidation services follow a hierarchical approach when querying:

1. **Identifier-based queries** (highest priority):
   - DOI (both services)
   - HAL ID, PMID, PMCID (biblio-glutton only)

2. **Full text queries**:
   - Raw citation string (both services, with different parameter names)

3. **Partial metadata queries** (biblio-glutton only):
   - Title + author combinations
   - Journal + volume + page combinations
   - Year-based filtering

4. **Fallback mechanisms**:
   - CrossRef requires minimal metadata (author + title OR raw citation)
   - biblio-glutton can work with any combination of available fields

This field mapping and prioritization ensures optimal matching accuracy while respecting the capabilities and limitations of each consolidation service.
