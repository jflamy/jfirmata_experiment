package app.owlcms.firmata.board;

import org.firmata4j.IODeviceEventListener;
import org.firmata4j.IOEvent;
import org.firmata4j.Pin;
import org.firmata4j.Pin.Mode;
import org.slf4j.LoggerFactory;

import app.owlcms.firmata.Main;
import app.owlcms.firmata.devicespec.ButtonPinDefinitionHandler;
import app.owlcms.firmata.mqtt.MQTTMonitor;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;

public final class DeviceEventListener implements IODeviceEventListener {
	
	final Logger logger = (Logger) LoggerFactory.getLogger(Main.class);
	
	private final Board board;
	private ButtonPinDefinitionHandler buttonPinDefinitions;

	private MQTTMonitor mqtt;

	public DeviceEventListener(
			Board board,
			ButtonPinDefinitionHandler buttonPinDefinitions, 
			MQTTMonitor mqtt) {
		this.board = board;
		this.buttonPinDefinitions = buttonPinDefinitions;
		this.mqtt = mqtt;
		logger.setLevel(Level.DEBUG);
		logger.debug("device event listener ok");
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
				// this is the triggering edge of a button press
				buttonPinDefinitions.handle(pin.getIndex(), mqtt);
			}
		}
	}

	@Override
	public void onStart(IOEvent event) {
	}

	@Override
	public void onStop(IOEvent event) {
	}
}