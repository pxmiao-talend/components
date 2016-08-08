package org.talend.components.cassandra;

import org.talend.components.api.component.Connector;
import org.talend.components.api.component.PropertyPathConnector;
import org.talend.components.api.properties.ConnectionPropertiesProvider;
import org.talend.components.common.FixedConnectorsComponentProperties;
import org.talend.daikon.properties.presentation.Form;

import java.util.Collections;
import java.util.Set;

public class CassandraIOBasedProperties extends FixedConnectorsComponentProperties implements ConnectionPropertiesProvider<CassandraConnectionProperties> {
    /**
     * named constructor to be used is these properties are nested in other properties. Do not subclass this method for
     * initialization, use {@link #init()} instead.
     *
     * @param name
     */
    public CassandraIOBasedProperties(String name) {
        super(name);
    }

    //TODO(bchen) want it private, but can't
    public CassandraConnectionProperties connectionProperties = new CassandraConnectionProperties("connectionProperties");
    public CassandraSchemaProperties schemaProperties = new CassandraSchemaProperties("schemaProperties", connectionProperties);

    protected transient PropertyPathConnector MAIN_CONNECTOR = new PropertyPathConnector(Connector.MAIN_NAME, "schemaProperties.main");

    public CassandraSchemaProperties getSchemaProperties() {
        return schemaProperties;
    }

    @Override
    public void setupLayout() {
        super.setupLayout();
        Form mainForm = new Form(this, Form.MAIN);
        mainForm.addRow(connectionProperties.getForm(Form.REFERENCE));
        mainForm.addRow(schemaProperties.getForm(Form.REFERENCE));
    }

    @Override
    public CassandraConnectionProperties getConnectionProperties() {
        return connectionProperties;
    }

    @Override
    protected Set<PropertyPathConnector> getAllSchemaPropertiesConnectors(boolean isOutputConnection) {
        if (isOutputConnection) {
            return Collections.singleton(MAIN_CONNECTOR);
        } else {
            return Collections.EMPTY_SET;
        }
    }
}
