package app.owlcms.firmata.ui;

import java.io.InputStream;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.formlayout.FormLayout.ResponsiveStep;
import com.vaadin.flow.component.formlayout.FormLayout.ResponsiveStep.LabelsPosition;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.radiobutton.RadioButtonGroup;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.receivers.MemoryBuffer;
import com.vaadin.flow.router.Route;

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

		var title = new H2("owlcms Refereeing Device Controller");
		title.getStyle().set("margin-top", "0");
		
		var deviceSelectionTitle = new H4("Device");
		deviceSelectionTitle.getStyle().set("margin-top", "0");
		RadioButtonGroup<DeviceType> rbg = new RadioButtonGroup<>();
		rbg.setItems(DeviceType.values());

		Upload upload = new Upload(memoryBuffer);
		upload.addFinishedListener(e -> {
			InputStream inputStream = memoryBuffer.getInputStream();
			// read the contents of the buffered memory
			// from inputStream
		});

		var mqttConfigTitle = new H4("MQTT Server Configuration");

		// Use TextField for standard text input
		TextField mqttServerField = new TextField();
		mqttServerField.addThemeName("bordered");
		TextField mqttPortField = new TextField();
		mqttPortField.addThemeName("bordered");
		TextField mqttUsernameField = new TextField();
		PasswordField mqttPasswordField = new PasswordField();

		// Button click listeners can be defined as lambda expressions
		FirmataService firmataService = new FirmataService();
		Button button = new Button("Start Device",
				e -> Notification.show(firmataService.greet(mqttServerField.getValue())));

		// Theme variants give you predefined extra styles for components.
		// Example: Primary button is more prominent look.
		button.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

		// You can specify keyboard shortcuts for buttons.
		// Example: Pressing enter in this view clicks the Button.
		button.addClickShortcut(Key.ENTER);

		// Use custom CSS classes to apply styling. This is defined in
		// shared-styles.css.
		// addClassName("centered-content");

		add(title);
		
		form.add(deviceSelectionTitle);
		addFormItemX(rbg, "Device Type");
		addFormItemX(upload, "Custom Configuration");
		form.add(mqttConfigTitle);
		addFormItemX(mqttServerField, "MQTT Server");
		addFormItemX(mqttPortField, "MQTT Port");
		addFormItemX(mqttUsernameField, "MQTT Username");
		addFormItemX(mqttPasswordField, "MQTT Password");
		form.setResponsiveSteps(new ResponsiveStep("0px", 1, LabelsPosition.ASIDE));
		this.add(form);
		
		add(button);
	}

	private void addFormItemX(Component c, String string) {
		var item = form.addFormItem(c, string);
		item.getElement().getStyle().set("--vaadin-form-item-label-width", "15em");

	}
}
