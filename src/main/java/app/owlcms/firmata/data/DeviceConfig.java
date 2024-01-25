package app.owlcms.firmata.data;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.stream.Stream;

import org.slf4j.LoggerFactory;

import app.owlcms.firmata.ui.FirmataService;
import app.owlcms.utils.ResourceWalker;
import ch.qos.logback.classic.Logger;

public class DeviceConfig {
	Logger logger = (Logger) LoggerFactory.getLogger(DeviceConfig.class);
	private String deviceTypeName;
	private String serialPort;
	private FirmataService firmataService;
	
	public DeviceConfig(String serialPort, String deviceTypeName) {
		this.setDeviceTypeName(deviceTypeName);
		this.serialPort = serialPort;
	}

	public InputStream getDeviceInputStream() {
		InputStream deviceInputStream ;
		deviceInputStream = this.searchFile(ResourceWalker.getLocalDirPath(), getDeviceConfigFileName());
		return deviceInputStream;
	}

	public String getDeviceTypeName() {
		return deviceTypeName;
	}

	public String getSerialPort() {
		if (serialPort != null) {
			return serialPort;
		}

		return null;
	}

	public void setDevice(String configName) {
		if (configName.endsWith(".xlsx")) {
			configName = configName.replace(".xlsx","");
		}
		logger.info("setting device to {}", configName);
		this.setDeviceTypeName(configName);
	}

	public void setDeviceTypeName(String device) {
		this.deviceTypeName = device;
	}

	public void setSerialPort(String serialPort) {
		this.serialPort = serialPort;
	}


	private String getDeviceConfigFileName() {
		return getDeviceTypeName()+".xlsx";
	}

	private InputStream searchFile(Path dirPath, String fileName) {
		// Use Files.walk to get a stream of paths from the directory
		try (Stream<Path> walkStream = Files.walk(dirPath)) {
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

	public FirmataService getFirmataService() {
		return firmataService;
	}

	public void setFirmataService(FirmataService firmataService) {
		this.firmataService = firmataService;
	}
}
