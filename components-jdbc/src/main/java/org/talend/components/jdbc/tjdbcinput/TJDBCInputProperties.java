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

import org.talend.components.api.properties.ComponentPropertiesImpl;
import org.talend.components.common.SchemaProperties;
import org.talend.components.jdbc.DBProvideConnectionProperties;
import org.talend.components.jdbc.module.DataSourceModule;
import org.talend.components.jdbc.module.JDBCConnectionModule;
import org.talend.daikon.properties.presentation.Form;
import org.talend.daikon.properties.property.Property;
import org.talend.daikon.properties.property.PropertyFactory;

public class TJDBCInputProperties extends ComponentPropertiesImpl implements DBProvideConnectionProperties {

    public Property tablename = (Property) PropertyFactory.newString("tablename").setRequired(true);

    public SchemaProperties schema = new SchemaProperties("schema");

    public Property sql = PropertyFactory.newString("sql");

    public TJDBCInputProperties(String name) {
        super(name);
    }

    @Override
    public void setupLayout() {
        super.setupLayout();

        Form mainForm = getForm(Form.MAIN);
        mainForm.addRow(tablename);
        mainForm.addColumn(schema.getForm(Form.REFERENCE));
        mainForm.addRow(sql);
    }

    @Override
    public void setupProperties() {
        super.setupProperties();
    }

    @Override
    public void refreshLayout(Form form) {
        super.refreshLayout(form);
    }

    @Override
    public JDBCConnectionModule getJDBCConnectionModule() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public DataSourceModule getDataSourceModule() {
        // TODO Auto-generated method stub
        return null;
    }

}
