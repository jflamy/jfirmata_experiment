package app.owlcms.firmata.mqtt;

import java.nio.charset.StandardCharsets;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.slf4j.LoggerFactory;

import app.owlcms.firmata.board.Board;
import app.owlcms.firmata.eventhandlers.OutputEventHandler;
import ch.qos.logback.classic.Logger;

/**
 * This inner class contains the routines executed when an MQTT message is
 * received.
 */
public class MQTTCallback implements MqttCallback {

	final Logger logger = (Logger) LoggerFactory.getLogger(MqttCallback.class);

	private final MQTTMonitor mqttMonitor;
	private OutputEventHandler outputEventHandler;
	private Board board;

	MQTTCallback(MQTTMonitor mqttMonitor, OutputEventHandler outputEventHandler, Board board) {
		this.outputEventHandler = outputEventHandler;
		this.mqttMonitor = mqttMonitor;
		this.board = board;
	}

	@Override
	public void connectionLost(Throwable cause) {
		logger.debug("{}lost connection to MQTT: {}", this.mqttMonitor.getFopName(),
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
//		new Thread(() -> {
			String messageStr = new String(message.getPayload(), StandardCharsets.UTF_8);
			var ntopic = topic.trim();
			if (ntopic.startsWith("owlcms/fop/") && ntopic.endsWith("/" + this.mqttMonitor.getFopName())) {
				logger.debug("handling {} {}", ntopic, messageStr);
				outputEventHandler.handle(simplifyTopic(ntopic), messageStr, board);
			} else {
				logger.error("{} Malformed MQTT unrecognized topic message topic='{}' message='{}'",
						this.mqttMonitor.getFopName(), topic, messageStr);
			}
//		}).start();
	}

	/**
	 * Remove leading and trailing parts to simplify matching
	 * @param topic
	 * @return
	 */
	private String simplifyTopic(String topic) {
		String simpleTopic = topic.substring(topic.indexOf("/")+1);
		simpleTopic = simpleTopic.substring(0, simpleTopic.lastIndexOf('/'));
		return simpleTopic;
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