package app.owlcms.firmata.piezo;

import java.io.IOException;
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

	public void play() {
		double cycleDuration;
		long nbFullCycles;
		long squareNanos;

		if (frequency == 0) {
			// 250Hz silence
			cycleDuration = 1.0D / 250;
			nbFullCycles = (long) ((msDuration / 1000D) / cycleDuration);
			squareNanos = Math.round((cycleDuration / 2.0D) * 1_000_000_000);
		} else {
			cycleDuration = (1.0D / frequency);
			nbFullCycles = (long) ((msDuration / 1000D) / cycleDuration);
			squareNanos = Math.round((cycleDuration / 2.0D) * 1_000_000_000);
		}

		var t1 = new Thread(() -> {
			var lock = new ReentrantLock();
			var done = lock.newCondition();
			lock.lock();
			//var start = System.currentTimeMillis();
			//logger.debug("pin {} frequency {} cycleDuration {} nanos {} cycles {}", pin.getIndex(), frequency, Double.toString(cycleDuration), squareNanos, nbFullCycles);

			try {
				for (int i = 0; i < nbFullCycles; i++) {
					setPin(squareNanos, done, frequency > 0 ? 1 : 0);
					setPin(squareNanos, done, 0);
				}
				done.signal();
			} catch (IllegalStateException | IOException | InterruptedException e) {
				logger.error("exception {}", e);
			} finally {
				//logger.debug("done {}", System.currentTimeMillis() - start);
				lock.unlock();
			}
		});
		t1.start();
		try {
			t1.join();
		} catch (InterruptedException e) {
		}
	}

	private void setPin(long squareNanos, Condition done, int value) throws IOException, InterruptedException {
		var now = System.nanoTime();
		pin.setValue(value);
		var wasted = System.nanoTime() - now;
		int remaining = (int) (squareNanos - wasted);
		if (remaining > 0) {
			done.awaitNanos(remaining);
		}
	}

//	public void playWait() {
//		double cycleDuration;
//		long nbFullCycles;
//		long squareNanos;
//
//		if (frequency == 0) {
//			// 1000Hz silence
//			cycleDuration = 1.0D / 1000;
//			nbFullCycles = (long) ((msDuration / 1000D) / cycleDuration);
//			squareNanos = Math.round((cycleDuration / 2.0D) * 1_000_000_000);
//		} else {
//			cycleDuration = (1.0D / frequency);
//			nbFullCycles = (long) ((msDuration / 1000D) / cycleDuration);
//			squareNanos = Math.round((cycleDuration / 2.0D) * 1_000_000_000);
//		}
//
//		var start = System.currentTimeMillis();
//		logger.debug("{}", squareNanos);
//		logger.debug("pin {} frequency {} cycleDuration {} ms {} + nanos {} cycles {}", pin.getIndex(), frequency,
//				Double.toString(cycleDuration), (squareNanos / 1_000_000), (int) (squareNanos % 1_000_000) , nbFullCycles);
//
//		var t1 = new Thread(() -> {
//			long totalPinNanos = 0;
//			try {
//
//				for (int i = 0; i < nbFullCycles; i++) {
//					var startPin = System.nanoTime();
//					pin.setValue(frequency > 0 ? 1 : 0);
//					var wasted = System.nanoTime() - startPin;
//					int remaining = (int) ((squareNanos % 1_000_000) - wasted);
//					if (remaining > 0) {
//						Thread.sleep((squareNanos / 1_000_000), remaining );
//					}
//					
//					startPin = System.nanoTime();
//					pin.setValue(0);
//					wasted = System.nanoTime() - startPin;
//					remaining = (int) ((squareNanos % 1_000_000) - wasted);
//					if (remaining > 0) {
//						Thread.sleep((squareNanos / 1_000_000), remaining );
//						totalPinNanos = totalPinNanos + wasted;
//					};
//				}
//			} catch (IllegalStateException | IOException | InterruptedException e) {
//				logger.error("exception {}", e);
//			} finally {
//				logger.debug("done {} pin average {}", System.currentTimeMillis() - start, (1.0D*totalPinNanos) / nbFullCycles);
//			}
//		});
//		t1.start();
//		try {
//			t1.join();
//		} catch (InterruptedException e) {
//		}
//	}

}
