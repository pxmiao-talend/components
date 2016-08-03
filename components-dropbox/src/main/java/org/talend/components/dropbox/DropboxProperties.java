// ============================================================================
//
// Copyright (C) 2006-2016 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.components.dropbox;

import org.talend.components.api.properties.ComponentProperties;
import org.talend.components.common.FixedConnectorsComponentProperties;
import org.talend.components.dropbox.tdropboxconnection.TDropboxConnectionProperties;
import org.talend.daikon.properties.Properties;
import org.talend.daikon.properties.presentation.Form;
import org.talend.daikon.properties.property.Property;
import org.talend.daikon.properties.property.PropertyFactory;

/**
 * Common {@link ComponentProperties} for Dropbox components
 */
public abstract class DropboxProperties extends FixedConnectorsComponentProperties {

    /**
     * Dropbox connection properties
     */
    public TDropboxConnectionProperties connection = new TDropboxConnectionProperties("connection");

    /**
     * Path to file on Dropbox server
     */
    public Property<String> path = PropertyFactory.newString("path");

    /**
     * Constructor sets {@link Properties} name
     * 
     * @param name {@link Properties} name
     */
    public DropboxProperties(String name) {
        super(name);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setupProperties() {
        super.setupProperties();
        path.setValue("");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setupLayout() {
        super.setupLayout();
        Form form = new Form(this, Form.MAIN);
        form.addRow(connection.getForm(Form.REFERENCE));
        form.addRow(path);
    }

}
