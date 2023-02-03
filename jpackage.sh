#!/bin/bash -

# x.y.z  x = major, y = minor
# z = 0 : normal release
# z > 0 : bug fix release
# z00 .. z19 = alpha
# z20 .. z39 = beta
# z40 .. z49 = rc
# z90 .. z99 = release

VERSION=0.8.040

cp target/owlcms-firmata.jar package

jpackage --input package --main-jar owlcms-firmata.jar --main-class app.owlcms.firmata.ui.Main \
 --name owlcms-firmata --icon package/owlcms.ico --runtime-image jre \
 --win-menu --win-menu-group owlcms --win-console  --win-dir-chooser \
 --app-version ${VERSION} --win-per-user-install --win-shortcut 
