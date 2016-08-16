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
package org.talend.components.jdbc.wizard;

import static org.talend.daikon.properties.presentation.Widget.widget;
import static org.talend.daikon.properties.property.PropertyFactory.newProperty;

import java.util.List;

import org.apache.avro.Schema;
import org.apache.commons.lang3.reflect.TypeLiteral;
import org.talend.components.api.properties.ComponentPropertiesImpl;
import org.talend.components.jdbc.JDBCConnectionInfoProperties;
import org.talend.components.jdbc.module.JDBCConnectionModule;
import org.talend.components.jdbc.runtime.JDBCSourceOrSink;
import org.talend.daikon.NamedThing;
import org.talend.daikon.properties.Properties;
import org.talend.daikon.properties.ValidationResult;
import org.talend.daikon.properties.presentation.Form;
import org.talend.daikon.properties.presentation.Widget;
import org.talend.daikon.properties.property.Property;
import org.talend.daikon.properties.service.Repository;

public class JDBCModuleListWizardProperties extends ComponentPropertiesImpl implements JDBCConnectionInfoProperties {

    private JDBCConnectionModule connection;

    private String repositoryLocation;

    private List<NamedThing> moduleNames;

    public Property<List<NamedThing>> selectedModuleNames = newProperty(new TypeLiteral<List<NamedThing>>() {
    }, "selectedModuleNames");

    public JDBCModuleListWizardProperties(String name) {
        super(name);
    }

    public JDBCModuleListWizardProperties setConnection(JDBCConnectionModule connection) {
        this.connection = connection;
        return this;
    }

    public JDBCModuleListWizardProperties setRepositoryLocation(String location) {
        repositoryLocation = location;
        return this;
    }

    @Override
    public void setupLayout() {
        super.setupLayout();
        Form moduleForm = Form.create(this, Form.MAIN);
        moduleForm.addRow(widget(selectedModuleNames).setWidgetType(Widget.NAME_SELECTION_AREA_WIDGET_TYPE));
    }

    public void beforeFormPresentMain() throws Exception {
        JDBCSourceOrSink sourceOrSink = new JDBCSourceOrSink();
        sourceOrSink.initialize(null, this);
        moduleNames = sourceOrSink.getSchemaNames(null);
        selectedModuleNames.setPossibleValues(moduleNames);
        getForm(Form.MAIN).setAllowBack(true);
        getForm(Form.MAIN).setAllowFinish(true);
    }

    public ValidationResult afterFormFinishMain(Repository<Properties> repo) throws Exception {
        JDBCSourceOrSink sourceOrSink = new JDBCSourceOrSink();
        sourceOrSink.initialize(null, this);
        ValidationResult vr = sourceOrSink.validate(null);
        if (vr.getStatus() != ValidationResult.Result.OK) {
            return vr;
        }

        // TODO adjust the second parameter
        String connRepLocation = repo.storeProperties(connection, null, repositoryLocation, null);

        for (NamedThing nl : selectedModuleNames.getValue()) {
            String moduleId = nl.getName();
            Schema schema = sourceOrSink.getEndpointSchema(null, moduleId);
            /*
            SalesforceModuleProperties modProps = new SalesforceModuleProperties(moduleId);
            modProps.connection = connection;
            modProps.init();
            
            modProps.moduleName.setValue(moduleId);
            modProps.main.schema.setValue(schema);
            repo.storeProperties(modProps, nl.getName(), connRepLocation, "schema.schema");
            */
        }
        return ValidationResult.OK;
    }

    @Override
    public JDBCConnectionModule getJDBCConnectionModule() {
        return connection;
    }
}
