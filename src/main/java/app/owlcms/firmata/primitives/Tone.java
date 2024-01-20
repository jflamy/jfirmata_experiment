package app.owlcms.firmata.primitives;

import java.util.ArrayList;
import java.util.List;

import org.firmata4j.Pin;
import org.slf4j.LoggerFactory;

import app.owlcms.firmata.refdevice.Interruptible;
import app.owlcms.firmata.refdevice.RefDevice;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;

public class Tone {
	final Logger logger = (Logger) LoggerFactory.getLogger(Tone.class);

	private int frequency;
	private int msDuration;
	private Pin pin;

	private RefDevice board;

	public Tone(int frequency, int msDuration, Pin pin, RefDevice board) {
		this.frequency = frequency;
		this.msDuration = msDuration;
		this.pin = pin;
		this.board = board;
		logger.setLevel(Level.DEBUG);
	}

	public List<String> nanoTimes = new ArrayList<>();

	public void playWait(Interruptible parent) throws InterruptedException {
		double upDuration;
		long nbFullCycles;
		long squareNanos;

		if (frequency == 0) {
			// 250Hz silence
			upDuration = (1.0D / 250) / 2;
			nbFullCycles = 250;
			squareNanos = Math.round((upDuration) * 1_000_000_000);
		} else {
			upDuration = (1.0D / frequency) / 2;
			nbFullCycles = (long) ((msDuration / 1000D) * frequency);
			squareNanos = Math.round((upDuration) * 1_000_000_000);
		}

		var start = System.nanoTime();
		logger.debug("pin {} frequency {} upDuration {}s nanos {}ns cycles {}", pin.getIndex(), frequency,
		        Double.toString(upDuration), squareNanos, nbFullCycles);

		try {
			for (int i = 0; i < nbFullCycles; i++) {
				var curStart = System.nanoTime();
				board.pinSetValue(pin, frequency > 0 ? 1 : 0);
				while (System.nanoTime() < curStart + squareNanos) {
					if (parent.isInterrupted()) {
						return;
					}
					Thread.yield();
				}

				board.pinSetValue(pin, 0);
				curStart = System.nanoTime();
				while (System.nanoTime() < curStart + squareNanos) {
					if (parent.isInterrupted()) {
						return;
					}
					Thread.yield();
				}
			}
		} catch (IllegalStateException e) {
			logger.error("exception {}", e);
		} finally {
			long arg1 = System.nanoTime() - start;
			logger.debug("done {}ns nano cycle average {}", arg1, (1.0D * arg1) / nbFullCycles);
			try {
				board.pinSetValue(pin, 0);
			} catch (IllegalStateException e) {
			}
		}
		return;
	}

}
