'''
    Basic script to check if every lines in a training data file has the expected number of features.
    Report any related problems.

    If, when you launch the Wapiti CRF training, you see this: 

        * Load training data
        warning: missing tokens, cannot apply pattern

    you should apply this script on the training data...

    usage:

    > python3 check_training_data.py fulltext5758072701954158384.train
'''
import sys

if __name__ == "__main__":

    if len(sys.argv) != 2:
        print("invalid number of arguments, usage: python3 check_training_data.py training_file_name")
        exit(0)

    training_file = sys.argv[1]
    print("checking", training_file, "...")

    lines = {}
    with open(training_file) as fp:
        for cnt, line in enumerate(fp): 
            if len(line.strip()) == 0:
                continue
            if line.find(" ") == -1:
                pieces = line.split("\t")
            else:
                pieces = line.split(" ")
            if len(pieces) in lines:
                lines[len(pieces)] += 1
            else:
                lines[len(pieces)] = 1

    # report
    expected = 0
    for key in lines:
        if lines[key] > expected:
            expected = int(key)

    with open(training_file) as fp:
        for cnt, line in enumerate(fp): 
            if len(line.strip()) == 0:
                continue
            if line.find(" ") == -1:
                pieces = line.split("\t")
            else:
                pieces = line.split(" ")
            if len(pieces) != expected:
                print("line", cnt, "- number of features", len(pieces), "(expected", str(expected)+"):", line.replace("\n",""))

    # report
    expected = 0
    for key in lines:
        print(key, lines[key])
        if lines[key] > expected:
            expected = int(key)

    print("expected number of features per line:", expected)