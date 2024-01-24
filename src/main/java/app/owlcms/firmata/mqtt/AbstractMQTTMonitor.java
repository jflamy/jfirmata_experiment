package app.owlcms.firmata.mqtt;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
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
import app.owlcms.firmata.utils.LoggerUtils;
import ch.qos.logback.classic.Logger;

public abstract class AbstractMQTTMonitor {

	protected MqttAsyncClient client;
	protected String password;
	protected String userName;
	private boolean closed;
	private Logger logger = (Logger) LoggerFactory.getLogger(AbstractMQTTMonitor.class);

	public boolean connectionLoop(MqttAsyncClient mqttAsyncClient) {
		logger.warn("connection loop {}", LoggerUtils.stackTrace());
		int i = 0;
		setClosed(false);
		while (!mqttAsyncClient.isConnected() && !isClosed()) {
			try {
				// doConnect will generate a new client Id, and wait for completion
				doConnect();
			} catch (Exception e) {
				if (i == 0) {
					logger.error("{}", e.getMessage(),
					        e.getCause() != null ? e.getCause().getMessage() : e);
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

	public void quickCheckConnection() throws NumberFormatException, IOException {
		try (Socket socket = new Socket()) {
			int portNum = 0;
			try {
				portNum = Integer.parseInt(Config.getCurrent().getMqttPort());
			} catch (NumberFormatException e) {
				throw new NumberFormatException("Port Number must be a number: "+ Config.getCurrent().getMqttPort());
			}
			try {
				socket.connect(new InetSocketAddress(
						Config.getCurrent().getMqttServer(),
				        portNum), 
						2000);
			} catch (IOException e) {
				logger.error("{}",e.getLocalizedMessage());
				throw e;
			}
		}
	}

	public void close() {
		if (client == null) {
			return;
		}
		try {
			client.disconnect();
			client.close();
			setClosed(true);
		} catch (MqttException e) {
			logger.error("cannot close client {}", e);
			e.printStackTrace();
		}
	}

}
