#!/bin/bash

if [ $# -lt 1 ]; then
    echo 'usage: <path to config.yaml>'
    exit 1
fi

config=$1

echo "Starting grobid service with config $config"

java -Djava.library.path=../grobid-home/lib/lin-64:../grobid-home/lib/lin-64/jep -jar build/libs/grobid-service-*-onejar.jar server $config || exit $?
