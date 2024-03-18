##### Changes since 2.0.0

- clientId now less than 23 characters to prevent spurious error message in owlcms
- host/port/username from last connection are saved and restored on next start
- The Windows setup was not copying the default configuration files to the owlcms/devices directory in the user HOMEDIR.
- The detection of the remote platforms has been made more robust.
- The default port is now 8090 to avoid conflicts with owlcms

##### Release 2.0

- Complete rework of the user interface
  - See the [INSTRUCTIONS.md](https://github.com/jflamy/owlcms-firmata/blob/v24/INSTRUCTIONS.md) file in the repository

- A single instance of the program can handle multiple connected devices
- Detection of the connected devices.
- The name of the firmware is used to locate the matching configuration file by default.

##### Known issues

- If several browsers are run at the same time display between browsers will not be synchronized. Normally only one at a time is needed anyway.

