package org.talend.components.cassandra;

import java.util.Scanner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talend.components.api.Constants;
import org.talend.components.api.component.AbstractDatastoreDefinition;
import org.talend.components.api.component.DatastoreDefinition;
import org.talend.components.api.properties.ComponentProperties;

import aQute.bnd.annotation.component.Component;

@Component(name = Constants.DATASTORE_BEAN_PREFIX + CassandraDatastore.DATASTORE_NAME, provide = DatastoreDefinition.class)
public class CassandraDatastore extends AbstractDatastoreDefinition {

    private static final Logger LOG = LoggerFactory.getLogger(CassandraDatastore.class);

    public static final String DATASTORE_NAME = "CassandraDatastore"; //$NON-NLS-1$

    public static final String jsonSchema;

    static {
        String currentSchema = "";

        Scanner s = new Scanner(CassandraDatastore.class.getResourceAsStream("CassandraDatastore.json"));
        while (s.hasNext()) {
            currentSchema += s.next();
        }
        jsonSchema = currentSchema;
    }

    public CassandraDatastore() {
        super(DATASTORE_NAME);
    }

    public CassandraDatastore(String datastoreName) {
        super(datastoreName);
    }

    @Override
    public Class<? extends ComponentProperties> getPropertyClass() {
        return CassandraConnectionProperties.class;
    }

    @Override
    public String[] getFamilies() {
        return new String[] { "Databases/Cassandra", "Big Data/Cassandra" }; //$NON-NLS-1$  //$NON-NLS-2$
    }

    @Override
    public String[] getDatasets() {
        return new String[] { new CassandraDataset().getName() };
    }

    @Override
    public String getJSONSchema() {
        return jsonSchema;
    }

    @Override
    public String getMavenGroupId() {
        return "org.talend.components"; //$NON-NLS-1$
    }

    @Override
    public String getMavenArtifactId() {
        return "component-cassandra"; //$NON-NLS-1$
    }

}
