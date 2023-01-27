package app.owlcms.firmata;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.firmata4j.IODevice;
import org.firmata4j.IODeviceEventListener;
import org.firmata4j.IOEvent;
import org.firmata4j.Pin;
import org.firmata4j.Pin.Mode;
import org.firmata4j.firmata.FirmataDevice;
import org.firmata4j.transport.JSerialCommTransport;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Logger;

public class Main {

	private static final int INITIAL_QUIET_DURATION = 5000;
	private static final int DEBOUNCE_DURATION = 150;

	private static final Logger logger = (Logger) LoggerFactory.getLogger(Main.class);

	public static void main(String[] args) {
		String myPort = "CNCA0"; // modify for your own computer & setup.

		IODevice board = new FirmataDevice(new JSerialCommTransport(myPort));

		Thread t1 = new Thread(() -> firmataThread(board));
		waitForever(board, t1);

	}

	private static void waitForever(IODevice board, Thread t1) {
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
			InputStream is = Main.class.getResourceAsStream("/Referee.xlsx");
			XSSFWorkbook workbook = new XSSFWorkbook(is);
			var dsr = new DeviceSpecReader();
			dsr.readPinDefinitions(workbook);
			var emitPinDefinitions = dsr.getEmitPinDefinitions();
			var buttonPinDefinitions = dsr.getButtonPinDefinitions();

			initBoard(board);
			initModes(board, emitPinDefinitions, buttonPinDefinitions);

			showPinConfig(board);

			startupLED(board);

			board.addEventListener(new IODeviceEventListener() {
				@Override
				public void onStart(IOEvent event) {
				}

				@Override
				public void onStop(IOEvent event) {
				}

				@Override
				public void onPinChange(IOEvent event) {
					// here we react to changes of pins' state
					Pin pin = event.getPin();
					if (pin.getMode() == Mode.PULLUP || pin.getMode() == Mode.INPUT) {
						if (debounce(pin.getIndex(), pin.getValue())) {
							logger.warn("new press on pin {} {}", pin.getIndex(), System.currentTimeMillis());
						}
					} else {
						logger.warn("output on pin {} {}", pin.getIndex(), System.currentTimeMillis());
					}
				}

				@Override
				public void onMessageReceive(IOEvent event, String message) {
					// here we react to receiving a text message from the device
					System.err.println(message);
				}
			});
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static void initModes(IODevice board, List<EmitPinDefinition> emitPinDefinition,
			List<ButtonPinDefinition> buttonPinDefinition) {
		emitPinDefinition.stream().forEach(i -> {
			try {
				logger.warn("emit {}", i.getPinNumber());
				Pin pin = board.getPin(i.getPinNumber());
				pin.setMode(Mode.OUTPUT);
//				pin.setValue(0L);
			} catch (IOException e) {
				e.printStackTrace();
			}
		});
		buttonPinDefinition.stream().forEach(i -> {
			try {
				logger.warn("button {}", i.getPinNumber());
				board.getPin(i.getPinNumber()).setMode(Mode.PULLUP);
			} catch (IOException e) {
				e.printStackTrace();
			}
		});
	}

	private static void startupLED(IODevice board) {
		Pin myLED = board.getPin(13);
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
	}

	private static void initBoard(IODevice board) {
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
	}

	private static void initDebounce(IODevice board) {
		long now = System.currentTimeMillis();
		long until = now + INITIAL_QUIET_DURATION;
		logger.warn("init end = {}", until);
		for (int i = 0; i < ignoredUntil.length; i++) {
			ignoredUntil[i] = until;
		}
	}

	@SuppressWarnings("unused")
	private static void showPinConfig(IODevice board) throws IOException {
		for (int i = 0; i < board.getPinsCount(); i++) {
			Pin pin = board.getPin(i);
			logger.warn("{} {}", i, pin.getSupportedModes());
			if (pin.getMode() == Mode.ANALOG) {
				if (pin.getSupportedModes().contains(Mode.OUTPUT)) {
					pin.setMode(Mode.OUTPUT);
				}
			}
		}
	}

	protected static boolean debounce(byte index, long value) {
		long now = System.currentTimeMillis();
		long end = ignoredUntil[index - 1];
		if (value == 1 && (now > end)) {
			// logger.trace("ok now={} end={} : elapsed {}", now, end, now - end);

			// valid press.
			// we start a new blocked duration to avoid stutter
			ignoredUntil[index - 1] = now + DEBOUNCE_DURATION; // wait
			return true;
		} else {
			// logger.trace("blocked {} now={} end={} : {} {}", value, now, end, (end - now)
			// < 0 ? "elapsed" : "remaining", Math.abs(end - now));
		}
		return false;
	}

}
