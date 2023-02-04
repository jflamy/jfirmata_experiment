## Instructions for packaging

1. Checkout the https://github.com/owlcms/owlcms-firmata and the https://github.com/jflamy/firmata4j repositories.  Make sure they are side-by-side.
   - switch the firmata4j clone to the `webserial` branch using `git checkout webserial` 
2. Install a Java SDK 17 in order to get the `jpackage` program.   https://adoptium.net/temurin/releases/ has them.  On Ubuntu, type `java` and you will see options to install.  Or use `sdkman` to install (use Google to find instructions).
3. For shipping, we only need the runtime. Obtain a JRE for the target platform from https://adoptium.net/temurin/releases/.  We don't install it, we just want the files.
   - Uncompress and copy in the current `dist` directory under the name `jre` (directly inside `jre` there should be a `bin` and a `lib` directory.)  (`tar xfz filename.tar.gz .` followed by a `mv` to rename)
4. Make sure you have installed Maven (mvn) and that it is on the PATH
5. Edit the `jpackage.sh` script
   - uncomment the two lines that start with`#(cd ..`
   - uncomment the`jpackage` lines section that deals with your platform 
   - comment out the lines that deal with the other types.
6. Run `./jpackage.sh`
   - This will compile a fixed version of the firmat4j library
   - This will compile a "uberjar" archive containing all the dependencies
   - This will run jpackage to create an installable package.