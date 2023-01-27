package app.owlcms.firmata;

import java.io.InputStream;

import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.firmata4j.IODevice;
import org.firmata4j.firmata.FirmataDevice;
import org.firmata4j.transport.JSerialCommTransport;
import org.slf4j.LoggerFactory;

import app.owlcms.firmata.board.Board;
import app.owlcms.firmata.board.DeviceEventListener;
import app.owlcms.firmata.devicespec.DeviceSpecReader;
import ch.qos.logback.classic.Logger;

public class Main {

	static final Logger logger = (Logger) LoggerFactory.getLogger(Main.class);

	private static void firmataThread(String myPort, InputStream is) {
		try {
			IODevice device = new FirmataDevice(new JSerialCommTransport(myPort));
			XSSFWorkbook workbook = new XSSFWorkbook(is);
			var dsr = new DeviceSpecReader();
			dsr.readPinDefinitions(workbook);
			var emitPinDefinitions = dsr.getEmitPinDefinitions();
			var buttonPinDefinitions = dsr.getButtonPinDefinitions();
			
			var board = new Board();
			board.initBoard(device);
			board.initModes(device, emitPinDefinitions, buttonPinDefinitions);
			board.showPinConfig(device);
			board.startupLED(device);

			device.addEventListener(new DeviceEventListener(board));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		String myPort = "CNCA0"; // modify for your own computer & setup.
		InputStream is = Main.class.getResourceAsStream("/Referee.xlsx");
		
		Thread t1 = new Thread(() -> firmataThread(myPort, is));
		waitForever(t1);

	}

	private static void waitForever(Thread t1) {
		t1.start();
		try {
			Thread.sleep(Long.MAX_VALUE);
			t1.join();
		} catch (InterruptedException e) {
			logger.warn("Thread interrupted.");
		}
	}

	public static Logger getStartupLogger() {
		return logger;
	}



}
