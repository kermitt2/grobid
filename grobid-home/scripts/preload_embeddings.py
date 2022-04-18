'''
This script is an optional part of the GROBID docker image build, to pre-load selected embeddings in 
the image.

The script is supposed to be copied under the delft installation in the docker image, then executed
either with just an embedding name (e.g. "glove-840B") for online download of the embedding file
or with an embedding name (e.g. "glove-840B") and a local path to the embedding file copied temporary 
in the image.
If the embedding file is downloaded, it will be removed by the script. 
If the embedding file is copied in the image and passed as argument, it's up to the docker build file to
remove the embedding file. 

Obviously it will add a few GB more to the docker image. Without pre-loading, the embedding file will be 
downloaded and loaded in lmdb at each run of the docker container. 
'''

import os
import argparse
from delft.utilities.Embeddings import Embeddings, open_embedding_file
from delft.utilities.Utilities import download_file
import lmdb
import json

map_size = 100 * 1024 * 1024 * 1024 

def preload(embeddings_name, input_path=None, registry_path=None):
    resource_registry = None
    if registry_path != None:
        with open(registry_path, 'r') as f:
            resource_registry = json.load(f)

    embeddings = Embeddings(embeddings_name, resource_registry=resource_registry, load=False)

    description = embeddings.get_description(embeddings_name)
    if description is None:
        print("Error: embedding name", embeddings_name, "is not registered in", path)

    if input_path is None:
        embeddings_path = None
        # download if url is available
        if description is not None and "url" in description and len(description["url"])>0:
            url = description["url"]
            download_path = embeddings.registry['embedding-download-path']
            # if the download path does not exist, we create it
            if not os.path.isdir(download_path):
                try:
                    os.mkdir(download_path)
                except OSError:
                    print ("Creation of the download directory", download_path, "failed")

            print("Downloading resource file for", embeddings_name, "...")
            embeddings_path = download_file(url, download_path)
            if embeddings_path != None and os.path.isfile(embeddings_path):
                print("Download sucessful:", embeddings_path)
        else:
            print("Embeddings resource is not specified in the embeddings registry:", embeddings_name)
    else:
        embeddings_path = input_path

    if embeddings_path == None:
        print("Fail to retrive embedding file for", embeddings_name)

    embedding_file = open_embedding_file(embeddings_path)
    if embedding_file is None:
        print("Error: could not open embeddings file", embeddings_path)
        return

    # create and load the database in write mode
    embedding_lmdb_path = embeddings.registry["embedding-lmdb-path"]
    if not os.path.isdir(embedding_lmdb_path):
        os.makedirs(embedding_lmdb_path)

    envFilePath = os.path.join(embedding_lmdb_path, embeddings_name)
    embeddings.env = lmdb.open(envFilePath, map_size=map_size)
    embeddings.load_embeddings_from_file(embeddings_path)
    embeddings.clean_downloads()

if __name__ == "__main__":
    parser = argparse.ArgumentParser(description = "preload embeddings during the GROBID docker image build as embedded lmdb")
    parser.add_argument("--embedding", default='glove-840B',
        help=(
            "the desired pre-trained word embeddings using their descriptions in the file"
            " embedding-registry.json,"
            " be sure to use here the same name as in the registry (e.g. 'glove-840B', 'fasttext-crawl', 'word2vec')"
        )
    )
    parser.add_argument("--input", help="path to the embeddings file to be loaded located on the host machine (where the docker image is built),"
                                       " this is optional, without this parameter the embeddings file will be downloaded from the url indicated"
                                       " in the embddings registry, embedding-registry.json")
    parser.add_argument("--registry", help="path to the embedding registry to be considered for setting the paths/urls to embeddings")

    args = parser.parse_args()

    embeddings_name = args.embedding
    input_path = args.input
    registry_path = args.registry

    preload(embeddings_name, input_path, registry_path)
