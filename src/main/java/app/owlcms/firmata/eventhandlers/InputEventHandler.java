package app.owlcms.firmata.eventhandlers;

import java.util.List;

import org.eclipse.paho.client.mqttv3.MqttException;
import org.slf4j.LoggerFactory;

import app.owlcms.firmata.devicespec.InputPinDefinition;
import app.owlcms.firmata.mqtt.FMQTTMonitor;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;

/**
 * Perform actions resulting from the receipt of an input on the board.
 * 
 * These actions trigger the sending of MQTT messages
 * The messages to be sent are found in a definition table.
 * 
 * @author jflamy
 *
 */
public class InputEventHandler {
	private final Logger logger = (Logger) LoggerFactory.getLogger(InputEventHandler.class);

	private List<InputPinDefinition> definitions;

	private String platform;

	public InputEventHandler(List<InputPinDefinition> definitions, String platform) {
		this.definitions = definitions;
		this.platform = platform;
		logger.setLevel(Level.DEBUG);
	}

	public List<InputPinDefinition> getDefinitions() {
		return definitions;
	}

	public void setDefinitions(List<InputPinDefinition> definitions) {
		this.definitions = definitions;
	}

	public void handle(byte index, FMQTTMonitor mqtt) {
		definitions.stream()
			.filter(d -> d.getPinNumber() == index)
			.forEach(d -> {
				try {
					String topic = "owlcms/" + d.topic+ "/" + platform;
					logger.debug("button {} : sending {} {}", index, topic, d.message);
					mqtt.publishMqttMessage(topic, d.message);
				} catch (MqttException e) {
					logger.error("could not publish message: {}", e);
				}
			});
	}

}
