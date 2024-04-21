#!/bin/bash
set -euo pipefail

projectRoot=$(pwd)

git submodule update --init --recursive

cd ./DataConverter
chmod +x gradlew
./gradlew clean build --no-daemon
cp build/devlibs/*-dev.jar $projectRoot/dataconverter.jar
git reset --hard
