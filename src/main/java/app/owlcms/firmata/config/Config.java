package app.owlcms.firmata.config;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.LoggerFactory;

import app.owlcms.firmata.mqtt.ConfigMQTTMonitor;
import app.owlcms.firmata.refdevice.DeviceConfig;
import ch.qos.logback.classic.Logger;

public class Config {
	static private Config current = null;
	static private List<DeviceConfig> devices = new ArrayList<>();
	public static Config getCurrent() {
		if (current == null) {
			current = new Config();
		}
		return current;
	}
	
	protected static Logger logger = (Logger) LoggerFactory.getLogger(Config.class);
	
	private String fop;
	private List<String> fops;
	private String mqttPassword;
	private String mqttPort;
	private String mqttServer;
	private String mqttUsername;
	private ConfigMQTTMonitor configMqttMonitor;

	private Config() {
		this.mqttServer = "192.168.1.175";
		this.mqttPort = "1883";
		this.mqttUsername = "";
		this.mqttPassword = "";
		this.fops = new ArrayList<>();
	}


	public String getFop() {
		return this.fop;
	}

	public List<String> getFops() {
		return this.fops;
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
		return "";
	}

	public String getMqttServer() {
		if (mqttServer != null) {
			return mqttServer;
		}
		return "";
	}

	public String getMqttUsername() {
		if (mqttUsername != null) {
			return mqttUsername;
		}
		return "";
	}

	public void register(DeviceConfig mm) {
		devices.add(mm);
	}

	public void setFop(String platform) {
		this.fop = platform;
	}

	public void setFops(List<String> fops) {
		this.fops = fops;
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


	public boolean isConnected() {
		return getConfigMqttMonitor().isConnected();
	}


	public ConfigMQTTMonitor getConfigMqttMonitor() {
		return configMqttMonitor;
	}
	
	public void closeAll() {
		for (DeviceConfig mm : devices) {
			mm.getFirmataService().stopDevice(()->{});
		}
		devices.clear();
	}


	public void setConfigMqttMonitor(ConfigMQTTMonitor configMqttMonitor) {
		this.configMqttMonitor = configMqttMonitor;
	}


	public static boolean fullyConnected() {
		logger.warn("connected = {} fop = {}", getCurrent().isConnected(), getCurrent().getFop());
		return getCurrent().isConnected() && (getCurrent().getFop() != null);
	}

}