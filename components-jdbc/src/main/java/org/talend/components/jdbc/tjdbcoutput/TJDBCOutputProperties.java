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
package org.talend.components.jdbc.tjdbcoutput;

import static org.talend.daikon.properties.presentation.Widget.widget;

import org.talend.components.api.properties.ComponentPropertiesImpl;
import org.talend.components.api.properties.ComponentReferenceProperties;
import org.talend.components.api.properties.ComponentReferencePropertiesEnclosing;
import org.talend.components.common.SchemaProperties;
import org.talend.components.jdbc.CommonUtils;
import org.talend.components.jdbc.module.JDBCConnectionModule;
import org.talend.components.jdbc.tjdbcconnection.TJDBCConnectionDefinition;
import org.talend.daikon.properties.presentation.Form;
import org.talend.daikon.properties.presentation.Widget;
import org.talend.daikon.properties.property.Property;
import org.talend.daikon.properties.property.PropertyFactory;

public class TJDBCOutputProperties extends ComponentPropertiesImpl implements ComponentReferencePropertiesEnclosing {

    public TJDBCOutputProperties(String name) {
        super(name);
    }

    // main
    public ComponentReferenceProperties referencedComponent = new ComponentReferenceProperties("referencedComponent", this);

    public JDBCConnectionModule connection = new JDBCConnectionModule("connection");

    public Property<String> tablename = PropertyFactory.newString("tablename").setRequired(true);

    public enum DataAction {
        Insert,
        Update,
        InsertOrUpdate,
        UpdateOrInsert,
        Delete
    }

    public Property<DataAction> dataAction = PropertyFactory.newEnum("dataAction", DataAction.class).setRequired();

    public Property<Boolean> clearDataInTable = PropertyFactory.newBoolean("clearDataInTable").setRequired();

    public SchemaProperties schema = new SchemaProperties("schema");

    public Property<Boolean> dieOnError = PropertyFactory.newBoolean("dieOnError").setRequired();

    public Property<Boolean> useDataSource = PropertyFactory.newBoolean("useDataSource").setRequired();

    public Property<String> dataSource = PropertyFactory.newProperty("dataSource").setRequired();

    // advanced
    public Property<Integer> commitEvery = PropertyFactory.newInteger("commitEvery").setRequired();

    // TODO additional columns

    // TODO use field options and table

    public Property<Boolean> debug = PropertyFactory.newBoolean("debug").setRequired();

    public Property<Boolean> useBatch = PropertyFactory.newBoolean("useBatch").setRequired();

    public Property<Integer> batchSize = PropertyFactory.newInteger("batchSize").setRequired();

    @Override
    public void setupLayout() {
        super.setupLayout();

        Form mainForm = CommonUtils.addForm(this, Form.MAIN);

        Widget compListWidget = widget(referencedComponent).setWidgetType(Widget.COMPONENT_REFERENCE_WIDGET_TYPE);
        referencedComponent.componentType.setValue(TJDBCConnectionDefinition.COMPONENT_NAME);
        mainForm.addRow(compListWidget);

        mainForm.addRow(connection.getForm(Form.MAIN));
        mainForm.addRow(tablename);

        mainForm.addRow(dataAction);
        mainForm.addRow(clearDataInTable);

        mainForm.addRow(schema.getForm(Form.REFERENCE));

        mainForm.addRow(dieOnError);

        mainForm.addRow(useDataSource);
        mainForm.addRow(dataSource);

        Form advancedForm = CommonUtils.addForm(this, Form.ADVANCED);
        advancedForm.addRow(commitEvery);
        advancedForm.addRow(debug);
        advancedForm.addRow(useBatch);
        advancedForm.addRow(batchSize);
    }

    @Override
    public void setupProperties() {
        super.setupProperties();

        dataAction.setValue(DataAction.Insert);

        commitEvery.setValue(10000);
        batchSize.setValue(10000);
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
            form.getWidget(batchSize.getName()).setHidden(!useBatch.getValue());
            form.getWidget(commitEvery.getName()).setHidden(useOtherConnection);
        }
    }

    @Override
    public void afterReferencedComponent() {
        refreshLayout(getForm(Form.MAIN));
    }

    public void afterUseDataSource() {
        refreshLayout(getForm(Form.MAIN));
    }

    public void afterUseBatch() {
        refreshLayout(getForm(Form.ADVANCED));
    }
}
