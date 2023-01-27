package app.owlcms.firmata;

/**
 * Output pins are triggered by MQTT messages.
 * 
 * @author 
 */
public class ButtonPinDefinition extends PinDefinition {
	
	@Override
	public String toString() {
		return "ButtonPinDefinition [pin=" + pin + ", topic=" + topic + ", message=" + message + "]";
	}
	
	public String topic;
	public String message;

}
