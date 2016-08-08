package org.talend.components.cassandra.input;

import org.talend.components.cassandra.CassandraIOBasedProperties;
import org.talend.daikon.properties.presentation.Form;
import org.talend.daikon.properties.property.Property;

import static org.talend.daikon.properties.property.PropertyFactory.newString;


public class TCassandraInputProperties extends CassandraIOBasedProperties {
    /**
     * named constructor to be used is these properties are nested in other properties. Do not subclass this method for
     * initialization, use {@link #init()} instead.
     *
     * @param name
     */
    public TCassandraInputProperties(String name) {
        super(name);
    }

    public Property<String> query = newString("query");

    @Override
    public void setupLayout(){
        super.setupLayout();
        Form form = getForm(Form.MAIN);
        form.addRow(query);
    }

}
