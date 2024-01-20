package app.owlcms.firmata.refdevice;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.stream.Stream;

import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Logger;

public class DeviceConfig {
	Logger logger = (Logger) LoggerFactory.getLogger(DeviceConfig.class);
	private InputStream deviceInputStream;
	private String deviceTypeName;
	private String platform;
	private String serialPort;

	public DeviceConfig(String serialPort, String deviceTypeName) {
		this.setDeviceTypeName(deviceTypeName);
		this.serialPort = serialPort;
		try {
			this.findDeviceInputStream();
		} catch (Exception e) {
			// leave as null.
		}
	}

	public InputStream getDeviceInputStream() {
		if (deviceInputStream == null) {
			deviceInputStream = findDeviceInputStream();
			if (deviceInputStream == null) {
				throw new RuntimeException(new FileNotFoundException(getDeviceConfigFileName()));			
			}
		}
		return deviceInputStream;
	}

	public String getDeviceTypeName() {
		return deviceTypeName;
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

	public void setDevice(String configName) {
		// logger.info("setting device to {} {}", configName , LoggerUtils.stackTrace() );
		this.setDeviceTypeName(configName);
	}

	public void setDeviceInputStream(InputStream deviceInputStream) {
		this.deviceInputStream = deviceInputStream;
	}

	public void setDeviceTypeName(String device) {
		this.deviceTypeName = device;
	}

	public void setPlatform(String platform) {
		this.platform = platform;
	}

	public void setSerialPort(String serialPort) {
		this.serialPort = serialPort;
	}

	private InputStream findDeviceInputStream() {

		InputStream resourceAsStream;
		resourceAsStream = searchFile(".", getDeviceConfigFileName());
		if (resourceAsStream != null) {
			return resourceAsStream;
		}

		String homedir = System.getProperty("user.home");
		resourceAsStream = searchFile(homedir + "/.owlcms/devices", getDeviceConfigFileName());
		if (resourceAsStream != null) {
			return resourceAsStream;
		}

		resourceAsStream = searchFile("./app/devices", getDeviceConfigFileName());
		if (resourceAsStream != null) {
			return resourceAsStream;
		}

		resourceAsStream = searchFile("./dist", getDeviceConfigFileName());
		if (resourceAsStream != null) {
			return resourceAsStream;
		}
		
		return null;
	}

	private String getDeviceConfigFileName() {
		return getDeviceTypeName()+".xlsx";
	}

	private InputStream searchFile(String dirPath, String fileName) {
		// Use Files.walk to get a stream of paths from the directory
		try (Stream<Path> walkStream = Files.walk(Paths.get(dirPath))) {
			// Filter the stream by checking if the file name matches
			Optional<Path> hit = walkStream
			        .filter(p -> p.toFile().isFile() && p.getFileName().toString().equals(fileName)).findAny();
			// If a match is found, return an InputStream from the path
			if (hit.isPresent()) {
				return Files.newInputStream(hit.get());
			}
		} catch (Exception e) {
		}
		// If no match is found, return null
		return null;
	}
}
