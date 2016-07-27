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

import org.talend.components.api.properties.ComponentPropertiesImpl;
import org.talend.components.jdbc.CommonUtils;
import org.talend.components.jdbc.module.DataSourceModule;
import org.talend.components.jdbc.module.JDBCConnectionModule;
import org.talend.daikon.properties.presentation.Form;
import org.talend.daikon.properties.property.Property;
import org.talend.daikon.properties.property.PropertyFactory;

public class TJDBCConnectionProperties extends ComponentPropertiesImpl {

    public TJDBCConnectionProperties(String name) {
        super(name);
    }

    // main
    public JDBCConnectionModule connection = new JDBCConnectionModule("connection");

    public DataSourceModule dataSource = new DataSourceModule("dataSource");

    public Property<Boolean> shareConnection = PropertyFactory.newBoolean("shareConnection");

    public Property<String> sharedConnectionName = PropertyFactory.newString("sharedConnectionName");

    // advanced
    public Property<Boolean> useAutoCommit = PropertyFactory.newBoolean("useAutoCommit");

    public Property<Boolean> autocommit = PropertyFactory.newBoolean("autocommit");

    @Override
    public void setupProperties() {
        super.setupProperties();
        useAutoCommit.setValue(true);
    }

    @Override
    public void setupLayout() {
        super.setupLayout();

        Form mainForm = CommonUtils.addForm(this, Form.MAIN);

        mainForm.addRow(connection.getForm(Form.MAIN));
        mainForm.addRow(shareConnection);
        mainForm.addRow(sharedConnectionName);
        mainForm.addRow(dataSource.getForm(Form.MAIN));

        Form advancedForm = CommonUtils.addForm(this, Form.ADVANCED);
        advancedForm.addRow(useAutoCommit);
        advancedForm.addColumn(autocommit);
    }

    @Override
    public void refreshLayout(Form form) {
        super.refreshLayout(form);

        if (Form.ADVANCED.equals(form.getName())) {
            form.getWidget(autocommit.getName()).setHidden(!useAutoCommit.getValue());
        }

        if (Form.MAIN.equals(form.getName())) {
            if (shareConnection.getValue()) {
                form.getWidget(sharedConnectionName.getName()).setHidden(false);

                form.getChildForm(dataSource.getName()).getWidget(dataSource.useDataSource.getName()).setHidden(true);
                form.getChildForm(dataSource.getName()).getWidget(dataSource.dataSource.getName()).setHidden(true);
            } else {
                form.getWidget(sharedConnectionName.getName()).setHidden(true);

                form.getChildForm(dataSource.getName()).getWidget(dataSource.useDataSource.getName()).setHidden(false);
                form.getChildForm(dataSource.getName()).getWidget(dataSource.dataSource.getName())
                        .setHidden(!dataSource.useDataSource.getValue());
            }

            if (dataSource.useDataSource.getValue()) {
                form.getChildForm(dataSource.getName()).getWidget(dataSource.dataSource.getName()).setHidden(false);

                form.getWidget(shareConnection.getName()).setHidden(true);
                form.getWidget(sharedConnectionName.getName()).setHidden(true);
            } else {
                form.getChildForm(dataSource.getName()).getWidget(dataSource.dataSource.getName()).setHidden(true);

                form.getWidget(shareConnection.getName()).setHidden(false);
                form.getWidget(sharedConnectionName.getName()).setHidden(!shareConnection.getValue());
            }
        }
    }

}
