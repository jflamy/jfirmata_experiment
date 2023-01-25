package app.owlcms.firmata;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

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

		Thread t1 = new Thread(() -> firmataThread(board));
		t1.start();
		

		try {
			Thread.sleep(Long.MAX_VALUE);
			t1.join();
			board.stop();
			System.out.println("Board stopped.");
		} catch (InterruptedException e) {
			System.out.println("Thread interrupted.");
		} catch (IOException e) {
		}


	}

	private static void firmataThread(IODevice board) {
		try {
			try {
				board.start(); // start comms with board;
				System.out.println("Board started.");
				board.ensureInitializationIsDone();
			} catch (Exception ex) {
				System.out.println("couldn't connect to board. " + ex);
				System.exit(-1);
			}
			
			for (int i = 0; i < board.getPinsCount(); i++) {
				Pin pin = board.getPin(i);
				System.err.println(i + " " + pin.getSupportedModes());
				if (pin.getMode() == Mode.ANALOG) {
					if (pin.getSupportedModes().contains(Mode.OUTPUT)) {
						pin.setMode(Mode.OUTPUT);
					}
				}
			}

			Pin myLED = board.getPin(13);
			myLED.setMode(Pin.Mode.OUTPUT);
			// LED D4 on.
			
			new Timer().schedule(new TimerTask() {
				@Override
				public void run() {
					try {
						System.out.println("turning on LED");
						myLED.setValue(1);
					} catch (IllegalStateException | IOException e) {
					}					
				}
			}, 2500);
			new Timer().schedule(new TimerTask() {
				@Override
				public void run() {
					try {
						System.out.println("turning off LED");
						myLED.setValue(0);
					} catch (IllegalStateException | IOException e) {
					}					
				}
			}, 5000);

			Pin myButton = board.getPin(9);
			myButton.addEventListener(new PinEventListener() {

				public void onModeChange(IOEvent event) {
				}

				public void onValueChange(IOEvent event) {
					System.err.println("new value " + event.getValue());
				}

			});
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
