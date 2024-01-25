# owlcms-firmata
owlcms-firmata is a user-configurable device driver for refereeing devices used with the [owlcms](https://owlcms.github.io/owlcms4-prerelease/#/index) Olympic Weightlifting competition management system.  This program allows hobbyists to their own devices using Arduino board. Instructions for use are found in the [INSTRUCTIONS.md](INSTRUCTIONS) file.

The uses configuration files to change pin assignments as required.  No programming is required. See the [CONFIGURATION](diagrams/CONFIGURATION) file for details.

Construction diagrams and configuration files are provided for common device configurations:

- [Timekeeper Buttons](https://github.com/owlcms/owlcms-firmata/tree/main/diagrams/timekeeper) The buttons can be connected to the announcer laptop and do not interfere, or to a separate timekeeper laptop if one is used.
- [Refereeing Devices](https://github.com/owlcms/owlcms-firmata/tree/main/diagrams/referee) This is the typical owlcms setup, used with a laptop provides the down signal and buzzer. The design includes LEDs and buzzers to provide reminders and summon the referees. These can be omitted to create a minimal buttons-only design.
- [Refereeing with External Down Signal Light and Buzzer](https://github.com/owlcms/owlcms-firmata/tree/main/diagrams/refereeDownSignal) Two relays control the external light and buzzer.  This allows compliance with the traditional competition setup.
- [Jury President Buttons](https://github.com/owlcms/owlcms-firmata/tree/main/diagrams/juryButtons)  This is the typical owlcms setup, used with a laptop that shows the referee and jury member decisions.
- [Full Jury President Console with Indicator Lights](https://github.com/owlcms/owlcms-firmata/tree/main/diagrams/juryFull). This diagram shows how a device that has all the indicator lights described in the IWF TCRR can be built.

The driver relays commands from owlcms to the board and sends events from the board back to owlcms.  Any board that can be loaded with [Firmata](https://github.com/firmata/protocol) can be used.  The following illustrates the process using the refereeing devices.

![overview](docs/img/overview.png)

Any board that can be loaded with  [Firmata](https://github.com/firmata/protocol) can be used. Firmata (see the [firmware](https://github.com/owlcms/owlcms-firmata/tree/main/firmware) folder) is loaded once and not touched afterwards.  The program also supports the pin assignments for commercial devices being developed by the [Blue-Owl](https://github.com/scottgonzalez/blue-owl) project.

The pin configuration for a device is read from an Excel spreadsheet.  

- Each pin number can be mapped to a button and the MQTT message to be sent when the button is pressed is defined.
- Conversely, an MQTT message received can be mapped to one or more pins.  For each pin the expected action is given -- turning the pin on or off, flashing the pin, emitting a tone, triggering a relay. 

- Sample pinout configurations are copied into the installation directory.  You can take these files and edit them to change the pin numbers.  To run with the modified assignments, copy the files to the installation directory next to the program.
- Instructions for using the [Wokwi](https://docs.wokwi.com) Arduino simulator together with this program as also given: this allows running the design live with owlcms before building it.

**Credit** The idea and incentive for this program come from the [Blue-Owl](https://github.com/scottgonzalez/blue-owl) project by Scott González.   
