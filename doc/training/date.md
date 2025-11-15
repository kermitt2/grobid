# Annotation guidelines for dates

## Introduction

Date segments can be recognized by the `header` model (for example the _date of publication_, or the _online date_) or by the `citation` model. These date segments are further structured by the `date` model which will try to normalize the date into the standard ISO 8601 format. 

For convenience, the structuring of the dates (training files `*.date.tei.xml`) does (exceptionally) not follow the TEI but a basic XML format based on `<day>`, `<month>` and `<year>` elements. 

## Analysis

Each date is enclosed in a `<date>` element. Day, month and year are identified respectively with `<day>`, `<month>` and `<year>` elements. Additional text/characters that do not belong to one of these specific elements (punctuations, etc.) must be left untagged under the `<date>` elements.

For example:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<dates>
	<date>Received <month>August</month> <day>17</day>, <year>2005</year>. </date>
</dates>
```
