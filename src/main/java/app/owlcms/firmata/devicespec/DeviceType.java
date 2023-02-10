package app.owlcms.firmata.devicespec;

public enum DeviceType {
    Referees("Referees", true),
    Timekeeper("Timekeeper", true),
    Jury("JuryFull", true),
    JuryButtons("JuryButtons", false);

    public final String configName;
	public boolean isBlueOwl;

    private DeviceType(String configName, boolean isBlueOwl) {
        this.configName = configName;
        this.isBlueOwl = isBlueOwl;
    }
}
