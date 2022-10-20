'''
Modest script to help selecting "interesting" error cases in a set of Grobid processed JATS/PDF pairs (e.g. PMC, bioRxiv, etc.).
For example:
- we can select "hard-failed" results without title, authors, affiliation, abstract, without full text and/or without bibliographical references,
- we can select results with interesting patterns to be better cover, like availability statements,
- we can select error extracted metadata when comparing with JATS encoding.

The script offers a base for different selection scenario, to be refined at desired. 

Before running this script, it is assumed that the following resources are available:
- a repository of JATS and PDF pair files is available, for instance from bioRxiv,
e.g. https://zenodo.org/record/3873702 or PMC 
(see https://grobid.readthedocs.io/en/latest/End-to-end-evaluation/#directory-structure)
- a processing of the PDF files by Grobid with resulting TEI XML files
- the batch createTraining has been run on all the PDF
https://grobid.readthedocs.io/en/latest/Grobid-batch/#createtraining

Then the present script can be run as follow:

---
usage: select_error_cases.py [-h] --grobid-tei GROBID_TEI --jats JATS
                             [--grobid-training GROBID_TRAINING] [--out OUT]

Select ineresting error cases from biorxiv for Grobid training data

optional arguments:
  -h, --help            show this help message and exit
  --grobid-tei GROBID_TEI
                        path to the generated Grobid TEI result directory
  --jats JATS           path to the bioRxiv JATS directory
  --grobid-training GROBID_TRAINING
                        path to the directory containing the generated grobid training files
  --out OUT             path to the directory where to add the selected training files

For example:

> python3 select_error_cases.py --grobid-tei result/ --jats /media/lopez/data1/biblio/bioRxiv/biorxiv-10k-validation-2000 --grobid-training training --out training_selected

The script will select error cases that would be worth adding as training data, and will copy all the pre-labeld training files
for these error cases under the repository given by the --output parameter. 

'''

import os
import lxml
import argparse
from lxml import etree as ET
import shutil

# Grobid TEI xpath
grobid_title = "//tei:titleStmt/tei:title/text()"
grobid_authors = "//tei:sourceDesc/tei:biblStruct/tei:analytic/tei:author/tei:persName/tei:surname/text()"
grobid_first_author = "//tei:sourceDesc/tei:biblStruct/tei:analytic/tei:author[1]/tei:persName/tei:surname/text()"
grobid_affiliation = "//tei:sourceDesc/tei:biblStruct/tei:analytic/tei:author/tei:affiliation/tei:orgName/text()"
grobid_abstract = "//tei:profileDesc/tei:abstract//text()"
grobid_citations_base = "//tei:back/tei:div/tei:listBibl/tei:biblStruct"
grobid_availability = "//tei:back/tei:div[@type=\"availability\"]"

# JATS xpath
jats_title = "/article/front/article-meta/title-group/article-title//text()"
jats_authors = "/article/front/article-meta/contrib-group/contrib[@contrib-type=\"author\"]/name/surname/text()"
jats_first_author = "/article/front/article-meta/contrib-group/contrib[@contrib-type=\"author\"][1]/name/surname/text()"
jats_affiliation = "/article/front/article-meta/contrib-group/aff//text()"
jats_abstract = "/article/front/article-meta/abstract//text()"
jats_citations_base = "//ref-list/ref"

# this is an example of availability section titles
interesting_availability_patterns = [ 
    "Availability of data and materials", 
    "Availability of data and material",
    "AVAILABILITY OF DATA AND MATERIALS",
    "Availability of supporting data", 
    "Data access",
    "DATA ACCESS", 
    "Data accessibility"
    "DATA ACCESSIBILITY",
    "Code availability", 
    "Code Accessibility",
    "Software availability",
    "Data availability",
    "Data preprocessing and availability",
    "Software and Data Availability",
    "Availability and implementation",
    "Availability of code and data",
    "Implementation and availability",
    "Data availability statement",
    "Data and code availability",
    "Accessibility of biological resources",
    "Availability of Data materials",
    "DATA AND SOFTWARE AVAILABILITY",
    "Data and software availability",
    "Computer code and data availability",
    "Software availability and documentation",
    "Data Accession Information",
    "Availability of data and code",
    "Data and Materials Availability",
    "Data and Reagent availability",
    "Data access in NCBI",
    "Data availability and distribution",
    "DATABASE AVAILABILITY",
    "Code and data availability",
    "Code dependencies and availability",
    "AVAILABILITY"
]

