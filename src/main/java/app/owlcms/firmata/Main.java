package app.owlcms.firmata;

import java.io.IOException;

import org.firmata4j.IODevice;
import org.firmata4j.Pin;
import org.firmata4j.firmata.FirmataDevice;

public class Main {

	public static void main(String[] args) {
		String myPort = "COM6"; // modify for your own computer & setup.

		IODevice board = new FirmataDevice(myPort);

		try {
			board.start(); // start comms with board;
			System.out.println("Board started.");
			board.ensureInitializationIsDone();
		} catch (Exception ex) {
			System.out.println("couldn't connect to board. "+ex);
			System.exit(-1);
		}
		try {
			Pin myLED = board.getPin(13);
			myLED.setMode(Pin.Mode.OUTPUT);

			// LED D4 on.
			myLED.setValue(1);

			// Pause for half a second.
			try {
				Thread.sleep(2500);
			} catch (Exception ex) {
				System.out.println("sleep error.");
			}
			// LED D4 off.
			myLED.setValue(0);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				board.stop();
				System.out.println("Board stopped.");
			} catch (IOException e) {
				System.out.println("couldn't stop board. "+e);
			} // finish with the board.

		}
	}

}
