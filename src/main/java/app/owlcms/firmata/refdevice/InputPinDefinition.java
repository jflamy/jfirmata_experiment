package app.owlcms.firmata.refdevice;

/**
 * Output pins are triggered by MQTT messages.
 * 
 * @author 
 */
public class InputPinDefinition extends PinDefinition {
	
	@Override
	public String toString() {
		return "InputPinDefinition [pin=" + pin + ", topic=" + topic + ", message=" + message + "]";
	}
	
	public String topic;
	public String message;

}
