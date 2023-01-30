package app.owlcms.firmata.utils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import app.owlcms.firmata.Main;

public class Config {

	private static Config currentConfig = null;

	public static Config getCurrent() {
		if (currentConfig == null) {
			currentConfig = new Config();
		}
		return currentConfig;
	}

	private InputStream configStream;

	private String device;

	private String mqttPassword;

	private String mqttPort;

	private String mqttServer;

	private String mqttUsername;

	private String serialPort;
	
	private String platform;
	
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
	
	public void setPlatform(String platform) {
		this.platform = platform;
	}

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

		return "Referees";
	}

	public InputStream getDeviceConfig() {
		if (configStream != null) {
			return configStream;
		}
		var deviceName = getDevice();
		try {
			Path path;
			boolean found;

			path = Paths.get(deviceName);
			found = Files.exists(path);
			if (found) {
				return Files.newInputStream(path);
			}
			path = Paths.get(path + ".xlsx");
			found = Files.exists(path);
			if (found) {
				return Files.newInputStream(path);
			}

			InputStream resourceAsStream = Main.class.getResourceAsStream("/devices/" + deviceName + ".xlsx");
			if (resourceAsStream != null) {
				return resourceAsStream;
			}
		} catch (IOException e) {
		}
		throw new RuntimeException("File not found " + deviceName);
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

		return "CNCA0";
	}

	public void setConfigStream(InputStream inputStream) {
		this.configStream = inputStream;
	}

	public void setDevice(String device) {
		this.device = device;
	}

	public void setMqttPassword(String mqttPassword) {
		this.mqttPassword = mqttPassword;
	}

	public void setMqttPort(String mqttPort) {
		this.mqttPort = mqttPort;
	}

	public void setMqttServer(String value) {
		// TODO Auto-generated method stub

	}

	public void setMqttUsername(String mqttUsername) {
		this.mqttUsername = mqttUsername;
	}

	public Object setMqttUserName(String value) {
		// TODO Auto-generated method stub
		return null;
	}

	public void setSerialPort(String serialPort) {
		this.serialPort = serialPort;
	}

}
