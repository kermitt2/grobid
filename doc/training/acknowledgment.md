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

## Analysis
Each acknowledgment information is enclosed in a `<acknowledgment>` element.

```
<acknowledgment>
	<fundingAgency>the Centre National de la Recherche Scientifique</fundingAgency>
	<grantNumber>CNRS - INSU</grantNumber>
	<individual>E . Brockmann</individual>
</acknowledgment>
```

In the Pdf to TEI conversion services, the information regarding the acknolwdgment is placed under the tag `<back/>` along with the information for annexes  `<div type ="annex">` and references `<div type ="references">`. Furthermore, the information regarding the bounding box coordinates is embedded in each information that has been successfully extracted by the parser.

```
<back>
    <div type="acknowledgement">
        <div
            xmlns="http://www.tei-c.org/ns/1.0">
            <head n="12">Acknowledgments</head>
            <p>We are very grateful to 
                <rs type="individual" coords="14,175.47,639.42,44.60,8.97">Dan Boneh</rs>, 
                <rs type="individual" coords="14,229.20,639.42,47.74,8.97">Constantine Sapunzakis</rs>, 
                <rs type="individual" coords="14,106.68,651.42,38.36,8.97">Ben Pfaff</rs>, 
                <rs type="individual" coords="14,152.73,651.42,54.30,8.97">Steve Gribble</rs>, and 
                <rs type="individual" coords="14,233.14,651.42,61.32,8.97">Matthias Jacob</rs> for their feedback, help, and support in the course of this work. This material is based upon work supported in part by 
                <rs type="fundingAgency" coords="14,110.29,687.30,116.22,8.97">the National Science Foundation</rs> under Grant No. 
                <rs type="grantNumber" coords="14,64.80,699.18,33.05,8.97">0121481</rs>.
            </p>
        </div>
    </div>
</back>
```

## Inter Annotator Agreement
Like other models in Grobid, annotated data for the acknowledgment falls into two folders:
- Data for training, under `grobid/grobid-trainer/resources/dataset/acknowledgment/corpus/`, and 
- Data for evaluation, under `grobid/grobid-trainer/resources/dataset/acknowledgment/evaluation/`.

The annotated corpus received some inputs from other annotators when those data was introduced at the [DESIR Code Sprint 2019](https://desircodesprint.sciencesconf.org/) activity.

Some annotators proposed to add two types of information in the label. Those two scales for the label are:

####Types

To whom acknowledgment is addressed, e.g.:
- person
- institution
- grant number

####Roles

The role of whom acknowledgment is addressed, e.g.:
- founding agency
- technical support
- language support
- providing data
- personal support
- consulting
- unknown & others

Label could be the combination of both (types and roles) e.g. `LanguageSupportPerson`, `FoundingAgencyGrantNumber`

### Consensus
Suggestions about the labels and the labelling levels will be considered for inclusion in the next version of the acknowledgment model.

### Decision for Disambiguities
The use of `the`, for example, in `the Centre National de la Recherche Scientifique` should not be involved in the labelling.







