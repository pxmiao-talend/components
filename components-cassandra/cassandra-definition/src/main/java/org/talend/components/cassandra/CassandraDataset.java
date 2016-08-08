package org.talend.components.cassandra;

import java.util.Scanner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talend.components.api.Constants;
import org.talend.components.api.component.AbstractDatasetDefinition;
import org.talend.components.api.component.DatasetDefinition;
import org.talend.components.api.properties.ComponentProperties;
import org.talend.components.cassandra.input.TCassandraInputDefinition;
import org.talend.components.cassandra.output.TCassandraOutputDefinition;

import aQute.bnd.annotation.component.Component;

@Component(name = Constants.DATASET_BEAN_PREFIX + CassandraDataset.DATASET_NAME, provide = DatasetDefinition.class)
public class CassandraDataset extends AbstractDatasetDefinition {

    private static final Logger LOG = LoggerFactory.getLogger(CassandraDataset.class);

    public static final String DATASET_NAME = "CassandraDataset"; //$NON-NLS-1$

    public static final String jsonSchema;

    static {
        String currentSchema = "";

        Scanner s = new Scanner(CassandraDataset.class.getResourceAsStream("CassandraDataset.json"));
        while (s.hasNext()) {
            currentSchema += s.next();
        }
        jsonSchema = currentSchema;
    }

    public CassandraDataset() {
        super(DATASET_NAME);
    }

    public CassandraDataset(String datasetName) {
        super(datasetName);
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
    public String[] getComponents() {
        return new String[] { new TCassandraInputDefinition().getName(), new TCassandraOutputDefinition().getName() };
    }

    @Override
    public Object[] getSample(Integer size) {
        // TODO implement
        return null;
    }

    @Override
    public String getSchema() {
        // TODO implement
        return null;
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
