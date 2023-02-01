package app.owlcms.firmata.devicespec;

public class PinDefinition {
	public String pin;
	
	public int getPinNumber() {
		Integer val;
		try {
			val = Integer.valueOf(pin);
		} catch (NumberFormatException e) {
			val = 0;
		}
		return val;
	}

}
