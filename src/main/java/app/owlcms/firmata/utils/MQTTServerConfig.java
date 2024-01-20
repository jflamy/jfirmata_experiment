package app.owlcms.firmata.utils;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Logger;

public class MQTTServerConfig {
	private String mqttPassword;
	private String mqttPort;
	private String mqttServer;
	private String mqttUsername;
	protected Logger logger = (Logger) LoggerFactory.getLogger(MQTTServerConfig.class);
	static private MQTTServerConfig current = null;

	private MQTTServerConfig() {
	}
	
	public static MQTTServerConfig getCurrent() {
		if (current == null) {
			current = new MQTTServerConfig();
		}
		return current;
	}

	public String getMqttPassword() {
		if (mqttPassword != null) {
			return mqttPassword;
		}
	
		return "";
	}

	public String getMqttPort() {
		if (mqttPort != null) {
			return mqttPort;
		}
	
		return "1883";
	}

	public String getMqttServer() {
		if (mqttServer != null) {
			return mqttServer;
		}
		return "127.0.0.1";
	}

	public String getMqttUsername() {
		if (mqttUsername != null) {
			return mqttUsername;
		}
	
		return "";
	}

	public void setMqttPassword(String mqttPassword) {
		this.mqttPassword = mqttPassword;
	}

	public void setMqttPort(String mqttPort) {
		this.mqttPort = mqttPort;
	}

	public void setMqttServer(String value) {
		this.mqttServer = value;
	}

	public void setMqttUsername(String mqttUsername) {
		this.mqttUsername = mqttUsername;
	}

	public List<String> getFOPs() {
		// FIXME: update the list when the config message is read
		ArrayList<String> fops = new ArrayList<>();
		fops.add("A");
		return fops;
		
	}

}