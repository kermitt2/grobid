import argparse
import ntpath
import os
import sys
from pathlib import Path
import random
import re
from lxml import etree

"""
Script for combining training data from the affilition-address model and 
the header author model, targeted to the name-address sequence model

To be executed locally:
python3 combine_training.py --output-file ../corpus/combined-training.tei.xml
"""

template_tei = '''<tei xmlns="http://www.tei-c.org/ns/1.0" xmlns:xlink="http://www.w3.org/1999/xlink" xmlns:mml="http://www.w3.org/1998/Math/MathML">
        <teiHeader>
            <fileDesc>
                <sourceDesc>
                    <biblStruct>
                        <analytic>
                            <author/>
                        </analytic>
                    </biblStruct>
                </sourceDesc>
            </fileDesc>
        </teiHeader>
    </tei>'''

def _create_tei_envelop(piece):
    template_xml = etree.fromstring(template_tei)
    nodes = template_xml.findall('.//{http://www.tei-c.org/ns/1.0}author')
    if nodes != None and len(nodes)>0:
        nodes[0].append(piece)
        return template_xml
    return None

def combine(output_path):

    root_combined = etree.Element("teiCorpus")
    root_combined.set("xmlns", "http://www.tei-c.org/ns/1.0")
    tree_combined = etree.ElementTree(root_combined)

    # get a pool of interesting annotated names from header author training data
    name_sequence_pool = []
    for root, directories, filenames in os.walk(os.path.join("..","..","name","header","corpus")):
        for filename in filenames: 
            if filename.endswith(".xml"):
                local_path = os.path.join(root, filename)
                try:
                    parser = etree.XMLParser(dtd_validation=False, no_network=True)
                    tree_name = etree.parse(local_path)
                    root_name = tree_name.getroot()

                    # remove <marker>
                    for elem in tree_name.findall('.//{http://www.tei-c.org/ns/1.0}marker'):
                        parent = elem.getparent()
                        parent.remove(elem)

                    # add name sequences in training
                    root_combined.append(root_name)

                    for elem in tree_name.findall('.//{http://www.tei-c.org/ns/1.0}author'):
                        xml_chunk = etree.tostring(elem)
                        name_sequence_pool.append(xml_chunk)

                except Exception as e:
                    print(e)
    print(len(name_sequence_pool),"name sequences")

    index_name = 0
    for root, directories, filenames in os.walk(os.path.join("..","..","affiliation-address","corpus")):
        for filename in filenames: 
            if filename.endswith(".xml"):
                local_path = os.path.join(root, filename)
                # for each affiliation address file, we inject one name taken from header author training data
                try:
                    orgnames = []

                    parser = etree.XMLParser(dtd_validation=False, no_network=True)
                    tree_aff = etree.parse(local_path)
                    root_aff = tree_aff.getroot()

                    # remove all <marker> and <orgName type="laboratory">
                    # remove some departments as it is not so frequent in the targeted sequences
                    for elem in tree_aff.findall('.//{http://www.tei-c.org/ns/1.0}marker'):
                        parent = elem.getparent()
                        parent.remove(elem)

                    for elem in tree_aff.findall('.//{http://www.tei-c.org/ns/1.0}orgName[@type="laboratory"]'):
                        parent = elem.getparent()
                        parent.remove(elem)

                    for elem in tree_aff.findall('.//{http://www.tei-c.org/ns/1.0}orgName[@type="department"]'):
                        parent = elem.getparent()
                        rand = random.uniform(0, 1)
                        if rand > 0.1:
                            parent.remove(elem)
                    
                    # add just one affiliation in training
                    for elem in tree_aff.findall('.//{http://www.tei-c.org/ns/1.0}affiliation'):
                        # this deep copy is an okay solution :)
                        root_aff_copy = etree.fromstring(etree.tostring(root_aff)) 
                        # remove all person name
                        for elem2 in root_aff_copy.findall('.//{http://www.tei-c.org/ns/1.0}persName'):
                            parent2 = elem2.getparent()
                            parent2.remove(elem2)
                        # add each distinct affiliation
                        for elem2 in root_aff_copy.findall('.//{http://www.tei-c.org/ns/1.0}affiliation'):
                            # we need at least one orgName
                            elems3 = elem2.findall('.//{http://www.tei-c.org/ns/1.0}orgName')
                            if elems3 != None and len(elems3)>0:
                                orgname = elems3[0].text
                                if orgname not in orgnames:
                                    local_content = _create_tei_envelop(elem2)
                                    root_combined.append(local_content)
                                    orgnames.append(orgname)

                    # keep one distinct affiliation
                    for elem in tree_aff.findall('.//{http://www.tei-c.org/ns/1.0}affiliation'):
                        # we need at least one orgName
                        elems2 = elem.findall('.//{http://www.tei-c.org/ns/1.0}orgName')
                        if elems2 != None and len(elems2)>0:
                            local_content = _create_tei_envelop(elem)
                            # inject a name
                            new_name = name_sequence_pool[index_name]
                            index_name += 1
                            if index_name >= len(name_sequence_pool):
                                index_name = 0
                            new_name = etree.fromstring(new_name)
                            pos = 0
                            elems3 = local_content.findall('.//{http://www.tei-c.org/ns/1.0}author')
                            if elems3 != None and len(elems3)>0:
                                elem3 = elems3[0]
                                # preprend the person name(s)
                                for child in new_name.getchildren():
                                    #elem.append(child)
                                    elem3.insert(pos, child)
                                    pos += 1
                                    rand = random.uniform(0, 1)
                                    if rand > 0.1:
                                        break;

                        root_combined.append(local_content)

                except Exception as e:
                    print(e)

    tree_combined.write(output_path, pretty_print=True)

if __name__ == "__main__":
    parser = argparse.ArgumentParser(
        description="Combine training data for name-address sequece model."
    )
    parser.add_argument(
        "--output-file",
        type=str,
        help="path to a TEI XML file where the data combination will be written",
    )

    args = parser.parse_args()
    output = args.output_file

    combine(output)