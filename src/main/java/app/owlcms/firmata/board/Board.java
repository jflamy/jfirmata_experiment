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
				logger.trace("output {}", i.getPinNumber());
				Pin pin = device.getPin(i.getPinNumber());
				pin.setMode(Mode.OUTPUT);
				pin.setValue(0L);
			} catch (Exception e) {
				logger.trace("exception setting outputs {} {}", i.getPinNumber(), e);
			}
		});
		inputEventHandler.getDefinitions().stream().forEach(i -> {
			try {
				logger.trace("button {}", i.getPinNumber());
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

	public class FlashDoer implements Interruptible {
		public boolean isInterrupted() {
			return interrupted;
		}

		private boolean interrupted;

		@Override
		public void setInterrupted(boolean interrupted) {
			this.interrupted = interrupted;
		}

		public void doit(long totalDuration, long onDuration, long offDuration, Pin pin, Board board) {
			long start = System.currentTimeMillis();
			while (!interrupted && (System.currentTimeMillis() - start) < totalDuration) {
				try {
					long targetDuration;
					board.pinSetValue(pin,1L);

					targetDuration = System.currentTimeMillis() + onDuration;
					while (!interrupted && (System.currentTimeMillis() < targetDuration)) {
						Thread.yield();
					}

					board.pinSetValue(pin,0L);
					targetDuration = System.currentTimeMillis() + offDuration;
					while (!interrupted && (System.currentTimeMillis() < targetDuration)) {
						Thread.yield();
					}
				} catch (IllegalStateException e) {
					;
				}
			}
		}
	}

	public FlashDoer doFlash(Pin pin, String parameters, String action) {
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
		FlashDoer th = new FlashDoer();
		addInterruptible(interruptionButtonInts, th, action + " " + pin.getIndex());
		th.doit(totalDuration, onDuration, offDuration, pin, this);
		return th;
	}

	private synchronized void addInterruptible(List<Integer> interruptionButtonInts, Interruptible th,
	        String whereFrom) {
		if (interruptionButtonInts == null || interruptionButtonInts.size() == 0) {
			return;
		}
		logger.debug("adding interruption buttons for {}: {}", whereFrom, interruptionButtonInts);
		for (int i : interruptionButtonInts) {
			// pressing button i should interrupt the thread (red and white buttons)
			addInterruptible((byte) i, th);
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

	Map<Byte, List<Interruptible>> toBeInterrupted = new HashMap<>();

	public synchronized void addInterruptible(byte interruptionButtonIndex, app.owlcms.firmata.board.Interruptible th) {
		List<Interruptible> threads = toBeInterrupted.get(interruptionButtonIndex);
		if (threads == null) {
			threads = new ArrayList<>();
		}
		threads.add(th);
		synchronized (toBeInterrupted) {
			toBeInterrupted.put(interruptionButtonIndex, threads);
		}
	}

	public synchronized void interruptInterruptibles(byte interruptionButtonIndex) {
		List<app.owlcms.firmata.board.Interruptible> interruptibles = null;
		synchronized (toBeInterrupted) {
			interruptibles = toBeInterrupted.get(interruptionButtonIndex);
			if (interruptibles != null) {
				logger.debug("interrupting doers {} for interruption button {}", interruptibles, interruptionButtonIndex);
				for (var interruptible : interruptibles) {
					interruptible.setInterrupted(true);
				}
				List<Byte> emptyButtons = new ArrayList<>();

				// each thread can be registered to multiple buttons. remove from all buttons.
				for (Entry<Byte, List<Interruptible>> entry : toBeInterrupted.entrySet()) {
					var buttonInterruptibles = entry.getValue();
					buttonInterruptibles.removeAll(interruptibles);
					if (buttonInterruptibles.isEmpty()) {
						emptyButtons.add(entry.getKey());
					}
				}
				// clean-up
				for (Byte b : emptyButtons) {
					logger.debug("no more entries for button {}", b);
					toBeInterrupted.remove(b);
				}
			}
		}
	}

	public synchronized void cleanInterruptibles(Interruptible interruptible) {
		if (interruptible != null) {
			synchronized (toBeInterrupted) {
				logger.debug("interruptible {} done", interruptible);
				// each thread can be registered to multiple buttons. remove from all buttons.
				List<Byte> emptyButtons = new ArrayList<>();
				for (Entry<Byte, List<Interruptible>> entry : toBeInterrupted.entrySet()) {
					var buttonInterruptibles = entry.getValue();
					buttonInterruptibles.remove(interruptible);
					if (buttonInterruptibles.isEmpty()) {
						emptyButtons.add(entry.getKey());
					}
				}
				// clean-up
				for (Byte b : emptyButtons) {
					logger.debug("no more entries for button {}", b);
					toBeInterrupted.remove(b);
				}
			}
		}
	}

	public class ToneDoer implements Interruptible {
		private boolean interrupted;

		public boolean isInterrupted() {
			return interrupted;
		}

		@Override
		public void setInterrupted(boolean interrupted) {
			this.interrupted = interrupted;
		}

		public void doit(String[] params, Pin pin, Board board) {
			try {
				interrupted: for (int i = 0; i < params.length; i = i + 2) {
					try {
						if (interrupted) {
							break interrupted;
						}
						var curNote = Note.valueOf(params[i].trim());
						var curDuration = Integer.parseInt(params[i + 1].trim());
						Tone tone = new Tone(curNote.getFrequency(), curDuration, pin, board);
						tone.playWait(this);
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
		}
	}

	public ToneDoer doTones(Pin pin, String parameters) {
		String[] topLevelParams = parameters.split("[-]");
		String[] params = topLevelParams[0].trim().split("[,;]");
		
		List<Integer> interruptionButtonInts = readInterruptionButtons(pin, topLevelParams, 1);
		ToneDoer th = new ToneDoer();
		addInterruptible(interruptionButtonInts, th, "Tone " + pin.getIndex());
		th.doit(params, pin, this);
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
