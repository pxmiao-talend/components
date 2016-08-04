package org.talend.components.fileinput.runtime;

import org.apache.avro.Schema;
import org.apache.avro.generic.IndexedRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talend.components.fileinput.connection.FileInputField;
import org.talend.components.fileinput.tFileInputDelimited.TFileInputDelimitedDefinition;
import org.talend.daikon.avro.AvroRegistry;
import org.talend.daikon.avro.AvroUtils;
import org.talend.daikon.avro.converter.AvroConverter;
import org.talend.daikon.avro.converter.IndexedRecordConverter;

public class FileInputAdaptorFactory implements IndexedRecordConverter<FileInputField[], IndexedRecord>{

	 private static final Logger LOGGER = LoggerFactory.getLogger(TFileInputDelimitedDefinition.class);

	    private Schema schema;

	    @Override
	    public Schema getSchema() {
	        return this.schema;
	    }

	    @Override
	    public Class<FileInputField[]> getDatumClass() {
	        return FileInputField[].class;
	    }

	    @Override
	    public FileInputField[] convertToDatum(IndexedRecord indexedRecord) {
	        throw new UnmodifiableAdapterException();
	    }

	    @Override
	    public IndexedRecord convertToAvro(FileInputField[] fileInputSetRecord) {
	        if (AvroUtils.isIncludeAllFields(schema)) {
	            AvroRegistry avroRegistry = FileInputAvroRegistry.getFileInputInstance();
	            schema = avroRegistry.inferSchema(fileInputSetRecord);
	            LOGGER.debug("Source schema is: {}", schema.toString(true));
	        }
	        return new FileInputIndexedRecord(fileInputSetRecord);
	    }

	    @Override
	    public void setSchema(Schema schema) {
	        this.schema = schema;
	    }

	    private class FileInputIndexedRecord implements IndexedRecord {

	        private FileInputField[] fileInputFields;

	        private AvroConverter[] fieldConverter;

	        private String[] names;

	        FileInputIndexedRecord(FileInputField[] fileInputFields) {
	            this.fileInputFields = fileInputFields;
	        }

	        @Override
	        public void put(int i, Object v) {
	            throw new UnmodifiableAdapterException();
	        }

	        @Override
	        public Object get(int i) {
	            if (names == null) {
	                names = new String[getSchema().getFields().size()];
	                fieldConverter = new AvroConverter[names.length];
	                for (int j = 0; j < names.length; j++) {
	                    Schema.Field f = getSchema().getFields().get(j);
	                    names[j] = f.name();
	                    fieldConverter[j] = FileInputAvroRegistry.getFileInputInstance().getConverterFromString(f);
	                }
	            }
	            Object value = null;
	            for (FileInputField field : fileInputFields) {
	                if (field.getColumnName().equals(names[i])) {
	                    value = fieldConverter[i].convertToAvro(field.getContent());
	                }
	            }
	            return value;
	        }

	        @Override
	        public Schema getSchema() {
	            return FileInputAdaptorFactory.this.getSchema();
	        }
	    }

}
