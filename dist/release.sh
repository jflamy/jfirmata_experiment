#!/bin/bash -

# windows version number mapping

# TAG = x.y.z  x = major, y = minor
# z = 0 : normal release
# z > 0 : bug fix release
# Windows versions can only use 3 numeric levels, no letters
# z00 .. z19 = alpha00 .. alpha19  2.0.0-alpha01 = 2.0.001 version
# z20 .. z39 = beta00 .. beta19    2.0.0-beta01 = 2.0.021 version
# z40 .. z49 = rc00 .. rc19        2.0.0-rc01 = 2.0.041 version
# z50 .. z99 = release z.          2.0.0 = 2.0.050 version.

export TAG=2.0.3
export VERSION=2.0.350

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

#jpackage --type pkg --main-jar owlcms-firmata.jar --main-class app.owlcms.firmata.ui.Main \
# --name owlcms-firmata --icon files/owlcms.png --runtime-image jre \
# --app-version ${VERSION}

git add ../pom.xml
git commit -m $TAG
git push
gh release delete $TAG -y
gh release create $TAG --notes-file ../RELEASE.md -t "owlcms-firmata $TAG"
gh release upload $TAG *.exe
#find ../diagrams -name '*.xlsx' -print0 | xargs -0 -n 1 gh release upload --clobber $TAG
gh release upload $TAG files/owlcms-firmata.jar

# get the newly created tag back
git pull