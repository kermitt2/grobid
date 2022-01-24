#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Created on Fri Jan 14 11:00:20 2022

@author: bryder
"""

import xml.etree.ElementTree as ET
import sys
import json
from shutil import copyfile


def main(argv):

    json_file = argv[0] 
    
    reference_data = []
    i = 0
    with open(json_file,'r') as f:
        for line in f:
            reference_data.append(json.loads(line, strict=False))
            i+=1

            print(i)
    
    input_dir = argv[1]
    output_dir = argv[2]
    
    for item in reference_data:
        
        print(item['item_id'])
        
        item_id = item['item_id']
        
        file_path = input_dir + item_id + '.training.references.referenceSegmenter.tei.xml'
        print(file_path)
        citations = item['raw_citations']
        
        try:
            tree = ET.parse(file_path)
        except:
            print(f"Error Parsing File {item_id}")
            continue

        root = tree.getroot()
        
        text = root.find('text')
        
        bib_list = text.find('listBibl')
        
        bib_list_size = len(bib_list.findall('bibl'))
        citations_size = len(citations)

        for item in bib_list.findall('bibl'):
            bib_list.remove(item)

        i = 1
        for citation in citations:
            label = str(i) + '.'
            
            new_bibl = ET.SubElement(bib_list,'bibl')
            new_label = ET.SubElement(new_bibl,'label')
            new_label.text = label
            
            ref_text = ET.SubElement(new_bibl,'lb')
            ref_text.text = citation
            
            i += 1
            
        tree.write(output_dir + 'tei/' + item_id + ".training.references.referenceSegmenter.tei.xml")
        
        copyfile(input_dir + item_id + '.training.references.referenceSegmenter',output_dir + 'raw/' + item_id + '.training.references.referenceSegmenter')

if __name__ == "__main__":
    main(sys.argv[1:])
