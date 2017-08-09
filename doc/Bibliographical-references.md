 <h1>Annotation guidelines for bibliographical references</h1>

This section describes how to annotate training data for the __citation__ model. This model parses a bibliographical reference in isolation (as typically present in the bibliographical section at the end of an article).

In the model cascading sequence, a bibliographical reference in isolation is obtained from the model _reference segmenter_ which aims at segmenting a bibliographical section into a list of individual bibliographical references. 

The complete bibliographical reference is enclosed in a `<bibl>` element. One <bibl> structure must match with exactly one bibliographical reference.
Then the following tags are used for structuring the bibliographical references:

* `<author>` for the complete sequence of authors

* `<title level="a">` for article title and chapter title. Here "a" stands for analytics (a part of a monograph)

* `<title level="j">` for journal title

* `<title level="m">` for non journal bibliographical item holding the cited article. Note if a book is cited, the title of the book is annotated with `<title level="m">`. If a thesis is cited, the title of the thesis is annotated with `<title level="m">`, and the type of thesis as `<note>`. Here `m` stands for monograph.

* `<date>` the date sequence (including parenthesis, etc.)

* `<biblScope type="pp">` the full range of pages or the article ID

* `<biblScope type="vol">` the block for volume (e.g. `<biblScope type="vol"> vol. 7,</biblScope>`)

* `<biblScope type="issue">` the block for the issue, also known as number, (e.g. no. `<biblScope type="issue">3</biblScope>`,)

* `<orgName>` the institution for thesis or technical reports

* `<publisher>` the name of the publisher

* `<pubPlace>` publication place, or location of the "publishing" institution

* `<editor>` for all the sequence of editors

* `<ptr>` for web url

* `<idno>` for the document-specific identifier, in particular DOI and arXiv identifiers

* `<note>` for any indications related to the reference and not covered by one of the previous tags. In the case of technical report, the indication of the document kind is encoded with the following attribute value <note type="report">

Additional text/characters that do not belong to one of these elements (punctuations, syntactic sugar, etc.) has to be be left untagged under the `<bibl>` elements. This is the case for instance for the tag `<date>`, the caracters such as parenthesis have to be put outside this element (see the example bellow).

Example: 

```xml

<?xml version="1.0" encoding="UTF-8"?>
<tei xmlns="http://www.tei-c.org/ns/1.0" 
	  xmlns:xlink="http://www.w3.org/1999/xlink" 
	  xmlns:mml="http://www.w3.org/1998/Math/MathML">

	<listBibl>
		<bibl>
			<title level="j">Biostatistics</title> (<date>2008</date>), <biblScope type="vol">9</biblScope>, <biblScope type="issue">2</biblScope>, pp. <biblScope type="pp">234–248</biblScope>
    	</bibl>
	</listBibl>

</tei>

```
