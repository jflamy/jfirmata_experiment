##### New in this release

- Added JuryButtons for an Arduino Nano build with only the jury buttons
- Separated RefereeDownSignal from Referee without down signal
- Each device is now a self-contained package
  - Brought the Excel configuration file and a specific README together with the diagrams
- Automated creation of release and inclusion of configuration files.


##### To create your own device mappings:

- Use the .xlsx files below. The files are also available in the installation directory, in the `app/devices/wokwi` folder 
- You can then tell the program to use your files using the user interface. Keep your definitions in a safe place.
- If you install a new version, you need to copy your definitions again (unfortunately we cannot change that behavior).

##### To run on Windows download and run the `.exe` installer below.  

- This will create a desktop icon and start menu entries.
- To uninstall, use the "Installed App" system settings menu.

##### To run on macOS or Linux

You currently have to install java 17 once, beforehand. Instructions for installing Java on macOS are found [here](https://adoptium.net/installation/macOS/).  You can download the .pkg file from [here](https://adoptium.net/temurin/releases/) -- select aarch64 if you have a recent M1 or M2 Mac.  The JRE package is sufficient.  For Linux, openjdk is available as an apt package.

 Then download the `.jar` file below and run.
```
java -jar owlcms-firmata.jar
```