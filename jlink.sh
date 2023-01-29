mvn dependency:build-classpath -Dmdep.outputFile=cp.txt
"$JAVA_HOME/bin/jdeps" --multi-release 17 --print-module-deps --ignore-missing-deps --api-only -cp `cat cp.txt` target/startdevice-*.jar | grep -v Warning > deps.txt
"$JAVA_HOME/bin/jlink" -v --add-modules `cat deps.txt` --compress=2 --no-header-files --no-man-pages --output jlink