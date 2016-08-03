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
package org.talend.components.dropbox.tdropboxconnection;

import static org.talend.daikon.properties.presentation.Widget.widget;

import org.talend.components.api.properties.ComponentProperties;
import org.talend.components.api.properties.ComponentPropertiesImpl;
import org.talend.components.api.properties.ComponentReferenceProperties;
import org.talend.components.api.properties.ComponentReferencePropertiesEnclosing;
import org.talend.daikon.properties.Properties;
import org.talend.daikon.properties.presentation.Form;
import org.talend.daikon.properties.presentation.Widget;
import org.talend.daikon.properties.property.Property;
import org.talend.daikon.properties.property.PropertyFactory;

/**
 * {@link ComponentProperties} of Dropbox Connection component
 */
public class TDropboxConnectionProperties extends ComponentPropertiesImpl implements ComponentReferencePropertiesEnclosing {

    /**
     * Default value of Proxy Host property
     */
    private static final String DEFAULT_HOST = "127.0.0.1";

    /**
     * Default value of Proxy Port property
     */
    private static final int DEFAULT_PORT = 8087;

    /**
     * Referenced component property
     */
    public ComponentReferenceProperties referencedComponent = new ComponentReferenceProperties("referencedComponent", this);

    /**
     * Stores value of Dropbox Access Token 
     */
    public Property<String> accessToken = PropertyFactory.newString("accessToken");

    /**
     * Flag, which indicates whether to use HTTP Proxy
     */
    public Property<Boolean> useHttpProxy = PropertyFactory.newBoolean("useHttpProxy");

    /**
     * Proxy Host property
     */
    public Property<String> proxyHost = PropertyFactory.newString("proxyHost");

    /**
     * Proxy Port property
     */
    public Property<Integer> proxyPort = PropertyFactory.newInteger("proxyPort");

    /**
     * Constructor sets {@link Properties} name
     * 
     * @param name {@link Properties} name
     */
    public TDropboxConnectionProperties(String name) {
        super(name);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setupProperties() {
        super.setupProperties();
        referencedComponent.componentType.setValue(TDropboxConnectionDefinition.COMPONENT_NAME);
        accessToken.setValue("");
        proxyHost.setValue(DEFAULT_HOST);
        proxyPort.setValue(DEFAULT_PORT);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setupLayout() {
        super.setupLayout();
        Form mainForm = new Form(this, Form.MAIN);
        mainForm.addRow(accessToken);
        mainForm.addRow(useHttpProxy);
        mainForm.addRow(proxyHost);
        mainForm.addColumn(proxyPort);

        Form refForm = new Form(this, Form.REFERENCE);
        Widget componentListWidget = widget(referencedComponent).setWidgetType(Widget.COMPONENT_REFERENCE_WIDGET_TYPE);
        refForm.addRow(componentListWidget);
        refForm.addRow(mainForm);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void refreshLayout(Form form) {
        if (form.getName().equals(Form.MAIN)) {
            boolean useHttpProxyValue = useHttpProxy.getValue();
            if (useHttpProxyValue) {
                form.getWidget(proxyHost.getName()).setHidden(false);
                form.getWidget(proxyPort.getName()).setHidden(false);
            } else {
                form.getWidget(proxyHost.getName()).setHidden(true);
                form.getWidget(proxyPort.getName()).setHidden(true);
            }
        }

        if (form.getName().equals(Form.REFERENCE)) {
            String refComponentIdValue = referencedComponent.componentInstanceId.getValue();
            if (refComponentIdValue != null && refComponentIdValue.startsWith(TDropboxConnectionDefinition.COMPONENT_NAME)) {
                /*
                 * Main form widget is stored in widgetMap under properties.getName() key. Generally, this name is "root"
                 * See Form.getWidgetContentName() method for details
                 * that's why this.getName() is used here
                 */
                form.getWidget(getName()).setHidden(true);
            } else {
                form.getWidget(getName()).setHidden(false);
            }
        }
    }

    /**
     * Refreshes layout after Use HTTP Proxy checkbox is changed
     */
    public void afterUseHttpProxy() {
        refreshLayout(getForm(Form.MAIN));
    }

    /**
     * Refreshes layout after changes of referenced component property
     */
    @Override
    public void afterReferencedComponent() {
        refreshLayout(getForm(Form.REFERENCE));
    }

}
