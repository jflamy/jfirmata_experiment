package app.owlcms.firmata.board;

import java.io.IOException;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.firmata4j.IODevice;
import org.firmata4j.Pin;
import org.firmata4j.Pin.Mode;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Logger;

public class Board {

	private final int DEBOUNCE_DURATION = 150;
	private final int INITIAL_QUIET_DURATION = 5000;

	private long[] ignoredUntil = new long[54];

	private final Logger logger = (Logger) LoggerFactory.getLogger(Board.class);

	public boolean debounce(byte index, long value) {
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

	public void initBoard(IODevice board) {
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

	public void initDebounce(IODevice board) {
		long now = System.currentTimeMillis();
		long until = now + INITIAL_QUIET_DURATION;
		logger.warn("init end = {}", until);
		for (int i = 0; i < ignoredUntil.length; i++) {
			ignoredUntil[i] = until;
		}
	}

	public void initModes(IODevice board, List<EmitPinDefinition> emitPinDefinition,
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
	public void showPinConfig(IODevice board) throws IOException {
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
	public void startupLED(IODevice board) {
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

}
