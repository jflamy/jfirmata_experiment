package app.owlcms.firmata.mqtt;

import org.eclipse.paho.client.mqttv3.MqttAsyncClient;

import app.owlcms.firmata.utils.MQTTServerConfig;

public interface MQTTMonitor {
	
	public boolean connectionLoop(MqttAsyncClient mqttAsyncClient, int max);
	
	public void close();

	public String getName();
	
	public default void register() {
		MQTTServerConfig.getCurrent().register(this);
	}

}
