#!/bin/bash
set -e
cd "$(dirname "$0")"
mkdir -p out
find src/main/java -name '*.java' > out/sources.txt
javac --release 17 -encoding UTF-8 -d out @out/sources.txt
java -Dcinema.port="${PORT:-8080}" -cp out com.cinema.app.CinemaApplication
