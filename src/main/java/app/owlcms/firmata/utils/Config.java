package app.owlcms.firmata.utils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;

public class Config {

	private static Config currentConfig = null;

	public static Config getCurrent() {
		if (currentConfig == null) {
			currentConfig = new Config();
		}
		currentConfig.logger.setLevel(Level.DEBUG);
		return currentConfig;
	}

	private String device;

	private String mqttPassword;

	private String mqttPort;

	private String mqttServer;

	private String mqttUsername;

	private String serialPort;

	private String platform;

	private Logger logger = (Logger) LoggerFactory.getLogger(Config.class);

	private String deviceDir;

	public String getDevice() {
		var p = System.getenv("BLUE_OWL_DEVICE");
		if (p != null) {
			return p;
		}

		p = System.getProperty("blueOwlDevice");
		if (p != null) {
			return p;
		}

		if (device != null) {
			return device;
		}

		return null;
	}

	public InputStream getDeviceInputStream() {

		InputStream resourceAsStream;
		var deviceName = getDevice();
		if ("custom".equals(getDeviceDir()) || getDeviceDir() == null || getDeviceDir().isBlank()) {
			resourceAsStream = getFromFile(deviceName);
			if (resourceAsStream != null) {
				return resourceAsStream;
			}
		}

		String location = "/devices/" + getDeviceDir() + "/" + deviceName + ".xlsx";
		resourceAsStream = Config.class.getResourceAsStream(location);
		if (resourceAsStream != null) {
			logger.info("reading configuration from distribution file {}", location);
			return resourceAsStream;
		}
		throw new RuntimeException("File not found " + "/devices/" + getDeviceDir() + "/" + deviceName);
	}

	public String getMqttPassword() {
		var p = System.getenv("BLUE_OWL_MQTT_PASSWORD");
		if (p != null) {
			return p;
		}

		p = System.getProperty("blueOwlMqttPassword");
		if (p != null) {
			return p;
		}

		if (mqttPassword != null) {
			return mqttPassword;
		}

		return "";
	}

	public String getMqttPort() {
		var p = System.getenv("BLUE_OWL_MQTT_PORT");
		if (p != null) {
			return p;
		}

		p = System.getProperty("blueOwlMqttPort");
		if (p != null) {
			return p;
		}

		if (mqttPort != null) {
			return mqttPort;
		}

		return "1883";
	}

	public String getMqttServer() {
		var p = System.getenv("BLUE_OWL_MQTT_SERVER");
		if (p != null) {
			return p;
		}

		p = System.getProperty("blueOwlMqttServer");
		if (p != null) {
			return p;
		}

		if (mqttServer != null) {
			return mqttServer;
		}

		return "127.0.0.1";
	}

	public String getMqttUsername() {
		var p = System.getenv("BLUE_OWL_MQTT_USERNAME");
		if (p != null) {
			return p;
		}

		p = System.getProperty("blueOwlMqttUsername");
		if (p != null) {
			return p;
		}

		if (mqttUsername != null) {
			return mqttUsername;
		}

		return "";
	}

	public String getPlatform() {
		var p = System.getenv("BLUE_OWL_PLATFORM");
		if (p != null) {
			return p;
		}

		p = System.getProperty("blueOwlPlatform");
		if (p != null) {
			return p;
		}

		if (platform != null) {
			return platform;
		}

		return "A";
	}

	public String getSerialPort() {
		var p = System.getenv("BLUE_OWL_SERIAL_PORT");
		if (p != null) {
			return p;
		}

		p = System.getProperty("blueOwlSerialPort");
		if (p != null) {
			return p;
		}

		if (serialPort != null) {
			return serialPort;
		}

		return null;
	}

	public void setDevice(String dir, String configName) {
		logger.info("setting device {} {} from {}", dir, configName, LoggerUtils.whereFrom());
		this.setDeviceDir(dir);
		this.setDevice(configName);
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

	public void setPlatform(String platform) {
		this.platform = platform;
	}

	public void setSerialPort(String serialPort) {
		this.serialPort = serialPort;
	}

	private String getDeviceDir() {
		return deviceDir;
	}

	private InputStream getFromFile(String deviceName) {
		Path path = null;
		try {
			boolean found;
			path = Paths.get(deviceName + ".xlsx");
			found = Files.exists(path);
			if (found) {
				logger.info("Configuration found in {}", path.toAbsolutePath() );
				return Files.newInputStream(path);
			} else {
				logger./**/warn("Configuration not found in {}", path.toAbsolutePath() );
			}
		} catch (IOException e) {
			logger./**/warn("Cannot open {} {}", path != null ? path.toAbsolutePath() : null, e.toString());
		}
		return null;
	}

	private void setDevice(String device) {
		this.device = device;
	}

	private void setDeviceDir(String deviceDir) {
		this.deviceDir = deviceDir;
	}

}
