# owlcms-firmata
Experimental (*) table-driven device driver for owlcms refereeing devices using the Firmata firmware.

This program is a table-driven alternative to [Blue-Owl](https://github.com/scottgonzalez/blue-owl), from which the idea of having Firmata refereeing devices originates.  Like Blue-Owl, this driver receives MQTT updates from owlcms and forwards commands to an Arduino or other board running the Firmata firmware.  Conversely, when buttons are pressed on the device, the driver forwards the events to owlcms using MQTT.



![overview](docs/img/overview.png)

The main purpose of this program is to allow hobbyists that build their own devices to be able to change pin assignments as required by their project without having to be programmers themselves.  Schematics for building devices can be found in the [blue-owl-biy](https://github.com/owlcms/blue-owl-biy) "Build-it-yourself" project.

In order to support this flexibility, the program reads the pin allocation from an Excel spreadsheet. 

- Each pin number can be mapped to a button and the MQTT message to be sent when the button is pressed is defined.
- Conversely, an MQTT message received can be mapped to one or more pins.  For each pin the expected action is given -- turning the pin on or off, flashing the pin, emitting a tone, triggering a relay.

(*) This program is to be considered experimental because it is built directly on a Firmata library, without the benefit of proven programming for the devices (piezo buzzer, etc.)

