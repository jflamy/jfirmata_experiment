The configuration file for each device is as follows:

## Input Buttons

Input buttons emit MQTT messages according to the owlcms specification.

| Column      | Description                                                  |
| ----------- | ------------------------------------------------------------ |
| description | An arbitrary text describing the button                      |
| pin         | the pin controlled by the input, that is turned on when the button is pressed |
| button      | ON to indicate what happens when the button is pressed       |
| output      | the MQTT topic to be emitted.  the "owlcms" prefix and the current field of play are added by the program.  For example<br />`refbox/decision` |
| message     | The message portion to complete the topic.  For example, if the button is for referee 1 giving a good decision, the message for the `refbox/decision` topic above would be<br />`1 good` |
| comment     | An arbitrary description                                     |

## Output Action Types

When MQTT messages are received, the entries in the table are examined. All the ones for which the topic and message match are triggered and the corresponding outputs are done.

The following types of outputs are defined, and are used in the definition of Outputs (see below)

| Type  | Description                                                  |
| ----- | ------------------------------------------------------------ |
| ON    | Turn the signal on on the pin (provide voltage)              |
| OFF   | Turn the signal off                                          |
| FLASH | total duration, on duration, off duration - resettingPins<br /><br />total duration, in milliseconds.  The flashing will stop after that duration.<br />on duration: the signal will be on for that duration<br />off duration: the signal will turn off for that duration<br />resetting Pins: the flashing will stop if one of the pins in the list is pressed. The list of resetting pins follows space-hyphen-space ` - `<br /><br />example<br />1800,200,100  - 3,2<br />This will flash for 1800ms maximum.  200ms on, 100ms off.  If either of the pins 2 or 3 turn on (through a button press for example) then the flashing stops. |
| TONE  | note,duration   *repeated*  - resettingPins<br />The notes are in the AngloSaxon convention (A to G) followed by the octave number (C7)<br />There is a special note `PAUSE` to turn off output.  After the sequence of notes, you can specify pins that will interrupt the tone being played (same as for FLASH).<br /><br />Example to play the note C7 once for 500ms, a pause of 250ms, and then D7 for 200ms<br /><br />C7,500,PAUSE,250,D7,200 |

## Outputs

Each output is a row in the Excel, with the following columns being used:

| Column      | Description                                                  |
| ----------- | ------------------------------------------------------------ |
| description | An arbitrary text describing the purpose of the output       |
| pin         | the pin on which the output will take place                  |
| topic       | the topic that must be present in the MQTT message received  |
| message     | the message that, in addition to the topic, must match for the output to take place |
| pin action  | an output action from ON, OFF, FLASH, TONE as defined above  |
| parameters  | the parameters for the action, as defined in the section above |
| comment     | arbitrary explanations                                       |

