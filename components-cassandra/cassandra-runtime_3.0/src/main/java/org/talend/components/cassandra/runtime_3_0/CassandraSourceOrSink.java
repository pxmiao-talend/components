package org.talend.components.cassandra.runtime_3_0;

import com.datastax.driver.core.*;
import org.apache.avro.Schema;
import org.talend.components.api.component.runtime.SourceOrSink;
import org.talend.components.api.container.RuntimeContainer;
import org.talend.components.api.properties.ComponentProperties;
import org.talend.components.api.properties.ConnectionPropertiesProvider;
import org.talend.components.cassandra.CassandraConnectionProperties;
import org.talend.daikon.NamedThing;
import org.talend.daikon.SimpleNamedThing;
import org.talend.daikon.properties.ValidationResult;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class CassandraSourceOrSink implements org.talend.components.cassandra.runtime.CassandraSourceOrSink {

    protected transient ConnectionPropertiesProvider<CassandraConnectionProperties> properties;

    protected static final String SESSION_GLOBALMAP_KEY = "Session"; //FIXME only for Studio?
    protected static final String CLUSTER_GLOBALMAP_KEY = "Cluster"; //FIXME only for Studio?

    @Override
    public void initialize(RuntimeContainer container, ComponentProperties properties) {
        this.properties = (ConnectionPropertiesProvider<CassandraConnectionProperties>) properties;
    }

    @Override
    public ValidationResult validate(RuntimeContainer container) {
        try {
            connect(container);
        } catch (IOException ex) {
            return new ValidationResult().setStatus(ValidationResult.Result.ERROR).setMessage(ex.getMessage());
        }
        return ValidationResult.OK;
    }

    //FIXME can it be connection pool?
    protected Session connect(RuntimeContainer container) throws IOException {
        String referencedComponentId = properties.getConnectionProperties().getReferencedComponentId();
        if (referencedComponentId != null) {
            if (container != null) {
                Session conn = (Session) container.getComponentData(referencedComponentId, SESSION_GLOBALMAP_KEY);
                if (conn != null)
                    return conn;
                throw new IOException("Referenced component: " + referencedComponentId + " not connected");
            }
        }
        try {
            return getCluster(container).connect();
        } catch (Exception ex) {
            throw new IOException(ex.getMessage());
        }
    }

    private Cluster getCluster(RuntimeContainer container) {
        CassandraConnectionProperties connProps = properties.getConnectionProperties();
        Cluster.Builder clusterBuilder = Cluster.builder()
                .addContactPoints(connProps.host.getStringValue().split(","))
                .withPort(Integer.valueOf(connProps.port.getValue()));
        if (connProps.needAuth.getValue()) {
            clusterBuilder.withCredentials(connProps.userPassword.userId.getStringValue(), connProps.userPassword.password.getStringValue());
        }
        return clusterBuilder.build();
    }

    private List<KeyspaceMetadata> getKeyspaces(RuntimeContainer container) {
        Metadata metadata = getCluster(container).getMetadata();
        return metadata.getKeyspaces();
    }

    public List<NamedThing> getKeyspaceNames(RuntimeContainer container) throws IOException {
        List<NamedThing> ksNames = new ArrayList<>();
        List<KeyspaceMetadata> keyspaces = getKeyspaces(container);
        for (KeyspaceMetadata keyspace : keyspaces) {
            ksNames.add(new SimpleNamedThing(keyspace.getName(), keyspace.getName()));
        }
        return ksNames;
    }

    private List<TableMetadata> getTables(RuntimeContainer container, String keyspaceName) {
        List<TableMetadata> tables = new ArrayList<>();
        List<KeyspaceMetadata> keyspaces = getKeyspaces(container);
        for (KeyspaceMetadata keyspace : keyspaces) {
            if (keyspaceName.equals(keyspace.getName())) {
                for (TableMetadata table : keyspace.getTables()) {
                    tables.add(table);
                }
            }
        }
        return tables;
    }

    public List<NamedThing> getTableNames(RuntimeContainer container, String keyspaceName) throws IOException {
        List<NamedThing> tableNames = new ArrayList<>();
        List<TableMetadata> tables = getTables(container, keyspaceName);
        for (TableMetadata table : tables) {
            tableNames.add(new SimpleNamedThing(table.getName(), table.getName()));
        }
        return tableNames;
    }

    public Schema getSchema(RuntimeContainer container, String keyspaceName, String tableName) throws IOException {
        return CassandraAvroRegistry.get().inferSchema(getCassandraMetadata(container,
                keyspaceName, tableName));
    }

    public TableMetadata getCassandraMetadata(RuntimeContainer container, String keyspaceName,
                                              String tableName) throws IOException {
        return getCluster(container).getMetadata().getKeyspace(keyspaceName).getTable(tableName);
    }

    @Override
    public List<NamedThing> getSchemaNames(RuntimeContainer container) throws IOException {
        return null;
    }

    public Schema getEndpointSchema(RuntimeContainer container, String schemaName) throws IOException {
        return null;
    }
}
