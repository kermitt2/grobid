#!/bin/bash

# This modest script will build the JEP library according to your JDK, python version and 
# the shared gcc libraries available on your system and will install it for usage by
# GROBID. The JEP library is required to use the Deep Learning models of GROBID. 

# Curently JEP library and the GROBID Deep Learning models are supported only for Linux,
# and thus we only provide a build and install script as linux shell script

# This script should be launched from grobid root directory (grobid/), e.g.:
# $ ./grobid-home/scripts/install_jep_lib.sh 

cd grobid-home/tmp
pwd
git clone --branch v4.0.2 https://github.com/ninia/jep
cd jep
git checkout v4.0.2
echo "Building jep library..."
#sudo -E python3 setup.py build install
python3 setup.py build install
# python3 setup.py build
echo "Build successful"

if [ -n "$(ls -d build/lib.linux-x86_64-*/)" ]; then
    cp build/lib.linux-x86_64-*/jep/jep.*.so ../../lib/lin-64/jep/libjep.so && echo "libjep.so installed successfully (Linux)"
fi

if [ -n "$(ls -d build/lib.macosx-*-x86_64-*/)" ]; then
    cp build/lib.macosx-*-x86_64-*/jep/jep.*.so ../../lib/mac-64/libjep.dylib && echo "libjep.dylib installed successfully (macOS x86_64)"
fi

cd ..
rm -rf jep
echo "done"
