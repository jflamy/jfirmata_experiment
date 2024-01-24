package app.owlcms.firmata.mqtt;

import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttSecurityException;
import org.slf4j.LoggerFactory;

import com.vaadin.flow.component.UI;

import app.owlcms.firmata.config.Config;
import app.owlcms.firmata.ui.MainView;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;

/**
 * This class receives and emits MQTT events.
 *
 * Events initiated by the devices start with topics that names the device
 * (owlcms/jurybox) Devices do not listen to other devices. Devices listen to MQTT
 * events that come from the field of play. These events are of the form
 * (owlcms/fop). The field of play is always the last element in the topic.
 *
 * @author Jean-François Lamy
 */
public class ConfigMQTTMonitor extends AbstractMQTTMonitor {

	static Logger logger = (Logger) LoggerFactory.getLogger(ConfigMQTTMonitor.class);
	private static final String OWLCMS_CONFIG = "owlcms/fop/config/#";
	private ConfigMQTTCallback callback;
	private String fopName;

	private String password;
	private UI ui;
	private String userName;
	private MainView view;

	public ConfigMQTTMonitor(MainView view, UI ui) {
		logger.setLevel(Level.DEBUG);
		this.view = view;
		this.ui = ui;
		Config.getCurrent().setConfigMqttMonitor(this);
	}

	@Override
	public void doConnect() throws MqttSecurityException, MqttException {
		userName = Config.getCurrent().getMqttUsername();
		password = Config.getCurrent().getMqttPassword();
		MqttConnectOptions connOpts = setupMQTTClient(userName, password);
		client.connect(connOpts).waitForCompletion();
		client.subscribe(OWLCMS_CONFIG, 0);
		logger.info("Platform {} MQTT subscribed to {} {}", getName(), OWLCMS_CONFIG,
		        client.getCurrentServerURI());
	}

	@Override
	public String getName() {
		return "config";
	}

	public boolean isConnected() {
		return client!= null && client.isConnected();
	}

	public MqttConnectOptions setupMQTTClient(String userName, String password) {
		MqttConnectOptions connOpts = setUpConnectionOptions(userName != null ? userName : "",
				password != null ? password : "");
		callback = new ConfigMQTTCallback(this, view);
		client.setCallback(callback);
		return connOpts;
	}

	public void start() {
		try {
			String mqttServer = Config.getCurrent().getMqttServer();
			if (mqttServer != null && !mqttServer.isBlank()) {
				client = createMQTTClient(fopName);
				connectionLoop(client);
			} else {
				logger.info("no MQTT server configured, skipping");
			}
		} catch (MqttException e) {
			logger.error("cannot initialize MQTT: {}", e);
		}
	}
	
	public void stop() {
		try {
			client.disconnect();
			client.close();
		} catch (MqttException e) {
			logger.error("cannot close: {}", e);
		}
	}


	public void updatePlatforms() {
		view.doPlatformsUpdate(ui);
	}

}
