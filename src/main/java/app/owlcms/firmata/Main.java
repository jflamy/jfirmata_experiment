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
import app.owlcms.firmata.mqtt.MQTTMonitor;
import app.owlcms.firmata.utils.Config;
import ch.qos.logback.classic.Logger;

public class Main {

	static final Logger logger = (Logger) LoggerFactory.getLogger(Main.class);

	private static void firmataThread(String fopName, String myPort, InputStream is) {
		try {
			// read configurations
			XSSFWorkbook workbook = new XSSFWorkbook(is);
			var dsr = new DeviceSpecReader();
			dsr.readPinDefinitions(workbook);
			var outputPinDefinitions = dsr.getOutputPinDefinitions();
			var buttonPinDefinitions = dsr.getButtonPinDefinitions();
			
			// create the Firmata device and its wrapper
			IODevice device = new FirmataDevice(new JSerialCommTransport(myPort));
			var board = new Board(myPort, device, outputPinDefinitions, buttonPinDefinitions);
			MQTTMonitor mqtt = new MQTTMonitor(fopName, outputPinDefinitions, board);
			device.addEventListener(new DeviceEventListener(
					board,
					buttonPinDefinitions,
					mqtt));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		String myPort = Config.getCurrent().getParamSerialPort(); // modify for your own computer & setup.
		InputStream is = Main.class.getResourceAsStream("/Referee.xlsx");
		
		Thread t1 = new Thread(() -> firmataThread("A", myPort, is));
		waitForever(t1);

	}

	private static void waitForever(Thread t1) {
		t1.start();
		try {
			Thread.sleep(Long.MAX_VALUE);
			t1.join();
		} catch (InterruptedException e) {
			logger./**/warn("Thread interrupted.");
		}
	}

	public static Logger getStartupLogger() {
		return logger;
	}



}
