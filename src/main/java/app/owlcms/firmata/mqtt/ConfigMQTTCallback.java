package app.owlcms.firmata.mqtt;

import java.nio.charset.StandardCharsets;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import app.owlcms.firmata.ui.MainView;
import ch.qos.logback.classic.Logger;

/**
 * This class contains the routines executed when an MQTT message is
 * received.
 */
public class ConfigMQTTCallback implements MqttCallback {

	final Logger logger = (Logger) LoggerFactory.getLogger(MqttCallback.class);

	private final ConfigMQTTMonitor mqttMonitor;


	ConfigMQTTCallback(ConfigMQTTMonitor configMQTTMonitor, MainView mainView) {
		this.mqttMonitor = configMQTTMonitor;
	}

	@Override
	public void connectionLost(Throwable cause) {
		logger.debug("{}lost connection to MQTT: {}", this.mqttMonitor.getFopName(), cause.getLocalizedMessage());
		// Called when the client lost the connection to the broker
		this.mqttMonitor.connectionLoop(this.mqttMonitor.client, -1);
	}

	@Override
	public void deliveryComplete(IMqttDeliveryToken token) {
		// required by abstract class
	}

	@Override
	public void messageArrived(String topic, MqttMessage message) throws Exception {
		String messageStr = new String(message.getPayload(), StandardCharsets.UTF_8);
		var ntopic = topic.trim();
		if (ntopic.startsWith("owlcms/fop/config")) {
			logger.debug("handling {} {}", ntopic, messageStr);
			ObjectMapper objectMapper = new ObjectMapper();

			JsonNode jsonNode = objectMapper.readTree(messageStr);

			logger.warn(jsonNode.get("platforms").asText());
			
		} else {
			// ignored
		}
	}


}