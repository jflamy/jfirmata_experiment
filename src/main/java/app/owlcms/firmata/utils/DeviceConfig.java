package app.owlcms.firmata.utils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Logger;

public class DeviceConfig {
	Logger logger = (Logger) LoggerFactory.getLogger(DeviceConfig.class);

	private String device;

	private String serialPort;

	private String platform;

	private String deviceDir;

	public String getDevice() {
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
		resourceAsStream = DeviceConfig.class.getResourceAsStream(location);
		if (resourceAsStream != null) {
			logger.info("reading default configuration from distribution: {}", location);
			return resourceAsStream;
		}
		throw new RuntimeException("Configuration not found " + "/devices/" + getDeviceDir() + "/" + deviceName);
	}

	public String getPlatform() {
		if (platform != null) {
			return platform;
		}

		return "A";
	}

	public String getSerialPort() {
		if (serialPort != null) {
			return serialPort;
		}

		return null;
	}

	public void setDevice(String dir, String configName) {
		//logger.info("setting device to {} {}", configName , LoggerUtils.stackTrace() );
		this.setDeviceDir(dir);
		this.setDevice(configName);
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
