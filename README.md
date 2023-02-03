# owlcms-firmata
Table-driven device driver for [owlcms](https://owlcms.github.io/owlcms4-prerelease/#/index) refereeing devices built with Arduino (or similar) boards running the [Firmata](https://github.com/firmata/protocol) firmware.  This program relays commands from owlcms to the board and sends events from the board back to owlcms.



![overview](docs/img/overview.png)

This program allows hobbyists to build their own devices and change pin assignments as required.  The pin configuration for a device is read from an Excel spreadsheet.

- Each pin number can be mapped to a button and the MQTT message to be sent when the button is pressed is defined.
- Conversely, an MQTT message received can be mapped to one or more pins.  For each pin the expected action is given -- turning the pin on or off, flashing the pin, emitting a tone, triggering a relay. 

Schematics for building devices can be found in the `diagrams` folder in this repository. Instructions for using the [Wokwi](https://docs.wokwi.com) simulator together with this program as also given, allowing to test a design live with owlcms before building it.
