package org.talend.components.cassandra.runtime_3_0;

import org.apache.beam.sdk.io.cassandra.CassandraRow;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;
import org.talend.components.api.component.runtime.AbstractBoundedReader;
import org.talend.components.api.container.RuntimeContainer;
import org.talend.components.cassandra.input.TCassandraInputProperties;

import java.io.IOException;
import java.util.Map;
import java.util.NoSuchElementException;

public class CassandraReader extends AbstractBoundedReader<CassandraRow> {

    private TCassandraInputProperties properties;

    private transient ResultSet rs;

    private transient Row current;

    protected RuntimeContainer container;

    protected CassandraReader(RuntimeContainer container, CassandraSource source, TCassandraInputProperties properties) {
        super(source);
        this.properties = properties;
        this.container = container;
        CassandraAvroRegistry.get(); //have call it once to registry, which will used by next
    }

    @Override
    public boolean start() throws IOException {
        if (rs == null) {
            Session session = ((CassandraSource) getCurrentSource()).connect(container);
            String keyspace = properties.getSchemaProperties().keyspace.getStringValue();
            if (keyspace != null) {
                session.execute("USE " + keyspace);
            }
            rs = session.execute(properties.query.getStringValue());
        }
        current = rs.one();
        return current != null;
    }

    @Override
    public boolean advance() throws IOException {
        return start();
    }

    @Override
    public CassandraRow getCurrent() throws NoSuchElementException {
        return CassandraRow.fromJavaDriverRow(current);
    }

    @Override
    public Map<String, Object> getReturnValues() {
        return null;
    }
}
