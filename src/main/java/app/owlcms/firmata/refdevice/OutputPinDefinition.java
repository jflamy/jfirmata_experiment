package app.owlcms.firmata.refdevice;

public class OutputPinDefinition extends PinDefinition {
	
	@Override
	public String toString() {
		return "OutputPinDefinition [pin=" + pin + ", description=" + description + ", topic=" + topic + ", message="
				+ message + ", action=" + action + ", parameters=" + parameters + "]";
	}
	
	public String description;
	public String topic;
	public String message;
	public String action;
	public String parameters;
}
