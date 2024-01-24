package app.owlcms.firmata.mqtt;

import org.eclipse.paho.client.mqttv3.MqttAsyncClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttPersistenceException;
import org.eclipse.paho.client.mqttv3.MqttSecurityException;
import org.slf4j.LoggerFactory;

import app.owlcms.firmata.data.Config;
import app.owlcms.firmata.data.DeviceConfig;
import app.owlcms.firmata.eventhandlers.OutputEventHandler;
import app.owlcms.firmata.refdevice.RefDevice;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;

/**
 * This class receives and emits MQTT events.
 *
 * Events initiated by the devices start with topics that names the device (owlcms/jurybox) Devices do not listen to
 * other devices. Devices listen to MQTT events that come from the field of play. These events are of the form
 * (owlcms/fop). The field of play is always the last element in the topic.
 *
 * @author Jean-François Lamy
 */
public class FopMQTTMonitor extends AbstractMQTTMonitor {

	private static final String OWLCMS_FOP = "owlcms/fop/#";
	MqttAsyncClient client;
	String fopName;
	Logger logger = (Logger) LoggerFactory.getLogger(FopMQTTMonitor.class);
	FopMQTTCallback callback;
	OutputEventHandler emitDefinitionHandler;
	RefDevice board;
	boolean closed;

	public FopMQTTMonitor(String fopName, OutputEventHandler emitDefinitionHandler, RefDevice board,
	        DeviceConfig config) {
		logger.setLevel(Level.DEBUG);
		this.setFopName(fopName);
		this.board = board;
		this.emitDefinitionHandler = emitDefinitionHandler;
		try {
			String mqttServer = Config.getCurrent().getMqttServer();
			if (mqttServer != null && !mqttServer.isBlank()) {
				client = createMQTTClient(fopName);
				connectionLoop(client);
			} else {
				logger.info("no MQTT server configured, skipping");
			}
		} catch (MqttException e) {
			logger.error("cannot initialize MQTT: {}", e);
		}
	}

	String getFopName() {
		return fopName;
	}

	private void setFopName(String fopName) {
		this.fopName = fopName;
	}
	
	public String getName(FopMQTTMonitor fopMQTTMonitor) {
		return fopName;
	}


	public void publishMqttMessageForFop(String topic, String message) throws MqttException, MqttPersistenceException {
		topic = topic + "/" + getFopName();
		publishMqttMessage(topic, message);
	}

	
	protected MqttConnectOptions setupMQTTClient(String userName, String password, FopMQTTMonitor fopMQTTMonitor) {
		MqttConnectOptions connOpts = setUpConnectionOptions(userName != null ? userName : "",
		        password != null ? password : "");
		callback = new FopMQTTCallback(this, emitDefinitionHandler, board);
		client.setCallback(callback);
		return connOpts;
	}
	
	@Override
	public void doConnect() throws MqttSecurityException, MqttException {
		userName = Config.getCurrent().getMqttUsername();
		password = Config.getCurrent().getMqttPassword();
		MqttConnectOptions connOpts = setupMQTTClient(userName, password, this);
		client.connect(connOpts).waitForCompletion();
		client.subscribe(OWLCMS_FOP, 0);
		logger.info("Platform {} MQTT subscribed to {} {}", getFopName(), OWLCMS_FOP,
		        client.getCurrentServerURI());
	}

	@Override
	public String getName() {
		return getFopName();
	}
}
