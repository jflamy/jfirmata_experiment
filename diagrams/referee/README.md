## Simple Refereeing Box

diagram.json is the plain referee device used in conjunction with a laptop that gives a down signal and sound.
See https://wokwi.com/projects/353212897007580161 for the interactive diagram.

## Full Refereeing Box with External Tower

diagramDown.json adds the relays to trigger a light and a buzzer.
See https://wokwi.com/projects/354593337853679617 for the interactive diagram.

> wokwi only undertands diagram.json, you will need to make sure you use that name if the wokwi link is gone.

In the diagram, a blue LED represents the down signal light (relay triggered by pin A0) and the yellow LED represents the buzzer (relay triggered by pin A1)

The simulator does not have a symbol for 240/120/24V as required by the equipment, so the VCC symbol connected to the relay stands for the live higher voltage wire.