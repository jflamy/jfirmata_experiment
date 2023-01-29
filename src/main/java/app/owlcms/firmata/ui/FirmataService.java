package app.owlcms.firmata.ui;

import java.io.InputStream;

import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.firmata4j.IODevice;
import org.firmata4j.firmata.FirmataDevice;
import org.firmata4j.transport.JSerialCommTransport;
import org.slf4j.LoggerFactory;

import app.owlcms.firmata.Main;
import app.owlcms.firmata.board.Board;
import app.owlcms.firmata.board.DeviceEventListener;
import app.owlcms.firmata.devicespec.DeviceSpecReader;
import app.owlcms.firmata.mqtt.MQTTMonitor;
import app.owlcms.firmata.utils.Config;
import ch.qos.logback.classic.Logger;

public class FirmataService {
	
	static final Logger logger = (Logger) LoggerFactory.getLogger(Main.class);
	
	public void startDevice() {
		logger.info("starting");
		String serialPort = Config.getCurrent().getSerialPort(); // modify for your own computer & setup.
		InputStream is = Config.getCurrent().getDeviceConfig();
		
		Thread t1 = new Thread(() -> firmataThread("A", serialPort, is));
		waitForever(t1);

	}

	private static void firmataThread(String fopName, String serialPort, InputStream is) {
		try {
			// read configurations
			XSSFWorkbook workbook = new XSSFWorkbook(is);
			var dsr = new DeviceSpecReader();
			dsr.readPinDefinitions(workbook);
			var outputEventHandler = dsr.getOutputEventHandler();
			var inputEventHandler = dsr.getInputEventHandler();
			logger.info("configuration read");
			
			// create the Firmata device and its Board wrapper
			IODevice device = new FirmataDevice(new JSerialCommTransport(serialPort));
			var board = new Board(serialPort, device, outputEventHandler, inputEventHandler);
			MQTTMonitor mqtt = new MQTTMonitor(fopName, outputEventHandler, board);
			device.addEventListener(new DeviceEventListener(
					board,
					inputEventHandler,
					mqtt));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}


    public String greet(String name) {
        if (name == null || name.isEmpty()) {
            return "Hello anonymous user";
        } else {
            return "Hello " + name;
        }
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
}

