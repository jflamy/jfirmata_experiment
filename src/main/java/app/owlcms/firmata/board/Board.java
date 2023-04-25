package app.owlcms.firmata.board;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
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
import ch.qos.logback.classic.Level;
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
		logger.setLevel(Level.DEBUG);
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
			logger.debug("board init exception {}", ex.getMessage());
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
			logger.debug("before throwable board init exception");
			Throwable cause = ex.getCause() != null ? ex.getCause() : ex;
			logger.debug("after throwable board init exception", ex.getMessage());
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

	public Thread doFlash(Pin pin, String parameters, String action) {
		String[] topLevelParams = parameters.split("[-]");
		String[] params = topLevelParams[0].split("[ ,;]");
		int totalDuration;
		int onDuration;
		int offDuration;

		try {
			totalDuration = Integer.parseInt(params[0].trim());
			if (params.length > 1) {
				onDuration = Integer.parseInt(params[1].trim());
				offDuration = Integer.parseInt(params[2].trim());
			} else {
				onDuration = totalDuration + 1;
				offDuration = 0;
			}
		} catch (NumberFormatException | IllegalStateException e1) {
			return null;
		}

		List<Integer> interruptionButtonInts = readInterruptionButtons(pin, topLevelParams, 1);

		Thread th = new Thread(() -> {
			long start = System.currentTimeMillis();
			interrupted: while ((System.currentTimeMillis() - start) < totalDuration) {
				try {
					if (Thread.interrupted()) {
						break interrupted;
					}
					pin.setValue(1L);
					Thread.sleep(onDuration);
					pin.setValue(0L);
					Thread.sleep(offDuration);
				} catch (InterruptedException e) {
					break interrupted;
				} catch (IllegalStateException | IOException e) {
					;
				}
			}
		});

		addInterruptibleThread(interruptionButtonInts, th, action+" " + pin.getIndex());

		th.start();
		return th;
	}

	private synchronized void addInterruptibleThread(List<Integer> interruptionButtonInts, Thread th, String whereFrom) {
		if (interruptionButtonInts == null || interruptionButtonInts.size() == 0) {
			return;
		}
		logger.debug("adding interruption buttons for {}: {}", whereFrom, interruptionButtonInts);
		for (int i : interruptionButtonInts) {
			// pressing button i should interrupt the thread (red and white buttons)
			addThread((byte) i, th);
		}
	}

	private List<Integer> readInterruptionButtons(Pin pin, String[] params, int paramIndex) {
		List<Integer> interruptionButtonInts = new ArrayList<>();
		if (params.length > paramIndex) {
			String[] interruptionButtons = params[paramIndex].trim().split("[,;]");
			for (String buttonNo : interruptionButtons) {
				int buttonIndex = Integer.parseInt(buttonNo);
				interruptionButtonInts.add(buttonIndex);
			}
			logger.debug("pin {} can be interrupted by {}", pin.getIndex(), interruptionButtonInts);
		}
		return interruptionButtonInts;
	}

	Map<Byte, List<Thread>> threadsToBeKilled = new HashMap<>();

	public synchronized void addThread(byte interruptionButtonIndex, Thread th) {
		List<Thread> threads = threadsToBeKilled.get(interruptionButtonIndex);
		if (threads == null) {
			threads = new ArrayList<>();
		}
		threads.add(th);
		synchronized (threadsToBeKilled) {
			threadsToBeKilled.put(interruptionButtonIndex, threads);
		}
	}

	public synchronized void killThreads(byte interruptionButtonIndex) {
		List<Thread> threads = null;
		synchronized (threadsToBeKilled) {
			threads = threadsToBeKilled.get(interruptionButtonIndex);
			if (threads != null) {
				logger.debug("removing threads {} for interruption button {}", threads, interruptionButtonIndex);
			}
		}
		if (threads != null) {
			for (Thread thread : threads) {
				thread.interrupt();
			}
			List<Byte> emptyButtons = new ArrayList<>();
			
			// each thread can be registered to multiple buttons. remove from all buttons.
			for (Entry<Byte, List<Thread>> entry : threadsToBeKilled.entrySet()) {
				var buttonThreads = entry.getValue();
				buttonThreads.removeAll(threads);
				if (buttonThreads.isEmpty()) {
					emptyButtons.add(entry.getKey());
				}
			}
			// clean-up
			for (Byte b : emptyButtons) {
				logger.debug("no more entries for button {}",b);
				threadsToBeKilled.remove(b);
			}
		}
	}

	public Thread doTones(Pin pin, String parameters) {
		String[] topLevelParams = parameters.split("[-]");
		String[] params = topLevelParams[0].trim().split("[,;]");
		List<Integer> interruptionButtonInts = readInterruptionButtons(pin, topLevelParams, 1);

		Thread th = new Thread(() -> {
			try {
				interrupted: for (int i = 0; i < params.length; i = i + 2) {
					try {
						if (Thread.interrupted()) {
							break interrupted;
						}
						var curNote = Note.valueOf(params[i].trim());
						var curDuration = Integer.parseInt(params[i + 1].trim());
						Tone tone = new Tone(curNote.getFrequency(), curDuration, pin, this);
						tone.playWait();
					} catch (InterruptedException e) {
						break interrupted;
					} catch (IllegalArgumentException e1) {
						logger.error("pin {} illegal TONE pair, expecting Note,Duration: {},{}", pin.getIndex(),
						        params[i], params[i + 1]);
					}
				}
			} catch (ArrayIndexOutOfBoundsException e2) {
				logger./**/warn("pin {} illegal TONE array length, must be > 0 and multiple of 2", pin.getIndex());
			}
		});
		
		addInterruptibleThread(interruptionButtonInts, th, "Tone " + pin.getIndex());
		
		th.start();
		return th;
	}

	public void stop() {
		try {
			logger.info("stopping device {} ", device);
			device.stop();
		} catch (IOException e) {
			// ignored
		}
	}

	synchronized public void pinSetValue(Pin pin, long l) {
		try {
			pin.setValue(l);
		} catch (IllegalStateException | IOException e) {
			logger.debug("cannot set pin {} value : {}", pin.getIndex(), e);
		}
	}
}
