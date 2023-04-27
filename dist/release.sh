#!/bin/bash -

# windows version number mapping

# x.y.z  x = major, y = minor
# z = 0 : normal release
# z > 0 : bug fix release
# z00 .. z19 = alpha00 .. alpha19
# z20 .. z39 = beta00 .. beta19
# z40 .. z49 = rc00 .. rc19
# z90 .. z99 = release z normally 00 only

export VERSION=1.3.001
export TAG=1.3.0-alpha01

echo building $TAG "(" $VERSION ")"

(cd ../../firmata4j; mvn -DskipTests install)
(cd ..; mvn versions:set -DnewVersion=$TAG;)
(cd ..; mvn -Pproduction clean package)

cp ../target/owlcms-firmata.jar files
rm *.exe 2>/dev/null
rm -rf files/devices
mkdir files/devices
find ../diagrams -name '*.xlsx' -print0 | xargs -0 -I {} cp {} files/devices

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