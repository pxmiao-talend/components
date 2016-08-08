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
package org.talend.components.api.service.testdatastore;

import org.talend.components.api.Constants;
import org.talend.components.api.component.AbstractDatastoreDefinition;
import org.talend.components.api.component.DatastoreDefinition;
import org.talend.components.api.component.DatastoreImageType;
import org.talend.components.api.properties.ComponentProperties;
import org.talend.components.api.service.testcomponent.ComponentPropertiesWithDefinedI18N;
import org.talend.components.api.service.testcomponent.TestComponentProperties;
import org.talend.components.api.service.testcomponent.nestedprop.NestedComponentProperties;
import org.talend.components.api.service.testcomponent.nestedprop.inherited.InheritedComponentProperties;

import aQute.bnd.annotation.component.Component;

@Component(name = Constants.COMPONENT_BEAN_PREFIX + TestDatastoreDefinition.DATASTORE_NAME, provide = DatastoreDefinition.class)
public class TestDatastoreDefinition extends AbstractDatastoreDefinition implements DatastoreDefinition {

    public static final String DATASTORE_NAME = "TestDatastore"; //$NON-NLS-1$

    public TestDatastoreDefinition() {
        super(DATASTORE_NAME);
    }

    protected TestComponentProperties properties;

    @Override
    public String[] getFamilies() {
        return new String[] { "level1/level2", "newlevel1/newlevel2" };
    }

    @Override
    public String getPngImagePath(DatastoreImageType imageType) {
        return "testCompIcon_32x32.png";
    }

    @Override
    public Class<? extends ComponentProperties> getPropertyClass() {
        return TestDatastoreProperties.class;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Class<? extends ComponentProperties>[] getNestedCompatibleComponentPropertiesClass() {
        return new Class[] { NestedComponentProperties.class, ComponentPropertiesWithDefinedI18N.class,
                InheritedComponentProperties.class };
    }

    @Override
    public String getMavenGroupId() {
        return "org.talend.datastores.api.test";
    }

    @Override
    public String getMavenArtifactId() {
        return "test-datastores";
    }

    @Override
    public String[] getDatasets() {
        return new String[] {};
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.components.api.component.DatastoreDefinition#getJSONSchema()
     */
    @Override
    public String getJSONSchema() {
        return "";
    }

}
