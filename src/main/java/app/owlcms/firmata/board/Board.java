package app.owlcms.firmata.board;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import org.eclipse.jetty.util.NanoTime;
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

	public Thread doFlash(Pin pin, String parameters) {
		String[] params = parameters.split("[ ]");
		int totalDuration;
		try {
			pin.setValue(1L);
			totalDuration = Integer.parseInt(params[0]);
		} catch (NumberFormatException | IllegalStateException | IOException e1) {
			return null;
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

		List<Integer> interruptionButtonInts = new ArrayList<>();
		if (params.length > 3) {
			String[] interruptionButtons = params[3].split("[,;]");
			for (String buttonNo : interruptionButtons) {
				int buttonIndex = Integer.parseInt(buttonNo);
				interruptionButtonInts.add(buttonIndex);
			}
			logger.warn("pin {} can be interrupted by {}", pin.getIndex(), interruptionButtonInts);
		}

		Thread th = new Thread(() -> {
			long start = System.currentTimeMillis();
			interrupted: while ((System.currentTimeMillis() - start) < totalDuration) {
				try {
					pin.setValue(1L);
					Thread.sleep(onDuration);
					pin.setValue(0L);
					Thread.sleep(offDuration);
				} catch (InterruptedException e) {
					try {
						logger.warn("pin {} interrupted",pin.getIndex());
						pin.setValue(0L);
						break interrupted;
					} catch (IllegalStateException | IOException e1) {
					}
				} catch (IllegalStateException | IOException e) {
				}
			}
			// we are done, this thread will not need to be interrupted by a button anymore
			for (int i : interruptionButtonInts) {
				logger.warn("pin {} no longer managed by button {}", pin.getIndex(), i);
				cleanThread((byte) i);
			}
		});

		for (int i : interruptionButtonInts) {
			// pressing button i should interrupt the thread (red and white buttons)
			addThread((byte) i, th);
		}

		th.start();
		return th;
	}

	Map<Byte, List<Thread>> threadsToBeKilled = new HashMap<>();

	public void addThread(byte index, Thread th) {
		List<Thread> threads = threadsToBeKilled.get(index);
		if (threads == null) {
			threads = new ArrayList<>();
		}
		threads.add(th);
		threadsToBeKilled.put(index, threads);
	}

	public void killThreads(byte index) {
		List<Thread> threads = threadsToBeKilled.get(index);
		if (threads != null) {
			for (Thread thread : threads)
				thread.interrupt();
		}
	}

	private void cleanThread(byte index) {
		threadsToBeKilled.remove(index);
	}

	public void doTones(Pin pin, String parameters) {
		String[] params = parameters.split("[ ,;]");
		try {
			interrupted: for (int i = 0; i < params.length; i = i + 2) {
				try {
					var curNote = Note.valueOf(params[i]);
					var curDuration = Integer.parseInt(params[i + 1]);
					logger.warn("============= {} {}",curNote, curDuration);
					Tone tone = new Tone(curNote.getFrequency(), curDuration, pin);
					Thread t1 = tone.playWait();
					
					//FIXME: register the thread with the button that can interrupt the tone
					//FIXME: cleanup
					try {
						t1.join();
					} catch (InterruptedException e) {
						break interrupted;
					}
//					String prev = tone.nanoTimes.get(0);
//					for (String t : tone.nanoTimes) {
//						//logger.warn("delta {}", t - prev);
//						logger.warn("loop {}", t);
//						prev = t;
//					}
				} catch (IllegalArgumentException e1) {
					e1.printStackTrace();
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
