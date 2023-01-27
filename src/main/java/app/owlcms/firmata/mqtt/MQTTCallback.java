package app.owlcms.firmata.mqtt;

import java.nio.charset.StandardCharsets;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.slf4j.LoggerFactory;

import app.owlcms.firmata.Main;
import app.owlcms.firmata.board.Board;
import app.owlcms.firmata.devicespec.OutputPinDefinitionHandler;
import ch.qos.logback.classic.Logger;

/**
 * This inner class contains the routines executed when an MQTT message is
 * received.
 */
class MQTTCallback implements MqttCallback {
	
	final Logger logger = (Logger) LoggerFactory.getLogger(Main.class);

	private final MQTTMonitor mqttMonitor;
	String fopTopicName;
	private OutputPinDefinitionHandler outputPinDefinitionHandler;

	MQTTCallback(MQTTMonitor mqttMonitor, OutputPinDefinitionHandler outputPinDefinitionHandler, Board board) {
		this.outputPinDefinitionHandler = outputPinDefinitionHandler;
		this.mqttMonitor = mqttMonitor;
		// these are the owlcms-initiated events that the monitor tracks
		this.fopTopicName = "owlcms/fop/#";
	}

	@Override
	public void connectionLost(Throwable cause) {
		MQTTMonitor.logger.debug("{}lost connection to MQTT: {}", this.mqttMonitor.getFopName(),
				cause.getLocalizedMessage());
		// Called when the client lost the connection to the broker
		this.mqttMonitor.connectionLoop(this.mqttMonitor.client);
	}

	@Override
	public void deliveryComplete(IMqttDeliveryToken token) {
		// required by abstract class
	}

	@Override
	public void messageArrived(String topic, MqttMessage message) throws Exception {
		new Thread(() -> {
			String messageStr = new String(message.getPayload(), StandardCharsets.UTF_8);
			MQTTMonitor.logger.info("{}{} : {}", this.mqttMonitor.getFopName(), topic, messageStr.trim());
			if (topic.startsWith(fopTopicName) && topic.endsWith("/" + this.mqttMonitor.getFopName())) {
				outputPinDefinitionHandler.getDefinitions().stream()
					.filter(d -> d.topic.startsWith(fopTopicName))
					.forEach(d -> {
							// FIXME call board
						});
			} else {
				MQTTMonitor.logger.error("{}Malformed MQTT unrecognized topic message topic='{}' message='{}'",
						this.mqttMonitor.getFopName(), topic, messageStr);
			}
		}).start();
	}

//		/**
//		 * Tell others that the refbox has given the down signal
//		 * 
//		 * @param topic
//		 * @param messageStr
//		 */
//		private void postFopEventDownEmitted(String topic, String messageStr) {
//			messageStr = messageStr.trim();
//		}

}