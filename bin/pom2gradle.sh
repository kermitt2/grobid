#!/bin/bash

if [ $# -lt 1 ]; then
    echo 'usage: <grobid-project>'
    exit -1
fi

echo " === Started at `date` === "

cd ..
echo "Working folder is $PWD"
project="grobid-$1"

if [ -d "./$project" ] 
then
  echo "Working with $project"
else
  echo "Folder $project does not seem to exist. Exit..."
  exit -1
fi

project="$PWD/$project"

pom=$project/pom.xml
if [ -f $pom ]
then
  echo "Converting $pom to gradle script"
else 
  echo "$pom was not found. Exit..."
  exit -1
fi
if [ -z "$2" ]; then
  gradle_script=$project/build.gradle
else
  gradle_script=$project/build-upload.gradle
fi
cmd="java -cp bin/saxon9he.jar net.sf.saxon.Query -o:$gradle_script docpath=$pom url=$2 user=$3 password=$4 bin/pom2gradle.xq"
echo "Executing: $cmd"
$cmd &&
echo " === Finished successfully at `date` === "




