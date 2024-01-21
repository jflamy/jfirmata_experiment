package app.owlcms.firmata.mqtt;

import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttSecurityException;
import org.slf4j.LoggerFactory;

import app.owlcms.firmata.ui.MainView;
import app.owlcms.firmata.utils.MQTTServerConfig;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;

/**
 * This class receives and emits MQTT events.
 *
 * Events initiated by the devices start with topics that names the device
 * (owlcms/jurybox) Devices do not listen to other devices. Devices listen to MQTT
 * events that come from the field of play. These events are of the form
 * (owlcms/fop). The field of play is always the last element in the topic.
 *
 * @author Jean-François Lamy
 */
public class ConfigMQTTMonitor extends MQTTMonitor {

	private static final String OWLCMS_CONFIG = "owlcms/fop/config/#";
	private String fopName;
	static Logger logger = (Logger) LoggerFactory.getLogger(ConfigMQTTMonitor.class);
	private String password;

	private String userName;
	private ConfigMQTTCallback callback;
	private MainView view;

	public ConfigMQTTMonitor(MainView view) {
		logger.setLevel(Level.DEBUG);
		this.view = view;
		register();
		try {
			String mqttServer = MQTTServerConfig.getCurrent().getMqttServer();
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

	@Override
	public String getName() {
		return "config";
	}

	public MqttConnectOptions setupMQTTClient(String userName, String password) {
		MqttConnectOptions connOpts = setUpConnectionOptions(userName != null ? userName : "",
				password != null ? password : "");
		callback = new ConfigMQTTCallback(this, view);
		client.setCallback(callback);
		return connOpts;
	}

	@Override
	public void doConnect() throws MqttSecurityException, MqttException {
		userName = MQTTServerConfig.getCurrent().getMqttUsername();
		password = MQTTServerConfig.getCurrent().getMqttPassword();
		MqttConnectOptions connOpts = setupMQTTClient(userName, password);
		client.connect(connOpts).waitForCompletion();
		client.subscribe(OWLCMS_CONFIG, 0);
		logger.info("Platform {} MQTT subscribed to {} {}", getName(), OWLCMS_CONFIG,
		        client.getCurrentServerURI());
	}

	public boolean isConnected() {
		return client.isConnected();
	}

}
