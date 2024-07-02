import argparse
import ntpath
import os
import sys
import yaml
from pathlib import Path
import xml
from xml.sax import make_parser
import re


"""
Script for combining training data from the affilition-address model and 
the header author model, targeted to the name-address sequence model

To be executed locally:
python3 combine_training.py --output-file ../corpus/combined-training.tei.xml
"""

def combine(output_path):
    for root, directories, filenames in os.walk(os.path.join("..","affiliation-address","corpus")):
        for filename in filenames: 
            local_path = os.path.join(root, filename)
            


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