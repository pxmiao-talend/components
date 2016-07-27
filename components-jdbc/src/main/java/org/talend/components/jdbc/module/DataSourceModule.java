package org.talend.components.jdbc.module;

import org.talend.daikon.properties.PropertiesImpl;
import org.talend.daikon.properties.presentation.Form;
import org.talend.daikon.properties.property.Property;
import org.talend.daikon.properties.property.PropertyFactory;

public class DataSourceModule extends PropertiesImpl {

    public Property<Boolean> useDataSource = PropertyFactory.newBoolean("useDataSource").setRequired();

    public Property<String> dataSource = PropertyFactory.newProperty("dataSource").setRequired();

    public DataSourceModule(String name) {
        super(name);
    }

    @Override
    public void setupLayout() {
        super.setupLayout();

        Form form = Form.create(this, Form.MAIN);
        form.addRow(useDataSource);
        form.addRow(dataSource);
    }

    @Override
    public void refreshLayout(Form form) {
        super.refreshLayout(form);

        if (Form.MAIN.equals(form.getName())) {
            form.getWidget(dataSource.getName()).setHidden(!useDataSource.getValue());
        }
    }

}
