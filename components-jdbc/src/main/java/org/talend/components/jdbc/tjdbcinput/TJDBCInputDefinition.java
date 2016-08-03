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
import org.talend.components.jdbc.runtime.JDBCSource;
import org.talend.daikon.properties.property.Property;
import org.talend.daikon.properties.property.PropertyFactory;

import aQute.bnd.annotation.component.Component;

@Component(name = Constants.COMPONENT_BEAN_PREFIX + TJDBCInputDefinition.COMPONENT_NAME, provide = ComponentDefinition.class)
public class TJDBCInputDefinition extends AbstractComponentDefinition implements InputComponentDefinition {

    public static final String COMPONENT_NAME = "tJDBCInputNew";

    public TJDBCInputDefinition() {
        super(COMPONENT_NAME);
    }

    @Override
    public Class<? extends ComponentProperties> getPropertyClass() {
        return TJDBCInputProperties.class;
    }

    // TODO it is useful now?
    @SuppressWarnings("unchecked")
    @Override
    public Class<? extends ComponentProperties>[] getNestedCompatibleComponentPropertiesClass() {
        return new Class[] { TJDBCInputProperties.class };
    }

    @Override
    public Source getRuntime() {
        return new JDBCSource();
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

    static final Property<String> RETURN_QUERY_PROP = PropertyFactory.newString(RETURN_ERROR_MESSAGE);

    @Override
    public Property[] getReturnProperties() {
        return new Property[] { RETURN_ERROR_MESSAGE_PROP, RETURN_TOTAL_RECORD_COUNT_PROP, RETURN_QUERY_PROP };
    }

}
