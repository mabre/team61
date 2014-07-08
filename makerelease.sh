#!/bin/bash
# Packs all files needed to run the game in a zip and tar.gz file
# Note that tar.gz is better for Linux since it preserves the executable attribute.

version=`grep -m 1 VERSION_NUMBER src/de/hhu/propra/team61/Afrobob.java | sed -E s/.*\"\(.*\)\".*/\\\\1/`
echo -e "\e[00;32m creating release $version\e[00m"
echo -e "\e[01;30m======================================================\e[00m"

echo -e "\e[00;33m creating out/release ...\e[00m"
rm -r out/release
mkdir out/release

echo -e "\e[00;33mcopying jar file created by IntelliJ ...\e[00m"
cp out/artifacts/team61_jar/team61.jar out/release/Charly_in_Madagascar.jar

echo -e "\e[00;33mcopying resources, removing unneded files ...\e[00m"
cp -r resources out/release/
find out/release -iname '*.svg' -print0 | xargs -0 --no-run-if-empty rm -v
find out/release -iname '*.xcf' -print0 | xargs -0 --no-run-if-empty rm -v
find out/release -iname '.*' -print0 | xargs -0 --no-run-if-empty rm -v
find out/release -iname '*~' -print0 | xargs -0 --no-run-if-empty rm -v
rm -rv out/release/resources/audio/user

echo -e "\e[00;33mcopying other files ...\e[00m"
chmod a+x Launch.sh
cp Launch.bat Launch.sh out/release/
cp COPYING README.md manual.pdf out/release/

echo -e "\e[00;33mpacking ...\e[00m"
cd out/release
tar -zcf Charly_in_Madagascar_$version.tar.gz .
mv Charly_in_Madagascar_$version.tar.gz ../..
zip -rq Charly_in_Madagascar_$version.zip .
mv Charly_in_Madagascar_$version.zip ../..
cd ../..

echo -e "\e[01;30m======================================================\e[00m"
echo -e "\e[00;32mfinished packing Charly_in_Madagascar_$version\e[00m"
ls -l Charly_in_Madagascar_*
