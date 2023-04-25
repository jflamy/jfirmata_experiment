##### New in this release

- Added support for interrupting output when a key is pressed.  
  - In the configuration file, a list of input keys can be provided. For example, adding ` - 2,3` at the end of an `ON`, `FLASH` or `TONE` action indicates that pressing 2 or 3 should interrupt the output.

- Redid the management of concurrency to prevent situations where a pin would not be reset at the end of an action or on an interruption.
- Clicking "Start Device" if a device is already started will restart it instead of failing and requiring a restart.
- Added support for the MQTT challenge message.  This is a different way of starting a deliberation (the difference is needed to comply with TCRR requirement to show a message to the public)


##### To create your own device mappings:

1. Use the .xlsx files below. The files are also available in the installation directory, in the `app/devices/wokwi` folder 

2. The configuration file is uploaded using the user interface. Keep your definitions in a safe place.

   Note: If you install a new version of the package, you need to copy your definitions again (unfortunately we cannot change that behavior).

##### To run on Windows download and run the `.exe` installer below.  

- This will create a desktop icon and start menu entries.
- To uninstall, use the "Installed App" system settings menu.

##### To run on macOS or Linux

You currently have to install Java 17 once, beforehand. Instructions for installing Java on macOS are found [here](https://adoptium.net/installation/macOS/).  You can download the .pkg file from [here](https://adoptium.net/temurin/releases/) -- select aarch64 if you have a recent M1 or M2 Mac.  The JRE package is sufficient.  For Linux, openjdk is available as an apt package.

Then download the `.jar` file below and run.
```
java -jar owlcms-firmata.jar
```