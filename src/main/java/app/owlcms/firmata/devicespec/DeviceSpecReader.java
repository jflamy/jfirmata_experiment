package app.owlcms.firmata.devicespec;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.slf4j.LoggerFactory;

import app.owlcms.firmata.eventhandlers.InputEventHandler;
import app.owlcms.firmata.eventhandlers.OutputEventHandler;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;

public class DeviceSpecReader {

	private OutputEventHandler outputEventHandler;
	private InputEventHandler inputEventHandler;

	Logger logger = (Logger) LoggerFactory.getLogger(DeviceSpecReader.class);
	
	public DeviceSpecReader() {
		logger.setLevel(Level.TRACE);
	}

	public void readPinDefinitions(Workbook workbook) {
		workbook.getCreationHelper().createFormulaEvaluator().evaluateAll();
		List<PinDefinition> pinDefinitions = new ArrayList<>();
		Sheet sheet = workbook.getSheetAt(0);
		Iterator<Row> rowIterator = sheet.rowIterator();
		int iRow = 0;
		while (rowIterator.hasNext()) {
			Row row;
			if (iRow == 0) {
				// process header
				row = rowIterator.next();
			}
			row = rowIterator.next();
			Cell triggerCell = row.getCell(7);
			if (triggerCell != null && !triggerCell.toString().isBlank()) {
				InputPinDefinition inputDefinition = readInputDefinition(pinDefinitions, row);
				if (inputDefinition != null) {
					pinDefinitions.add(inputDefinition);
				}
			} else {
				OutputPinDefinition outputDefinition = readOutputDefinition(pinDefinitions, row);
				if (outputDefinition != null) {
					pinDefinitions.add(outputDefinition);
				}
			}
			iRow++;
		}

		outputEventHandler = new OutputEventHandler(
				pinDefinitions.stream().filter(p -> p instanceof OutputPinDefinition).map(p -> (OutputPinDefinition) p)
						.collect(Collectors.toList()));
		inputEventHandler = new InputEventHandler(
				pinDefinitions.stream().filter(p -> p instanceof InputPinDefinition)
				.map(p -> (InputPinDefinition) p).collect(Collectors.toList()));
		return;
	}

	private DataFormatter df = new DataFormatter();

	private OutputPinDefinition readOutputDefinition(List<PinDefinition> pinDefinitions, Row row) {
		var ip = new OutputPinDefinition();
		ip.description = getCellAsString(row, 0);
		ip.pin = getCellAsString(row, 1);
		ip.topic = getCellAsString(row, 2);
		ip.message = getCellAsString(row, 3);
		ip.action = getCellAsString(row, 4);
		ip.parameters = getCellAsString(row, 5);
		logger.trace("row {} ", ip);
		return (ip.getPinNumber() > 0 ? ip : null);
	}

	private InputPinDefinition readInputDefinition(List<PinDefinition> pinDefinitions, Row row) {
		var op = new InputPinDefinition();
		op.pin = getCellAsString(row, 1);
		op.topic = getCellAsString(row, 7);
		op.message = getCellAsString(row, 8);
		logger.trace("row {} ", op);
		return (op.getPinNumber() > 0 ? op : null);
	}

	private String getCellAsString(Row row, int i) {
		var val = row.getCell(i);
		return (val != null ? df.formatCellValue(val) : "");
	}

	public InputEventHandler getInputEventHandler() {
		return inputEventHandler;
	}

	public OutputEventHandler getOutputEventHandler() {
		return outputEventHandler;
	}

}
