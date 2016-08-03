// ============================================================================
//
// Copyright (C) 2006-2015 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.components.jdbc.tjdbcinput;

import static org.talend.daikon.properties.presentation.Widget.widget;

import org.talend.components.api.properties.ComponentPropertiesImpl;
import org.talend.components.api.properties.ComponentReferenceProperties;
import org.talend.components.api.properties.ComponentReferencePropertiesEnclosing;
import org.talend.components.common.SchemaProperties;
import org.talend.components.jdbc.CommonUtils;
import org.talend.components.jdbc.JDBCConnectionInfoProperties;
import org.talend.components.jdbc.ReferAnotherComponent;
import org.talend.components.jdbc.module.JDBCConnectionModule;
import org.talend.components.jdbc.tjdbcconnection.TJDBCConnectionDefinition;
import org.talend.daikon.properties.presentation.Form;
import org.talend.daikon.properties.presentation.Widget;
import org.talend.daikon.properties.property.Property;
import org.talend.daikon.properties.property.PropertyFactory;

public class TJDBCInputProperties extends ComponentPropertiesImpl
        implements ComponentReferencePropertiesEnclosing, JDBCConnectionInfoProperties, ReferAnotherComponent {

    public TJDBCInputProperties(String name) {
        super(name);
    }

    // main
    public ComponentReferenceProperties referencedComponent = new ComponentReferenceProperties("referencedComponent", this);

    public JDBCConnectionModule connection = new JDBCConnectionModule("connection");

    public SchemaProperties schema = new SchemaProperties("schema");

    public Property<String> tablename = PropertyFactory.newString("tablename").setRequired(true);

    // TODO query type

    // TODO guess the query by the talend schema

    // TODO guess the talend schema by the query

    public Property<String> sql = PropertyFactory.newString("sql").setRequired(true);

    public Property<Boolean> useDataSource = PropertyFactory.newBoolean("useDataSource").setRequired();

    public Property<String> dataSource = PropertyFactory.newProperty("dataSource").setRequired();

    // advanced
    public Property<Boolean> useCursor = PropertyFactory.newBoolean("useCursor").setRequired();

    public Property<Integer> cursor = PropertyFactory.newInteger("cursor").setRequired();

    public Property<Boolean> trimStringOrCharColumns = PropertyFactory.newBoolean("trimStringOrCharColumns").setRequired();

    // TODO the tirm table

    // TODO enable mapping for dynamic

    @Override
    public void setupLayout() {
        super.setupLayout();

        Form mainForm = CommonUtils.addForm(this, Form.MAIN);

        Widget compListWidget = widget(referencedComponent).setWidgetType(Widget.COMPONENT_REFERENCE_WIDGET_TYPE);
        referencedComponent.componentType.setValue(TJDBCConnectionDefinition.COMPONENT_NAME);
        mainForm.addRow(compListWidget);

        mainForm.addRow(connection.getForm(Form.MAIN));
        mainForm.addRow(schema.getForm(Form.REFERENCE));

        mainForm.addRow(tablename);
        mainForm.addRow(sql);

        mainForm.addRow(useDataSource);
        mainForm.addRow(dataSource);

        Form advancedForm = CommonUtils.addForm(this, Form.ADVANCED);
        advancedForm.addRow(useCursor);
        advancedForm.addRow(cursor);
        advancedForm.addRow(trimStringOrCharColumns);
    }

    @Override
    public void setupProperties() {
        super.setupProperties();

        // TODO fix it later
        // sql.setValue("select id, name from employee");
    }

    @Override
    public void refreshLayout(Form form) {
        super.refreshLayout(form);

        String refComponentIdValue = referencedComponent.componentInstanceId.getStringValue();
        boolean useOtherConnection = refComponentIdValue != null
                && refComponentIdValue.startsWith(TJDBCConnectionDefinition.COMPONENT_NAME);

        if (form.getName().equals(Form.MAIN)) {
            form.getChildForm(connection.getName()).setHidden(useOtherConnection);
            if (useOtherConnection) {
                form.getWidget(useDataSource.getName()).setHidden(true);
                form.getWidget(dataSource.getName()).setHidden(true);
            } else {
                form.getWidget(useDataSource.getName()).setHidden(false);
                form.getWidget(dataSource.getName()).setHidden(!useDataSource.getValue());
            }
        }

        if (form.getName().equals(Form.ADVANCED)) {
            form.getWidget(cursor.getName()).setHidden(!useCursor.getValue());
        }
    }

    @Override
    public void afterReferencedComponent() {
        refreshLayout(getForm(Form.MAIN));
    }

    public void afterUseDataSource() {
        refreshLayout(getForm(Form.MAIN));
    }

    public void afterUseCursor() {
        refreshLayout(getForm(Form.ADVANCED));
    }

    @Override
    public JDBCConnectionModule getJDBCConnectionModule() {
        return connection;
    }

    @Override
    public String getReferencedComponentId() {
        return referencedComponent.componentInstanceId.getValue();
    }
}
