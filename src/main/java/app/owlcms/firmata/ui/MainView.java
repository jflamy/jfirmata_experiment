package app.owlcms.firmata.ui;

import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

import com.fazecast.jSerialComm.SerialPort;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.formlayout.FormLayout.ResponsiveStep;
import com.vaadin.flow.component.formlayout.FormLayout.ResponsiveStep.LabelsPosition;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.radiobutton.RadioButtonGroup;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.receivers.MemoryBuffer;
import com.vaadin.flow.router.Route;

import app.owlcms.firmata.utils.Config;

/**
 * The main view contains a button and a click listener.
 */
@Route("")
public class MainView extends VerticalLayout {
	MemoryBuffer memoryBuffer = new MemoryBuffer();
	FormLayout form = new FormLayout();

	public MainView() {
		setWidth("60%");
		setMargin(true);
		setPadding(true);
		form.setResponsiveSteps(new ResponsiveStep("0px", 1, LabelsPosition.ASIDE));
		
		var title = new H2("owlcms Firmata Refereeing Device Controller");
		title.getStyle().set("margin-top", "0");
		add(title);
		
		var deviceSelectionTitle = new H4("Device Selection");
		deviceSelectionTitle.getStyle().set("margin-top", "0");
		RadioButtonGroup<DeviceType> deviceSelector = new RadioButtonGroup<>();
		deviceSelector.setItems(DeviceType.values());
		deviceSelector.addValueChangeListener(e -> Config.getCurrent().setSerialPort(e.getValue().configName));
		
		Upload upload = new Upload(memoryBuffer);
		upload.addFinishedListener(e -> {
			InputStream inputStream = memoryBuffer.getInputStream();
			Config.getCurrent().setConfigStream(inputStream);
		});
		form.add(deviceSelectionTitle);
		addFormItemX(deviceSelector, "Standard Blue-Owl Device");
		addFormItemX(upload, "Custom Configuration");
		
		TextField platformField = new TextField();
		platformField.setValue(Config.getCurrent().getPlatform());
		platformField.addValueChangeListener(e -> Config.getCurrent().setPlatform(e.getValue()));
		addFormItemX(platformField, "Platform");
		
		var serialPortTitle = new H4("Serial Port Selection");
		form.add(serialPortTitle);
		ComboBox<SerialPort> serial = new ComboBox<>();
		serial.setItems(getSerialPorts());
		serial.setValue(null);
		serial.setPlaceholder("Automatic Detection");
		addFormItemX(serial, "Serial Port");
		serial.addThemeName("bordered");
		serial.addValueChangeListener(e -> Config.getCurrent().setSerialPort(e.getValue().getSystemPortName()));
		
		var mqttConfigTitle = new H4("MQTT Server Configuration");
		
		TextField mqttServerField = new TextField();
		mqttServerField.setHelperText("This is normally the address of the owlcms server");
		mqttServerField.setValue(Config.getCurrent().getMqttServer());
		mqttServerField.addValueChangeListener(e -> Config.getCurrent().setMqttServer(e.getValue()));
		
		TextField mqttPortField = new TextField();
		mqttPortField.setValue(Config.getCurrent().getMqttPort());
		mqttPortField.addValueChangeListener(e -> Config.getCurrent().setMqttPort(e.getValue()));

		TextField mqttUsernameField = new TextField();
		mqttUsernameField.setValue(Config.getCurrent().getMqttUsername());
		mqttUsernameField.addValueChangeListener(e -> Config.getCurrent().setMqttUserName(e.getValue()));
		
		PasswordField mqttPasswordField = new PasswordField();
		mqttPasswordField.setValue(Config.getCurrent().getMqttPassword());
		mqttPasswordField.addValueChangeListener(e -> Config.getCurrent().setMqttPassword(e.getValue()));
		
		form.add(mqttConfigTitle);
		addFormItemX(mqttServerField, "MQTT Server");
		addFormItemX(mqttPortField, "MQTT Port");
		addFormItemX(mqttUsernameField, "MQTT Username");
		addFormItemX(mqttPasswordField, "MQTT Password");

		this.add(form);
		
		Button button = new Button("Start Device",
				e -> {
					new FirmataService().startDevice();
				});
		button.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
		button.addClickShortcut(Key.ENTER);	
		add(button);
		
		// Use custom CSS classes to apply styling. This is defined in
		// shared-styles.css.
		// addClassName("centered-content");

	}

	private void addFormItemX(Component c, String string) {
		var item = form.addFormItem(c, string);
		item.getElement().getStyle().set("--vaadin-form-item-label-width", "15em");

	}
	
	private List<SerialPort> getSerialPorts() {
		SerialPort[] ports = SerialPort.getCommPorts();
		return Arrays.asList(ports) ;
		
	}
}
