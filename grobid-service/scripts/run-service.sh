#!/bin/bash

if [ $# -lt 1 ]; then
    echo 'usage: <path to config.yaml>'
    exit 1
fi

config=$1

echo "Starting grobid service with config $config"

java -jar grobid-service-*-shaded.jar server $config || exit $?