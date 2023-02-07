#!/bin/bash -

# x.y.z  x = major, y = minor
# z = 0 : normal release
# z > 0 : bug fix release
# z00 .. z19 = alpha
# z20 .. z39 = beta
# z40 .. z49 = rc
# z90 .. z99 = release

VERSION=1.0.020

#(cd ../../firmata4j; mvn -DskipTests install)
(cd ..; mvn -Pproduction clean package)

rm -rf files/devices

cp ../target/owlcms-firmata.jar files
cp -r ../src/main/resources/devices files

#jpackage --type exe --input files --main-jar owlcms-firmata.jar --main-class app.owlcms.firmata.ui.Main \
# --name owlcms-firmata --icon files/owlcms.ico --runtime-image jre \
# --win-menu --win-menu-group owlcms --win-console  --win-dir-chooser \
# --app-version ${VERSION} --win-per-user-install --win-shortcut 

jpackage --type deb --input files --main-jar owlcms-firmata.jar --main-class app.owlcms.firmata.ui.Main \
 --name owlcms-firmata --icon files/owlcms.png --runtime-image jre \
 --app-version ${VERSION} 
