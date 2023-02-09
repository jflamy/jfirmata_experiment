package app.owlcms.firmata.board;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

import org.firmata4j.IODevice;
import org.firmata4j.Pin;
import org.firmata4j.Pin.Mode;
import org.slf4j.LoggerFactory;

import app.owlcms.firmata.eventhandlers.InputEventHandler;
import app.owlcms.firmata.eventhandlers.OutputEventHandler;
import app.owlcms.firmata.piezo.Note;
import app.owlcms.firmata.piezo.Tone;
import ch.qos.logback.classic.Logger;

public class Board {

	// https://github.com/arduino/ArduinoCore-avr/blob/master/variants/mega/pins_arduino.h
	public static final int NB_MEGA_PINS = 69;


	private final Logger logger = (Logger) LoggerFactory.getLogger(Board.class);
	private IODevice device;
	private InputEventHandler inputEventHandler;
	private OutputEventHandler outputEventHandler;
	private String serialPortName;

	public Board(String myPort, IODevice device, OutputEventHandler outputEventHandler,
	        InputEventHandler inputEventHandler) {
		this.device = device;
		this.serialPortName = myPort;
		this.outputEventHandler = outputEventHandler;
		this.inputEventHandler = inputEventHandler;
	}

	public void init() throws Exception {
		try {
			initBoard();
			initModes();
			if (logger.isTraceEnabled()) {
				showPinConfig();
			}
		} catch (Exception ex) {
			logger.warn("board init exception {}", ex.getMessage());
//			System.err.println("1");
//			try {
//				System.err.println("2");
//				if (device != null) {
//					device.stop();
//				}
//				System.err.println("3");
//			} catch (IOException e1) {
//				System.err.println("4");
//			}
//			System.err.println("5");
			logger.warn("before throwable board init exception");
			Throwable cause = ex.getCause() != null ? ex.getCause() : ex;
			logger.warn("after throwable board init exception", ex.getMessage());
			throw new RuntimeException(cause);
		}

	}

	public void initBoard() throws Exception {
		try {
			device.start(); // start comms with board;
			logger.info("Communication started on port {}", serialPortName);
			device.ensureInitializationIsDone();
			logger.info("Board initialized.");
		} catch (Exception ex) {
			logger.error("Could not connect to board. " + ex);
			throw new RuntimeException(ex.getCause() != null ? ex.getCause() : ex);
		}
	}

	public void initModes() {
		outputEventHandler.getDefinitions().stream().forEach(i -> {
			try {
				logger.debug("output {}", i.getPinNumber());
				Pin pin = device.getPin(i.getPinNumber());
				pin.setMode(Mode.OUTPUT);
				pin.setValue(0L);
			} catch (Exception e) {
				logger.trace("exception setting outputs {} {}", i.getPinNumber(), e);
			}
		});
		inputEventHandler.getDefinitions().stream().forEach(i -> {
			try {
				logger.debug("button {}", i.getPinNumber());
				device.getPin(i.getPinNumber()).setMode(Mode.PULLUP);
			} catch (Exception e) {
				logger.trace("exception setting outputs {} {}", i.getPinNumber(), e);
			}
		});
	}

	public void showPinConfig() throws IOException {
		for (int i = 0; i < device.getPinsCount(); i++) {
			Pin pin = device.getPin(i);
			logger.debug("{} {}", i, pin.getMode());
		}
	}

	public void startupLED() {
		Pin myLED = device.getPin(13);
		new Timer().schedule(new TimerTask() {
			@Override
			public void run() {
				try {
					logger.debug("startup LED on");
					myLED.setValue(1);
				} catch (IllegalStateException | IOException e) {
				}
			}
		}, 2500);
		new Timer().schedule(new TimerTask() {
			@Override
			public void run() {
				try {
					logger.debug("startup LED off");
					myLED.setValue(0);
				} catch (IllegalStateException | IOException e) {
				}
			}
		}, 5000);
	}

	public Pin getPin(int pinNumber) {
		return device.getPin(pinNumber);
	}

	public void doFlash(Pin pin, String parameters) {
		String[] params = parameters.split("[ ,;]");
		int totalDuration;
		try {
			pin.setValue(1L);
			totalDuration = Integer.parseInt(params[0]);
		} catch (NumberFormatException | IllegalStateException | IOException e1) {
			return;
		}
		int onDuration;
		int offDuration;
		if (params.length > 1) {
			onDuration = Integer.parseInt(params[1]);
			offDuration = Integer.parseInt(params[2]);
		} else {
			onDuration = totalDuration + 1;
			offDuration = 0;
		}
		new Thread(() -> {
			long start = System.currentTimeMillis();
			while ((System.currentTimeMillis() - start) < totalDuration) {
				try {
					pin.setValue(1L);
					Thread.sleep(onDuration);
					pin.setValue(0L);
					Thread.sleep(offDuration);
				} catch (IllegalStateException | IOException | InterruptedException e) {
				}
			}
		}).start();
	}

	public void doTones(Pin pin, String parameters) {
		String[] params = parameters.split("[ ,;]");
		try {
			for (int i = 0; i < params.length; i = i + 2) {
				try {
					var curNote = Note.valueOf(params[i]);
					var curDuration = Integer.parseInt(params[i + 1]);
					new Tone(curNote.getFrequency(), curDuration, pin).play();
				} catch (IllegalArgumentException e1) {
					// not a note, not a number, ignore
					logger./**/warn("pin {} illegal TONE pair, expecting Note,Duration: {} {}", pin.getIndex(),
					        params[i], params[i + 1]);
				}
			}
		} catch (ArrayIndexOutOfBoundsException e2) {
			logger./**/warn("pin {} illegal TONE array length, must be > 0 and multiple of 2", pin.getIndex());
		}
	}

	public void stop() {
		try {
			logger.info("stopping device {} ", device);
			device.stop();
		} catch (IOException e) {
			// ignored
		}
	}
}
