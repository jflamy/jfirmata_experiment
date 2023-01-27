package app.owlcms.firmata.board;

import org.firmata4j.IODeviceEventListener;
import org.firmata4j.IOEvent;
import org.firmata4j.Pin;
import org.firmata4j.Pin.Mode;
import org.slf4j.LoggerFactory;

import app.owlcms.firmata.Main;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;

public final class DeviceEventListener implements IODeviceEventListener {
	
	final Logger logger = (Logger) LoggerFactory.getLogger(Main.class);
	
	private final Board board;

	public DeviceEventListener(Board board) {
		this.board = board;
		logger.setLevel(Level.DEBUG);
	}

	@Override
	public void onMessageReceive(IOEvent event, String message) {
		// here we react to receiving a text message from the device
		System.err.println(message);
	}

	@Override
	public void onPinChange(IOEvent event) {
		// here we react to changes of pin state
		Pin pin = event.getPin();
		if (pin.getMode() == Mode.PULLUP || pin.getMode() == Mode.INPUT) {
			if (board.debounce(pin.getIndex(), pin.getValue())) {
				logger.debug("new press on pin {} {}", pin.getIndex(), System.currentTimeMillis());
			}
		} else {
			logger.debug("output on pin {} {}", pin.getIndex(), System.currentTimeMillis());
		}
	}

	@Override
	public void onStart(IOEvent event) {
	}

	@Override
	public void onStop(IOEvent event) {
	}
}