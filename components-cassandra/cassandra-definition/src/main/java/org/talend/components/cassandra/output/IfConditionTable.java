package org.talend.components.cassandra.output;

import org.apache.commons.lang3.reflect.TypeLiteral;
import org.talend.components.api.properties.ComponentPropertiesImpl;
import org.talend.daikon.properties.presentation.Form;
import org.talend.daikon.properties.property.Property;

import java.util.List;

import static org.talend.daikon.properties.property.PropertyFactory.newProperty;
import static org.talend.daikon.properties.property.PropertyFactory.newString;

public class IfConditionTable extends ComponentPropertiesImpl {
    /**
     * named constructor to be used is these properties are nested in other properties. Do not subclass this method for
     * initialization, use {@link #init()} instead.
     *
     * @param name
     */
    public IfConditionTable(String name) {
        super(name);
    }

    private static final TypeLiteral<List<String>> LIST_STRING_TYPE = new TypeLiteral<List<String>>() {
    };

    public Property<List<String>> columnName = newProperty(LIST_STRING_TYPE, "columnName");

    @Override
    public void setupLayout(){
        super.setupLayout();
        Form mainForm = new Form(this, Form.MAIN);
        mainForm.addColumn(columnName);
    }
}

