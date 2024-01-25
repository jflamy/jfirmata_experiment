package app.owlcms.firmata.mqtt;

import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.slf4j.LoggerFactory;

import com.vaadin.flow.component.UI;

import app.owlcms.firmata.data.Config;
import app.owlcms.firmata.ui.MainView;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;

/**
 * Deal with configuration events that concern all platforms
 *
 * @author Jean-François Lamy
 */
public class ConfigMQTTMonitor extends AbstractMQTTMonitor {

	static Logger logger = (Logger) LoggerFactory.getLogger(ConfigMQTTMonitor.class);
	private static final String OWLCMS_CONFIG = "owlcms/fop/config/#";

	private UI ui;
	private MainView view;

	public ConfigMQTTMonitor(MainView view, UI ui) {
		logger.setLevel(Level.DEBUG);
		this.view = view;
		this.ui = ui;
		this.setName("config");
		this.setSubscription(OWLCMS_CONFIG);
		Config.getCurrent().setConfigMqttMonitor(this);
	}

	@Override
	public MqttConnectOptions setupMQTTClient(String userName, String password) {
		MqttConnectOptions connOpts = setUpConnectionOptions(userName != null ? userName : "",
				password != null ? password : "");
		client.setCallback(new ConfigMQTTCallback(this, view));
		return connOpts;
	}

	public void updatePlatforms() {
		view.doPlatformsUpdate(ui);
	}

}