def evaluate_error_cases(grobid_tei, jats_files, grobid_training=None, output=None):
    interesting_error_cases = []
    ns = {"tei": "http://www.tei-c.org/ns/1.0"}
    for root, directories, filenames in os.walk(grobid_tei):
        for filename in filenames: 
            if filename.endswith(".tei.xml"):
                tei_file = os.path.join(root, filename)
                jats_file = os.path.join(jats_files, filename.replace(".tei.xml", ""), filename.replace(".tei.xml", ".xml"))

                # apply some xpath on the files
                parser = ET.XMLParser(remove_comments=True)

                try:
                    tei_xml = ET.parse(tei_file, parser=parser)
                except:
                    #print("XML parsing error with", tei_file)
                    continue

                try:
                    jats_xml = ET.parse(jats_file, parser=parser)
                except:
                    #print("XML parsing error with", jats_file)
                    continue

                # titles
                tei_title = tei_xml.xpath(grobid_title, namespaces=ns)
                if tei_title != None and len(tei_title)>0:
                    tei_title = tei_title[0]
                else:
                    tei_title = None 
                nlm_title = jats_xml.xpath(jats_title)
                if nlm_title != None and len(nlm_title)>0:
                    nlm_title = nlm_title[0]
                else:
                    nlm_title = None

                #print("tei_title:", tei_title)
                #print("nlm_title:", nlm_title)
                
                # authors
                tei_authors = tei_xml.xpath(grobid_authors, namespaces=ns)
                nlm_authors = jats_xml.xpath(jats_authors)

                #print("tei_authors:", tei_authors)
                #print("nlm_authors:", nlm_authors)

                # first author
                tei_first_author = tei_xml.xpath(grobid_first_author, namespaces=ns)
                nlm_first_author = jats_xml.xpath(jats_first_author)

                #print("tei_first_author:", tei_first_author)
                #print("nlm_first_author:", nlm_first_author)

                # affiliation
                tei_affiliation = tei_xml.xpath(grobid_affiliation, namespaces=ns)
                nlm_affiliation = jats_xml.xpath(jats_affiliation)

                #print("tei_affiliation:", tei_affiliation)
                #print("nlm_affiliation:", nlm_affiliation)

                # abstract
                tei_abstract = tei_xml.xpath(grobid_abstract, namespaces=ns)
                nlm_abstract = jats_xml.xpath(jats_abstract)

                #print("tei_abstract:", tei_abstract)
                #print("nlm_abstract:", nlm_abstract)

                # citations
                tei_citations_base = tei_xml.xpath(grobid_citations_base, namespaces=ns)
                nlm_citations_base = jats_xml.xpath(jats_citations_base)

                # availability section (Grobid only)
                tei_availability = tei_xml.xpath(grobid_availability, namespaces=ns)

                # check availability statement patterns
                all_text_tei = "".join(tei_xml.xpath(".//text()"))
                all_text_nlm = " ".join(jats_xml.xpath(".//text()"))

                # check conditions
                main_error = False

                if tei_title == None or len(tei_title.strip()) == 0:
                    main_error = True
                elif (tei_citations_base == None or len(tei_citations_base)<2) and (nlm_citations_base != None and len(nlm_citations_base)>0):
                    main_error = True
                elif (tei_authors == None or len(tei_authors)==0) and (nlm_authors != None and len(nlm_authors)>0):
                    main_error = True
                elif (tei_abstract == None or len(tei_abstract)==0) and (nlm_abstract != None and len(nlm_abstract)>0):
                    main_error = True

                avail_stat_match = False

                # consider only file without Grobid found availability section, but with an availability pattern
                if (tei_availability == None or len(tei_availability) ==0):
                    for pattern in interesting_availability_patterns:
                        if all_text_nlm.find(pattern) != -1:
                            avail_stat_match = True
                            break

                if main_error and avail_stat_match:
                    print("selected case:", jats_file)

                    # if available, copy the selected training files into the out directory 
                    if grobid_training != None and output != None:
                        # training file
                        file_base = filename.replace(".tei.xml", "")
                        
                        segmentation_file = os.path.join(grobid_training, file_base + ".training.segmentation.tei.xml")
                        segmentation_raw = os.path.join(grobid_training, file_base + ".training.segmentation")
                        shutil.copy2(segmentation_file, output)
                        shutil.copy2(segmentation_raw, output)
                        
                        header_file = os.path.join(grobid_training, file_base + ".training.header.tei.xml")
                        header_raw = os.path.join(grobid_training, file_base + ".training.header")
                        if os.path.isfile(header_file):
                            shutil.copy2(header_file, output)
                            shutil.copy2(header_raw, output)

                        fulltext_file = os.path.join(grobid_training, file_base + ".training.fulltext.tei.xml")
                        fulltext_raw = os.path.join(grobid_training, file_base + ".training.fulltext")
                        shutil.copy2(fulltext_file, output)
                        shutil.copy2(fulltext_raw, output)

                        affiliation_file = os.path.join(grobid_training, file_base + ".training.affiliation.tei.xml")
                        if os.path.isfile(affiliation_file):
                            shutil.copy2(affiliation_file, output)
                        
                        authors_file = os.path.join(grobid_training, file_base + ".training.header.authors.tei.xml")
                        if os.path.isfile(authors_file):
                            shutil.copy2(authors_file, output)
                        
                        references_authors_file = os.path.join(grobid_training, file_base + ".training.references.authors.tei.xml")
                        if os.path.isfile(references_authors_file):
                            shutil.copy2(references_authors_file, output)

                        references_file = os.path.join(grobid_training, file_base + ".training.references.tei.xml")
                        if os.path.isfile(references_file):
                            shutil.copy2(references_file, output)
                            referenceSegmenter_file = os.path.join(grobid_training, file_base + ".training.referenceSegmenter.tei.xml")
                            referenceSegmenter_raw = os.path.join(grobid_training, file_base + ".training.referenceSegmenter")
                            if os.path.isfile(referenceSegmenter_file):
                                shutil.copy2(referenceSegmenter_file, output)
                                shutil.copy2(referenceSegmenter_raw, output)

                        table_file = os.path.join(grobid_training, file_base + ".training.table.tei.xml")
                        table_raw = os.path.join(grobid_training, file_base + ".training.table")
                        if os.path.isfile(table_file):
                            shutil.copy2(table_file, output)
                            shutil.copy2(table_raw, output)

                        figure_file = os.path.join(grobid_training, file_base + ".training.figure.tei.xml")
                        figure_raw = os.path.join(grobid_training, file_base + ".training.figure")
                        if os.path.isfile(figure_file):
                            shutil.copy2(figure_file, output)
                            shutil.copy2(figure_raw, output)

                        # and we copy the PDF for reference/corrections
                        pdf_file = os.path.join(jats_files, file_base, file_base + ".pdf")
                        if os.path.isfile(pdf_file):
                            shutil.copy2(pdf_file, output)

