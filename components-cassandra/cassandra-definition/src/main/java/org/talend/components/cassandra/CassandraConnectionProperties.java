package org.talend.components.cassandra;

import org.talend.components.api.properties.*;
import org.talend.components.cassandra.connection.TCassandraConnectionDefinition;
import org.talend.components.common.UserPasswordProperties;
import org.talend.daikon.properties.PresentationItem;
import org.talend.daikon.properties.presentation.Form;
import org.talend.daikon.properties.presentation.Widget;
import org.talend.daikon.properties.property.Property;

import static org.talend.daikon.properties.presentation.Widget.widget;
import static org.talend.daikon.properties.property.PropertyFactory.newBoolean;
import static org.talend.daikon.properties.property.PropertyFactory.newEnum;
import static org.talend.daikon.properties.property.PropertyFactory.newString;

public class CassandraConnectionProperties extends ComponentPropertiesImpl implements ComponentReferencePropertiesEnclosing, ConnectionPropertiesProvider<CassandraConnectionProperties> {
    /**
     * named constructor to be used is these properties are nested in other properties. Do not subclass this method for
     * initialization, use {@link #init()} instead.
     *
     * @param name
     */
    public CassandraConnectionProperties(String name) {
        super(name);
    }

    public enum CassandraVersion {
        V_3_0,
        V_2_0
    }

    public Property<CassandraVersion> version = newEnum("version", CassandraVersion.class).setRequired();

    public Property<String> host = newString("host", "localhost").setRequired();

    public Property<String> port = newString("port", "9042").setRequired();

    public Property<Boolean> needAuth = newBoolean("needAuth", false);

    public UserPasswordProperties userPassword = new UserPasswordProperties("userPassword");

    public PresentationItem testConnection = new PresentationItem("testConnection", "Test connection");

    public ComponentReferenceProperties referencedComponent = new ComponentReferenceProperties("referencedComponent", this);

    @Override
    public void setupLayout() {
        super.setupLayout();

        Form wizardForm = new Form(this, "Wizard");
        wizardForm.addRow((Property) newString("name").setRequired());
        wizardForm.addRow(widget(version).setDeemphasize(true));
        wizardForm.addRow(host);
        wizardForm.addColumn(port);
        wizardForm.addRow(needAuth);
        wizardForm.addRow(userPassword.getForm(Form.MAIN));
        wizardForm.addColumn(widget(testConnection).setLongRunning(true).setWidgetType(Widget.BUTTON_WIDGET_TYPE));

        Form mainForm = new Form(this, Form.MAIN);
        mainForm.addRow(version);
        mainForm.addRow(host);
        mainForm.addColumn(port);
        mainForm.addRow(needAuth);
        mainForm.addRow(userPassword.getForm(Form.MAIN));

        Form mainAndRefForm = new Form(this, Form.REFERENCE);
        Widget compListWidget = widget(referencedComponent).setWidgetType(Widget.COMPONENT_REFERENCE_WIDGET_TYPE);
        referencedComponent.componentType.setValue(TCassandraConnectionDefinition.COMPONENT_NAME);
        mainAndRefForm.addRow(compListWidget);
        mainAndRefForm.addRow(mainForm);
    }

    @Override
    public void afterReferencedComponent() {
        refreshLayout(getForm(Form.MAIN));
        refreshLayout(getForm(Form.REFERENCE));
    }

    public String getReferencedComponentId() {
        return referencedComponent.componentInstanceId.getStringValue();
    }

    public CassandraConnectionProperties getReferencedConnectionProperties() {
        CassandraConnectionProperties refProps = (CassandraConnectionProperties) referencedComponent.componentProperties;
        if (refProps != null)
            return refProps;
        return null;
    }

    public void afterNeedAuth() {
        refreshLayout(getForm(Form.MAIN));
        refreshLayout(getForm("Wizard"));
    }

    //TODO implement it after validateConnection method
    //    public ValidationResult validateTestConnection() throws Exception{
    //    }

    @Override
    public void refreshLayout(Form form) {
        super.refreshLayout(form);

        String refComponentIdValue = getReferencedComponentId();
        boolean useOtherConnection = refComponentIdValue != null && refComponentIdValue.startsWith(TCassandraConnectionDefinition.COMPONENT_NAME);
        if (form.getName().equals(Form.MAIN) || form.getName().equals("Wizard")) {
            if (useOtherConnection) {
                form.getWidget(version.getName()).setHidden(true);
                form.getWidget(host.getName()).setHidden(true);
                form.getWidget(port.getName()).setHidden(true);
                form.getWidget(needAuth.getName()).setHidden(true);
                form.getWidget(userPassword.getName()).setHidden(true);
            } else {
                form.getWidget(version.getName()).setHidden(false);
                form.getWidget(host.getName()).setHidden(false);
                form.getWidget(port.getName()).setHidden(false);
                if (needAuth.getValue()) {
                    form.getWidget(userPassword.getName()).setHidden(false);
                } else {
                    form.getWidget(userPassword.getName()).setHidden(true);
                }
            }
        }
    }

    @Override
    public CassandraConnectionProperties getConnectionProperties() {
        return this;
    }
}
