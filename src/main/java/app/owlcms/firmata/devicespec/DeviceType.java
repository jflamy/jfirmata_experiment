package app.owlcms.firmata.devicespec;

public enum DeviceType {
	Timekeeper("Timekeeper", true),
	Referees("Referees", true),
	RefereesDown("RefereesDown", false),
	SoloReferee("RefereeSolo", true),
	JuryButtons("JuryButtons", false),
	Jury("JuryFull", true);

	public final String configName;
	public boolean isBlueOwl;

	private DeviceType(String configName, boolean isBlueOwl) {
		this.configName = configName;
		this.isBlueOwl = isBlueOwl;
	}
}
