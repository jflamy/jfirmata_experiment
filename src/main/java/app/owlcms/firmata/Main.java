package app.owlcms.firmata;

import java.io.IOException;
import java.io.InputStream;

import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.firmata4j.IODevice;
import org.firmata4j.IODeviceEventListener;
import org.firmata4j.IOEvent;
import org.firmata4j.Pin;
import org.firmata4j.Pin.Mode;
import org.firmata4j.firmata.FirmataDevice;
import org.firmata4j.transport.JSerialCommTransport;
import org.slf4j.LoggerFactory;

import app.owlcms.firmata.board.Board;
import app.owlcms.firmata.board.DeviceSpecReader;
import ch.qos.logback.classic.Logger;

public class Main {

	private static final Logger logger = (Logger) LoggerFactory.getLogger(Main.class);

	private static void firmataThread(IODevice device) {
		try {
			InputStream is = Main.class.getResourceAsStream("/Referee.xlsx");
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

			device.addEventListener(new IODeviceEventListener() {
				@Override
				public void onMessageReceive(IOEvent event, String message) {
					// here we react to receiving a text message from the device
					System.err.println(message);
				}

				@Override
				public void onPinChange(IOEvent event) {
					// here we react to changes of pins' state
					Pin pin = event.getPin();
					if (pin.getMode() == Mode.PULLUP || pin.getMode() == Mode.INPUT) {
						if (board.debounce(pin.getIndex(), pin.getValue())) {
							logger.warn("new press on pin {} {}", pin.getIndex(), System.currentTimeMillis());
						}
					} else {
						logger.warn("output on pin {} {}", pin.getIndex(), System.currentTimeMillis());
					}
				}

				@Override
				public void onStart(IOEvent event) {
				}

				@Override
				public void onStop(IOEvent event) {
				}
			});
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		String myPort = "CNCA0"; // modify for your own computer & setup.

		IODevice board = new FirmataDevice(new JSerialCommTransport(myPort));

		Thread t1 = new Thread(() -> firmataThread(board));
		waitForever(board, t1);

	}

	private static void waitForever(IODevice board, Thread t1) {
		t1.start();
		try {
			Thread.sleep(Long.MAX_VALUE);
			t1.join();
			board.stop();
			System.out.println("Board stopped.");
		} catch (InterruptedException e) {
			System.out.println("Thread interrupted.");
		} catch (IOException e) {
		}
	}

	public static Logger getStartupLogger() {
		return logger;
	}

}
