##### New in this release

- Added support for the MQTT challenge message.  This is a different way of starting a deliberation (the difference is needed to comply with TCRR requirement to show a message to the public)
- Added support for new MQTT juryDeliberation message.  This resets the jury member lights so the jury president can call a vote on a video replay instead of having a verbal discussion.
- Fixed the configuration file name validation.  The file names in the github repository and the apps/devices/wokwi folder are correct (the file name validation and error message was wrong.)


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