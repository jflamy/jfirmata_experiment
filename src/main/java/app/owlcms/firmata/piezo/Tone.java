package app.owlcms.firmata.piezo;

import java.io.IOException;
import java.util.concurrent.locks.ReentrantLock;

import org.firmata4j.Pin;
import org.slf4j.LoggerFactory;

import app.owlcms.firmata.Main;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;

public class Tone {
	final Logger logger = (Logger) LoggerFactory.getLogger(Main.class);
	
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
		double squareDuration;
		long nbFullCycles;
		long squareNanos;
		
		squareDuration = (1.0D / frequency) / 2D;
		nbFullCycles = Math.round((msDuration / 1000D) * frequency);
		squareNanos = Math.round(squareDuration * 1_000_000);
		
		//FIXME special case if frequency 0
		
		var lock = new ReentrantLock();
		var done = lock.newCondition();
		
		logger.debug("pin {} frequency {} squareDuration {} nanos {} cycles {}", pin.getIndex(), frequency, Double.toString(squareDuration), squareNanos, nbFullCycles);
		new Thread(() -> {
			lock.lock();
			try {
				for (int i = 0; i < nbFullCycles; i++) {
					pin.setValue(frequency > 0 ? 1 : 0);
					done.awaitNanos(squareNanos);
					pin.setValue(0);
					done.awaitNanos(squareNanos);
				}
				done.signal();
			} catch (IllegalStateException | IOException | InterruptedException e) {
			} finally {
				lock.unlock();
			}
		}).start();
	}
	
//	public void playWait() {
//		double squareDuration = (1D / frequency) / 2D;
//		long nbFullCycles = Math.round((msDuration / 1000D) * frequency);
//
//		double msBeatDuration = squareDuration * 1000;
//		int ms = (int) msBeatDuration;
//		double nanoPart = (msBeatDuration - ms) * 100000;
//		int nanos = (int) nanoPart;
//
//		logger.warn("pin {} frequency {} squareDuration {} ms {} nano {} cycles {}", pin.getIndex(), frequency,
//				Double.toString(squareDuration), ms, nanos, nbFullCycles);
//		new Thread(() -> {
//			try {
//				for (int i = 0; i < nbFullCycles; i++) {
//					pin.setValue(1);
//					Thread.sleep(ms, nanos);
//					pin.setValue(0);
//					Thread.sleep(ms, nanos);
//				}
//			} catch (IllegalStateException | IOException | InterruptedException e) {
//			}
//		}).start();
//	}

}
