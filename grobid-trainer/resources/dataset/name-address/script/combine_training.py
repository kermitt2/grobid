import argparse
import ntpath
import os
import sys
import yaml
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

                    for elem in tree_aff.findall('.//{http://www.tei-c.org/ns/1.0}author'):
                        new_name = name_sequence_pool[index_name]
                        index_name += 1
                        if index_name >= len(name_sequence_pool):
                            index_name = 0
                        new_name = etree.fromstring(new_name)
                        pos = 0
                        # preprend the person name(s)
                        for child in new_name.getchildren():
                            #elem.append(child)
                            elem.insert(pos, child)
                            pos += 1

                    root_combined.append(root_aff)
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