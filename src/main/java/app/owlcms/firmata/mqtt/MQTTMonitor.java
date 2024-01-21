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

import app.owlcms.firmata.config.Config;
import app.owlcms.firmata.ui.Main;
import ch.qos.logback.classic.Logger;

public abstract class MQTTMonitor {

	protected MqttAsyncClient client;
	protected String password;
	protected String userName;
	private boolean closed;
	private Logger logger = (Logger) LoggerFactory.getLogger(MQTTMonitor.class);

	public boolean connectionLoop(MqttAsyncClient mqttAsyncClient) {
		int i = 0;
		setClosed(false);
		while (!mqttAsyncClient.isConnected() && !isClosed()) {
			try {
				// doConnect will generate a new client Id, and wait for completion
				doConnect();
			} catch (Exception e1) {
				if (i == 0) {
					logger.error("{}MQTT connection error: {}",
					        e1.getCause() != null ? e1.getCause().getMessage() : e1);
				}
			}
			sleep(1000);
			i++;
		}
		return false;
	}
	
	private synchronized boolean isClosed() {
		return this.closed;
	}

	public synchronized void setClosed(boolean b) {
		this.closed = b;
	}

	public MqttAsyncClient createMQTTClient(String fopName) throws MqttException {
		String server = Config.getCurrent().getMqttServer();
		server = (server != null ? server : "127.0.0.1");
		String port = Config.getCurrent().getMqttPort();
		port = (port != null ? port : "1883");
		String protocol = port.startsWith("8") ? "ssl://" : "tcp://";
		Main.getStartupLogger().info("connecting to MQTT {}{}:{}", protocol, server, port);

		client = new MqttAsyncClient(protocol + server + ":" + port,
		        fopName + "_" + MqttClient.generateClientId(), // ClientId
		        new MemoryPersistence()); // Persistence
		return client;
	}

	public abstract String getName();

	public void publishMqttMessage(String topic, String message) throws MqttException, MqttPersistenceException {
		MqttMessage message2 = new MqttMessage(message.getBytes(StandardCharsets.UTF_8));
		client.publish(topic, message2);
	}

	public void register() {
		// TODO Auto-generated method stub
	}

	protected MqttConnectOptions setUpConnectionOptions(String username, String password) {
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

	abstract void doConnect() throws MqttSecurityException, MqttException;

	private void sleep(int ms) {
		try {
			Thread.sleep(ms);
		} catch (InterruptedException e) {
		}
	}

}
