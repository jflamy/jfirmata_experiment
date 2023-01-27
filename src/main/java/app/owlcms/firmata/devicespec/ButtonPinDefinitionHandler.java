package app.owlcms.firmata.devicespec;

import java.util.List;

import org.eclipse.paho.client.mqttv3.MqttException;
import org.slf4j.LoggerFactory;

import app.owlcms.firmata.mqtt.MQTTMonitor;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;

public class ButtonPinDefinitionHandler {
	private final Logger logger = (Logger) LoggerFactory.getLogger(ButtonPinDefinitionHandler.class);

	private List<ButtonPinDefinition> definitions;

	public ButtonPinDefinitionHandler(List<ButtonPinDefinition> definitions) {
		this.definitions = definitions;
		logger.setLevel(Level.DEBUG);
	}

	public List<ButtonPinDefinition> getDefinitions() {
		return definitions;
	}

	public void setDefinitions(List<ButtonPinDefinition> definitions) {
		this.definitions = definitions;
	}

	public void handle(byte index, MQTTMonitor mqtt) {
		definitions.stream().filter(d -> d.getPinNumber() == index).forEach(d -> {
			logger.debug("button {} : sending {} {}", index, d.topic, d.message);
			try {
				mqtt.publishMqttMessage("owlcms/"+d.topic, d.message);
			} catch (MqttException e) {
				logger.error("could not publish message: {}", e);
			}
		});
	}

}
