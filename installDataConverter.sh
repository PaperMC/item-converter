#!/bin/bash
set -euo pipefail

projectRoot=$(pwd)

rm -rf work || true
mkdir ./work
cd ./work

git clone https://github.com/PaperMC/DataConverter.git -b dev/1.20.5
cd ./DataConverter
./gradlew build --no-daemon
cp build/devlibs/*-dev.jar $projectRoot/dataconverter.jar

cd "$projectRoot"
rm -rf ./work
