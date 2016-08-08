package org.talend.components.cassandra.runtime_3_0.beam;

import org.apache.beam.sdk.coders.Coder;
import org.apache.beam.sdk.coders.SerializableCoder;
import org.apache.beam.sdk.io.cassandra.CassandraConfig;
import org.apache.beam.sdk.io.cassandra.CassandraIO;
import org.apache.beam.sdk.io.cassandra.CassandraRow;
import org.apache.beam.sdk.transforms.PTransform;
import org.apache.beam.sdk.values.PBegin;
import org.apache.beam.sdk.values.PCollection;
import org.apache.beam.sdk.values.PDone;
import org.apache.avro.generic.IndexedRecord;
import org.talend.bigdata.BeamIOGenrator;
import org.talend.components.api.properties.ComponentProperties;
import org.talend.components.cassandra.input.TCassandraInputProperties;
import org.talend.components.cassandra.output.TCassandraOutputProperties;
import org.talend.components.cassandra.runtime_3_0.CassandraSource;
import org.talend.daikon.avro.converter.IndexedRecordConverter;

import java.util.HashMap;
import java.util.Map;

public class CassandraIOGenerator implements BeamIOGenrator<CassandraRow, CassandraRow> {

    private ComponentProperties properties;

    @Override
    public void setComponentProperties(ComponentProperties componentProperties) {
        this.properties = componentProperties;
    }

    @Override
    public PTransform<PBegin, PCollection<CassandraRow>> genInput() {
        TCassandraInputProperties props = (TCassandraInputProperties) properties;

        Map<String, String> config = new HashMap<>();
        config.put(CassandraConfig.PORT, props.getConnectionProperties().port.getValue());

        return CassandraIO.read().withHost(props.getConnectionProperties().host.getValue())
                .withKeyspace(props.getSchemaProperties().keyspace.getValue()).withTable
                        (props.getSchemaProperties().columnFamily.getValue()).withConfig(config);
    }

    @Override
    public PTransform<PCollection<CassandraRow>, PDone> genOutput() {
        TCassandraOutputProperties props = (TCassandraOutputProperties) properties;

        Map<String, String> config = new HashMap<>();
        config.put(CassandraConfig.PORT, props.getConnectionProperties().port.getValue());


        return CassandraIO.write().withHost(props.getConnectionProperties().host.getValue())
                .withKeyspace(props.getSchemaProperties().keyspace.getValue())
                .withTable(props.getSchemaProperties().columnFamily.getValue()).withConfig(config);
    }

    @Override
    public Coder getInputCoder() {
        CassandraSource source = new CassandraSource();
        source.initialize(null, properties);
        return source.getCoder();
    }

    @Override
    public Coder getOutputCoder() {
        return SerializableCoder.of(CassandraRow.class);
    }

    @Override
    public IndexedRecordConverter<CassandraRow, IndexedRecord> getInputConverter() {
        return new CassandraIORowConverter();
    }

    @Override
    public IndexedRecordConverter<CassandraRow, IndexedRecord> getOutputConverter() {
        CassandraIORowConverter cassandraIORowConverter = new CassandraIORowConverter();
        cassandraIORowConverter.setProps((TCassandraOutputProperties) properties);
        return cassandraIORowConverter;
    }

}
