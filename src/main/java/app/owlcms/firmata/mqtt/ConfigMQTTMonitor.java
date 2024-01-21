package app.owlcms.firmata.mqtt;

import java.nio.charset.StandardCharsets;

import org.eclipse.paho.client.mqttv3.MqttAsyncClient;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttPersistenceException;
import org.eclipse.paho.client.mqttv3.MqttSecurityException;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.slf4j.LoggerFactory;

import app.owlcms.firmata.ui.Main;
import app.owlcms.firmata.ui.MainView;
import app.owlcms.firmata.utils.LoggerUtils;
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
public class ConfigMQTTMonitor implements MQTTMonitor {

	private static final String OWLCMS_CONFIG = "owlcms/fop/config";
	MqttAsyncClient client;
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
				connectionLoop(client, 5);
			} else {
				logger.info("no MQTT server configured, skipping");
			}
		} catch (MqttException e) {
			logger.error("cannot initialize MQTT: {}", LoggerUtils.stackTrace(e));
		}
	}
	
	@Override
	public void close() {
		try {
			client.close(true);
		} catch (MqttException e) {
			LoggerUtils.logError(logger, e);
		}
	}

	public MqttAsyncClient createMQTTClient(String fopName) throws MqttException {
		String server = MQTTServerConfig.getCurrent().getMqttServer();
		server = (server != null ? server : "127.0.0.1");
		String port = MQTTServerConfig.getCurrent().getMqttPort();
		port = (port != null ? port : "1883");
		String protocol = port.startsWith("8") ? "ssl://" : "tcp://";
		Main.getStartupLogger().info("connecting to MQTT {}{}:{}", protocol, server, port);

		MqttAsyncClient client = new MqttAsyncClient(protocol + server + ":" + port,
				fopName + "_" + MqttClient.generateClientId(), // ClientId
				new MemoryPersistence()); // Persistence
		return client;
	}

	public String getFopName() {
		return fopName;
	}

	public void setFopName(String fopName) {
		this.fopName = fopName;
	}

	@Override
	public boolean connectionLoop(MqttAsyncClient mqttAsyncClient, int max) {
		int i = 0;
		while (!mqttAsyncClient.isConnected()) {
			try {
				// doConnect will generate a new client Id, and wait for completion
				// client.reconnect() and automaticReconnection do not work as I expect.
				doConnect();
			} catch (Exception e1) {
				logger.error("{}MQTT refereeing device server error: {}", getFopName(),
						e1.getCause() != null ? e1.getCause().getMessage() : e1);
			}
			if (max > 0 && i <= max) {
				sleep(1000);
				i++;
			} else {
				break;
			}
		}
		return false;
	}

	private void doConnect() throws MqttSecurityException, MqttException {
			userName = MQTTServerConfig.getCurrent().getMqttUsername();
			password = MQTTServerConfig.getCurrent().getMqttPassword();
		MqttConnectOptions connOpts = setupMQTTClient(userName, password);
		client.connect(connOpts).waitForCompletion();

		client.subscribe(OWLCMS_CONFIG, 0);
		logger.info("Platform {} MQTT subscribed to {} {}", getFopName(), OWLCMS_CONFIG,
				client.getCurrentServerURI());
	}

	public void publishMqttMessageForFop(String topic, String message) throws MqttException, MqttPersistenceException {
		topic = topic + "/" + getFopName();
		publishMqttMessage(topic, message);
	}

	public void publishMqttMessage(String topic, String message) throws MqttException, MqttPersistenceException {
		MqttMessage message2 = new MqttMessage(message.getBytes(StandardCharsets.UTF_8));
		client.publish(topic, message2);
	}

	private MqttConnectOptions setUpConnectionOptions(String username, String password) {
		MqttConnectOptions connOpts = new MqttConnectOptions();
		connOpts.setCleanSession(true);
		if (username != null) {
			connOpts.setUserName(username);
		}
		if (password != null) {
			connOpts.setPassword(password.toCharArray());
		}
		connOpts.setCleanSession(true);
		// connOpts.setAutomaticReconnect(true);
		return connOpts;
	}

	private MqttConnectOptions setupMQTTClient(String userName, String password) {
		MqttConnectOptions connOpts = setUpConnectionOptions(userName != null ? userName : "",
				password != null ? password : "");
		callback = new ConfigMQTTCallback(this, view);
		client.setCallback(callback);
		return connOpts;
	}

	private void sleep(int ms) {
		try {
			Thread.sleep(ms);
		} catch (InterruptedException e) {
		}
	}

	@Override
	public String getName() {
		return "config";
	}

}
