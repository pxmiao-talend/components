package org.talend.components.fileinput.runtime;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import org.apache.avro.Schema;
import org.talend.components.api.component.runtime.SourceOrSink;
import org.talend.components.api.container.RuntimeContainer;
import org.talend.components.api.properties.ComponentProperties;
import org.talend.daikon.NamedThing;
import org.talend.daikon.properties.ValidationResult;

public class FileInputSourceOrSink implements SourceOrSink{

	@Override
	public void initialize(RuntimeContainer container, ComponentProperties properties) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public ValidationResult validate(RuntimeContainer container) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<NamedThing> getSchemaNames(RuntimeContainer container) throws IOException {
		// TODO Auto-generated method stub
		return Collections.emptyList();
	}

	@Override
	public Schema getEndpointSchema(RuntimeContainer container, String schemaName) throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

}
