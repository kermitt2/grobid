# Annotation Guidelines for Acknowledgment

## Introduction

The structuring of the acknowledgment datasets (*.tei.xml) based on elements as follows: 

* `<affiliation>` for the affiliation of individual
* `<educationalInstitution>` for the educational institution
* `<fundingAgency>` for the funding agency
* `<grantName>` for the grant name
* `<grantNumber>` for the grant number
* `<individual>` for the individual
* `<otherInstitution>` to the name of institution other than a research institution or university
* `<projectName>` for the project name
* `<researchInstitution>` for the name of research institution
``
Each acknowledgment is enclosed in a `<acknowledgment>` element.

## Inter Annotator Agreement

### Consensus

### Decision for Disambiguities

## Example of Result

```

<acknowledgment>
	<fundingAgency>the Centre National de la Recherche Scientifique</fundingAgency>
	<grantNumber>CNRS - INSU</grantNumber>
	<individual>E . Brockmann</individual>
</acknowledgment>

```






