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

import ch.qos.logback.classic.Logger;

public class DeviceSpecReader {

	private List<EmitPinDefinition> emitPinDefinitions;
	private List<ButtonPinDefinition> buttonPinDefinitions;

	Logger logger = (Logger) LoggerFactory.getLogger(DeviceSpecReader.class);

	/**
	 * Create category templates that will be copied to instantiate the actual
	 * categories. The world records are read and included in the template.
	 *
	 * @param workbook
	 * @return
	 */
	@SuppressWarnings("unused")
	public void readPinDefinitions(Workbook workbook) {
		List<PinDefinition> pinDefinitions = new ArrayList<>();
		Sheet sheet = workbook.getSheetAt(0);
		Iterator<Row> rowIterator = sheet.rowIterator();
		int iRow = 0;
		rows: while (rowIterator.hasNext()) {
			Row row;
			if (iRow == 0) {
				// process header
				row = rowIterator.next();
			}
			row = rowIterator.next();
			Cell triggerCell = row.getCell(7);
			if (triggerCell != null && !triggerCell.toString().isBlank()) {
				pinDefinitions.add(readButtonDefinitions(pinDefinitions, row));
			} else {
				pinDefinitions.add(readEmitDefinitions(pinDefinitions, row));
			}
			iRow++;
		}

		emitPinDefinitions = pinDefinitions.stream().filter(p -> p instanceof EmitPinDefinition)
				.map(p -> (EmitPinDefinition) p).collect(Collectors.toList());
		buttonPinDefinitions = pinDefinitions.stream().filter(p -> p instanceof ButtonPinDefinition)
				.map(p -> (ButtonPinDefinition) p).collect(Collectors.toList());
		return;
	}

	private DataFormatter df = new DataFormatter();

	private EmitPinDefinition readEmitDefinitions(List<PinDefinition> pinDefinitions, Row row) {
		var ip = new EmitPinDefinition();
		ip.description = getCellAsString(row, 0);
		ip.pin = getCellAsString(row, 1);
		ip.topic = getCellAsString(row, 2);
		ip.message = getCellAsString(row, 3);
		ip.action = getCellAsString(row, 4);
		ip.parameters = getCellAsString(row, 5);
		logger.warn("row {} ", ip);
		return ip;
	}

	private ButtonPinDefinition readButtonDefinitions(List<PinDefinition> pinDefinitions, Row row) {
		var op = new ButtonPinDefinition();
		op.pin = getCellAsString(row, 1);
		op.topic = getCellAsString(row, 7);
		op.message = getCellAsString(row, 8);
		logger.warn("row {} ", op);
		return op;
	}

	private String getCellAsString(Row row, int i) {
		var val = row.getCell(i);
		return (val != null ? df.formatCellValue(val) : "");
	}

	public List<ButtonPinDefinition> getButtonPinDefinitions() {
		return buttonPinDefinitions;
	}

	public List<EmitPinDefinition> getEmitPinDefinitions() {
		return emitPinDefinitions;
	}

}
