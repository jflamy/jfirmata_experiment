package app.owlcms.firmata.board;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

import org.firmata4j.IODevice;
import org.firmata4j.Pin;
import org.firmata4j.Pin.Mode;
import org.slf4j.LoggerFactory;

import app.owlcms.firmata.devicespec.ButtonPinDefinitionHandler;
import app.owlcms.firmata.devicespec.OutputPinDefinitionHandler;
import ch.qos.logback.classic.Logger;

public class Board {

	private final int DEBOUNCE_DURATION = 150;
	private final int INITIAL_QUIET_DURATION = 5000;

	private long[] ignoredUntil = new long[54];

	private final Logger logger = (Logger) LoggerFactory.getLogger(Board.class);
	private IODevice device;
	private ButtonPinDefinitionHandler buttonPinDefinitions;
	private OutputPinDefinitionHandler outputPinDefinitions;
	
	public Board(IODevice device, OutputPinDefinitionHandler outputPinDefinitions, ButtonPinDefinitionHandler buttonPinDefinitions) {
		this.device = device;
		this.outputPinDefinitions = outputPinDefinitions;
		this.buttonPinDefinitions = buttonPinDefinitions;
		
		init();
	}

	private void init() {
		try {
			initBoard();
			initModes();
			if (logger.isTraceEnabled()) {
				showPinConfig();
			}
			startupLED();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

	}

	public boolean debounce(byte index, long value) {
		long now = System.currentTimeMillis();
		long end = ignoredUntil[index - 1];
		if (value == 1 && (now > end)) {
			// logger.trace("ok now={} end={} : elapsed {}", now, end, now - end);

			// valid press. we start a new blocked duration to avoid stutter
			ignoredUntil[index - 1] = now + DEBOUNCE_DURATION; // wait
			return true;
		} else {
			// logger.trace("blocked {} now={} end={} : {} {}", value, now, end, (end - now) < 0 ? "elapsed" : "remaining", Math.abs(end - now));
		}
		return false;
	}

	public void initBoard() {
		try {
			initDebounce(device);
			device.start(); // start comms with board;
			logger.info("Communications started.");
			device.ensureInitializationIsDone();
			logger.info("Board initialized.");
		} catch (Exception ex) {
			logger.error("Could not connect to board. " + ex);
			System.exit(-1);
		}
	}

	public void initDebounce(IODevice board) {
		long now = System.currentTimeMillis();
		long until = now + INITIAL_QUIET_DURATION;
		for (int i = 0; i < ignoredUntil.length; i++) {
			ignoredUntil[i] = until;
		}
	}

	public void initModes() {
			outputPinDefinitions.getDefinitions().stream().forEach(i -> {
				try {
					logger.debug("output {}", i.getPinNumber());
					Pin pin = device.getPin(i.getPinNumber());
					pin.setMode(Mode.OUTPUT);
	//				pin.setValue(0L);
				} catch (IOException e) {
					e.printStackTrace();
				}
			});
			buttonPinDefinitions.getDefinitions().stream().forEach(i -> {
				try {
					logger.debug("button {}", i.getPinNumber());
					device.getPin(i.getPinNumber()).setMode(Mode.PULLUP);
				} catch (IOException e) {
					e.printStackTrace();
				}
			});
		}
	
	public void showPinConfig() throws IOException {
		for (int i = 0; i < device.getPinsCount(); i++) {
			Pin pin = device.getPin(i);
			logger.debug("{} {}", i, pin.getMode());
		}
	}
	public void startupLED() {
		Pin myLED = device.getPin(13);
		new Timer().schedule(new TimerTask() {
			@Override
			public void run() {
				try {
					logger.debug("startup LED on");
					myLED.setValue(1);
				} catch (IllegalStateException | IOException e) {
				}
			}
		}, 2500);
		new Timer().schedule(new TimerTask() {
			@Override
			public void run() {
				try {
					logger.debug("startup LED off");
					myLED.setValue(0);
				} catch (IllegalStateException | IOException e) {
				}
			}
		}, 5000);
	}

}
