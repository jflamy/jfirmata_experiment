package app.owlcms.firmata.ui;

import java.io.OutputStream;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.LoggerFactory;

import com.vaadin.flow.component.upload.receivers.FileBuffer;
import com.vaadin.flow.component.upload.receivers.FileFactory;

import app.owlcms.firmata.devicespec.DeviceType;
import ch.qos.logback.classic.Logger;

public class FileUploader extends FileBuffer {

	Logger logger = (Logger) LoggerFactory.getLogger(FileUploader.class);

	public FileUploader(FileFactory fileFactory) {
		super(fileFactory);
	}

	@Override
	public OutputStream receiveUpload(String fileName, String MIMEType) {
		Set<String> values = Arrays.asList(DeviceType.values()).stream().map(v -> (v.configName + ".xlsx"))
				.collect(Collectors.toSet());
		if (!values.contains(fileName)) {
			throw new RuntimeException("Illegal file name.<br>Must use one of " + values);
		}
		return super.receiveUpload(fileName, MIMEType);
	}

}
