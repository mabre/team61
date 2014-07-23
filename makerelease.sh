#!/bin/bash
# Packs all files needed to run the game in a zip and tar.gz file
# Note that tar.gz is better for Linux since it preserves the executable attribute.

version=`grep -m 1 VERSION_NUMBER src/de/hhu/propra/team61/Afrobob.java | sed -E s/.*\"\(.*\)\".*/\\\\1/`
echo -e "\e[00;32mcreating release $version\e[00m"
echo -e "\e[01;30m======================================================\e[00m"

echo -e "\e[00;33mcreating out/Charly_in_Madagascar ...\e[00m"
rm -r out/Charly_in_Madagascar
mkdir out/Charly_in_Madagascar

echo -e "\e[00;33mcopying jar file created by IntelliJ ...\e[00m"
cp out/artifacts/team61_jar/team61.jar out/Charly_in_Madagascar/Charly_in_Madagascar.jar

echo -e "\e[00;33mcopying resources, removing unneeded files ...\e[00m"
cp -r resources out/Charly_in_Madagascar/
find out/Charly_in_Madagascar -iname '*.svg*' -print0 | xargs -0 --no-run-if-empty rm -v
find out/Charly_in_Madagascar -iname '*.xcf*' -print0 | xargs -0 --no-run-if-empty rm -v
find out/Charly_in_Madagascar -iname '.*' -print0 | xargs -0 --no-run-if-empty rm -v
find out/Charly_in_Madagascar -iname '*~' -print0 | xargs -0 --no-run-if-empty rm -v
find out/Charly_in_Madagascar -iname '*Test*' -print0 | xargs -0 --no-run-if-empty rm -v
rm -v out/Charly_in_Madagascar/resources/levels/High.lvl
rm -v out/Charly_in_Madagascar/resources/levels/Flat.lvl
rm -rv out/Charly_in_Madagascar/resources/audio/user

echo -e "\e[00;33mcopying other files ...\e[00m"
chmod a+x Launch.sh
cp Launch.bat Launch.sh out/Charly_in_Madagascar/
cp COPYING README.md manual.pdf out/Charly_in_Madagascar/

echo -e "\e[00;33mpacking ...\e[00m"
cd out
GZIP=-9 tar -zcf Charly_in_Madagascar_$version.tar.gz Charly_in_Madagascar
mv Charly_in_Madagascar_$version.tar.gz ..
zip -9rq Charly_in_Madagascar_$version.zip Charly_in_Madagascar
mv Charly_in_Madagascar_$version.zip ..
cd ..
git archive --format=tar --prefix=Charly_in_Madagascar_source/ HEAD | gzip -9 > Charly_in_Madagascar_source_$version.tar.gz

echo -e "\e[01;30m======================================================\e[00m"
echo -e "\e[00;32mfinished packing Charly_in_Madagascar_$version\e[00m"
ls -l Charly_in_Madagascar_*
