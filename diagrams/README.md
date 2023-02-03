# Diagrams

This directory contains definitions and diagrams for the devices

## Interactive diagrams

- The definitions use the Wokwi JSON format and can be run on the free wokwi.com simulator.
- It is actually easier to look at the diagrams on the wokwi site because mousing over the pins reveal their number.

The files in this directory are normally available as wokwi projects, but we cannot guarantee that the URLs will remain (see below if the links stop working.)

- Referee control device and button boxes :  https://wokwi.com/projects/354593337853679617
- Timekeeper device: https://wokwi.com/projects/353216754939098113
- Jury device and jury member button boxes: https://wokwi.com/projects/352943774351361025

If the kinks are broken: 

1. go to the wokwi.com site and sign up/login
2. Go to your own projects (there is an icon at the top right)
3. Click on "+ New Project" at the top.
4. Start a new Arduino Nano project (except for the jury, which is Arduino Mega)
5. Go to the design directory you want in this area (jury, referee, timekeeper)
6. Select the content  `diagram.json` (select all) and paste it in the diagram.json window on Wokwi.
7. Select the content `sketch.ino` and paste it in the sketch.ino on Wokwi.

## Running a simulation

### Windows Instructions

1. (Initial installation) Copy the files from the `firmware` folder to a location on your disk.  You will need the Nano and Mega versions.
2. (Initial installation, only once) Install the com0com package, version 2.2.2.0
3. Locate the the Setup program for com0com in the start menu, and start it.
   1. Check that you have two ports CNCA0 and CNCB0
   2. Click Apply
   3. Leave the setup program running
4. Start owlcms
5. Start Chrome or Edge (must be one or the other)
6. Go to https://wokwi.com and open your project
7. In the diagram.json window, click ONCE and hit `F1` .
8. Type the letters `Upload` and hit enter to run
9. The browser will open a dialog asking for a serial port to open
   1. **Wait** for characters to appear in the bottom right area.  Firmata is a binary protocol, so it is normal that the output is not completely legible.  You should see the word "Firmata" with extra spaces.
   2. Select the odd-numbered port (CNCB0)  that is the other member of the pair.
   3. Click `Select`
10. Launch the owlcms-firmata program and select the other port (CNCA0)

You can stop and start the owlcms-firmata program, and use the start/stop device button.

But if you stop the simulation, you must exit the browser, and start all over.  You may also need to stop the com0com setup program and start it again.
