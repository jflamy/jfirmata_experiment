# owlcms-firmata
owlcms-firmata is a user-configurable device driver for Olympic weightlifting refereeing devices. 

The driver allows devices built with Arduino (or similar) boards to interface with the [owlcms](https://owlcms.github.io/owlcms4-prerelease/#/index) Olympic Weightlifting competition management system.  It relays commands from owlcms to the board and sends events from the board back to owlcms.  The program can handle fully IWF-compliant Timekeeper, Referee, and Jury Devices (see the [diagrams](https://github.com/owlcms/owlcms-firmata/tree/main/diagrams) section for examples.)

The illustration below uses the refereeing devices as an example.

![overview](docs/img/overview.png)

This program allows hobbyists to build their own devices and change pin assignments as required.  Any board that can be loaded with  [Firmata](https://github.com/firmata/protocol) can be used. Firmata (see the [firmware](https://github.com/owlcms/owlcms-firmata/tree/main/firmware) folder) is loaded once and not touched afterwards.  The program also supports the pin assignments for commercial devices being developed by the [Blue-Owl](https://github.com/scottgonzalez/blue-owl) project.

The pin configuration for a device is read from an Excel spreadsheet.  

- Each pin number can be mapped to a button and the MQTT message to be sent when the button is pressed is defined.
- Conversely, an MQTT message received can be mapped to one or more pins.  For each pin the expected action is given -- turning the pin on or off, flashing the pin, emitting a tone, triggering a relay. 

- Sample pinout configurations are copied into the installation directory.  You can take these files and edit them to change the pin numbers.  To run with the modified assignments, copy the files to the installation directory next to the program.
- Instructions for using the [Wokwi](https://docs.wokwi.com) Arduino simulator together with this program as also given: this allows running the design live with owlcms before building it.

**Credit** The idea and incentive for this program come from the [Blue-Owl](https://github.com/scottgonzalez/blue-owl) project by Scott González.   
