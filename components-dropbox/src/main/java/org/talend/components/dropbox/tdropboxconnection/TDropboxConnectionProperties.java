package org.talend.components.dropbox.tdropboxconnection;

import org.talend.components.api.properties.ComponentProperties;
import org.talend.components.api.properties.ComponentPropertiesImpl;
import org.talend.components.api.properties.ComponentReferencePropertiesEnclosing;
import org.talend.daikon.properties.Properties;
import org.talend.daikon.properties.presentation.Form;
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
    }

    /**
     * Refreshes layout after Use HTTP Proxy checkbox is changed
     */
    public void afterUseHttpProxy() {
        refreshLayout(getForm(Form.MAIN));
    }

    @Override
    public void afterReferencedComponent() {
        // TODO Auto-generated method stub
    }

}
