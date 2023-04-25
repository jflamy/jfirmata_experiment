package app.owlcms.firmata.eventhandlers;

import java.util.List;

import org.firmata4j.Pin;
import org.slf4j.LoggerFactory;

import app.owlcms.firmata.board.Board;
import app.owlcms.firmata.devicespec.OutputPinDefinition;
import app.owlcms.firmata.utils.LoggerUtils;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;

/**
 * Perform actions resulting from the receipt of an MQTT Message.
 * 
 * These actions affect the output components (LEDs, buzzers, relays, etc.) The
 * actions to be performed are found in a definition table.
 * 
 * @author jflamy
 *
 */
public class OutputEventHandler {
	private List<OutputPinDefinition> definitions;

	private final Logger logger = (Logger) LoggerFactory.getLogger(OutputEventHandler.class);

	public OutputEventHandler(List<OutputPinDefinition> definitions) {
		this.setDefinitions(definitions);
		logger.setLevel(Level.DEBUG);
	}

	public List<OutputPinDefinition> getDefinitions() {
		return definitions;
	}

	public void handle(String topic, String messageStr, Board board) {
		getDefinitions().stream().filter(d1 -> d1.topic.startsWith(topic)
		        && (d1.message == null || d1.message.isBlank() || d1.message.trim().contentEquals(messageStr.trim())))
		        .forEach(d -> {
			        doPin(d, board);
		        });
	}

	public void setDefinitions(List<OutputPinDefinition> definitions) {
		this.definitions = definitions;
	}

	private void doPin(OutputPinDefinition d, Board board) {
		logger.debug("pin {} {} {} -> {} {} {}", d.getPinNumber(), d.topic, d.message, d.description, d.action,
		        d.parameters);
		Pin pin = board.getPin(d.getPinNumber());
		new Thread(() -> {
			try {
				switch (d.action.toUpperCase()) {
				case "OFF" -> {
					board.pinSetValue(pin, 0L);
				}
				case "ON" -> {
					Thread th = board.doFlash(pin, d.parameters, "ON");
					th.join();
					board.pinSetValue(pin, 0L);
				}
				case "FLASH" -> {
					Thread th = board.doFlash(pin, d.parameters, "FLASH");
					th.join();
					board.pinSetValue(pin, 0L);
				}
				case "TONE" -> {
					Thread th = board.doTones(pin, d.parameters);
					th.join();
					board.pinSetValue(pin, 0L);
				}
				}
			} catch (Exception e) {
				logger.error("Exception {}", LoggerUtils.stackTrace(e));
			}
		}).start();
	}

}