if __name__ == "__main__":
    parser = argparse.ArgumentParser(
        description = "Select ineresting error cases from biorxiv for Grobid training data")
    parser.add_argument("--grobid-tei", type=str, required=True,
        help="path to the generated Grobid TEI result directory")
    parser.add_argument("--jats", type=str, required=True,
        help="path to the bioRxiv JATS directory")
    parser.add_argument("--grobid-training", type=str, required=False,
        help="path to the directory containing the generated grobid training files")
    parser.add_argument("--out", type=str, required=False,
        help="path to the directory where to add the selected training files")

    args = parser.parse_args()
    grobid_tei = args.grobid_tei
    jats_files = args.jats
    grobid_training = args.grobid_training
    output = args.out

    # check path and call methods
    if grobid_tei is None or not os.path.isdir(grobid_tei):
        print("error: the path to the grobid TEI result directory is not valid: ", grobid_tei)
        exit(0)

    if jats_files is None or not os.path.isdir(jats_files):
        print("error: the path to the JATS files is not valid: ", jats_files)
        exit(0)

    if grobid_training is not None and not os.path.isdir(grobid_training):
        print("warning: the path to the Grobid training directory is not valid: ", grobid_training)
        print("selected training files will not be copied")
        grobid_training = None

    if output is not None and not os.path.isdir(output):
        print("warning: the path to the output directory is not valid: ", output)
        print("selected training files will not be copied")
        output = None

    evaluate_error_cases(grobid_tei, jats_files, grobid_training, output)
