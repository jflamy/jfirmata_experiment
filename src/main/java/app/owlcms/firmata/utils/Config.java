package app.owlcms.firmata.utils;

public class Config {
	
	private static Config currentConfig = null;

	public static Config getCurrent() {
		if (currentConfig == null) {
			currentConfig = new Config();
		}
		return currentConfig;
	}

	public String getParamMqttServer() {
		// FIXME read env. variables + config file
		return "127.0.0.1";
	}

	public String getParamMqttPort() {
		return "1883";
	}

	public String getMqttUserName() {
		return "";
	}

	public String getParamMqttPassword() {
		return "";
	}

	public String getParamMqttUserName() {
		return "";
	}

	public String getParamSerialPort() {
		return "CNCA0";
	}

}
