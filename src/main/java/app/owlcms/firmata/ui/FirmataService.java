package app.owlcms.firmata.ui;

import java.io.InputStream;
import java.util.function.Consumer;

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
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;

public class FirmataService {
	
	static final Logger logger = (Logger) LoggerFactory.getLogger(FirmataService.class);
	private Runnable confirmationCallback;
	private Consumer<Throwable> errorCallback;
	private Board board;
	
	public FirmataService(Runnable confirmationCallback, Consumer<Throwable> errorCallback) {
		this.confirmationCallback = confirmationCallback;
		this.errorCallback = errorCallback;
		logger.setLevel(Level.DEBUG);
	}

	public void startDevice() {
		logger.info("starting");
		String serialPort = Config.getCurrent().getSerialPort(); // modify for your own computer & setup.
		InputStream is = Config.getCurrent().getDeviceConfig();
		String platform = Config.getCurrent().getPlatform();
		
		Thread t1 = new Thread(() -> firmataThread(platform, serialPort, is));
		t1.start();
	}

	private void firmataThread(String fopName, String serialPort, InputStream is) {
		try {
			// read configurations
			XSSFWorkbook workbook = new XSSFWorkbook(is);
			var dsr = new DeviceSpecReader();
			dsr.readPinDefinitions(workbook);
			var outputEventHandler = dsr.getOutputEventHandler();
			var inputEventHandler = dsr.getInputEventHandler();
			logger.info("Configuration read.");
			
			// create the Firmata device and its Board wrapper
			logger.debug("starting firmata device on port {}", serialPort);
			IODevice device = new FirmataDevice(new JSerialCommTransport(serialPort));
			logger.info("Device created on port {}", serialPort);
			
			System.err.println("before setting board "+board);
			Board board2 = new Board(serialPort, device, outputEventHandler, inputEventHandler);
			System.err.println("after creating board "+board);
			this.setBoard(board2);
			System.err.println("after setting board "+board);
			MQTTMonitor mqtt = new MQTTMonitor(fopName, outputEventHandler, getBoard());
			device.addEventListener(new DeviceEventListener(
					this.getBoard(),
					inputEventHandler,
					mqtt));
			confirmationCallback.run();
		} catch (Exception e) {
			errorCallback.accept(e);
		}
	}

	public void stopDevice() {
		getBoard().stop();
	}

	private Board getBoard() {
		return board;
	}

	private void setBoard(Board board) {
		this.board = board;
	}
}

