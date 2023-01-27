package app.owlcms.firmata;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

import org.firmata4j.IODevice;
import org.firmata4j.IODeviceEventListener;
import org.firmata4j.IOEvent;
import org.firmata4j.Pin;
import org.firmata4j.Pin.Mode;
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
	
	private static long[] ignoredUntil = new long[54];

	private static void firmataThread(IODevice board) {
		try {
			try {
				initDebounce(board);
				board.start(); // start comms with board;
				System.out.println("Communications started.");
				board.ensureInitializationIsDone();
				System.out.println("Board initialized.");
			} catch (Exception ex) {
				System.out.println("couldn't connect to board. " + ex);
				System.exit(-1);
			}
			
			//showPinConfig(board);

			Pin myLED = board.getPin(13);
			myLED.setMode(Pin.Mode.OUTPUT);
			Pin myButton = board.getPin(6);
			myButton.setMode(Pin.Mode.INPUT);
			myButton.setMode(Pin.Mode.PULLUP);
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

			board.addEventListener(new IODeviceEventListener() {
			    @Override
			    public void onStart(IOEvent event) {
			        // since this moment we are sure that the device is initialized
			        // so we can hide initialization spinners and begin doing cool stuff
			        System.out.println("Device is ready");
			    }

			    @Override
			    public void onStop(IOEvent event) {
			        // since this moment we are sure that the device is properly shut down
			        System.out.println("Device has been stopped");
			    }

			    @Override
			    public void onPinChange(IOEvent event) {
			        // here we react to changes of pins' state
			        Pin pin = event.getPin();
			        if (debounce(pin.getIndex(), pin.getValue())) {
			        	System.out.println("new press on pin "+pin.getIndex());
			        }
			    }

			    @Override
			    public void onMessageReceive(IOEvent event, String message) {
			        // here we react to receiving a text message from the device
			        System.out.println(message);
			    }
			});
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static void initDebounce(IODevice board) {
		long now = System.currentTimeMillis();
		for (int i = 0; i < board.getPinsCount(); i++) {
			ignoredUntil[i] = now + 1000;
		}
	}

	@SuppressWarnings("unused")
	private static void showPinConfig(IODevice board) throws IOException {
		for (int i = 0; i < board.getPinsCount(); i++) {
			Pin pin = board.getPin(i);
			System.err.println(i + " " + pin.getSupportedModes());
			if (pin.getMode() == Mode.ANALOG) {
				if (pin.getSupportedModes().contains(Mode.OUTPUT)) {
					pin.setMode(Mode.OUTPUT);
				}
			}
		}
	}


	protected static boolean debounce(byte index, long value) {
		long now = System.currentTimeMillis();
		if (value == 1 && now > ignoredUntil[index-1]) {
			ignoredUntil[index-1] = now + 120; // wait 120ms
			return true;
		}
		return false;
	} 

}
