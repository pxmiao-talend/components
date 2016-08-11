package org.talend.components.fileinput.runtime;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Map;

import org.apache.avro.generic.IndexedRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talend.components.api.component.runtime.AbstractBoundedReader;
import org.talend.components.api.component.runtime.BoundedSource;
import org.talend.components.api.component.runtime.Result;
import org.talend.components.api.container.RuntimeContainer;
import org.talend.components.fileinput.FileInputDefinition;
import org.talend.components.fileinput.FileInputProperties;

/**
 * Simple implementation of a reader.
 */
public class FileInputReader extends AbstractBoundedReader<IndexedRecord> {

	/** Default serial version UID. */
	private static final long serialVersionUID = 1L;

	private static final Logger LOGGER = LoggerFactory.getLogger(FileInputDefinition.class);

	private RuntimeContainer container;

	private boolean started = false;

	private BufferedReader reader = null;

	private transient IndexedRecord currentIndexRecord;

	private transient String currentRow;

	FileInputProperties properties;

	public FileInputReader(RuntimeContainer container, BoundedSource source, FileInputProperties properties) {
		super(source);
		this.container = container;
		this.properties = properties;
	}

	@Override
	public boolean start() throws IOException {
		started = true;
		LOGGER.debug("open: " + properties.filename.getStringValue()); //$NON-NLS-1$
		reader = new BufferedReader(new FileReader(properties.filename.getStringValue()));
		currentRow = reader.readLine();
		return currentRow != null;
	}

	@Override
	public boolean advance() throws IOException {
		currentRow = reader.readLine();
		return currentRow != null;
	}

	@Override
	public IndexedRecord getCurrent() {
		String[] values = currentRow.split(",");

		FileInputAdaptorFactory factory = new FileInputAdaptorFactory();
		factory.setSchema(properties.schema.schema.getValue());

		currentIndexRecord = factory.convertToAvro(values);
		return currentIndexRecord;
	}

	@Override
	public void close() throws IOException {
		reader.close();
		LOGGER.debug("close: " + properties.filename.getStringValue()); //$NON-NLS-1$
	}

	@Override
	public Map<String, Object> getReturnValues() {
		return new Result().toMap();
	}

}
