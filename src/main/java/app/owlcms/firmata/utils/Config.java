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

	public String getMqttServer() {
		var p = System.getenv("BLUE_OWL_MQTT_SERVER");
		if (p != null) {
			return p;
		}

		p = System.getProperty("blueOwlMqttServer");
		if (p != null) {
			return p;
		}

		return "127.0.0.1";
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

		return "1883";
	}

	public String getMqttUserName() {
		var p = System.getenv("BLUE_OWL_MQTT_USERNAME");
		if (p != null) {
			return p;
		}

		p = System.getProperty("blueOwlMqttUsername");
		if (p != null) {
			return p;
		}

		return "";
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

		return "CNCA0";
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

		return "Referees";
	}

	public InputStream getDeviceConfig(String[] ignored) {
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

}
