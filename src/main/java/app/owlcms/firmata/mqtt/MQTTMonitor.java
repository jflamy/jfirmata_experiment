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

import app.owlcms.firmata.Main;
import app.owlcms.firmata.board.Board;
import app.owlcms.firmata.eventhandlers.OutputEventHandler;
import app.owlcms.firmata.utils.Config;
import app.owlcms.firmata.utils.LoggerUtils;
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
public class MQTTMonitor {

	private static final String OWLCMS_FOP = "owlcms/fop/#";
	MqttAsyncClient client;
	private String fopName;
	static Logger logger = (Logger) LoggerFactory.getLogger(MQTTMonitor.class);
	private String password;

	private String userName;
	private MQTTCallback callback;
	private OutputEventHandler emitDefinitionHandler;
	private Board board;

	public MQTTMonitor(String fopName, OutputEventHandler emitDefinitionHandler, Board board) {
		logger.setLevel(Level.DEBUG);
		this.setFopName(fopName);
		this.board = board;
		this.emitDefinitionHandler = emitDefinitionHandler;
		try {
			if (Config.getCurrent().getMqttServer() != null) {
				client = createMQTTClient(fopName);
				connectionLoop(client);
			} else {
				logger.info("no MQTT server configured, skipping");
			}
		} catch (MqttException e) {
			logger.error("cannot initialize MQTT: {}", LoggerUtils.stackTrace(e));
		}
	}

	public static MqttAsyncClient createMQTTClient(String fopName) throws MqttException {
		String server = Config.getCurrent().getMqttServer();
		server = (server != null ? server : "127.0.0.1");
		String port = Config.getCurrent().getMqttPort();
		port = (port != null ? port : "1883");
		String string = port.startsWith("8") ? "ssl://" : "tcp://";
		Main.getStartupLogger().info("connecting to MQTT {}{}:{}", string, server, port);

		MqttAsyncClient client = new MqttAsyncClient(string + server + ":" + port,
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

	void connectionLoop(MqttAsyncClient mqttAsyncClient) {
		while (!mqttAsyncClient.isConnected()) {
			try {
				// doConnect will generate a new client Id, and wait for completion
				// client.reconnect() and automaticReconnection do not work as I expect.
				doConnect();
			} catch (Exception e1) {
				logger.error("{}MQTT refereeing device server error: {}", getFopName(),
						e1.getCause() != null ? e1.getCause().getMessage() : e1);
			}
			sleep(1000);
		}
	}

	private void doConnect() throws MqttSecurityException, MqttException {
			userName = Config.getCurrent().getMqttUsername();
			password = Config.getCurrent().getMqttPassword();
		MqttConnectOptions connOpts = setupMQTTClient(userName, password);
		client.connect(connOpts).waitForCompletion();

		client.subscribe(OWLCMS_FOP, 0);
		logger.info("Platform {} MQTT subscribed to {} {}", getFopName(), OWLCMS_FOP,
				client.getCurrentServerURI());
	}

	public void publishMqttMessage(String topic, String message) throws MqttException, MqttPersistenceException {
		// logger.debug("{}MQTT LedOnOff", getFopName());
		topic = topic + "/" + getFopName();
		client.publish(topic, new MqttMessage(message.getBytes(StandardCharsets.UTF_8)));
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
		callback = new MQTTCallback(this, emitDefinitionHandler, board);
		client.setCallback(callback);
		return connOpts;
	}

	private void sleep(int ms) {
		try {
			Thread.sleep(ms);
		} catch (InterruptedException e) {
		}
	}

}
