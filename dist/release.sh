#!/bin/bash -

# x.y.z  x = major, y = minor
# z = 0 : normal release
# z > 0 : bug fix release
# z00 .. z19 = alpha
# z20 .. z39 = beta
# z40 .. z49 = rc
# z90 .. z99 = release

VERSION=1.0.042
TAG=1.0.0-rc02

#(cd ../../firmata4j; mvn -DskipTests install)
(cd ..; mvn version:set -DnewVersion=$TAG; mvn -Pproduction -Dversion=$VERSION clean package)

cp ../target/owlcms-firmata.jar files
cp -r ../src/main/resources/devices files
rm *.exe 2>/dev/null
rm -rf files/devices/wokwi
mkdir files/devices/wokwi
find ../diagrams -name '*.xlsx' -print0 | xargs -0 -I {} cp {} files/devices/wokwi

echo jpackage...
jpackage --type exe --input files --main-jar owlcms-firmata.jar --main-class app.owlcms.firmata.ui.Main \
 --name owlcms-firmata --icon files/owlcms.ico --runtime-image jre \
 --win-menu --win-menu-group owlcms --win-console  --win-dir-chooser \
 --app-version ${VERSION} --win-per-user-install --win-shortcut 

#jpackage --type pkg --input files --main-jar owlcms-firmata.jar --main-class app.owlcms.firmata.ui.Main \
# --name owlcms-firmata --icon files/owlcms.png --runtime-image jre \
# --app-version ${VERSION}

gh release delete $TAG -y
gh release create $TAG --notes-file ../RELEASE.md -t "owlcms-firmata $TAG"
gh release upload $TAG *.exe
find ../diagrams -name '*.xlsx' -print0 | xargs -0 -n 1 gh release upload --clobber $TAG