package org.talend.components.cassandra.runtime_3_0;

import org.apache.beam.sdk.coders.AvroCoder;
import org.apache.beam.sdk.coders.Coder;
import org.apache.avro.Schema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talend.bigdata.BeamCoderProvider;
import org.talend.components.api.component.runtime.BoundedReader;
import org.talend.components.api.component.runtime.BoundedSource;
import org.talend.components.api.container.RuntimeContainer;
import org.talend.components.api.properties.ComponentProperties;
import org.talend.components.cassandra.input.TCassandraInputProperties;
import org.talend.daikon.properties.Properties;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;

public class CassandraSource extends CassandraSourceOrSink implements BoundedSource, BeamCoderProvider {

    private static final Logger LOG = LoggerFactory.getLogger(CassandraSource.class);

    public CassandraSource(){}

    @Override
    public List<? extends BoundedSource> splitIntoBundles(long desiredBundleSizeBytes, RuntimeContainer container) throws Exception {
        List<BoundedSource> list = new ArrayList<>();
        list.add(this);
        return list;
    }

    @Override
    public long getEstimatedSizeBytes(RuntimeContainer container) {
        return 0;
    }

    @Override
    public boolean producesSortedKeys(RuntimeContainer container) {
        return false;
    }

    @Override
    public BoundedReader createReader(RuntimeContainer container) {
        return new CassandraReader(container, this, (TCassandraInputProperties)properties);
    }

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeUTF(((ComponentProperties)properties).toSerialized());
    }

    private void readObject(ObjectInputStream in) throws IOException {
        properties = (TCassandraInputProperties)Properties.Helper.fromSerializedPersistent(in
                .readUTF(),
                TCassandraInputProperties.class).object;
    }

    public Class getInstanceClass() {
        return CassandraBaseAdapterFactory.GettableAdapterIndexedRecord.class;
    }

    public Schema getInstanceSchema() {
        TCassandraInputProperties props = (TCassandraInputProperties) this.properties;
        try {
            return getSchema(null, props.schemaProperties.keyspace.getValue(), props
                    .schemaProperties
                    .columnFamily.getValue());
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    @Override
    public Coder getCoder() {
        return AvroCoder.of(getInstanceClass(), getInstanceSchema());
    }
}
