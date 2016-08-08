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
package org.talend.components.api.service.testdataset;

import org.talend.components.api.Constants;
import org.talend.components.api.component.AbstractDatasetDefinition;
import org.talend.components.api.component.DatasetDefinition;
import org.talend.components.api.component.DatasetImageType;
import org.talend.components.api.properties.ComponentProperties;
import org.talend.components.api.service.testcomponent.ComponentPropertiesWithDefinedI18N;
import org.talend.components.api.service.testcomponent.TestComponentProperties;
import org.talend.components.api.service.testcomponent.nestedprop.NestedComponentProperties;
import org.talend.components.api.service.testcomponent.nestedprop.inherited.InheritedComponentProperties;

import aQute.bnd.annotation.component.Component;

@Component(name = Constants.COMPONENT_BEAN_PREFIX + TestDatasetDefinition.DATASET_NAME, provide = DatasetDefinition.class)
public class TestDatasetDefinition extends AbstractDatasetDefinition implements DatasetDefinition {

    public static final String DATASET_NAME = "TestDataset"; //$NON-NLS-1$

    public TestDatasetDefinition() {
        super(DATASET_NAME);
    }

    protected TestComponentProperties properties;

    @Override
    public String[] getFamilies() {
        return new String[] { "level1/level2", "newlevel1/newlevel2" };
    }

    @Override
    public String getPngImagePath(DatasetImageType imageType) {
        return "testCompIcon_32x32.png";
    }

    @Override
    public Class<? extends ComponentProperties> getPropertyClass() {
        return TestDatasetProperties.class;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Class<? extends ComponentProperties>[] getNestedCompatibleComponentPropertiesClass() {
        return new Class[] { NestedComponentProperties.class, ComponentPropertiesWithDefinedI18N.class,
                InheritedComponentProperties.class };
    }

    @Override
    public String getMavenGroupId() {
        return "org.talend.components.api.test";
    }

    @Override
    public String getMavenArtifactId() {
        return "test-datasets";
    }

    @Override
    public String[] getComponents() {
        return new String[] { "testComponent1", "testComponent2", "testComponent3" };
    }

    @Override
    public String getJSONSchema() {
        return "{\"testJSON\":\"true\"}";
    }

    @Override
    public Object[] getSample(Integer size) {
        return null;
    }

    @Override
    public String getSchema() {
        return null;
    }

}
