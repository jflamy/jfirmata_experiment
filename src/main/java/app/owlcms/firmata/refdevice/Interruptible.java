package app.owlcms.firmata.refdevice;

public interface Interruptible {

	void setInterrupted(boolean interrupted);

	boolean isInterrupted();

	String getWhereFrom();

}