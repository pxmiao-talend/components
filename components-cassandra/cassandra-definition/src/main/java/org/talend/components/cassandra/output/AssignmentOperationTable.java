package org.talend.components.cassandra.output;

import org.apache.commons.lang3.reflect.TypeLiteral;
import org.talend.components.api.properties.ComponentPropertiesImpl;
import org.talend.daikon.properties.presentation.Form;
import org.talend.daikon.properties.property.Property;

import java.util.List;

import static org.talend.daikon.properties.property.PropertyFactory.newEnum;
import static org.talend.daikon.properties.property.PropertyFactory.newProperty;
import static org.talend.daikon.properties.property.PropertyFactory.newString;

public class AssignmentOperationTable extends ComponentPropertiesImpl {
    /**
     * named constructor to be used is these properties are nested in other properties. Do not subclass this method for
     * initialization, use {@link #init()} instead.
     *
     * @param name
     */
    public AssignmentOperationTable(String name) {
        super(name);
    }

    private static final TypeLiteral<List<String>> LIST_STRING_TYPE = new TypeLiteral<List<String>>() {
    };

    private static final TypeLiteral<List<Operation>> LIST_OPERATION_TYPE = new TypeLiteral<List<Operation>>() {
    };

    public Property<List<String>> columnName = newProperty(LIST_STRING_TYPE, "columnName");

    public enum Operation {
        Append,
        Prepend,
        Minus,
        Position_Or_Key
    }

    public Property<List<Operation>> operation = newProperty(LIST_OPERATION_TYPE, "operation");
    public Property<List<String>> keyColumn = newProperty(LIST_STRING_TYPE, "keyColumn");

    @Override
    public void setupLayout(){
        super.setupLayout();
        Form mainForm = new Form(this, Form.MAIN);
        mainForm.addColumn(columnName);
        mainForm.addColumn(operation);
        mainForm.addColumn(keyColumn);
    }
}
