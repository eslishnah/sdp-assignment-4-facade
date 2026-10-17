#!/usr/bin/env bash
set -e
cd "$(dirname "$0")"
mkdir -p out
find src -name '*.java' > sources.tmp
javac -encoding UTF-8 -d out @sources.tmp
rm sources.tmp
java -cp out Main
