package app.owlcms.firmata.piezo;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import org.firmata4j.Pin;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;

public class Tone {
	final Logger logger = (Logger) LoggerFactory.getLogger(Tone.class);

	private int frequency;
	private int msDuration;
	private Pin pin;

	public Tone(int frequency, int msDuration, Pin pin) {
		this.frequency = frequency;
		this.msDuration = msDuration;
		this.pin = pin;
		logger.setLevel(Level.DEBUG);
	}

	public Thread play() {
		double upDuration;
		long nbFullCycles;
		long squareNanos;

		if (frequency == 0) {
			// 250Hz silence
			upDuration = 1.0D / 250;
			nbFullCycles = (long) (((msDuration / 1000D) / upDuration));
			squareNanos = Math.round((upDuration) * 1_000_000_000);
		} else {
			upDuration = (1.0D / frequency);
			nbFullCycles = (long) (((msDuration / 1000D) / upDuration));
			squareNanos = Math.round((upDuration) * 1_000_000_000);
		}

		var t1 = new Thread(() -> {
			var lock = new ReentrantLock();
			var done = lock.newCondition();
			lock.lock();
			var start = System.currentTimeMillis();
			logger.warn("pin {} frequency {} upDuration {}s nanos {}ns cycles {}", pin.getIndex(), frequency, Double.toString(upDuration), squareNanos, nbFullCycles);

			try {
				for (int i = 0; i < nbFullCycles; i++) {
					setPin(squareNanos, done, frequency > 0 ? 1 : 0);
					setPin(squareNanos, done, 0);
				}
				//done.signal();
			} catch (IllegalStateException | IOException | InterruptedException e) {
				logger.error("exception {}", e);
			} finally {
				logger.warn("done {}", System.currentTimeMillis() - start);
				lock.unlock();
			}
		});
		t1.start();
		return t1;
	}
	
	public List<String> nanoTimes = new ArrayList<>();

	private void setPin(long squareNanos, Condition done, int value) throws IOException, InterruptedException {
		var now = System.nanoTime();
		pin.setValue(value);
		var wasted = System.nanoTime() - now;
		int remaining = (int) (squareNanos - wasted);
		if (remaining > 0) {
			done.awaitNanos(squareNanos);
		}
		nanoTimes.add("late "+(System.nanoTime()-(now+squareNanos)));
	}

	public Thread playWait() {
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
		logger.warn("pin {} frequency {} upDuration {}s nanos {}ns cycles {}", pin.getIndex(), frequency, Double.toString(upDuration), squareNanos, nbFullCycles);

		var t1 = new Thread(() -> {
			try {
				for (int i = 0; i < nbFullCycles; i++) {
					var curStart = System.nanoTime();
					pin.setValue(frequency > 0 ? 1 : 0);					
					while (System.nanoTime() < curStart + squareNanos) {
						Thread.sleep(0); // this wastes 200ns
					}
					
					pin.setValue(0);
					curStart = System.nanoTime();
					while (System.nanoTime() < curStart + squareNanos) {
						Thread.sleep(0); // this wastes 200ns
					}
				}
			} catch (IllegalStateException | IOException | InterruptedException e) {
				logger.error("exception {}", e);
			} finally {
				long arg1 = System.nanoTime() - start;
				logger.debug("done {}ns nano cycle average {}", arg1, (1.0D*arg1) / nbFullCycles);
				try {
					pin.setValue(0);
				} catch (IllegalStateException | IOException e) {
				}
			}
		});
		t1.setPriority(Thread.MAX_PRIORITY);
		t1.start();
		return t1;
	}

}
