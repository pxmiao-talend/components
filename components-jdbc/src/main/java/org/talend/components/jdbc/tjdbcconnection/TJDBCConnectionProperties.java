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
package org.talend.components.jdbc.tjdbcconnection;

import static org.talend.daikon.properties.presentation.Widget.widget;

import org.talend.components.api.properties.ComponentPropertiesImpl;
import org.talend.components.api.properties.ComponentReferenceProperties;
import org.talend.components.api.properties.ComponentReferencePropertiesEnclosing;
import org.talend.components.jdbc.CommonUtils;
import org.talend.components.jdbc.module.DataSourceModule;
import org.talend.components.jdbc.module.JDBCConnectionModule;
import org.talend.daikon.properties.presentation.Form;
import org.talend.daikon.properties.presentation.Widget;
import org.talend.daikon.properties.property.Property;
import org.talend.daikon.properties.property.PropertyFactory;

public class TJDBCConnectionProperties extends ComponentPropertiesImpl implements ComponentReferencePropertiesEnclosing {

    public TJDBCConnectionProperties(String name) {
        super(name);
    }

    public ComponentReferenceProperties referencedComponent = new ComponentReferenceProperties("referencedComponent", this);

    public JDBCConnectionModule connection = new JDBCConnectionModule("connection");

    public DataSourceModule dataSource = new DataSourceModule("dataSource");

    public Property autocommit = PropertyFactory.newBoolean("autocommit");

    @Override
    public void setupLayout() {
        super.setupLayout();

        Form mainForm = CommonUtils.addForm(this, Form.MAIN);

        mainForm.addRow(host);
        mainForm.addColumn(port);

        mainForm.addRow(database);
        mainForm.addColumn(dbschema);

        mainForm.addRow(userPassword.getForm(Form.MAIN));

        mainForm.addRow(jdbcparameter);

        Form advancedForm = CommonUtils.addForm(this, Form.ADVANCED);
        advancedForm.addRow(autocommit);

        // only store it, will use it later
        Form refForm = CommonUtils.addForm(this, Form.REFERENCE);

        Widget compListWidget = widget(referencedComponent).setWidgetType(Widget.COMPONENT_REFERENCE_WIDGET_TYPE);
        referencedComponent.componentType.setValue(getReferencedComponentName());

        refForm.addRow(compListWidget);
        refForm.addRow(mainForm);

    }

    protected String getReferencedComponentName() {
        return null;
    }

    public String getReferencedComponentId() {
        return referencedComponent.componentInstanceId.getStringValue();
    }

    @Override
    public void afterReferencedComponent() {
        refreshLayout(getForm(Form.MAIN));
    }

    @Override
    public void refreshLayout(Form form) {
        super.refreshLayout(form);

        String refComponentIdValue = getReferencedComponentId();
        boolean useOtherConnection = refComponentIdValue != null && refComponentIdValue.startsWith(getReferencedComponentName());

        if (form.getName().equals(Form.MAIN)) {
            if (useOtherConnection) {
                form.getWidget(host.getName()).setHidden(true);
                form.getWidget(port.getName()).setHidden(true);
                form.getWidget(database.getName()).setHidden(true);
                form.getWidget(dbschema.getName()).setHidden(true);
                form.getWidget(userPassword.getName()).setHidden(true);
                form.getWidget(jdbcparameter.getName()).setHidden(true);
                return;
            }

            form.getWidget(host.getName()).setHidden(false);
            form.getWidget(port.getName()).setHidden(false);
            form.getWidget(database.getName()).setHidden(false);
            form.getWidget(dbschema.getName()).setHidden(false);
            form.getWidget(userPassword.getName()).setHidden(false);
            form.getWidget(jdbcparameter.getName()).setHidden(false);
        }
    }

}
