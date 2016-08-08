package org.talend.components.cassandra;

import org.talend.components.api.component.AbstractComponentDefinition;
import org.talend.daikon.properties.property.Property;

public abstract class CassandraDefinition extends AbstractComponentDefinition {

    public CassandraDefinition(String componentName) {
        super(componentName);
    }

    @Override
    public String[] getFamilies() {
        return new String[]{"Databases/Cassandra", "Big Data/Cassandra"};
    }


    @Override
    public String getMavenGroupId() {
        return "org.talend.components";
    }

    @Override
    public String getMavenArtifactId() {
        return "components-cassandra";
    }

    @Override
    public Property[] getReturnProperties() {
        return new Property[0];
    }
}
