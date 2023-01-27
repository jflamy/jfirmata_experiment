package app.owlcms.firmata;

import java.io.InputStream;

import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.firmata4j.IODevice;
import org.firmata4j.firmata.FirmataDevice;
import org.firmata4j.transport.JSerialCommTransport;
import org.slf4j.LoggerFactory;

import app.owlcms.firmata.board.Board;
import app.owlcms.firmata.board.DeviceEventListener;
import app.owlcms.firmata.devicespec.DeviceSpecReader;
import ch.qos.logback.classic.Logger;

public class Main {

	static final Logger logger = (Logger) LoggerFactory.getLogger(Main.class);

	private static void firmataThread(String myPort, InputStream is) {
		try {
			IODevice device = new FirmataDevice(new JSerialCommTransport(myPort));
			XSSFWorkbook workbook = new XSSFWorkbook(is);
			var dsr = new DeviceSpecReader();
			dsr.readPinDefinitions(workbook);
			var emitPinDefinitions = dsr.getEmitPinDefinitions();
			var buttonPinDefinitions = dsr.getButtonPinDefinitions();
			
			var board = new Board();
			board.initBoard(device);
			board.initModes(device, emitPinDefinitions, buttonPinDefinitions);
			board.showPinConfig(device);
			board.startupLED(device);

			device.addEventListener(new DeviceEventListener(board));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		String myPort = "CNCA0"; // modify for your own computer & setup.
		InputStream is = Main.class.getResourceAsStream("/Referee.xlsx");
		
		Thread t1 = new Thread(() -> firmataThread(myPort, is));
		waitForever(t1);

	}

	private static void waitForever(Thread t1) {
		t1.start();
		try {
			Thread.sleep(Long.MAX_VALUE);
			t1.join();
		} catch (InterruptedException e) {
			logger.warn("Thread interrupted.");
		}
	}
	
	private static long[] ignoredUntil = new long[54];

<<<<<<< HEAD
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
=======
	public static Logger getStartupLogger() {
		return logger;
>>>>>>> branch 'main' of https://github.com/jflamy/owlcms-firmata.git
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
