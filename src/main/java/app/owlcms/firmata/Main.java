package app.owlcms.firmata;

import java.io.IOException;

import org.firmata4j.IODevice;
import org.firmata4j.IOEvent;
import org.firmata4j.Pin;
import org.firmata4j.Pin.Mode;
import org.firmata4j.PinEventListener;
import org.firmata4j.firmata.FirmataDevice;
import org.firmata4j.transport.JSerialCommTransport;

public class Main {

	public static void main(String[] args) {
		String myPort = "CNCA0"; // modify for your own computer & setup.

		IODevice board = new FirmataDevice(new JSerialCommTransport(myPort));

		try {
			board.start(); // start comms with board;
			System.out.println("Board started.");
			board.ensureInitializationIsDone();
		} catch (Exception ex) {
			System.out.println("couldn't connect to board. " + ex);
			System.exit(-1);
		}

		Thread l1 = new Thread(() -> {
			try {
				for (int i = 0; i < board.getPinsCount(); i ++) {
					Pin pin = board.getPin(i);
					System.err.println(i+" "+pin.getSupportedModes());
					if (pin.getMode() == Mode.ANALOG) {
						if (pin.getSupportedModes().contains(Mode.OUTPUT)) {
							pin.setMode(Mode.OUTPUT);
						} else {
							pin.setMode(Mode.IGNORED);
						}
						
					}
				}
				board.stop();
				
				Pin myLED = board.getPin(13);
				myLED.setMode(Pin.Mode.OUTPUT);

				Pin myButton = board.getPin(9);
				myButton.addEventListener(new PinEventListener() {

					public void onModeChange(IOEvent event) {
					}

					public void onValueChange(IOEvent event) {
						System.err.println("new value " + event.getValue());
					}

				});
				

 
				// LED D4 on.
				myLED.setValue(1);

				// Pause for half a bit.
				try {
					Thread.sleep(2500);
				} catch (Exception ex) {
					System.out.println("sleep error.");
				}
				// LED D4 off.
				myLED.setValue(0);
			} catch (Exception e) {
				e.printStackTrace();
			}
		});
		l1.start();
		System.err.println("thread started");
		
		try {
			l1.join();
			System.err.println("thread started");
			board.stop();
			System.out.println("Board stopped.");
		} catch (IOException e) {
			System.out.println("couldn't stop board. " + e);
		} // finish with the board.
		catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
