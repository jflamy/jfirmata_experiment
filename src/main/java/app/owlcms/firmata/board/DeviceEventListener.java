package app.owlcms.firmata.board;

import java.util.Timer;
import java.util.TimerTask;

import org.firmata4j.IODeviceEventListener;
import org.firmata4j.IOEvent;
import org.firmata4j.Pin;
import org.firmata4j.Pin.Mode;
import org.slf4j.LoggerFactory;

import app.owlcms.firmata.eventhandlers.InputEventHandler;
import app.owlcms.firmata.mqtt.MQTTMonitor;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;

public final class DeviceEventListener implements IODeviceEventListener {

	private class Debouncer {
		long debounceUntil;
		Timer debounceTimer = new Timer();
		
		void debounce() {
			debounceUntil = System.currentTimeMillis() + DEBOUNCE_DURATION;
			debounceTimer.schedule(new TimerTask() {

				@Override
				public void run() {
					debounceUntil = System.currentTimeMillis();
				}
			}, DEBOUNCE_DURATION);
		}
		
		boolean isIgnoring() {
			var now = System.currentTimeMillis();
			return now <= debounceUntil;
		}
		
	}
	final static long DEBOUNCE_DURATION = 300;

	final Logger logger = (Logger) LoggerFactory.getLogger(DeviceEventListener.class);
	private Debouncer[] debouncer = new Debouncer[Board.NB_MEGA_PINS];
	private InputEventHandler inputEventHandler;

	private MQTTMonitor mqtt;

	public DeviceEventListener(
	        InputEventHandler inputEventHandler,
	        MQTTMonitor mqtt) {
		this.inputEventHandler = inputEventHandler;
		this.mqtt = mqtt;
		initDebounce();
		logger.setLevel(Level.DEBUG);
	}

	@Override
	public void onMessageReceive(IOEvent event, String message) {
		// here we react to receiving a text message from the device
		logger.debug(message);
	}

	@Override
	public void onPinChange(IOEvent event) {
		// here we react to changes of pin state
		Pin pin = event.getPin();

		if (pin.getMode() == Mode.PULLUP || pin.getMode() == Mode.INPUT) {
			byte index = pin.getIndex();
			long value = pin.getValue();
			Debouncer debouncer2 = getDebouncer(index);
			//logger.trace("input {}={} {} {} {}", index, value, System.currentTimeMillis(), debouncer2.debounceUntil, debouncer2.isIgnoring());

			if (!debouncer2.isIgnoring()) {
				// we assume the presence of pull-up resistor. 0 = pressed.
				// we only care about press, not release.
				if (value == 0) {
					inputEventHandler.handle(index, mqtt);
				}
				// the input is bouncing. ignore transitions on pin
				// until timer comes back
				debouncer2.debounce();

			}
		}
	}

	@Override
	public void onStart(IOEvent event) {
	}

	@Override
	public void onStop(IOEvent event) {
	}

	private Debouncer getDebouncer(byte index) {
		return debouncer[index - 1];
	}

	private void initDebounce() {
		long now = System.currentTimeMillis();
		for (int i = 0; i < Board.NB_MEGA_PINS; i++) {
			debouncer[i] = new Debouncer();
			debouncer[i].debounceUntil = now;
		}
	}

}