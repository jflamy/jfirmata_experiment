package app.owlcms.firmata.mqtt;

import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttPersistenceException;
import org.slf4j.LoggerFactory;

import app.owlcms.firmata.data.DeviceConfig;
import app.owlcms.firmata.eventhandlers.OutputEventHandler;
import app.owlcms.firmata.refdevice.RefDevice;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;

/**
 * This class receives and emits MQTT events for individual devices.  The actual processing is done in a callback class.
 *
 * Events initiated by the devices start with topics that name the device (owlcms/jurybox)
 * 
 * Devices do not listen to other devices.
 * 
 * A monitor listen to MQTT events that come from a single field of play. These events are of the form (owlcms/fop). The field
 * of play is always the last element in the topic.
 *
 * @author Jean-François Lamy
 */
public class FopMQTTMonitor extends AbstractMQTTMonitor {

	private static final String OWLCMS_FOP = "owlcms/fop/#";
	RefDevice board;
	boolean closed;
	OutputEventHandler emitDefinitionHandler;
	Logger logger = (Logger) LoggerFactory.getLogger(FopMQTTMonitor.class);

	public FopMQTTMonitor(String fopName, OutputEventHandler emitDefinitionHandler, RefDevice board,
	        DeviceConfig config) {
		logger.setLevel(Level.DEBUG);
		this.setName(fopName);
		this.setSubscription(OWLCMS_FOP);
		this.board = board;
		this.emitDefinitionHandler = emitDefinitionHandler;
		this.start(fopName);
	}

	public void publishMqttMessageForFop(String topic, String message) throws MqttException, MqttPersistenceException {
		topic = topic + "/" + getName();
		publishMqttMessage(topic, message);
	}

	@Override
	protected MqttConnectOptions setupMQTTClient(String userName, String password) {
		MqttConnectOptions connOpts = setUpConnectionOptions(userName != null ? userName : "",
		        password != null ? password : "");
		client.setCallback(new FopMQTTCallback(this, emitDefinitionHandler, board));
		return connOpts;
	}

}
