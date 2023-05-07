##### New in this release

- Fixed the allowable names for uploaded files


##### To create your own device mappings:

1. Use the .xlsx files below. The files are also available in the installation directory, in the `app/devices` folder 

2. The configuration file is uploaded using the user interface. Keep your definitions in a safe place.

   Note: If you install a new version of the package, you need to copy your definitions again (unfortunately we cannot change that behavior).

##### To run on Windows download and run the `.exe` installer below.  

- This will create a desktop icon and start menu entries.
- To uninstall, use the "Installed App" system settings menu.

##### To run on macOS or Linux

You currently have to install Java 17 once, beforehand. Instructions for installing Java on macOS are found [here](https://adoptium.net/installation/macOS/).  You can download the .pkg file from [here](https://adoptium.net/temurin/releases/) -- select aarch64 if you have a recent M1 or M2 Mac.  The JRE package is sufficient.  For Linux, OpenJDK is available as an apt package (`openjdk`)

Then download the `.jar` file below and run.
```
java -jar owlcms-firmata.jar
```