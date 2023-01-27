package app.owlcms.firmata.devicespec;

import java.util.List;

public class OutputPinDefinitionHandler {

	private List<OutputPinDefinition> definitions;

	public OutputPinDefinitionHandler(List<OutputPinDefinition> definitions) {
		this.setDefinitions(definitions);
	}

	public List<OutputPinDefinition> getDefinitions() {
		return definitions;
	}

	public void setDefinitions(List<OutputPinDefinition> definitions) {
		this.definitions = definitions;
	}

}
