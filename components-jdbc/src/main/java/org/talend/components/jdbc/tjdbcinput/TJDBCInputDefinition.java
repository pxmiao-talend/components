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

import org.talend.components.api.Constants;
import org.talend.components.api.component.AbstractComponentDefinition;
import org.talend.components.api.component.ComponentDefinition;
import org.talend.components.api.component.InputComponentDefinition;
import org.talend.components.api.component.runtime.Source;
import org.talend.components.api.properties.ComponentProperties;
import org.talend.components.jdbc.tjdbcconnection.TJDBCConnectionProperties;
import org.talend.daikon.properties.property.Property;

import aQute.bnd.annotation.component.Component;

/**
 * Component that can connect to a db system and get some data out of it.
 */

@Component(name = Constants.COMPONENT_BEAN_PREFIX + TJDBCInputDefinition.COMPONENT_NAME, provide = ComponentDefinition.class)
public class TJDBCInputDefinition extends AbstractComponentDefinition implements InputComponentDefinition {

    public TJDBCInputDefinition(String componentName) {
        super(componentName);
    }

    public static final String COMPONENT_NAME = "tJDBCInput";

    @Override
    public Class<? extends ComponentProperties> getPropertyClass() {
        return TJDBCInputProperties.class;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Class<? extends ComponentProperties>[] getNestedCompatibleComponentPropertiesClass() {
        return new Class[] { TJDBCConnectionProperties.class, TJDBCInputProperties.class };
    }

    @Override
    public Source getRuntime() {
        return null;
    }

    @Override
    public String[] getFamilies() {
        return new String[] { "Databases/DB_JDBC" };
    }

    @Override
    public String getMavenGroupId() {
        return "org.talend.components";
    }

    @Override
    public String getMavenArtifactId() {
        return "components-jdbc";
    }

    @Override
    public Property[] getReturnProperties() {
        return null;
    }

}
