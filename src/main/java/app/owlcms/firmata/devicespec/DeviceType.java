package app.owlcms.firmata.devicespec;

public enum DeviceType {
    Referees("Referees"),
    Timekeeper("Timekeeper"),
    Jury("Jury");

    public final String configName;

    private DeviceType(String configName) {
        this.configName = configName;
    }
}
