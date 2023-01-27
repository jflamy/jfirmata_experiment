# owlcms-firmata
Experiments for a Table-driven Firmata device driver for owlcms refereeing devices

Since 
- Olympic Weightlifting Refereeing/Timekeeping/Jury devices only use simple actions, 
- that these actions (e.g. turn on LED) are in direct response to MQTT messages, or
- that they trigger simple MQTT messages (e.g. button press)
it appears doable to write a table-driven driver.

Such a driver would allow hobbyists to build their own devices and remap the pins without having to recompile a script.
A simple configuration file in the form of an Excel could be used.
