package app.owlcms.firmata.board;

public interface Interruptible {

	void setInterrupted(boolean interrupted);

	boolean isInterrupted();

}