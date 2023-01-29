package app.owlcms.firmata.ui;

public enum DeviceType {
    Referees("Referees"),
    Timekeeper("Timekeeper"),
    Jury("Jury");

    public final String configName;

    private DeviceType(String configName) {
        this.configName = configName;
    }
}
