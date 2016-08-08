package org.talend.components.cassandra.runtime_3_0;

import org.junit.Test;
import org.talend.components.api.component.runtime.BoundedReader;
import org.talend.components.api.component.runtime.WriteOperation;
import org.talend.components.api.component.runtime.Writer;
import org.talend.components.cassandra.CassandraTestBase;
import org.talend.components.cassandra.input.TCassandraInputDefinition;
import org.talend.components.cassandra.input.TCassandraInputProperties;
import org.talend.components.cassandra.output.TCassandraOutputDefinition;
import org.talend.components.cassandra.output.TCassandraOutputProperties;
import org.talend.components.cassandra.runtime_3_0.CassandraSink;
import org.talend.components.cassandra.runtime_3_0.CassandraSource;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class CassandraWriterTestIT extends CassandraTestBase {
    @Test
    public void testReaderAndWriter() throws Throwable {
        TCassandraInputProperties inProps = (TCassandraInputProperties) getComponentService().getComponentProperties(TCassandraInputDefinition.COMPONENT_NAME);
        setupSchemaProps(inProps, false, KS_NAME, "example_src");
        inProps.query.setValue("select * from " + inProps.schemaProperties.columnFamily.getStringValue());

        TCassandraOutputProperties outProps = (TCassandraOutputProperties) getComponentService().getComponentProperties(TCassandraOutputDefinition.COMPONENT_NAME);
        setupSchemaProps(outProps, false, KS_NAME, "example_dst");
        //default is insert

        CassandraSource cassandraSource = new CassandraSource();
        cassandraSource.initialize(null, inProps);
        cassandraSource.validate(null);
        BoundedReader reader = cassandraSource.createReader(null);

        CassandraSink cassandraSink = new CassandraSink();
        cassandraSink.initialize(null, outProps);
        cassandraSink.validate(null);
        WriteOperation<?> writeOperation = cassandraSink.createWriteOperation();
        writeOperation.initialize(null);
        Writer<?> writer = writeOperation.createWriter(null);


        writer.open("");
        try {
            for (boolean available = reader.start(); available; available = reader.advance()) {
                Object current = reader.getCurrent();
                writer.write(current);
            }
        } finally {
            reader.close();
        }

        setupSchemaProps(inProps, false, KS_NAME, "example_dst");
        cassandraSource.initialize(null, inProps);
        cassandraSource.validate(null);
        reader = cassandraSource.createReader(null);
        assertTrue(reader.start());
        assertNotNull(reader.getCurrent());
        assertTrue(reader.advance());
        reader.close();


    }
}
