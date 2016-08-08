package org.talend.components.cassandra.output;

import org.apache.commons.lang3.reflect.TypeLiteral;
import org.talend.components.api.properties.ComponentPropertiesImpl;
import org.talend.daikon.properties.presentation.Form;
import org.talend.daikon.properties.property.Property;

import java.util.List;

import static org.talend.daikon.properties.property.PropertyFactory.newInteger;
import static org.talend.daikon.properties.property.PropertyFactory.newProperty;

public class NetworkReplicaTable extends ComponentPropertiesImpl {
    /**
     * named constructor to be used is these properties are nested in other properties. Do not subclass this method for
     * initialization, use {@link #init()} instead.
     *
     * @param name
     */
    public NetworkReplicaTable(String name) {
        super(name);
    }

    private static final TypeLiteral<List<String>> LIST_STRING_TYPE = new TypeLiteral<List<String>>() {
    };

    private static final TypeLiteral<List<Integer>> LIST_INTEGER_TYPE = new TypeLiteral<List<Integer>>() {
    };

    public Property<List<String>> datacenterName = newProperty(LIST_STRING_TYPE, "datacenterName");

    public Property<List<Integer>> replicaNumber = newProperty(LIST_INTEGER_TYPE, "replicaNumber");

    @Override
    public void setupLayout(){
        super.setupLayout();
        Form mainForm = new Form(this, Form.MAIN);
        mainForm.addColumn(datacenterName);
        mainForm.addColumn(replicaNumber);
    }
}
