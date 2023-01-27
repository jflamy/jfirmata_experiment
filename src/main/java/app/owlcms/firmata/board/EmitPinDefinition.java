package app.owlcms.firmata.board;

public class EmitPinDefinition extends PinDefinition {
	
	@Override
	public String toString() {
		return "EmitPinDefinition [pin=" + pin + ", description=" + description + ", topic=" + topic + ", message="
				+ message + ", action=" + action + ", parameters=" + parameters + "]";
	}
	
	public String description;
	public String topic;
	public String message;
	public String action;
	public String parameters;
}
